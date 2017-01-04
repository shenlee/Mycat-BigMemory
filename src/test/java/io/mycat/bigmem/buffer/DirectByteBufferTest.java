
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
		BaseByteBuffer<DirectBuffer>[] buffer = null;
		Arena<DirectBuffer> arena = new DirectArena(8192,16 * 1024 * 1024 , 11);
		//Chunk<DirectBuffer> chunk = arena.newChunk();
		//buffer.init(chunk , 0, 0,300, 300);
		//buffer._putByte(0, (byte)9);
		int size = 47;
		buffer = new BaseByteBuffer[size];
		for(int i = 0 ; i < size ; i++) {
			buffer[i] = arena.allocateBuffer(300);
			buffer[i]._putByte(0, (byte)(i + 1));
			
			//System.out.println(buffer[i]._getByte(0));
		}
		System.out.println("=======================================");
		for(int i = 0 ; i < size ; i++) {
			System.out.println(buffer[i]._getByte(0));
		}
		
		buffer = new BaseByteBuffer[size];
		for(int i = 0 ; i < size ; i++) {
			buffer[i] = arena.allocateBuffer(16777216);
			buffer[i]._putByte(0, (byte)(i + 1));
			
			//System.out.println(buffer[i]._getByte(0));
		}
		System.out.println("=======================================");
		for(int i = 0 ; i < size ; i++) {
			System.out.println(buffer[i]._getByte(0));
		}
		//System.out.println(buffer._getByte(0));
		System.out.println(arena.toString());
	}
}

