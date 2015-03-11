/**
 * @(#)IMDB.java
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

package net.sf.xmm.moviemanager.imdblib;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.http.HttpSettings;
import net.sf.xmm.moviemanager.http.HttpUtil;
import net.sf.xmm.moviemanager.http.HttpUtil.HTTPResult;
import net.sf.xmm.moviemanager.models.imdb.*;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class IMDbScraper implements IMDb {
  
	static Logger log = Logger.getLogger(IMDb.class);
    	
    private HttpUtil httpUtil = new HttpUtil();
    
    private HttpSettings settings = null;
    
    private HTTPResult lastHTTPResult = null;
    
    private ModelIMDbEntry lastDataModel;
    
    public final String [] movieHitCategory = {"Popular Titles", "Titles (Exact Matches)", "Titles (Partial Matches)", "Titles (Approx Matches)"};
	
    public IMDbScraper() throws Exception {
    	this(null, null, null);		
    }
    
    public IMDbScraper(String urlID) throws Exception {
    	this(urlID, null, null);		
    }
    
    public IMDbScraper(String urlID, StringBuffer data) throws Exception {
    	this(urlID, data, null);		
    }
    
    public IMDbScraper(String urlID, HttpSettings settings) throws Exception {
    	this(urlID, null, settings);	
    }
    
    public IMDbScraper(HttpSettings settings) throws Exception {
    	this(null, null, settings);	
    }
         
    /**
     * The constructor. Initializes all vars (read from the net) for
     * the movie with key.
     **/
    public IMDbScraper(String urlID, StringBuffer data, HttpSettings settings) throws Exception {
	
		this.settings = settings;
		httpUtil = new HttpUtil(settings);
		
		if (urlID != null || data != null)
			grabInfo(urlID, data);
    }
    
    public String getVotrUrl(String urlID, int vote, String urlVoteID) {
    	
    	String voteUrlPrefix = "http://www.imdb.com/title/tt";
    	voteUrlPrefix += urlID + "/vote?v=" + vote + ";k=" + urlVoteID;
    	
    	return voteUrlPrefix;
    	// http://www.imdb.com/title/tt0093773/vote?v=8;k=k6HOfCDHhmP2.KdTU
    }

    public String getVotrUrl(ModelIMDbEntry model, int vote) {
    	return getVotrUrl(model.getUrlID(), vote, model.getVoteUrlID());
    }
    
   
    
    public ModelIMDbEntry grabInfo(String urlID) throws Exception {
    	return grabInfo(urlID, null);	
    }
    
    public HTTPResult getURLData(String urlID) throws Exception {
    	
    	if (urlID == null)
    		throw new Exception("Movie ID is empty");
    
    	URL url = new URL("http://akas.imdb.com/title/tt"+ urlID +"/");
    	lastHTTPResult = httpUtil.readData(url);
		return lastHTTPResult;
    }
    
    public ModelIMDbEntry grabInfo(String urlID, StringBuffer data) throws Exception {
    	
    	//long time = System.currentTimeMillis();
    	
    	if (urlID == null && data == null)
    		throw new Exception("Input data is null.");
    	
    	if (urlID != null && data == null) {
    		HTTPResult res = getURLData(urlID);
    		data = res.getData();
    	}
    	
    	if (data == null) {
    		throw new Exception("Error occured when reading data.(urlID:"+ urlID +")");
		}
    	
    	return parseData(urlID, data);
    }
    
    
    String retrieveUrlCover(StringBuffer data, ModelIMDbEntry dataModel) {
    	
    	String coverURL = null;
    	String coverName = null;
    	int start, end;
    	
    	/* Gets the cover url... */
		if ((start = data.indexOf("<div class=\"photo\">")) != -1 && 
			(end = data.indexOf("</div>", start)) != -1) {
    	
			String tmp = data.substring(start, end);
			
			if (tmp.indexOf("Poster Not Submitted") == -1) {
    	
				if ((start = data.indexOf("src=\"", start) +5) !=4 &&
					(end = data.indexOf("\"", start)) != -1) {
					coverURL = HttpUtil.decodeHTML(data.substring(start, end));
					
					dataModel.setCoverURL(coverURL);
					
					start = coverURL.lastIndexOf(".");

					if (start != 0 && start != -1) {
						coverName = dataModel.getUrlID() + coverURL.substring(start, coverURL.length());
						dataModel.setCoverName(coverName);
					}
					
					// Get id of big cover
											
					Pattern p = Pattern.compile("href=\".*/media/(rm\\d+/tt\\d+)\"");
											
					Matcher m = p.matcher(tmp);
					
					if (m.find()) {
					
						String g = m.group();
					
						//System.out.println("g:" + m.group(0));
						//System.out.println("g1:" + m.group(1));
						
						dataModel.bigCoverUrlId = m.group(1);
						
					}
					//rm3535314176/tt0093773
				}
			}
		}
    	return coverURL;
    }
    
    private ModelIMDbEntry parseData(String urlID, StringBuffer data) throws Exception {
    	return parseData(urlID, data, true);
    }
    
    /*
     * If urlID is null, no extra plot will be retrieved
     */
    private ModelIMDbEntry parseData(String urlID, StringBuffer data, boolean getCover) throws Exception {
		
        String date = "", title = "", seasonNumber = "", episodeNumber = "";
    	  
    	//long time = System.currentTimeMillis();
    	
		int start = 0;
		int end = 0;
	
		Object [] tmpArray;
	
		boolean isEpisode = false;
		boolean isSeries = false;
		boolean loggedIn = false;
		//net.sf.xmm.moviemanager.util.FileUtil.writeToFile("HTML-debug/imdb.html", data);
			
		try {
			/* Processes the data... */
			
			int endIndex = data.indexOf("Genre:");
			
			if (endIndex == -1)
				endIndex = 1000;
			
			String beginning = data.substring(0, endIndex);
						
			if (beginning.indexOf("TV Series:") != -1)
				isEpisode = true;
			else if (beginning.indexOf("Seasons:") != -1)
				isSeries = true;
			
			
			if ((start = data.indexOf("\"/register/logout\"", start)) != -1) {
				loggedIn = true;	
			}
						
			/* Gets the title... */
			if ((start = data.indexOf("<div id=\"tn15title\">", start)) != -1 &&
					(end = data.indexOf("</div>", start)) != -1) {

				tmpArray = HttpUtil.decodeHTMLtoArray(data.substring(start, end));

				if (isEpisode) {
					title = (String) tmpArray[1];
					date = (String) tmpArray[2];
					
					if (date.startsWith("(") && date.endsWith(")"))
						date = date.substring(1, date.length() -1);
					
				}
				else { 
					title = (String) tmpArray[0];
					date = (String) tmpArray[2];
				}
				
				if (!isEpisode && title.startsWith("\""))
					isSeries = true;
			}	
			else
				throw new Exception("Title could not be found");
	    
			ModelIMDbEntry tmpModel = null;
			
			if (isSeries)
				tmpModel = new ModelIMDbSeries();
			else if (isEpisode)
				tmpModel = new ModelIMDbEpisode();
			else
				tmpModel = new ModelIMDbMovie();
			
			// Must be accessible from within inner class
			final ModelIMDbEntry dataModel = tmpModel;
						
			dataModel.setLoggedIn(loggedIn);
			
			// Original title from IMDb
			dataModel.setIMDbTitle(title);
			
			dataModel.setTitle(getModifiedTitle(title));
			
			dataModel.setDate(date);
			dataModel.setUrlID(urlID);
			
			final ReentrantLock lock = new ReentrantLock();
			
			if (getCover) {
				String coverURL = retrieveUrlCover(data, dataModel);
				
				if (coverURL != null) {

					Thread t = new Thread(new Runnable() {
						public void run() {
							try {
								lock.lock();
								retrieveCover(dataModel);
							} finally {
								lock.unlock();
							}
						}
					});
					t.start();
				}
			}
			
			start = 0;
			end = 0;
						
			/* Gets the rating... */
			if ((start = data.indexOf("User Rating:", start)) != -1 && 
					((start = data.indexOf("<div class=\"starbar-meta\">", start)) != -1) &&
					(end = data.indexOf("/10</b>",start)) != -1 &&
					(start = data.indexOf("<b>",end-9) +3) != 2) {

				
				try {
					String rating = HttpUtil.decodeHTML(data.substring(start, end));
					dataModel.setRating(rating);
				} catch (IndexOutOfBoundsException e) {
					log.debug("No rating found for " + title  + " ("+urlID+")");
				}
			}
			
			// Get personal rating
			if ((start = data.indexOf("Your Rating:", start)) != -1) {

				Pattern p;
				Matcher m;
				
				//  <a title="10" href="vote?v=10;k=6ypj9zBHdet.9LkyB-xTpgas13Wl.1QDVa.EQFYMQCQWrNd0FZ-kc1X.lBAGD8RANqzAM.Uv52NV79RQNjuAQHas11P2.KdTU_" onclick="return vote

				//Get the vote url
				p = Pattern.compile("<a title=\"10\" href=\"vote\\?v=10;k=(.+?)\" onclick");
				m = p.matcher(data);
				
				if (m.find()) {
					String voteID = m.group(1);
					dataModel.setVoteUrlID(voteID);
				}
									 
				if ((start = data.indexOf("voteuser", start)) != -1) {

					String voteData = data.substring(start, start + 30);

					//	<span id="voteuser">9</span>/10
					p = Pattern.compile("voteuser\">(\\d+)</span>/10");
					m = p.matcher(voteData);

					if (m.find()) {
						String rating = m.group(1);

						if (!rating.equals("10"))
							rating += ".0";

						dataModel.setPersonalRating(rating);
					}
				}
			}				
			// Gets the directed by... 
			
			HashMap<String, String> classInfo = decodeClassInfo(data);
			
			String tmp = "";
			ArrayList <String> list;
			
			String directedBy = "";
			
			if (classInfo.containsKey("Director:")) {
				directedBy = getDecodedClassInfo("Director", (String) classInfo.get("Director:"));
			}
			else if (classInfo.containsKey("Directors:")) {
				
				tmp = (String) classInfo.get("Directors:");
				list = getLinkContentName(tmp);
		    	 
				while (!list.isEmpty()) {
					if (!directedBy.equals(""))
						directedBy += ", ";
		    			
					directedBy += list.remove(0);
				}
			}
				
			dataModel.setDirectedBy(directedBy);
			
			tmp = null;
			
			//Set<String> keys = classInfo.keySet();
			//for (String key : keys)
			//	System.out.println("key:" + key);
			
			// Gets the Writer or Writers (Writer matches both Writer: and Writers:)
			if (classInfo.containsKey("Writer:")) {
				//tmp = getClassInfo(data, "Writer:");
				tmp = classInfo.get("Writer:");
			}
			if (classInfo.containsKey("Writers:")) {
				//tmp = getClassInfo(data, "Writers:");
				tmp = classInfo.get("Writers:");
			}
			else if (classInfo.containsKey("Creator:")) {
				tmp = classInfo.get("Creator:");
				//tmp = getClassInfo(data, "Creator:");
			}
			
			if (tmp != null) {
				//tmp = tmp.substring(tmp.indexOf(":")+1, tmp.length());
				
				list = getLinkContentName(tmp);

				String writtenBy = "";
				
				while (!list.isEmpty()) {
					if (!writtenBy.equals(""))
						writtenBy += ", ";

					writtenBy += list.remove(0);
				}

				dataModel.setWrittenBy(writtenBy);
			}
			
			if (classInfo.containsKey("Genre:")) {
				String genre = getDecodedClassInfo("Genre:", (String) classInfo.get("Genre:"));
				genre = genre.replaceAll("(more)$", "");
				dataModel.setGenre(genre);
			}
						
			if (classInfo.containsKey("Plot:")) {
				String plot = getDecodedClassInfo("Plot:", (String) classInfo.get("Plot:"));
				dataModel.setPlot(plot);
			}
			
			String cast = getDecodedClassInfo("class=\"cast\">", data);
			cast = cast.replaceAll(" \\.\\.\\.", ",");
			cast = HttpUtil.decodeHTML(cast);
			
			dataModel.setCast(cast);
			
			if (classInfo.containsKey("Also Known As:")) {
				String aka = getDecodedClassInfo("Also Known As:", (String) classInfo.get("Also Known As:"));
				aka = aka.trim();
				dataModel.setAka(aka);
			}
			
			String mpaa = getDecodedClassInfo("<a href=\"/mpaa\">MPAA</a>:", data);
			dataModel.setMpaa(mpaa);
			
			if (classInfo.containsKey("Runtime:")) {
				String runtime = getDecodedClassInfo("Runtime:", (String) classInfo.get("Runtime:"));
				dataModel.setWebRuntime(runtime);
			}
			
			if (classInfo.containsKey("Country:")) {
				String country = getDecodedClassInfo("Country:", (String) classInfo.get("Country:"));
				dataModel.setCountry(country);
			}
			
			if (classInfo.containsKey("Language:")) {
				String language = getDecodedClassInfo("Language:", (String) classInfo.get("Language:"));
				dataModel.setLanguage(language);
			}
			
			if (classInfo.containsKey("Color:")) {
				String colour = getDecodedClassInfo("Color:", (String) classInfo.get("Color:"));
				dataModel.setColour(colour);
			}
			
			if (classInfo.containsKey("Sound Mix:")) {
				String soundMix = getDecodedClassInfo("Sound Mix:", (String) classInfo.get("Sound Mix:"));
				dataModel.setWebSoundMix(soundMix);
			}
			
			if (classInfo.containsKey("Certification:")) {
				String certification = getDecodedClassInfo("Certification:", (String) classInfo.get("Certification:"));
				dataModel.setCertification(certification);
			}
			
			if (classInfo.containsKey("Awards:")) {
				String awards = getDecodedClassInfo("Awards:", (String) classInfo.get("Awards:"));
				awards = awards.replaceAll("(more)$", "");
				dataModel.setAwards(awards);
			}
						
			String airdateContent = null;
			
			if (classInfo.containsKey("Original Air Date:"))
				airdateContent = getDecodedClassInfo("Original Air Date:", (String) classInfo.get("Original Air Date:"));
						
			// Ex   29 April 2002 (Season 3, Episode 19)
			// Ex: 5 October 1999 (Season 1, Episode 1)
			if (airdateContent != null) {
				Pattern p = Pattern.compile("(.+)?\\s\\(.+?(\\d+?),\\s?.+?(\\d+?)\\)");
				Matcher m = p.matcher(airdateContent);
								
				if (m.find()) {

					int gCount = m.groupCount();

					if (gCount == 3) {
							
						//String airdat = m.group(1);
						String season = m.group(2);
						String episode = m.group(3);
	
						seasonNumber = season;
					    episodeNumber = episode;
					    
					    ((ModelIMDbEpisode) dataModel).setSeasonNumber(seasonNumber);
					    ((ModelIMDbEpisode) dataModel).setEpisodeNumber(episodeNumber);
					}
				}
			}
			
			if (urlID != null) {
				/* Gets a bigger plot (if it exists...) */
				retrieveBiggerPlot(data, dataModel);
			}
						
			lock.tryLock((long) 10, TimeUnit.SECONDS);
						
			lastDataModel = dataModel;
			
			return dataModel;
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
		
		return null;
    }

    

    void retrieveBiggerPlot(StringBuffer data, ModelIMDbEntry dataModel) throws TimeoutException, Exception {
    	
    	URL url = new URL("http://akas.imdb.com/title/tt"+ dataModel.getUrlID() +"/plotsummary");
	    
		HTTPResult result = httpUtil.readData(url);
		data = result.getData();
		
		/* Processes the data... */
		int start = 0;
		int end = 0;
    				
		String plot = null;
		
		if (data != null) {
				
			if ((start = data.indexOf("class=\"plotpar\">",start)+16) != 15 &&
					(end=data.indexOf("</p>",start)) != -1) {
				plot = HttpUtil.decodeHTML(data.substring(start, end));

				if (plot.indexOf("Written by") != -1)
					plot = plot.substring(0, plot.indexOf("Written by"));
			}
		}
		
		if (plot != null && !plot.equals("")) {
			plot = plot.trim();
			plot = plot.replaceAll("(more)$", "");
			dataModel.setPlot(plot);
		}
    }
    
    private ModelIMDbEntry parseData2(String urlID, StringBuffer data) throws Exception {
		
        String date = "", title = "", plot = "", coverURL = "", coverName = "";
    	  
    	//long time = System.currentTimeMillis();
    	
		int start = 0;
		int end = 0;
	
		Object [] tmpArray;
	
		boolean isEpisode = false;
		boolean isSeries = false;
		
		net.sf.xmm.moviemanager.util.FileUtil.writeToFile("HTML-debug/imdb.html", data);
			
		try {
			/* Processes the data... */

			if (data.indexOf("Full Episode List") != -1)
				isEpisode = true;
			
			/* Gets the title... */
			if ((start = data.indexOf("<div id=\"tn15title\">", start)) != -1 &&
					(end = data.indexOf("</div>", start)) != -1) {

				tmpArray = HttpUtil.decodeHTMLtoArray(data.substring(start, end));

				if (isEpisode) {
					title = (String) tmpArray[1];
					date = (String) tmpArray[2];
					
					if (date.startsWith("(") && date.endsWith(")"))
						date = date.substring(1, date.length() -1);
					
				}
				else { 
					title = (String) tmpArray[0];
					date = (String) tmpArray[2];
				}
				
				if (!isEpisode && title.startsWith("\""))
					isSeries = true;
			}	
			else
				throw new Exception("Title could not be found");
	    
			ModelIMDbEntry tmpModel = null;
			
			if (isSeries)
				tmpModel = new ModelIMDbSeries();
			else if (isEpisode)
				tmpModel = new ModelIMDbEpisode();
			else
				tmpModel = new ModelIMDbMovie();
			
			// Must be accessible from within inner class
			final ModelIMDbEntry dataModel = tmpModel;
			
			dataModel.setTitle(title);
			dataModel.setDate(date);
			dataModel.setUrlID(urlID);
			
			coverURL = retrieveUrlCover(data, dataModel);
	    				
			final ReentrantLock lock = new ReentrantLock();
			
			if (coverURL != null) {
								
				Thread t = new Thread(new Runnable() {
					public void run() {
						try {
							lock.lock();
							retrieveCover(dataModel);
						} finally {
							lock.unlock();
						}
					}
				});
				t.start();
			}
			
			
			XPathParser.parseDataUsingXPath(dataModel, data, urlID);
			
			/*
			start = 0;
			end = 0;
			
			
		if (classInfo.containsKey("Original Air Date:"))
				airdateContent = getDecodedClassInfo("Original Air Date:", (String) classInfo.get("Original Air Date:"));
						
			// Ex   29 April 2002 (Season 3, Episode 19)
			// Ex: 5 October 1999 (Season 1, Episode 1)
			if (airdateContent != null) {
				Pattern p = Pattern.compile("(.+)?\\s\\(.+?(\\d+?),\\s?.+?(\\d+?)\\)");
				Matcher m = p.matcher(airdateContent);
								
				if (m.find()) {

					int gCount = m.groupCount();

					if (gCount == 3) {
							
						//String airdat = m.group(1);
						String season = m.group(2);
						String episode = m.group(3);
	
						seasonNumber = season;
					    episodeNumber = episode;
					    
					    ((ModelIMDbEpisode) dataModel).setSeasonNumber(seasonNumber);
					    ((ModelIMDbEpisode) dataModel).setEpisodeNumber(episodeNumber);
					}
				}
			}
			*/	
			
			if (urlID != null) {
				/* Gets a bigger plot (if it exists...) */
				retrieveBiggerPlot(data, dataModel);
			}
			
			lock.tryLock((long) 10, TimeUnit.SECONDS);
			
			lastDataModel = dataModel;
			
			return dataModel;
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
		
		return null;
    }
    

    
    
    public StringBuffer getEpisodesStream(ModelIMDbSearchHit modelSeason) {
	
		StringBuffer data = null;
		
		String urlType = "http://akas.imdb.com/title/tt"+ modelSeason.getUrlID() +"/episodes";
	
		try {

			URL url = new URL(urlType);

			try {
				HTTPResult res = httpUtil.readData(url);
	    		data = res.getData();
			} catch (SocketTimeoutException s) {
				log.error("Exception: " + s.getMessage());
				data = null;
			}

			if (data == null) {
				return null;
			}

			//net.sf.xmm.moviemanager.util.FileUtil.writeToFile("HTML-debug/episodeStream.html", data);
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		} 

		return data;
	}
    
    
    
    public ArrayList<ModelIMDbSearchHit> getEpisodes(ModelIMDbSearchHit modelSeason, StringBuffer stream) {

    	ArrayList<ModelIMDbSearchHit> hits = new ArrayList<ModelIMDbSearchHit>();
		
		try {

			String classContent = getDivClass("season-filter-all filter-season-" + modelSeason.getSeasonNumber(), stream);
			
			StringBuffer data = new StringBuffer(classContent);
			
			//net.sf.xmm.moviemanager.util.FileUtil.writeToFile("HTML-debug/episodeStream"+modelSeason.getSeasonNumber()+".html", data);
						
			Pattern p = Pattern.compile("<h3>(.+)\\s?<a href=\"/title/tt(\\d+)/\">(.+?)</a>");
			Matcher m = p.matcher(data);
							
			while (m.find()) {

				int gCount = m.groupCount();

				if (gCount == 3) {
					
					String episode = m.group(1);
					String key = m.group(2);
					String title = m.group(3);

					title = HttpUtil.decodeHTML(title);
					
					title = episode + title;
					
					hits.add(new ModelIMDbSearchHit(key, title, modelSeason.getSeasonNumber()));
				}
			}

		} catch (Exception e) {
			log.error("", e);
		}
	
		return hits;
	}
    
    
    
	public ArrayList<ModelIMDbSearchHit> getSeasons(ModelIMDbSearchHit modelSeries) {

		ArrayList<ModelIMDbSearchHit> hits = new ArrayList<ModelIMDbSearchHit>();
				
		String urlString = "http://akas.imdb.com/title/tt" + modelSeries.getUrlID();
				
		try {

			URL url = new URL(urlString);
			HTTPResult res = httpUtil.readData(url);
			StringBuffer data = res.getData();

			//net.sf.xmm.moviemanager.util.FileUtil.writeToFile("seasonsOutput.html", data);

			String title = "";

			int start = data.indexOf("Seasons:");
			
			/* No season....?. */
			if (start != -1) {
				
				int end = data.indexOf("</div>", start);
				
				String seasons = data.substring(start, end);
				
				int seasonCount = 1;
				String season = "episodes#season-";
				
				while (seasons.indexOf(season + seasonCount) != -1) {
					title = modelSeries.getTitle()+ " - Season "+ seasonCount;
					hits.add(new ModelIMDbSearchHit(modelSeries.getUrlID(), title, seasonCount));
					seasonCount++;
				}
			}
		} catch (Exception e) {
			log.error("", e);
		} 

		return hits;
	}
	
	
	public ModelIMDbEntry getEpisodeInfo(ModelIMDbSearchHit episode) throws Exception {
		
		ModelIMDbEntry model = grabInfo(episode.getUrlID());
		episode.setDataModel(model);
		
		return model;
	}
	

	public ArrayList<ModelIMDbSearchHit> getSeriesMatches(String title) {

		ArrayList<ModelIMDbSearchHit> all = null;
				
		try {
			all = getSimpleMatches(title);
			
			for (int i = 0; i < all.size(); i++) {

				ModelIMDbSearchHit imdb = all.get(i);

				if (!imdb.getTitle().startsWith("\"")) {
					all.remove(i);
					i--;
				}
			}
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
		return all;
	}

	/**
	 * Returns simple matches list...
	 * @throws UnsupportedEncodingException 
	 * @throws UnknownHostException 
	 **/
	public ArrayList<ModelIMDbSearchHit> getSimpleMatches(String title) throws UnsupportedEncodingException, UnknownHostException {
					
		//System.out.println("UTF-8:" + java.net.URLEncoder.encode(title, "UTF-8"));
		//System.out.println("US-ASCII:" + java.net.URLEncoder.encode(title, "US-ASCII"));
		//System.out.println("ISO-8859-1:" + java.net.URLEncoder.encode(title, "ISO-8859-1"));
		
		log.debug("getSimpleMatches:" + title);
		
		return getMatches("http://akas.imdb.com/find?s=tt&q="+ java.net.URLEncoder.encode(title, "ISO-8859-1"));
	}

	public HTTPResult getLastHTTPResult() {
		return lastHTTPResult;
	}
	
    private ArrayList<ModelIMDbSearchHit> getMatches(String strUrl) throws UnknownHostException {

    	try {
    		    		
    		URL url = new URL(strUrl);

    		log.debug("getMatches:" + url);
    		
    		lastHTTPResult = httpUtil.readData(url);
			StringBuffer data = lastHTTPResult.getData();

    		if (data == null) {
    			log.warn("Failed to retrieve data from :" + url);
    			return new ArrayList<ModelIMDbSearchHit>();
    		}

    		return getMatches(data);
    	} catch (UnknownHostException u) {
    		throw u;
    	} catch (Exception e) {
    		log.warn("Exception:" + e.getMessage(), e);
    	}
    	return null;
    }

    public ArrayList<ModelIMDbSearchHit> getMatches(StringBuffer data) {

    	ArrayList<ModelIMDbSearchHit> listModel = new ArrayList<ModelIMDbSearchHit>();

    	try {

    		//new java.io.File("HTML-debug").mkdir();
    		//net.sf.xmm.moviemanager.util.FileUtil.writeToFile("HTML-debug/imdb-search.html", data);

    		int start = 0;
			String key = "";
			String movieTitle = "", year = null, aka = "";
			int titleSTart, titleEnd;
			int movieCount = 0;
			
			/* If there's only one movie for that title it goes directly to that site...  */
			if (!data.substring(data.indexOf("<title>")+7, data.indexOf("<title>")+11).equals("IMDb")) {
			
				/* Gets the title... */
				titleSTart = data.indexOf("<title>", start)+7;
				titleEnd = data.indexOf("</title>", titleSTart);
				movieTitle = HttpUtil.decodeHTML(data.substring(titleSTart, titleEnd));
				
				// get date
				String [] year2 = new String[1];
				StringUtil.removeYearAndAllAfter(movieTitle, year2);
				
				if (year2[0] != null) {
					year = year2[0];
					
					// Clean up title
					movieTitle = movieTitle.replaceFirst("\\("+year+"\\)", "");
				}
				
				if ((start=data.indexOf("title/tt",start) + 8) != 7) {
					key = HttpUtil.decodeHTML(data.substring(start, start + 7));
				}
				
				aka = getDecodedClassInfo("Also Known As:", data);
				aka = aka.trim();
				
				listModel.add(new ModelIMDbSearchHit(key, movieTitle, year, aka));
				
				return listModel;
			}
				
			// Insert newline before each href, as dot in regex will not match newline
			int index = 0;
			while ((index = data.indexOf("<a href", index)) != -1) {
				data.insert(index, "\n");
				index += 2;
			}
			
			int [] movieHitCategoryIndex = new int[4];
			boolean empty = true;
			
			int startIndex = -1;
			
			for (int u = 0; u < movieHitCategory.length; u++) {
				movieHitCategoryIndex[u] = data.indexOf(movieHitCategory[u]);
				
				if (movieHitCategoryIndex[u] != -1)  {
					empty = false;
					
					if (startIndex == -1) {
						startIndex = movieHitCategoryIndex[u];
						data.delete(0, startIndex); // remove the top html 
						movieHitCategoryIndex[u] = data.indexOf(movieHitCategory[u]);
					}
				}
			}
			
			// NO results, returning empty list
			if (empty) {
				return listModel;
			}
			
			// <a href="/title/tt0496424/" onclick="(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=/title/tt0496424/';">&#34;30 Rock&#34;</a> (2006) <small>(TV series)</small>     <div style="font-size: small">&#160;&#45;&#160;Season 3, Episode 11: 
			// <a href="/title/tt0074853/">The Man in the Iron Mask</a> (1977) (TV)</td></tr>
			// <a href="/title/tt0120744/">The Man in the Iron Mask</a> (1998/I)</td></tr>
			// <a href="/title/tt0103064/">Terminator 2: Judgment Day</a> (1991)<br>&#160;aka <em>"Terminator 2 - Le jugement dernier"</em> - France<br>&#160;aka <em>"T2 - Terminator 2: Judgment Day"</em></td></tr>
			
			// <a href="/title/tt0822832/" onclick="(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=/title/tt0822832/';">Marley &#x26; Me</a> (2008)     </td></tr></table> 
			
			
			// should match strings like the above
			
			Pattern p = Pattern.compile("<a\\shref=\"/title/tt(\\d{5,})/\".*?>(.+?)</a>.+?\\((\\d+(/I*)?)\\).*?(;aka\\s<em>.+?</em>)*?(?:</td></tr>|\\n)"); // last group matches series that do not end with </td></tr>
			Matcher m = p.matcher(data);
			
			while (m.find()) {
			
				//int gCount = m.groupCount();
				
				key = m.group(1);
				String title = m.group(2);
				year = m.group(3);
				
				title = HttpUtil.decodeHTML(title);
				
				if (title.equals(""))
					continue;
				
				//Video game
				if (m.group(0).indexOf("VG") != -1) {
					continue;	
				}
				
				// Aka
				aka = grabAkaTitlesFromSearchHit(m.group(0));
				
				int matchIndex = m.start();
				
				String category = null;
				
				for (int i = 0; i < movieHitCategoryIndex.length; i++) {
					
					if (movieHitCategoryIndex[i] != -1 && matchIndex > movieHitCategoryIndex[i])
						category = movieHitCategory[i];
				}
				
				listModel.add(new ModelIMDbSearchHit(key, title, year, aka, category));
				
				movieCount++;
			}
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
	
		/* Returns the model... */
		return listModel;
    }
    
    
    
    public static String grabAkaTitlesFromSearchHit(String substring) {
		
    	/* 
    	  Example: 
    	  
    	  <a href="/title/tt0093773/" onclick="(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=/title/tt0093773/';">
    	    	Predator</a> (1987)  <br>
    		&nbsp;aka <em>"Predator - Jagten er begyndt"</em> - Denmark<br>
    		&nbsp;aka <em>"Pred&#xE1;tor"</em> - Czech Republic<br>
    		&nbsp;aka <em>"Predator - saalistaja"</em> - Finland<br>
    		&nbsp;aka <em>"Predator: Den usynlige fiende"</em> - Norway </td></tr>
    	 */
    	
    	
    	String aka = " ";
    	String akas [] = substring.split("aka ");
    	
    	// skips the first index which is the original title
    	for (int i = 1; i < akas.length; i++) {
    		
    		if (!aka.equals(" "))
    			aka += "\r\n ";
    		    		
    		aka += HttpUtil.decodeHTML(akas[i]);
    	}
    	return aka;
    }
  
   // <div.*?class=\"info.+?<.+?>(.*?)(\(?:.*?\))?<.+?>(.+?)</div>
   // <div.*?class="info">.*?<.+?>(.+?)</.+?>.*?<
    protected HashMap<String, String> decodeClassInfo(StringBuffer data) {
    		
    	HashMap<String, String> classInfo = new HashMap<String, String>();
    	
    	Pattern contentPattern = Pattern.compile("<div.*?class=\"info\">.*?<.+?>(.+?)(?:\\(.*?)?</.+?>.*?<div\\sclass=\"info-content.*?\">(.+?)</div>", Pattern.DOTALL);
		    	
    	// Find start index of all class info, which is the conten we seek
    	Pattern classStart = Pattern.compile("<div.*?class=\"info\">");
    	ArrayList<Integer> infoStart = new ArrayList<Integer>();
    	
    	Matcher m = classStart.matcher(data);
    	while (m.find()) {
    		infoStart.add(m.start());
    	}
    	    	    	
		try {
			
			while (!infoStart.isEmpty()) {
												
				// Get the next <div.*?class=\"info\">
				
				int index_start = infoStart.remove(0);
				int next_info = data.length();
					
				if (!infoStart.isEmpty())
					next_info = infoStart.get(0);
											
				m = contentPattern.matcher(data.substring(index_start, next_info));
								
				if (m.find()) {
										
					String className = m.group(1);
					String info = m.group(2);

					if (className != null && info != null) {
						className = className.trim();

						if (!className.endsWith(":"))
							className += ":";

						if (className.equals("Writers:")) {
							//System.out.println("Writers put info:" + info);
						}
						
						classInfo.put(className, info);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return classInfo;
    }
    
    
    
    /**
     * Grabs the content of a class info containing the classname
     **/
    
    protected static String getClassInfo(StringBuffer data, String className) {
    	String tmp = "";

    	int start = 0;
    	int end = 0;

    	while ((start = data.indexOf("<div class=\"info", end)) != -1 && 
    			(end = data.indexOf("</div>", start)) != -1) {

    		tmp = data.substring(start, end);	

    		if (tmp.indexOf(className) != -1) {
    			start = tmp.indexOf(className) + className.length();
    			tmp = tmp.substring(start, tmp.length());	
    			tmp = tmp.trim();
    			break;
    		}
    		tmp = "";
    	}
    	return tmp;
    }
    
    
    protected static String getDecodedClassInfo(String className, StringBuffer data) throws Exception {
    	String tmp = getClassInfo(data, className);
    	return getDecodedClassInfo(className, tmp);
    }
    
    /**
     * Grabs the content of a class info containing the classname
     * and cleans it up by removing html and paranthesis.
     * @throws Exception 
     **/
    protected static String getDecodedClassInfo(String className, String classInfo) throws Exception {
    	String decoded = "";
    	String tmp = classInfo;

    	if (tmp == null)
    		throw new Exception("classInfo cannot be null!");
    	
    	if (className == null)
    		className = "";
    	
    	try {
    		int end = 0;
    		
    		//tmp = getClassInfo(data, className);
    		    		    		
    		end = tmp.indexOf("<a class=\"tn15more");
    			
    		// Link to "more" will be removed
    		if (end != -1) {
    			tmp = tmp.substring(0, end);
    		} 
    				
			if (className.equals("Also Known As:")) {
				decoded = decodeAka(tmp);
			}
			else if (className.equals("class=\"cast\">")) {
				decoded = decodeCast(tmp).trim();
			}
			else {
				decoded = HttpUtil.decodeHTML(tmp);
				decoded = decoded.replaceAll("\\|", ",");
				decoded = decoded.replaceAll("\r\n|\n|\r", " ");
				
				// Removes all space before comma
				while (decoded.indexOf(" ,") != -1) {
					decoded = decoded.replaceAll("\\s,", ",");
				}
				
				decoded = StringUtil.removeDoubleSpace(decoded);
				decoded = decoded.trim();
			}
    	} catch (Exception e) {
    		log.error("Exception:" + e.getMessage(), e);
    	} 
    	/* Returns the decoded string... */
    	return decoded;
    }
    
    
    
    
    /**
     * Returns the name of the link; <a href="">name</a>
     **/
    protected static ArrayList<String> getLinkContentName(String toDecode) {
    	ArrayList<String> decoded = new ArrayList<String>();
    	String tmp = "";
		
		try {
			int start = 0;
			int end = 0;
	    
			while ((start = toDecode.indexOf("<a href=", start)) != -1) {
	    	
				start = toDecode.indexOf(">", start) +1;
				end = toDecode.indexOf("</a>", start);
	    	
				tmp = toDecode.substring(start, end);
				decoded.add(HttpUtil.decodeHTML(tmp.trim()));
			}
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		} 
		/* Returns the decoded string... */
		return decoded;
    }
    
    
    /**
     * Returns the aka titles.
     **/
    protected static String decodeAka(String toDecode) {
		String decoded = " ";
		
		try {
			String [] akaTitles = toDecode.split("<br>");
		
			for (int i = 0; i < akaTitles.length; i++) {
				
				if (!decoded.equals(" "))
					decoded += System.getProperty("line.separator");
					
				decoded += HttpUtil.decodeHTML(akaTitles[i].trim());
			}
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		} 
		/* Returns the decoded string... */
		return decoded;
    }
    
    
    /**
     * Decodes a html string and returns its unicode string.
     **/
    protected static String decodeCast(String toDecode) {
    	
    	StringBuffer decoded = new StringBuffer();
    	
    	try {
    		try {
    			String [] castSplit = toDecode.split("<td class=\"hs\">");
    		 	//Pattern p = Pattern.compile("<a\\shref=\"/name/nm\\d+/.+?castlist.+?>(.+?)</a>.+?\\.\\.\\..+?<td\\sclass=\"char\">(?:<a\\shref=\"/character/ch\\d+/\">)?(.+?)</td>");
    			    			
    			for (int i = 0; i < castSplit.length; i++) {
    				    				
    				String nmClass = getCustomElementClass("td", "nm", new StringBuffer(castSplit[i]));
    				String charClass = getCustomElementClass("td", "char", new StringBuffer(castSplit[i]));
    				    				
    				if (nmClass == null || charClass == null)
    					continue;
    					
    				ArrayList<String> nm = getLinkContentName(nmClass);
    				String character = HttpUtil.decodeHTML(charClass); //getLinkContentName(charClass);
    				
    				String name = nm.get(0);
					decoded.append(name);
					
					if (!character.trim().equals("")) {
						if (!(character.trim().startsWith("(") && character.trim().endsWith(")")))
							character = " (" + character + ")";
						decoded.append(character);
					}
					
					decoded.append(", ");
					
    				/*
    				Matcher m = p.matcher(castSplit[i]);
    								
    				if (m.find()) {

    					int gCount = m.groupCount();
	
    					if (gCount == 2) {
    							
    						String name = m.group(1);
    						String character = m.group(2);
    						System.out.println("name:" + name);
    						decoded.append(name);
        					decoded.append(" (" + character + "), ");
    					}
    				}
    				*/
    			}
    			
    			// Removes spaces at end
    			while (decoded.length() > 0 && decoded.charAt(decoded.length() - 1) == ' ')
    				decoded = decoded.deleteCharAt(decoded.length() - 1);

    			// Replace comma at the end
    			if (decoded.length() > 0 && decoded.charAt(decoded.length() - 1) == ',') 
    				decoded = decoded.replace(decoded.length() - 1, decoded.length(), "");

    		} catch (Exception e) {
    			log.error("Exception:" + e.getMessage(), e);
    		} 
    	} catch (Exception e) {
    		log.error("Exception:" + e.getMessage(), e);
    	}
    	/* Returns the decoded string... */
    	return decoded.toString();
    }
    
    
    
    /**
     * Get the content of the <div class="XX">
     * @param classname
     * @param buffer
     * @return
     */
    public static String getDivClass(String classname, StringBuffer buffer) {
    
    	int safety = 10000;
    	
    	String searchStr = "<div class=\""+ classname + "\">";
    	
    	int start = buffer.indexOf(searchStr);
    	
    	if (start == -1)
    		return null;
    	
    	start += 3;
    	
    	int end = start;
    	
    	int div_count = 1;
    	
    	while (div_count > 0) {
    	
    		if (safety-- == 0)
    			break;
    		
    		int i = buffer.indexOf("div", end);
    		
    		if (i != -1) {
    			
    			if (buffer.charAt(i-1) == '<')
    				div_count++;
    			else if (buffer.charAt(i-1) == '/')
    				div_count--;

    			end = i + 3;
    		}
    		else
    			break;
    	}
     	
    	if (start > 0 && end < buffer.length() && start < end)
    		return buffer.substring(start, end);
    	else
    		return null;
    }
    
        
    
  
    public static String getCustomElementClass(String element, String classname, StringBuffer buffer) {
        	
    	String searchStr = "<" +element+ " class=\""+ classname + "\">";
    	String elementEnd = "</" +element+ ">";
    	
    	int start = buffer.indexOf(searchStr);
    	
    	if (start == -1)
    		return null;
    	
    	int end = buffer.indexOf(elementEnd, start);
     	
    	if (end == -1)
    		return null;
    	
    	if (start > 0 && end < buffer.length() && start < end)
    		return buffer.substring(start, end);
    	else
    		return null;
    }
    
    
    public ModelIMDbEntry getLastDataModel() {
    	return lastDataModel;
    }
    	
   
         
    
    /**
     * Returns the title where 'The', 'A' and 'An' are moved to the end of the title, If the settings are set.
     * @return
     */
    public String getModifiedTitle(String title) {
    	    	
    	if (settings != null) {
    		
    		if (settings.getRemoveQuotesOnSeriesTitles()) {
    			if (title.startsWith("\"") && title.endsWith("\"")) {
    				title = title.substring(1, title.length()-1);
    			}
    		}
    		
    		if (settings.getAutoMoveThe() && title.startsWith("The ")) {
    			title = title.substring(title.indexOf(" ")+1, title.length())+ ", The";
    		}
    		else if (settings.getAutoMoveAnAndA() && (title.startsWith("A ") || title.startsWith("An "))) {
    			title = title.substring(title.indexOf(" ")+1, title.length())+ ", "+ title.substring(0, title.indexOf(" "));
    		} 
    	}
    	return title;
    }
    
    
    
    /**
     * Gets the cover.
     **/
    private boolean retrieveSmallCover(ModelIMDbEntry dataModel) {
      
		byte[] coverData = null;
		
		try {
			if (dataModel.getCoverURL() != null && !dataModel.getCoverURL().equals("")) {
				coverData = httpUtil.readDataToByteArray(new URL(dataModel.getCoverURL()));
			}
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		} 
	
		dataModel.setCoverData(coverData);
		/* Returns the data... */
		return coverData != null;
    }


    private boolean retrieveCover(ModelIMDbEntry dataModel) {
    	
    	if (!retrieveBiggerCover(dataModel))
    		retrieveSmallCover(dataModel);
    	else {
    		dataModel.setCoverData(dataModel.getBigCoverData());
    	}
    	return true;    	
    }
    
    public boolean retrieveBiggerCover(ModelIMDbEntry dataModel) {
   	
    	// Disable method for now
    	if (true)
    		return false;
    	
    	try {
			throw new Exception();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	URL url;

    	byte [] coverData = null;
    	
    	try {
    		url = new URL("http://akas.imdb.com/media/" + dataModel.bigCoverUrlId);
    		
    		System.out.println("url:" + url);
    		
    		HTTPResult res = httpUtil.readData(url);
    		StringBuffer data = res.getData();

    		int imgIndex = data.indexOf("<img oncontextmenu");

    		if (imgIndex != -1) {

    			String tmp = data.substring(imgIndex, data.indexOf(">", imgIndex));

    			//System.out.println("tmp:" + tmp);
    			
    			//src="http://ia.media-imdb.com/images/M/MV5BMTI4ODg5MjkwMl5BMl5BanBnXkFtZTcwNTkzMjYyMQ@@._V1._SX307_SY400_.jpg">

    			Pattern p = Pattern.compile("src=\"(.+)\"");

    			Matcher m = p.matcher(tmp);

    			if (m.find()) {

    				//String g = m.group();

    				//System.out.println("BC g:" + m.group(0));
    				//System.out.println("BC g1:" + m.group(1));

    				coverData = httpUtil.readDataToByteArray(new URL(m.group(1)));
    				
    				//dataModel.bigCoverUrlId = m.group(1);
    			}

    		}

    	} catch (SocketTimeoutException s) {
    		log.error("Exception: " + s.getMessage());
    	} catch (Exception e) {
    		log.error("Exception:" + e.getMessage(), e);
    	}

    	dataModel.setBigCoverData(coverData);
    	
    	//http://ia.media-imdb.com/images/M/MV5BMTI4ODg5MjkwMl5BMl5BanBnXkFtZTcwNTkzMjYyMQ@@._V1._SX307_SY400_.jpg

    	return coverData != null;
    }

    /*
    if (coverData == null)      // previous routine failed. Now try changing resolution in cover image URL
    	+           {
    	+   
    	+               if (dataModel.getCoverURL() != null && !dataModel.getCoverURL().equals(""))
    	+               {
    	+                   String tmp = dataModel.getCoverURL();
    	+
    	+                   Pattern p = Pattern.compile("(.*)(._SX)(\\d+)(_SY)(\\d+)(.*)");
    	+               
    	+                   Matcher m = p.matcher(tmp);
    	+               
    	+                   if (m.find() && m.groupCount() == 6)
    	+                   {
    	+                       url = new URL(m.group(1) + m.group(2) + m.group(3)+"0" + m.group(4) + m.group(5)+"0" +m.group(6));
    	+                           // try 10 times the resolution. imdb will return the maximum resolution possible
    	+               
    	+                       //System.out.println("retrBiggerCover url:" + url);
    	+                       coverData = httpUtil.readDataToByteArray( url );
    	+                   }
    	+               }
    	+           }
*/
  
    
    
    public HTTPResult getVoteHistory() throws Exception {
   	
    	URL url = new URL("http://www.imdb.com/mymovies/list?votehistory");
    	lastHTTPResult = httpUtil.readData(url);
    	return lastHTTPResult;
    }
    
    
    public ArrayList<ModelIMDbListHit> getVotedMovies() throws Exception {
    	
    	ArrayList<ModelIMDbListHit> movies = new ArrayList<ModelIMDbListHit>();
    	
    	HTTPResult res = getVoteHistory();
    	
    	ArrayList<URL> pages = getListPages(res.getData());
    	
    	// All hits are on one page
    	if (pages.size() == 0) {
    		Document doc = XPathParser.parseXPath(res.getData(), res.getUrl().toString());
    		ArrayList<ModelIMDbListHit> list = parseList(doc);
    		movies.addAll(list);
    	}
    	else {
    		
    		for (URL page : pages) {

    			res = httpUtil.readData(page);

    			Document doc = XPathParser.parseXPath(res.getData(), page.toString());

    			ArrayList<ModelIMDbListHit> list = parseList(doc);

    			movies.addAll(list);
    		}
    	}
    	
    	return movies;
    }
    
    ArrayList<URL> getListPages(StringBuffer data) throws XPathExpressionException, SAXException, IOException {
    	
    	ArrayList<URL> ret = new ArrayList<URL>();
    	
    	Pattern p;
		Matcher m;
    	
    	Document document = XPathParser.parseXPath(data, "");
    	
    	// Get total list size
    	String expression = "html//table/tr/td/table/tr/td/table/tr/td[@class='standard' and @colspan='2']";
    	expression = "html//td[@class='standard' and @colspan='2']";
    	 	
    	ArrayList<Element> elements = XPathParser.evaluateDocument(document, expression);
		    	
    	int size = -1;
    	String listID = null;
		int listSize = -1;
    	
    	for (Element e : elements) {
    		//XPathParser.printNodeTree(e);
    		
    		p = Pattern.compile(".*?Titles in total:.*?(\\d+).*");
    		m = p.matcher(e.getTextContent());
    		    		
    		if (m.find()) {
    			size = Integer.parseInt(m.group(1));
    		}
    	}
				
    	expression = "html//table/tr/td[@align='right' and @class='standard']/a";
    	    		
    	elements = XPathParser.evaluateDocument(document, expression);
    	
    	for (Element e : elements) {
    		//XPathParser.printNodeTree(e);
    		    		
    		String link = e.getAttribute("href");
    		
    		// on the form "list?l=6308135&o=10"
    		p = Pattern.compile("list\\?l=(\\d+)&o=(\\d+)");
    		m = p.matcher(link);
    		
    		if (m.find()) {
    			listID = m.group(1);
    			listSize = Integer.parseInt(m.group(2));
    			break;
    		}
    	}    	
    	    	
    	String url = "http://www.imdb.com/mymovies/list?l={listID}&o={listSize}";
    	
    	int pageCount = size/listSize;
    	    	
    	for (int i = 0; i <= pageCount; i++) {
    		ret.add(new URL(url.replace("{listID}", listID).replace("{listSize}", "" + (listSize*i))));
    	}
    	
    	return ret;
    }
    
    public ArrayList<ModelIMDbListHit> parseList(Document document) throws XPathExpressionException, SAXException, IOException {
    	
    	ArrayList<ModelIMDbListHit> ret = new ArrayList<ModelIMDbListHit>();
    	    	
    	//eval = "html//div[@class='info']|html//div[@class='info stars']";
    	//ArrayList<Element> elements = parser.parseXPath(data, "", "html//table/tr/form[name='list']/*");
    	
    	String expression = "html//form[@name='list']/tr/td/a";
    	
    	ArrayList<Element> elements = XPathParser.evaluateDocument(document, expression);
    	
    	//ArrayList<Element> elements = parser.parseXPath(data, "", expression);
    	
    	// /title/tt0118636/
    	Pattern p;
    	
    	for (Element e : elements) {
    		//XPathParser.printNodeTree(e);
    	
    		String urlID = null;
    		String link = e.getAttribute("href");
    		String title = e.getTextContent();
    		    		
    		p = Pattern.compile("/title/tt(\\d+)/");
    		Matcher m = p.matcher(link);

			if (m.find()) {
				urlID = m.group(1);
			}

			// On the form "Once Were Warriors (1994)"
    		String date = e.getParentNode().getTextContent();
    		date = date.substring(title.length() +1);
    		
    		p = Pattern.compile(".+?(\\d+).+");
    		m = p.matcher(date);
			
    		if (m.find()) {
    			date = m.group(1);
			}
    		
    		ModelIMDbListHit hit = new ModelIMDbListHit(urlID, title, date);
    		
    		
    		try {
				// get personal vote
				Node n = e.getParentNode().getNextSibling().getNextSibling();
				
				if (n != null) {
					hit.setVote("" + Integer.parseInt(n.getTextContent()));
				}
			} catch (Exception e1) {
				log.warn("Rating not found for " + title);
			}
    		
    		
			if (urlID != null) {
				ret.add(hit);
			}
    	}
    	    	
    	return ret;
    }
}
