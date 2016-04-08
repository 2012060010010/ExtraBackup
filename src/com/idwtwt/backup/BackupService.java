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

	private SharedPreferences preferences; // ������Ϣ

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
		
		//���糬ʱ��������Ϣ��������Ȼ����
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
							Thread.sleep(100);//˯��һ�£�������ű���̫�죬���û�����û�б��ݶ���
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
						Thread.sleep(100);//˯��һ��
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
	
	
	// ��ϵ�˱��ݺ���
		public boolean backupContactToSD(SQLiteDatabase db)
		{

			
			int contacts_id;
			String person_name;
			String  tel_number;
			String e_mail;
			Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			// �����¼��Ϊ��
			
			
	/********************************************��ʼ����***********************************************************************/
			db.beginTransaction(); // ��ʼ������
	
			if (cursor.getCount() > 0)
			{
			 while (cursor.moveToNext())
				{
	
					contacts_id = cursor.getInt(cursor.getColumnIndex(Contacts._ID));// ���contacts
																					// ��ϵ��ID
					// ��ȡ��ϵ������
					person_name=cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
					//System.out.println(person_name);
					//ʹ��ContentResolver������ϵ�˺���
					Cursor phones = getContentResolver().query(CommonDataKinds.Phone.CONTENT_URI,
							null, CommonDataKinds.Phone.CONTACT_ID + " = " +contacts_id,
					null, null);
					if(phones.moveToFirst()){
					tel_number=phones.getString(phones.getColumnIndex(CommonDataKinds.Phone.NUMBER));
					}else{
						tel_number=null;
					}
					phones.close();
					//ʹ��ContentResolver������ϵ��e_mail address
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
	                   // ��ȡ�������ݲ����½������ݿ�
						String insert_sql = "insert into contacts values(null, ?, ?, ?, ?)";
						Object[] bindArgs = new Object[]
						{ contacts_id, person_name, tel_number,e_mail};
						db.execSQL(insert_sql, bindArgs);	
				}
	
			}
			cursor.close();
			cursor = null;
			db.setTransactionSuccessful(); // ����������ɹ��������û��Զ��ع����ύ
		/********************************************��������***********************************************************************/
			db.endTransaction(); //

			return true;
		}
		
	// ���ű��ݺ���
	public boolean backupSMStoSD(SQLiteDatabase db)
	{
		/*------------------------------------��ȡ�������ݿ�-------------------------------------------------*/
		Uri SMS_URI = Uri.parse("content://sms/");
		String[] projection =new String[]
		{        "_id", // ������Ϣ��
				"address", // �����˵�ַ �ֻ���
				"person", // �����ˣ�����һ�����־�����ϵ���б�������
				"body", // ����Ϣ����
				"date", // ���� long�ͣ�
			    "type" // ���� 1�ǽ��յ��ģ�2�Ƿ�����
		};
		
		Cursor cursor =getContentResolver().query(SMS_URI, projection, null, null, "date desc");
		
		while (db.isDbLockedByCurrentThread()){  
	       try {  
	            Thread.sleep(10);  
	        } catch (InterruptedException e) {  
	            e.printStackTrace();  
	        }  
	    }  
		
		/*--------------------------------------������д�����ݿ�-----------------------------------------------*/
         System.out.println("��ʼ���ݶ��ţ�����");
		db.beginTransaction(); // ��ʼ������
		if (cursor.moveToFirst())
		{
			String address; // �����˵�ַ �ֻ���
			int person; // �����ˣ�����һ�����־�����ϵ���б�������
			String body; // ����Ϣ����
			String date; // ���� long�ͣ�
			int type; // ���� 1�ǽ��յ��ģ�2�Ƿ�����

			int _address = cursor.getColumnIndex("address"); // �����˵�ַ �ֻ���
			int _person = cursor.getColumnIndex("person"); // �����ˣ�����һ�����־�����ϵ���б�������
			int _body = cursor.getColumnIndex("body"); // ����Ϣ����
			int _date = cursor.getColumnIndex("date"); // ���� long�ͣ�
			int _type = cursor.getColumnIndex("type"); // ���� 1�ǽ��յ��ģ�2�Ƿ�����
			
			while (db.isDbLockedByCurrentThread()){  
			       try {  
			            Thread.sleep(10);  
			        } catch (InterruptedException e) {  
			            e.printStackTrace();  
			        }  
			    }  
			
			//System.out.println("��ʼд�����ݿ�");
			do
			{
				while (db.isDbLockedByCurrentThread()){  
				       try {  
				            Thread.sleep(10);  
				        } catch (InterruptedException e) {  
				            e.printStackTrace();  
				        }  
				    }  
				
				address = cursor.getString(_address); // �����˵�ַ �ֻ���
				person = cursor.getInt(_person); // �����ˣ�����һ�����־�����ϵ���б�������
				body = cursor.getString(_body); // ����Ϣ����
				date = cursor.getString(_date); // ���� long�ͣ�
				type = cursor.getInt(_type); // ���� 1�ǽ��յ��ģ�2�Ƿ�����
                
				System.out.println("beifenzhong");
				
				String insert_sql = "insert into sms(address, person, body,date, type) values(?, ?, ?, ?, ?);";
				Object[] bindArgs = new Object[]
				{ address, person, body, date, type };
				db.execSQL(insert_sql, bindArgs);
			} while (cursor.moveToNext());
			
			
			cursor.close();
			cursor = null;
			db.setTransactionSuccessful(); // ����������ɹ��������û��Զ��ع����ύ
			db.endTransaction(); //
			System.out.println("д�����ݿ����");
		}
		return true;

	}

	// ��SD���������ݿ�
	// type�����ݿ����ͣ����ţ���ϵ�˵�
	@SuppressLint("SimpleDateFormat") public SQLiteDatabase createDatabase() 
	{
		/*------------------------------�ж��Ƿ���SD��-------------------------------------------------------*/
		// Is there any SD?
		boolean sdExist = android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());

		if (!sdExist)
		{// ���ǲ�����,

			return null;
		}
		/*------------------------------��SD���½����ݿ�-------------------------------------------------------*/
		SQLiteDatabase db;
		String dbPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ExtraBackup/";
		// ������Ϊ�������ݿ���ļ���
		SimpleDateFormat format = new SimpleDateFormat("yyyy��MM��dd�� HHʱmm��");
		String name = format.format(new Date()) + ".db";
		content = name;

		File dbp = new File(dbPath);
		File dbf = new File(dbPath + name);
		// ���·�������ڣ�����·��
		if (!dbp.exists())
		{
			dbp.mkdir();
		}

		// ���ݿ��ļ������ɹ�
			db = SQLiteDatabase.openOrCreateDatabase(dbf, null);
	
		String sql = "create table if not exists contacts("
		        + "_id integer primary key autoincrement," 
				+ " contacts_id integer,"//��ϵ��ID
		        + "person_name text, "//����
				+ " tel_number varchar(200)," //�绰
				+ " e_mail varchar(200));" ;//����
		db.execSQL(sql);
		sql = "create table if not exists sms(" 
		        + "_id integer primary key autoincrement," // ������Ϣ��
				+ "address varchar(255)," // �����˵�ַ �ֻ���
				+ "person varchar(255)," // �����ˣ�����һ�����־�����ϵ���б�������
				+ "body text," // ����Ϣ����
				+ "date varchar(255)," // ���� long
				+ "type integer);"; // ���� 1�ǽ��յ��ģ�2�Ƿ�����  
		db.execSQL(sql);
		// ���ݿ⽨���ɹ�������
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
