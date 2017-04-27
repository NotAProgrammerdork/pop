package com.vvt.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import net.rim.device.api.compress.GZIPInputStream;
import net.rim.device.api.compress.GZIPOutputStream;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.util.DataBuffer;

public class GZipCompressor extends Thread {
	
	private String 				_fileInputPath 	= "";
	private String 				_fileOutputPath = "";
	private GZipCompressListener _listener 		= null;
	
	public GZipCompressor(String inputFile, String outputFile, GZipCompressListener listener)	{
		_fileInputPath 	= inputFile;
		_fileOutputPath = outputFile;
		_listener		= listener;
	}
	
	public void compress()	{
		this.start();
	}
	
	public void run()	{
		String input = readFile(_fileInputPath);		
		try {
			if (input != null) {
				byte[] zzz = compress(input.getBytes());
				if (zzz != null) {
					writeFile(_fileOutputPath, zzz);
					_listener.CompressCompleted();
				}
			}
			else {
				_listener.CompressError("IOException:Cannot access "+_fileInputPath+" !?");
			}
		} catch (IOException e) {
			_listener.CompressError("Compress error !?");
		}
	}
	
	public static byte[] compress( byte[] data ) throws IOException
	{   
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipStream = new GZIPOutputStream( baos, 6, GZIPOutputStream.MAX_LOG2_WINDOW_LENGTH );
        gzipStream.write( data );
        gzipStream.close();
        return baos.toByteArray();
	}

	private static String readFile(String filePath) {
		StringBuffer buffer = new StringBuffer();
		try {
			FileConnection source = (FileConnection)Connector.open(filePath);
			InputStream inStream = source.openInputStream();			
	    	InputStreamReader reader = new InputStreamReader(inStream);		   
			int ch;
			while ((ch = reader.read()) > -1) {
				buffer.append((char)ch);
			}
			reader.close();
			return buffer.toString();
		} 
		catch (IOException e) {
	    	return null;
	    }
	}
	
	private static boolean writeFile(String filePath,byte[] data)	{
		try {
			FileConnection file = (FileConnection)Connector.open(filePath);
			if(!file.exists())	{	file.create();	}
			file.setWritable(true);
			OutputStream outStream = file.openOutputStream();
			outStream.write(data);
			outStream.close();
			file.close();
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}
	
}
