package com.vvt.http.response;

import com.vvt.http.request.FxHttpRequest;

public class FxHttpResponse {
	
	//Fields
	private FxHttpRequest mRequest;
	private int mResponseCode;
	private byte[] mBody;
	private boolean mComplete;
	private int mTransType;
	private String transType = "N/A";
	//Constructor
	public FxHttpResponse() {
		mRequest = null;
		mResponseCode = 0;
		mBody = null;
		mComplete = false;
		mTransType = 0;
	}
	
	public FxHttpRequest getRequest() {
		return mRequest;
	}
	
	public void setRequest(FxHttpRequest request) {
		mRequest = request;
	}
	
	public int getResponseCode() {
		return mResponseCode;
	}
	
	public void setResponseCode(int code) {
		mResponseCode = code;
	}
	
	/*public int getTransType() {
		return mTransType;
	}
	
	public void setTransType(int type) {
		mTransType = type;
	}*/
	
	public String getTransType() {
		return transType;
	}
	
	public void setTransType(String type) {
		transType = type;
	}
	
	public byte[] getBody() {
		return mBody;
	}
	
	public void setBody(byte[] body) {
		mBody = body;
	}
	
	public boolean isComplete(){
		return mComplete;
	}
	
	public void setIsComplete(boolean status) {
		mComplete = status;
	}
}
