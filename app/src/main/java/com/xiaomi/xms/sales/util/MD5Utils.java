package com.xiaomi.xms.sales.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

	private static final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private static ThreadLocal<MessageDigest> MD5 = new ThreadLocal<MessageDigest>() {
		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance( "MD5" );
			}
			catch ( NoSuchAlgorithmException e ) {
				throw new IllegalStateException( "No md5 algorithm found");
			}
		}
	};

	public static String getMD5(String msg){
		MessageDigest md5 = MD5.get();
		md5.reset();
		return toHexStr(md5.digest(msg.getBytes()));
	}

	public static String toHexStr(byte[] bytes) {
		int j = bytes.length;
		char str[] = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) {
			byte byte0 = bytes[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
			str[k++] = hexDigits[byte0 & 0xf];
		}
		return String.valueOf(str);
	}
}
