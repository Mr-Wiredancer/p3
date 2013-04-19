package edu.berkeley.cs162;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test functionalities of kvstore. You'd better change the delay time in AutoGrader.java before running the tests.
 * @author amos0528
 *
 */
public class KVClientTest {
  static KVClient  client;
  
  /**
	 * The thread that runs KVServer codes
	 * @author JL
	 *
	 */
	private static class ServerThread extends Thread implements Debuggable {
	
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
			try {
				server.connect();
				DEBUG.debug("Starting Server");
				server.run();
			} catch (Exception e) {
				DEBUG.debug("server shut down because of errors");
				e.printStackTrace();
			}
		}
	}
	/**
	 * set store to a clean state
	 */
	@BeforeClass
	public static void reset(){
		client = new KVClient("localhost", 8080);
		new ServerThread().start();
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
  
  /** Test successful put. */
  @Test
  public void testPut1() {
    try {
      client.put("key1", "value1");
      String result = client.get("key1");
      assertEquals("value1", result);
    } catch (KVException e) {
      fail();
    }
  }
  
  /** Test multiple puts. */
  @Test
  public void testPut2() {
    try {
      client.put("key1", "value1");
      client.put("key1", "value2");
      assertEquals("value2", client.get("key1"));
    } catch (KVException e) {
      fail();
    }
  }
  
  /** Test successful get. */
  @Test
  public void testGet1() {
    try {
      client.put("key1", "value1");
      String result = client.get("key1");
      assertEquals("value1", result);
    } catch (KVException e) {
      fail();
    }
  }
  
  /** Test get with nonexistent key. */
  @Test(expected = KVException.class)
  public void testGet2() throws KVException {
    String result = client.get("key3");
  }
  
  /** Test successful del. */
  @Test(expected = KVException.class)
  public void testDel1() throws KVException {
    client.put("key1", "value1");
    client.del("key1");
    client.get("key1");
  }
  
  /** Test del with nonexistent key. */
  @Test(expected = KVException.class)
  public void testDel2() throws KVException {
    client.del("key");
  }
}
