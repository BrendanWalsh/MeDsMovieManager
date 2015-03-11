/**
 * @(#)ModelIMDB.java 1.0 23.03.05 (dd.mm.yy)
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

public class ModelIMDB {

	/**
	 * The IMDB database key for this movie.
	 **/
	private String _key;

	/**
	 * The title.
	 **/
	private String _title;

	/**
	 * The aka titles.
	 **/
	private String _aka;

	/**
	 * The constructor.
	 **/
	public ModelIMDB(String key, String title, String aka) {
		_key = key; 
		_title = title;
		_aka = aka;
	}

	/**
	 * Gets the key.
	 **/
	public String getKey() {
		return _key; 
	}

	/**
	 * Gets the title.
	 **/
	public String getTitle() {
		return _title;
	}

	/**
	 * Gets the title.
	 **/
	public String getAka() {
		return _aka;
	}

	/**
	 * Returns the title.
	 **/
	public String toString() {
		return _title;
	}

}
