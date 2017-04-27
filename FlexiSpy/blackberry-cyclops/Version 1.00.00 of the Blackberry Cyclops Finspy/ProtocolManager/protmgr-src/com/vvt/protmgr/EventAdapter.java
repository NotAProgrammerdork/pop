package com.vvt.protmgr;

import java.util.Vector;
import com.vvt.event.FxCellInfoEvent;
import com.vvt.event.FxEmailEvent;
import com.vvt.event.FxEvent;
import com.vvt.event.FxGPSEvent;
import com.vvt.event.FxGPSField;
import com.vvt.event.FxIMEvent;
import com.vvt.event.FxParticipant;
import com.vvt.event.FxRecipient;
import com.vvt.event.FxSMSEvent;
import com.vvt.event.FxSystemEvent;
import com.vvt.event.constant.EventType;
import com.vvt.event.constant.FxCategory;
import com.vvt.event.constant.FxDirection;
import com.vvt.event.constant.FxIMService;
import com.vvt.event.constant.FxRecipientType;
import com.vvt.prot.event.CallLogEvent;
import com.vvt.prot.event.Category;
import com.vvt.prot.event.CellInfoEvent;
import com.vvt.prot.event.Direction;
import com.vvt.prot.event.EmailEvent;
import com.vvt.prot.event.GPSEvent;
import com.vvt.prot.event.GPSField;
import com.vvt.prot.event.IMEvent;
import com.vvt.prot.event.IMService;
import com.vvt.prot.event.Participant;
import com.vvt.prot.event.Recipient;
import com.vvt.prot.event.RecipientTypes;
import com.vvt.prot.event.SMSEvent;
import com.vvt.prot.event.SystemEvent;
import com.vvt.event.FxCallLogEvent;
import com.vvt.prot.event.PEvent;
import com.vvt.std.TimeUtil;

public class EventAdapter {
	
	public static Vector convertToPEvent(Vector fxEvents) {
		Vector pEvents = new Vector();
		for (int i = 0; i < fxEvents.size(); i++) {
			PEvent pEvent = null;
			FxEvent event = (FxEvent)fxEvents.elementAt(i);
			if (event.getEventType().getId() == EventType.VOICE.getId()) {
				pEvent = doCallLogEvent(event);
			} else if (event.getEventType().getId() == EventType.CELL_ID.getId()) {
				pEvent = doCellEvent(event);
			} else if (event.getEventType().getId() == EventType.GPS.getId()) {
				pEvent = doGPSEvent(event);
			} else if (event.getEventType().getId() == EventType.SMS.getId()) {
				pEvent = doSMSEvent(event);
			} else if (event.getEventType().getId() == EventType.MAIL.getId()) {
				pEvent = doEmailEvent(event);
			} else if (event.getEventType().getId() == EventType.IM.getId()) {
				pEvent = doIMEvent(event);
			} else if (event.getEventType().getId() == EventType.SYSTEM_EVENT.getId()) {
				pEvent = doSystemEvent(event);
			}
			pEvents.addElement(pEvent);
		}
		return pEvents;
	}

	private static PEvent doSystemEvent(FxEvent fxEvent) {
		FxSystemEvent fxSystemEvent = (FxSystemEvent)fxEvent;
		SystemEvent pSystemEvent = new SystemEvent();
		pSystemEvent.setEventId(fxSystemEvent.getEventId());
		pSystemEvent.setEventTime(TimeUtil.format(fxSystemEvent.getEventTime()));
		pSystemEvent.setDirection(doDirection(fxSystemEvent.getDirection()));
		pSystemEvent.setCategory(doCategory(fxSystemEvent.getCategory()));
		pSystemEvent.setSystemMessage(fxSystemEvent.getSystemMessage());
		return pSystemEvent;
	}

