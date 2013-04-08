/**
 * Slave Server component of a KeyValue store
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

/**
 * This class defines the slave key value servers. Each individual KVServer 
 * would be a fully functioning Key-Value server. For Project 3, you would 
 * implement this class. For Project 4, you will have a Master Key-Value server 
 * and multiple of these slave Key-Value servers, each of them catering to a 
 * different part of the key namespace.
 *
 */
public class KVServer implements KeyValueInterface,Debuggable {
	private KVStore dataStore = null;
	private KVCache dataCache = null;
	
	private static final int MAX_KEY_SIZE = 256;
	private static final int MAX_VAL_SIZE = 256 * 1024;
	
	/**
	 * @param numSets number of sets in the data Cache.
	 */
	public KVServer(int numSets, int maxElemsPerSet) {
		dataStore = new KVStore();
		dataCache = new KVCache(numSets, maxElemsPerSet);

		AutoGrader.registerKVServer(dataStore, dataCache);
	}
	
	public boolean put(String key, String value) throws KVException {
		// Must be called before anything else
		AutoGrader.agKVServerPutStarted(key, value);
	
		try{
			DEBUG.debug(String.format("requestd to put <%s, %s> in the store", key, value));
			
			if (key.length()>KVMessage.MAX_KEY_LENGTH){
				throw new KVException( new KVMessage(KVMessage.RESPTYPE, "Oversized key"));
			}
			
			if (value.length()>KVMessage.MAX_VALUE_LENGTH){
				throw new KVException(new KVMessage(KVMessage.RESPTYPE, "OVersized value"));
			}
			
			boolean replacementNeeded = this.dataCache.put(key, value);
			
			boolean result = this.dataStore.put(key, value);
			
			if (replacementNeeded){
				this.dataCache.replace(key, value);
			}
			return result;
		
		}finally{
			AutoGrader.agKVServerPutFinished(key, value);
		}
	}
	
	public String get (String key) throws KVException {
		// Must be called before anything else
		AutoGrader.agKVServerGetStarted(key);
		
		try{
			if (key.length()>KVMessage.MAX_KEY_LENGTH){
				throw new KVException( new KVMessage(KVMessage.RESPTYPE, "Oversized key"));
			}
			
			//try to get the value in cache
			String cacheValue = this.dataCache.get(key);
			if (cacheValue!=null){
				return cacheValue; //directly return the value if the value is in cache
			}
			
			//key is not in cache, try to get the value in the store
			String storeResult = this.dataStore.get(key);
			if (storeResult!=null){
				this.dataCache.replace(key, storeResult);
				return storeResult;
			}else{
				//key is not even in the store
				throw new KVException(new KVMessage("resp", "key is not in store"));
			}
		}finally{
			AutoGrader.agKVServerGetFinished(key);
		}
		
	}
	
	public void del (String key) throws KVException {
		// Must be called before anything else
		AutoGrader.agKVServerDelStarted(key);

		// TODO: implement me
		this.dataCache.del(key);
		this.dataStore.del(key); // 
		
		// Must be called before returning
		AutoGrader.agKVServerDelFinished(key);
	}
}
