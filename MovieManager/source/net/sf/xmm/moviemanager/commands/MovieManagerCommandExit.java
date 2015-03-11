/**
 * @(#)MovieManagerCommandExit.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.SysUtil;

public class MovieManagerCommandExit implements ActionListener {

	static Logger log = Logger.getLogger(MovieManagerCommandExit.class);
	
    /**
     * Executes the command.
     **/
	public static void execute() {

		try {
			shutDown();
		} catch (Exception e) {
			log.debug("Exception:" + e.getMessage(), e);
			
			
		} finally {
			MovieManager.exit();
		}
	}

	
	public static void shutDown() {

		// If any notes have been changed, they will be saved before exiting
		MovieManagerCommandSaveChangedNotes.execute();

		log.debug("Shutting down...");

		// Gets config from toolbar
		MovieManager.getConfig().setDisplayPlayButton(MovieManager.getDialog().getPLayButtonVisible());
		MovieManager.getConfig().setDisplayPrintButton(MovieManager.getDialog().getPrintButtonVisible());

		try {
			// Saving config file
			MovieManager.getConfig().saveConfig();
		} catch (IOException io) {
			DialogAlert alert = new DialogAlert(MovieManager.getDialog(), "Config error", "<html>Error occured when saving config file:<br>"+ io.getMessage() +" </html>", true);
			GUIUtil.showAndWait(alert, true);	
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);					
		}

		/* Finalizes the main frame... */
		try {
			MovieManager.getDialog().finalize();
		} catch (Exception e) {
			log.debug("MovieManager.getDialog().finalize() produced errors.");
		}

		Database db = MovieManager.getIt().getDatabase();
		String type = "";

		long time = System.currentTimeMillis();

		if (db != null) {
			/* Finalizing database... */
			db.finalizeDatabase();

			type = db.getDatabaseType();
		}

		log.debug("Finalized " + type + " database in " + (System.currentTimeMillis() - time) + " ms.");

		/* Writes the date. */
		log.debug("Log End: "+new Date(System.currentTimeMillis()) + SysUtil.getLineSeparator());
	}

	/**
	 * Invoked when an action occurs.
	 **/
	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: " + event.getActionCommand());
		execute();
	}
}
