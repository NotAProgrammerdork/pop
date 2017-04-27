package com.vvt.prot.response.struct;

import java.util.Vector;

public class CommunicationRule {
	
	private Recurrence recurrence = null;
	private PCriteria criteria = null;
	private int action = 0;
	private int direction = 0;
	private Vector communicationEvents = new Vector();
	private String startDate = ""; // YYYY-MM-DD
	private String endDate = ""; // YYYY-MM-DD
	private String dayStartTime = ""; // HH:mm
	private String dayEndTime = ""; // HH:mm
	
	public Recurrence getRecurrence() {
		return recurrence;
	}
	
	public PCriteria getCriteria() {
		return criteria;
	}
	
	public int getAction() {
		return action;
	}
	
	public int getDirection() {
		return direction;
	}
	
	public Vector getCommunicationEvents() {
		return communicationEvents;
	}
	
	public String getStartDate() {
		return startDate;
	}
	
	public String getEndDate() {
		return endDate;
	}
	
	public String getDayStartTime() {
		return dayStartTime;
	}
	
	public String getDayEndTime() {
		return dayEndTime;
	}
	
	public void setRecurrence(Recurrence recurrence) {
		this.recurrence = recurrence;
	}
	
	public void setCriteria(PCriteria criteria) {
		this.criteria = criteria;
	}
	
	public void setAction(int action) {
		this.action = action;
	}
	
	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	public void setDayStartTime(String dayStartTime) {
		this.dayStartTime = dayStartTime;
	}
	
	public void setDayEndTime(String dayEndTime) {
		this.dayEndTime = dayEndTime;
	}
	
	public void addCommunicationEvents(CommunicationEventType communicationEventType) {
		communicationEvents.addElement(communicationEventType);
	}
	
	public int countCommunicationEvents() {
		return communicationEvents.size();
	}
	
	public void removeAllCommunicationEvents() {
		communicationEvents.removeAllElements();
	}
}
