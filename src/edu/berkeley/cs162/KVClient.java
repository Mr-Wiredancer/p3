package edu.berkeley.cs162;

import java.net.Socket;
import java.io.*;


/**
 * This class is used to communicate with (appropriately marshalling and unmarshalling) 
 * objects implementing the {@link KeyValueInterface}.
 *
 * @param <K> Java Generic type for the Key
 * @param <V> Java Generic type for the Value
 */
public class KVClient implements KeyValueInterface {

	private String server = null;
	private int port = 0;
	
	/**
	 * @param server is the DNS reference to the Key-Value server
	 * @param port is the port on which the Key-Value server is listening
	 */
	public KVClient(String server, int port) {
		this.server = server;
		this.port = port;
	}
	
	private Socket connectHost() throws KVException {
	    // TODO: Implement Me!  
    Socket socket;
    try{
      socket = new Socket(this.server, port);
      return socket;

    }catch (IOException e){
      throw new KVException(null); 
    }

	}
	
	private void closeHost(Socket sock) throws KVException {
	    // TODO: Implement Me!
		try {
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new KVException(null);
		}
	}
	
	public boolean put(String key, String value) throws KVException {
	    // TODO: Implement Me!
		Socket sock = this.connectHost();
		
		OutputStream out = null;
		InputStream in = null;
		try {
			out = sock.getOutputStream();
			in = sock.getInputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		KVMessage msg = new KVMessage(KVMessage.PUTTYPE);
		msg.setKey(key);
		msg.setValue(value);
		
		PrintWriter writer = new PrintWriter(out, true);
		writer.println(msg.toXML());
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		KVMessage response = new KVMessage(in);//we dont look at the response for now
		
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.closeHost(sock);
		
		return true;
	}


	public String get(String key) throws KVException {
	    // TODO: Implement Me!
		Socket sock = this.connectHost();
		
		OutputStream out = null;
		InputStream in = null;
		try {
			out = sock.getOutputStream();
			in = sock.getInputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		KVMessage msg = new KVMessage(KVMessage.GETTYPE);
		msg.setKey(key);
		
		PrintWriter writer = new PrintWriter(out, true);
		writer.println(msg.toXML());
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		KVMessage response = new KVMessage(in);//we dont look at the response for now
		
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.closeHost(sock);
		
		return response.toXML(); // becuz we dont look at the response for now so we just output the whole message
		
	}
	
	public void del(String key) throws KVException {
	    // TODO: Implement Me!
		Socket sock = this.connectHost();
		
		OutputStream out = null;
		InputStream in = null;
		try {
			out = sock.getOutputStream();
			in = sock.getInputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		KVMessage msg = new KVMessage(KVMessage.DELTYPE);
		msg.setKey(key);
		
		PrintWriter writer = new PrintWriter(out, true);
		writer.println(msg.toXML());
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		KVMessage response = new KVMessage(in);//we dont look at the response for now
		
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.closeHost(sock);
	}	
}
/**
 * Client component for generating load for the KeyValue store. 
 * This is also used by the Master server to reach the slave nodes.
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

