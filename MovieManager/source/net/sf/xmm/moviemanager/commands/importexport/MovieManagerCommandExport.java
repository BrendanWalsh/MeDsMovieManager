/**
 * @(#)MovieManagerCommandExport.java 1.0 26.09.06 (dd.mm.yy)
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSaveChangedNotes;
import net.sf.xmm.moviemanager.gui.DialogDatabaseImporterExporter;
import net.sf.xmm.moviemanager.gui.DialogExport;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ExportMode;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;

public class MovieManagerCommandExport implements ActionListener{

	static Logger log = Logger.getLogger(MovieManagerCommandExport.class);
	
	ModelImportExportSettings exportSettings;
	DialogExport dialogExport = null;
	
	protected void execute() {

		try {

			// If any notes have been changed, they will be saved before changing list
			MovieManagerCommandSaveChangedNotes.execute();

			 SwingUtilities.invokeAndWait(new Runnable() {
		        	public void run() {
		        		dialogExport = new DialogExport();
		        		GUIUtil.showAndWait(dialogExport, true);
		        	}
		        });
			 	
			exportSettings = dialogExport.getSettings();

			if (dialogExport.isCancelled())
				return;

			MovieManagerCommandImportExportHandler exporter = null;

			//CSV  or  Excel spreadsheet 
			if (exportSettings.exportMode == ExportMode.CSV)
				exporter = new MovieManagerCommandExportCSV(exportSettings);
			else if (exportSettings.exportMode == ExportMode.EXCEL)
				exporter = new MovieManagerCommandExportExcel(exportSettings);
			else if (exportSettings.exportMode == ExportMode.XML_DATABASE)
				exporter = new MovieManagerCommandExportXMLDatabase(exportSettings);
			else if (exportSettings.exportMode == ExportMode.XML)
				exporter = new MovieManagerCommandExportXML(exportSettings);
			else if (exportSettings.exportMode == ExportMode.HTML) {
			
				if (exportSettings.getIsHTMLSimpleMode())
					exporter = new MovieManagerCommandExportToSimpleXHTML(exportSettings.getHTMLTitle());
				else
					exporter = new MovieManagerCommandExportToFullHTML(exportSettings.getHTMLAlphabeticSplit(), exportSettings.getHTMLTitle());
			}
			
			
			if (exporter != null) {
				final MovieManagerCommandImportExportHandler finalExporter = exporter;
							
				finalExporter.execute();
				
				if (!exporter.isCancelled()) {
					
					Runnable createAndShow = new Runnable() {
						public void run() {
							final JDialog dialogExorter = new DialogDatabaseImporterExporter(MovieManager.getDialog(), finalExporter, exportSettings);
							dialogExorter.setTitle("Exporter");
							GUIUtil.show(dialogExorter, true);
						}
					};
					
					SwingUtilities.invokeAndWait(createAndShow);
				}
			}
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
	}

	
	/**
	 * Invoked when an action occurs.
	 **/
	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		
		Thread t = new Thread() {
			public void run() {
				execute();
			}
		};
		t.start();
	}
}

