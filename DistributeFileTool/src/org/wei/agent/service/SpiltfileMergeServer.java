package org.wei.agent.service;

import org.wei.agent.DataAgent;
import org.wei.util.Cons;
import org.wei.util.FileStat;
import org.wei.util.FileUtil;

/**
 * 将小文件合并
 * @author 魏伦凯
 *
 */
public class SpiltfileMergeServer implements Runnable {
	private String fileDir;
	private String fileName;
	private DataAgent dataAgent;
	
	
	public SpiltfileMergeServer(DataAgent dataAgent,String fileDir,String fileName){
		this.fileDir = fileDir;
		this.fileName = fileName;
		this.dataAgent = dataAgent;
	}
	
	@Override
	public void run() {
		try {
			String statKey = fileDir+Cons.SEQ+fileName;
			dataAgent.putFileStat(statKey, FileStat.Merging);
			
			//将文件块合并成文件
			boolean res = FileUtil.mergeSpiltFile(fileDir , fileName);
			
			if(res){
				dataAgent.putFileStat(statKey, FileStat.Complete);
				System.out.println(fileDir+Cons.SEQ+fileName + " upload complete! Congratulation..");
			}else{
				dataAgent.putFileStat(statKey, FileStat.Merge_err);
				System.out.println(fileDir+Cons.SEQ+fileName + " merge error..");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
