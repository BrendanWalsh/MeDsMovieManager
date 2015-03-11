/**
 * @(#)MovieManagerCommandImportHandler.java 1.0 26.09.05 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands.importexport;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler.ImportExportReturn;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogIMDB;
import net.sf.xmm.moviemanager.gui.DialogIMDbImport;
import net.sf.xmm.moviemanager.gui.DialogIMDbUpdate;
import net.sf.xmm.moviemanager.gui.DialogIMDbMultiAdd;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies.Files;
import net.sf.xmm.moviemanager.imdblib.IMDb;
import net.sf.xmm.moviemanager.imdblib.IMDbLib;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;

public abstract class MovieManagerCommandImportHandler implements MovieManagerCommandImportExportHandler {

	static Logger log = Logger.getLogger(MovieManagerCommandImportHandler.class);
	
	private boolean cancelled = false;
	boolean aborted = false;
	
	ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
	ModelMovie movie = null;
	
	ModelImportExportSettings settings;
	
	ArrayList<String> addToThisList = new ArrayList<String>();
	
	MovieManagerCommandImportHandler(ModelImportExportSettings settings) {
		this.settings = settings;
		addToThisList.add(settings.addToThisList);
		
		if (settings.isIMDbEnabled() &&
				(!MovieManager.getIt().getDatabase().isMySQL() || 
				(MovieManager.getIt().getDatabase().isMySQL() && MovieManager.getConfig().getStoreCoversLocally())))
			modelMovieInfo.setSaveCover(true);
		else
			modelMovieInfo.setSaveCover(false);
	}
	
	public boolean isImporter() {
		return true;
	}
	
	public boolean isExporter() {
		return false;
	}
	
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
		modelMovieInfo.clearModel();
	}
		
	public void setAborted(boolean abort) {
		aborted = abort;
	}
		
	public boolean isCancelled() {
		return cancelled;
	}
	
	public boolean isAborted() {
		return aborted;
	}
	
	public void resetStatus() {
		cancelled = false;
	}
	
	public void execute() throws Exception {};
	
	public void done() throws Exception {};
		
	public abstract ImportExportReturn addMovie(int i) throws Exception;
	
	public abstract void retrieveMovieList() throws Exception;
	
	public abstract int getMovieListSize() throws Exception;
	
	public abstract String getTitle(int i) throws Exception;
	
	ImportExportReturn ret;
		
	/**
	 * Gets the IMDB info for movies (multiAdd)
	 **/
	public ImportExportReturn executeCommandGetIMDBInfoMultiMovies(final String searchString, final ModelImportExportSettings settings, final ModelMovie model) {

		ret = ImportExportReturn.success;

		/* Checks the movie title... */
		log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$

		try {

			if (searchString.equals("")) { //$NON-NLS-1$
				DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("DialogMovieInfo.alert.title.alert"), Localizer.get("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
				GUIUtil.showAndWait(alert, true);
				return ImportExportReturn.error;
			}

			addToThisList.clear();
			
			// Only pull list from imdb if not "Select FirstHit" is selected and no IMDB Id was found in an nfo/txt file
			final ArrayList<ModelIMDbSearchHit> hits = IMDbLib.newIMDb(MovieManager.getConfig().getHttpSettings()).getSimpleMatches(searchString);

			/*Number of movie hits*/
			int hitCount = hits.size();

			if ((hitCount > 0 && (settings.multiAddIMDbSelectOption == ImdbImportOption.selectFirst ||
					settings.multiAddIMDbSelectOption == ImdbImportOption.selectFirstOrAddToSkippedList)) || 
					(hitCount == 1 && (settings.multiAddIMDbSelectOption == ImdbImportOption.selectIfOnlyOneHit ||
							settings.multiAddIMDbSelectOption == ImdbImportOption.selectIfOnlyOneHitOrAddToSkippedList))) {

				addToThisList.add(settings.addToThisList);
				DialogIMDB.getIMDbInfo(model, hits.get(0).getUrlID());
			}
			else if (hitCount == 0 && 
					(settings.multiAddIMDbSelectOption == ImdbImportOption.selectFirstOrAddToSkippedList || 
							settings.multiAddIMDbSelectOption == ImdbImportOption.selectIfOnlyOneHitOrAddToSkippedList)) {
				addToThisList.add(settings.skippedListName);
			}
			else {

				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						DialogIMDbImport dialogIMDB;
						
						if (settings.getParent() == null)
							dialogIMDB = new DialogIMDbImport(model, searchString, hits);
						else
							dialogIMDB = new DialogIMDbImport(settings.getParent(), model, searchString, hits);
						
						GUIUtil.showAndWait(dialogIMDB, true);
						
						if (dialogIMDB.getCanceled()) {
							setCancelled(true);
							ret = ImportExportReturn.cancelled;
						}
						
						if (dialogIMDB.getAborted()) {
							setAborted(true);
							ret = ImportExportReturn.aborted;
						}

						addToThisList.add(settings.addToThisList);
					}
				});
			}
		} catch (Exception e) {
			log.debug("Exception:" + e.getMessage(), e);
		}
		return ret;
	}
}
