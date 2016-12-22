package org.wei.agent.protocol;

import java.util.List;
import java.util.Map;

import org.apache.hadoop.ipc.ProtocolInfo;
import org.wei.util.AgentStat;
import org.wei.util.Cons;
import org.wei.util.FileStat;
/**
 * RPC协议
 * @author 魏伦凯
 *
 */
@ProtocolInfo(protocolName = "", protocolVersion = Cons.VersionID_RPC_VERSION)
public interface DataAgentProtocol {
	
	public AgentStat getStat();
	
	public long getCurrentBlockSequence();
	
	public long getCurrentBlockSize();
	
	public long getCurrentBlockOffset();

	public List<String> getErrorHosts();

	void setDesFileDir(String fileDir);

	void setFileName(String fileName);

	Map<String, FileStat> getFileStat();

	void setErrorHosts(List<String> errorHosts);

	String getFileName();

	String getDesFileDir();

	boolean initSenderAndHosts(String slaves);

	boolean getSendFlag();

}
