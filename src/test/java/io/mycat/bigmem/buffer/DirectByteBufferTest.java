
package io.mycat.bigmem.buffer;

import java.nio.ByteBuffer;

import org.junit.Test;

import sun.nio.ch.DirectBuffer;

/**
*@desc
*@author zhangwy   @date 2017年1月2日 下午6:02:44
**/
public class DirectByteBufferTest {
	@Test
	public void testNewInstance() {
		BaseByteBuffer<DirectBuffer> buffer = null;
		Arena<DirectBuffer> arena = new DirectArena(8192,16 * 1024 * 1024 , 13);
		//Chunk<DirectBuffer> chunk = arena.newChunk();
		//buffer.init(chunk , 0, 0,300, 300);
		//buffer._putByte(0, (byte)9);
		buffer = arena.allocateBuffer(300);
		buffer._putByte(0, (byte)9);
		System.out.println(buffer._getByte(0));
		
		System.out.println(arena.toString());
	}
}

