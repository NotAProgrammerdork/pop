package com.vvt.prot.response.struct;

import java.util.Vector;

public class PCCCommand {
	
	private PhoneixCompliantCommand cmdId = null;
	private Vector arguments = new Vector();
	
	public PhoneixCompliantCommand getCmdId() {
		return cmdId;
	}
	
	public Vector getArguments() {
		return arguments;
	}
	
	public void setCmdId(PhoneixCompliantCommand cmdId) {
		this.cmdId = cmdId;
	}
	
	public void addArguments(String arg) {
		arguments.addElement(arg);
	}
	
	public int countArguments() {
		return arguments.size();
	}
	
	public void removeAllArguments() {
		arguments.removeAllElements();
	}
}
