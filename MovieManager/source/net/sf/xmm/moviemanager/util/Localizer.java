package net.sf.xmm.moviemanager.util;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.MissingResourceException;

import net.sf.xmm.moviemanager.MovieManager;

import org.apache.log4j.Logger;


public class Localizer {

	static Logger log = Logger.getLogger(Localizer.class);
	
	static TMXResourceBundle resource = null;

	static String temp;

	static {

		InputStream inpuStream = null;

		try {
			inpuStream = FileUtil.getResourceAsStream("/config/MovieManager.tmx");
			// inpuStream = DialogMovieManager.applet.getClass().getResourceAsStream("/MovieManager.tmx");
			
			if (inpuStream != null) {
				resource = new TMXResourceBundle(null, inpuStream, "");
			}
			else {
				log.error("TMX lanuguage file not accessible");
				resource = null;
			}
						
		} catch (Exception e) {
			log.error("Failed to load languge file from MovieManager.jar", e);
		}

		if (resource == null) {
			          
			log.debug("Loading locaal language file");
			
			// TMXResourceBundle searches the file in half a dozen places anyway, so it's 
			// probably better to not use a path here... (actually, it fails on mac if we don't
			// do so)

			// First try to get the file from the current dir
			File f = FileUtil.getFile("config/MovieManager.tmx");
						
			if (f == null || !f.isFile()) {
				 f = new File(FileUtil.getFileURL(System.getProperty("user.dir") + "/config/MovieManager.tmx").getPath());
			}

			// If no success the MovieManager.tmx is grabbed from the MovieManager.jar file.
			if (f.isFile()) {
				resource = new TMXResourceBundle(f.getAbsolutePath());
			}
		}
		
		// failed to load langauge file
		if (resource == null) {
			log.error("Failed to load languge file");
		}

		java.util.HashMap<String, String> langs = resource.getLanuages();

		int counter = 0;

		log.debug("Loaded languages:");
		for (String key : langs.keySet()) {
			log.debug(counter++ + ":" + key);
		}
		
		
		try {
			
			String locale = MovieManager.getConfig().getLocale();			
			
			resource.load();
			
			if (!resource.setDefaultLangauge("en-US"))
				log.warn("Failed to set default language");
			
			// If failed to load language, load default en-US
			if (!resource.setLangauge(locale))
				resource.setLangauge("en-US");
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
	}
	
	public static String get(String key) {

		try {
			temp = resource.getString(key);
			return temp;
		} catch (MissingResourceException e) {
			log.warn("Invalid key:" + key, e);
			return '!' + key + '!';
		}
	}
	
	public static String [] getAvailableLanguages() {
		HashMap<String, String> langauges = resource.getLanuages();
		String [] langs = new String[langauges.size()];
		
		int index = 0;
		for (String key : langauges.keySet()) {
			langs[index] = key;
			index++;
		}
		return langs;
	}
	
}
