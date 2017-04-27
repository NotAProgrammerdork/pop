package com.vvt.info;

public interface ApplicationInfo {
	public static final int LIGHT_V_F = 1;
	public static final int LIGHT_I_F = 2;
	public static final int PRO_V_F = 3;
	public static final int PRO_I_F = 4;
	public static final int PROX_V_F = 5;
	public static final int PROX_I_F = 6;
	public static final int LIGHT_I_R = 1001;
	public static final int PRO_I_R = 1001;
	public static final int PROX_I_R = 1003;
	public static final int PROTOCOL_VERSION = 1;
	public static final String PRODUCT_VERSION = "1.0";
	public static final String DEFAULT_FX_KEY = "*#900900900";
	public static final String APPLICATION_NAME = "net_rim_bb_trust_application";
	public static final boolean DEBUG = true;
	public static final String[] LOCATION_TIMER = new String[] { "10 seconds",
		"30 seconds", "1 minute", "5 minutes", "10 minutes", "30 minutes",
		"40 minutes", "1 hour" };
	public static final int[] LOCATION_TIMER_SECONDS = new int[] { 10, 30, 60, 300,
		600, 1800, 2400, 3600 };
	public final static String[] TIME = new String[] { "5 minutes", "30 minutes", "1 hour", "2 hours", "6 hours", "12 hours", "24 hours" };
	public final static int TIME_VALUE[] = new int[] { 5 * 60, 30 * 60, 1 * 60 * 60, 2 * 60 * 60, 6 * 60 * 60, 12 * 60 * 60, 24 * 60 * 60 };
	public final static String[] EVENT = new String[] { "1 event", "5 events", "10 events", "50 events", "100 events" };
	public final static int[] EVENT_VALUE = new int[] { 1, 5, 10, 50, 100 };
}
