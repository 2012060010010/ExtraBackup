package com.idwtwt.restore;

import java.util.ArrayList;

import javax.security.auth.PrivateCredentialPermission;

import com.idwtwt.backup.BackupActivity;
import com.idwtwt.backup.BackupAdapter;
import com.idwtwt.backup.BackupService;
import com.idwtwt.backup.Item;
import com.idwtwt.extrabackup.R;
import com.idwtwt.restore.RestoreService.RestorepBinder;

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RestoreActivity extends Activity
{
	private boolean isRunning = false;
	private RestorepBinder binder;
	
	
	private ServiceConnection connection = new ServiceConnection()
	{

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			// TODO Auto-generated method stub
			Toast.makeText(RestoreActivity.this, "���ݷ����쳣�˳�", Toast.LENGTH_LONG).show();

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			// TODO Auto-generated method stub
			binder = (RestoreService.RestorepBinder) service;
			isRunning = true;
			System.out.println("service ok");
			if (where.equals("�ƶ�"))
			{
				binder.getService().getNetList();
			}else {
				binder.getService().getList();
			}
		}
	};
	
	/******************************************************************************************/
	public ListView listView;
	public ArrayList<ListItem> items;
	public RestoreAdapter mAdapter;
	public Button start_restore_btn;
	public ImageButton refresh_btn;
	private String where;
	public TextView title_tv;
	private ProgressDialog dialog;
	
	private int del_position;
	
	public RestoreReceiver receiver;
	
	private final int MESSAGE_TYPE_LIST = 0;
	private final int MESSAGE_TYPE_STATUES_FILE_START = 1;
	private final int MESSAGE_TYPE_STATUES_SMS_START = 2;
	private final int MESSAGE_TYPE_STATUES_CONTACT_START = 3;
	private final int MESSAGE_TYPE_STATUES_DEL_START = 4;
	@Override
	protected void onDestroy()
	{
		unregisterReceiver(receiver);
		super.onDestroy();
		
	}

	private final int MESSAGE_TYPE_STATUES_ALL_DONE = 5;
	private final int MESSAGE_TYPE_STATUES_DEL_DONE = 6;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.restore);
		
		
		init();
	}
	private void init()
	{
		// TODO Auto-generated method stub
		listView = (ListView) findViewById(R.id.listview_restore);
		start_restore_btn = (Button) findViewById(R.id.start_restore);
		refresh_btn = (ImageButton)findViewById(R.id.refresh_btn);
		title_tv = (TextView) findViewById(R.id.title_tv);
		
		refresh_btn.setOnClickListener(refresh_listener);
		start_restore_btn.setOnClickListener(listener);
		Intent title_intent = getIntent();
		where = title_intent.getStringExtra("where");
		title_tv.setText("��" + where + "�ָ�");
		
		start_restore_btn.setEnabled(false);
		/******************************************************************************************/
		
		
		
		/************************************listView���*******************************************************/
		items = new ArrayList<ListItem>();
		mAdapter = new RestoreAdapter(RestoreActivity.this, items);
		listView.setAdapter(mAdapter);
		 
		//listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);//�����ʹ��������ã�ѡ���е�radiobutton�޷���Ӧѡ���¼�

		IntentFilter filter = new IntentFilter();
		filter.addAction("com.idwtwt.restore.DATA_REFRESH");
		
		receiver = new RestoreReceiver();
		
		registerReceiver(receiver, filter);
		/*********************************service���**********************************************************/

		Intent intent = new Intent();// ���ݸ��·���
		intent.setClass(RestoreActivity.this, RestoreService.class);
		bindService(intent, connection, Service.BIND_AUTO_CREATE);
		
		
		/*********************************��Ϣ���**********************************************************/
	
		
		/**********************************�����ļ��б�*********************************************************/
		dialog = ProgressDialog.show(RestoreActivity.this, "�ָ�����", "���ڻ�ȡ�����ļ��б�...");
	}
	public void call_delete(int position)
	{
		del_position = position;
		if (where.equals("�ƶ�"))
		{
		binder.getService().deleteNetContent(items.get(position).name);
		}else {
			binder.getService().delete(items.get(position).name);
		}
		 System.out.println("delete :" + del_position);
	}
	
	private OnClickListener listener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			
			if (isRunning)
			{
				if (where.equals("�ƶ�"))
				{
					//System.out.println(items.get(mAdapter.getSelected()).name);
					String name = items.get(mAdapter.getSelected()).name;//�õ�ѡ�еı����ļ���
					binder.getService().restoreFromNet(name);
					start_restore_btn.setEnabled(false);
					
				}else if (where.equals("SD��"))
				{
					String name = items.get(mAdapter.getSelected()).name;//�õ�ѡ�еı����ļ���
					binder.getService().restoreFromLocal(name);
					start_restore_btn.setEnabled(false);
					dialog = ProgressDialog.show(RestoreActivity.this, "�ָ�����", "׼����������...");
				}
				
				
			}
			else {
				Toast.makeText(RestoreActivity.this, "��������ʧ��", Toast.LENGTH_SHORT).show();
			}
			
			

		}
	};
	
	private OnClickListener refresh_listener = new OnClickListener()//�б���°�ť��Ӧ
	{
		
		@Override
		public void onClick(View v)
		{
			items.clear();
			mAdapter.notifyDataSetChanged();
			dialog = ProgressDialog.show(RestoreActivity.this, "�ָ�����", "���ڻ�ȡ�����ļ��б�...");
			if (where.equals("�ƶ�"))
			{
				binder.getService().getNetList();
			}else {
				binder.getService().getList();
			}
			
		}
	};

	public class RestoreReceiver extends BroadcastReceiver
	{
		
		@Override
		public void onReceive(Context context, Intent intent)
		{
			switch (intent.getIntExtra("type", -1))
			{
			case  MESSAGE_TYPE_LIST :	
				ArrayList<String> data = intent.getStringArrayListExtra("data");
				System.out.println("data get ok");
				items.clear();//����б�����
				for (int i = 0; i < data.size(); i++)
				{
					ListItem item = new ListItem();
					item.setName(data.get(i));//���ñ����ļ���
					item.setSel_logo(R.drawable.select_off);
					items.add(item);
				} 
				mAdapter.notifyDataSetChanged();
				dialog.dismiss();
				break;
			case MESSAGE_TYPE_STATUES_FILE_START:
				dialog = ProgressDialog.show(RestoreActivity.this, "�ָ�����", "��ȡ�������˱�������...");
				
			
				break;
			case MESSAGE_TYPE_STATUES_CONTACT_START:
				dialog.dismiss();
				dialog = ProgressDialog.show(RestoreActivity.this, "�ָ�����", "�ָ���ϵ������...");
				
				break;
			case MESSAGE_TYPE_STATUES_SMS_START:
				dialog.dismiss();
				dialog = ProgressDialog.show(RestoreActivity.this, "�ָ�����", "�ָ���������...");
				
				break;
			case MESSAGE_TYPE_STATUES_DEL_START:
				dialog.dismiss();
				dialog = ProgressDialog.show(RestoreActivity.this, "�ָ�����", "ɾ����ʱ�ļ�...");

				break;
			case MESSAGE_TYPE_STATUES_ALL_DONE:
				
				dialog.dismiss();
				Toast.makeText(RestoreActivity.this,"���ݻָ����", Toast.LENGTH_SHORT).show();
				start_restore_btn.setEnabled(true);
				break;
			case MESSAGE_TYPE_STATUES_DEL_DONE:
				items.remove(del_position);
				mAdapter.notifyDataSetInvalidated();
				Toast.makeText(RestoreActivity.this,"ɾ���ɹ�", Toast.LENGTH_SHORT).show();
				break;
			
			default:
				break;
			}
			
		}
		
		
	}
	

}
