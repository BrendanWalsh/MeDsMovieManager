/**
 * @(#)EntryListRemover.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.util.tools;

import java.util.ArrayList;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedJTree;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedTreeCellRenderer;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;


public class EntryListRemover {

	Logger log = Logger.getLogger(getClass());

	DefaultTreeModel listModel;
	ArrayList<DefaultMutableTreeNode> entries;
	TreePath[] selectedPaths;

	Database database;
	JTree moviesList;
	int selectedIndex = -1;

	ExtendedTreeCellRenderer treeCellRenderer = MovieManager.getDialog().getTreeCellRenderer();
	
	public EntryListRemover(Database database, JTree moviesList) {
		this.database = database;
		this.moviesList = moviesList;

		listModel = (DefaultTreeModel) moviesList.getModel();

		selectedPaths = moviesList.getSelectionPaths();
		entries = new ArrayList<DefaultMutableTreeNode>(selectedPaths.length);

		for (int i = 0; i < selectedPaths.length; i++)
			entries.add((DefaultMutableTreeNode) selectedPaths[i].getLastPathComponent());

		selectedIndex = moviesList.getMinSelectionRow();
	}

	public void go() {
		final net.sf.xmm.moviemanager.swing.util.SwingWorker worker = new net.sf.xmm.moviemanager.swing.util.SwingWorker() {
			public Object construct() {
				return new Deleter();
			}
		};
		worker.start();
	}

	void updateList(final DefaultMutableTreeNode parent, final DefaultMutableTreeNode child) {
		Runnable updateListModel = new Runnable() {
			public void run() {
				
				if (parent != null && child != null) {
					parent.remove(child);
					
					//Removing the cached info for the node
					treeCellRenderer.removeNode(child);
					listModel.nodeStructureChanged(parent);
					MovieManager.getDialog().setAndShowEntries();
				}
			}
		};
		SwingUtilities.invokeLater(updateListModel);
	}

	/**
	 * The actual removal task.
	 * This runs in a SwingWorker thread.
	 */
	class Deleter {
		Deleter() {

			boolean fastremove = false;

			if (entries.size() > 10)
				fastremove = true;

			/* Setting the priority of the thread to 4 */
			Thread.currentThread().setPriority(3);

			int ret = 0;

			try {

				DefaultMutableTreeNode root = (DefaultMutableTreeNode) MovieManager.getDialog().getMoviesList().getModel().getRoot();

				while (!entries.isEmpty()) {
					/* Removes the movie from database... */

					if (((DefaultMutableTreeNode) entries.get(0)).getUserObject() instanceof ModelEpisode) {

						if ((ret = database.removeEpisode(((ModelEpisode)((DefaultMutableTreeNode) entries.get(0)).getUserObject()).getKey())) == 0) {
							/* Removes from the list... */
							if (!fastremove)
								updateList((DefaultMutableTreeNode) ((DefaultMutableTreeNode) entries.get(0)).getParent(), (DefaultMutableTreeNode) entries.get(0));
						}
						else
							log.warn("Error deleting entry (" +ret+ "):" + ((ModelEpisode) ((DefaultMutableTreeNode) entries.get(0)).getUserObject()).getKey() +" "+ ((ModelEpisode) ((DefaultMutableTreeNode) entries.get(0)).getUserObject()).toString());
					}
					else {
						if (!((DefaultMutableTreeNode) entries.get(0)).isLeaf()) {

							DefaultMutableTreeNode node = (DefaultMutableTreeNode) entries.get(0);
							DefaultMutableTreeNode [] child = new DefaultMutableTreeNode[node.getChildCount()]; 

							for (int o = 0; o < node.getChildCount(); o++)
								child[o] = (DefaultMutableTreeNode) node.getChildAt(o);

							for (int u = 0; u < child.length; u++) {

								if ((ret = database.removeEpisode(((ModelEpisode) child[u].getUserObject()).getKey())) == 0) {
									if (!fastremove) {
										node.remove(child[u]);
//										Removing the chached info for the node
										treeCellRenderer.removeNode(child[u]);
									}
								}
								else
									log.warn("Error deleting episode (" +ret+ ") with key:" + ((ModelEpisode) child[u].getUserObject()).getKey());
							}
						}

						if ((ret = database.removeMovie(((ModelMovie)((DefaultMutableTreeNode) entries.get(0)).getUserObject()).getKey())) == 0) {
							/* Removes from the list... */
							if (!fastremove)
								updateList(root, (DefaultMutableTreeNode) entries.get(0));
						}
						else
							log.warn("Error deleting entry (" +ret+ "):"+ ((ModelMovie) ((DefaultMutableTreeNode) entries.get(0)).getUserObject()).getKey() +" "+ ((ModelMovie) ((DefaultMutableTreeNode) entries.get(0)).getUserObject()).toString());
					}
					entries.remove(0);
				}

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			MovieManager.getIt().setDeleting(false);

			final boolean finalFastremove = fastremove;
			
			SwingUtilities.invokeLater(new Runnable() {
				
				public void run() {
					if (finalFastremove)
						MovieManagerCommandSelect.executeAndReload(selectedIndex);
					else {
						
						if (selectedIndex == 0)
							moviesList.setSelectionRow(selectedIndex);
						else
							moviesList.setSelectionRow(selectedIndex-1);
						MovieManagerCommandSelect.execute();
					}
					MovieManager.getDialog().setAndShowEntries();
				}
			});			
		}
	}
}
