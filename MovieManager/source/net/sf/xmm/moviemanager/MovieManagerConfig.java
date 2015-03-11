/**
 * @(#)MovieManagerConfig.java
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import net.sf.xmm.moviemanager.LookAndFeelManager.LookAndFeelType;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.gui.DialogMovieManager;
import net.sf.xmm.moviemanager.http.HttpSettings;
import net.sf.xmm.moviemanager.models.AdditionalInfoFieldDefaultValues;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplate;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplateStyle;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ExportMode;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImportMode;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedJTree;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.events.NewDatabaseLoadedEvent;
import net.sf.xmm.moviemanager.util.events.NewDatabaseLoadedEventListener;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerConfigHandler;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerLoginHandler;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerPlayHandler;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerStartupHandler;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerStreamerHandler;

import org.apache.log4j.Logger;

public class MovieManagerConfig implements NewDatabaseLoadedEventListener {

	Logger log = Logger.getLogger(MovieManagerConfig.class);

	public class SystemSettings {

		/**
		 * The current version of the program.
		 **/
		private static final String _version = "2.9.1.3"; //$NON-NLS-1$
		
		// Increase with one for each release. Used for jupidator update library
		private static final int release = 7;

		String appTitle = " MeD's Movie Manager v" + getVersion().trim();
		String lookAndFeelTitle = "Look & Feel";

		/**
		 * Returns the version.
		 *
		 * @return Program Version.
		 **/
		public String getVersion() {
			return _version;
		}

		public int getRelease() {
			return release;
		}
				
		public String getLookAndFeelTitle() {
	    	return lookAndFeelTitle;
	    }
	    
	    public void setLookAndFeelTitle(String lookAndFeelTitle) {
	    	this.lookAndFeelTitle = lookAndFeelTitle;
	    }
	    
	      
	    public String getAppTitle() {
	    	return appTitle;
	    }
	    
	    public void setAppTitle(String t) {
	    	appTitle = t;
	    }
		
	}
	
	public final SystemSettings sysSettings = new SystemSettings();
		
	HTMLTemplateHandler htmlTemplateHandler = new HTMLTemplateHandler();
	
	public HTMLTemplateHandler getHTMLTemplateHandler() {
		return htmlTemplateHandler;
	}
	
	private boolean localConfigMode = false;
	
	public boolean getLocalConfigMode() {
		return localConfigMode;
	}
	
	/**
	 * Keeps track of the last directory open...(Moviefiles)
	 **/
	private static File lastFileDir;
	private static File lastDVDDir;

	private static File lastExportAndImportDir;

	private static File lastCoversDir;

	/**
	 * Keeps track of the last directory open...(Databasefiles)
	 **/
	private static File lastDatabaseDir;

	/**
	 * Keeps track of the last directory open...(Misc)
	 **/
	private static File lastMiscDir;

	/**
	 * Database file path
	 **/
	private String databasePath;

	private String coversFolder = "";

	private String queriesFolder = "";

	private Color invalidPathColor = new Color(233, 180, 180);
	
	public Color getInvalidPathColor() {
		return invalidPathColor;
	}
	
	private boolean displayPlayButton = true;
	private boolean displayPrintButton = false;
	
	private boolean useRegularSeenIcon = true;

	private boolean useJTreeIcons = true;

	private boolean useJTreeCovers = true;

	private int movieListRowHeight = 45;

	
	final private int defaultFrameHeight = 635;
	final private int defaultFrameWidth = 850;

	final private int coverAreaWidth = 110;
	final private int coverAreaHeight = 165;
	
	public Dimension getCoverAreaSize() {
		return new Dimension(coverAreaWidth, coverAreaHeight);
	}
	
	/* Main window size */
	public Dimension mainSize = new Dimension(defaultFrameWidth, defaultFrameHeight);

	int addMovieWindowHeight = -1;
		
	private Point screenLocation;

	private boolean mainMaximized = false;

	private boolean enableCtrlMouseRightClick = false;

	/* 0 == No, 1 == Preserve all, 2 == Preserve episode covers only */
	private int preserveCoverAspectRatioSetting = 2;

	private String playerPath = "";
	private String browserPath = "";

	private String mediaPlayerCmdArgument = "";
		
	private boolean useDefaultWindowsPlayer = true;
	private boolean executeExternalPlayCommand = false;
	
	public final String defaultLocale = "en-US";
	private String locale = "en-US";
	
	private String systemWebBrowser = SysUtil.getDefaultPlatformBrowser();
	
	private String databaseBackupEveryLaunch = "2";
	private String databaseBackupDeleteOldest = "20";
	private String databaseBackupLaunchCount = "0";
	private String databaseBackupDirectory = "";
	private boolean databaseBackupWarnInvalidDir = true;
	
	private int lastMovieInfoTabIndex = 0;
	private int lastPreferencesTabIndex = 0;

	public enum MediaInfoOption {MediaInfo_No, MediaInfo_yesifnojava, MediaInfo_Yes;
	
		public static MediaInfoOption getValue(int val) {

		switch(val) {
		case 0: return MediaInfoOption.MediaInfo_No;
		case 1: return MediaInfoOption.MediaInfo_yesifnojava;
		case 2: return MediaInfoOption.MediaInfo_Yes;
		}
		return MediaInfoOption.MediaInfo_No;
	}

	}
	/* 0 = no, 1 = yes if no java parser avaliable, 2 = yes */
	//private int useMediaInfoDLL = 1;
	private MediaInfoOption useMediaInfoDLL = MediaInfoOption.MediaInfo_yesifnojava;

	
	/* Used only with MySQL database */
	private boolean storeCoversLocally = false;

	private boolean mySQLSocketTimeoutEnabled = false;
	
	/**
	 * Decides what kind of filter the movie search will use.
	 **/
	private String filterCategory = "Movie Title";

	/* Only applies if filterCategory == "Movie Title" */
	private boolean includeAkaTitlesInFilter = true;

	/**
	 * Decides what kind of filter the movie search will use.
	 **/
	private String sortOption = "Title";

	/**
       Tells if the filter should filter out seen/unseen movies.
       0 means the seen is disabled (and seenButton selected),
       1 means the seen is disabled (and unseenButton selected),
       2 means show only seen,
       3 means show only unseen
	 **/
	private int filterSeen = 0;

	/**
       0 means the rating is disabled (and ratingAboveButton selected),
       1 means the rating is disabled (and ratingBelowButton selected),
       2 means show only above the ratingValue,
       3 means show only below the ratingValue.
       ratingValue == value from JComboBox
	 **/
	private int ratingOption = 0;

	/*from 1-10, the actual movie rating value*/
	private double ratingValue = 5;

	/**
       0 means the date is disabled (and dateAboveButton selected),
       1 means the date is disabled (and dateBelowButton selected),
       2 means show only above the dateValue,
       3 means show only below the dateValue.
       dateOption == value from JComboBox
	 **/
	private int dateOption = 0;

	/*Any string, for instance 1990*/
	private String dateValue = "";

	private HashMap<String, String> searchAlias = new HashMap<String, String>();

	/* Stores default values for additional info fields, key == fieldName */
	private HashMap<String, AdditionalInfoFieldDefaultValues> additionalInfoDefaultValues = new HashMap<String, AdditionalInfoFieldDefaultValues>();

	private ArrayList<String> mainFilterSearchValues = new ArrayList<String>();
	
	private boolean loadDatabaseOnStartup = true;

	private boolean loadLastUsedListAtStartup = false;
	private boolean addNewMoviesToCurrentLists = false;
		
	private boolean seenEditableInMainWindow = true;

	private String titleLanguageCode = "";

	private boolean storeAllAkaTitles = false;
	private boolean includeAkaLanguageCodes = false;
	private boolean useLanguageSpecificTitle = false;

	/*********************
	  Multiadd options
	 *********************/
	
	ArrayList<String> multiaddRootDevices = new ArrayList<String>();
	
	private String multiAddDirectoryPath = "";
		
	private boolean multiAddRegexCaseSensitive = false;
	private String multiAddRegexString = "";
	private boolean multiAddRegexStringEnabled = false;
	private boolean multiAddRegexStringNegated = false;

	private ArrayList <String> multiAddValidExtensions = new ArrayList<String>();
	private String multiAddCustomExtensions = "";
	
	private ImdbImportOption multiAddSelectOption = ImdbImportOption.displayList;
	private boolean multiAddEnableExludeParantheses;
	private boolean multiAddEnableExludeCDNotation;
	private boolean multiAddEnableExludeIntegers;
	private boolean multiAddEnableExludeYear;
	private boolean multiAddEnableExludeAllAfterMatchOnUserDefinedInfo;
	private boolean multiAddEnableSearchInSubdirectories;
	private boolean multiAddTitleOption;
	private boolean multiAddEnableExludeUserdefinedInfo;
	private boolean multiAddTitleOptionNoCd;
	private boolean multiAddAddSearchNfoForImdb;
	private boolean multiAddSkipHiddenDirectories = true;
	private boolean multiAddPrefixMovieTitle;
	private boolean multiAddEnableAutomaticCombine = true;
	private boolean multiAddFilterOutDuplicates = true;
	private boolean multiAddFilterOutDuplicatesByAbsolutePath = true;
	
	
	private String multiAddExcludeUserDefined = "";
		
	int multiAddMainSliderPosition = -1;
	int multiAddFileSliderPosition = -1;
	int multiAddTabIndex = 0;
	
	Dimension defaultMultiAddWindowSize = new Dimension(800,600);
	Dimension multiAddWindowSize = new Dimension(defaultMultiAddWindowSize);
	
	Dimension defaultMultiAddIMDbDialogWindowSize = new Dimension(400, 300);
	Dimension multiAddIMDbDialogWindowSize = new Dimension(defaultMultiAddIMDbDialogWindowSize);
	
	/* Import */
	private ImportMode lastDialogImportType = ImportMode.TEXT;
	
	private String importTextFilePath = "";
	private String importExcelFilePath = "";
	private String importXMLFilePath = "";
	private String importCSVFilePath = "";
	private String importCSVseparator = ",";
	
	private ImdbImportOption importIMDbSelectOption = ImdbImportOption.off;
	private boolean importIMDbInfoEnabled = true;
	
	/* Export */
	private ExportMode lastDialogExportType = ExportMode.CSV;
		
	private String exportTextFilePath = "";
	private String exportExcelFilePath = "";
	private String exportXMLDbFilePath = "";
	private String exportXMLFilePath = "";
	private String exportCSVFilePath = "";
	private String exportCSVseparator = ",";
			
	private String castorMappingFile = "config/Castor-mapping.xml";
		
	private String lastFileFilterMovieInfoUsed = "";

	private HttpSettings httpSettings = new HttpSettings();
	
	/*Export*/
	private String htmlExportType = "simple";

	public enum NoCoverType {Puma, Jaguar, Tiger};
	public NoCoverType noCoverType = NoCoverType.Puma;
	
	
	/* Current list */
	protected ArrayList <String> currentLists = new ArrayList<String>();
	boolean showUnlistedEntries = true;
	
	protected boolean multiAddListEnabled = false;

	private int useRelativeDatabasePath = 0; // 0 == absolute, 2 == program location
	private int useRelativeCoversPath = 1;   // 0 == absolute, 1 == database, 2 == program location 
	private int useRelativeQueriesPath = 1;  // 0 == absolute, 1 == database, 2 == program location 

	private String coversPath;
	private String queriesPath;

	private boolean databasePathPermanent = false;

	private boolean useDisplayQueriesInTree = true;

	private String multipleAddList = "";

	/* Movie Info SplitPane */
	public int mainWindowSliderPosition = -1;
	public int mainWindowLastSliderPosition = -1;
	
	public int movieInfoSliderPosition = -1;
	public int movieInfoLastSliderPosition = -1;

	/* Additional Info / Notes SplitPane */
	public int additionalInfoNotesSliderPosition = -1;
	public int additionalInfoNotesLastSliderPosition = -1;

	int plotCastMiscellaneousIndex = 0;
	

	private boolean checkForProgramUpdates = true;
	
	private boolean htmlViewDebugMode = false;
	
	private InternalConfig internalConfig = new InternalConfig();
	
	public class InternalConfig {
		
		// Everything enabled by default
			
		private boolean toolBarPopup = true;
		private boolean addMovieEnabled = true;
		private boolean removeMovieEnabled = true;
		private boolean editMovieEnabled = true;
		private boolean playMovieEnabled = true;
		private boolean printFunctionEnabled = true;
		private boolean searchMenuEnabled = true;
				
		private boolean displayEntriesCount = true;
				
		private boolean listsFunctionalityEnabled = true;
		
		private boolean preferencesMovieList = true;
		private boolean preferencesExternalPrograms = true;
		private boolean preferencesExternalProgramsPlayer = true;
		private boolean preferencesDatabaseBackup = true;
		private boolean preferencesIMDbSettings = true;
		private boolean preferencesLookAndFeel = true;
		private boolean preferencesProxySettings = true;
		private boolean preferencesMiscellaneous = true;
		private boolean preferencesCoverSettings = true;
		private boolean movieSeenReplaceWithPlay = false;
		private boolean additionalInfoAndNotesReplacedByHTMLAdd = false;
		private boolean playButtonNeverDisabled = false;
		private boolean disableHTMLView = false;
		private boolean loadLastUsedListEnabled = true;
		private boolean movieListPopupEnabled = true;
		private boolean searchAliasEnabled = true;
		private boolean sensitivePrintMode = false;
		
		private HashMap<String, String> plugins = new HashMap<String, String>();
		
		public void addPlugin(String key, String value) {
			if (key != null && !"".equals(key) && value != null && !"".equals(value))
				plugins.put(key, value);
		}
		
		public String getPlugin(String key) {
			return (String) plugins.get(key);
		}
		
		InternalConfig() {}
		
		InternalConfig(ArrayList<String> lines) {
							
			for (int i = 0; i < lines.size(); i++) {
			
				String line = lines.get(i);
													
				if (line.startsWith("toolBarPopup:")) 
					toolBarPopup = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("addMovieEnabled:")) 
					addMovieEnabled = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("removeMovieEnabled:")) 
					removeMovieEnabled = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("editMovieEnabled:")) 
					editMovieEnabled = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("playMovieEnabled")) 
					playMovieEnabled = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("printFunctionEnabled:")) 
					printFunctionEnabled = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("searchMenuEnabled:")) 
					searchMenuEnabled = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("listsFunctionalityEnabled:")) 
					listsFunctionalityEnabled = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("preferencesMovieList:")) 
					preferencesMovieList = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("preferencesExternalPrograms:")) 
					preferencesExternalPrograms = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("preferencesExternalProgramsPlayer:")) 
					preferencesExternalProgramsPlayer = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("preferencesDatabaseBackup:")) 
					preferencesDatabaseBackup = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("preferencesIMDbSettings:")) 
					preferencesIMDbSettings = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("preferencesLookAndFeel:")) 
					preferencesLookAndFeel = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("preferencesProxySettings:")) 
					preferencesProxySettings = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("preferencesMiscellaneous:")) 
					preferencesMiscellaneous = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("preferencesCoverSettings:")) 
					preferencesCoverSettings = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("movieSeenReplaceWithPlay:")) 
					movieSeenReplaceWithPlay = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("displayEntriesCount:")) 
					displayEntriesCount = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("additionalInfoAndNotesReplacedByHTMLAdd:")) 
					additionalInfoAndNotesReplacedByHTMLAdd = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("playButtonNeverDisabled:")) 
					playButtonNeverDisabled = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("disableHTMLView:")) 
					disableHTMLView = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("loadLastUsedListEnabled:")) 
					loadLastUsedListEnabled = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("movieListPopupEnabled:")) 
					movieListPopupEnabled = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
				else if (line.startsWith("searchAliasEnabled:")) 
					searchAliasEnabled = new Boolean(line.substring(line.indexOf(":") +1, line.length())).booleanValue();
			}		
		}
		
			
		public boolean isToolBarPopupDisabled() {
			return !toolBarPopup;
		}
		
		public boolean isAddMovieDisabled() {
			return !addMovieEnabled;
		}
		
		public boolean isEditMovieDisabled() {
			return !editMovieEnabled;
		}
		
		public boolean isRemoveMovieDisabled() {
			return !removeMovieEnabled;
		}
	
		public boolean isSearchMenuDisabled() {
			return !searchMenuEnabled;
		}
		
		public boolean isPlayMovieDisabled() {
			return !playMovieEnabled;
		}

		public boolean isPrintFunctionDisabled() {
			return !printFunctionEnabled;
		}
		
		public boolean isEntriesCountDisabled() {
			return !displayEntriesCount;
		}
		
		public boolean movieSeenReplaceWithPlay() {
			return movieSeenReplaceWithPlay;
		}
				
		public boolean isListsFunctionalityDisabled() {
			return !listsFunctionalityEnabled;
		}
				
		public boolean isPreferencesCoverSettingsDisabled() {
			return !preferencesCoverSettings;
		}

		public boolean isPreferencesDatabaseBackupDisabled() {
			return !preferencesDatabaseBackup;
		}

		public boolean isPreferencesExternalProgramsDisabled() {
			return !preferencesExternalPrograms;
		}
		
		public boolean isPreferencesExternalProgramsPlayerDisabled() {
			return !preferencesExternalProgramsPlayer;
		}
		
		public boolean isPreferencesIMDbSettingsDisabled() {
			return !preferencesIMDbSettings;
		}

		public boolean isPreferencesLookAndFeelDisabled() {
			return !preferencesLookAndFeel;
		}

		public boolean isPreferencesMiscellaneousDisabled() {
			return !preferencesMiscellaneous;
		}
		
		public boolean isPreferencesMovieListDisabled() {
			return !preferencesMovieList;
		}

		public boolean isPreferencesProxySettingsDisabled() {
			return !preferencesProxySettings;
		}
			
		public boolean isAdditionalInfoAndNotesReplacedByHTMLAdd() {
			return additionalInfoAndNotesReplacedByHTMLAdd;
		}					
		
		public boolean getPlayButtonNeverDisabled() {
			return playButtonNeverDisabled;
		}
		
		public boolean getDisableHTMLView() {
			return disableHTMLView;
		}
		
		public boolean getDisableLoadLastUsedList() {
			return !loadLastUsedListEnabled;
		}

		public boolean getMovieListPopupDisabled() {
			return !movieListPopupEnabled;
		}		
		
		public boolean getSearchAliasDisabled() {
			return !searchAliasEnabled;
		}	
		
		public boolean getSensitivePrintMode() {
			return sensitivePrintMode;
		}
		
		/*
		 * Sensitive info like username and password for server shouldn't be printed.
		 */
		public void enableSensitivePrint() {
			sensitivePrintMode = true;
		}
	}

	
	public MovieManagerConfig(boolean exampleConfig) {}
	
	public MovieManagerConfig() {
		
		MovieManager.getDatabaseHandler().getNewDatabaseLoadedHandler().addNewDatabaseLoadedEventListener(this);
		
		try {
			InputStream inputStream = FileUtil.getResourceAsStream("/config/internalConfig.ini");

			if (inputStream != null) {

				ArrayList<String> lines = FileUtil.readArrayList(new InputStreamReader(inputStream));

				if (lines != null) {
					internalConfig = new InternalConfig(lines);
					log.debug("internalConfig.ini");
				}

				inputStream = FileUtil.getResourceAsStream("/config/plugins.ini");

				if (inputStream == null)
					return;

				lines = FileUtil.readArrayList(new InputStreamReader(inputStream));

				for (int i = 0; i < lines.size(); i++) {

					String line = (String) lines.get(i);
					String key = line.substring(0, line.indexOf("="));
					String value = line.substring(line.indexOf("=") + 1, line.length());
					internalConfig.addPlugin(key, value);
				}
			}
			
			File localConfig = new File(new File(SysUtil.getUserDir(), "config"), "uselocalconfig");
						
			if (localConfig.isFile()) {
				localConfigMode = true;
			}
			
		} catch (Exception e) {
			log.warn("Exception: " + e.getMessage(), e);
		}	
	}
		
	
	public InternalConfig getInternalConfig() {
		return internalConfig;
	}

	
	
	public MovieManagerPlayHandler getPlayHandler() {
		Object playHandlerObject = SysUtil.getClass(internalConfig.getPlugin("playHandler"));
		
		log.debug("playHandlerObject:" + playHandlerObject);
		
		if (playHandlerObject != null)
			return (MovieManagerPlayHandler) playHandlerObject;
		
		return null;
	}
	
	public MovieManagerLoginHandler getLoginHandler() {
		Object loginHandlerObject = SysUtil.getClass(internalConfig.getPlugin("loginHandler"));
		
		if (loginHandlerObject != null)
			return (MovieManagerLoginHandler) loginHandlerObject;
		
		return null;
	}
			
	public MovieManagerStreamerHandler getStreamerHandler() {
		Object streamerHandlerObject = SysUtil.getClass(internalConfig.getPlugin("streamerHandler"));
		
		if (streamerHandlerObject != null)
			return (MovieManagerStreamerHandler) streamerHandlerObject;
		
		return null;
	}
	
	
	public MovieManagerConfigHandler getConfigHandler() {
		Object configHandlerObject = SysUtil.getClass(internalConfig.getPlugin("configHandler"));
		
		if (configHandlerObject != null) 
			return (MovieManagerConfigHandler) configHandlerObject;
		
		return null;
	}
	
	
	public MovieManagerStartupHandler getStartupHandler() {
		
		Object startupHandlerObject = SysUtil.getClass(internalConfig.getPlugin("startupHandler"));
		
		if (startupHandlerObject != null)
			return (MovieManagerStartupHandler) startupHandlerObject;
		
		return null;
	}
	
	
	public void newDatabaseLoaded(NewDatabaseLoadedEvent evt) {
		resetCoverAndQueries();
	}


	public String getLastFileFilterUsed() {
		return lastFileFilterMovieInfoUsed;
	}

	public void setLastFileFilterUsed(String lastFileFilterMovieInfoUsed) {
		this.lastFileFilterMovieInfoUsed = lastFileFilterMovieInfoUsed;
	}

	/**
	 * @return The last fileChooser directory.
	 **/
	public File getLastFileDir() {

		if (lastFileDir == null)
			return null;

		if (lastFileDir.exists()) {
			return lastFileDir;
		}
		
		return new File("");
	}


	/**
	 * Sets the current directory.
	 *
	 * @param A directory.
	 **/
	public void setLastFileDir(File directory) {
		lastFileDir = directory;
	}

	/* If the display name is empty, it's probably an empty removable device */
	public File getLastDVDDir() {

		String displayName = null;
		
		if (lastDVDDir != null) {
			displayName = SysUtil.getDriveDisplayName(lastDVDDir);
			
			try {
				displayName = StringUtil.performExcludeParantheses(displayName, false);
			} catch (Exception e) {
				log.error("Exception:" + e.getMessage(), e);
			}
		}
		
		if (displayName != null) {

			if (!displayName.equals(""))
				return lastDVDDir;

			new File("");
		}
		return lastDVDDir;
	}


	public void setLastDVDDir(File directory) {
		lastDVDDir = directory;
	}

	public File getLastExportAndImportDir() {
		return lastExportAndImportDir;
	}

	public void setLastExportAndImportDir(File directory) {
		lastExportAndImportDir = directory;
	}

	public File getLastCoversDir() {
		return lastCoversDir;
	}

	public void setLastCoversDir(File directory) {
		lastCoversDir = directory;
	}


	public File getLastDatabaseDir() {
		return lastDatabaseDir;
	}

	public void setLastDatabaseDir(File directory) {
		lastDatabaseDir = directory;
	}

	public File getLastMiscDir() {
		return lastMiscDir;
	}

	public void setLastMiscDir(File directory) {
		lastMiscDir = directory;
	}


	public void setCoverAndQueriesPaths(String coversPath, String queriesPath) {

		this.queriesFolder = "";
		this.coversFolder = "";

		this.coversPath = coversPath;
		this.queriesPath = queriesPath;
		MovieManager.getIt().getDatabase().setFolders(coversPath, queriesPath);

	}

	public void updateCoverAndQueriesPaths(String coversPath, String queriesPath) {
		this.coversPath = coversPath;
		this.queriesPath = queriesPath;
	}


	public void resetCoverAndQueries() {
		coversPath = "";
		queriesPath = "";
		coversFolder = "";
		queriesFolder = "";
	}

	public String getCoversFolder() {
		return getCoversFolder(null);
	}
	
	
	/* Returns the value stored in the database */
	public String getCoversFolder(Database database) {
		
		if (database != null) {
			return database.getCoversFolder();
		}

		database = MovieManager.getIt().getDatabase();
		
		if (this.coversFolder.equals("") && database != null) {
			this.coversFolder = database.getCoversFolder();
		}
		
		return this.coversFolder;
	}

	
	public String getCoversPath() {
		return getCoversPath(true);
	}
	
	public String getCoversPath(boolean usedatabase) {
		return getCoversPath(usedatabase ? MovieManager.getIt().getDatabase() : null);
	}

	/* Returns the absolute cover path: NOT healthy*/
	public String getCoversPath(Database database) {
		/* Get covers folder from database*/
		String coversFolder = getCoversFolder(database);
		String coversPath = "";
		
		
		/* Relative to user dir */
		if (getUseRelativeCoversPath() == 2) {
			coversPath = SysUtil.getUserDir() + File.separator;
		}
		/* Relative to database location - incompatible with MySQL */
		else if (getUseRelativeCoversPath() == 1 && database != null && !database.isMySQL()) {
			String dbPath = database.getPath();
			
			if (dbPath != null && dbPath.indexOf(SysUtil.getDirSeparator()) != -1) {
				dbPath = dbPath.substring(0, dbPath.lastIndexOf(SysUtil.getDirSeparator()));
			}
			coversPath = dbPath + File.separator;
		}
				
		if (new File(coversPath + coversFolder).isDirectory())
			return new File(coversPath + coversFolder).getAbsolutePath();
		else if (new File(coversFolder).isDirectory())
			return new File(coversFolder).getAbsolutePath();
		else if (new File(coversPath).isDirectory())
			return new File(coversPath).getAbsolutePath();
			
//		 May not be replaced by "new File(coversPath, coversFolder)"
		return new File(coversPath + coversFolder).getAbsolutePath();
	}
	
	public String getQueriesPath() {
		return getQueriesPath(true);
	}
	
	public String getQueriesPath(boolean usedatabase) {
		return getQueriesPath(usedatabase ? MovieManager.getIt().getDatabase() : null);
	}
	
	/* Returns the relative queries path */
	public String getQueriesPath(Database database) {

		String queriesFolder = getQueriesFolder(database);

		/* Get queries folder from database*/
		String queriesPath = "";

		/* If relative path is used checks if directory exist after the user dir is added to the beginning */
		if (getUseRelativeQueriesPath() == 2) {
			queriesPath = SysUtil.getUserDir() + File.separator;
		}
		/* Relative to database location - incompatible with MySQL */
		else if (getUseRelativeQueriesPath() == 1 && database != null && !database.isMySQL()) {
			String dbPath = database.getPath();
						
			if (dbPath != null && dbPath.indexOf(SysUtil.getDirSeparator()) != -1) {
				dbPath = dbPath.substring(0, dbPath.lastIndexOf(SysUtil.getDirSeparator()));
			}
							
			queriesPath = dbPath + File.separator;
		}
		
		if (new File(queriesPath + queriesFolder).isDirectory())
			return new File(queriesPath + queriesFolder).getAbsolutePath();
		else if (new File(queriesFolder).isDirectory())
			return new File(queriesFolder).getAbsolutePath();
		else if (new File(queriesPath).isDirectory())
			return new File(queriesPath).getAbsolutePath();
		
		// May not be replaced by "new File(queriesPath, queriesFolder)"
		return new File(queriesPath + queriesFolder).getAbsolutePath();
	}

	
	public String getQueriesFolder(Database database) {

		if (database != null) {
			return database.getQueriesFolder();
		}

		database = MovieManager.getIt().getDatabase();

		if (this.queriesFolder.equals("") && database != null) {
			this.queriesFolder = database.getQueriesFolder();
		}
		return this.queriesFolder;
	}

	

	
	public String getQueriesFolder() {
		return getQueriesFolder(null);
	}

	public String getDatabasePath(boolean getPathFromDatabase) {

		/* When loading the database from the config file the path is stored in this.databasePath */
		if (!getPathFromDatabase || MovieManager.getIt().getDatabase() == null)
			return this.databasePath;

		return MovieManager.getIt().getDatabase().getPath();
	}

	public void setDatabasePath(String dbPath) {
		databasePath = dbPath;	
	}

	// Not the database file, but the folder containing the database files.
	public String getDatabaseFolder(boolean getPathFromDatabase) {
		return new File(getDatabasePath(getPathFromDatabase)).getParent() + SysUtil.getDirSeparator();
	}

	/* Used by the filter to determine which conditions to filter out movies. */
	/* filterCategory to determine which category to filter by */
	public void setFilterCategory(String filterString) {
		filterCategory = filterString;
	}

	public String getFilterCategory() {
		return filterCategory;
	}

	public void setIncludeAkaTitlesInFilter(boolean includeAkaTitlesInFilter) {
		this.includeAkaTitlesInFilter = includeAkaTitlesInFilter;
	}

	public boolean getIncludeAkaTitlesInFilter() {
		return includeAkaTitlesInFilter;
	}


	/*Used to determine how to sort.*/
	/*sortOption to determine which category to sort by*/
	public void setSortOption(String sortOption) {
		this.sortOption = sortOption;
	}

	public String getSortOption() {
		return sortOption;
	}



	/* filterSeen is to determine if all movies, only seen, or only unseen movies should show up*/
	public void setFilterSeen(int filterSeen) {
		this.filterSeen = filterSeen;
	}

	public int getFilterSeen() {
		return filterSeen;
	}

	public void setRatingOption(int ratingOption) {
		this.ratingOption = ratingOption;
	}

	public int getRatingOption() {
		return ratingOption;
	}

	public void setRatingValue(double ratingValue) {
		this.ratingValue = ratingValue;
	}

	public double getRatingValue() {
		return ratingValue;
	}

	public void setDateOption(int dateOption) {
		this.dateOption = dateOption;
	}

	public int getDateOption() {
		return dateOption;
	}

	public void setDateValue(String dateValue) {
		this.dateValue = dateValue;
	}

	public String getDateValue() {
		return dateValue;
	}


	public MediaInfoOption getUseMediaInfoDLL() {
		return useMediaInfoDLL;
	}

	public void setUseMediaInfoDLL(MediaInfoOption useMediaInfoDLL) {
		this.useMediaInfoDLL = useMediaInfoDLL;
	}

	public boolean getStoreCoversLocally() {
		return storeCoversLocally;
	}

	public void setStoreCoversLocally(boolean storeCoversLocally) {
		this.storeCoversLocally = storeCoversLocally;
	}

	public boolean getMySQLSocketTimeoutEnabled() {
		return mySQLSocketTimeoutEnabled;
	}

	public void setMySQLSocketTimeoutEnabled(boolean mySQLSocketTimeoutEnabled) {
		this.mySQLSocketTimeoutEnabled = mySQLSocketTimeoutEnabled;
	}
		
	public boolean getLoadDatabaseOnStartup() {
		return loadDatabaseOnStartup;
	}

	public void setLoadDatabaseOnStartup(boolean loadDatabaseOnStartup) {
		this.loadDatabaseOnStartup = loadDatabaseOnStartup;
	}
	
	public boolean getStoreAllAkaTitles() {
		return storeAllAkaTitles;
	}

	public void setStoreAllAkaTitles(boolean storeAllAkaTitles) {
		this.storeAllAkaTitles = storeAllAkaTitles;
	}

	public boolean getIncludeAkaLanguageCodes() {
		return includeAkaLanguageCodes;
	}

	public void setIncludeAkaLanguageCodes(boolean includeAkaLanguageCodes) {
		this.includeAkaLanguageCodes = includeAkaLanguageCodes;
	}

	public String getTitleLanguageCode() {
		return titleLanguageCode;
	}

	public void setTitleLanguageCode(String titleLanguageCode) {
		this.titleLanguageCode = titleLanguageCode;
	}

	public boolean getUseLanguageSpecificTitle() {
		return useLanguageSpecificTitle;
	}

	public void setUseLanguageSpecificTitle(boolean useLanguageSpecificTitle) {
		this.useLanguageSpecificTitle = useLanguageSpecificTitle;
	}

	public boolean getSeenEditable() {
		return seenEditableInMainWindow;
	}

	public void setSeenEditable(boolean seenEditableInMainWindow) {
		this.seenEditableInMainWindow = seenEditableInMainWindow;
	}

	public int getMovieListRowHeight() {
		return movieListRowHeight;
	}

	public void setMovieListRowHeight(int movieListRowHeight) {
		this.movieListRowHeight = movieListRowHeight;
	}
	
	public boolean getEnableCtrlMouseRightClick() {
		return enableCtrlMouseRightClick;
	}

	public void setEnableCtrlMouseRightClick(boolean enableCtrlMouseRightClick) {
		this.enableCtrlMouseRightClick = enableCtrlMouseRightClick;
	}

	public ImportMode getLastDialogImportType() {
		return lastDialogImportType;
	}

	public void setLastDialogImportType(ImportMode lastDialogImportType) {
		this.lastDialogImportType = lastDialogImportType;
	}
		
	public ExportMode getLastDialogExportType() {
		return lastDialogExportType;
	}

	public void setLastDialogExportType(ExportMode lastDialogExportType) {
		this.lastDialogExportType = lastDialogExportType;
	}
	
	
	// Import
	
	// XML 
	public String getImportXMLFilePath() {
		return importXMLFilePath;
	}

	public void setImportXMLFilePath(String importXMLFilePath) {
		this.importXMLFilePath = importXMLFilePath;
	}
	
	// Txt
	public String getImportTextFilePath() {
		return importTextFilePath;
	}

	public void setImportTextFilePath(String importTextFilePath) {
		this.importTextFilePath = importTextFilePath;
	}

	// Excel
	public String getImportExcelFilePath() {
		return importExcelFilePath;
	}

	public void setImportExcelFilePath(String importExcelFilePath) {
		this.importExcelFilePath = importExcelFilePath;
	}

	
	// CSV
	public String getImportCSVFilePath() {
		return importCSVFilePath;
	}

	public void setImportCSVFilePath(String importCSVFilePath) {
		this.importCSVFilePath = importCSVFilePath;
	}

	public String getImportCSVseparator() {
		return importCSVseparator;
	}

	public void setImportCSVseparator(String importCSVseparator) {
		this.importCSVseparator = importCSVseparator;
	}
	
	
	// Export 
	
	// Excel
	public String getExportExcelFilePath() {
		return exportExcelFilePath;
	}

	public void setExportExcelFilePath(String exportExcelFilePath) {
		this.exportExcelFilePath =  exportExcelFilePath;
	}
	
	// CSV
	public String getExportCSVFilePath() {
		return exportCSVFilePath;
	}

	public void setExportCSVFilePath(String exportCSVFilePath) {
		this.exportCSVFilePath = exportCSVFilePath;
	}
	
	public String getExportCSVseparator() {
		return exportCSVseparator;
	}

	public void setExportCSVseparator(String exportCSVseparator) {
		this.exportCSVseparator = exportCSVseparator;
	}
		
	// XML database 
	public String getExportXMLDbFilePath() {
		return exportXMLDbFilePath;
	}
	
	public void setExportXMLDbFilePath(String value) {
		exportXMLDbFilePath = value;
	}
	
	// XML 
	public String getExportXMLFilePath() {
		return exportXMLFilePath;
	}
	
	public void setExportXMLFilePath(String value) {
		exportXMLFilePath = value;
	}

	// Text
	
	private String getExportTextFilePath() {
		return exportTextFilePath;
	}
	
	private void setExportTextFilePath(String value) {
		exportTextFilePath = value;
	}


	public String getCastorMappingFile() {
		return castorMappingFile;
	}
	
	public boolean getImportIMDbInfoEnabled() {
		return importIMDbInfoEnabled;
	}
	
	public void setImportIMDbInfoEnabled(boolean enabled) {
		importIMDbInfoEnabled = enabled;
	}
	
	public ImdbImportOption getImportIMDbSelectOption() {
		return importIMDbSelectOption;
	}
	
	public void setImportIMDbSelectOption(ImdbImportOption option) {
		importIMDbSelectOption = option;
	}
	
	
	/********************************
	        Mulitadd options
	 ********************************/
	
	public boolean getMultiAddListEnabled() {
		return multiAddListEnabled;
	}

	public void setMultiAddListEnabled(boolean multiAddListEnabled) {
		this.multiAddListEnabled = multiAddListEnabled;
	}

	public String getMultiAddList() {
		return multipleAddList;
	}

	public void setMultiAddList(String multipleAddList) {
		this.multipleAddList = multipleAddList;
	}

	public String getMultiAddDirectoryPath() {
		return multiAddDirectoryPath;
	}

	public void setMultiAddDirectoryPath(String p) {
		multiAddDirectoryPath = p;
	}

	public String getMultiAddRegexString() {
		return multiAddRegexString;
	}

	public void setMultiAddRegexString(String e) {
		multiAddRegexString = e;
	}
		
	public boolean getMultiAddRegexCaseSensitive() {
		return multiAddRegexCaseSensitive;
	}

	public void setMultiAddRegexCaseSensitive(boolean b) {
		multiAddRegexCaseSensitive = b;
	}
	
	public boolean getMultiAddRegexStringNegated() {
		return multiAddRegexStringNegated;
	}

	public void setMultiAddRegexStringNegated(boolean b) {
		multiAddRegexStringNegated = b;
	}
	
	public boolean getMultiAddRegexStringEnabled() {
		return multiAddRegexStringEnabled;
	}

	public void setMultiAddRegexStringEnabled(boolean b) {
		multiAddRegexStringEnabled = b;
	}

	public ArrayList<String> getMultiAddValidExtensions() {
		return multiAddValidExtensions;
	}

	public String getMultiAddValidExtensionsString() {
		String extStr = "";
		
		for (int i = 0; i < multiAddValidExtensions.size(); i++) {
			if (extStr.length() > 0)
				extStr += ", ";
			extStr += multiAddValidExtensions.get(i);
		}
		return extStr;
	}
	
	public void setMultiAddValidExtension(ArrayList<String> ext) {
		multiAddValidExtensions = ext;
	}
	
	public void addMultiAddValidExtension(String ext) {
		multiAddValidExtensions.add(ext);
	}
	
	public void addMultiAddValidExtensions(String extensions) {
		String [] ext = extensions.split(",\\s");
		
		for (int i = 0; i < ext.length; i++) {
			addMultiAddValidExtension(ext[i]);
		}
	}
	
	
	public String getMultiAddCustomExtensions() {
		return multiAddCustomExtensions;
	}

	public void setMultiAddCustomExtensions(String multiAddCustomExtensions) {
		this.multiAddCustomExtensions = multiAddCustomExtensions;
	}
	

	public ImdbImportOption getMultiAddSelectOption() {
		return multiAddSelectOption;
	}

	public void setMultiAddSelectOption(ImdbImportOption o) {
		multiAddSelectOption = o;
	}

	
	public boolean getMultiAddEnableExludeParantheses() {
		return multiAddEnableExludeParantheses;
	}

	public void setMultiAddEnableExludeParantheses(boolean val) {
		multiAddEnableExludeParantheses = val;
	}
	
	
	public boolean getMultiAddEnableExludeCDNotation() {
		return multiAddEnableExludeCDNotation;
	}

	public void setMultiAddEnableExludeCDNotation(boolean val) {
		multiAddEnableExludeCDNotation = val;
	}
	
	
	public boolean getMultiAddEnableExludeIntegers() {
		return multiAddEnableExludeIntegers;
	}

	public void setMultiAddEnableExludeIntegers(boolean val) {
		multiAddEnableExludeIntegers = val;
	}
	
	public boolean getMultiAddEnableExludeYear() {
		return multiAddEnableExludeYear;
	}

	public void setMultiAddEnableExludeYear(boolean val) {
		multiAddEnableExludeYear = val;
	}
	
	
	public boolean getMultiAddEnableExludeAllAfterMatchOnUserDefinedInfo() {
		return multiAddEnableExludeAllAfterMatchOnUserDefinedInfo;
	}

	public void setMultiAddEnableExludeAllAfterMatchOnUserDefinedInfo(boolean val) {
		multiAddEnableExludeAllAfterMatchOnUserDefinedInfo = val;
	}
	
	
	public boolean getMultiAddEnableSearchInSubdirectories() {
		return multiAddEnableSearchInSubdirectories;
	}

	public void setMultiAddEnableSearchInSubdirectories(boolean val) {
		multiAddEnableSearchInSubdirectories = val;
	}
		
	
	public boolean getMultiAddTitleOption() {
		return multiAddTitleOption;
	}

	public void setMultiAddTitleOption(boolean val) {
		multiAddTitleOption = val;
	}


	public boolean getMultiAddEnableExludeUserdefinedInfo() {
		return multiAddEnableExludeUserdefinedInfo;
	}
	
	public void setMultiAddEnableExludeUserdefinedInfo(boolean val) {
		multiAddEnableExludeUserdefinedInfo = val;
	}
	
	
	public boolean getMultiAddTitleOptionNoCd() {
		return multiAddTitleOptionNoCd;
	}
	
	public void setMultiAddTitleOptionNoCd(boolean val) {
		multiAddTitleOptionNoCd = val;
	}
	
	
	public String getMultiAddExcludeUserDefinedString() {
		return multiAddExcludeUserDefined;
	}
	
	public void setMultiAddExcludeUserDefinedString(String excludeString) {
		multiAddExcludeUserDefined = excludeString;
	}
		
	public boolean getMultiAddSearchNfoForImdb() {
		return multiAddAddSearchNfoForImdb;
	}
	
	public void setMultiAddSearchNfoForImdb(boolean val) {
		multiAddAddSearchNfoForImdb = val;
	}
	
	public boolean getMultiAddSkipHiddenDirectories() {
		return multiAddSkipHiddenDirectories;
	}
	
	public void setMultiAddSkipHiddenDirectories(boolean val) {
		multiAddSkipHiddenDirectories = val;
	}
	
	public boolean getMultiAddPrefixMovieTitle() {
		return multiAddPrefixMovieTitle;
	}
	
	public void setMultiAddPrefixMovieTitle(boolean val) {
		multiAddPrefixMovieTitle = val;
	}
	
	
	public boolean getMultiAddEnableAutomaticCombine() {
		return multiAddEnableAutomaticCombine;
	}
	
	public void setMultiAddEnableAutomaticCombine(boolean val) {
		multiAddEnableAutomaticCombine = val;
	}
	
	public boolean getMultiAddFilterOutDuplicates() {
		return multiAddFilterOutDuplicates;
	}
	
	public void setMultiAddFilterOutDuplicates(boolean val) {
		multiAddFilterOutDuplicates = val;
	}
	
	public boolean getMultiAddFilterOutDuplicatesByAbsolutePath() {
		return multiAddFilterOutDuplicatesByAbsolutePath;
	}
	
	public void setMultiAddFilterOutDuplicatesByAbsolutePath(boolean val) {
		multiAddFilterOutDuplicatesByAbsolutePath = val;
	}
			
	public void addMultiAddRootDevice(String filePath) {
				
		if (!multiaddRootDevices.contains(filePath))
			multiaddRootDevices.add(filePath);
	}
	
	public boolean removeMultiAddRootDevice(String filePath) {
		return multiaddRootDevices.remove(filePath);
	}
	
	public String [] getMultiAddRootDevices() {
		return multiaddRootDevices.toArray(new String[multiaddRootDevices.size()]);
	}
	
	public String getMultiAddRootDevicesAsString() {

		String devices = "";
		for (String device : multiaddRootDevices) {

			if (devices.length() > 0)
				devices += "|";

			devices += device;
		}
		return devices;
	}
	
	public void parseMultiAddRootDevices(String str) {
		
		if (str == null)
			return;
			
		String [] split = str.split("\\|");
				
		for (String elem : split) {				
			if (!elem.equals(""))
				addMultiAddRootDevice(elem);
		}		
	}

	
	
	public int getPlotCastMiscellaneousIndex() {
		return plotCastMiscellaneousIndex;
	}
	
	public void setPlotCastMiscellaneousIndex(int plotCastMiscellaneousIndex) {
		this.plotCastMiscellaneousIndex = plotCastMiscellaneousIndex;
	}
	
	
	


	public void setDisplayPlayButton(boolean displayPlayButton) {
		this.displayPlayButton = displayPlayButton;
	}

	public boolean getDisplayPlayButton() {
		return displayPlayButton;
	}

	public void setDisplayPrintButton(boolean displayPrintButton) {
		this.displayPrintButton = displayPrintButton;
	}

	public boolean getDisplayPrintButton() {
		return displayPrintButton;
	}

	public void setCheckForProgramUpdates(boolean check) {
		this.checkForProgramUpdates = check;
	}

	public boolean getCheckForProgramUpdates() {
		return checkForProgramUpdates;
	}	

	public void setHTMLViewDebugMode(boolean htmlViewDebugMode) {
		this.htmlViewDebugMode = htmlViewDebugMode;
	}

	public boolean getHTMLViewDebugMode() {
		return htmlViewDebugMode;
	}
	
	public void setUseRegularSeenIcon(boolean useRegularSeenIcon) {
		this.useRegularSeenIcon = useRegularSeenIcon;
	}

	public boolean getUseRegularSeenIcon() {
		return useRegularSeenIcon;
	}

	public Point getScreenLocation() {
		return screenLocation;
	}

	public void setScreenLocation(Point screenLocation) {
		this.screenLocation = screenLocation;
	}

	public void setMainSize(Dimension mainSize) {
		this.mainSize = mainSize;
	}

	public boolean getMainMaximized() {
		return mainMaximized;
	}

	public void setMainMaximized(boolean mainMaximized) {
		this.mainMaximized = mainMaximized;
	}

	public int getAddMovieWindowHeight() {
		return addMovieWindowHeight;
	}
	
	public void setAddMovieWindowHeight(int height) {
		addMovieWindowHeight = height;
	}
		
	public HttpSettings getHttpSettings() {
		return httpSettings;	
	}
	
	public String getProxyType() {
		return httpSettings.getProxyType();
	}

	public void setProxyType(String proxyType) {
		httpSettings.setProxyType(proxyType);
	}

	public String getProxyHost() {
		return httpSettings.getProxyHost();
	}

	public void setProxyHost(String proxyHost) {
		httpSettings.setProxyHost(proxyHost);
	}

	public String getProxyPort() {
		return httpSettings.getProxyPort();
	}

	public void setProxyPort(String proxyPort) {
		httpSettings.setProxyPort(proxyPort);
	}

	public String getProxyUser() {
		return httpSettings.getProxyUser();
	}

	public void setProxyUser(String proxyUser) {
		httpSettings.setProxyUser(proxyUser);
	}

	public String getProxyPassword() {
		return httpSettings.getProxyPassword();
	}

	public void setProxyPassword(String proxyPassword) {
		httpSettings.setProxyPassword(proxyPassword);
	}

	public boolean getProxyEnabled() {
		return httpSettings.getProxyEnabled();
	}

	public void setProxyEnabled(boolean proxyEnabled) {
		httpSettings.setProxyEnabled(proxyEnabled);
	}

	public boolean getProxyAuthenticationEnabled() {
		return httpSettings.getProxyAuthenticationEnabled();
	}

	public void setProxyAuthenticationEnabled(boolean proxtAuthenticationEnabled) {
		httpSettings.setProxyAuthenticationEnabled(proxtAuthenticationEnabled);
	}

	public void resetIMDbAuth() {
		httpSettings.setIMDbAuthenticationEnabled(false);
		httpSettings.setIMDbAuthenticationUser("");
		httpSettings.setIMDbAuthenticationPassword("");
	}
	
	public boolean getIMDbAuthenticationEnabled() {
		return httpSettings.getIMDbAuthenticationEnabled();
	}

	public void setIMDbAuthenticationEnabled(boolean IMDbAuthenticationEnabled) {
		httpSettings.setIMDbAuthenticationEnabled(IMDbAuthenticationEnabled);
	}
	
	
	public String getIMDbAuthenticationUser() {
		return httpSettings.getIMDbAuthenticationUser();
	}
	
	public void setIMDbAuthenticationUser(String IMDbAuthenticationUser) {
		httpSettings.setIMDbAuthenticationUser(IMDbAuthenticationUser);
	}
	
	public String getIMDbAuthenticationPassword() {
		return httpSettings.getIMDbAuthenticationPassword();
	}

	public void setIMDbAuthenticationPassword(String IMDbAuthenticationPassword) {
		httpSettings.setIMDbAuthenticationPassword(IMDbAuthenticationPassword);
	}
	
	public boolean getAutoMoveThe() {
		return httpSettings.getAutoMoveThe();
	}

	public void setAutoMoveThe(boolean autoMoveThe) {
		httpSettings.setAutoMoveThe(autoMoveThe);
	}

	public boolean getAutoMoveAnAndA() {
		return httpSettings.getAutoMoveAnAndA();
	}

	public void setAutoMoveAnAndA(boolean autoMoveAnAndA) {
		httpSettings.setAutoMoveAnAndA(autoMoveAnAndA);
	}
	
	public boolean getRemoveQuotesOnSeriesTitle() {
		return httpSettings.getRemoveQuotesOnSeriesTitles();
	}

	public void setRemoveQuotesOnSeriesTitle(boolean removeQuotesOnSeriesTitle) {
		httpSettings.setRemoveQuotesOnSeriesTitles(removeQuotesOnSeriesTitle);
	}
	
	public void setUseRelativeDatabasePath(int useRelativeDatabasePath) {
		this.useRelativeDatabasePath = useRelativeDatabasePath;
	}

	public int getUseRelativeDatabasePath() {
		return useRelativeDatabasePath;
	}

	public void setUseRelativeCoversPath(int useRelativeCoversPath) {
		this.useRelativeCoversPath = useRelativeCoversPath;
	}

	public int getUseRelativeCoversPath() {
		return useRelativeCoversPath;
	}

	public void setUseRelativeQueriesPath(int useRelativeQueriesPath) {
		this.useRelativeQueriesPath = useRelativeQueriesPath;
	}

	public int getUseRelativeQueriesPath() {
		return useRelativeQueriesPath;
	}

	public void setDatabasePathPermanent(boolean databasePathPermanent) {
		this.databasePathPermanent = databasePathPermanent;
	}

	public boolean getDatabasePathPermanent() {
		return databasePathPermanent;
	}

	public void setUseDisplayQueriesInTree(boolean useDisplayQueriesInTree) {
		this.useDisplayQueriesInTree = useDisplayQueriesInTree;
	}

	public boolean getUseDisplayQueriesInTree() {
		return useDisplayQueriesInTree;
	}

	public String getHTMLExportType() {
		return htmlExportType;
	}

	public void setHTMLExportType(String exportType) {
		this.htmlExportType = exportType;
	}

	public String getNoCoverFilename() {

		switch (noCoverType) {
		case Puma: return "nocover_puma.png";
		case Jaguar: return "nocover_jaguar.png";
		case Tiger: return "nocover_tiger.png";
		}		

		return "nocover_puma.png";
	}

	public NoCoverType getNoCoverType() {
		return noCoverType;
	}

	public void setNoCoverType(NoCoverType noCoverType) {
		this.noCoverType = noCoverType;
	}
	
	
	public HashMap<String, String> getSearchAlias() {
		return searchAlias;
	}
	
	public HashMap<String, AdditionalInfoFieldDefaultValues> getAdditionalInfoDefaultValues() {
		return additionalInfoDefaultValues;
	}
	
	public ArrayList<String> getMainFilterSearchValues() {
		return mainFilterSearchValues;
	}
	
	public void addMainFilterSearchValue(String val) {
		mainFilterSearchValues.add(val);
	}
	
	public void removeMainFilterSearchValue(String val) {
		mainFilterSearchValues.remove(val);
	}
	
	/**
	 * @return ArrayList containing the lists that are currently selected
	 */
	public ArrayList <String> getCurrentLists() {
		return currentLists;
	}

	public boolean addToCurrentLists(String listName) {
		
		if (listName == null || listName.trim().equals("") ||
				currentLists.contains(listName))
			return false;
		
		currentLists.add(listName);
		return true;
	}
		
	
	public void setCurrentLists(ArrayList<String> currentLists) {
		this.currentLists = currentLists;
	}

	
	public void setShowUnlistedEntries(boolean val) {
		showUnlistedEntries = val;
	}
	
	
	public boolean getShowUnlistedEntries() {
		return showUnlistedEntries;
	}
	
	
	public boolean getLoadLastUsedListAtStartup() {
		return loadLastUsedListAtStartup;
	}

	public void setLoadLastUsedListAtStartup(boolean loadLastUsedListAtStartup) {
		this.loadLastUsedListAtStartup = loadLastUsedListAtStartup;
	}

	public boolean getAddNewMoviesToCurrentLists() {
		return addNewMoviesToCurrentLists;
	}

	public void setAddNewMoviesToCurrentLists(boolean addNewMoviesToCurrentLists) {
		this.addNewMoviesToCurrentLists = addNewMoviesToCurrentLists;
	}
	
	public boolean getUseJTreeIcons() {
		return useJTreeIcons;
	}

	public void setUseJTreeIcons(boolean useJTreeIcons) {
		this.useJTreeIcons = useJTreeIcons;
	}

	public boolean getUseJTreeCovers() {
		return useJTreeCovers;
	}

	public void setUseJTreeCovers(boolean useJTreeCovers) {
		this.useJTreeCovers = useJTreeCovers;
	}

	public int getPreserveCoverAspectRatio() {
		return preserveCoverAspectRatioSetting;
	}

	public void setPreserveCoverAspectRatio(int preserveCoverAspectRatioSetting) {
		this.preserveCoverAspectRatioSetting = preserveCoverAspectRatioSetting;
	}

	public int getLastPreferencesTabIndex() {
		return lastPreferencesTabIndex;
	}

	public void setLastPreferencesTabIndex(int lastPreferencesTabIndex) {
		this.lastPreferencesTabIndex = lastPreferencesTabIndex;
	}

	public int getLastMovieInfoTabIndex() {
		return lastMovieInfoTabIndex;
	}

	public void setLastMovieInfoTabIndex(int lastMovieInfoTabIndex) {
		this.lastMovieInfoTabIndex = lastMovieInfoTabIndex;
	}
	
	
	public String getMediaPlayerPath() {
		return this.playerPath;
	}

	public void setMediaPlayerPath(String playerPath){
		this.playerPath = playerPath;
	}

	public String getMediaPlayerCmdArgument() {
		return mediaPlayerCmdArgument;
	}

	public void setMediaPlayerCmdArgument(String cmdArg){
		mediaPlayerCmdArgument = cmdArg;
	}
	
	public String getBrowserPath() {
		return browserPath;
	}

	public void setBrowserPath(String browserPath){
		this.browserPath = browserPath;
	}
	
	public boolean getUseDefaultWindowsPlayer() {
		return useDefaultWindowsPlayer;
	}

	public void setUseDefaultWindowsPlayer(boolean useDefaultWindowsPlayer) {
		this.useDefaultWindowsPlayer = useDefaultWindowsPlayer;
	}

	public boolean getExecuteExternalPlayCommand() {
		return executeExternalPlayCommand;
	}

	public void setExecuteExternalPlayCommand(boolean executeExternalPlayCommand) {
		this.executeExternalPlayCommand = executeExternalPlayCommand;
	}
	
	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	public String getSystemWebBrowser() {
		return systemWebBrowser;
	}

	public void setSystemWebBrowser(String systemWebBrowser) {
		this.systemWebBrowser = systemWebBrowser;
	}

	public String getDatabaseBackupLaunchCount() {
		return databaseBackupLaunchCount;
	}

	public void setDatabaseBackupLaunchCount(String backupLaunchCount) {
		this.databaseBackupLaunchCount = backupLaunchCount;
	}
	
	
	public String getDatabaseBackupEveryLaunch() {
		return databaseBackupEveryLaunch;
	}

	public void setDatabaseBackupEveryLaunch(String backupEveryLaunch) {
		this.databaseBackupEveryLaunch = backupEveryLaunch;
	}
	
	
	public String getDatabaseBackupDeleteOldest() {
		return databaseBackupDeleteOldest;
	}

	public void setDatabaseBackupDeleteOldest(String backupDeleteOldest) {
		this.databaseBackupDeleteOldest = backupDeleteOldest;
	}
	
    
    public String getDatabaseBackupDirectory() {
		return databaseBackupDirectory;
	}

	public void setDatabaseBackupDirectory(String backupDirectory) {
		this.databaseBackupDirectory = backupDirectory;
	}

	public boolean getDatabaseBackupWarnInvalidDir() {
		return databaseBackupWarnInvalidDir;
	}

	public void setDatabaseBackupWarnInvalidDir(boolean warn) {
		this.databaseBackupWarnInvalidDir = warn;
	}

	// DialogMovieManager window values
	
	public int getMainWindowSliderPosition() {
		return mainWindowSliderPosition;
	}
	
	public void setMainWindowSliderPosition(int value) {
		mainWindowSliderPosition = value;
	}
	
	public int getMainWindowLastSliderPosition() {
		return mainWindowLastSliderPosition;
	}
	
	public void setMainWindowLastSliderPosition(int value) {
		mainWindowLastSliderPosition = value;
	}
	
	public int getMovieInfoSliderPosition() {
		return movieInfoSliderPosition;
	}
	
	public void setMovieInfoSliderPosition(int value) {
		movieInfoSliderPosition = value;
	}
	
	public int getMovieInfoLastSliderPosition() {
		return movieInfoLastSliderPosition;
	}
	
	public void setMovieInfoLastSliderPosition(int value) {
		movieInfoLastSliderPosition = value;
	}
	
	public int getAdditionalInfoNotesSliderPosition() {
		return additionalInfoNotesSliderPosition;
	}
	
	public void setAdditionalInfoNotesSliderPosition(int value) {
		additionalInfoNotesSliderPosition = value;
	}
	
	public int getAdditionalInfoNotesLastSliderPosition() {
		return additionalInfoNotesLastSliderPosition;
	}
	
	public void setAdditionalInfoNotesLastSliderPosition(int value) {
		additionalInfoNotesLastSliderPosition = value;
	}
	
		
	// DialogMultiAdd window values
	
	public int getMultiAddMainSliderPosition() {
		return multiAddMainSliderPosition;
	}
	
	public void setMultiAddMainSliderPosition(int value) {
		multiAddMainSliderPosition = value;
	}
	
	public int getMultiAddFileSliderPosition() {
		return multiAddFileSliderPosition;
	}
	
	public void setMultiAddFileSliderPosition(int value) {
		multiAddFileSliderPosition = value;
	}
		
	public int getMultiAddTabIndex() {
		return multiAddTabIndex;
	}
	
	public void setMultiAddTabIndex(int value) {
		multiAddTabIndex = value;
	}
		
	public Dimension getMultiAddWindowSize() {
		return multiAddWindowSize;
	}
	
	public void setMultiAddWindowSize(Dimension value) {
				
		if (value.width < defaultMultiAddWindowSize.width || 
				value.height < defaultMultiAddWindowSize.height) {
			log.debug("setMultiAddWindowSize ignore too small window:" + value + " - Using default value instead:" + defaultMultiAddWindowSize);
			multiAddWindowSize = new Dimension(defaultMultiAddWindowSize);
			return;
		}
		multiAddWindowSize = value;
	}
	
	public Dimension getMultiAddIMDbDialogWindowSize() {
		return multiAddIMDbDialogWindowSize;
	}
	
	public void setMultiAddIMDbDialogWindowSize(Dimension value) {
		multiAddIMDbDialogWindowSize = value;
	}
	
		
	boolean getBooleanValue(String key, HashMap<String, String> config) {
		return getBooleanValue(key, config, false);
	}
	
	boolean getBooleanValue(String key, HashMap<String, String> config, boolean defaultValue) {
		
		String value = (String) config.get(key);
		
		try {
			
			if (value != null) {
				return new Boolean(value).booleanValue();
			}
		} catch (Exception e) {
			log.warn("Invalid boolean value:" + value);
			e.printStackTrace();
		}
		return defaultValue;
	}
			
	
	String getStringValue(String key, HashMap<String, String> config) {
		return getStringValue(key, config, null);
	}
	
	String getStringValue(String key, HashMap<String, String> config, String defaultValue) {
		
		String value = (String) config.get(key);
		
		try {
			
			if (value != null) {
				return value;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}
	
	
	int getIntValue(String key, HashMap<String, String> config) {
		return getIntValue(key, config, 0);
	}
	
	int getIntValue(String key, HashMap<String, String> config, int defaultValue) {
		
		String value = (String) config.get(key);
		
		try {
			
			if (value != null) {
				return Integer.parseInt(value);
			}
		} catch (Exception e) {
			log.warn("Invalid int value:" + value);
			e.printStackTrace();
		}
		return defaultValue;
	}
	
	double getDoubleValue(String key, HashMap<String, String> config) {
		return getDoubleValue(key, config, 0);
	}
	
	double getDoubleValue(String key, HashMap<String, String> config, double defaultValue) {
		
		String value = (String) config.get(key);
		
		try {
			
			if (value != null) {
				return new Double(value);
			}
		} catch (Exception e) {
			log.warn("Invalid int value:" + value);
			e.printStackTrace();
		}
		return defaultValue;
	}
	
	
	
	/**
	 * Loads info from the config file...
	 **/
	 protected void loadConfig() {
		 		 
		try {

			URL url = SysUtil.getConfigURL();
			
			if (url == null)
				return;
		
			log.debug("Loading configuration data from " + url.toString());
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

			// search Alias And Additional Info Default Values
			ArrayList<String> searchAliasList = new ArrayList<String>();
			ArrayList<String> additionalFieldDefaults = new ArrayList<String>();
			ArrayList<String> mainFilterDefaults = new ArrayList<String>();
			
			HashMap<String, String> config = new HashMap<String, String>();
						
			String tmp, key, value;

			while ((tmp = reader.readLine()) != null) {

				try {
					if (tmp.indexOf(":") == -1)
						continue;
					
					key = tmp.substring(0, tmp.indexOf(":") + 1);
					value = tmp.substring(tmp.indexOf(":") + 1, tmp.length());

					if ("AdditionalInfoDefaultValues:".equals(key))
						additionalFieldDefaults.add(value);
					else if ("Search Alias:".equals(key))
						searchAliasList.add(value);
					else if ("mainFilterSearchValues:".equals(key))
						mainFilterDefaults.add(value);
					else
						config.put(key, value);

				} catch (Exception e) {
					log.warn("Error in Config file:" + e.getMessage());
				}
			}


			setDatabasePath(getStringValue("Database:", config));
			
			value = (String) config.get("useRelativeDatabasePath:");

			if (value != null) {

				if (value.equals("true"))
					setUseRelativeDatabasePath(2);
				else if (value.equals("false"))
					setUseRelativeDatabasePath(0);
				else
					setUseRelativeDatabasePath(Integer.parseInt(value));
			}

			
			value = (String) config.get("useRelativeCoverPath:");

			if (value != null) {
				
				if (value.equals("true"))
					setUseRelativeCoversPath(2);
				else if (value.equals("false"))
					setUseRelativeCoversPath(0);
				else
					setUseRelativeCoversPath(Integer.parseInt(value));
			}


			value = (String) config.get("useRelativeQueriesPath:");

			if (value != null) {

				if (value.equals("true"))
					setUseRelativeQueriesPath(2);
				else if (value.equals("false"))
					setUseRelativeQueriesPath(0);
				else
					setUseRelativeQueriesPath(Integer.parseInt(value));
			}

			
			setStoreCoversLocally(getBooleanValue("storeCoversLocally:", config, getStoreCoversLocally()));
			setMySQLSocketTimeoutEnabled(getBooleanValue("mySQLSocketTimeoutEnabled:", config, getMySQLSocketTimeoutEnabled()));
			setLoadDatabaseOnStartup(getBooleanValue("loadDatabaseOnStartup:", config, getLoadDatabaseOnStartup()));
			setDatabasePathPermanent(getBooleanValue("databasePathPermanent:", config, getDatabasePathPermanent()));
			setUseDisplayQueriesInTree(getBooleanValue("useDisplayQueriesInTree:", config, getUseDisplayQueriesInTree()));

			LookAndFeelManager lafManager =  MovieManager.getLookAndFeelManager();
			
			lafManager.setCustomLookAndFeel(getStringValue("lookAndFeel:", config, lafManager.getCustomLookAndFeel()));
			lafManager.setSkinlfThemePack(getStringValue("skinlfTheme:", config, lafManager.getSkinlfThemePack()));
			lafManager.setSubstanceSkin(getStringValue("substanceSkin:", config, lafManager.getSubstanceSkin()));
			lafManager.setNimRODTheme(getStringValue("nimRODTheme:", config, lafManager.getNimRODTheme()));
						
			value = (String) config.get("lookAndFeelType:");

			if (value != null) {

				LookAndFeelType laf = null;

				// Old setting using ints
				try {
					int val = Integer.parseInt(value);
					laf = val == 1 ? LookAndFeelType.SkinlfLaF : LookAndFeelType.CustomLaF;
				} catch (Exception e) {
				}

				try {
					laf  = LookAndFeelType.valueOf(value);
				} catch (Exception e) {
				}

				if (laf != null)
					MovieManager.getLookAndFeelManager().setLookAndFeelType(laf);
			}

			setUseRegularSeenIcon(getBooleanValue("useRegularSeenIcon:", config, getUseRegularSeenIcon()));
			lafManager.setDefaultLookAndFeelDecorated(getBooleanValue("defaultLookAndFeelDecorated:", config, lafManager.getDefaultLookAndFeelDecorated()));
			setPlotCastMiscellaneousIndex(getIntValue("plotCastMiscellaneousIndex:", config, getPlotCastMiscellaneousIndex()));
									
			htmlTemplateHandler.setHTMLTemplateName(getStringValue("HTMLTemplateName:", config));
			htmlTemplateHandler.setHTMLTemplateStyleName(getStringValue("HTMLTemplateStyleName:", config));
			setLocale(getStringValue("locale:", config, getLocale()));
			setFilterCategory(getStringValue("filterOption:", config, getFilterCategory()));
			setSortOption(getStringValue("sortOption:", config, getSortOption()));
			setFilterSeen(getIntValue("filterSeen:", config, getFilterSeen()));
			setRatingOption(getIntValue("ratingOption:", config, getRatingOption()));
			setRatingValue(getDoubleValue("ratingValue:", config, getRatingValue()));
			setDateOption(getIntValue("dateOption:", config, getDateOption()));
			setDateValue(getStringValue("dateValue:", config));
			
			setSeenEditable(getBooleanValue("seenEditableInMainWindow:", config));

			value = (String) config.get("multiAddDirectoryPath:");

			if (value != null) {
				if (!value.equals("null"))
					setMultiAddDirectoryPath(value);
			}


			value = (String) config.get("multiAddRegexString:");

			if (value != null) {
				if (!value.equals("null"))
					setMultiAddRegexString(value);
			}

			setMultiAddRegexStringEnabled(getBooleanValue("multiAddRegexStringEnabled:", config));
			setMultiAddRegexStringNegated(getBooleanValue("multiAddRegexStringNegated:", config));
			setMultiAddRegexCaseSensitive(getBooleanValue("multiAddRegexCaseSensitive:", config));
						
			addMultiAddValidExtensions(getStringValue("multiAddValidExtensions:", config));
			setMultiAddCustomExtensions(getStringValue("multiAddCustomExtensions:", config));
			
			value = (String) config.get("multiAddSelectOption:");

			if (value != null) {
				try {
					setMultiAddSelectOption(ImdbImportOption.valueOf(value));
				} catch (Exception e) {
					log.warn("Exception:" + e.getMessage());
				}
			}

			
			setMultiAddEnableExludeParantheses(getBooleanValue("multiAddEnableExludeParantheses:", config, getMultiAddEnableExludeParantheses()));
			setMultiAddEnableExludeCDNotation(getBooleanValue("multiAddEnableExludeCDNotation:", config, getMultiAddEnableExludeCDNotation()));
			setMultiAddEnableExludeIntegers(getBooleanValue("multiAddEnableExludeIntegers:", config, getMultiAddEnableExludeIntegers()));
			setMultiAddEnableExludeYear(getBooleanValue("multiAddEnableExludeYear:", config, getMultiAddEnableExludeYear()));
			setMultiAddEnableExludeUserdefinedInfo(getBooleanValue("multiAddEnableExludeUserdefinedInfo:", config, getMultiAddEnableExludeUserdefinedInfo()));
			setMultiAddPrefixMovieTitle(getBooleanValue("multiAddPrefixMovieTitle:", config, getMultiAddPrefixMovieTitle()));
			setMultiAddEnableAutomaticCombine(getBooleanValue("multiAddEnableAutomaticCombine:", config, getMultiAddEnableAutomaticCombine()));
			setMultiAddFilterOutDuplicates(getBooleanValue("multiAddFilterOutDuplicates:", config, getMultiAddFilterOutDuplicates()));
			setMultiAddFilterOutDuplicatesByAbsolutePath(getBooleanValue("multiAddFilterOutDuplicatesByAbsolutePath:", config, getMultiAddFilterOutDuplicatesByAbsolutePath()));
						
			setMultiAddTitleOptionNoCd(getBooleanValue("multiAddTitleOptionNoCd:", config, getMultiAddTitleOptionNoCd()));
			setMultiAddSearchNfoForImdb(getBooleanValue("multiAddSearchNfoForImdb:", config, getMultiAddSearchNfoForImdb()));
			setMultiAddSkipHiddenDirectories(getBooleanValue("multiAddSkipHiddenDirectories:", config, getMultiAddSkipHiddenDirectories()));
			setMultiAddEnableExludeAllAfterMatchOnUserDefinedInfo(getBooleanValue("multiAddEnableExludeAllAfterMatchOnUserDefinedInfo:", config, getMultiAddEnableExludeAllAfterMatchOnUserDefinedInfo()));
			setMultiAddEnableSearchInSubdirectories(getBooleanValue("multiAddEnableSearchInSubdirectories:", config, getMultiAddEnableSearchInSubdirectories()));
			setMultiAddTitleOption(getBooleanValue("multiAddTitleOption:", config, getMultiAddTitleOption()));
			
			setMultiAddExcludeUserDefinedString(getStringValue("multiAddExcludeUserDefinedString:", config, getMultiAddExcludeUserDefinedString()));
			
			parseMultiAddRootDevices(getStringValue("multiaddRootDevices:", config));
			
			setProxyEnabled(getBooleanValue("proxyEnabled:", config, getProxyEnabled()));
			setProxyType(getStringValue("proxyType:", config, getProxyType()));
			setProxyAuthenticationEnabled(getBooleanValue("authenticationEnabled:", config, getProxyAuthenticationEnabled()));
			setProxyHost(getStringValue("proxyHost:", config));
			setProxyPort(getStringValue("proxyPort:", config));
			setProxyUser(getStringValue("proxyUser:", config));
			setProxyPassword(getStringValue("proxyPassword:", config));
						
			setIMDbAuthenticationEnabled(getBooleanValue("IMDbAuthenticationEnabled:", config, getIMDbAuthenticationEnabled()));
			setIMDbAuthenticationUser(getStringValue("IMDbAuthenticationUser:", config));
			setIMDbAuthenticationPassword(getStringValue("IMDbAuthenticationPassword:", config));
							
			value = (String) config.get("lastFileDir:");

			if (value != null) {
				File lastFileDirctory = new File(value);
				setLastFileDir(lastFileDirctory);
			}

			value = (String) config.get("lastDVDDir:");

			if (value != null) {
				File lastDVDDirctory = new File(value);
				setLastDVDDir(lastDVDDirctory);
			}


			value = (String) config.get("lastDatabaseDir:");

			if (value != null) {
				File lastDatabaseDirctory = new File(value);
				if (lastDatabaseDirctory.exists())
					setLastDatabaseDir(lastDatabaseDirctory);
			}


			value = (String) config.get("lastMiscDir:");

			if (value != null) {
				File lastMiscDirctory = new File(value);
				if (lastMiscDirctory.exists())
					setLastMiscDir(lastMiscDirctory);
			}


			setLastFileFilterUsed(getStringValue("lastFileFilterMovieInfoUsed:", config, getLastFileFilterUsed()));
			setHTMLExportType(getStringValue("htmlExportType:", config, getHTMLExportType()));

			setMainWindowSliderPosition(getIntValue("mainWindowSliderPosition:", config, getMainWindowSliderPosition()));
			setMainWindowLastSliderPosition(getIntValue("mainWindowLastSliderPosition:", config, getMainWindowLastSliderPosition()));
			setMovieInfoSliderPosition(getIntValue("movieInfoSliderPosition:", config, getMovieInfoSliderPosition()));
			
			setMovieInfoLastSliderPosition(getIntValue("movieInfoLastSliderPosition:", config, getMovieInfoLastSliderPosition()));
			setAdditionalInfoNotesSliderPosition(getIntValue("addionalInfoNotesSliderPosition:", config, getAdditionalInfoNotesSliderPosition()));
			setAdditionalInfoNotesLastSliderPosition(getIntValue("addionalInfoNotesLastSliderPosition:", config, getAdditionalInfoNotesLastSliderPosition()));
				
			value = (String) config.get("mainWidth:");
			int mainWidth = 0;

			if (value != null) {
				mainWidth = Integer.parseInt(value);
			}

			value = (String) config.get("mainHeight:");
			int mainHeight = 0;

			if (value != null) {
				mainHeight = Integer.parseInt(value);
			}

			mainSize = new Dimension(mainWidth, mainHeight);


			value = (String) config.get("screenLocationX:");
			int screenLocationX = 0;

			if (value != null) {
				screenLocationX = Integer.parseInt(value);
			}


			value = (String) config.get("screenLocationY:");
			int screenLocationY = 0;

			if (value != null) {
				screenLocationY = Integer.parseInt(value);
			}

			if (screenLocationX > 0 && screenLocationY > 0)
				screenLocation = new Point(screenLocationX, screenLocationY);


			setMainMaximized(getBooleanValue("mainMaximized:", config, getMainMaximized()));
			
			setAddMovieWindowHeight(getIntValue("addMovieWindowHeight:", config, getAddMovieWindowHeight()));
			
			// MultiAdd window
			setMultiAddMainSliderPosition(getIntValue("multiAddMainSliderPosition:", config, getMultiAddMainSliderPosition()));
			setMultiAddFileSliderPosition(getIntValue("multiAddFileSliderPosition:", config, getMultiAddFileSliderPosition()));
			setMultiAddTabIndex(getIntValue("multiAddTabIndex:", config, getMultiAddTabIndex()));
			
			// Size of MultiAdd dialog
			int multiAddWidth = getIntValue("multiAddWindowWidth:", config, -1);
			int multiAddHeight = getIntValue("multiAddWindowHeight:", config, -1);
			
			if (multiAddHeight != -1 && multiAddWidth != -1)
				setMultiAddWindowSize(new Dimension(multiAddWidth, multiAddHeight));
			
			// Size of MultiAdd IMDb dialog
			multiAddWidth = getIntValue("multiAddIMDbDialogWindowWidth:", config, -1);
			multiAddHeight = getIntValue("multiAddIMDbDialogWindowHeight:", config, -1);
			
			if (multiAddHeight != -1 && multiAddWidth != -1)
				setMultiAddIMDbDialogWindowSize(new Dimension(multiAddWidth, multiAddHeight));
					
			
			// Misc options
			setShowUnlistedEntries(getBooleanValue("showUnlistedEntries:", config, getShowUnlistedEntries()));
			setLoadLastUsedListAtStartup(getBooleanValue("loadCurrentListAtStartup:", config, getLoadLastUsedListAtStartup()));
			setAddNewMoviesToCurrentLists(getBooleanValue("addNewMoviesToCurrentLists:", config, getAddNewMoviesToCurrentLists()));
						
			value = (String) config.get("currentLists:");

			if (value == null) {
			
				// Handle special case caused by the change from single to multiple lists
				value = (String) config.get("currentList:");
				
				if (value != null && value.trim().equals("")) {
					setShowUnlistedEntries(false);
				}
			}
			
			if (value != null) {
				
				String [] lists = value.split(";");
								
				for (int i = 0; i < lists.length; i++) {
					addToCurrentLists(lists[i]);
				}
			}
			

			setMultiAddList(getStringValue("multiAddList:", config, getMultiAddList()));
			setMultiAddListEnabled(getBooleanValue("multiAddListEnabled:", config, getMultiAddListEnabled()));
			
			value = getStringValue("lastDialogImportType:", config);
			
			if (ImportMode.isValidValue(value))
				setLastDialogImportType(ImportMode.valueOf(value));
			
			setImportTextFilePath(getStringValue("importTextfilePath:", config, getImportTextFilePath()));
			setImportExcelFilePath(getStringValue("importExcelfilePath:", config, getImportExcelFilePath()));
			setImportXMLFilePath(getStringValue("importXMLfilePath:", config, getImportXMLFilePath()));
			setImportCSVFilePath(getStringValue("importCSVfilePath:", config, getImportCSVFilePath()));
			setImportCSVseparator(getStringValue("importCSVseparator:", config, getImportCSVseparator()));
			
			setUseJTreeIcons(getBooleanValue("useJTreeIcons:", config, getUseJTreeIcons()));
			setUseJTreeCovers(getBooleanValue("useJTreeCovers:", config, getUseJTreeCovers()));
		
			value = (String) config.get("lastPreferencesTabIndex:");

			if (value != null) {
				if (Character.isDigit(value.charAt(0)))
					setLastPreferencesTabIndex(Character.digit(value.charAt(0), 10));
			}

			value = (String) config.get("lastMovieInfoTabIndex:");

			if (value != null) {
				if (Character.isDigit(value.charAt(0)))
					setLastMovieInfoTabIndex(Character.digit(value.charAt(0), 10));
			}
			
			setMovieListRowHeight(getIntValue("movieListRowHeight:", config, getMovieListRowHeight()));
			setEnableCtrlMouseRightClick(getBooleanValue("enableCtrlMouseRightClick:", config));

			value = (String) config.get("preserveCoverAspectRatioSetting:");

			if (value != null) {
				
				if (value.equals("0"))
					preserveCoverAspectRatioSetting = 0;
				else if (value.equals("1"))
					preserveCoverAspectRatioSetting = 1;
				else if (value.equals("2"))
					preserveCoverAspectRatioSetting = 2;
			}


			value = (String) config.get("noCoverType:");
			
			if (value != null) {
				try {
					setNoCoverType(NoCoverType.valueOf(value));
				} catch (IllegalArgumentException i) {
					if (value.equals("nocover_puma.png"))
						setNoCoverType(NoCoverType.Puma);
					else
						setNoCoverType(NoCoverType.Jaguar);
				}
			}

			setAutoMoveThe(getBooleanValue("autoMoveThe:", config));
			setAutoMoveAnAndA(getBooleanValue("autoMoveAnAndA:", config));
			setStoreAllAkaTitles(getBooleanValue("storeAllAkaTitles:", config));
			setIncludeAkaLanguageCodes(getBooleanValue("includeAkaLanguageCodes:", config));
			setUseLanguageSpecificTitle(getBooleanValue("useLanguageSpecificTitle:", config));
			setRemoveQuotesOnSeriesTitle(getBooleanValue("removeQuotesOnSeriesTitle:", config));
			
			setTitleLanguageCode(getStringValue("titleLanguageCode:", config, getTitleLanguageCode()));
			
			value = (String) config.get("useMediaInfoDLL:");

			if (value != null) {
				if (!value.equals("")) {
					
					if (StringUtil.isInteger(value))
						setUseMediaInfoDLL(MediaInfoOption.getValue(Integer.parseInt(value)));
					else
						setUseMediaInfoDLL(MediaInfoOption.valueOf(value));
				}
			}

			setMediaPlayerPath(getStringValue("playerPath:", config, getMediaPlayerPath()));
			setMediaPlayerCmdArgument(getStringValue("mediaPlayerCmdArgument:", config, getMediaPlayerCmdArgument()));
			setBrowserPath(getStringValue("browserPath:", config, getBrowserPath()));
			setUseDefaultWindowsPlayer(getBooleanValue("useDefaultWindowsPlayer:", config));
			setExecuteExternalPlayCommand(getBooleanValue("executeExternalPlayCommand:", config));
			setSystemWebBrowser(getStringValue("systemWebBrowser:", config, getSystemWebBrowser()));
			
			setDatabaseBackupEveryLaunch(getStringValue("databaseBackupEveryLaunch:", config, getDatabaseBackupEveryLaunch()));
			setDatabaseBackupDeleteOldest(getStringValue("databaseBackupDeleteOldest:", config, getDatabaseBackupDeleteOldest()));
			setDatabaseBackupLaunchCount(getStringValue("databaseBackupLaunchCount:", config, getDatabaseBackupLaunchCount()));
			setDatabaseBackupDirectory(getStringValue("databaseBackupDirectory:", config, getDatabaseBackupDirectory()));
			setDatabaseBackupWarnInvalidDir(getBooleanValue("databaseBackupWarnInvalidDir:", config, getDatabaseBackupWarnInvalidDir()));
					
			setDisplayPlayButton(getBooleanValue("displayPlayButton:", config, getDisplayPlayButton()));
			setDisplayPrintButton(getBooleanValue("displayPrintButton:", config));
			setCheckForProgramUpdates(getBooleanValue("checkForProgramUpdates:", config));
			setHTMLViewDebugMode(getBooleanValue("htmlViewDebugMode:", config));
									
			value = getStringValue("lastDialogExportType:", config);
			
			if (ExportMode.isValidValue(value))
				setLastDialogExportType(ExportMode.valueOf(value));
			
			value = getStringValue("lastDialogImportType:", config);
			
			if (ImportMode.isValidValue(value))
				setLastDialogImportType(ImportMode.valueOf(value));
			
			setImportTextFilePath(getStringValue("importTextfilePath:", config, getImportTextFilePath()));
			setImportExcelFilePath(getStringValue("importExcelfilePath:", config, getImportExcelFilePath()));
			setImportXMLFilePath(getStringValue("importXMLfilePath:", config, getImportXMLFilePath()));
			setImportCSVFilePath(getStringValue("importCSVfilePath:", config, getImportCSVFilePath()));
			setImportCSVseparator(getStringValue("importCSVseparator:", config, getImportCSVseparator()));
			setImportIMDbInfoEnabled(getBooleanValue("importIMDbInfoEnabled:", config));
			
			value = (String) config.get("importIMDbSelectOption:");

			if (value != null) {
				try {
					setImportIMDbSelectOption(ImdbImportOption.valueOf(value));
				} catch (Exception e) {
					log.warn("Exception:" + e.getMessage());
				}
			}

			setExportTextFilePath(getStringValue("exportTextfilePath:", config, getExportTextFilePath()));
			setExportExcelFilePath(getStringValue("exportExcelfilePath:", config, getExportExcelFilePath()));
			setExportXMLDbFilePath(getStringValue("exportXMLDbfilePath:", config, getExportXMLDbFilePath()));
			setExportXMLFilePath(getStringValue("exportXMLfilePath:", config, getExportXMLFilePath()));
			setExportCSVFilePath(getStringValue("exportCSVfilePath:", config, getExportCSVFilePath()));
			
			value = (String) config.get("exportCSVseparator:");

			if (value != null && !value.trim().equals("")) {
				setExportCSVseparator(value);
			}
			
			
			// Default values stored for the additional info fields
			
			AdditionalInfoFieldDefaultValues defaultValue;
			StringTokenizer tokenizer;
			String name = "";
			String token;

			for (int i = 0; i < additionalFieldDefaults.size(); i++) {
			
				tmp = (String) additionalFieldDefaults.get(i);
				name = tmp.substring(0, tmp.indexOf(":"));
				
				defaultValue = new AdditionalInfoFieldDefaultValues(name);

				tokenizer = new StringTokenizer(tmp.substring(tmp.indexOf(":") + 1, tmp.length()), "|");

				while (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					defaultValue.addValue(token);					
				}
				additionalInfoDefaultValues.put(name, defaultValue);
			}

			String tableAndColumn;
			String alias;
			
			for (int i = 0; i < searchAliasList.size(); i++) {

				tmp = (String) searchAliasList.get(i);
				tableAndColumn = tmp.substring(0, tmp.indexOf("="));
				alias = tmp.substring(tmp.indexOf("=") + 1, tmp.length());
				
				if (!alias.equals("") && !tableAndColumn.equals(""))
					searchAlias.put(tableAndColumn, alias);
			}
			
			// Setting main filter default values
			mainFilterSearchValues = mainFilterDefaults;
			
		} catch (Exception e) {
			log.warn("Error occured when loadig config file:" + e.getMessage(), e);
		}
	 }

	
	 public void appendToConfig(String key, int value, StringBuffer settings) {
		 appendToConfig(key, new Integer(value).toString(), settings);
	 }

	 public void appendToConfig(String key, double value, StringBuffer settings) {
		 appendToConfig(key, new Double(value).toString(), settings);
	 }

	 public void appendToConfig(String key, boolean value, StringBuffer settings) {
		 appendToConfig(key, new Boolean(value).toString(), settings);
	 }

	 public void appendToConfig(String key, String value, StringBuffer settings) {

		 if (value != null) {
			 settings.append(key);
			 settings.append(value);
			 settings.append(SysUtil.getLineSeparator());
		 }
	 }
	
	
	public void saveConfig() throws Exception {

		 StringBuffer settings = new StringBuffer(1500);
		 Database database = MovieManager.getIt().getDatabase();

		 /* Absort if Applet */
		 if (MovieManager.isApplet())
			 return;

		 String dbPath = "";

		 /* Verifies if the database is initialized... */
		 if (database == null ||  (database != null && getDatabasePathPermanent())) {
			 dbPath = getDatabasePath(false);
		 }
		 else {

			 String databaseType = database.getDatabaseType();
			 dbPath = getDatabasePath(true);

			 // Relative to program location
			 if (getUseRelativeDatabasePath() == 2) {
				 if (dbPath.indexOf(SysUtil.getUserDir()) != -1)
					 dbPath = databaseType + ">" + dbPath.substring(SysUtil.getUserDir().length(), dbPath.length());

			 }
			 else
				 dbPath = databaseType + ">" + dbPath;
		 }

		 if (dbPath != null && !dbPath.equals("")) {
			 appendToConfig("Database:", dbPath, settings);
		 }

		 appendToConfig("useRelativeDatabasePath:", getUseRelativeDatabasePath(), settings);
		 appendToConfig("storeCoversLocally:", getStoreCoversLocally(), settings);
		 appendToConfig("mySQLSocketTimeoutEnabled:", getMySQLSocketTimeoutEnabled(), settings);
		 appendToConfig("databasePathPermanent:", getDatabasePathPermanent(), settings);
		 
		 appendToConfig("loadDatabaseOnStartup:", getLoadDatabaseOnStartup(), settings);
		 appendToConfig("useRelativeCoverPath:", getUseRelativeCoversPath(), settings);
		 appendToConfig("useRelativeQueriesPath:", getUseRelativeQueriesPath(), settings);
		 appendToConfig("useDisplayQueriesInTree:", getUseDisplayQueriesInTree(), settings);
		 appendToConfig("locale:", getLocale(), settings);
		 
		 LookAndFeelManager lafManager =  MovieManager.getLookAndFeelManager();
		 
		 // GUI/L&F settings
		 appendToConfig("lookAndFeel:", lafManager.getCustomLookAndFeel(), settings);
		 appendToConfig("skinlfTheme:", lafManager.getSkinlfThemePack(), settings);
		 appendToConfig("substanceSkin:", lafManager.getSubstanceSkin(), settings);
		 appendToConfig("nimRODTheme:", lafManager.getNimRODTheme(), settings);
		 appendToConfig("lookAndFeelType:", lafManager.getLookAndFeelType().toString(), settings);
		 appendToConfig("defaultLookAndFeelDecorated:", lafManager.getDefaultLookAndFeelDecorated(), settings);
		 appendToConfig("useRegularSeenIcon:", getUseRegularSeenIcon(), settings);
		 appendToConfig("plotCastMiscellaneousIndex:", getPlotCastMiscellaneousIndex(), settings);
		 appendToConfig("seenEditableInMainWindow:", getSeenEditable(), settings);
		 appendToConfig("displayPlayButton:", getDisplayPlayButton(), settings);
		 appendToConfig("displayPrintButton:", getDisplayPrintButton(), settings);
		 appendToConfig("preserveCoverAspectRatioSetting:", getPreserveCoverAspectRatio(), settings);
		 appendToConfig("useJTreeIcons:", getUseJTreeIcons(), settings);
		 appendToConfig("useJTreeCovers:", getUseJTreeCovers(), settings);
		 appendToConfig("lastPreferencesTabIndex:", getLastPreferencesTabIndex(), settings);
		 
		 appendToConfig("lastMovieInfoTabIndex:", getLastMovieInfoTabIndex(), settings);
		 appendToConfig("movieListRowHeight:", getMovieListRowHeight(), settings);
		 appendToConfig("enableCtrlMouseRightClick:", getEnableCtrlMouseRightClick(), settings);
		 
		 
		 // Slider positions
		 appendToConfig("mainWindowSliderPosition:", MovieManager.getDialog().getMainWindowSplitPane().getDividerLocation(), settings);
		 appendToConfig("mainWindowLastSliderPosition:", MovieManager.getDialog().getMainWindowSplitPane().getLastDividerLocation(), settings);
		 
		 if (MovieManager.getDialog().getMovieInfoSplitPane() != null) {
			 appendToConfig("movieInfoSliderPosition:", MovieManager.getDialog().getMovieInfoSplitPane().getDividerLocation(), settings);
			 appendToConfig("movieInfoLastSliderPosition:", MovieManager.getDialog().getMovieInfoSplitPane().getLastDividerLocation(), settings);
		 }
		 		 
		 if (MovieManager.getDialog().getAdditionalInfoNotesSplitPane() != null) {
			 appendToConfig("addionalInfoNotesSliderPosition:", MovieManager.getDialog().getAdditionalInfoNotesSplitPane().getDividerLocation(), settings);
			 appendToConfig("addionalInfoNotesLastSliderPosition:", MovieManager.getDialog().getAdditionalInfoNotesSplitPane().getLastDividerLocation(), settings);
		 }
		 
		 // Size of Movie Manager window
		 appendToConfig("mainWidth:", (int) mainSize.getWidth(), settings);
		 appendToConfig("mainHeight:", (int) mainSize.getHeight(), settings);
		 appendToConfig("mainMaximized:", getMainMaximized(), settings);
		 
		 // Main dialog location
		 appendToConfig("screenLocationX:", (int) getScreenLocation().getX(), settings);
		 appendToConfig("screenLocationY:", (int) getScreenLocation().getY(), settings);
		 
		 // Window mode
		
		 appendToConfig("addMovieWindowHeight:", getAddMovieWindowHeight(), settings);
		 
		 
		 // MultiAdd window
		 appendToConfig("multiAddMainSliderPosition:", getMultiAddMainSliderPosition(), settings);
		 appendToConfig("multiAddFileSliderPosition:", getMultiAddFileSliderPosition(), settings);
		 appendToConfig("multiAddTabIndex:", getMultiAddTabIndex(), settings);
		 appendToConfig("multiAddWindowWidth:", getMultiAddWindowSize().width, settings);
		 appendToConfig("multiAddWindowHeight:", getMultiAddWindowSize().height, settings);
		 appendToConfig("multiAddIMDbDialogWindowWidth:", getMultiAddIMDbDialogWindowSize().width, settings);
		 appendToConfig("multiAddIMDbDialogWindowHeight:", getMultiAddIMDbDialogWindowSize().height, settings);
		 		 
		 // The current template
		 appendToConfig("HTMLTemplateName:", htmlTemplateHandler.getHTMLTemplateName(), settings);
		 appendToConfig("HTMLTemplateStyleName:", htmlTemplateHandler.getHTMLTemplateStyleName(), settings);
		 
		 // Search options
		 appendToConfig("filterOption:", getFilterCategory(), settings);
		 appendToConfig("sortOption:", getSortOption(), settings);
		 appendToConfig("filterSeen:", getFilterSeen(), settings);
		 appendToConfig("ratingOption:", getRatingOption(), settings);
		 appendToConfig("ratingValue:", getRatingValue(), settings);
		 appendToConfig("dateOption:", getDateOption(), settings);
		 appendToConfig("dateValue:", getDateValue(), settings);
		 
		 
		 // Multi-add settings
		 appendToConfig("multiAddDirectoryPath:", getMultiAddDirectoryPath(), settings);
		 appendToConfig("multiAddRegexString:", getMultiAddRegexString(), settings);
		 appendToConfig("multiAddRegexStringEnabled:", getMultiAddRegexStringEnabled(), settings);
		 appendToConfig("multiAddRegexStringNegated:", getMultiAddRegexStringNegated(), settings);
		 appendToConfig("multiAddRegexCaseSensitive:", getMultiAddRegexCaseSensitive(), settings);
		 appendToConfig("multiAddValidExtensions:", getMultiAddValidExtensionsString(), settings);
		 appendToConfig("multiAddCustomExtensions:", getMultiAddCustomExtensions(), settings);
		 appendToConfig("multiAddSelectOption:", getMultiAddSelectOption().toString(), settings);
		 appendToConfig("multiAddEnableExludeParantheses:", getMultiAddEnableExludeParantheses(), settings);
		 appendToConfig("multiAddEnableExludeCDNotation:", getMultiAddEnableExludeCDNotation(), settings);
		 appendToConfig("multiAddEnableExludeIntegers:", getMultiAddEnableExludeIntegers(), settings);
		 appendToConfig("multiAddEnableExludeYear:", getMultiAddEnableExludeYear(), settings);
		 appendToConfig("multiAddEnableExludeUserdefinedInfo:", getMultiAddEnableExludeUserdefinedInfo(), settings);
		 appendToConfig("multiAddPrefixMovieTitle:", getMultiAddPrefixMovieTitle(), settings);
		 appendToConfig("multiAddEnableAutomaticCombine:", getMultiAddEnableAutomaticCombine(), settings);
		 appendToConfig("multiAddFilterOutDuplicates:", getMultiAddFilterOutDuplicates(), settings);
		 appendToConfig("multiAddFilterOutDuplicatesByAbsolutePath:", getMultiAddFilterOutDuplicatesByAbsolutePath(), settings);
		 appendToConfig("multiAddTitleOption:", getMultiAddTitleOption(), settings);
		 appendToConfig("multiAddTitleOptionNoCd:", getMultiAddTitleOptionNoCd(), settings);
		 appendToConfig("multiAddSearchNfoForImdb:", getMultiAddSearchNfoForImdb(), settings);
		 appendToConfig("multiAddSkipHiddenDirectories:", getMultiAddSkipHiddenDirectories(), settings);
		 appendToConfig("multiAddEnableExludeAllAfterMatchOnUserDefinedInfo:", getMultiAddEnableExludeAllAfterMatchOnUserDefinedInfo(), settings);
		 appendToConfig("multiAddEnableSearchInSubdirectories:", getMultiAddEnableSearchInSubdirectories(), settings);
		 appendToConfig("multiAddExcludeUserDefinedString:", getMultiAddExcludeUserDefinedString(), settings);
		 appendToConfig("multiaddRootDevices:", getMultiAddRootDevicesAsString(), settings);
		 appendToConfig("multiAddList:", getMultiAddList(), settings);
		 appendToConfig("multiAddListEnabled:", getMultiAddListEnabled(), settings);
		 
		 // Proxy settings
		 appendToConfig("proxyEnabled:", getProxyEnabled(), settings);
		 appendToConfig("proxyType:", getProxyType(), settings);
		 appendToConfig("authenticationEnabled:", getProxyAuthenticationEnabled(), settings);
		 appendToConfig("proxyHost:", getProxyHost(), settings);
		 appendToConfig("proxyPort:", getProxyPort(), settings);
		 appendToConfig("proxyUser:", getProxyUser(), settings);
		 appendToConfig("proxyPassword:", getProxyPassword(), settings);
		 
		 // IMDb settings
		 appendToConfig("IMDbAuthenticationEnabled:", getIMDbAuthenticationEnabled(), settings);
		 appendToConfig("IMDbAuthenticationUser:", getIMDbAuthenticationUser(), settings);
		 appendToConfig("IMDbAuthenticationPassword:", getIMDbAuthenticationPassword(), settings);
		 
		 // Saved directories for file choosers
		 if (getLastFileDir() != null)
			 appendToConfig("lastFileDir:", getLastFileDir().getPath(), settings);

		 if (getLastFileDir() != null)
			 appendToConfig("lastDVDDir:", getLastFileDir().getPath(), settings);

		 if (getLastDatabaseDir() != null)
			 appendToConfig("lastDatabaseDir:", getLastDatabaseDir().getPath(), settings);
		
		 if (getLastMiscDir() != null)
			 appendToConfig("lastMiscDir:", getLastMiscDir().getPath(), settings);
			 

		 // Miscellaneous
		 appendToConfig("checkForProgramUpdates:", getCheckForProgramUpdates(), settings);
		 appendToConfig("htmlViewDebugMode:", getHTMLViewDebugMode(), settings);
		 appendToConfig("lastFileFilterMovieInfoUsed:", getLastFileFilterUsed(), settings);
		 appendToConfig("showUnlistedEntries:", getShowUnlistedEntries(), settings);
		 appendToConfig("addNewMoviesToCurrentLists:", getAddNewMoviesToCurrentLists(), settings);
		 appendToConfig("loadCurrentListAtStartup:", getLoadLastUsedListAtStartup(), settings);
		 		 
		 if (getLoadLastUsedListAtStartup()) {

			 String strTmp = "";

			 for (int i = 0; i < currentLists.size(); i++)
				 strTmp += ";" + currentLists.get(i);
	 
			 if (strTmp.length() > 0) {
				 appendToConfig("currentLists:", strTmp, settings);
			 }
		 }
		 		
		 
		 // IMDb parser
		 appendToConfig("autoMoveAnAndA:", getAutoMoveAnAndA(), settings);
		 appendToConfig("storeAllAkaTitles:", getStoreAllAkaTitles(), settings);
		 appendToConfig("includeAkaLanguageCodes:", getIncludeAkaLanguageCodes(), settings);
		 appendToConfig("noCoverTYpe:", getNoCoverType().toString(), settings);
		 appendToConfig("autoMoveThe:", getAutoMoveThe(), settings);
		 appendToConfig("titleLanguageCode:", getTitleLanguageCode(), settings);
		 appendToConfig("useLanguageSpecificTitle:", getUseLanguageSpecificTitle(), settings);
		 appendToConfig("removeQuotesOnSeriesTitle:", getRemoveQuotesOnSeriesTitle(), settings);
		 
		 // External programs
		 appendToConfig("useMediaInfoDLL:", getUseMediaInfoDLL().toString(), settings);
		 appendToConfig("playerPath:", getMediaPlayerPath(), settings);
		 appendToConfig("mediaPlayerCmdArgument:", getMediaPlayerCmdArgument(), settings);
		 appendToConfig("browserPath:", getBrowserPath(), settings);
		 appendToConfig("useDefaultWindowsPlayer:", getUseDefaultWindowsPlayer(), settings);
		 appendToConfig("executeExternalPlayCommand:", getExecuteExternalPlayCommand(), settings);
		 appendToConfig("systemWebBrowser:", getSystemWebBrowser(), settings);
		 
		 // Backup options
		 appendToConfig("databaseBackupEveryLaunch:", getDatabaseBackupEveryLaunch(), settings);
		 appendToConfig("databaseBackupDeleteOldest:", getDatabaseBackupDeleteOldest(), settings);
		 appendToConfig("databaseBackupLaunchCount:", getDatabaseBackupLaunchCount(), settings);
		 appendToConfig("databaseBackupDirectory:", getDatabaseBackupDirectory(), settings);
		 appendToConfig("databaseBackupWarnInvalidDir:", getDatabaseBackupWarnInvalidDir(), settings);
		 
		 // Import/Export settings
		 appendToConfig("htmlExportType:", getHTMLExportType(), settings);
		 appendToConfig("lastDialogExportType:", getLastDialogExportType().toString(), settings);
		 appendToConfig("exportTextfilePath:", getExportTextFilePath(), settings);
		 appendToConfig("exportExcelfilePath:", getExportExcelFilePath(), settings);
		 appendToConfig("exportXMLDbfilePath:", getExportXMLDbFilePath(), settings);
		 appendToConfig("exportXMLfilePath:", getExportXMLFilePath(), settings);
		 appendToConfig("exportCSVfilePath:", getExportCSVFilePath(), settings);
		 appendToConfig("exportCSVseparator:", getExportCSVseparator(), settings);
		 
		 appendToConfig("lastDialogImportType:", getLastDialogImportType().toString(), settings);
		 appendToConfig("importTextfilePath:", getImportTextFilePath(), settings);
		 appendToConfig("importExcelfilePath:", getImportExcelFilePath(), settings);
		 appendToConfig("importXMLfilePath:", getImportXMLFilePath(), settings);
		 appendToConfig("importCSVfilePath:", getImportCSVFilePath(), settings);
		 appendToConfig("importCSVseparator:", getImportCSVseparator(), settings);
		 appendToConfig("importIMDbInfoEnabled:", getImportIMDbInfoEnabled(), settings);
		 appendToConfig("importIMDbSelectOption:", getImportIMDbSelectOption().toString(), settings);
		  		 
		 
		 // Default values in the additional info fields in DialogMovieInfo

		 HashMap<String, AdditionalInfoFieldDefaultValues> defaultValues = getAdditionalInfoDefaultValues();
		 AdditionalInfoFieldDefaultValues value;

		 Object[] keys = defaultValues.keySet().toArray();

		 for (int i = 0; i < keys.length; i++) {
			 value = (AdditionalInfoFieldDefaultValues) defaultValues.get(keys[i]);

			 if (value != null) {
				 appendToConfig("AdditionalInfoDefaultValues:" +value.getFieldName()+":", value.getDefaultValuesString("|"), settings);
			 }
		 }

		 // Search aliases
		 
		 HashMap<String, String> searchAlias = getSearchAlias();
		 String key;
		 String val;

		 for (Iterator<String> i = searchAlias.keySet().iterator(); i.hasNext();) {
			 key = (String) i.next();
			 val = (String) searchAlias.get(key);

			 appendToConfig("Search Alias:", key + "=" + val, settings);
		 }
		 
		 
		 ArrayList<String> filterValues = getMainFilterSearchValues();
		 
		 for (int i = 0; i < filterValues.size(); i++) {
			 appendToConfig("mainFilterSearchValues:", filterValues.get(i), settings);
		 }

		 
		 File config = null;
		 int appMode = SysUtil.getAppMode();

		 // Applet
		 if (appMode == 1)
			 config = new File(new URI(FileUtil.getFileURL("config/Config_Applet.ini", DialogMovieManager.applet).toString()));
		 else if (appMode == 2) { // Java Web Start

			 MovieManagerConfigHandler configHandler = getConfigHandler();

			 if (configHandler != null)
				 config = new File(new URI(configHandler.getConfigURL().toString()));

		 } else {
			 if (SysUtil.isMac() || SysUtil.isWindowsVista() || SysUtil.isWindows7())
				 config = new File(SysUtil.getConfigDir(), "Config.ini");
			 else 
				 config = new File(SysUtil.getUserDir(), "config/Config.ini");  
		 }

		 if (config == null)
			 throw new Exception("Failed to save config file:" + config);

		 log.debug("Saving configuration data: " + config);

		 long modTime = config.lastModified();
		 
		 FileUtil.writeToFile(config.getAbsolutePath(), settings);
		 
		 long modTime2 = config.lastModified();
		 
		 if (modTime == modTime2) {
			 log.error("Modification time didn't change. Seems the config file couldn't be written: " + config.getAbsolutePath());
		 }
	 }
	 
	
	
	public boolean handleBackupSettings() {
			 
		 try {
			 boolean ret = false;
			 int launchCount = Integer.parseInt(getDatabaseBackupLaunchCount());
			 int backupEveryLaunch = Integer.parseInt(getDatabaseBackupEveryLaunch());
			 
			 if (backupEveryLaunch < 1)
				 return false;
			 
			 if (launchCount % backupEveryLaunch == 0) {
				 ret = true;
			 }	 
			 
			 launchCount++;
			 setDatabaseBackupLaunchCount(String.valueOf(launchCount));
			 return ret;
			 
		 } catch (Exception e) {
			 log.error("Exception:" + e.getMessage(), e);
			 log.warn("Unable to handle backup settings!");
		 }
		 return false;
	 }
	
	
	
}




