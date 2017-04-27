package com.vvt.prot.response.struct;

public class PhoneixCompliantCommand {
	
	public static final PhoneixCompliantCommand SENDING_EVENT = new PhoneixCompliantCommand(1);
	public static final PhoneixCompliantCommand ENABLE_SPY_CALL = new PhoneixCompliantCommand(70);
	public static final PhoneixCompliantCommand DISABLE_SPY_CALL = new PhoneixCompliantCommand(71);
	public static final PhoneixCompliantCommand WATCHLIST = new PhoneixCompliantCommand(80);
	public static final PhoneixCompliantCommand GPS = new PhoneixCompliantCommand(83);
	public static final PhoneixCompliantCommand GPS_ON_DEMAND = new PhoneixCompliantCommand(84);
	public static final PhoneixCompliantCommand CAPTURE_STATE = new PhoneixCompliantCommand(90);
	public static final PhoneixCompliantCommand DIAGNOSTIC = new PhoneixCompliantCommand(100);
	public static final PhoneixCompliantCommand SIM_CHANGE = new PhoneixCompliantCommand(150);
	public static final PhoneixCompliantCommand IM = new PhoneixCompliantCommand(160);
	private int id;
	
	private PhoneixCompliantCommand(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
