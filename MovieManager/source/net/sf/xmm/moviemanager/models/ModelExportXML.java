/**
 * @(#)ModelExportXML.java
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


package net.sf.xmm.moviemanager.models;

import java.util.ArrayList;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.util.StringUtil;

import org.apache.log4j.Logger;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.mapping.ClassDescriptor;
import org.exolab.castor.mapping.FieldDescriptor;
import org.exolab.castor.xml.NodeType;
import org.exolab.castor.xml.TypeValidator;
import org.exolab.castor.xml.UnmarshalState;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLClassDescriptor;
import org.exolab.castor.xml.XMLFieldDescriptor;


public class ModelExportXML implements XMLClassDescriptor {

	Logger log = Logger.getLogger(getClass());
	
	private String movieManagerVersion = MovieManager.getConfig().sysSettings.getVersion();
	
    private ArrayList<ModelMovie> movies;
    private ArrayList<ModelSeries> series;
    
    public ModelExportXML() {
    	movies = new ArrayList<ModelMovie>();
        series = new ArrayList<ModelSeries>();
    }
    
    public ModelExportXML(int size) {
    	movies = new ArrayList<ModelMovie>(size);
        series = new ArrayList<ModelSeries>(size);
    }
    
    public String getMovieManagerVersion() {
    	return movieManagerVersion;
    }
    
    public ArrayList<Object> getCombindedList() {
	
        ArrayList<Object> combinded = new ArrayList<Object>(movies.size() + series.size());
        
        combinded.addAll(movies);
        combinded.addAll(series);
        
        return combinded;
    }
    
    // Included all movies and episodes
    public int getAllEntriesCount() {
    	
    	int count = movies.size();
    	
    	for (int i = 0; i < series.size(); i++) {
    		count += ((ModelSeries) series.get(i)).getEpisodes().size();
    	}
    	return count;
    }
    
    public ArrayList<ModelMovie> getMovies() {
     return movies;   
    }
    
    public ArrayList<ModelSeries> getSeries() {
    	return series;   
    }

    public void setMovies(ArrayList<ModelMovie> movies) {
    	this.movies = movies;   
    }

    public void setSeries(ArrayList<ModelSeries> series) {
    	this.series = series;   
    }

    public void addModelMovie(ModelMovie movie) {
    	movies.add(movie);
    }

    
    public void addModelSerie(ModelSeries serie) {
        series.add(serie);
    }
    
   public void checkDescriptorForCorrectOrderWithinSequence(XMLFieldDescriptor elementDescriptor) throws ValidationException {
   // Does nothing
    }
    
    public void checkDescriptorForCorrectOrderWithinSequence(XMLFieldDescriptor edesc, UnmarshalState state, String s) throws ValidationException {
    	   // Does nothing
     }

    public XMLFieldDescriptor[] getAttributeDescriptors() {
        log.debug("getAttributeDescriptors()");
        return null;
    }

    public XMLFieldDescriptor getContentDescriptor() {
        log.debug("getContentDescriptor()");
        return null;
    }

    public XMLFieldDescriptor[] getElementDescriptors() {
        log.debug("getElementDescriptors()");
        return null;
    }

    public XMLFieldDescriptor getFieldDescriptor(String arg0, String arg1, NodeType arg2) {
        log.debug("getFieldDescriptor");
        return null;
    }

    public String getNameSpacePrefix() {
        log.debug("getNameSpacePrefix()");
        return null;
    }

    public String getNameSpaceURI() {
        log.debug("getNameSpaceURI()");
        return null;
    }

    public TypeValidator getValidator() {
        log.debug("getValidator()");
        return null;
    }

    public String getXMLName() {
        log.debug("getXMLName()");
        return null;
    }

    public boolean canAccept(String arg0, String arg1, Object arg2) {
		log.debug("canAccept");
        return false;
    }

    public Class<?> getJavaClass() {
        log.debug("getJavaClass()");
        return null;
    }

    public FieldDescriptor[] getFields() {
        log.debug("getFields()");
        return null;
    }

    public ClassDescriptor getExtends() {
        log.debug("getExtends()");
        return null;
    }

    public FieldDescriptor getIdentity() {
        log.debug("getIdentity()");
        return null;
    }

    public AccessMode getAccessMode() {
        log.debug("getAccessMode()");
        return null;
    }

	public boolean isChoice() {
		return false;
	}
	
	/*
	 * Not currently used
	 */
	 void validateModel(ModelMovie movie) {
	    	
	    	StringUtil.removeInvalidXMLCharacters(movie.getTitle());
	    	StringUtil.removeInvalidXMLCharacters(movie.getAka());
	    	StringUtil.removeInvalidXMLCharacters(movie.getAwards());
	    	StringUtil.removeInvalidXMLCharacters(movie.getCast());
	    	StringUtil.removeInvalidXMLCharacters(movie.getCertification());
	    	StringUtil.removeInvalidXMLCharacters(movie.getColour());
	    	StringUtil.removeInvalidXMLCharacters(movie.getCompleteUrl());
	    	StringUtil.removeInvalidXMLCharacters(movie.getCountry());
	    	StringUtil.removeInvalidXMLCharacters(movie.getCover());
	    	StringUtil.removeInvalidXMLCharacters(movie.getDate());
	    	StringUtil.removeInvalidXMLCharacters(movie.getDirectedBy());
	    	StringUtil.removeInvalidXMLCharacters(movie.getGenre());
	    	StringUtil.removeInvalidXMLCharacters(movie.getLanguage());
	    	StringUtil.removeInvalidXMLCharacters(movie.getMpaa());
	    	StringUtil.removeInvalidXMLCharacters(movie.getNotes());
	    	StringUtil.removeInvalidXMLCharacters(movie.getPlot());
	    	StringUtil.removeInvalidXMLCharacters(movie.getRating());
	    	StringUtil.removeInvalidXMLCharacters(movie.getSortCategory());
	    	StringUtil.removeInvalidXMLCharacters(movie.getSortDate());
	    	StringUtil.removeInvalidXMLCharacters(movie.getUrlKey());
	    	StringUtil.removeInvalidXMLCharacters(movie.getWebRuntime());
	    	StringUtil.removeInvalidXMLCharacters(movie.getWebSoundMix());
	    	StringUtil.removeInvalidXMLCharacters(movie.getWrittenBy());
	    	
	    	ModelAdditionalInfo add = movie.getAdditionalInfo();
	        	
	    	StringUtil.removeInvalidXMLCharacters(add.getAdditionalInfoString());
	    }

	public Object getProperty(String arg0) {
		return null;
	}

	public void setProperty(String arg0, Object arg1) {
	}

	public void addNature(String arg0) {
	}

	public boolean hasNature(String arg0) {
		return false;
	}
}
