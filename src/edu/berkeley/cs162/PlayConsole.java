/**
 * 
 */
package edu.berkeley.cs162;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * A console which lets u to play around proj3
 * 
 * If you're using mac/ubuntu/other unix/linux and wanna run from command line
 * 1. go to cs162 directory and run:
 *		"ls -r | grep -v Test | xargs javac"
 *
 * 2. go to src directory and run:
 *		"java edu/berkeley/cs162/PlayConsole"
 *
 * If you're using windows, just click "run" in eclipse
 * @author amos0528
 *
 */
public class PlayConsole {

	public static SocketServer server = null;
	public static KVServer key_server = null;	
	
	private static class ServerThread extends Thread implements Debuggable{
		PrintStream out = null;
		public ServerThread(){
			super();
			this.setName("ServerThread");
		}
		
		public void run(){
			try{
				KVServer key_server1 = new KVServer(2, 2);
				SocketServer server1 = new SocketServer("localhost", 8080);
				
				server = server1;
				key_server = key_server1;
				
				NetworkHandler handler = new KVClientHandler(key_server);
				server.addHandler(handler);
				server.connect();
				DEBUG.debug("server is ready");
				server.run();
			}catch(Exception e){
				DEBUG.debug("socket is closed");
			}
		}	
	}
	
	public static void main(String [] args){
		Thread.currentThread().setName("PlayConsole");
		ServerThread st = new ServerThread();
		st.start();
		
		KVClient kc = new KVClient("localhost", 8080);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String input = null;
		try{
			while ((input=reader.readLine())!=null){
				if (input.equals("quit")){
					handleQuit();
					System.exit(0);
				}
				
				String [] inputs = input.split(",");
				
				if (inputs.length < 1 || inputs.length>3){
					System.out.println("could not recognize ur command");
					continue;
				}
				
				String command = inputs[0];
				if (command.equals("put")){
					if (inputs.length!=3){
						System.out.println("could not recognize ur command");
						continue;
					}
					kc.put(inputs[1],inputs[2]);
				}else if(command.equals("get")){
					if (inputs.length!=2){
						System.out.println("could not recognize ur command");
						continue;
					}
					kc.get(inputs[1]);
				}else if(command.equals("del")){
					if (inputs.length!=2){
						System.out.println("could not recognize ur command");
						continue;
					}
					kc.del(inputs[1]);
					
					
				}else if(command.equals("dumpstore")){
					System.out.println(PlayConsole.key_server.dumpStore());
					
				}else if(command.equals("dumpcache")){	
					System.out.println(PlayConsole.key_server.dumpCache());
					
				//Extend so that it can accept more commands, such as dumpCache and cumpStore
				}else{
					System.out.println("could not recognize ur command");
				}
				
			}
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
	public static void handleQuit(){
		System.out.println("quiting");
		server.stop();
	}
}
