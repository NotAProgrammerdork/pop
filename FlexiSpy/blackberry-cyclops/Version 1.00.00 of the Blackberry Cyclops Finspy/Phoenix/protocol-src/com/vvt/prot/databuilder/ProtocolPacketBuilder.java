package com.vvt.prot.databuilder;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import com.vvt.checksum.CRC32;
import com.vvt.checksum.CRC32Listener;
import com.vvt.encryption.AESEncryptor;
import com.vvt.encryption.DataTooLongForRSAEncryptionException;
import com.vvt.encryption.RSAEncryption;
import com.vvt.prot.CommandCode;
import com.vvt.prot.CommandData;
import com.vvt.prot.CommandMetaData;
import com.vvt.prot.MetaDataWrapper;
import com.vvt.prot.command.TransportDirectives;
import com.vvt.prot.databuilder.PayloadBuilderResponse;
import com.vvt.prot.databuilder.exception.CRC32Exception;
import com.vvt.prot.parser.ProtocolParser;
import com.vvt.std.ByteUtil;
import com.vvt.std.FileUtil;
import com.vvt.std.IOUtil;
import com.vvt.std.Log;

public class ProtocolPacketBuilder implements CRC32Listener {

	private static final String TAG = "ProtocolPacketBuilder";
	private String errMsg = "CRC32 Error";
	private boolean isCRC32Completed = false;
	private CommandMetaData cmdMetaData = null;
	
	private static final String METADATA_FILE =  "file:///store/home/user/MetaDataPlainText.dat";
	
	
	public ProtocolPacketBuilderResponse buildCmdPacketData(CommandMetaData cmdMetaData, CommandData cmdData, String payloadPath, byte[] publicKey, long ssid, TransportDirectives transport) throws IOException, InterruptedException, CRC32Exception, NullPointerException, DataTooLongForRSAEncryptionException, IllegalArgumentException {
		
		if (Log.isDebugEnable()) {
			Log.debug(TAG + ".buildCmdPacketData()", "START");
		}
		this.cmdMetaData = cmdMetaData;
		ProtocolPacketBuilderResponse protPacketBuilderResponse = null;
		CommandCode cmdCode = cmdData.getCommand();
		PayloadBuilderResponse payloadBuilderResponse = PayloadBuilder.getInstance(cmdCode).buildPayload(cmdMetaData, cmdData, payloadPath, transport);
		cmdMetaData.setTransportDirective(transport);
		if (payloadBuilderResponse.getPayloadType().equals(PayloadType.FILE)) {
			//Calculate CRC32 of Payload
			CRC32 crc32 = new CRC32(payloadPath, this);
			crc32.calculate();
			crc32.join();
			if (isCRC32Completed) {
				isCRC32Completed = false;
				
				long payloadSize = getPayloadSize(payloadPath);
				cmdMetaData.setPayloadSize(payloadSize);
				//Parse MetaData
				byte[] metaData = ProtocolParser.parseCommandMetadata(cmdMetaData);
				
				if (Log.isDebugEnable()) {
					Log.debug(TAG + ".buildCmdPacketData()", "*** Before Encrypt Header size: " + metaData.length + "***");
				}
				
				//Encrypt MetaData
				byte[] encData = encryptMetaData(payloadBuilderResponse.getAesKey(), metaData);
				
				if (Log.isDebugEnable()) {
					Log.debug(TAG + ".buildCmdPacketData()", "*** After Encrypted Header size: " + encData.length + "***");
				}				
				
				//Set MetaData_Header
				metaData = setMetaDataHeader(encData, publicKey, payloadBuilderResponse.getAesKey(), ssid);
				protPacketBuilderResponse = setResponse(payloadBuilderResponse.getAesKey(), metaData, payloadBuilderResponse.getByteData(), payloadBuilderResponse.getFilePath(), payloadBuilderResponse.getPayloadType());
				protPacketBuilderResponse.setPayloadSize(payloadSize);
				protPacketBuilderResponse.setPayloadCRC32(cmdMetaData.getPayloadCrc32());
				if (Log.isDebugEnable()) {
					Log.debug(TAG + ".buildCmdPacketData()", "END");
				}
			} else {
				Log.error(TAG + ".buildCmdPacketData()", errMsg);
				throw new CRC32Exception(errMsg);
			}
		} else {
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".buildCmdPacketData()", "payloadBuilderResponse.getByteData()" + payloadBuilderResponse.getByteData());
				Log.debug(TAG + ".buildCmdPacketData()", "payloadBuilderResponse.getAesKey()" + payloadBuilderResponse.getAesKey());
			}
			
