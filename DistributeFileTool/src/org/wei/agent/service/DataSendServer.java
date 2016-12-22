package org.wei.agent.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class DataSendServer {
	
	private String ip;
	private int port;
	
	private Socket outSock;
	
	private OutputStream outStream;
	
	public DataSendServer(String ip, int port) throws UnknownHostException, IOException{
		this.ip = ip;
		this.port =port;
	}

	public OutputStream getOutStream() throws UnknownHostException, IOException {
		this.outSock = new Socket(ip, port); 
		this.outStream = outSock.getOutputStream();
		return outStream;
	}

	public void close() throws IOException{
		outStream.close();
		outSock.close();
	}
	

}
