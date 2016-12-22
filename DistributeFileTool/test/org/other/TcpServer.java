package org.other;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class TcpServer
{
	public static void main(String[] args) throws Exception
	{
		ServerSocket ss = new ServerSocket(5000);
		
		Socket socket = ss.accept();
		//SocketChannel sc = socket.getChannel();
		//sc.w
		
		InputStream is = socket.getInputStream();
		
		OutputStream os = socket.getOutputStream();
		
		byte[] buffer = new byte[128];
		
		//int length = is.read(buffer);
		
		//System.out.println(new String(buffer, 0 ,length));
		
		
		
		int len = 0;
		while(-1 != (len = is.read(buffer)))
		{
			System.out.print(buffer[0]+",");
			//System.arraycopy(src, srcPos, dest, destPos, length);
			
		}
		System.out.println();
		//os.write("welcome".getBytes());
		
		is.close();
		os.close();
		socket.close();
	}
}
