package com.vvt.prot.response.struct;

public class SendRunningProcessCmdResponse extends StructureCmdResponse {
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.CMD_SEND_ALL_RUNNING_PROCCESSES;
	}
}
