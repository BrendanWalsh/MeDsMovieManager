/**
 * @(#)Databasehandler.java
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


package net.sf.xmm.moviemanager;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.database.DatabaseAccess;
import net.sf.xmm.moviemanager.database.DatabaseHSQL;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogAlertCheckBox;
import net.sf.xmm.moviemanager.gui.DialogDatabase;
import net.sf.xmm.moviemanager.gui.DialogQuestion;
import net.sf.xmm.moviemanager.models.ModelDatabaseSearch;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.progressbar.ProgressBean;
import net.sf.xmm.moviemanager.swing.progressbar.ProgressBeanImpl;
import net.sf.xmm.moviemanager.swing.progressbar.SimpleProgressBar;
import net.sf.xmm.moviemanager.swing.util.SwingWorker;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.events.NewDatabaseLoadedHandler;
import net.sf.xmm.moviemanager.util.events.NewMovieListLoadedHandler;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerLoginHandler;

public class DatabaseHandler {

	static Logger log = Logger.getLogger(DatabaseHandler.class);

	public static NewDatabaseLoadedHandler newDbHandler = new NewDatabaseLoadedHandler();
	public static NewMovieListLoadedHandler newMovieListLoadedHandler = new NewMovieListLoadedHandler();

	public NewDatabaseLoadedHandler getNewDatabaseLoadedHandler() {
		return newDbHandler;
	}

	public NewMovieListLoadedHandler getNewMovieListLoadedHandler() {
		return newMovieListLoadedHandler;
	}

	private Database database;

	/* Stores the active additional fields */
	private int [] activeAdditionalInfoFields;

	// used in allowDatabaseUpdate method.
	boolean allowDatabaseUpdateTmp = false;

	/**
	 * Returns the current database.
	 *
	 * @return The current database.
	 **/
	synchronized public Database getDatabase() {
		return database;
	}

	
	public int [] getActiveAdditionalInfoFields() {
		return activeAdditionalInfoFields;
	}

	public void setActiveAdditionalInfoFields(int [] activeAdditionalInfoFields) {
		this.activeAdditionalInfoFields = activeAdditionalInfoFields;
	}

	public void saveActiveAdditionalInfoFields(int [] newActiveFields) {
		this.activeAdditionalInfoFields = newActiveFields;
		MovieManager.getIt().getDatabase().setActiveAdditionalInfoFields(newActiveFields);
	}



	public boolean setDatabase(Database _database, boolean cancelRelativePaths) throws Exception {
		return setDatabase(_database, null, cancelRelativePaths);
	}

	/**
	 * * Sets the current database.
	 *
	 * @param The current database.
	 * @throws Exception 
	 **/
	public boolean setDatabase(Database _database, ProgressBean progressBean, boolean cancelRelativePaths) throws Exception {

		MovieManagerConfig config = MovieManager.getConfig();

		if (_database != null) {

			if (_database.isMySQL())
				cancelRelativePaths = true;

			boolean databaseUpdateAllowed = false;

			//  If database loading aborted by user
			if (progressBean != null && progressBean.getCancelled()) {
				return false;
			}

			/* Check if script file needs update (v2.1) */
			if (_database.isHSQL()) {
				boolean scriptOutdated = ((DatabaseHSQL) _database).isScriptOutOfDate();
				boolean driverOld = ((DatabaseHSQL) _database).isDriverOld();

				if (scriptOutdated || driverOld) {

					log.error("Updates must be done to the database");
					
					if (!allowDatabaseUpdate(_database.getPath())) {
						return false;
					}
					
					if (driverOld) {
						log.error("HSQLDb driver is old");
						if (!makeDatabaseBackup(_database, "Update_from_HSQL_1.7_to_1.8")) {
							log.error("Failed to create database backup!");
							showDatabaseUpdateMessage("Backup failed"); //$NON-NLS-1$
							return false;
						}
					}
					else if (!makeDatabaseBackup(_database)) {
						showDatabaseUpdateMessage("Backup failed"); //$NON-NLS-1$
						return false;
					}

					/* updates the script if audio channel type is INTEGER (HSQLDB)*/
					if (scriptOutdated && !((DatabaseHSQL) _database).updateScriptFile()) {
						showDatabaseUpdateMessage("Script update error"); //$NON-NLS-1$
						return false;
					}
					databaseUpdateAllowed = true;
				}
			}

			//  If database loading aborted by user
			if (progressBean != null && progressBean.getCancelled()) {
				return false;
			}

			if (!_database.isSetUp()) {
				boolean retry;
				do {

					retry = false;
					if (!_database.setUp()) {
						String message = _database.getErrorMessage();

						boolean ret = DialogDatabase.showDatabaseMessage(MovieManager.getDialog(), _database, null);

						// Check if its database already in use
						if (ret && message.indexOf("The database is already in use") != -1) {
							retry = true;
						}
						else
							return false;
					}

				} while (retry);

				if (progressBean != null && !progressBean.getCancelled())
					MovieManager.getDialog().resetTreeModel();

			}

			//  If database loading aborted by user
			if (progressBean != null && progressBean.getCancelled()) {
				return false;
			}

			/* If it went ok. */
			if (_database.isInitialized()) {

				/* If database is old, it's updated */
				if (_database.isDatabaseOld()) {

					if (!databaseUpdateAllowed) {
						if (!allowDatabaseUpdate(_database.getPath())) {
							return false;
						}
						if (!makeDatabaseBackup(_database)) {
							log.error("Failed to create database backup!");
							showDatabaseUpdateMessage("Backup failed"); //$NON-NLS-1$
							return false;
						}
					}

					if (_database.makeDatabaseUpToDate() == 1)
						showDatabaseUpdateMessage("Success"); //$NON-NLS-1$
					else {
						String message = _database.getErrorMessage();

						showDatabaseUpdateMessage(message);
						return false;
					}
				}

				//  If database loading aborted by user
				if (progressBean != null && progressBean.getCancelled()) {
					return false;
				}

				setActiveAdditionalInfoFields(_database.getActiveAdditionalInfoFields());

				/* Error occurred */
				if (_database.getFatalError()) {

					if (!_database.getErrorMessage().equals("")) { //$NON-NLS-1$
						DialogDatabase.showDatabaseMessage(MovieManager.getDialog(), _database, null);

						if (progressBean != null && !progressBean.getCancelled())
							MovieManager.getDialog().resetTreeModel();
					}
					return false;
				}

				log.info("Loads the movies list"); //$NON-NLS-1$

				ModelDatabaseSearch options = getFilterOptions(_database);

				// Verifies that all the lists are available
				if (config.getLoadLastUsedListAtStartup()) {

					ArrayList<String> lists = config.getCurrentLists();

					if (lists == null) {
						options.setListOption(0);
						config.setCurrentLists(new ArrayList<String>());
						config.setShowUnlistedEntries(false);
					} else {

						boolean changed = false;

						for (int i = 0; i < lists.size(); i++) {

							if (!_database.listColumnExist((String) lists.get(i))) { //$NON-NLS-1$
								lists.remove(i);
								changed = true;
							}
						}

						if (changed) {
							options = getFilterOptions(_database);
						}
					}
				}
				else {
					options.setListOption(0);
					config.setCurrentLists(new ArrayList<String>());
					config.setShowUnlistedEntries(false);
				}

				if (_database.getDatabaseType().equals("MySQL"))
					options.getFullGeneralInfo = false;
				else
					options.getFullGeneralInfo = true;

				options.setFilterString(""); //$NON-NLS-1$
				options.setOrderCategory("Title"); //$NON-NLS-1$
				options.setSeen(0);
				options.setRatingOption(0);
				options.setDateOption(0);
				options.setSearchAlias(config.getSearchAlias());

				//  If database loading aborted by user  
				if (progressBean != null && progressBean.getCancelled()) {
					return false;
				}

				ArrayList<ModelMovie> moviesList = _database.getMoviesList(options);
				ArrayList<ModelEpisode> episodesList = _database.getEpisodeList(); //$NON-NLS-1$
				DefaultTreeModel treeModel = MovieManager.getDialog().createTreeModel(moviesList, episodesList);

				if (cancelRelativePaths && !MovieManager.isApplet()) {

					if (_database.isMySQL()) {
						if (config.getUseRelativeCoversPath() == 1)
							config.setUseRelativeCoversPath(0);

						if (config.getUseRelativeQueriesPath() == 1)
							config.setUseRelativeQueriesPath(0);

						config.setUseRelativeDatabasePath(0);
					}
					else {
						if (!new File(config.getCoversPath(_database)).isDirectory() && new File(config.getCoversFolder(_database)).isDirectory()) {
							config.setUseRelativeCoversPath(0);
						}

						if (!new File(config.getQueriesPath(_database)).isDirectory() && new File(config.getQueriesFolder(_database)).isDirectory()) {
							config.setUseRelativeQueriesPath(0);
						}

						if (_database.getPath().indexOf(SysUtil.getUserDir()) == -1) {
							config.setUseRelativeDatabasePath(0);
						}
					}
				}

				//  If database loading aborted by user
				if (progressBean != null && progressBean.getCancelled()) {
					return false;
				}

				/* Must be set here and not earlier. 
	                 If the database is set at the top and the  method returns because of an error after the database is set, 
	                 a faulty database will then be stored and used */
				database = _database;

				newDbHandler.newDatabaseLoaded(this);

				/* Loads the movies list. */
				MovieManager.getDialog().setTreeModel(treeModel, moviesList, episodesList);

				/* Updates the entries Label */
				MovieManager.getDialog().setAndShowEntries();

				MovieManager.getDialog().loadMenuLists(database);

				/* Makes database components visible. */
				MovieManager.getDialog().getAppMenuBar().setDatabaseComponentsEnable(true);

			} else {
				/* Makes database components invisible. */
				MovieManager.getDialog().getAppMenuBar().setDatabaseComponentsEnable(false);
				DialogDatabase.showDatabaseMessage(MovieManager.getDialog(), _database, null);
			}
		}
		else
			database = null;

		if (_database != null) {

			Runnable selectMovie = new Runnable() {
				public void run() {

					/* Selects the first movie in the list and loads its info. */
					if (MovieManager.getDialog().getMoviesList().getModel().getChildCount(MovieManager.getDialog().getMoviesList().getModel().getRoot()) > 0)
						MovieManager.getDialog().getMoviesList().setSelectionRow(0); 

					MovieManagerCommandSelect.execute();
				}};
				GUIUtil.invokeLater(selectMovie);
		}

		return _database != null;
	}

	protected boolean allowDatabaseUpdate(final String databasePath) {

		allowDatabaseUpdateTmp = false;

		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					DialogQuestion question = new DialogQuestion("Old Database", "<html>This version of MeD's Movie Manager requires your old database:<br> ("+databasePath+") to be updated.<br>"+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"Perform update now? (A backup will be made)</html>"); //$NON-NLS-1$
					GUIUtil.showAndWait(question, true);

					if (question.getAnswer()) {
						allowDatabaseUpdateTmp = true;
					}
					else {
						DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("DialogMovieManager.update-necessary"), Localizer.get("DialogMovieManager.update-necessary-message")); //$NON-NLS-1$ //$NON-NLS-2$
						GUIUtil.showAndWait(alert, true);
					}

				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return allowDatabaseUpdateTmp;
	}

	protected void showDatabaseUpdateMessage(final String result) {

		try {
			GUIUtil.invokeAndWait(new Runnable() {

				public void run() {
					if (result.equals("Success")) { //$NON-NLS-1$
						DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("DialogMovieManager.operation-successfull"), Localizer.get("DialogMovieManager.operation-successfullMessage")); //$NON-NLS-1$ //$NON-NLS-2$
						GUIUtil.showAndWait(alert, true);
					}
					else if (result.equals("Database update error")) { //$NON-NLS-1$
						DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("DialogMovieManager.database-update-failed"), Localizer.get("DialogMovieManager.database-update-failed-message")); //$NON-NLS-1$ //$NON-NLS-2$
						GUIUtil.showAndWait(alert, true);
					}
					else if (result.equals("Script update error")) { //$NON-NLS-1$
						DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("DialogMovieManager.script-update-failed"), Localizer.get("DialogMovieManager.script-update-failed-message")); //$NON-NLS-1$ //$NON-NLS-2$
						GUIUtil.showAndWait(alert, true);
					}
					else if (result.equals("Backup failed")) { //$NON-NLS-1$
						DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("DialogMovieManager.backup-failed"), Localizer.get("DialogMovieManager.backup-failed-message")); //$NON-NLS-1$ //$NON-NLS-2$
						GUIUtil.showAndWait(alert, true);
					}
					else {
						DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("DialogMovieManager.update-failed"), result); //$NON-NLS-1$
						GUIUtil.showAndWait(alert, true);
					}
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void processDatabaseError(Database db) {

		final MovieManagerConfig config = MovieManager.getConfig();

		try {
			String error = db.getErrorMessage();

			log.error("Database error:" + error);

			if (error.equals("Server shutdown in progress")) { //$NON-NLS-1$

				MovieManager.getDialog().getAppMenuBar().setDatabaseComponentsEnable(false);

				Runnable r = new Runnable() {
					public void run() {
						DialogAlert alert = new DialogAlert(MovieManager.getDialog(), "Server shutdown in progress", "<html>MySQL server is shutting down.</html>"); //$NON-NLS-1$
						GUIUtil.showAndWait(alert, true);
					}
				};
				GUIUtil.invokeAndWait(r);
				database = null;
			}
			else if (error.equals("Connection reset")) { //$NON-NLS-1$

				MovieManager.getDialog().getAppMenuBar().setDatabaseComponentsEnable(false);

				if (database != null) {
					Runnable r = new Runnable() {
						public void run() {
							DialogQuestion question = new DialogQuestion(Localizer.get("DialogMovieManager.connection-reset"), "<html>The connection to the MySQL server has been reset.<br>"+ //$NON-NLS-1$ //$NON-NLS-2$
							"Reconnect now?</html>"); //$NON-NLS-1$
							GUIUtil.showAndWait(question, true);

							if (question.getAnswer()) {
								try {
									setDatabase(new DatabaseMySQL(database.getPath(), config.getMySQLSocketTimeoutEnabled()), false);
								} catch (Exception e) {
									log.error("Exception:" + e.getMessage(), e);
								}
							}
						}
					};
					GUIUtil.invokeAndWait(r);
				}
			}
			else if (error.equals("Connection closed")) { //$NON-NLS-1$

				MovieManager.getDialog().getAppMenuBar().setDatabaseComponentsEnable(false);

				if (database != null) {
					Runnable r = new Runnable() {
						public void run() {
							DialogQuestion question = new DialogQuestion("Connection closed", "<html>The connection to the MySQL server has closed.<br>" +
							"Reconnect now?</html>");
							GUIUtil.showAndWait(question, true);

							if (question.getAnswer()) {
								try {
									setDatabase(new DatabaseMySQL(database.getPath(), config.getMySQLSocketTimeoutEnabled()), false);
								} catch (Exception e) {
									log.error("Exception:" + e.getMessage(), e);
								}
							}
						}
					};
					GUIUtil.invokeAndWait(r);
				}
			}	
			else if (error.equals("Connection refused: connect")) {
				//Should be handled by DialogDatabase
			}
			else if (error.equals("Socket Write Error")) {

				MovieManager.getDialog().getAppMenuBar().setDatabaseComponentsEnable(false);

				if (database != null) {
					Runnable r = new Runnable() {
						public void run() {
							DialogQuestion question = new DialogQuestion("Socket Write Error", "<html>Software caused connection abort due to a Socket Write Error.<br>" +
							"Reconnect to server now?</html>");
							GUIUtil.showAndWait(question, true);

							if (question.getAnswer()) {
								try {
									setDatabase(new DatabaseMySQL(database.getPath(), config.getMySQLSocketTimeoutEnabled()), false);
								} catch (Exception e) {
									log.error("Exception:" + e.getMessage(), e);
								}
							}
						}
					};
					GUIUtil.invokeAndWait(r);
				}
			}                  
			else if (error.equals("UnknownHostException")) {
				Runnable r = new Runnable() {
					public void run() {
						DialogAlert alert = new DialogAlert(MovieManager.getDialog(), "Unknown Host", "<html>The host could not be found.</html>"); //$NON-NLS-1$ //$NON-NLS-2$
						GUIUtil.showAndWait(alert, true);
					}
				};
				GUIUtil.invokeAndWait(r);
			}   
			else if (error.equals("Communications link failure")) {

				MovieManager.getDialog().getAppMenuBar().setDatabaseComponentsEnable(false);

				if (database != null) {

					Runnable r = new Runnable() {
						public void run() {
							DialogQuestion question = new DialogQuestion("Communications link failure", "<html>Client failed to connect to MySQL server.<br>" +
							"Reconnect to server now?</html>");
							GUIUtil.showAndWait(question, true);

							if (question.getAnswer()) {
								try {
									setDatabase(new DatabaseMySQL(database.getPath(), config.getMySQLSocketTimeoutEnabled()), false);
								} catch (Exception e) {
									log.error("Exception:" + e.getMessage(), e);
								}
							}
						}        			
					};

					GUIUtil.invokeAndWait(r);
				}
			}        	
			else if (error.equals("MySQL server is out of space")) { //$NON-NLS-1$

				Runnable r = new Runnable() {
					public void run() {
						DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("DialogMovieManager.mysql-out-of-space"), Localizer.get("DialogMovieManager.mysql-out-of-space-message")); //$NON-NLS-1$ //$NON-NLS-2$
						GUIUtil.showAndWait(alert, true);
					}
				};
				GUIUtil.invokeAndWait(r);
			}
			else
				DialogDatabase.showDatabaseMessage(MovieManager.getDialog(), db, "");

		} catch (InterruptedException e) {
			log.error("Exception:" + e.getMessage(), e);
		} catch (InvocationTargetException e) {
			log.error("Exception:" + e.getMessage(), e);
		}
	}

	protected void loadDatabase() {
		loadDatabase(false);
	}

	protected void loadDatabase(final boolean perfromBackup) {

		final MovieManagerConfig config = MovieManager.getConfig();

		if (!config.getLoadDatabaseOnStartup())
			return;

		final ProgressBean worker = new ProgressBeanImpl() {

			public void run() {

				try {

					//Thread.currentThread().setPriority(8);

					// Gets the database path read from the config file.
					String databasePath = config.getDatabasePath(false);
					String type = ""; //$NON-NLS-1$

					if (!MovieManager.getConfig().getInternalConfig().getSensitivePrintMode())
						log.debug("databasePath:"+ databasePath); //$NON-NLS-1$

					if (databasePath == null || databasePath.equals("null")) { //$NON-NLS-1$
						if (listener != null)
							listener.propertyChange(new PropertyChangeEvent(this, "value", null, null));
						return;
					}

					// Checking if there is a login feature enabled
					MovieManagerLoginHandler loginHandler = MovieManager.getConfig().getLoginHandler();

					if (loginHandler != null) {

						listener.propertyChange(new PropertyChangeEvent(this, "value", null, "Verifying username and password"));
						databasePath = loginHandler.handleLogin(databasePath);

						if (databasePath == null) {
							log.warn("Login failed. Database loading aborted");

							if (listener != null)
								listener.propertyChange(new PropertyChangeEvent(this, "value", null, null));
							return;
						}

					}

					log.debug("Start loading database."); //$NON-NLS-1$

					if (!databasePath.equals("")) { //$NON-NLS-1$

						/* If not, no database type specified */
						if (databasePath.indexOf(">") != -1) { //$NON-NLS-1$
							type = databasePath.substring(0, databasePath.indexOf(">")); //$NON-NLS-1$
							databasePath = databasePath.substring(databasePath.indexOf(">")+1, databasePath.length()); //$NON-NLS-1$
						}
						else {
							if (databasePath.endsWith(".mdb") || databasePath.endsWith(".accdb")) //$NON-NLS-1$
								type = "MSAccess"; //$NON-NLS-1$
							else if (new File(databasePath+".properties").exists() && new File(databasePath+".script").exists()) //$NON-NLS-1$ //$NON-NLS-2$
								type = "HSQL"; //$NON-NLS-1$
						}
						//config.setDatabasePath(databasePath);
					}
					else {
						log.debug("database path is empty"); //$NON-NLS-1$
						if (listener != null)
							listener.propertyChange(new PropertyChangeEvent(this, "value", null, null));
						return;
					}

					if (type.equals("MySQL")) { //$NON-NLS-1$
						if (listener != null)
							this.listener.propertyChange(new PropertyChangeEvent(this, "value", null, Localizer.get("DialogMovieManager.progress.connecting-to-database")));
					}
					else {
						if (listener != null)
							listener.propertyChange(new PropertyChangeEvent(this, "value", null, Localizer.get("DialogMovieManager.progress.creating-connection")));
					}

					if (!MovieManager.getConfig().getInternalConfig().getSensitivePrintMode())
						log.info("Loading " +type+ ":" + databasePath); //$NON-NLS-1$ //$NON-NLS-2$

					/* Database path relative to program location */
					if (config.getUseRelativeDatabasePath() == 2)
						databasePath = SysUtil.getUserDir() + databasePath;

					Database db = null;

					//	      			If database loading aborted by user
					if (getCancelled()) {
						return;
					}


					try {

						if (type.equals("MSAccess")) { //$NON-NLS-1$
							if (new File(databasePath).exists()) {
								log.debug("Loading Access database"); //$NON-NLS-1$
								db = new DatabaseAccess(databasePath);
							}
							else
								log.debug("Access database does not exist"); //$NON-NLS-1$
						}
						else if (type.equals("HSQL")) { //$NON-NLS-1$
							if (new File(databasePath+".properties").exists() && new File(databasePath+".script").exists()) { //$NON-NLS-1$ //$NON-NLS-2$
								log.debug("Loading HSQL database"); //$NON-NLS-1$
								db = new DatabaseHSQL(databasePath);
							}
							else
								log.debug("HSQL database does not exist"); //$NON-NLS-1$
						}
						else if (type.equals("MySQL")) { //$NON-NLS-1$
							log.debug("Loading MySQL database"); //$NON-NLS-1$
							db = new DatabaseMySQL(databasePath, config.getMySQLSocketTimeoutEnabled());
						}

					} catch (Exception e) {
						log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
					}

					// If database loading aborted by user
					if (getCancelled()) {
						return;
					}

					if (db != null) {


						Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

						long time = System.currentTimeMillis();

						if (listener != null)
							listener.propertyChange(new PropertyChangeEvent(this, "value", null, Localizer.get("DialogMovieManager.progress.retrieving-movie-list")));

						// If database loading aborted by user
						if (getCancelled()) {
							return;
						}

						if (setDatabase(db, this, false)) {
							log.debug("Database loaded in:" + (System.currentTimeMillis() - time) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
						} 
						else
							log.debug("Failed to load database"); //$NON-NLS-1$
					}

					if (listener != null)
						listener.propertyChange(new PropertyChangeEvent(this, "value", null, null));


					// Perform database backup

					if (perfromBackup) {

						if (MovieManager.getIt().getDatabase() != null && MovieManager.getIt().getDatabase().isSetUp()) {

							if (config.handleBackupSettings()) {
								log.debug("Creating backup.");
								makeDatabaseBackup(MovieManager.getIt().getDatabase());
							}
						}
					}

				} catch (Exception e) {
					log.error("Exception:" + e.getMessage(), e);
				}
			}
		};

		SimpleProgressBar progressBar = new SimpleProgressBar(MovieManager.getDialog(), "Loading Database", true, worker);
		GUIUtil.show(progressBar, true);        

		final SwingWorker swingWorker = new SwingWorker() {
			public Object construct() {
				worker.start();
				return worker;
			}
		};
		swingWorker.start();
	}


	public boolean makeDatabaseBackup(Database db) throws Exception {
		return makeDatabaseBackup(db, null);
	}

	/*
	 * Creating backup of the datdabase.
	 */
	public boolean makeDatabaseBackup(Database db, String prefix) throws Exception {

		if (db == null) {
			throw new Exception("Database cannot be null");
		}

		MovieManagerConfig config = MovieManager.getConfig();

		try {
			String backupFolder = config.getDatabaseBackupDirectory(); 

			if (backupFolder.equals("")) {
				String tmp = db.getPath();

				if (tmp != null && !tmp.equals("")) {
					File f = new File(tmp);
					backupFolder = f.getAbsolutePath();
					f = f.getParentFile();

					if (f.isDirectory()) {
						f = new File(f, "backup");
						f.mkdir();
						config.setDatabaseBackupDirectory(f.getAbsolutePath());
						backupFolder = f.getAbsolutePath();
					}
				}
				else
					return false;
			}

			if (!new File(backupFolder).isDirectory()) {
				throw new Exception("Invalid backup directory:" + backupFolder);
			}

			int sizeLimit = Integer.parseInt(config.getDatabaseBackupDeleteOldest());

			if (sizeLimit > 0) {

				sizeLimit = sizeLimit * 1000 * 1000;

				// Getting the size of the database backup files
				int sizeOfBackupDir = 0;
				File [] f3 = new File(backupFolder).listFiles();

				for (int i = 0; i < f3.length; i++) {
					if (f3[i].isDirectory()) {
						File [] f4 = f3[i].listFiles();

						if (f4 == null)
							continue;

						for (int u = 0; u < f4.length; u++) {
							if (f4[u].isDirectory()) {
								File [] f5 = f4[u].listFiles();

								if (f5 == null)
									continue;

								for (int y = 0; y < f5.length; y++) {
									if (f5[y].isFile()) {
										sizeOfBackupDir += f5[y].length();
									}
								}
							}
						}
					}
				}

				// Delete oldest backup(s)
				if (sizeOfBackupDir > sizeLimit) {

					String[] files = new File(backupFolder).list();
					Arrays.sort(files);

					for (int u = 0; u < files.length; u++) {

						File f1 = new File(backupFolder + "/" + files[u]);

						if (!f1.isDirectory() || !Pattern.matches("\\d+\\.\\d+\\.\\d+", files[u]))
							continue;

						String [] files2 = f1.list();
						Arrays.sort(files2);
						int i;

						for (i = 0; i < files2.length && sizeOfBackupDir > sizeLimit; i++) {
							File f2 = new File(backupFolder + "/" + files[u] + "/" + files2[i]);

							long s = FileUtil.getDirectorySize(f2, null);

							FileUtil.deleteDirectoryStructure(f2);

							if (f2.getParentFile().list().length == 0)
								f2.getParentFile().delete();

							sizeOfBackupDir -= s;
						}

						if (i == files2.length-1) {
							f1.delete();
						}
					}
				}
			}

			//	    		creating the new backup
			String dbPath = db.getPath();

			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm.ss");

			Calendar c = Calendar.getInstance();

			String date = dateFormat.format(c.getTime());
			String time = timeFormat.format(c.getTime());

			if (prefix != null)
				backupFolder += "/" + prefix;

			backupFolder += "/" + date + "/" + time;

			File dbBackup = new File(backupFolder);
			boolean success = dbBackup.mkdirs();

			if (!success) {
				log.warn("Failed to create backup directory:" + dbBackup.getAbsolutePath());
				throw new Exception("Failed to create directory:" + dbBackup.getAbsolutePath());
			}

			if (db.isHSQL()) {
				File tmp = new File(dbPath + ".script");

				if (!tmp.isFile())
					throw new Exception("HSQLDB script file does not exist.");
				else {
					FileUtil.copyToDir(tmp, dbBackup);
					log.debug("Created backup of file " + tmp + " to " + dbBackup);
				}
				tmp = new File(dbPath + ".properties");

				if (!tmp.isFile())
					throw new Exception("HSQLDB properties file does not exist.");
				else {
					FileUtil.copyToDir(tmp, dbBackup);
					log.debug("Created backup of file " + tmp + " to " + dbBackup);
				}
			}
			else if (db.isMSAccess()) {
				File tmp;

				if ((tmp = new File(dbPath)).isFile()) {
					FileUtil.copyToDir(tmp, dbBackup);
					log.debug("Created backup of database to " + tmp + " in directory " + dbBackup);
				}
				else
					throw new Exception("MS Access database file does not exist:" + dbPath);
			}
		} catch (final Exception e) {
			log.warn("Error occured in backup procedure:" + e.getMessage());
			e.printStackTrace();

			try {
				if (MovieManager.getConfig().getDatabaseBackupWarnInvalidDir()) {

					GUIUtil.invokeAndWait(new Runnable() {

						public void run() {
							DialogAlertCheckBox alert = new DialogAlertCheckBox(MovieManager.getDialog(), "Backup error", "<html>Failed to create database backup:<br>" + e.getMessage() + "</html>", "Do not warn me again");
							GUIUtil.show(alert, true);
							MovieManager.getConfig().setDatabaseBackupWarnInvalidDir(alert.isButtonChecked());
						}
					});
				}
			} catch (InterruptedException e1) {
				log.error("Exception:" + e.getMessage(), e);
			} catch (InvocationTargetException e1) {
				log.error("Exception:" + e.getMessage(), e);
			}
		}
		return true;
	}


	public ModelDatabaseSearch getFilterOptions() {
		return getFilterOptions(getDatabase());
	}

	public ModelDatabaseSearch getFilterOptions(Database db) {

		MovieManagerConfig config = MovieManager.getConfig();

		ModelDatabaseSearch options = new ModelDatabaseSearch();

		options.setFilterCategory(config.getFilterCategory());

		if ("Movie Title".equals(config.getFilterCategory()) && config.getIncludeAkaTitlesInFilter()) //$NON-NLS-1$
			options.setIncludeAkaTitlesInFilter(true);
		else
			options.setIncludeAkaTitlesInFilter(false);

		options.setFilterString(MovieManager.getDialog().getFilterString());
		options.setOrderCategory(config.getSortOption());
		options.setSeen(config.getFilterSeen());

		if (config.getCurrentLists() == null)
			options.setCurrentListNames(new ArrayList<String>());
		else
			options.setCurrentListNames(new ArrayList<String>(config.getCurrentLists()));

		options.setShowUnlistedEntries(config.getShowUnlistedEntries());
		options.setListOption(0);


		if (db != null) {

			// If there are no lists, or if all the lists are shown in addition to the unlisted ones, no point in enabling lists
			if ((options.getCurrentListNames().size() > 0 || options.getShowUnlistedEntries()) &&
					!(options.getShowUnlistedEntries() && options.getCurrentListNames().size() == db.getListsColumnNames().size())) {
				options.setListOption(1);
			}

			ArrayList <String> currentLists = options.getCurrentListNames();
			ArrayList <String> dbLists = db.getListsColumnNames();

			// Verify all lists. When changing database, this might be a problem
			for (int i = 0; i < currentLists.size(); i++) {
				String list = currentLists.get(i);

				if (!dbLists.contains(list)) {
					log.warn("Found list " + list + " in currentLists which does not exist in database.");
					MovieManager.getConfig().getCurrentLists().remove(list);
					currentLists.remove(i);
					i--; // Just removed the entry, must use same index again
				}
			}

			options.getFullGeneralInfo = !db.isMySQL();
		}

		options.setRatingOption(config.getRatingOption());
		options.setRating(config.getRatingValue());
		options.setDateOption(config.getDateOption());
		options.setDate(config.getDateValue());
		options.setSearchAlias(config.getSearchAlias());

		return options;
	}
}
