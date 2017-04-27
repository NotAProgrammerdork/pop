package com.vvt.gpsc;

import com.vvt.event.FxEvent;
import com.vvt.event.FxEventCapture;
import com.vvt.event.FxEventListener;

public class GPSCapture extends FxEventCapture implements FxEventListener {
	
	private GPSEngine gpsEngine = new GPSEngine();
	
	public GPSCapture() {
		gpsEngine.setFxEventListener(this);
	}
	
	public void setGPSOption(GPSOption option) {
		gpsEngine.setGPSOption(option);
	}
	
	public void startCapture() {
		if (!isEnabled() && sizeOfFxEventListener() > 0 && gpsEngine.getGPSOption() != null) {
			setEnabled(true);
			gpsEngine.startGPSEngine();
		}
	}
	
	public void stopCapture() {
		if (isEnabled()) {
			setEnabled(false);
			gpsEngine.stopGPSEngine();
		}
	}

	// FxEventListener
	public void onError(Exception e) {
		if (isEnabled()) {
			notifyError(e);
			if (gpsEngine.isEnabled()) {
				gpsEngine.stopGPSEngine();
			}
			gpsEngine.startGPSEngine();
		}
	}
	
	public void onEvent(FxEvent event) {
		if (isEnabled()) {
			notifyEvent(event);
			if (gpsEngine.isEnabled()) {
				gpsEngine.stopGPSEngine();
			}
			gpsEngine.startGPSEngine();
		}
	}
}
