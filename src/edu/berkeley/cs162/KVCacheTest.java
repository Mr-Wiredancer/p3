package edu.berkeley.cs162;

import static org.junit.Assert.*;

import org.junit.Test;

public class KVCacheTest {

	@Test
	public void replacementTest1() {
		KVCache cache = new KVCache(2,2);
		try {
			cache.put("key1", "val1");
			cache.put("key2", "val2");
			cache.put("key3", "val3");
			cache.put("key4", "val4");

			cache.get("key1"); // this should set key1's reference bit
			
			cache.put("key5", "val5");//should replace key3
			
			assertEquals(cache.get("key3"),null);

		} catch (KVException e) {
			// TODO Auto-generated catch block
			fail();
		}		
	}
	
	@Test
	public void replacementTest2(){
		KVCache cache = new KVCache(1, 4);
		try {
			cache.put("key1", "val1");
			cache.put("key2", "val2");
			cache.put("key3", "val3");
			cache.put("key4", "val4");

			cache.get("key1"); // this should set key1's reference bit
			cache.get("key3");
			
			cache.put("key5", "val5");//should replace key3
			
			assertEquals(cache.get("key2"),null);
			
			cache.get("key3");cache.get("key1");
			
			cache.put("key6", "val6");
			assertEquals(cache.get("key4"), null);
			assertEquals(cache.get("key5"), "val5");

		} catch (KVException e) {
			// TODO Auto-generated catch block
			fail();
		}		
	}
}
