package edu.berkeley.cs162;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

public class KVMessageTest {
	@Test
	public void constructorFromInputTest(){
		try {
			KVMessage msg = new KVMessage(KVMessage.GETTYPE);
			msg.setKey("key1");
			msg.setValue("val1");
      assertEquals("key1", msg.getKey());
      assertEquals("val1", msg.getValue());
		} catch(KVException e) {
			fail();
		}
	}
	
  @Test
  public void testReconstructFromXML() {
    try {
    	String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"resp\"><Message>this is a test msg</Message></KVMessage>";
		ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
			
		KVMessage newMsg = new KVMessage(in);
		assertEquals(newMsg.getMsgType(), KVMessage.RESPTYPE);
		assertEquals(newMsg.getMessage(), "this is a test msg");
		assertEquals(newMsg.getValue(), null);
		assertEquals(newMsg.getKey(), null);
    }
    catch (KVException e) {
      fail();
    }
  }
  
  @Test
  public void testToXML() {
     try {
      KVMessage msg = new KVMessage(KVMessage.GETTYPE);
			msg.setKey("key1");
			msg.setValue("val1");
		      assertEquals("key1", msg.getKey());
		      assertEquals("val1", msg.getValue());
      
			String xml = msg.toXML();
			InputStream in = new ByteArrayInputStream(xml.getBytes());			
			KVMessage newMsg = new KVMessage(in);
			assertEquals(newMsg.getKey(), "key1");
     }
     catch (KVException e) {
      fail();
     }
  }
  
	@Test(expected = KVException.class)
	public void getTest1() throws KVException {
		KVMessage msg = new KVMessage(KVMessage.GETTYPE);
		msg.toXML();
	}
	
	@Test
	public void buildXMLFailureTest(){
		try {
			KVMessage msg = new KVMessage(KVMessage.GETTYPE);
			msg.toXML();
		} catch(KVException e) {
			assertEquals(e.getMsg().getMessage(), "Unknown Error: the key is null");
		}
		
		try {
			KVMessage msg = new KVMessage(KVMessage.GETTYPE);
			msg.setKey("key1");
			System.out.println(msg.toXML());
		} catch(KVException e) {
			fail();
		}
		
		try {
			KVMessage msg = new KVMessage(KVMessage.PUTTYPE);
			msg.setValue("value1");
			msg.toXML();
		} catch(KVException e) {
			assertEquals(e.getMsg().getMessage(), "Unknown Error: the key is null");
		}
		
		try {
			KVMessage msg = new KVMessage(KVMessage.PUTTYPE);
			msg.setMessage("value1");
			msg.toXML();
		} catch(KVException e) {
			assertEquals(e.getMsg().getMessage(), "Unknown Error: the key is null");
		}
		
		try {
			KVMessage msg = new KVMessage(KVMessage.RESPTYPE);
			msg.setMessage("value1");
			msg.toXML();
		} catch(KVException e) {
			fail();
		}
		
		try {
			KVMessage msg = new KVMessage(KVMessage.GETTYPE);
			msg.setKey("1234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678");
			msg.toXML();
		} catch(KVException e) {
			assertEquals(e.getMsg().getMessage(), "Oversized key");
		}
	}
}
