package com.vvt.prot.response.struct;

public class CommandCode {

	public static final CommandCode UNKNOWN = new CommandCode(0);
	public static final CommandCode STORE_EVENTS = new CommandCode(1);
	public static final CommandCode ACTIVATE = new CommandCode(2);
	public static final CommandCode DEACTIVATE = new CommandCode(3);
	public static final CommandCode HEARTBEAT = new CommandCode(4);
	public static final CommandCode REQUEST_CONFIGURATION = new CommandCode(5);
	public static final CommandCode GETCSID = new CommandCode(6);
	public static final CommandCode CLEARCSID = new CommandCode(7);
	public static final CommandCode REQUEST_ACTIVATION = new CommandCode(8);
	public static final CommandCode GET_ADDRESS_BOOK = new CommandCode(9);
	public static final CommandCode SEND_ADDRESS_BOOK_FOR_APPROVAL = new CommandCode(10);
	public static final CommandCode SEND_ADDRESS_BOOK = new CommandCode(11);
	public static final CommandCode CMD_SEND_IMAGES = new CommandCode(12);
	public static final CommandCode CMD_SEND_AUDIO_CONVERSATIONS = new CommandCode(13);
	public static final CommandCode CMD_SEND_AUDIO_FILES = new CommandCode(14);
	public static final CommandCode CMD_SEND_VIDEOS = new CommandCode(15);
	public static final CommandCode CMD_GET_COMMUNICATION_MANAGER_SETTINGS = new CommandCode(16);
	public static final CommandCode CMD_GET_TIME = new CommandCode(17);
	public static final CommandCode CMD_SEND_SERVER_MESSAGE = new CommandCode(18);
	public static final CommandCode CMD_REQUEST_PROCESS_WHITELIST = new CommandCode(19);
	public static final CommandCode CMD_SEND_ALL_RUNNING_PROCCESSES = new CommandCode(20);	
	public static final CommandCode CMD_REQUEST_PROCESS_BLACKLIST = new CommandCode(21);	
	/*public static final CommandCode RESUMABLE = new CommandCode(1);
	public static final CommandCode NON_RESUMABLE = new CommandCode(2);
	public static final CommandCode RSEND = new CommandCode(3);
	public static final CommandCode RASK = new CommandCode(4);*/
	private int cmdCode;
	
	private CommandCode(int cmdCode) {
		this.cmdCode = cmdCode;
		
	}
	
	public int getId() {
		return cmdCode;
	}
	
	public String toString() {
		return ""+cmdCode;
	}
	
}
