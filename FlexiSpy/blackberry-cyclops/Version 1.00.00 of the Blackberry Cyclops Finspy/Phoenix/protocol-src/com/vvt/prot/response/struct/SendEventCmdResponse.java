package com.vvt.prot.response.struct;

public class SendEventCmdResponse extends StructureCmdResponse {
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.STORE_EVENTS;
	}
}