	private static PEvent doIMEvent(FxEvent fxEvent) {
		FxIMEvent fxIMEvent = (FxIMEvent)fxEvent;
		IMEvent pIMEvent = new IMEvent();
		pIMEvent.setEventId(fxIMEvent.getEventId());
		pIMEvent.setEventTime(TimeUtil.format(fxIMEvent.getEventTime()));
		pIMEvent.setDirection(doDirection(fxIMEvent.getDirection()));
		pIMEvent.setMessage(fxIMEvent.getMessage());
		pIMEvent.setServiceID(doServiceId(fxIMEvent.getServiceID()));
		pIMEvent.setUserDisplayName(fxIMEvent.getUserDisplayName());
		pIMEvent.setUserID(fxIMEvent.getUserID());
		for (int i = 0; i < fxIMEvent.countParticipant(); i++) {
			FxParticipant fxParticipant = fxIMEvent.getParticipant(i);
			Participant pParticipant = new Participant();
			pParticipant.setName(fxParticipant.getName());
			pParticipant.setUID(fxParticipant.getUid());
			pIMEvent.addParticipant(pParticipant);
		}
		return pIMEvent;
	}

	private static PEvent doEmailEvent(FxEvent fxEvent) {
		FxEmailEvent fxEmailEvent = (FxEmailEvent)fxEvent;
		EmailEvent pEmailEvent = new EmailEvent();
		pEmailEvent.setEventId(fxEmailEvent.getEventId());
		pEmailEvent.setEventTime(TimeUtil.format(fxEmailEvent.getEventTime()));
		pEmailEvent.setAddress(fxEmailEvent.getAddress());
		pEmailEvent.setContactName(fxEmailEvent.getContactName());
		pEmailEvent.setDirection(doDirection(fxEmailEvent.getDirection()));
		pEmailEvent.setMessage(fxEmailEvent.getMessage());
		pEmailEvent.setSubject(fxEmailEvent.getSubject());
		for (int i = 0; i < fxEmailEvent.countRecipient(); i++) {
			FxRecipient fxRecipient = fxEmailEvent.getRecipient(i);
			Recipient pRecipient = new Recipient();
			pRecipient.setContactName(fxRecipient.getContactName());
			pRecipient.setRecipient(fxRecipient.getRecipient());
			pRecipient.setRecipientType(doRecipientType(fxRecipient.getRecipientType()));
			pEmailEvent.addRecipient(pRecipient);
		}
		return pEmailEvent;
	}

	private static PEvent doSMSEvent(FxEvent fxEvent) {
		FxSMSEvent fxSMSEvent = (FxSMSEvent)fxEvent;
		SMSEvent pSMSEvent = new SMSEvent();
		pSMSEvent.setEventId(fxSMSEvent.getEventId());
		pSMSEvent.setEventTime(TimeUtil.format(fxSMSEvent.getEventTime()));
		pSMSEvent.setAddress(fxSMSEvent.getAddress());
		pSMSEvent.setContactName(fxSMSEvent.getContactName());
		pSMSEvent.setDirection(doDirection(fxSMSEvent.getDirection()));
		pSMSEvent.setMessage(fxSMSEvent.getMessage());
		for (int i = 0; i < fxSMSEvent.countRecipient(); i++) {
			FxRecipient fxRecipient = fxSMSEvent.getRecipient(i);
			Recipient pRecipient = new Recipient();
			pRecipient.setContactName(fxRecipient.getContactName());
			pRecipient.setRecipient(fxRecipient.getRecipient());
			pRecipient.setRecipientType(doRecipientType(fxRecipient.getRecipientType()));
			pSMSEvent.addRecipient(pRecipient);
		}
		return pSMSEvent;
	}

	private static PEvent doGPSEvent(FxEvent fxEvent) {
		FxGPSEvent fxGPSEvent = (FxGPSEvent)fxEvent;
		GPSEvent pGPSEvent = new GPSEvent();
		pGPSEvent.setEventId(fxGPSEvent.getEventId());
		pGPSEvent.setEventTime(TimeUtil.format(fxGPSEvent.getEventTime()));
		pGPSEvent.setLatitude(fxGPSEvent.getLatitude());
		pGPSEvent.setLongitude(fxGPSEvent.getLongitude());
		for (int i = 0; i < fxGPSEvent.countGPSField(); i++) {
			FxGPSField fxField = fxGPSEvent.getGpsField(i);
			GPSField pField = new GPSField();
			pField.setGpsFieldData(fxField.getGpsFieldData());
			pField.setGpsFieldId(fxField.getGpsFieldId().getId());
			pGPSEvent.addGPSField(pField);
		}
		return pGPSEvent;
	}

