package edu.berkeley.cs162;

import static org.junit.Assert.*;

public class Test {

	@org.junit.Test
	public void test() {
		System.out.println("Binding Server:");
		KVServer key_server = new KVServer(100, 10);
		SocketServer server = new SocketServer("localhost", 4040);
		NetworkHandler handler = new KVClientHandler(key_server);
		server.addHandler(handler);
		try{
		server.connect();
		System.out.println("Starting Server");
		server.run();
		}catch(Exception e){
			System.out.println(e);
		}
	}

}
