package edu.berkeley.cs162;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.io.*;
import java.net.*;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * This is the object that is used to generate messages the XML based messages 
 * for communication between clients and servers. 
 */
public class KVMessage {
  public static final String GETTYPE = "getreq";
  public static final String PUTTYPE = "putreq";
  public static final String DELTYPE = "delreq";
  public static final String RESPTYPE = "resp";
  
  public static final int MAX_KEY_LENGTH = 256;
  public static final int MAX_VALUE_LENGTH = 256*1024;



	private String msgType = null;
	private String key = null;
	private String value = null;
	private String status = null;
	private String message = null;
	
	public final String getKey() {
		return key;
	}

	public final void setKey(String key) {
		this.key = key;
	}

	public final String getValue() {
		return value;
	}

	public final void setValue(String value) {
		this.value = value;
	}

	public final String getStatus() {
		return status;
	}

	public final void setStatus(String status) {
		this.status = status;
	}

	public final String getMessage() {
		return message;
	}

	public final void setMessage(String message) {
		this.message = message;
	}

	public String getMsgType() {
		return msgType;
	}

	/* Solution from http://weblogs.java.net/blog/kohsuke/archive/2005/07/socket_xml_pitf.html */
	private class NoCloseInputStream extends FilterInputStream {
	    public NoCloseInputStream(InputStream in) {
	        super(in);
	    }
	    
	    public void close() {} // ignore close
	}
	
