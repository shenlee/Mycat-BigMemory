package io.mycat.bigmem.buffer;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.InvalidMarkException;
import java.nio.LongBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
 

public abstract class BaseByteBuffer<T> implements Comparable<BaseByteBuffer<T>> {

	// Invariants: mark <= position <= limit <= capacity
	private int mark = -1;
	private int position = 0;
	private int limit;
	private int capacity;
	
	private boolean readonly;
	/**
	 * 内存地址是否是连续分配的
	 */
	private boolean continuity;
	
	private volatile boolean initialized = false;
	
	protected List<Chunk<T>> chunkList;
	protected int offsetInLastChunk;
	protected int lengthInLastChunk;
	protected long handle;

	BaseByteBuffer(int mark, int pos, int lim, int cap) {
		if (cap < 0)
			throw new IllegalArgumentException("Negative capacity: " + cap);
		this.capacity = cap;
		limit(lim);
		position(pos);
		if (mark >= 0) {
			if (mark > pos)
				throw new IllegalArgumentException("mark > position: (" + mark
						+ " > " + pos + ")");
			this.mark = mark;
		}
		chunkList =  Collections.synchronizedList(new ArrayList<Chunk<T>>());
	}
	
	BaseByteBuffer(int cap) {
		this(0, 0, 0, cap);
	}
	
	public BaseByteBuffer<T> init(Chunk<T> chunk, long handle, int offset, int length)
	{
		if(!initialized)
		{
			chunkList.clear();
			this.offsetInLastChunk = offset;
			this.lengthInLastChunk = length;
			this.handle = handle;
			chunkList.add(chunk);
			initialized = true;
		}
		return this;
	}

	public BaseByteBuffer<T> init(Chunk<T> chunks[], long handleInLastChunk, int offsetInLastChunk, int lengthInLastChunk)
	{
		if(!initialized)
		{
			chunkList.clear();
			this.handle = handleInLastChunk;
			this.offsetInLastChunk = offsetInLastChunk;
			this.lengthInLastChunk = lengthInLastChunk;
			chunkList.addAll(Arrays.asList(chunks));
			initialized = true;
		}
		return this;
	}
	
	public BaseByteBuffer<T> initUnpooled(Chunk<T> hugeChunk, int capacity)
	{
		if(!initialized)
		{
			chunkList.clear();
			chunkList.add(hugeChunk);
			continuity = true;
			initialized = true;
		}
		return this;
	}
	
	/**
	 * Returns this buffer's capacity.
	 *
	 * @return The capacity of this buffer
	 */
	public final int capacity() {
		return capacity;
	}

	/**
	 * Returns this buffer's position.
	 *
	 * @return The position of this buffer
	 */
	public final int position() {
		return position;
	}

	/**
	 * Sets this buffer's position. If the mark is defined and larger than the
	 * new position then it is discarded.
	 *
	 * @param newPosition
	 *            The new position value; must be non-negative and no larger
	 *            than the current limit
	 *
	 * @return This buffer
	 *
	 * @throws IllegalArgumentException
	 *             If the preconditions on <tt>newPosition</tt> do not hold
	 */
	public final BaseByteBuffer position(int newPosition) {
		if ((newPosition > limit) || (newPosition < 0))
			throw new IllegalArgumentException();
		position = newPosition;
		if (mark > position)
			mark = -1;
		return this;
	}

	/**
	 * Returns this buffer's limit.
	 *
	 * @return The limit of this buffer
	 */
	public final int limit() {
		return limit;
	}

	public boolean isReadOnly()
	{
		return this.readonly;
	}
	
	public void setReadOnly(boolean readonly)
	{
		 this.readonly = readonly;
	}
	
	public final BaseByteBuffer reset() {
		int m = mark;
		if (m < 0)
			throw new InvalidMarkException();
		position = m;
		return this;
	}

	public final BaseByteBuffer clear() {
		position = 0;
		limit = capacity;
		mark = -1;
		return this;
	}

