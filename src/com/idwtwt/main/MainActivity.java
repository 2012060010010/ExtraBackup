package com.idwtwt.main;
import com.idwtwt.extrabackup.R;
import com.idwtwt.setting.SettingActivity;
import com.idwtwt.start.UserDialog;

import android.os.Bundle;

import android.app.Activity;

import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.SharedPreferences;

import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;


public class MainActivity extends Activity
{

	private FinishReceiver finishReceiver;
	private SharedPreferences preferences;
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
	
	private Button vdisk_login;
	private Button vdisk_logout;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		init();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onPause()
	{
		
		super.onPause();
		
		
	}

	@Override
	protected void onResume()
	{
		
		super.onResume();
		user = preferences.getString("user", "");
		password = preferences.getString("password", "");
		host = preferences.getString("host", "");
		if (user.equals("") || password.equals("") || host.equals("") )
		{
			startActivity(dialogIntent);
			
			
		}
		
		above_content_normal.setVisibility(View.VISIBLE);
		above_content_press.setVisibility(View.INVISIBLE);
		below_content_normal.setVisibility(View.VISIBLE);
		below_content_press.setVisibility(View.INVISIBLE);
		
		
	}

	public void init()
	{
		preferences = getSharedPreferences("ExtraBackup", MODE_PRIVATE);
		
		
		
		dialogIntent = new Intent();
		dialogIntent.setClass(MainActivity.this, UserDialog.class);
		
		
		
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
		vdisk_login = (Button)findViewById(R.id.vdisk_login);
		//vdisk_logout = (Button)findViewById(R.id.vdisk_logout);

		backup_btn.setOnClickListener(listener);
		restore_btn.setOnClickListener(listener);
		
		
		
		backup_local_btn.setOnClickListener(listener);
		backup_cloud_btn.setOnClickListener(listener);
		restore_local_btn.setOnClickListener(listener);
		restore_cloud_btn.setOnClickListener(listener);
		
		setting_btn.setOnClickListener(listener);
		vdisk_login.setOnClickListener(listener);
		vdisk_logout.setOnClickListener(listener);
		
		
		
		finishReceiver = new FinishReceiver();
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.idwtwt.command.finish");
		registerReceiver(finishReceiver, intentFilter);
		
		
		
		
	}
	
	@Override
	protected void onStart()
	{
		
		super.onStart();
	}

	@Override
	protected void onDestroy()
	{
		unregisterReceiver(finishReceiver);
		
		super.onDestroy();
		
	}

	private OnClickListener listener = new OnClickListener()
	{
		
		
		
		@Override
		public void onClick(View v)
		{
			
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
			
				above_content_normal.setVisibility(View.INVISIBLE);
				above_content_press.setVisibility(View.VISIBLE);
				
				below_content_normal.setVisibility(View.VISIBLE);
				below_content_press.setVisibility(View.INVISIBLE);
				
				
				break;
			
			case R.id.restore:
				
				below_content_normal.setVisibility(View.INVISIBLE);
				below_content_press.setVisibility(View.VISIBLE);
				
				above_content_normal.setVisibility(View.VISIBLE);
				above_content_press.setVisibility(View.INVISIBLE);
				
				break;
			case R.id.backup_local:
				startBackup("SDCard");
				break;	
			case R.id.backup_cloud:
				startBackup("Cloud");
				break;	
			case R.id.restore_local:
				startRestore ("SDCard");
				break;	
			case R.id.restore_cloud:
				startRestore ("Cloud");
				break;	
				
			case R.id.setting_btn:
				set();
				break;
			case R.id.vdisk_login:
				login();
				break;
//			case R.id.vdisk_logout:
//				set();
//				break;
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
	
	protected void login() {
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, com.vdisk.android.backup.OAuthActivity.class);
		startActivity(intent);
	}

	private void startRestore (String where)
	{
		Intent intent = new Intent();
		intent.putExtra("where", where);
		intent.setClass(MainActivity.this, com.idwtwt.restore.RestoreActivity.class);
		startActivity(intent);
	}
	

	private void set()
	{
		
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, SettingActivity.class);
		startActivity(intent);
		
	}

	public class FinishReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			finish();	
		}	
	}
}
