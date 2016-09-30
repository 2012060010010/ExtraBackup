package com.idwtwt.backup;

import android.widget.ImageView;
import android.widget.TextView;

public class Item
{
	public int header;
	public String  name;
	public int check;
	public Item(int header, String  name, int check)
	{
		this.header = header;
		this.name = name;
		this.check = check;
	}
	public int getHeader()
	{
		return header;
	}
	public void setHeader(int header)
	{
		this.header = header;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public int getCheck()
	{
		return check;
	}
	public void setCheck(int check)
	{
		this.check = check;
	}
	
	

}