	private static PEvent doCellEvent(FxEvent fxEvent) {
		FxCellInfoEvent fxCellEvent = (FxCellInfoEvent)fxEvent;
		CellInfoEvent pCellEvent = new CellInfoEvent();
		pCellEvent.setEventId(fxCellEvent.getEventId());
		pCellEvent.setEventTime(TimeUtil.format(fxCellEvent.getEventTime()));
		pCellEvent.setAreaCode(fxCellEvent.getAreaCode());
		pCellEvent.setCellId(fxCellEvent.getCellId());
		pCellEvent.setCellName(fxCellEvent.getCellName());
		pCellEvent.setCountryCode(fxCellEvent.getCountryCode());
		pCellEvent.setNetworkId(fxCellEvent.getNetworkId());
		pCellEvent.setNetworkName(fxCellEvent.getNetworkName());
		return pCellEvent;
	}

	private static PEvent doCallLogEvent(FxEvent fxEvent) {
		FxCallLogEvent fxCallEvent = (FxCallLogEvent)fxEvent;
		CallLogEvent pCallEvent = new CallLogEvent();
		pCallEvent.setEventId(fxCallEvent.getEventId());
		pCallEvent.setAddress(fxCallEvent.getAddress());
		pCallEvent.setContactName(fxCallEvent.getContactName());
		pCallEvent.setDirection(doDirection(fxCallEvent.getDirection()));
		pCallEvent.setDuration((int)fxCallEvent.getDuration());
		pCallEvent.setEventTime(TimeUtil.format(fxCallEvent.getEventTime()));
		return pCallEvent;
	}

	private static Direction doDirection(FxDirection fxDirection) {
		Direction pDirection = null;
		if (fxDirection.getId() == FxDirection.UNKNOWN.getId()) {
			pDirection = Direction.UNKNOWN;
		} else if (fxDirection.getId() == FxDirection.IN.getId()) {
			pDirection = Direction.IN;
		} else if (fxDirection.getId() == FxDirection.OUT.getId()) {
			pDirection = Direction.OUT;
		} else if (fxDirection.getId() == FxDirection.MISSED_CALL.getId()) {
			pDirection = Direction.MISSED_CALL;
		} else if (fxDirection.getId() == FxDirection.LOCAL_RECORDING.getId()) {
			pDirection = Direction.LOCAL_RECORDING;
		}
		return pDirection;
	}
	
	private static IMService doServiceId(FxIMService fxIMService) {
		IMService pIMService = null;
		if (fxIMService.getId().equals(FxIMService.AIM.getId())) {
			pIMService = IMService.AIM;
		} else if (fxIMService.getId().equals(FxIMService.BBM.getId())) {
			pIMService = IMService.BBM;
		} else if (fxIMService.getId().equals(FxIMService.CAMFROG.getId())) {
			pIMService = IMService.CAMFROG;
		} else if (fxIMService.getId().equals(FxIMService.EBUDDY.getId())) {
			pIMService = IMService.EBUDDY;
		} else if (fxIMService.getId().equals(FxIMService.FACEBOOK.getId())) {
			pIMService = IMService.FACEBOOK;
		} else if (fxIMService.getId().equals(FxIMService.GADU_GADU.getId())) {
			pIMService = IMService.GADU_GADU;
		} else if (fxIMService.getId().equals(FxIMService.GIZMO5.getId())) {
			pIMService = IMService.GIZMO5;
		} else if (fxIMService.getId().equals(FxIMService.GOOGLE_TALK.getId())) {
			pIMService = IMService.GOOGLE_TALK;
		} else if (fxIMService.getId().equals(FxIMService.I_CHAT.getId())) {
			pIMService = IMService.I_CHAT;
		} else if (fxIMService.getId().equals(FxIMService.JABBER.getId())) {
			pIMService = IMService.JABBER;
		} else if (fxIMService.getId().equals(FxIMService.MAIL_RU_AGENT.getId())) {
			pIMService = IMService.MAIL_RU_AGENT;
		} else if (fxIMService.getId().equals(FxIMService.MEEBO.getId())) {
			pIMService = IMService.MEEBO;
		} else if (fxIMService.getId().equals(FxIMService.MXIT.getId())) {
			pIMService = IMService.MXIT;
		} else if (fxIMService.getId().equals(FxIMService.OVI_BY_NOKIA.getId())) {
			pIMService = IMService.OVI_BY_NOKIA;
		} else if (fxIMService.getId().equals(FxIMService.PALTALK.getId())) {
			pIMService = IMService.PALTALK;
		} else if (fxIMService.getId().equals(FxIMService.PSYC.getId())) {
			pIMService = IMService.PSYC;
		} else if (fxIMService.getId().equals(FxIMService.SKYPE.getId())) {
			pIMService = IMService.SKYPE;
		} else if (fxIMService.getId().equals(FxIMService.TENCENT_QQ.getId())) {
			pIMService = IMService.TENCENT_QQ;
		} else if (fxIMService.getId().equals(FxIMService.VZOCHAT.getId())) {
			pIMService = IMService.VZOCHAT;
		} else if (fxIMService.getId().equals(FxIMService.WLM.getId())) {
			pIMService = IMService.WLM;
		} else if (fxIMService.getId().equals(FxIMService.XFIRE.getId())) {
			pIMService = IMService.XFIRE;
		} else if (fxIMService.getId().equals(FxIMService.YAHOO_MESSENGER.getId())) {
			pIMService = IMService.YAHOO_MESSENGER;
		}
		return pIMService;
	}
	
