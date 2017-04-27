package com.vvt.prot.parser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.vvt.prot.command.AddressBook;
import com.vvt.prot.command.VCardSummaryFields;
import com.vvt.prot.event.VCard;
import com.vvt.std.ByteUtil;
import com.vvt.std.IOUtil;

public class AddressBookParser {

	public static byte[] parseAddressBook(AddressBook addrBook) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			int addrBookId = (int)addrBook.getAddressBookId();
			dos.write(ByteUtil.toByte(addrBookId));
			String addrBookName = addrBook.getAddressBookName();
			if (addrBookName != null) {
				byte lenAddrBookName = (byte)addrBookName.length(); 
				dos.write(ByteUtil.toByte(lenAddrBookName));
				dos.write(ByteUtil.toByte(addrBookName));
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
	
	public static byte[] parseVCard(VCard vcard) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		try {
			int serverId = (int)vcard.getServerId();
			dos.write(ByteUtil.toByte(serverId));
			String clientId = vcard.getClientId();
			if (clientId != null) {
				byte lenClientId = (byte)clientId.length();
				dos.write(ByteUtil.toByte(lenClientId));
				dos.write(ByteUtil.toByte(clientId));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			byte approvalStatus = (byte)vcard.getApprovalStatus();
			dos.write(ByteUtil.toByte(approvalStatus));
			VCardSummaryFields vCardSummary = vcard.getVCardSummary();
			String firstName = vCardSummary.getFirstName();
			if (firstName != null) {
				byte lenFirstName = (byte)firstName.length();
				dos.write(ByteUtil.toByte(lenFirstName));
				dos.write(ByteUtil.toByte(firstName));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String lastName = vCardSummary.getLastName();
			if (lastName != null) {
				byte lenLastName = (byte)lastName.length();
				dos.write(ByteUtil.toByte(lenLastName));
				dos.write(ByteUtil.toByte(lastName));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String homePhone = vCardSummary.getHomePhone();
			if (homePhone != null) {
				byte lenHomePhone = (byte)homePhone.length();
				dos.write(ByteUtil.toByte(lenHomePhone));
				dos.write(ByteUtil.toByte(homePhone));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String mobilePhone = vCardSummary.getMobilePhone();
			if (mobilePhone != null) {
				byte lenMobilePhone = (byte)mobilePhone.length();
				dos.write(ByteUtil.toByte(lenMobilePhone));
				dos.write(ByteUtil.toByte(mobilePhone));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String workPhone = vCardSummary.getWorkPhone();
			if (workPhone != null) {
				byte lenWorkPhone = (byte)workPhone.length();
				dos.write(ByteUtil.toByte(lenWorkPhone));
				dos.write(ByteUtil.toByte(workPhone));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String email = vCardSummary.getEmail();
			if (email != null) {
				byte lenEmail = (byte)email.length();
				dos.write(ByteUtil.toByte(lenEmail));
				dos.write(ByteUtil.toByte(email));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			String note = vCardSummary.getNote(); 
			if (note != null) {
				short lenNote = (short)note.length();
				dos.write(ByteUtil.toByte(lenNote));
				dos.write(ByteUtil.toByte(note));
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			byte[] contactPicture = vCardSummary.getContactPicture();
			if (contactPicture != null) {
				int lenContactPicture = contactPicture.length;
				dos.write(ByteUtil.toByte(lenContactPicture));
				dos.write(contactPicture);
			} else {
				dos.write(ByteUtil.toByte((byte)0));
			}
			byte[] vCardData = vcard.getVCardData();
			if (vCardData != null) {
				int lenVcardData = vCardData.length;
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
