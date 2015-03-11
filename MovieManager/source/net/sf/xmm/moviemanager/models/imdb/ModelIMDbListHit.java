/**
 * @(#)ModelSearchHit.java 1.0 19.01.06 (dd.mm.yy)
 *
 * Copyright (2003) Bro
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


public class ModelIMDbListHit extends ModelIMDbSearchHit {

	public ModelIMDbListHit(String key, String title, String date) {
		super(key, title, date);
	}
	
	
	String vote = "";
	
	public String getVote() {
		return vote;
	}
	
	public void setVote(String vote) {
		this.vote = vote;
	}
	
	@Override
	public String toString() {
		
		String str = getTitle();
		
		if (getDate() != null && !getDate().equals(""))
			str += " (" + getDate() + ")";
			
		if (getVote() != null)
			str += ":" + getVote();
		
		return str;
	}	
}
