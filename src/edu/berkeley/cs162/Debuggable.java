package edu.berkeley.cs162;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public interface Debuggable {
	
	public static DefaultDebuggable DEBUG = new DefaultDebuggable();
				
	class DefaultDebuggable implements Debuggable{ 
	
		public void debug(String s){
			System.out.println(Thread.currentThread().getName()+": "+s);
		}
	}
}
