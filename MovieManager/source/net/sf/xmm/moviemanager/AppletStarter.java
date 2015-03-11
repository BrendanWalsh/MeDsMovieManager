/**
 * @(#)AppletStarter.java 1.0 21.04.06 (dd.mm.yy)
 *
 * Copyright (2008) Bro	
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

package net.sf.xmm.moviemanager;

import javax.swing.JApplet;
import javax.swing.JFrame;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class AppletStarter extends JApplet {
    
    JApplet applet;
    public JFrame moviemanager;
    
    /** Initializes Applet by adding MovieManager's content pane. */
    public void init() {
        setSize(400,300);
        
        Logger root = Logger.getRootLogger();
        //root.setLevel(Level.OFF);
        
        root.removeAllAppenders();
        root.addAppender(new ConsoleAppender(new PatternLayout()));
        //ConsoleAppender appender = new ConsoleAppender(new PatternLayout());
        
        //Logger timestampLogger = Logger.getLogger("TimeStamp");
        //timestampLogger.setLevel(Level.OFF);
        //BasicConfigurator.configure();
        
        new MovieManager(this);
    }
}
