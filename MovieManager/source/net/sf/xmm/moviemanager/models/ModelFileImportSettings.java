/**
 * @(#)ModelFileImportSettings.java
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
import java.util.HashMap;

import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies;


public class ModelFileImportSettings extends ModelImportExportSettings {
	
	String excludeString;
	boolean enableExludeString = false;
	public ImdbImportOption multiAddSelectOption;
	public boolean enableExludeParantheses = false;
	public boolean enableExludeCDNotations = false;
	public boolean enableExludeIntegers = false;
	public boolean enableExludeYear = false;
	public boolean enableExludeAllAfterMatchOnUserDefinedInfo = false;
	public boolean enableSearchNfoForImdb = false;
	public boolean searchInSubdirectories = false;
	public boolean addMovieToList = false;
	public boolean enableUseFolderName = false;
	public boolean enableExludeUserdefinedInfo = false;
	public boolean enableUseParentFolderIfCD = false;

	public String addToThisList = null;
	
	public ArrayList <DialogAddMultipleMovies.Files> fileList;
    	 
    public HashMap<String, ModelEntry> existingMediaFileNames;
    public HashMap<String, ModelEntry> existingMediaFiles;
}

