package com.idwtwt.backup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import com.idwtwt.encrypt.EncryAES;
import com.idwtwt.net.UploadMail;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;

import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.widget.Toast;

public class BackupService extends Service
{

	UploadMail uploadMail;

	private SharedPreferences preferences; // 分享设置

	private String host;
	private String user;
	private String password;
	private String content;
	private Thread thread;
	private Handler handler;
	private Timer timer;
	private String filePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ExtraBackup/";
	   // 文件路径
	private SimpleDateFormat format = new SimpleDateFormat("MMddHH");
	
	private final int MESSAGE_TYPE_STATUES_SMS_DONE = 0;
	private final int MESSAGE_TYPE_STATUES_CONTACT_DONE = 1;
	private final int MESSAGE_TYPE_STATUES_NET_DONE = 2;
	private final int MESSAGE_TYPE_STATUES_NET_ERRO = 3;
	
	
	public boolean contacts_action = false;
	public boolean sms_action = false;
	public boolean net_action = false;
    public boolean calls_action=false;
	public class BackupBinder extends Binder
	{
		public BackupService getService()
		{
			return BackupService.this;
		
		}

	}

	private BackupBinder binder = new BackupBinder();

	@Override
	public IBinder onBind(Intent intent)
	{
		return binder;
	}

	@Override
	public void onCreate()
	{
		
		super.onCreate();
		preferences = getSharedPreferences("ExtraBackup", MODE_PRIVATE);

		user = preferences.getString("user", "");
		password = preferences.getString("password", "");
		host = preferences.getString("host", "");
		
		handler = new Handler()
		{

			@Override
			public void handleMessage(Message msg)
			{
				thread.interrupt();
				timer.cancel();
				Intent intent = new Intent();
				intent.setAction("com.idwtwt.restore.STATUES_REFRESH");
				intent.putExtra("type", MESSAGE_TYPE_STATUES_NET_ERRO); 
				sendBroadcast(intent);
			}
			
		};
		

	}

	@Override
	public void onDestroy()
	{
		
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		return super.onUnbind(intent);
	}

	
	public void backupAction(boolean action1,boolean action2,boolean action4,boolean action3)
	{
		contacts_action = action1;
		sms_action = action2;
		net_action = action3;
		calls_action=action4;
		Thread contactsms=new Thread() 
		{
			@Override
			public void run()
			{	
				Intent intent = new Intent();
				createDir();
				intent.setAction("com.idwtwt.restore.STATUES_REFRESH");
				if(calls_action){
					 boolean call=backupCallstoSD();
					 if (call)
						{   
							intent.putExtra("type", MESSAGE_TYPE_STATUES_CONTACT_DONE); 
							sendBroadcast(intent);
						}
				}
				if (contacts_action)
				{  
				    boolean contact=backupContactToSD();
				 
					if (contact)
					{  
						intent.putExtra("type", MESSAGE_TYPE_STATUES_CONTACT_DONE); 
						sendBroadcast(intent);
					}
				}
				if (sms_action)
				{ 
				    boolean sms=backupSMStoSD();
					if (sms)
					{
						intent.putExtra("type",MESSAGE_TYPE_STATUES_SMS_DONE ); 
						sendBroadcast(intent);
					}
				}
			}
		};
	
		contactsms.start();
		
		
		if (net_action)
		{
//			thread = new Thread()
//			{
//
//				@Override
//				public void run()
//				{
//					timer = new Timer();
//					timer.schedule(new TimerTask()
//					{
//						
//						@Override
//						public void run()
//						{
//							handler.sendEmptyMessage(0);
//							
//						}
//					},10000,1000);
//					if (send())
//					{   
//					try
//					{
//						Thread.sleep(100);//˯��һ��
//					} catch (InterruptedException e)
//					{
//						
//						e.printStackTrace();
//					}
//						Intent intent = new Intent();
//						intent.setAction("com.idwtwt.restore.STATUES_REFRESH");
//						intent.putExtra("type",MESSAGE_TYPE_STATUES_NET_DONE ); 
//						sendBroadcast(intent);
//						timer.cancel();
//					}	
//				}	
//			};
//			thread.start();
//			delete(content);
//			delete(content + "-journal");
		}
		
	}
 public void createDir(){
	 File dir=new File(filePath);
	 if (!dir.exists())
		{
			dir.mkdir();
		}
}

