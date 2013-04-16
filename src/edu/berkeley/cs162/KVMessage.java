package edu.berkeley.cs162;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * This is the object that is used to generate messages the XML based messages 
 * for communication between clients and servers. 
 */
public class KVMessage implements Debuggable{

	public static final String GETTYPE = "getreq";
	public static final String PUTTYPE = "putreq";
	public static final String DELTYPE = "delreq";
	public static final String RESPTYPE = "resp";
  
	public static final int MAX_KEY_LENGTH = 256;
	public static final int MAX_VALUE_LENGTH = 256*1024;

	private String msgType = null;
	private String key = null;
	private String value = null;
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
	public static class NoCloseInputStream extends FilterInputStream {
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
		if ( msgType!=KVMessage.DELTYPE && msgType!=KVMessage.GETTYPE && msgType!=KVMessage.PUTTYPE && msgType!=KVMessage.RESPTYPE ){	
			DEBUG.debug("Could not recognize the msgType: "+msgType);
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "Message format incorrect"));
		}
		this.msgType = msgType;
	}
	
	public KVMessage(String msgType, String message) throws KVException {
		if ( msgType!=KVMessage.RESPTYPE ){	
			DEBUG.debug("Only a resp type can have a message field");
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
    	try {
    		
    		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    		Document doc = dBuilder.parse(input);
    		
    		doc.getDocumentElement().normalize();
    		
    		//check doc structure
    		checkDocStructure(doc);
    		
    		Node message = doc.getFirstChild();
    		
    		String type = checkKVMessageStructure(message);
    		this.msgType = type;
    		
    		NodeList nodes = message.getChildNodes();
    		if (type.equals(KVMessage.PUTTYPE)){
    			checkKeyNode(nodes.item(0));
    			checkValNode(nodes.item(1));
    		}else if(type.equals(KVMessage.GETTYPE)){
    			checkKeyNode(nodes.item(0));
    		}else if(type.equals(KVMessage.DELTYPE)){
    			checkKeyNode(nodes.item(0));
    		}else{
    			if (nodes.getLength()==1){
	    			checkMessageNode(nodes.item(0));
    			}else if(nodes.getLength()==2){
    				checkKeyNode(nodes.item(0));
    				checkValNode(nodes.item(1));
    			}else{
    				throw new KVException(new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message"));
    			}
    		}
    		
		} catch (ParserConfigurationException e) {
			//this should not happen
			DEBUG.debug("This should not happen");
			e.printStackTrace();
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "Unknown Error: this should not happen") );
		} catch (SAXException e) {
			//not a valid XML
			DEBUG.debug("Invalid XML");
			e.printStackTrace();
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );
		} catch (IOException e) {
			//io error
			DEBUG.debug("Could not receive data");
			e.printStackTrace();
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "Network Error: Could not receive data") );
		}
	}
	
	/**
	 * Check messageNode structure( no attribute and a child of Document.TEXT_NODE). Will set this.message if the node is valid.
	 * @param keyNode
	 * @throws KVException
	 */
	private void checkMessageNode(Node messageNode) throws KVException{
		if (messageNode.getAttributes().getLength()!=0)
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );

		NodeList nodes = messageNode.getChildNodes();
		if ( nodes.getLength()!=1 || nodes.item(0).getNodeType()!=Document.TEXT_NODE)
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );

		String message = messageNode.getFirstChild().getTextContent();
		this.message = message;
	}
	
	/**
	 * Check keynode structure( no attribute and a child of Document.TEXT_NODE). Will set this.key if the node is valid.
	 * @param keyNode
	 * @throws KVException
	 */
	private void checkKeyNode(Node keyNode) throws KVException{
		if (keyNode.getAttributes().getLength()!=0)
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );

		NodeList nodes = keyNode.getChildNodes();
		if ( nodes.getLength()!=1 || nodes.item(0).getNodeType()!=Document.TEXT_NODE)
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );

		String key = keyNode.getFirstChild().getTextContent();
		
		CheckHelper.sanityCheckKey(key);
		
		this.key = key;
	}
	
	/**
	 * Check valNode structure( no attribute and a child of Document.TEXT_NODE). Will set this.value if the node is valid.
	 * @param keyNode
	 * @throws KVException
	 */
	private void checkValNode(Node valNode) throws KVException{
		if (valNode.getAttributes().getLength()!=0)
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );

		NodeList nodes = valNode.getChildNodes();
		if ( nodes.getLength()!=1 || nodes.item(0).getNodeType()!=Document.TEXT_NODE)
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );

		String val = valNode.getFirstChild().getTextContent();
		
		CheckHelper.sanityCheckValue(val);

		this.value = val;
	}
	
	/**
	 * Check attributes of KVMessage( should only have one attribute type, which is one of the 4 types),
	 * and check structure of KVMessage according to type (number and names of children).
	 * @param messageNode
	 * @return type of the KVmessage
	 * @throws KVException
	 */
	private String checkKVMessageStructure(Node messageNode) throws KVException{
		NamedNodeMap attrs = messageNode.getAttributes();
		
		if (attrs.getLength()!=1)
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );

		Node attr = attrs.item(0);
		String attrName = attr.getNodeName();
		String attrValue = attr.getNodeValue();
		if (!attrName.equals("type"))
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );

		if (attrValue.equals(KVMessage.PUTTYPE)){
			checkPutTypeMessage(messageNode);
		}else if (attrValue.equals(KVMessage.GETTYPE)){
			checkGetTypeMessage(messageNode);
		}else if(attrValue.equals(KVMessage.DELTYPE)){
			checkDelTypeMessage(messageNode);
		}else if(attrValue.equals(KVMessage.RESPTYPE)){
			checkRespTypeMessage(messageNode);
		}else{
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );
		}
		return attrValue;
		
	}
	
	/**
	 * check if messageNode is a resp type message( either a messageNode or a keyNode & valueNode)
	 * @param messageNode
	 * @throws KVException
	 */
	private void checkRespTypeMessage(Node messageNode) throws KVException{
		NodeList nodes = messageNode.getChildNodes();
			
		if (nodes.getLength()==1){

			Node message = nodes.item(0);
			
			if (!message.getNodeName().equals("Message"))
				throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );
		
		}else if(nodes.getLength()==2){
			Node keyNode = nodes.item(0);
			Node valNode = nodes.item(1);

			if (!keyNode.getNodeName().equals("Key") || !valNode.getNodeName().equals("Value"))
				throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );
		}else{
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );
		}
	}
	
	/**
	 * check if messageNode is a valid Del message ( 1 keyNode )
	 * @param messageNode
	 * @throws KVException
	 */
	private void checkDelTypeMessage(Node messageNode) throws KVException{
		NodeList nodes = messageNode.getChildNodes();
			
		if (nodes.getLength()!=1)
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );

		Node key = nodes.item(0);
		
		if (!key.getNodeName().equals("Key"))
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );
		
	}
	
	/**
	 * check if messageNode is a valid Get message ( 1 keyNode )
	 * @param messageNode
	 * @throws KVException
	 */
	private void checkGetTypeMessage(Node messageNode) throws KVException{
	NodeList nodes = messageNode.getChildNodes();
		
		if (nodes.getLength()!=1)
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );

		Node key = nodes.item(0);
		
		if (!key.getNodeName().equals("Key"))
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );
	}
	
	/**
	 * Check if messageNode is a valid Put message ( 1 keyNode and 1 valueNode ).
	 * @param messageNode
	 * @throws KVException
	 */
	private void checkPutTypeMessage(Node messageNode) throws KVException{
		NodeList nodes = messageNode.getChildNodes();
		
		if (nodes.getLength()!=2)
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );

		Node key = nodes.item(0);
		Node value = nodes.item(1);
		
		if (!key.getNodeName().equals("Key") || !value.getNodeName().equals("Value"))
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "XML Error: Received unparseable message") );
	}
	
	/**
	 * Check encoding, XML version, and structure(number and names of children). 
	 * @param doc
	 * @throws KVException
	 */
	private void checkDocStructure(Document doc) throws KVException{
		//doc shoudl have only one child with name KVMessage	
		if ( !doc.getXmlEncoding().equals("UTF-8") 
				|| !doc.getXmlVersion().equals("1.0")
				|| doc.getChildNodes().getLength()!=1 
				|| !doc.getFirstChild().getNodeName().equals("KVMessage"))
			throw new KVException( new KVMessage(KVMessage.RESPTYPE,"XML Error: Received unparseable message"));
	}
	
	/**
	 * Generate the XML representation for this message.
	 * @return the XML String
	 * @throws KVException if not enough data is available to generate a valid KV XML message
	 */
	public String toXML() throws KVException {
    	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();

		} catch (ParserConfigurationException e) {
			DEBUG.debug("this should not happen");
			e.printStackTrace();
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "this should not happen"));
		}
 		
		// root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("KVMessage");
		rootElement.setAttribute("type", this.msgType);
		doc.setXmlStandalone(true);
		doc.appendChild(rootElement);
		
		if (this.msgType.equals(KVMessage.PUTTYPE)){
			CheckHelper.sanityCheckKeyValue(this.key, this.value);
			
			Element keyElement = doc.createElement("Key");
			keyElement.appendChild(doc.createTextNode(this.key));
			
			Element valElement = doc.createElement("Value");
			valElement.appendChild(doc.createTextNode(this.value));	
			
			rootElement.appendChild(keyElement);
			rootElement.appendChild(valElement);
		}else if(this.msgType.equals(KVMessage.GETTYPE) || this.msgType.equals(KVMessage.DELTYPE)){
			CheckHelper.sanityCheckKey(this.key);
			
			Element keyElement = doc.createElement("Key");
			keyElement.appendChild(doc.createTextNode(this.key));
		
			rootElement.appendChild(keyElement);
		}else{
			if (this.message!=null){
				Element messageElement = doc.createElement("Message");
				messageElement.appendChild(doc.createTextNode(this.message));
			
				rootElement.appendChild(messageElement);
			}else{
				CheckHelper.sanityCheckKeyValue(this.key, this.value);
					
				Element keyElement = doc.createElement("Key");
				keyElement.appendChild(doc.createTextNode(this.key));
				
				Element valElement = doc.createElement("Value");
				valElement.appendChild(doc.createTextNode(this.value));	
				
				rootElement.appendChild(keyElement);
				rootElement.appendChild(valElement);
			}
		}
		
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			DEBUG.debug("this should not happen either");
			e.printStackTrace();
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "this should not happen"));
		}
		
		StringWriter writer = new StringWriter();
	
		DOMSource  source= new DOMSource(doc);
		StreamResult result = new StreamResult(writer);
 
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			//this should not happen
			DEBUG.debug("this should not happen either");
			e.printStackTrace();
			throw new KVException( new KVMessage(KVMessage.RESPTYPE, "this should not happen"));
		}
				
		String xml = writer.toString();
		return xml;
	}
	
	/**
	 * Open the socket's output stream and send the message as XML
	 * @param sock
	 * @throws KVException error when dumping message as XML, could not open output stream or cannot close output stream
	 */
	public void sendMessage(Socket sock) throws KVException {
		String msg = this.toXML();
		try {
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			out.println(msg);
			sock.shutdownOutput();
		} catch (IOException e) {
			e.printStackTrace();
			throw new KVException(new KVMessage(KVMessage.RESPTYPE, "Unkown Error: IO error in sendMessage"));
		}
	}	
}

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