	/**
	 * Flips this buffer. The limit is set to the current position and then the
	 * position is set to zero. If the mark is defined then it is discarded.
	 */
	public final BaseByteBuffer flip() {
		limit = position;
		position = 0;
		mark = -1;
		return this;
	}

	/**
	 * Rewinds this buffer. The position is set to zero and the mark is
	 * discarded.
	 */
	public final BaseByteBuffer rewind() {
		position = 0;
		mark = -1;
		return this;
	}

	/**
	 * Returns the number of elements between the current position and the
	 * limit.
	 *
	 * @return The number of elements remaining in this buffer
	 */
	public final int remaining() {
		return limit - position;
	}

	/**
	 * Tells whether there are any elements between the current position and the
	 * limit.
	 *
	 * @return <tt>true</tt> if, and only if, there is at least one element
	 *         remaining in this buffer
	 */
	public final boolean hasRemaining() {
		return position < limit;
	}

	public final BaseByteBuffer limit(int newLimit) {
		if ((newLimit > capacity) || (newLimit < 0))
			throw new IllegalArgumentException();
		limit = newLimit;
		if (position > limit)
			position = limit;
		if (mark > limit)
			mark = -1;
		return this;
	}

	public abstract BaseByteBuffer slice();

	public abstract BaseByteBuffer duplicate();

	public abstract BaseByteBuffer asReadOnlyBuffer();

	public abstract byte get();

	public abstract byte get(int index);

	public abstract BaseByteBuffer put(byte b);

	public abstract BaseByteBuffer put(int index, byte b);

	public abstract ByteBuffer compact();

	public abstract boolean isDirect();

	public BaseByteBuffer get(byte[] dst, int offset, int length) {
		checkBounds(offset, length, dst.length);
		if (length > remaining())
			throw new BufferUnderflowException();
		int end = offset + length;
		for (int i = offset; i < end; i++)
			dst[i] = get();
		return this;
	}

	public BaseByteBuffer get(byte[] dst) {
		return get(dst, 0, dst.length);
	}

	public BaseByteBuffer put(BaseByteBuffer src) {
		if (src == this)
			throw new IllegalArgumentException();
		if (isReadOnly())
			throw new ReadOnlyBufferException();
		int n = src.remaining();
		if (n > remaining())
			throw new BufferOverflowException();
		for (int i = 0; i < n; i++)
			put(src.get());
		return this;
	}

	public BaseByteBuffer put(byte[] src, int offset, int length) {
		checkBounds(offset, length, src.length);
		if (length > remaining())
			throw new BufferOverflowException();
		int end = offset + length;
		for (int i = offset; i < end; i++)
			this.put(src[i]);
		return this;
	}

	public final BaseByteBuffer put(byte[] src) {
		return put(src, 0, src.length);
	}

