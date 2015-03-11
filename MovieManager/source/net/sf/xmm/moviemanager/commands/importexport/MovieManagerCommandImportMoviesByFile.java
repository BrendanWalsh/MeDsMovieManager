package net.sf.xmm.moviemanager.commands.importexport;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogIMDB;
import net.sf.xmm.moviemanager.gui.DialogIMDbMultiAdd;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies.Files;
import net.sf.xmm.moviemanager.imdblib.IMDb;
import net.sf.xmm.moviemanager.imdblib.IMDbLib;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelFileImportSettings;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.util.SysUtil;

public class MovieManagerCommandImportMoviesByFile extends MovieManagerCommandImportHandler {

	ModelFileImportSettings fileSettings;
	
	HashMap<String, ModelEntry> existingMediaFiles;
	HashMap<String, ModelEntry> existingMediaFileNames;
		
	public MovieManagerCommandImportMoviesByFile(ModelImportExportSettings settings) {
		super(settings);
		fileSettings = (ModelFileImportSettings) settings;
		
		existingMediaFileNames = fileSettings.existingMediaFileNames;
		existingMediaFiles = fileSettings.existingMediaFiles;
	}
	
	Logger log = Logger.getLogger(getClass());

	String[] stringFiles;
	
	ModelMovieInfo movieInfoModel = null;

	ArrayList<String> moviesToAdd;

	ArrayList <DialogAddMultipleMovies.Files> fileList = null;
	
	MovieManagerCommandImportMoviesByFile importProcess = this;
	
	public int getMovieListSize() throws Exception {
		return fileList.size();
	}
		
	public void retrieveMovieList() throws Exception {
		fileList = fileSettings.fileList;
		movieInfoModel = new ModelMovieInfo(false, true);
		movieInfoModel.setSaveCover(true);
	}
	
