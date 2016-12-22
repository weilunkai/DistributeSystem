package org.wei.util;

import static org.wei.util.Cons.SEQ;
import static org.wei.util.Cons.TAB;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.wei.agent.bean.BlockFileBean;

public class FileUtil {

	/**
	 * 创建文件块的验证文件src
	 * @param fileDir
	 * @param tmp
	 * @param currentBlockSequence
	 * @param currentBlockSize
	 * @param currentBlockOffset
	 */
	public static void writeSrc(String fileDir,String tmp, long currentBlockSequence,
			long currentBlockSize, long currentBlockOffset) {

		File file = new File(fileDir + SEQ +"."+ tmp +SEQ+"src." + currentBlockSequence);
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(file, false));
			out.write(currentBlockSequence + TAB + currentBlockSize + TAB
					+ currentBlockOffset);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				if(out!=null){
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 创建临时目录，用户存放文件块和验证数据
	 * @param fileDir
	 * @param tmp
	 * @return
	 */
	public static boolean createTmp(String fileDir,String tmp){
		File tmpDir = new File(fileDir + SEQ + "."+ tmp );
		if(!tmpDir.exists()){
			return tmpDir.mkdirs();
		}
		return false;
	}
	/**
	 * 初始化数据节点列表
	 * @param slaveFile
	 * @return
	 */
	public static List<String> inistHosts(String slaveFile){
		File file = new File(slaveFile);
		BufferedReader reader = null;
		try {
			 reader = new BufferedReader(new FileReader(file));  			
			 String line = null;  
			 List<String> result = new ArrayList<String>();
			 //一次读入一行，直到读入null为文件结束  
			 while ((line = reader.readLine()) != null) { 
				 if(line.isEmpty()){
					 continue;
				 }
				 System.out.println("host="+line);
				 result.add(line.trim());
			 }  
			 return result;
		} catch (IOException e) {			
			e.printStackTrace();
			return null;
		} finally{
			try {
				if(reader!=null){
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 初始化数据节点列表
	 * @param slaveFile
	 * @return
	 */
	public static List<String> inistHosts() {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		BufferedReader reader = null;
		try {
			URL url = classLoader.getResource("slaves");
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line = null;
			List<String> result = new ArrayList<String>();
			// 一次读入一行，直到读入null为文件结束
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				System.out.println("host=" + line);
				result.add(line.trim());
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 过滤文件，获取data.X文件
	 * @param dirFile
	 * @return
	 */
	public static File[] listDataFiles(File dirFile){
		return dirFile.listFiles(new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				String fileName = pathname.getName();
				return fileName.startsWith("data.");
			}
			
		}) ;
	}
	/**
	 * 获取数据文件的序号
	 * @param dataFile
	 * @return
	 */
	public static int getFileSeq(File dataFile){
		String fileName = dataFile.getName();
		int doIdx = fileName.indexOf(".");
		return Integer.valueOf(fileName.substring(doIdx+1));
	}
	/**
	 * 按照数据文件的序列号排序
	 * @param fileList
	 */
	public static void sortFileBean(List<BlockFileBean> fileList){
		
		Collections.sort(fileList, new Comparator<BlockFileBean>(){
			@Override
			public int compare(BlockFileBean o1, BlockFileBean o2) {
				return o1.getSequence()-o2.getSequence();
			}
		});
	}
	/**
	 * 数据块文件合并
	 * @param fileDir
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static boolean mergeSpiltFile(String fileDir , String fileName) throws Exception{
		//临时文件目录
		String tmpDir = fileDir+SEQ+"."+fileName;
		File tmpFile = new File(tmpDir);
		
		if(tmpFile.isDirectory()){
			
			File[] files = listDataFiles(tmpFile);
			//计算列表的长度，以防添加元素过程中容量自增
			int size = files.length + (files.length >> 1);
			List<BlockFileBean> fileList = new ArrayList<BlockFileBean>(size);
			for(File file : files){
				int seq = getFileSeq(file);
				fileList.add(new BlockFileBean(file,seq));
			}
			//按照文件序列号排序
			sortFileBean(fileList);
			
			return merge(fileList,fileDir,fileName);
			
		}else{
			throw new Exception("TmpFile is not a Directory. tmpFile="+tmpFile);
		}
	}
	/**
	 * 合并操作
	 * @param fileList 文件块列表
	 * @param fileDir 文件目标目录
	 * @param fileName 文件名称
	 * @return 
	 * @throws Exception
	 */
	public static boolean merge(List<BlockFileBean> fileList,String fileDir , String fileName) throws Exception{
		File file = new File(fileDir + SEQ + fileName);	
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			for(int i=0;i<fileList.size();i++){
				BlockFileBean fb = fileList.get(i);
				if(fb.getSequence()==i){
					InputStream in = fb.getInputStrem();
					byte[] buffer = new byte[1024*1024*1];//缓冲区文件大小为1M  
					int len = 0;  
					while((len = in.read(buffer)) != -1){  
						out.write(buffer,0,len);  
					}  
					in.close();						
				}else{						
					throw new Exception("文件存在漏洞！Sequence="+fb.getSequence()+",index="+i);
				}
			}
			return true;
		} catch (Exception e) {
			throw e;				
		} finally{
			try {
				if(out != null){
					out.close();
				}
			} catch (IOException e) {
				throw e;
			}
		}
		
	}
	
	
	

}
