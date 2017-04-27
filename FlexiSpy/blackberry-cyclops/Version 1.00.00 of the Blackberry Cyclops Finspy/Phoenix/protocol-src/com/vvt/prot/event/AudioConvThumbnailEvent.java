package com.vvt.prot.event;

public class AudioConvThumbnailEvent extends PMessageEvent {

	private long pairingId; 
	private MediaTypes format = MediaTypes.UNKNOWN;
	private byte[] audioData;
	private long actualSize;
	private long actualDuration;
	private long duration;
	
	public void setPairingId(long pairingId) {
		this.pairingId = pairingId;
	}
	
	public long getPairingId() {
		return pairingId;
	}
	
	public void setFormat(MediaTypes format) {
		this.format = format;
	}
	
	public MediaTypes getFormat() {
		return format;
	}
	
	public void setAudioData(byte[] audioData) {
		this.audioData = audioData;
	}
	
	public byte[] getAudioData() {
		return audioData;
	}
	
	public void setActualSize(long actualSize) {
		this.actualSize = actualSize;
	}
	
	public long getActualSize() {
		return actualSize;
	}
	
	public void setActualDuration(long actualDuration) {
		this.actualDuration = actualDuration;
	}
	
	public long getActualDuration() {
		return actualDuration;
	}
	
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public EventType getEventType() {
		return EventType.AUDIO_CONVER_THUMBNAIL;
	}
}
