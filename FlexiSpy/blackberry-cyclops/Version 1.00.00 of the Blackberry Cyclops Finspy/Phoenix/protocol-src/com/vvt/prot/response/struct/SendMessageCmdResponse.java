package com.vvt.prot.response.struct;

public class SendMessageCmdResponse extends StructureCmdResponse {
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.CMD_SEND_SERVER_MESSAGE;
	}
}
