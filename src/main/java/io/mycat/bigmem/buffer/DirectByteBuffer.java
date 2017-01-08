package io.mycat.bigmem.buffer;

import io.mycat.bigmem.util.ByteUtil;
import io.mycat.bigmem.util.UnsafeUtil;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements of DirectByteBuffer should completely implement
 * java.nio.ByteBuffer
 * 
 * @author shenli
 *
 */

public class DirectByteBuffer extends BaseByteBuffer<ByteBuffer> {

	final static Logger LOGGER = LoggerFactory
			.getLogger(DirectByteBuffer.class);

	public static BaseByteBuffer<ByteBuffer> newInstance(int cap) {
		BaseByteBuffer<ByteBuffer> byteBuffer = new DirectByteBuffer(cap);
		return byteBuffer;
	}

	/**
	 * 
	 */
	private DirectByteBuffer(int cap) {
		super(0, 0, 0, cap);
	}

	@Override
	public DirectByteBuffer init(Chunk<ByteBuffer> chunk, long handle, int offset, int length, int maxLength) {
		super.init(chunk, handle, offset, length, maxLength);
		return this;
	}

	@Override
	public DirectByteBuffer init(Chunk<ByteBuffer> chunks[],long handleInLastChunk, int offsetInLastChunk,
			int lengthInLastChunk, int maxLengthInLastChunk) {
		super.init(chunks, handleInLastChunk, offsetInLastChunk, lengthInLastChunk, maxLengthInLastChunk);
		return this;
	}

