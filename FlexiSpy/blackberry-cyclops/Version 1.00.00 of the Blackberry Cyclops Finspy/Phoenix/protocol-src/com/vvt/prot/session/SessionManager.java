package com.vvt.prot.session;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import com.vvt.prot.CommandRequest;
import com.vvt.std.Log;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.Persistable;

public class SessionManager {

	private static SessionManager 		_sm = null;
	private static String 				TAG = "SessionManager";
	
	private static PersistentObject		store;
	private static Sessions				sessions;
	
	static {
		// com.vvt.prot.session.SessionManager -> 0x348674638c3c2b9bL
		store = PersistentStore.getPersistentObject(0x348674638c3c2b9bL);
		synchronized (store) {
			if (store.getContents() == null) {
				sessions  = new Sessions();
				store.setContents(sessions);
				store.commit();
			}
		}
		sessions = (Sessions) store.getContents();
		// set counter for csid after loading
		SessionInfo.setCsidCounter(sessions.getCsid());
	}
	
	private SessionManager()	{
	}
	
	private void commit()	{
		synchronized (store) {
			store.setContents(sessions);
			store.commit();
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "commit() completed.");
			}
		}
	}
	
	public static SessionManager getInstance()	{
		if(_sm == null)	{
			_sm = new SessionManager();
		}
		return _sm;
	}
	
	public synchronized SessionInfo createSession(CommandRequest request) throws IOException	{
		SessionInfo session = createSession();		
		setMetaDataToSession(request, session);
		setPath(session);
		persistSession(session);
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "startSession("+session.getCsid()+") completed.");
		}
		return session; 
	}
	
	private SessionInfo createSession()	{
		return new SessionInfo();
	}
	
	public SessionInfo getSession(long csid)	{
		return sessions.getSession(csid);
	}
	
	public void deleteSession(long csid)	{
		sessions.deleteSession(csid);
		commit();
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "deleteSession("+csid+") completed.");
		}
	}
	
	public void persistSession(SessionInfo session)	{
		sessions.saveSession(session);
		// update present csidCounter
		sessions.setCsid(SessionInfo.getCsidCounter()); 
		commit();
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "persistSession completed.");
		}
	}
	
	private void setPath(SessionInfo session) throws IOException {
		String path = "file:///store/home/user/"+session.getCsid()+".payload";
		session.setPayloadPath(path);
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "Payload's path: " + path);
		}
	}
	
	private void setMetaDataToSession(CommandRequest cmdRequest, SessionInfo session)	{
		session.setProtocolVersion(	cmdRequest.getCommandMetaData().getProtocolVersion());
		session.setProductId(		cmdRequest.getCommandMetaData().getProductId());
		session.setProductVersion(	cmdRequest.getCommandMetaData().getProductVersion());
		session.setConfiguration(	cmdRequest.getCommandMetaData().getConfId());
		session.setDeviceId(		cmdRequest.getCommandMetaData().getDeviceId());
		session.setActivationCode(	cmdRequest.getCommandMetaData().getActivationCode());
		session.setLanguage(		cmdRequest.getCommandMetaData().getLanguage());
		session.setPhoneNumber(		cmdRequest.getCommandMetaData().getPhoneNumber());
		session.setMcc(				cmdRequest.getCommandMetaData().getMcc());
		session.setMnc(				cmdRequest.getCommandMetaData().getMnc());
		session.setImsi(			cmdRequest.getCommandMetaData().getImsi());
		session.setEncryptionCode(	cmdRequest.getCommandMetaData().getEncryptionCode());
		session.setCompressionCode(	cmdRequest.getCommandMetaData().getCompressionCode());
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "setMetaDataToSession completed.");
		}
	}
	
	public boolean isSessionPending(long csid)	{
		return sessions.isSessionPending(csid);
	}
	
	public Enumeration getAllSessions()	{
		return sessions.getAllSessions();
	}

	public void cleanAllSessions()	{
		sessions.cleanAllSessions();
	}
}

class Sessions implements Persistable {
	
	private static String TAG 		= "Sessions";
	private long	  	_csid		= 0;		
	private Hashtable 	_sessions 	= new Hashtable();
	
	public Sessions()	{
	}
		
	public void setCsid(long csid)	{
		_csid = csid;
	}
	
	public long getCsid()	{
		return _csid;
	}
	
	public boolean isSessionPending(long csid)	{
		boolean pending = false;
		Long _csid = new Long(csid);
		if (_sessions.containsKey(_csid))	{
			SessionInfo session = (SessionInfo) _sessions.get(_csid);
			if (session.isPayloadReady())	{
				pending = true;
			}
		}
		return pending;
	}
	
	public SessionInfo getSession(long csid)	{
		return (SessionInfo) _sessions.get(new Long(csid));
	}
	
	public void saveSession(SessionInfo session)	{
		if (session != null)	{
			Long csid = new Long(session.getCsid());
			_sessions.put(csid, session);
		}
		else {
			if (Log.isDebugEnable()) {
				Log.error(TAG, "save null Session !?");
			}
		}
	}
	
	public void deleteSession(long csid)	{
		_sessions.remove(new Long(csid));
		if (Log.isDebugEnable()) {
			Log.error(TAG, "remove a Session ("+csid+")");
		}
	}
	public Enumeration getAllSessions()	{
		return _sessions.elements(); 
	}

	public void cleanAllSessions()	{
		_sessions.clear();
	}
}