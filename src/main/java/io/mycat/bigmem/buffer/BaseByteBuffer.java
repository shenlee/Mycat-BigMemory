package io.mycat.bigmem.buffer;

import io.mycat.bigmem.util.ByteUtil;

public abstract class BaseByteBuffer<T> {
	private long address ;
	private long size ;
	private long readerIndex;
	private long writerIndex;
	
	
	public long readerIndex() {
		return readerIndex;
	}
	
	
	public long writerIndex() {
		return writerIndex;
	}
	public abstract void init();
	public abstract byte getByte(long readerIndex);
	public abstract short getShort(long readerIndex);
	public abstract int getInt(long readerIndex);
	public abstract long getLong(long readerIndex);
	public abstract float getFloat(long readerIndex);
	public abstract double getDouble(long readerIndex);

	public abstract byte putByte(long readerIndex, byte value);
	public abstract byte putShort(long readerIndex, short value);
	public abstract byte putInt(long readerIndex, int value);
	public abstract byte putLong(long readerIndex, long value);
	public abstract byte putfloat(long readerIndex, float value);
	public abstract byte putdouble(long readerIndex, double value);
	
    public  byte _getByte(long readerIndex) {
        return ByteUtil.getByte(addr(readerIndex));
    }
    
    public  short _getShort(long readerIndex) {
       return ByteUtil.getShort(addr(readerIndex));
    }

    public  int _getInt(long readerIndex) {
       return ByteUtil.getInt(addr(readerIndex));
    }

    public  long _getLong(long readerIndex) {
    	return ByteUtil.getLong(addr(readerIndex));
    }

    public float _getFloat(long readerIndex) {
    	return Float.intBitsToFloat(ByteUtil.getInt(addr(readerIndex)));
    }
    public double _getDouble(long readerIndex) {
    	return Double.longBitsToDouble(ByteUtil.getLong(addr(readerIndex)));
    }
    public  void _putByte(long readerIndex, byte value) {
        ByteUtil.putByte(addr(readerIndex), value);
    }

    public  void _putShort(long readerIndex, short value) {
    	ByteUtil.putShort(addr(readerIndex), value);
    }

    public  void _putInt(long readerIndex, int value) {
        ByteUtil.putInt(addr(readerIndex), value);
    }
    public  void _putLong(long readerIndex, long value) {
        ByteUtil.putLong(addr(readerIndex), value);
    }
    public  void _putfloat(long readerIndex, float value) {
        ByteUtil.putInt(addr(readerIndex), Float.floatToIntBits(value));
    }
    public  void _putDouble(long readerIndex, double value) {
        ByteUtil.putLong(addr(readerIndex), Double.doubleToLongBits(value));
    }
    private  long addr(long readerIndex) {
    	if(readerIndex > writerIndex) {
    		throw new IndexOutOfBoundsException("readerIndex can't max smaller writeIndex");
    	}
    	return address + readerIndex;
    }
		
}
