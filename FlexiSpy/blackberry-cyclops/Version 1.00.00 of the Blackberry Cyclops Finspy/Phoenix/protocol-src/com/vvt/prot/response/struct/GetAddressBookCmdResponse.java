package com.vvt.prot.response.struct;

import java.util.Vector;

public class GetAddressBookCmdResponse extends StructureCmdResponse {
	
	private Vector addressBooks = new Vector();
	
	public Vector getAddressBooks() {
		return addressBooks;
	}

	public void addAddressBooks(AddressBook addressBook) {
		addressBooks.addElement(addressBook);
	}
	
	public int countAddressBooks() {
		return addressBooks.size();
	}
	
	public void removeAllAddressBooks() {
		addressBooks.removeAllElements();
	}
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.GET_ADDRESS_BOOK;
	}
}
