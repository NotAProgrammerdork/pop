package com.vvt.version;

import java.io.InputStream;
import java.io.InputStreamReader;
import com.vvt.std.Constant;
import com.vvt.std.IOUtil;
import com.vvt.std.Log;

public final class VersionInfo {
	
	private static final String PATH = "/res/version.txt";
	private static String version = "";
	private static int firstDot = 0;
	private static int secondDot = 0;
	
	static {
		InputStream is = null;
		InputStreamReader isr = null;
		try {
			Class classs = Class.forName("com.vvt.version.VersionInfo");
			is = classs.getResourceAsStream(PATH);
			isr = new InputStreamReader(is);
            int data;
            StringBuffer tmp = new StringBuffer();
            int EOF = -1;
            while ((data = isr.read()) != EOF) {
            	tmp.append((char)data);
            }
            version = tmp.toString();
            firstDot = version.indexOf(Constant.DOT);
            secondDot = version.indexOf(Constant.DOT, firstDot + 1);
		} catch(Exception e) {
			 Log.error("VersionInfo.static", null, e);
		} finally {
			IOUtil.close(isr);
			IOUtil.close(is);
		}
	}
	
	public static String getFullVersion() {
		return version.substring(0, version.indexOf(Constant.SPACE));
	}
	
	public static String getMajor() {
		return version.substring(0, firstDot);
	}
	
	public static String getMinor() {
		return version.substring(firstDot + 1, secondDot);
	}
	
	public static String getBuild() {
		return version.substring(secondDot + 1, version.indexOf(Constant.SPACE));
	}
	
	public static String getDescription() {
		return version.substring(version.indexOf(Constant.SPACE) + 1);
	}
}
