package org.other;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class IOTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		File file = new File("F:/tmp/des/a"+0);
		
		FileOutputStream out =  new FileOutputStream(file); 	
		
		out.write("hello".getBytes());
		
		file = new File("F:/tmp/des/a"+1);

		out.write("nice".getBytes());
		
		out.close();
	}

}
