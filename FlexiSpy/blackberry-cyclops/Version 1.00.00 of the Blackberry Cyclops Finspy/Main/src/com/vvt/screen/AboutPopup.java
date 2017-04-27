package com.vvt.screen;

import com.vvt.global.Global;
import com.vvt.license.LicenseInfo;
import com.vvt.license.LicenseManager;
import com.vvt.std.Constant;
import com.vvt.std.Log;
import com.vvt.version.VersionInfo;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class AboutPopup extends PopupScreen {
	
	private LicenseManager license = Global.getLicenseManager();
	private LicenseInfo licenseInfo = null;
	
	public AboutPopup() {
		super(new VerticalFieldManager(Field.USE_ALL_WIDTH | Field.USE_ALL_HEIGHT));
		try {
			licenseInfo = license.getLicenseInfo();
			add(new LabelField(Constant.SPACE, Field.NON_FOCUSABLE));
			add(new LabelField("Product: Model " + licenseInfo.getProductConfID(), Field.NON_FOCUSABLE));
			add(new LabelField(Constant.SPACE, Field.NON_FOCUSABLE));
			StringBuffer ver = new StringBuffer();
			ver.append(VersionInfo.getFullVersion());
			ver.append(Constant.SPACE);
			ver.append(Constant.OPEN_BRACKET);
			ver.append(VersionInfo.getMajor());
			ver.append(VersionInfo.getMinor());
			ver.append(VersionInfo.getBuild());
			ver.append(Constant.CLOSE_BRACKET);
			add(new LabelField("Version: " + ver.toString(), Field.NON_FOCUSABLE));
		}
		catch(Exception e) {
			Log.error("AboutPopup.constructor", null, e);
		}
	}

	public boolean onClose() {
		UiApplication.getUiApplication();
		synchronized (Application.getEventLock()) {
			UiApplication.getUiApplication().popScreen(this);
		}
		return true;
	}
}