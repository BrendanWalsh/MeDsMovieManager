/**
 * @(#)FileProperties.java 1.0 26.01.06 (dd.mm.yy)
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.StringTokenizer;

import net.sf.xmm.moviemanager.util.FileUtil;

import org.apache.log4j.Logger;

abstract class FileProperties {

	Logger log = Logger.getLogger(getClass());

	protected boolean supported = false;
	protected boolean errorOccured = false;

	public String toString() {
		return getClass().getName();
	}
	
	/**
	 * The subtitles.
	 **/
	private String _subtitles = "";

	/**
	 * The resolution (width x heigth).
	 **/
	private String _videoResolution = "";

	/**
	 * The video codec.
	 **/
	private String _videoCodec = "";

	/**
	 * video codec library identifier.
	 **/
	private String _libIdentifier = "";

	/**
	 * The video rate (fps).
	 **/
	private String _videoRate = "";

	/**
	 * The video bit rate (kbps).
	 **/
	private String _videoBitrate = "";

	/**
	 * The video duration (seconds).
	 **/
	private int _duration = -1;

	/**
	 * The audio codec.
	 **/
	private String _audioCodec = "";

	/**
	 * The audio rate (Hz).
	 **/
	private String _audioRate = "";

	/**
	 * The audio bit rate (kbps).
	 **/
	private String _audioBitrate = "";

	/**
	 * The audio channels.
	 **/
	private String _audioChannels = "";

	/**
	 * The audio channels.
	 **/
	private String _container = "";

	/**
	 * The media Type.
	 **/
	private String _mediaType = "";

	/**
	 * list of meta data.
	 **/
	private ArrayList<String> metaData;

	protected String filePath = "";

	/**
	 * Returns the subtitles.
	 **/
	protected String getSubtitles() {
		return _subtitles;
	}

	/**
	 * Returns the resolution.
	 **/
	protected String getVideoResolution() {
		return _videoResolution;
	}

	/**
	 * Returns the video codec.
	 **/
	protected String getVideoCodec() {
		return _videoCodec;
	}

	/**
	 * Returns the video codec.
	 **/
	protected String getVideoCodecLibraryIdentifier() {
		return _libIdentifier;
	}


	/**
	 * Returns the video rate.
	 **/
	protected String getVideoRate() {
		return _videoRate;
	}

	/**
	 * Returns the video bit rate.
	 **/
	protected String getVideoBitrate() {
		return _videoBitrate;
	}

	/**
	 * Returns the duration.
	 **/
	protected int getDuration() {
		return _duration;
	}

	/**
	 * Returns the audio codec.
	 **/
	protected String getAudioCodec() {
		return _audioCodec;
	}

	/**
	 * Returns the audio rate.
	 **/
	protected String getAudioRate() {
		return _audioRate;
	}

	/**
	 * Returns the audio bit rate.
	 **/
	protected String getAudioBitrate() {
		return _audioBitrate;
	}

	/**
	 * Returns the audio channels.
	 **/
	protected String getAudioChannels() {
		return _audioChannels;
	}

	/**
	 * Sets the subtitles.
	 **/
	protected void setSubtitles(String subtitles) {
		_subtitles = subtitles;
	}

	/**
	 * Sets the resolution.
	 **/
	protected void setVideoResolution(String videoResolution) {
		_videoResolution = videoResolution;
	}

	/**
	 * Sets the video codec (vids handler).
	 **/
	protected void setVideoCodec(String videoCodec) {
		_videoCodec = videoCodec;
	}

	/**
	 * Sets the video rate.
	 **/
	protected void setVideoRate(String videoRate) {
		_videoRate = videoRate;
	}

	/**
	 * Sets the video bit rate.
	 **/
	protected void setVideoBitrate(String videoBitrate) {
		_videoBitrate = videoBitrate;
	}

	/**
	 * Sets the duration.
	 **/
	protected void setDuration(int duration) {
		_duration = duration;
	}

	/**
	 * Sets the audio codec (auds handler).
	 **/
	protected void setAudioCodec(String audioCodec) {
		_audioCodec = audioCodec;
	}

	/**
	 * Sets the audio rate.
	 **/
	protected void setAudioRate(String audioRate) {
		_audioRate = audioRate;
	}

	/**
	 * Sets the audio bit rate.
	 **/
	protected void setAudioBitrate(String audioBitrate) {
		_audioBitrate = audioBitrate;
	}

	/**
	 * Sets the audio channels.
	 **/
	protected void setAudioChannels(String audioChannels) {
		_audioChannels = audioChannels;
	}

	/**
	 * Sets the container.
	 **/
	protected void setContainer(String container) {
		_container = container;
	}

	/**
	 * Returns the container.
	 **/
	protected String getContainer() {
		return _container;
	}

	/**
	 * Sets the Media Type.
	 **/
	protected void setMediaType(String mediaType) {
		_mediaType = mediaType;
	}

	/**
	 * Returns the Media Type.
	 **/
	protected String getMediaType() {
		return _mediaType;
	}

	/**
	 * Sets the meta data ArrayList.
	 **/
	protected void setMetaData(ArrayList<String> metaData) {
		this.metaData = metaData;
	}

	/**
	 * Returns the meta data ArrayList.
	 **/
	protected ArrayList<String> getMetaData() {
		return metaData;
	}

	protected boolean isSupported() {
		return supported;
	}

	/**
	 * Processes a file from the given DataInputStream.
	 **/
	protected void process(RandomAccessFile dataStream) throws Exception {}


	/**
	 * Reads an unsigned 8-bit integer.
	 **/
	protected int readUnsignedByte(byte [] b, int offset) throws Exception {
		return b[offset];
	}

	/**
	 * Reads an unsigned 16-bit integer.
	 **/
	protected int readUnsignedInt16(byte [] b, int offset) throws Exception {
		return (b[offset] | (b[offset+1] << 8));
	}

	/**
	 * Reads an unsigned 32-bit integer.
	 **/
	protected int readUnsignedInt32(byte [] b, int offset) throws Exception {
		return (readUnsignedInt16(b, offset) | (readUnsignedInt16(b, offset+2) << 16));
	}

	/**
	 * Returns a 16-bit integer.
	 **/
	protected int getUnsignedInt16(int byte1, int byte2) throws Exception {
		return (byte2 | (byte1 << 8));
	}

	/**
	 * Returns a 16-bit integer.
	 **/
	protected int getUnsignedInt16(byte byte1, byte byte2) throws Exception {
		return (new Byte(byte2).intValue() | new Byte(byte1).intValue() << 8);
	}

	/**
	 * Returns an unsigned 32-bit integer.
	 **/
	protected int getUnsignedInt32(byte byte1, byte byte2) throws Exception {
		return (new Byte(byte2).intValue() | new Byte(byte1).intValue() << 16);
	}

	/**
	 * Returns an unsigned 32-bit integer.
	 **/
	protected int getUnsignedInt32(int byte1, int byte2) throws Exception {
		return (byte1 | byte2 << 16);
	}

	/**
	 * Reads an unsigned byte and returns its int representation.
	 **/
	protected int readUnsignedByte(RandomAccessFile dataStream) throws Exception {
		int data = dataStream.readUnsignedByte();
		if (data == -1) {
			throw new Exception("Unexpected end of stream.");
		}
		return data;
	}

	/**
	 * Reads n unsigned bytes and returns it in an int[n].
	 **/
	protected int[] readUnsignedBytes(RandomAccessFile dataStream, int n) throws Exception {
		int[] data = new int[n];
		for (int i = 0; i < data.length; i++) {
			data[i] = readUnsignedByte(dataStream);
		}
		return data;
	}

	/**
	 * Reads an unsigned 16-bit integer.
	 **/
	protected int readUnsignedInt16(RandomAccessFile dataStream) throws Exception {
		return (readUnsignedByte(dataStream) | (readUnsignedByte(dataStream) << 8));
	}

	/**
	 * Reads an unsigned 32-bit integer.
	 **/
	protected int readUnsignedInt32(RandomAccessFile dataStream) throws Exception {
		return (readUnsignedInt16(dataStream) | (readUnsignedInt16(dataStream) << 16));
	}

	/**
	 * Discards n bytes.
	 **/
	protected boolean skipBytes(RandomAccessFile dataStream, int n) throws Exception {

		int len = (int) (dataStream.length() - dataStream.getFilePointer());

		if (n > 0 && n < 10000 && len > n) {
			readUnsignedBytes(dataStream,n);
			return true;
		}
		else
			return false;
	}


	/**
	 * Reverses the byte order
	 **/
	int changeEndianness(int num) {
		return (num >>> 24) | (num << 24) | ((num << 8) & 0x00FF0000 | ((num >> 8) & 0x0000FF00));
	}


	/**
	 * Returns the ascii value of id
	 **/
	String fromByteToAscii(int id, int numberOfBytes) throws Exception {
		/* Transforms the id in a string... */
		StringBuffer buffer = new StringBuffer(4);

		for (int i=0; i<numberOfBytes; i++) {
			int c = id & 0xff;
			buffer.append((char)c);
			id >>= 8;
		}
		return new String (buffer);
	}


	/**
	 * Returns the decimal value of a specified number of bytes from a specific part of a byte.
	 **/
	int getDecimalValue(int[] bits, int start, int stop, boolean printBits) {

		String dec = "";

		for (int i = start; i >= stop; i--)
			dec += bits[i];

		if (printBits)
			log.debug("dec:"+dec);

		return Integer.parseInt(dec, 2);
	}


	/**
	 * Returns an array containing the bits from the value.
	 **/
	int [] getBits(int value, int numberOfBytes) {

		int [] bits = new int [numberOfBytes*8];

		for (int i = bits.length-1; i >= 0; i--) {
			bits [i] = (value >>> i) & 1;
		}
		return bits;
	}

	/**
	 * Debugging.
	 **/
	void printBits(int [] bits) {

		for (int i = bits.length-1; i >= 0; i--) {
			System.out.print(bits[i]);

			if ((i)%8 == 0)
				System.out.print(" ");
		}

		System.out.print(" ");
	}


	/**
	 * Searchs in the inputStream stream the name following the string id (seperated by a \t).
	 * @throws IOException 
	 **/
	protected String findName(InputStream stream, String id) throws IOException {
		return findName(new InputStreamReader(stream), id);
	}
	
	protected String findName(InputStreamReader stream, String id) throws IOException {
		
		if (stream == null || id == null)
			return "";

		BufferedReader reader = new BufferedReader(stream);
		String line = null;

		while ((line = reader.readLine()) != null) {
			if (line.length() > 0) {
				StringTokenizer tokenizer = new StringTokenizer(line, "\t");
				if (tokenizer.countTokens() > 0 && id.compareToIgnoreCase(tokenizer.nextToken()) == 0) {
					return tokenizer.nextToken();
				}
			}
		}
		return "";
	}


	/*
	 * Returns true if found, else false
	 */
	protected boolean getExtendedCodecInfo(RandomAccessFile dataStream, int chunkSize) throws Exception {


		int temp;
		String extendedInfo = "";

		while (chunkSize > 0) {

			temp = readUnsignedByte(dataStream);
			chunkSize--;

			/*44 == D, 58 == X*/
			if (Integer.toHexString(temp).equals("44") || Integer.toHexString(temp).equals("58")) {
				extendedInfo = "";
				extendedInfo += fromByteToAscii(temp, 1);

				for (int u = 0; u < 3; u++) {

					if (chunkSize == 0)
						return false;

					temp = readUnsignedByte(dataStream);
					chunkSize--;
					extendedInfo += fromByteToAscii(temp, 1);
				}

				if ((extendedInfo.toLowerCase().equals("divx")) || (extendedInfo.toLowerCase().equals("xvid"))) {

					for (int a = 0; a < 100; a++) {
						temp = readUnsignedByte(dataStream);
						chunkSize--;

						if (temp == 0)
							break;

						if (chunkSize == 0)
							return false;

						extendedInfo += fromByteToAscii(temp, 1);
					}

					/*If last character is not a digit it is removed*/
					for (int i = 0; i < extendedInfo.length(); i++) {

						if (!Character.isDigit(extendedInfo.charAt(extendedInfo.length()-1))) {
							if (extendedInfo.charAt(extendedInfo.length()-1) == 'p')
								;//Encoded with BVOP I think
							extendedInfo = extendedInfo.substring(0, (extendedInfo.length()-1));
						}
						else
							break;
					}

					/* Replaces "Build" with "b" if it occurs. */
					if ((extendedInfo.toLowerCase().startsWith("divx")) && extendedInfo.length() > 12) {
						if (extendedInfo.substring(7, 12).equals("Build")) {
							extendedInfo = extendedInfo.replaceFirst("Build", "b");
						}
					}


					_libIdentifier = extendedInfo;
					String codecName  = findName(FileUtil.getResourceAsStream("/codecs/videoExtended.txt"), extendedInfo);

					if (!codecName.equals("")) {
						setVideoCodec(codecName);
						skipBytes(dataStream,chunkSize);
						return true;
					}
				}
			}
		}
		return false;
	}
}
