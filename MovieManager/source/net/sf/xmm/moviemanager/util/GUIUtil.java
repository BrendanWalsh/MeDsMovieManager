/**
 * @(#)GUIUtil.java 1.0 26.09.06 (dd.mm.yy)
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;

import org.apache.log4j.Logger;

public class GUIUtil {

	static Logger log = Logger.getLogger(GUIUtil.class);

	public static void show(final java.awt.Container container, final boolean visible) {
		show(container, visible, null);
	}
	
	public static void show(final Container container, final boolean visible, Window appearOnLeftSide) {

		if (visible)
			adjustLocation(container, appearOnLeftSide);
		
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				container.setVisible(visible);
			}
		});
	}

	public static void showAndWait(final Container container, final boolean visible) {
		showAndWait(container, visible, null);
	}
	
	public static void showAndWait(final Container container, final boolean visible, Window appearOnLeftSide) {

		try {

			if (visible)
				adjustLocation(container, appearOnLeftSide);

			if (SwingUtilities.isEventDispatchThread()) {
				container.setVisible(visible);
			}
			else {
				SwingUtilities.invokeAndWait(new Runnable(){
					public void run() {
						container.setVisible(visible);
					}
				});
			}
		} catch (InterruptedException i) {
			log.error("InterruptedException:" + i.getMessage(), i);
		} catch (java.lang.reflect.InvocationTargetException i) {
			log.error("InvocationTargetException:" + i.getMessage(), i);
		} catch (Exception i) {
			log.error("Exception:" + i.getMessage(), i);
		}
	}

	private static void adjustLocation(Container container, Window appearOnLeftSide) {
		
		Point p = container.getLocation();

		Dimension size = container.getSize();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		int widthLocation = (int) (p.getX() + size.getWidth());
		int heightLocation = (int ) (p.getY() + size.getHeight());	

		if (appearOnLeftSide != null) {
			
			Point appearLeft = appearOnLeftSide.getLocation();
			
			int wSize = (int) size.getWidth();
			p.setLocation(appearLeft.getX()- wSize, p.getY());
		}
		
		if (widthLocation > screenSize.getWidth()) {
			int diff = (int) (widthLocation - screenSize.getWidth());
			p.setLocation((p.getX() - diff), p.getY());
		}

		if (heightLocation > screenSize.getHeight()) {
			int diff = (int) (heightLocation - screenSize.getHeight());
			p.setLocation(p.getX(), (p.getY() - diff));
		}

		if (p.getX() < 0) {
			p.setLocation(0.0, p.getY());
		}

		if (p.getY() < 10) {
			p.setLocation(p.getX(), 15.0);
		}

		container.setLocation(p);
	}

	public static void invokeLater(Runnable runnable) {

		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(runnable);
		else
			runnable.run();
	}
	
	public static void invokeAndWait(Runnable r) throws InterruptedException, InvocationTargetException {
		
		if (SwingUtilities.isEventDispatchThread())
			r.run();
		else
			SwingUtilities.invokeAndWait(r);
		
	}

	public static void isEDT() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new Error("assertion failed: not on EDT");
		}
	}

	/**
	 * Must not be executed on the EDT.
	 */  
	public static void isNotEDT() {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new Error("assertion failed: on EDT");
		}
	}
	
	
	public static DefaultListModel toDefaultListModel(ArrayList<?> list) {

		DefaultListModel listModel = new DefaultListModel();

		for (int i = 0; i < list.size(); i++) {
			listModel.addElement(list.get(i));
		}

		return listModel;
	}
	
	
	public static boolean isRightMouseButton(MouseEvent event) {
		return event.getButton() == MouseEvent.BUTTON3;
	}
	
	// SwingUtilities.isLeftButton seemed to return true on right button on OS X.
	public static boolean isLeftMouseButton(MouseEvent event) {
		return event.getButton() == MouseEvent.BUTTON1;
	}
	
	
	
	public static void enableDisposeOnEscapeKey(KeyboardShortcutManager shortcutManager) {
		enableDisposeOnEscapeKey(shortcutManager, null);
	}
	
	public static void enableDisposeOnEscapeKey(KeyboardShortcutManager shortcutManager, Action escapeAction) {
		enableDisposeOnEscapeKey(shortcutManager, null, escapeAction);
	}
	
	public static void enableDisposeOnEscapeKey(final KeyboardShortcutManager shortcutManager, String actionDescription, final Action escapeAction) {

		if (actionDescription == null)
			actionDescription = "Close window";
		
		Action disposeAction = enableDisposeOnAction(shortcutManager, escapeAction);
		enableActionOnEscapeKey(shortcutManager, actionDescription, disposeAction);
	}
	
	public static void enableActionOnEscapeKey(final KeyboardShortcutManager shortcutManager, String actionDescription, final Action escapeAction) {
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);

		try {
			shortcutManager.registerKeyboardShortcut(key, actionDescription, escapeAction);
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
	}
	
	
	public static void enableDisposeOnEscapeKey(final JDialog dialog) {
		enableDisposeOnEscapeKey(dialog, null);
	}
	
	public static void enableDisposeOnEscapeKey(final JDialog dialog, final Action escapeAction) {
		Action disposeAction = enableDisposeOnAction(dialog, escapeAction);
		enableActionOnEscapeKey(dialog.getRootPane(), disposeAction);
	}
	
	public static void enableActionOnEscapeKey(JRootPane rootPane, final Action escapeAction) {
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		KeyboardShortcutManager.registerKeyboardShortcut(key, escapeAction, rootPane);
	}
	
	public static Action enableDisposeOnAction(final JDialog dialog, final Action escapeAction) {
		return enableDisposeOnAction(null, dialog, escapeAction);
	}
	
	
	public static Action enableDisposeOnAction(KeyboardShortcutManager shortcutManager, final Action escapeAction) {
		return enableDisposeOnAction(shortcutManager, null, escapeAction);
	}
	
	public static Action enableDisposeOnAction(final KeyboardShortcutManager shortcutManager, final JDialog dialog, final Action escapeAction) {

		Action disposeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				
				if (escapeAction != null) {
					escapeAction.actionPerformed(e);
				}
								
				// Hide shortcut panel if it's visible
				if (shortcutManager != null) {
					if (shortcutManager.isShortCutPanelVisible())
						shortcutManager.hideShortCutPanel();
					else
						shortcutManager.getJDialog().dispose();
				}
				else 
					dialog.dispose();
			}
		};
		return disposeAction;
	}
}
