/**
 * @(#)ModelMovie.java 26.01.06 (dd.mm.yy)
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

import java.util.ArrayList;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;

public class ModelMovie extends ModelEntry {

	public static boolean notesHaveBeenChanged = false;
	
	/* default public constructor for XML import using Castor */
	public ModelMovie() {
		additionalInfo = new ModelAdditionalInfo();
	}
	
	public ModelMovie(ModelMovie model) {
		copyData(model);
	}

	/**
	 * The constructor.
	 **/
	public ModelMovie(int key, String urlKey, String cover, String date, String title, 
			String directedBy, String writtenBy, String genre, String rating, String personalRating, 
			String plot, String cast, String notes, boolean seen, String aka, 
			String country, String language, String colour, String certification, 
			String mpaa, String webSoundMix, String webRuntime, String awards) {

		setKey(key);
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
		setMpaa(mpaa);
		setWebSoundMix(webSoundMix);
		setWebRuntime(webRuntime);
		setAwards(awards);

		hasGeneralInfoData = true;
		additionalInfo = new ModelAdditionalInfo();
	}

	public ModelMovie(int key, String title) {
		setKey(key);
		setTitle(title);
		
		additionalInfo = new ModelAdditionalInfo();
	}

	public ModelMovie(int key, String title, String urlKey, String cover, String date) {
		setKey(key);
		setTitle(title);
		setUrlKey(urlKey);
		setCover(cover);
		setDate(date);
		
		additionalInfo = new ModelAdditionalInfo();
	}
	
	public boolean isMovie() {
		return true;
	}
	
	public void copyData(ModelEntry model) {
		
		super.copyData(model);
		
		// Copy the lists this movie is a member of. Only movies are members of lists
		ArrayList<String> lists = model.getMemberLists();
		setMemberOfLists(lists);
	}
		
	public void updateGeneralInfoData() {
		
		if (getKey() != -1) {

			ModelEntry model = null;
			model = MovieManager.getIt().getDatabase().getMovie(getKey());

			if (model != null) {
				copyData(model);
				hasGeneralInfoData = true;
			}
		}
	}
	
	public void updateGeneralInfoData(boolean getCover) {
		
		if (getKey() != -1 && MovieManager.getIt().getDatabase().isMySQL()) {

			ModelEntry model = null;
			model = ((DatabaseMySQL) MovieManager.getIt().getDatabase()).getMovie(getKey(), getCover);

			if (model != null) {
				copyData(model);
			}
		}
	}

	public void updateCoverData() {

		if (getKey() != -1) {
			if (MovieManager.getIt().getDatabase().isMySQL())
				setCoverData(((DatabaseMySQL) MovieManager.getIt().getDatabase()).getCoverDataMovie(getKey()));
		}
	}

	
	
	public void updateAdditionalInfoData() {
		
		if (getKey() != -1) {

			ModelAdditionalInfo tmp = MovieManager.getIt().getDatabase().getAdditionalInfo(getKey(), false);
	
			if (tmp != null) {
				setAdditionalInfo(tmp);
			}
		}
		
		if (additionalInfo == null)
			additionalInfo = new ModelAdditionalInfo();
	}
}
