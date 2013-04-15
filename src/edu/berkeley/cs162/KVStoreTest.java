package edu.berkeley.cs162;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Test functionalities of kvstore. You'd better change the delay time in AutoGrader.java before running the tests.
 * @author amos0528
 *
 */
public class KVStoreTest {
	public static KVStore store = new KVStore();

	/**
	 * set store to a clean state
	 */
	@Before
	public void reset(){
		store = new KVStore();
	}
	
	@Test
	public void singlePutTest() {		
		try {
			store.put("key1", "val1");
			assertEquals(store.get("key1"),"val1");
			System.out.println(store.toXML());
		} catch (KVException e) {
			fail();
		}
	}	
	
	private void naivePopulateStore() throws KVException{
		for (int i = 0; i<10; i++){
			store.put("key"+i, "value"+i);				
		}
	}
	
	@Test
	public void singleGetTest(){
		try{
			this.naivePopulateStore();
			
			for (int i = 0; i < 10; i++){
				assertEquals(store.get("key"+i), "value"+i);
			}
		} catch (KVException e){
			fail();
		}
	}
	
	@Test(expected = KVException.class)
	public void getNonexistentTest() throws KVException{
		store.get("key1");
	}
	
	@Test(expected = KVException.class)
	public void getNonexistentTest2() throws KVException{
		store.put("key", "val");
		assertEquals(store.get("key"), "val");
		store.get("key1"); //should throw kvexception
	}
	
	@Test
	public void putOnSameKeyTest(){
		String key1="key1", key2="key2";
		String val1="val1", val2 = "val2", val3 = "val3";
		try{
			store.put(key1, val1);
			assertEquals(store.get(key1),val1);
			store.put(key2, val2);
			assertEquals(store.get(key1), val1);
			store.put(key1, val2);
			assertEquals(store.get(key1), val2);
		} catch (KVException e){
			fail();
		}
	}
	
	@Test
	public void dumpAndRestoreTest(){
		try{
			//populate the store
			this.naivePopulateStore();
			
			String original = store.toXML();
			
			//save the store to storeDump
			store.dumpToFile("storeDump");
			
			//some operations on the store
			store.put("key1", "val7");
			store.del("key6"); store.del("key3");
			
			//restore the store from storeDump
			store.restoreFromFile("storeDump");
						
			assertEquals(original, store.toXML());
			
		}catch(KVException e){
			fail();
		}
	}

}
