package com.vvt.prot.response.unstruct;

import com.vvt.prot.unstruct.UnstructCmdCode;

public class AckSecCmdResponse extends UnstructCmdResponse {

	// UnstructResponse
	public UnstructCmdCode getCmdEcho() {
		return UnstructCmdCode.UCMD_ACKNOWLEDGE_SECURE;
	}
}