package net.sf.xmm.moviemanager.models;

import java.util.ArrayList;

public class ModelHTMLTemplateStyle {

	ModelHTMLTemplate parentTemplate = null; // the parent of styles
	private String name = null;
	
	String version = null;
	String authorName = null;
	String authorEmail = null;
	String exportCompatible = null;
	String compatibleBrowsers = null;
	String knownBugs = null;
	String comments = null;
	
	String HTMLTemplateFileName;
	String cssStyleFile;
	
	String imageFileNames = null;
		
	public ModelHTMLTemplateStyle(ModelHTMLTemplate parentTemplate, ArrayList<String> info) throws Exception {
		
		this. parentTemplate = parentTemplate;
		
		for (int i = 0; i < info.size(); i++) {
			String tmp = info.get(i);
			
			String [] split = tmp.split("=");
			
			if (split[0].equalsIgnoreCase("Style Name"))
				name = split[1];
			else if (split[0].equalsIgnoreCase("Style CSS"))
				cssStyleFile = split[1];
			else if (split[0].equalsIgnoreCase("Author Name"))
				authorName = split[1];
			else if (split[0].equalsIgnoreCase("Author email"))
				authorEmail = split[1];
			else if (split[0].equalsIgnoreCase("Image files"))
				imageFileNames = split[1];
			else if (split[0].equalsIgnoreCase("Comments"))
				comments = split[1];
		}
		
		if (name == null) {
			throw new Exception("Style name is not specified.");
		}
		
		if (cssStyleFile == null) {
			throw new Exception("CSS style file is not specified.");
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getCssFileName() {
		return cssStyleFile;
	}
	
	public String getAuthor() {
		return authorName;
	}
	
	public String getImageFileNames() {
		return imageFileNames;
	}
		
	public String toString() {
		return name;
	}
	
	public String getInfo() {
		String str = " Name:  " + name;
		
		if (authorName != null)
			str += "\r\n Author: " + authorName;
	
		return str;
	}
}
