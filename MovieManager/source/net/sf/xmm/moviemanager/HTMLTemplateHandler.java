package net.sf.xmm.moviemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.models.ModelHTMLTemplate;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplateStyle;
import net.sf.xmm.moviemanager.util.FileUtil;

public class HTMLTemplateHandler {

	static Logger log = Logger.getLogger(HTMLTemplateHandler.class);
	
	public HashMap<String, ModelHTMLTemplate> htmlTemplates = new HashMap<String, ModelHTMLTemplate>();

	public final String HTMLTemplateRoot = "HTML_templates/";
	
	ModelHTMLTemplate htmlTemplate = null;
	ModelHTMLTemplateStyle htmlTemplateStyle = null;
	
	public final String HTMLTemplateRootDir = "HTML_templates/";
	
	private String DefaultHTMLTemplateName = "Simple Virtue";
	private String DefaultHTMLTemplateStyleName = "A Touch of Blue";
	
	private String HTMLTemplateName = DefaultHTMLTemplateName;
	private String HTMLTemplateStyleName = DefaultHTMLTemplateStyleName;

	public void setHTMLTemplateStyleName(String HTMLTemplateStyleName) {
		
		if (HTMLTemplateStyleName == null)
			return;
		
		this.HTMLTemplateStyleName = HTMLTemplateStyleName;
	}
	
	public void setHTMLTemplateName(String HTMLTemplateName) {
		
		if (HTMLTemplateName == null)
			return;
			
		this.HTMLTemplateName = HTMLTemplateName;
	}

	public ModelHTMLTemplate getTemplate(String name) {
		
		if (!htmlTemplates.containsKey(name)) {
			return null;
		}
		
		return (ModelHTMLTemplate) htmlTemplates.get(name);
	}


	public HashMap<String, ModelHTMLTemplate> getHTMLTemplates() {
		return htmlTemplates;
	}

	
	void loadHTMLTemplates() {

		if (MovieManager.getConfig().getInternalConfig().getDisableHTMLView())
			return;

		try {

			File f = FileUtil.getFile(HTMLTemplateRootDir);

			if (f != null && f.isDirectory()) {

				File [] templateFiles = f.listFiles();

				for (int i = 0; i < templateFiles.length; i++) {

					try {
						// For each template directory
						if (templateFiles[i].isDirectory()) {

							// Finding template.txt
							File template = new File(templateFiles[i], "template.txt");

							if (!template.isFile()) {
								log.debug("No template.txt file found in the directory of template " + templateFiles[i] + 
								"\n Template not added.");
								continue;
							}

							ArrayList<String> lines = FileUtil.readFileToArrayList(template);

							if (lines == null) {
								log.error("Failed to read file "  + template);
								throw new Exception("Failed to read file "  + template);
							}

							ModelHTMLTemplate newTemplate = new ModelHTMLTemplate(templateFiles[i].getName(), lines);

							if (htmlTemplates.containsKey(newTemplate.getName())) {
								log.warn("A template named " + newTemplate.getName() + " already exists! \r\n" + 
										templateFiles[i] + " is not added.");
								continue;
							}
							htmlTemplates.put(newTemplate.getName(), newTemplate);

							// Getting the styles
							File styles = new File(templateFiles[i], "Styles");

							if (!styles.isDirectory()) {
								log.debug("No styles found for HTML template " + templateFiles[i] + 
								"\n No template styles added.");
							} else {

								File [] styleFiles = styles.listFiles();

								for (int u = 0; u < styleFiles.length; u++) {

									// Style files end with .style.txt
									if (!styleFiles[u].getName().endsWith(".style.txt"))
										continue;

									// Getting all all available styles for this template
									lines = FileUtil.readFileToArrayList(styleFiles[u]);
									ModelHTMLTemplateStyle style = new ModelHTMLTemplateStyle(newTemplate, lines);

									newTemplate.addStyle(style);
								}
							}
						}
					} catch (Exception e) {
						log.warn(e.getMessage()+ "\n Failed to import template " + templateFiles[i], e);
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to read HTML temlplate files.", e);
		}

		log.debug("Done loading HTML templates."); //$NON-NLS-1$
	}
	
	
	/********************************
	 * HTML templates
	 ********************************/
	
	public ModelHTMLTemplate getHTMLTemplate() {
		
		if (htmlTemplate == null) {
			htmlTemplate = MovieManager.getTemplateHandler().getTemplate(HTMLTemplateName);
		
			if (htmlTemplate == null) {
				log.warn("Requested template does not exist:" + HTMLTemplateName);
				return null;
			}
		}
		return htmlTemplate;
	}
	
	public void setHTMLTemplate(ModelHTMLTemplate htmlTemplate, ModelHTMLTemplateStyle htmlTemplateStyle) {
		this.htmlTemplate = htmlTemplate;
		this.htmlTemplateStyle = htmlTemplateStyle;
		
		if (htmlTemplateStyle == null)
			setHTMLTemplateStyleName(null);
	}
			

	public void setHTMLTemplate(ModelHTMLTemplate t) {
		htmlTemplate = t;
	}
	
	public void setHTMLTemplateStyle(ModelHTMLTemplateStyle s) {
		htmlTemplateStyle = s;
		
		if (s == null)
			setHTMLTemplateStyleName(null);
	}
	
//	 Returns the template dir, e.g. "HTML_templates/Simple Virtue/" 
	public File getHTMLTemplateDir() {
		
		if (getHTMLTemplate() != null)
			return FileUtil.getFile("HTML_templates/" + getHTMLTemplate().getDirName());

		return null;
	}
	
//	 Returns the template name, e.g. "Simple Virtue" 
	public String getHTMLTemplateName() {
		
		if (htmlTemplate == null) {
			if (getHTMLTemplate() == null)
				return HTMLTemplateName;
		}
		return getHTMLTemplate().getName();
	}

		
//	Returns the template html file e.g. "HTML_templates/Simple Virtue/Simple_Virtue.html"
	public File getHTMLTemplateFile() {
		if (getHTMLTemplate() != null)
			return new File(getHTMLTemplateDir(), getHTMLTemplate().getHTMLTemplateFileName());

		return null;
	}

	public File getHTMLTemplateCssFile() {
		if (getHTMLTemplate() != null)
			return new File(getHTMLTemplateDir(), getHTMLTemplate().getHTMLTemplateCssFileName());  

		return null;
	}
	
	public ModelHTMLTemplateStyle getHTMLTemplateStyle() {
		
		if (htmlTemplateStyle == null && HTMLTemplateStyleName != null) {
						
			ModelHTMLTemplate tmpTemplate = getHTMLTemplate();
			
			if (tmpTemplate == null || (htmlTemplateStyle = getHTMLTemplate().getStyle(HTMLTemplateStyleName)) == null) {
				log.debug("Requested template style does not exist:" + HTMLTemplateStyleName);
				return null;
			}
		}
		return htmlTemplateStyle;
	}
	
	public String getHTMLTemplateStyleName() {
		
		if (htmlTemplateStyle == null) {
			if (getHTMLTemplateStyle() == null) {
				return HTMLTemplateStyleName; // Update style
			}
		}
		
		return htmlTemplateStyle.getName();
	}
	
	public File getHTMLTemplateCssStyleFile() {
		return new File(getHTMLTemplateDir(), "Styles/" + htmlTemplateStyle.getCssFileName());  
	}
	
	public String getHTMLTemplateCssStyleFileName() {
		return htmlTemplateStyle.getCssFileName();
	}
}
