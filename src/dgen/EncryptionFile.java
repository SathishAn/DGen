package dgen;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class EncryptionFile{
 
  private static final byte[] keyValue = {'0','2','3','4','5','6','7','8','9','1','2','3','4','5','6','7'};
  private static final String ALGO = "AES";
  
	private static Key generateKey() {
		return new SecretKeySpec(keyValue, "AES");
	}

  
	public static String encrypt(String Data) {

		String encryptedValue = null;
		try {
			Key key = generateKey();
			Cipher c = Cipher.getInstance("AES");
			c.init(1, key);
			byte[] encVal = c.doFinal(Data.getBytes());
			encryptedValue = (new BASE64Encoder()).encode(encVal);
		} catch (Exception e) {

		}

		return encryptedValue;
	}
  
	public static String decrypt(String encryptedData) {
		String decryptedValue = null;

		try {
			Key key = generateKey();
			Cipher c = Cipher.getInstance("AES");
			c.init(2, key);
			byte[] decordedValue = (new BASE64Decoder()).decodeBuffer(encryptedData);
			byte[] decValue = c.doFinal(decordedValue);
			decryptedValue = new String(decValue);
		} catch (Exception e) {

		}
		return decryptedValue;
	}
  
 
  
}

