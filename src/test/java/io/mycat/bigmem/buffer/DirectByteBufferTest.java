
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
