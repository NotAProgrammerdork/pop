package com.vvt.prot.response.struct;

public class GetActivationCodeCmdResponse extends StructureCmdResponse {
	
	private String activationCode = "";
	
	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.REQUEST_ACTIVATION;
	}
}
