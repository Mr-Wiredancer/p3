package edu.berkeley.cs162;

import static org.junit.Assert.*;

import org.junit.Test;

public class KVServerTest {
  private KVServer server;

  @Before
  public void reset() {
    server = new KVServer(1, 4);
  }
  
	@Test
	public void testPut() {
    try {
		  server.put("key1", "value1");
    } catch (KVException e) {
      fail();
    }
	}

  @Test
	public void testGet1() {
    try {
      server.put("key1", "value1");
      String result = server.get("key1");
      assertEquals("value1", result);
    } catch (KVException e) {
      fail();
    }
	}
  
  @Test(expected = KVException.class)
	public void testGet2() throws KVException {
		server.get("key1");
	}

  @Test(expected = KVException.class)
	public void testDel1() throws KVException {
    server.put("key1", "value1");
    server.del("key1");
    server.get("key1");
	}
  
  @Test(expected = KVException.class)
	public void testDel2() throws KVException {
		server.del("key1");
	}
}
