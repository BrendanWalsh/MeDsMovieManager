/**
 * @(#)DatabaseConverter.java
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

package net.sf.xmm.moviemanager.database;

import java.util.ArrayList;

import javax.swing.ListModel;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelAdditionalInfo;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.util.SwingWorker;

import org.apache.log4j.Logger;


public class DatabaseConverter {

	Logger log = Logger.getLogger(getClass());

	private int lengthOfTask;
	private int current = -1;
	private boolean done = false;
	private boolean canceled = false;
	private String [] transferred;

	public DatabaseConverter(int length) {
		lengthOfTask = length;
	}

	public void go(final Database newDatabase, final ListModel movieListModel, final ArrayList<ModelEpisode> episodeList) {
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				current = -1;
				done = false;
				canceled = false;
				transferred = new String[getLengthOfTask()];

				return new Converter(newDatabase, movieListModel, episodeList);
			}
		};
		worker.start();
	}

	public int getLengthOfTask() {
		return lengthOfTask;
	}

	/*Returns the current position in the array*/
	public int getCurrent() {
		return current;
	}

	/*Stops the converting process*/
	public void stop() {
		canceled = true;
	}

	public boolean isDone() {
		return done;
	}

	/*Returns the array transferred which contains all the finished database entries*/
	public String[] getTransferred() {
		return transferred;
	}

	/**
	 * The actual database "conversion" task.
	 * This runs in a SwingWorker thread.
	 */
	class Converter {
		Converter(Database newDatabase, ListModel movieListModel,  ArrayList<ModelEpisode> episodeList) {

			/* Setting the priority of the thrad to 4 to give the GUI room to update more often */
			Thread.currentThread().setPriority(4);

			/* current database */
			Database cDb = MovieManager.getIt().getDatabase();
			ModelEntry model;
			ModelAdditionalInfo addInfo;
			int movieListSize = movieListModel.getSize();

			try {

				newDatabase.setFolders(cDb.getCoversFolder(), cDb.getQueriesFolder());
				newDatabase.setActiveAdditionalInfoFields(cDb.getActiveAdditionalInfoFields());

				ArrayList<String> extraFieldNamesList = cDb.getExtraInfoFieldNames(true);
				ArrayList<String> extraFieldValuesList;

				ArrayList<String> listsColumnNamesList = cDb.getListsColumnNames();
				ArrayList<Boolean> listsRecordValueList;


				/* Transferring movie entries */
				for (int i = 0; i < movieListSize; i++) {

					while (!done && !canceled) {

						model = (ModelMovie) movieListModel.getElementAt(i);
						int key = model.getKey();
						int index = newDatabase.addGeneralInfo(model.getKey(), model.getTitle(), model.getCover(), null,
								model.getUrlKey(), model.getDate(), 
								model.getDirectedBy(), model.getWrittenBy(),
								model.getGenre(), model.getRating(), model.getPersonalRating(), model.getSeen(), 
								model.getAka(), model.getCountry(), model.getLanguage(), 
								model.getColour(), model.getPlot(), 
								model.getCast(), model.getNotes(), model.getCertification(), model.getMpaa(), model.getWebSoundMix(), model.getWebRuntime(), model.getAwards());
						if (index != -1) {

							addInfo = cDb.getAdditionalInfo(key, false);

							if (addInfo != null) {

								newDatabase.addAdditionalInfo(index, addInfo);

								/* Copies the data in the extra info fields */
								extraFieldValuesList = new ArrayList<String>();

								for (int u = 0; u < extraFieldNamesList.size(); u++)
									extraFieldValuesList.add(cDb.getExtraInfoMovieField(key, (String) extraFieldNamesList.get(u)));

								newDatabase.addExtraInfoMovie(index, extraFieldNamesList, extraFieldValuesList);


								/* Copies the data in the lists fields */
								listsRecordValueList = new ArrayList<Boolean>();

								for (int u = 0; u < listsColumnNamesList.size(); u++)
									listsRecordValueList.add(new Boolean(cDb.getList(key, (String) listsColumnNamesList.get(u))));

								newDatabase.addLists(index, listsColumnNamesList, listsRecordValueList);
							}
							else
								log.warn("newDatabase.addAdditionalInfo("+ index +", addInfo): returned null");
						}

						current = i;
						transferred[i] = model.getTitle()+" ("+model.getDate()+")";
						break;
					}

					if (canceled) {
						break;
					}		
				}

				extraFieldNamesList = cDb.getExtraInfoFieldNames(true);

				/* Transferring episode entries */
				for (int i = movieListSize; i < lengthOfTask; i++) {

					while (!done && !canceled) {

						model = (ModelEpisode) episodeList.get(i- movieListSize);
						int key = model.getKey();

						model = cDb.getEpisode(key);

						int index = newDatabase.addGeneralInfoEpisode((ModelEpisode) model);

						if (index != -1) {

							ModelAdditionalInfo additionalModel = cDb.getAdditionalInfo(key, true);

							newDatabase.addAdditionalInfoEpisode(index, additionalModel);

							//extraFieldValuesList = new ArrayList();

							// for (int u = 0; u < extraFieldNamesList.size(); u++) {
							// 				extraFieldValuesList.add(cDb.getExtraInfoMovieField(key, (String) extraFieldNamesList.get(u)));
							// 			    }
							newDatabase.addExtraInfoEpisode(index, extraFieldNamesList, additionalModel.getExtraInfoFieldValues());
						}

						current = i;
						transferred[i] = model.getTitle()+" ("+model.getDate()+")";
						break;
					}

					if (canceled) {
						break;
					}		
				}

				done = true;

			} catch (Exception e) {
				log.error("Converter process interrupted:" + e);
			}
		}
	}
}
