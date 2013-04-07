/**
 * Persistent Key-Value storage layer. Current implementation is transient, 
 * but assume to be backed on disk when you do your project.
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
package edu.berkeley.cs162;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * This is a dummy KeyValue Store. Ideally this would go to disk, 
 * or some other backing store. For this project, we simulate the disk like 
 * system using a manual delay.
 *
 */
public class KVStore implements KeyValueInterface {
	private Dictionary<String, String> store 	= null;
	
	public KVStore() {
		resetStore();
	}

	private void resetStore() {
		store = new Hashtable<String, String>();
	}
	
	/**
	 * Put the <key, value> pair in the store. Return true if key is already in the store; false otherwise.
	 */
	public synchronized boolean put(String key, String value) throws KVException {
		AutoGrader.agStorePutStarted(key, value);
		
		try {
			putDelay();
			if (store.get(key)==null){
				store.put(key, value);
				return false;
			}else{
				if (store.get(key).equals(value))
					return false;
				
				//overwritting happens
				store.put(key, value);
				return true;
			}
		} finally {
			AutoGrader.agStorePutFinished(key, value);
		}
	}
	
	public synchronized String get(String key) throws KVException {
		AutoGrader.agStoreGetStarted(key);
		
		try {
			getDelay();
			String retVal = this.store.get(key);
			if (retVal == null) {
//			    KVMessage msg = new KVMessage("resp", "key \"" + key + "\" does not exist in store");
				KVMessage msg = new KVMessage(KVMessage.RESPTYPE, "Does not exsit");
				throw new KVException(msg);
			}
			return retVal;
		} finally {
			AutoGrader.agStoreGetFinished(key);
		}
	}
	
	/**
	 * Delete the value which is mapped to key. If the key does not exist, throw a KVException.
	 */
	public synchronized void del(String key) throws KVException {
		AutoGrader.agStoreDelStarted(key);

		try {
			delDelay();
			String val;
			if(key != null){
				val = this.store.remove(key);
				if (val==null)
					throw new KVException(new KVMessage(KVMessage.RESPTYPE, "Does not exist"));
			}
		} finally {
			AutoGrader.agStoreDelFinished(key);
		}
	}
	
	private void getDelay() {
		AutoGrader.agStoreDelay();
	}
	
	private void putDelay() {
		AutoGrader.agStoreDelay();
	}
	
	private void delDelay() {
		AutoGrader.agStoreDelay();
	}
	
    public synchronized String toXML() throws KVException {
        // TODO: implement me
    	return this.storeToXML();
    }        
    
    /**
     * helper method to output store as XML;
     * @return XML representation of store
     */
    private String storeToXML(){
    	//TODO: implement me. For now it is just JL-representation. Need to change to XML. 
    	String result = "";
    	
    	Enumeration<String> e = store.keys();
    	while (e.hasMoreElements()){
    		String key = e.nextElement();
    		String val = store.get(key);
    		result+=String.format("%s,%s\n", key, val);
    	}
    	return result;   	
    }

    /**
     * Dump the current state of store to corresponding file. This does not change the state of the store
     * @param fileName 
     * @throws KVException when file not found and cannot create the file
     */
    public synchronized void dumpToFile(String fileName) throws KVException {
    	try {
			PrintWriter out = new PrintWriter(fileName);
			out.print(this.storeToXML());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new KVException(
					new KVMessage(KVMessage.RESPTYPE, "Unknown Error: could not open/create file "+fileName));
		}
    }

    /**
     * This dummy helper reads the file of JL-representation instead of XML. May be deleted later
     * @param in
     * @return
     * @throws KVException
     * @throws IOException
     */
    private Hashtable restoreHelper(BufferedReader in) throws KVException{
    	Hashtable<String, String> newStore = new Hashtable<String, String>();
    	
    	String i;
    	try {
			while (( i = in.readLine())!=null){
				i = i.replace("\n", "").replace("\r", "");
				String [] pair = i.split(",");
				
				//not key,value format
				if (pair.length!=2) throw new KVException(new KVMessage(KVMessage.RESPTYPE,"Unknown Error: Could not recognize the format of the file"));    		
				//oversized key
				if (pair[0].length()>256) throw new KVException(new KVMessage(KVMessage.RESPTYPE,"Oversized key"));
				//oversized value
				if (pair[1].length()>256*1024) throw new KVException(new KVMessage(KVMessage.RESPTYPE,"Oversized value"));
				
				newStore.put(pair[0], pair[1]);    		    		
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new KVException(
					new KVMessage(KVMessage.RESPTYPE, "Unknown Error: error happens in the file reader"));
			
		}
    	return newStore;
    }
    
    /**
     * restore the state of the store to the one indicated by the file
     * @param fileName 
     * @throws KVException if file could not be opened or there is error when parsing the XML
     */
    public synchronized void restoreFromFile(String fileName) throws KVException{
    	//TODO: implement me. For now it assumes the file format is JL-representation. Need to change to XML. 
    	try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			
			//change this to XML builder later
			Hashtable newStore = this.restoreHelper(in);
			
			this.store = newStore;			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new KVException(
					new KVMessage(KVMessage.RESPTYPE, "Unknown Error: could not open the fiile "+fileName));			
		} 
    }
}
