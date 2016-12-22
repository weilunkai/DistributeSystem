package org.wei.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SentClientDataMain {

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		/*File file = new File("F:/tmp/src/data.txt");
		FileInputStream input = new FileInputStream(file);

		Socket socket = new Socket("127.0.0.1", 5051);

		OutputStream os = socket.getOutputStream();
		byte[] buffer = new byte[200];

		int len = 0;
		while ((len = input.read(buffer)) != -1) {
			os.write(buffer, 0, len);
		}

		input.close();
		os.close();
		socket.close();*/
		
		
		Socket socket = new Socket("192.168.223.2", 5051);
		OutputStream os = socket.getOutputStream();
//		for(int i=0;i<3;i++){
//			os.write((i+"nice to meet you.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx12321xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxweqwewqxxxxxxxxxxxxxxxxx").getBytes());
//		}
		//os.write("aaabbbcccddddeeefff".getBytes());
		//os.write("111112222233333444445555566666777778888899999".getBytes());
		
		os.close();
		socket.close();

	}

}
