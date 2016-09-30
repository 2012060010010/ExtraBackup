package com.idwtwt.restore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.idwtwt.encrypt.EncryAES;
import com.idwtwt.net.DownloadMail;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

public class RestoreService extends Service
{
	private SharedPreferences preferences; // 配置信息
	private DownloadMail downloadMail;
	private String contactfile = null;
	private String fileName = null;
	private String smsfile = null;
	private String callfile = null;
	private String filePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ExtraBackup/";
	
	private final int MESSAGE_TYPE_LIST = 0;
	private final int MESSAGE_TYPE_STATUES_FILE_START = 1;
	private final int MESSAGE_TYPE_STATUES_SMS_START = 2;
	private final int MESSAGE_TYPE_STATUES_CONTACT_START = 3;
	private final int MESSAGE_TYPE_STATUES_DEL_START = 4;
	private final int MESSAGE_TYPE_STATUES_ALL_DONE = 5;
	private final int MESSAGE_TYPE_STATUES_DEL_DONE = 6;
	public class RestorepBinder extends Binder
	{
		public RestoreService getService()
		{
			return RestoreService.this;
		}

	}

	private RestorepBinder binder = new RestorepBinder();

	@Override
	public void onCreate()
	{
		
		super.onCreate();

		preferences = getSharedPreferences("ExtraBackup", MODE_PRIVATE);
		user = preferences.getString("user", "");
		password = preferences.getString("password", "");
		host = preferences.getString("host", "");


		

	}

	private String host;
	private String user;
	private String password;

	@Override
    public IBinder onBind(Intent intent)
	{
	
		return binder;
	}
   public void restoreFromLocal(String contact,String sms,String call)
	  {
		 contactfile = contact;
		 smsfile=sms;
		 callfile=call;
		 new Thread()
		 {

			@Override
			public void run()
			{
				Intent intent = new Intent();
				intent.setAction("com.idwtwt.restore.DATA_REFRESH");
				
				intent.putExtra("type", MESSAGE_TYPE_STATUES_CONTACT_START); 
				sendBroadcast(intent);
				boolean reCon=false;
				boolean reSms=false;
				boolean reCall=false;
				if (contactfile.endsWith(".vcf"))
				{
					  reCon=restoreContactFromSD(contactfile); 
				}
				intent.putExtra("type",MESSAGE_TYPE_STATUES_SMS_START ); 
				sendBroadcast(intent);
				if (smsfile.endsWith(".csv"))
				{
					 reSms=restoreSMSfromSD(smsfile);
				}
				if (callfile.endsWith(".vcl"))
				{
					 reCall=restoreCallsFromSD(callfile);
				}
				try
				{
					Thread.sleep(10);
				} catch (InterruptedException e)
				{
				
					e.printStackTrace();
				}
				
				intent.putExtra("type",MESSAGE_TYPE_STATUES_ALL_DONE ); 
				sendBroadcast(intent);
				System.out.println("all restore done");
			}
			 
		 }.start(); 
	 }

 public void restoreFromNet(String name)
 {
	 fileName = name;
	 new Thread()
	 {

		@Override
		public void run()
		{
			Intent intent = new Intent();
			intent.setAction("com.idwtwt.restore.DATA_REFRESH");
		
			intent.putExtra("type", MESSAGE_TYPE_STATUES_FILE_START); 
			sendBroadcast(intent);
			if (getNetContent(fileName))
			{
				
			}
			else {
				
			}
			intent.putExtra("type", MESSAGE_TYPE_STATUES_CONTACT_START); 
			sendBroadcast(intent);
			boolean reContact=restoreContactFromSD(fileName);
			if (reContact==true)
			{
				
			}
			intent.putExtra("type",MESSAGE_TYPE_STATUES_SMS_START ); 
			sendBroadcast(intent);
			if (restoreSMSfromSD(fileName))
			{
				
			}
			else {
				
			}
			
			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				
				e.printStackTrace();
			}
			intent.putExtra("type",MESSAGE_TYPE_STATUES_DEL_START ); 
			sendBroadcast(intent);
			if (delete(fileName))
			{	
				
			}
			else {
				
			}
			
			intent.putExtra("type",MESSAGE_TYPE_STATUES_ALL_DONE ); 
			sendBroadcast(intent);
			
			System.out.println("all net restore done");
		}
		 
	 }.start();
	 
 }
 

	// 短信恢复函数
	public boolean restoreSMSfromSD(String filename)
	{

		File Smsfile = new File(filePath + filename);
		if (!Smsfile.exists())
		{
			return false;
		}
		//File smsfile=EncryAES.Decryption(Smsfile,filename,password);//解密已备份文件
		String s = null;
		final String ADDRESS = "address"; // 发件人地址 手机号
		final String PERSON = "person"; // 发件人，返回一个数字就是联系人列表里的序号
		final String BODY = "body";// 短消息内容
		final String DATE = "date";// 日期 long型，
		final String TYPE = "type";// 类型 1是接收到的，2是发出
		
		ContentValues values = new ContentValues();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(Smsfile)));
			try {
				while ((s = br.readLine()) != null) {
					if (s.startsWith("SMS:")) {
						int index = s.indexOf(":");
						String content = s.substring(index + 1, s.length());
						String[] cateArr = content.split(",");
						switch(cateArr.length){
						case 3:{
					        values.put(ADDRESS, cateArr[0]);
					        values.put(PERSON, cateArr[1]);
					        values.put(BODY, cateArr[2]);
					        }
						case 4:{
					        values.put(ADDRESS, cateArr[0]);
					        values.put(PERSON, cateArr[1]);
					        values.put(BODY, cateArr[2]);
					        values.put(DATE, cateArr[3]);}
						case 5:{
				            values.put(ADDRESS, cateArr[0]);
				            values.put(PERSON, cateArr[1]);
				            values.put(BODY, cateArr[2]);
				            values.put(DATE, cateArr[3]);
				            values.put(TYPE, cateArr[4]);}
						}
		
							}
						}
					
					getContentResolver().insert(Uri.parse("content://sms"),
							values);
					values.clear();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return true;

	}
