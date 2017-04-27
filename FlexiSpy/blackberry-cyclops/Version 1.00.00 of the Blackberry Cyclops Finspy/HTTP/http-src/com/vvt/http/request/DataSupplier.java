package com.vvt.http.request;

import java.io.IOException;
import java.util.Vector;

/**
 * @author nattapon
 * @version 1.0
 * @created 20-July-2010 8:11 PM
 */
public class DataSupplier {
	
	//Fields
	private int mCurReadingIndex;
	private Vector mDataItemList; 
	
	/**
	 * Constructor
	 */
	public DataSupplier() {
		mDataItemList = new Vector();
		mCurReadingIndex = 0;
	}
	
	public void setDataItemList(Vector dataItemList) {
		mDataItemList = dataItemList;
	}
	
	public int getDataItemCount() {
		return mDataItemList.size();
	}
	
	public long getTotalDataSize() throws SecurityException, IOException {
		
		long totalSize = 0;
		
		for (int i=0; i<mDataItemList.size(); i++) {
			PostItem postItem = (PostItem) mDataItemList.elementAt(i);
			totalSize += postItem.getTotalSize();
		}
		return totalSize;
	}
	
	public int read(byte[] buffer) throws IndexOutOfBoundsException, SecurityException, IOException {
		
		PostItem item = (PostItem) mDataItemList.elementAt(mCurReadingIndex);
		int readCount = 0;
	
		readCount = item.read(buffer);
			
		if (readCount == -1) {
			if (mCurReadingIndex == (mDataItemList.size() - 1)) {	//we have reached last element
				return -1;	//all data in supplier have been read
			}
			else {
				mCurReadingIndex++;  // goto next element and continue read it
				return read(buffer); //recursive
			}
		}
		return readCount;
	}
}