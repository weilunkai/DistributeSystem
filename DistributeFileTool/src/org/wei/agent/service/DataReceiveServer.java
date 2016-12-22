package org.wei.agent.service;

import static org.wei.util.Cons.SEQ;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wei.agent.DataAgent;
import org.wei.util.FileStat;
import org.wei.util.FileUtil;


/**
 * 数据接收器，接收文件 ;并向下个节点写数据;文件传输完成后，合并成单个文件
 * @author 魏伦凯
 * 20161217
 *
 */
public class DataReceiveServer implements Runnable {
	public static final Log LOG = LogFactory.getLog(DataReceiveServer.class);
	
	private boolean closed = false;
	
	private DataAgent dataAgent;
	
	private ServerSocketChannel socketServer;
	
	private OutputStream mirrorOut; //nex agent writer
	
	private long blockSize = 1024*1024*64;//block size : 64M
	
	public DataReceiveServer(DataAgent dataAgent ,int port) throws IOException{
		this.dataAgent = dataAgent;
		
		this.socketServer =  ServerSocketChannel.open();
		socketServer.bind(new InetSocketAddress(dataAgent.getPort()));		
		socketServer.configureBlocking(true);//设置通道为阻塞
		
	}
	
	@Override
	public void run() {
		System.out.println("Data receive server start ok.");
		while(!closed){
			
			FileOutputStream out = null;
			SocketChannel socketChannel = null;
			
			String desFileDir = null;//文件目录
			String fileName = null;//文件名称
			String fileStatKey = null;
			try {
				socketChannel = socketServer.accept();
				System.out.println("--------------------------------------");
				
				System.out.println("***Start write file.....");
				desFileDir = dataAgent.getDesFileDir();//文件目录
				fileName = dataAgent.getFileName();//文件名称
				fileStatKey = desFileDir+SEQ+fileName;
				
				System.out.println("desFileDir="+desFileDir);
				System.out.println("fileName="+fileName);
				System.out.println("fileStatKey="+fileStatKey);
				
				
				/**初次写入文件**/
				File file = new File(desFileDir + SEQ + "."+ fileName + SEQ+"data."+dataAgent.getCurrentBlockSequence());	
				FileUtil.createTmp(desFileDir,fileName);
				out = new FileOutputStream(file); 
				
				dataAgent.putFileStat(fileStatKey, FileStat.Writing);
				
				//初始化，发送流
				if(dataAgent.getSender()!=null){
					this.mirrorOut = dataAgent.getSender().getOutStream();
					System.out.println("***Init mirrorOut, mirrorOut = "+mirrorOut);
				}
				
				ByteBuffer buf = ByteBuffer.allocate(128);
				socketChannel.read(buf);
				buf.flip();
				while(buf.hasRemaining()){
					
					int len = buf.limit();
					if(mirrorOut != null){
						mirrorOut.write(buf.array(), 0,len);
					}					
					out.write(buf.array(), 0,len);//将数据写入本地文件
					
					//更新当前的文件块大小,并判断是否达到阈值
					if(dataAgent.updateCBSzie(len) >= blockSize){
						out.close();//结束一个文件块的写操作	
						FileUtil.writeSrc(desFileDir,fileName,
								dataAgent.getCurrentBlockSequence(),
								dataAgent.getCurrentBlockSize(),
								dataAgent.getCurrentBlockOffset());
						System.out.println("**Current block complete. Sequence = "+dataAgent.getCurrentBlockSequence());
						dataAgent.incrCBSeq();//文件块序号递增
						
						file = new File(desFileDir+SEQ+ "."+fileName +SEQ+"data."+dataAgent.getCurrentBlockSequence());
						out = new FileOutputStream(file); 
						dataAgent.updateCBOffset();//更新偏移量						
						dataAgent.clearCBSzie();//重置当前块大小
						
					}					
					buf.clear();//清空缓冲
					
					socketChannel.read(buf);
					buf.flip();
				}				
				
			} catch (IOException e) {				
				e.printStackTrace();
			} finally{				
				try {
					if(out != null){
						out.close();
						FileUtil.writeSrc(desFileDir,fileName,
								dataAgent.getCurrentBlockSequence(),
								dataAgent.getCurrentBlockSize(),
								dataAgent.getCurrentBlockOffset());
					}
					if(socketChannel!=null){
						socketChannel.close();
					}
					if(mirrorOut!= null ){
						dataAgent.getSender().close();
					}
					dataAgent.putFileStat(fileStatKey, FileStat.Write_ok);
					dataAgent.clearCBSeq();//清空文件块序号
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			while(true){
				if(dataAgent.getFileStat().get(fileStatKey).equals(FileStat.Write_ok)){
					System.out.println("***Start merge the file....");
					//合并文件块
					new Thread(new SpiltfileMergeServer(dataAgent,desFileDir, fileName)).start();
					break;
				}
			}
			
			
		}

	}
	

}
