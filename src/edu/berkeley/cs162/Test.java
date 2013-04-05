package edu.berkeley.cs162;

import static org.junit.Assert.*;

import java.io.IOException;

public class Test {

	private class ServerThread extends Thread{
	
		public void run(){
			System.out.println("Binding Server:");
			KVServer key_server = new KVServer(100, 10);
			SocketServer server = new SocketServer("localhost", 8080);
			NetworkHandler handler = new KVClientHandler(key_server);
			server.addHandler(handler);
			try{
				server.connect();
				System.out.println("Starting Server");
				System.out.println("thread id:"+Thread.currentThread().getId());
				server.run();
			}catch (Exception e){
				System.out.println("unable to start server");
			}
		}
	}
	
	private class ClientThread extends Thread{
		public void run(){
			System.out.println("starting client, thread id is: "+ Thread.currentThread().getId());
			KVClient kc = new KVClient("localhost", 8080);
			try{
				String three = "3";
				String seven = "7";
				System.out.println("putting (3, 7)");
				boolean status = kc.put(three, seven);
				System.out.println("status: " + status);

			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
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

}
