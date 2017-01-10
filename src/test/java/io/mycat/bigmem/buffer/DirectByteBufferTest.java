
package io.mycat.bigmem.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import io.mycat.bigmem.util.MpsQueue;
//import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueue;
import sun.nio.ch.DirectBuffer;

/**
*@desc
*@author zhangwy   @date 2017年1月2日 下午6:02:44
**/
public class DirectByteBufferTest {
	@Test
	public void testNewInstance() {
		BaseByteBuffer<ByteBuffer>[] buffer = null;
<<<<<<< HEAD
		Arena<ByteBuffer> arena = new DirectArena(8192,16 * 1024 * 1024 , 11);

=======
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
>>>>>>> 395d280a687243a92f24929617539906be7bfe4e

	}
	@Test
	public void testHugeChunk() {
<<<<<<< HEAD
//		BaseByteBuffer<ByteBuffer> buffer = null;
//		Arena<ByteBuffer> arena = new DirectArena(8192,16 * 1024 * 1024 , 11);
=======
//		BaseByteBuffer<DirectBuffer> buffer = null;
//		Arena<DirectBuffer> arena = new DirectArena(8192,16 * 1024 * 1024 , 11);
>>>>>>> 395d280a687243a92f24929617539906be7bfe4e
//		buffer = arena.allocateBuffer(16 * 1024 * 1024 * 25);
//		for(int i = 0 ; i < buffer.capacity; i++) {
//			buffer.putByte(i, (byte)(i % 255));
//		}
//		System.out.println(buffer.getByte(buffer.capacity - 1));
//		buffer.free();
<<<<<<< HEAD
	}
	/*@Test
	public  void testMpiArray() {
		int length = 16;
		MpsQueue<Integer> array = new MpsQueue<Integer>(length );
		int count = 20;
		StringBuilder output = new StringBuilder("");
		StringBuilder input = new StringBuilder("");
		for(int j = 0 ; j < 10; j ++) {
			for(int i = 0 ; i < count % 97; i ++) {
				if(!array.put(count)){
					//System.out.println("在count = " + (count -1) +"的时候失败了");
					break;
				} else {
					System.out.println("push " + count);
					input.append(count +" ");
					count ++;
				}
			}
			for(int i = 0 ; i < count % 15; i++) {
				//System.out.println(array.get());
				output.append(array.get() +" ");
			}
			count ++;
			System.out.println("=========================");
		}
		Integer element;
		while((element = array.get()) != null) {
			output.append(element +" ");
		}
		System.out.println("push " + output.toString());
		System.out.println("pop  " + input.toString());
	}
	*/
	@Test
	public void testMPIMulThread() {
		int length = 16;
		MpsQueue<Integer> array = new MpsQueue<Integer>(length );
		ReadThread readThread = new ReadThread(array);
		readThread.start();
		ReadThread readThread1 = new ReadThread(array);
		readThread1.start();
		new WriteThread(0, 1000000000, array).start();
		new WriteThread(1000000000, 2000000000, array).start();
		new WriteThread(2000000000, 2100000000, array).start();
//		new WriteThread(3000000000, 4000000000, array).start();
		//new WriteThread(400000, 500000, array).start();

		while(readThread.isAlive()){
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
class WriteThread<Integer> extends Thread {
	protected int start;
	protected int end;
	protected MpsQueue<Integer> queue;
	public WriteThread(int start ,int end,MpsQueue<Integer> queue) {
		this.start = start;
		this.end = end;
		this.queue = queue;
	}
	@Override
	public void run() {
		int content = start;
		for(; content < end;) {
			final Integer  p = (Integer) new java.lang.Integer(content);
			if(queue.put(p)) {
				content ++;
			} else {
				try {
					Thread.currentThread().sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("======" + content);
=======
>>>>>>> 395d280a687243a92f24929617539906be7bfe4e
	}
}

class ReadThread<Integer> extends Thread {
	protected AtomicLong count ;
	MpsQueue<Integer> queue = null;
	public ReadThread(MpsQueue<Integer> queue) {
		this.queue = queue;
		count = new AtomicLong();
	}
	@Override
	public void run() {
		Integer element = null;
		while(true) {
			
			while((element = queue.get()) != null) {
				count.incrementAndGet();
			}
			System.out.println(count.get());
			;
			try {
				Thread.currentThread().sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
