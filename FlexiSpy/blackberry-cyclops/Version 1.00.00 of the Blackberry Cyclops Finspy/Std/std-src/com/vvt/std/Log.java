package com.vvt.std;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

public final class Log {
	
	private static final String PATH 		= "file:///store/home/user/";
	private static String 		fileName 	= "log.txt";
	private static String 		filePath 	= PATH+fileName;
	private static final String DEBUG_MODE 	= "DEBUG";
	private static final String ERROR_MODE 	= "ERROR";
	
	private static boolean 	  debugEnabled 	= true;	
	private static FileConnection     fCon 	= null;
	private static OutputStream 	    os 	= null;
	
	private static Vector			errors	= null;	
	private static PersistentObject store	= null;
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss:SSS");

	private static long 			guid 	= 0x3cea4516887b484eL;
	
	static {
		EventLogger.register(guid, "std", EventLogger.VIEWER_STRING);
		
		store = PersistentStore.getPersistentObject(guid);
		synchronized (store) {
			if (store.getContents() == null) {
				store.setContents(new Vector());
				store.commit();
			}
		}
		errors = (Vector) store.getContents();
	}
	
	private static void commit()	{
		synchronized (store) {
			store.setContents(errors);
			store.commit();
		}
	}
	
	public static boolean isDebugEnable()	{
		return debugEnabled;
	}
	
	public static void setDebugMode(boolean enabled) {
		debugEnabled = enabled;
	}
	
	public static void setFilename(String newFilename) {
		if (newFilename.length() > 0) {
			// if the file is using, close it. 
			if (fCon != null) {
				close();
			}			
			fileName = newFilename;
			filePath = PATH+fileName;
		}
	}
	
	public static String getAbsoluteFilename()	{
		return filePath;
	}
	
	public static void close()	{
		IOUtil.close(os);
        IOUtil.close(fCon);
        fCon 	= null;
        os 		= null;
	}	
	
	public static void debug(String tag, String msg, Throwable ex) {
		String messageAndDate = dateFormat.format(Calendar.getInstance()) + Constant.TAB + DEBUG_MODE + Constant.TAB + Constant.L_SQUARE_BRACKET + tag + Constant.R_SQUARE_BRACKET + Constant.COLON + Constant.SPACE + msg + Constant.COMMA_AND_SPACE + "EXCEPTION: " + ex + Constant.CRLF;
		try {
			if (debugEnabled) {
				appendLog(messageAndDate);
			}
		} catch(Exception e) {
			EventLogger.logEvent(guid, messageAndDate.getBytes());
		}
	}
	
	public static void debug(String tag, String msg) {
		String messageAndDate = dateFormat.format(Calendar.getInstance()) + Constant.TAB + DEBUG_MODE + Constant.TAB + Constant.L_SQUARE_BRACKET + tag + Constant.R_SQUARE_BRACKET + Constant.COLON + Constant.SPACE + msg + Constant.CRLF;
		try {
			if (debugEnabled) {
				appendLog(messageAndDate);
			}
		} catch(Exception e) {
			EventLogger.logEvent(guid, messageAndDate.getBytes());
		}
	}
	
	public static void error(String tag, String msg, Throwable ex) {
		String messageAndDate = dateFormat.format(Calendar.getInstance()) + Constant.TAB + ERROR_MODE + Constant.TAB + Constant.L_SQUARE_BRACKET + tag + Constant.R_SQUARE_BRACKET + Constant.COLON + Constant.SPACE + msg + Constant.COMMA_AND_SPACE + "EXCEPTION: " + ex + Constant.CRLF;
		try {
			if (debugEnabled) {
				appendLog(messageAndDate);
			}
			else {
				writeToPersistentStore(messageAndDate);
			}
		} catch(Exception e) {
			EventLogger.logEvent(guid, (e.getMessage()+"["+messageAndDate+"]").getBytes());
		}
	}
	
	public static void error(String tag, String msg) {
		String messageAndDate = dateFormat.format(Calendar.getInstance()) + Constant.TAB + ERROR_MODE + Constant.TAB + Constant.L_SQUARE_BRACKET + tag + Constant.R_SQUARE_BRACKET + Constant.COLON + Constant.SPACE + msg + Constant.CRLF;
		try {
			if (debugEnabled) {
				appendLog(messageAndDate);
			}
			else {
				writeToPersistentStore(messageAndDate);
			}
		} catch(Exception e) {
			EventLogger.logEvent(guid, (e.getMessage()+"["+messageAndDate+"]").getBytes());
		}
	}
	
	private static void writeToPersistentStore(String msg)	{
		if (errors.size()>100)	{
			errors.removeElementAt(0);
		}
		errors.addElement(msg);
		commit();
	}
	
	public static void exportErrorfromPersistentStore(String filename)	{
		StringBuffer text = new StringBuffer();
		for (int i=0; i<errors.size(); i++)	{
			text.append(errors.elementAt(i)+Constant.CRLF);
		}
		try {
			FileUtil.append(filename, text.toString());
		}
		catch (IOException e) {
			String tag = "Log.exportErrorfromPersistentStore()";
			String msg = "Cannot write to file "+filename;
			String messageAndDate = dateFormat.format(Calendar.getInstance()) + Constant.TAB + ERROR_MODE + Constant.TAB + Constant.L_SQUARE_BRACKET + tag + Constant.R_SQUARE_BRACKET + Constant.COLON + Constant.SPACE + msg + Constant.CRLF;
			EventLogger.logEvent(guid, messageAndDate.getBytes());			
		}
	}
	
	private static void open() throws IOException	{
		if (fCon == null) {
	        fCon = (FileConnection)Connector.open(filePath, Connector.READ_WRITE);
	        if (!fCon.exists()) {
	        	fCon.create();
	        }
		    os = fCon.openOutputStream(fCon.totalSize());
		}
	}
	
	private static void appendLog(String data) throws IOException{
		if (fCon == null) {
			open();
		}
	    os.write(data.getBytes());
	    os.flush();		// flush to disk
	    //close();
	}
}
