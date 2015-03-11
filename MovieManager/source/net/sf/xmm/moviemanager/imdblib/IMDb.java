package net.sf.xmm.moviemanager.imdblib;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import net.sf.xmm.moviemanager.http.HttpUtil.HTTPResult;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbEntry;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbSearchHit;

public interface IMDb {

	public HTTPResult getLastHTTPResult();
	
	public ModelIMDbEntry getLastDataModel();
	
	public HTTPResult getURLData(String urlID) throws Exception;
	public ModelIMDbEntry grabInfo(String urlID, StringBuffer data) throws Exception;
	public ModelIMDbEntry getEpisodeInfo(ModelIMDbSearchHit episode) throws Exception;
	public boolean retrieveBiggerCover(ModelIMDbEntry dataModel);
	
	public ArrayList<ModelIMDbSearchHit> getSimpleMatches(String title) throws UnsupportedEncodingException, UnknownHostException;
	public ArrayList<ModelIMDbSearchHit> getSeriesMatches(String title);
	public ArrayList<ModelIMDbSearchHit> getSeasons(ModelIMDbSearchHit modelSeries);
	public ArrayList<ModelIMDbSearchHit> getEpisodes(ModelIMDbSearchHit modelSeason, StringBuffer stream);
	
	public StringBuffer getEpisodesStream(ModelIMDbSearchHit modelSeason);
	
	
	
	
	
	
	
	
	
}
