/*************************************************************************** 
 * JSerWeb web server Copyright (c) 2006                                   *
 ***************************************************************************
 *   Title.......: PerlPlugin.java                                         *
 *   Description.: <describe the PerlPlugin class here>                    *
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
package SerWeb;

import java.io.*;
import java.util.*;

public class PerlPlugin extends Plugin
{
	Messages msg;
	
	public boolean process(Transaction transaction)
	{
		if(!transaction.httpFileExt.startsWith("pl"))
			return false;
		
		transaction.httpStatus = 500;
		msg = new Messages(transaction.config);
		
		Process proc;
		String command = transaction.config.getPlugin("pl");

		command += " -I" + (new File(transaction.httpFilePath)).getParent() + 
			" " + transaction.httpFilePath;
		String[] envVar = getEnvironment(transaction);
		try{
			proc = Runtime.getRuntime().exec(command, envVar);
		}catch(IOException e){
			transaction.httpStatus = 500;
			return false;
		}
		
		DataInputStream in = new DataInputStream(proc.getInputStream());
		OutputStream procOut = proc.getOutputStream();
		if(proc!=null && transaction.httpPostQuery!=null && transaction.httpMethod.equals("POST")){
			try{
				procOut.write(transaction.httpPostQuery);
				procOut.flush();
				procOut.close();
			}catch(IOException e){
				transaction.httpStatus = 500;
				return false;
			}
		}		
		
		
		String tmpFile = transaction.config.getTmpFile();
		String line = "";
		byte[] buffer = new byte[65536];
		if(proc!=null){			
			try{				
				if(!getHeader(in, transaction)){
					transaction.httpStatus = 500;				
					return false;
				}
				DataOutputStream out = new DataOutputStream(new FileOutputStream(tmpFile));
				
				int r = in.read(buffer);

				while(r!=-1){
					out.write(buffer,0,r);				
					r = in.read(buffer);
				}
				out.flush();			
				out.close();
			}catch(IOException e){
				transaction.httpStatus = 500;
				msg.printErr("PerlPlugin::process:1", e.getMessage());
			}
		}

		transaction.httpTempFilePath = tmpFile;
		transaction.httpStatus = 200;
		return true;
	}
	
	boolean getHeader(DataInputStream in, Transaction t){
		int i=0;
		String line="";
		boolean ret=false;
		t.httpContentType = "";
		t.httpSetCookie = "";
		try{
			line = in.readLine();
			if(line != null){
				line.toLowerCase();
				while(line.length()>0){
					if(line.indexOf(':')>0){
						if(line.substring(0,line.indexOf(':')).equals("content-type"))
							t.httpContentType = line.substring(line.indexOf(':')+1).trim();
						else if(line.substring(0,line.indexOf(':')).equals("set-cookie"))
							t.httpSetCookie = line.substring(line.indexOf(':')+1).trim();
						
						i++;
					}
					
					line = in.readLine().toLowerCase();
				}
			}else
				return false;			
			//line = in.readLine();
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
		Vector tmp = new Vector();;
		tmp.add("SERVER_SOFTWARE=" + t.config.SERVER_ID);
		tmp.add("SERVER_NAME=" + t.config.httpServerName);
		tmp.add("GATEWAY_INTERFACE=CGI/1.1");
		tmp.add("SERVER_PROTOCOL=" + t.httpVersion);
		tmp.add("SERVER_PORT=" + t.config.httpPort);
		tmp.add("REQUEST_METHOD=" + t.httpMethod);	
		tmp.add("PATH_INFO=" + t.httpUrl);	
		tmp.add("PATH_TRANSLATED=" + t.httpFilePath);
		tmp.add("QUERY_STRING=" + t.httpQuery);
		tmp.add("REMOTE_ADDR=" + t.httpHost);
		tmp.add("REDIRECT_STATUS=1");
		tmp.add("HTTP_ACCEPT=" + t.httpAccept);
		tmp.add("HTTP_ACCEPT_LANGUAGE=" + t.httpLanguage);
		tmp.add("HTTP_ACCEPT_ENCODING=" + t.httpEncoding);
		tmp.add("HTTP_USER_AGENT=" + t.httpUserAgent);
		tmp.add("HTTP_HOST=" + t.httpHost);
		tmp.add("HTTP_REFER=" + t.httpRefer);
		tmp.add("HTTP_CACHE_CONTROL=" + t.httpCacheControl);
		tmp.add("CONTENT_TYPE=" + t.httpInContentType);
		if(t.httpCookie.length()>0)
			tmp.add("HTTP_COOKIE=" + t.httpCookie);
			
		if(t.httpContentLength>0){
			tmp.add("HTTP_CONTENT_TYPE=" + t.httpContentType);
			tmp.add("HTTP_CONTENT_LENGTH=" + Integer.toString(t.httpContentLength));
			tmp.add("CONTENT_LENGTH=" + Integer.toString(t.httpContentLength));
		}
	
		
		if(t.config.httpKeepAlive)
			tmp.add("HTTP_CONNECTION=Keep-Alive");
		
		String[] strTmp = new String[tmp.size()];
		tmp.copyInto(strTmp);
		
		return strTmp;
	}	
}


/* @(#)PerlPlugin.java  */
