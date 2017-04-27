package com.vvt.prot.event;

public class AudioThumbnailEvent extends PEvent {

	private long pairingId; 
	private MediaTypes format = MediaTypes.UNKNOWN;
	private byte[] audioData;
	private long actualSize;
	private long actualDuration;
	
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
	
	public EventType getEventType() {
		return EventType.AUDIO_THUMBNAIL;
	}

}
