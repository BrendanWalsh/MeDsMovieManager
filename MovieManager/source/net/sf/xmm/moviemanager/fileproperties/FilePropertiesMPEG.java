/**
 * @(#)FilePropertiesMPEG.java 1.0 26.01.06 (dd.mm.yy)
 *
 * Copyright (2003) Mediterranean
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Boston, MA 02111.
 * 
 * Contact: mediterranean@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.fileproperties;

import java.io.RandomAccessFile;
import java.util.Arrays;

import org.apache.log4j.Logger;

class FilePropertiesMPEG extends FileProperties {

	Logger log = Logger.getLogger(getClass());
	
	//private final int[] PICTURE_START_CODE = {0x00, 0x00, 0x01, 0x00};

	//private final int[] USER_DATA_START_CODE = {0x00, 0x00, 0x01, 0xb2};

	private final int[] SEQUENCE_HEADER_CODE = {0x00, 0x00, 0x01, 0xb3};

	//private final int[] SEQUENCE_END_CODE = {0x00, 0x00, 0x01, 0xb7};

	//private final int[] GOP_START_CODE = {0x00, 0x00, 0x01, 0xb8};

	private final int[] PACK_HEADER = {0x00, 0x00, 0x01, 0xba};

	private final int GOP_START_CODE = 0xb8010000;

	//private final int[] PRIVATE_STREAM1_CODE = {0x00, 0x00, 0x01, 0xbd};

	String videoCodec = "";

	/**
	 * Processes a file from the given RandomAccessFile.
	 **/
	protected void process(RandomAccessFile dataStream) throws Exception {
		log.debug("Start processing MPEG file.");

		/* Sets the pointer to offset 4*/
		dataStream.seek(4);

		/* For storing the start codes...*/
		int [] code;

		boolean sequenceHeaderFound = false;
		boolean packHeaderFound = false;
		//boolean audioHeaderFound = false;

		int security = 0;

		/* Loops until SEQUENCE_HEADER_CODE and PACK_HEADER is found... */	
		do {
			/* Gets the next start code... */
			code = getNextStartCode(dataStream);

			if (!sequenceHeaderFound && Arrays.equals(code, SEQUENCE_HEADER_CODE)) {
				if (processSequenceHeader(dataStream))
					sequenceHeaderFound = true;
			}

			if(!packHeaderFound && Arrays.equals(code, PACK_HEADER)) {
				getType(dataStream);
				packHeaderFound = true;
			}

			//if(!privateStream1Found && Arrays.equals(code, PRIVATE_STREAM1_CODE)) {

			//if(Arrays.equals(code, PRIVATE_STREAM1_CODE)) {
			//processPrivateStream1(dataStream);
			//privateStream1Found = true;
			//}

			//if (!audioHeaderFound && code[3] >= 192 && code[3] <= 223) {
			//processAudio(dataStream);
			//audioHeaderFound = true;
			//}

		} while(((!packHeaderFound || !sequenceHeaderFound)) && security++ < 5000);

		if (videoCodec.equals(""))
			videoCodec = "MPEG";

		getDuration(dataStream);

		setVideoCodec(videoCodec);
		setAudioCodec("MPEG-1 Layer 2");
		setContainer("MPEG");

		supported = true;

		log.debug("Processing MPEG file done.");
	}


	/* Currently not used */
	void processPrivateStream1(RandomAccessFile dataStream) {

		try {

			int temp;
			int [] bits;

			dataStream.skipBytes(4);

			temp = readUnsignedByte(dataStream);

			log.debug("PES packet length:" + temp);

			bits = getBits(temp, 1);
			printBits(bits);

			dataStream.skipBytes(temp+5);

			temp = readUnsignedByte(dataStream);

			bits = getBits(temp, 1);
			printBits(bits);

			temp = readUnsignedByte(dataStream);

			log.debug("byte 1:" + temp);
			bits = getBits(temp, 1);
			printBits(bits);

			temp = readUnsignedByte(dataStream);

			log.debug("byte 2:" + temp);
			bits = getBits(temp, 1);
			printBits(bits);
		}
		catch (Exception e) {
			log.error("", e);
		}
	}


	/**
	 * Gets the last GOP in the file and reads the timecode
	 **/
	void getDuration(RandomAccessFile dataStream) {

		int[] bits;
		int duration = 1;

		try {

			if (getLastGOP(dataStream) == 1) {

				bits = getBits(changeEndianness(readUnsignedInt32(dataStream)), 4);

				duration += getDecimalValue(bits, 30, 26, false)*60*60;
				duration += getDecimalValue(bits, 25, 20, false)*60;
				duration += getDecimalValue(bits, 18, 13, false);
			}

			setDuration(duration);
		}

		catch (Exception e) {
			log.error("", e);
		}
	}


	/**
	 * Starts at the end of the file and finds the first GOP (Group of pictures) and returns
	 **/
	int getLastGOP(RandomAccessFile dataStream) {

		boolean found = false;
		int read;
		long length;

		try {
			length = dataStream.length()-4;

			while(!found) {

				dataStream.seek(length--);
				read = readUnsignedInt32(dataStream);

				if (read == GOP_START_CODE)
					found = true;
			}
		}

		catch (Exception e) {
			log.error("", e);
			return 0;
		}

		return 1;
	}


	/**
	 * Finds the first GOP (Group of pictures) and returns
	 **/
	int getFirstGOP(RandomAccessFile dataStream) {

		boolean found = false;

		try {
			long start = dataStream.getFilePointer();

			int read;

			while(!found) {

				dataStream.seek(start++);

				read = readUnsignedInt32(dataStream);

				if (read == GOP_START_CODE)
					found = true;
			}
		}

		catch (Exception e) {
			log.error("", e);
			return 0;
		}

		return 1;
	}

	/**
	 * Finds the MPEG type (MPEG-1 or MPEG2)
	 **/
	int getType(RandomAccessFile dataStream) {

		int temp = 0;;

		try {
			temp = readUnsignedByte(dataStream);
			int [] bits = getBits(temp, 1);

			switch (getDecimalValue(bits, 7, 6, true)) {

			case 0: videoCodec = "MPEG-1"; break;
			case 1: videoCodec = "MPEG-2"; break;
			default: temp = 0; break;
			}

		} catch (Exception e) {
			log.error("", e);
		}

		return temp;
	}


	/**
	 * Processes the header sequence to obtain the properties...
	 **/
	private boolean processSequenceHeader(RandomAccessFile dataStream) throws Exception {

		/* Reads the needed data... */
		int[] data = readUnsignedBytes(dataStream, 7);

		/* Gets the resolution... */
		setVideoResolution(((data[0] << 4) + ((data[1] >> 4) & 0x0f)) + "x" + (data[2] + ((data[1] & 0x0f) << 8)));

		/* Gets the video rate... */
		switch(data[3] & 0x0f) {
		case(0): {
			return false;
		}
		case(1): setVideoRate("23.976"); break; /* 24000/1001 */
		case(2): setVideoRate("24.000"); break;
		case(3): setVideoRate("25.000"); break;
		case(4): setVideoRate("29.970");  break; /* 30000/1001 */
		case(5): setVideoRate("30.000"); break;
		case(6): setVideoRate("50.000"); break;
		case(7): setVideoRate("59.940");  break; /* 60000/1001 */
		case(8): setVideoRate("60.000"); break;
		}

		/* Gets the video bitrate... (* 400 bits/s) */
		setVideoBitrate("" + Math.round((float)(((data[4] << 10) + (data[5] << 2) + ((data[6] >> 6) & 0x03)) * 400) / 1000F));
		return true;
	}

	/**
	 * Scans the dataStrem until it finds a four-byte sequence starting
	 * with 0x00 0x00 0x01 followed by 0x?? and then returns it.
	 **/
	private int[] getNextStartCode(RandomAccessFile dataStream) throws Exception {
		int i;

		int[] data = new int[4];
		/* Reads the first 3 bytes... */
		for (i=0; i< 3; i++) {
			data[i] = readUnsignedByte(dataStream);
		}

		/* Loops until a 0x00 0x00 0x01 0x?? sequence has been found... */
		boolean found = false;
		do {
			data[i] = readUnsignedByte(dataStream);

			switch(i) {
			case(0): {
				if (data[1] == 0 && data[2] == 0 && data[3] == 1) {
					found = true;
					break;
				}
				i = 1;
				break;
			}
			case(1): {
				if (data[2] == 0 && data[3] == 0 && data[0] == 1) {
					found = true;
					break;
				}
				i = 2;
				break;
			}
			case(2): {
				if (data[3] == 0 && data[0] == 0 && data[1] == 1) {
					found = true;
					break;
				}
				i = 3;
				break;
			}
			case(3): {
				if (data[0] == 0 && data[1] == 0 && data[2] == 1) {
					found = true;
					break;
				}
				i = 0;
				break;
			}
			}

		} while (!found);
		/* Creates a right code... */
		data[3] = data[i];
		data[0] = 0;
		data[1] = 0;
		data[2] = 1;
		/* Retuns it... */
		return data;
	}
}


















