package io.mycat.bigmem.buffer;

import org.junit.Test;


public class ArenaAllocateTest {
	@Test
	public void testHuge() {
		Arena arena = new DirectArena(8192, 8192 << 11 , 11);

		BaseByteBuffer buffer = arena.allocateBuffer((8192 << 11) *2 + 26);
		buffer.put((8192 << 11) + 1,(byte)1);
		System.out.println(buffer.get((8192 << 11) + 1));
		BaseByteBuffer bufferA = arena.allocateBuffer( 32);
		bufferA.put(1,(byte)2);
		System.out.println(bufferA.get(1));
		;
	}
}
