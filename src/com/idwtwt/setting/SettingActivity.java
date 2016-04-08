package com.idwtwt.setting;

import com.idwtwt.extrabackup.R;
import com.idwtwt.main.MainActivity;
import com.idwtwt.start.StartupActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class SettingActivity extends Activity
{
	private SharedPreferences preferences; //配置信息
	private SharedPreferences.Editor editor; 
	private Intent mainIntent;
	
	
	private String host;
	private String user;
	private String password;
	
	
	private EditText user_edit;
	private EditText password_edit;
	private EditText host_edit;
	private ImageButton save_btn;
	private ImageButton cancel_btn;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		init();
	}
	private void init()
	{
		preferences = getSharedPreferences("ExtraBackup", MODE_PRIVATE);									
		editor = preferences.edit();
		

		mainIntent = new Intent();
		mainIntent.setClass(SettingActivity.this, MainActivity.class);
		
		user = preferences.getString("user", null);
		password = preferences.getString("password", null);
		host = preferences.getString("host", null);
		
		
		user_edit = (EditText)findViewById(R.id.user_edit);
		password_edit = (EditText)findViewById(R.id.password_edit);
		host_edit= (EditText)findViewById(R.id.host_edit);
		save_btn = (ImageButton)findViewById(R.id.save_btn);
		cancel_btn = (ImageButton)findViewById(R.id.cancel_btn);
		user_edit.setText(user);
		password_edit.setText(password);
		host_edit.setText(host);
		
		
		save_btn.setOnClickListener(listener);
		cancel_btn.setOnClickListener(listener);
	}
	
	private OnClickListener listener = new OnClickListener()
	{
		
		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
			case R.id.save_btn:
				save_setting();
				break;
			case R.id.cancel_btn:
				cancel();
				break;

			default:
				break;
			}
			
		}

		

		
	};
	
	private void save_setting()
	{
		user = user_edit.getText().toString().trim();
		password = password_edit.getText().toString().trim();
		host = host_edit.getText().toString().trim();
		
		if (user.equals("") || password.equals("") || host.equals(""))//为空，说明没设置好，强制要求重新设置
		{
			Toast.makeText(SettingActivity.this, "信息未填写完整", Toast.LENGTH_SHORT).show();
			return;
		}
		
		editor.putString("user", user);//设置好后，重新写入数据
		editor.putString("password", password);
		editor.putString("host", host);
		editor.commit();
		
		
		finish();//关闭设置界面
		
	}
	
	private void cancel()
	{
		finish();//关闭设置界面	
	}

}
