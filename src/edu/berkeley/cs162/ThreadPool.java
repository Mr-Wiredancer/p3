package edu.berkeley.cs162;

import java.util.LinkedList;
import java.util.concurrent.locks.*;

public class ThreadPool {
	/**
	 * Set of threads in the threadpool
	 */
	protected WorkerThread threads[] = null;
	LinkedList<Runnable> jobQueue = new LinkedList<Runnable>();
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	
	public Lock helperLock = new ReentrantLock();
	public Condition jobQueueNotEmpty = helperLock.newCondition();
	/**
	 * Initialize the number of threads required in the threadpool. 
	 * 
	 * @param size  How many threads in the thread pool.
	 */
	public ThreadPool(int size)
	{      
	    // TODO: implement me
		threads = new WorkerThread[size];
		initializeThreads();
	}
	
	private void initializeThreads(){
		for (int i = 0; i < threads.length; i++){
			threads[i] = new WorkerThread(this);
		}
		System.out.println("there are "+threads.length+" threads");
		for (WorkerThread w : this.threads){
			w.start();
		}
	}

	/**
	 * Add a job to the queue of tasks that has to be executed. As soon as a thread is available, 
	 * it will retrieve tasks from this queue and start processing.
	 * @param r job that has to be executed asynchronously
	 * @throws InterruptedException 
	 */
	public void addToQueue(Runnable r) throws InterruptedException
	{
	      // TODO: implement me
		lock.writeLock().lock();	
		jobQueue.add(r);
		lock.writeLock().unlock();
		
		this.helperLock.lock();
		this.jobQueueNotEmpty.signal();
		this.helperLock.unlock();
	}
	
	/** 
	 * Block until a job is available in the queue and retrieve the job
	 * @return A runnable task that has to be executed
	 * @throws InterruptedException 
	 */
	public synchronized Runnable getJob() throws InterruptedException {
	    lock.writeLock().lock();
	    Runnable r = null;
	    try{
	    r = jobQueue.remove();
	    }catch(Exception e){
	    	
	    }
	    lock.writeLock().unlock();
	    return r;
	}
}

/**
 * The worker threads that make up the thread pool.
 */
class WorkerThread extends Thread {
	/**
	 * The constructor.
	 * 
	 * @param o the thread pool 
	 */
	private static int workerThreadCounter = 0;
	
	private ThreadPool o;
	
	WorkerThread(ThreadPool o)
	{
		this.o = o;
		this.setName("WorkerThread"+WorkerThread.workerThreadCounter++);
	}

	public void debug(String s){
		System.out.println(Thread.currentThread().getName()+": "+s);
	}
	/**
	 * Scan for and execute tasks.
	 */
	public void run()
	{
		debug("running");
	      // TODO: implement me
		Runnable r = null;
		try {
			r = o.getJob();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (true){
			if (r !=null ){
				debug("get a job");
				r.run();
			}else{
				try {
					o.helperLock.lock();
					o.jobQueueNotEmpty.await();
					o.helperLock.unlock();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				r = o.getJob();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
/**
 * A simple thread pool implementation
 * 
 * @author Mosharaf Chowdhury (http://www.mosharaf.com)
 * @author Prashanth Mohan (http://www.cs.berkeley.edu/~prmohan)
 * 
 * Copyright (c) 2012, University of California at Berkeley
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of University of California, Berkeley nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

