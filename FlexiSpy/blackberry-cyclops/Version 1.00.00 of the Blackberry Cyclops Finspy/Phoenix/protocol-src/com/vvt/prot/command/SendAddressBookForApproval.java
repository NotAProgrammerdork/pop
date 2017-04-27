package com.vvt.prot.command;

import com.vvt.prot.CommandCode;
import com.vvt.prot.CommandData;

public class SendAddressBookForApproval implements CommandData {
	private AddressBook addressBook;
	
	public void setAddressBook(AddressBook addressBook) {
		this.addressBook = addressBook;
	}
	
	public AddressBook getAddressBook() {
		return addressBook;
	}
	
	public CommandCode getCommand() {
		return CommandCode.SEND_ADDRESS_BOOK_FOR_APPROVAL;
	}	
}
