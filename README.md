# MycatBigMemory

**MycatBigMemory**仿造Netty中PooledDirectByteBuf的内存池管理策略，并对大于Chunk Size的内存请求管理进行扩充，以支持对BigMemory的管理。
**与https://github.com/orgs/MyCATApache的关系**：本repository作为MycatBigMemory实现的候选者之一，供开发人员交流。

[TOC]

##内存分配策略
针对小于ChunkSize（默认8K*2048）的内存请求，MycatBigMemory的内存分配策略与Netty一致，将用户的内存请求值转换成相应的normCapacity：

- **tiny**：将小于512字节的内存请求值对应到区间[16，512)内，即内存分配最小为16，不满足则每次增加16，直到512，一共有32个不同normCapacity值（16，32，48，64，80，.... , 496）。
- **small**：用于分配大于等于512字节但是小于pageSize的内存，内存分配最小为512，不满足则每次*2，一共有4个不同normCapacity值(512, 1024, 2048,  4096)。
- **large**：用于分配大于等于pageSize但是小于chunkSize的内存，normCapacity值最小为8K，不满足则以2倍进行扩充，一直到chunkSize边界。
- **huge**:用于分配大于等于chunkSize的内存，当内存请求值小于等于1/2ArenaSize,则采用chunk[]+memoryIdx方式记录占用的内存，除chunk[length-1]之外，都是被整块占用的chunk，而chunk[length-1]对应占用了一部分的memoryIdx值。
- **superHuge**:当内存请求值大于1/2ArenaSize，则直接另外分配堆外内存，不占用Arena内的空间, 但是Arena会负责对其进行回收和swap。
  
## 内存管理单元
###Subpage
管理策略等同于io.netty.buffer.PoolSubpage，本段可以认为是对PoolSubpage的解析。
	
####内部元素
- **pageSize**：Subpage的字节长度，大小为$2^n$，默认为8K
- **elemSize，maxNumElems**:为了更好的利用Subpage内的空间，将等分为更小的内存段，每小段的长度就是elemSize。因此maxNumElems  = pageSize / elemSize
|elem|elem|elem|...|...|elem|elem|
| :-------- | --------:| :--: |

- **long[] bitmap**：使用bit来记录Subpage内每个element内存段的占用情况。一个long可以描述$2^6$即64个内存段,因此bitmap初始化如下
```java
bitmapLength = maxNumElems >>> 6
for (int i = 0; i < bitmapLength; i ++) {
       bitmap[i] = 0;
}
```
- **prev， next**：使用双向链表来关联相同elemSize的Subpage。
- **chunk, memoryMapIdx, runOffset**：分别标识该SubPage所属的Chunk，以及在该Chunk中的memoryMapIdx(Chunk章节中会有详细描述)，和Chunk实际内存地址上的(chunk.memory)的偏移量
####bitmap的分配方式
```java
    private int findNextAvail() 
    {
        final long[] bitmap = this.bitmap;
        final int bitmapLength = this.bitmapLength;
        for (int i = 0; i < bitmapLength; i ++) {
            long bits = bitmap[i];
            if (~bits != 0) { 
            //该bits中有空闲位
            //从左向右遍历bits，找到为0的空闲位,返回格式为: i<<64|空闲位bit值
                return findNextAvail0(i, bits);
            }
        }
        return -1;
    }
```

###Chunk
管理策略等同于io.netty.buffer.PoolChunk，本段可以认为是对PoolChunk的解析。
####基本描述
- 一个page是chunk能够分配的最小内存单位。
- chunk是pages的集合。
- chunkSize = 2^{maxOrder} * pageSize
- chunk是有实际物理内存分配的，并且在初始化时候已经分配好,堆外就是使用UNSAFE.allocateMemory(chunkSize)

####byte[] memoryMap
- 使用一个存放在byte数组中的平衡二叉树来标识chunk内的内存占用情况，并且此二叉树可以用来定位查找以及判断chunk是否能够满足需要申请的内存大小。

		depth0                             node1
									      /     \
		depth1                       node2        node3            
							   	     / \            / \
		depth2                    node4  node5    node6 node7
		......                            ...... ......
		......                            ...... ......
								  /
	    depth9                node512 
	                         /       \
	    depth10      node1024         node1025
				        / \               / \
	    depth11  node2048 node2049 node2050 node2051 .... node4195
