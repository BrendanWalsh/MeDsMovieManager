/**
 * @(#)ExtendedPatternLayout.java 1.0 03.11.05 (dd.mm.yy)
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


package net.sf.xmm.moviemanager.swing.extentions;

import org.apache.log4j.PatternLayout;


public class ExtendedPatternLayout extends PatternLayout {
    
    
    public ExtendedPatternLayout() {
	super();
    }
    
    public ExtendedPatternLayout(String pattern) {
	super(pattern);
    }

    public void setConversionPattern(String conversionPattern) {
	super.setConversionPattern(conversionPattern);
    }
}