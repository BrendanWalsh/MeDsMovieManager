/**
 * @(#)MovieManagerCommandExportHandler.java 1.0 26.09.05 (dd.mm.yy)
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

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;

import org.apache.log4j.Logger;

/**
 * The Class MovieManagerCommandExportHandler.
 */
public abstract class MovieManagerCommandExportHandler implements MovieManagerCommandImportExportHandler {

	/** The log. */
	static Logger log = Logger.getLogger(MovieManagerCommandExportHandler.class);
	
	/** The cancelled. */
	boolean cancelled = false;
	
	/** The aborted. */
	boolean aborted = false;
	
	/** The model movie info. */
	ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
	
	/** The movie. */
	//private ModelMovie movie = null;

	/** The movie list. */
	//private ArrayList movieList = null;
	
	/** The list to add movie to. */
	public String listToAddMovieTo = null;
	
	public void resetStatus() {
		cancelled = false;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#setCancelled(boolean)
	 */
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
		modelMovieInfo.clearModel();
	}
		
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#setAborted(boolean)
	 */
	public void setAborted(boolean abort) {
		aborted = abort;
	}
		
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#isCancelled()
	 */
	public boolean isCancelled() {
		return cancelled;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#isAborted()
	 */
	public boolean isAborted() {
		return aborted;
	}
	
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#execute()
	 */
	public void execute() throws Exception {};
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#retrieveMovieList()
	 */
	public abstract void retrieveMovieList() throws Exception;
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#getMovieListSize()
	 */
	public abstract int getMovieListSize() throws Exception;
		
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#getTitle(int)
	 */
	public abstract String getTitle(int i) throws Exception;
	
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#addMovie(int)
	 */
	public abstract ImportExportReturn addMovie(int i) throws Exception;
	
	/* (non-Javadoc)
	 * @see net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler#done()
	 */
	public void done() throws Exception {};
	
	
	public boolean isImporter() {
		return false;
	}
	
	public boolean isExporter() {
		return true;
	}
	
    /**
	 * Gets the database data.
	 * 
	 * @return the database data
	 */
	public Object [][] getDatabaseData() {

		ArrayList<String> generalInfoFieldNames = MovieManager.getIt().getDatabase().getGeneralInfoMovieFieldNames();
		ArrayList<String> additionalInfoFieldNames = MovieManager.getIt().getDatabase().getAdditionalInfoFieldNames();
		ArrayList<String> extraInfoFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);

		// Available for MySQL only
		generalInfoFieldNames.remove("CoverData");
		
		int columnCount = generalInfoFieldNames.size() + additionalInfoFieldNames.size() + extraInfoFieldNames.size();

		ArrayList<ModelMovie> movies = MovieManager.getDialog().getCurrentMoviesList();
		Object [][] data = new Object[movies.size()][columnCount];

		for (int i = 0; i < movies.size(); i++) {

			int tableIndex = 0;
						
			if (!movies.get(i).getHasGeneralInfoData())
				movies.get(i).updateGeneralInfoData();
			
			if (!movies.get(i).getHasAdditionalInfoData())
				movies.get(i).updateAdditionalInfoData();
				
			
			for (int o = 0; o < generalInfoFieldNames.size(); o++) {
				data[i][tableIndex] = movies.get(i).getValue((String) generalInfoFieldNames.get(o), "General Info");
				tableIndex++;
			}

			for (int o = 0; o < additionalInfoFieldNames.size(); o++) {
				data[i][tableIndex] = movies.get(i).getValue((String) additionalInfoFieldNames.get(o), "Additional Info");
				tableIndex++;
			}
			
			for (int o = 0; o < extraInfoFieldNames.size(); o++) {
				data[i][tableIndex] = movies.get(i).getValue((String) extraInfoFieldNames.get(o), "Extra Info");
				tableIndex++;
			}
		}
			
		return data;
	}
}
