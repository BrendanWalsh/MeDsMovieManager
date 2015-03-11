/**
 * @(#)ModelImportExportSettings.java
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

import java.io.File;
import java.util.ArrayList;

import javax.swing.JDialog;


import net.sf.xmm.moviemanager.util.Localizer;

public class ModelImportExportSettings {
	
	public static enum ImportMode {
		TEXT("Text File"), 
		CSV("CSV File"), 
		EXCEL("Excel Spreadsheet"), 
		XML_DATABASE("XML Database"), 
		XML("XML File");

		String title;

		ImportMode(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}

		public static boolean isValidValue(String val) {

			for (ImportMode mode : values()) {
				if (mode.toString().equals(val))
					return true;
			}
			return false;
		}
	};
	   
    public static enum ExportMode {
    	CSV("CSV File"), 
    	EXCEL("Excel Spreadsheet"), 
    	XML_DATABASE("XML Database"), 
    	XML("XML File"), 
    	HTML("HTML");
    
    	String title;

    	ExportMode(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}
		
		public static boolean isValidValue(String val) {
			
			for (ExportMode mode : values()) {
				if (mode.toString().equals(val))
					return true;
			}
			return false;
		}
    };
    
    public final static int IMPORT_MODE_COUNT = ImportMode.values().length;
	public final static int EXPORT_MODE_COUNT = ExportMode.values().length;
	    
    public static String [] encodings = new String[] {"US-ASCII", "ISO-8859-1", "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-16"};
    
    
   
        
    public enum ImdbImportOption {off, displayList, selectFirst, selectFirstOrAddToSkippedList, 
    	selectIfOnlyOneHit, selectIfOnlyOneHitOrAddToSkippedList}
       
      
    public ImdbImportOption multiAddIMDbSelectOption = ImdbImportOption.off;
    
    public boolean isIMDbEnabled() {
    	return multiAddIMDbSelectOption != ImdbImportOption.off;
    }
    
    public ImportMode importMode = ImportMode.TEXT;
    public ExportMode exportMode = ExportMode.HTML;
    public boolean overwriteWithImdbInfo = false;
    
    public String addToThisList = null;
     
    
    public String skippedListName = "Importer-skipped";
    
    public String filePath = "";
    private File file = null;
    public String coverPath = "";
    
    public char csvSeparator = ',';
    public String textEncoding = null;
    
    public String htmlTitle = null;
    public boolean htmlAlphabeticSplit = false;
    public boolean htmlSimpleMode = false;
    
    JDialog parent = null;
    
    public JDialog getParent() {
    	return parent;
    }
    
    public void setParent(JDialog parent) {
    	this.parent = parent;
    }
    
    public String getFilePath() {
    	return filePath;	
    }
    
    public File getFile() {
    	if (file == null)
    		file = new File(filePath);
    	
    	return file;
    }
    
    public void setFile(File f) {
    	file = f;
    }
    
    public String getHTMLTitle() {
    	return htmlTitle;	
    }
    
    public boolean getHTMLAlphabeticSplit() {
    	return htmlAlphabeticSplit;	
    }
        
    public boolean getIsHTMLSimpleMode() {
    	return htmlSimpleMode;	
    }
    
    public char getCSVSeparator() {
    	return csvSeparator;	
    }
    
    public String getTextEncoding() {
    	return textEncoding;	
    }
    
    public ArrayList<String> getAddToThisList() {
    	ArrayList<String> l = new ArrayList<String>();
    	
    	if (addToThisList != null)
    		l.add(addToThisList);
    	
    	return l;
    }
    
}

