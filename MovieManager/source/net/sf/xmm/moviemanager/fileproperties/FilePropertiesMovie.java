/**
 * @(#)FilePropertiesMovie.java 1.0 26.01.06 (dd.mm.yy)
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

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.SysUtil;

import net.sf.xmm.moviemanager.MovieManagerConfig.MediaInfoOption;
import org.apache.log4j.Logger;

/**
 * @author		Bro3@sf.net
 **/
public class FilePropertiesMovie {

	Logger log = Logger.getLogger(getClass());

	private String mediaInfoUrl = "http://mediainfo.sourceforge.net/en/Download";
	
	/**
	 * The filesize.
	 **/
	private String _subtitles = "";

	/**
	 * The filesize.
	 **/
	private int _fileSize = -1;

	/**
	 * The resolution (width x heigth).
	 **/
	private String _videoResolution = "";

	/**
	 * The video codec (vids handler).
	 **/
	private String _videoCodec = "";

	/**
	 * The video codec (vids handler).
	 **/
	private String _codecLibraryIdentifier = "";

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
	 * The audio codec (auds handler).
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
	 * The file location.
	 **/
	private String _location = "";

	/**
	 * The file container.
	 **/
	private String _container = "";

	private ArrayList<String> metaData;

	protected String fileName = "";

	private boolean infoAvailable = false;

	private boolean supported = false;
	
