/*************************************************************************** 
 * JSerWeb web server Copyright (c) 2006                                   *
 ***************************************************************************
 *   Title.......: Transaction.java                                        *
 *   Description.: <describe the Transaction class here>                   *
 *   Author......: Diego C. Barrientos <dc_barrientos@yahoo.com.ar>        *
 *   Version.....: 1.0	                                                   *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

package SerWeb;

import java.io.*;
import java.lang.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class Transaction extends Thread{
	Config config;
	DataInputStream in;
	DataOutputStream out;
	Socket connection;
	Messages msg;
	Plugin[] plugins;
		
	String httpMethod="";	//Metodo de la peticion	
	String origReq="";		//Peticion original.	
	String httpUrl="";		//Url de la peticion.
	String httpVersion="";
	String httpAccept="";
	String httpLanguage="";
	String httpEncoding="";
	String httpUserAgent="";
	String httpHost="";
	String httpConnection="";
	String httpHostIp="";
	String httpFilePath="";
	String httpFileExt="";
	String httpContentType="";
	String httpInContentType="";
	String httpCurrentDate="";
	String httpFileLength="";
	String httpFileLastModified="";
	String httpRefer="";
	String httpCacheControl="";
	String httpSetCookie="";
	String httpTempFilePath="";
	String httpLocation="";
	String httpCookie="";
	byte[] httpPostQuery;
	int httpContentLength=0;
	int httpStatus=-1;
	String httpQuery="";
	
	Transaction(Config config, Socket socket)
	{
		this.config = config;
		connection = socket;
		plugins = new Plugin[config.nPlugins];
		plugins[0] = new PhpPlugin();
		plugins[1] = new PerlPlugin();
		msg = new Messages(config);
	}
	
	public void run(){
		try
		{	in = new DataInputStream(connection.getInputStream());		
			out = new DataOutputStream(connection.getOutputStream());
		}catch(IOException e)
		{	msg.printErr("Transaction::run:1", e.getMessage());			
		}
		
		try
		{	connection.setSoTimeout(config.httpTimeout*1000);
		}catch(SocketException e)
		{	msg.printErr("Transaction::run 2", e.getMessage());
		}
		
		if(!getHttpRequest()){
			//Agregar corte por mala dirección.
		}
		httpHostIp=getHostIp(httpHost);		
		httpFilePath = getFilePath(httpUrl);
		httpStatus = 200;
		httpCurrentDate = config.getFormatDate(new Date());
		
		if(!fileExist(httpFilePath)){
			if(isDirectory(httpFilePath)){
				httpFilePath = getIndexPage();
			}else{
				writeErrorLog(httpHostIp, getErrorTxt("File not found."));
				httpStatus = 404;
			}
		}
		
		boolean runplugin = false;
		httpFileExt = getFileExt(httpFilePath);
		httpInContentType = httpContentType;
		httpContentType = config.getMimeType(httpFileExt);

		if(httpStatus == 200){
			int j=0;
			while(j<config.nPlugins && !runplugin){
				runplugin = plugins[j].process(this);
				j++;
			}
			
			if(!runplugin)	
				httpTempFilePath = httpFilePath;
		}
		if(httpStatus == 500)
			internalError();
		else if(httpStatus == 404)		
			notFound();

		httpTempFilePath = httpTempFilePath.replace('/', config.FILE_SEPARATOR);
		httpFileLength = Integer.toString(getFileSize(httpTempFilePath));
		sendResponse(httpTempFilePath);
	
		if(runplugin)
			deleteFile(httpTempFilePath);

		writeAccessLog(getStrRequest());
		
		try
		{	connection.close();
		}catch(IOException e)
		{	msg.printErr("Transaction::run:3", e.getMessage());
		}
	}
	
	void deleteFile(String fileName)
	{
		File f = new File(fileName);
		f.delete();
	}
	
	int getFileSize(String fileName)
	{
		File f = new File(fileName);
		return (int)f.length();
	}
	
	String getIndexPage(){
		int i = 0;
		boolean found = false;
		String path="";
		while(i<config.directoryIndex.size() && !found){ 
			path = httpFilePath + config.directoryIndex.get(i);
			if(!fileExist(path))
				i++;
			else
				found=true;
		}

		if(!found){
			notFound();
			path = httpFilePath;
		}
		return path;
	}
	
	boolean getHttpRequest()
	{
		String httpLine="";
		String strKey="";
		String strValue="";
		int i=0;
		
		try{
			httpLine = in.readLine();
		}catch(IOException e){
		}

		origReq=httpLine;
		i = httpLine.indexOf(' ');
		httpMethod = httpLine.substring(0, i).toUpperCase();
		httpLine = httpLine.substring(i+1);		
		
		i = httpLine.indexOf(' ');
		httpUrl = httpLine.substring(0, i);
		httpVersion = httpLine.substring(i+1);
		if((i=httpUrl.indexOf('?'))>0){
			httpQuery = httpUrl.substring(i+1);
			httpUrl = httpUrl.substring(0, i);
		}
		
		while (httpLine != null && httpLine.length()>0)
		{	try
			{	
				httpLine = in.readLine().trim();
				if(httpLine.length()>0){
					i = httpLine.indexOf(":");
					strKey = httpLine.substring(0, i).trim().toLowerCase();
					strValue = httpLine.substring(i+1).trim();
					if(strKey.equals("accept"))
						httpAccept = strValue;
					else if(strKey.equals("accept-language"))
						httpLanguage = strValue;
					else if(strKey.equals("accept-encoding"))
						httpEncoding = strValue;
					else if(strKey.equals("user-agent"))
						httpUserAgent = strValue;
					else if(strKey.equals("host")){		
						if(strValue.indexOf(":")>=0)				
							httpHost = strValue.substring(0, strValue.indexOf(":"));
						else
							httpHost = strValue;
					}else if(strKey.equals("connection"))
						httpConnection = strValue;
					else if(strKey.equals("content-length"))
						httpContentLength = Integer.parseInt(strValue, 10);
					else if(strKey.equals("content-type"))
						httpContentType = strValue;
					else if(strKey.equals("refer"))
						httpRefer = strValue;
					else if(strKey.equals("cache-control"))
						httpCacheControl = strValue;
					else if(strKey.equals("cookie"))
						httpCookie = strValue;
						
				}
			}catch(IOException e)
			{	msg.printErr("Transaction::getHttpRequest:1", e.getMessage());
			}
		}
		
		byte[] others=null;
		if(httpContentLength>0){
			try{
				httpPostQuery = new byte[httpContentLength];
				in.read(httpPostQuery);			
			}catch(IOException e){
				return false;
			}			
		}
		/*
		if(httpContentType.equals("application/x-www-form-urlencoded") 
			&& others.length>0)
		{
			String t = new String(others);
			if(httpQuery.length()>0)
				httpQuery+='&';
				
			httpQuery += t;
		}	
			*/
		return true;
	}
		
	String getUrlPath()
	{
		String urlpath="";
		return urlpath;
	}
	
	String getHostIp(String host)
	{
		String ip="";
		try{
			ip = InetAddress.getByName(host).getHostAddress();
		}catch(UnknownHostException e){
		}
		return ip;
	}
	
	String getFilePath(String url)
	{
		String filePath="";
		vHost v = new vHost();
		v = config.getVHost(httpHostIp);
				
		if(v==null)
			filePath = config.httpDocumentRoot + url;
		else{
			if(v!=null && v.hScriptAliasKey.length()==0){
				filePath = v.hDocumentRoot + url;
			}else if(url.startsWith(v.hScriptAliasKey) && v != null){			
				filePath = v.hScriptAliasValue + "/" + url.substring(v.hScriptAliasKey.length());
			}else{
				filePath = v.hDocumentRoot + url;
			}
		}
					
		filePath = filePath.replace('/', config.FILE_SEPARATOR);

		return filePath;
	}
	
	String getFileExt(String url)
	{
		int i = url.indexOf(".");
		if(i>0)
			return url.substring(i + 1);
			
		return "";
	}
	
	boolean fileExist(String strFile){
		File file = new File(strFile);
		
		if(!file.exists() || !file.isFile())
			return false;

		httpFileLength = Long.toString(file.length(),10);
		httpFileLastModified = config.getFormatDate(new Date(file.lastModified()));
		return true;
	}
	
	
	boolean isDirectory(String path)
	{
		File file = new File(path);
		return file.isDirectory();
	}

	void sendHeader()
	{
		try{
			out.writeBytes(httpVersion +  " " + config.getStatus(httpStatus) + "\r\n");
				
			out.writeBytes("Server: " + config.SERVER_ID + "/" + config.VERSION + " by " + config.AUTHOR + "\r\n");	
			out.writeBytes("Date: " + httpCurrentDate + "\r\n");
			if(httpContentType != null)
				out.writeBytes("Content-Type: " + httpContentType + "\r\n");				
			else
				out.writeBytes("Content-Type: \r\n");				
			out.writeBytes("Connection: close" + "\r\n");
			out.writeBytes("Content-Length: " + httpFileLength + "\r\n");
//			out.writeBytes("Last-Modified: " + httpFileLastModified + "\r\n");
			out.writeBytes("Accept-Ranges: bytes\r\n");
			if(httpLocation.length()>0)
				out.writeBytes("Location: " + httpLocation + "\r\n");
			if(httpSetCookie.length()>0)	{
				out.writeBytes("Set-Cookie: " + httpSetCookie + "\r\n");
			}
			out.writeBytes("\r\n");
			
		}catch(IOException e){
			msg.printErr("Transaction::sendHeader():1", "Error enviando datos");
		}
	}	

	void sendResponse(String fileName)
	{
		int fSize = 0;
		byte[] buffer = new byte[4096];
		try{
			if(httpFileLength.length()==0){			
				fSize = getFileSize(fileName);
				httpFileLength = Integer.toString(fSize);			
			}else
				fSize = Integer.parseInt(httpFileLength, 10);
				
			sendHeader();
			FileInputStream f = new FileInputStream(fileName);
			int x = 0;
			while((x = f.read(buffer))>0)
				out.write(buffer,0,x);
			out.flush();
			f.close();
		}catch(FileNotFoundException e){
			msg.printErr("Transaction::sendResponse():1", "El archivo no existe: " + fileName);
		}catch(IOException e){
//			System.out.println(e.getMessage());
			msg.printErr("Transaction::sendResponse():2", "Error en la lectura del archivo: " + fileName);
		}
		
	}

	void notFound()
	{	
		httpFilePath = config.getErrorFile("404");
//		httpLocation = "http://" + config.httpServerName + config.getErrorFileName("404");

		if(!fileExist(httpFilePath))
			msg.printErr("notFound", "Error en notFound");
	}
	
	void internalError()
	{
		httpFilePath = config.getErrorFile("500");

		if(!fileExist(httpFilePath))
			msg.printErr("notFound", "Error en internalError");
	}
	
	String getStrRequest()
	{
		String ret="";
		ret += "[" + httpHost + "] -- [";
		ret += httpCurrentDate + "] -- " + httpMethod;
		ret += " " + httpUrl + " " + httpVersion + " ";
		ret += config.getStatus(httpStatus);
		return ret;
	}
	
	boolean writeAccessLog(String linea)
	{
//		String fName = config.getAccessLogFile(ip);
		return config.writeFile(config.httpLogRoot + config.httpAccessFile, linea);
	} 
	
	boolean writeErrorLog(String ip, String linea)
	{
		String fName = config.getErrorLogFile(ip);
		
		if(fName.length()>0)
			config.writeFile(fName, linea);			
			
		config.writeFile(config.httpLogRoot + config.httpErrorFile, linea);
		
		return true;
	}
	
	String getErrorTxt(String strError)
	{
		String ret = "[" + config.getFormatDate(new Date())	+ "] - [";
		ret += httpHostIp + "] " + strError + " " + httpFilePath;
		
		return ret;
	}
	
	void printLog()
	{
		System.out.println("-----------------------------------------------------");
		System.out.println(httpVersion +  config.getStatus(httpStatus));
		System.out.println("Server: " + config.SERVER_ID);
		System.out.println("Date: " + httpCurrentDate);
		System.out.println("Content-Type: " + httpContentType);
		System.out.println("Connection: close");
		System.out.println("Content-Length: " + httpFileLength);
		System.out.println("Last-Modified: " + httpFileLastModified);
		System.out.println("SetCookie: " + httpSetCookie);
		System.out.println("Fin-Transaction");
		System.out.println("-----------------------------------------------------");		
	}
}

