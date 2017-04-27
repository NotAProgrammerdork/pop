package com.vvt.http.selector;

import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RadioInfo;

import com.vvt.http.request.FxHttpRequest;
import com.vvt.http.request.MethodType;
import com.vvt.std.Log;

public class FxCheckConnection {
	
	private static final String TAG = "FxCheckConnection";
	private FxHttpRequest mRequest = null;
	private int transType = 0;
	private HttpConnection urlConn = null;	
	private static final long PERSISTENT_INTERNET_SETTING_ID = 0x40d954358bab5ceeL;
	private PersistentObject internetPersistent = null;
	private StoreInfo info = null;
	private boolean timerExpired = false;
	private static final byte[] UPING_CMD = {0,103,0,1};
	private TransportType type = new TransportType();
	
	public void setTimerExpired(boolean flag) {
		timerExpired = flag;
	}
	
	private boolean isTimerExpired() {
		return timerExpired;
	}
	
	public FxCheckConnection() {
		try {
			internetPersistent = PersistentStore.getPersistentObject(PERSISTENT_INTERNET_SETTING_ID);
			info = (StoreInfo)internetPersistent.getContents();
			if (info == null) {
				info = new StoreInfo();
				internetPersistent.setContents(info);
				internetPersistent.commit();
			}
		} catch (Exception e) {
			Log.error(TAG, "PersistentObject is failed!", e);
			e.printStackTrace();
		} 
	}
	
	public int getWorkingTransType(FxHttpRequest request) throws Exception {
		mRequest = request;
		if (!checkConnection()) {
			transType = 0;
		}
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "getWorkingTransType: " + transType);
		}
		return transType;
	}
		
	private boolean checkConnection() throws Exception {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "checkConnection is staring!");
		}
		String url = mRequest.getUrl();		
		String urlTransType = null;		
		boolean isConnectionSuccess = false;
		int curTransType = 0;
		//Check WIFI connection first
		transType = TransportType.WIFI;
		
		while(!isTimerExpired() && ((urlTransType = type.getTransType(transType)) != null)) {
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "Transport Type: " + urlTransType);
			}
			if (transType == TransportType.WIFI) {
				if (openConnection(url + urlTransType)) {
					isConnectionSuccess = true;
					break;
				} else if ((curTransType = getSavedWorkingAPN()) != 0) {
					if (Log.isDebugEnable()) {
						Log.debug(TAG, "Current Type: " + curTransType);
					}
					if ((urlTransType = type.getTransType(curTransType)) != null) {
						if (openConnection(url + urlTransType)) {
							isConnectionSuccess = true;
							transType = curTransType;
							break;
						}
					}
				}
			} else {
				if (openConnection(url + urlTransType)) {
					if (savedWorkingAPN(transType)) {
						isConnectionSuccess = true;
						break;
					}
					else {
						break;
					}
				}
			}			
			++transType;
		}
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "checkConnection is finished!");
		}
		return isConnectionSuccess;
	}
	
	private boolean openConnection(String url) throws Exception {		
		long startTime = 0;
		long endTime = 0;
		boolean connSuccess = false;	
		DataOutputStream dos = null;
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "chkConnection is starting!");
			startTime = System.currentTimeMillis();
		}
		try {		
			if (!isTimerExpired()) {			
				urlConn = (HttpConnection)Connector.open(url);
				urlConn.setRequestMethod(MethodType.POST.toString());
				urlConn.setRequestProperty("Content-type", mRequest.getContentType().toString());				
				//Post data
				dos = new DataOutputStream(urlConn.openDataOutputStream());
				dos.write(UPING_CMD);				
				dos.flush();
				//Get response code
				int status = urlConn.getResponseCode();			
				if (Log.isDebugEnable()) {				
					endTime = System.currentTimeMillis();
					long time = endTime - startTime;
					Log.debug(TAG, "getResponse Status: " + status + " Connected Time: " + time);
				}
				if (status == HttpConnection.HTTP_OK) {
					connSuccess = true;					
				}	
			}
		} catch (ControlledAccessException e) {
			Log.error(TAG, "chkConnection Error", e);
			e.printStackTrace();			
		} catch (IllegalArgumentException e) {
			Log.error(TAG, "chkConnection Error", e);
			e.printStackTrace();			
	 	} catch (IOException e) {
	 		Log.error(TAG, "chkConnection Error", e);
	 		e.printStackTrace();	 
	 	} finally {
			if (urlConn != null) {
				urlConn.close();
			}
			if (dos != null) {
				dos.close();
			}
		}	 	
		return connSuccess;
    }
	
	private int getSavedWorkingAPN() {	
		internetPersistent = PersistentStore.getPersistentObject(PERSISTENT_INTERNET_SETTING_ID);
		info = (StoreInfo)internetPersistent.getContents();
		return info.getInternetSetting();		
	}
	
	private boolean savedWorkingAPN(int type) {
		boolean savedSuccess = false;
		try {
			info.setInternetSetting(type);
			internetPersistent.setContents(info);
			internetPersistent.commit();
			savedSuccess = true;
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "Transport type was saved!: "+type);
			}
		} catch (Exception e) {
			Log.error(TAG, "Save Transport Type wad failed: ", e);
			e.printStackTrace();
		}
		return savedSuccess;
	}	
}
