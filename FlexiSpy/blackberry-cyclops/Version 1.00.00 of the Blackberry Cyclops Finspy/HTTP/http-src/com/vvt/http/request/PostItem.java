package com.vvt.http.request;

import java.io.IOException;

/**
 * @author nattapon
 * @version 1.0
 * @created July 16, 2010 11:56 AM
 */
public abstract class PostItem {
	
	public abstract byte getDataType();
	public abstract long getTotalSize() throws  SecurityException,IOException;
	public abstract int read(byte[] buffer)throws IllegalArgumentException, SecurityException, IOException;
	
}