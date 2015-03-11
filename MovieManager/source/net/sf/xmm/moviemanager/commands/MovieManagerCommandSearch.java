/**
 * @(#)MovieManagerCommandSearch.java 1.0 26.09.06 (dd.mm.yy)
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

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogSearch;
import net.sf.xmm.moviemanager.util.GUIUtil;

public class MovieManagerCommandSearch implements ActionListener {
    
	Logger log = Logger.getLogger(getClass());
	
	void execute() {
    
		if (DialogSearch.getDialogSearch() == null) {
		    DialogSearch dialogSearch = new DialogSearch();
		    GUIUtil.show(dialogSearch, true, MovieManager.getDialog());
		} else if (DialogSearch.getDialogSearch().isShowing()) {
            GUIUtil.show(DialogSearch.getDialogSearch(), false);
		} else {
            GUIUtil.show(DialogSearch.getDialogSearch(), true, MovieManager.getDialog());
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
