/**
 * @(#)MovieManagerCommandAddMultipleMoviesByFile.java 1.0 24.01.06 (dd.mm.yy)
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
 * 7Contact: bro3@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportMoviesByFile;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogDatabaseImporterExporter;
import net.sf.xmm.moviemanager.gui.DialogIMDB;
import net.sf.xmm.moviemanager.gui.DialogIMDbMultiAdd;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelFileImportSettings;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.StringUtil;

import org.apache.log4j.Logger;

public class MovieManagerCommandAddMultipleMoviesByFile extends MovieManagerCommandAddMultipleMovies implements ActionListener {

	Logger log = Logger.getLogger(getClass());
	
		
	DialogAddMultipleMovies damm;

	/**
	 * Executes the command.
	 * Checks all the options before starting to process the list of movies
	 * found in the directory
	 **/
	protected void execute() {

//		If any notes have been changed, they will be saved before changing list
		MovieManagerCommandSaveChangedNotes.execute();
		
		cancelAll = false;

		damm = new DialogAddMultipleMovies();

		damm.buttonAddMovies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				damm.executeSave();
				if (damm.validateAddList())
					startAddMovies();
			}
		});

		GUIUtil.showAndWait(damm, true);
		
		damm.initializeTree();
	}

	void startAddMovies() {
		ModelImportExportSettings importSettings = getSettings();
		MovieManagerCommandImportMoviesByFile importByFile = new MovieManagerCommandImportMoviesByFile(importSettings);

		final JDialog dialog = new DialogDatabaseImporterExporter(damm, importByFile, importSettings, true);
		importSettings.setParent(dialog);
		GUIUtil.show(dialog, true);
	}
	

	ModelImportExportSettings getSettings() {

		ModelFileImportSettings importSettings = new ModelFileImportSettings();
				
		ArrayList <DialogAddMultipleMovies.Files> fileList = damm.getMoviesToAdd();

		if (fileList == null) {
			return null;
		}
		
		importSettings.fileList = fileList;
		importSettings.multiAddSelectOption = damm.getMultiAddSelectOption();
		importSettings.enableExludeParantheses = damm.enableExludeParantheses.isSelected();
		importSettings.enableExludeCDNotations = damm.enableExludeCDNotation.isSelected();
		importSettings.enableExludeIntegers = damm.enableExludeIntegers.isSelected();
		importSettings.enableExludeYear = damm.enableExludeYear.isSelected();
		importSettings.enableUseFolderName = damm.enableUseFolderName.isSelected();
		importSettings.enableSearchNfoForImdb = damm.enableSearchNfoForImdb.isSelected();
		importSettings.enableExludeUserdefinedInfo = damm.getEnableExludeUserdefinedInfo();
		importSettings.enableExludeAllAfterMatchOnUserDefinedInfo = damm.enableExludeAllAfterMatchOnUserDefinedInfo.isSelected();
		importSettings.enableUseParentFolderIfCD = damm.enableUseParentFolderIfCD.isSelected();
			
		if (damm.enableAddMoviesToList != null && damm.enableAddMoviesToList.isSelected()) {
			importSettings.addMovieToList = true;
			importSettings.addToThisList = (String) damm.listChooser.getSelectedItem();
		}
		else
			importSettings.addMovieToList = false;
		
		
		importSettings.existingMediaFiles = damm.getExistingMediaFiles();
		importSettings.existingMediaFileNames = damm.getExistingMediaFileNames();
		
		return importSettings;
	}

	
	public String searchNfoForImdb(String _path) {
		try {
			if (_path != null && !_path.equals("")) {
				String tmp;
				BufferedReader br;
				File path = new File(_path);
				File files[] = path.listFiles();
				
				for (int i = 0; i < files.length; i++) {
					// Cycle through all entries in the directory
					String filename = files[i].getName().toLowerCase();
					if (files[i].isFile() && files[i].length() < 40000 && (filename.endsWith(".txt") || filename.endsWith(".nfo") || filename.endsWith(".url"))) {
						// Only process files < 40000 Bytes with with .txt or .nfo suffix and no directories
												
						br = new BufferedReader(new FileReader(files[i]));
						tmp = br.readLine();
						while (tmp != null) {
							if (tmp.contains("imdb.com/title/tt") || tmp.contains("imdb.de/title/tt")) {
								// If File contains an imdb url than get it out
								if (tmp.contains("imdb.com/title/tt"))
									tmp = tmp.substring(tmp.indexOf("imdb.com/title/tt") + 17);
								else
									tmp = tmp.substring(tmp.indexOf("imdb.de/title/tt") + 16);

								// Search for a 6 to 8 digits long number (normally 7 digits is used in the url)
								Pattern p = Pattern.compile("[\\d]{6,8}");
								Matcher m = p.matcher(tmp);

								if (m.find()) {
									br.close();
									return m.group();
								}
							}
							tmp = br.readLine();
						}
						br.close();
					}
				}
			}
		}
		catch (FileNotFoundException e) {
			log.debug("No nfo/txt file found for parsing");
		}
		catch (IOException e) {
			log.debug("I/O error while processing nfo/txt files");
		}

		return null;
	}
	
	
	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: "+ event.getActionCommand());
		execute();
	}
	
}

