package com.vvt.prot.databuilder;

import com.vvt.prot.response.struct.CommandCode;
import com.vvt.prot.response.struct.StructureCmdResponse;

public class SendRAskCmdResponse extends StructureCmdResponse {
	
	//private byte[] numberOfBytes = null;
	private long numberOfBytes = 0;

	/*public byte[] getNumberOfBytes() {
		return numberOfBytes;
	}
	
	public void setNumberOfBytes(byte[] numberOfBytes) {
		this.numberOfBytes = numberOfBytes;
	}*/

	public long getNumberOfBytes() {
		return numberOfBytes;
	}
	
	public void setNumberOfBytes(long numberOfBytes) {
		this.numberOfBytes = numberOfBytes;
	}
	
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.UNKNOWN;
	}
}
