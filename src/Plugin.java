/***************************************************************************
 * JSerWeb web server Copyright (c) 2006                                   *
 ***************************************************************************
 *   Title.......: HttpPlugin.java                                         *
 *   Description.: <describe the HttpPlugin class here>                    *
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

public abstract class Plugin 
{
	public abstract boolean process(Transaction transaction);
}


/* @(#)HttpPlugin.java  */
