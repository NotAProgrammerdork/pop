package com.vvt.bbm;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;

public class Log {

	private static boolean 	enable 	= false;
	private static String 	path 	= "file:///store/home/user/documents/bbmdebugging.txt";
	private static OutputStream out = null;
	private static SimpleDateFormat formatter = new SimpleDateFormat("HH-mm-ss:");
	private static ApplicationManager manager = ApplicationManager.getApplicationManager();

	public static void setEnable(boolean flag)	{
		enable = flag;
	}

	public static boolean isEnable()	{
		return enable;
	}

	public static void setBBMLoggerPath(String newPath)	{
		if (newPath.length()>0)	{
			path = newPath;
		}
	}

	public static String getBBMLoggerPath()	{
		return path;
	}

	public static String activeApp()	{
        ApplicationDescriptor descriptors[] = manager.getVisibleApplications();
        ApplicationDescriptor d = descriptors[0];
        String name = d.getName();
        name = name.substring(name.lastIndexOf('.')+1);
        return name;
	}

	private static String getNow()	{
		Date today 	= new Date(System.currentTimeMillis());
		return formatter.format(today);
	}

	public static void debug(String data) {
		try {
			String detail = getNow()+" "+activeApp()+":"+data+"\r\n";	
			FileConnection fileOutput = (FileConnection)Connector.open(path, Connector.READ_WRITE);
			if (!fileOutput.exists()) {
				fileOutput.create();
			}
			long endPosition = fileOutput.totalSize();
			out = fileOutput.openOutputStream(endPosition);
			out.write(detail.getBytes());
			out.close();
		} catch (IOException e) {
			//
		}
	}
}
