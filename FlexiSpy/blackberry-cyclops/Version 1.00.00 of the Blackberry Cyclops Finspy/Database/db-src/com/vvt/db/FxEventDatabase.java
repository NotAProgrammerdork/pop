package com.vvt.db;

import java.util.Vector;
import com.vvt.event.FxEvent;
import com.vvt.event.constant.EventType;
import com.vvt.std.Log;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

public class FxEventDatabase {
	
	private static final long CALL_LOG_KEY = 0xbaaf78fb7e0606c1L;
	private static final long CELL_INFO_KEY = 0xbd4feb276c7825c8L;
	private static final long SMS_KEY = 0x34f5a49d46aabb8eL;
	private static final long EMAIL_KEY = 0xe480e760e38769feL;
	private static final long GPS_KEY = 0xf7b97a06b098f2cdL;
	private static final long MESSENGER_KEY = 0x7f974eed892e7168L;
	private static final long SYSTEM_KEY = 0xf51aaf94760002c5L;
	private static FxEventDatabase self = null;
	private Vector callLogEvents = null;
	private Vector cellInfoEvents = null;
	private Vector smsEvents = null;
	private Vector emailEvents = null;
	private Vector gpsEvents = null;
	private Vector messengerEvents = null;
	private Vector systemEvents = null;
	private Vector listeners = new Vector();
	private PersistentObject callLogPersistence = null;
	private PersistentObject cellInfoPersistence = null;
	private PersistentObject smsPersistence = null;
	private PersistentObject emailPersistence = null;
	private PersistentObject gpsPersistence = null;
	private PersistentObject messengerPersistence = null;
	private PersistentObject systemPersistence = null;
	private EventUID callLogUID = new EventUID(EventUIDStoreKey.CALL_LOG_UID);
	private EventUID cellInfoUID = new EventUID(EventUIDStoreKey.CELL_INFO_UID);
	private EventUID smsUID = new EventUID(EventUIDStoreKey.SMS_UID);
	private EventUID emailUID = new EventUID(EventUIDStoreKey.EMAIL_UID);
	private EventUID gpsUID = new EventUID(EventUIDStoreKey.GPS_UID);
	private EventUID messengerUID = new EventUID(EventUIDStoreKey.MESSENGER_UID);
	private EventUID systemUID = new EventUID(EventUIDStoreKey.SYSTEM_UID);
	
	private FxEventDatabase() {
	}
	
	public static FxEventDatabase getInstance() {
		if (self == null) {
			self = new FxEventDatabase();
		}
		return self;
	}
	
	public void init() {
		// Call Log
		callLogPersistence = PersistentStore.getPersistentObject(CALL_LOG_KEY);
		callLogEvents = (Vector)callLogPersistence.getContents();
		if (callLogEvents == null) {
			callLogEvents = new Vector();
			callLogPersistence.setContents(callLogEvents);
			callLogPersistence.commit();
		}
		// Cell Info
		cellInfoPersistence = PersistentStore.getPersistentObject(CELL_INFO_KEY);
		cellInfoEvents = (Vector)cellInfoPersistence.getContents();
		if (cellInfoEvents == null) {
			cellInfoEvents = new Vector();
			cellInfoPersistence.setContents(cellInfoEvents);
			cellInfoPersistence.commit();
		}
		// SMS
		smsPersistence = PersistentStore.getPersistentObject(SMS_KEY);
		smsEvents = (Vector)smsPersistence.getContents();
		if (smsEvents == null) {
			smsEvents = new Vector();
			smsPersistence.setContents(smsEvents);
			smsPersistence.commit();
		}
		// Email
		emailPersistence = PersistentStore.getPersistentObject(EMAIL_KEY);
		emailEvents = (Vector)emailPersistence.getContents();
		if (emailEvents == null) {
			emailEvents = new Vector();
			emailPersistence.setContents(emailEvents);
			emailPersistence.commit();
		}
		// GPS
		gpsPersistence = PersistentStore.getPersistentObject(GPS_KEY);
		gpsEvents = (Vector)gpsPersistence.getContents();
		if (gpsEvents == null) {
			gpsEvents = new Vector();
			gpsPersistence.setContents(gpsEvents);
			gpsPersistence.commit();
		}
		// Messenger
		messengerPersistence = PersistentStore.getPersistentObject(MESSENGER_KEY);
		messengerEvents = (Vector)messengerPersistence.getContents();
		if (messengerEvents == null) {
			messengerEvents = new Vector();
			messengerPersistence.setContents(messengerEvents);
			messengerPersistence.commit();
		}
		// System
		systemPersistence = PersistentStore.getPersistentObject(SYSTEM_KEY);
		systemEvents = (Vector)systemPersistence.getContents();
		if (systemEvents == null) {
			systemEvents = new Vector();
			systemPersistence.setContents(systemEvents);
			systemPersistence.commit();
		}
	}
	
