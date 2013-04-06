package edu.berkeley.cs162;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Scanner;

//fuck gqc
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
		
		ClientThread cThread2 = new ClientThread();
		cThread2.start();
	}
	

	/**
	 * The thread that runs KVServer codes
	 * @author JL
	 *
	 */
	private class ServerThread extends Thread implements Debuggable{
	
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
	private class ClientThread extends Thread implements Debuggable{
		
		public ClientThread(){
			super();
			this.setName("ClientThread"+Test.clientThreadCounter++);
		}
		/**
		 * 
		 */
		public void run(){
			DEBUG.debug("starting client");
			KVClient kc = new KVClient("localhost", 8080);
			try{
				String three = "3";
				String seven = "7";
				DEBUG.debug("putting (3, 7)");
				boolean status = kc.put(three, seven);
				DEBUG.debug("status: " + status);
			}catch(Exception e){
				DEBUG.debug("error");
				e.printStackTrace();
			}
		}
	}

}
