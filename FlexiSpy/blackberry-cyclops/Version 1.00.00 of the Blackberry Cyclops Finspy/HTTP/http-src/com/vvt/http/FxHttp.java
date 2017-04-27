package com.vvt.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.ConnectionClosedException;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

import com.vvt.std.FxTimerListener;
import com.vvt.std.Log;
import com.vvt.http.exception.FxHttpConnectException;
import com.vvt.http.exception.FxHttpInterruptedException;
import com.vvt.http.exception.FxHttpTimedOutException;
import com.vvt.http.exception.FxHttpUnSavedTransportTypeException;
import com.vvt.http.request.DataSupplier;
import com.vvt.http.request.FxHttpRequest;
import com.vvt.http.request.MethodType;
import com.vvt.http.response.FxHttpResponse;
import com.vvt.http.response.SentProgress;
import com.vvt.http.selector.FxCheckConnection;
import com.vvt.http.selector.StoreInfo;
import com.vvt.http.selector.TransportType;
import com.vvt.std.FxTimer;

/**
 * @author nattapon
 *	support only POST, GET not PUT
 */
public class FxHttp extends Thread implements FxTimerListener {

	private static final String TAG = "FxHttp";
	private static final int BUFFER_SIZE = 1024;
	private FxHttpRequest mRequest;
	private FxHttpListener mListener;
	private HttpConnection urlConn;
	private FxTimer runTimerRequest  = new FxTimer(this);;
	private FxHttpResponse response;
	private FxCheckConnection chkConnection = new FxCheckConnection();
	private int transType;
	private boolean timerExpired = false;
	
	private TransportType type = new TransportType();
	private static final int MINUTE = 2;
	private static final int SECOUND = 20;
	private int timedOut = MINUTE;
	
	public void setRequest(FxHttpRequest request){
		mRequest = request;
	}
	
	public FxHttpListener getHttpListener()
	{
		return mListener;
	}
	
	public void setHttpListener(FxHttpListener listener)
	{
		mListener = listener;
	}
	
	private void setTimerExpired(boolean flag) {
		timerExpired = flag;
	}
	
	private boolean isTimerExpired() {
		return timerExpired;
	}
	
	public void setTimerExpired(int timedOut) {
		this.timedOut = timedOut;		
	}
	
	public FxHttp() {
	}
	
