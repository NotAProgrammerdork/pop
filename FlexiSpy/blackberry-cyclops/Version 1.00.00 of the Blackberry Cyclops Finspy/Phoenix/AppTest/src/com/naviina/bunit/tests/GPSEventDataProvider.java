package com.naviina.bunit.tests;

import java.util.Vector;

import com.vvt.prot.CommandDataProvider;
import com.vvt.prot.event.GPSEvent;
import com.vvt.prot.event.GPSExtraFields;
import com.vvt.prot.event.GPSField;
import com.vvt.prot.event.GPSProviders;

public class GPSEventDataProvider implements CommandDataProvider {
	
	private GPSEvent gpsEvent;
	private int count;
	private Vector gpsStore = new Vector();
	
	public GPSEventDataProvider() {
		gpsEvent = new GPSEvent();
		int eventId = 1;
		gpsEvent.setEventId(eventId);
		String eventTime = "2010-05-13 09:41:22";
		gpsEvent.setEventTime(eventTime);
		double latitude = 13.284868;
		gpsEvent.setLatitude(latitude);
		double longitude = 82.4233811;
		gpsEvent.setLongitude(longitude);
		GPSField firstField = new GPSField();
		firstField.setGpsFieldId(GPSExtraFields.HOR_ACCURACY.getId());
		float horAccuracy = 1.02f;
		firstField.setGpsFieldData(horAccuracy);
		gpsEvent.addGPSField(firstField);
		GPSField secondField = new GPSField();
		secondField.setGpsFieldId(GPSExtraFields.PROVIDER.getId());
		int provider = GPSProviders.AGPS.getId();
		secondField.setGpsFieldProviderData(provider);
		gpsEvent.addGPSField(secondField);
		gpsStore.addElement(gpsEvent);
		
		gpsEvent = new GPSEvent();
		eventId = 2;
		gpsEvent.setEventId(eventId);
		eventTime = "2010-05-13 09:41:22";
		gpsEvent.setEventTime(eventTime);
		latitude = 13.123456789;
		gpsEvent.setLatitude(latitude);
		longitude = 82.987654123;
		gpsEvent.setLongitude(longitude);
		gpsStore.addElement(gpsEvent);		
	}
		
	public Object getObject() {
		count++;
		return (Object) gpsStore.elementAt(count-1);
	}

	public boolean hasNext() {
		return count<gpsStore.size();
	}

}
