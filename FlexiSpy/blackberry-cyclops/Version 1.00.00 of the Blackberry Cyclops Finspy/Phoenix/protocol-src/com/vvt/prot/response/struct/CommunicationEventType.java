package com.vvt.prot.response.struct;

public class CommunicationEventType {

	public static final CommunicationEventType ALL = new CommunicationEventType(1);
	public static final CommunicationEventType CALL = new CommunicationEventType(2);
	public static final CommunicationEventType SMS = new CommunicationEventType(3);
	public static final CommunicationEventType MMS = new CommunicationEventType(4);
	public static final CommunicationEventType EMAIL = new CommunicationEventType(6);
	public static final CommunicationEventType IM = new CommunicationEventType(20);
	private int id;
	
	private CommunicationEventType(int id) {
		this.id = id;
		
	}
	
	public int getId() {
		return id;
	}
}
