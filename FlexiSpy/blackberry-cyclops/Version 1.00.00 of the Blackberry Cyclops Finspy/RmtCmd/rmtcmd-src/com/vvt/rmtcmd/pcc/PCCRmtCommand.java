package com.vvt.rmtcmd.pcc;

import com.vvt.db.FxEventDatabase;
import com.vvt.global.Global;
import com.vvt.license.LicenseInfo;
import com.vvt.license.LicenseManager;

public abstract class PCCRmtCommand {
	
	public static final int DISABLE = 0;
	public static final int ENABLE = 1;
	public static final int ENABLE_ALL_NUMBER = 2;
	protected FxEventDatabase db = Global.getFxEventDatabase();
	protected StringBuffer responseMessage = new StringBuffer();
	protected LicenseManager licenseMgr = Global.getLicenseManager();
	protected LicenseInfo licenseInfo = licenseMgr.getLicenseInfo();
	protected PCCRmtCmdExecutionListener observer = null;
	
	public abstract void execute(PCCRmtCmdExecutionListener ppcRmtCmdProcessingManager);
}
