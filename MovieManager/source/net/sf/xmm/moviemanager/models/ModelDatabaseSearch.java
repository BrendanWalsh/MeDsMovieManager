/**
 * @(#)ModelDatabaseSearch.java 1.0 24.01.06 (dd.mm.yy)
 *
 * Copyright (2003) Mediterranean
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
 * Contact: mediterranean@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.models;

import java.util.ArrayList;
import java.util.HashMap;


public class ModelDatabaseSearch {
    
    /* Which column to search */
    private String filterCategory;
    
    /* Only applies if filterCategory == "Movie Title" */
    private boolean includeAkaTitlesInFilter = false;
    
    
    /* The string to search for */
    private String filterString = "";
    
    /* ORDER BY column */
    private String orderCategory = null;
    
    public ArrayList<String> searchTerms = new ArrayList<String>();
    
    public boolean getFullGeneralInfo = true;
    
    public void addSearchTerm(String term) {
    	searchTerms.add(term);
    }
       
    // MySQL
    public boolean getCoverData = false;
    
    /**
       Tells if the filter should filter out seen/unseen movies.
       0 == off
       1 == off
       2 == show only seen, 
       3 == show only unseen
    **/
    private int seen = 0; 
    
    /* Currently chosen lists */
    private ArrayList<String> listNames = new ArrayList<String>();
    
    private int listOption = 0; /* 1 == on, 0 == off */
    private boolean showUnlistedEntries = true;
    
    
    /** 0 == disabled
	1 == disabled
	2 == show only above the ratingValue,
	3 == show only below the ratingValue.
    **/	
    private int ratingOption = 0;
    private double rating = 0;
    
    /**
       0 == disabled 
       1 == disabled 
       2 == show only above the dateValue,
       3 == show only below the dateValue.
    **/
    private int dateOption = 0; 
    private String date = "";
    
    public boolean duplicates = false;
    
    private HashMap<String, String> searchAlias;
    
    /* Used by the database  */
    public  boolean where;
    
    
    /* 'AND' or 'OR' */
    private String defaultOperator = "AND";
    
    public String getDefaultOperator() {
	return defaultOperator;
    }
    
    public void setDefaultOperator(String defaultOperator) {
	this.defaultOperator = defaultOperator;
    }
    
    public String getFilterCategory() {
	return filterCategory;
    }

    public void setFilterCategory(String filterCategory) {
	if (filterCategory.equals("Movie Title"))
	    this.filterCategory = "Title";
	else
	    this.filterCategory = filterCategory;
    }
    
    public String getFilterString() {
	return filterString;
    }

    public void setFilterString(String filterString) {
	this.filterString = filterString;
    }
    
    public String getOrderCategory() {
	return orderCategory;
    }

    public void setOrderCategory(String orderCategory) {
	this.orderCategory = orderCategory;
    }
    
    public int getSeen() {
	return seen;
    }
    
    public void setSeen(int seen) {
	this.seen = seen;
    }
    
    public ArrayList<String> getCurrentListNames() {
    	return listNames;
    }

    public void setCurrentListNames(ArrayList<String> listNames) {
    	this.listNames = listNames;
    }
    
    public int getListOption() {
	return listOption;
    }
    
    public void setListOption(int listOption) {
	this.listOption = listOption;
    }
	
    public boolean getShowUnlistedEntries() {
    	return showUnlistedEntries;
    }
        
    public void setShowUnlistedEntries(boolean val) {
    	showUnlistedEntries = val;
    }
    
    
    public int getRatingOption() {
	return ratingOption;
    }
    
    public void setRatingOption(int ratingOption) {
	this.ratingOption = ratingOption;
    }
    
    public double getRating() {
	return rating;
    }
    
    public void setRating(double rating) {
	this.rating = rating;
    }
    
    public int getDateOption() {
	return dateOption;
    }

    public void setDateOption(int dateOption) {
	this.dateOption = dateOption;
    }
    
    public String getDate() {
	return date;
    }

    public void setDate(String date) {
	this.date = date;
    }
    
    public HashMap<String, String> getSearchAlias() {
	return searchAlias;
    }

    public void setSearchAlias(HashMap<String, String> searchAlias) {
	this.searchAlias = searchAlias;
    }
    
    public void setIncludeAkaTitlesInFilter(boolean includeAkaTitlesInFilter) {
	this.includeAkaTitlesInFilter = includeAkaTitlesInFilter;
    }
    
    public boolean getIncludeAkaTitlesInFilter() {
	return includeAkaTitlesInFilter;
    }
    
    public String toString() {
	return this.toString();
    }
}
