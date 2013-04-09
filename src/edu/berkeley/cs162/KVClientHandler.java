package edu.berkeley.cs162;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This NetworkHandler will asynchronously handle the socket connections. 
 * It uses a threadpool to ensure that none of it's methods are blocking.
 *
 */
public class KVClientHandler implements NetworkHandler, Debuggable {
	private KVServer kv_Server = null;
	private ThreadPool threadpool = null;
		
	public KVClientHandler(KVServer kvServer) {
		initialize(kvServer, 1);
	}

	public KVClientHandler(KVServer kvServer, int connections) {
		initialize(kvServer, connections);
	}

	private void initialize(KVServer kvServer, int connections) {
		this.kv_Server = kvServer;
		threadpool = new ThreadPool(connections);	
	}
	

	private class ClientHandler implements Runnable {
		private KVServer kvServer = null;
		private Socket client = null;
			
		private void handlePut(KVMessage msg){
			try {
				this.kvServer.put(msg.getKey(), msg.getValue());
			} catch (KVException e) {
				try {
					e.getMsg().sendMessage(this.client);
				} catch (KVException e1) {
					DEBUG.debug("error happens when trying to send back message");
					e1.printStackTrace();
				}
				return;
			}
			
			try{
				KVMessage successMsg = new KVMessage(KVMessage.RESPTYPE, "Success");
				successMsg.sendMessage(this.client);
			}catch(KVException e){
				DEBUG.debug("error happens when trying to send back message");
				e.printStackTrace();
			}
		}
		
		private void handleDel(KVMessage msg){
			try{
				this.kvServer.del(msg.getKey());
			}catch (KVException e){
				try {
					e.getMsg().sendMessage(this.client);
				} catch (KVException e1) {
					DEBUG.debug("error happens when trying to send back message");
					e1.printStackTrace();
				}
				return;			}
			
			try{
				KVMessage successMsg = new KVMessage(KVMessage.RESPTYPE, "Success");
				successMsg.sendMessage(this.client);
			}catch(KVException e){
				DEBUG.debug("error happens when trying to send back message");
				e.printStackTrace();
			}
		}
		
		private void handleGet(KVMessage msg){
			String val = null;
			try {
				val = this.kvServer.get(msg.getKey());
			} catch (KVException e) {
				try {
					e.getMsg().sendMessage(this.client);
				} catch (KVException e1) {
					DEBUG.debug("error happens when trying to send back message");
					e1.printStackTrace();
				}
				return;
			}
		
			try{
				// val should not be null. kvserver.get return non-null string or KVException.
				if (val!=null){
					//successful get
					KVMessage successMsg = null;
					try {
						successMsg = new KVMessage(KVMessage.RESPTYPE);
					} catch (KVException e) {
						//silence this exception
						DEBUG.debug("this error should not happen");
						e.printStackTrace();
					}
					successMsg.setKey(msg.getKey());
					successMsg.setValue(val);
					successMsg.sendMessage(this.client);
				}else{
					DEBUG.debug("this should not be printed");
				}
			}catch (KVException e){
				DEBUG.debug("error happens when trying to send back message");
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
		     try {
		    	 
		    	InputStream in = client.getInputStream();
		    	 
				KVMessage msg = new KVMessage(new KVMessage.NoCloseInputStream(in));
				
				//at this point, the msg is a valid KVMessage
				DEBUG.debug("the job is: "+ msg.toXML());
				//get request
				if ( msg.getMsgType().equals(KVMessage.GETTYPE)){
					handleGet(msg);
				
				//put request	
				}else if ( msg.getMsgType().equals(KVMessage.PUTTYPE)){
					handlePut(msg);
					
				//del request	
				}else if (msg.getMsgType().equals(KVMessage.DELTYPE)){
					handleDel(msg);
			
				//resp request
				}else{
					try {
						new KVMessage(KVMessage.RESPTYPE, "Unknown Error: server received a response message").sendMessage(this.client);
					} catch (KVException e1) {
						DEBUG.debug("error happens when trying to send back message");
						e1.printStackTrace();
					}
				}
				
			} catch (KVException e) {
				//exception when getting KVMessage from the socket's input stream
				try {
					e.getMsg().sendMessage(this.client);
				} catch (KVException e1) {
					DEBUG.debug("error happens when trying to send back message");
					e1.printStackTrace();
				}
			} catch (IOException e) {
				DEBUG.debug("could not receive data");
				e.printStackTrace();
				try {
					new KVMessage(KVMessage.RESPTYPE, "Network Error: Could not receive data").sendMessage(this.client);
				} catch (KVException e1) {
					DEBUG.debug("error happens when trying to send back message");
					e1.printStackTrace();
				}
			}
		}
		
		public ClientHandler(KVServer kvServer, Socket client) {
			this.kvServer = kvServer;
			this.client = client;
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.berkeley.cs162.NetworkHandler#handle(java.net.Socket)
	 */
	@Override
	public void handle(Socket client) throws IOException {
		DEBUG.debug("creating a new job");
		Runnable r = new ClientHandler(kv_Server, client);
		try {
			threadpool.addToQueue(r);
			DEBUG.debug("added to queue");
		} catch (InterruptedException e) {
			// Ignore this error
			return;
		}
	}
}
/**
 * Handle client connections over a socket interface
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

