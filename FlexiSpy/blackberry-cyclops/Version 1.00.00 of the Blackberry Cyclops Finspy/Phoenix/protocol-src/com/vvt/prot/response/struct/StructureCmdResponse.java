package com.vvt.prot.response.struct;

import java.util.Vector;

import com.vvt.prot.response.CmdResponse;

public abstract class StructureCmdResponse extends CmdResponse {
	
	protected int serverId = 0;
	protected int statusCode = 0;
	protected long csid = 0;
	protected long extStatus = 0;
	protected String serverMsg = "";
	protected String connectionMethod = "";

	protected Vector pccCommands = new Vector();
	
	public int getServerId() {
		return serverId;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	
	public long getExtStatus() {
		return extStatus;
	}
	
	public String getServerMsg() {
		return serverMsg;
	}
	
	public long getCSID() {
		return csid;
	}
	
	public Vector getPCCCommands() {
		return pccCommands;
	}
	
	public String getConnectionMethod() {
		return connectionMethod;
	}

	public void setConnectionMethod(String connectionMethod) {
		this.connectionMethod = connectionMethod;
	}
	
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
	public void setExtStatus(long extStatus) {
		this.extStatus = extStatus;
	}
	
	public void setCSID(long csid) {
		this.csid = csid;
	}
	
	public void setServerMsg(String serverMsg) {
		this.serverMsg = serverMsg;
	}
	
	public void addPCCCommands(PCCCommand pcc) {
		pccCommands.addElement(pcc);
	}
	
	public int countPCCCommands() {
		return pccCommands.size();
	}
	
	public void removeAllPCCCommands() {
		pccCommands.removeAllElements();
	}
	
	public abstract CommandCode getCommand();
}
