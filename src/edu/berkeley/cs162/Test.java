package edu.berkeley.cs162;

import static org.junit.Assert.*;

import java.lang.String;
import java.util.HashMap;
import java.util.LinkedList;

public class Test implements Debuggable{
	public static int clientThreadCounter = 0;
	public static HashMap<Integer, KVMessage> messageMap = new HashMap<Integer, KVMessage>();
	public static HashMap<Integer, KVMessage> respMap = new HashMap<Integer, KVMessage>();
	public static ThreadPool threadPool;
	public static LinkedList<Integer> jobQueue = new LinkedList<Integer>();
	public static LinkedList<KVMessage> request, resp;
		
	@org.junit.Test
	public void test() throws Exception{
		testPut();
		testGet();
		testMultipleGet();
		testDoublePut();
		testGetKeyNotInStore();
		testDel();
		testDelKeyNotInStore();
		testPutandDel();
		pressuretest();

	}
	
	public void init(){
		threadPool = null;
		jobQueue = new LinkedList<Integer>();
		messageMap = new HashMap<Integer, KVMessage>();
		respMap = new HashMap<Integer,KVMessage>();
	}
	
	public void print() throws KVException{
		if (jobQueue!=null){
			DEBUG.debug(jobQueue.toString());
		}
		
	}
	
	public void check() throws KVException{
		request = new LinkedList<KVMessage>();
		resp = new LinkedList<KVMessage>();
		while (!jobQueue.isEmpty()){
			int a = jobQueue.pop();
			KVMessage req = Test.messageMap.get(a);
			request.add(req);
			KVMessage res= Test.respMap.get(a);
			resp.add(res);
			DEBUG.debug(req.toXML());
			DEBUG.debug(res.toXML());
			if (req.getMsgType().equals(KVMessage.PUTTYPE)){
				assertTrue(res.getMessage().equals("Success"));
			}
			if (req.getMsgType().equals(KVMessage.DELTYPE)){
				assertTrue(res.getMessage().equals("Success")|res.getMessage().equals("Does not exist"));
			}
			if (req.getMsgType().equals(KVMessage.GETTYPE)){
				boolean t1 = false;
				boolean t2 = false;
				if (res.getKey()!=null)
					t1 = res.getKey().equals(req.getKey());
				if (res.getMessage()!=null)
					t2 = res.getMessage().equals("Does not exist");
				assertTrue(t1|t2);
			}
			
		}
		
	}
	
    public void testPut() throws Exception{
    	init();
    	TestHelper t = new TestHelper("1,put,k1,v1");
		Thread.currentThread().sleep(500);
    	check();
    }
    
    public void testGet() throws Exception{
    	init();
    	TestHelper t = new TestHelper("1,put,k1,v1;2,get,k1");
    	Thread.currentThread().sleep(500);
    	check();
    }
    
    public void testMultipleGet() throws Exception{
    	init();
    	TestHelper t = new TestHelper("1,put,k1,v1;2,get,k1;3,get,k1;4,get,k1;5,get,k1");
    	Thread.currentThread().sleep(500);
    	check();
    }
    

    public void testDoublePut() throws Exception{
    	init();
    	TestHelper t = new TestHelper("1,put,k1,v1;2,put,k1,v2;3,get,k1");
    	Thread.currentThread().sleep(500);
    	check();
    }
    
    public void testGetKeyNotInStore() throws Exception{
    	init();

    	TestHelper t = new TestHelper("1,put,k1,v1;2,get,k2");
    	Thread.currentThread().sleep(500);
    	check();
    }
    
    public void testDel() throws Exception{
    	init();
    	TestHelper t = new TestHelper("1,put,k1,v1;2,del,k1;3,get,k1");
    	Thread.currentThread().sleep(500);
    	check();
    }

    
    public void testDelKeyNotInStore() throws Exception{
    	init();

    	TestHelper t = new TestHelper("1,put,k1,v1;2,del,k2");
    	Thread.currentThread().sleep(500);
    	check();
    }
	
    public void testPutandDel() throws Exception{
    	init();
    	TestHelper t = new TestHelper("1,put,k1,v1;2,del,k1;3,put,k1,v2;4,get,k1");
    	Thread.currentThread().sleep(500);
    	check();    }
    
    public void pressuretest() throws Exception{
    	init();
    	String puts = "";
    	String gets = "";
    	String dels = "";
    	int n= 100;
    	for (int i=1;i<=n;i++){
    		puts = puts + i + ",put,k"+i+",v"+i+";";
    		gets = gets + (i+n) + ",get,k"+i+";";
    		dels = dels + (i+2*n) + ",del,k"+i+";";
    	}
    	TestHelper t = new TestHelper(puts+gets+dels);
    	Thread.currentThread().sleep(2000);
    	check();    	
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
			NetworkHandler handler = new KVClientHandler(key_server, 10);
			Test.threadPool = ((KVClientHandler)handler).getThreadPool();
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
					kc.put(this.key, this.value);
				}else
				if (this.type.equals("get")){
					DEBUG.debug("getting " + this.key );
					String result = kc.get(this.key);
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
