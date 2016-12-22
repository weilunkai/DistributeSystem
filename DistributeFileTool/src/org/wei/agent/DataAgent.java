package org.wei.agent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.RPC.Server;
import org.wei.agent.protocol.DataAgentProtocol;
import org.wei.agent.service.DataReceiveServer;
import org.wei.agent.service.DataSendServer;
import org.wei.util.AgentStat;
import org.wei.util.FileStat;
import org.wei.util.FileUtil;

/**
 * 数据节点，接收数据并转发数据
 * 
 * @author 魏伦凯
 *
 */
public class DataAgent implements DataAgentProtocol {
	public static final Log LOG = LogFactory.getLog(DataAgent.class);
	
	/**RPC Server**/
	public AgentStat stat; //运行状态
	private long currentBlockSequence;	//当前文件块序号
	private long currentBlockSize;		//当前文件块大小
	private long currentBlockOffset;	//当前文件块偏移量
	
	private String desFileDir;  //由客户端通过RPC赋值
	private String fileName; //由客户端通过RPC赋值
	
	//private String slaves;//数据节点文件,完整的目录+文件 
	private List<String> hosts;//所有数据节点
	private List<String> errorHosts = new ArrayList<String>();//异常节点
	
	public Map<String,FileStat> fileStat = new HashMap<String,FileStat>();
	
	
	/**当前节点ip和端口信息**/
	private int CEIVER_PORT = 5051; //接收器和发送器 端口
	private int AGENT_IPC_PORT=5055;//Agent RPC端口	
	private String CURRENT_AGENT_IP;//当前节点ip	
	
	private Server server;//RPC server
	
	//数据接收器
	private DataReceiveServer receive;
	//数据发送器
	private DataSendServer sender;
	private boolean sendFlag=false;
	
	public DataAgent() throws Exception{
		this.CURRENT_AGENT_IP = getLocalIp();		
		this.receive = new DataReceiveServer(this,CEIVER_PORT);		
		//实例化RPC服务
		this.server = new RPC.Builder(new Configuration()).setProtocol(DataAgentProtocol.class)
				.setBindAddress(CURRENT_AGENT_IP).setPort(AGENT_IPC_PORT).setInstance(this).build();
		
		initSenderAndHosts();
	}
	
	/**
	 * 实例化 数据发送器,当前节点为 hosts列表中最后一个节点时，无需实例化发送器
	 * @return DataSendServer
	 * @throws Exception 
	 * @throws UnknownHostException 
	 * @throws IOException 
	 */
	public DataSendServer creatSenderServer() throws Exception{
		// 实例化 数据发送器,当前节点为 hosts列表中最后一个节点时，无需实例化发送器
		int size = hosts.size();
		if(size<=1){
			return null;
		}
		for (int i = 0; i < size; i++) {
			// 查找当前节点在 节点列表中的位置
			String host = hosts.get(i);
			if (host.equals(CURRENT_AGENT_IP)) {
				// 判断当前节点是否为节点列表中的最后一个
				if((i+1)==size){
					return null;
				}
				int nextIdx = i + 1;
				DataSendServer sender = null;
				while (nextIdx <= size - 1) {
					String nextHost = hosts.get(nextIdx);
					try {
						sender = new DataSendServer(nextHost,CEIVER_PORT);
						System.out.println("Sender init. Next agent node,host="+nextHost+",sender="+sender);
						setSendFlag(true);
						return sender;
					} catch (Exception e) {
						e.printStackTrace();
						errorHosts.add(nextHost);
					}
					nextIdx++;

				}
				this.stat = AgentStat.sender_error;
				throw new Exception("construct sender of data failure!!");
			}
		}
		return null;
	}
	
	/**
	 * 根据slaves，初始化发送器和数据节点列表
	 */
	@Override
	public boolean initSenderAndHosts(String slaves) {
		//this.slaves = slaves;
		this.hosts = FileUtil.inistHosts(slaves);
		if(hosts==null){
			return false;
		}
		try {			
			this.sender = creatSenderServer();//构造发送器
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if(sender==null){
			return false;
		}
		return true;
	}
	
	/**
	 * 根据slaves，初始化发送器和数据节点列表
	 */
	public boolean initSenderAndHosts() {
		//this.slaves = slaves;
		this.hosts = FileUtil.inistHosts();
		if(hosts==null){
			return false;
		}
		try {			
			this.sender = creatSenderServer();//构造发送器
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if(sender==null){
			return false;
		}
		return true;
	}
	
	/**
	 * 数据节点服务启动
	 * @throws IOException
	 */
	public void runDataAgentDaemon() throws IOException {		
		new Thread(receive).start();
		server.start();		
		this.stat = AgentStat.running;
		
		System.out.println("Data agent start ok.");
	}

	public static void main(String[] args) throws Exception {
		try {
			DataAgent agent = new DataAgent();
			agent.runDataAgentDaemon();
		} catch (IOException e) {
			System.out.println("Data agent start failure.");
			e.printStackTrace();
		}
		
	}

	public DataReceiveServer getReceive() {
		return receive;
	}

	public void setReceive(DataReceiveServer receive) {
		this.receive = receive;
	}
	
	public String getLocalIp(){
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public DataSendServer getSender() {
		return sender;
	}

	public int getPort() {
		return CEIVER_PORT;
	}


	public void setPort(int port) {
		this.CEIVER_PORT = port;
	}

	@Override
	public long getCurrentBlockSequence() {
		return currentBlockSequence;
	}


	public void setCurrentBlockSequence(long currentBlockSequence) {
		this.currentBlockSequence = currentBlockSequence;
	}

	@Override
	public long getCurrentBlockSize() {
		return currentBlockSize;
	}


	public void setCurrentBlockSize(long currentBlockSize) {
		this.currentBlockSize = currentBlockSize;
	}

	@Override
	public long getCurrentBlockOffset() {
		return currentBlockOffset;
	}


	public void setCurrentBlockOffset(long currentBlockOffset) {
		this.currentBlockOffset = currentBlockOffset;
	}
	
	//当前文件块序号
	public long incrCBSeq(){		
		return this.currentBlockSequence++;
	}
	public long clearCBSeq(){
		return this.currentBlockSequence =0;
	}
	public long clearCBSzie(){
		return this.currentBlockSize = 0;
	}
	//当前文件块大小
	public long updateCBSzie(int len){
		return this.currentBlockSize += len;
	}
	//当前文件块偏移量
	public long updateCBOffset(){
		return this.currentBlockOffset += currentBlockSize;
	}

	@Override
	public AgentStat getStat() {
		return stat;
	}

	public void setStat(AgentStat stat) {
		this.stat = stat;
	}

	@Override
	public String getDesFileDir() {
		return desFileDir;
	}

	@Override
	public void setDesFileDir(String fileDir) {
		this.desFileDir = fileDir;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public List<String> getErrorHosts() {
		return errorHosts;
	}

	@Override
	public void setErrorHosts(List<String> errorHosts) {
		this.errorHosts = errorHosts;
	}

	@Override
	public Map<String, FileStat> getFileStat() {
		return fileStat;
	}


	public void setFileStat(Map<String, FileStat> fileStat) {
		this.fileStat = fileStat;
	}
	
	public void putFileStat(String kye,FileStat stat){
		this.fileStat.put(kye, stat);
	}


	@Override
	public boolean getSendFlag() {
		return sendFlag;
	}



	public void setSendFlag(boolean sendFlag) {
		this.sendFlag = sendFlag;
	}
	
}
