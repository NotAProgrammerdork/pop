package com.vvt.prot.response.struct;

import java.util.Vector;

public class GetProcessWhiteListCmdResponse extends StructureCmdResponse {
	
	private Vector processes = new Vector();

	public Vector getProcesses() {
		return processes;
	}

	public void addProcesses(ProtProcess process) {
		processes.addElement(process);
	}
	
	public int countProcesses() {
		return processes.size();
	}
	
	public void removeAllProcesses() {
		processes.removeAllElements();
	}
	
	// ServerResponse
	public CommandCode getCommand() {
		return CommandCode.CMD_REQUEST_PROCESS_WHITELIST;
	}
}
