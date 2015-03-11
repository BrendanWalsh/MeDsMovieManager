/**
 * @(#)DatabaseImporterExporter.java 1.0 26.09.06 (dd.mm.yy)
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

import java.awt.Dialog;
import java.util.ArrayList;

import javax.swing.JDialog;

import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler.ImportExportReturn;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.swing.util.SwingWorker;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;



public class DatabaseImporterExporter {

	Logger log = Logger.getLogger(getClass());

	private int lengthOfTask = 0;
	private int current = -1;
	private boolean done = false;
	private boolean stopped = false;
	private ArrayList<String> transferred = new ArrayList<String>();
	private ModelImportExportSettings importSettings;
	private Dialog parent;

	public MovieManagerCommandImportExportHandler handler;

	public DatabaseImporterExporter(Dialog parent, MovieManagerCommandImportExportHandler handler, ModelImportExportSettings importSettings) {
		this.importSettings = importSettings;
		this.parent = parent;
		this.handler = handler;
	}

	public void go() {
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				current = -1;
				done = false;
				stopped = false;

				ImportExportTask importer = null;

				try {
					importer =  new ImportExportTask(importSettings);
				}
				catch (Exception e) {
					log.warn("Exception:" + e.getMessage(), e);   
				}
				return importer;
			}
		};
		worker.start();
	}

	public int getLengthOfTask() {
		return lengthOfTask;
	}


	public int getCurrent() {
		return current;
	}

	/* Stops the importing process */
	public void stop() {
		stopped = true;
	}

	public boolean isStopped() {
		return stopped;
	}
	
	public boolean isDone() {
		return done;
	}

	public synchronized boolean hasMoreTransferred() {
		return transferred.size() > 0;
	}
	
	public synchronized String getNextTransferred() {
		return transferred.remove(0);
	}
	
	synchronized void addNewTransferred(String val) {
		transferred.add(val);
	}
	

	/**
	 * The actual database import task.
	 * This runs in a SwingWorker thread.
	 */
	class ImportExportTask {

		int extraInfoFieldsCount;

		ImportExportTask(ModelImportExportSettings importExportSettings) {

			try {

				/* Setting the priority of the thread to 4 to give the GUI room to update more often */
				Thread.currentThread().setPriority(3);

				try {
					handler.retrieveMovieList();
					lengthOfTask = handler.getMovieListSize();

				} catch (Exception e) {
					log.error("Exception:" + e.getMessage(), e);
					JDialog alert = new DialogAlert(parent, "Error", e.getMessage());
					GUIUtil.showAndWait(alert, true);
					return;
				}

				String title = "";

				for (int i = 0; i < lengthOfTask; i++) {

					title = handler.getTitle(i);
					
					if (title != null && !title.equals("")) {
												
						if (handler.isCancelled()) {
							addNewTransferred("Skipped " + title);
							continue;
						}
						
						if (isStopped()) {
							addNewTransferred((handler.isImporter() ? "Import" : "Export") + " process stopped");
							break;
						}
						
						if (handler.isAborted()) {
							addNewTransferred((handler.isImporter() ? "Import" : "Export") + " process aborted");
							break;
						}
												
						ImportExportReturn ret = handler.addMovie(i);
												
						if (ret == ImportExportReturn.error)
							addNewTransferred("Failed to " + (handler.isImporter() ? "import " : "export ") + title);
						else if (ret == ImportExportReturn.cancelled)
							addNewTransferred("Skipped " + title);
						else if (!title.equals(""))
							addNewTransferred(title);

						current++;
					}
					else {
						addNewTransferred("Empty entry");
						current++;
					}
					handler.resetStatus();
				}
				done = true;
				handler.done();
				
			} catch(Exception e) {
				log.error("Exception:" + e.getMessage(), e);
			} 
		}
	}
}