//
	
	// 联系人恢复函数
	public boolean restoreContactFromSD(String filename) 
	{
		  File Contactfile = new File(filePath + filename);
		   if (!Contactfile.exists())
		    {
			  return false;
		     }
		   //File contactfile=EncryAES.Decryption(Contactfile,filename,password);//解密已备份文件
			String s = null;
			String l = "END:VCARD";
			BufferedReader br = null;
		
				try {
					br = new BufferedReader(new InputStreamReader(
							new FileInputStream(Contactfile)));
				} catch (FileNotFoundException e1) {
					
					e1.printStackTrace();
				}
			try {
				while ((s = br.readLine()) != null) {
					ContentValues values = new ContentValues();
					Uri rawContactUri = getContentResolver().insert(
							RawContacts.CONTENT_URI, values);
					long rawContactsId = ContentUris.parseId(rawContactUri);
					while ((s = br.readLine()).equals(l) == false) {
						if (s.startsWith("FN:")) {
							String FN = s.substring(3);
							values.clear();
							values.put(StructuredName.RAW_CONTACT_ID, rawContactsId);
							values.put(Data.MIMETYPE,
									StructuredName.CONTENT_ITEM_TYPE);
							values.put(StructuredName.DISPLAY_NAME, FN);
							getContentResolver().insert(Data.CONTENT_URI, values);
						} else if (s.startsWith("TEL;TYPE=HOME:")) {
							String TelHome = s.substring(14);
							values.clear();
							values.put(Phone.RAW_CONTACT_ID, rawContactsId);
							values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
							values.put(
									ContactsContract.CommonDataKinds.Phone.TYPE,
									ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
							values.put(Phone.NUMBER, TelHome);
							getContentResolver().insert(Data.CONTENT_URI, values);
						} else if (s.startsWith("TEL;TYPE=CELL:")) {
							String TelCell = s.substring(14);
							values.clear();
							values.put(Phone.RAW_CONTACT_ID, rawContactsId);
							values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
							values.put(
									ContactsContract.CommonDataKinds.Phone.TYPE,
									ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
							values.put(Phone.NUMBER, TelCell);
							getContentResolver().insert(Data.CONTENT_URI, values);
						} else if (s.startsWith("TEL;TYPE=WORK:")) {
							String TelWork = s.substring(14);
							values.clear();
							values.put(Phone.RAW_CONTACT_ID, rawContactsId);
							values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
							values.put(
									ContactsContract.CommonDataKinds.Phone.TYPE,
									ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
							values.put(Phone.NUMBER, TelWork);
							getContentResolver().insert(Data.CONTENT_URI, values);
						} else if (s.startsWith("EMAIL;TYPE=HOME:")) {
							String EmailHome = s.substring(15);
							values.clear();
							values.put(ContactsContract.Data.RAW_CONTACT_ID,
									rawContactsId);
							values.put(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
							values.put(
									ContactsContract.CommonDataKinds.Email.TYPE,
									ContactsContract.CommonDataKinds.Email.TYPE_HOME);
							values.put(ContactsContract.CommonDataKinds.Email.DATA,
									EmailHome);
							getContentResolver().insert(
									ContactsContract.Data.CONTENT_URI, values);
						} else if (s.startsWith("EMAIL;TYPE=PREF:")) {
							String EmailWork = s.substring(15);
							values.clear();
							values.put(ContactsContract.Data.RAW_CONTACT_ID,
									rawContactsId);
							values.put(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
							values.put(
									ContactsContract.CommonDataKinds.Email.TYPE,
									ContactsContract.CommonDataKinds.Email.TYPE_WORK);
							values.put(ContactsContract.CommonDataKinds.Email.DATA,
									EmailWork);
							getContentResolver().insert(
									ContactsContract.Data.CONTENT_URI, values);
						} else if (s.startsWith("EMAIL;TYPE=OTHER")) {
							String EmailOther = s.substring(16);
							values.clear();
							values.put(ContactsContract.Data.RAW_CONTACT_ID,
									rawContactsId);
							values.put(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
							values.put(
									ContactsContract.CommonDataKinds.Email.TYPE,
									ContactsContract.CommonDataKinds.Email.TYPE_OTHER);
							values.put(ContactsContract.CommonDataKinds.Email.DATA,
									EmailOther);
							getContentResolver().insert(
									ContactsContract.Data.CONTENT_URI, values);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return true;
	}
	@SuppressLint("SdCardPath")
	public boolean restoreCallsFromSD(String filename)
	{
		File Callfile = new File(filePath +  filename);
		if (!Callfile.exists())
		{
			return false;
		}
		//File callfile=EncryAES.Decryption(Callfile,filename,password);//解密已备份文件
		String s = null;
		ContentValues values = new ContentValues();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(Callfile)));
			try {
				while ((s = br.readLine()) != null && s.startsWith("CALL:")) {
					String[] cateArr = s.substring(5, s.length()).split(",");
					values.put(CallLog.Calls.NUMBER, cateArr[0]);
					values.put(CallLog.Calls.DATE, cateArr[1]);
					values.put(CallLog.Calls.TYPE, cateArr[2]);
					values.put(CallLog.Calls.DURATION, cateArr[3]);
					getContentResolver().insert(CallLog.Calls.CONTENT_URI,
							values);
					values.clear();
				}
			} catch (IOException e) {
			
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
		
			e.printStackTrace();
		}
	
		return true;
}
public boolean isDirExist(String filePath){
		 File dir=new File(filePath);
		 if (!dir.exists())
			{
				dir.mkdir();
				return false;
			}
		 return true;
	}
	public void getNetList()
	{
		new Thread()
		{

			@Override
			public void run()
			{
				downloadMail = new DownloadMail("imap." + host, user, password,"imap");
				try
				{
//					System.out.println("get");
					ArrayList<String> list = downloadMail.getList();// 得到邮件列表
					
					Intent intent = new Intent();
					intent.setAction("com.idwtwt.restore.DATA_REFRESH");
					intent.putExtra("type", MESSAGE_TYPE_LIST);
					intent.putStringArrayListExtra("data", list);
					sendBroadcast(intent);
					

//					System.out.println("get ok");

				} catch (Exception e)
				{
					
					e.printStackTrace();
				}
			}

		}.start();

	}
	public void getList()
	{
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			System.out.println("获得SD卡 备份文件列表");
			String filePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ExtraBackup/";
			ArrayList<String> list = new ArrayList<String>();
			File file = new File(filePath);
			File fileList[] = file.listFiles(new DataBaseFilter());
			for (int i = 0; i <fileList.length ; i++)
			{
				list.add(fileList[i].getName());
			}
			Intent intent = new Intent();
			intent.setAction("com.idwtwt.restore.DATA_REFRESH");
			intent.putExtra("type", MESSAGE_TYPE_LIST);
			intent.putStringArrayListExtra("data", list);
			sendBroadcast(intent);
		}
	}

	public boolean getNetContent(String name)
	{
		fileName = name;

		downloadMail = new DownloadMail("imap." + host, user, password,"imap");
		try
		{
			return downloadMail.getContent(fileName);
			
		} catch (Exception e)
		{
		
			e.printStackTrace();
			return false;
		}
		
	}

	public void deleteNetContent(String name)
	{
		fileName = name;
		new Thread()
		{

			@Override
			public void run()
			{
				downloadMail = new DownloadMail("imap." + host, user, password,"imap");
				
				try
				{
					System.out.println("send del ok");
					downloadMail.delete(fileName);
					
					Intent intent = new Intent();
					intent.setAction("com.idwtwt.restore.DATA_REFRESH");
					intent.putExtra("type",MESSAGE_TYPE_STATUES_DEL_DONE);				
					sendBroadcast(intent);
					
					
				} catch (Exception e)
				{
					
					e.printStackTrace();
				}
			}
		}.start();

	}

	public boolean delete(String name)
	{
		fileName = name;
		String filePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ExtraBackup/";
		File file = new File(filePath + name);
		file.delete();
		file = new File(filePath + name + "-journal" );//一起删除日志文件
		file.delete();
		Intent intent = new Intent();
		intent.setAction("com.idwtwt.restore.DATA_REFRESH");
		intent.putExtra("type",MESSAGE_TYPE_STATUES_DEL_DONE);				
		sendBroadcast(intent);
		return  true;

	}
	
public class DataBaseFilter implements FilenameFilter
	{

		@Override
		public boolean accept(File dir, String filename)
		{
			if(filename.endsWith(".vcl")||filename.endsWith(".vcf")||filename.endsWith(".csv"))
			{return true ;}
			else{return false;}
		}
		
	}

}
