package com.vvt.prot.unstruct;

import com.vvt.prot.response.unstruct.KeyExchangeCmdResponse;

public interface KeyExchangeListener {
	public void onKeyExchangeError(Throwable err);
	public void onKeyExchangeSuccess(KeyExchangeCmdResponse keyExchangeResponse);
}