package com.vvt.protmgr;

import com.vvt.prot.response.CmdResponse;

public interface PhoenixProtocolListener {
	public void onSuccess(CmdResponse response);
	public void onError(String message);
}
