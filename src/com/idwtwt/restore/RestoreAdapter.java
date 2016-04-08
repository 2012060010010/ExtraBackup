package com.idwtwt.restore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.mail.internet.NewsAddress;

import com.idwtwt.extrabackup.R;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RestoreAdapter extends BaseAdapter
{

	private LayoutInflater mInflater;
	private ArrayList<ListItem> mList;
	private int selected = -1;
	private ImageView selectedView = null;
	private RestoreActivity activity;
	public RestoreAdapter(Context context, ArrayList<ListItem> list)
	{
		activity = (RestoreActivity)context;
		mList = list;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return mList.size();
	}

	public int getSelected()
	{
		return selected;
	}
	public void setSelected(int selected)
	{
		this.selected = selected;
	}
	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		ListItem item = mList.get(position);
		ViewHolder viewHolder = null;
		if (convertView == null)//没有item被覆盖
		{
			convertView = mInflater.inflate(R.layout.list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.name_tv = (TextView)convertView.findViewById(R.id.name_tv);
			viewHolder.del_ib = (ImageButton)convertView.findViewById(R.id.delete_ib);
			viewHolder.sel_iv = (ImageView)convertView.findViewById(R.id.select_iv);
			convertView.setTag(viewHolder);//converView为空，将数据放到添加到converView
		}
		else 
		{
			viewHolder = (ViewHolder)convertView.getTag();//取出数据
		}
		viewHolder.name_tv.setText(item.getName());//对应文字		
		viewHolder.sel_iv.setImageResource(item.getSel_logo());//对应选中
		
		viewHolder.sel_iv.setTag(new Integer(position));
		viewHolder.sel_iv.setOnClickListener(listener);
		
		viewHolder.del_ib.setTag(new Integer(position));
		viewHolder.del_ib.setOnClickListener(del_listener);
		
		return convertView;
	}

	private OnClickListener listener = new OnClickListener()//选择监听器
	{
		
		@Override
		public void onClick(View v)
		{
			
			/*****************************实现备份文件单选*******************************************/
			if(selectedView != null)//如果不为空，说明是改选行为，需要把前次选中的灭了，才点亮新选中的
			{
				mList.get(selected).setSel_logo(R.drawable.select_off);//mode层
				selectedView.setImageResource(R.drawable.select_off);//view层
			}
			else {
				activity.start_restore_btn.setEnabled(true);//有备份文件被选中,但只刚打开界面是用得着这么做
			}
			
			//如果为空，说明是第一次选择，没有所谓的改选，直接点亮选中的就行了
			selected =((Integer)v.getTag()).intValue();
			selectedView = (ImageView)v;
			((ImageView)v).setImageResource(R.drawable.select_on);//view层
			mList.get(selected).sel_logo = R.drawable.select_on;//mode层
			
			
			
			
			
			
		}
	};
	
	private OnClickListener del_listener = new OnClickListener()//相应删除按钮
	{
		
		@Override
		public void onClick(View v)
		{
			int position = ((Integer)v.getTag()).intValue();
			
			activity.call_delete(position);
		
		}
	};
	static class ViewHolder
	{
		
		public TextView  name_tv;
		public ImageButton del_ib;
		public ImageView sel_iv;
	}
	
	
}
