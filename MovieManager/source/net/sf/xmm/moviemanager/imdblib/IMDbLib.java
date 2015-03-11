package net.sf.xmm.moviemanager.imdblib;

import net.sf.xmm.moviemanager.http.HttpSettings;

public class IMDbLib {
	
	final static String version = "10.03.2010";
	final static String name = "IMDb lib";
	
	final static int release = 6;
	
	public static int getRelease() {
		return release;
	}
	
	public String getName() {
		return name;
	}
	
	public static String getVersion() {
		return version;
	}
	
	public static String getVersionString() {
		return name + " " + version;
	}
	
	
	public static IMDb newIMDb() throws Exception {
		return new IMDbScraper(null, null, null);
	}
	
	public static IMDb newIMDb(String urlID) throws Exception {
		return new IMDbScraper(urlID, null, null);
	}
    
	public static IMDb newIMDb(String urlID, StringBuffer data) throws Exception {
		return new IMDbScraper(urlID, data, null);
	}
	
	public static IMDb newIMDb(String urlID, HttpSettings settings) throws Exception {
		return new IMDbScraper(urlID, null, settings);
	}
	
	public static IMDb newIMDb(HttpSettings settings) throws Exception {
		return new IMDbScraper(null, null, settings);
	}
    
	public static IMDb newIMDb(String urlID, StringBuffer data, HttpSettings settings) throws Exception {
		return new IMDbScraper(urlID, data, settings);
	}
}
