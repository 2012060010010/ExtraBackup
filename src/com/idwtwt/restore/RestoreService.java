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
	private SharedPreferences preferences; // ������Ϣ
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
				}//˯��һ�£�������ű���̫�죬���û�����û�лָ�����

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
			}//˯��һ�£�������ű���̫�죬���û�����û�лָ�����
			
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
	// ���ű��ݺ���
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
		// ��ñ������ݿ��е����м�¼
		String sql = "select * from sms order by date desc";
		Cursor cursor = db.rawQuery(sql, null);

		/*-------------------------------------------------------------------------------------*/

		if (cursor.moveToFirst())
		{
			int _id; // ������Ϣ��
			// int thread_id;// �Ի������
			String address; // �����˵�ַ �ֻ���
			int person; // �����ˣ�����һ�����־�����ϵ���б�������
			long date; // ���� long�ͣ�
			int read; // �Ƿ��Ķ�
			String body; // ����Ϣ����
			String service_center;// ��Ϣ����
			int error_code;// �������
			int type; // ���� 1�ǽ��յ��ģ�2�Ƿ�����

			// int _thread_id = cursor.getColumnIndex("thread_id");// �Ի������
			int _address = cursor.getColumnIndex("address"); // �����˵�ַ �ֻ���
			int _person = cursor.getColumnIndex("person"); // �����ˣ�����һ�����־�����ϵ���б�������
			int _date = cursor.getColumnIndex("date"); // ���� long�ͣ�
			int _read = cursor.getColumnIndex("read"); // �Ƿ��Ķ�
			int _body = cursor.getColumnIndex("body"); // ����Ϣ����
			int _service_center = cursor.getColumnIndex("service_center");// ��Ϣ����
			int _error_code = cursor.getColumnIndex("error_code");// �������
			int _type = cursor.getColumnIndex("type"); // ���� 1�ǽ��յ��ģ�2�Ƿ�����

			do
			{
				// _id = cursor.getString(); // ������Ϣ��
				// thread_id = cursor.getInt(_thread_id);// �Ի������
				address = cursor.getString(_address); // �����˵�ַ �ֻ���
				person = cursor.getInt(_person); // �����ˣ�����һ�����־�����ϵ���б�������
				date = cursor.getLong(_date); // ���� long�ͣ�
				read = cursor.getInt(_read); // �Ƿ��Ķ�
				body = cursor.getString(_body); // ����Ϣ����
				service_center = cursor.getString(_service_center);// ��Ϣ����
				error_code = cursor.getInt(_error_code);// �������
				type = cursor.getInt(_type); // ���� 1�ǽ��յ��ģ�2�Ƿ�����

				// д�뵽��������Դ
				ContentValues SMS_Values = new ContentValues();
				// SMS_Values.put("thread_id",thread_id);// �Ի������
				SMS_Values.put("address", address);// �����˵�ַ �ֻ���
				SMS_Values.put("person", person);// �����ˣ�����һ�����־�����ϵ���б�������
				SMS_Values.put("date", date);// ���� long�ͣ�
				SMS_Values.put("read", read);// �Ƿ��Ķ�
				SMS_Values.put("body", body);// ����Ϣ����
				SMS_Values.put("service_center", service_center);// ��Ϣ����
				SMS_Values.put("error_code", error_code);// �������
				SMS_Values.put("type", type);// ���� 1�ǽ��յ��ģ�2�Ƿ�����
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
	// �������˻ָ�����
	public boolean restoreContactFromSD(String file)
	{
		/*---------------------------------------�����ݿⱸ��----------------------------------------------*/
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

			if (id != cursor.getInt(cursor.getColumnIndex("raw_contacts_id")))// �������µ���ϵ��
			{
				id = cursor.getInt(cursor.getColumnIndex("raw_contacts_id"));
				// ��rawContacts���в�������
				values = new ContentValues();
				Uri rawContactUri = getContentResolver().insert(RawContacts.CONTENT_URI, values);
				rawContactId = ContentUris.parseId(rawContactUri);

			}
			// ��data���в�����������
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
					ArrayList<String> list = downloadMail.getList();// �õ��ʼ��б�
					
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
			System.out.println("���SD�� �����ļ��б�");
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
		file = new File(filePath + name + "-journal" );//һ��ɾ����־�ļ�
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
