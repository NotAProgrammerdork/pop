package com.vvt.gpsc;

import net.rim.device.api.util.Persistable;
import com.vvt.event.constant.GPSProvider;

public class GPSMethod implements Persistable {
	
	private GPSPriority priority = GPSPriority.DEFAULT_PRIORITY;
	private GPSProvider method = GPSProvider.UNKNOWN;
	
	public GPSProvider getMethod() {
		return method;
	}
	
	public GPSPriority getPriority() {
		return priority;
	}
	
	public void setMethod(GPSProvider method) {
		this.method = method;
	}
	
	public void setPriority(GPSPriority priority) {
		this.priority = priority;
	}
}
