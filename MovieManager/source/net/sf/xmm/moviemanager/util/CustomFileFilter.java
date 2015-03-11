/**
  * @(#)CustomFileFilter.java 1.0 21.04.05 (dd.mm.yy)
 *
 * Implementation of the Filter for Access DB's
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

package net.sf.xmm.moviemanager.util;

import java.io.File;
 
public class CustomFileFilter extends javax.swing.filechooser.FileFilter {
	
    public static final int	DIRECTORIES_ONLY = 1;
    public static final int	FILES_AND_DIRECTORIES = 2;
    public static final int	FILES_ONLY = 0;
	
    int fileMode = FILES_AND_DIRECTORIES;
	
    /**
     * The filter extensions.
     **/
    private String [] _extensions;
  
    /**
     * The filter description.
     **/
    private String _description;
	private String identifier = "";
    
    
    /* Accepts only directores */
    public CustomFileFilter(int fileMode, String description) {
	this.fileMode = fileMode;
	_description = description;
    }
	
    /**
     * Initializes the private vars.
     **/
    public CustomFileFilter(String[] extensions, String description, String identifier) {
    	this(extensions, description);
    	this.identifier = identifier;
    }
    
    /**
     * Initializes the private vars.
     **/
    public CustomFileFilter(String[] extensions, String description) {
    	super();
    	_extensions = extensions;
    	_description = description;
    }
  
    /**
     * Accept all directories and all extensions in extensions[] files.
     *
     * @return Whether the given file is accepted by this filter.
     **/
    public boolean accept(File file) {
	
	if (file.isDirectory())
	    return true;
	else if (fileMode == DIRECTORIES_ONLY)
		return false;
	
	String extension = null;
	String str = file.getName();
	int i = str.lastIndexOf('.');
    
	if (i > 0 &&  i < str.length() - 1) {
	    extension = str.substring(i+1).toLowerCase();
	}
	if (extension != null) {
	    for (i=0; i<_extensions.length; i++) {
		if (_extensions[i].equals("*.*") || extension.equals(_extensions[i])) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * The extensions of this filter.
     **/
    public String[] getExtensions() {
	return _extensions;
    }
  
    /**
     * The description of this filter.
     **/
    public String getDescription() {
	return _description;
    }
    
    /**
     * The description of this filter.
     **/
    public String getIdentifier() {
	return identifier;
    }

    /**
     * Sets the extensions of this filter.
     **/
    public void setExtensions(String[] extensions) {
	_extensions = extensions;
    }
  
    /**
     * Sets the description of this filter.
     **/
    public void setDescription(String description) {
	_description = description;
    }

	
	public int getFileAcceptMode() {
		return fileMode;
	}
  
}
