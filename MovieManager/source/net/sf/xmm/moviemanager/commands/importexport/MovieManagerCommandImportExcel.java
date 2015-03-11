/**
 * @(#)MovieManagerCommandImportExcel.java 1.0 26.09.05 (dd.mm.yy)
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

import org.apache.log4j.Logger;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler.ImportExportReturn;
import net.sf.xmm.moviemanager.gui.DialogTableExport;
import net.sf.xmm.moviemanager.gui.DialogTableImport;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.util.GUIUtil;

public class MovieManagerCommandImportExcel extends MovieManagerCommandImportHandler {

	static Logger log = Logger.getLogger(MovieManagerCommandImportExcel.class);
	
	ArrayList<ModelMovie> movieList = null;
	
	Object [][] tableData;
	int len = -1;

	int titleColumnIndex = 0;
	
	DialogTableImport dialogImportTable = null;
	
	MovieManagerCommandImportExcel(ModelImportExportSettings settings) {
		super(settings);
	}

	public void execute() {

		final Object [][] data = readData();

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				
				public void run() {
					dialogImportTable = new DialogTableImport(MovieManager.getDialog(), data, settings);
					GUIUtil.showAndWait(dialogImportTable, true);
					
					if (dialogImportTable.cancelled)
						setCancelled(true);
				}
			});
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}		
	}


	public int getMovieListSize() {

		if (movieList == null)
			movieList = dialogImportTable.retrieveMovieListFromTable();
		
		return movieList.size();
	}

	
	public String getTitle(int i) {
		modelMovieInfo.clearModel();

		try {

			if (movieList == null)
				retrieveMovieList();

			if (movieList == null) {
				return null;
			}
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}

		String title = ((ModelMovie) movieList.get(i)).getTitle();

		return title;
	}



	public ImportExportReturn addMovie(int i) {

		int key = -1;
		ModelMovie movie = movieList.get(i);

		String title = movie.getTitle();

		if (title != null && !title.equals("")) {
	
			if (settings.multiAddIMDbSelectOption != ImdbImportOption.off) {
								
				ImportExportReturn ret = executeCommandGetIMDBInfoMultiMovies(title, settings, movie);

				if (ret == ImportExportReturn.cancelled || ret == ImportExportReturn.aborted) {
					return ret;
				}
			}
		}
		
		modelMovieInfo.setModel(movie, false, false);

		try {
			key = modelMovieInfo.saveToDatabase(addToThisList).getKey();
			modelMovieInfo.saveCoverToFile();
		} catch (Exception e) {
			log.error("Saving to database failed.", e);
			return ImportExportReturn.error;
		}
		
		if (key == -1)
			return ImportExportReturn.error;
		         
		return ImportExportReturn.success;
	}
	
	
	public void done() throws Exception {
		log.debug("EXCEL import completetd");
	}

	
	//  Retrieved the data from the table and stores it in movieList in super class.
	public void retrieveMovieList() {
		tableData = dialogImportTable.retrieveValuesFromTable();
		titleColumnIndex = dialogImportTable.titleColumnIndex;
	}
	

	// Returns the data read from excel file
	public Object [][] readData() {

		Object [][] data = null;
		
		try {
			
			Workbook workbook = Workbook.getWorkbook(settings.getFile());
			Sheet sheet = workbook.getSheet(0);
			data = getTableData(sheet);
			len = data[0].length;
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}

		return data;
	}


	protected Object[][] getTableData(Sheet sheet) throws Exception {

		Object[][] tableData = null;
		Cell[] cells;;
		
		try {
			tableData = new Object[sheet.getRows()][sheet.getColumns()];
					
			for (int i = 0; i < sheet.getRows(); i++) {
				cells = sheet.getRow(i);
				
				for (int u = 0; u < cells.length; u++) {
					tableData[i][u] = cells[u].getContents();
				}
			}
		}
		catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}

		return tableData;
	}
}


