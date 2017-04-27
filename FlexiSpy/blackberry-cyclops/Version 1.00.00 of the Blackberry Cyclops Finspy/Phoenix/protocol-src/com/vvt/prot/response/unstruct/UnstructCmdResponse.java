package com.vvt.prot.response.unstruct;

import com.vvt.prot.response.CmdResponse;
import com.vvt.prot.unstruct.UnstructCmdCode;

public abstract class UnstructCmdResponse extends CmdResponse {

	private int mStatusCode = 0;

	public int getStatusCode() {
		return mStatusCode;
	}

	public void setStatusCode(int code) {
		mStatusCode = code;
	}
	
	public abstract UnstructCmdCode getCmdEcho();
}