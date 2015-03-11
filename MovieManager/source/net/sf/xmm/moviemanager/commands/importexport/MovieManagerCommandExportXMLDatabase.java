/**
 * @(#)MovieManagerCommandExportXMLDatabase.java
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

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelExportXML;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelSeries;
import net.sf.xmm.moviemanager.util.FileUtil;

import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;


public class MovieManagerCommandExportXMLDatabase extends MovieManagerCommandExportHandler {

	static Logger log = Logger.getLogger(MovieManagerCommandExportXMLDatabase.class);

	ModelImportExportSettings exportSettings;

	ModelExportXML exportXMLDatabase;
	
	Marshaller marshaller;
	
	DefaultMutableTreeNode movieList;
	
	MovieManagerCommandExportXMLDatabase(ModelImportExportSettings exportSettings) {
		this.exportSettings = exportSettings;
	}

	public String getTitle(int index) {
		
		String title = null;

		DefaultMutableTreeNode node = ((DefaultMutableTreeNode) movieList.getChildAt(index));
		Object model = node.getUserObject();
		
		if (model instanceof ModelMovie)
			title = ((ModelEntry) model).getTitle();
		else if (model instanceof ModelSeries) {
			title = ((ModelSeries) model).getMovie().getTitle();
		}

		return title;
	}

	public ImportExportReturn addMovie(int index) {

		DefaultMutableTreeNode node = ((DefaultMutableTreeNode) movieList.getChildAt(index));
		ModelEntry model = (ModelEntry) node.getUserObject();

		if (!model.getHasGeneralInfoData()) {
			model.updateGeneralInfoData();
		}

		if (!model.getHasAdditionalInfoData()) {
			model.updateAdditionalInfoData();
		}

		// Has no children 
		if (node.isLeaf()) {
			exportXMLDatabase.addModelMovie((ModelMovie) model);
		}
		else {
			ModelSeries serie = new ModelSeries((ModelMovie) model);

			Enumeration<? extends DefaultMutableTreeNode> children = node.children();

			while (children.hasMoreElements()) {
				serie.addEpisode((ModelEpisode) children.nextElement().getUserObject());
			}

			exportXMLDatabase.addModelSerie(serie);
		}

		return ImportExportReturn.success;
	}

	
	public void done() throws Exception {
		marshaller.marshal(exportXMLDatabase);
	}

	public void execute() {

		movieList = (DefaultMutableTreeNode) ((DefaultTreeModel) MovieManager.getDialog().getMoviesList().getModel()).getRoot();
		
		try {

			Mapping mapping = new Mapping(new ModelMovie().getClass().getClassLoader());
			URL mappingFile = FileUtil.getFileURL(MovieManager.getConfig().getCastorMappingFile());

			exportXMLDatabase = new ModelExportXML(movieList.getChildCount());

			// 1. Load the mapping information from the file
			mapping.loadMapping(mappingFile);

			String encoding = "UTF-8";
			
			marshaller = new Marshaller(new OutputStreamWriter(new FileOutputStream(exportSettings.getFilePath()), encoding));
			marshaller.setEncoding(encoding);
			marshaller.setMarshalAsDocument(true);
			marshaller.setMapping(mapping);

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
			return;
		}
	}
	
	public void retrieveMovieList() throws Exception {

	}


	public int getMovieListSize() {

		if (movieList == null)
			return -1;

		return movieList.getChildCount();
	}
}
