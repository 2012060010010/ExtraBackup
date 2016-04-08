package com.idwtwt.net;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import android.util.Log;

public class UploadMail
{
	private String to;
	private String from;
	private String host;
	private String user;
	private String password;
	private String subject;
	private String text;
	private String content;
	
	public UploadMail()
	{
		
	}
	public UploadMail(String host,String user,String password, String content)
	{
		this.to = user;
		this.from = user;
		this.host = host;
		this.user = user;
		this.password = password;
		this.subject = "ExtraBackup";
		this.content = content; 
		this.text = null;
	}
	public String getTo()
	{
		return to;
	}
	
	public void setTo(String to)
	{
		this.to = to;
	}
	
	public String getFrom()
	{
		return from;
	}
	
	public void setFrom(String from)
	{
		this.from = from;
	}
	
	public String getHost()
	{
		return host;
	}
	
	public void setHost(String host)
	{
		this.host = host;
	}
	public boolean send() throws Exception
	{
		Properties prop = new Properties(); //参数对象
		prop.put("mail.smtp.host", host);
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.transport.protocol", "smtp"); 
		
		Session session = Session.getInstance(prop,null);//会话对象
		
		MimeMessage msg = new MimeMessage(session);
		
			Multipart multipart = new MimeMultipart();   //容器
			MimeBodyPart mbpText = new MimeBodyPart();
			mbpText.setText(content);
			multipart.addBodyPart(mbpText);
			
			MimeBodyPart mbpFile = new MimeBodyPart();
			FileDataSource fileDataSource = new FileDataSource("/mnt/sdcard/ExtraBackup/"+ content);//
			mbpFile.setFileName(MimeUtility.encodeText(content));//设置附件名
			mbpFile.setDataHandler(new DataHandler(fileDataSource));//加载附件
			multipart.addBodyPart(mbpFile);
			
			msg.setFrom(new InternetAddress(from));//实际邮件对象
			msg.setRecipients(Message.RecipientType.TO, to);
			msg.setSubject(subject);
			msg.setContent(multipart);
			//msg.set
			msg.setSentDate(new Date());//设置发信时间
			msg.saveChanges();
			Transport transport = session.getTransport("smtp");//传输对象
			transport.connect(host, user, password);
			transport.sendMessage(msg,msg.getAllRecipients());
			transport.close();
			Log.i("NetService","Success");
			
		

		
		return true;
	}
	
	

}
