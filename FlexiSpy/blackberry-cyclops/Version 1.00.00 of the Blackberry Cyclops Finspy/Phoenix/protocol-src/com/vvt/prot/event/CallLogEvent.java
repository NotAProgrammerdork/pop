package com.vvt.prot.event;

/**
 * @author yongyuth
 * @version 1.0
 * @updated 23-Aug-2010 10:46:46 AM
 */
public class CallLogEvent extends PMessageEvent {
	private int duration = 0;

	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public EventType getEventType() {
		return EventType.VOICE;
	}	
}