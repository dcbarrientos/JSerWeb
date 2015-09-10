/************************************************************************************
 *   Copyright
 * 
 *   JSerWeb Copyright 2006 Diego C. Barrientos, dc_barrientos@yahoo.com.ar  
 *	
 *   This program is free software; you can redistribute it and/or modify it 
 *   under the terms of the GNU General Public License as published by the Free 
 *   Software Foundation; either version 2 of the License, or (at your option) 
 *   any later version.
 *	
 *   This program is distributed in the hope that it will be useful, but WITHOUT 
 *   ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more 
 *   details.
 * 	
 *   You should have received a copy of the GNU General Public Licence along with 
 *   this program, if not, write to de Free Software 
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. 
 *************************************************************************************/


function getDate()
{
	var date = new Date();
	var month = new Array("January", "February", "March", "April", "May", "June", 
		"July", "August", "September", "October", "November", "December");
	var day = new Array("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", 
		"Friday", "Saturday");
	return (day[date.getDay()] + ", " + month[date.getMonth()] + " " + 
	date.getDate() + ", " + (date.getYear()<1900?date.getYear()+1900:date.getYear()) );
}

function mouseOver(src) {
	if (!src.contains(event.fromElement)) {
		src.style.cursor = 'hand';
		src.bgColor = '#D4D4D4';
		src.style.border = 'solid #666666 1px';
	}
}

function mouseOut(src) {
	if (!src.contains(event.toElement)) {
		src.style.cursor = 'default';
		src.bgColor = '#DFDFDF';
		src.style.border = 'solid #DFDFDF 1px';
	}
}

function mouseClick(src) {
	if(event.srcElement.tagName=='TD'){		
		src.children.tags('a')[0].click();
	}
}
