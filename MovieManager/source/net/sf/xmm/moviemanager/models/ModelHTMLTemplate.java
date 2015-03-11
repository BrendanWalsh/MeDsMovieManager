package net.sf.xmm.moviemanager.models;

import java.util.ArrayList;


public class ModelHTMLTemplate {
	
	String name = null;
	String version = null;
	String authorName = null;
	String authorEmail = null;
	String exportCompatible = null;
	String compatibleBrowsers = null;
	String knownBugs = null;
	String comments = null;
	
	String HTMLTemplateFileName;
	String HTMLTemplateCssFileName;
	
	String templateDirName = "";
	
	ArrayList<ModelHTMLTemplateStyle> styles = new ArrayList<ModelHTMLTemplateStyle>();
	
	private boolean hasStyles = false;
	
	public ModelHTMLTemplate(String templateDirName, ArrayList<String> info) throws Exception {
		
		this.templateDirName = templateDirName;
		
		for (int i = 0; i < info.size(); i++) {
			String tmp = info.get(i);
			
			String [] split = tmp.split("=");
			
			if (split[0].equalsIgnoreCase("Template Name"))
				name = split[1];
			else if (split[0].equalsIgnoreCase("Template Version"))
				version = split[1];
			else if (split[0].equalsIgnoreCase("Template HTML"))
				HTMLTemplateFileName = split[1];
			else if (split[0].equalsIgnoreCase("Template CSS"))
				HTMLTemplateCssFileName = split[1];
			else if (split[0].equalsIgnoreCase("Author Name"))
				authorName = split[1];
			else if (split[0].equalsIgnoreCase("Author email"))
				authorEmail = split[1];
			else if (split[0].equalsIgnoreCase("Export compatible"))
				exportCompatible = split[1];
			else if (split[0].equalsIgnoreCase("Compatible browsers"))
				compatibleBrowsers = split[1];
			else if (split[0].equalsIgnoreCase("Known bugs"))
				knownBugs = split[1];
			else if (split[0].equalsIgnoreCase("Comments"))
				comments = split[1];
		}
		
		if (name == null) {
			throw new Exception("Name is missing in template file.");
		}
	}

	public String toString() {
		return name;
	}
	
	public String getInfo() {
		
		String str =   " Name:   " + name;
		
		if (authorName != null)
			str += "\r\n Author:  " + authorName;
		
		if (authorEmail != null)
			str += "\r\n E-mail:  " + authorEmail;
		
		if (version != null)
			str += "\r\n Version: " + version;
		
		if (exportCompatible != null)
			str += "\r\n Export compatible:" + exportCompatible;
		
		if (compatibleBrowsers != null && (exportCompatible.trim().equalsIgnoreCase("Yes") || exportCompatible.trim().equalsIgnoreCase("True")))
			str += "\r\n Compatible Browsers: " + compatibleBrowsers;
		
		return str;
	}
	
	public boolean hasStyles() {
		return hasStyles;
	}
	
	public void addStyle(ModelHTMLTemplateStyle style) {
		styles.add(style);
		hasStyles = true;
	}
	
	public ArrayList<ModelHTMLTemplateStyle> getStyles() {
		return styles;
	}

	public ModelHTMLTemplateStyle getStyle(String name) {
		
		for (int i = 0; i < styles.size(); i++) {
			if (((ModelHTMLTemplateStyle) styles.get(i)).getName().equals(name))
				return (ModelHTMLTemplateStyle) styles.get(i);
		}
		return null;
	}
	
//	 Returns the template name, e.g. "Simple Virtue" 
	public String getName() {
		return name;
	}
		
//	 Returns the template html file e.g. "Simple_Virtue.html"
	public String getHTMLTemplateFile() {
		return templateDirName + "/" + HTMLTemplateFileName;
	}
	
//	 Returns the template html file e.g. "Simple_Virtue.html"
	public String getHTMLTemplateFileName() {
		return HTMLTemplateFileName;
	}
	  	
	public String getHTMLTemplateCssFileName() {
		return HTMLTemplateCssFileName;
	}
		
	public String getAuthor() {
		return authorName;
	}
	
	public String getExportCompatible() {
		return exportCompatible;
	}
	
	public String getCompatibleBrowsers() {
		return compatibleBrowsers;
	}
	
	public String getKnownBugs() {
		return knownBugs;
	}
	
	public String getComments() {
		return comments;
	}
	
	public String getDirName() {
		return templateDirName;
	}
}