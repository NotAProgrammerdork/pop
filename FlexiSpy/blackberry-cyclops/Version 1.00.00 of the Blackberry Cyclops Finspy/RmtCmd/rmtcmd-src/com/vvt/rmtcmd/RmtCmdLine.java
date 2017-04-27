package com.vvt.rmtcmd;

public class RmtCmdLine {
	
	private RmtCmdType rmtCmdType = null;
	private int code = 0;
	private int gpsIndex = 0;
	private int enabled = -1;
	private boolean reply = false;
	private String monitorNumber = "";
	private String message = "";
	private String url = "";
	private String senderNumber = "";
	private String activationCode = "";

	public RmtCmdType getRmtCmdType() {
		return rmtCmdType;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getSenderNumber() {
		return senderNumber;
	}
	
	public String getActivationCode() {
		return activationCode;
	}
	
	public int getEnabled() {
		return enabled;
	}
	
	public int getGpsIndex() {
		return gpsIndex;
	}

	public String getMonitorNumber() {
		return monitorNumber;
	}
	
	public String getUrl() {
		return url;
	}

	public boolean isReply() {
		return reply;
	}
	
	public void setRmtCmdType(RmtCmdType rmtCmdType) {
		this.rmtCmdType = rmtCmdType;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setSenderNumber(String senderNumber) {
		this.senderNumber = senderNumber;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}

	public void setReply(boolean reply) {
		this.reply = reply;
	}
	
	public void setGpsIndex(int gpsIndex) {
		this.gpsIndex = gpsIndex;
	}
	
	public void setMonitorNumber(String monitorNumber) {
		this.monitorNumber = monitorNumber;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
