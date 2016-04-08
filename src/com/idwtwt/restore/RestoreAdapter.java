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
		if (convertView == null)//û��item������
		{
			convertView = mInflater.inflate(R.layout.list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.name_tv = (TextView)convertView.findViewById(R.id.name_tv);
			viewHolder.del_ib = (ImageButton)convertView.findViewById(R.id.delete_ib);
			viewHolder.sel_iv = (ImageView)convertView.findViewById(R.id.select_iv);
			convertView.setTag(viewHolder);//converViewΪ�գ������ݷŵ���ӵ�converView
		}
		else 
		{
			viewHolder = (ViewHolder)convertView.getTag();//ȡ������
		}
		viewHolder.name_tv.setText(item.getName());//��Ӧ����		
		viewHolder.sel_iv.setImageResource(item.getSel_logo());//��Ӧѡ��
		
		viewHolder.sel_iv.setTag(new Integer(position));
		viewHolder.sel_iv.setOnClickListener(listener);
		
		viewHolder.del_ib.setTag(new Integer(position));
		viewHolder.del_ib.setOnClickListener(del_listener);
		
		return convertView;
	}

	private OnClickListener listener = new OnClickListener()//ѡ�������
	{
		
		@Override
		public void onClick(View v)
		{
			
			/*****************************ʵ�ֱ����ļ���ѡ*******************************************/
			if(selectedView != null)//�����Ϊ�գ�˵���Ǹ�ѡ��Ϊ����Ҫ��ǰ��ѡ�е����ˣ��ŵ�����ѡ�е�
			{
				mList.get(selected).setSel_logo(R.drawable.select_off);//mode��
				selectedView.setImageResource(R.drawable.select_off);//view��
			}
			else {
				activity.start_restore_btn.setEnabled(true);//�б����ļ���ѡ��,��ֻ�մ򿪽������õ�����ô��
			}
			
			//���Ϊ�գ�˵���ǵ�һ��ѡ��û����ν�ĸ�ѡ��ֱ�ӵ���ѡ�еľ�����
			selected =((Integer)v.getTag()).intValue();
			selectedView = (ImageView)v;
			((ImageView)v).setImageResource(R.drawable.select_on);//view��
			mList.get(selected).sel_logo = R.drawable.select_on;//mode��
			
			
			
			
			
			
		}
	};
	
	private OnClickListener del_listener = new OnClickListener()//��Ӧɾ����ť
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