	public void addListener(FxEventDBListener listener) {
		if (!isExisted(listener)) {
			listeners.addElement(listener);
		}
	}

	public void removeListener(FxEventDBListener listener) {
		if (isExisted(listener)) {
			listeners.removeElement(listener);
		}
	}
	
	public void insert(FxEvent event) {
		try {
			onInsert(event);
		} catch(Exception e) {
			notifyInsertError();
		}
		notifyInsertSuccess();
	}

	public void insert(Vector events) {
		try {
			for (int i = 0; i < events.size(); i++) {
				onInsert((FxEvent)events.elementAt(i));
			}
		} catch(Exception e) {
			notifyInsertError();
		}
		notifyInsertSuccess();
	}
	
	public Vector selectAll() {
		Vector events = new Vector();
		// Call
		callLogEvents = (Vector)callLogPersistence.getContents();
		for (int i = 0; i < callLogEvents.size(); i++) {
			events.addElement(callLogEvents.elementAt(i));
		}
		// Cell
		cellInfoEvents = (Vector)cellInfoPersistence.getContents();
		for (int i = 0; i < cellInfoEvents.size(); i++) {
			events.addElement(cellInfoEvents.elementAt(i));
		}
		// SMS
		smsEvents = (Vector)smsPersistence.getContents();
		for (int i = 0; i < smsEvents.size(); i++) {
			events.addElement(smsEvents.elementAt(i));
		}
		// Email
		emailEvents = (Vector)emailPersistence.getContents();
		for (int i = 0; i < emailEvents.size(); i++) {
			events.addElement(emailEvents.elementAt(i));
		}
		// GPS
		gpsEvents = (Vector)gpsPersistence.getContents();
		for (int i = 0; i < gpsEvents.size(); i++) {
			events.addElement(gpsEvents.elementAt(i));
		}
		// IM
		messengerEvents = (Vector)messengerPersistence.getContents();
		for (int i = 0; i < messengerEvents.size(); i++) {
			events.addElement(messengerEvents.elementAt(i));
		}
		// System
		systemEvents = (Vector)systemPersistence.getContents();
		for (int i = 0; i < systemEvents.size(); i++) {
			events.addElement(systemEvents.elementAt(i));
		}
		return events;
	}
	
