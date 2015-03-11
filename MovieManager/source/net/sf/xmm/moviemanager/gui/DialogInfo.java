/**
 * @(#)DialogAlert.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import net.sf.xmm.moviemanager.commands.CommandDialogDispose;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;

public class DialogInfo extends JDialog {

	Logger log = Logger.getLogger(getClass());

	KeyboardShortcutManager shortcutManager = new KeyboardShortcutManager(this);
	
	public DialogInfo(Dialog parent, String title, String infoMsg) {
		super(parent, true);
		createHTMLDialog(parent, title, infoMsg);
	}

	public DialogInfo(Frame parent, String title, String infoMsg) {
		super(parent, true);
		createHTMLDialog(parent, title, infoMsg);
	}


	void createHTMLDialog(Window parent, String title, String infoMsg) {

		try {

			setTitle(title);

			/* All stuff together... */
			JPanel panelInfo = new JPanel(new BorderLayout());
			panelInfo.setBorder(BorderFactory.createEmptyBorder(10,5,5,10));

			JLabel labelIcon = new JLabel();
			labelIcon.setBorder(BorderFactory.createEmptyBorder(5,5,5,8));
			labelIcon.setIcon(new ImageIcon(FileUtil.getImage("/images/film.png").getScaledInstance(50,50,Image.SCALE_SMOOTH)));

			JTextPane area = new JTextPane();
			area.setOpaque(false);
			area.setBorder(null);
			area.setEditable(false);
			area.setContentType("text/html");
			area.setText(infoMsg);

			area.setFocusable(true);

			JScrollPane scrollPane = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			panelInfo.add(labelIcon, BorderLayout.WEST);
			panelInfo.add(scrollPane, BorderLayout.EAST);

			if (scrollPane.getPreferredSize().getHeight() > 200)
				scrollPane.setPreferredSize(new Dimension((int)scrollPane.getPreferredSize().getWidth(), 200));

			makeRest(parent, panelInfo);

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage());
		}
	}


	void makeRest(Window parent, JComponent panelAlert) {
		/* Dialog properties...*/

		setModal(true);
		setResizable(false);

		GUIUtil.enableDisposeOnEscapeKey(shortcutManager);

		/* Buttons panel...*/
		JPanel panelButtons = new JPanel();
		panelButtons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
		JButton buttonOk = new JButton("OK");
		buttonOk.setActionCommand("Alert - OK");
		buttonOk.addActionListener(new CommandDialogDispose(this));
		panelButtons.add(buttonOk);
		/* Adds all and buttonsPanel... */    
		getContentPane().add(panelAlert, BorderLayout.NORTH);
		getContentPane().add(panelButtons, BorderLayout.SOUTH);
		/* Packs and sets location... */
		pack();

		//setSize(new Dimension((int)panelAlert.getPreferredSize().getWidth(), 200));

		//setLocationRelativeTo(parent);

		if (parent != null) {

			if ((parent.getLocation().getX() != 0) && (parent.getLocation().getY() != 0))
				setLocation((int) parent.getLocation().getX()+(parent.getWidth()-getWidth())/2,
						(int) parent.getLocation().getY()+(parent.getHeight()-getHeight())/2);
			else {
				Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
				setLocation((int)(dim.getWidth()-getWidth())/2, (int)(dim.getHeight()-getHeight())/2);
			}
		}
	}
}


