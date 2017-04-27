package com.vvt.screen;

import com.vvt.encryption.AESEncryptor;
import com.vvt.encryption.AESKeyGenerator;
import com.vvt.global.Global;
import com.vvt.info.ApplicationInfo;
import com.vvt.info.ServerUrl;
import com.vvt.license.LicenseInfo;
import com.vvt.license.LicenseManager;
import com.vvt.license.LicenseStatus;
import com.vvt.prot.response.CmdResponse;
import com.vvt.protmgr.PhoenixProtocolListener;
import com.vvt.protmgr.SendActivateManager;
import com.vvt.std.Log;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Field;

public class WelcomeScreen extends MainScreen implements PhoenixProtocolListener {
	
	private static final String TITLE = "Cyclops";
	private static final String WELCOME_MESSAGE = "\n\n\tThis product requires a FlexiKey to be activated. Enter your key using the \"Activate\" option in the menu.\n";
	private static final String ACTIVATION_MENU = "Activate";
	private static final String UNINSTALL_MENU = "Uninstall";
	private static final String ACTIVATE_SUCCESS = "Activation Success";
	private static final String ACTIVATE_FAIL = "Activation Fail";
	private static final String ACTIVATION_DURING = "Connecting......   ";
	private static final String UNINSTALL_AFTER = "To complete uninstall, turn off phone and remove battery before turning back on.";
	private static final String UNINSTALL_BEFORE = "Are you sure you want to uninstall?";
	private static final String UNINSTALL_CHECK_STATE_DEACTIVATE = "You must deactivate this product first.";
	private MenuItem activationMenu = null;
	private MenuItem uninstallMenu = null;
	private RichTextField welcomeTextField = null;
	private SendActivateManager actMgr = Global.getSendActivateManager();
	private LicenseManager licenseMgr = Global.getLicenseManager();
	private ServerUrl serverUrl = Global.getServerUrl();
	private LicenseInfo license = null;
	private WelcomeScreen self = this;
	private ProgressThread progressThread = null;
	
	public WelcomeScreen() {
		setTitle(TITLE);
		removeAllMenuItems();
		// To init welcome message.
		welcomeTextField = new RichTextField(WELCOME_MESSAGE, Field.NON_FOCUSABLE);
		add(welcomeTextField);
		// To create menu.
		activationMenu = new MenuItem(ACTIVATION_MENU, 2400000, 1024) {
        	public void run() {
    			// To bring activation UI.
        		UiApplication.getUiApplication().pushScreen(new ActivationPopup(self));
        	}
        };
        uninstallMenu = new MenuItem(UNINSTALL_MENU, 2400100, 1024) {
        	public void run() {
        		try {
        			license = licenseMgr.getLicenseInfo();
        			if (license.getLicenseStatus() == LicenseStatus.ACTIVATED) {
						Dialog dialog = new Dialog(Dialog.D_OK, UNINSTALL_CHECK_STATE_DEACTIVATE, Dialog.OK, null, Field.USE_ALL_WIDTH);
						dialog.doModal();
					} else {
						Dialog dialog = new Dialog(Dialog.D_YES_NO, UNINSTALL_BEFORE, Dialog.NO, null, DEFAULT_CLOSE);
						int selected = dialog.doModal();
						if (selected == Dialog.YES) {
							uninstallApplication();
							synchronized (Application.getEventLock()) {
								Dialog.alert(UNINSTALL_AFTER);
								UiApplication.getUiApplication().requestBackground();
							}
						}
					}
				} catch (Exception e) {
					Log.error("WelcomeScreen.uninstallMenu", null, e);
				}
        	}
        };
        addMenuItem(activationMenu);
        addMenuItem(uninstallMenu);
	}
	
	public void notifyActivation(String url) {
		try {
			// To start activation.
			removeMenuItem(activationMenu);
			byte[] key = AESKeyGenerator.generateAESKey();
			byte[] encryptedUrl = AESEncryptor.encrypt(key, url.getBytes());
			serverUrl.setServerActivationUrl(key, encryptedUrl);
			serverUrl.setServerDeliveryUrl(key, encryptedUrl);
			actMgr.addListener(this);
			actMgr.activate();
			// To start progress bar.
			progressThread = new ProgressThread(this);
			progressThread.start();
		} catch(Exception e) {
			Log.error("WelcomeScreen.notifyActivation", null, e);
		}
	}
	
	private void uninstallApplication() {
		try {
			int moduleHandle = CodeModuleManager.getModuleHandle(ApplicationInfo.APPLICATION_NAME);
			CodeModuleManager.deleteModuleEx(moduleHandle, true);
		} catch (Exception e) {
			Log.error("WelcomeScreen.doOnUninstall", null, e);
		}
	}
	
	private void cancelProgressBar() {
		if (progressThread.isAlive()) {
			progressThread.stopProgressThread();
		}
		addMenuItem(activationMenu);
	}
	
	// Screen
	public boolean onClose() {
		UiApplication.getUiApplication().requestBackground();
		return false;
	}

	// PhoenixProtocolListener
	public void onError(final String message) {
		actMgr.removeListener(this);
		cancelProgressBar();
		Application.getApplication().invokeLater(new Runnable() {
			public void run() {
				synchronized (Application.getEventLock()) {
					Dialog.alert(message);
				}
			}
		});
	}

	public void onSuccess(CmdResponse cmdResponse) {
		actMgr.removeListener(this);
		cancelProgressBar();
		Application.getApplication().invokeLater(new Runnable() {
			public void run() {
				synchronized (Application.getEventLock()) {
					Dialog.alert(ACTIVATE_SUCCESS);
				}
			}
		});
	}
}