	static void checkBounds(int off, int len, int size) { // package-private
		if ((off | len | (off + len) | (size - (off + len))) < 0)
			throw new IndexOutOfBoundsException();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName());
		sb.append("[pos=");
		sb.append(position());
		sb.append(" lim=");
		sb.append(limit());
		sb.append(" cap=");
		sb.append(capacity());
		sb.append("]");
		return sb.toString();
	}

	public int hashCode() {
		int h = 1;
		int p = position();
		for (int i = limit() - 1; i >= p; i--)

			h = 31 * h + (int) get(i);

		return h;
	}

	public boolean equals(Object ob) {
		if (this == ob)
			return true;
		if (!(ob instanceof BaseByteBuffer))
			return false;
		BaseByteBuffer that = (BaseByteBuffer) ob;
		if (this.remaining() != that.remaining())
			return false;
		int p = this.position();
		for (int i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j--)
			if (!equals(this.get(i), that.get(j)))
				return false;
		return true;
	}

	private static boolean equals(byte x, byte y) {

		return x == y;

	}

	public int compareTo(BaseByteBuffer that) {
		int n = this.position() + Math.min(this.remaining(), that.remaining());
		for (int i = this.position(), j = that.position(); i < n; i++, j++) {
			int cmp = compare(this.get(i), that.get(j));
			if (cmp != 0)
				return cmp;
		}
		return this.remaining() - that.remaining();
	}

	private static int compare(byte x, byte y) {

		return Byte.compare(x, y);

	}

	boolean bigEndian = true;


	// Unchecked accessors, for use by BaseByteBufferAs-X-Buffer classes
	//
	abstract byte _get(int i); // package-private

	abstract void _put(int i, byte b); // package-private

	/**
	 * Relative <i>get</i> method for reading a char value.
	 *
	 * <p>
	 * Reads the next two bytes at this buffer's current position, composing
	 * them into a char value according to the current byte order, and then
	 * increments the position by two.
	 * </p>
	 *
	 * @return The char value at the buffer's current position
	 *
	 * @throws BufferUnderflowException
	 *             If there are fewer than two bytes remaining in this buffer
	 */
	public abstract char getChar();

	/**
	 * Relative <i>put</i> method for writing a char
	 * value&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * Writes two bytes containing the given char value, in the current byte
	 * order, into this buffer at the current position, and then increments the
	 * position by two.
	 * </p>
	 *
	 * @param value
	 *            The char value to be written
	 *
	 * @return This buffer
	 *
	 * @throws BufferOverflowException
	 *             If there are fewer than two bytes remaining in this buffer
	 *
	 * @throws ReadOnlyBufferException
	 *             If this buffer is read-only
	 */
	public abstract BaseByteBuffer putChar(char value);

	/**
	 * Absolute <i>get</i> method for reading a char value.
	 *
	 * <p>
	 * Reads two bytes at the given index, composing them into a char value
	 * according to the current byte order.
	 * </p>
	 *
	 * @param index
	 *            The index from which the bytes will be read
	 *
	 * @return The char value at the given index
	 *
	 * @throws IndexOutOfBoundsException
	 *             If <tt>index</tt> is negative or not smaller than the
	 *             buffer's limit, minus one
	 */
	public abstract char getChar(int index);

	/**
	 * Absolute <i>put</i> method for writing a char
	 * value&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * Writes two bytes containing the given char value, in the current byte
	 * order, into this buffer at the given index.
	 * </p>
	 *
	 * @param index
	 *            The index at which the bytes will be written
	 *
	 * @param value
	 *            The char value to be written
	 *
	 * @return This buffer
	 *
	 * @throws IndexOutOfBoundsException
	 *             If <tt>index</tt> is negative or not smaller than the
	 *             buffer's limit, minus one
	 *
	 * @throws ReadOnlyBufferException
	 *             If this buffer is read-only
	 */
	public abstract BaseByteBuffer putChar(int index, char value);

	/**
	 * Creates a view of this byte buffer as a char buffer.
	 *
	 * <p>
	 * The content of the new buffer will start at this buffer's current
	 * position. Changes to this buffer's content will be visible in the new
	 * buffer, and vice versa; the two buffers' position, limit, and mark values
	 * will be independent.
	 *
	 * <p>
	 * The new buffer's position will be zero, its capacity and its limit will
	 * be the number of bytes remaining in this buffer divided by two, and its
	 * mark will be undefined. The new buffer will be direct if, and only if,
	 * this buffer is direct, and it will be read-only if, and only if, this
	 * buffer is read-only.
	 * </p>
	 *
	 * @return A new char buffer
	 */
	public abstract CharBuffer asCharBuffer();

	/**
	 * Relative <i>get</i> method for reading a short value.
	 *
	 * <p>
	 * Reads the next two bytes at this buffer's current position, composing
	 * them into a short value according to the current byte order, and then
	 * increments the position by two.
	 * </p>
	 *
	 * @return The short value at the buffer's current position
	 *
	 * @throws BufferUnderflowException
	 *             If there are fewer than two bytes remaining in this buffer
	 */
	public abstract short getShort();

	/**
	 * Relative <i>put</i> method for writing a short
	 * value&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * Writes two bytes containing the given short value, in the current byte
	 * order, into this buffer at the current position, and then increments the
	 * position by two.
	 * </p>
	 *
	 * @param value
	 *            The short value to be written
	 *
	 * @return This buffer
	 *
	 * @throws BufferOverflowException
	 *             If there are fewer than two bytes remaining in this buffer
	 *
	 * @throws ReadOnlyBufferException
	 *             If this buffer is read-only
	 */
	public abstract BaseByteBuffer putShort(short value);

	/**
	 * Absolute <i>get</i> method for reading a short value.
	 *
	 * <p>
	 * Reads two bytes at the given index, composing them into a short value
	 * according to the current byte order.
	 * </p>
	 *
	 * @param index
	 *            The index from which the bytes will be read
	 *
	 * @return The short value at the given index
	 *
	 * @throws IndexOutOfBoundsException
	 *             If <tt>index</tt> is negative or not smaller than the
	 *             buffer's limit, minus one
	 */
	public abstract short getShort(int index);

	/**
	 * Absolute <i>put</i> method for writing a short
	 * value&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * Writes two bytes containing the given short value, in the current byte
	 * order, into this buffer at the given index.
	 * </p>
	 *
	 * @param index
	 *            The index at which the bytes will be written
	 *
	 * @param value
	 *            The short value to be written
	 *
	 * @return This buffer
	 *
	 * @throws IndexOutOfBoundsException
	 *             If <tt>index</tt> is negative or not smaller than the
	 *             buffer's limit, minus one
	 *
	 * @throws ReadOnlyBufferException
	 *             If this buffer is read-only
	 */
	public abstract BaseByteBuffer putShort(int index, short value);

	/**
	 * Creates a view of this byte buffer as a short buffer.
	 *
	 * <p>
	 * The content of the new buffer will start at this buffer's current
	 * position. Changes to this buffer's content will be visible in the new
	 * buffer, and vice versa; the two buffers' position, limit, and mark values
	 * will be independent.
	 *
	 * <p>
	 * The new buffer's position will be zero, its capacity and its limit will
	 * be the number of bytes remaining in this buffer divided by two, and its
	 * mark will be undefined. The new buffer will be direct if, and only if,
	 * this buffer is direct, and it will be read-only if, and only if, this
	 * buffer is read-only.
	 * </p>
	 *
	 * @return A new short buffer
	 */
	public abstract ShortBuffer asShortBuffer();

	/**
	 * Relative <i>get</i> method for reading an int value.
	 *
	 * <p>
	 * Reads the next four bytes at this buffer's current position, composing
	 * them into an int value according to the current byte order, and then
	 * increments the position by four.
	 * </p>
	 *
	 * @return The int value at the buffer's current position
	 *
	 * @throws BufferUnderflowException
	 *             If there are fewer than four bytes remaining in this buffer
	 */
	public abstract int getInt();

	/**
	 * Relative <i>put</i> method for writing an int
	 * value&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * Writes four bytes containing the given int value, in the current byte
	 * order, into this buffer at the current position, and then increments the
	 * position by four.
	 * </p>
	 *
	 * @param value
	 *            The int value to be written
	 *
	 * @return This buffer
	 *
	 * @throws BufferOverflowException
	 *             If there are fewer than four bytes remaining in this buffer
	 *
	 * @throws ReadOnlyBufferException
	 *             If this buffer is read-only
	 */
	public abstract BaseByteBuffer putInt(int value);

	/**
	 * Absolute <i>get</i> method for reading an int value.
	 *
	 * <p>
	 * Reads four bytes at the given index, composing them into a int value
	 * according to the current byte order.
	 * </p>
	 *
	 * @param index
	 *            The index from which the bytes will be read
	 *
	 * @return The int value at the given index
	 *
	 * @throws IndexOutOfBoundsException
	 *             If <tt>index</tt> is negative or not smaller than the
	 *             buffer's limit, minus three
	 */
	public abstract int getInt(int index);

	/**
	 * Absolute <i>put</i> method for writing an int
	 * value&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * Writes four bytes containing the given int value, in the current byte
	 * order, into this buffer at the given index.
	 * </p>
	 *
	 * @param index
	 *            The index at which the bytes will be written
	 *
	 * @param value
	 *            The int value to be written
	 *
	 * @return This buffer
	 *
	 * @throws IndexOutOfBoundsException
	 *             If <tt>index</tt> is negative or not smaller than the
	 *             buffer's limit, minus three
	 *
	 * @throws ReadOnlyBufferException
	 *             If this buffer is read-only
	 */
	public abstract BaseByteBuffer putInt(int index, int value);

	/**
	 * Creates a view of this byte buffer as an int buffer.
	 *
	 * <p>
	 * The content of the new buffer will start at this buffer's current
	 * position. Changes to this buffer's content will be visible in the new
	 * buffer, and vice versa; the two buffers' position, limit, and mark values
	 * will be independent.
	 *
	 * <p>
	 * The new buffer's position will be zero, its capacity and its limit will
	 * be the number of bytes remaining in this buffer divided by four, and its
	 * mark will be undefined. The new buffer will be direct if, and only if,
	 * this buffer is direct, and it will be read-only if, and only if, this
	 * buffer is read-only.
	 * </p>
	 *
	 * @return A new int buffer
	 */
	public abstract IntBuffer asIntBuffer();

	/**
	 * Relative <i>get</i> method for reading a long value.
	 *
	 * <p>
	 * Reads the next eight bytes at this buffer's current position, composing
	 * them into a long value according to the current byte order, and then
	 * increments the position by eight.
	 * </p>
	 *
	 * @return The long value at the buffer's current position
	 *
	 * @throws BufferUnderflowException
	 *             If there are fewer than eight bytes remaining in this buffer
	 */
	public abstract long getLong();

	/**
	 * Relative <i>put</i> method for writing a long
	 * value&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * Writes eight bytes containing the given long value, in the current byte
	 * order, into this buffer at the current position, and then increments the
	 * position by eight.
	 * </p>
	 *
	 * @param value
	 *            The long value to be written
	 *
	 * @return This buffer
	 *
	 * @throws BufferOverflowException
	 *             If there are fewer than eight bytes remaining in this buffer
	 *
	 * @throws ReadOnlyBufferException
	 *             If this buffer is read-only
	 */
	public abstract BaseByteBuffer putLong(long value);

	/**
	 * Absolute <i>get</i> method for reading a long value.
	 *
	 * <p>
	 * Reads eight bytes at the given index, composing them into a long value
	 * according to the current byte order.
	 * </p>
	 *
	 * @param index
	 *            The index from which the bytes will be read
	 *
	 * @return The long value at the given index
	 *
	 * @throws IndexOutOfBoundsException
	 *             If <tt>index</tt> is negative or not smaller than the
	 *             buffer's limit, minus seven
	 */
	public abstract long getLong(int index);

	/**
	 * Absolute <i>put</i> method for writing a long
	 * value&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * Writes eight bytes containing the given long value, in the current byte
	 * order, into this buffer at the given index.
	 * </p>
	 *
	 * @param index
	 *            The index at which the bytes will be written
	 *
	 * @param value
	 *            The long value to be written
	 *
	 * @return This buffer
	 *
	 * @throws IndexOutOfBoundsException
	 *             If <tt>index</tt> is negative or not smaller than the
	 *             buffer's limit, minus seven
	 *
	 * @throws ReadOnlyBufferException
	 *             If this buffer is read-only
	 */
	public abstract BaseByteBuffer putLong(int index, long value);

	/**
	 * Creates a view of this byte buffer as a long buffer.
	 *
	 * <p>
	 * The content of the new buffer will start at this buffer's current
	 * position. Changes to this buffer's content will be visible in the new
	 * buffer, and vice versa; the two buffers' position, limit, and mark values
	 * will be independent.
	 *
	 * <p>
	 * The new buffer's position will be zero, its capacity and its limit will
	 * be the number of bytes remaining in this buffer divided by eight, and its
	 * mark will be undefined. The new buffer will be direct if, and only if,
	 * this buffer is direct, and it will be read-only if, and only if, this
	 * buffer is read-only.
	 * </p>
	 *
	 * @return A new long buffer
	 */
	public abstract LongBuffer asLongBuffer();

	/**
	 * Relative <i>get</i> method for reading a float value.
	 *
	 * <p>
	 * Reads the next four bytes at this buffer's current position, composing
	 * them into a float value according to the current byte order, and then
	 * increments the position by four.
	 * </p>
	 *
	 * @return The float value at the buffer's current position
	 *
	 * @throws BufferUnderflowException
	 *             If there are fewer than four bytes remaining in this buffer
	 */
	public abstract float getFloat();

	/**
	 * Relative <i>put</i> method for writing a float
	 * value&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * Writes four bytes containing the given float value, in the current byte
	 * order, into this buffer at the current position, and then increments the
	 * position by four.
	 * </p>
	 *
	 * @param value
	 *            The float value to be written
	 *
	 * @return This buffer
	 *
	 * @throws BufferOverflowException
	 *             If there are fewer than four bytes remaining in this buffer
	 *
	 * @throws ReadOnlyBufferException
	 *             If this buffer is read-only
	 */
	public abstract BaseByteBuffer putFloat(float value);

	/**
	 * Absolute <i>get</i> method for reading a float value.
	 *
	 * <p>
	 * Reads four bytes at the given index, composing them into a float value
	 * according to the current byte order.
	 * </p>
	 *
	 * @param index
	 *            The index from which the bytes will be read
	 *
	 * @return The float value at the given index
	 *
	 * @throws IndexOutOfBoundsException
	 *             If <tt>index</tt> is negative or not smaller than the
	 *             buffer's limit, minus three
	 */
	public abstract float getFloat(int index);

	/**
	 * Absolute <i>put</i> method for writing a float
	 * value&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * Writes four bytes containing the given float value, in the current byte
	 * order, into this buffer at the given index.
	 * </p>
	 *
	 * @param index
	 *            The index at which the bytes will be written
	 *
	 * @param value
	 *            The float value to be written
	 *
	 * @return This buffer
	 *
	 * @throws IndexOutOfBoundsException
	 *             If <tt>index</tt> is negative or not smaller than the
	 *             buffer's limit, minus three
	 *
	 * @throws ReadOnlyBufferException
	 *             If this buffer is read-only
	 */
	public abstract BaseByteBuffer putFloat(int index, float value);

	/**
	 * Creates a view of this byte buffer as a float buffer.
	 *
	 * <p>
	 * The content of the new buffer will start at this buffer's current
	 * position. Changes to this buffer's content will be visible in the new
	 * buffer, and vice versa; the two buffers' position, limit, and mark values
	 * will be independent.
	 *
	 * <p>
	 * The new buffer's position will be zero, its capacity and its limit will
	 * be the number of bytes remaining in this buffer divided by four, and its
	 * mark will be undefined. The new buffer will be direct if, and only if,
	 * this buffer is direct, and it will be read-only if, and only if, this
	 * buffer is read-only.
	 * </p>
	 *
	 * @return A new float buffer
	 */
	public abstract FloatBuffer asFloatBuffer();

	/**
	 * Relative <i>get</i> method for reading a double value.
	 *
	 * <p>
	 * Reads the next eight bytes at this buffer's current position, composing
	 * them into a double value according to the current byte order, and then
	 * increments the position by eight.
	 * </p>
	 *
	 * @return The double value at the buffer's current position
	 *
	 * @throws BufferUnderflowException
	 *             If there are fewer than eight bytes remaining in this buffer
	 */
	public abstract double getDouble();

	/**
	 * Relative <i>put</i> method for writing a double
	 * value&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * Writes eight bytes containing the given double value, in the current byte
	 * order, into this buffer at the current position, and then increments the
	 * position by eight.
	 * </p>
	 *
	 * @param value
	 *            The double value to be written
	 *
	 * @return This buffer
	 *
	 * @throws BufferOverflowException
	 *             If there are fewer than eight bytes remaining in this buffer
	 *
	 * @throws ReadOnlyBufferException
	 *             If this buffer is read-only
	 */
	public abstract BaseByteBuffer putDouble(double value);

	/**
	 * Absolute <i>get</i> method for reading a double value.
	 *
	 * <p>
	 * Reads eight bytes at the given index, composing them into a double value
	 * according to the current byte order.
	 * </p>
	 *
	 * @param index
	 *            The index from which the bytes will be read
	 *
	 * @return The double value at the given index
	 *
	 * @throws IndexOutOfBoundsException
	 *             If <tt>index</tt> is negative or not smaller than the
	 *             buffer's limit, minus seven
	 */
	public abstract double getDouble(int index);

	/**
	 * Absolute <i>put</i> method for writing a double
	 * value&nbsp;&nbsp;<i>(optional operation)</i>.
	 *
	 * <p>
	 * Writes eight bytes containing the given double value, in the current byte
	 * order, into this buffer at the given index.
	 * </p>
	 *
	 * @param index
	 *            The index at which the bytes will be written
	 *
	 * @param value
	 *            The double value to be written
	 *
	 * @return This buffer
	 *
	 * @throws IndexOutOfBoundsException
	 *             If <tt>index</tt> is negative or not smaller than the
	 *             buffer's limit, minus seven
	 *
	 * @throws ReadOnlyBufferException
	 *             If this buffer is read-only
	 */
	public abstract BaseByteBuffer putDouble(int index, double value);

	/**
	 * Creates a view of this byte buffer as a double buffer.
	 *
	 * <p>
	 * The content of the new buffer will start at this buffer's current
	 * position. Changes to this buffer's content will be visible in the new
	 * buffer, and vice versa; the two buffers' position, limit, and mark values
	 * will be independent.
	 *
	 * <p>
	 * The new buffer's position will be zero, its capacity and its limit will
	 * be the number of bytes remaining in this buffer divided by eight, and its
	 * mark will be undefined. The new buffer will be direct if, and only if,
	 * this buffer is direct, and it will be read-only if, and only if, this
	 * buffer is read-only.
	 * </p>
	 *
	 * @return A new double buffer
	 */
	public abstract DoubleBuffer asDoubleBuffer();

    /**
     * Checks the current position against the limit, throwing a {@link
     * BufferUnderflowException} if it is not smaller than the limit, and then
     * increments the position.
     *
     * @return  The current position value, before it is incremented
     */
    final int nextGetIndex() {                          // package-private
        if (position >= limit)
            throw new BufferUnderflowException();
        return position++;
    }

    final int nextGetIndex(int nb) {                    // package-private
        if (limit - position < nb)
            throw new BufferUnderflowException();
        int p = position;
        position += nb;
        return p;
    }
    
    /**
     * Checks the current position against the limit, throwing a {@link
     * BufferOverflowException} if it is not smaller than the limit, and then
     * increments the position.
     *
     * @return  The current position value, before it is incremented
     */
    final int nextPutIndex() {                          // package-private
        if (position >= limit)
            throw new BufferOverflowException();
        return position++;
    }

    final int nextPutIndex(int nb) {                    // package-private
        if (limit - position < nb)
            throw new BufferOverflowException();
        int p = position;
        position += nb;
        return p;
    }
    
    final int checkIndex(int i) {                       // package-private
        if ((i < 0) || (i >= limit))
            throw new IndexOutOfBoundsException();
        return i;
    }
	// public void init(Chunk<T> chunk, long handle, long offset, int capacity,
	// int maxCapacity) {
	// this.chunk = chunk;
	// this.memory = chunk.getMemory();
	// this.handle = handle;
	// this.offset = offset;
	// this.capacity = capacity;
	// this.maxCapacity = maxCapacity;
	// };
	//
	// public byte _getByte(long readerIndex) {
	// return ByteUtil.getByte(addr(readerIndex));
	// }
	//
	// public void _putDouble(long readerIndex, double value) {
	// ByteUtil.putLong(addr(readerIndex), Double.doubleToLongBits(value));
	// }

//	protected long addr(long readerIndex) {
//		if (readerIndex > writerIndex) {
//			throw new IndexOutOfBoundsException(
//					"readerIndex can't max smaller writeIndex");
//		}
//		return offset + readerIndex;
//	}

}
