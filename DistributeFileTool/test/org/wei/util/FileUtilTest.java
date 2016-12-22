package org.wei.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class FileUtilTest {

	@Test
	public void writeSrc(){
		FileUtil.createTmp("F:/tmp/src/","Test");
		FileUtil.writeSrc("F:/tmp/src/","Test", 123, 60000, 90000);
	}
	
	@Test
	public void inistHosts() throws IOException{
		
		/*String slaves = "slaves";
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		URL url = classLoader.getResource(slaves);
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));  			
		 String line = null;  
		 //一次读入一行，直到读入null为文件结束  
		 while ((line = reader.readLine()) != null) { 
			System.out.println(line);
		 }  */
		
		
		
		/*List<String> list = FileUtil.inistHosts();
		for(String h : list){
			System.out.println(h);
		}*/
	}
	@Test
	public void mergeSpiltFile(){
		try {
			FileUtil.mergeSpiltFile("F:/tmp/agent/des/1155", "datatest.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
}
