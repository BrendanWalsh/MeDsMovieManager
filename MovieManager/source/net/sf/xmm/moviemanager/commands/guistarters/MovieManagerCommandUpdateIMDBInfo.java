/**
 * @(#)MovieManagerCommandUpdateIMDBInfo.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands.guistarters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSaveChangedNotes;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.gui.DialogUpdateIMDbInfo;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;


public class MovieManagerCommandUpdateIMDBInfo extends JPanel implements ActionListener{

	Logger log = Logger.getLogger(getClass());

	boolean canceled = true;
	boolean done = false;
	JDialog dbImporter;

	boolean cancelAll = false;

	ModelImportExportSettings importSettings;

	void createAndShowGUI() {

		/* Owner, title, modal=true */
		dbImporter = new JDialog(MovieManager.getDialog(), Localizer.get("MovieManagerCommandUpdateIMDBInfo.dialog-imdb-info-updater.title"), true); //$NON-NLS-1$
		dbImporter.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		final JComponent newContentPane = new DialogUpdateIMDbInfo(this, dbImporter);
		newContentPane.setOpaque(true);
		dbImporter.setContentPane(newContentPane);
		dbImporter.pack();
		dbImporter.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dbImporter.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

				if (canceled || done) {
					dbImporter.dispose();
					MovieManagerCommandSelect.execute();
				}
			}
		});
		
		GUIUtil.enableDisposeOnEscapeKey(dbImporter, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (canceled || done) {
					dbImporter.dispose();
					MovieManagerCommandSelect.execute();
				}
			}
		});
		
		MovieManager mm = MovieManager.getIt();

		setLocation((int) mm.getLocation().getX()+(mm.getWidth()-getWidth())/2,
				(int) mm.getLocation().getY()+(mm.getHeight()-getHeight())/2);

		dbImporter.setLocation((int)mm.getLocation().getX()+(mm.getWidth()- dbImporter.getWidth())/2,
				(int)mm.getLocation().getY()+(mm.getHeight()- dbImporter.getHeight())/2);
		GUIUtil.show(dbImporter, true);
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public void dispose() {
		GUIUtil.show(dbImporter, false);
	}

	public void setCancelAll(boolean value) {
		cancelAll = value;
	}

	protected void execute() {

		// If any notes have been changed, they will be saved before updating list
		MovieManagerCommandSaveChangedNotes.execute();

		createAndShowGUI();
	}

	/**
	 * Invoked when an action occurs.
	 **/
	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
		execute();
	}
}

    
    
    
