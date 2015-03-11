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

package net.sf.xmm.moviemanager.models.imdb;

public class ModelIMDbEpisode extends ModelIMDbEntry {
	
	private String seasonNumber = "";
	private String episodeNumber = "";
	
	public ModelIMDbEpisode() {}
	
	/**
	 * The constructor.
	 **/
	public ModelIMDbEpisode(String episodeNumber, String seasonNumber, String urlID, String cover, String date, String title, 
			String directedBy, String writtenBy, String genre, String rating, String plot, String cast,
			String aka, String country, String language, String colour, 
			String certification, String mpaa, String webSoundMix, String webRuntime, String awards) {
		
		super(urlID, cover, date, title, directedBy, writtenBy, genre, 
				rating, plot, cast, aka, country, language, colour, certification, 
				mpaa, webSoundMix, webRuntime, awards);
		
		this.episodeNumber = episodeNumber;
		this.seasonNumber = seasonNumber;
	}


	public boolean isEpisode() {
		return true;
	}
	
	public String getEpisodeNumber() {
		return episodeNumber; 
	}
	
	public void setEpisodeNumber(String episodeNumber) {
		this.episodeNumber = episodeNumber; 
	}
	
	public String getSeasonNumber() {
		return seasonNumber; 
	}
	
	public void setSeasonNumber(String seasonNumber) {
		this.seasonNumber = seasonNumber; 
	}
	
}
