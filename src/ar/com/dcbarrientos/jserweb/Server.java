/***************************************************************************
 * JSerWeb web server Copyright (c) 2006                                   *
 ***************************************************************************
 *   Title.......: Server.java                                             *
 *   Description.: <describe the Server class here>                        *
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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Server extends Thread{	
	Config config;
	Messages msg;
		
	Server(Config config)
	{
		this.config = config;
		msg = new Messages(config);
	}
	
	public void run(){
		ServerSocket ser_sock = null;
		Socket cli_sock = null;
		Transaction transaction;
		
		try
		{
			ser_sock = new ServerSocket(config.getHttpPort());		
		}catch(IOException e)
		{	
			msg.printErr("Server::run():1", e.getMessage());		
			String msgErr = "[" + config.getFormatDate(new Date()) + "] ";
			msgErr += "Dirección en uso."; 
			config.writeFile(config.getHttpLogRoot() + config.getHttpErrorFile(), msgErr);
			System.exit(0);
		}

		printHeader();

		while(true)
		{ try
			{	
				cli_sock = ser_sock.accept();			
				transaction = new Transaction(config, cli_sock);
				transaction.start();
			}catch(IOException e)
			{	msg.printErr("Server::run():2", e.getMessage());
				try {
					ser_sock.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	public String getPluginVersion(String strPlugin, String name)
	{
		String version = config.getPluginVersion(strPlugin);
		if (version.length() > 0){
			int x = version.toLowerCase().indexOf(name);
			if(x>=0)
				version = name.toUpperCase() + version.substring(x+name.length(), version.length()-1);
			else
				version = name.toUpperCase() + " v" + version;
		}
		
		return version;
	}
	
	public void printHeader()
	{
		System.out.println("_________________________________________________________________");		
		System.out.println(config.getVersion() + " running...");
		if (getPluginVersion("php", "PHP").length()>0)
			System.out.println("  [  OK  ] " + getPluginVersion("php", "php"));
		else
			System.out.println("  [ FAIL ] Php not found");
			
		if (getPluginVersion("pl", "perl").length()>0)
			System.out.println("  [  OK  ] " + getPluginVersion("pl", "perl"));
		else
			System.out.println("  [ FAIL ] Perl not found");		
			
		System.out.println("_________________________________________________________________");		
	}
}


/* @(#)Server.java  */
