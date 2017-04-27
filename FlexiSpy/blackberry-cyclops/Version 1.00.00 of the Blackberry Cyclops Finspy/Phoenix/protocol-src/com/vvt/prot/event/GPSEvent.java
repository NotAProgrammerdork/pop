package com.vvt.prot.event;
import java.util.Vector;

public class GPSEvent extends PEvent {
	private double latitude = 0;
	private double longitude = 0;
	private Vector gpsFieldStore = new Vector();
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public GPSField getGpsField(int index) {
		return (GPSField)gpsFieldStore.elementAt(index);
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public void addGPSField(GPSField gpsField) {
		gpsFieldStore.addElement(gpsField);
	}
	
	public short countGPSField() {
		return (short)gpsFieldStore.size();
	}

	public EventType getEventType() {
		return EventType.GPS;
	}

}