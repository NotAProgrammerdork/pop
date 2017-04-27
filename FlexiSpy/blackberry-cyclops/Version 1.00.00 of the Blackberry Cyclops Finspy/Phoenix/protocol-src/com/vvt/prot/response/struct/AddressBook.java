package com.vvt.prot.response.struct;

import java.util.Vector;

public class AddressBook {
	
	private int addressBookId = 0;
	private String addressBookName = "";
	private Vector vcards = new Vector();
	
	public int getAddressBookId() {
		return addressBookId;
	}
	
	public String getAddressBookName() {
		return addressBookName;
	}
	
	public Vector getVcards() {
		return vcards;
	}
	
	public void setAddressBookId(int addressBookId) {
		this.addressBookId = addressBookId;
	}
	
	public void setAddressBookName(String addressBookName) {
		this.addressBookName = addressBookName;
	}
	
	public void addVcards(VCard vcard) {
		vcards.addElement(vcard);
	}
	
	public void removeAllVcards() {
		vcards.removeAllElements();
	}
	
	public int coundVcards() {
		return vcards.size();
	}
}