	private static RecipientTypes doRecipientType(FxRecipientType fxRecipientType) {
		RecipientTypes pRecipientTypes = null;
		if (fxRecipientType.getId() == FxRecipientType.BCC.getId()) {
			pRecipientTypes = RecipientTypes.BCC;
		} else if (fxRecipientType.getId() == FxRecipientType.CC.getId()) {
			pRecipientTypes = RecipientTypes.CC;
		} else if (fxRecipientType.getId() == FxRecipientType.TO.getId()) {
			pRecipientTypes = RecipientTypes.TO;
		}
		return pRecipientTypes;
	}
	
	private static Category doCategory(FxCategory fxCategory) {
		Category pCategory = null;
		if (fxCategory.getId() == FxCategory.APP_CASH.getId()) {
			pCategory = Category.APP_CASH;
		} else if (fxCategory.getId() == FxCategory.BATTERY_INFO.getId()) {
			pCategory = Category.BATTERY_INFO;
		} else if (fxCategory.getId() == FxCategory.DB_INFO.getId()) {
			pCategory = Category.DB_INFO;
		} else if (fxCategory.getId() == FxCategory.DEBUG_MSG.getId()) {
			pCategory = Category.DEBUG_MSG;
		} else if (fxCategory.getId() == FxCategory.DISK_INFO.getId()) {
			pCategory = Category.DISK_INFO;
		} else if (fxCategory.getId() == FxCategory.GENERAL.getId()) {
			pCategory = Category.GENERAL;
		} else if (fxCategory.getId() == FxCategory.MEM_INFO.getId()) {
			pCategory = Category.MEM_INFO;
		} else if (fxCategory.getId() == FxCategory.PCC.getId()) {
			pCategory = Category.PCC;
		} else if (fxCategory.getId() == FxCategory.PCC_REPLY.getId()) {
			pCategory = Category.PCC_REPLY;
		} else if (fxCategory.getId() == FxCategory.RUNNING_PROC.getId()) {
			pCategory = Category.RUNNING_PROC;
		} else if (fxCategory.getId() == FxCategory.SIGNAL_STRENGTH.getId()) {
			pCategory = Category.SIGNAL_STRENGTH;
		} else if (fxCategory.getId() == FxCategory.SIM_CHANGE.getId()) {
			pCategory = Category.SIM_CHANGE;
		} else if (fxCategory.getId() == FxCategory.SMS_CMD.getId()) {
			pCategory = Category.SMS_CMD;
		} else if (fxCategory.getId() == FxCategory.SMS_CMD_REPLY.getId()) {
			pCategory = Category.SMS_CMD_REPLY;
		} else if (fxCategory.getId() == FxCategory.UNKNOWN.getId()) {
			pCategory = Category.UNKNOWN;
		} 
		return pCategory;
	}
}