	/**
	 * Class 	gets all info available from the media file.
	 * 
	 * Class  constructor specifying number of objects to create.
	 * @param filePath 		a path for the file.
	 * @param useMediaInfo 	0 = no
	 * 						1 = yes if no java parser avaliable
	 * 						2 = yes 
	 **/
	public FilePropertiesMovie(String filePath, MediaInfoOption mediaInfoOption) throws Exception {

		FileProperties fileProperties = null;
		
		try {
			/* The known magic header bytes... */
			final int[][] MAGIC_BYTES = {
					{0x00, 0x00, 0x01, 0xb3}, // MPEG (video)
					{0x00, 0x00, 0x01, 0xba}, // MPEG (video)
					{0x52, 0x49, 0x46, 0x46}, // RIFF (WAV / audio, AVI / video)
					{0x4f, 0x67, 0x67, 0x53}, // OGG ,OGM 
					{0x44, 0x56, 0x44, 0x56}, // IFO (DVD)
					//{0x1a, 0x45, 0xdf, 0xa3}, // MKV
			};

			/**
			 * The respective objects.
			 **/
			final FileProperties[] FORMATS = {
					new FilePropertiesMPEG(),
					new FilePropertiesMPEG(),
					new FilePropertiesRIFF(),
					new FilePropertiesOGM(),
					new FilePropertiesIFO(),
					//new FilePropertiesMKV(),
			};

			_fileSize = Math.round((new File(filePath).length()) / 1024F / 1024F);
			_location = filePath;
			
			log.debug("Processing file " + filePath);
			
			/* The input stream... */
			RandomAccessFile dataStream = new RandomAccessFile(filePath, "r");

			// Load 1MB straight into memory
			//MappedByteBuffer map = dataStream.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, ((long) 1024*1024*3));
			long time = System.currentTimeMillis();
			//map.load();
			
			//System.out.println("map.load:" + (System.currentTimeMillis() - time));
			
			/* Gets the header for filetype identification... */
			int[] header = new int[4];

			for (int i=0; i<header.length; i++)
				header[i] = dataStream.readUnsignedByte();

			/* Finds the right object... */
			int format;
			for (format = 0; format < FORMATS.length; format++) {
				if (Arrays.equals(header, MAGIC_BYTES[format])) {
					break;
				}
			}

			boolean done = false;
			boolean tryMediaInfo = true;
			
			while (!done) {

				try {

					switch (mediaInfoOption) {
					
					// No media info
					case MediaInfo_No: {
						tryMediaInfo = false;
					}

					// Java parsing, if fail, media info
					case MediaInfo_yesifnojava: {
						if (format < FORMATS.length) {

							if (format < FORMATS.length) {
								fileProperties = FORMATS[format];
								break;
							}
						}

						if (tryMediaInfo) {
							tryMediaInfo = false;
							mediaInfoOption = MediaInfoOption.MediaInfo_Yes;
						}
						else
							done = true;

						break;
					}
					// Use media info
					case MediaInfo_Yes: {

						if (!SysUtil.isMac()) {
							try {
								fileProperties = new FilePropertiesMediaInfo(filePath);
								break;
							} catch (Exception e) {
								fileProperties = null;
								log.error("Exception: " + e.getMessage());
							}
						}

						if (tryMediaInfo) {
							tryMediaInfo = false;
							mediaInfoOption = MediaInfoOption.MediaInfo_No;
						}
						else
							done = true;

						break;

					}
					}


					if (fileProperties != null) {

						log.debug(fileProperties + " processing file " + filePath);
						
						time = System.currentTimeMillis();
						
						/* Starts parsing the file...*/
						fileProperties.process(dataStream);

						log.debug("Processed file in " + (System.currentTimeMillis() - time) + "ms");
						
						supported = fileProperties.isSupported();

						if (supported)
							infoAvailable = true;
						else
							throw new Exception("Unable to retrieve info");

						/* Gets the processed info... */
						_subtitles = fileProperties.getSubtitles();
						_videoResolution = fileProperties.getVideoResolution();
						_videoCodec = fileProperties.getVideoCodec();
						_videoRate = fileProperties.getVideoRate();
						_videoBitrate = fileProperties.getVideoBitrate();
						_duration = fileProperties.getDuration();
						_audioCodec = fileProperties.getAudioCodec();
						_audioRate = fileProperties.getAudioRate();
						_audioBitrate = fileProperties.getAudioBitrate();
						_audioChannels = fileProperties.getAudioChannels();
						
						_container = fileProperties.getContainer();
						metaData = fileProperties.getMetaData();
						_codecLibraryIdentifier = fileProperties.getVideoCodecLibraryIdentifier();

						fileName = new File(filePath).getName();

						if (!fileProperties.errorOccured)
							done = true;
					}

					/* Closes the input stream... */
					dataStream.close();

				} catch (UnsatisfiedLinkError err) {
					
					log.error("UnsatisfiedLinkError: " + err.getMessage(), err);
					
					// Unable to load library 'mediainfo'
					if (err.getMessage().indexOf("Unable to load library 'mediainfo") != -1) {
						log.debug("MediaInfo library is missing.");
						
						if (SysUtil.isWindows()) {
							
						} else if (SysUtil.isLinux()) {
							DialogAlert alert = new DialogAlert(MovieManager.getDialog(), "MediaInfo library not found!", 
									"<html>The MediaInfo library could not be found. <br>Please go to <a href=\""+ 
									mediaInfoUrl + "\">"+ mediaInfoUrl +
									"</a> and install the appropriate package for your GNU+Linux distribution.</html>", true);
							GUIUtil.show(alert, true);
						}
					}
					
					tryMediaInfo = false;
					mediaInfoOption = MediaInfoOption.MediaInfo_No;
					
				} catch (Exception e) {
					log.debug("Media file:" + filePath);
					log.error("Exception: " + e.getMessage(), e);
					
					if (tryMediaInfo) {
						
						if (mediaInfoOption == MediaInfoOption.MediaInfo_Yes)
							mediaInfoOption = MediaInfoOption.MediaInfo_No;
						else
							mediaInfoOption = MediaInfoOption.MediaInfo_Yes;

						tryMediaInfo = false;
					}
					else {
						/* The file is corrupted, tries to save the info that may have been found */
						if (fileProperties != null) {
							_subtitles = fileProperties.getSubtitles();
							_videoResolution = fileProperties.getVideoResolution();
							_videoCodec = fileProperties.getVideoCodec();
							_videoRate = fileProperties.getVideoRate();
							_videoBitrate = fileProperties.getVideoBitrate();
							_duration = fileProperties.getDuration();
							_audioCodec = fileProperties.getAudioCodec();
							_audioRate = fileProperties.getAudioRate();
							_audioBitrate = fileProperties.getAudioBitrate();
							_audioChannels = fileProperties.getAudioChannels();
							_location = filePath;
							_container = fileProperties.getContainer();
							metaData = fileProperties.getMetaData();
							_codecLibraryIdentifier = fileProperties.getVideoCodecLibraryIdentifier();

							fileName = new File(filePath).getName();
						}
						throw new Exception("File could be corrupted. Some info may have been saved.");
					}
				}
			}

		} catch (Exception e) {
			log.error("", e);
		} finally {

			if (!supported) {
			//	throw new Exception("File format not supported.");
			}
		}
	}


