package com.idwtwt.backup;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.idwtwt.extrabackup.R.id;
import com.idwtwt.net.DownloadMail;
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

	private SharedPreferences preferences; // 配置信息

	private String host;
	private String user;
	private String password;
	private String content;
	private Thread thread;
	private Handler handler;
	private Timer timer;
	
	
	private final int MESSAGE_TYPE_STATUES_SMS_DONE = 0;
	private final int MESSAGE_TYPE_STATUES_CONTACT_DONE = 1;
	private final int MESSAGE_TYPE_STATUES_NET_DONE = 2;
	private final int MESSAGE_TYPE_STATUES_NET_ERRO = 3;
	
	
	public boolean contacts_action = false;
	public boolean sms_action = false;
	public boolean net_action = false;

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
		// TODO Auto-generated method stub
		return binder;
	}

	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
		preferences = getSharedPreferences("ExtraBackup", MODE_PRIVATE);

		user = preferences.getString("user", "");
		password = preferences.getString("password", "");
		host = preferences.getString("host", "");
		
		//网络超时，会有消息发过来，然后处理
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
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	
	public void backupAction(boolean action1,boolean action2,boolean action3)
	{
		contacts_action = action1;
		sms_action = action2;
		net_action = action3;
		new Thread()
		{

			@Override
			public void run()
			{	Intent intent = new Intent();
				intent.setAction("com.idwtwt.restore.STATUES_REFRESH");
				SQLiteDatabase db = createDatabase();
				if (contacts_action)
				{
					if (backupContactToSD(db))
					{
						intent.putExtra("type", MESSAGE_TYPE_STATUES_CONTACT_DONE); 
						sendBroadcast(intent);
					}
				}
				if (sms_action)
				{
					if (backupSMStoSD(db))
					{
						try
						{
							Thread.sleep(100);//睡眠一下，否则短信备份太快，让用户觉得没有备份短信
						} catch (InterruptedException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						intent.putExtra("type",MESSAGE_TYPE_STATUES_SMS_DONE ); 
						sendBroadcast(intent);
					}
				}
				db.close();
			}
			
			
		}.start();
		
		
		if (net_action)
		{
			
			
			thread = new Thread()
			{

				@Override
				public void run()
				{
					timer = new Timer();
					timer.schedule(new TimerTask()
					{
						
						@Override
						public void run()
						{
							handler.sendEmptyMessage(0);
							
						}
					},10000,1000);
					if (send())
					{   
					try
					{
						Thread.sleep(100);//睡眠一下
					} catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						Intent intent = new Intent();
						intent.setAction("com.idwtwt.restore.STATUES_REFRESH");
						intent.putExtra("type",MESSAGE_TYPE_STATUES_NET_DONE ); 
						sendBroadcast(intent);
						timer.cancel();
					}	
				}	
			};
			
			thread.start();
			delete(content);
			delete(content + "-journal");
		}
		
	}
	
	
	// 联系人备份函数
		public boolean backupContactToSD(SQLiteDatabase db)
		{

			
			int contacts_id;
			String person_name;
			String  tel_number;
			String e_mail;
			Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			// 如果记录不为空
			
			
	/********************************************开始事务***********************************************************************/
			db.beginTransaction(); // 开始事务处理
	
			if (cursor.getCount() > 0)
			{
			 while (cursor.moveToNext())
				{
	
					contacts_id = cursor.getInt(cursor.getColumnIndex(Contacts._ID));// 获得contacts
																					// 联系人ID
					// 获取联系人名字
					person_name=cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
					//System.out.println(person_name);
					//使用ContentResolver查找联系人号码
					Cursor phones = getContentResolver().query(CommonDataKinds.Phone.CONTENT_URI,
							null, CommonDataKinds.Phone.CONTACT_ID + " = " +contacts_id,
					null, null);
					if(phones.moveToFirst()){
					tel_number=phones.getString(phones.getColumnIndex(CommonDataKinds.Phone.NUMBER));
					}else{
						tel_number=null;
					}
					phones.close();
					//使用ContentResolver查找联系人e_mail address
					Cursor emails = getContentResolver().query(CommonDataKinds.Email.CONTENT_URI,
							null, CommonDataKinds.Email.CONTACT_ID + " = " +contacts_id,
					null, null);
					//emails.moveToNext();
					if(emails.moveToFirst()){
					e_mail=emails.getString(emails.getColumnIndex(CommonDataKinds.Email.DATA));
					}else{
						e_mail=null;
					}
					emails.close();
					
				     // System.out.println( RawContacts._ID);
	                   // 将取出的数据插入新建的数据库
						String insert_sql = "insert into contacts values(null, ?, ?, ?, ?)";
						Object[] bindArgs = new Object[]
						{ contacts_id, person_name, tel_number,e_mail};
						db.execSQL(insert_sql, bindArgs);	
				}
	
			}
			cursor.close();
			cursor = null;
			db.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		/********************************************结束事务***********************************************************************/
			db.endTransaction(); //

			return true;
		}
		
	// 短信备份函数
	public boolean backupSMStoSD(SQLiteDatabase db)
	{
		/*------------------------------------读取短信数据库-------------------------------------------------*/
		Uri SMS_URI = Uri.parse("content://sms/");
		String[] projection =new String[]
		{        "_id", // 短信消息号
				"address", // 发件人地址 手机号
				"person", // 发件人，返回一个数字就是联系人列表里的序号
				"body", // 短消息内容
				"date", // 日期 long型，
			    "type" // 类型 1是接收到的，2是发出的
		};
		
		Cursor cursor =getContentResolver().query(SMS_URI, projection, null, null, "date desc");
		
		while (db.isDbLockedByCurrentThread()){  
	       try {  
	            Thread.sleep(10);  
	        } catch (InterruptedException e) {  
	            e.printStackTrace();  
	        }  
	    }  
		
		/*--------------------------------------将短信写入数据库-----------------------------------------------*/
         System.out.println("开始备份短信！！！");
		db.beginTransaction(); // 开始事务处理
		if (cursor.moveToFirst())
		{
			String address; // 发件人地址 手机号
			int person; // 发件人，返回一个数字就是联系人列表里的序号
			String body; // 短消息内容
			String date; // 日期 long型，
			int type; // 类型 1是接收到的，2是发出的

			int _address = cursor.getColumnIndex("address"); // 发件人地址 手机号
			int _person = cursor.getColumnIndex("person"); // 发件人，返回一个数字就是联系人列表里的序号
			int _body = cursor.getColumnIndex("body"); // 短消息内容
			int _date = cursor.getColumnIndex("date"); // 日期 long型，
			int _type = cursor.getColumnIndex("type"); // 类型 1是接收到的，2是发出的
			
			while (db.isDbLockedByCurrentThread()){  
			       try {  
			            Thread.sleep(10);  
			        } catch (InterruptedException e) {  
			            e.printStackTrace();  
			        }  
			    }  
			
			//System.out.println("开始写入数据库");
			do
			{
				while (db.isDbLockedByCurrentThread()){  
				       try {  
				            Thread.sleep(10);  
				        } catch (InterruptedException e) {  
				            e.printStackTrace();  
				        }  
				    }  
				
				address = cursor.getString(_address); // 发件人地址 手机号
				person = cursor.getInt(_person); // 发件人，返回一个数字就是联系人列表里的序号
				body = cursor.getString(_body); // 短消息内容
				date = cursor.getString(_date); // 日期 long型，
				type = cursor.getInt(_type); // 类型 1是接收到的，2是发出的
                
				System.out.println("beifenzhong");
				
				String insert_sql = "insert into sms(address, person, body,date, type) values(?, ?, ?, ?, ?);";
				Object[] bindArgs = new Object[]
				{ address, person, body, date, type };
				db.execSQL(insert_sql, bindArgs);
			} while (cursor.moveToNext());
			
			
			cursor.close();
			cursor = null;
			db.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
			db.endTransaction(); //
			System.out.println("写入数据库完成");
		}
		return true;

	}

	// 在SD卡建立数据库
	// type：数据库类型：短信，联系人等
	@SuppressLint("SimpleDateFormat") public SQLiteDatabase createDatabase() 
	{
		/*------------------------------判断是否有SD卡-------------------------------------------------------*/
		// Is there any SD?
		boolean sdExist = android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());

		if (!sdExist)
		{// 若是不存在,

			return null;
		}
		/*------------------------------在SD卡新建数据库-------------------------------------------------------*/
		SQLiteDatabase db;
		String dbPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ExtraBackup/";
		// 日期作为备份数据库的文件名
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分");
		String name = format.format(new Date()) + ".db";
		content = name;

		File dbp = new File(dbPath);
		File dbf = new File(dbPath + name);
		// 如果路径不存在，建立路径
		if (!dbp.exists())
		{
			dbp.mkdir();
		}

		// 数据库文件创建成功
			db = SQLiteDatabase.openOrCreateDatabase(dbf, null);
	
		String sql = "create table if not exists contacts("
		        + "_id integer primary key autoincrement," 
				+ " contacts_id integer,"//联系人ID
		        + "person_name text, "//名字
				+ " tel_number varchar(200)," //电话
				+ " e_mail varchar(200));" ;//邮箱
		db.execSQL(sql);
		sql = "create table if not exists sms(" 
		        + "_id integer primary key autoincrement," // 短信消息号
				+ "address varchar(255)," // 发件人地址 手机号
				+ "person varchar(255)," // 发件人，返回一个数字就是联系人列表里的序号
				+ "body text," // 短消息内容
				+ "date varchar(255)," // 日期 long
				+ "type integer);"; // 类型 1是接收到的，2是发出的  
		db.execSQL(sql);
		// 数据库建立成功，返回
		return db;
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
					// TODO Auto-generated catch block
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
