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
import java.io.PushbackInputStream;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import net.sourceforge.dvb.projectx.common.Common;
import net.sourceforge.dvb.projectx.common.Resource;
import net.sourceforge.dvb.projectx.common.Keys;
import net.sourceforge.dvb.projectx.common.JobCollection;
import net.sourceforge.dvb.projectx.common.JobProcessing;

import net.sourceforge.dvb.projectx.io.RawFile;

import net.sourceforge.dvb.projectx.xinput.XInputFile;
import net.sourceforge.dvb.projectx.xinput.StreamInfo;

import net.sourceforge.dvb.projectx.parser.CommonParsing;
import net.sourceforge.dvb.projectx.parser.StreamConverter;
import net.sourceforge.dvb.projectx.parser.StreamDemultiplexer;
import net.sourceforge.dvb.projectx.parser.StreamParserBase;
import net.sourceforge.dvb.projectx.parser.StreamProcess;


/**
 * main thread
 */
public class StreamParserTS extends StreamParserBase {

	/**
	 * 
	 */
	public StreamParserTS()
	{
		super();
	}

	/**
	 * ts Parser
	 */
	public String parseStream(JobCollection collection, XInputFile xInputFile, int pes_streamtype, int action, String vptslog)
	{
		String fchild = collection.getOutputName(xInputFile.getName());
		String fparent = collection.getOutputNameParent(fchild);

		JobProcessing job_processing = collection.getJobProcessing();

		/**
		 * split part 
		 */
		fparent += job_processing.getSplitSize() > 0 ? "(" + job_processing.getSplitPart() + ")" : "" ;

		vptslog = "-1"; //fix

		boolean Message_1 = Common.getSettings().getBooleanProperty(Keys.KEY_MessagePanel_Msg1);
		boolean Message_2 = Common.getSettings().getBooleanProperty(Keys.KEY_MessagePanel_Msg2);
		boolean Debug = collection.DebugMode();
		boolean JoinPackets = Common.getSettings().getBooleanProperty(Keys.KEY_TS_joinPackets);
		boolean HumaxAdaption = Common.getSettings().getBooleanProperty(Keys.KEY_TS_HumaxAdaption);
		boolean FinepassAdaption = Common.getSettings().getBooleanProperty(Keys.KEY_TS_FinepassAdaption);
		boolean GetEnclosedPackets = Common.getSettings().getBooleanProperty(Keys.KEY_Input_getEnclosedPackets);
		boolean IgnoreScrambledPackets = Common.getSettings().getBooleanProperty(Keys.KEY_TS_ignoreScrambled);
		boolean PcrCounter = Common.getSettings().getBooleanProperty(Keys.KEY_Conversion_PcrCounter);
		boolean BlindSearch = Common.getSettings().getBooleanProperty(Keys.KEY_TS_blindSearch);
		boolean CreateD2vIndex = Common.getSettings().getBooleanProperty(Keys.KEY_ExternPanel_createD2vIndex);
		boolean SplitProjectFile = Common.getSettings().getBooleanProperty(Keys.KEY_ExternPanel_splitProjectFile);
		boolean UseAutoPidFilter = Common.getSettings().getBooleanProperty(Keys.KEY_useAutoPidFilter);
		boolean Overlap = Common.getSettings().getBooleanProperty(Keys.KEY_ExportPanel_Export_Overlap);

		boolean ts_isIncomplete = false;
		boolean ts_startunit = false;
		boolean ts_hasErrors = false;
		boolean containsPts = false;
		boolean ende = false;
		boolean missing_syncword = false;
		boolean usePidfilter = false;
		boolean isTeletext;
		boolean foundObject;

		int ts_buffersize = 189;
		int ts_packetlength = 188;

		byte[] ts_packet = new byte[ts_buffersize];
		byte[] pes_packet;
		byte[] hav_chunk = { 0x5B, 0x48, 0x4F, 0x4A, 0x49, 0x4E, 0x20, 0x41 }; //'[HOJIN A'

		int Infoscan_Value = Integer.parseInt(Common.getSettings().getProperty(Keys.KEY_ExportPanel_Infoscan_Value));
		int CutMode = Common.getSettings().getIntProperty(Keys.KEY_CutMode);
		int ts_pid;
		int ts_scrambling = 0;
		int ts_adaptionfield = 0;
		int ts_counter = 0;
		int ts_adaptionfieldlength = 0;
		int payload_pesID = 0;
		int payload_psiID = 0;
		int pes_extensionlength = 0;
		int pes_payloadlength = 0;
		int pes_packetlength;
		int pes_packetoffset = 6;
		int pes_headerlength = 9;
		int pes_offset = 0;
		int pes_subID = 0;
		int pes_ID;
		int bytes_read = 0;

		int[] newID = { 0x80, 0x90, 0xC0, 0xE0, 0xA0, 0x20 };

		job_processing.clearStatusVariables();
		int[] clv = job_processing.getStatusVariables();

		long next_CUT_BYTEPOSITION = 0;
		long lastpts = 0;
		long ptsoffset = 0;
		long packet = 0;
		long count = 0;
		long size = 0;
		long base;
		long startPoint = 0;
		long starts[] = new long[collection.getPrimaryInputFileSegments()];
		long Overlap_Value = 1048576L * (Common.getSettings().getIntProperty(Keys.KEY_ExportPanel_Overlap_Value) + 1);
		long qexit;


		StreamBuffer streambuffer = null;
		StreamDemultiplexer streamdemultiplexer = null;
		StreamConverter streamconverter = new StreamConverter();

		ArrayList usedPIDs = new ArrayList();

		List demuxList = job_processing.getTSDemuxList();
		List TSPidlist = job_processing.getTSPidList();

		/**
		 * re-read old streams, for next split part
		 */
		if (job_processing.getSplitPart() == 0)
		{ 
			TSPidlist.clear();
			demuxList.clear();
		}

		else
		{
			for (int i = 0; i < TSPidlist.size(); i++)
			{
				streambuffer = (StreamBuffer) TSPidlist.get(i);
				streambuffer.reset();
				streambuffer.setStarted(false);
			}

			for (int i = 0; i < demuxList.size(); i++)
			{
				streamdemultiplexer = (StreamDemultiplexer) demuxList.get(i);

				if (streamdemultiplexer.getnewID() != 0)
					newID[streamdemultiplexer.getType()]++;

				if (streamdemultiplexer.getNum() == -1) 
					continue;

				if (streamdemultiplexer.getType() == CommonParsing.MPEG_VIDEO) 
					streamdemultiplexer.initVideo2(fparent);

				else 
					streamdemultiplexer.init2(fparent);
			}
		}

		/**
		 * first split part, or one file only
		 */
		if (job_processing.getSplitPart() == 0)
		{
			StreamInfo streamInfo = xInputFile.getStreamInfo();

			int[] pids = streamInfo.getPIDs();

			if (pids.length > 0)
			{
				Common.setMessage(Resource.getString("parseTS.sid") + Integer.toHexString(pids[0]).toUpperCase());
				Common.setMessage(Resource.getString("parseTS.pmt.refers", Integer.toHexString(pids[1]).toUpperCase()));

				Common.setMessage(Resource.getString("ScanInfo.Video"));
				Common.setMessage(streamInfo.getVideo());

				Common.setMessage(Resource.getString("ScanInfo.Audio"));
				Common.setMessage(streamInfo.getAudio());

				Common.setMessage(Resource.getString("ScanInfo.Teletext"));
				Common.setMessage(streamInfo.getTeletext());

				Common.setMessage(Resource.getString("ScanInfo.Subpicture"));
				Common.setMessage(streamInfo.getSubpicture());

				Common.setMessage("");

				for (int i = 2; i < pids.length; i++)
				{
					TSPidlist.add(streambuffer = new StreamBuffer());
					streambuffer.setPID(pids[i]);
				}
			}

			else 
				Common.setMessage(Resource.getString("parseTS.no.pmt"));
		}



		/**
		 * init conversions 
		 */
		switch (action)
		{
		case CommonParsing.ACTION_TO_VDR:
			streamconverter.init(fparent + ".vdr", MainBufferSize, action, job_processing.getSplitPart());
			break;

		case CommonParsing.ACTION_TO_M2P:
			streamconverter.init(fparent + ".m2p", MainBufferSize, action, job_processing.getSplitPart());
			break;

		case CommonParsing.ACTION_TO_PVA:
			streamconverter.init(fparent + ".pva", MainBufferSize, action, job_processing.getSplitPart());
			break;

		case CommonParsing.ACTION_TO_TS:
			streamconverter.init(fparent + ".new.ts", MainBufferSize, action, job_processing.getSplitPart());
			break;

		case CommonParsing.ACTION_FILTER:
			streamconverter.init(fparent + "[filtered].ts", MainBufferSize, action, job_processing.getSplitPart());
		}

		/**
		 * d2v project 
	 	 */
		if (CreateD2vIndex || SplitProjectFile)
			job_processing.getProjectFileD2V().Init(fparent);


		job_processing.setMinBitrate(CommonParsing.MAX_BITRATE_VALUE);
		job_processing.setMaxBitrate(0);
		job_processing.setExportedVideoFrameNumber(0);
		job_processing.setEndPtsOfGop(-10000);
		job_processing.setSequenceHeader(true);
		job_processing.setAllMediaFilesExportLength(0);
		job_processing.setProjectFileExportLength(0);
		job_processing.setCutByteposition(0);

		/**
		 * pid inclusion 
		 */
		int[] predefined_Pids = UseAutoPidFilter ? xInputFile.getStreamInfo().getMediaPIDs() : collection.getPIDsAsInteger();

		int[] include = new int[predefined_Pids.length];

		for (int i = 0; i < include.length; i++) 
			include[i] = 0x1FFF & predefined_Pids[i];

		if (include.length > 0)
		{
			Arrays.sort(include);

			String str = " ";

			for (int i = 0; i < include.length; i++)
				str += "0x" + Integer.toHexString(include[i]).toUpperCase() + " ";

			Common.setMessage(Resource.getString("parseTS.special.pids") + ": {" + str + "}");

			usePidfilter = true;
		}


		try {

			/**
			 * determine start & end byte pos. of each file segment
			 */
			for (int i = 0; i < starts.length; i++)
			{
				xInputFile = (XInputFile) collection.getInputFile(i);
				starts[i] = size;
				size += xInputFile.length();
			}

			xInputFile = (XInputFile) collection.getInputFile(job_processing.getFileNumber());

			/**
			 * set start & end byte pos. of first file segment
			 */
			count = starts[job_processing.getFileNumber()];
			size = count + xInputFile.length();

			/**
			 * split skipping first, for next split part
			 */
			if (job_processing.getSplitSize() > 0)
			{
				startPoint = job_processing.getLastHeaderBytePosition();
				startPoint -= !Overlap ? 0 : Overlap_Value;

				job_processing.setLastGopTimecode(0);
				job_processing.setLastGopPts(0);
				job_processing.setLastSimplifiedPts(0);
			}

			List CutpointList = collection.getCutpointList();
			List ChapterpointList = collection.getChapterpointList();

			/**
			 * jump near to first cut-in point to collect more audio
			 */
			if (CutMode == CommonParsing.CUTMODE_BYTE && CutpointList.size() > 0 && CommonParsing.getCutCounter() == 0)
				startPoint = Long.parseLong(CutpointList.get(CommonParsing.getCutCounter()).toString()) - ((action == CommonParsing.ACTION_DEMUX) ? 2048000: 0);

			if (startPoint < 0)
				startPoint = count;

			else if (startPoint < count)
			{
				for (int i = starts.length; i > 0; i--)
					if (starts[i - 1] > startPoint)
						job_processing.countFileNumber(-1);
			}


			else if (startPoint > count)
			{
				for (int i = job_processing.getFileNumber() + 1; i < starts.length; i++)
				{
					if (starts[i] > startPoint)
						break;
					else 
						job_processing.countFileNumber(+1);
				}
			}

			xInputFile = (XInputFile) collection.getInputFile(job_processing.getFileNumber());
			count = starts[job_processing.getFileNumber()];

			if (job_processing.getFileNumber() > 0)
				Common.setMessage(Resource.getString("parseTS.continue") + " " + xInputFile);

			base = count;
			size = count + xInputFile.length();

			PushbackInputStream in = new PushbackInputStream(xInputFile.getInputStream(startPoint - base), 200);

			count += (startPoint - base);

			Common.updateProgressBar((action == CommonParsing.ACTION_DEMUX ? Resource.getString("parseTS.demuxing") : Resource.getString("parseTS.converting")) + " " + Resource.getString("parseTS.dvb.mpeg") + " " + xInputFile.getName(), (count - base), (size - base));

			qexit = count + (0x100000L * Infoscan_Value);


			bigloop:
			while (true)
			{
				loop:
				while (count < size)
				{
					while (pause())
					{}

					if (CommonParsing.isProcessCancelled() || (CommonParsing.isInfoScan() && count > qexit))
					{ 
						CommonParsing.setProcessCancelled(false);
						job_processing.setSplitSize(0); 

						break bigloop; 
					}

					/**
					 * cut end reached 
					 */
					if (job_processing.getCutComparePoint() + 20 < job_processing.getSourceVideoFrameNumber())
					{
						ende = true;
						break bigloop; 
					}

					/**
					 * cut end reached 
					 */
					if (CutMode == CommonParsing.CUTMODE_BYTE && CutpointList.size() > 0)
					{
						if (CommonParsing.getCutCounter() == CutpointList.size() && (CommonParsing.getCutCounter() & 1) == 0)
							if (count > Long.parseLong(CutpointList.get(CommonParsing.getCutCounter() - 1).toString()) + 2048000)
							{
								ende = true;
								break bigloop;
							}
					}

					/**
					 * regular read
					 */
					if (!ts_isIncomplete || !JoinPackets)
					{
						bytes_read = in.read(ts_packet, 0, ts_buffersize);

						/**
						 * EOF is packet aligned
						 */
						if (bytes_read == ts_packetlength && size - count == bytes_read)
							ts_packet[188] = 0x47;

						else if (bytes_read < ts_buffersize && JoinPackets)
						{
							Common.setMessage(Resource.getString("parseTS.incomplete") + " " + count);
							count += bytes_read;
							break loop;
						}
					}

					/**
					 * humax .vid workaround, skip special data chunk
					 */
					if (HumaxAdaption && ts_packet[0] == 0x7F && ts_packet[1] == 0x41 && ts_packet[2] == 4 && ts_packet[3] == (byte)0xFD)
					{
						in.skip(995);
						count += 1184;
						continue loop;
					}

					/**
					 * finepass .hav workaround, chunks fileposition index (hdd sectors) unused, because a file can be hard-cut anywhere
					 */
					if (FinepassAdaption && ts_packet[0] == 0x47 && ts_packet[188] != 0x47)
					{
						int i = ts_packetlength;
						int j;
						int k = ts_buffersize;
						int l = hav_chunk.length;

						while (i > 0)
						{
							j = 0;

							while (i > 0 && ts_packet[i] != hav_chunk[j])
								i--;

							for ( ; i > 0 && j < l && i + j < k; j++)
								if (ts_packet[i + j] != hav_chunk[j])
									break;

							/**
							 * found at least one byte of chunk
							 */
							if (j > 0)
							{
								/** ident of chunk doesnt match completely */
								if (j < l && i + j < k)
								{
									i--;
									continue;
								}

								in.skip(0x200 - (k - i));
								in.read(ts_packet, i, k - i);

								count += 0x200;

								break;
							}
						}
					}


		 			if (HumaxAdaption && ts_packet[0] == 0x47 && ts_packet[188] == 0x7F)
					{}  // do nothing, take the packet

		 			else if (ts_packet[0] != 0x47 || (GetEnclosedPackets && ts_packet[188] != 0x47) )
					{
						if (Message_2 && !missing_syncword) 
							Common.setMessage(Resource.getString("parseTS.missing.sync") + " " + count);

						if (ts_isIncomplete && JoinPackets)
						{
							Common.setMessage(Resource.getString("parseTS.comp.failed"));

							in.unread(ts_packet, 190 - bytes_read, bytes_read - 1);

							ts_isIncomplete = false;

							count++;
						}

						else
						{
							int i = 1;

							while (i < ts_buffersize)
							{
								if (ts_packet[i] == 0x47)
									break;

								i++;
							}
							
						//	in.unread(ts_packet, 1, ts_packetlength);
							in.unread(ts_packet, i, ts_buffersize - i);

							count += i;
						}

						missing_syncword = true;

						continue loop;
					}

					else if (ts_isIncomplete && JoinPackets)
						Common.setMessage(Resource.getString("parseTS.comp.ok"));


					if (Message_2 && missing_syncword)
						Common.setMessage(Resource.getString("parseTS.found.sync") + " " + count);

					missing_syncword = false;

					in.unread(ts_packet, ts_packetlength, 1);

					/**
					 * mark for split and cut
					 */
					job_processing.setLastHeaderBytePosition(count);
					next_CUT_BYTEPOSITION = count;


					if (ts_isIncomplete && JoinPackets)
					{
						count += (bytes_read - 1);
						ts_isIncomplete = false;
					}

					else
						count += ts_packetlength;


					packet++;

					ts_hasErrors  = (0x80 & ts_packet[1]) != 0;							// TS error indicator
					ts_startunit  = (0x40 & ts_packet[1]) != 0; 							// new PES packet start
					ts_pid        = (0x1F & ts_packet[1])<<8 | (0xFF & ts_packet[2]);    // the PID
					ts_scrambling = (0xC0 & ts_packet[3])>>>6;                       	// packet is scrambled (>0)
					ts_adaptionfield = (0x30 & ts_packet[3])>>>4;                    		// has adaption field ?
					ts_counter    = (0x0F & ts_packet[3]);                          		// packet counter 0..f
					ts_adaptionfieldlength = ts_adaptionfield > 1 ? (0xFF & ts_packet[4]) + 1 : 0;  		// adaption field length

					Common.updateProgressBar((count - base), (size - base));

					//yield();

					/** 
					 * pid inclusion 
					 */
					if (usePidfilter && Arrays.binarySearch(include, ts_pid) < 0)
						continue loop;

					/**
					 * raw pid filter extraction
					 */
					if (action == CommonParsing.ACTION_FILTER)
					{
						streamconverter.write(job_processing, ts_packet, 0, ts_packetlength, null, next_CUT_BYTEPOSITION, CommonParsing.isInfoScan(), CutpointList);
						continue loop;
					}

					/**
					 * 00 = reserved value
					 */
					if ((ts_adaptionfield & 1) == 0)
						continue loop;

					if (ts_adaptionfieldlength > 183 || (ts_adaptionfieldlength > 180 && ts_startunit))
						ts_hasErrors = true;

					if (ts_hasErrors)
					{
						if (Message_1)
							Common.setMessage(Resource.getString("parseTS.bit.error", Integer.toHexString(ts_pid).toUpperCase(), "" + packet, "" + (count-188)));

						continue loop;
					}

					payload_pesID = ts_startunit ? CommonParsing.getIntValue(ts_packet, 4 + ts_adaptionfieldlength, 4, !CommonParsing.BYTEREORDERING) : 0;
					payload_psiID = ts_startunit ? payload_pesID>>>16 : 0;

					foundObject = false;

					/**
					 * find PID object
					 */
					for (int i = 0; i < TSPidlist.size(); i++)
					{      
						streambuffer = (StreamBuffer)TSPidlist.get(i);

						foundObject = ts_pid == streambuffer.getPID();

						if (foundObject)
							break; 
					}

					/**
					 * create new PID object
					 */
					if (!foundObject)
					{
						TSPidlist.add(streambuffer = new StreamBuffer());
						streambuffer.setPID(ts_pid);

						/**
						 * padding packet
						 */
						if (ts_pid == 0x1FFF)
						{
							Common.setMessage(Resource.getString("parseTS.stuffing"));
							streambuffer.setneeded(false);
						}
					}

					if (Debug) 
						System.out.println("pk " + packet + " /pid " + Integer.toHexString(ts_pid) + " /pes " + Integer.toHexString(payload_pesID) + " /tn " + streambuffer.isneeded() + " /er " + ts_hasErrors + " /st " + ts_startunit + " /sc " + ts_scrambling + " /ad " + ts_adaptionfield + " /al " + ts_adaptionfieldlength);

					/**
					 * PID not of interest
					 */
					if (!streambuffer.isneeded()) 
						continue loop;


					if (IgnoreScrambledPackets)
					{
						// cannot work with scrambled data
						if (ts_scrambling > 0)
						{
							if (!streambuffer.getScram())
							{
								streambuffer.setScram(true);
								Common.setMessage(Resource.getString("parseTS.scrambled", Integer.toHexString(ts_pid).toUpperCase(), String.valueOf(packet), String.valueOf(count - 188)));
							}
							continue loop;
						}

						else
						{
							if (streambuffer.getScram())
							{
								streambuffer.setScram(false);
								Common.setMessage(Resource.getString("parseTS.clear", Integer.toHexString(ts_pid).toUpperCase(), String.valueOf(packet), String.valueOf(count - 188)));
							}
						}
					}

					/**
					 * out of sequence? 
					 * no payload == no counter++
					 */
					if (Message_1 && (PcrCounter || (!PcrCounter && (1 & ts_adaptionfield) != 0)))
					{
						if (streambuffer.getCounter() != -1)
						{
							if (streambuffer.isStarted() && ts_counter != streambuffer.getCounter())
							{
								Common.setMessage(Resource.getString("parseTS.outof.sequence", Integer.toHexString(ts_pid).toUpperCase(), String.valueOf(packet), String.valueOf(count - 188), String.valueOf(ts_counter), String.valueOf(streambuffer.getCounter())) + " (~" + Common.formatTime_1( (long)((CommonParsing.getVideoFramerate() / 90.0f) * job_processing.getExportedVideoFrameNumber())) + ")");
								streambuffer.setCounter(ts_counter);
							}

							streambuffer.count();
						}

						else
						{ 
							streambuffer.setCounter(ts_counter);
							streambuffer.count();
						}
					}

					/**
					 * buffering of subsequent packets
					 */
					if (!ts_startunit)
					{
						if (streambuffer.isneeded() && streambuffer.isStarted())
							streambuffer.writeData(ts_packet, 4 + ts_adaptionfieldlength, 184 - ts_adaptionfieldlength);
					}

					else
					{
						isTeletext = false;
						pes_subID = 0;

						if (streambuffer.getID() == -1 && payload_pesID == 0x1BD) 
						{
							pes_extensionlength = 0;
							pes_offset = 0;

							try {
								pes_extensionlength = 0xFF & ts_packet[12 + ts_adaptionfieldlength];
								pes_offset = 13 + ts_adaptionfieldlength + pes_extensionlength;
								isTeletext = (pes_extensionlength == 0x24 && (0xFF & ts_packet[pes_offset])>>>4 == 1);

								if (!isTeletext)
									pes_subID = ((0xFF & ts_packet[pes_offset]) == 0x20 && (0xFF & ts_packet[pes_offset + 1]) == 0 && (0xFF & ts_packet[pes_offset + 2]) == 0xF) ? 0x20 : 0;

							} catch (ArrayIndexOutOfBoundsException e) {

								Common.setMessage(Resource.getString("parseTS.io.error") + " / " + pes_extensionlength + " / " + pes_offset);
								Common.setExceptionMessage(e);

								streambuffer.reset();
								streambuffer.setStarted(false);

								continue loop;
							}
						}

						streambuffer.setStarted(true);

						/**
						 * create new streamdemultiplexer object
						 */
						if (streambuffer.getID() == -1)
						{
							streambuffer.setID(payload_pesID);
							String type = "";

							switch (0xFFFFFFF0 & payload_pesID)
							{
							case 0x1E0:
								type = Resource.getString("idtype.mpeg.video");

								streambuffer.setDemux(demuxList.size());

								streamdemultiplexer = new StreamDemultiplexer();
								streamdemultiplexer.setPID(ts_pid);
								streamdemultiplexer.setID(payload_pesID);
								streamdemultiplexer.setnewID(newID[CommonParsing.MPEG_VIDEO]++);
								streamdemultiplexer.setsubID(0);
								streamdemultiplexer.setType(CommonParsing.MPEG_VIDEO);
								streamdemultiplexer.setStreamType(pes_streamtype);

								demuxList.add(streamdemultiplexer);

								if (action == CommonParsing.ACTION_DEMUX)
								{
									if (newID[CommonParsing.MPEG_VIDEO] - 1 == 0xE0)
										streamdemultiplexer.initVideo(fparent, MainBufferSize / demuxList.size(), demuxList.size(), 2);

									else
									{
										type += Resource.getString("idtype.ignored");
										streambuffer.setneeded(false); 
									}
								}

								else 
									type += " " + Resource.getString("idtype.mapped.to") + Integer.toHexString(newID[CommonParsing.MPEG_VIDEO] - 1).toUpperCase();

								break;

							case 0x1C0:
							case 0x1D0:
								type = Resource.getString("idtype.mpeg.audio");

								streambuffer.setDemux(demuxList.size());

								streamdemultiplexer = new StreamDemultiplexer();
								streamdemultiplexer.setPID(ts_pid);
								streamdemultiplexer.setID(payload_pesID);
								streamdemultiplexer.setnewID(newID[CommonParsing.MPEG_AUDIO]++);
								streamdemultiplexer.setsubID(0);
								streamdemultiplexer.setType(CommonParsing.MPEG_AUDIO);
								streamdemultiplexer.setStreamType(pes_streamtype);

								demuxList.add(streamdemultiplexer);

								if (action == CommonParsing.ACTION_DEMUX) 
									streamdemultiplexer.init(fparent, MainBufferSize / demuxList.size(), demuxList.size(), 2);

								else
									type += " " + Resource.getString("idtype.mapped.to") + Integer.toHexString(newID[CommonParsing.MPEG_AUDIO] - 1).toUpperCase();

								break;
							}

							switch (payload_pesID)
							{
							case 0x1BD: 
								type = Resource.getString("idtype.private.stream");
								type += (isTeletext ? " (TTX) ": "") + (pes_subID != 0 ? " (SubID 0x" + Integer.toHexString(pes_subID).toUpperCase() + ")" : ""); 

								streambuffer.setDemux(demuxList.size());

								streamdemultiplexer = new StreamDemultiplexer();
								streamdemultiplexer.setPID(ts_pid);
								streamdemultiplexer.setID(payload_pesID);
								streamdemultiplexer.setsubID(pes_subID);
								streamdemultiplexer.setTTX(isTeletext);

								if (isTeletext)
								{
									streamdemultiplexer.setnewID(newID[CommonParsing.TELETEXT]++);
									streamdemultiplexer.setType(CommonParsing.TELETEXT);
								}

								else if (pes_subID == 0x20)
								{
									streamdemultiplexer.setnewID(newID[CommonParsing.SUBPICTURE]++);
									streamdemultiplexer.setType(CommonParsing.SUBPICTURE);
								}

								else
								{
									streamdemultiplexer.setnewID(newID[CommonParsing.AC3_AUDIO]++);
									streamdemultiplexer.setType(CommonParsing.AC3_AUDIO);
								}

								streamdemultiplexer.setStreamType(pes_subID == 0x20 ? CommonParsing.MPEG2PS_TYPE : CommonParsing.PES_AV_TYPE);
								demuxList.add(streamdemultiplexer);

								if (action == CommonParsing.ACTION_DEMUX) 
									streamdemultiplexer.init(fparent, MainBufferSize/demuxList.size(), demuxList.size(), 2);

								if (action != CommonParsing.ACTION_DEMUX && pes_subID != 0) 
								{
									type += Resource.getString("idtype.ignored");
									streambuffer.setneeded(false);
								}

								if (action != CommonParsing.ACTION_DEMUX && !isTeletext) 
									type += " " + Resource.getString("idtype.mapped.to") + Integer.toHexString(newID[CommonParsing.AC3_AUDIO] - 1).toUpperCase();

								break;

							case 0x1BF:
								Common.setMessage(Resource.getString("parseTS.priv.stream2.ignored", Integer.toHexString(ts_pid).toUpperCase()));
								//break;

								streambuffer.setneeded(false); // skip foll. packs

								continue loop;
							}


							if (type.equals(""))
							{
								if (ts_pid == 0 && payload_psiID == 0)
									type = "(PAT)";

								else if (ts_pid == 1 && payload_psiID == 1)
									type = "(CAT)"; 

								else if (ts_pid == 2 && payload_psiID == 3)
									type = "(TSDT)"; 

								else if (ts_pid == 0x10 && (payload_psiID == 6 || payload_psiID == 0x40 || payload_psiID == 0x41))
									type = "(NIT)"; 

								else if (ts_pid == 0x11 && (payload_psiID == 0x42 || payload_psiID == 0x46))
									type = "(SDT)"; 

								else if (ts_pid == 0x11 && payload_psiID == 0x4A)
									type = "(BAT)"; 

								else if (ts_pid == 0x12 && payload_psiID >= 0x4E && payload_psiID <= 0x6F)
									type = "(EIT)"; 

								else if (ts_pid == 0x13 && payload_psiID == 0x71)
									type = "(RST)"; 

								else if (ts_pid == 0x1F && payload_psiID == 0x7F)
									type = "(SIT)"; 

								else if (ts_pid == 0x1E && payload_psiID == 0x7E)
									type = "(DIT)"; 

								else if (ts_pid == 0x14 && payload_psiID == 0x70)
									type = "(TDS)"; 

								else if (ts_pid == 0x14 && payload_psiID == 0x73)
									type = "(TOT)"; 

								else if (payload_psiID == 0x72 && ts_pid >= 0x10 && ts_pid <= 0x14)
									type = "(ST)"; 

								else
								{
									switch (payload_psiID)
									{
									case 2: 
										type = "(PMT)"; 
										break;

									case 4: 
										type = "(PSI)"; 
										break;

									case 0x82: 
										type = "(EMM)"; 
										break;

									case 0x80:
									case 0x81:
									case 0x83:
									case 0x84: 
										type = "(ECM)"; 
										break;

									case 0x43: 
									case 0x44: 
									case 0x45: 
									case 0x47: 
									case 0x48: 
									case 0x49: 
									case 0x4B: 
									case 0x4C: 
									case 0x4D: 
									case 0xFF: 
										type = "(res.)"; 
										break;

									default:
										if ((payload_psiID >= 4 && payload_psiID <= 3F) || (payload_psiID >= 0x74 && payload_psiID <= 0x7D))
										{
											type = "(res.)"; 
											break;
										}

										if (payload_psiID >= 0x80 && payload_psiID < 0xFF)
										{
											type = "(user def. 0x" + Integer.toHexString(payload_psiID).toUpperCase() + ")"; 
											break;
										}
	
										type += "(payload: ";

										for (int j = 0; j < 8; j++)
										{
											String val = Integer.toHexString((0xFF & ts_packet[4 + ts_adaptionfieldlength + j])).toUpperCase();
											type += " " + (val.length() < 2 ? ("0" + val) : val);
										}

										type += " ..)";
									}
								}

								if (ts_scrambling > 0 && !IgnoreScrambledPackets)
								{
									type += " (0x" + Long.toHexString(count - 188).toUpperCase() + " #" + packet + ") ";  // pos + packno

									if (!streambuffer.getScram()) 
										Common.setMessage(Resource.getString("parseTS.scrambled.notignored", Integer.toHexString(ts_pid).toUpperCase(), type));

									streambuffer.setScram(true);
									streambuffer.setStarted(false);
									streambuffer.setID(-1);
									streambuffer.reset();

									continue loop;
								}

								type += " (" + (count - 188) + " #" + packet + ") ";  // pos + packno
								Common.setMessage("--> PID 0x" + Integer.toHexString(ts_pid).toUpperCase() + " " + type + Resource.getString("parseTS.ignored"));

								if (!BlindSearch || type.indexOf("pay") == -1)
									streambuffer.setneeded(false);

								else
									streambuffer.setID(-1);

								continue loop;
							}

							else
							{
								type += " (" + (count - 188) + " #" + packet + ") ";  // pos + packno
								Common.setMessage(Resource.getString("parseTS.pid.has.pes", Integer.toHexString(ts_pid).toUpperCase(), Integer.toHexString(0xFF & payload_pesID).toUpperCase(), type));
								usedPIDs.add("0x" + Integer.toHexString(ts_pid));
							}
						}

						if (streambuffer.getDemux() == -1 || !streambuffer.isneeded())
							continue loop;


						streamdemultiplexer = (StreamDemultiplexer) demuxList.get(streambuffer.getDemux());

						/**
						 * pes_packet completed
						 */
						if (streamdemultiplexer.StreamEnabled())
						{
							pes_packet = streambuffer.getData().toByteArray();

							if (pes_packet.length < 6)
							{
								if (streamdemultiplexer.getPackCount() != -1) 
									Common.setMessage(Resource.getString("parseTS.lackof.pes", Integer.toHexString(ts_pid).toUpperCase()));
							}

							else if (CommonParsing.validateStartcode(pes_packet, 0) < 0)
								Common.setMessage("!> PID 0x" + Integer.toHexString(ts_pid).toUpperCase() + " - invalid start_code of buffered packet..");

							else
							{
								pes_payloadlength = CommonParsing.getPES_LengthField(pes_packet, 0);
								pes_packetlength = pes_packetoffset + pes_payloadlength;
								pes_ID = streamdemultiplexer.getID();

								/**
								 * non video packet size usually < 0xFFFF
								 */
								if (streamdemultiplexer.getType() != CommonParsing.MPEG_VIDEO)
								{
									if (action == CommonParsing.ACTION_DEMUX)
											streamdemultiplexer.write(job_processing, pes_packet, 0, pes_packetlength, true);

									else
										streamconverter.write(job_processing, pes_packet, streamdemultiplexer, next_CUT_BYTEPOSITION, CommonParsing.isInfoScan(), CutpointList);
								}

								/**
								 * special handling, video packet is possibly greater than 0xffff max. size 
								 */
								else
								{
									pes_packetlength = action == CommonParsing.ACTION_DEMUX ? 0xFFFC : 0x1800;

									for (int i = 0, j, pes_remaininglength = pes_packetlength, flags = (0xF3 & pes_packet[6])<<16; i < pes_packet.length; i += pes_remaininglength)
									{
										if (pes_packet.length - i < pes_remaininglength)
											pes_remaininglength = pes_packet.length - i;

										if (i == 0)
										{
											CommonParsing.setPES_LengthField(pes_packet, i, pes_remaininglength - pes_packetoffset);

											if (action == CommonParsing.ACTION_DEMUX)
											{
												streamdemultiplexer.writeVideo(job_processing, pes_packet, i, pes_remaininglength, true, CutpointList, ChapterpointList);
												job_processing.setCutByteposition(next_CUT_BYTEPOSITION);
											}

											else
												streamconverter.write(job_processing, pes_packet, i, streamdemultiplexer, next_CUT_BYTEPOSITION, CommonParsing.isInfoScan(), CutpointList);
										}

										else
										{
											j = i - pes_headerlength;

											CommonParsing.setValue(pes_packet, j, 4, !CommonParsing.BYTEREORDERING, 0x100 | pes_ID);
											CommonParsing.setPES_LengthField(pes_packet, j, pes_remaininglength + 3);
											CommonParsing.setValue(pes_packet, j + pes_packetoffset, 3, !CommonParsing.BYTEREORDERING, flags);

											if (action == CommonParsing.ACTION_DEMUX)
											{
												streamdemultiplexer.writeVideo(job_processing, pes_packet, j, pes_remaininglength + 3, true, CutpointList, ChapterpointList);
												job_processing.setCutByteposition(next_CUT_BYTEPOSITION);
											}

											else
												streamconverter.write(job_processing, pes_packet, j, streamdemultiplexer, next_CUT_BYTEPOSITION, CommonParsing.isInfoScan(), CutpointList);
										}
									}
								}
							}
						}

						pes_packet = null;
						streambuffer.reset();

						/**
						 * buffer actual packet data
						 */
						streambuffer.writeData(ts_packet, 4 + ts_adaptionfieldlength, 184 - ts_adaptionfieldlength);
					}

					clv[5]++;

					if (action != CommonParsing.ACTION_DEMUX) 
						job_processing.setLastHeaderBytePosition(count);

					/**
					 * split size reached 
					 */
					if (job_processing.getSplitSize() > 0 && job_processing.getSplitSize() < job_processing.getAllMediaFilesExportLength()) 
						break loop;
				}

				/**
				 * split size reached 
				 */
				if (job_processing.getSplitSize() > 0 && job_processing.getSplitSize() < job_processing.getAllMediaFilesExportLength()) 
					break bigloop;

				/**
				 * more files 
				 */
				if (job_processing.getFileNumber() < collection.getPrimaryInputFileSegments() - 1)
				{ 
					in.close();
					//System.gc();

					XInputFile nextXInputFile = (XInputFile) collection.getInputFile(job_processing.countFileNumber(+1));
					count = size;

					in = new PushbackInputStream(nextXInputFile.getInputStream(), 200);

					size += nextXInputFile.length();
					base = count;

				//	job_processing.addCellTime(String.valueOf(job_processing.getExportedVideoFrameNumber()));

					Common.setMessage(Resource.getString("parseTS.actual.vframes") + " " + job_processing.getExportedVideoFrameNumber());
					Common.setMessage(Resource.getString("parseTS.switch.to") + " " + nextXInputFile + " (" + Common.formatNumber(nextXInputFile.length()) + " bytes) @ " + base);

					Common.updateProgressBar((action == CommonParsing.ACTION_DEMUX ? Resource.getString("parseTS.demuxing") : Resource.getString("parseTS.converting")) + " " + Resource.getString("parseTS.dvb.mpeg") + " " + nextXInputFile.getName());

					if (JoinPackets && bytes_read < 188 && nextXInputFile.length() >= 189 - bytes_read)
					{
						ts_isIncomplete = true;
						bytes_read = in.read(ts_packet, bytes_read, 189 - bytes_read);

						Common.setMessage(Resource.getString("parseTS.tryto.complete"));
					}
				}

				else 
					break bigloop;
			}

			Common.setMessage(Resource.getString("parseTS.packs", String.valueOf(clv[5]), String.valueOf(count * 100 / size), String.valueOf(count)));
	
			/**
			 * file end reached for split 
			 */
			if ( (count >= size || ende) && job_processing.getSplitSize() > 0 ) 
				job_processing.setSplitLoopActive(false);

			in.close(); 


			if (action != CommonParsing.ACTION_DEMUX) 
				streamconverter.close(job_processing, CommonParsing.isInfoScan());

			else
			{
				for (int i = 0, NumberOfVideostreams = 0; i < demuxList.size(); i++)
				{
					streamdemultiplexer = (StreamDemultiplexer) demuxList.get(i);

					if (streamdemultiplexer.getType() == CommonParsing.MPEG_VIDEO)
					{ 
						/**
						 * accept only first video
						 */
						if (NumberOfVideostreams > 0)
						{
							Common.setMessage("!> further videostream found (PID 0x" + Integer.toHexString(streamdemultiplexer.getPID()).toUpperCase() + ") -> ignored");
							continue;
						}

						/**
						 * d2v project 
						 */
						if (CreateD2vIndex || SplitProjectFile)
							job_processing.getProjectFileD2V().write(job_processing.getProjectFileExportLength(), job_processing.getExportedVideoFrameNumber());

						Common.setMessage("");
						Common.setMessage(Resource.getString("video.msg.summary") + " " + job_processing.getExportedVideoFrameNumber() + "/ " + clv[0] + "/ " + clv[1] + "/ " + clv[2] + "/ " + clv[3] + "/ " + clv[4]);

						vptslog = streamdemultiplexer.closeVideo(job_processing, collection.getOutputDirectory() + collection.getFileSeparator());

						NumberOfVideostreams++;
					}
				} 

				//System.gc();

				int[] stream_number = new int[10];

				for (int i = 0, es_streamtype; i < demuxList.size(); i++)
				{
					streamdemultiplexer = (StreamDemultiplexer) demuxList.get(i);
					es_streamtype = streamdemultiplexer.getType();

					if (es_streamtype == CommonParsing.MPEG_VIDEO) 
						continue;

					String[] values = streamdemultiplexer.close(job_processing, vptslog);

					if (values[0].equals("")) 
					{
						Common.setMessage(Resource.getString("parseTS.msg.noexport", Integer.toHexString(0xFF & streamdemultiplexer.getID()).toUpperCase(), Integer.toHexString(streamdemultiplexer.getPID()).toUpperCase()));
						continue;
					}

					String newfile = values[3] + (stream_number[es_streamtype] > 0 ? ("[" + stream_number[es_streamtype] + "]") : "") + "." + values[2];

					Common.renameTo(values[0], newfile);

					values[0] = newfile;
					values[3] = vptslog;

					switch (es_streamtype)
					{
					case CommonParsing.AC3_AUDIO:
					case CommonParsing.DTS_AUDIO:
						Common.setMessage("");
						Common.setMessage(Resource.getString("parseTS.ac3.audio") + Integer.toHexString(streamdemultiplexer.getPID()).toUpperCase());

						new StreamProcess(es_streamtype, collection, values[0], values[1], values[2], values[3]);
						break;

					case CommonParsing.TELETEXT:
						Common.setMessage("");
						Common.setMessage(Resource.getString("parseTS.teletext.onpid") + Integer.toHexString(streamdemultiplexer.getPID()).toUpperCase()+" (SubID 0x"+Integer.toHexString(streamdemultiplexer.subID()).toUpperCase() + ")");

						new StreamProcess(es_streamtype, collection, values[0], values[1], values[2], values[3]);
						break;

					case CommonParsing.MPEG_AUDIO: 
						Common.setMessage("");
						Common.setMessage(Resource.getString("parseTS.mpeg.audio", Integer.toHexString(0xFF & streamdemultiplexer.getID()).toUpperCase(), Integer.toHexString(streamdemultiplexer.getPID()).toUpperCase()));

						new StreamProcess(es_streamtype, collection, values[0], values[1], values[2], values[3]);
						break;

					case CommonParsing.LPCM_AUDIO:
						Common.setMessage("");
						Common.setMessage(Resource.getString("parseTS.lpcm.audio") + Integer.toHexString(streamdemultiplexer.subID()).toUpperCase() + ")");

						new StreamProcess(es_streamtype, collection, values[0], values[1], values[2], values[3]);
						break;

					case CommonParsing.SUBPICTURE:
						Common.setMessage("");
						Common.setMessage(Resource.getString("parseTS.subpicture") + Integer.toHexString(streamdemultiplexer.subID()).toUpperCase() + ")");

						new StreamProcess(es_streamtype, collection, values[0], values[1], values[2], values[3]);
						break;
					}

					stream_number[es_streamtype]++;

					new File(newfile).delete();
					new File(values[1]).delete();
				}
			}


			/**
			 * on InfoScan load usable PIDs in pidlist, if list was empty 
			 */
			if (CommonParsing.isInfoScan() && collection.getPIDCount() == 0)
			{
				collection.addPID(usedPIDs.toArray());

				Common.setActiveCollection(Common.getProcessedCollection());

				/* update pid list of an opened panel */
				Common.getGuiInterface().updateCollectionPanel(Common.getActiveCollection());
			}

		} catch (IOException e2) { 

			Common.setExceptionMessage(e2);
		}

		return vptslog;
	}

}