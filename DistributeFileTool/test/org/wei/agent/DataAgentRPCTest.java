package org.wei.agent;

import java.net.InetSocketAddress;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.ipc.RPC;
import org.wei.agent.protocol.DataAgentProtocol;

public class DataAgentRPCTest {

	public static void main(String[] args) {

		InetSocketAddress addr = new InetSocketAddress("192.168.223.3", 5055);
		try {
			//RPC.getProtocolVersion(CaculateService.class);
			DataAgentProtocol agent = (DataAgentProtocol) RPC.getProxy(DataAgentProtocol.class,
					RPC.getProtocolVersion(DataAgentProtocol.class), addr, new Configuration());
			
			System.out.println(agent.getStat());
			System.out.println(agent.getDesFileDir());
			System.out.println(agent.getFileName());
			System.out.println(agent.getSendFlag());
			
			/*List<String> list = agent.getErrorHosts();
			for(String s : list){
				System.out.println(s);
			}
			*/
			
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Client has error,info is " + ex.getMessage());
		}

	

	}

}
