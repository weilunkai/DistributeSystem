package org.wei.agent.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 文件块实体
 * @author 魏伦凯
 *
 */
public class BlockFileBean {
	
	private File file;//块文件
	private int sequence;//块序号
	
	public BlockFileBean(File file,int sequence){
		this.file = file;
		this.sequence = sequence;
	}
	
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	
	
	public InputStream getInputStrem() throws FileNotFoundException{
		return new FileInputStream(file);  
	}

}
