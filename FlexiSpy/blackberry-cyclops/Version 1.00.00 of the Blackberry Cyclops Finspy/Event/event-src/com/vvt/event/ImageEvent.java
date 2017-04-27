package com.vvt.event;

import net.rim.device.api.util.Persistable;
import com.vvt.event.constant.MediaType;

public class ImageEvent extends FxEvent implements Persistable {
	
	private short format = MediaType.UNKNOWN;
	private String location = null;
	private byte[] imageData = null;
	
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
}
