<?php
/*****************************************************************
COPYRIGHT (C):    1995, All Rights Reserved. 
PROJECT:          CS 46A Homework #4 
FILE:             wm.cpp 
PURPOSE:          widget manipulation 
COMPILER:         PHP 
TARGET:           IBM PC/DOS 
PROGRAMMER:       Diego Barrientos (DB) 
START DATE:       6/11/95 
*****************************************************************/ 

	if(getenv("REQUEST_METHOD") == "GET"){
		if (!empty($HTTP_GET_VARS)) {
			while(list($name, $value) = each($HTTP_GET_VARS)) {
				$$name = $value;
			}
		}
	}else{
		if (!empty($HTTP_POST_VARS)) {
			while(list($name, $value) = each($HTTP_POST_VARS)) {
				$$name = $value;
			}
		}
	}

	$method=getenv("REQUEST_METHOD");
	include("testphpresult.html");
?>