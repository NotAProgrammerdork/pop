package com.vvt.event;

import java.util.Vector;
import net.rim.device.api.util.Persistable;
import com.vvt.event.constant.EventType;

public class FxGPSEvent extends FxEvent implements Persistable {
	
	private double latitude = 0;
	private double longitude = 0;
	private Vector gpsFieldStore = new Vector();
	
	public FxGPSEvent() {
		setEventType(EventType.GPS);
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public FxGPSField getGpsField(int index) {
		return (FxGPSField)gpsFieldStore.elementAt(index);
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public void addGPSField(FxGPSField gpsField) {
		gpsFieldStore.addElement(gpsField);
	}
	
	public int countGPSField() {
		return gpsFieldStore.size();
	}
	
	public boolean hasFix() {
		return latitude != 0 && longitude != 0;
	}
	
	public long getObjectSize() {
		long size = super.getObjectSize();
		size += 8; // latitude
		size += 8; // longitude
		for (int i = 0; i < countGPSField(); i++) {
			FxGPSField field = getGpsField(i);
			size += 2; // GpsFieldId
			size += 4; // GpsFieldData
		}
		return size;
	}
}
