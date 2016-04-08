package com.idwtwt.net;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeUtility;
import javax.mail.search.BodyTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import com.sun.mail.handlers.multipart_mixed;
import com.sun.mail.imap.IMAPFolder;

import android.R.integer;

public class DownloadMail
{

	private String host;
	private String user;
	private String password;
	private String protocal;

	public DownloadMail(String host, String user, String password,String protocal)
	{
		this.protocal = protocal;
		this.host = host;
		this.user = user;
		this.password = password;
	}

	public ArrayList<String> getList() throws Exception
	{
		
		Properties prop = new Properties();
		prop.setProperty("mail.store.protocol", protocal);

//		prop.setProperty("mail.imap.auth.login.disable", "true");

		Session session = Session.getInstance(prop,null);
//		session.setDebug(true);
		ArrayList<String> items = new ArrayList<String>();// 邮件附件字符串数组

		Store store = session.getStore();
		System.out.println(user +":" +password + ":" + host);
		store.connect(host, user, password);
		
		System.out.println(" get list done");
		Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_ONLY);

		SearchTerm searchTerm = new SubjectTerm("ExtraBackup");// 构造搜索条件
		
		Message message[] = folder.search(searchTerm);// 搜索备份文件邮件
		for (int i = 0; i < message.length; i++)
		{

			Multipart multipart = (Multipart) message[i].getContent();
//			System.out.println("内容数目：" + multipart.getCount());

			BodyPart textPart = multipart.getBodyPart(0); // 获取正文内容
			String text = textPart.getContent().toString();
			items.add(text);
//			System.out.println(text);

		}

		folder.close(true);
		store.close();
		
		return items;

	}

	//
	public boolean getContent(String name) throws Exception
	{
		/*------------------------------判断是否有SD卡-------------------------------------------------------*/
		// Is there any SD?
		boolean sdExist = android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());

		if (!sdExist)
		{// 若是不存在,

			return false;
		}

		String filePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ExtraBackup/";
		/*-------------------------------------------------------------------------------------*/
		Properties prop = new Properties();
		prop.setProperty("mail.store.protocol", "imap");
		prop.setProperty("mail.imap.host", host);
		prop.setProperty("mail.imap.auth.login.disable", "true");

		Session session = Session.getInstance(prop,null);
		session.setDebug(true);
		Store store = session.getStore();

		store.connect(user, password);
		IMAPFolder folder = (IMAPFolder)store.getFolder("INBOX");
		folder.open(IMAPFolder.READ_WRITE);

		SearchTerm searchTerm = new BodyTerm(name);// 构造搜索条件

		Message message[] = folder.search(searchTerm);// 搜索备份文件邮件

		for (int i = 0; i < message.length; i++)
		{

			Multipart multipart = (Multipart) message[i].getContent();
		//	System.out.println("内容数目：" + multipart.getCount());

		//	BodyPart textPart = multipart.getBodyPart(0); // 获取正文内容
		
		//	String text = textPart.getContent().toString();
			
			BodyPart extraPart = multipart.getBodyPart(1);//获取附件
			InputStream is = extraPart.getInputStream();
			FileOutputStream os = new FileOutputStream(filePath + name);
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = is.read(buffer)) != -1)
			{
				os.write(buffer, 0, len);

			}

			if (os != null)
				os.close();
			if (is != null)
				is.close();

			System.out.println(" get file done");

		}
		folder.close(true);
		store.close();
		

		return true;
	}

	public boolean delete(String name) throws Exception
	{
		Properties prop = new Properties();
		prop.setProperty("mail.store.protocol", "imap");
		prop.setProperty("mail.imap.host", host);
		prop.setProperty("mail.imap.auth.plain.disable","true");
		prop.setProperty("mail.imap.auth.login.disable", "true");

		Session session = Session.getInstance(prop,new  MyAuthenricator(user,password)); 
	
		Store store = session.getStore();

		store.connect(user, password);
		IMAPFolder folder = (IMAPFolder)store.getFolder("INBOX");
		folder.open(Folder.READ_WRITE);
		
		 
		SearchTerm searchTerm = new BodyTerm(name);// 构造搜索条件

		Message message[] = folder.search(searchTerm);// 搜索备份文件邮件
		System.out.println("send del begin");
		
		for (int i = 0; i < message.length; i++)//
		{
			
				
				message[i].setFlag(Flags.Flag.DELETED, true);// 删除标记
				
				message[i].saveChanges();// 保存设置
				System.out.println("send del done");
			
		}
		folder.close(true);
		store.close();
		
		return true;
	}
	
	static class MyAuthenricator extends Authenticator{
		String user=null;
		String pass="";
		public MyAuthenricator(String user,String pass){
			this.user=user;
			this.pass=pass;
		}
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user,pass);
		}
		
	}
	


}
