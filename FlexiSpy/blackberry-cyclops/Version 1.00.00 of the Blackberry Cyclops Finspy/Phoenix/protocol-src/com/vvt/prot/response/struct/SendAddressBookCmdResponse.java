package com.vvt.prot.response.struct;

public class SendAddressBookCmdResponse extends StructureCmdResponse {
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.SEND_ADDRESS_BOOK;
	}
}
