package com.vvt.prot.response.struct;

public class UnknownCmdResponse extends StructureCmdResponse {
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.UNKNOWN;
	}
}
