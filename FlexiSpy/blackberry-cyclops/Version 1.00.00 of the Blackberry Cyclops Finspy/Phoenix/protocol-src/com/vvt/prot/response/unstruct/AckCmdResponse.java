package com.vvt.prot.response.unstruct;

import com.vvt.prot.unstruct.UnstructCmdCode;

public class AckCmdResponse extends UnstructCmdResponse {

	// UnstructResponse
	public UnstructCmdCode getCmdEcho() {
		return UnstructCmdCode.UCMD_ACKNOWLEDGE;
	}
}