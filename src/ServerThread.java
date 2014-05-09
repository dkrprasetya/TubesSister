package com.moedikra.tubes3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerThread extends Thread {
	
	private static final int MODE_IDLE = 0;
	private static final int MODE_DISPLAY = 1;
	private static final int MODE_INSERT = 2;
	
	private final String hostName;
	private final int portNumber;
	private String table;
	private String key;
	private String val;
	
	private int currentMode;	
	
	private String result;
	
	private boolean isExit;	
	
	public ServerThread(String hostName, int portNumber) {
		super();
		
		this.hostName = hostName;
		this.portNumber = portNumber;
		currentMode = MODE_IDLE;
		isExit = false;
	}
	
	public String doDisplay(String table){
		this.table = table;
		currentMode = MODE_DISPLAY;
		while (currentMode == MODE_DISPLAY){};
		return result;
	}
	
	public String doInsert(String table, String key, String val){
		this.table = table;
		this.key = key;
		this.val = val;
		currentMode = MODE_INSERT;
		while (currentMode == MODE_INSERT){};
		return result;
	}
	
	public void doExit(){
		isExit = true;
	}
	
	public void run(){
		try (
	            Socket kkSocket = new Socket(hostName, portNumber);
	            PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
	            BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream())); //ini nerima data dari server, utk fetch-nya in.readLine()
	        ) {
				out.println("server");
			
	            while (true) {
					
	            	while ((currentMode == MODE_IDLE) && (!isExit)){}
	            	
					if (isExit) break; // exit
	            	
	            	if (currentMode == MODE_DISPLAY){
		            	out.println("display " + table);	
	            	}
	            	else // MODE_INSERT
	            	{
	            		out.println("insert " + table + " " + key + " " + val);
	            	}
	            	
	            	result = in.readLine();			
	            	currentMode = MODE_IDLE;
	            }
	        } catch (UnknownHostException e) {
	            System.err.println("Don't know about host " + hostName);
	            System.exit(1);
	        } catch (IOException e) {
	            System.err.println("Couldn't get I/O for the connection to " +
	                hostName);
	            System.exit(1);
	        }
	}
	
}
