package com.vvt.http.request;

/**
 * @author nattapon
 * @version 0.0.1
 * @created Aug 5, 2010
 */
public class MethodType {

	public static final MethodType GET = new MethodType("GET");
	public static final MethodType POST = new MethodType("POST");
	private String method = "";
	
	private MethodType(String method) {
		this.method = method;
	}
	
	public String toString() {
		return method;
	}
	
}