/**
 * @(#)MovieManagerCommandExportExcel.java 1.0 26.09.05 (dd.mm.yy)
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

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler.ImportExportReturn;
import net.sf.xmm.moviemanager.gui.DialogTableExport;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;

public class MovieManagerCommandExportExcel extends MovieManagerCommandExportHandler {
	
	static Logger log = Logger.getLogger(MovieManagerCommandExportExcel.class);
	
	File output;
	
	Object [][] data = null;
	
	ModelImportExportSettings settings;
	DialogTableExport dialogExportTable = null;
	
	StringWriter writer = new StringWriter(100);
	
	ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
	
	Object [][] tableData = null;
	
	int titleColumnIndex = -1;	
	WritableWorkbook workbook = null;
	WritableSheet sheet = null;
	
	MovieManagerCommandExportExcel(ModelImportExportSettings exportSettings) {
		settings = exportSettings;
		
		try {
			workbook = Workbook.createWorkbook(exportSettings.getFile());
			sheet = workbook.createSheet("Movies", 0);
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
	}
	
	
	public void execute() {
			
		data = getDatabaseData();
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				
				public void run() {
					dialogExportTable = new DialogTableExport(MovieManager.getDialog(), data, settings);
					GUIUtil.showAndWait(dialogExportTable, true);

					if (dialogExportTable.cancelled)
						setCancelled(true);
				}
			});
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
	}


	public void retrieveMovieList() throws Exception {
		tableData = dialogExportTable.retrieveValuesFromTable();
		titleColumnIndex = dialogExportTable.titleColumnIndex;
	}

	public String getTitle(int i) {
				
		try {

			if (tableData == null)
				retrieveMovieList();

			if (tableData == null) {
				return null;
			}
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
		
		if (titleColumnIndex == -1) {
			String ret = "";
			
			for (int u = 0; u < tableData[0].length; u++) {
				ret += (String) tableData[i][u] + ",  ";
			}
			return ret;
		}
		else
			return (String) tableData[i][titleColumnIndex];
	}
	
	public int getMovieListSize() {
		
		
		try {
			if (tableData == null)
				retrieveMovieList();

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}

		return tableData.length;
	}
	
	public void done() throws Exception {
		workbook.write(); 
		workbook.close();
		log.debug("Excel export finished.");
	}
		
	public ImportExportReturn addMovie(int i) {
		
		try {
			for (int u = 0; u < tableData[0].length; u++) {
				Label label = new Label(u, i, (String) tableData[i][u]);
				sheet.addCell(label);
			}
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
			 return ImportExportReturn.error;
		}
		         
		return ImportExportReturn.success;
	}	
}
