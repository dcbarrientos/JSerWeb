/*************************************************************************** 
 * JSerWeb web server Copyright (c) 2006                                   *
 ***************************************************************************
 *   Title.......: Messages.java                                           *
 *   Description.: <describe the Messages class here>                      *
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

public class Messages {
	Config config;
	
	public Messages(Config c)
	{
		config = c;
	}
	
	public void printErr(String origen, String msg)
	{		
		if(config.DEBUG){
			System.out.println(origen);
			System.out.println("\t" + msg);
		}
	}
	
}


/* @(#)Messages.java  */
