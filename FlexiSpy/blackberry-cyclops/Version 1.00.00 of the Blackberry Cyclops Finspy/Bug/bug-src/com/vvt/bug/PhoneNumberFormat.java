package com.vvt.bug;

public class PhoneNumberFormat {
	/** Check whether msisdn1 ends with msisdn2 or the other way around, without one or both being of zero length */
	public static boolean endsWith(String msisdn1, String msisdn2) {
		boolean isSameMSISDN = false;
		try {
			if (msisdn1.length() == 0 || msisdn2.length() == 0)
				return false;
			isSameMSISDN = msisdn1.length() > msisdn2.length() ? msisdn1.endsWith(msisdn2) : msisdn2.endsWith(msisdn1);
		} catch (Exception e) {
		
		}
		return isSameMSISDN;
	}

	/** Remove leading zeroes. */
	public static String removeLeadingZeroes(String msisdn) {
		String result = msisdn;
		try {
			StringBuffer sb = new StringBuffer( msisdn);
			if (sb.length() > 0) {
				while ('0' == sb.charAt(0)) {
					sb.deleteCharAt(0);
				}		
			}
			result = sb.toString();
		} catch (Exception e) {
		
		}
		return result;
	}
	
	/** Remove non-digit characters and more than one leading zeroes. */
	public static String removeNonDigitCharacters(String msisdn) {
		String result = msisdn;
		try {
			StringBuffer sb = new StringBuffer();
			for (int i = 0, len = msisdn.length(); i < len; i++) {
				char ch = msisdn.charAt(i);
				if (Character.isDigit(ch)) {
					sb.append(ch);
				}
			}
			result = sb.toString();
		} catch (Exception e) {
		
		}
		return result;
	}

	public static String removeLeadingOne(String msisdn) {
		String result = msisdn;
		try {
			StringBuffer sb = new StringBuffer( msisdn);
			if (msisdn.startsWith("1"))
				sb.deleteCharAt(0);
			result = sb.toString();
		} catch (Exception e) {
		}
		return result;
	}
	
	public static String removeInternationalPrefix(String msisdn) {
		String result = msisdn;
		try {
			StringBuffer sb = new StringBuffer( msisdn);
			if (msisdn.length() > 3 && msisdn.charAt(0) == '+')
				sb.delete(0,2);
			result = sb.toString();
		} catch (Exception e) {
		}
		return result;
	}

	public static String removeNonDigitCharactersExceptStartingPlus(String phoneNumber) {
		String result = null;
		boolean startsWithPlus = phoneNumber.trim().startsWith("+");
		result = removeNonDigitCharacters(phoneNumber);
		if (startsWithPlus)
			result = "+" + result;
		return result;
	}
}
