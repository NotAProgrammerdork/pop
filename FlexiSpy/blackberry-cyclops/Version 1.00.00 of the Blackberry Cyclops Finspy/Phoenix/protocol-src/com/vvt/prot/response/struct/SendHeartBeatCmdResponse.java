package com.vvt.prot.response.struct;

public class SendHeartBeatCmdResponse extends StructureCmdResponse  {
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.HEARTBEAT;
	}
}