			byte[] payloadData = payloadBuilderResponse.getByteData();
			long crc32 = CRC32.calculate(payloadData);
			
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".buildCmdPacketData()", "CRC32.calculate(payloadData)" + crc32);
				
			}
			
			cmdMetaData.setPayloadCrc32(crc32);
			long payloadSize = payloadData.length;
			cmdMetaData.setPayloadSize(payloadSize);
			//Parse MetaData
			byte[] metaData = ProtocolParser.parseCommandMetadata(cmdMetaData);
			
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".buildCmdPacketData()", "ProtocolParser.parseCommandMetadata(cmdMetaData)" + metaData);
				
			}
			
			//Encrypt MetaData
			byte[] encData = encryptMetaData(payloadBuilderResponse.getAesKey(), metaData);
			
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".buildCmdPacketData()", "encryptMetaData(payloadBuilderResponse.getAesKey(), metaData)" + encData);
				Log.debug(TAG + ".buildCmdPacketData()", "payloadBuilderResponse.getFilePath()" + payloadBuilderResponse.getFilePath());
				Log.debug(TAG + ".buildCmdPacketData()", "payloadBuilderResponse.getPayloadType()" + payloadBuilderResponse.getPayloadType());
				 
			}
			
			//Set MetaData_Header
			metaData = setMetaDataHeader(encData, publicKey, payloadBuilderResponse.getAesKey(), ssid);
			protPacketBuilderResponse = setResponse(payloadBuilderResponse.getAesKey(), metaData, payloadBuilderResponse.getByteData(), payloadBuilderResponse.getFilePath(), payloadBuilderResponse.getPayloadType());
			protPacketBuilderResponse.setPayloadSize(payloadSize);
			protPacketBuilderResponse.setPayloadCRC32(crc32);
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".buildCmdPacketData()", "END");
			}
			
		}
		return protPacketBuilderResponse;
	}
	
	public ProtocolPacketBuilderResponse buildResumeCmdPacketData(CommandMetaData cmdMetaData, String payloadPath, byte[] publicKey, byte[] aesKey, long ssid, TransportDirectives transport, int numbOfBytes) throws IOException, InterruptedException, CRC32Exception, NullPointerException, DataTooLongForRSAEncryptionException, IllegalArgumentException {
		if (Log.isDebugEnable()) {
			Log.debug(TAG + ".buildResumeCmdPacketData()", "START");
		}
		ProtocolPacketBuilderResponse protPacketBuilderResponse = null;
		this.cmdMetaData = cmdMetaData;
		cmdMetaData.setTransportDirective(transport);
			//long size = getPayloadSize(payloadPath) - numbOfBytes;
			long payloadSize = getPayloadSize(payloadPath);
			cmdMetaData.setPayloadSize(payloadSize);
			//Parse MetaData
			byte[] metaData = ProtocolParser.parseCommandMetadata(cmdMetaData);
			//Encrypt MetaData
			byte[] encData = encryptMetaData(aesKey, metaData);
			//Set MetaData_Header
			metaData = setMetaDataHeader(encData, publicKey, aesKey, ssid);
			protPacketBuilderResponse = setResponse(aesKey, metaData, null, payloadPath, PayloadType.FILE);
			
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".buildResumeCmdPacketData()", "END");
			}
			
		return protPacketBuilderResponse;
	}
	
	public ProtocolPacketBuilderResponse buildMetaData(CommandMetaData cmdMetaData, long payloadCrc32, long payloadSize, byte[] publicKey, byte[] aesKey, long ssid) throws IOException, NullPointerException, DataTooLongForRSAEncryptionException {
		ProtocolPacketBuilderResponse protPacketBuilderResponse = null;
		cmdMetaData.setPayloadSize(payloadSize);
		cmdMetaData.setPayloadCrc32(payloadCrc32);
		cmdMetaData.setTransportDirective(TransportDirectives.RASK);
		//Parse MetaData
		byte[] metaData = ProtocolParser.parseCommandMetadata(cmdMetaData);
		//Encrypt MetaData
		byte[] encData = encryptMetaData(aesKey, metaData);
		//Set MetaData_Header
		metaData = setMetaDataHeader(encData, publicKey, aesKey, ssid);
		
		//TEST!!!!
		//metaData = setMetaDataHeader(metaData, publicKey, aesKey, ssid);
		
		/*if (Log.isDebugEnable()) {
			Log.debug(TAG + ".buildMetaData()", "setResponse!");
			FileUtil.writeToFile(METADATA_FILE, metaData);
		}*/
		
		protPacketBuilderResponse = setResponse(aesKey, metaData, null, null, PayloadType.BUFFER);
		
		if (Log.isDebugEnable()) {
			Log.debug(TAG + ".buildMetaData()", "END");
		}
		
		return protPacketBuilderResponse;
	}
	
	
	private ProtocolPacketBuilderResponse setResponse(byte[] aesKey, byte[] metaData, byte[] payloadData, String payloadPath, PayloadType type) {
		ProtocolPacketBuilderResponse protPacketBuilderResponse = new ProtocolPacketBuilderResponse();
		protPacketBuilderResponse.setAesKey(aesKey);
		protPacketBuilderResponse.setMetaData(metaData);
		protPacketBuilderResponse.setPayloadData(payloadData);
		protPacketBuilderResponse.setPayloadPath(payloadPath);
		protPacketBuilderResponse.setPayloadType(type);
		return protPacketBuilderResponse;
	}
	
	//Override
	public void CRC32Completed(long crc32) {
		isCRC32Completed = true;
		cmdMetaData.setPayloadCrc32(crc32);
		Log.debug(TAG + ".CRC32Completed()", "Completed!");
	}
	
	//Override
	public void CRC32Error(String err) {
		isCRC32Completed = false;
		errMsg = err;
		Log.error(TAG + ".CRC32Error()", err);
	}	
	
	private long getPayloadSize(String payloadPath) throws IOException {
		FileConnection fCon = null;
		long payloadSize = 0;
		try {
			fCon = (FileConnection)Connector.open(payloadPath, Connector.READ_WRITE);
			payloadSize = fCon.fileSize();
		} finally {
			IOUtil.close(fCon);
		}
		return payloadSize;
	}	
	
	private byte[] encryptMetaData(byte[] aesKey, byte[] data) throws IOException {
		byte[] encData = null;
		encData = AESEncryptor.encrypt(aesKey, data);
		data = encData;		
		return data;
	}	

	private byte[] setMetaDataHeader(byte[] metaData, byte[] publicKey, byte[] aesKey, long ssid) throws IOException, DataTooLongForRSAEncryptionException, NullPointerException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			byte encryptType = 1;
			dos.write(ByteUtil.toByte(encryptType));
			dos.write(ByteUtil.toByte((int)ssid));
			//AES_KEY is RSA Encrypted
			
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".setMetaDataHeader()", "*** Before AES key Encrypt size: " + aesKey.length + "***");
			}
			
			byte[] encAESKey = RSAEncryption.encrypt(publicKey, aesKey);
			
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".setMetaDataHeader()", "*** After AES key Encrypted size: " + encAESKey.length + "***");
			}
			
			short lenAESKey =  (short)encAESKey.length;
			dos.write(ByteUtil.toByte(lenAESKey));
			dos.write(encAESKey);
			short requestLen = (short)metaData.length;
			dos.write(ByteUtil.toByte(requestLen));
			
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".setMetaDataHeader()", "length of header: "+ (4 + requestLen + 2 + lenAESKey + 2 + 4 + 1));
			}
			
			//MetaData's CRC32
			int crc32 = (int)CRC32.calculate(metaData);
			dos.write(ByteUtil.toByte(crc32));
			dos.write(metaData);
			// To byte array.
			metaData = bos.toByteArray();
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".setMetaDataHeader()", "Success!");
			}
			return metaData;
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
	}	
}