	/**
	 * @return 		all the info in a string
	 **/
	public String toString() {
		return "MovieProperties [ fileSize:"+_fileSize+", "+
		"vResolution:"+_videoResolution+", "+
		"vCodec:"+_videoCodec+", "+
		"vRate:"+_videoRate+", "+
		"vBitrate:"+_videoBitrate+", "+
		"duration:("+_duration+") ,"+
		"aCoded:"+_audioCodec+", "+
		"aRate:"+_audioRate+", "+
		"aBitrate:"+_audioBitrate+", "+
		"aChannels:"+_audioChannels+" ]";
	}


	/**
	 * @return 		list of meta data
	 **/
	public ArrayList<String> getMetaData() {
		return metaData;
	}

	/**
	 * @return 		the the tag info if it exists, null if it doesn't exist
	 **/
	public String getMetaDataTagInfo(String tag) {

		String temp = "";

		if (metaData != null) {

			for (int i = 0; i < metaData.size(); i++) {

				temp = ((String) metaData.get(i));

				if (temp.startsWith(tag)) {
					return temp.substring(tag.length()+1, temp.length());
				}
			}
		}
		return null;
	}

	/**
	 * @return 		if the info is available
	 **/
	public boolean getInfoAvailable() {
		return infoAvailable;
	}

	/**
	 * @return 		if the info is available
	 **/
	public boolean getFileFormatSupported() {
		return supported;
	}

	/**
	 * @return the subtitles
	 **/
	public String getSubtitles() {
		return _subtitles;
	}

	/**
	 * @return 		the filesize in MiB
	 **/
	public int getFileSize() {
		return _fileSize;
	}

	/**
	 * @return gets the file name
	 **/
	public String getFileName() {
		return fileName;
	}

	/**
	 * sets the file name
	 **/
	public void setFileName(String newName) {
		fileName = newName;
	}

	/**
	 * @return 		the video resolution
	 **/
	public String getVideoResolution() {
		return _videoResolution;
	}

	/**
	 * @return 		the video codec
	 **/
	public String getVideoCodec() {
		return _videoCodec;
	}

	/**
	 * @return 		the bitstream version info for Xvid or DivX 5-6
	 **/
	public String getVideoCodecLibraryIdentifier() {
		return _codecLibraryIdentifier;
	}


	/**
	 * @return 		the video rate
	 **/
	public String getVideoRate() {
		return _videoRate;
	}

	/**
	 * @return		 the video bit rate
	 **/
	public String getVideoBitrate() {
		return _videoBitrate;
	}

	/**
	 * @return 		the duration in seconds
	 **/
	public int getDuration() {
		return _duration;
	}

	/**
	 * @return 		the audio codec
	 **/
	public String getAudioCodec() {
		return _audioCodec;
	}

	/**
	 * @return 		the audio rate
	 **/
	public String getAudioRate() {
		return _audioRate;
	}

	/**
	 * @return 		the audio bit rate
	 **/
	public String getAudioBitrate() {
		return _audioBitrate;
	}

	/**
	 * @return 		the audio channels
	 **/
	public String getAudioChannels() {
		return _audioChannels;
	}

	/**
	 * @return 		the file location
	 **/
	public String getLocation() {
		return _location;
	}

	/**
	 * @return 		the file container
	 **/
	public String getContainer() {
		return _container;
	}
}
