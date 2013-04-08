package edu.berkeley.cs162;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Scanner;
import java.lang.String;
import java.util.HashMap;
import java.util.LinkedList;

public class Test implements Debuggable{
	public static int clientThreadCounter = 0;
	public static boolean status;
	public static String result;
	
	//@org.junit.Test
	/*public void test() {
		ServerThread sThread = new ServerThread();
		sThread.start();
		
		try {
			Thread.currentThread().sleep(500); //wait the server to start
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		ClientThread cThread = new ClientThread();
		cThread.start();
		
		ClientThread cThread2 = new ClientThread();
		cThread2.start();
	}*/
	@org.junit.Test
	public void test() throws Exception{
		//testPut();
		testGet();
		//testDoublePut();
		//testGetKeyNotInStore();
		//testDel();
		//testPutandDel();
	}
	
	public void init(){
		this.status = true;
		this.result = null;
	}
	
	public void print(){
		DEBUG.debug(this.status?"true":"false");
		DEBUG.debug(this.result);
		
	}
	
    public void testPut() throws Exception{
    	init();
    	TestHelper t = new TestHelper("1,put,k1,v1");
		Thread.currentThread().sleep(500);
    	assertTrue(status);
    }
    
    public void testGet() throws Exception{
    	init();
    	TestHelper t = new TestHelper("1,put,k1,v1;2,get,k1");
		Thread.currentThread().sleep(500);
    	assertEquals(this.result, "v1");
    }
    
    
    public void testDoublePut() throws Exception{
    	init();
    	TestHelper t = new TestHelper("1,put,k1,v1;2,put,k1,v2;3,get,k1");
		Thread.currentThread().sleep(500);
    	assertEquals(this.result, "v2");
    }
    
    public void testGetKeyNotInStore() throws Exception{
    	init();
    	try{
    	TestHelper t = new TestHelper("1,put,k1,v1;2,get,k2");
		Thread.currentThread().sleep(500);
    	assertTrue(false); // shouldn't be reached
    	}
    	catch (Exception e){
    		assertTrue(true);
    	}
    }
    
    public void testDel() throws Exception{
    	init();
    	try{
    	TestHelper t = new TestHelper("1,put,k1,v1;2,del,k1;3,get,k1");
		Thread.currentThread().sleep(500);
    	assertTrue(false); // shouldn't be reached
    	}
    	catch (Exception e){
    		assertTrue(true);
    	}
    }
	
    public void testPutandDel() throws Exception{
    	init();
    	TestHelper t = new TestHelper("1,put,k1,v1;2,del,k1;3,put,k1,v2;4,get,k1");
		Thread.currentThread().sleep(500);
    	assertEquals(this.result,"v2");
    }
    
    /* **
	 * The thread that runs KVServer codes
	 * @author JL
	 *
	 */
	private class ServerThread extends Thread implements Debuggable, Runnable{
	
		public ServerThread(){
			super();
			this.setName("ServerThread");
		}
		
		public void run(){
			DEBUG.debug("Binding Server:");
			KVServer key_server = new KVServer(100, 10);
			SocketServer server = new SocketServer("localhost", 8080);
			NetworkHandler handler = new KVClientHandler(key_server);
			server.addHandler(handler);
			try{
				server.connect();
				DEBUG.debug("Starting Server");
				server.run();
			}catch (Exception e){
				DEBUG.debug("server shut down because of errors");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * The thread that runs KVclient code
	 * @author JL
	 *
	 */
	private class ClientThread extends Thread implements Debuggable, Runnable{
		private String type, key, value;
		
		public ClientThread(String type, String key, String value){
			super();
			this.setName("ClientThread"+Test.clientThreadCounter++);
			this.type = type;
			this.key = key;
			this.value = value;
		}
		/**
		 * 
		 */
		public void run(){
			DEBUG.debug("starting client");
			KVClient kc = new KVClient("localhost", 8080);
			try{
				if (this.type.equals("put")){
					DEBUG.debug("putting " + this.key + " " + this.value);
					boolean status = kc.put(this.key, this.value);
					if (!status) Test.status = false; 
				}else
				if (this.type.equals("get")){
					DEBUG.debug("getting " + this.key );
					String result = kc.get(this.key);
					Test.result = result;
				}else
				if (this.type.equals("del")){
					DEBUG.debug("deleting " + this.key );
					kc.del(this.key);
				}else 
					throw new Exception("unknown type");

			}catch(Exception e){
				DEBUG.debug("error");
				e.printStackTrace();
			}
		}
	}
	
	private class TestHelper implements Debuggable {
		
		public TestHelper(String input) throws Exception{
			HashMap<String, Thread> clientMap= new HashMap<String, Thread>();
			clientMap.put("0", new ServerThread());
			ServerThread server = new ServerThread();
			server.start();
			Thread.currentThread().sleep(500);
			
			String[] commands = input.split(";");
			for (String command:commands){
				String[] args = command.split(",");
				// clientID, type, key, value 
				String clientID = args[0];
				String type = args[1];
				String key = args[2];
				String value = null;
				if (args.length == 4) 
					value = args[3];
				if (!clientMap.containsKey(clientID)){
						Runnable r = new ClientThread(type, key, value);
						new Thread(r).start();
						clientMap.put(clientID, (Thread)r);
					}				
			
		}

	}
	}

}
