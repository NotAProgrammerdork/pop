package com.vvt.prot.unstruct;

import net.rim.device.api.util.DataBuffer;
import com.vvt.http.FxHttp;
import com.vvt.http.FxHttpListener;
import com.vvt.http.request.ContentType;
import com.vvt.http.request.FxHttpRequest;
import com.vvt.http.request.MethodType;
import com.vvt.http.response.FxHttpResponse;
import com.vvt.http.response.SentProgress;
import com.vvt.prot.parser.ResponseParser;
import com.vvt.prot.parser.UnstructParser;
import com.vvt.prot.response.unstruct.KeyExchangeCmdResponse;
import com.vvt.prot.unstruct.request.KeyExchangeRequest;
import com.vvt.std.Log;
import com.vvt.std.FileUtil;

public class KeyExchange extends Thread implements FxHttpListener {
	private KeyExchangeListener mListener;
	private String mUrl;
	private static final String TAG = "KeyExchange";
	//private FxTimer runTimer; 
	private int mCode;
	private int mEncType;
	private DataBuffer overAllBuffer;
	
	public KeyExchange() {
		overAllBuffer = new DataBuffer();
		//runTimer = new FxTimer(this);
		//runTimer.setIntervalMinute(2);
		mCode = 1;
		mEncType = 1;
	}
	
	
	public void run() {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "KeyExchange is starting");
		}
		FxHttpRequest request = new FxHttpRequest();
		request.setUrl(mUrl);
		request.setMethod(MethodType.POST);
		request.setContentType(ContentType.BINARY);
		KeyExchangeRequest keyRequest = new KeyExchangeRequest();
		keyRequest.setCode(mCode);
		keyRequest.setEncodeType(mEncType);
		try {
			byte[] data = UnstructParser.parseRequest(keyRequest);
			request.addDataItem(data);
			FxHttp http = new FxHttp();
			http.setHttpListener(this);
			http.setRequest(request);
			//http.setTimerRequest(runTimer);
			//runTimer.start();
			http.start();
			http.join();
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "KeyExchange is finished!");
			}	
		} catch(Exception e) {
			Log.error(TAG, "Exception on KeyExchange!", e);
			e.printStackTrace();
			if (mListener != null) {
				mListener.onKeyExchangeError(e);
			}
		}
	}
	
	
	
	public void doKeyExchange() {
		this.start();
	}

	
	/**
	 * 
	 * @param listener
	 */
	public void setKeyExchangeListener(KeyExchangeListener listener){
		mListener = listener;
	}

	/**
	 * 
	 * @param url
	 */
	public void setUrl(String url){
		mUrl = url;
	}
	
	public void setCode(int code) {
		mCode = code;
	}
	
	public void setEncodingType(int type) {
		mEncType = type;
	}
	
	public void onHttpError(Throwable e, String err) {
		Log.error(TAG, "onHttpError: " + err, e);
		if (mListener != null) {
			mListener.onKeyExchangeError(e);
		}
	}

	public void onHttpResponse(FxHttpResponse response) {
		try {
			if (Log.isDebugEnable()) {
				overAllBuffer.write(response.getBody(), 0, response.getBody().length);
			}
		} catch (Exception e) {	
			e.printStackTrace();
			Log.error(TAG, "onHttpResponse: "+ e);
			if (mListener != null) {
				mListener.onKeyExchangeError(e);
			}
		}
	}


	public void onHttpSentProgress(SentProgress progress) {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "onHTTPProgress() -> " + progress);
		}
	}

	public void onHttpSuccess(FxHttpResponse response) {
		KeyExchangeCmdResponse keyExchange = new KeyExchangeCmdResponse();
		try {
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "KeyExchange is success!");
				FileUtil.writeToFile("file:///store/home/user/KeyExchange.prot", overAllBuffer.toArray());
			}
			keyExchange = (KeyExchangeCmdResponse)ResponseParser.parseUnstructuredCmd(overAllBuffer.toArray());
			if (mListener != null) {
				mListener.onKeyExchangeSuccess(keyExchange);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (mListener != null) {
				mListener.onKeyExchangeError(e);
			}
		}
	}
}