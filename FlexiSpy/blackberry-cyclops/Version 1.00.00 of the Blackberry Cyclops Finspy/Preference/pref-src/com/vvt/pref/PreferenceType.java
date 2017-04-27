package com.vvt.pref;

import net.rim.device.api.util.Persistable;

public final class PreferenceType implements Persistable {
	
	public static final PreferenceType PREF_UNKNOWN = new PreferenceType(0);
	public static final PreferenceType PREF_EVENT_INFO = new PreferenceType(1);
	public static final PreferenceType PREF_BUG_INFO = new PreferenceType(2);
	public static final PreferenceType PREF_CELL_INFO = new PreferenceType(3);
	public static final PreferenceType PREF_GPS = new PreferenceType(4);
	public static final PreferenceType PREF_IM = new PreferenceType(5);
	public static final PreferenceType PREF_SYSTEM = new PreferenceType(6);
	public static final PreferenceType PREF_GENERAL = new PreferenceType(7);
	private int id;
	
	private PreferenceType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