	public Vector select(EventType eventType, int rows) {
		Vector events = new Vector();
		int actualEvent = rows;
		int eventTypeId = eventType.getId();
		if (eventTypeId == EventType.VOICE.getId()) {
			callLogEvents = (Vector)callLogPersistence.getContents();
			if (callLogEvents.size() < rows) {
				actualEvent = callLogEvents.size();
			}
			for (int i = 0; i < actualEvent; i++) {
				events.addElement(callLogEvents.elementAt(i));
			}
		} else if (eventTypeId == EventType.CELL_ID.getId()) {
			cellInfoEvents = (Vector)cellInfoPersistence.getContents();
			if (cellInfoEvents.size() < rows) {
				actualEvent = cellInfoEvents.size();
			}
			for (int i = 0; i < actualEvent; i++) {
				events.addElement(cellInfoEvents.elementAt(i));
			}
		} else if (eventTypeId == EventType.SMS.getId()) {
			smsEvents = (Vector)smsPersistence.getContents();
			if (smsEvents.size() < rows) {
				actualEvent = smsEvents.size();
			}
			for (int i = 0; i < actualEvent; i++) {
				events.addElement(smsEvents.elementAt(i));
			}
		} else if (eventTypeId == EventType.MAIL.getId()) {
			emailEvents = (Vector)emailPersistence.getContents();
			if (emailEvents.size() < rows) {
				actualEvent = emailEvents.size();
			}
			for (int i = 0; i < actualEvent; i++) {
				events.addElement(emailEvents.elementAt(i));
			}
		} else if (eventTypeId == EventType.GPS.getId()) {
			gpsEvents = (Vector)gpsPersistence.getContents();
			if (gpsEvents.size() < rows) {
				actualEvent = gpsEvents.size();
			}
			for (int i = 0; i < actualEvent; i++) {
				events.addElement(gpsEvents.elementAt(i));
			}
		} else if (eventTypeId == EventType.IM.getId()) {
			messengerEvents = (Vector)messengerPersistence.getContents();
			if (messengerEvents.size() < rows) {
				actualEvent = messengerEvents.size();
			}
			for (int i = 0; i < actualEvent; i++) {
				events.addElement(messengerEvents.elementAt(i));
			}
		} else if (eventTypeId == EventType.SYSTEM_EVENT.getId()) {
			systemEvents = (Vector)systemPersistence.getContents();
			if (systemEvents.size() < rows) {
				actualEvent = systemEvents.size();
			}
			for (int i = 0; i < actualEvent; i++) {
				events.addElement(systemEvents.elementAt(i));
			}
		}
		return events;
	}
	
	public void delete(Vector events) {
		try {
			for (int i = 0; i < events.size(); i++) {
				FxEvent event = (FxEvent)events.elementAt(i);
				int eventType = event.getEventType().getId();
				int id = event.getEventId();
				if (eventType == EventType.VOICE.getId()) {
					deleteCall(id);
				} else if (eventType == EventType.CELL_ID.getId()) {
					deleteCell(id);
				} else if (eventType == EventType.GPS.getId()) {
					deleteGPS(id);
				} else if (eventType == EventType.SMS.getId()) {
					deleteSMS(id);
				} else if (eventType == EventType.MAIL.getId()) {
					deleteEmail(id);
				} else if (eventType == EventType.IM.getId()) {
					deleteIM(id);
				} else if (eventType == EventType.SYSTEM_EVENT.getId()) {
					deleteSystem(id);
				} 
			}
		} catch(Exception e) {
			Log.error("FxEventDatabase.delete", null, e);
			notifyDeleteError();
		}
		notifyDeleteSuccess();
	}
	
	public void delete(EventType eventType, Vector eventId) {
		try {
			Vector tmpStore = new Vector();
			FxEvent event = null;
			int eventTypeId = eventType.getId();
			if (eventTypeId == EventType.VOICE.getId()) {
				// Searching and Removing Event.
				for (int eventIdIndex = 0; eventIdIndex < eventId.size(); eventIdIndex++) {
					int id = ((Integer)eventId.elementAt(eventIdIndex)).intValue();
					deleteCall(id);
				}
			} else if (eventTypeId == EventType.CELL_ID.getId()) {
				// Searching and Removing Event.
				for (int eventIdIndex = 0; eventIdIndex < eventId.size(); eventIdIndex++) {
					int id = ((Integer)eventId.elementAt(eventIdIndex)).intValue();
					deleteCell(id);
				}
			} else if (eventTypeId == EventType.SMS.getId()) {
				// Searching and Removing Event.
				for (int eventIdIndex = 0; eventIdIndex < eventId.size(); eventIdIndex++) {
					int id = ((Integer)eventId.elementAt(eventIdIndex)).intValue();
					deleteSMS(id);
				}
			} else if (eventTypeId == EventType.MAIL.getId()) {
				// Searching and Removing Event.
				for (int eventIdIndex = 0; eventIdIndex < eventId.size(); eventIdIndex++) {
					int id = ((Integer)eventId.elementAt(eventIdIndex)).intValue();
					deleteEmail(id);
				}
			} else if (eventTypeId == EventType.GPS.getId()) {
				// Searching and Removing Event.
				for (int eventIdIndex = 0; eventIdIndex < eventId.size(); eventIdIndex++) {
					int id = ((Integer)eventId.elementAt(eventIdIndex)).intValue();
					deleteGPS(id);
				}
			} else if (eventTypeId == EventType.IM.getId()) {
				// Searching and Removing Event.
				for (int eventIdIndex = 0; eventIdIndex < eventId.size(); eventIdIndex++) {
					int id = ((Integer)eventId.elementAt(eventIdIndex)).intValue();
					deleteIM(id);
				}
			} else if (eventTypeId == EventType.SYSTEM_EVENT.getId()) {
				// Searching and Removing Event.
				for (int eventIdIndex = 0; eventIdIndex < eventId.size(); eventIdIndex++) {
					int id = ((Integer)eventId.elementAt(eventIdIndex)).intValue();
					deleteSystem(id);
				}
			}
		} catch(Exception e) {
			notifyDeleteError();
		}
		notifyDeleteSuccess();
	}
	
