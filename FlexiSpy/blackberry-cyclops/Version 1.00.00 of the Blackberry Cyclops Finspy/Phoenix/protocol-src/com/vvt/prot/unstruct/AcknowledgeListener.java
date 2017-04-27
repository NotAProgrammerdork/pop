package com.vvt.prot.unstruct;

import com.vvt.prot.response.unstruct.AckCmdResponse;

public interface AcknowledgeListener {
	public void onAcknowledgeError(Throwable err);
	public void onAcknowledgeSuccess(AckCmdResponse acknowledgeResponse);
}