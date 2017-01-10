package io.mycat.bigmem.buffer;

import io.mycat.bigmem.buffer.BaseByteBuffer;
import io.mycat.bigmem.buffer.MemoryAllocator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MemoryAllocatorTest {

	@Test
	public void testDirectBufferTiny1()
	{
		int capacity = 16 - 1;
		BaseByteBuffer buffer = MemoryAllocator.CURRENT.directBuffer(capacity);
 
		assertEquals(buffer.capacity(), capacity);
	}
	
	@Test
	public void testDirectBufferTiny2()
	{
		int capacity = 64 -2 ;
		MemoryAllocator.CURRENT.directBuffer(capacity);
	}
	
	
	@Test
	public void testDirectBufferSmall1()
	{
		int capacity = 512 - 1;
		MemoryAllocator.CURRENT.directBuffer(capacity);
	}
	
	@Test
	public void testDirectBufferSmall2()
	{
		int capacity = 8192 - 1;
		MemoryAllocator.CURRENT.directBuffer(capacity);
	}
	
	@Test
	public void testDirectBufferLarge1()
	{
		int capacity = 8192 + 1;
		MemoryAllocator.CURRENT.directBuffer(capacity);
	}
	
	@Test
	public void testDirectBufferLarge2()
	{
		int capacity = 8192 << 10 ;
		MemoryAllocator.CURRENT.directBuffer(capacity);
	}
	
	
	@Test
	public void testDirectBufferHuge1()
	{
		int capacity = (8192 << 11) * 5 + 123456;
		MemoryAllocator.CURRENT.directBuffer(capacity);
	}
	
	@Test
	public void testDirectBufferHuge2()
	{
		int capacity =  (8192 << 11) * 1024;
		MemoryAllocator.CURRENT.directBuffer(capacity);
	}
	
	@Test
	public void testPrivateDirectBuffer()
	{
//		int index = MemoryAllocator.CURRENT.allocatePrivate()
//		directBuffer(index, capacity);
	}
}
