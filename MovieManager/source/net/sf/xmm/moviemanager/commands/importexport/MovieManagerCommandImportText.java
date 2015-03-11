/**
 * @(#)MovieManagerCommandImportText.java 1.0 26.09.05 (dd.mm.yy)
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler.ImportExportReturn;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;

import org.apache.log4j.Logger;

public class MovieManagerCommandImportText extends MovieManagerCommandImportHandler {

	static Logger log = Logger.getLogger(MovieManagerCommandImportHandler.class);

	ArrayList<ModelMovie> movieList = null;
	
	MovieManagerCommandImportText(ModelImportExportSettings settings) {
		super(settings);
	}
	
	public int getMovieListSize() throws Exception {
		
		if (movieList == null)
			retrieveMovieList();
			
		if (movieList == null)
			return -1;
		
		return movieList.size();
	}
	
	
	public String getTitle(int i) {
		
		modelMovieInfo.clearModel();
		
		String title = ((ModelMovie) movieList.get(i)).getTitle();
		return title;
	}	
	
	
	public ImportExportReturn addMovie(int i) {
		
		ImportExportReturn ret = ImportExportReturn.success;
		
		ModelMovie movie = (ModelMovie) movieList.get(i);
		String title = movie.getTitle();
		
		if (title != null && !title.equals("")) {

			if (settings.multiAddIMDbSelectOption != ImdbImportOption.off) {
				ret = executeCommandGetIMDBInfoMultiMovies(title, settings, (ModelMovie) movieList.get(i));
				
				if (ret == ImportExportReturn.cancelled || ret == ImportExportReturn.aborted) {
					return ret;
				}
			}
		}

		modelMovieInfo.setModel(movie, false, false);

		try {						
			int key = (modelMovieInfo.saveToDatabase(addToThisList)).getKey();
			
			if (key == -1)
				ret = ImportExportReturn.error;
			
		} catch (Exception e) {
			log.error("Saving to database failed.", e);
			ret = ImportExportReturn.error;
		}
		
		return ret;
	}
	 
	
	public void retrieveMovieList() throws Exception {

		File textFile = new File(settings.filePath);

		if (!textFile.isFile()) {
			throw new Exception("Text file does not exist.");
		}

		movieList = new ArrayList<ModelMovie>(10);

		try {

			ModelMovie tmpMovie;
			FileReader reader = new FileReader(textFile);
			BufferedReader stream = new BufferedReader(reader);

			
			String line;
			while ((line = stream.readLine()) != null) {
				tmpMovie = new ModelMovie();
				tmpMovie.setTitle(line.trim());
				movieList.add(tmpMovie);
			}
		}
		catch (Exception e) {
			log.error("", e);
		}
	}
}
