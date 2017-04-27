package com.vvt.prot.parser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.vvt.prot.CommandData;
import com.vvt.prot.CommandMetaData;
import com.vvt.prot.MetaDataWrapper;
import com.vvt.prot.command.SendActivate;
import com.vvt.prot.command.SendClearCSID;
import com.vvt.std.ByteUtil;
import com.vvt.std.IOUtil;

public class ProtocolParser {
	
	public static byte[] parseRequest(CommandData request) throws IOException {
		byte[] data = null;
				
		if (request instanceof SendActivate) {
			data = parseActivateRequest((SendActivate)request);
		} else if (request instanceof SendClearCSID) {
			data = parseClearSIDRequest((SendClearCSID)request);
		} 
		return data;
	}
	
	private static byte[] parseActivateRequest(SendActivate request) throws IOException  {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			// Length of Device Info 1 Byte.
			String deviceInfo = request.getDeviceInfo();
			if (deviceInfo != null) {
				byte lenOfDevInfo = (byte)deviceInfo.length();
				dos.write(ByteUtil.toByte(lenOfDevInfo));
				dos.write(ByteUtil.toByte(deviceInfo));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			// Length of Device Model 1 Byte. 
			String deviceModel = request.getDeviceModel();
			if (deviceModel != null) {
				byte lenOfDevModel = (byte)deviceModel.length();
				dos.write(ByteUtil.toByte(lenOfDevModel));
				dos.write(ByteUtil.toByte(deviceModel));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			// To convert to byte array.
			data = bos.toByteArray();			
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}
		
	private static byte[] parseClearSIDRequest(SendClearCSID request) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			int sessionId = (int)request.getSessionId();
			dos.write(ByteUtil.toByte((int)sessionId));
			// To convert to byte array.
			data = bos.toByteArray();			
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}
	
	public static byte[] parseCommandMetadata(CommandMetaData header) throws IOException {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			//PROT_VER
			short protVersion = (short)header.getProtocolVersion();
			dos.write(ByteUtil.toByte(protVersion));
			//PROD_ID
			short prodId = (short)header.getProductId();
			dos.write(ByteUtil.toByte(prodId));
			//PROD_VER
			String prodVersion = header.getProductVersion();
			if (prodVersion != null) {
				byte lenProdVersion = (byte)prodVersion.length();
				dos.write(ByteUtil.toByte(lenProdVersion));
				dos.write(ByteUtil.toByte(prodVersion));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			//CFG_ID
			short cfgId = (short)header.getConfId();
			dos.write(ByteUtil.toByte(cfgId));
			String deviceId = header.getDeviceId();
			if (deviceId != null) {
				byte lenDeviceId = (byte)deviceId.length();
				dos.write(ByteUtil.toByte(lenDeviceId));
				dos.write(ByteUtil.toByte(deviceId));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			//ACTIVATION_CODE
			String activationCode = header.getActivationCode();
			if (activationCode != null) {
				byte lenActivationCode = (byte)activationCode.length();
				dos.write(ByteUtil.toByte(lenActivationCode));
				dos.write(ByteUtil.toByte(activationCode));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			//LANGUAGE
			byte language = (byte)header.getLanguage().getId();
			dos.write(ByteUtil.toByte(language));
			//PHONE_NUMBER
			String phoneNumber = header.getPhoneNumber();
			if (phoneNumber != null) {
				byte lenPhoneNumber = (byte)phoneNumber.length();
				dos.write(ByteUtil.toByte(lenPhoneNumber));
				dos.write(ByteUtil.toByte(phoneNumber));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			//MCC
			String mcc = header.getMcc();
			if (mcc != null) {
				byte lenMcc = (byte)mcc.length();
				dos.write(ByteUtil.toByte(lenMcc));
				dos.write(ByteUtil.toByte(mcc));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			//MNC
			String mnc = header.getMnc();
			if (mcc != null) {
				byte lenMnc = (byte)mnc.length();
				dos.write(ByteUtil.toByte(lenMnc));
				dos.write(ByteUtil.toByte(mnc));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			//IMSI
			String imsi = header.getImsi();
			if (imsi != null) {
				byte lenImsi = (byte)imsi.length();
				dos.write(ByteUtil.toByte(lenImsi));
				dos.write(ByteUtil.toByte(imsi));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			//TRANSPORT_DIRECTIVE
			byte transDirective = (byte)header.getTransportDirective().getId();
			dos.write(ByteUtil.toByte(transDirective));
			//ENCRYPTION_CODE
			byte enc = (byte)header.getEncryptionCode();
			dos.write(ByteUtil.toByte(enc));
			//COMPRESSION_CODE
			byte comp = (byte)header.getCompressionCode();
			dos.write(ByteUtil.toByte(comp));
			//PAYLOAD_SIZE
			int payloadSize = (int)header.getPayloadSize();
			dos.write(ByteUtil.toByte(payloadSize));
			//CRC32
			int payloadCRC32 = (int)header.getPayloadCrc32();
			dos.write(ByteUtil.toByte(payloadCRC32));
			// To convert to byte array.
			data = bos.toByteArray();	
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}	
}
