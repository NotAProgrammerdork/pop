package com.vvt.prot.parser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.vvt.prot.command.VCardSummaryFields;
import com.vvt.prot.event.VCard;
import com.vvt.std.ByteUtil;
import com.vvt.std.IOUtil;

public class VCardParser {

	public static byte[] parseVCard(VCard vcard) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			int serverId = (int)vcard.getServerId();
			dos.write(ByteUtil.toByte(serverId));
			String clientId = vcard.getClientId();
			byte lenClientId = (byte)clientId.length();
			if (lenClientId > 0) {
				dos.write(ByteUtil.toByte(lenClientId));
				dos.write(ByteUtil.toByte(clientId));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			byte approvalStatus = (byte)vcard.getApprovalStatus();
			dos.write(ByteUtil.toByte(approvalStatus));
			VCardSummaryFields vCardSummary = vcard.getVCardSummary();
			String firstName = vCardSummary.getFirstName();
			byte lenFirstName = (byte)firstName.length();
			if (lenFirstName > 0) {
				dos.write(ByteUtil.toByte(lenFirstName));
				dos.write(ByteUtil.toByte(firstName));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String lastName = vCardSummary.getLastName();
			byte lenLastName = (byte)lastName.length();
			if (lenLastName > 0) {
				dos.write(ByteUtil.toByte(lenLastName));
				dos.write(ByteUtil.toByte(lastName));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String homePhone = vCardSummary.getHomePhone();
			byte lenHomePhone = (byte)homePhone.length();
			if (lenHomePhone > 0) {
				dos.write(ByteUtil.toByte(lenHomePhone));
				dos.write(ByteUtil.toByte(homePhone));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String mobilePhone = vCardSummary.getMobilePhone();
			byte lenMobilePhone = (byte)mobilePhone.length();
			if (lenMobilePhone > 0) {
				dos.write(ByteUtil.toByte(lenMobilePhone));
				dos.write(ByteUtil.toByte(mobilePhone));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String workPhone = vCardSummary.getWorkPhone();
			byte lenWorkPhone = (byte)workPhone.length();
			if (lenWorkPhone > 0) {
				dos.write(ByteUtil.toByte(lenWorkPhone));
				dos.write(ByteUtil.toByte(workPhone));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String email = vCardSummary.getEmail();
			byte lenEmail = (byte)email.length();
			if (lenEmail > 0) {
				dos.write(ByteUtil.toByte(lenEmail));
				dos.write(ByteUtil.toByte(email));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String note = vCardSummary.getNote(); 
			short lenNote = (short)note.length(); 
			if (lenNote > 0) {
				dos.write(ByteUtil.toByte(lenNote));
				dos.write(ByteUtil.toByte(note));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			byte[] contactPicture = vCardSummary.getContactPicture();
			int lenContactPicture = contactPicture.length;
			if (lenContactPicture > 0) {
				dos.write(ByteUtil.toByte(lenContactPicture));
				dos.write(contactPicture);
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			byte[] vCardData = vcard.getVCardData();
			int lenVcardData = vCardData.length;
			if (lenVcardData > 0) {
				dos.write(ByteUtil.toByte(lenVcardData));
				dos.write(vCardData);
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
	
}