	boolean isAlreadyInDatabase(int i) {

		// Check if file is in database
		if (MovieManager.getConfig().getMultiAddFilterOutDuplicates() || 
				MovieManager.getConfig().getMultiAddFilterOutDuplicatesByAbsolutePath()) {
			DialogAddMultipleMovies.Files fileNode = (DialogAddMultipleMovies.Files) fileList.get(i);
			ArrayList<Files> files = fileNode.getFiles();

			for (int u = 0; u < files.size(); u++) {

				File file = files.get(u).getFile();
				
				if (MovieManager.getConfig().getMultiAddFilterOutDuplicatesByAbsolutePath()) {
					if (existingMediaFiles.containsKey(file.getAbsolutePath())) {
						return true;
					}
				}
				else if (MovieManager.getConfig().getMultiAddFilterOutDuplicates()) {
					if (existingMediaFileNames.containsKey(file.getName())) {
						return true;
					}
				}
			}		
		}
		
		return false;
	}
	
	
	public String getTitle(final int i) throws Exception {

		// Reset cancelled status
		resetStatus();
		
		if (isAlreadyInDatabase(i)) {
			setCancelled(true);
						
			ArrayList<Files> f = fileList.get(i).getFiles();
			String [] fString = new String[f.size()];
			
			for (int y = 0; y < f.size(); y++)
				fString[y] = f.get(y).getFile().getAbsolutePath();
			
			return getTitleWithMediaFiles("One or more files are already in the database:", fString);
		}
		
		File [] tempFile = new File[1];

		String searchString = null; // Used to search on imdb 
		String searchTitle = null; // Title of the IMDb dialog
		String path = null; // Path of the file 
		String imdbId = null;

		DialogAddMultipleMovies.Files fileNode = (DialogAddMultipleMovies.Files) fileList.get(i);
		ArrayList<Files> files = fileNode.getFiles();

		tempFile = new File[files.size()];

		for (int u = 0; u < tempFile.length; u++) {
			tempFile[u] = files.get(u).getFile();
		}				

		searchString = tempFile[0].getName();
		searchTitle = searchString;

		log.debug("Processing:" + searchTitle);

		movieInfoModel.clearModel();
		movieInfoModel.setAdditionalInfoFieldsEmpty();

		// Getting the fileinfo from file 
		try {
			movieInfoModel.getFileInfo(tempFile);
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}

		
		// Sets up search string as Folder name or file name 
		if (fileSettings.enableUseFolderName) {

			path = tempFile[0].getPath();
			int slash = path.lastIndexOf(File.separator);

			if (slash == -1) {
				searchString = path;
			}
			else {
				path = path.substring(0, slash);
				slash = path.lastIndexOf(File.separator);

				if (slash == -1) {
					searchString = path;
				}
				else {
					String temp = path.substring(slash+1);
					if (fileSettings.enableUseParentFolderIfCD && temp.toLowerCase().startsWith("cd")) {
						// IF last directory is CD* than the name is in the directory above.
						path = path.substring(0, slash);
						slash = path.lastIndexOf(File.separator);
					}
					searchString = path.substring(slash+1);
				}
			}
		}

		String year = null;
				
		String [] extensions = new String[] {"avi", "mkv", "mpg", "mpeg", "mpe", "divx", "mp4", "ogm", "ogv", "ogg", "flv", "rm", "swf", "vob", "wmv", "asf"};
				
		// Remove file extension
		searchString = StringUtil.removeExtension(searchString, extensions);
		
		if (fileSettings.enableExludeYear) {
			String [] year2 = new String[1];
			searchString = StringUtil.removeYearAndAllAfter(searchString, year2);
			year = year2[0];
		}
		else {
			// only find the year
			String [] year2 = new String[1];
			StringUtil.removeYearAndAllAfter(searchString, year2);
			year = year2[0];
		}
		
		if (fileSettings.enableExludeParantheses) {
			searchString = StringUtil.performExcludeParantheses(searchString, false);
		}

		if (fileSettings.enableExludeAllAfterMatchOnUserDefinedInfo) {
			String info = MovieManager.getConfig().getMultiAddExcludeUserDefinedString();

			if (!info.equals("")) {
				Pattern p = Pattern.compile("[,]");
				String[] excludeStrings = p.split(info);
				searchString = StringUtil.performExcludeUserdefinedInfo(searchString, excludeStrings);
			}
		}
		else if (fileSettings.enableExludeUserdefinedInfo) {
			String info = MovieManager.getConfig().getMultiAddExcludeUserDefinedString();

			if (!info.equals("")) {
				Pattern p = Pattern.compile("[,]");
				String[] excludeStrings = p.split(info);
				searchString = StringUtil.performExcludeStrings(searchString, excludeStrings);
			}
		}

		if (fileSettings.enableExludeCDNotations) {
			searchString = StringUtil.performExcludeCDNotations(searchString);
		}
		
		if (fileSettings.enableExludeIntegers) {
			searchString = StringUtil.performExcludeIntegers(searchString);
		}
		
		if (fileSettings.enableSearchNfoForImdb)
			imdbId = searchNfoForImdb(path);
				
		//removes dots, double spaces, underscore...
		searchString = StringUtil.removeVarious(searchString);
				
		ImportExportReturn ret = executeCommandGetIMDBInfoMultiMovies(imdbId, searchString, searchTitle, year, fileList.get(i), fileSettings.multiAddSelectOption, fileSettings.addToThisList);
		
		if (ret == ImportExportReturn.success) {
			String title = movieInfoModel.model.getTitle();
			String [] fileLoc = movieInfoModel.model.getAdditionalInfo().getFileLocationAsArray();
			return getTitleWithMediaFiles(title, fileLoc);
		}
			
		return fileList.get(i).getName();
	}

	
	String getTitleWithMediaFiles(String title, String [] files) {
		
		for (int u = 0; u < files.length; u++) {
			title += SysUtil.getLineSeparator() + "      " +  files[u];
		}
		return title;
	}
	
