/***************************************************************************
 * JSerWeb web server Copyright (c) 2006                                   *
 ***************************************************************************
 *   Title.......: Config.java                                             *
 *   Description.: <describe the Config class here>                        *
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

package ar.com.dcbarrientos.jserweb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.util.Vector;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class Config {
	private int httpPort=80;
	private int httpTimeout=30;
	private String httpServerAdmin="postmaster@diegobar";
	private String httpServerName="localhost";
	private String httpDocumentRoot=getCurrentDirectory()+"/httpdoc";
	private String httpServerRoot=getCurrentDirectory();
	private boolean httpKeepAlive=true;
	private String httpErrorsRoot=getCurrentDirectory()+"httperrors";
	private String httpTempRoot=getCurrentDirectory()+"/temp";
	private String httpLogRoot=getCurrentDirectory()+"/logs";
	private String httpAccessFile="/access.log";
	private String httpErrorFile="/error.log";
	private int nPlugins = 0;//2;
	
	Hashtable<String, vHost> vhosts = new Hashtable<String, vHost>();
	Hashtable<String, String> mime = new Hashtable<String, String>();
	Hashtable<String, String> errors = new Hashtable<String, String>();
	Hashtable<String, String> plugins = new Hashtable<String, String>();
	Vector<String> directoryIndex = new Vector<String>();

	static public final String SERVER_ID = "JSerWeb";
	static public final String AUTHOR = "Diego Barrientos";
	static public final String AUTHOR_MAIL = "dc_barrientos@yahoo.com.ar";
	static public final String VERSION = "1.0";
	static public final boolean DEBUG = false;
	
	static public final String CIERRAVHOST = "CierraVHost";
	static public final char SEPARATOR_1 = ' ';
	static public final char SEPARATOR_2 = '\t';
	static public final char FILE_SEPARATOR = System.getProperties().getProperty("file.separator").charAt(0);
	Messages msg;
	String key;
	String value;
	
	Config()
	{	
		msg = new Messages(this);
	}
	
	public boolean load(String confFile, String hostsFile, String mimeFile, 
		String errFile)
	{	
		if(!loadConfig(confFile))
			return false;
		if(!loadVHost(hostsFile))
			return false;
		if(!loadMime(mimeFile))
			return false;
		if(!loadErrors(errFile))
			return false;
		return true;
	}	
	
	boolean loadConfig(String confFile)
	{
		BufferedReader fileStream;
		
		try {	
			fileStream = new BufferedReader(new FileReader(confFile)); 
			while(getNextRecord(fileStream)){
				if(key.length()>0){
					if(key.equals("port"))
						httpPort = Integer.parseInt(value, 10);
					else if(key.equals("serveradmin"))
						httpServerAdmin = value;
					else if(key.equals("servername"))
						httpServerName = value;
					else if(key.equals("documentroot"))
						httpDocumentRoot = getAbsolutePath(value);
					else if(key.equals("serverroot"))
						httpServerRoot = getAbsolutePath(value);
					else if(key.equals("timeout"))
						httpTimeout = Integer.parseInt(value, 10);
					else if(key.equals("errorsroot"))
						httpErrorsRoot = getAbsolutePath(value);
					else if(key.equals("directoryindex"))
						directoryIndex.add(value);
					else if(key.equals("keepalive")){							
						if(value.toLowerCase().equals("on"))
							httpKeepAlive = true;
						else
							httpKeepAlive = false;					
					}else if(key.equals("loadplugin")){
						int i = value.indexOf(' ');
						plugins.put(value.substring(0, i), value.substring(i+1));
						nPlugins++;
					}else if(key.equals("temproot"))
						httpTempRoot = getAbsolutePath(value);
					else if(key.equals("logroot"))
						httpLogRoot = getAbsolutePath(value);
				}
			}

			fileStream.close();
		}catch(FileNotFoundException e){
			msg.printErr("Config::load():1", e.getMessage());			
			return false;			
		}catch(IOException e){
			msg.printErr("Config::load():2", e.getMessage());
			return false;
		}
		return true;
	}
	
	String getAbsolutePath(String path)
	{
		String nPath = "";
		
		try
		{
			nPath = ((new File(path)).getCanonicalPath());
		}catch(IOException e){
		}
					
		return (nPath);
	}
	
	boolean loadVHost(String hostFile)
	{
		BufferedReader fileStream;
		boolean abierto = false;
		vHost vhost = new vHost();
		String addr="";
		
		try{
			fileStream = new BufferedReader(new FileReader(hostFile));
			while(getNextRecord(fileStream)){
				if(key.length()>0){
					if(key.equals("virtualhost")){
						if(abierto){
							msg.printErr("Config::cargaVHost():1", 
								"Archivo de vhost.conf incorrecto.");
							fileStream.close();
							return false;
						}else{
							vhost = new vHost();
							addr = value;
							abierto=true;
						}
					}else if(key.equals(CIERRAVHOST)){
						if(abierto){
							abierto = false;
							vhosts.put(addr, vhost);
						}else{
							msg.printErr("Config::cargaVHost():2", "Archivo de vhost.conf incorrecto.");
							fileStream.close();
							return false;
						}
					}	else if(key.equals("servername"))
						vhost.hServerName = value;
					else if(key.equals("serveradmin"))
						vhost.hServerAdmin = value;
					else if(key.equals("documentroot"))
						vhost.hDocumentRoot = value;
					else if(key.equals("errorlog"))
						vhost.hErrorLog = value;
					else if(key.equals("scriptalias")){
						vhost.hScriptAliasKey= value.substring(0,value.indexOf(' ')).trim();
						vhost.hScriptAliasValue=value.substring(value.indexOf(' ')+1).trim();
					}
				}				
			}			
			fileStream.close();						
		}catch(FileNotFoundException e){
			msg.printErr("Config::cargaVHost():3", e.getMessage());			
			return false;			
		}catch(IOException e){
			msg.printErr("Config::cargaVHost():4", e.getMessage());
			return false;
		}		
		return true;
	}		

	boolean loadMime(String mimeFile)
	{
		BufferedReader fileStream;

		try{
			fileStream = new BufferedReader(new FileReader(mimeFile));
			while(getNextRecord(fileStream)){
				if(key.length()>0){
					mime.put(key, value);
				}
			}
			fileStream.close();
		}catch(FileNotFoundException e){
			msg.printErr("Config::loadMime():1", e.getMessage());
			return false;
		}catch(IOException e){
			msg.printErr("Config::loadMime():2", e.getMessage());
			return false;
		}
		
		return true;
	}

	boolean loadErrors(String errFile)
	{
		BufferedReader fileStream;
		try{
			fileStream = new BufferedReader(new FileReader(errFile));
			while(getNextRecord(fileStream)){
				if(key.length()>0){
					errors.put(key, value);
				}
			}
			fileStream.close();
		}catch(FileNotFoundException e){
			msg.printErr("Config::loadMime():1", e.getMessage());
			return false;
		}catch(IOException e){
			msg.printErr("Config::loadMime():2", e.getMessage());
			return false;
		}
		
		return true;
	}
	
	boolean getNextRecord(BufferedReader f){
		String strLine="";
		int i=0;
		try{
			strLine = f.readLine();			
			if(strLine == null)
				return false;
			else{
				strLine = strLine.trim();
				if(strLine.startsWith("#")){
					key = "";
					return true;
				}
				else if (strLine.startsWith("<"))
					strLine = strLine.substring(1);
				else if (strLine.endsWith(">")){
					key = CIERRAVHOST;
					return true;
				}
				if((i = indexSeparator(strLine))>0){
					key = strLine.substring(0, i).trim().toLowerCase();
					value = strLine.substring(i+1).trim().toLowerCase();			
				}else
					key = "";
			}
		}catch(IOException e){
			return false;
		}
		
		return true;
	}
	
	int indexSeparator(String text){
		int i = -1;
		if ((i = text.indexOf(SEPARATOR_1))>0)
			return i;
		else if((i = text.indexOf(SEPARATOR_2))>0)
			return i;
		return i;
	}
	
	public vHost getVHost(String key)
	{
		return (vHost)vhosts.get(key);
	}
	
	public String getMimeType(String key)
	{
		return (String)mime.get(key);
	}
	
	public String getErrorFile(String key)
	{
		return (httpErrorsRoot + (String)errors.get(key)).replace('/', FILE_SEPARATOR);
	}

	public String getErrorFileName(String key){
		return (String)(errors.get(key));
	}
	
	public String getErrorLogFile(String key)
	{
		String ret="";
		if(key.equals("127.0.0.1")){
			ret = getLogsDir() + "/" + "localhost.log";
		}else{
			vHost v = (vHost)vhosts.get(key);
			if(v!=null)
				ret = (getLogsDir() + "/" + ((String)v.hErrorLog)).replace('/', FILE_SEPARATOR);
		}
		return ret;
	}
	
	public String getPlugin(String key)
	{
		return (String)plugins.get(key);
	}
		
	public String getTmpFile()
	{
		int r = (int)(Math.random()*999);
		int r2 = ((int)System.currentTimeMillis())&0xff;
		int l = (int)(4-Integer.toString(r).length());
		
		String tmpFile = getRandomChar(l);
		tmpFile += Integer.toString(r);

		l = (int)(4-Integer.toString(r2).length());
		tmpFile += getRandomChar(l) + Integer.toString(r2)+".tmp";
		tmpFile = getTmpDir() + FILE_SEPARATOR + tmpFile;
		return (tmpFile);
	}

	String getRandomChar(int numberChar)
	{
		String lstChar = "";
		int i=0;
		int num;
		while(i<numberChar){
			do{
				num = (int)(Math.random()*255);
			}while(num<65 || (num>122 || num<97) || num>122);
			lstChar += (char)(num);;
			i++;
		}
		return lstChar;
	}
		
	String getTmpDir()
	{
		String tmp = httpTempRoot;
		File d = new File(tmp);
		if(!d.exists())
			d.mkdir();
			
		return tmp;
	}

	String getLogsDir()
	{
		String tmp = httpLogRoot;
		File d = new File(tmp);
		if(!d.exists())
			d.mkdir();

		return tmp;
	}
	
	boolean writeFile(String fName, String linea)
	{
		try{
			DataOutputStream f = new DataOutputStream(new FileOutputStream(fName, true));
			f.writeBytes(linea + "\n");
			f.flush();
			f.close();
		}catch(FileNotFoundException e){
			return false;
		}catch(IOException e){
			return false;
		}
		
		return true;
	}

	String getFormatDate(Date d)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return (dateFormat.format(d) + " GMT");
	}

	String getCurrentDirectory()
	{
		return System.getProperties().getProperty("user.dir");
	}

	public String getVersion(){
		String os = System.getProperties().getProperty("os.name") + ":" + System.getProperties().getProperty("os.version");
		return (SERVER_ID + " v" + VERSION + " (" + os + ")");
	}

	public String getPluginVersion(String pluginName){
		String command = getPlugin(pluginName) + " -v";
		String pluginVersion = "";
		Process proc;
		try{
			proc = Runtime.getRuntime().exec(command);
			DataInputStream in = new DataInputStream(proc.getInputStream());
			byte[] buffer = new byte[128];
			int i = in.read(buffer);
			pluginVersion = new String(buffer,0,i).trim();
			if(pluginVersion.indexOf("\n")>0)
				pluginVersion = pluginVersion.substring(0, pluginVersion.indexOf("\n"));
		}catch(IOException e){
			return "";
		}
		return pluginVersion.trim();
	}
				
	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public int getHttpTimeout() {
		return httpTimeout;
	}

	public void setHttpTimeout(int httpTimeout) {
		this.httpTimeout = httpTimeout;
	}

	public String getHttpServerAdmin() {
		return httpServerAdmin;
	}

	public void setHttpServerAdmin(String httpServerAdmin) {
		this.httpServerAdmin = httpServerAdmin;
	}

	public String getHttpServerName() {
		return httpServerName;
	}

	public void setHttpServerName(String httpServerName) {
		this.httpServerName = httpServerName;
	}

	public String getHttpDocumentRoot() {
		return httpDocumentRoot;
	}

	public void setHttpDocumentRoot(String httpDocumentRoot) {
		this.httpDocumentRoot = httpDocumentRoot;
	}

	public String getHttpServerRoot() {
		return httpServerRoot;
	}

	public void setHttpServerRoot(String httpServerRoot) {
		this.httpServerRoot = httpServerRoot;
	}

	public boolean isHttpKeepAlive() {
		return httpKeepAlive;
	}

	public void setHttpKeepAlive(boolean httpKeepAlive) {
		this.httpKeepAlive = httpKeepAlive;
	}

	public String getHttpErrorsRoot() {
		return httpErrorsRoot;
	}

	public void setHttpErrorsRoot(String httpErrorsRoot) {
		this.httpErrorsRoot = httpErrorsRoot;
	}

	public String getHttpTempRoot() {
		return httpTempRoot;
	}

	public void setHttpTempRoot(String httpTempRoot) {
		this.httpTempRoot = httpTempRoot;
	}

	public String getHttpLogRoot() {
		return httpLogRoot;
	}

	public void setHttpLogRoot(String httpLogRoot) {
		this.httpLogRoot = httpLogRoot;
	}

	public String getHttpAccessFile() {
		return httpAccessFile;
	}

	public void setHttpAccessFile(String httpAccessFile) {
		this.httpAccessFile = httpAccessFile;
	}

	public String getHttpErrorFile() {
		return httpErrorFile;
	}

	public void setHttpErrorFile(String httpErrorFile) {
		this.httpErrorFile = httpErrorFile;
	}

	public int getnPlugins() {
		return nPlugins;
	}

	public void setnPlugins(int nPlugins) {
		this.nPlugins = nPlugins;
	}

	String getStatus(int code)
	{
		switch(code)
		{
			case 100: return "100 Continue";
			case 101: return "101 Switching Protocols";
			case 200: return "200 OK";
			case 201: return "201 Created";
			case 202: return "202 Accepted";
			case 203: return "203 Non-Authoritative Information";
			case 204: return "204 No Content";
			case 205: return "205 Reset Content";
			case 206: return "206 Partial Content";
			case 300: return "300 Multiple Choices";
			case 301: return "301 Moved Permanently";
			case 302: return "302 Found";
			case 303: return "303 See Other";
			case 304: return "304 Not Modified";
			case 305: return "305 Use Proxy";
			case 307: return "307 Temporary Redirect";
			case 400: return "400 Bad Request";
			case 401: return "401 Unauthorized";
			case 402: return "402 Payment Required";
			case 403: return "403 Forbidden";
			case 404: return "404 Not Found";
			case 405: return "405 Method Not Allowed";
			case 406: return "406 Not Acceptable";
			case 407: return "407 Proxy Authentication Required";
			case 408: return "408 Request Time-out";
			case 409: return "409 Conflict";
			case 410: return "410 Gone";
			case 411: return "411 Length Required";
			case 412: return "412 Precondition Failed";
			case 413: return "413 Request Entity Too Large";
			case 414: return "414 Request-URI Too Large";
			case 415: return "415 Unsupported Media Type";
			case 416: return "416 Requested range not satisfiable";
			case 417: return "417 Expectation Failed";
			case 500: return "500 Internal Server Error";
			case 501: return "501 Not Implemented";
			case 502: return "502 Bad Gateway";
			case 503: return "503 Service Unavailable";
			case 504: return "504 Gateway Time-out";
			case 505: return "505 HTTP Version not supported";
		}
		return "400 Bad Request";
	}
}

class vHost{
	String hServerName="";
	String hServerAdmin="";
	String hDocumentRoot="";
	String hErrorLog="";
	String hScriptAliasKey="";
	String hScriptAliasValue="";
}

/* @(#)Config.java  */
