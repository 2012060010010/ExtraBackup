package com.idwtwt.main;



import com.idwtwt.backup.BackupService;
import com.idwtwt.extrabackup.R;
import com.idwtwt.setting.SettingActivity;
import com.idwtwt.start.UserDialog;

import android.os.Bundle;
import android.os.IBinder;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity
{

	private FinishReceiver finishReceiver;
	
	private SharedPreferences preferences; //配置信息
	private SharedPreferences.Editor editor;
	
	private Intent dialogIntent;
	
	
	private String host;
	private String user;
	private String password;
	
	private RelativeLayout above_content_normal;
	private RelativeLayout below_content_normal;
	private RelativeLayout above_content_press;
	private RelativeLayout below_content_press;
	
	
	
	private ImageButton backup_btn;
	private ImageButton restore_btn;
	private ImageButton backup_local_btn;
	private ImageButton backup_cloud_btn;
	private ImageButton restore_local_btn;
	private ImageButton restore_cloud_btn;
	private ImageButton setting_btn;
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);//自定义主题
		setContentView(R.layout.main);
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);//设置自定义标题栏
		
		init();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		
		
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		user = preferences.getString("user", "");
		password = preferences.getString("password", "");
		host = preferences.getString("host", "");
		if (user.equals("") || password.equals("") || host.equals("") )
		{
			startActivity(dialogIntent);
			
			
		}
		
		//主界面恢复原状
		above_content_normal.setVisibility(View.VISIBLE);
		above_content_press.setVisibility(View.INVISIBLE);
		below_content_normal.setVisibility(View.VISIBLE);
		below_content_press.setVisibility(View.INVISIBLE);
		
		
	}

	public void init()
	{
		preferences = getSharedPreferences("ExtraBackup", MODE_PRIVATE);
		//editor = preferences.edit();
		
		
		dialogIntent = new Intent();
		dialogIntent.setClass(MainActivity.this, UserDialog.class);
		
		
		//控制图层的显示/隐藏
		above_content_normal = (RelativeLayout)findViewById(R.id.above_content_normal);
		below_content_normal = (RelativeLayout)findViewById(R.id.below_content_normal);
		above_content_press = (RelativeLayout)findViewById(R.id.above_content_press);
		below_content_press = (RelativeLayout)findViewById(R.id.below_content_press);
		
		
		backup_btn = (ImageButton)findViewById(R.id.backup);
		restore_btn = (ImageButton)findViewById(R.id.restore);
		
		
		backup_local_btn = (ImageButton)findViewById(R.id.backup_local);
		backup_cloud_btn = (ImageButton)findViewById(R.id.backup_cloud);
		restore_local_btn = (ImageButton)findViewById(R.id.restore_local);
		restore_cloud_btn = (ImageButton)findViewById(R.id.restore_cloud);
		
		setting_btn = (ImageButton)findViewById(R.id.setting_btn);
		

		backup_btn.setOnClickListener(listener);
		restore_btn.setOnClickListener(listener);
		
		
		
		backup_local_btn.setOnClickListener(listener);
		backup_cloud_btn.setOnClickListener(listener);
		restore_local_btn.setOnClickListener(listener);
		restore_cloud_btn.setOnClickListener(listener);
		
		setting_btn.setOnClickListener(listener);
		
		
		
		
		finishReceiver = new FinishReceiver();
		
		IntentFilter intentFilter = new IntentFilter();//注册广播接收，当从设置界面关闭时，配置信息为空，关闭主界面
		intentFilter.addAction("com.idwtwt.command.finish");
		registerReceiver(finishReceiver, intentFilter);
		
		
		
		
	}
	
	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onDestroy()
	{
		unregisterReceiver(finishReceiver);
		// TODO Auto-generated method stub
		super.onDestroy();
		
	}

	private OnClickListener listener = new OnClickListener()
	{
		
		
		
		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			
			user = preferences.getString("user", null);
			password = preferences.getString("password", null);
			host = preferences.getString("host", null);
			if (user == "" || password == "" || host == "" )
			{
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, UserDialog.class);
				startActivity(intent);
				
			}
			
			switch (v.getId())
			{
			case R.id.backup:
				//binder.getService().backupSMStoSD();
				//显示备份选项
				above_content_normal.setVisibility(View.INVISIBLE);
				above_content_press.setVisibility(View.VISIBLE);
				
				below_content_normal.setVisibility(View.VISIBLE);
				below_content_press.setVisibility(View.INVISIBLE);
				
				
				break;
			
			case R.id.restore:
				//显示恢复选项
				below_content_normal.setVisibility(View.INVISIBLE);
				below_content_press.setVisibility(View.VISIBLE);
				
				above_content_normal.setVisibility(View.VISIBLE);
				above_content_press.setVisibility(View.INVISIBLE);
				//binder.getService().recoverContactFromSD("B-2013-12-15-01-00-02.db");
				
				break;
			case R.id.backup_local:
				startBackup("SD卡");
				break;	
			case R.id.backup_cloud:
				startBackup("云端");
				break;	
			case R.id.restore_local:
				startRestore ("SD卡");
				break;	
			case R.id.restore_cloud:
				startRestore ("云端");
				break;	
				
			case R.id.setting_btn:
				set();
				break;

			default:
				break;
			}
			
		}
		
		

		
		
	};
	private void startBackup(String where)
	{
		Intent intent = new Intent();
		intent.putExtra("where", where);
		intent.setClass(MainActivity.this, com.idwtwt.backup.BackupActivity.class);
		startActivity(intent);
	}
	
	private void startRestore (String where)
	{
		Intent intent = new Intent();
		intent.putExtra("where", where);
		intent.setClass(MainActivity.this, com.idwtwt.restore.RestoreActivity.class);
		startActivity(intent);
	}
	
//	private void updateData()
//	{
//		// TODO Auto-generated method stub
//		
//		if(isRunning == true)
//		{
//			binder.getService().send();
//			Log.i("ExtraBackup","OK");
//			
//		}
//		
//		
//	}
//	
//	private void recoverData()
//	{
//		// TODO Auto-generated method stub
//		if(isRunning == true)
//		{
//			binder.getService().get();
//			
//			
//		}
//		
//		
//	}
//	
	private void set()
	{
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, SettingActivity.class);
		startActivity(intent);
		
	}

	public class FinishReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO Auto-generated method stub
			finish();
			
		}
		
	}


}
