/**
 * @(#)ModelEpisode.java 29.01.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.models;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;

public class ModelEpisode extends ModelEntry {

	Logger log = Logger.getLogger(getClass());
	
	public static boolean notesHaveBeenChanged = false;
	
	/*The key to the entry this episode is linked to.*/
	private int movieKey = -1;

	/*The database key for this episode.*/
	private int episodeKey;

	private int episodeNumber = -1;
	private int seasonNumber = -1;

	private String episodeTitle = null;
	
	/* default public constructor for XML export using Castor */
	public ModelEpisode() {
		additionalInfo = new ModelAdditionalInfo();
	}

	public ModelEpisode(int movieKey) {
		this.movieKey = movieKey;
		additionalInfo = new ModelAdditionalInfo();
	}

	public ModelEpisode(ModelEpisode model) {
		copyData(model);
	}

	/**
	 * The constructor.
	 **/
	public ModelEpisode(int key, int movieKey, int episodeKey, String urlKey, 
			String cover, String date, String title, String directedBy, 
			String writtenBy, String genre, String rating, String personalRating, String plot, 
			String cast, String notes, boolean seen, String aka, String country, 
			String language, String colour, String certification, 
			String webSoundMix, String webRuntime, String awards) {

		setKey(key);
		this.movieKey = movieKey;
		setEpisodeKey(episodeKey);
		setUrlKey(urlKey);
		setCover(cover);
		setDate(date);
		setTitle(title);
		setDirectedBy(directedBy);
		setWrittenBy(writtenBy);
		setGenre(genre);
		setRating(rating);
		setPersonalRating(personalRating);
		setPlot(plot);
		setCast(cast);
		setNotes(notes);
		setSeen(seen);
		setAka(aka);
		setCountry(country); 
		setLanguage(language);
		setColour(colour);
		setCertification(certification);
		//setMpaa(mpaa); Not yet implemented in Episodes (database field is missing)
		setWebSoundMix(webSoundMix);
		setWebRuntime(webRuntime);
		setAwards(awards);
		
		additionalInfo = new ModelAdditionalInfo();
		hasGeneralInfoData = true;
	}

	public ModelEpisode(int key, int movieKey, int episodeKey, String title, String cover) {

		setKey(key);
		this.movieKey = movieKey;
		setEpisodeKey(episodeKey);
		setTitle(title);
		setCover(cover);
		
		additionalInfo = new ModelAdditionalInfo();
	}

	public boolean isEpisode() {
		return true;
	}
	

	public String getEpisodeTitle() {
		
		if (episodeTitle == null && getTitle() != null) {
		
			if (seasonNumber == -1)
				episodeTitle = getTitle();
			else {
				episodeTitle = "S" + seasonNumber + "E" + episodeNumber +  (episodeNumber < 10 ? "   - " : " - ") + getTitle();
			}
		}
		return episodeTitle;
	}
	
	public int getMovieKey() {
		return movieKey; 
	}

	public void setMovieKey(int movieKey) {
		this.movieKey = movieKey; 
	}

	public int getEpisodeKey() {
		return episodeKey; 
	}

	public void setEpisodeKey(int episodeKey) {
		
		this.episodeKey = episodeKey; 
		
		// Does not contain valid season/episode info
		if (episodeKey < 10000) {
			//log.warn("Old episode with episodeKey smaller than 10000:" + episodeKey);
			return;
		}
		
		String tmp = String.valueOf(episodeKey);
		seasonNumber = new Integer(tmp.substring(0, tmp.length() - 4)).intValue();		
		episodeNumber = new Integer(tmp.substring(tmp.length() - 4, tmp.length())).intValue();
	}
	
	
	public int getEpisodeNumber() {
		return episodeNumber; 
	}
	
	public int getSeasonNumber() {
		return seasonNumber; 
	}
	
	public String getCompleteUrl() {

		// Old entry with data from tv.com
		if (episodeKey < 10000) {
			return "http://www.tv.com"+ getUrlKey() + "summary.html";
		}
				
		return "http://www.imdb.com/title/tt" + getUrlKey();
	}
	
	public void copyData(ModelEntry model) {
		
		super.copyData(model);
		this.movieKey = ((ModelEpisode) model).getMovieKey();
		setEpisodeKey(((ModelEpisode) model).getEpisodeKey());
	}
	

	public void updateGeneralInfoData() {
		
		if (getKey() != -1) {

			ModelEntry model = null;
			model = MovieManager.getIt().getDatabase().getEpisode(getKey());

			if (model != null) {
				copyData(model);
				hasGeneralInfoData = true;
			}
		}
	}
	
	
	public void updateGeneralInfoData(boolean getCover) {
		if (getKey() != -1 && MovieManager.getIt().getDatabase().isMySQL()) {

			ModelEntry model = null;
			model = ((DatabaseMySQL) MovieManager.getIt().getDatabase()).getEpisode(getKey(), getCover);

			if (model != null) {
				copyData(model);
			}
		}
	}
	

	public void updateCoverData() {

		if (MovieManager.getIt().getDatabase().isMySQL())
			setCoverData(((DatabaseMySQL) MovieManager.getIt().getDatabase()).getCoverDataEpisode(getKey()));
	}

	public void updateAdditionalInfoData() {
			
		if (getKey() != -1) {
			
			ModelAdditionalInfo tmp = MovieManager.getIt().getDatabase().getAdditionalInfo(getKey(), true);
		
			if (tmp != null) {
				setAdditionalInfo(tmp);
			}
		}
		
		if (additionalInfo == null)
			additionalInfo = new ModelAdditionalInfo();
	}
}
