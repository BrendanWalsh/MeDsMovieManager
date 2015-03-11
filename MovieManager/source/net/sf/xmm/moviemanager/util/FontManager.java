/**
 * @(#)FontManager.java 1.0 24.11.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Vector;

import javax.swing.JComponent;

public class FontManager {

	private static Vector<JComponent> components = new Vector<JComponent>();

	static GraphicsEnvironment graphics  = GraphicsEnvironment.getLocalGraphicsEnvironment();

	String [] getAvailableFontFamilyNames() {
		return graphics.getAvailableFontFamilyNames();
	}


	public static void addComponent(JComponent c) {
		components.add(c);
	}

	public static void setFont(Font f) {

		// UIManager.put("Button.font", new Font("SansSerif",1,11));
		// 	UIManager.put("Label.font" , new Font("SansSerif",0,12));
		// 	UIManager.put("Menu.font", new Font("MS Sans Serif",0,12));
		// 	UIManager.put("MenuItem.font", new Font("MS Sans Serif",0,11));
		// 	UIManager.put("TextField.font",new Font("MS Sans Serif",0,12));

		for (int i = 0; i < components.size(); i++)
			((JComponent) components.get(i)).setFont(f);
	}
}
