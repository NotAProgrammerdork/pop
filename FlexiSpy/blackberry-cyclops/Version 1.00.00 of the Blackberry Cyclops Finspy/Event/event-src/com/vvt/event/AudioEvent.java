package com.vvt.event;

import java.util.Vector;
import com.vvt.event.constant.MediaType;
import net.rim.device.api.util.Persistable;

public class AudioEvent extends FxEvent implements Persistable {
	
	private short format = MediaType.UNKNOWN;
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
			FxCallLogEvent event = (FxCallLogEvent)callLogStore.elementAt(i);
			lenght += (1 + 8 + 2 + event.getAddress().length() + 2 + event.getContactName().length());
		}
		return lenght;
	}
}
