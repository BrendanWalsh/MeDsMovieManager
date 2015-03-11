/**
 * @(#)FilePropertiesMediaInfo.java 1.0 26.01.06 (dd.mm.yy)
 *
 * Copyright (2003) bro3
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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.mediainfodll.MediaInfo;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.LibPathHacker;
import net.sf.xmm.moviemanager.util.SysUtil;

public class FilePropertiesMediaInfo extends FileProperties {

	static Logger log = Logger.getLogger(FilePropertiesMediaInfo.class);

	private String filePath;

	MediaInfo mi;
	

	public FilePropertiesMediaInfo(String filePath) throws Exception {
		this.filePath = filePath;
				
		SysUtil.loadMediaInfoLib();
	
		mi = new MediaInfo();
		
		String version = MediaInfo.Option_Static("Info_Version");
		log.debug("Using " + version);
		
	}
		
	protected void process(RandomAccessFile nodeUsed) {

		try {
			
			int open = mi.Open(filePath);
			String tmp;

			if (open > 0) {

				supported = true;

				String audioCodec = "";
				String audioChannels = "";
				String audioRate = "";
				String audioBitrate = "";

				setContainer(mi.Get(MediaInfo.StreamKind.General, 0, "Format", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name));

				String dur = mi.Get(MediaInfo.StreamKind.General, 0, "Duration", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

				if (!"".equals(dur))
					setDuration((int) (Double.parseDouble(dur)/1000));

				String codecName = mi.Get(MediaInfo.StreamKind.Video, 0, "Codec", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

				codecName  = findName(FileUtil.getResourceAsStream("/codecs/FOURCCvideo.txt"), codecName);

				String library = mi.Get(MediaInfo.StreamKind.Video, 0, "Encoded_Library", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

				if (!library.equals("")) {
					tmp = findName(FileUtil.getResourceAsStream("/codecs/videoExtended.txt"), library);

					if (!tmp.equals("")) {
						codecName = tmp;
					}
				}

				setVideoCodec(codecName);

				String vBitrate = mi.Get(MediaInfo.StreamKind.Video, 0, "BitRate", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

				if (vBitrate.length() > 2)
					setVideoBitrate(vBitrate.substring(0, vBitrate.length() - 3));

				setVideoResolution(mi.Get(MediaInfo.StreamKind.Video, 0, "Width", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name) + "x" + mi.Get(MediaInfo.StreamKind.Video, 0, "Height", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name));

				setVideoRate(mi.Get(MediaInfo.StreamKind.Video, 0, "FrameRate", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name));

				tmp = mi.Get(MediaInfo.StreamKind.Audio, 0, "StreamCount", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

				int audioCount = 0;

				if (!"".equals(tmp))
					audioCount = Integer.parseInt(tmp);

				log.debug("audioCount:" + audioCount);

				for (int i = 0; i < audioCount; i++) {

					if (!audioCodec.equals(""))
						audioCodec += ", ";

					String value = mi.Get(MediaInfo.StreamKind.Audio, i, "Codec", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

					log.debug("value:" + value);

					if (!value.equals("")) {
						StringBuffer buffer = new StringBuffer("0x");
						int u = 4 - value.length();
						while (u-- > 0) {
							buffer.append('0');
						}
						buffer.append(value);

						value = findName(FileUtil.getResourceAsStream("/codecs/FOURCCaudio.txt"), buffer.toString());
					}

					if (!value.equals(""))
						audioCodec += value;
					else
						audioCodec += mi.Get(MediaInfo.StreamKind.Audio, i, "Codec/String", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);


					if (!audioBitrate.equals(""))
						audioBitrate += ", ";

					String aBitrate = mi.Get(MediaInfo.StreamKind.Audio, i, "BitRate", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

					if (aBitrate.length() > 2)
						audioBitrate += aBitrate.substring(0, aBitrate.length() - 3);

					if (!audioChannels.equals(""))
						audioChannels += ", ";

					audioChannels += mi.Get(MediaInfo.StreamKind.Audio, i, "Channel(s)", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

					if (!audioRate.equals(""))
						audioRate += ", ";

					audioRate += mi.Get(MediaInfo.StreamKind.Audio, i, "SamplingRate", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

				}

				setAudioCodec(audioCodec);
				/* Sets the audio channels... */
				setAudioChannels(audioChannels);
				/* Sets the audio rate... */
				setAudioRate(audioRate);
				/* Sets the audio bit rate... */
				setAudioBitrate(audioBitrate);

				mi.Close();
			}
		} catch (NumberFormatException e) {
			log.error("Exception:" + e.getMessage(), e);
		} catch (IOException e) {
			log.error("Exception:" + e.getMessage(), e);
		}
	}
}
