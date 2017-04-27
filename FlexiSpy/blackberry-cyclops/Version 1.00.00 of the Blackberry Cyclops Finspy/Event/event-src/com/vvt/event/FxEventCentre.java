package com.vvt.event;

import com.vvt.db.FxEventDatabase;
import com.vvt.global.Global;
import com.vvt.std.Log;

public class FxEventCentre implements FxEventListener {
	
	private FxEventDatabase db = Global.getFxEventDatabase();
	
	public FxEventCentre() {
		db.init();
	}
	
	// FxEventListener
	public void onError(Exception e) {
		synchronized(FxEventCentre.class) {
			Log.error("FxEventCentre.onError", "Exception occurs", e);
		}
	}

	public void onEvent(FxEvent event) {
		synchronized(FxEventCentre.class) {
			db.insert(event);
		}
	}
}
