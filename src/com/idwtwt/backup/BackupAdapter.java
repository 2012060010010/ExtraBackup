package com.idwtwt.backup;

import java.util.ArrayList;
import com.idwtwt.extrabackup.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BackupAdapter extends BaseAdapter
{

	private LayoutInflater mInflater;
	private ArrayList<Item> mList;
	public BackupActivity activity;
	
	private final int CHECK_ON = R.drawable.btn_check_on_pressed;
	private final int CHECK_OFF = R.drawable.btn_check_off;
	public BackupAdapter(Context context, ArrayList<Item> list)
	{
		activity = (BackupActivity)context;
		mList = list;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount()
	{
	
		return mList.size();
	}

	@Override
	public Object getItem(int position)
	{
	
		return null;
	}

	@Override
	public long getItemId(int position)
	{
	
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		
		Item item = mList.get(position);
		ViewHolder viewHolder = null;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.item, null);
			viewHolder = new ViewHolder();
			viewHolder.header_iv = (ImageView)convertView.findViewById(R.id.header_iv);
			viewHolder.name_tv = (TextView)convertView.findViewById(R.id.name_tv);
			viewHolder.check_iv = (ImageView)convertView.findViewById(R.id.check_iv);
			convertView.setTag(viewHolder);
		}
		else 
		{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		viewHolder.header_iv.setImageResource(item.getHeader());//logo
		viewHolder.name_tv.setText(item.getName());//文件名
		viewHolder.check_iv.setImageResource(item.getCheck());
		
		viewHolder.check_iv.setTag(new StatusOfCheck(item.getCheck(), position));
		viewHolder.check_iv.setOnClickListener(listener);
		
		return convertView;
	}

	private OnClickListener listener = new OnClickListener()
	{
		
		@Override
		public void onClick(View v)
		{
			
			StatusOfCheck statusOfCheck = (StatusOfCheck)v.getTag();
			int position = statusOfCheck.getPosition();//check item
			int status = statusOfCheck.getStatus();//check item、
			
			
			System.out.println("---" + position + ":" + status);
			if(status == CHECK_OFF)
			{
				((ImageView)v).setImageResource(R.drawable.btn_check_on_pressed);
				(mList.get(position)).check = R.drawable.btn_check_on_pressed; 
				statusOfCheck.setStatus(CHECK_ON);//
				
			}
			else {
				
				((ImageView)v).setImageResource(R.drawable.btn_check_off);
				(mList.get(position)).check = R.drawable.btn_check_off;
				statusOfCheck.setStatus(CHECK_OFF);
			}
			
			boolean isHave = false;
			for (int i = 0; i < mList.size(); i++)
			{
				if (mList.get(i).check == R.drawable.btn_check_on_pressed)
				{
					isHave = true;
				}
			}
			
			if (isHave)
			{
				activity.start_backup_btn.setEnabled(true);
				
			}else {
				activity.start_backup_btn.setEnabled(false);
			}
		}
	};
	static class ViewHolder
	{
		public ImageView header_iv;
		public TextView  name_tv;
		public ImageView check_iv;
		
	}
	
	class StatusOfCheck
	{
		public int getStatus()
		{
			return status;
		}
		public void setStatus(int status)
		{
			this.status = status;
		}
		public int getPosition()
		{
			return position;
		}
		public void setPosition(int position)
		{
			this.position = position;
		}
		int status;
		int position;
		public StatusOfCheck(int status, int position)
		{
			this.status = status;
			this.position = position;
			
		}
	}
}
