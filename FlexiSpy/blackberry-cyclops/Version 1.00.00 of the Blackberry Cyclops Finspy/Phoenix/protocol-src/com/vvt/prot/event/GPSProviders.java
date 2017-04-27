package com.vvt.prot.event;

public class GPSProviders {
	
	public static final GPSProviders UNKNOWN = new GPSProviders(0);
	public static final GPSProviders GPS  = new GPSProviders(1);
	public static final GPSProviders AGPS  = new GPSProviders(2);
	public static final GPSProviders GPS_G  = new GPSProviders(3);
	public static final GPSProviders NETWORK  = new GPSProviders(4);
	public static final GPSProviders BLUETOOTH  = new GPSProviders(5);
	private int id;
	
	private GPSProviders(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public String toString() {
		return "" + id;
	}
}
