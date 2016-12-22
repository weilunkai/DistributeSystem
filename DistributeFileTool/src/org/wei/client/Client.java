package org.wei.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.wei.agent.protocol.DataAgentProtocol;
import org.wei.util.FileUtil;

/**
 * 客户端实现
 * @author 魏伦凯
 *
 */
public class Client {
	
	private List<String> agentIpList;
	
	//private String primaryNode;
	private int primaryIndex;
	
	private List<DataAgentProtocol> agentServerList; 
	
	private String srcFile;
	private String desFileDir;
	private String fileName;
	
	private String slaves;
	
	public Client(String slaves,String srcFile,String desFileDir,String fileName){
		this.setSrcFile(srcFile);
		this.desFileDir = desFileDir;
		this.fileName = fileName;
		this.slaves = slaves;
	}
	
	//初始化RPC dataAgent
	public void init() throws IOException {
		
		this.agentIpList = FileUtil.inistHosts(slaves);
		this.agentServerList = new ArrayList<DataAgentProtocol>(agentIpList.size());
		for(String remoteIp : agentIpList){
			InetSocketAddress addr = new InetSocketAddress(remoteIp, 5055);
			
			DataAgentProtocol agent = (DataAgentProtocol) RPC.getProxy(
					DataAgentProtocol.class,
					RPC.getProtocolVersion(DataAgentProtocol.class), addr,
					new Configuration());
			//1、设置slaves文件，并通过RPC初始化Agent 
			//agent.initSenderAndHosts(remoteSlaves);
			
			//2、设置要传输的文件相关信息  desFileDir fileName
			agent.setDesFileDir(desFileDir);
			agent.setFileName(fileName);
			
			agentServerList.add(agent);
		}
		
	}

	public boolean checkInit(){
		for(DataAgentProtocol agent : agentServerList){
			String dfd = agent.getDesFileDir();
			String fn = agent.getFileName();
			System.out.println("dfd="+dfd+",fn="+fn);
			System.out.println("sendFlag="+agent.getSendFlag());
			if(desFileDir.equals(dfd) && fileName.equals(fn)){
				continue;
			}
			
			return false;
		}
		return true;
	}
	
	
	//args[0]:源文件, 
	//args[1]:传输的目标路径, 
	//args[2]:本地slaves文件	
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("The args is not right.Please set it. args.length = "
							+ args.length);
			return;
		}
		
		String srcFileDir = args[0];//源文件路径
		String fileName = parseFileName(srcFileDir);//源文件名称
		String desFileDir = args[1];//要传输的目标路径
		if(desFileDir.endsWith("/")){
			desFileDir = desFileDir.substring(0, desFileDir.lastIndexOf("/"));
		}
		String slaves = args[2];
		
		System.out.println("SrcFileDir = "+srcFileDir);
		System.out.println("FileName = "+fileName);
		System.out.println("DesFileDir = "+desFileDir);		
		System.out.println("LocalSlaves = "+slaves);
		
		long startTime = System.currentTimeMillis();
		Client client = new Client(slaves,srcFileDir,desFileDir,fileName);		
		try {
			// 初始化
			client.init();
			
			// 初始化primaryNode
			Socket socket = null;
			String primaryIp = client.getPrimaryNode();
			while(primaryIp != null){
				try {
					socket = new Socket(primaryIp, 5051);
					break;
				} catch (Exception e) {
					primaryIp = client.nextPrimaryNode();
					e.printStackTrace();
				}
			}
			if(socket == null){
				System.out.println("PrimaryNode socket init failure...");
				return;
			}
			
			OutputStream os = socket.getOutputStream();
			
			File file = new File(srcFileDir);
			FileInputStream input = new FileInputStream(file);

			byte[] buffer = new byte[128];
			int len = 0;
			while ((len = input.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}

			input.close();
			os.close();
			socket.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Client has error,info is " + ex.getMessage());
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Elapse time :"+(endTime-startTime) +" ms." );

	}
	//通过目录解析文件名称
	public static String parseFileName(String fileOpt){
		int lsq = fileOpt.lastIndexOf("/");		
		String fileName = fileOpt.substring(lsq+1);
		return fileName;
	}
	
	public List<String> getAgentIpList() {
		return agentIpList;
	}

	public void setAgentIpList(List<String> agentIpList) {
		this.agentIpList = agentIpList;
	}

	public String getSlaves() {
		return slaves;
	}

	public void setSlaves(String slaves) {
		this.slaves = slaves;
	}

	public String getSrcFile() {
		return srcFile;
	}

	public void setSrcFile(String srcFile) {
		this.srcFile = srcFile;
	}

	public String getPrimaryNode() {
		return agentIpList.get(primaryIndex);
	}

	public String nextPrimaryNode(){
		primaryIndex++;
		if(primaryIndex>agentIpList.size()){
			return null;
		}
		return agentIpList.get(primaryIndex);
		
	}
	
	public int getPrimaryIndex() {
		return primaryIndex;
	}

	public void setPrimaryIndex(int primaryIndex) {
		this.primaryIndex = primaryIndex;
	}
}
