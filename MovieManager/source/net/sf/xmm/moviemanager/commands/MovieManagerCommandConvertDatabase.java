/**
 * @(#)MovieManagerCommandConvertDatabase.java
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

package net.sf.xmm.moviemanager.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.WindowConstants;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.database.DatabaseAccess;
import net.sf.xmm.moviemanager.database.DatabaseHSQL;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogDatabaseConverter;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.util.CustomFileFilter;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.log4j.Logger;


public class MovieManagerCommandConvertDatabase extends JPanel implements ActionListener{

	Logger log = Logger.getLogger(getClass());

	Database newDatabase;
	ListModel movieListModel;
	ArrayList<ModelEpisode> episodeList;
	boolean canceled = true;
	boolean done = false;
	boolean dbOpened = false;
	String filePath;
	JDialog dbConverter;
	
	enum DatabaseType {MSAccess, HSQL};
	DatabaseType newDatabaseType = DatabaseType.MSAccess;

	public Database createNewDatabase() {

		try {

			if (newDatabaseType == DatabaseType.HSQL) {
				/* Creates a new HSQL database... */
				newDatabase = new DatabaseHSQL(filePath);
				newDatabase.setUp();

				((DatabaseHSQL)newDatabase).createDatabaseTables();

				/* Adds extra info field names */
				ArrayList<String> columnNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);

				for (int i = 0; i < columnNames.size(); i++)
					newDatabase.addExtraInfoFieldName(columnNames.get(i));

				/* Adds lists columns */
				columnNames = MovieManager.getIt().getDatabase().getListsColumnNames();
				for (int i = 0; i < columnNames.size(); i++)
					newDatabase.addListsColumn(columnNames.get(i));

			}
			else {

				/* Creates a new MS Access database file... */
				File dataBaseFile = new File(filePath);

				if (!dataBaseFile.createNewFile()) {
					throw new Exception("Cannot create database file."); //$NON-NLS-1$
				}

				/* Copies the empty database file in the package to the new file... */
				byte[] data;
				InputStream inputStream;
				OutputStream outputStream;

				inputStream = new FileInputStream(FileUtil.getFile("config/Temp.mdb")); //$NON-NLS-1$
				outputStream = new FileOutputStream(dataBaseFile);
				data = new byte[inputStream.available()];

				while (inputStream.read(data) != -1)
					outputStream.write(data);

				outputStream.close();
				inputStream.close();

				newDatabase = new DatabaseAccess(filePath);
				newDatabase.setUp();

				/* Adds extra info field names */
				ArrayList<String> columnNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);
				for (int i = 0; i < columnNames.size(); i++)
					newDatabase.addExtraInfoFieldName(columnNames.get(i));

				/* Adds lists columns */
				columnNames = MovieManager.getIt().getDatabase().getListsColumnNames();
				for (int i = 0; i < columnNames.size(); i++)
					newDatabase.addListsColumn(columnNames.get(i));
			}

		} catch (Exception e) {
			log.error("", e); //$NON-NLS-1$
		}

		return newDatabase;
	}


	void createAndShowGUI() {

		/* Owner, title, modal=true */
		dbConverter = new JDialog(MovieManager.getDialog(), Localizer.get("MovieManagerCommandConvertDatabase.database-converter.title"), true); //$NON-NLS-1$
		dbConverter.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		final JComponent newContentPane = new DialogDatabaseConverter(newDatabase, movieListModel, episodeList, this);
		newContentPane.setOpaque(true);
		dbConverter.setContentPane(newContentPane);
		dbConverter.pack();
		dbConverter.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dbConverter.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

				if (canceled) {
					deleteNewDatabase();
					dbConverter.dispose();
				}		    
				else if (done){
					if (!dbOpened)
						finalizeDatabase();
					dbConverter.dispose();
				}
			}
		});

		/*Dispose on escape*/
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (canceled) {
					deleteNewDatabase();
					dbConverter.dispose();
				}
				else if (done){
					if (!dbOpened)
						finalizeDatabase();
					dbConverter.dispose();
				}
			}
		};

		dbConverter.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); //$NON-NLS-1$
		dbConverter.getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$

		MovieManager mm = MovieManager.getIt();

		setLocation((int) mm.getLocation().getX()+(mm.getWidth()-getWidth())/2,
				(int) mm.getLocation().getY()+(mm.getHeight()-getHeight())/2);

		dbConverter.setLocation((int)mm.getLocation().getX()+(mm.getWidth()- dbConverter.getWidth())/2,
				(int)mm.getLocation().getY()+(mm.getHeight()- dbConverter.getHeight())/2);
		GUIUtil.show(dbConverter, true);
	}

	void finalizeDatabase() {
		if (newDatabase instanceof DatabaseHSQL)
			((DatabaseHSQL) newDatabase).shutDownDatabase("SHUTDOWN COMPACT;"); //$NON-NLS-1$
		newDatabase.finalizeDatabase();
	}

	public void deleteNewDatabase() {
		newDatabase.deleteDatabase();
	}

	public void loadDatabase() throws Exception {
		MovieManager.getDatabaseHandler().setDatabase(newDatabase, true);
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public void setDbOpened(boolean dbOpened) {
		this.dbOpened = dbOpened;
	}

	public void dispose() {
		GUIUtil.show(dbConverter, false);
	}

	protected String getFilePath() {

		/* Opens the Open dialog... */
		ExtendedFileChooser fileChooser = new ExtendedFileChooser();
		newDatabaseType = DatabaseType.MSAccess;

		if (SysUtil.isWindows()) {
			if (MovieManager.getIt().getDatabase() instanceof DatabaseAccess) {
				fileChooser.setFileFilter(new CustomFileFilter(new String[]{"mdb", "accdb"},new String("MS Access Database File (*.mdb, *.accdb)"))); //$NON-NLS-1$ //$NON-NLS-2$
				fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"properties", "script"},new String("HSQL Database Files (*.properties, *.script)"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}	
			else {
				fileChooser.setFileFilter(new CustomFileFilter(new String[]{"properties", "script"},new String("HSQL Database Files (*.properties, *.script)"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"mdb", "accdb"},new String("MS Access Database File (*.mdb, *.accdb)"))); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else 
			fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"properties", "script"},new String("HSQL Database Files (*.properties, *.script)"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		if (MovieManager.getConfig().getLastDatabaseDir() != null)
			fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastDatabaseDir());

		fileChooser.setDialogTitle(Localizer.get("MovieManagerCommandConvertDatabase.filechooser.title.save-new-database")); 
		fileChooser.setApproveButtonText(Localizer.get("MovieManagerCommandConvertDatabase.filechooser.approve-button.text")); 
		fileChooser.setApproveButtonToolTipText(Localizer.get("MovieManagerCommandConvertDatabase.filechooser.approve-button.tooltip"));
		fileChooser.setDialogType(1);
		fileChooser.setAcceptAllFileFilterUsed(false);

		int returnVal = fileChooser.showDialog(MovieManager.getDialog(), Localizer.get("MovieManagerCommandConvertDatabase.filechooser.approve-button.text"));

		if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
			/* Gets the path... */
			String absolutePath = fileChooser.getSelectedFile().getAbsolutePath();

			if (fileChooser.getFileFilter().getDescription().equals("HSQL Database Files (*.properties, *.script)")) {//$NON-NLS-1$
				newDatabaseType = DatabaseType.HSQL;

				if (absolutePath.endsWith(".properties")) //$NON-NLS-1$
					absolutePath = absolutePath.substring(0, absolutePath.length()-11);
				else if (absolutePath.endsWith(".script")) //$NON-NLS-1$
					absolutePath = absolutePath.substring(0, absolutePath.length()-7);
			}
			else {
				if (!absolutePath.endsWith(".mdb") && !absolutePath.endsWith(".accdb")) //$NON-NLS-1$
					absolutePath += ".mdb";
			}
			return absolutePath;
		}
		return null;
	}


	protected void execute() {

		//    	 If any notes have been changed, they will be saved before converting database.
		MovieManagerCommandSaveChangedNotes.execute();

		if (!SysUtil.isWindows() && !MovieManager.getIt().getDatabase().getDatabaseType().equals("MySQL")) {
			DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("MovieManagerCommandConvertDatabase.alert.not-linux-hsql.title"), Localizer.get("MovieManagerCommandConvertDatabase.alert.not-linux-hsql.message"));
			GUIUtil.showAndWait(alert, true);
		}
		else {
			ArrayList<ModelMovie> list = MovieManager.getIt().getDatabase().getMoviesList(); //$NON-NLS-1$
			movieListModel = GUIUtil.toDefaultListModel(list);
			episodeList = MovieManager.getIt().getDatabase().getEpisodeList(); //$NON-NLS-1$

			int listModelSize = movieListModel.getSize();

			if (listModelSize == 0) {
				DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("MovieManagerCommandConvertDatabase.alert.empty-database.title"), Localizer.get("MovieManagerCommandConvertDatabase.alert.empty-database.message"));
				GUIUtil.showAndWait(alert, true);
			}
			else {
				filePath = getFilePath();

				if (filePath != null) {
					if (createNewDatabase() == null) {
						DialogAlert alert = new DialogAlert(MovieManager.getDialog(), "Error", "Failed to create new database");
						GUIUtil.showAndWait(alert, true);
						return;
					}
					createAndShowGUI();
				}	
			}
		}
	}

	/**
	 * Invoked when an action occurs.
	 **/
	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		execute();
	}
}



