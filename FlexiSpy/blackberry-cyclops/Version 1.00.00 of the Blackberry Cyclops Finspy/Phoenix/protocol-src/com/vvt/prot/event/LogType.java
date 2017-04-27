package com.vvt.prot.event;

public class LogType {
	public static final LogType UNKNOWN = new LogType(0);
	public static final LogType INCOMING_SMS_CMD = new LogType(1);
	public static final LogType OUTGOING_SMS_REPLY = new LogType(2);
	public static final LogType INCOMING_GPRS_CMD = new LogType(3);
	public static final LogType OUTGOING_GPRS_REPLY = new LogType(4);
	public static final LogType LOCAL_CHANGE = new LogType(5);
	public static final LogType COMM_MANAGER_VERBOSE_SETTINGS = new LogType(6);
	private int logType;
	
	private LogType(int logType) {
		this.logType = logType;
	}
	
	public int getId() {
		return logType;
	}
	
	public String toString() {
		return "" + logType;
	}
}