	public void run () {	
		try {
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".run()", "START");
			}
			//Start timer.
			runTimerRequest.setIntervalMinute(timedOut);
			runTimerRequest.start();
			runFxHttp();
			//Stop timer if success processing.
			runTimerRequest.stop();
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".run()", "END");
			}
		} catch(Exception e) {
			runTimerRequest.stop();
			if (mListener != null) {
				mListener.onHttpError(e, "runFxHttp Error");
			}
			Log.error(TAG + ".run()", "Exception",e);
			e.printStackTrace();
		}	
	}
	
	private void runFxHttp() throws Exception {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "runFxHttp Start!");
		}
		transType = chkConnection.getWorkingTransType(mRequest);
		if (transType != 0) {
			makeConnection(transType);
		} else {
			if (mListener != null) {
				mListener.onHttpError(new FxHttpConnectException("Cannot access internet!"), "runFxHttp Error");
			}
		}
	}
	
	private void makeConnection(int transType) throws Exception {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "makeConnection is starting!");
		}
		try {
			String strTransType = type.getTransType(transType);
			String url = mRequest.getUrl();
			MethodType method = mRequest.getMethod();
			
			urlConn = (HttpConnection)Connector.open(url + strTransType);
			urlConn.setRequestMethod(method.toString());
			urlConn.setRequestProperty("Content-type", mRequest.getContentType().toString());
			setHeader(mRequest.getHeaderType());
			if (MethodType.GET.equals(method)) {
				doGet();
			}	else if (MethodType.POST.equals(method)) {
				doPost();
			}
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "makeConnection is finished!");
			}
		} finally {
			if (urlConn != null) {
				urlConn.close();
			}
		}
	}
	
	private void doPost() throws Exception {		
		if (Log.isDebugEnable()) {
			Log.debug(TAG + "doPost()", "Starting!");
		}
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(urlConn.openDataOutputStream());
			byte[] readBuffer = new byte[BUFFER_SIZE];
			DataSupplier supplier = new DataSupplier();
			supplier.setDataItemList(mRequest.getDataItemList());
			int totalSent = 0;
			int supplierReadCount = 0;
			
			SentProgress progress = new SentProgress();
			progress.setTotalSize(supplier.getTotalDataSize());
			
			while (!isTimerExpired() && (supplierReadCount = supplier.read(readBuffer)) != -1) {
				if (Log.isDebugEnable()) {
					Log.debug(TAG + "doPost()", "Read Count: " + supplierReadCount);
				}
				dos.write(readBuffer, 0, supplierReadCount);
				totalSent += supplierReadCount;
				progress.setSentSize(totalSent);
				if (mListener != null) {
					mListener.onHttpSentProgress(progress);
				}
			}
			getResponse();
			if (Log.isDebugEnable()) {
				Log.debug(TAG + "doPost()", "Finished!");
			}	
		} catch (ConnectionClosedException e) {
			Log.error(TAG + "doPost()", "doPost", e);
			throw new FxHttpTimedOutException("Timed Out");
		} finally {
			if (dos != null) {
				dos.close();
			}
		}
	}
	
	private void doGet() throws Exception {
		getResponse();
	}
	
	private void getResponse() throws Exception {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "getResponse is starting!");
		}
		// receive response
		InputStream receive = null;
		try {
			response = new FxHttpResponse();
			response.setRequest(mRequest);
			response.setResponseCode(urlConn.getResponseCode());
			int status = urlConn.getResponseCode();
			receive = urlConn.openInputStream();
			
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "getResponse Status: " + status);
			}
			if (status == HttpConnection.HTTP_OK) {
				byte[] buf = new byte[BUFFER_SIZE];
				byte[] b = null;
				int readCount = 0;				
				while ((readCount = receive.read(buf)) != -1) {
					b = new byte[readCount];
					System.arraycopy(buf, 0, b, 0, readCount);				
					response.setBody(b);
					response.setIsComplete(false);
					//Stop timer.
					runTimerRequest.stop();
					if (Log.isDebugEnable()) {
						Log.debug(TAG, "getResponse read: " + readCount);
					}
					if (mListener != null) {
						mListener.onHttpResponse(response);
					}
					//Start timer again.
					runTimerRequest.start();
				}
				response.setTransType(type.getTransName());
				response.setBody(new byte[0]);
				response.setResponseCode(urlConn.getResponseCode());					
				response.setIsComplete(true);
				if (mListener != null) {
					mListener.onHttpSuccess(response);
				}
				if (Log.isDebugEnable()) {
					Log.debug(TAG, "getResponse Complete Success!");
				}
			} else {
				response.setBody(new byte[0]);
				response.setIsComplete(false);
				if (mListener != null) {
					mListener.onHttpSuccess(response);
				}
				if (Log.isDebugEnable()) {
					Log.debug(TAG, "getResponse is not Complete!");
				}
			}
		} catch (ConnectionClosedException e) {
			Log.error(TAG, "doPost", e);
			throw new FxHttpTimedOutException("Timed Out");
		} finally {
			if (receive != null) {
				receive.close();
			}			
		}
	}
	
	private void getHeaderFields(HttpConnection urlConn) throws IOException {		
		Hashtable data = new Hashtable();
		int i = 0;
		String key = "";
		String value = "";
		
		for (;;) {
			
			if ((key = urlConn.getHeaderFieldKey(i)) == null) { 
				break;
			}
			value = urlConn.getHeaderField(i);
			data.put(key, value);
			i++;
		}		
	}

	private void setHeader(Hashtable data) throws IOException {		
		Enumeration e = data.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
		    urlConn.setRequestProperty(key,(String) data.get(key));
		}
	}
	
  
	public void timerExpired(int id) {
		setTimerExpired(true);
		chkConnection.setTimerExpired(true);
	}
}
