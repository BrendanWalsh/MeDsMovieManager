package net.sf.xmm.moviemanager.gui;

/**
 * @(#)DialogNewVersionInfo.java 1.0 24.12.07 (dd.mm.yy)
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
 * Contact: mediterranean@users.sourceforge.net
 **/


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.CommandDialogDispose;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;

public class DialogNewVersionInfo extends JDialog {


	String version;
	String info;

	KeyboardShortcutManager shortcutManager = new KeyboardShortcutManager(this);

	/**
	 * The Constructor.
	 **/
	public DialogNewVersionInfo(String version, String info) {
		/* Dialog creation...*/
		super(MovieManager.getDialog());

		GUIUtil.enableDisposeOnEscapeKey(shortcutManager);

		/* Dialog properties...*/
		setTitle("Version Info");
		setModal(true);
		setResizable(false);

		/* Info panel...*/
		JPanel panelInfo = new JPanel();
		panelInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Current Version "),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		JLabel labelInfo = new JLabel(" Current version: " + MovieManager.getConfig().sysSettings.getVersion(),
				new ImageIcon(FileUtil.getImage("/images/filmFolder.png").getScaledInstance(55,55,Image.SCALE_SMOOTH)),
				JLabel.CENTER);
		labelInfo.setFont(new Font(labelInfo.getFont().getName(),Font.PLAIN,labelInfo.getFont().getSize()));
		panelInfo.add(labelInfo);

		/* New version panel... */
		JPanel newVersionPanel = new JPanel();
		newVersionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," New Version "),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		JLabel newVersionLabel = new JLabel(info, JLabel.CENTER);

		newVersionLabel.setFont(new Font(newVersionLabel.getFont().getName(),Font.PLAIN,newVersionLabel.getFont().getSize()));
		newVersionPanel.add(newVersionLabel);

		/* All stuff together... */
		JPanel all = new JPanel();
		all.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		all.setLayout(new BoxLayout(all,BoxLayout.Y_AXIS));
		all.add(panelInfo);
		all.add(newVersionPanel);

		/* Buttons panel...*/
		JPanel panelButtons = new JPanel();
		panelButtons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton buttonOk = new JButton("OK");
		buttonOk.setToolTipText("Close the About dialog");
		buttonOk.setActionCommand("About - OK");
		buttonOk.addActionListener(new CommandDialogDispose(this));
		panelButtons.add(buttonOk);

		/* Adds all and buttonsPanel... */
		getContentPane().add(all,BorderLayout.NORTH);
		getContentPane().add(panelButtons,BorderLayout.SOUTH);
		/* Packs and sets location... */
		pack();
		setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
				(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
	}

}
