package edu.berkeley.cs162;

import static org.junit.Assert.*;

import org.junit.Test;

public class KVCacheTest {

	@Test
	public void test() {
		KVCache cache = new KVCache(2,2);
		cache.replace("key1", "val1");
		cache.replace("key2", "val2");
		cache.replace("key3", "val3");
		cache.replace("key4", "val4");
		
		System.out.println(cache.toXML());
		
	}

}
