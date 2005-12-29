/*
 * @(#)StreamParser
 *
 * Copyright (c) 2005 by dvb.matt, All rights reserved.
 * 
 * This file is part of ProjectX, a free Java based demux utility.
 * By the authors, ProjectX is intended for educational purposes only, 
 * as a non-commercial test project.
 * 
 *
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by
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

package net.sourceforge.dvb.projectx.parser;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import net.sourceforge.dvb.projectx.common.Common;
import net.sourceforge.dvb.projectx.common.Resource;
import net.sourceforge.dvb.projectx.common.Keys;
import net.sourceforge.dvb.projectx.common.JobCollection;
import net.sourceforge.dvb.projectx.common.JobProcessing;

import net.sourceforge.dvb.projectx.xinput.XInputFile;

import net.sourceforge.dvb.projectx.parser.CommonParsing;

/**
 * main thread
 */
public class StreamParserBase extends Object {

	public int ERRORCODE = 0;
	public int MainBufferSize = 8192000;

	/**
	 * 
	 */
	public StreamParserBase()
	{
		init();
	}

	/**
	 * 
	 */
	public void init()
	{
		MainBufferSize = Integer.parseInt(Common.getSettings().getProperty(Keys.KEY_MainBuffer));

		if (MainBufferSize <= 0)
			MainBufferSize = 4096000;
	}

	/**
	 * 
	 */
	public String parseStream(JobCollection collection, XInputFile aXInputFile, int pes_streamtype, int action, String vptslog)
	{
		return null;
	}

	/**
	 * 
	 */
	public boolean pause()
	{
		return Common.waitingMainProcess();
	}

	/**
	 * nextfile PTS check
	 * 
	 * returns new pts offset to append
	 */
	public long nextFilePTS(JobCollection collection, int parser_type, int pes_streamtype, long lastpts, int file_number)
	{
		return nextFilePTS(collection, parser_type, pes_streamtype, lastpts, file_number, 0L);
	}


