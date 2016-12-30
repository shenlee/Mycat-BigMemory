package io.mycat.bigmem.buffer;
import java.nio.ByteBuffer;

import javax.imageio.stream.MemoryCacheImageOutputStream;

import sun.nio.ch.DirectBuffer;

/**
*@desc:
*@author: zhangwy   @date: 2016年12月28日 上午6:51:16
**/
public class Chunk<T> {
	private final Arena<T> arena;
	private final int pageSize;
	private final int maxOrder;
	private final int pageShift;
	private final int log2ChunkSize;
	private final int chunkSize;
	private final byte unusable;
	//容器用来存放数据的地方.
	private T memory;
	private final int maskSubpage;
	
	private final int maxSubpageAllocs;
	private final Subpage<T>[] subpagesList;
	
	private final byte[] memoryMap;
	private final byte[] depth;
	
	private int freeBytes;
	//链表使用
	ChunkList<T> parent;
	Chunk<T> prev;
	Chunk<T> next;
	
	
	public Chunk(Arena<T> arena,T memory, int chunkSize, int pageSize,
			int maxOrder) {
		this.arena = arena;
		this.memory = memory;
		this.pageSize = pageSize;
		this.maxOrder = maxOrder;
		this.pageShift = log2(pageSize);
		this.log2ChunkSize = log2(chunkSize);
		this.chunkSize = chunkSize;
		this.unusable = (byte) (maxOrder + 1);
		this.maxSubpageAllocs = (1 << maxOrder);
		this.maskSubpage = ~(pageSize -1);
		subpagesList = newSubpages(this.maxSubpageAllocs);
		memoryMap = new byte[maxSubpageAllocs << 1];
		depth = new byte[maxSubpageAllocs << 1];
		int id = 1 ;
		for(int d = 0; d <= maxOrder; d++) {
			int len = 1 << d;
			for(int p = 0 ; p < len; p++) {
				memoryMap[id] = (byte) d;
				depth[id] = (byte) d;
				id++;
			}
		}
		freeBytes = chunkSize;
	}
	
	public long allocate(int normalSize) {
		if((normalSize & maskSubpage) != 0) {
			return allocateRun(normalSize);
		} else {
			return allocateSubpage(normalSize);
		}
	}
	//分配大于等于一个节点的(>=pageSize)
	private long allocateRun(int normalSize) {
		int d = maxOrder - (log2(normalSize) - pageShift) ;
		int memoryMapId = allocateNode(d);
		return memoryMapId;
	}
	
	//分配小于等于一个pageSize(<pageSize)
	private long allocateSubpage(int normalSize) {
		int memoryMapId = allocateNode(maxOrder);
		if(memoryMapId < 0) {
			return memoryMapId;
		}
		Subpage<T> subpage = subpagesList[subpageId(memoryMapId)];
		if(subpage == null) {
			subpage = new Subpage(this, memoryMapId, pageSize, normalSize);
		} else {
			subpage.initSubpage(normalSize);
		}
		return memoryMapId;
	}
	public void initBuf(BaseByteBuffer<T> byteBuffer ,long handle, int normalSize ) {
		
	}
	/**
	*@desc:
	*完全二叉树的跟节点最小,儿子节点大.
	*@return: int
	*@auth: zhangwy @date: 2016年12月29日 下午8:45:29
	**/
	private int allocateNode(int d) {
		int id = 1;
		byte val = value(id);
		//val需要小于等于d才说明能够被分配.
		if(d < val) {
			return -1;
		}
		//
		int targetLevel = 1 << d;
		//找到儿子中可分配的,并且到达了第d层了
		while(d < val || id < targetLevel ) {
			id = id << 1;
			val = value(id);
			//判断如果左儿子不满足分配,则使用右儿子进行分配
			if(d < val) {
				id ^= 1;
				val = value(id);
			}
		}
		
		//设置当前节点已经被使用
		setValue(id, unusable);
		//更新父亲节点的使用状态
		updateParentAlloc(id);
		freeBytes -= runLength(id);
		return id;
	}

	/**
	*@desc:
	*@return: void
	*@auth: zhangwy @date: 2016年12月29日 下午8:57:59
	**/
	private void updateParentAlloc(int id) {
		int parentId = id ;  ///除以2取得父亲节点
		while(parentId > 1) {
			byte leftValue = value(id);
			byte rightValue =  value(id ^ 1);
			parentId = id >>> 1;
			setValue(parentId, leftValue < rightValue ?leftValue : rightValue);
			id = parentId;
		}
	}
	
	private void updateParentfree(int id) {
		int parentId = id ;  ///除以2取得父亲节点
		while(parentId > 1) {
			byte leftValue = value(id);
			byte rightValue =  value(id ^ 1);
			parentId = id >>> 1;
			byte dep = depth[id];
			if(dep == leftValue && dep == rightValue) {
				setValue(parentId, (byte)(dep - 1));
			} else {
				setValue(parentId, leftValue < rightValue ?leftValue : rightValue);
			}
			id = parentId;
		}
	}
	private void free(int memoryMapId) {

		//需要释放subpage 代码为实现
		
		setValue(memoryMapId, depth[memoryMapId]);
		freeBytes += runLength(memoryMapId);
		updateParentfree(memoryMapId);
	}
	
	private byte value(int memoryMapId) {
		return memoryMap[memoryMapId];
	}
	
	private void setValue(int memoryMapId,byte value) {
		memoryMap[memoryMapId] = value;
	} 
	

	/*    获取当前层数的偏移量然后 * 当前层数一个节点所管理的大小*/
	private long runOffset(int memoryMapId) {
		int nodeOffset = memoryMapId ^ (1 << depth[memoryMapId]);
		System.out.println("nodeOffset " + nodeOffset);
		return  nodeOffset * runLength(memoryMapId);
	}
	
	/*当前节点管理的大小*/
	private int runLength(int memoryMapId) {
        // represents the size in #bytes supported by node 'id' in the tree
        return 1 << log2ChunkSize -depth[memoryMapId];
    }
	
	/*最后一层memoryMapId对应的subpageId是多少,移除最高位.*/
	private int subpageId(int memoryMapId) {
		return memoryMapId ^ maxSubpageAllocs;
	}
	
	@SuppressWarnings("unchecked")
	private Subpage<T>[] newSubpages(int num) {
		return new Subpage[num];
	}

	static int log2(int value) {
		return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(value);
	}
	
	public int usage() {
		
		int usePercent = 0;
		if(freeBytes == 0) {
			return 100;
		}
		usePercent = (chunkSize - freeBytes) * 100 / chunkSize;
		if(usePercent == 0) {
			return 99;
		}
		return usePercent;
	}
	public static void main(String[] args) {
		System.out.println(log2(8192));
		int pageSize = 1024;
		int maxOrder = 3;
		int chunkSize = (1 << maxOrder) * pageSize;
		System.out.println(chunkSize);

		Chunk<DirectBuffer> chunk = new Chunk<DirectBuffer>(
				new Arena<DirectBuffer>(), (DirectBuffer)ByteBuffer.allocateDirect(chunkSize) ,chunkSize, pageSize,maxOrder);
		System.out.println(chunk.allocateRun(1024));
		System.out.println("======" + chunk.usage());

	}
	
}