		@SuppressLint("SimpleDateFormat") 
	public boolean backupContactToSD()
		{

			   String name = format.format(new Date()) + "contact.vcf";
			   OutputStreamWriter outWriter;
			   File contactFile;
				try {
				    contactFile = new File(filePath, name);
					FileOutputStream os = new FileOutputStream(contactFile);
					outWriter = new OutputStreamWriter(os, "UTF-8");
				} catch (Exception e) {
					return false;
				}
				
			Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			
			int contactsID = cursor
					.getColumnIndex(ContactsContract.Contacts._ID);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				try {
					outWriter.write("BEGIN:VCARD\r\n");
				} catch (IOException e1) {
				
					e1.printStackTrace();
				}
				try {
					outWriter.write("VERSION:3.0\r\n");
				} catch (IOException e) {
					
					e.printStackTrace();
				}

				int contactid = cursor.getInt(contactsID);
				Cursor rawcur = getContentResolver()
						.query(ContactsContract.RawContacts.CONTENT_URI,
								null,
								ContactsContract.RawContacts.CONTACT_ID + " = "
										+ contactid, null, null);
				if (rawcur.moveToFirst()) {
					int rawid = cursor.getInt(cursor
							.getColumnIndex(ContactsContract.RawContacts._ID));
					Cursor datas = getContentResolver().query(
							ContactsContract.Data.CONTENT_URI, null,
							ContactsContract.Data.RAW_CONTACT_ID + " = " + rawid,
							null, null);
					for (datas.moveToFirst(); !datas.isAfterLast(); datas
							.moveToNext()) {
						try {
							storeContact(datas, outWriter);
						} catch (IOException e) {
							
							e.printStackTrace();
						}
					}
					datas.close();
				}
				rawcur.close();
				try {
					outWriter.write("END:VCARD\r\n");
				} catch (IOException e) {
			
					e.printStackTrace();
				}
				try {
					outWriter.flush();
				} catch (IOException e) {
			
					e.printStackTrace();
				}

			}
			try {
				outWriter.close();
			} catch (IOException e) {
		
				e.printStackTrace();
			}
			cursor.close();
          
           //EncryAES.Encryption(contactFile,name,password);//加密
			return true;
		}



	@SuppressLint("SimpleDateFormat")
	public boolean backupSMStoSD() 
	{
		   String name = format.format(new Date()) + "sms.csv";
		   OutputStreamWriter outWriter;
		   File smsFile;
			try {
				 smsFile = new File(filePath, name);
				System.out.println(smsFile.getAbsolutePath());
				FileOutputStream os = new FileOutputStream(smsFile);
				outWriter = new OutputStreamWriter(os, "UTF-8");
			} catch (Exception e) {
				return false;
			}


		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/"),
				null, null, null, null);
		System.out.println(cursor.getCount());
		if (cursor.moveToFirst())
		{
			int _address = cursor.getColumnIndex("address"); 
			int _person = cursor.getColumnIndex("person");
			int _body = cursor.getColumnIndex("body"); 
			int _date = cursor.getColumnIndex("date"); 
			int _type = cursor.getColumnIndex("type");
			do
			{  

				if (cursor.getString(_body) != null)
					try {
						outWriter.write("SMS:" + cursor.getString(_address) + ","
								+ cursor.getInt(_person) + ","
								+ cursor.getString(_body) + ","
								+ cursor.getString(_date) +","
								+ cursor.getInt(_type) +"\r\n");
					} catch (IOException e) {
						e.printStackTrace();
					
					}
				
			} while (cursor.moveToNext());
			
			try {
				outWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			cursor.close();
			cursor = null;
		
		}
	   //EncryAES.Encryption(smsFile,name,password);
		return true;

	}

