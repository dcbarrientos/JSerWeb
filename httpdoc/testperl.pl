#!c:\perl\bin\perl.exe
#	*****************************************************************
#	COPYRIGHT (C):    1995, All Rights Reserved. 
#	PROJECT:          CS 46A Homework #4 
#	FILE:             wm.cpp 
#	PURPOSE:          widget manipulation 
#	COMPILER:         PERL
#	TARGET:           IBM PC/DOS 
#	PROGRAMMER:       Diego Barrientos (DB)
#	START DATE:       6/11/95 
#	*****************************************************************
require 'cgi-lib.pl';

getFormData();

printResult();
#showENV();



sub printResult{
$method = $ENV{'REQUEST_METHOD'};

print <<EOF;
Content-type: text/html\r\n\r\n
<html>
<head>
<title>Perl test result</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>

<body bgcolor="#FFFFFF" text="#000000">
<br>
<table width="100%" border="0">
  <tr>
    <td>
      <div align="center">
        <table width="22%" border="0" cellspacing="3" bgcolor="#0099CC" height="63">
          <tr> 
            <td bgcolor="#0099CC"> 
              <div align="center"><font face="Tahoma, sans-serif"><b><font color="#FFFFFF">Data</font></b></font></div>
            </td>
          </tr>
          <tr> 
            <td height="9" bgcolor="#0099CC"> 
              <div align="center"> 
                <table width="100%" border="0" cellspacing="0" cellpadding="3">
                  <tr> 
                    <td width="24%" bgcolor="#FFFFFF"><font face="Tahoma, sans-serif">Method:</font></td>
                    <td width="76%" bgcolor="#FFFFFF"><font face="Tahoma, sans-serif">$method</font></td>
                  </tr>
                  <tr> 
                    <td width="24%" bgcolor="#FFFFFF"><font face="Tahoma, sans-serif">Name:</font></td>
                    <td width="76%" bgcolor="#FFFFFF"><font face="Tahoma, sans-serif">@field{'nombre'} 
                      </font></td>
                  </tr>
                  <tr> 
                    <td width="24%" bgcolor="#FFFFFF"><font face="Tahoma, sans-serif">Surname:</font></td>
                    <td width="76%" bgcolor="#FFFFFF"><font face="Tahoma, sans-serif">@field{'apellido'} 
                      </font></td>
                  </tr>
                  <tr> 
                    <td width="24%" bgcolor="#FFFFFF"><font face="Tahoma, sans-serif">Address:</font></td>
                    <td width="76%" bgcolor="#FFFFFF"><font face="Tahoma, sans-serif">@field{'direccion'}</font></td>
                  </tr>
                  <tr bgcolor="#FFFFFF"> 
                    <td colspan=2 height="2"> </td>
                  </tr>
                </table>
              </div>
            </td>
          </tr>
        </table>
      </div>
    </td>
  </tr>
</table>
</body>
</html>

EOF
;
}
