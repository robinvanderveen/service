package com.rabo.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class StringEncryptor {
  private Cipher deCipher;
  
  private Cipher enCipher;
  
  private SecretKeySpec key;
  
  private IvParameterSpec ivSpec;
  
  public StringEncryptor(byte[] keyBytes, byte[] ivBytes) {
    this.ivSpec = new IvParameterSpec(ivBytes);
    try {
      DESKeySpec dkey = new DESKeySpec(keyBytes);
      this.key = new SecretKeySpec(dkey.getKey(), "DES");
      this.deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
      this.enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } 
  }
  
  public byte[] encrypt(Object obj) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException, IllegalBlockSizeException, ShortBufferException, BadPaddingException {
    byte[] input = convertToByteArray(obj);
    this.enCipher.init(1, this.key, this.ivSpec);
    return this.enCipher.doFinal(input);
  }
  
  public Object decrypt(byte[] encrypted) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {
    this.deCipher.init(2, this.key, this.ivSpec);
    return convertFromByteArray(this.deCipher.doFinal(encrypted));
  }
  
  private Object convertFromByteArray(byte[] byteObject) throws IOException, ClassNotFoundException {
    ByteArrayInputStream bais = new ByteArrayInputStream(byteObject);
    ObjectInputStream in = new ObjectInputStream(bais);
    Object o = in.readObject();
    in.close();
    return o;
  }
  
  private byte[] convertToByteArray(Object complexObject) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(baos);
    out.writeObject(complexObject);
    out.close();
    return baos.toByteArray();
  }
}