	/***
	 * 
	 * @param msgType
	 * @throws KVException of type "resp" with message "Message format incorrect" if msgType is unknown
	 */
	public KVMessage(String msgType) throws KVException {
	    // TODO: implement me
		if ( msgType!=KVMessage.DELTYPE && msgType!=KVMessage.GETTYPE && msgType!=KVMessage.PUTTYPE && msgType!=KVMessage.RESPTYPE ){	
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "Message format incorrect"));
		}
		this.msgType = msgType;
	}
	
	public KVMessage(String msgType, String message) throws KVException {
        // TODO: implement me
		if ( msgType!=KVMessage.RESPTYPE ){	
			throw new KVException(new KVMessage(KVMessage.RESPTYPE, "Message format incorrect"));
		}
		this.message = message;
		this.msgType = msgType;
		
	
	}
	
	 /***
     * Parse KVMessage from incoming network connection
     * @param sock
     * @throws KVException if there is an error in parsing the message. The exception should be of type "resp and message should be :
     * a. "XML Error: Received unparseable message" - if the received message is not valid XML.
     * b. "Network Error: Could not receive data" - if there is a network error causing an incomplete parsing of the message.
     * c. "Message format incorrect" - if there message does not conform to the required specifications. Examples include incorrect message type. 
     */
	public KVMessage(InputStream input) throws KVException {
	     // TODO: implement me
		BufferedReader in;
		in = new BufferedReader(new InputStreamReader(input));
		try {
			String msg = in.readLine();
			System.out.println(Thread.currentThread().getName()+": "+msg);
			String[] fields = msg.split(",");
			System.out.println(Thread.currentThread().getName()+": "+fields[0]+";"+fields[1]);
			if ( fields[0].equals(KVMessage.PUTTYPE) ){
				this.msgType = fields[0];
				this.key = fields[1];
				this.value = fields[2];	
			}else if( fields[0].equals(KVMessage.GETTYPE)){
				this.msgType = fields[0];
				this.key = fields[1];
			}else if( fields[0].equals(KVMessage.DELTYPE) ){
				this.msgType = fields[0];
				this.key = fields[1];
			}else if(fields[0].equals(KVMessage.RESPTYPE)){
				this.msgType = fields[0];
				this.message = fields[1];
			}else{
				throw new KVException(new KVMessage(KVMessage.RESPTYPE, "Message format incorrect"));
			}
			
		//this is case b	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new KVException(new KVMessage(KVMessage.RESPTYPE, "Network Error: Could not receive data"));
		}
		
//		DocumentBuilderFactory builderFactory =
//		        DocumentBuilderFactory.newInstance();
//		DocumentBuilder builder = null;
//		try {
//		    builder = builderFactory.newDocumentBuilder();
//		} catch (ParserConfigurationException e) {
//		    e.printStackTrace();  
//		}
//		try{
//			Document dom = builder.parse(input);
//			if (!isValidMessage(dom)){
//				this.msgType = KVMessage.RESPTYPE;
//				this.message = "Message format incorrect";
//			}
//			
//		} catch (SAXException e) {
//		    this.msgType = KVMessage.RESPTYPE;
//		    this.message = "XML Error: Received unparseable message";
//		    throw new KVException(this);
//		} catch (IOException e) {
//			this.msgType = KVMessage.RESPTYPE;
//			this.message = "Network Error: Could not receive data";
//			throw new KVException(this);
//		}
		
	}
	
	private boolean isValidMessage(Document dom){
		return true;
//		try{
//			TransformerFactory tf = TransformerFactory.newInstance();
//			Transformer transformer = tf.newTransformer();
//			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//			StringWriter writer = new StringWriter();
//			transformer.transform(new DOMSource(dom), new StreamResult(writer));
//			String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
//			
//			if ( output.matches("\\<\\?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"getreq\"><Key>%s</Key></KVMessage>")){
//				
//			}
//			
//			return true;
//		}catch (Exception e){
//			return false;
//		}
	}
	
	/**
	 * Generate the XML representation for this message.
	 * @return the XML String
	 * @throws KVException if not enough data is available to generate a valid KV XML message
	 */
	public String toXML() throws KVException {
	    // TODO: implement me
		if ( this.msgType.equals(KVMessage.GETTYPE) )
			return String.format("%s,%s", KVMessage.GETTYPE, this.key);
//			return String.format(KVMessage.GET_TEMPLATE, this.key);
		else if( this.msgType .equals( KVMessage.PUTTYPE) )
			return String.format("%s,%s,%s", KVMessage.PUTTYPE, this.key, this.value);
//			return String.format(KVMessage.PUT_TEMPLATE, this.key, this.value);
		else if( this.msgType .equals( KVMessage.DELTYPE ))
			return String.format("%s,%s", KVMessage.DELTYPE, this.key);
			//			return String.format(KVMessage.DEL_TEMPLATE, this.key);
		else if( this.msgType .equals( KVMessage.RESPTYPE))
			return String.format("%s,%s", KVMessage.RESPTYPE, this.message);
		else
			throw new KVException(this);
			
	}
	
	public void sendMessage(Socket sock) throws KVException {
	      // TODO: implement me
		String msg = this.toXML();
		try {
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			out.println(msg);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static final String GET_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"getreq\"><Key>%s</Key></KVMessage>";
	private static final String PUT_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"putreq\"><Key>%s</Key><Value>%s</Value></KVMessage>";
	private static final String DEL_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"putreq\"><Key>%s</Key></KVMessage>";
}

/**
 * Get Value Request:
<?xml version="1.0" encoding="UTF-8"?>
<KVMessage type="getreq">
<Key>key</Key>
</KVMessage>

Put Value Request:
<?xml version="1.0" encoding="UTF-8"?>
<KVMessage type="putreq">
<Key>key</Key>
<Value>value</Value>
</KVMessage>

Delete Value Request:
<?xml version="1.0" encoding="UTF-8"?>
<KVMessage type="delreq">
<Key>key</Key>
</KVMessage>

Successful Get Response:
<?xml version="1.0" encoding="UTF-8"?>
<KVMessage type="resp">
<Key>key</Key>
<Value>value</Value>
</KVMessage>

Successful Put Response:
<?xml version="1.0" encoding="UTF-8"?>
<KVMessage type="resp">
<Message>Success</Message>
</KVMessage>

Successful Delete Response:
<?xml version="1.0" encoding="UTF-8"?>
<KVMessage type="resp">
<Message>Success</Message>
</KVMessage>

Unsuccessful Get/Put/Delete Response:
<?xml version="1.0" encoding="UTF-8"?>
<KVMessage type="resp">
<Message>Error Message</Message>
</KVMessage>
 */

/**
 * XML Parsing library for the key-value store
 * 
 * @author Mosharaf Chowdhury (http://www.mosharaf.com)
 * @author Prashanth Mohan (http://www.cs.berkeley.edu/~prmohan)
 * 
 * Copyright (c) 2012, University of California at Berkeley
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of University of California, Berkeley nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

