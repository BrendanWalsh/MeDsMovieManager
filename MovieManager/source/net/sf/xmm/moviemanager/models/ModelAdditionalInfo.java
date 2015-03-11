/**
 * @(#)ModelAdditionalInfo.java
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
 * You should have received a copy of the GNU General Public License along with 
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Boston, MA 02111.
 * 
 * Contact: bro3@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.models;

import java.util.ArrayList;
import java.util.HashMap;

import net.sf.xmm.moviemanager.MovieManager;

import org.apache.log4j.Logger;

public class ModelAdditionalInfo {

	static Logger log = Logger.getLogger(ModelAdditionalInfo.class);
	
	public static int additionalInfoFieldCount = 17;

	private int index = -1;

	private String subtitles = null;
	private int duration = 0;
	private int fileSize = 0;
	private int cds = 0;
	private double cdCases = 0;
	private String resolution = null;
	private String videoCodec = null;
	private String videoRate = null;
	private String videoBitrate = null;
	private String audioCodec = null;
	private String audioRate = null;
	private String audioBitrate = null;
	private String audioChannels = null;
	private String fileLocation = null;
	private int fileCount = 0;
	private String container = null;
	private String mediaType = null;

	// Each time a new additional info field is added, this is increased so that each ModelEntry knows that the additional info it already has needs to be updated from the database.
	private static int extraInfoChanged = 0;
	private int lastExtraInfoCount = 0;

	private HashMap<String, String> extraInfoFieldValuesMap = new HashMap<String, String>();
	
	
	// Default empty constructor used when importing from XML using Castor
	public ModelAdditionalInfo() {}
	
	/**
	 * The constructor.
	 **/
	public ModelAdditionalInfo(String subtitles, int duration, int fileSize, int cds, double cdCases, String resolution, String videoCodec, String videoRate, String videoBitrate, String audioCodec, String audioRate, String audioBitrate, String audioChannels, String fileLocation, int fileCount, String container, String mediaType) {

		this.subtitles = subtitles;
		this.duration = duration;
		this.fileSize = fileSize;
		this.cds = cds;
		this.cdCases = cdCases;
		this.resolution = resolution;
		this.videoCodec = videoCodec;
		this.videoRate = videoRate;
		this.videoBitrate = videoBitrate;
		this.audioCodec = audioCodec;
		this.audioRate = audioRate;
		this.audioBitrate = audioBitrate;
		this.audioChannels = audioChannels;
		this.fileLocation = fileLocation;
		this.fileCount = fileCount;
		this.container = container;
		this.mediaType = mediaType;
	}


	public boolean hasOldExtraInfoData() {
		return extraInfoChanged != lastExtraInfoCount;
	}

	
	public String getExtraInfoFieldName(int index) {

		if (MovieManager.getIt().getDatabase() == null)
			return "";
			
		ArrayList<String> extraInfoFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);
		
		if (index >= extraInfoFieldNames.size())
			return "";
		else
			return (String) extraInfoFieldNames.get(index);
	}

	
	public String getExtraInfoFieldValue(String columnName) {
	
		if (extraInfoFieldValuesMap.containsKey(columnName))
			return (String) extraInfoFieldValuesMap.get(columnName);
		
		return null;
	}
	
	
	public String getExtraInfoFieldValue(int index) {

		ArrayList<String> extraInfoFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);
				
		if (index >= 0 && index < extraInfoFieldNames.size()) {
			String column = (String) extraInfoFieldNames.get(index);
		
			if (extraInfoFieldValuesMap.containsKey(column))
				return (String) extraInfoFieldValuesMap.get(column);
		}
	
		return "";
	}


	public ArrayList<String> getExtraInfoFieldValues() {
		
		ArrayList<String> extraInfoFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);
		ArrayList<String> extraInfoFieldValues = new ArrayList<String>();
		
		for (int i = 0; i < extraInfoFieldNames.size(); i++) {
			
			if (extraInfoFieldValuesMap.containsKey(extraInfoFieldNames.get(i)))
				extraInfoFieldValues.add(extraInfoFieldValuesMap.get(extraInfoFieldNames.get(i)));
			else
				extraInfoFieldValues.add("");
		}
		
		return extraInfoFieldValues;
	}


	public void setExtraInfoFieldValues(ArrayList<String> extraInfoFieldValues) {
		 
		ArrayList<String> extraInfoFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);
	
		if (extraInfoFieldValues.size() != extraInfoFieldNames.size()) {
			
			try {
				throw new Exception("extraInfoFieldValues.size(" + extraInfoFieldValues.size()+ ") != extraInfoFieldNamesCount(" + extraInfoFieldNames.size() + ")");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		 
		for (int i = 0; i < extraInfoFieldNames.size(); i++) {
			extraInfoFieldValuesMap.put(extraInfoFieldNames.get(i), extraInfoFieldValues.get(i));
		}
	}

	
//	 Used when importing/exporting XML with Castor
	public ArrayList<String> getExtraInfoFieldNames2() {
		return MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);
	}
	
