/**
 * @(#)ModelSearchHit.java 1.0 19.01.06 (dd.mm.yy)
 *
 * Copyright (2003) Bro
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


public class ModelIMDbSearchHit {
	
	/**
	 * The show number used in the url.
	 **/
	private String urlID;

	private String completeIMDbURLPrefix = "http://www.imdb.com/title/tt";
		
	/**
	 * The title.
	 **/
	private String title;

	public StringBuffer seasonStream = null;
	
	public boolean processed = false;
	public boolean error = false;
	
	// Index value in list of hits
	public int index = -1;
	
	private String hitCategory = null;
	
	/**
	 * Aka
	 */
	private String aka;

	/**
	 * The string that is used to search for the episode when parsing the html code.
	 **/
	private String searchTitle;

	private String date;
	
	/**
	 * The season number
	 **/
	private int seasonNumber;

	/**
	 * The episode number
	 **/
	private int streamNumber;


	private String coverExtension;

	// A model that may be used to store the data retrieved from a search on this specific search hit 
	private ModelIMDbEntry model;
	
	public void setDataModel(ModelIMDbEntry model) {
		this.model = model;
	}
	
	public ModelIMDbEntry getDataModel() {
		return model;
	}
	
	/**
	 * Used for message to user like "Connection time out and "no hits"
	 * @param title
	 */
	public ModelIMDbSearchHit(String title) {
		this(null, title, null, null);
	}
	
	public ModelIMDbSearchHit(String key, String title, String date) {
		this(key, title, date, null, null);
	}
		
	public ModelIMDbSearchHit(String key, String title, String date, String aka) {
		this(key, title, date, aka, null);
	}

	
	public ModelIMDbSearchHit(String key, String title, String date, String aka, String hitCategory) {
		urlID = key; 
		this.title = title;
		this.date = date;
		this.aka = aka;
		this.hitCategory = hitCategory;
	}

	
	/**
	 * The constructor.
	 **/
	public ModelIMDbSearchHit(String urlID, String title) {
		this.urlID = urlID; 
		this.title = title;
	}

	/**
	 * The constructor.
	 **/
	public ModelIMDbSearchHit(String showKey, String title, int seasonNumber) {
		this.urlID = showKey; 
		this.title = title;
		this.seasonNumber = seasonNumber;
	}

	/**
	 * The constructor.
	 **/
	public ModelIMDbSearchHit(String showKey, String title, String searchTitle, int seasonNumber, int streamNumber) {
		this.urlID = showKey; 
		this.title = title;
		this.searchTitle = searchTitle;
		this.seasonNumber = seasonNumber;
		this.streamNumber = streamNumber;
	}

	/**
	 * Gets the key.
	 **/
	public String getUrlID() {
		return urlID; 
	}

	public String getCompleteUrl() {
		return completeIMDbURLPrefix + urlID;
	}

	/**
	 * Gets the title.
	 **/
	public String getTitle() {
		return title;
	}

	public String getDate() {
		return date;
	}
	
	public String getAka() {
		return aka;
	}

	public String getHitCategory() {
		return hitCategory;
	}
	
		
	/**
	 * Gets the title.
	 **/
	public String getSearchTitle() {
		return searchTitle;
	}

	/**
	 * Gets the title.
	 **/
	public int getSeasonNumber() {
		return seasonNumber;
	}

	/**
	 * Gets the title.
	 **/
	public int getStreamNumber() {
		return streamNumber;
	}

	/**
	 * Gets the title.
	 **/
	public String getCoverExtension() {
		return coverExtension;
	}

	/**
	 * Gets the title.
	 **/
	public void setCoverExtension(String coverExtension) {
		this.coverExtension = coverExtension;
	}

	/**
	 * Gets the title.
	 **/
	protected void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Returns the title.
	 **/
	public String toString() {
		
		if (date != null && !date.equals(""))
			return title + " (" + date + ")";
			
		return title;
	}
}
