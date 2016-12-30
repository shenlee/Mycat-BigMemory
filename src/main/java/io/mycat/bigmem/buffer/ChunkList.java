package io.mycat.bigmem.buffer;

/**
*@desc
*@author: zhangwy   @date: 2016年12月28日 上午6:50:37
**/
public class ChunkList<T> {
	private final Arena<T> arena;
	private final ChunkList<T> preList;
	private ChunkList<T> nextList;
	
	private final int minUsage;
	private final int maxUsage;
	private  Chunk<T> head;
	
	/**
	 * 
	 */
	public ChunkList(Arena<T> arena, ChunkList<T> preList, int minUsage, int maxUsage) {
		this.arena = arena;
		this.preList = preList;
		this.minUsage = minUsage;
		this.maxUsage = maxUsage;
	}
	public void setNext(ChunkList<T> nextList) {
		this.nextList = nextList;
	}
	
	public boolean allocate(BaseByteBuffer<T> byteBuffer ,int normalSize) {
		
		if(head == null) return false;
		Chunk<T> cur = head;
		while(cur != null) {
			long handle = cur.allocate(normalSize);
			if(handle > 0) {
				cur.initBuf(byteBuffer, handle, normalSize);
				if(cur.usage() >= maxUsage) {
					remove(cur);
					addChunk(cur);
				}
				return true;
			}
			cur = cur.next;
		}
		return false;
	}
	/**
	*@desc:  
	*@return: void
	*@auth: zhangwy @date: 2016年12月30日 上午7:39:20
	**/
	private void addChunk(Chunk<T> cur) {
		if(cur.usage() >= maxUsage) {
			nextList.addChunk(cur);
		}
		cur.parent = this;
		if(head == null) {
			head = cur;
			cur.next = null;
			cur.prev = null;
		} else {
			cur.next = head;
			head.prev = cur;
			cur.prev = null;
		}
	}
	/**
	*@desc: 将chunk从链表中移除
	*@return: void
	*@auth: zhangwy @date: 2016年12月30日 上午7:27:48
	**/
	private void remove(Chunk<T> cur) {
		if(head == cur) {
			head = cur.next;
			if(head != null) {
				//将头指针.prev 向null
				head.prev = null;
			}
		} else {
			Chunk<T> next = cur.next;
			cur.prev.next = next;
			if(next != null) {
				next.prev = cur.prev;
			}
		}
	}
}

