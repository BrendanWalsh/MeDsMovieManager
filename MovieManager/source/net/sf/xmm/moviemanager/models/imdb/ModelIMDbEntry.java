/**
 * @(#)ModeIMDblEntry.java 
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

package net.sf.xmm.moviemanager.models.imdb;

abstract public class ModelIMDbEntry {

	boolean loggedIn = false;
	
	private String urlID = ""; 
	private String coverName = "";
	private String title = "";
	private String imdbTitle = ""; // original title on IMDb with no changes 
	private String date = "";
	private String directedBy = "";
	private String writtenBy = "";
	private String genre = "";
	private String rating = "";
	private String plot = "";
	private String cast = "";

	private String personalRating = "";
	
	private String voteUrlID = null;
	
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

	private String coverURL = "";
		
	private byte [] coverData = null;
	
	public String bigCoverUrlId = null;
	
	private byte [] bigCoverData = null;
	
	ModelIMDbEntry() {}
	
	ModelIMDbEntry(String urlID, String cover, String date, String title, 
			String directedBy, String writtenBy, String genre, String rating, String plot, 
			String cast, String aka, String country, String language, String colour, 
			String certification, String mpaa, String webSoundMix, String webRuntime, String awards) {

		setUrlID(urlID);
		setCoverName(cover);
		setDate(date);
		setTitle(title);
		setDirectedBy(directedBy);
		setWrittenBy(writtenBy);
		setGenre(genre);
		setRating(rating);
		setPlot(plot);
		setCast(cast);
		setAka(aka);
		setCountry(country); 
		setLanguage(language);
		setColour(colour);
		setCertification(certification);
		setMpaa(mpaa);
		setWebSoundMix(webSoundMix);
		setWebRuntime(webRuntime);
		setAwards(awards);
	}
	
	public boolean isEpisode() {
		return false;
	}

	public boolean isSeries() {
		return false;
	}
	
	public boolean isMovie() {
		return false;
	}
	
	public String getVoteUrlID() {
		return voteUrlID;
	}

	public void setVoteUrlID(String voteUrlID) {
		this.voteUrlID = voteUrlID;
	}
	
	
	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	public void setLoggedIn(boolean loggedIn) {	
		this.loggedIn = loggedIn;
	}
	
	public String getUrlID() {
		if (urlID == null)
			return "";
		return urlID;
	}

	public void setUrlID(String urlID) {
		this.urlID = urlID;	
	}

	public String getCoverName() {
		if (coverName == null)
			return "";
		return coverName;
	}

	public void setCoverName(String coverName) {
		this.coverName = coverName;
	}


	public byte [] getCoverData() {
		return coverData;
	}

	
	public void setCoverData(byte [] data) {
		coverData = data;
	}

	public boolean hasCover() {
		return coverData != null;
	}
	
	
	public byte [] getBigCoverData() {
		return bigCoverData;
	}

	
	public void setBigCoverData(byte [] data) {
		bigCoverData = data;
	}
	
	public boolean hasBigCover() {
		return bigCoverData != null;
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

	public String getIMDbTitle() {
		return imdbTitle;
	}

	public void setIMDbTitle(String title) {
		this.imdbTitle = title;
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
		if (rating == null || rating.equals("-1.0"))
			return "";
		return rating;
	}

	public String getSortRating() {
		if (rating == null || rating.equals("-1.0"))
			return "0";
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getPersonalRating() {
		return personalRating;
	}
	
	public void setPersonalRating(String personalRating) {
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

	public String getCoverURL() {
		if (coverURL == null)
			return "";
		return coverURL;
	}

	public void setCoverURL(String coverURL) {
		this.coverURL = coverURL;
	}
	

	public String toString() { 
		return title;
	}
}
