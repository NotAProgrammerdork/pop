package com.vvt.prot.command;

import com.vvt.prot.DataProvider;

public class AddressBook {
	private long addrBookId;
	private String addrBookName;
	private int vCardCount;
	private DataProvider vcardDataProvider;
	
	public void setAddressBookId(long addrBookId) {
		this.addrBookId = addrBookId;
	}
	
	public long getAddressBookId() {
		return addrBookId;
	}
	
	public void setAddressBookName(String addressBookName) {
		this.addrBookName = addressBookName;
	}
	
	public String getAddressBookName() {
		return addrBookName;
	}
	
	public void setVCardCount(int vCardCount) {
		this.vCardCount = vCardCount;
	}
	
	public int getVCardCount() {
		return vCardCount;
	}
	
	public void setVCardProvider(DataProvider vcardDataProvider) {
		this.vcardDataProvider = vcardDataProvider;
	}
	
	public DataProvider getVCardProvider() {
		return vcardDataProvider;
	}
}
