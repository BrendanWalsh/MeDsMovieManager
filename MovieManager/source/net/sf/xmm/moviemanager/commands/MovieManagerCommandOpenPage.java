/**
 * @(#)MovieManagerCommandOpenPage.java 1.0 10.10.05 (dd.mm.yy)
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.util.tools.BrowserOpener;

import org.apache.log4j.Logger;

public class MovieManagerCommandOpenPage extends MouseAdapter implements ActionListener {
    
	Logger log = Logger.getLogger(getClass());
        
    BrowserOpener opener;
    
	public MovieManagerCommandOpenPage(String url) {
		opener = new BrowserOpener(url);
	}
	
    
    /**
     * Invoked when the mouse button has been clicked
     * (pressed and released) on a component.
     **/
    public void mouseClicked(MouseEvent event) {
        log.debug("ActionPerformed: OpenPage (Movie Page)");
        opener.executeOpenBrowser(MovieManager.getConfig().getSystemWebBrowser(), MovieManager.getConfig().getBrowserPath());
    }
    
    /**
     * Invoked when an action occurs.
     **/
    public void actionPerformed(ActionEvent event) {
        log.debug("ActionPerformed: " + event.getActionCommand());
        opener.executeOpenBrowser(MovieManager.getConfig().getSystemWebBrowser(), MovieManager.getConfig().getBrowserPath());
    }
}