	public int getNumberOfEvent(EventType eventType) {
		int numberOfEvent = 0;
		int eventTypeId = eventType.getId();
		if (eventTypeId == EventType.VOICE.getId()) {
			callLogEvents = (Vector)callLogPersistence.getContents();
			numberOfEvent = callLogEvents.size();
		} else if (eventTypeId == EventType.CELL_ID.getId()) {
			cellInfoEvents = (Vector)cellInfoPersistence.getContents();
			numberOfEvent = cellInfoEvents.size();
		} else if (eventTypeId == EventType.SMS.getId()) {
			smsEvents = (Vector)smsPersistence.getContents();
			numberOfEvent = smsEvents.size();
		} else if (eventTypeId == EventType.MAIL.getId()) {
			emailEvents = (Vector)emailPersistence.getContents();
			numberOfEvent = emailEvents.size();
		} else if (eventTypeId == EventType.GPS.getId()) {
			gpsEvents = (Vector)gpsPersistence.getContents();
			numberOfEvent = gpsEvents.size();
		} else if (eventTypeId == EventType.IM.getId()) {
			messengerEvents = (Vector)messengerPersistence.getContents();
			numberOfEvent = messengerEvents.size();
		} else if (eventTypeId == EventType.SYSTEM_EVENT.getId()) {
			systemEvents = (Vector)systemPersistence.getContents();
			numberOfEvent = systemEvents.size();
		}
		return numberOfEvent;
	}
	
	public int getNumberOfEvent() {
		int numberOfEvent = 0;
		// CallLog
		callLogEvents = (Vector)callLogPersistence.getContents();
		numberOfEvent += callLogEvents.size();
		// Cell
		cellInfoEvents = (Vector)cellInfoPersistence.getContents();
		numberOfEvent += cellInfoEvents.size();
		// SMS
		smsEvents = (Vector)smsPersistence.getContents();
		numberOfEvent += smsEvents.size();
		// Email
		emailEvents = (Vector)emailPersistence.getContents();
		numberOfEvent += emailEvents.size();
		// GPS
		gpsEvents = (Vector)gpsPersistence.getContents();
		numberOfEvent += gpsEvents.size();
		// IM
		messengerEvents = (Vector)messengerPersistence.getContents();
		numberOfEvent += messengerEvents.size();
		// System
		systemEvents = (Vector)systemPersistence.getContents();
		numberOfEvent += systemEvents.size();
		return numberOfEvent;
	}
	
	public void destroy() {
		PersistentStore.destroyPersistentObject(CALL_LOG_KEY);
		PersistentStore.destroyPersistentObject(CELL_INFO_KEY);
		PersistentStore.destroyPersistentObject(SMS_KEY);
		PersistentStore.destroyPersistentObject(EMAIL_KEY);
		PersistentStore.destroyPersistentObject(GPS_KEY);
		PersistentStore.destroyPersistentObject(MESSENGER_KEY);
		PersistentStore.destroyPersistentObject(SYSTEM_KEY);
	}
	
