package com.vvt.prot.event;


public class CameraImageThumbnailEvent extends PEvent {
	private long pairingId; 
	private MediaTypes format = MediaTypes.UNKNOWN;
	private long actualSize;
	private double longitude;
	private double lattitude;
	private float altitude;
	private byte[] imageData;
	
	
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
	
	public void setActualSize(long actualSize) {
		this.actualSize = actualSize;
	}
	
	public long getActualSize() {
		return actualSize;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	public void setLattitude(double lattitude) {
		this.lattitude = lattitude; 
	}
	
	public double getLattitude() {
		return lattitude;
	}
	
	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}
	
	public float getAltitude() {
		return altitude;
	}
	
	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}
	
	public byte[] getImageData() {
		return imageData;
	}
	
	
	public EventType getEventType() {
		return EventType.CAMERA_IMAGE_THUMBNAIL;
	}
}
