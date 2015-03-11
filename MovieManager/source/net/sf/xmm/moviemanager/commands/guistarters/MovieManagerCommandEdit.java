/**
 * @(#)MovieManagerCommandEdit.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands.guistarters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogMovieInfo;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.util.GUIUtil;

public class MovieManagerCommandEdit implements ActionListener {

	static Logger log = Logger.getLogger(MovieManagerCommandEdit.class);
	
	/**
	 * Executes the command.
	 **/
	public static void execute() {
		/* Makes sure a movie is selected... */
		int listIndex = -1;

		if (MovieManager.getConfig().getInternalConfig().isEditMovieDisabled())
			return;

		if (MovieManager.getDialog().getMoviesList().getModel().getChildCount(MovieManager.getDialog().getMoviesList().getModel().getRoot()) > 0) {

			listIndex = MovieManager.getDialog().getMoviesList().getLeadSelectionRow();

			if (listIndex == -1)
				listIndex = MovieManager.getDialog().getMoviesList().getMaxSelectionRow();

			if (MovieManager.getDialog().getMoviesList().getSelectionCount() > 1) {
				MovieManager.getDialog().getMoviesList().setSelectionRow(listIndex);
			}
		}

		if (listIndex != -1) {

			ModelEntry selected = ((ModelEntry) ((DefaultMutableTreeNode) MovieManager.getDialog().getMoviesList().getLastSelectedPathComponent()).getUserObject());

			if (selected.getKey() != -1) {
				DialogMovieInfo dialogMovieInfo = new DialogMovieInfo(selected);
				GUIUtil.show(dialogMovieInfo, true);
			}
		}
	}

	/**
	 * Invoked when an action occurs.
	 **/
	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		execute();
	}
}