- **depth=maxOrder**是树的叶节点，用于对应pages，如图depth11（maxOrder默认为11）可以记录2048个page
即$chunkSize/2^{maxOrder} = pageSize$
- 当需要申请分配一个chunkSize/2^k大小的内存段，只需要在depth_k从左向右寻找第一个可用的节点。例如k=1，则分配1/2*chunkSize，立此类推。
- **memoryMap[node_id] = x** ，
1) x = depth_of_node_id => memoryMap初始化值或未分配时 
2) x > depth_of_node_id => 至少该节点下的一个子节点已经被分配了，所以不能在本节点上分配对应大小的内存段，需要向右继续寻找合适的同级兄弟节点进行分配，但是不妨碍该节点下的字节点响应对应大小的内存分配请求。
例如：node2048(8k)上已被分配，node2049～node4195未被分配， 则node1024无法响应16k的分配请求，需要在node1025上分配，但是node1024上仍可以响应8k的分配请求。
 3) x = maxOrder + 1 => 这个节点和其下字节点都已经被分配完毕。



####分配算法
  -  **allocateNode(d)** => 在高度h上从左向右查找第一个可用的节点 
 1) 从 depth = 0 (node_id = 1)开始遍历
 如果memoryMap[ 1 ]>d, 则说明整个chunk都无法分配。
 如果memoryMap[node_id] < d,  则说明d不在此层，通过 node_id <<= 1 向下一层寻找。 
 2) 如果memoryMap[node_id] > depth_of_node_id，参照定义， 本节点无法满足分配要求， 需要在同级兄弟节点上继续查找。
 3) 如果memoryMap[node_id] <= h，则子节点或者本级叶节点可以找到可分配的节点。
 4)节点node_id分配完成后， 需要将memoryMap[node_id] 设置为 maxOrder + 1，同时递归更新父节点。
 5)父节点更新方法为将memoryMap[parent_node_id]设置成左右子节点中值最小的那个。
 
  -  **allocateRun(size)**=> 用于分配>=pageSize的内存请求，size已经是经过处理的normCapacity，可以保证(详见后续“内存分配策略”中的描述)是pageSize的$2^n$倍。
  
 1) Compute d = $log_2(chunkSize/size)$
 2) Return allocateNode(d)
  
  -  **allocateSubpage(size)** => 用于分配<pageSize的内存请求，size已经是经过处理的normCapacity
 1) 使用allocateNode(maxOrder)找出一个未使用的叶子节点。
 2) 根据normCapacity构造和初始化Subpage对象实例，并放入内部属性PoolSubpage<T>[] subpages中，对应的叶子节点的node_id即是其下标值，便于后续方便查找。
 

###PoolChunkList
PoolChunkList维护了两个链表，一个是PoolChunkList之间的链表，一个是PoolChunkList内部PoolChunk的链表。
- **PoolChunkList之间的链表**：根据minUsage和maxUsage定义的使用量进行划分为qInit，q000，q025，q050，q075, q100六个不同的区间，内存分配优先级为q050=>q025=>q000=>qInit=>q075。两个相邻的chunkList，前一个的maxUsage和后一个的minUsage必须有一段交叉值进行缓冲，否则会出现某个chunk的usage处于临界值，而导致不停的在两个chunk间移动。
- **PoolChunkList内部PoolChunk链表**内存分配的时候，从内部PoolChunk链表head开始，尝试分配所需内存，如果分配失败，则尝试next chunk，不断循环直到成功。分配成功后，判断当前已分配的总使用量是否超过maxUsage， 若超过， 则将此chunk移动到前一个PoolChunkList链表中。

###Arena
 Arena负责管理Arena内所有被分配的内存。

 与Netty不同， 当前Arena不实现与线程的绑定，以及Cache。
####针对tiny的内存管理
 使用Subpage[]来记录所有已分配tiny大小内存的Subpages，数组下标为normCapacity>>>4,也就是length=32，里面存放的是对应Subpage.head。

####针对small的内存管理
 使用Subpage[]来记录所有已分配small大小内存的Subpages，数组length=4 (参见“内存分配策略”)，里面存放的是对应Subpage.head。

####针对large的内存管理
 使用qInit，q000，q025，q050，q075，q100这5个PoolChunkList按照使用率来记录所有已分配的Chunk清单。

####针对huge的内存管理
 小于1/2ArenaSize的huge内存同large的内存管理方式。
 
####针对superHuge的内存管理
 大于1/2ArenaSize的huge内存，直接使用MycatBigMemoryBuf[] superHuge来存放引用。

####内存分配锁
 
 
##MycatBigMemoryAllocator
内存分配／回收器， 支持回收策略定制。
待补充。

##MycatBigMemoryByteBuf
java.nio.ByteBuffer的重新实现，创建方式为Unsafe Direct方式。
####内部元素
  -  **Chunk<T> chunk[]** : 所包含的chunks引用。如果length>1,则说明此ByteBuff有多个离散的(也可能是连续的)chunk内存空间组成。在put/get时需要计算好偏移量。
  -  **memory， offset** :chunk[length-1]里，此MycatBigMemoryByteBuf包含的空间，即chunk内存首地址和偏移量。


##swap
swap到FileChannel或者其他Channel的能力，待开发。

##碎片整理
需求待收集。