package com.idwtwt.restore;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.idwtwt.net.DownloadMail;

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
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;

public class RestoreService extends Service
{
	private SharedPreferences preferences; // 配置信息
	private DownloadMail downloadMail;
	private String fileName = null;
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return binder;
	}
	 public void restoreFromLocal(String name)
	 {
		 fileName = name;
		 new Thread()
		 {

			@Override
			public void run()
			{
				Intent intent = new Intent();
				intent.setAction("com.idwtwt.restore.DATA_REFRESH");
				
				intent.putExtra("type", MESSAGE_TYPE_STATUES_CONTACT_START); 
				sendBroadcast(intent);
				if ( restoreContactFromSD(fileName) )
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//睡眠一下，否则短信备份太快，让用户觉得没有恢复短信

				intent.putExtra("type",MESSAGE_TYPE_STATUES_ALL_DONE ); 
				sendBroadcast(intent);
				
				System.out.println("all done");
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
			if ( restoreContactFromSD(fileName) )
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//睡眠一下，否则短信备份太快，让用户觉得没有恢复短信
			
			intent.putExtra("type",MESSAGE_TYPE_STATUES_DEL_START ); 
			sendBroadcast(intent);
			if (delete(fileName))
			{
				
				
			}
			else {
				
			}
			
			intent.putExtra("type",MESSAGE_TYPE_STATUES_ALL_DONE ); 
			sendBroadcast(intent);
			
			System.out.println("all done");
		}
		 
	 }.start();
	 
 }
	
	
	//
	//
	// 短信备份函数
	public boolean restoreSMSfromSD(String name)
	{

		/*-------------------------------------------------------------------------------------*/
		SQLiteDatabase db;
		String dbPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ExtraBackup/";
		File dbf = new File(dbPath + name);
		if (!dbf.exists())
		{
			return false;
		}

		db = SQLiteDatabase.openOrCreateDatabase(dbf, null);
		// 获得备份数据库中的所有记录
		String sql = "select * from sms order by date desc";
		Cursor cursor = db.rawQuery(sql, null);

		/*-------------------------------------------------------------------------------------*/

		if (cursor.moveToFirst())
		{
			int _id; // 短信消息号
			// int thread_id;// 对话的序号
			String address; // 发件人地址 手机号
			int person; // 发件人，返回一个数字就是联系人列表里的序号
			long date; // 日期 long型，
			int read; // 是否阅读
			String body; // 短消息内容
			String service_center;// 信息中心
			int error_code;// 错误代码
			int type; // 类型 1是接收到的，2是发出的

			// int _thread_id = cursor.getColumnIndex("thread_id");// 对话的序号
			int _address = cursor.getColumnIndex("address"); // 发件人地址 手机号
			int _person = cursor.getColumnIndex("person"); // 发件人，返回一个数字就是联系人列表里的序号
			int _date = cursor.getColumnIndex("date"); // 日期 long型，
			int _read = cursor.getColumnIndex("read"); // 是否阅读
			int _body = cursor.getColumnIndex("body"); // 短消息内容
			int _service_center = cursor.getColumnIndex("service_center");// 信息中心
			int _error_code = cursor.getColumnIndex("error_code");// 错误代码
			int _type = cursor.getColumnIndex("type"); // 类型 1是接收到的，2是发出的

			do
			{
				// _id = cursor.getString(); // 短信消息号
				// thread_id = cursor.getInt(_thread_id);// 对话的序号
				address = cursor.getString(_address); // 发件人地址 手机号
				person = cursor.getInt(_person); // 发件人，返回一个数字就是联系人列表里的序号
				date = cursor.getLong(_date); // 日期 long型，
				read = cursor.getInt(_read); // 是否阅读
				body = cursor.getString(_body); // 短消息内容
				service_center = cursor.getString(_service_center);// 信息中心
				error_code = cursor.getInt(_error_code);// 错误代码
				type = cursor.getInt(_type); // 类型 1是接收到的，2是发出的

				// 写入到短信数据源
				ContentValues SMS_Values = new ContentValues();
				// SMS_Values.put("thread_id",thread_id);// 对话的序号
				SMS_Values.put("address", address);// 发件人地址 手机号
				SMS_Values.put("person", person);// 发件人，返回一个数字就是联系人列表里的序号
				SMS_Values.put("date", date);// 日期 long型，
				SMS_Values.put("read", read);// 是否阅读
				SMS_Values.put("body", body);// 短消息内容
				SMS_Values.put("service_center", service_center);// 信息中心
				SMS_Values.put("error_code", error_code);// 错误代码
				SMS_Values.put("type", type);// 类型 1是接收到的，2是发出的
				SMS_Values.put("status", -1);

				
				getContentResolver().insert(Uri.parse("content://sms"), SMS_Values);

			} while (cursor.moveToNext());

			cursor.close();
			cursor = null;
		}
		return true;

	}

	/*
	 * ContactsContract defines an extensible database of contact-related
	 * information. Contact information is stored in a three-tier data model: A
	 * row in the ContactsContract.Data table can store any kind of personal
	 * data, such as a phone number or email addresses. The set of data kinds
	 * that can be stored in this table is open-ended. There is a predefined set
	 * of common kinds, but any application can add its own data kinds. A row in
	 * the ContactsContract.RawContacts table represents a set of data
	 * describing a person and associated with a single account (for example,
	 * one of the user's Gmail accounts). A row in the ContactsContract.Contacts
	 * table represents an aggregate of one or more RawContacts presumably
	 * describing the same person. When data in or associated with the
	 * RawContacts table is changed, the affected aggregate contacts are updated
	 * as necessary.
	 */

	//
	//
	//
	//
	// 来虚拟人恢复函数
	public boolean restoreContactFromSD(String file)
	{
		/*---------------------------------------打开数据库备份----------------------------------------------*/
		SQLiteDatabase db;
		String dbPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ExtraBackup/";
		File dbf = new File(dbPath + file);
		if (!dbf.exists())
		{
			return false;
		}

		db = SQLiteDatabase.openOrCreateDatabase(dbf, null);

		String sql = "select * from contacts";
		Cursor cursor = db.rawQuery(sql, null);
		cursor.moveToFirst();

		int id = -1;
		ContentValues values = null;
		String dataColumn[] =
		{ "data1", "data2", "data3", "data4", "data5", "data6", "data7", "data8", "data9", "data10", "data11", "data12", "data13", "data14", "data15" };
		long rawContactId = -1;

		do
		{

			if (id != cursor.getInt(cursor.getColumnIndex("raw_contacts_id")))// 表明是新的联系人
			{
				id = cursor.getInt(cursor.getColumnIndex("raw_contacts_id"));
				// 项rawContacts表中插入数据
				values = new ContentValues();
				Uri rawContactUri = getContentResolver().insert(RawContacts.CONTENT_URI, values);
				rawContactId = ContentUris.parseId(rawContactUri);

			}
			// 想data表中插入数据类型
			values.clear();
			values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
			values.put(ContactsContract.Data.MIMETYPE, cursor.getString(cursor.getColumnIndex("mimetype")));

			for (int i = 0; i < dataColumn.length; i++)
			{
				values.put(dataColumn[i], cursor.getString(cursor.getColumnIndex(dataColumn[i])));

			}

			getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

		} while (cursor.moveToNext());

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
					// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
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
			
			return filename.endsWith(".db");
		}
		
	}

}