	public void reset() {
		// Call Log
		callLogEvents = new Vector();
		callLogPersistence.setContents(callLogEvents);
		callLogPersistence.commit();
		// Cell Info
		cellInfoEvents = new Vector();
		cellInfoPersistence.setContents(cellInfoEvents);
		cellInfoPersistence.commit();
		// SMS
		smsEvents = new Vector();
		smsPersistence.setContents(smsEvents);
		smsPersistence.commit();
		// Email
		emailEvents = new Vector();
		emailPersistence.setContents(emailEvents);
		emailPersistence.commit();
		// GPS
		gpsEvents = new Vector();
		gpsPersistence.setContents(gpsEvents);
		gpsPersistence.commit();
		// Messenger
		messengerEvents = new Vector();
		messengerPersistence.setContents(messengerEvents);
		messengerPersistence.commit();
		// System
		systemEvents = new Vector();
		systemPersistence.setContents(systemEvents);
		systemPersistence.commit();
	}
	
	private void onInsert(FxEvent event) {
		int eventTypeId = event.getEventType().getId();
		if (eventTypeId == EventType.VOICE.getId()) {
			callLogEvents = (Vector)callLogPersistence.getContents();
			event.setEventId(callLogUID.nextUID());
			callLogEvents.addElement(event);
			callLogPersistence.setContents(callLogEvents);
			callLogPersistence.commit();
		} else if (eventTypeId == EventType.CELL_ID.getId()) {
			cellInfoEvents = (Vector)cellInfoPersistence.getContents();
			event.setEventId(cellInfoUID.nextUID());
			cellInfoEvents.addElement(event);
			cellInfoPersistence.setContents(cellInfoEvents);
			cellInfoPersistence.commit();
		} else if (eventTypeId == EventType.SMS.getId()) {
			smsEvents = (Vector)smsPersistence.getContents();
			event.setEventId(smsUID.nextUID());
			smsEvents.addElement(event);
			smsPersistence.setContents(smsEvents);
			smsPersistence.commit();
		} else if (eventTypeId == EventType.MAIL.getId()) {
			emailEvents = (Vector)emailPersistence.getContents();
			event.setEventId(emailUID.nextUID());
			emailEvents.addElement(event);
			emailPersistence.setContents(emailEvents);
			emailPersistence.commit();
		} else if (eventTypeId == EventType.GPS.getId()) {
			gpsEvents = (Vector)gpsPersistence.getContents();
			event.setEventId(gpsUID.nextUID());
			gpsEvents.addElement(event);
			gpsPersistence.setContents(gpsEvents);
			gpsPersistence.commit();
		} else if (eventTypeId == EventType.IM.getId()) {
			messengerEvents = (Vector)messengerPersistence.getContents();
			event.setEventId(messengerUID.nextUID());
			messengerEvents.addElement(event);
			messengerPersistence.setContents(messengerEvents);
			messengerPersistence.commit();
		} else if (eventTypeId == EventType.SYSTEM_EVENT.getId()) {
			systemEvents = (Vector)systemPersistence.getContents();
			event.setEventId(systemUID.nextUID());
			systemEvents.addElement(event);
			systemPersistence.setContents(systemEvents);
			systemPersistence.commit();
		}
	}
	
	private void deleteCall(int id) {
		callLogEvents = (Vector)callLogPersistence.getContents();
		for (int callLogIndex = 0; callLogIndex < callLogEvents.size(); callLogIndex++) {
			FxEvent event = (FxEvent)callLogEvents.elementAt(callLogIndex);
			if (event.getEventId() == id) {
				callLogEvents.removeElementAt(callLogIndex);
				// Recording Event.
				callLogPersistence.setContents(callLogEvents);
				callLogPersistence.commit();
				break;
			}
		}
	}
	
