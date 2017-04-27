package com.vvt.prot.databuilder;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import com.vvt.compression.GZipCompressListener;
import com.vvt.compression.GZipCompressor;
import com.vvt.encryption.AESEncryptor;
import com.vvt.encryption.AESKeyGenerator;
import com.vvt.encryption.AESListener;
import com.vvt.prot.CommandData;
import com.vvt.prot.DataProvider;
import com.vvt.prot.CommandMetaData;
import com.vvt.prot.command.SendEvents;
import com.vvt.prot.command.TransportDirectives;
import com.vvt.prot.event.PEvent;
import com.vvt.prot.event.Thumbnail;
import com.vvt.prot.event.VideoThumbnailEvent;
import com.vvt.prot.parser.EventParser;
import com.vvt.std.ByteUtil;
import com.vvt.std.IOUtil;
import com.vvt.std.Log;

public class StoreEventPayloadBuilder extends PayloadBuilder implements AESListener, GZipCompressListener {
	private static final String TAG = "StoreEventPayloadBuilder";
	private static final String tempExtension = ".tmp";	
	private CommandMetaData cmdMetaData = null;
	private SendEvents event = null;
	private boolean compressSuccess = false;
	private boolean encryptedSuccess = false;
	
	public PayloadBuilderResponse buildPayload(CommandMetaData cmdMetaData, CommandData cmdData, String payloadPath, TransportDirectives transport) throws IllegalArgumentException, InterruptedException, IOException  {
		if (Log.isDebugEnable()) {
			Log.debug(TAG + ".buildPayload()", "START!");
		}
		PayloadBuilderResponse response = null;
		this.cmdMetaData = cmdMetaData;
		event = (SendEvents) cmdData;
		byte[] key = AESKeyGenerator.generateAESKey();
		if (transport.equals(TransportDirectives.RESUMABLE)) {
			response = buildFilePayload(key, payloadPath);			
		} else {
			response = buildBufferPayload(key);
		}
		if (Log.isDebugEnable()) {
			Log.debug(TAG + ".buildPayload()", "END!");
		}
		return response;
	}
	
	private PayloadBuilderResponse buildFilePayload(byte[] key, String payloadPath) throws IllegalArgumentException, IOException, InterruptedException {
		writePayloadFile(payloadPath);
		compressPayload(payloadPath);
		
		if (Log.isDebugEnable()) {
			Log.debug(TAG + ".buildFilePayload()", "*** Before Encrypt Payload size: " + getPayloadSize(payloadPath) + "***");
		}
		
		encryptPayload(payloadPath, key);
		
		if (Log.isDebugEnable()) {
			Log.debug(TAG + ".buildFilePayload()", "*** After Encrypted Payload size: " + getPayloadSize(payloadPath) + "***");
		}
		
		
		//Set Response.
		return setResponse(PayloadType.FILE, key, payloadPath, null);
	}
	
	private PayloadBuilderResponse buildBufferPayload(byte[] key) throws IOException, InterruptedException {
		byte[] payload = writeBuffer();
		byte[] cmpPayload = compressPayload(payload);
		byte[] encPayload = encryptPayload(cmpPayload, key);
		//Set Response.
		return setResponse(PayloadType.BUFFER, key, null, encPayload);
	}
	
	private PayloadBuilderResponse setResponse(PayloadType type, byte[] key, String filePath, byte[] data) {
		PayloadBuilderResponse response = new PayloadBuilderResponse();
		response.setPayloadType(type);
		response.setAesKey(key);
		response.setFilePath(filePath);
		response.setByteData(data);
		return response;
	}
	
