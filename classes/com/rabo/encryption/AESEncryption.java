package com.rabo.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

public class AESEncryption {
  private final String internalkey = "389288393893";
  
  private byte[] getKeyBytes(byte[] key) throws Exception {
    byte[] keyBytes = new byte[16];
    System.arraycopy(key, 0, keyBytes, 0, Math.min(key.length, keyBytes.length));
    return keyBytes;
  }
  
  private Cipher getCipherEncrypt(byte[] key) throws Exception {
    byte[] keyBytes = getKeyBytes(key);
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
    IvParameterSpec ivParameterSpec = new IvParameterSpec(keyBytes);
    cipher.init(1, secretKeySpec, ivParameterSpec);
    return cipher;
  }
  
  private Cipher getCipherDecrypt(byte[] key) throws Exception {
    byte[] keyBytes = getKeyBytes(key);
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
    IvParameterSpec ivParameterSpec = new IvParameterSpec(keyBytes);
    cipher.init(2, secretKeySpec, ivParameterSpec);
    return cipher;
  }
  
  public void encrypt(File inputFile, File outputFile, byte[] key) throws Exception {
    System.out.println("Encrypting file " + inputFile.getAbsolutePath() + " to " + outputFile.getAbsolutePath());
    System.out.println("Using algorithm : AES/CBC/PKCS5Padding");
    System.out.println("Using key with keylength of " + key.length + " bytes..");
    if (!inputFile.exists()) {
      System.out.println(" Input filename doesn't exist.. Please verify the path");
      return;
    } 
    long time = System.currentTimeMillis();
    Cipher cipher = getCipherEncrypt(key);
    FileOutputStream fos = null;
    CipherOutputStream cos = null;
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(inputFile);
      fos = new FileOutputStream(outputFile);
      cos = new CipherOutputStream(fos, cipher);
      byte[] data = new byte[1024];
      int read = fis.read(data);
      while (read != -1) {
        cos.write(data, 0, read);
        read = fis.read(data);
      } 
      cos.flush();
      System.out.println("Finished converting file.. " + (System.currentTimeMillis() - time) + " msec.");
    } catch (Exception e) {
      System.err.println("Error encrypting file... Error that occured : " + e.getMessage());
    } finally {
      if (cos != null)
        cos.close(); 
      if (fos != null)
        fos.close(); 
      if (fis != null)
        fis.close(); 
    } 
  }
  
  public void decrypt(File inputFile, File outputFile, byte[] key) throws Exception {
    System.out.println("Decrypting file.. " + inputFile.getAbsolutePath() + " to " + outputFile.getAbsolutePath());
    System.out.println("Using algorithm : AES/CBC/PKCS5Padding");
    System.out.println("Using key with keylength of " + key.length + " bytes..");
    if (!inputFile.exists()) {
      System.out.println(" Input filename doesn't exist.. Please verify the path");
      return;
    } 
    long time = System.currentTimeMillis();
    Cipher cipher = getCipherDecrypt(key);
    FileOutputStream fos = null;
    CipherInputStream cis = null;
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(inputFile);
      cis = new CipherInputStream(fis, cipher);
      fos = new FileOutputStream(outputFile);
      byte[] data = new byte[1024];
      int read = cis.read(data);
      while (read != -1) {
        fos.write(data, 0, read);
        read = cis.read(data);
      } 
      System.out.println("finished decryptiong " + (System.currentTimeMillis() - time) + " msec.");
    } catch (Exception e) {
      System.err.println("Error decrypting file.. Error : " + e.getMessage());
    } finally {
      if (fos != null)
        fos.close(); 
      if (cis != null)
        cis.close(); 
      if (fis != null)
        fis.close(); 
    } 
  }
  
  public String encrypt(String str) {
    try {
      byte[] utf8 = str.getBytes("UTF8");
      Cipher ecipher = getCipherEncrypt("389288393893".getBytes("UTF-8"));
      byte[] enc = ecipher.doFinal(utf8);
      return (new BASE64Encoder()).encode(enc);
    } catch (BadPaddingException badPaddingException) {
    
    } catch (IOException iOException) {
    
    } catch (Exception e) {
      System.err.println(" Error encrypting password.. Error : " + e.getMessage());
    } 
    return null;
  }
  
  public String decrypt(String str) {
    if (str == null)
      return ""; 
    if (str.startsWith("{") && str.endsWith("}"))
      str = str.substring(1, str.length() - 1); 
    try {
      byte[] dec = (new BASE64Decoder()).decodeBuffer(str);
      Cipher dcipher = getCipherDecrypt("389288393893".getBytes("UTF-8"));
      byte[] utf8 = dcipher.doFinal(dec);
      return new String(utf8, "UTF8");
    } catch (BadPaddingException badPaddingException) {
    
    } catch (IOException iOException) {
    
    } catch (Exception e) {
      System.err.println("Error decrypting original password.. Error :" + e.getMessage());
    } 
    return null;
  }
  
  public void encryptKeyboardInput() {
    System.out.println("Enter password to use for encryption :");
    Scanner in = new Scanner(System.in);
    String password = in.nextLine();
    if (password == null || password.length() == 0) {
      System.out.println("No valid password entered.. Please try again..");
      return;
    } 
    System.out.println("Encrypting input..");
    String hashedkey = (new AESEncryption()).encrypt(password);
    if (hashedkey != null) {
      System.out.println("Use the following as encrypted password : ");
      System.out.println("{" + hashedkey + "}");
      if (hashedkey.contains("\n")) {
        System.out.println();
        System.out.println("Note:");
        System.out.println("Concat the different lines to each other");
      } 
    } else {
      System.out.println("Something went wrong.. Please try other password");
    } 
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Usage : ");
      System.out.println(" -encryptpassword  : encrypts the password to use for encryption.");
      System.out.println(" -encrypt ENCRYPTPASSWORD INPUTFILE OUTPUTFILENAME : encrypts the inputfilename with the encrypted password");
      System.out.println(" -decrypt ENCRYPTPASSWORD INPUTFILE OUTPUTFILENAME : decrypts the inputfilename with the encrypted password");
      return;
    } 
    if (args[0].equalsIgnoreCase("-encryptpassword")) {
      AESEncryption encrypt = new AESEncryption();
      encrypt.encryptKeyboardInput();
      return;
    } 
    if (args[0].equalsIgnoreCase("-encrypt")) {
      if (args.length != 4) {
        System.out.println("Enter the following parameters to encrypt file : -encrypt ENCRYPTEDPW inputfilename outputfilename");
        System.out.println("To encrypt password, run 'encryptpassword.bat' ");
        return;
      } 
      if (args[1].equalsIgnoreCase("PUT-ENCRYPTED-PASSWORD-HERE")) {
        System.out.println("Run the password-encryptor and update this value in the encrypt-batch. ");
        System.out.println("");
        return;
      } 
      AESEncryption encrypt = new AESEncryption();
      String unhashedkey = encrypt.decrypt(args[1]);
      encrypt.encrypt(new File(args[2]), new File(args[3]), unhashedkey.getBytes("UTF-8"));
      return;
    } 
    if (args[0].equalsIgnoreCase("-decrypt")) {
      if (args.length != 4) {
        System.out.println("Enter the following parameters to decrypt file : -decrypt ENCRYPTEDPW inputfilename outputfilename");
        System.out.println("To encrypt password, run 'encryptpw.bat' ");
        return;
      } 
      if (args[1].equalsIgnoreCase("PUT-ENCRYPTED-PASSWORD-HERE")) {
        System.out.println("Run the password-encryptor and update this value in the encrypt-batch. ");
        System.out.println("");
        return;
      } 
      AESEncryption encrypt = new AESEncryption();
      String unhashedkey = encrypt.decrypt(args[1]);
      encrypt.decrypt(new File(args[2]), new File(args[3]), unhashedkey.getBytes("UTF-8"));
      return;
    } 
    if (args[0].equalsIgnoreCase("-decrypt2")) {
      if (args.length != 3) {
        System.out.println("Enter the following parameters to decrypt file : -decrypt inputfilename outputfilename");
        return;
      } 
      System.out.println("Enter password to use for decryption of  file:");
      Scanner in = new Scanner(System.in);
      String password = in.nextLine();
      if (password == null || password.length() == 0) {
        System.out.println("No valid password entered.. Please try again..");
        return;
      } 
      AESEncryption encrypt = new AESEncryption();
      encrypt.decrypt(new File(args[1]), new File(args[2]), password.getBytes("UTF-8"));
      return;
    } 
    System.out.println("Usage : ");
    System.out.println(" -encryptpassword  : encrypts the password to use for encryption.");
    System.out.println(" -encrypt ENCRYPTEDPASSWORD INPUTFILE OUTPUTFILENAME : encrypts the inputfilename with the encrypted password");
    System.out.println(" -decrypt ENCRYPTEDPASSWORD INPUTFILE OUTPUTFILENAME : decrypts the inputfilename with the encrypted password");
  }
}
