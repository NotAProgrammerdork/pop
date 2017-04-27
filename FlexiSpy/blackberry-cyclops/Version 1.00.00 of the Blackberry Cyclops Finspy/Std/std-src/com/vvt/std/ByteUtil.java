package com.vvt.std;

import net.rim.device.api.util.DataBuffer;

public final class ByteUtil {
		
	public static byte[] toByte(byte data) {
		DataBuffer dataBuffer = new DataBuffer();
		dataBuffer.writeByte(data);
		return dataBuffer.toArray();
	}
	
	public static byte[] toByte(short data) {
		DataBuffer dataBuffer = new DataBuffer();
		dataBuffer.writeShort(data);
		return dataBuffer.toArray();
	}
	
	public static byte[] toByte(int data) {
		DataBuffer dataBuffer = new DataBuffer();
		dataBuffer.writeInt(data);
		return dataBuffer.toArray();
	}
	
	public static byte[] toByte(long data) {
		DataBuffer dataBuffer = new DataBuffer();
		dataBuffer.writeLong(data);
		return dataBuffer.toArray();
	}
	
	public static byte[] toByte(float data) {
		DataBuffer dataBuffer = new DataBuffer();
		dataBuffer.writeFloat(data);
		return dataBuffer.toArray();
	}
	
	public static byte[] toByte(double data) {
		DataBuffer dataBuffer = new DataBuffer();
		dataBuffer.writeDouble(data);
		return dataBuffer.toArray();
	}
	
	public static byte[] toByte(String data) {
		DataBuffer dataBuffer = new DataBuffer();
		dataBuffer.write(data.getBytes());
		return dataBuffer.toArray();
	}
}
