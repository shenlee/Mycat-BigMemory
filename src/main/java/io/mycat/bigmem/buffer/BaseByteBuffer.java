package io.mycat.bigmem.buffer;

import io.mycat.bigmem.util.ByteUtil;

public abstract class BaseByteBuffer<T> {
	protected long offset ;
	protected long capacity ;
	protected long maxCapacity;
	protected long readerIndex;
	protected long writerIndex;
	protected T memory;
	protected Chunk<T> chunk;
	protected long handle;
	
	public long readerIndex() {
		return readerIndex;
	}
	
	protected void setMaxCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}
	
	public long writerIndex() {
		return writerIndex;
	}
	public void init(Chunk<T> chunk,long handle, long offset,int capacity, int maxCapacity) {
		this.chunk = chunk;
		this.memory = chunk.getMemory();
		this.handle = handle;
		this.offset = offset;
		this.capacity = capacity;
		this.maxCapacity = maxCapacity;
	};
	public abstract byte getByte(long readerIndex);
	public abstract short getShort(long readerIndex);
	public abstract int getInt(long readerIndex);
	public abstract long getLong(long readerIndex);
	public abstract float getFloat(long readerIndex);
	public abstract double getDouble(long readerIndex);

	public abstract void putByte(long readerIndex, byte value);
	public abstract void putShort(long readerIndex, short value);
	public abstract void putInt(long readerIndex, int value);
	public abstract void putLong(long readerIndex, long value);
	public abstract void putfloat(long readerIndex, float value);
	public abstract void putdouble(long readerIndex, double value);
	
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
    protected  long addr(long readerIndex) {
    	if(readerIndex > writerIndex) {
    		throw new IndexOutOfBoundsException("readerIndex can't max smaller writeIndex");
    	}
    	return offset + readerIndex;
    }
		
}
