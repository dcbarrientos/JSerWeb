/***************************************************************************
 * JSerWeb web server Copyright (c) 2006                                   *
 ***************************************************************************
 *   Title.......: SerWeb.java                                             *
 *   Description.: <describe the SerWeb class here>                        *
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

public class SerWeb {

	public static void main(String argv[])
	{
		Config config = new Config();
		String vhostFile = "conf/vhosts.conf";
		String confFile = "conf/serweb.conf";
		String typeFile = "conf/mimetypes.conf";
		String errorFile = "conf/errors.conf";

		int cargv = argv.length;
		int i = 0;

		while(i<cargv){
			if(argv[i].equals("-c")){
				i++;
				vhostFile = argv[i];
			}else if(argv[i].equals("-e")){
				i++;
				errorFile = argv[i];
			}else if(argv[i].equals("-h")){
				help();
				System.exit(0);
			}else if(argv[i].equals("-s")){
				i++;
				confFile = argv[i];
			}else if(argv[i].equals("-t")){
				i++;
				typeFile = argv[i];
			}else if(argv[i].equals("-v")){
				printVersion(config);
				System.exit(0);
			}else{
				System.out.println("Error in argument " + i + ": no argument for option " + argv[i]);
				help();
				System.exit(0);
			}
			
			i++;
		}
		
		if(config.load(confFile, vhostFile, typeFile, errorFile))
		{	Server server = new Server(config);
			server.run();
		}else
			System.exit(0);
	}
	
	public static void help()
	{
		System.out.println("Usage: SerWeb [-c file] [-e file] [-h] [-s file] [-t file] [-v]");
		System.out.println("  -c file: specify an alternate virtual-hosts file");
		System.out.println("  -e file: specify an alternate errors file");
		System.out.println("  -h     : list available command lint options.");
		System.out.println("  -s file: specify an alternate server configuration file");
		System.out.println("  -t file: specify an alternate type file");
		System.out.println("  -v     : show version number");
		System.out.println("");
	}
	
	public static void printVersion(Config config)
	{
		System.out.println(config.getVersion());
		System.out.println("Copyright (c) 2006 " + config.AUTHOR + " <" + config.AUTHOR_MAIL + ">\n");				
		System.out.println("This software comes with ABSOLUTELY NO WARRANTY. This is free software,");
		System.out.println("and you are welcome to modify and redistribute it under the GNU General");
		System.out.println("Public License, which may be found in the file LICENSE.");
	}
}


/* @(#)SerWeb.java */
