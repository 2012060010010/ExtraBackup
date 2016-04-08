package com.idwtwt.start;

import com.idwtwt.extrabackup.R;
import com.idwtwt.main.MainActivity;
import com.idwtwt.setting.SettingActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class StartupActivity extends Activity
{

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup);
		
		new Thread()
		{

			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
					Intent intent = new Intent();
					intent.setClass(StartupActivity.this, MainActivity.class);
					startActivity(intent);
				
				finish();
			}
			
		}.start();
	}



	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		
	}

}
