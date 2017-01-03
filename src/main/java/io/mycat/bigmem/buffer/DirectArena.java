
package io.mycat.bigmem.buffer;

import java.nio.ByteBuffer;

import sun.nio.ch.DirectBuffer;

/**
*@desc
*@author zhangwy   @date 2017年1月2日 下午6:08:54
**/
public class DirectArena extends Arena<DirectBuffer>{

	/**
	 * @param pageSize
	 * @param chunkSize
	 * @param maxOrder
	 */
	public DirectArena(int pageSize, int chunkSize, int maxOrder) {
		super(pageSize, chunkSize, maxOrder);
	}

	public Chunk<DirectBuffer> newChunk() {
		Chunk<DirectBuffer> chunk = new Chunk<DirectBuffer>(this, (DirectBuffer) ByteBuffer.allocateDirect(chunkSize), 
				chunkSize, pageSize, maxOrder);
		return chunk;
	}
	public BaseByteBuffer<DirectBuffer> newBuffer() {
		return DirectByteBuffer.newInstance();
	}
	public void freeChunk(Chunk<DirectBuffer> chunk) {
		chunk.getMemory().cleaner().clean();
	}
}

