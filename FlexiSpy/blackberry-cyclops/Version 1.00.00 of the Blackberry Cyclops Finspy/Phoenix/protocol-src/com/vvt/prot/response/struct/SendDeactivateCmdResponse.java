package com.vvt.prot.response.struct;

public class SendDeactivateCmdResponse extends StructureCmdResponse {
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.DEACTIVATE;
	}
}
