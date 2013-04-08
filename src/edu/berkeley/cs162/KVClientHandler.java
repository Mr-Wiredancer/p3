package edu.berkeley.cs162;

import java.io.IOException;
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
		
		/**
		 * send the message back to client
		 * @param msg
		 */
		private void sendback( KVMessage msg){
			//TODO NEED TO IMPLEMENT
			OutputStream out = null;
			try {
				out= this.client.getOutputStream();
				PrintWriter writer = new PrintWriter(out, true);
				writer.println(msg.toXML());
			} catch (IOException e) {
				DEBUG.debug("cannot open output stream of socket to send back msg to client"); // TODO: Client side needa handle the situation that no response from the server
				e.printStackTrace();
				return;
			} catch (KVException e) {
				//ignore this exception (impossible to happen cuz msg is generated by server)
				DEBUG.debug("this error should not happen");
				e.printStackTrace();
				return;
			}			
			
			try {
				out.close();
			} catch (IOException e) {
				DEBUG.debug("could not close the output stream");
				e.printStackTrace();
			}
		}
		
		private void handlePut(KVMessage msg){
			//TODO NEED TO IMPLEMENT
		}
		
		private void handleDel(KVMessage msg){
			
		}
		
		private void handleGet(KVMessage msg){
			String val = null;
			try {
				val = this.kvServer.get(msg.getKey());
			} catch (KVException e) {
				this.sendback(e.getMsg());
			}
		
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
				this.sendback(successMsg);
			}else{
				DEBUG.debug("this should not be printed");
			}
		}
		
		@Override
		public void run() {
		     try {
		    	 
				KVMessage msg = new KVMessage(client.getInputStream());
				
				//at this point, the msg is a valid KVMessage
				DEBUG.debug("the job is: "+ msg.toXML());
				//get request
				if ( msg.getMsgType()==KVMessage.GETTYPE){
					handleGet(msg);
				
				//put request	
				}else if ( msg.getMsgType()==KVMessage.PUTTYPE){
					handlePut(msg);
					
				//del request	
				}else if (msg.getMsgType()==KVMessage.DELTYPE){
					handleDel(msg);
				}else{
					//resp request
					this.sendback(new KVMessage(KVMessage.RESPTYPE, "Unknown Error: server received a response message"));
				}
				
			} catch (KVException e) {
				//exception when getting KVMessage from the socket's input stream
				this.sendback(e.getMsg());
			} catch (IOException e) {
				//cannot open the inputstream of the socket
				try {
					this.sendback(new KVMessage(KVMessage.RESPTYPE, "Network Error: Could not receive data"));
				} catch (KVException e1) {
					//ignore this exception (impossible)
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

