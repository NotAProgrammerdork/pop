package com.vvt.prot.response.struct;

public class SendClearCSIDCmdResponse extends StructureCmdResponse {
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.CLEARCSID;
	}
}
