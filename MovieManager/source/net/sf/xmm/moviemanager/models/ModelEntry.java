/**
 * @(#)ModelEntry.java
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

abstract public class ModelEntry {

	static public String sortCategory = "";
	static public int sort = 1; /* 1 = Title, 2 = directed by, 3 = Rating, 4 = Date, 5 = Duration */

	/* The database key for this movie/episode. */
	private int key = -1;
	private String urlKey = ""; /* imdb/tv.com key*/
	private String cover = "";
	private String title = "";
	private String date = "";
	private String directedBy = "";
	private String writtenBy = "";
	private String genre = "";
	private String rating = "";
	private String personalRating = "";
	private String plot = "";
	private String cast = "";
	private String notes = "";
	private boolean seen = false;

	/* Aka - Also known as */
	private String aka = "";
	private String country = "";
	private String language = "";
	private String colour = "";

	private String mpaa = "";
	private String certification = "";
	private String webSoundMix = "";
	private String webRuntime = "";
	private String awards = "";

	private byte [] coverData = null;

	protected boolean hasGeneralInfoData = false;
	protected boolean hasAdditionalInfoData = false;

	/* Changed notes that aren't saved to the database */
	public boolean hasChangedNotes = false;

	ModelAdditionalInfo additionalInfo = null;

	// which list this entry is a member of
	ArrayList<String> memberOfLists = new ArrayList<String>();
	
	public ArrayList<String> getMemberLists() {
		return new ArrayList<String>(memberOfLists);
	}
	
	public boolean addToMemberOfList(String listName) {
		if (memberOfLists.contains(listName))
			return false;
		
		memberOfLists.add(listName);
		return true;
	}
	
	public boolean isMemberOfList(String listName) {
		return memberOfLists.contains(listName);
	}
	
	public boolean removeAsMemberOfList(String listName) {
		if (!memberOfLists.contains(listName))
			return false;
		
		memberOfLists.remove(listName);
		return true;
	}
	
	public boolean isEpisode() {
		return false;
	}
	
	public boolean isMovie() {
		return false;
	}

	public boolean getHasGeneralInfoData() {
		return hasGeneralInfoData;
	}

	public boolean getHasAdditionalInfoData() {
		return hasAdditionalInfoData && !additionalInfo.hasOldExtraInfoData();
	}

	public abstract void updateAdditionalInfoData();
	public abstract void updateGeneralInfoData();
	
	public abstract void updateGeneralInfoData(boolean getCover);
	
	public ModelAdditionalInfo getAdditionalInfo() {
		return additionalInfo;
	}

	/*
	 * Necessary for Castor export where the additional info must be updated automatically
	 */
	public ModelAdditionalInfo getUpdatedAdditionalInfo() {
		
		if (!hasAdditionalInfoData)
			updateAdditionalInfoData();
			
		return additionalInfo;
	}

	/**
	 * Used by Castor for XML export/import
	 * @param list
	 */
	public void setMemberOfLists(ArrayList<String> list) {
		memberOfLists = list;
	}
	
	/**
	 * Used by Castor for XML export/import
	 */
	public ArrayList<String> getMemberOfLists() {
		return memberOfLists;
	}
	
	public void setAdditionalInfo(ModelAdditionalInfo additionalInfo) {
		this.additionalInfo = additionalInfo;
		hasAdditionalInfoData = true;
	}
	
	public String getSortCategory() {
		return sortCategory;
	}

	public int getSort() {
		return sort;
	}

	public int getKey() {
		return key; 
	}

	public void setKey(int key) {
		this.key = key;
	}

	public String getUrlKey() {
		if (urlKey == null)
			return "";
		return urlKey;
	}

	public void setUrlKey(String urlKey) {
		this.urlKey = urlKey;	
	}

	public String getCompleteUrl() {
		if (urlKey == null)
			return "";
		
		return "http://www.imdb.com/title/tt" + urlKey;
	}
	
	public String getCover() {
		if (cover == null)
			return "";
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public abstract void updateCoverData();

	public byte [] getCoverData() {
		return coverData;
	}
	
	public void setCoverData(byte [] data) {
		coverData = data;
	}
	
	public String getDate() {
		if (date == null)
			return "";
		return date;
	}

	public String getSortDate() {
		if (date == null)
			return "0";
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {		
		this.title = title;
	}

	public String getDirectedBy() {
		if (directedBy == null)
			return "";
		return directedBy;
	}

	public void setDirectedBy(String directedBy) {
		this.directedBy = directedBy;
	}

	public String getWrittenBy() {
		return writtenBy;
	}

	public void setWrittenBy(String writtenBy) {
		this.writtenBy = writtenBy;
	}

	public String getGenre() {
		if (genre == null)
			return "";
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getRating() {
		return rating;
	}

	public String getSortRating() {
		if (rating.equals(""))
			return "0";
		return rating;
	}

	public void setRating(String rating) {
		
		if (rating == null || rating.equals("-1.0"))
			rating = "";
		
		this.rating = rating;
	}	
	
	
	public String getPersonalRating() {
		return personalRating;
	}
	
	public void setPersonalRating(String personalRating) {
		
		if (personalRating == null || personalRating.equals("-1.0"))
			personalRating = "";
		
		this.personalRating = personalRating;
	}

	public String getPlot() {
		if (plot == null)
			return "";
		return plot;
	}
	
	public void setPlot(String plot) {
		this.plot = plot;
	}

	public String getCast() {
		if (cast == null)
			return "";
		return cast;
	}

	public void setCast(String cast) {
		this.cast = cast;
	}

	public String getNotes() {
		if (notes == null)
			return "";
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public boolean getSeen() {
		return seen;
	}

	public void setSeen(boolean seen) {
		this.seen = seen;
	}

	public String getAka() {
		if (aka == null)
			return "";
		return aka;
	}

	public void setAka(String aka) {
		this.aka = aka;
	}

	public String getCountry() {
		if (country == null)
			return "";
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getLanguage() {
		if (language == null)
			return "";
		return language;
	}

	public void setLanguage(String language) {
		this.language =  language;
	}

	public String getColour() {
		if (colour == null)
			return "";
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	public String getMpaa() {
		if (mpaa == null)
			return "";
		return mpaa;
	}

	public void setMpaa(String mpaa) {
		this.mpaa = mpaa;
	}

	public String getCertification() {
		if (certification == null)
			return "";
		return certification;
	}

	public void setCertification(String certification) {
		this.certification = certification;
	}

	public String getWebSoundMix() {
		if (webSoundMix == null)
			return "";
		return webSoundMix;
	}

	public void setWebSoundMix(String webSoundMix) {
		this.webSoundMix = webSoundMix;
	}

	public String getWebRuntime() {
		if (webRuntime == null)
			return "";
		return webRuntime;
	}

	public void setWebRuntime(String webRuntime) {
		this.webRuntime = webRuntime;
	}

	public String getAwards() {
		if (awards == null)
			return "";
		return awards;
	}

	public void setAwards(String awards) {
		this.awards = awards;
	}

	
	/* Convenience method for setting values */
	public boolean setValue(String fieldName, String value, String tableName) {

		if (!tableName.equals("General Info"))
			return additionalInfo.setValue(fieldName, value, tableName);

		if (fieldName.equalsIgnoreCase("Title"))
			setTitle(value);
		else if (fieldName.equalsIgnoreCase("Cover"))
			setCover(value);
		else if (fieldName.equalsIgnoreCase("Imdb"))
			setUrlKey(value);
		else if (fieldName.equalsIgnoreCase("Date"))
			setDate(value);
		else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Directed By"))
			setDirectedBy(value);
		else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Written By"))
			setWrittenBy(value);
		else if (fieldName.equalsIgnoreCase("Genre"))
			setGenre(value);
		else if (fieldName.equalsIgnoreCase("Rating"))
			setRating(value);
		else if (fieldName.equalsIgnoreCase("Seen"))
			setSeen(new Boolean(value).booleanValue());
		else if (fieldName.equalsIgnoreCase("Plot"))
			setPlot(value);
		else if (fieldName.equalsIgnoreCase("Cast"))
			setCast(value);
		else if (fieldName.equalsIgnoreCase("Notes"))
			setNotes(value);
		else if (fieldName.equalsIgnoreCase("Aka"))
			setAka(value);
		else if (fieldName.equalsIgnoreCase("Country"))
			setCountry(value);
		else if (fieldName.equalsIgnoreCase("Language"))
			setLanguage(value);
		else if (fieldName.equalsIgnoreCase("Colour"))
			setColour(value);
		else if (fieldName.equalsIgnoreCase("Certification"))
			setCertification(value);
		else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Sound Mix"))
			setWebSoundMix(value);
		else if (fieldName.equalsIgnoreCase("Mpaa"))
			setMpaa(value);
		else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Web Runtime"))
			setWebRuntime(value);
		else if (fieldName.equalsIgnoreCase("Awards"))
			setAwards(value);

		return true;
	}

	
	/* Convenience method for setting values */
	public String getValue(String fieldName, String tableName) {

		if (!tableName.equals("General Info"))
			return additionalInfo.getValue(fieldName, tableName);

		if (fieldName.equalsIgnoreCase("Title"))
			return getTitle();
		else if (fieldName.equalsIgnoreCase("Cover"))
			return getCover();
		else if (fieldName.equalsIgnoreCase("Imdb"))
			return getUrlKey();
		else if (fieldName.equalsIgnoreCase("Date"))
			return getDate();
		else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Directed By"))
			return getDirectedBy();
		else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Written By"))
			return getWrittenBy();
		else if (fieldName.equalsIgnoreCase("Genre"))
			return getGenre();
		else if (fieldName.equalsIgnoreCase("Rating"))
			return getRating();
		else if (fieldName.equalsIgnoreCase("Seen"))
			return "" + getSeen();
		else if (fieldName.equalsIgnoreCase("Plot"))
			return getPlot();
		else if (fieldName.equalsIgnoreCase("Cast"))
			return getCast();
		else if (fieldName.equalsIgnoreCase("Notes"))
			return getNotes();
		else if (fieldName.equalsIgnoreCase("Aka"))
			return getAka();
		else if (fieldName.equalsIgnoreCase("Country"))
			return getCountry();
		else if (fieldName.equalsIgnoreCase("Language"))
			return getLanguage();
		else if (fieldName.equalsIgnoreCase("Colour"))
			return getColour();
		else if (fieldName.equalsIgnoreCase("Certification"))
			return getCertification();
		else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Sound Mix"))
			return getWebSoundMix();
		else if (fieldName.equalsIgnoreCase("Mpaa"))
			return getMpaa();
		else if (fieldName.replaceFirst("_", " ").equalsIgnoreCase("Web Runtime"))
			return getWebRuntime();
		else if (fieldName.equalsIgnoreCase("Awards"))
			return getAwards();

		return "";
	}

	
	public void copyData(ModelEntry model) {

		setKey(model.getKey());
		
		setUrlKey(model.getUrlKey());
		setCover(model.getCover());
		setDate(model.getDate());
		setTitle(model.getTitle());
		setDirectedBy(model.getDirectedBy());
		setWrittenBy(model.getWrittenBy());
		setGenre(model.getGenre());
		setRating(model.getRating());
		setPersonalRating(model.getPersonalRating());
		setPlot(model.getPlot());
		setCast(model.getCast());
		setNotes(model.getNotes());
		setSeen(model.getSeen());
		setAka(model.getAka());
		setCountry(model.getCountry()); 
		setLanguage(model.getLanguage());
		setColour(model.getColour());
		setCertification(model.getCertification());
		setWebSoundMix(model.getWebSoundMix());
		setWebRuntime(model.getWebRuntime());
		setAwards(model.getAwards());
		setMpaa(model.getMpaa());
		
		if (model.getCoverData() != null)
			setCoverData(model.getCoverData());

		hasGeneralInfoData = model.getHasGeneralInfoData();
		
		if (model.getHasAdditionalInfoData())
			setAdditionalInfo(model.getAdditionalInfo());
		
		hasChangedNotes = model.hasChangedNotes;
	}
	

	public String toString() { 
		return title;
	}
}
