/**
 * @(#)MovieManager.java
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

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Date;

import net.sf.xmm.moviemanager.commands.MovieManagerCommandExit;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.gui.DialogMovieManager;
import net.sf.xmm.moviemanager.imdblib.IMDbLib;
import net.sf.xmm.moviemanager.updater.AppUpdater;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerLoginHandler;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerStartupHandler;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;

public class MovieManager {

	static Logger log = Logger.getLogger(MovieManager.class);
    
	
	static DatabaseHandler databaseHandler = new DatabaseHandler();
	
	static HTMLTemplateHandler templateHandler = new HTMLTemplateHandler();
	
	static LookAndFeelManager lookAndFeelManager = new LookAndFeelManager();
	
    /**
     * Reference to the only instance of MovieManagerConfig.
     **/
    public static MovieManagerConfig config = new MovieManagerConfig();
    
    /**
     * Reference to the only instance of MovieManager.
     **/
    static MovieManager movieManager;
    
    /**
     * Reference to the only instance of DialogMovieManager.
     **/
    static DialogMovieManager dialogMovieManager;
     
    
    /* While multi-deleting, this is set to true */
    private boolean deleting = false;
    
    private boolean sandbox = false;
    
    public boolean isSandbox() {
    	return sandbox;
    }
      
    /**
     * Creates the main movie manager dialog
     */
    void createDialog() {
    	
    	dialogMovieManager = new DialogMovieManager();
        
        dialogMovieManager.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                MovieManagerCommandExit.execute();
            }
        });
    }
    
    /**
     * Constructor.
     **/
    private MovieManager() {}
    
    /* Applet feature is not finished */
    MovieManager(Object applet) {
        
    	
    	SysUtil.getAppMode();
    		
        movieManager = this;
        dialogMovieManager = new DialogMovieManager(applet);
        
        EventQueue.invokeLater(new Runnable() {
            public final void run() {
            	
                /* Disable HTTPClient logging output */
                //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog"); //$NON-NLS-1$ //$NON-NLS-2$
                //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
               
                //System.setProperty("apache.commons.logging.simplelog.defaultlog", "OFF");
               
                //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Log4JLogger");
                //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
                
               // System.setProperty("log4j.logger.org.apache.commons.httpclient", "OFF");
                
                //URL configFile = FileUtil.getFileURL("log4j.properties"); //$NON-NLS-1$
                
                //PropertyConfigurator.configure(configFile);
                
                log = Logger.getRootLogger();
                
                /* Writes the date. */
                log.debug("Log Start: " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
                log.debug("Starting applet"); //$NON-NLS-1$
                
                /* Loads the config */
                config.loadConfig();
                
                /* Must be executed before the JFrame (MovieManager) object is created. */
                if (lookAndFeelManager.getDefaultLookAndFeelDecorated()) {
                	DialogMovieManager.setDefaultLookAndFeelDecorated(true);
                }
                
                /* Starts the MovieManager. */
                MovieManager.getDialog().setUp();
                
                /* Loads the database. */
                databaseHandler.loadDatabase();
            }
        });
    }
    
    
    /**
     * Returns a reference to the only instance of MovieManager.
     *
     * @return Reference to the only instance of MovieManager.
     **/
    public static MovieManager getIt() {
        return movieManager;
    }
    
    /**
     * Returns a reference to the only instance of MovieManager.
     *
     * @return Reference to the only instance of the DialogMovieManager.
     **/
    public static DialogMovieManager getDialog() {
        return dialogMovieManager;
    }
    
      
    /**
     * Returns a reference to the only instance of MovieManager.
     *
     * @return Reference to the only instance of MovieManager.
     **/
    public static MovieManagerConfig getConfig() {
        return config;
    }
    
    public static DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }
    
    public static HTMLTemplateHandler getTemplateHandler() {
        return templateHandler;
    }
    
    public static LookAndFeelManager getLookAndFeelManager() {
        return lookAndFeelManager;
    }     
    
    
    /**
     * Returns the current database.
     *
     * @return The current database.
     **/
    synchronized public Database getDatabase() {
        return databaseHandler.getDatabase();
    }
    
   
      
    public int getHeight() {
        return dialogMovieManager.getHeight();
    }
    
    public int getWidth() {
        return dialogMovieManager.getWidth();
    }
    
    public Point getLocation() {
        return dialogMovieManager.getLocation();
    }
    
    public int getFontSize() {
        return dialogMovieManager.getFontSize();
    }
    
    
    public static boolean isApplet() {
    	return DialogMovieManager.isApplet();
    }
    
    public void addDatabaseList(String listName) {

		log.info("Ceating list " + listName);

		getDatabase().addListsColumn(listName);
		MovieManager.getConfig().addToCurrentLists(listName);

		MovieManager.getDialog().loadMenuLists();
	}


    public void setDeleting(boolean deleting) {
    	this.deleting = deleting;
    }

    public boolean isDeleting() {
    	return deleting;
    }
    
    
    public static void exit() {
        
        if (isApplet())
            DialogMovieManager.destroy();
        else
            System.exit(0);
    }
    
    
    public static void main(String args[]) {
     
    	boolean sandbox = SysUtil.isRestrictedSandbox();
    	
    	// Uses this to check if the app is running in a sandbox with limited privileges
    	try {
    		/* Disable HTTPClient logging output */
    		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog"); //$NON-NLS-1$ //$NON-NLS-2$
    				
    	} catch (java.security.AccessControlException s) {    
    		s.printStackTrace();
    		sandbox = true;
    	}
    		
    	if (!sandbox) {
    		// Disables logging for cobra html renderer
    		java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.OFF); 

    		File log4jConfigFile = FileUtil.getFile("config/log4j.properties"); //$NON-NLS-1$
		
    		if (log4jConfigFile.isFile()) {
    			PropertyConfigurator.configure(log4jConfigFile.getAbsolutePath());
    		} else {
    			BasicConfigurator.configure();
    		}
    	}
    	else
    		BasicConfigurator.configure();
		
    	log = Logger.getRootLogger();
    	
    	// Places the Log file in the user directory (the program location)
    	RollingFileAppender appndr = (RollingFileAppender) log.getAppender("FileAppender");
    	
    	String logFile = null;
    	
    	try {
			if (SysUtil.isMac() || SysUtil.isWindowsVista() || SysUtil.isWindows7())
				logFile = new File(SysUtil.getConfigDir(), "Log.txt").getAbsolutePath();
    	} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			
			if (logFile == null)
				logFile = new File(SysUtil.getUserDir(), "Log.txt").getAbsolutePath();
		}
    		
		if (appndr != null && appndr.getFile() == null) {
			appndr.setFile(logFile);
			appndr.activateOptions();
		}
				
		/* Writes the date. */
		log.debug("================================================================================"); //$NON-NLS-1$
		log.debug("Log Start: " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
		log.debug("MeD's Movie Manager v" + config.sysSettings.getVersion()); //$NON-NLS-1$
		log.debug("MovieManager release:" + MovieManager.getConfig().sysSettings.getRelease() + " - " +
				"IMDb Lib release:" + IMDbLib.getRelease() + " (" + IMDbLib.getVersion() + ")");
		log.debug(SysUtil.getSystemInfo(SysUtil.getLineSeparator())); //$NON-NLS-1$
						
		/* Loads the config */
		if (!sandbox)
			config.loadConfig();
		
		// Calls the plugin startup method 
		MovieManagerStartupHandler startupHandler = MovieManager.getConfig().getStartupHandler();
		
		if (startupHandler != null) {
			startupHandler.startUp();
		}
				
		if (!sandbox) {
							
			if (SysUtil.isAtLeastJRE6()) {
				SysUtil.includeJarFilesInClasspath("lib/LookAndFeels/1.6");
			}
			
			SysUtil.includeJarFilesInClasspath("lib/LookAndFeels");
			SysUtil.includeJarFilesInClasspath("lib/drivers");
		
			/* Must be called before the GUI is created */
			if (SysUtil.isMac()) { 
				SysUtil.includeJarFilesInClasspath("lib/mac");
				lookAndFeelManager.setupOSXLaF(); 
			}
		}
				
		movieManager = new MovieManager();
		movieManager.sandbox = sandbox;
		
//		 Loads the HTML templates
		templateHandler.loadHTMLTemplates();
		
		EventQueue.invokeLater(new Runnable() {
            public final void run() {

            	try {
            		
            		/* Installs the Look&Feels */
            		lookAndFeelManager.instalLAFs();

            		if (!MovieManager.isApplet())
            			lookAndFeelManager.setLookAndFeel();
            		
            		log.debug("Look & Feels installed.");
                	
            		log.debug("Creating MovieManager Dialog");
            		movieManager.createDialog();
            		            		
            		/* Starts the MovieManager. */
            		MovieManager.getDialog().setUp();
            		log.debug("MovieManager Dialog - setup.");
            		
            		MovieManager.getDialog().showDialog();
            		
            		/* SetUp the Application Menu for OSX */
            		if (SysUtil.isMac()) {
            			LookAndFeelManager.macOSXRegistration(MovieManager.getDialog());
            		}   
            		            		
            		// Calls the plugin startup method 
            		MovieManagerLoginHandler loginHandler = MovieManager.getConfig().getLoginHandler();
            		
					if (loginHandler != null) {
            			loginHandler.loginStartUp();
        			}
            		
					log.debug("Loading Database....");
            		
            		/* Loads the database. */
            		databaseHandler.loadDatabase(true);

            		log.debug("Database loaded.");
            		
            		AppUpdater.handleVersionUpdate();
            		            		
            	} catch (Exception e) {
            		log.error("Exception occured while intializing MeD's Movie Manager", e);
            	}
            }
        });
    }
}