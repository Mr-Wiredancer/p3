package edu.berkeley.cs162;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ThreadPoolTest {
	int threadCount= 0;

	@Test
	public void test() throws InterruptedException {

		ThreadPool pool = new ThreadPool(8);
		for (int k =0; k <20; k++) {
			pool.addToQueue(new Runnable(){

				public void run(){
					System.out.println("This thread is running");
					threadCount++;

				}
			});
		}
		assertTrue(threadCount == 20);
	}

    
	@Test
	public void test2() throws InterruptedException {
		ThreadPool pool = new ThreadPool(8);
		for (int k=0; k <3; k++){
            
			pool.addToQueue(new Runnable(){
				public void run(){
					System.out.println("This thread is running");
					threadCount++;
				}
			});
		}
		assertTrue(threadCount == 3);

	}
}
