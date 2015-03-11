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

package net.sf.xmm.moviemanager.models.imdb;

public class ModelIMDbSeries extends ModelIMDbMovie {
    
	public ModelIMDbSeries() {}
	
    /* Empty constructor for XML export */
  
	public ModelIMDbSeries(String urlID, String cover, String date, String title, 
			String directedBy, String writtenBy, String genre, String rating, String plot, String cast,
			String aka, String country, String language, String colour, 
			String certification, String mpaa, String webSoundMix, String webRuntime, String awards) {
		
		super(urlID, cover, date, title, directedBy, writtenBy, genre, 
				rating, plot, cast, aka, country, language, colour, certification, mpaa,
				webSoundMix, webRuntime, awards);
			
	}
    
    public boolean isSeries() {
		return true;
	}
    
}
