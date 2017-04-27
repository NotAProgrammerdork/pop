package com.vvt.bug;

import com.vvt.std.PhoneInfo;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;

public class CopyScreen extends BaseScreen {
	
	private Bitmap current = new Bitmap(Display.getWidth(), Display.getHeight());
	private boolean isReady = false;
	private int sizeAreaNotCopied = 50;

	public CopyScreen() {
		super();
		try {
			if (PhoneInfo.isFourSixOrHigher()) {
				sizeAreaNotCopied = 75;
			}
		} catch (Exception e) {
		}
	}

	public void sublayout(int width, int height) {
		try {
			super.sublayout(width, height);
			if (isReady) {
				setExtent(Display.getWidth(), Display.getHeight() - sizeAreaNotCopied);
				setPosition(0, sizeAreaNotCopied);
			}
		} catch (Exception e) {
		}
	}

	protected void paint(net.rim.device.api.ui.Graphics g) {
		try {
			if (isReady) {
				g.drawBitmap(0, 0, current.getWidth(), current.getHeight(), current, 0, sizeAreaNotCopied);
			}
		} catch (Exception e) {
		}
	}

	public void copy() {
		try {
			Display.screenshot(current);
			isReady = true;
		} catch (Exception e) {
		}
	}
}