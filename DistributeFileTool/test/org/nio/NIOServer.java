package org.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NIOServer {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		Socket socket = new Socket("127.0.0.2", 5000);
		
		
		ServerSocketChannel server = ServerSocketChannel.open();
		server.bind(new InetSocketAddress(5000));
		// 设置通道为非阻塞
		server.configureBlocking(true);
		
		ByteBuffer buf = ByteBuffer.allocate(100);
		while(true){
			SocketChannel socketChannel = server.accept();	
			while(true){
				socketChannel.read(buf);
				buf.flip();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(new String(buf.array(),0,buf.limit()));
				buf.clear();
			}
		
		}
		
		

	}

}
