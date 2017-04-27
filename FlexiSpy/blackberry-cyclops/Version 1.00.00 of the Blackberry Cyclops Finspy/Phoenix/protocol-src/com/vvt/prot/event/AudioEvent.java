package com.vvt.prot.event;
import java.util.Vector;

public class AudioEvent extends PEvent {
	private short format = (short)MediaTypes.UNKNOWN.getId();
	private String location = null;
	private byte[] audioData = null;
	private Vector callLogStore = new Vector();
	
	public short getFormat() {
		return format;
	}
	
	public String getLocation() {
		return location;
	}
	
	public byte[] getAudioData() {
		return audioData;
	}
	
	public void setFormat(short format) {
		this.format = format;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public void setAudioData(byte[] audioData) {
		this.audioData = audioData;
	}
	
	public int lengthOfLocation() {
		return location.length();
	}
	
	public long lenghtOfAudioData() {
		return audioData.length;
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
		return EventType.AUDIO;
	}

}

