package com.vvt.prot.response.struct;

public class SendAddressBookApprovalCmdResponse extends StructureCmdResponse {

	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.SEND_ADDRESS_BOOK_FOR_APPROVAL;
	}
}
