
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
		BaseByteBuffer<ByteBuffer>[] buffer = null;
		Arena<ByteBuffer> arena = new DirectArena(8192, 8192 << 11 , 11);
		//Chunk<DirectBuffer> chunk = arena.newChunk();
		//buffer.init(chunk , 0, 0,300, 300);
		//buffer._putByte(0, (byte)9);
		int size = 26;
		buffer = new DirectByteBuffer[size];
		for(int i = 0 ; i < size ; i++) {
			buffer[i] = MemoryAllocator.CURRENT.directBuffer(300);
			buffer[i] = arena.allocateBuffer(300);
//			buffer[i].putByte(0, (byte)(i + 1));
			
			//System.out.println(buffer[i]._getByte(0));
		}
		for(int i = 0 ; i < size ; i++) {
//			buffer[i].free();
		}
		System.out.println(arena.toString());

//		size = size ;
//		buffer = new BaseByteBuffer[size ];
//		for(int i = 0 ; i < size ; i++) {
//			buffer[i] = arena.allocateBuffer(1024);
//			buffer[i]._putByte(0, (byte)(i + 1));
//		}
//		System.out.println("=======================================");
//		for(int i = 0 ; i < size ; i++) {
//			System.out.println(buffer[i]._getByte(0));
//		}
//		//System.out.println(buffer._getByte(0));
//		System.out.println(arena.toString());
//		
//		for(int i = 0 ; i < size ; i++) {
//			buffer[i].free();
//		}
//		System.out.println(arena.toString());

	}
	@Test
	public void testHugeChunk() {
//		BaseByteBuffer<DirectBuffer> buffer = null;
//		Arena<DirectBuffer> arena = new DirectArena(8192,16 * 1024 * 1024 , 11);
//		buffer = arena.allocateBuffer(16 * 1024 * 1024 * 25);
//		for(int i = 0 ; i < buffer.capacity; i++) {
//			buffer.putByte(i, (byte)(i % 255));
//		}
//		System.out.println(buffer.getByte(buffer.capacity - 1));
//		buffer.free();
	}
}

