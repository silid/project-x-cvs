/*
 * @(#)PreviewObject.java - file object for preview
 *
 * Copyright (c) 2004-2005 by dvb.matt, All Rights Reserved.
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

package net.sourceforge.dvb.projectx.video;

import net.sourceforge.dvb.projectx.xinput.XInputFile;


//DM24062004 081.7 int05 introduced

public class PreviewObject extends Object {

	private long start, end;
	private int file_type;
	private XInputFile xInputFile;

	private PreviewObject()
	{}

	public PreviewObject(long _start, long _end, int _file_type, XInputFile _xInputFile)
	{
		start = _start;
		end = _end;
		file_type = _file_type;
		xInputFile = _xInputFile;
	}

	public long getStart()
	{
		return start;
	}

	public long getEnd()
	{
		return end;
	}

	public int getType()
	{
		return file_type;
	}

	public XInputFile getFile()
	{
		return xInputFile;
	}
}
