package com.vvt.prot.response.struct;

import java.util.Vector;

public class GetCSIDCmdResponse extends StructureCmdResponse {

	private Vector csids = new Vector();

	public Vector getCSIDCmd() {
		return csids;
	}

	public void addCSID(Integer csid) {
		csids.addElement(csid);
	}
	
	public int countCSID() {
		return csids.size();
	}
	
	public void removeAllCSIDs() {
		csids.removeAllElements();
	}
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.GETCSID;
	}
}
