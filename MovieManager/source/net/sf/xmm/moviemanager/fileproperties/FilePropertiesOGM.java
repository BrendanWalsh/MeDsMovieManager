/**
 * @(#)FilePropertiesOGM.java 1.0 26.01.06 (dd.mm.yy)
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

import java.io.RandomAccessFile;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.util.FileUtil;

class FilePropertiesOGM extends FileProperties {

	Logger log = Logger.getLogger(getClass());
	
	//private final int Ogm = 0x4f676753;
	private final int End = 0x0373;
	private final int vide = 0x65646976;
	private final int audi = 0x69647561;
	private final int vorb = 0x62726f76;
	private final int text = 0x74786574;
	private final int OggS = 0x5367674f;
	//private final int OggS1 = 0x4f676753;
	private final int ffff = 0xffffffff; //(����)

	private float frameRate;

	private boolean videoStream = false;

	/**
	 * Processes a file from the given RandomAccessFile.
	 **/
	protected void process(RandomAccessFile dataStream) throws Exception {

		log.info("Start processing OGM file.");

		supported = true;

		dataStream.seek(0);

		getInfo(dataStream);
		setDuration(getDuration(dataStream));

		if (videoStream)
			calculateVideoBitrate(dataStream.length());

		setContainer("OGM");

		log.info("Done processing OGM file.");
	}

	private void getInfo(RandomAccessFile dataStream) throws Exception {

		int videoFccHandler;
		int audioFccHandler;
		int type;

		int twoByteCheck;

		String audioCodecs = "";
		String audioSampleRate = "";
		String audioBitrate = "";
		String audioChannels = "";
		boolean quit = false;
		int streamCounter = 0; /* Counts audio and subtitle streams */
		int safety = 1500;

		if (dataStream.length() < safety)
			safety = (int) dataStream.length();

		while (!quit && --safety > 0) {

			if (readUnsignedByte(dataStream) == 0x4f && readUnsignedByte(dataStream) == 0x67 && readUnsignedByte(dataStream) == 0x67 && readUnsignedByte(dataStream) == 0x53) {

				skipBytes(dataStream, 1);

				if (readUnsignedByte(dataStream) == 2) {

					readUnsignedInt32(dataStream);
					skipBytes(dataStream, 4);
					/* Not used */
					int streamType = readUnsignedByte(dataStream);

					skipBytes(dataStream, 12);

					twoByteCheck = readUnsignedInt16(dataStream);
					type = readUnsignedInt32(dataStream);

					if (twoByteCheck == End) {
						break;
					}

					/* Video info */
					if (type == vide) {

						videoStream = true;

						skipBytes(dataStream, 4);
						videoFccHandler = readUnsignedInt32(dataStream);

						String videoCodec = findName(FileUtil.getResourceAsStream("/codecs/FOURCCvideo.txt"), fromByteToAscii(videoFccHandler, 4));

						setVideoCodec(videoCodec);

						skipBytes(dataStream, 4);

						frameRate = ((float)10000000/(float)readUnsignedInt32(dataStream))*1000;

						frameRate = (float) ((Math.ceil((double) frameRate))/1000);

						setVideoRate(String.valueOf(frameRate));

						skipBytes(dataStream, 24);

						int dwWidth = readUnsignedInt32(dataStream);
						int dwHeight = readUnsignedInt32(dataStream);

						setVideoResolution(dwWidth+"x"+dwHeight);
					}

					/* Audio info */
					if (type == audi) {

						streamCounter++;

						skipBytes(dataStream, 4);
						audioFccHandler = readUnsignedInt32(dataStream);

						if (!audioCodecs.equals(""))
							audioCodecs += ", ";
						audioCodecs += findName(FileUtil.getResourceAsStream("/codecs/FOURCCaudio.txt"), "0x"+fromByteToAscii(audioFccHandler, 4));
						skipBytes(dataStream, 12);
						float sampleRate = readUnsignedInt32(dataStream);

						if (!audioSampleRate.equals(""))
							audioSampleRate += ", ";
						audioSampleRate += "" + (int) sampleRate;

						skipBytes(dataStream, 16);

						int channels = readUnsignedByte(dataStream);

						if (!audioChannels.equals(""))
							audioChannels += ", ";

						audioChannels += "" + channels;

						skipBytes(dataStream, 3);

						int bitRate = readUnsignedInt16(dataStream)/1000;

						if (!audioBitrate.equals(""))
							audioBitrate += ", ";
						audioBitrate += "" + bitRate*8;
					}

					if (type == vorb) {

						streamCounter++;

						if (!audioCodecs.equals(""))
							audioCodecs += ", ";
						audioCodecs += "Vorbis";

						skipBytes(dataStream, 6);

						int channels = readUnsignedByte(dataStream);

						if (!audioChannels.equals(""))
							audioChannels += ", ";
						audioChannels += "" + channels;

						float sampleRate = readUnsignedInt16(dataStream);

						if (!audioSampleRate.equals(""))
							audioSampleRate += ", ";
						audioSampleRate += "" + (int) sampleRate;

						skipBytes(dataStream, 6);

						int bitRate = readUnsignedInt32(dataStream)/1000;

						if (!audioBitrate.equals(""))
							audioBitrate += ", ";
						audioBitrate += "" + bitRate;
					}

					/* Subtitles */
					if (type == text)
						streamCounter++;

				} 
				else {

					skipBytes(dataStream, 8);
					type = readUnsignedByte(dataStream);
					skipBytes(dataStream, 14);
					int streamType = readUnsignedInt32(dataStream);

					if (type != 0) {

						/* Subtitles */
						if (streamType == vorb) {

							String subtitles = getSubtitles();

							if (!subtitles.equals(""))
								subtitles += ", ";

							subtitles += getSubtitle(dataStream);

							setSubtitles(subtitles);
							streamCounter--;
						}
						else if (streamType == ffff) { /* Audio */
							streamCounter--;
						}
					}
					else {

						if (streamCounter == 0) {
							getExtendedCodecInfo(dataStream, 100);
							quit = true;
						}
					}
				}
			}
		}

		setAudioCodec(audioCodecs);
		setAudioRate(audioSampleRate);
		setAudioBitrate(audioBitrate);
		setAudioChannels(audioChannels);
	}

	String getSubtitle(RandomAccessFile dataStream) {

		int temp;
		String subtitle = "";
		int counter = 0;

		try {

			while (counter++ < 100) {

				temp = readUnsignedByte(dataStream);

				if (temp == 0x4c && readUnsignedByte(dataStream) == 0x41 && readUnsignedByte(dataStream) == 0x4e && readUnsignedByte(dataStream) == 0x47) {
					skipBytes(dataStream, 5);

					temp = readUnsignedByte(dataStream);

					/* 0x5b == [ */
					while (temp != 0x01 && temp != 0x5b) {
						subtitle += fromByteToAscii(temp, 1);
						temp = readUnsignedByte(dataStream);
					}
					return subtitle;
				}		
				else if (readUnsignedByte(dataStream) == 0x4f && readUnsignedByte(dataStream) == 0x67 && readUnsignedByte(dataStream) == 0x67 && readUnsignedByte(dataStream) == 0x53) {

					dataStream.seek(dataStream.getFilePointer()-4);
					return "";
				}
			}
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}
		return "";
	}

	private void calculateVideoBitrate(long fileSize) { 

		/*Calculates the size of the video only and then calculates the videoBitrate/kbps.*/
		int audioSize = 0;
		int audioBitrate;

		StringTokenizer string = new StringTokenizer(getAudioBitrate(), ", ");

		while (string.hasMoreTokens()) {
			audioBitrate = Integer.parseInt(string.nextToken()); /*Audio Bit Rate in kbit/s*/
			audioSize += ((audioBitrate*getDuration())/8)*1000;
		}

		if (getDuration() > 0) {
			int videoBitrate = ((int) ((fileSize - audioSize)/getDuration())/1000)*8; /*Video bitrate kbit/s*/
			setVideoBitrate(String.valueOf(videoBitrate));
		}
	}

	/**
	 * Starts at the end of the file and finds the first OggS (Video) and returns
	 **/
	private int getDuration(RandomAccessFile dataStream) {

		boolean found = false;
		int read;
		long length;

		int duration = 0;

		try {
			/* Starts at end */
			length = dataStream.length()-4;

			while(!found && length > 0) {

				dataStream.seek(length--);
				read = readUnsignedInt32(dataStream);

				if (read == OggS) {
					skipBytes(dataStream, 2);

					duration = readUnsignedInt32(dataStream);
					skipBytes(dataStream, 4);

					if (readUnsignedByte(dataStream) == 0) /* 0 for video */
						found = true;
				}
			}
		}

		catch (Exception e) {
			log.error("", e);
			return 0;
		}

		return (int) ((float)duration/frameRate);
	}
}
