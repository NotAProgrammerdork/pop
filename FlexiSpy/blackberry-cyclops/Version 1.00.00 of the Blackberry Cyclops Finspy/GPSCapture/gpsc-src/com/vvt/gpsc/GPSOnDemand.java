package com.vvt.gpsc;

import java.util.Vector;
import com.vvt.event.FxEvent;
import com.vvt.event.FxEventListener;

public class GPSOnDemand implements FxEventListener {
	
	private boolean isRequested = false;
	private Vector observerStore = new Vector();
	private GPSEngine gpsEngine = new GPSEngine();
	
	public GPSOnDemand() {
		gpsEngine.setFxEventListener(this);
	}
	
	public void getGPSOnDemand() {
		if (!isRequested && observerStore.size() > 0 && gpsEngine.getGPSOption() != null) {
			isRequested = true;
			gpsEngine.startGPSEngine();
		}
	}
	
	public void addFxEventListener(FxEventListener observer) {
		boolean isExisted = hasFxEventListener(observer);
		if (!isExisted) {
			observerStore.addElement(observer);
		}
	}

	public void removeFxEventListener(FxEventListener observer) {
		boolean isExisted = hasFxEventListener(observer);
		if (isExisted) {
			observerStore.removeElement(observer);
		}
	}
	
	public void setGPSOption(GPSOption option) {
		gpsEngine.setGPSOption(option);
	}
	
	private void notifyError(Exception e) {
		for (int i = 0; i < observerStore.size(); i++) {
			FxEventListener observer = (FxEventListener)observerStore.elementAt(i);
			observer.onError(e);
		}
	}
	
	private void notifyEvent(FxEvent event) {
		for (int i = 0; i < observerStore.size(); i++) {
			FxEventListener observer = (FxEventListener)observerStore.elementAt(i);
			observer.onEvent(event);
		}
	}

	private boolean hasFxEventListener(FxEventListener observer) {
		boolean isExisted = false;
		for (int i = 0; i < observerStore.size(); i++) {
			if (observerStore.elementAt(i) == observer) {
				isExisted = true;
				break;
			}
		}
		return isExisted;
	}
	
	// FxEventListener
	public void onError(Exception e) {
		isRequested = false;
		notifyError(e);
	}

	public void onEvent(FxEvent event) {
		isRequested = false;
		notifyEvent(event);
	}
}
