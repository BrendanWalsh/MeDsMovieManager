package net.sf.xmm.moviemanager.models;
/**
 * @(#)ModelQuery.java 1.0 23.03.05 (dd.mm.yy)
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

public class ModelQuery {

  /**
   * The name to list.
   **/
  private String _name;
  
  /**
   * The query.
   **/
  private String _query;

  /**
   * Another constructor.
   **/
  public ModelQuery(String name, String query) {
    _name = name; 
    _query = query;
  }

  /**
   * Gets the name.
   **/
  protected String getName() {
    return _name; 
  }

  /**
   * Gets the query.
   **/
  public String getQuery() {
    return _query;
  }
  
  /**
   * Returns the title.
   **/
  public String toString() {
      return _name;
  }
    
}
