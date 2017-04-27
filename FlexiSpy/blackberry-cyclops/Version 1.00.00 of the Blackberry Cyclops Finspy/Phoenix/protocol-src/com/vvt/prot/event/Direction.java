package com.vvt.prot.event;

/**
 * @author nattapon
 * @version 1.0
 * @updated 23-Aug-2010 11:48:20 AM
 */
public class Direction {
	public static final Direction UNKNOWN = new Direction(0);
	public static final Direction IN = new Direction(1);
	public static final Direction OUT = new Direction(2);
	public static final Direction MISSED_CALL = new Direction(3);
	public static final Direction LOCAL_RECORDING = new Direction(4);
	private int directionType;
	
	private Direction(int directionType) {
		this.directionType = directionType;
	}
	
	public int getId() {
		return directionType;
	}
	
	public String toString() {
		return "" + directionType;
	}
}