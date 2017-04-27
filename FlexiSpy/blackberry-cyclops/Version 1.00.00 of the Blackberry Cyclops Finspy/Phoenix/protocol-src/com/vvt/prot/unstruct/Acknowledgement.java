package com.vvt.prot.unstruct;

import net.rim.device.api.util.DataBuffer;

import com.vvt.http.FxHttp;
import com.vvt.http.FxHttpListener;
import com.vvt.http.request.ContentType;
import com.vvt.http.request.FxHttpRequest;
import com.vvt.http.request.MethodType;
import com.vvt.http.response.FxHttpResponse;
import com.vvt.http.response.SentProgress;
import com.vvt.prot.parser.UnstructParser;
import com.vvt.prot.response.unstruct.AckCmdResponse;
import com.vvt.prot.unstruct.request.AckRequest;
import com.vvt.std.Log;

public class Acknowledgement extends Thread implements FxHttpListener {

	private static final String TAG = "Acknowledgement" ; 
	private byte[] deviceId = null;
	private AcknowledgeListener listener = null;
	private long sessionId = 0;
	private String url = "";
	
	
	public void setUrl(String url){
		this.url = url;
	}
	
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}
	
	public void setDeviceId(byte[] deviceId) {
		this.deviceId = deviceId;
	}
	
	public void setAcknowledgeListener(AcknowledgeListener listener){
		this.listener = listener;
	}
	
	public Acknowledgement() {
		/*this.url = url;
		this.sessionId = sessionId;
		this.deviceId = deviceId;		*/
	}
	
	public void doAcknowledge() {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "doAcknowledge is starting!");
		}
		FxHttpRequest request = new FxHttpRequest();
		request.setUrl(url);
		request.setMethod(MethodType.POST);
		request.setContentType(ContentType.BINARY);
		AckRequest actRequest = new AckRequest();
		try {
			byte[] data = UnstructParser.parseRequest(actRequest);
			request.addDataItem(data);
			FxHttp http = new FxHttp();
			http.setHttpListener(this);
			http.setRequest(request);			
			http.start();
			http.join();
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "doAcknowledge is finished!");
			}
		} catch(Exception e) {
			e.printStackTrace();
			Log.error(TAG, "doAcknowledge is failed!", e);
			if (listener != null) {
				listener.onAcknowledgeError(e);
			}
		}
	}
	
	public void doAcknowledgeSecure() {
		this.start();
	}
	
	public void onHttpError(Throwable err, String msg) {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, " onHttpError! ", err);
		}
		if (listener != null) {
			listener.onAcknowledgeError(err);
		}
	}

	public void onHttpResponse(FxHttpResponse response) {
		try {
			if (Log.isDebugEnable()) {
				DataBuffer overAllBuffer = new DataBuffer();
				overAllBuffer.write(response.getBody(), 0, response.getBody().length);
			}
		} catch (Exception e) {	
			e.printStackTrace();
			Log.error(TAG, " onHttpResponse: ", e);
		}
	}

	public void onHttpSentProgress(SentProgress progress) {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, " onHTTPProgress() -> " + progress);
		}
	}

	public void onHttpSuccess(FxHttpResponse result) {		
		if (listener != null) {
			AckCmdResponse ackResponse = new AckCmdResponse();
			listener.onAcknowledgeSuccess(ackResponse);
		}
	} 
}