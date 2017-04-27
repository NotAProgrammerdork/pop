package com.vvt.prot;

import com.vvt.prot.response.CmdResponse;

public interface CommandListener {
	//public void onError(long csid, Exception e);
	public void onSuccess(CmdResponse response);
	public void onConstructError(long csid, Exception e);
	public void onTransportError(long csid, Exception e);
}
