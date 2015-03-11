/**
 * @(#)MovieManagerCommandExportToFullHTML.java 1.0 26.09.05 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands.importexport;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler.ImportExportReturn;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogQuestion;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.util.CustomFileFilter;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.log4j.Logger;

public class MovieManagerCommandExportToFullHTML extends MovieManagerCommandExportHandler {

	static Logger log = Logger.getLogger(MovieManagerCommandExportToFullHTML.class);
	
	 boolean divideAlphabetically;
	 String reportTitle;
	
	 int templateBodyIndex = -1;
	 String lastStart = null;
	
	File htmlOutputFile = null;
	File coverOutputDir = null;
		
	StringBuffer templateSkeleton  = null;
	
	ArrayList<ModelMovie> movieList = MovieManager.getDialog().getCurrentMoviesList();
		
	public MovieManagerCommandExportToFullHTML(boolean divideAlphabetically, String reportTitle) {
		this.divideAlphabetically = divideAlphabetically;
		this.reportTitle = reportTitle;
	}
		
	ModelImportExportSettings settings;
		
	
	public void execute() {
			
		
		if (!handleGetOutputFile()) {
			cancelled = true;
		}
				
		if (cancelled) {
			return;
		}
						
		// Copies the css files
		try {
			File exportDirectory = htmlOutputFile.getParentFile();
			FileUtil.copyToDir(MovieManager.getConfig().getHTMLTemplateHandler().getHTMLTemplateCssFile(), exportDirectory);
			File cssStyleDir = MovieManager.getConfig().getHTMLTemplateHandler().getHTMLTemplateCssStyleFile().getParentFile();

			// Copy css style and image if it exists
			File [] files = cssStyleDir.listFiles();
			String cssStyle = MovieManager.getConfig().getHTMLTemplateHandler().getHTMLTemplateCssStyleFileName().substring(0, MovieManager.getConfig().getHTMLTemplateHandler().getHTMLTemplateCssStyleFileName().lastIndexOf("."));

			for (int i = 0; i < files.length; i++) {

				if (files[i].getName().startsWith(cssStyle))
					FileUtil.copyToDir(files[i], new File(exportDirectory, "Styles"));
			}
		} catch (Exception e) {
			log.error("Excpetion:" + e.getMessage(), e);
		}
	}
	
		
	public void retrieveMovieList() throws Exception {
		
		createMovieList(htmlOutputFile, coverOutputDir.getAbsolutePath());
	}

	String lastTitle = null;
	
	public String getTitle(int i) {
				
		ModelMovie model = movieList.get(i);
		lastTitle = model.getTitle();
		
		return model.getTitle();
	}
	
	public int getMovieListSize() {
		return movieList.size();
	}
	
	public void done() throws Exception {
	//	String output = writer.toString();
		//FileUtil.writeToFile(settings.getFile().getAbsolutePath(), new StringBuffer(output), settings.textEncoding);
	}
		
	
	HTMLMovieGroup movieGroup = null;
	ArrayList<HTMLMovieGroup> movieGroups = null;
	
	int movieGroupNumber = 0;
	
	public ImportExportReturn addMovie(int notUsed) {
		
		try {

			if (movieGroup == null) {

				if (movieGroups.size() > 0) {
					movieGroup = (HTMLMovieGroup) movieGroups.get(movieGroupNumber);
					movieGroupNumber++;
				}
				else {
					throw new Exception("No more movies to process!");
				}
			}
			
			if (movieGroup.getSize() > 0) {
				
				movieGroup.processNext();
				
				if (movieGroup.getSize() == 0) {
					
					StringBuffer html = movieGroup.getHTMLMovies();

					StringBuffer output = new StringBuffer(templateSkeleton.toString());

					StringBuffer bodyContent = new StringBuffer();

					bodyContent.append("<br>" + getHeader(reportTitle));

					if (divideAlphabetically) {
						bodyContent.append(getLinks(movieGroups, movieGroupNumber-1) + "<br>");
						bodyContent.append(html);
						bodyContent.append(getLinks(movieGroups, movieGroupNumber-1) + "<br><br>");
					}
					else
						bodyContent.append(html);

					
					bodyContent.append(SysUtil.getLineSeparator());
					bodyContent.append(SysUtil.getLineSeparator());
					bodyContent.append(SysUtil.getLineSeparator());
						
					output = output.insert(templateBodyIndex, bodyContent);

					output.append(" ");

					MovieManagerCommandSelect.processTemplateCssStyle(output);
	
					// Too slow for big strings
					//output = HttpUtil.getHtmlNiceFormat(output);
	
					String filepath = htmlOutputFile.getParentFile().getAbsolutePath() + SysUtil.getDirSeparator();
					
					String fName = filepath + movieGroup.getFileName();

					FileUtil.writeToFile(fName, output, "UTF8");
					
					movieGroup = null;
				}
			}

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
			 return ImportExportReturn.error;
		}
		         
		return ImportExportReturn.success;
	}
	
	

	/**
	 * Exports the content of the database to html...
	 **/
	void createMovieList(File outputFile, String coversPath) {

		log.debug(this.getClass().getName() + ".createMovieList");
		
		String fileName = outputFile.getName();

		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
		}

		StringBuffer template = null;
		
		StringBuffer templateBody = null;

		movieGroups = new ArrayList<HTMLMovieGroup>();

		int templateBodyEndIndex = -1;

		lastStart = null;

		try {

			File f = MovieManager.getConfig().getHTMLTemplateHandler().getHTMLTemplateFile();

			template = FileUtil.readFileToStringBuffer(f);

			templateBodyIndex = template.indexOf("<body>") + 6;
			templateBodyEndIndex = template.indexOf("</body>");

			templateSkeleton =  new StringBuffer(template.toString());
			templateSkeleton.delete(templateBodyIndex, templateBodyEndIndex);
			templateBody =  new StringBuffer(template.substring(templateBodyIndex, templateBodyEndIndex));

			ModelEntry model;
			HTMLMovieGroup tmpGroup = null;

			// Calculates the necessary html groups
			if (divideAlphabetically) {

				HTMLMovieGroup [] groups = new HTMLMovieGroup[28];

				for (int i = 0; i < movieList.size(); i++) {

					model = movieList.get(i);
					
					if (!model.getHasGeneralInfoData()) {
						model.updateGeneralInfoData();
					}

					if (!model.getHasAdditionalInfoData()) {
						model.updateAdditionalInfoData();
					}
					
					char c = Character.toLowerCase(model.getTitle().charAt(0));
					int groupIndex = -1;

					// Finding correct movie group
					
					if (c < 'a') {
						groupIndex = 0;
					}
					else if (c >= 'a' && c <= 'z') {
						groupIndex = c - 'a' + 1;
					}
					else
						groupIndex = 27;


					if (groups[groupIndex] == null){
						
						if (groupIndex == 0) {
							groups[groupIndex] = new HTMLMovieGroup(reportTitle, fileName, coversPath, new StringBuffer(templateBody.toString()));
							//groups[groupIndex].setTitle(reportTitle);
						}
						else 
							groups[groupIndex] = new HTMLMovieGroup("" + c, fileName, coversPath, new StringBuffer(templateBody.toString()));
					}

					// Adding movie
					groups[groupIndex].addModel(model);
				}

				for (int i = 0; i < groups.length; i++) {
					if (groups[i] != null)
						movieGroups.add(groups[i]);
				}
			}
			else {
				tmpGroup = new HTMLMovieGroup("", fileName, coversPath, templateBody);
				movieGroups.add(tmpGroup);

				for (int i = 0; i < movieList.size(); i++) {
					model = movieList.get(i);
					
					if (!model.getHasGeneralInfoData()) {
						model.updateGeneralInfoData();
					}

					if (!model.getHasAdditionalInfoData()) {
						model.updateAdditionalInfoData();
					}
					
					tmpGroup.addModel(model);
				}
			}

			/* Creates the nocover file... */
			try {
				byte[] noCover = FileUtil.getResourceAsByteArray("/images/" + MovieManager.getConfig().getNoCoverFilename());
				
				if (noCover != null) {
					FileUtil.writeToFile(noCover,  new File(coversPath, MovieManager.getConfig().getNoCoverFilename()));
				}
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage(), e);
			}

		} catch (Exception e) {
			log.error("", e);
		}
	}



	static String getLinks(ArrayList<HTMLMovieGroup> htmlGroups, int i) {
		
		StringBuffer buf = new StringBuffer();

		try {
			buf.append("<center>\n");

			for (int u = 0; u < htmlGroups.size(); u++) {

				HTMLMovieGroup tmpG  = htmlGroups.get(u);
				String link;
	
				if (i == u)
					link = tmpG.getTitle();
				else
					link = "<a href=\"" + tmpG.getFileName() + "\">"+ tmpG.getTitle() + "</a>";
				
				buf.append("<font size=\"+2\">" + link + "</font> &nbsp;");
			}

			buf.append("</center>\n");

		} catch (Exception e) {
			log.error("", e);
		}

		return buf.toString();
	}
	
	static String getHeader(String title) {
		
		String htmlHeader = "<p style=\"font-size: 5em; text-align: center; font-family: Garamond, Arial, Helvetica, sans-serif; \">" + title +"</p>";
		
		return htmlHeader;
	}


	/**
	 * Executes the command.
	 **/
	public boolean handleGetOutputFile() {

		log.debug(this.getClass() + ".handleGetOutputFile()");

		boolean ret = false;
		
		try {

			/* Opens the Export to HTML dialog... */
			ExtendedFileChooser fileChooser = new ExtendedFileChooser();
			fileChooser.setFileFilter(new CustomFileFilter(new String[]{"htm","html"},new String("HTML Files (*.htm, *.html)")));

			if (MovieManager.getConfig().getLastMiscDir() != null) {
				fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastMiscDir());
			}

			fileChooser.setDialogType(ExtendedFileChooser.CUSTOM_DIALOG);
			fileChooser.setDialogTitle("Export to HTML - Full");
			fileChooser.setApproveButtonToolTipText("Export to file (a folder \'Covers\' will also be created)");

			fileChooser.setAcceptAllFileFilterUsed(false);
			
			int returnVal = fileChooser.showDialog(MovieManager.getDialog(), "Export");
	
			while (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
				
				/* Gets the path... */
				File selected = fileChooser.getSelectedFile();
				String path = selected.getParent();

				String fileName = selected.getName();

				if (!fileName.endsWith(".html")) {
					fileName = fileName + ".html";
				}

				/* Creates the movielist file... */
				File htmlFile = new File(selected.getParent(), fileName);

				//String coversPath;
				File coversDir;

				if (fileName.indexOf(".") == -1)
					coversDir = new File(path, fileName + "_covers");
				else
					coversDir = new File(path, fileName.substring(0, fileName.lastIndexOf(".")) + "_covers");

				/* Relative path to covers dir... */

				if (htmlFile.exists()) {
					DialogQuestion fileQuestion = new DialogQuestion("File already exists", "A file with the chosen filename already exists. Would you like to overwrite the old file?");
					GUIUtil.showAndWait(fileQuestion, true);
	
					if (fileQuestion.getAnswer()) {

						if (coversDir.exists()) { 
							DialogQuestion coverQuestion = new DialogQuestion("Directory already exists.", "The directory to store covers already exists. Put cover images in the existing directory?");
							GUIUtil.showAndWait(coverQuestion, true);

							if (coverQuestion.getAnswer()) {
								htmlOutputFile = htmlFile;
								coverOutputDir = coversDir;
								ret = true;
								break;
							}
							else
								returnVal = fileChooser.showOpenDialog(MovieManager.getDialog());

						}
						else {
							htmlFile.delete();
							htmlOutputFile = htmlFile;
							coverOutputDir = coversDir;
							ret = true;
							break;
						}
					}
					else 
						returnVal = fileChooser.showOpenDialog(MovieManager.getDialog());

				} 
				else if (coversDir.exists()) { 
					DialogQuestion coverQuestion = new DialogQuestion("Directory already exists.", "The directory to store covers already exists. Overwrite existing files?");
					GUIUtil.showAndWait(coverQuestion, true);

					if (coverQuestion.getAnswer()) {
						htmlOutputFile = htmlFile;
						coverOutputDir = coversDir;
						ret = true;
						break;
					}
					else {
						returnVal = fileChooser.showOpenDialog(MovieManager.getDialog());
					}

				}
				else if(!coversDir.mkdir()) {
					DialogAlert coverAlert = new DialogAlert(MovieManager.getDialog(), "Couldn't create directory.", "The directory to store covers could not be created.");
					GUIUtil.showAndWait(coverAlert, true);
				}
				else {
					htmlOutputFile = htmlFile;
					coverOutputDir = coversDir;
					ret = true;
					break;
				}
			}

			/* Sets the last path... */
			MovieManager.getConfig().setLastMiscDir(fileChooser.getCurrentDirectory());
			return ret;

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
		return false;
	}
}


