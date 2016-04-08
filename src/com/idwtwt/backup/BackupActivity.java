package com.idwtwt.backup;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.internet.NewsAddress;

import com.idwtwt.extrabackup.R;
import com.idwtwt.main.MainActivity;
import com.idwtwt.restore.RestoreActivity;
import com.idwtwt.restore.RestoreActivity.RestoreReceiver;

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
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BackupActivity extends Activity
{

	private boolean isRunning = false;
	private BackupService.BackupBinder binder;
	
	
	private final int MESSAGE_TYPE_STATUES_SMS_DONE = 0;
	private final int MESSAGE_TYPE_STATUES_CONTACT_DONE = 1;
	private final int MESSAGE_TYPE_STATUES_NET_DONE = 2;
	private final int MESSAGE_TYPE_STATUES_NET_ERRO = 3;
	private ServiceConnection connection = new ServiceConnection()
	{

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			// TODO Auto-generated method stub
			Toast.makeText(BackupActivity.this, "���ݷ����쳣�˳�", Toast.LENGTH_LONG).show();

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			// TODO Auto-generated method stub
			binder = (BackupService.BackupBinder) service;
			isRunning = true;
		}
	};

	/******************************************************************************************/

	private ListView listView;
	private BackupAdapter mAdapter;
	private ArrayList<Item> mList;
	public Button start_backup_btn;
	public ImageButton check_btn;
	private String where;
	private TextView title_tv;
	private ProgressDialog dialog;
	

	private final int CHECK_ON = R.drawable.btn_check_on_pressed;
	private final int CHECK_OFF = R.drawable.btn_check_off;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backup);
		Intent intent = getIntent();
		where = intent.getStringExtra("where");

		init();
	}

	private void init()
	{
		// TODO Auto-generated method stub
		listView = (ListView) findViewById(R.id.listview_backup);
		start_backup_btn = (Button) findViewById(R.id.start_buckup);
		check_btn = (ImageButton)findViewById(R.id.check_btn);
		title_tv = (TextView) findViewById(R.id.title_tv);

		title_tv.setText("���ݵ�" + where);
		/******************************************************************************************/
		start_backup_btn.setOnClickListener(listener);
		check_btn.setOnClickListener(check_listener);
		check_btn.setTag(new Integer(CHECK_ON));
		/************************************ listView��� *******************************************************/
		String name_array[] =
		{ "��ϵ��", "����" };
		int header_array[] =
		{ R.drawable.contacts, R.drawable.sms };
		mList = new ArrayList<Item>();
		for (int i = 0; i < header_array.length; i++)
		{
			Item item = new Item(header_array[i], name_array[i], CHECK_ON);
			mList.add(item);
		}
		mAdapter = new BackupAdapter(this, mList);
		listView.setAdapter(mAdapter);

		/********************************* service��� **********************************************************/

		Intent intent = new Intent();// ���ݸ��·���
		intent.setClass(BackupActivity.this, BackupService.class);
		bindService(intent, connection, Service.BIND_AUTO_CREATE);
		/********************************* ����״̬��� **********************************************************/
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.idwtwt.restore.STATUES_REFRESH");
		
		BackupReceiver receiver = new BackupReceiver();
		
		registerReceiver(receiver, filter);
	}

	private OnClickListener listener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{

			if (isRunning)
			{
				
				
				boolean contacts_action = false;
				boolean sms_action = false;
				boolean net_action = false;
				dialog = ProgressDialog.show(BackupActivity.this, "��������", "������ϵ������...");
				if (mList.get(0).getCheck() == CHECK_ON)// ѡ�б�����ϵ��
				{
					
					contacts_action = true;
				}
				if (mList.get(1).getCheck() == CHECK_ON)// ѡ�б��ݶ���
				{	
					sms_action = true;
				}
				if (where.equals("�ƶ�"))// ���ݵ��ƶ�
				{
					
					net_action = true;
					
				}
				
				binder.getService().backupAction(contacts_action, sms_action, net_action);
			} else
			{
				Toast.makeText(BackupActivity.this, "��������ʧ��", Toast.LENGTH_SHORT).show();
			}

		}
	};
	//ʵ�ֵ��titlebar�ϵ�check��ť�ı������б�item��״̬
	private OnClickListener check_listener = new OnClickListener()
	{
		
		@Override
		public void onClick(View v)
		{
			int statues = -1;
			statues = ((Integer)check_btn.getTag()).intValue();
			if (statues == CHECK_OFF)
			{
				check_btn.setImageResource(R.drawable.btn_check_on_default);
				check_btn.setTag(new Integer(CHECK_ON));
				for (int i = 0; i < mList.size(); i++)
				{
					mList.get(i).check = CHECK_ON;
				}
				start_backup_btn.setEnabled(true);
			}else {
				
				check_btn.setImageResource(R.drawable.btn_check_off_default);
				check_btn.setTag(new Integer(CHECK_OFF));
				for (int i = 0; i < mList.size(); i++)
				{
					mList.get(i).check = CHECK_OFF;
				}
				start_backup_btn.setEnabled(false);
			}
			mAdapter.notifyDataSetChanged();
			
		}
	};

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	public class BackupReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			switch (intent.getIntExtra("type", -1))
			{
			
			case MESSAGE_TYPE_STATUES_CONTACT_DONE:
				dialog.dismiss();
				dialog = ProgressDialog.show(BackupActivity.this, "��������", "���ݶ�������...");
				//System.out.println("��ʼ���ݶ���");
				break;
			case MESSAGE_TYPE_STATUES_SMS_DONE:
				dialog.dismiss();
				if (where.equals("�ƶ�"))// ���ݵ��ƶ�
				{
					dialog = ProgressDialog.show(BackupActivity.this, "��������", "�ϴ����ƶ�...");
					
				}
				else {
					Toast.makeText(BackupActivity.this,"�����������", Toast.LENGTH_SHORT).show();
				}
				
				
				break;
			case MESSAGE_TYPE_STATUES_NET_DONE:
				
				dialog.dismiss();
				Toast.makeText(BackupActivity.this,"�����������", Toast.LENGTH_SHORT).show();
				
				break;
			case MESSAGE_TYPE_STATUES_NET_ERRO:
				
				dialog.dismiss();
				Toast.makeText(BackupActivity.this,"�����쳣", Toast.LENGTH_SHORT).show();
				
				break;
			}
			
		}
		
	}

}
