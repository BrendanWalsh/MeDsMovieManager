/**
 * @(#)FilePropertiesRIFF.java 1.0 26.01.06 (dd.mm.yy)
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
 * You should have received a copy of the GNU General Public License aloSng with 
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Boston, MA 02111.
 * 
 * Contact: mediterranean@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.fileproperties;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.util.FileUtil;

class FilePropertiesRIFF extends FileProperties {

	Logger log = Logger.getLogger(getClass());
	
	private final int RIFF_AVI = 0x20495641;

	private final int AVI_LIST = 0x5453494c;

	private final int AVI_movi = 0x69766f6d;

	private final int AVI_avih = 0x68697661;

	private final int AVI_dmlh = 0x686C6D64;

	private final int AVI_avih_SIZE = 0x00000038;

	private final int AVI_strh = 0x68727473;

	private final int AVI_strh_SIZE = 0x00000038;

	private final int AVI_vids = 0x73646976;  

	private final int AVI_auds = 0x73647561;//61756473 <-- reverse

	private final int AVI_strf = 0x66727473;

	private final int AVI_INFO = 0x4f464e49;

	//private final int AVI_INFO2 = 0x494e464f;// 4f464e49 <-- reverse

	private int fccHandler1;
	private boolean checkForSpecifiedCodecInfo = false;

	private boolean extendedCodecInfoFound = false;
	private int extendedCodecInfoChunkCounter = 0;

	private boolean header = true;

	/* Not always correct or valid... */
	private int lastSubChunk = 0;

	private long videoAudioStreamSize = 0;

	private boolean quit = false;

	private String audioCodec = "";
	private String audioChannels = "";
	private String audioRate = "";
	private String audioBitrate = "";

	/* Is needed if there is a dmlh chunk (openDML AVI/Extended AVI/AVI2) */
	private int dwMicroSecPerFrame; 

	private final int AVI_JUNK = 0x4b4e554a;

	// Unused (but that may exist)...
	//private final int AVI_idx1 = 0x31786469;
	//private final int AVI_strn = 0x6e727473;



	/**
	 * Processes a file from the given DataInputStream.
	 **/
	protected void process(RandomAccessFile dataStream) throws Exception {
		log.info("Start processing RIFF file.");

		dataStream.seek(4);

		/* Gets the stream size... (4 bytes) */          
		int streamSize = readUnsignedInt32(dataStream);

		/* Gets the stream type... (4 bytes) */
		int streamType = readUnsignedInt32(dataStream);

		/* If not RIFF_AVI, returns...*/
		if (streamType == RIFF_AVI) {


			supported = true;
			setContainer("AVI");

			/* Processes the AVI chunks... */
			processAviChunks(dataStream, streamSize);

			setAudioCodec(audioCodec);
			/* Sets the audio channels... */
			setAudioChannels(audioChannels);
			/* Sets the audio rate... */
			setAudioRate(audioRate);
			/* Sets the audio bit rate... */
			setAudioBitrate(audioBitrate);

			calculateVideoBitrate();

			log.info("Processing RIFF file done.");
		} else {
			log.info("RIFF file format not supported.");
		}
	}

	/**
	 * Processes n bytes of the AVI chunk.
	 **/
	private void processAviChunks(RandomAccessFile dataStream, int bytesToProcess) throws Exception {

		int chunkType;
		int chunkSize;
		int safety = 0;
		int n = bytesToProcess;


		while (n > 0 && !quit && safety++ < 100000) {

			/* Gets the chunk type...*/
			chunkType = readUnsignedInt32(dataStream);

			n -= 4;
			/* Gets the chunk size... */
			chunkSize = readUnsignedInt32(dataStream);

			n -= 4;
			/* processes this chunk... */
			n -= chunkSize;

			if (header) {

				switch (chunkType) {

				case(0): {
					// chunkType should never be 0
					errorOccured = true;
				}

				case(AVI_LIST): {

					chunkType = readUnsignedInt32(dataStream);

					/* If AVI_movi, the header is finished*/
					if (chunkType == AVI_movi) {

						/* No longer processing the header */
						header = false;

						/* A very aproximate test to check if the AVI_movi chunk size isn't wrong*/
						if (chunkSize < (dataStream.length()*0.7) || 
								chunkSize > dataStream.length())
							videoAudioStreamSize = dataStream.length();
						else
							videoAudioStreamSize = chunkSize;

						processAviChunks(dataStream, chunkSize-4);

					} else if (chunkType == AVI_INFO) {
						chunkSize = correctChunkSize(chunkSize);
						processMetaTags(dataStream, chunkSize-4);

					} else {
						processAviChunks(dataStream, chunkSize-4);
					}

					break;
				}
				case(AVI_avih): {

					if (chunkSize != AVI_avih_SIZE) {
						throw new Exception("RIFF file corrupted (avih chunk size is "+chunkSize+" and not 0x38 as expected).");
					}

					processAviAvih(dataStream, chunkSize);
					break;
				}

				case(AVI_strh): {

					if (chunkSize != AVI_strh_SIZE) {
						log.warn("RIFF file may be corrupted (strh chunk size is "+chunkSize+" and not 0x38 as expected).");
					}

					/* Get the sub chunk type... */
					int subChunk = readUnsignedInt32(dataStream);
					/* Processes according to the type... */

					switch(subChunk) {
					case(AVI_vids): {
						processAviVids(dataStream, chunkSize-4);
						lastSubChunk = AVI_strf;
						break;
					}
					case(AVI_auds): {
						/* Discards... */
						if (!skipBytes(dataStream, chunkSize-4))
							return;

						lastSubChunk = AVI_auds;
						break;
					}
					default: {
						/* Discards... */
						if (!skipBytes(dataStream, chunkSize-4))
							return;
						break;
					}
					}
					break;
				}
				case(AVI_strf): {

					switch (lastSubChunk) {

					case(AVI_strf): {
						processAviCodec(dataStream, chunkSize);
						break;
					}

					case(AVI_auds): {
						processAviSound(dataStream, chunkSize);
						break;
					}
					default: {
						/* Discards... */
						if (!skipBytes(dataStream, chunkSize))
							return;
						break;
					}
					}
					lastSubChunk = 0;
					break;
				}
				case(AVI_dmlh): {

					/* Extended AVI header (openDML AVI) */
					/* To get correct duration the total number of frames must be grabbed here. */

					setContainer("AVI (OpenDML)");

					int dwTotalFrames = readUnsignedInt32(dataStream);

					if (!skipBytes(dataStream, chunkSize-4))
						return;

					if (dwMicroSecPerFrame > 0 && dwTotalFrames > 0) {
						setDuration(Math.round(((float)dwTotalFrames / 1000F) * ((float)dwMicroSecPerFrame / 1000F)));
					}
					break;
				}
				case(AVI_JUNK): {

					if (chunkSize >= 4) {
						/* Identifying the Divx container.*/
						if (fromByteToAscii(readUnsignedInt32(dataStream), 4).equals("DivX"))
							setContainer("DivX");

						if (!skipBytes(dataStream, chunkSize-4))
							return;
					}

					break;
				}
				default: {

					/* Discards... */
					chunkSize = correctChunkSize(chunkSize);

					if (!skipBytes(dataStream, chunkSize)) {
						return;
					}

					break;
				}
				}
			}

			/*If done processing the header*/
			else if (checkForSpecifiedCodecInfo) {

				extendedCodecInfoChunkCounter++;

				/* 100000 is an approximate value to prevent the whole file from being parsed */
				if (chunkSize < 0 || dataStream.getFilePointer() > 100000) {
					quit = true;
				}
				chunkSize = correctChunkSize(chunkSize);

				/* if codec is either xvid or DivX5 the extended codec info will be extracted from the video stream.*/
				if (!quit) {

					/* Sometimes the chunksize is so huge it uses many minutes to process,
					   usually a sign of no usefull info available.
					   Setting limit to 500KB */

					if (chunkSize > 500000) {

						if (extendedCodecInfoChunkCounter > 60) {
							quit = true;
						}
						else {
							if (!skipBytes(dataStream, chunkSize))
								return;
						}
					}
					else
						extendedCodecInfoFound = getExtendedCodecInfo(dataStream, chunkSize);

					if (extendedCodecInfoFound) {
						quit = true;
					}
				}
				else {
					quit = true;
				}
			}
			else {
				quit = true;
			}
		}
	}

	/**
	 * Processes the AVI avih chunk.
	 **/
	private void processAviAvih(RandomAccessFile dataStream, int chunkSize) throws Exception {
		/* Gets the dwMicroSecPerFrame... */
		dwMicroSecPerFrame = readUnsignedInt32(dataStream);

		/* Gets the dwMaxBytesPerSec... (not used) */
		int dwMaxBytesPerSec = readUnsignedInt32(dataStream);

		/* Slips unwanted info (discarded)... */
		skipBytes(dataStream,8);
		/* Gets the dwTotalFrames... */
		int dwTotalFrames = readUnsignedInt32(dataStream);

		/* Skips unwanted info (discarded)... */
		skipBytes(dataStream,12);
		/* Gets the dwWidth... */
		int dwWidth = readUnsignedInt32(dataStream);

		/* Gets the dwHeight (discarded)... */
		int dwHeight = readUnsignedInt32(dataStream);

		/* Skips unwanted info (discarded)... */
		skipBytes(dataStream,chunkSize-40);
		/* Sets the duration... */
		if (dwMicroSecPerFrame > 0) {
			setDuration(Math.round(((float)dwTotalFrames / 1000F) * ((float)dwMicroSecPerFrame / 1000F)));
		}
		/* Sets the resolution... */
		setVideoResolution(dwWidth+"x"+dwHeight);
	}

	/**
	 * Processes of avi_vids sub chunk.
	 **/
	private void processAviVids(RandomAccessFile dataStream, int chunkSize) throws Exception {

		/* Gets the fccHandler... */
		fccHandler1 = readUnsignedInt32(dataStream);

		/* Skips unwanted info (discarded)... */
		skipBytes(dataStream,12);
		/* Gets the dwScale... */
		int dwScale = readUnsignedInt32(dataStream);
		/* Gets the dwRate... */
		int dwRate = readUnsignedInt32(dataStream);
		/* Skips unwanted info (discarded)... */
		skipBytes(dataStream,chunkSize-24);

		/* Sets the video rate... */
		setVideoRate((dwRate / dwScale) + "." + ((long)dwRate * 1000 / dwScale - dwRate / dwScale * 1000));
	}


	/**
	 * Processes of avi_strf on the same sub chuck that avi_auds.
	 **/
	private void processAviSound(RandomAccessFile dataStream, int chunkSize) throws Exception {

		/* Gets the wFormatTag... */
		int wFormatTag = readUnsignedInt16(dataStream);
		/* Gets the nChannels... */
		int nChannels = readUnsignedInt16(dataStream);
		/* Gets the nSamplesPerSec... */
		int nSamplesPerSec = readUnsignedInt32(dataStream);
		/* Gets the nAvgBytesPerSec... */
		int nAvgBytesPerSec = readUnsignedInt32(dataStream);
		/* Skips unwanted info (discarded)... */
		skipBytes(dataStream,chunkSize-12);

		if (!audioCodec.equals(""))
			audioCodec += ", ";
		audioCodec += getAudioCodecName(wFormatTag);

		if (!audioChannels.equals(""))
			audioChannels += ", ";
		audioChannels += String.valueOf(nChannels);

		if (!audioRate.equals(""))
			audioRate += ", ";
		audioRate += ""+nSamplesPerSec;

		if (!audioBitrate.equals(""))
			audioBitrate += ", ";
		audioBitrate += "" + Math.round(((float)nAvgBytesPerSec) * 8F / 1000F);
	}


	/**
	 * Gets the audio codec name from file based on the id.
	 **/
	private String getAudioCodecName(int id) throws Exception {

		/* Transforms the id in a string... */
		StringBuffer buffer = new StringBuffer("0x");
		String value = Integer.toHexString(id);
		int i = 4 - value.length();
		while (i-- > 0) {
			buffer.append('0');
		}
		buffer.append(value);
		return findName(FileUtil.getResourceAsStream("/codecs/FOURCCaudio.txt"), buffer.toString());
	}


	private void calculateVideoBitrate() { 

		/*Calculates the size of the video only and then calculates the videoBitrate/kbps.*/
		int audioSize = 0;
		int audioBitrate;

		StringTokenizer string = new StringTokenizer(getAudioBitrate(), ", ");

		while (string.hasMoreTokens()) {
			audioBitrate = Integer.parseInt(string.nextToken()); /*Audio Bit Rate in kbit/s*/
			audioSize += ((audioBitrate*getDuration())/8)*1000;
		}

		if (getDuration() > 0) {
			int videoBitrate = ((int) ((videoAudioStreamSize - audioSize)/getDuration())/1000)*8; /*Video rate kbit/s*/
			setVideoBitrate(String.valueOf(videoBitrate));
		}
	}


	private void processAviCodec(RandomAccessFile dataStream, int chunkSize) throws Exception {

		int fccHandler;
		int fccHandler2;

		skipBytes(dataStream, 16);

		fccHandler2 = readUnsignedInt32(dataStream);
		fccHandler = fccHandler2;

		skipBytes(dataStream, chunkSize - 20);

		/*DivX 3 Low and High motion is identified by the first fourcc.
		 *Other codecs is identified by the second fourcc code*/

		if ((fromByteToAscii(fccHandler1, 4).toUpperCase().equals("DIV3")) ||
				(fromByteToAscii(fccHandler1, 4).toUpperCase().equals("DIV4")))
			fccHandler = fccHandler1;

		if ((fromByteToAscii(fccHandler2, 4).toUpperCase().equals("DX50")) ||
				(fromByteToAscii(fccHandler2, 4).toUpperCase().equals("XVID"))) {
			checkForSpecifiedCodecInfo = true;
		}
		if (fccHandler == 0 && fccHandler1 != 0)
			fccHandler = fccHandler1;

		String codecName = fromByteToAscii(fccHandler, 4).toUpperCase();

		codecName  = findName(FileUtil.getResourceAsStream("/codecs/FOURCCvideo.txt"), codecName);

		setVideoCodec(codecName);
	}


	int correctChunkSize(int chunkSize) {

		int m = chunkSize % 2;
		if (m != 0)
			chunkSize += (2-m);

		return chunkSize;
	}


	void processMetaTags(RandomAccessFile dataStream, int chunkSize) throws Exception {

		//  private final int META_INAM = 0x4d414e49; /*Name*/
		//  	private final int META_ICOP = 0x504f4349; /*Copyright*/
		//  	private final int META_ILNG = 0x474e4c49; /*Language*/
		//  	private final int META_ICMT = 0x544d4349; /*Comment*/
		//  	private final int META_ISFT = 0x54465349; /*Software*/
		//  	private final int META_IMUS = 0x53554d49; /*Music*/
		//  	private final int META_ICNT = 0x49; /*Country*/
		//  	private final int META_IDST = 0x49; /*Distributed By*/
		//  	private final int META_IEDT = 0x49; /*Edited By*/
		//  	private final int META_IPRO = 0x49; /*Produced By*/
		//  	private final int META_IPDS = 0x49; /*Prod Designer*/
		//  	private final int META_ICNM = 0x49; /*Cinematographer*/
		//  	private final int META_IMIU = 0x49; /*Produced By*/
		//  	private final int META_I = 0x49; /*Produced By*/
		//  	private final int META_I = 0x49; /*Produced By*/
		//  	private final int META_I = 0x49; /*Produced By*/

		final int JUNK = 0x4b4e554a;

		String metaTagInfo = "";

		ArrayList<String> metaData = new ArrayList<String>();

		int metaChunkType;
		int metaChunkSize;

		while (chunkSize > 0) {

			metaTagInfo = "";

			/* Gets the chunk type...*/
			metaChunkType = readUnsignedInt32(dataStream);

			chunkSize -= 4;
			/* Gets the chunk size... */
			metaChunkSize = readUnsignedInt32(dataStream);

			chunkSize -= 4;	    

			/* Don't think this test is necessary */
			if (metaChunkType == JUNK)
				break;

			if (metaChunkSize > 0) {

				metaChunkSize = correctChunkSize(metaChunkSize);

				for (int i = 0; i < metaChunkSize; i++) {
					metaTagInfo +=  fromByteToAscii(readUnsignedByte(dataStream), 1);
					chunkSize--;
				}
				metaData.add(fromByteToAscii(metaChunkType, 4)+":" + metaTagInfo.trim());
			}
		}
		setMetaData(metaData);
	}
}
