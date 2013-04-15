package edu.berkeley.cs162;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

public class KVMessageTest {
	@Test
	public void constructorFromInputTest(){
		try{
			KVMessage msg = new KVMessage(KVMessage.GETTYPE);
			msg.setKey("key1");
			msg.setValue("val1");
			String xml = msg.toXML();
			InputStream in = new ByteArrayInputStream(xml.getBytes());
			
			KVMessage newMsg = new KVMessage(in);
			assertEquals(newMsg.getKey(), "key1");
			assertEquals(newMsg.getValue(), null);
			
			xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"resp\"><Message>this is a test msg</Message></KVMessage>";
			in = new ByteArrayInputStream(xml.getBytes());
			
			newMsg = new KVMessage(in);
			assertEquals(newMsg.getMsgType(), KVMessage.RESPTYPE);
			assertEquals(newMsg.getMessage(), "this is a test msg");
			assertEquals(newMsg.getValue(), null);
			assertEquals(newMsg.getKey(), null);
			

		}catch(KVException e){
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
		try{
			KVMessage msg = new KVMessage(KVMessage.GETTYPE);
			msg.toXML();
		}catch(KVException e){
			assertEquals(e.getMsg().getMessage(), "Unknown Error: not enough data to build XML");
		}
		
		try{
			KVMessage msg = new KVMessage(KVMessage.GETTYPE);
			msg.setKey("key1");
			System.out.println(msg.toXML());
		}catch(KVException e){
			fail();
		}
		
		try{
			KVMessage msg = new KVMessage(KVMessage.PUTTYPE);
			msg.setValue("value1");
			msg.toXML();
		}catch(KVException e){
			assertEquals(e.getMsg().getMessage(), "Unknown Error: not enough data to build XML");
		}
		
		try{
			KVMessage msg = new KVMessage(KVMessage.PUTTYPE);
			msg.setMessage("value1");
			msg.toXML();
		}catch(KVException e){
			assertEquals(e.getMsg().getMessage(), "Unknown Error: not enough data to build XML");
		}
		
		try{
			KVMessage msg = new KVMessage(KVMessage.RESPTYPE);
			msg.setMessage("value1");
			msg.toXML();
		}catch(KVException e){
			fail();
		}
		
		try{
			KVMessage msg = new KVMessage(KVMessage.GETTYPE);
			msg.setKey("1234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678");
			msg.toXML();
		}catch(KVException e){
			assertEquals(e.getMsg().getMessage(), "Oversized key");
		}

	}
}
