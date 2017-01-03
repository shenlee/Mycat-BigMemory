package io.mycat.ipc;

/**
 * 获取当前是这一个节点管理的大小
 * private int runLength(int id) {
        // represents the size in #bytes supported by node 'id' in the tree
        return 1 << log2ChunkSize - depth(id);
    }
    获取当前层数的偏移量然后 * 当前层数一个节点所管理的大小
    private int runOffset(int id) {
        // represents the 0-based offset in #bytes from start of the byte-array chunk
        int shift = id ^ 1 << depth(id);
        return shift * runLength(id);
    }
    获取的的节点的subPage的id,大于2048的扣去2048,小于的就直接取了~
    private int subpageIdx(int memoryMapIdx) {
        return memoryMapIdx ^ maxSubpageAllocs; // remove highest set bit, to get offset
    }
**/
public class testMemory {
	public static void main(String[] args) {
		System.out.println(1 << 11 - 4);
		System.out.println(4 ^ 1 << 2);
		System.out.println(4096 ^ 2048);

		
		//PooledByteBufAllocator pool = new PooledByteBufAllocator();
	//	ByteBuf buffer = pool.directBuffer(10, 4099);
	//	ByteBuf buffer1 = pool.directBuffer(32, 9999);
	
	//	System.out.println(buffer.alloc());
	//	System.out.println(buffer1.alloc());

		System.out.println(1 << 11 - 4);
		
	}
}