class HTMLMovieGroup {

	static Logger log = Logger.getLogger(HTMLMovieGroup.class);

	String groupTitle;
	String fileTitle;
	ArrayList<ModelEntry> models = new ArrayList<ModelEntry>();
	String coversPath;
	StringBuffer templateBody;
	String reportTitle = null;

	StringBuffer movieDataTmp = new StringBuffer();

	int coverWidth = 97;

	String nocoverFileName = MovieManager.getConfig().getNoCoverFilename();
	String coversDBFolder = MovieManager.getConfig().getCoversPath();

	HTMLMovieGroup(String groupTitle, String fileTitle, String coversPath, StringBuffer templateBody) {
		this.groupTitle = groupTitle;
		this.fileTitle = fileTitle;
		this.coversPath = coversPath;
		this.templateBody = templateBody;
	}

	public String toString() {
		return getTitle();
	}

	public void setTitle(String title) {
		//this.groupTitle = groupTitle;
		reportTitle = title;
	}

	public String getTitle() {

		if (groupTitle == null || groupTitle.equals(""))
			return reportTitle;

		return groupTitle;
	}

	public String getFileName() {

		if (groupTitle == null || groupTitle.equals(""))
			return fileTitle + ".html";
		else
			return fileTitle + "-"+ groupTitle + ".html";


	}

