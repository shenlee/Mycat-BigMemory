package io.mycat.bigmem.buffer;

import java.nio.ByteBuffer;

/**
 * allocator , recycle and gc buffers
 * @author shenli
 *
 */
public interface BufferAllocator {

	public DirectByteBuffer directBuffer(int capacity);
	
    public void recycle(DirectByteBuffer theBuf) ;
}
