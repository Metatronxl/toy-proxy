package com.xulei.toyproxy.encryption;

import java.io.ByteArrayOutputStream;

/**
 * crypt 加密
 *
 * 
 */
public interface ICrypt {
	
	void encrypt(byte[] data, ByteArrayOutputStream stream);

	void encrypt(byte[] data, int length, ByteArrayOutputStream stream);

	void decrypt(byte[] data, ByteArrayOutputStream stream);

	void decrypt(byte[] data, int length, ByteArrayOutputStream stream);

}