	public void addModel(ModelEntry model) {
		models.add(model);
	}


	public int getSize() {
		return models.size();
	}

	StringBuffer getHTMLMovies() {
		return movieDataTmp;
	}

	public void processNext() {

		try {

			int coverHeight = 0;

			String coverFileName;

			File coverInputFile, coverOutputFile = null;

			ModelMovie movie = (ModelMovie) models.remove(0);

			if (!movie.getHasGeneralInfoData())
				movie.updateGeneralInfoData();

			//	New fresh template
			StringBuffer htmlData = new StringBuffer(templateBody.toString());
			MovieManagerCommandSelect.processTemplateData(htmlData, movie);

			boolean nocover = false;
			
			try {
				coverFileName = movie.getCover();

				if (!coverFileName.equals("")) {

					coverHeight = 145;

					/* Creates the output cover file... */
					coverOutputFile = new File(coversPath, coverFileName);
	
					try {
						coverOutputFile.createNewFile();
					} catch (Exception e) {
						log.error("Failed to create cover file:" + coverOutputFile);
						nocover = true;
					}
					
					if (!nocover) {
						if (MovieManager.getIt().getDatabase().isMySQL()) {

							// Write cover file to disk
							if (!movie.getHasGeneralInfoData())
								movie.updateGeneralInfoData(true);

							if (movie.getCoverData() != null) {
								FileUtil.writeToFile(movie.getCoverData(), coverOutputFile);
							}
							else {
								coverFileName = nocoverFileName;
								coverHeight = 97;
								coverOutputFile.delete();
							}
						}
						else {
							/* Verifies that the input file exists... */
							coverInputFile = new File(coversDBFolder, coverFileName);

							if (coverInputFile.isFile()) {
								FileUtil.copyToDir(coverInputFile, new File(coversPath), coverFileName);
							}
							else {
								coverFileName = nocoverFileName;
								coverHeight = 97;
								coverOutputFile.delete();
							}
						}
					}						
				} else {
					coverFileName = nocoverFileName;
					coverHeight = 97;
				}
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage(), e);
				log.error("Failed to copy cover file:" + coverOutputFile);
				coverFileName = nocoverFileName;
			}

			String coverName = new File(coversPath).getName() + "/" + coverFileName;
			MovieManagerCommandSelect.processTemplateCover(htmlData, coverName, new Dimension(coverWidth, coverHeight), nocover);

			movieDataTmp.append(htmlData + "<br><br><br>");

			movieDataTmp.append(SysUtil.getLineSeparator());
			movieDataTmp.append(SysUtil.getLineSeparator());
			movieDataTmp.append(SysUtil.getLineSeparator());
			movieDataTmp.append(SysUtil.getLineSeparator());
			
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
	}



