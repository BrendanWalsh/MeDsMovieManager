/**
 * @(#)ModelEntry.java 29.01.06 (dd.mm.yy)
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

public class ModelSeries {
    
    ModelMovie movie;

    public ArrayList<ModelEpisode> episodes = new ArrayList<ModelEpisode>();
    
    /* Empty constructor for XML export */
    public ModelSeries() {}
    
    public ModelSeries(ModelMovie movie) {
        this.movie = movie;
    }
    
    public int getMovieKey() {
    	return movie.getKey();
    }
    
    public ModelMovie getMovie() {
        return movie;
    }
    
    public void setMovie(ModelMovie movie) {
        this.movie = movie;
    }

    public void addEpisode(ModelEpisode episode) {
        episodes.add(episode);
    }

    public ArrayList<ModelEpisode> getEpisodes() {
     return episodes;   
    }
}
