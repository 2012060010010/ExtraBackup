package com.idwtwt.encrypt; 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
  public class EncryAES{
	  private static String filePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ExtraBackup/";
/* 
 *  加密
 * @param file 
 * @param password
 * @return 
 */  
    public static void encrypt(File inFile,File outFile, String password){ 
    	   FileInputStream fis = null;
           FileOutputStream fos = null;
           CipherInputStream cis = null;
           try{
    	   fis = new FileInputStream(inFile);
           fos = new FileOutputStream(outFile);
           }catch(IOException e){
        	   e.printStackTrace();
           }
           byte[] buffer = new byte[1024*10];
           int length;
        try {             
                KeyGenerator kgen = KeyGenerator.getInstance("AES");  
                kgen.init(128, new SecureRandom(password.getBytes()));  
                SecretKey secretKey = kgen.generateKey();  
                byte[] enCodeFormat = secretKey.getEncoded();  
                SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");  
                Cipher cipher = Cipher.getInstance("AES");
                //byte[] byteContent = content.getBytes("utf-8");  
                cipher.init(Cipher.ENCRYPT_MODE, key);
                cis = new CipherInputStream(fis, cipher);
                while((length = cis.read(buffer)) != -1){
                    fos.write(buffer, 0, length);
                }
        } catch (NoSuchAlgorithmException e) {  
                e.printStackTrace();  
        } catch (NoSuchPaddingException e) {  
                e.printStackTrace();  
        } catch (InvalidKeyException e) {  
                e.printStackTrace();  
        }catch(IOException e){
     	   e.printStackTrace();
        } finally {
            if(cis != null){
                try {
                    cis.close();
                } catch (IOException e) {
                  
                    e.printStackTrace();
                }
            }
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                   
                    e.printStackTrace();
                }
            }
        }
} 
/**解密
 * @param file
 * @param password
 * @return 
 */  
  public static void decrypt(File inFile,File outFile, String password) {  
	  FileInputStream fis = null;
      FileOutputStream fos = null;
      CipherInputStream cis = null;
      try{
	   fis = new FileInputStream(inFile);
      fos = new FileOutputStream(outFile);
      }catch(IOException e){
   	   e.printStackTrace();
      }
      byte[] buffer = new byte[1024*10];
      int length;
   try {             
           KeyGenerator kgen = KeyGenerator.getInstance("AES");  
           kgen.init(128, new SecureRandom(password.getBytes()));  
           SecretKey secretKey = kgen.generateKey();  
           byte[] enCodeFormat = secretKey.getEncoded();  
           SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");  
           Cipher cipher = Cipher.getInstance("AES");// ����������  
           //byte[] byteContent = content.getBytes("utf-8");  
           cipher.init(Cipher.DECRYPT_MODE, key);// ��ʼ��  
           cis = new CipherInputStream(fis, cipher);
           while((length = cis.read(buffer)) != -1){
               fos.write(buffer, 0, length);
           }
   } catch (NoSuchAlgorithmException e) {  
           e.printStackTrace();  
   } catch (NoSuchPaddingException e) {  
           e.printStackTrace();  
   } catch (InvalidKeyException e) {  
           e.printStackTrace();  
   }catch(IOException e){
	   e.printStackTrace();
   } finally {
       if(cis != null){
           try {
               cis.close();
           } catch (IOException e) {
             
               e.printStackTrace();
           }
       }
       if(fos != null){
           try {
               fos.close();
           } catch (IOException e) {
              
               e.printStackTrace();
           }
       }
   }
} 
 public static void Encryption(File infile,String name,String password){
	 String Name="en"+name;
	 File outfile=new File(filePath,Name);
	 encrypt(infile,outfile, password);
	 infile.delete();
     outfile.renameTo(infile);
 }  
 public static File Decryption(File infile,String name,String password){
	 String Name="de"+name;
	 File outfile=new File(filePath,Name);
	 decrypt(infile,outfile, password);
	 infile.delete();
     return outfile;
 }  
// public static void main(String[] args)throws Exception{
//	     String password = "12345678";
//	    // EncryAES et=new EncryAES();
//	    
//	     File infile=new File("d:/","encrypted.docx");
//	     //Encryption(infile,password);
//	     //Decryption(infile,password);
//	     System.out.println("jie mi hou ");  
//    }
}