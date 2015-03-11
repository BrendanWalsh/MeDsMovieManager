/**
 * @(#)MovieManagerCommandSaveChangedNotes.java 1.0 26.09.05 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelMovie;

public class MovieManagerCommandSaveChangedNotes implements ActionListener {

	static Logger log = Logger.getLogger(MovieManagerCommandSaveChangedNotes.class);
	
	/**
	 * Executes the command.
	 **/
	public static void execute() {
		
		/* Saving the current selected node's changed notes value */
		if (MovieManager.getDialog().getMoviesList().getLeadSelectionRow() != -1) {

			ModelEntry entry = (ModelEntry) ((DefaultMutableTreeNode) MovieManager.getDialog().getMoviesList().getLeadSelectionPath().getLastPathComponent()).getUserObject();
			if (!entry.getNotes().equals(MovieManager.getDialog().getNotes().getText())) {
				entry.setNotes(MovieManager.getDialog().getNotes().getText());
				entry.hasChangedNotes = true;

				if (entry.isEpisode())
					ModelEpisode.notesHaveBeenChanged = true;
				else
					ModelMovie.notesHaveBeenChanged = true;
			}
		}
		
		if (ModelMovie.notesHaveBeenChanged || ModelEpisode.notesHaveBeenChanged) {

			DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((DefaultTreeModel) MovieManager.getDialog().getMoviesList().getModel()).getRoot();
			ModelEntry model;
			DefaultMutableTreeNode node;
			Enumeration<DefaultMutableTreeNode> enumeration = root.children();

			while (enumeration.hasMoreElements()) {

				node = ((DefaultMutableTreeNode) enumeration.nextElement());
				model = (ModelEntry) node.getUserObject();
				
				if (model.hasChangedNotes) {
					log.debug("Saving changed notes for movie " + model.getTitle() + " - cover:" + model.getCover());
					MovieManager.getIt().getDatabase().setGeneralInfo((ModelMovie) model);
				}

				/* Has children */
				if (ModelEpisode.notesHaveBeenChanged && !node.isLeaf()) {
					Enumeration<DefaultMutableTreeNode> episodeEnumeration = node.children();

					while (episodeEnumeration.hasMoreElements()) {
						model = (ModelEntry) ((DefaultMutableTreeNode) episodeEnumeration.nextElement()).getUserObject();

						if (model.hasChangedNotes) {
							MovieManager.getIt().getDatabase().setGeneralInfoEpisode((ModelEpisode) model);
						}
					}
				}
			}
			ModelMovie.notesHaveBeenChanged = false;
			ModelEpisode.notesHaveBeenChanged = false;
		}
	}
	
	/**
	 * Invoked when an action occurs.
	 **/
	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: " + event.getActionCommand());
		execute();
	}
}