	/*public StringBuffer processMovies() {

		StringBuffer movieDataTmp = new StringBuffer();

		try {

			String nocover = MovieManager.getConfig().getNoCoverSmall();
			String coversDBFolder = MovieManager.getConfig().getCoversPath();
			int coverHeight = 0;
			int coverWidth = 97;
			String coverFileName;

			File coverInputFile, coverOutputFile;

			for (int i = 0; i < models.size(); i++) {

				ModelMovie movie = (ModelMovie) models.get(i);

				if (!movie.getHasGeneralInfoData())
					movie.updateGeneralInfoData();

				//	New fresh template
				StringBuffer htmlData = new StringBuffer(templateBody.toString());
				MovieManagerCommandSelect.processTemplateData(htmlData, movie);

				try {
					coverFileName = movie.getCover();

					if (!coverFileName.equals("")) {

						 Creates the output file... 
						coverOutputFile = new File(coversPath, coverFileName);

						if (!coverOutputFile.createNewFile()) {
							coverOutputFile.delete();
						}

						if (MovieManager.getIt().getDatabase().isMySQL()) {

							// Write cover file to disk
							if (!movie.getHasGeneralInfoData())
								movie.updateGeneralInfoData(true);

							if (movie.getCoverData() != null) {
								FileUtil.writeToFile(movie.getCoverData(), coverOutputFile);
							}
						}
						else {
							 Verifies that the input file exists... 
							coverInputFile = new File(coversDBFolder, coverFileName);

							if (!coverInputFile.isFile()) {
								throw new Exception("Cover file not found:" + coverInputFile.getAbsolutePath());
							}

							FileUtil.copyToDir(coverInputFile, new File(coversPath), coverFileName);
						}

						coverHeight = 145;
					} else {
						coverFileName = nocover;
						coverHeight = 97;
					}
				} catch (Exception e) {
					log.error("Exception: " + e.getMessage());
					coverFileName = MovieManager.getConfig().getNoCover();
				}

				String coverName = new File(coversPath).getName() + "/" + coverFileName;
				MovieManagerCommandSelect.processTemplateCover(htmlData, coverName, new Dimension(coverWidth, coverHeight));

				movieDataTmp.append(htmlData + "<br><br><br>");
			}
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}

		return movieDataTmp;
	}*/
}