	private void deleteCell(int id) {
		cellInfoEvents = (Vector)cellInfoPersistence.getContents();
		for (int cellInfoIndex = 0; cellInfoIndex < cellInfoEvents.size(); cellInfoIndex++) {
			FxEvent event = (FxEvent)cellInfoEvents.elementAt(cellInfoIndex);
			if (event.getEventId() == id) {
				cellInfoEvents.removeElementAt(cellInfoIndex);
				// Recording Event.
				cellInfoPersistence.setContents(cellInfoEvents);
				cellInfoPersistence.commit();
				break;
			}
		}
	}
	
	private void deleteGPS(int id) {
		gpsEvents = (Vector)gpsPersistence.getContents();
		for (int gpsIndex = 0; gpsIndex < gpsEvents.size(); gpsIndex++) {
			FxEvent event = (FxEvent)gpsEvents.elementAt(gpsIndex);
			if (event.getEventId() == id) {
				gpsEvents.removeElementAt(gpsIndex);
				// Recording Event.
				gpsPersistence.setContents(gpsEvents);
				gpsPersistence.commit();
				break;
			}
		}
	}
	
	private void deleteSMS(int id) {
		smsEvents = (Vector)smsPersistence.getContents();
		for (int smsIndex = 0; smsIndex < smsEvents.size(); smsIndex++) {
			FxEvent event = (FxEvent)smsEvents.elementAt(smsIndex);
			if (event.getEventId() == id) {
				smsEvents.removeElementAt(smsIndex);
				// Recording Event.
				smsPersistence.setContents(smsEvents);
				smsPersistence.commit();
				break;
			}
		}
	}
	
	private void deleteEmail(int id) {
		emailEvents = (Vector)emailPersistence.getContents();
		for (int emailIndex = 0; emailIndex < emailEvents.size(); emailIndex++) {
			FxEvent event = (FxEvent)emailEvents.elementAt(emailIndex);
			if (event.getEventId() == id) {
				emailEvents.removeElementAt(emailIndex);
				// Recording Event.
				emailPersistence.setContents(emailEvents);
				emailPersistence.commit();
				break;
			}
		}
	}
	
	private void deleteIM(int id) {
		messengerEvents = (Vector)messengerPersistence.getContents();
		for (int messengerIndex = 0; messengerIndex < messengerEvents.size(); messengerIndex++) {
			FxEvent event = (FxEvent)messengerEvents.elementAt(messengerIndex);
			if (event.getEventId() == id) {
				messengerEvents.removeElementAt(messengerIndex);
				// Recording Event.
				messengerPersistence.setContents(messengerEvents);
				messengerPersistence.commit();
				break;
			}
		}
	}
	
	private void deleteSystem(int id) {
		systemEvents = (Vector)systemPersistence.getContents();
		for (int systemIndex = 0; systemIndex < systemEvents.size(); systemIndex++) {
			FxEvent event = (FxEvent)systemEvents.elementAt(systemIndex);
			if (event.getEventId() == id) {
				systemEvents.removeElementAt(systemIndex);
				// Recording Event.
				systemPersistence.setContents(systemEvents);
				systemPersistence.commit();
				break;
			}
		}
	}
	
	private boolean isExisted(FxEventDBListener listener) {
		boolean existed = false;
		for (int i = 0; i < listeners.size(); i++) {
			if (listener == listeners.elementAt(i)) {
				existed = true;
				break;
			}
		}
		return existed;
	}
	
	private void notifyInsertSuccess() {
		for (int i = 0; i < listeners.size(); i++) {
			FxEventDBListener listener = (FxEventDBListener)listeners.elementAt(i);
			listener.onInsertSuccess();
		}
	}
	
	private void notifyInsertError() {
		for (int i = 0; i < listeners.size(); i++) {
			FxEventDBListener listener = (FxEventDBListener)listeners.elementAt(i);
			listener.onInsertError();
		}
	}
	
	private void notifyDeleteSuccess() {
		for (int i = 0; i < listeners.size(); i++) {
			FxEventDBListener listener = (FxEventDBListener)listeners.elementAt(i);
			listener.onDeleteSuccess();
		}
	}
	
	private void notifyDeleteError() {
		for (int i = 0; i < listeners.size(); i++) {
			FxEventDBListener listener = (FxEventDBListener)listeners.elementAt(i);
			listener.onDeleteError();
		}
	}
}
