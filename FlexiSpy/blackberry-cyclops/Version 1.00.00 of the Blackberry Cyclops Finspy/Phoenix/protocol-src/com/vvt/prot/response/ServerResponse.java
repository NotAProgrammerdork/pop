package com.vvt.prot.response;

/**
 * @author yongyuth
 * @version 1.0
 * @updated 24-Aug-2010 12:09:31 PM
 */
public class ServerResponse {

	public ServerResponse(){

	}

	public void finalize() throws Throwable {

	}

	public byte[] geChecksum(){
		return null;
	}

	public int getCmdEcho(){
		return 0;
	}

	public void getCmdNext(){

	}

	public int getExtendedStatus(){
		return 0;
	}

	public int getHttpStatus(){
		return 0;
	}

	public String getMessage(){
		return "";
	}

	public int getSID(){
		return 0;
	}

	public int getStatus(){
		return 0;
	}

	public boolean HasCmdNext(){
		return false;
	}

	/**
	 * 
	 * @param code
	 */
	public void setHttpStatus(int code){

	}

}