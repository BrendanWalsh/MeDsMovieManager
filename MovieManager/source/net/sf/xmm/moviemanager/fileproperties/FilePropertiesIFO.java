/**
 * @(#)FilePropertiesIFO.java 1.0 06.06.05 (dd.mm.yy)
 *
 * Copyright (2003) Bro3
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License aloSng with 
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Boston, MA 02111.
 * 
 * Contact: bro3@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.fileproperties;

import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.util.FileUtil;

class FilePropertiesIFO extends FileProperties {

	Logger log = Logger.getLogger(getClass());
	
	//private final int DVDVIDEO_VMG = 0x474d56; /* 'VMG' */

	private final int DVDVIDEO_VTS = 0x535456; /* 'VTS' */

	//private final int VIDEO_OFFSET = 0x100;

	//private final int[] SEQUENCE_HEADER_CODE = {0x00, 0x00, 0x01, 0xb3};

	//private final int[] PRIVATE_STREAM1_CODE = {0x00, 0x00, 0x01, 0xbd};

	private final int SEQUENCE_HEADER = 0x000001b3;

	private final int PRIVATE_STREAM1 = 0x000001bd;

	private final int SIZE = 100000;

	/**
	 * Processes a file from the given DataInputStream.
	 **/
	protected void process(RandomAccessFile dataStream) throws Exception {
		log.info("Start processing IFO file.");

		byte [] ifoFile = new byte[SIZE+10];

		/* 4 bytes has already been read in FilePropertiesMovie, 
	   therefore the first byte read is stored in index 4*/
		dataStream.read(ifoFile, 4, SIZE);

		/* Gets the stream type... (4 bytes) */
		int streamType = readUnsignedInt32(ifoFile, 9);

		if (streamType == DVDVIDEO_VTS) {

			supported = true;
			processIfoFile(ifoFile);
			setContainer("VOB");

			log.info("Processing IFO file done.");
		}
		else
			log.info("IFO type not supported.");
	}

	void processIfoFile(byte [] ifoFile) throws Exception {

		getRuntime(ifoFile);
		getVideoAttributes(ifoFile);
		getAudioAttributes(ifoFile);
		getSubtitles(ifoFile);
	}


	/* Currently not used */
	void findHeader(byte [] ifoFile) {

		int temp;

		try {

			for (int i = 0; i < ifoFile.length - 3; i++) {

				temp = readUnsignedInt32(ifoFile, i);

				if (temp == SEQUENCE_HEADER)
					log.debug("SEQUENCE_HEADER");
				else if (temp == PRIVATE_STREAM1)
					log.debug("PRIVATE_STREAM1");
			}

		} catch (Exception e) {
			log.error("Exception:" + e);
		}
		log.debug("findHeader end");
	}
	
	/**
	 * Fix by gaelead to make the runtime correct
	 * @param dataByte
	 * @return
	 */
	public static int encode(byte dataByte) { 
		StringBuffer builder = new StringBuffer(); 

//		A byte stands for 2 hex characters 
		builder.append(Integer.toHexString((dataByte & 0xF0) >> 4)); 
		builder.append(Integer.toHexString(dataByte & 0x0F)); 

		return Integer.parseInt(builder.toString()); 
	} 


	void getRuntime(byte [] ifoFile) throws Exception {

		int [] runtime;

		/* sector pointer to VTS_PGCI (Title Program Chain table) Offset 204 */
		int sectorPointerVTS_PGCI = changeEndianness(readUnsignedInt32(ifoFile, 204));

		/* Offset value of the VTS_PGCITI (Video title set program chain information table) */
		int offsetVTS_PGCI = sectorPointerVTS_PGCI*2048;

		int numberOfTitles = readUnsignedInt16(ifoFile, offsetVTS_PGCI + 1);

		runtime = new int[numberOfTitles];

		//int startByteVTS_PGCI = offsetVTS_PGCI;

		int pointer = offsetVTS_PGCI;
		int startcode = changeEndianness(readUnsignedInt32(ifoFile, offsetVTS_PGCI + 12));

		offsetVTS_PGCI += 12;

		for (int i = 0; i < numberOfTitles; i++) {

			runtime[i] = 0;

			runtime[i] += encode(ifoFile[pointer + startcode + 4]) * 60 * 60; /* Hours */ 
			runtime[i] += encode(ifoFile[pointer + startcode + 5]) * 60; /* Minutes */ 
			runtime[i] += encode(ifoFile[pointer + startcode + 6]); /* Seconds */ 

			int [] bits = getBits(ifoFile[pointer + startcode + 7], 1);

			//printBits(bits);

			switch (getDecimalValue(bits, 7, 6, false)) {

			case 1: {
				setVideoRate("25.000");
				/*25 fps*/
				break;
			}
			case 3: {
				setVideoRate("30.000");
				/*30 fps*/
				break;
			}

			default:
				log.debug("Illigal framerate");
			/*Illigal*/
			break;
			}

			/* Encrease by 8 to get to the next startcode */
			offsetVTS_PGCI += 8;
			startcode = changeEndianness(readUnsignedInt32(ifoFile, offsetVTS_PGCI));
		}

		setDuration(runtime[0]);
	}


	void getVideoAttributes(byte [] ifoFile) throws Exception{

		/* Offset 0x0200 */
		int read = readUnsignedByte(ifoFile, 512);

		int [] bits = getBits(read, 1);

		String videoCodingMode = getDecimalValue(bits, 7, 6, false) == 0 ? "MPEG-1" : "MPEG-2";

		String standard = getDecimalValue(bits, 5, 4, false) == 0 ? "NTSC 525/60" : "PAL 625/50";

		String aspect;
		int asepctValue = getDecimalValue(bits, 3, 2, false);

		if (asepctValue == 1 || asepctValue == 2)
			aspect = "";
		else
			aspect = asepctValue == 0 ? "4:3" : "16:9";

		String automaticDisplayMode = "";

		if (getDecimalValue(bits, 1, 1, false) == 0)
			automaticDisplayMode = "pan-scan";

		if (getDecimalValue(bits, 0, 0, false) == 0) {
			if (!automaticDisplayMode.equals(""))
				automaticDisplayMode += " & ";
			automaticDisplayMode += "letterboxed";
		}

		if (automaticDisplayMode.equals(""))
			automaticDisplayMode = "Not specified";

		setVideoCodec(videoCodingMode);

		/* Offset 0x0201 */
		read = readUnsignedByte(ifoFile, 513);
		bits = getBits(read, 1);

		String bitRateMode = getDecimalValue(bits, 5, 5, false) == 0 ? "VBR" : "CBR";
		int res = getDecimalValue(bits, 4, 3, false);
		String resolution = "";

		switch (res) {

		case 0: {
			if (standard.equals("NTSC 525/60"))
				resolution = "720x480";
			else
				resolution = "720x576";
			break;
		}
		case 1:{
			if (standard.equals("NTSC 525/60"))
				resolution = "704x480";
			else
				resolution = "704x576";
			break;
		}

		case 2:{
			if (standard.equals("NTSC 525/60"))
				resolution = "352x480";
			else
				resolution = "352x576";
			break;
		}

		case 3:{
			if (standard.equals("NTSC 525/60"))
				resolution = "352x240";
			else
				resolution = "352x288";
			break;
		}
		}

		String letterboxed = getDecimalValue(bits, 2, 2, false) == 0 ? "no" : "yes";
		String mode = getDecimalValue(bits, 0, 0, false) == 0 ? "camera" : "film";

		setVideoResolution(resolution);
	}


	void getAudioAttributes(byte [] ifoFile) throws Exception{


		int numberOfAudioStreams;
		int offset = 516;
		int read;
		int [] bits;

		/* Offset 0x0202 */
		numberOfAudioStreams = getUnsignedInt16(readUnsignedByte(ifoFile, 514), readUnsignedByte(ifoFile, 515));

		/*Audio atributes*/

		for (int i = 0; i < numberOfAudioStreams; i++) {

			/* Offset 0x0204 */
			read = readUnsignedByte(ifoFile, offset++);
			bits = getBits(read, 1);

			int audCodingMode = getDecimalValue(bits, 7, 5, false);
			String audioCodingMode = "";

			switch (audCodingMode) {

			case 0: audioCodingMode = "AC3"; break;
			case 2: audioCodingMode = "Mpeg-1"; break;
			case 3: audioCodingMode = "Mpeg-2ext"; break;
			case 4: audioCodingMode = "LPCM"; break;
			case 6: audioCodingMode = "DTS"; break;
			}

			setAudioCodec(audioCodingMode);

			int languageType = getDecimalValue(bits, 3, 2, false);

			String applicationMode = "";
			int appMode = getDecimalValue(bits, 1, 0, false);

			switch (appMode) {
			case 0: applicationMode = "Unspecified"; break;
			case 1: applicationMode = "Karaoke"; break;
			case 2: applicationMode = "Surround"; break;
			}

			/* Offset 0x0204 */
			read = readUnsignedByte(ifoFile, offset++);
			bits = getBits(read, 1);

			int q_DRC = getDecimalValue(bits, 7, 6, false);
			String quantization_DRC = "Unspecified";

			switch (q_DRC) {

			case 0: {
				quantization_DRC = "16bps"; 
				break;
			}
			case 1: {
				quantization_DRC = "20bps"; 
				break;
			}
			case 2: {
				quantization_DRC = "24bps"; 
				break;
			}
			case 3: {
				quantization_DRC = "DRC"; 
				break;
			}
			}

			String sampleRate = getDecimalValue(bits, 5, 4, false) == 0 ? "48000" : "96000";

			setAudioRate(sampleRate);

			int numberOfAudioChannels = getDecimalValue(bits, 2, 0, false);

			setAudioChannels(String.valueOf(numberOfAudioChannels));

			String languageCode = "";
			String codeExtension = "";

			if (languageType == 1) {

				read = readUnsignedInt16(ifoFile, offset);
				languageCode += fromByteToAscii(read, 2);

				offset += 6;

				switch (read) {
				case 0: codeExtension = "unspecified"; break;
				case 1: codeExtension = "normal"; break;
				case 2: codeExtension = "for visually impaired"; break;
				case 3: codeExtension = "director's comments"; break;
				case 4: codeExtension = "alternate director's comments"; break;
				}
			}
		}

		log.info("process IFO File Done");
	}

	void getSubtitles(byte [] ifoFile) {

		int read;
		int offset = 598;
		int numberOfSubtitleStreams;
		int [] bits;
		String languageCode;
		String subtitles = "";

		try {

			numberOfSubtitleStreams = getUnsignedInt16(readUnsignedByte(ifoFile, 596), readUnsignedByte(ifoFile, 597));

			for (int i = 0; i < numberOfSubtitleStreams; i++) {

				languageCode = "";

				read = readUnsignedByte(ifoFile, offset);

				bits = getBits(read, 1);

				if (getDecimalValue(bits, 1, 0, false) == 1) {
					offset += 2;

					read = readUnsignedInt16(ifoFile, offset);
					languageCode += fromByteToAscii(read, 2);

					if (!subtitles.equals(""))
						subtitles += ", ";

					subtitles += findName(new InputStreamReader(FileUtil.getResourceAsStream("/codecs/LanguageCodes.txt"), "UTF-8"), new String(languageCode));
					offset += 4;
				}
				else {
					offset += 6;
				}
			}
			setSubtitles(subtitles);

		} catch (Exception e) {
			log.error("", e);
		}
	}
}