	/**
	 * @desc
	 * @auth zhangwy @date 2017年1月2日 下午5:33:51
	 **/
	private long chunkAddr(Chunk<ByteBuffer> chunk) {
		Field field;
		long memoryAddress = 0;
		try {
			field = Buffer.class.getDeclaredField("address");
			field.setAccessible(true);
			long fieldOffset = UnsafeUtil.getUnsafe().objectFieldOffset(field);
			memoryAddress = UnsafeUtil.getUnsafe().getLong(chunk.getMemory(),
					fieldOffset);
		} catch (NoSuchFieldException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (SecurityException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return memoryAddress;
	}

	private int findChunkIndex(int position) {
		if (chunkList.size() == 0) {
			return -1;
		}
		Chunk<ByteBuffer> c = chunkList.get(0);
		return position / c.getArena().chunkSize;
		// for (int low = 0, high = chunkList.size(); low <= high;) {
		// int mid = low + high >>> 1;
		// Chunk<ByteBuffer> c = chunkList.get(mid);
		// if (position >= c.getArena().chunkSize * mid) {
		// low = mid + 1;
		// } else if (position < c.getArena().chunkSize * mid) {
		// high = mid - 1;
		// } else {
		// return mid;
		// }
		// }
		// throw new Error("should not reach here");
	}

	private long addr(int index) {
		int size = chunkList.size();
		switch (size) {
		case 0:
			throw new BufferOverflowException();
		case 1:
			if (index > this.lengthInLastChunk) {
				throw new BufferOverflowException();
			}
			return chunkAddr(chunkList.get(0)) + this.offsetInLastChunk + index;
		default:
			int chunkIndex = findChunkIndex(index);
			Chunk<ByteBuffer> c = chunkList.get(chunkIndex);
			int length = index - c.getArena().chunkSize * chunkIndex;
			if (chunkIndex == size - 1) {
				if (length > this.lengthInLastChunk) {
					throw new BufferOverflowException();
				}
				return chunkAddr(c) + this.offsetInLastChunk + length;
			} else {
				return chunkAddr(c) + length;
			}
		}
	}

	@Override
	public BaseByteBuffer<ByteBuffer> slice() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseByteBuffer<ByteBuffer> duplicate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseByteBuffer<ByteBuffer> asReadOnlyBuffer() {

		return null;
	}

	@Override
	public byte get() {
		return _get(nextGetIndex());
	}

	@Override
	public byte get(int index) {
		return _get(checkIndex(index));
	}

	public BaseByteBuffer<ByteBuffer> get(byte[] dst, int offset, int length) {
		// todo:此处算法待优化
		return super.get(dst, offset, length);
	}

	public BaseByteBuffer<ByteBuffer> get(byte[] dst) {
		// todo:此处算法待优化
		return super.get(dst);
	}

	@Override
	public BaseByteBuffer<ByteBuffer> put(byte b) {
		_put(nextPutIndex(), b);
		return this;
	}

	@Override
	public BaseByteBuffer<ByteBuffer> put(int index, byte b) {
		_put(checkIndex(index), b);
		return this;
	}

	public BaseByteBuffer<ByteBuffer> put(BaseByteBuffer src) {
		// todo:此处算法待优化
		return super.put(src);
	}

	public BaseByteBuffer put(byte[] src, int offset, int length) {
		// todo:此处算法待优化
		return super.put(src, offset, length);
	}

	@Override
	public boolean isDirect() {
		return true;
	}

	@Override
	byte _get(int index) {
		int size = chunkList.size();
		switch (size) {
		case 0:
			throw new BufferOverflowException();
		case 1:
			if (index > this.lengthInLastChunk) {
				throw new BufferOverflowException();
			}
			return chunkList.get(0).getMemory().get(this.offsetInLastChunk + index);
		default:
			int chunkIndex = findChunkIndex(index);
			Chunk<ByteBuffer> c = chunkList.get(chunkIndex);
			int length = index - c.getArena().chunkSize * chunkIndex;
			if (chunkIndex == size - 1) {
				if (length > this.lengthInLastChunk) {
					throw new BufferOverflowException();
				}
				return c.getMemory().get(this.offsetInLastChunk + length);
			} else {
				return c.getMemory().get(length);
			}
		}
	}

	@Override
	void _put(int index, byte b) {
		int size = chunkList.size();
		switch (size) {
		case 0:
			throw new BufferOverflowException();
		case 1:
			if (index > this.lengthInLastChunk) {
				throw new BufferOverflowException();
			}
			chunkList.get(0).getMemory().put(this.offsetInLastChunk + index, b);
		default:
			int chunkIndex = findChunkIndex(index);
			Chunk<ByteBuffer> c = chunkList.get(chunkIndex);
			int length = index - c.getArena().chunkSize * chunkIndex;
			if (chunkIndex == size - 1) {
				if (length > this.lengthInLastChunk) {
					throw new BufferOverflowException();
				}
			    c.getMemory().put(this.offsetInLastChunk + length, b);
			} else {
				c.getMemory().put(length, b);
			}
		}
	}


	@Override
	public char getChar() {
		int charSize = 1 << 1;
		int index = nextGetIndex(charSize);
		int size = chunkList.size();
		switch (size) {
		case 0:
			throw new BufferOverflowException();
		case 1:
			if (index > this.lengthInLastChunk) {
				throw new BufferOverflowException();
			}
			return chunkList.get(0).getMemory()
					.getChar(this.offsetInLastChunk + index);
		default:
			int chunkIndex = findChunkIndex(index);
			Chunk<ByteBuffer> chunk = chunkList.get(chunkIndex);
			int length = index - chunk.getArena().chunkSize * chunkIndex;
			int count = length + charSize - chunk.getArena().chunkSize;
			if (count > 0) {
				Chunk<ByteBuffer> nextChunk = chunkList.get(chunkIndex+1);
				byte b0 = chunk.getMemory().get(length);
				byte b1 = nextChunk.getMemory().get(0);
				if(this.bigEndian)
				{
					return ByteUtil.makeChar(b0, b1);
				}else
				{
					return ByteUtil.makeChar(b1, b0);
				}
			} else {
				if (chunkIndex == size - 1) 
				{
					if (length > this.lengthInLastChunk) {
						throw new BufferOverflowException();
					}
					return chunk.getMemory().getChar(this.offsetInLastChunk + length);
				}
				else
				{
					return chunk.getMemory().getChar(length);
				}
			}
		}
	}

	@Override
	public BaseByteBuffer<ByteBuffer> putChar(char value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char getChar(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BaseByteBuffer putChar(int index, char value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharBuffer asCharBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public short getShort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BaseByteBuffer putShort(short value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public short getShort(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BaseByteBuffer putShort(int index, short value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ShortBuffer asShortBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getInt() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BaseByteBuffer putInt(int value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getInt(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BaseByteBuffer putInt(int index, int value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntBuffer asIntBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BaseByteBuffer putLong(long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLong(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BaseByteBuffer putLong(int index, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongBuffer asLongBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getFloat() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BaseByteBuffer putFloat(float value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getFloat(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BaseByteBuffer putFloat(int index, float value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FloatBuffer asFloatBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getDouble() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BaseByteBuffer putDouble(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getDouble(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BaseByteBuffer putDouble(int index, double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleBuffer asDoubleBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ByteBuffer compact() {
		// TODO Auto-generated method stub
		return null;
	}

}
