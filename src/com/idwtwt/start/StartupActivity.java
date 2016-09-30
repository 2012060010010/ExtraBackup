package com.idwtwt.start;

import com.idwtwt.extrabackup.R;
import com.idwtwt.main.MainActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartupActivity extends Activity
{

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup);
		
		new Thread()
		{			
			@Override
			public void run()
			{
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
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
	
		super.onResume();
		
	}

}