	public ImportExportReturn addMovie(int i) throws Exception {

		ImportExportReturn ret = ImportExportReturn.success;

		try {
			boolean status = movieInfoModel.saveCoverToFile();

			if (!status)
				log.debug("Cover for title " + movieInfoModel.model.getTitle() + " not saved to file.");

		} catch (Exception e) {
			log.warn("Exception: " + e.getMessage()); //$NON-NLS-1$
		}

		try {
			ArrayList<String> list = new ArrayList<String>();
			list.add(fileSettings.addToThisList);
			
			final ModelEntry model = movieInfoModel.saveToDatabase(list);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					MovieManagerCommandSelect.executeAndReload(model, false, false, true);
				}
			});
			
			String [] fileLoc = model.getAdditionalInfo().getFileLocationAsArray();
			
			for (int y = 0; y < fileLoc.length; y++) {
				existingMediaFiles.put(fileLoc[y], model);
				existingMediaFileNames.put(new File(fileLoc[y]).getName(), model);
			}
			
		} catch (Exception e) {
			log.error("Saving to database failed.", e);
			ret = ImportExportReturn.error;
		}

		movieInfoModel.model.setTitle("");
				
		return ret;
	}

	
	
	/**
	 * Gets the IMDB info for movies (multiAdd)
	 **/
	public ImportExportReturn executeCommandGetIMDBInfoMultiMovies(final String imdbId, final String searchString, 
			final String filename, final String year, final Files files, final ImdbImportOption multiAddSelectOption, final String addToThisList) {

		ImportExportReturn ret = ImportExportReturn.success;
		
		/* Checks the movie title... */
		log.debug("executeCommandGetIMDBInfoMultiMovies"); //$NON-NLS-1$
		
		try {

			if (!searchString.equals("")) { //$NON-NLS-1$

				int hitCount = -1;

				if (multiAddSelectOption == ImdbImportOption.selectFirst && imdbId != null) {
					DialogIMDB.getIMDbInfo(movieInfoModel.model, imdbId);

					//list.addElement(hit);
					//hitCount = 1;
				}
				else {
					// Only pull list from imdb if not "Select FirstHit" is selected and no IMDB Id was found in an nfo/txt file
					final ArrayList<ModelIMDbSearchHit> hits = IMDbLib.newIMDb(MovieManager.getConfig().getHttpSettings()).getSimpleMatches(searchString);

					/*Number of movie hits*/
					hitCount = hits.size();
	
					if ((hitCount > 0 && multiAddSelectOption == ImdbImportOption.selectFirst) || 
							hitCount == 1 && multiAddSelectOption == ImdbImportOption.selectIfOnlyOneHit) {
						
						DialogIMDB.getIMDbInfo(movieInfoModel.model, hits.get(0).getUrlID());

						// Insert prefix in Title to show that these movies maybe got wrong imdb infos
						if (MovieManager.getConfig().getMultiAddPrefixMovieTitle() && hitCount > 1 && 
								multiAddSelectOption == ImdbImportOption.selectFirst && (imdbId == null))
							movieInfoModel.model.setTitle("_verify_ " + movieInfoModel.model.getTitle()); //$NON-NLS-1$
					}
					else {
						
						GUIUtil.invokeAndWait(new Runnable() {
							public void run() {
								DialogIMDbMultiAdd dialogIMDB = new DialogIMDbMultiAdd(fileSettings.getParent(), 
										movieInfoModel.model, searchString, 
										year, filename, files, imdbId);
								GUIUtil.showAndWait(dialogIMDB, true);
																
								if (dialogIMDB.getCanceled()) {
									setCancelled(true);
								}

								if (dialogIMDB.getAborted()) {
									setAborted(true);
								}
							}
						});
					}
				}
			} else {
				GUIUtil.invokeAndWait(new Runnable() {
					public void run() {
						DialogAlert alert = new DialogAlert(MovieManager.getDialog(), Localizer.get("DialogMovieInfo.alert.title.alert"), Localizer.get("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
						GUIUtil.showAndWait(alert, true);
					}
				});
			}
		} catch (Exception e) {
			log.debug("Exception:" + e.getMessage(), e);
		}
		
		return ret;
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
}
