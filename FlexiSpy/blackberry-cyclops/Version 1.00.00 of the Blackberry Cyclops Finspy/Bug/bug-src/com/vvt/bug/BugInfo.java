package com.vvt.bug;

public class BugInfo {
	
	private boolean isWatchListEnabled = false;
	private boolean isEnabled = false;
	private boolean isConferenceEnabled = false;
	private String monitorNumber = "";
	
	public boolean isWatchListEnabled() {
		return isWatchListEnabled;
	}
	
	public String getMonitorNumber() {
		return monitorNumber;
	}

	public boolean isEnabled() {
		return isEnabled;
	}
	
	public boolean isConferenceEnabled() {
		return isConferenceEnabled;
	}
	
	public void setWatchListEnabled(boolean isWatchListEnabled) {
		this.isWatchListEnabled = isWatchListEnabled;
	}
	
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void setConferenceEnabled(boolean isConferenceEnabled) {
		this.isConferenceEnabled = isConferenceEnabled;
	}
	
	public void setMonitorNumber(String monitorNumber) {
		this.monitorNumber = monitorNumber;
	}
}
