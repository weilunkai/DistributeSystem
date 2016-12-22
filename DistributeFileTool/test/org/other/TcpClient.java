package org.other;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpClient
{
	public static void main(String[] args) throws Exception
	{
		Socket socket = new Socket("127.0.0.1", 5000);
		
		//InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();
		
		String hello = "nice to meet you.";
		byte[] hb = hello.getBytes();
		for(byte b : hb){
			System.out.print(b+",");
		}
		System.out.println();
		
		os.write(hb);
		os.write("OFF".getBytes());
		
		os.close();
		socket.close();
		/*int i=0;
		while(true){
			System.out.println("ok"+i);
			os.write("hello world".getBytes());
			
			i++;
			//System.out.println(is.read(new byte[1]));
		}*/
		
		
		//byte[] buffer = new byte[200];
		
		//int length = is.read(buffer);
		
		//System.out.println(new String(buffer, 0 ,length));
		
//		int length = 0;
//		
//		while(-1 != (length = is.read(buffer,0, buffer.length)))
//		{
//			String str = new String(buffer, 0, length);
//			
//			System.out.println(str);
//		}
		
		//is.close();
		//os.close();
		//socket.close();
	}
}
