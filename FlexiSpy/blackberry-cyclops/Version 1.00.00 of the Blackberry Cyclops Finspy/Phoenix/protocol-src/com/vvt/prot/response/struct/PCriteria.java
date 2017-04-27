package com.vvt.prot.response.struct;

public class PCriteria {

	private int offset = 0;
	private int dayOfMonth = 0;
	private DayOfWeek dayOfWeek = null;
	
	public int getOffset() {
		return offset;
	}
	
	public int getDayOfMonth() {
		return dayOfMonth;
	}
	
	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public void setDayOfMonth(int dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}
	
	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
}
