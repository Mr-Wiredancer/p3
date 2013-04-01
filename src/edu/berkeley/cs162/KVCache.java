/**
 * Implementation of a set-associative cache.
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;


/**
 * A set-associate cache which has a fixed maximum number of sets (numSets).
 * Each set has a maximum number of elements (MAX_ELEMS_PER_SET).
 * If a set is full and another entry is added, an entry is dropped based on the eviction policy.
 */
public class KVCache implements KeyValueInterface {	
	private int numSets = 100;
	private int maxElemsPerSet = 10;
		
	private CacheSet[] sets;
	/**
	 * Creates a new LRU cache.
	 * @param cacheSize	the maximum number of entries that will be kept in this cache.
	 */
	public KVCache(int numSets, int maxElemsPerSet) {
		this.numSets = numSets;
		this.maxElemsPerSet = maxElemsPerSet;     
		// TODO: Implement Me!
	}

	/**
	 * Retrieves an entry from the cache.
	 * Assumes the corresponding set has already been locked for writing.
	 * @param key the key whose associated value is to be returned.
	 * @return the value associated to this key, or null if no value with this key exists in the cache.
	 */
	public String get(String key) {
		// Must be called before anything else
		AutoGrader.agCacheGetStarted(key);
		AutoGrader.agCacheGetDelay();
        
		// TODO: Implement Me!
		int setId = this.getSetId(key);
		String result = this.sets[setId].get(key);
		// Must be called before returning
		AutoGrader.agCacheGetFinished(key);
		return result;
	}
	
	public void update(String key, String value){
		this.sets[getSetId(key)].update(key, value);
	}

	/**
	 * Adds an entry to this cache.
	 * If an entry with the specified key already exists in the cache, it is replaced by the new entry.
	 * If the cache is full, an entry is removed from the cache based on the eviction policy
	 * Assumes the corresponding set has already been locked for writing.
	 * @param key	the key with which the specified value is to be associated.
	 * @param value	a value to be associated with the specified key.
	 * @return true is something has been overwritten 
	 */
	public boolean put(String key, String value) {
		// Must be called before anything else
		AutoGrader.agCachePutStarted(key, value);
		AutoGrader.agCachePutDelay();

		// TODO: Implement Me!
		
		// Must be called before returning
		AutoGrader.agCachePutFinished(key, value);
		return false;
	}

	/**
	 * Removes an entry from this cache.
	 * Assumes the corresponding set has already been locked for writing.
	 * @param key	the key with which the specified value is to be associated.
	 */
	public void del (String key) {
		// Must be called before anything else
		AutoGrader.agCacheGetStarted(key);
		AutoGrader.agCacheDelDelay();
		
		// TODO: Implement Me!
		
		// Must be called before returning
		AutoGrader.agCacheDelFinished(key);
	}
	
	/**
	 * @param key
	 * @return	the write lock of the set that contains key.
	 */
	public WriteLock getWriteLock(String key) {
	    // TODO: Implement Me!
	    return null;
	}
	
	/**
	 * 
	 * @param key
	 * @return	set of the key
	 */
	private int getSetId(String key) {
		return Math.abs(key.hashCode()) % numSets;
	}
	
    public String toXML() {
        // TODO: Implement Me!
        return null;
    }
    
    private class CacheEntry{
    	private String value;
    	private boolean dirty = false;
    	private boolean missed = false;
    	private String key;
    	
    	public CacheEntry(String key, String val){
    		this.value = val;
    		this.key = key;
    	}
    	
    	public void miss(){
    		this.missed = true;
    	}
    	
    	public boolean isDirty(){
    		return this.dirty;
    	}
    	
    	public boolean isLastChance(){
    		return this.missed;
    	}
    
    	public String getKey(){
    		return this.key;
    	}
    	
    	public String getValue(){
    		return this.value;
    	}
    }
    
    private class CacheSet{
    	private WriteLock lock = new ReentrantReadWriteLock().writeLock();
    	private final int MAX_NUM_ELEMENT;
    	private LinkedList<CacheEntry> set = new LinkedList<CacheEntry>();
    	
    	public CacheSet(int maxElementPerSet){
    		this.MAX_NUM_ELEMENT = maxElementPerSet;
    	}
    	
    	/**
    	 * Linear search of the requested key.
    	 * @param key
    	 * @return value of the key; null if the key doesn't exist
    	 */
    	public synchronized String get(String key){
    		for ( CacheEntry e : this.set){
    			if ( e.getKey() == key ){
    				return e.getValue();
    			}
    		}
    		return null;
    	}
    	
    	/**
    	 * Called when the requested key is not in the cache set. <key, value> is retreived from KVStore.
    	 * @param key 
    	 * @param value
    	 */
    	public synchronized void update(String key, String value){
    		if (this.set.size() < this.MAX_NUM_ELEMENT-1){
    			this.set.addFirst(new CacheEntry(key, value));
    			return;
    		}

    		CacheEntry headEntry = this.set.removeFirst();
    		if ( headEntry.isLastChance() ){
    			this.set.addFirst(new CacheEntry(key, value));
    		}else{
    			headEntry.miss();
    			this.set.add(headEntry);
    		}
    	}
    }
}
