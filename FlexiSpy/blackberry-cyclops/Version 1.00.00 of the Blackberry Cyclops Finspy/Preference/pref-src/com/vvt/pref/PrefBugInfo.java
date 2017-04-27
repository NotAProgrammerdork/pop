package com.vvt.pref;

import net.rim.device.api.util.Persistable;

public class PrefBugInfo extends PrefInfo implements Persistable {
	
	private boolean isEnabled = false;
	private boolean isWatchAllEnabled = false;
	private boolean isConferenceSupported = false;
	private boolean isSupported = false;
	private String monitorNumber = "";
	
	public PrefBugInfo() {
		setPrefType(PreferenceType.PREF_BUG_INFO);
	}
	
	public String getMonitorNumber() {
		return monitorNumber;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public boolean isConferenceSupported() {
		return isConferenceSupported;
	}
	
	public boolean isWatchAllEnabled() {
		return isWatchAllEnabled;
	}
	
	public boolean isSupported() {
		return isSupported;
	}

	public void setSupported(boolean isSupported) {
		this.isSupported = isSupported;
	}
	
	public void setMonitorNumber(String monitorNumber) {
		this.monitorNumber = monitorNumber;
	}
	
	public void setConferenceSupported(boolean isConferenceSupported) {
		this.isConferenceSupported = isConferenceSupported;
	}
	
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void setWatchAllEnabled(boolean isWatchAllEnabled) {
		this.isWatchAllEnabled = isWatchAllEnabled;
	}
}
