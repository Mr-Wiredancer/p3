package edu.berkeley.cs162;

import static org.junit.Assert.*;

import java.io.IOException;

public class Test {
	public static int clientThreadCounter = 0;
	
	@org.junit.Test
	public void test() {
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
	}
	

	/**
	 * The thread that runs KVServer codes
	 * @author JL
	 *
	 */
	private class TestThread extends Thread{
		public void debug(String s){
			System.out.println(Thread.currentThread().getName()+": "+s);
		}
	}
	
	private class ServerThread extends TestThread{
	
		public ServerThread(){
			super();
			this.setName("ServerThread");
		}
		
		public void run(){
			debug("Binding Server:");
			KVServer key_server = new KVServer(100, 10);
			SocketServer server = new SocketServer("localhost", 8080);
			NetworkHandler handler = new KVClientHandler(key_server);
			server.addHandler(handler);
			try{
				server.connect();
				debug("Starting Server");
				server.run();
			}catch (Exception e){
				debug("unable to start server");
			}
		}
	}
	
	/**
	 * The server that runs KVServer code
	 * @author JL
	 *
	 */
	private class ClientThread extends TestThread{
		
		public ClientThread(){
			super();
			this.setName("ClientThread"+Test.clientThreadCounter);
		}
		/**
		 * 
		 */
		public void run(){
			debug("starting client");
			KVClient kc = new KVClient("localhost", 8080);
			try{
				String three = "3";
				String seven = "7";
				debug("putting (3, 7)");
				boolean status = kc.put(three, seven);
				debug("status: " + status);

			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

}
