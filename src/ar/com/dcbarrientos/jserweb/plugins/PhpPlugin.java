/***************************************************************************
 * JSerWeb web server Copyright (c) 2006                                   *
 ***************************************************************************
 *   Title.......: PhpPlugin.java                                          *
 *   Description.: <describe the PhpPlugin class here>                     *
 *   Author......: Diego C. Barrientos <dc_barrientos@yahoo.com.ar>        *
 *   Version.....: 1.0                                                     *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package ar.com.dcbarrientos.jserweb.plugins;

import java.util.Vector;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import ar.com.dcbarrientos.jserweb.Plugin;
import ar.com.dcbarrientos.jserweb.Transaction;
import ar.com.dcbarrientos.jserweb.Messages;
import ar.com.dcbarrientos.jserweb.Config;

public class PhpPlugin extends Plugin 
{
	Messages msg;
	
	public boolean process(Transaction transaction)
	{
		if(!transaction.getHttpFileExt().startsWith("php"))
			return false;
			
		transaction.setHttpStatus(500);
		msg = new Messages(transaction.getConfig());
		
		String[] envVar = getEnvironment(transaction);
		String command = transaction.getConfig().getPlugin("php");
		Process proc;
		try{
			proc = Runtime.getRuntime().exec(command, envVar);
		}catch(IOException e){
			transaction.setHttpStatus(500);
			return false;
		}
			
		BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		BufferedWriter procOut = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));		
		if(proc!=null && transaction.getHttpPostQuery()!=null && transaction.getHttpMethod().equals("POST")){
			try{
				procOut.write(transaction.getHttpPostQuery());
				procOut.flush();
				procOut.close();
			}catch(IOException e){
				transaction.setHttpStatus(500);
				return false;
			}
		}			
		
		String tmpFile = transaction.getConfig().getTmpFile();
		char[] buffer = new char[Config.BUFFER_SIZE];
		if(proc!=null){			
			try{				
				if(!getHeader(in, transaction)){
					transaction.setHttpStatus(500);
					return false;
				}
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile)));

				int r = in.read(buffer);

				while(r!=-1){
					out.write(buffer,0,r);				
					r = in.read(buffer);
				}
				out.flush();			
				out.close();
			}catch(IOException e){
				transaction.setHttpStatus(500);
				msg.printErr("PhpPlugin::process:1", e.getMessage());
			}
		}
		transaction.setHttpTempFilePath(tmpFile);		
		transaction.setHttpStatus(200);
		return true;
	}
	
	
	boolean getHeader(BufferedReader in, Transaction t){
		int i=0;
		String line="";

		try{
			line = in.readLine().toLowerCase();
			while(line.length()>0){
				if(line.indexOf(':')>0){
					if(line.substring(0,line.indexOf(':')).equals("content-type"))
						t.setHttpContentType(line.substring(line.indexOf(':')+1).trim());
					else if(line.substring(0,line.indexOf(':')).equals("set-cookie"))
						t.setHttpSetCookie(line.substring(line.indexOf(':')+1).trim());
						
					i++;
				}
				
				line = in.readLine().toLowerCase();
			}
			
		}catch(IOException e){
			return false;
		}
		
		if(i>0)
			return true;
		else
			return false;
	}
	
	String[] getEnvironment(Transaction t)
	{
		Vector<String> tmp = new Vector<String>();;
		tmp.add("SERVER_SOFTWARE=" + Config.SERVER_ID);
		tmp.add("SERVER_NAME=" + t.getConfig().getHttpServerName());
		tmp.add("GATEWAY_INTERFACE=CGI/1.1");
		tmp.add("SERVER_PROTOCOL=" + t.getHttpVersion());
		tmp.add("SERVER_PORT=" + t.getConfig().getHttpPort());
		tmp.add("REQUEST_METHOD=" + t.getHttpMethod());	
		tmp.add("PATH_INFO=" + t.getHttpUrl());	
		tmp.add("PATH_TRANSLATED=" + t.getHttpFilePath());
		tmp.add("QUERY_STRING=" + t.getHttpQuery());
		tmp.add("REMOTE_ADDR=" + t.getHttpHost());
		tmp.add("REDIRECT_STATUS=1");
		tmp.add("HTTP_ACCEPT=" + t.getHttpAccept());
		tmp.add("HTTP_ACCEPT_LANGUAGE=" + t.getHttpLanguage());
		tmp.add("HTTP_ACCEPT_ENCODING=" + t.getHttpEncoding());
		tmp.add("HTTP_USER_AGENT=" + t.getHttpUserAgent());
		tmp.add("HTTP_HOST=" + t.getHttpHost());
		tmp.add("HTTP_REFER=" + t.getHttpRefer());
		tmp.add("HTTP_CACHE_CONTROL=" + t.getHttpCacheControl());
		if(t.getHttpCookie().length()>0)
			tmp.add("HTTP_COOKIE=" + t.getHttpCookie());
		
		if(t.getHttpContentLength()>0){
			tmp.add("HTTP_CONTENT_TYPE=" + t.getHttpInContentType());
			tmp.add("HTTP_CONTENT_LENGTH=" + Integer.toString(t.getHttpContentLength()));
			tmp.add("CONTENT_TYPE=" + t.getHttpInContentType());
			tmp.add("CONTENT_LENGTH=" + Integer.toString(t.getHttpContentLength()));
		}
		
		if(t.getConfig().isHttpKeepAlive())
			tmp.add("HTTP_CONNECTION=Keep-Alive");
		
		String[] strTmp = new String[tmp.size()];
		tmp.copyInto(strTmp);
		
		return strTmp;
	}
}

/*
X SERVER_SOFTWARE=SimpleWeb-Webserver/1.00
X SERVER_NAME=localhost
X GATEWAY_INTERFACE=CGI/1.1
x SERVER_PROTOCOL=HTTP/1.1
X SERVER_PORT=80
x REQUEST_METHOD=GET
x PATH_INFO=/info.php
x PATH_TRANSLATED=webdocs\info.php
x QUERY_STRING=name=megatherium&iq=5&uber-alles=yes&info=megatherium+uber+alles
x REMOTE_ADDR=127.0.0.1
x REDIRECT_STATUS=1
x HTTP_ACCEPT=application/vnd.ms-excel, application/msword, application/vnd.ms-powerpoint, image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, *\/*
x HTTP_ACCEPT_LANGUAGE=es-ar
HTTP_ACCEPT_ENCODING=gzip, deflate
HTTP_USER_AGENT=Mozilla/4.0 (compatible; MSIE 6.0; Windows 98)
HTTP_HOST=127.0.0.1
HTTP_CONNECTION=Keep-Alive
*/
/* @(#)PhpPlugin.java  */
