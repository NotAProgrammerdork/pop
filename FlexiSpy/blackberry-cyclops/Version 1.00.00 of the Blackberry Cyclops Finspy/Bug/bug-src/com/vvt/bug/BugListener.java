package com.vvt.bug;

import com.vvt.smsutil.FxSMSMessage;

public interface BugListener {
	public void onCall(PhoneEventListenerSettings pelSettings, FxSMSMessage smsMessage);
	public void onFinish();
}