	/**
	 * nextfile PTS check
	 * 
	 * returns new pts offset to append
	 */
	public long nextFilePTS(JobCollection collection, int parser_type, int pes_streamtype, long lastpts, int file_number, long startPoint)
	{
		JobProcessing job_processing = collection.getJobProcessing();

		byte[] data;

		long pts = 0;

		int position = 0;
		int buffersize = Integer.parseInt(Common.getSettings().getProperty(Keys.KEY_ScanBuffer));
		int pes_ID;

		boolean PVA_Audio = Common.getSettings().getBooleanProperty(Keys.KEY_PVA_Audio);
		boolean containsPts;

		lastpts &= 0xFFFFFFFFL; //ignore bit33 of lastpts

		if (collection.getPrimaryInputFileSegments() > file_number)
		{
			try {

				XInputFile aXinputFile = ((XInputFile) collection.getInputFile(file_number)).getNewInstance();

				data = new byte[buffersize];

				aXinputFile.randomAccessSingleRead(data, startPoint);

				switch (parser_type)
				{
				case CommonParsing.PVA_PARSER:    // pva

					int pva_pid;
					int ptsflag;
					int pva_payloadlength;

					loop:
					while (position < buffersize - 20)
					{
						if (data[position] != 0x41 || data[position + 1] != 0x56 || data[position + 4] != 0x55)
						{
							position++; 
							continue loop;
						}

						pva_pid = 0xFF & data[position + 2];
						ptsflag = 0xFF & data[position + 5];
						pva_payloadlength = (0xFF & data[position + 6])<<8 | (0xFF & data[position + 7]);
						containsPts = (0x10 & ptsflag) != 0;

		 				if (pva_pid == 1) //video
						{
							if (containsPts)
							{
								pts = CommonParsing.readPTS(data, position + 8, 4, !CommonParsing.BYTEREORDERING, true);
								break loop;
							} 
						}

						else if (pva_pid != 0) //mainaudio mpa and other pids
						{
							ptsloop:
							for (int i = position + 8, j = (PVA_Audio && !containsPts) ? position + 8: position + 11; i < j; i++)
							{
								if (CommonParsing.validateStartcode(data, i) < 0)
									continue ptsloop;

								pes_ID = CommonParsing.getPES_IdField(data, i);

								if (pes_ID != 0xBD && (0xF0 & pes_ID) != 0xC0) 
									continue ptsloop;

								if ((0x80 & data[i + 7]) == 0) 
									break ptsloop;

								pts = CommonParsing.getPTSfromBytes(data, i + 9); //returns 32bit

								break loop;
							}
						}

						position += 8 + pva_payloadlength;
					}

					break;

				case CommonParsing.PRIMARY_PES_PARSER:    // mpg

					int pes_payloadlength = 0;
					int ptslength = 0;
					int returncode;
					int pes_offset = 0;

					boolean nullpacket = false;

					loop:
					while (position < buffersize - 20)
					{
						if ((returncode = CommonParsing.validateStartcode(data, position)) < 0)
						{ 
							position += -returncode;
							continue loop; 
						}

						pes_ID = CommonParsing.getPES_IdField(data, position);

						if (pes_ID < 0xBA)
						{
							position += 3;
							continue loop; 
						}

						if (pes_ID == 0xBA)
						{ 
							position += 12;
							continue loop; 
						}

						pes_payloadlength = CommonParsing.getPES_LengthField(data, position);

						if (pes_payloadlength == 0)
							nullpacket = true;

						if ((0xF0 & pes_ID) != 0xE0 && (0xF0 & pes_ID) != 0xC0 && pes_ID != 0xBD )
						{ 
							position += 6 + pes_payloadlength;
							continue loop; 
						}

						if (pes_streamtype == CommonParsing.MPEG1PS_TYPE)
						{
							pes_offset = 6;

							skiploop:
							while(true)
							{
								switch (0xC0 & data[position + pes_offset])
								{
								case 0x40: 
									pes_offset += 2; 
									continue skiploop; 

								case 0x80: 
									pes_offset += 3; 
									continue skiploop; 

								case 0xC0: 
									pes_offset++; 
									continue skiploop; 

								case 0:
									break; 
								}

								switch (0x30 & data[position + pes_offset])
								{
								case 0x20:
									containsPts = true; 
									break skiploop; 

								case 0x30:  
									containsPts = true; 
									break skiploop; 

								case 0x10: 
									containsPts = false; 
									break skiploop; 

								case 0:  
									containsPts = false; 
									pes_offset++; 
									break skiploop; 
								}
							}
						}

						else
						{
							containsPts = (0x80 & data[position + 7]) != 0;
							pes_offset = 9;
						}

						if (containsPts)
							pts = CommonParsing.getPTSfromBytes(data, position + pes_offset);

						position += 6 + pes_payloadlength;

						if (nullpacket)
							break;
					}

					break;
				} // end switch

			} catch (IOException e) { 

				Common.setExceptionMessage(e);
			}
		}

		if (file_number == 0 && startPoint == 0)
		{  // need global offset?
			pts &= 0xFFFFFFFFL;

			String str = Common.getSettings().getProperty(Keys.KEY_PtsShift_Value);

			if (str.equals("auto"))
			{ 
				long newpts = ((pts / 324000000L) - 1L) * 324000000L;
				Common.setMessage(Resource.getString("nextfile.shift.auto", "" + (newpts / 324000000L)));

				return newpts;
			}

			else if (!str.equals("0"))
			{ 
				Common.setMessage(Resource.getString("nextfile.shift.manual", Common.getSettings().getProperty(Keys.KEY_PtsShift_Value)));

				return ((long)(Double.parseDouble(Common.getSettings().getProperty(Keys.KEY_PtsShift_Value)) * 324000000L));
			}

			else 
				return 0L;
		}
		else
		{
			pts -= job_processing.getNextFileStartPts();
			pts &= 0xFFFFFFFFL;

			long ret = 0L;

			if (Math.abs(pts - lastpts) < 900000)
				ret = -1L;

			else if (pts > lastpts)  
				ret = 0L;

			else 
				ret = ((lastpts + 1728000L) - pts); // offset is multiple of 40,24,32,33.1,8ms

			if (ret >= 0)
				Common.setMessage(Resource.getString("nextfile.next.file.start", Common.formatTime_1(pts / 90L), Common.formatTime_1(lastpts / 90L)));

			if (ret > 0)
				Common.setMessage(Resource.getString("nextfile.next.file.start.adaption", Common.formatTime_1(ret / 90L)));

			return ret;
		}
	}


}
