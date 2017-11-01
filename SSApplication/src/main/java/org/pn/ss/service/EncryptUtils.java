package org.pn.ss.service;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.pn.ss.AppProperties;
import org.pn.ss.exception.SSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class EncryptUtils {

	@Autowired
	private AppProperties appProperties;

	private static Key aesKey = null;
	private static Cipher cipher = null;

	synchronized private void init() throws Exception {
		String keyStr = appProperties.getEncryptKey();
		if (keyStr == null || keyStr.length() != 16) {
			throw new SSException("bad aes key configured");
		}
		if (aesKey == null) {
			aesKey = new SecretKeySpec(keyStr.getBytes(), "AES");
			cipher = Cipher.getInstance("AES");
		}
	}

	synchronized public String encrypt(String text) throws SSException {
		try {
			init();
			if (StringUtils.isEmpty(text)) {
				return null;
			}
			cipher.init(Cipher.ENCRYPT_MODE, aesKey);

			return toHexString(cipher.doFinal(text.getBytes()));
		}

		catch (Exception e) {
			throw new SSException(e.getMessage());
		}
	}

	synchronized public String decrypt(String text) throws SSException {
    	try{
        init();
        if(StringUtils.isEmpty(text)){return null;}
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return new String(cipher.doFinal(toByteArray(text)));
    	}
        catch(Exception e){ throw new SSException(e.getMessage());}
    }

	public static String toHexString(byte[] array) {
		return DatatypeConverter.printHexBinary(array);
	}

	public static byte[] toByteArray(String s) {
		return DatatypeConverter.parseHexBinary(s);
	}

}