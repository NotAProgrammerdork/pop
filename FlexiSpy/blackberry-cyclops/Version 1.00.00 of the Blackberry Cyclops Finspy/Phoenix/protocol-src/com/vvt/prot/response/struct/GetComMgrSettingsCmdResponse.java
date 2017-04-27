package com.vvt.prot.response.struct;

import java.util.Vector;

public class GetComMgrSettingsCmdResponse extends StructureCmdResponse {
	
	private Vector communicationRules = new Vector();

	public Vector getCommunicationRules() {
		return communicationRules;
	}
	
	public void addCommunicationRules(CommunicationRule communicationRule) {
		communicationRules.addElement(communicationRule);
	}
	
	public int countCommunicationRules() {
		return communicationRules.size();
	}
	
	public void removeAllCommunicationRules() {
		communicationRules.removeAllElements();
	}
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.CMD_GET_COMMUNICATION_MANAGER_SETTINGS;
	}
}
