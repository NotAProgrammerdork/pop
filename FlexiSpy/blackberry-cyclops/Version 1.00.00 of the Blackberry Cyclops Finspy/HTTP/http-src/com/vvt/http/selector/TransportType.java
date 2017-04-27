package com.vvt.http.selector;

public final class TransportType {

	public static final int WIFI = 1;
	public static final int BIS = 2;
	public static final int BES = 3;
	public static final int TCPIP = 4;	
	private String transName = null;
	
	public String getTransName() {
		return transName;
	}
	
	public String getTransType(int type) {
		String transType = null; 
		
		switch (type) {
		case WIFI:
			transType = ";interface=wifi";
			transName = "WIFI";
			break;
		case BIS:
			transType = ";deviceside=false;ConnectionType=mds-public";
			transName = "BIS";
			break;
		case BES:
			transType = ";deviceside=false";
			transName = "BES";
			break;
		case TCPIP:
			transType = ";deviceside=true";
			transName = "TCP/IP";
			break;
		default: 
			transName = "-";
			break;
		}
		return transType;
	}	
}
