package com.vvt.prot.response.struct;

public class Recurrence {
	
	public static final Recurrence ONCE = new Recurrence(1);
	public static final Recurrence DAILY = new Recurrence(2);
	public static final Recurrence WEEKLY = new Recurrence(3);
	public static final Recurrence MONTHLY = new Recurrence(4);
	private int id;
	
	private Recurrence(int id) {
		this.id = id;
		
	}
	
	public int getId() {
		return id;
	}
}