@SuppressLint("SimpleDateFormat")
public boolean backupCallstoSD()  {
	  
   String name = format.format(new Date()) + "calls.vcl";

		OutputStreamWriter outWriter;
		File callsFile;
		try {
		    callsFile = new File(filePath + name);
			FileOutputStream os = new FileOutputStream(callsFile);
			outWriter = new OutputStreamWriter(os, "UTF-8");
		} catch (Exception e) {
			return false;
		}
		Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI,
				null, null, null, null);
		
		int idxNumber = cursor.getColumnIndex(CallLog.Calls.NUMBER);
		int idxDate = cursor.getColumnIndex(CallLog.Calls.DATE);
		int idxType = cursor.getColumnIndex(CallLog.Calls.TYPE);
		int idxDur = cursor.getColumnIndex(CallLog.Calls.DURATION);
		
		while (cursor.moveToNext()) {
			try {
				outWriter.write("CALL:"
				        + cursor.getString(idxNumber) + ","
						+ cursor.getLong(idxDate) + ","
				        + cursor.getString(idxType)+ ","
						+ cursor.getInt(idxDur) + "\r\n");
			} catch (IOException e) {
			
				e.printStackTrace();
			}
			try {
				outWriter.flush();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
       
		try {
			outWriter.close();
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		cursor.close();
		//EncryAES.Encryption(callsFile,name,password);
		return true;
	}
public void storeContact(Cursor cur, OutputStreamWriter outWriter)
		throws IOException {
	String t = cur.getString(cur
			.getColumnIndex(ContactsContract.Data.MIMETYPE));
	if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(t)) {
		String phone = cur
				.getString(cur
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		int type = cur
				.getInt(cur
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
		switch (type) {
		case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
			outWriter.write("TEL;TYPE=HOME:" + phone + "\r\n");
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
			outWriter.write("TEL;TYPE=CELL:" + phone + "\r\n");
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
			outWriter.write("TEL;TYPE=PREF:" + phone + "\r\n");
			break;
		default:
			outWriter.write("TEL;TYPE=WORK:" + phone + "\r\n");
			break;
		}
	} else if (ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
			.equals(t)) {
		String email = cur
				.getString(cur
						.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
		int type = cur
				.getInt(cur
						.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
		switch (type) {
		case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
			outWriter.write("EMAIL;TYPE=HOME:" + email + "\r\n");
			break;
		case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
			outWriter.write("EMAIL;TYPE=PREF:" + email + "\r\n");
			break;
		default:
			outWriter.write("EMAIL;TYPE=OTHER" + email + "\r\n");
			break;
		}
	} else if (ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE
			.equals(t)) {
		String im = cur.getString(cur
				.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
		int protocol = cur
				.getInt(cur
						.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
		switch (protocol) {
		case ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK:
			outWriter.write("X-GOOGLE_TALK" + im + "\r\n");
			break;
		case ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ:
			outWriter.write("X-QQ" + im + "\r\n");
			break;
		default:
			outWriter.write("X-OTHER" + im + "\r\n");
			break;
		}
	} else if (ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
			.equals(t)) {
		String dname = cur
				.getString(cur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
		String fname = cur
				.getString(cur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
		String gname = cur
				.getString(cur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
		if (fname != null)
			outWriter.write("N:" + fname + ";");
		else
			outWriter.write("N:;");
		if (gname != null)
			outWriter.write(gname);
		outWriter.write(";;;\r\n");
		outWriter.write("FN:" + dname + "\r\n");
	}
}
	
	public boolean send()
	{

		
				uploadMail = new UploadMail("smtp." + host, user, password, content);
				try
				{
					uploadMail.send();
					return true;
				} catch (Exception e)
				{
					
					e.printStackTrace();
					return false;
				}
		
	}
	
	public boolean delete(String name)
	{
		String filePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ExtraBackup/";
		File file = new File(filePath + name);
		file.delete();
		return  true;

	}

}