/* Ejemplo de peticion
GET /phpmyadmin/index.php HTTP/1.1
Accept: application/vnd.ms-excel, application/msword, application/vnd.ms-powerpoint, image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, *'/*
Accept-Language: es-ar
Accept-Encoding: gzip, deflate
User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows 98)
Host: 127.0.0.11
Connection: Keep-Alive

Ejemplo de respuesta
Server: SimpleWeb-Webserver/1.00
Date: Sat, 08 Apr 2006 22:38:56 GMT
Content-Type: image/png
Connection: close
Content-Length: 1621
Last-Modified: Sat, 16 Aug 2003 23:43:16 GMT

*/
/*
HTTP start Thread[Thread-2,5,main]
method:GET
uri:/image1.png
version:1.1
path:/image1.png
query:
file path:webdocs\image1.png
queryX:

request header:
Accept: *\/*
Accept-Language: es-ar
Accept-Encoding: gzip, deflate
If-Modified-Since: Sat, 16 Aug 2003 23:43:16 GMT
User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows 98)
Host: localhost
Connection: Keep-Alive


response header:
Server: SimpleWeb-Webserver/1.00
Date: Sat, 08 Apr 2006 22:38:56 GMT
Content-Type: image/png
Connection: close
Content-Length: 1621
Last-Modified: Sat, 16 Aug 2003 23:43:16 GMT


pido song_storms.rar
HTTP/1.1 200 OK
Date: Fri, 12 May 2006 17:12:37 GMT
Server: Apache/1.3.22 (Win32) PHP/4.0.6
Keep-Alive: timeout=15, max=100
Connection: Keep-Alive
Transfer-Encoding: chunked
Content-Type: text/html


pido: http://127.0.0.16/index.php
-------------------------------------
GET /index.php HTTP/1.1
Accept: application/vnd.ms-excel, application/msword, application/vnd.ms-powerpo
int, image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwav
e-flash, *\/*
Accept-Language: es-ar
Accept-Encoding: gzip, deflate
User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows 98)
Host: 127.0.0.16
Connection: Keep-Alive
Cookie: serweb=35

*/

/* @(#)Transaction.java */
