/*
 * @(#)TELETEXT.java - constants/decode of teletext System B
 *
 * Copyright (c) 2001-2004 by dvb.matt, All Rights Reserved. 
 * 
 * This file is part of X, a free Java based demux utility.
 * X is intended for educational purposes only, as a non-commercial test project.
 * It may not be used otherwise. Most parts are only experimental.
 * 
 *
 * This program is free software; you can redistribute it free of charge
 * and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */


//import java.awt.*;

public final class Teletext
{

private Teletext()
{}

//DM14032004 081.6 int18 changed
//DM05052004 081.7 int02 changed
private final static char[][] ncs = {
	/**** # is original ****/
	//{ '�','$','@','�','�','�','^','#','-','�','�','�','�' },  // westeuropean, polish 000
	{ '�','$','@','Z','\u015A','\u0141','\u0107','\u00F3','\u0119','\u017C','\u015B','\u0142','\u017A' },  // westeuropean, polish unicode
	{ '�','�','�','�','�','�','�','#','�','�','�','�','�' },  // french 001, checked
	{ '#','�','�','�','�','�','�','_','�','�','�','�','�' },  // 010 nor,swe  010, checked
	//{ '#','�','@','t','z','�','�','r','�','�','�','�','s' },  // turkish, czech, greek 011, some wrong w/o unicode
	{ '#','\u016F','\u010D','\u0165','\u017E','�','�','\u0159','�','�','\u011B','�','\u0161' },  // czech, using unicode
	{ '#','$','�','�','�','�','^','_','�','�','�','�','�' },  // german 100, checked
	{ '�','$','@','�','�','�','�','�','�','�','�','�','�' },  // portugal 101
	{ '�','$','�','�','�','�','^','�','�','�','�','�','�' },  // italian, jugoslav 110, checked
	//{ '#','�','T','�','S','�','�','i','t','�','s','�','�' },  // rumanian,english thai 111, some wrong w/o unicode
	{ ' ','�','\u0163','�','\u015E','�','�','i','\u0164','�','\u015F','\u0103','�' },  // rumanian,using unicode

	/**** # is replaced ****/
	//{ '�','$','@','�','�','�','^','#','-','�','�','�','�' },  // westeuropean, polish 000
	{ '�','$','@','Z','\u015A','\u0141','\u0107','\u00F3','\u0119','\u017C','\u015B','\u0142','\u017A' },  // westeuropean, polish unicode
	{ '�','�','�','�','�','�','�',' ','�','�','�','�','�' },  // french  001, checked
	{ ' ','�','�','�','�','�','�','_','�','�','�','�','�' },  // 010 nor,swe 010, checked
	//{ ' ','�','@','t','z','�','�','r','�','�','�','�','s' },  // turkish, czech, greek 011, some wrong w/o unicode
	{ ' ','\u016F','\u010D','\u0165','\u017E','�','�','\u0159','�','�','\u011B','�','\u0161' },  // czech, using unicode
	{ ' ','$','�','�','�','�','^','_','�','�','�','�','�' },  // german 100, checked
	{ '�','$','@','�','�','�','�','�','�','�','�','�','�' },  // portugal 101
	{ '�','$','�','�','�','�','^','�','�','�','�','�','�' },  // italian, jugoslav 110,  checked
	//{ ' ','�','T','�','S','�','�','i','t','�','s','�','�' }  // rumanian,english thai 111, some wrong w/o unicode
	{ ' ','�','\u0163','�','\u015E','�','�','i','\u0164','�','\u015F','\u0103','�' }  // rumanian,using unicode
}; 

private final static String[] ssaHeader = {
	"[Script Info]",
	"; This is a Sub Station Alpha v4 script.",
	"; For Sub Station Alpha info and downloads,",
	"; go to http://www.eswat.demon.co.uk/",
	"; or email kotus@eswat.demon.co.uk",
	"; to burn-in these subtitles into an AVI, just install subtitler2.3 PlugIn for VirtualDub, see doom9.org",
	"Title: Subtitles taken from TV teletext",
	"Original Script: by their respective owner",
	"ScriptType: v4.00",
	"Collisions: Normal",
	"PlayResY: 240",      // maybe replaced [10]
	"PlayDepth: 0", 
	"Timer: 100.0000",    // maybe replaced [12]
	"[V4 Styles]",
	"Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, TertiaryColour, BackColour, Bold, Italic, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, AlphaLevel, Encoding",
	"Style: MainB,Arial,14,&H00FFFF,&H00FFFF,&H00FFFF,0,0,-1,1,2,4,1,16,16,16,0,0",
	"Style: MainT,Arial,14,&HFFFFFF,&HFFFFFF,&HFFFFFF,0,1,0,1,2,4,1,16,16,16,0,0",
	"Style: MainI,Arial,14,&HFFFFFF,&HFFFFFF,&HFFFFFF,0,1,1,1,2,4,1,16,16,16,0,0",   //DM30122003 081.6 int10 add
	"Style: MainC,Courier New,14,&HFFFFFF,&HFFFFFF,&HFFFFFF,0,1,0,1,2,4,1,16,16,16,0,0",
	"[Events]",
	"Format: Marked, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text",
	"Comment: Marked=0,0:00:00.00,0:00:00.01,MainB,,0000,0000,0000,!Effect,This script was created by decoding a tv teletext stream to build coloured subtitles"
};

private final static String[] ssaLine = { 
	"Dialogue: Marked=0,",
	",MainT,,0000,0000,0000,!Effect,{\\q2\\a2}"
};

//DM26052004 081.7 int03 changed
private final static String[] stlHeader = {
	"",
	"//Font select and font size",
	"$FontName   = Arial",
	"$FontSize   = 30",
	"//Character attributes (global)",
	"$Bold    = FALSE",
	"$UnderLined = FALSE",
	"$Italic  = FALSE",
	"//Position Control",
	"$HorzAlign = Center",
	"$VertAlign = Bottom",
	"$XOffset   = 10",
	"$YOffset   = 10",
	"//Contrast Control",
	"$TextContrast        = 15",
	"$Outline1Contrast    = 8",
	"$Outline2Contrast    = 15",
	"$BackgroundContrast  = 0",
	"//Effects Control",
	"$ForceDisplay = FALSE",
	"$FadeIn   = 0",
	"$FadeOut  = 0",
	"//Other Controls",
	"$TapeOffset = FALSE",
	"//Colors",
	"$ColorIndex1 = 0",
	"$ColorIndex2 = 1",
	"$ColorIndex3 = 2",
	"$ColorIndex4 = 3",
	"//Subtitles"
};

//DM14052004 081.7 int02 add
private final static String[] sonHeader = {
	"st_format\t2",
	"Display_Start\tnon_forced",
	"TV_Type\t\tPAL",
	"Tape_Type\tNON_DROP",
	"Pixel_Area\t(0 575)",
	"Directory\t",
	"",
	"SP_NUMBER\tSTART\t\tEND\t\tFILE_NAME"
};

private final static String[] colors = {
	"{\\c&HC0C0C0&}",   // black /gray
	"{\\c&H4040FF&}",   // red
	"{\\c&H00FF00&}",   // green
	"{\\c&H00FFFF&}",   // yellow
	"{\\c&HFF409B&}",   // blue //DM15032004 081.6 int18 changed
	"{\\c&HFF00FF&}",   // magenta
	"{\\c&HFFFF00&}",   // cyan
	"{\\c&HFFFFFF&}",   // white
};


//DM14052004 081.7 int02 add
public static String[] getSONHead(String path, long frame_rate)
{
	if (frame_rate != 3600)
	{
		sonHeader[2] = "TV_Type\t\tNTSC";
		sonHeader[3] = "Tape_Type\tDROP";
	}
	else
	{
		sonHeader[2] = "TV_Type\t\tPAL";
		sonHeader[3] = "Tape_Type\tNON_DROP";
	}

	sonHeader[5] = "Directory\t" + path;

	return sonHeader;
}

/*****************
 * return STL header *
 *****************/
public static String[] getSTLHead(String version)
{
	stlHeader[0] = 	"//Generated by " + version;

	return stlHeader;
}

/*****************
 * return SSA header *
 *****************/
public static String[] getSSAHead()
{ 
	return ssaHeader; 
}

/*****************
 * return SSA line *
 *****************/
public static String[] getSSALine()
{ 
	return ssaLine; 
}

/*****************
 * return SMPTE *
 *****************/
public static String SMPTE(String time, long videoframetime)
{
	StringBuffer a = new StringBuffer();
	a.append(time.substring(0, 8) + ":00");
	String b = "" + (Integer.parseInt(time.substring(9, 12)) / ((int)videoframetime / 90));
	a.replace((b.length() == 1) ? 10 : 9 , 11, b);

	return a.toString();
}

/*****************
 * change endian *
 *****************/
public static byte bytereverse(byte n)
{
	n = (byte) (((n >> 1) & 0x55) | ((n << 1) & 0xaa));
	n = (byte) (((n >> 2) & 0x33) | ((n << 2) & 0xcc));
	n = (byte) (((n >> 4) & 0x0f) | ((n << 4) & 0xf0));
	return n;
}

/**************
 * set parity *
 **************/
public static byte parity(byte n)
{
	boolean par=true;

	if (n == 0) 
		return n;

	for (int a=0; a < 8; a++) 
		if ((n>>>a & 1) == 1) 
			par = !par;
	if (par) 
		return (byte)(0x80 | n);

	return n;
}

/****************
 * check parity *
 ****************/
public static boolean cparity(byte n)
{
	boolean par=true;

	if (n == 0) 
		return true;

	for (int a=0; a < 7; a++) 
		if ((n>>>a & 1) == 1) 
			par = !par;

	if (par && (1 & n>>>7) == 1) 
		return true;

	else if (!par && (1 & n>>>7) == 0) 
		return true;

	return false;
}



//DM24052004 081.7 int03 introduced
//no error correction ATM
public static int hamming24_18(byte b[], int off)
{
	int val = 0;
	val |= (0xFE & b[off + 2])>>>1;
	val |= (0xFE & b[off + 1])<<6;
	val |= (0xE & b[off])<<13;
	val |= (0x20 & b[off])<<12;

	return val;
}

/******************
 * hamming decode *
 ******************/
//DM12032004 081.6 int18 changed
//DM24052004 081.7 int03 changed
public static byte hamming_decode(byte a)
{
	switch (0xFF & a)
	{
	case 0xa8: 
		return 0;
	case 0x0b: 
		return 1;
	case 0x26: 
		return 2;
	case 0x85: 
		return 3;
	case 0x92: 
		return 4;
	case 0x31: 
		return 5;
	case 0x1c: 
		return 6;
	case 0xbf: 
		return 7;
	case 0x40: 
		return 8;
	case 0xe3: 
		return 9;
	case 0xce: 
		return 10;
	case 0x6d: 
		return 11;
	case 0x7a: 
		return 12;
	case 0xd9: 
		return 13;
	case 0xf4: 
		return 14;
	case 0x57: 
		return 15;
	default: 
		return -1;     // decode error , not yet corrected
	}
}


/******************************
 * make suppic from teletext *
 ******************************/
//DM30122003 081.6 int10 changed
public static int[] makepic(byte[] a, int cflag)
{
	boolean ascii=true;
	int[] chars = new int[a.length];
	int ac = 7;  // init with white ascii color per line + black background
	//  cflag = language 
	//  return int char<<8 | 0xF0&ac backgrnd | 0xF&ac foregrnd

	for (int c=0; c < a.length; c++) {
		if (!cparity(a[c])) 
			a[c]=8;
		int tx = 0x7F & bytereverse(a[c]);

		if (tx>>>3 ==0) { 
			ascii=true; 
			chars[c]= (0x20<<8 | ac); 
			ac= (0xF0&ac) | tx; 
			continue; 
		} //0x0..7
		else if (tx>>>4 ==0) { 
			chars[c]=0x20<<8 | ac; 
			continue; 
		}  //0x8..F
		else if (tx>>>7 ==1) { 
			chars[c]=0x20<<8 | ac; 
			continue; 
		}  //0x80..FF
		else if (tx < 27) { 
			ascii=false; 
			chars[c]=0x20<<8 | ac; 
			continue; 
		}  //0x10..1A
		else if (tx < 32) {    //1d=new bg with last color, 1c=black bg
			switch (tx){
			case 0x1C:
				ac&=0xF;
				break;
			case 0x1D:
				ac|=(0xF&ac)<<4;
			}
			chars[c]=0x20<<8 | ac; 
			continue; 
		} //0x1B..1F
		else if (tx == 0x7F) { 
			chars[c]=0x20<<8 | ac; 
			continue; 
		} //0x7F

		if (!ascii) { 
			chars[c]=0x20<<8 | ac; 
			continue; 
		}

		// all chars 0x20..7F    special characters
		switch (tx) { 
		case 0x23: { 
			chars[c] = ac | ncs[cflag][0]<<8;  
			continue; 
		}
		case 0x24: { 
			chars[c] = ac | ncs[cflag][1]<<8;  
			continue; 
		}
		case 0x40: { 
			chars[c] = ac | ncs[cflag][2]<<8;  
			continue; 
		}
		case 0x5b: { 
			chars[c] = ac | ncs[cflag][3]<<8;  
			continue; 
		}
		case 0x5c: { 
			chars[c] = ac | ncs[cflag][4]<<8;  
			continue; 
		}
		case 0x5d: { 
			chars[c] = ac | ncs[cflag][5]<<8;  
			continue; 
		}
		case 0x5e: { 
			chars[c] = ac | ncs[cflag][6]<<8;  
			continue; 
		}
		case 0x5f: { 
			chars[c] = ac | ncs[cflag][7]<<8;  
			continue; 
		}
		case 0x60: { 
			chars[c] = ac | ncs[cflag][8]<<8;  
			continue; 
		}
		case 0x7b: { 
			chars[c] = ac | ncs[cflag][9]<<8;  
			continue; 
		}
		case 0x7c: { 
			chars[c] = ac | ncs[cflag][10]<<8; 
			continue; 
		}
		case 0x7d: { 
			chars[c] = ac | ncs[cflag][11]<<8; 
			continue; 
		}
		case 0x7e: { 
			chars[c] = ac | ncs[cflag][12]<<8; 
			continue; 
		}
		default:   { 
			chars[c] = ac | tx<<8; 
			continue; 
		}
		}
	}

	String test = "";
	for (int s=0;s<chars.length;s++) 
		test += (char)(chars[s]>>>8);
	if (test.trim().length()==0) 
		return null;

	return chars;
}


/******************************
 * make strings from teletext *
 ******************************/
public static String makestring(byte[] a, int cflag, int color)
{
	boolean ascii=true;
	String text = "";

	loopi:
	for (int c=0; c < a.length; c++) {
		if (!cparity(a[c])) 
			a[c]=8;
		int tx = 0x7F & bytereverse(a[c]);

		if (tx>>>3 ==0) { 
			ascii=true; 
			text += ((color==1)?colors[tx]:"")+" "; 
			continue; 
		} //0x0..7
		else if (tx>>>4 ==0) { 
			text += " "; 
			continue; 
		}  //0x8..F
		else if (tx>>>7 ==1) { 
			text += " "; 
			continue; 
		}  //0x80..FF
		else if (tx < 27) { 
			ascii=false; 
			text += " "; 
			continue; 
		}  //0x10..1A
		else if (tx < 32) {  
			text += " "; 
			continue; 
		} //0x1B..1F
		else if (tx == 0x7F) {  
			text += " "; 
			continue; 
		} //0x7F

		if (!ascii) { 
			text += " "; 
			continue; 
		}

		// all chars 0x20..7F    special characters

		switch (tx) {      // special  characters
		case 0x23: { 
			text += ncs[cflag][0]; 
			continue loopi; 
		}
		case 0x24: { 
			text += ncs[cflag][1]; 
			continue loopi; 
		}
		case 0x40: { 
			text += ncs[cflag][2]; 
			continue loopi; 
		}
		case 0x5b: { 
			text += ncs[cflag][3]; 
			continue loopi; 
		}
		case 0x5c: { 
			text += ncs[cflag][4]; 
			continue loopi; 
		}
		case 0x5d: { 
			text += ncs[cflag][5]; 
			continue loopi; 
		}
		case 0x5e: { 
			text += ncs[cflag][6]; 
			continue loopi; 
		}
		case 0x5f: { 
			text += ncs[cflag][7]; 
			continue loopi; 
		}
		case 0x60: { 
			text += ncs[cflag][8]; 
			continue loopi; 
		}
		case 0x7b: { 
			text += ncs[cflag][9]; 
			continue loopi; 
		}
		case 0x7c: { 
			text += ncs[cflag][10]; 
			continue loopi; 
		}
		case 0x7d: { 
			text += ncs[cflag][11]; 
			continue loopi; 
		}
		case 0x7e: { 
			text += ncs[cflag][12]; 
			continue loopi; 
		}
		default:  { 
			text += (char)tx; 
			continue loopi; 
		}
		}
	}

	if (color==1) 
		return colors[7]+text.trim();
	else 
		return text;
}

}
