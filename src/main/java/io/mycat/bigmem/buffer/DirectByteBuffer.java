package io.mycat.bigmem.buffer;

import java.lang.reflect.Field;
import java.nio.Buffer;

import io.mycat.bigmem.util.UnsafeUtil;
import sun.nio.ch.DirectBuffer;

/**
 * Implements of DirectByteBuffer should completely implement java.nio.ByteBuffer
 * @author shenli
 *
 */

public class DirectByteBuffer extends BaseByteBuffer<DirectBuffer>{
	
	private long memoryAddress;
	
	public static BaseByteBuffer<DirectBuffer> newInstance() {
		BaseByteBuffer<DirectBuffer> byteBuffer = new DirectByteBuffer();
		return byteBuffer;
	}
	/**
	 * 
	 */
	private DirectByteBuffer() {
		//this.capacity = capacity;
	}
	
	@Override
	public void init(Chunk<DirectBuffer> chunk, long handle, long offset,int capacity, int maxCapacity) {
		super.init(chunk, handle, offset,capacity, maxCapacity);
		initMemoryAddress() ;
	}
	/* 
	 * @see io.mycat.bigmem.buffer.BaseByteBuffer#initUnpooled(io.mycat.bigmem.buffer.Chunk, int)
	 */
	@Override
	public void initUnpooled(Chunk<DirectBuffer> chunk, int capacity) {
		super.initUnpooled(chunk, capacity);
		initMemoryAddress();
	}
	
	/**
	*@desc
	*@auth zhangwy @date 2017年1月2日 下午5:33:51
	**/
	private void initMemoryAddress() {
		Field field;
		try {
			field = Buffer.class.getDeclaredField("address");
			field.setAccessible(true);
			long address = UnsafeUtil.getUnsafe().objectFieldOffset(field);
			memoryAddress = UnsafeUtil.getUnsafe().getLong(memory, address);
			memoryAddress += offset;
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	/* 
	 * @see io.mycat.bigmem.buffer.BaseByteBuffer#getByte(long)
	 */
	@Override
	public byte getByte(long readerIndex) {
		return _getByte(readerIndex);
	}

	

	/* 
	 * @see io.mycat.bigmem.buffer.BaseByteBuffer#getShort(long)
	 */
	@Override
	public short getShort(long readerIndex) {
		
		
		return 0;
	}

	/* 
	 * @see io.mycat.bigmem.buffer.BaseByteBuffer#getInt(long)
	 */
	@Override
	public int getInt(long readerIndex) {
		
		
		return 0;
		
		
	}

	/* 
	 * @see io.mycat.bigmem.buffer.BaseByteBuffer#getLong(long)
	 */
	@Override
	public long getLong(long readerIndex) {
		
		
		return 0;
		
		
	}

	/* 
	 * @see io.mycat.bigmem.buffer.BaseByteBuffer#getFloat(long)
	 */
	@Override
	public float getFloat(long readerIndex) {
		
		
		return 0;
		
		
	}

	/*
	 * @see io.mycat.bigmem.buffer.BaseByteBuffer#getDouble(long)
	 */
	@Override
	public double getDouble(long readerIndex) {
		
		
		return 0;
		
		
	}

	/* 
	 * @see io.mycat.bigmem.buffer.BaseByteBuffer#putByte(long, byte)
	 */
	@Override
	public void putByte(long readerIndex, byte value) {
		 _putByte(readerIndex, value);
	}

	/* 
	 * @see io.mycat.bigmem.buffer.BaseByteBuffer#putShort(long, short)
	 */
	@Override
	public void putShort(long readerIndex, short value) {
		
	}

	/* 
	 * @see io.mycat.bigmem.buffer.BaseByteBuffer#putInt(long, int)
	 */
	@Override
	public void putInt(long readerIndex, int value) {
		
	}

	/* 
	 * @see io.mycat.bigmem.buffer.BaseByteBuffer#putLong(long, long)
	 */
	@Override
	public void putLong(long readerIndex, long value) {
		
	}

	/* 
	 * @see io.mycat.bigmem.buffer.BaseByteBuffer#putfloat(long, float)
	 */
	@Override
	public void putfloat(long readerIndex, float value) {
		
		
	}

	/* 
	 * @see io.mycat.bigmem.buffer.BaseByteBuffer#putdouble(long, double)
	 */
	@Override
	public void putdouble(long readerIndex, double value) {
		
	}
	@Override
	protected  long addr(long readerIndex) {
	    	//if(readerIndex > writerIndex) {
	    	//	throw new IndexOutOfBoundsException("readerIndex can't max smaller writeIndex");
	    	//}
	    	return memoryAddress + readerIndex;
	    }
	
}