	private void writePayloadFile(String payloadPath) throws IOException {
		FileConnection fCon = null;
		OutputStream os = null;
		try {
			fCon = (FileConnection)Connector.open(payloadPath, Connector.READ_WRITE);
			if (fCon.exists()) {
				fCon.delete();
			}
			fCon.create();
			os = fCon.openOutputStream();
			short cmdCode = (short)event.getCommand().getId();
			os.write(ByteUtil.toByte(cmdCode));
			//Start to write Command Data
			short eventCount = (short)event.getEventCount();
			os.write(ByteUtil.toByte(eventCount));
			if (eventCount > 0) {
				DataProvider cmdDataProvider = event.getEventIterator();
				while (cmdDataProvider.hasNext()) {
					//os.write(EventParser.parseEvent((PEvent)cmdDataProvider.getObject()));
					PEvent pEvent = (PEvent)cmdDataProvider.getObject();
					if (pEvent instanceof VideoThumbnailEvent) {
						VideoThumbnailEvent event = (VideoThumbnailEvent) pEvent;
						//ParingId
						int paringId = (int)event.getPairingId(); 
						os.write(ByteUtil.toByte(paringId));
						byte format = (byte)event.getFormat().getId();
						os.write(ByteUtil.toByte(format));
						byte[] videoData = event.getVideoData();
						os.write(videoData);
						byte thumbnailCount = (byte)event.getThumbnailCount();
						os.write(ByteUtil.toByte(thumbnailCount));
						if (thumbnailCount > 0) {
							DataProvider thumbDataProvider = event.getThumbnailIterator();
							while(thumbDataProvider.hasNext()) {
								Thumbnail thumbnail = (Thumbnail)thumbDataProvider.getObject();
								byte[] imageData = thumbnail.getImageData();
								int lenImageData = imageData.length;
								if (lenImageData > 0) {
									os.write(ByteUtil.toByte(lenImageData));
									os.write(imageData);
								} else {
									os.write(ByteUtil.toByte(0));
								}
								int actualSize = (int)event.getActualSize();
								os.write(ByteUtil.toByte(actualSize));
								int actualDuration = (int)event.getActualDuration();
								os.write(ByteUtil.toByte(actualDuration));
							}
						}
					} else {
						os.write(EventParser.parseEvent(pEvent));
					}
				}
			}
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "writePayloadFile Passed!: " + payloadPath);
			}
		} finally {
			IOUtil.close(fCon);
			IOUtil.close(os);
		}
	}	
	
	private byte[] writeBuffer() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		short cmdCode = (short)event.getCommand().getId();
		dos.write(ByteUtil.toByte(cmdCode));
		//Start to write Command Data
		short eventCount = (short)event.getEventCount();
		dos.write(ByteUtil.toByte(eventCount));
		if (eventCount > 0) {
			DataProvider cmdDataProvider = event.getEventIterator();
			while (cmdDataProvider.hasNext()) {
				PEvent pEvent = (PEvent)cmdDataProvider.getObject();
				dos.write(EventParser.parseEvent(pEvent));
			}
		}
		return bos.toByteArray();
	}
	
	private void renameFile(String inputFile, String outputFile) throws IOException, IllegalArgumentException {
		FileConnection fcon = null;
		try {
			//Before rename need to delete original file first.
			fcon = (FileConnection)Connector.open(outputFile, Connector.READ_WRITE);
			//Get output name before deleted!
			String fileName = fcon.getName();
			if (fcon.exists()) {
				fcon.delete();
			}
			fcon.close();
			fcon = (FileConnection)Connector.open(inputFile, Connector.READ_WRITE);
			fcon.rename(fileName);
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "renameFile Success!: " + outputFile);
			}
		} finally {
			IOUtil.close(fcon);
		}
	}
	
	private void compressPayload(String inputFile) throws InterruptedException, IOException {
		byte compressCode = (byte)cmdMetaData.getCompressionCode();
		if (compressCode != 0) {			
			GZipCompressor gzipComp = new GZipCompressor(inputFile,inputFile + tempExtension, this);
			gzipComp.compress();
			gzipComp.join();
			if (compressSuccess) {
				renameFile(inputFile + tempExtension, inputFile);
				compressSuccess = false;
			}
		}
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "compressPayload Passed!: " + inputFile);
		}
	}
	
	private byte[] compressPayload(byte[] payload) throws InterruptedException, IOException {
		byte compressCode = (byte)cmdMetaData.getCompressionCode();
		if (compressCode != 0) {			
			byte[] cmpPayload = GZipCompressor.compress(payload);
			payload = cmpPayload;
		}
		return payload;
	}
	
	private void encryptPayload(String inputFile, byte[] key) throws InterruptedException, IOException {
		byte encryptCode = (byte)cmdMetaData.getEncryptionCode();
		if (encryptCode != 0) {
			AESEncryptor enc = new AESEncryptor(key, inputFile, inputFile + tempExtension, this);
			enc.encrypt();
			enc.join();
			if (encryptedSuccess) {
				renameFile(inputFile + tempExtension, inputFile);
				encryptedSuccess = false;
			}
		}
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "encryptPayload Passed!: " + inputFile);
		}
	}
	
	private byte[] encryptPayload(byte[] payload, byte[] key) throws IOException {
		byte encryptCode = (byte)cmdMetaData.getEncryptionCode();
		if (encryptCode != 0) {
			byte[] encPayload = AESEncryptor.encrypt(key, payload);
			payload = encPayload;
		}
		return payload;
	}
	
	public void CompressCompleted() {
		compressSuccess = true;
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "CompressCompleted!");
		}
	}
	
	public void CompressError(String err) {
		//Set COMPRESSION_CODE = 0
		cmdMetaData.setCompressionCode(0);
		compressSuccess = false;
		Log.error(TAG, "CompressError: " + err);
	}

	public void AESEncryptionCompleted(String file) {
		encryptedSuccess = true;
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "AESEncryptionCompleted: " + file);
		}
	}
	
	public void AESEncryptionError(String err) {
		//Set ENCRYPTION_CODE = 0
		cmdMetaData.setEncryptionCode(0);
		encryptedSuccess = false;
		Log.error(TAG, "AESEncryptionError: " + err);
	}
	
	//Debug
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
}