//	 Used when importing/exporting XML with Castor
	public HashMap<String, String> getExtraInfoFieldValuesMap() {
		return extraInfoFieldValuesMap;
	}
	
//	Used when importing/exporting XML with Castor
	// If the extra info field name doesn't exist, it's created automatically
	public void addExtraInfoFieldName(String extraInfoFieldName) {
	
		try {

			ArrayList<String> extraInfoFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);
			
			if (!extraInfoFieldNames.contains(extraInfoFieldName)) {
				
				if (MovieManager.getIt().getDatabase().addExtraInfoFieldName(extraInfoFieldName) < 0)
					log.error("Error occured when adding new extra info field:" + extraInfoFieldName);
				else {
					log.info("New extra info field added:" + extraInfoFieldName);

					int [] activeFields = MovieManager.getDatabaseHandler().getActiveAdditionalInfoFields();
					int [] newActiveFields = new int[activeFields.length + 1];

					System.arraycopy(activeFields, 0, newActiveFields, 0, activeFields.length);
					newActiveFields[newActiveFields.length-1] = additionalInfoFieldCount + extraInfoFieldNames.size();

					MovieManager.getDatabaseHandler().saveActiveAdditionalInfoFields(newActiveFields);
				}
			}
		} catch (Exception e) {
			log.warn("Exception:", e);
		}
	}
		
	
	public static int additionalInfoFieldCount() {
		return additionalInfoFieldCount;
	}
	
	
	public int getKey() {
		return index;
	}

	public String getSubtitles() {
		if (subtitles == null)
			return "";
		return subtitles;
	}

	public int getDuration() {
		return duration;
	}

	public int getFileSize() {
		return fileSize;
	}

	public int getCDs() {
		return cds;
	}

	public double getCDCases() {
		return cdCases;
	}

	public String getResolution() {
		if (resolution == null)
			return "";
		return resolution;
	}

	public String getVideoCodec() {
		if (videoCodec == null)
			return "";
		return videoCodec;
	}

	public String getVideoRate() {
		return videoRate;
	}

	public String getVideoBitrate() {
		if (videoBitrate == null)
			return "";
		return videoBitrate;
	}

	public String getAudioCodec() {
		if (audioCodec == null)
			return "";
		return audioCodec;
	}

	public String getAudioRate() {
		if (audioRate == null)
			return "";
		return audioRate;
	}

	public String getAudioBitrate() {
		if (audioBitrate == null)
			return "";
		return audioBitrate;
	}

	public String getAudioChannels() {
		if (audioChannels == null)
			return "";
		return audioChannels;
	}

	public String getFileLocation() {
		if (fileLocation == null)
			return "";
		return fileLocation;
	}
	
	public String [] getFileLocationAsArray() {
		if (fileLocation == null)
			return null;
		
		String [] files = fileLocation.trim().split("\\*");
		return files;
	}

	public int getFileCount() {
		return fileCount;
	}

	public String getContainer() {
		if (container == null)
			return "";
		return container;
	}

	public String getMediaType() {
		if (mediaType == null)
			return "";
		return mediaType;
	}
	
	
	public void setAudioBitrate(String audioBitrate) {
		this.audioBitrate = audioBitrate;
	}

	public void setAudioChannels(String audioChannels) {
		this.audioChannels = audioChannels;
	}

	public void setAudioCodec(String audioCodec) {
		this.audioCodec = audioCodec;
	}

	public void setAudioRate(String audioRate) {
		this.audioRate = audioRate;
	}

	public void setCDCases(double cdCases) {
		this.cdCases = cdCases;
	}

	public void setCDs(int cds) {
		this.cds = cds;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public void setSubtitles(String subtitles) {
		this.subtitles = subtitles;
	}

	public void setVideoBitrate(String videoBitrate) {
		this.videoBitrate = videoBitrate;
	}

	public void setVideoCodec(String videoCodec) {
		this.videoCodec = videoCodec;
	}

	public void setVideoRate(String videoRate) {
		this.videoRate = videoRate;
	}        


	/* Convenience method for setting values */
	public boolean setValue(String fieldName, String value, String tableName) {

		//String fieldName = fieldModel.getField();
		//String value = fieldModel.getValue();

		if (tableName.equals("Additional Info")) {

			if (fieldName.equalsIgnoreCase("SubTitles"))
				setSubtitles(value);
			else if (fieldName.equalsIgnoreCase("Duration"))
				setDuration(Integer.parseInt(value));
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("File Size"))
				setFileSize(Integer.parseInt(value));
			else if (fieldName.equalsIgnoreCase("CDs"))
				setCDs(Integer.parseInt(value));
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("CD Cases"))
				setCDCases(Integer.parseInt(value));
			else if (fieldName.equalsIgnoreCase("Resolution"))
				setResolution(value);
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Video Codec"))
				setVideoCodec(value);
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Video Rate"))
				setVideoRate(value);
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Video Bit Rate"))
				setVideoBitrate(value);
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Audio Codec"))
				setAudioCodec(value);
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Audio Rate"))
				setAudioRate(value);
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Audio Bit Rate"))
				setAudioBitrate(value);
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Audio Channels"))
				setAudioChannels(value);
			else if (fieldName.equalsIgnoreCase("Container"))
				setContainer(value);
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("File Location"))
				setFileLocation(value);
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("File Count"))
				setFileCount(Integer.parseInt(value));
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Media Type"))
				setMediaType(value);
			else
				return false;

			return true;
		}
		else if (tableName.equals("Extra Info")) {

			ArrayList<String> extraInfoFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);
			
			for (int i = 0; i < extraInfoFieldNames.size(); i++) {
				if (fieldName.equals(extraInfoFieldNames.get(i))) {
					extraInfoFieldValuesMap.put(fieldName, value);
					//extraInfoFieldValues.set(i, value);
					return true;
				}
			}
		}
		return false;
	}

	/* Convenience method for setting values */
	public String getValue(String fieldName, String tableName) {

		if (tableName.equals("Additional Info")) {
			
			if (fieldName.equalsIgnoreCase("SubTitles"))
				return getSubtitles();
			else if (fieldName.equalsIgnoreCase("Duration"))
				return "" + getDuration();
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("File Size"))
				return "" + getFileSize();
			else if (fieldName.equalsIgnoreCase("CDs"))
				return "" + getCDs();
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("CD Cases"))
				return "" + getCDCases();
			else if (fieldName.equalsIgnoreCase("Resolution"))
				return getResolution();
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Video Codec"))
				return getVideoCodec();
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Video Rate"))
				return getVideoRate();
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Video Bit Rate")) 
				return getVideoBitrate();
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Audio Codec"))
				return getAudioCodec();
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Audio Rate"))
				return getAudioRate();
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Audio Bit Rate"))
				return getAudioBitrate();
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Audio Channels"))
				return getAudioChannels();
			else if (fieldName.equalsIgnoreCase("Container"))
				return getContainer();
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("File Location"))
				return getFileLocation();
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("File Count"))
				return "" + getFileCount();
			else if (fieldName.replaceAll("_", " ").equalsIgnoreCase("Media Type"))
				return getMediaType();
			
			return "";
		}
		else if (tableName.equals("Extra Info")) {

			ArrayList<String> extraInfoFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);
			
			for (int i = 0; i < extraInfoFieldNames.size(); i++) {
				if (fieldName.equals(extraInfoFieldNames.get(i))) {
					return "" + extraInfoFieldValuesMap.get(fieldName);
				}
			}
		}
		return "";
	}

	public String getAdditionalInfoString() {
		return getAdditionalInfoString(this, "\n");
	}

	public String getAdditionalInfoString(String newLine) {
		return getAdditionalInfoString(this, newLine);
	}

	/**
	 * Returns the additional_info string of this model
	 **/
	public static String getAdditionalInfoString(ModelAdditionalInfo model, String newLine) {

		if (model == null)
			return "";

		//long time = System.currentTimeMillis();

		StringBuffer data = new StringBuffer("");

		try {
			/* Gets the fixed additional info... */

			int [] activeAdditionalInfoFields = MovieManager.getDatabaseHandler().getActiveAdditionalInfoFields();

			if (activeAdditionalInfoFields == null)
				return null;
			
			for (int i = 0; i < activeAdditionalInfoFields.length; i++) {

				switch (activeAdditionalInfoFields[i]) {

				case 0: {
					if (data.length() != 0)
						data.append(newLine);
					data.append("Subtitles: ");

					data.append(model.getSubtitles());
					break;
				}

				case 1: {
					if (data.length() != 0)
						data.append(newLine);
					data.append("Duration: ");

					int duration = model.getDuration();

					if (duration > 0) {

						int hours = duration / 3600;
						int mints = duration / 60 - hours * 60;
						int secds = duration - hours * 3600 - mints *60;

						data.append(hours + ":");

						if (mints < 10)
							data.append("0");
						data.append(mints + ":");

						if (secds < 10)
							data.append("0");
						data.append(secds);
					}
					break;
				}

				case 2: {
					if (data.length() != 0)
						data.append(newLine);
					data.append("File Size: ");

					if (model.getFileSize() > 0) {
						data.append(model.getFileSize());
						data.append(" MB");
					}
					break;
				}

				case 3: {
					if (data.length() != 0)
						data.append(newLine);
					data.append("CDs: ");

					if (model.getCDs() > 0)
						data.append(model.getCDs());
					break;
				}

				case 4: {
					if (data.length() != 0)
						data.append(newLine);
					data.append("CD Cases: ");

					if (model.getCDCases() > 0)
						data.append(model.getCDCases());
					break;
				}

				case 5: {
					if (data.length() != 0)
						data.append(newLine);
					data.append("Resolution: ");

					data.append(model.getResolution());
					break;
				}

				case 6: {
					if (data.length() != 0)
						data.append(newLine);
					data.append("Video Codec: ");

					data.append(model.getVideoCodec());
					break;
				}

				case 7: {
					if (data.length() != 0)
						data.append(newLine);
					data.append("Video Rate: ");

					if (model.getVideoRate() != null && !"".equals(model.getVideoRate())) {
						data.append(model.getVideoRate());
						data.append(" fps");
					}
					break;
				}

				case 8: {
					if (data.length() != 0)
						data.append(newLine);

					data.append("Video Bit Rate: ");

					if (model.getVideoBitrate() != null && !"".equals(model.getVideoBitrate())) {
						data.append(model.getVideoBitrate());
						data.append(" kbps");
					}
					break;
				}

				case 9: {
					if (data.length() != 0)
						data.append(newLine);

					data.append("Audio Codec: ");
					data.append(model.getAudioCodec());
					break;
				}

				case 10: {
					if (data.length() != 0)
						data.append(newLine);

					data.append("Audio Rate: ");

					if (model.getAudioRate() != null && !"".equals(model.getAudioRate())) {
						data.append(model.getAudioRate());
						data.append(" Hz");
					}
					break;
				}

				case 11: {

					if (data.length() != 0)
						data.append(newLine);

					data.append("Audio Bit Rate: ");

					if (model.getAudioBitrate() != null && !"".equals(model.getAudioBitrate())) {
						data.append(model.getAudioBitrate());
						data.append(" kbps");
					}
					break;
				}

				case 12: {

					if (data.length() != 0)
						data.append(newLine);

					data.append("Audio Channels: ");
					data.append(model.getAudioChannels());

					break;
				}

				case 13: {

					if (data.length() != 0)
						data.append(newLine);

					data.append("Location: ");
					data.append(model.getFileLocation());

					break;
				}

				case 14: {

					if (data.length() != 0)
						data.append(newLine);

					data.append("File Count: ");
					if (model.getFileCount() > 0)
						data.append(String.valueOf(model.getFileCount()));
					break;
				}

				case 15: {

					if (data.length() != 0)
						data.append(newLine);

					data.append("Container: ");
					data.append(model.getContainer());
					break;
				}

				case 16: {

					if (data.length() != 0)
						data.append(newLine);

					data.append("Media Type: ");
					data.append(model.getMediaType());
					break;
				}

				default : {

					int columnIndex = activeAdditionalInfoFields[i]-17;

					if (data.length() != 0)
						data.append(newLine);

					data.append(model.getExtraInfoFieldName(columnIndex) + ": ");
					data.append(model.getExtraInfoFieldValue(columnIndex));
				}	
				}
			}	

		} catch (Exception e) {
			log.error("Exception: ", e);
		}

		/* Returns the data... */
		return data.toString();
	}
}
