package io.mycat.bigmem;

import org.junit.Test;

public class MemoryAllocatorTest {

	@Test
	public void testDirectBufferTiny1()
	{
		int capacity = 16 - 1;
		MemoryAllocator.CURRENT.directBuffer(capacity);
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
