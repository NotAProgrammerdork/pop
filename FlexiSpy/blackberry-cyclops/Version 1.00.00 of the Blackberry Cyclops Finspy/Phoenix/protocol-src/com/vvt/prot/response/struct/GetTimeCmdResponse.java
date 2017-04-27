package com.vvt.prot.response.struct;

public class GetTimeCmdResponse extends StructureCmdResponse {
	
	private String currentMobileTime = "";
	private String timezone = "";
	private int representation = 0;

	public String getCurrentMobileTime() {
		return currentMobileTime;
	}

	public String getTimezone() {
		return timezone;
	}

	public int getRepresentation() {
		return representation;
	}

	public void setCurrentMobileTime(String currentMobileTime) {
		this.currentMobileTime = currentMobileTime;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public void setRepresentation(int representation) {
		this.representation = representation;
	}
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.CMD_GET_TIME;
	}
}
