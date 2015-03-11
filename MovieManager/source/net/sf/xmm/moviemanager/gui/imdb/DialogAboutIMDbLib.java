/**
 * @(#)DialogAbout.java 1.0 24.01.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.gui.imdb;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.sf.xmm.moviemanager.imdblib.IMDbLib;

public class DialogAboutIMDbLib extends JDialog {

	public static void main (String [] args) {
		DialogAboutIMDbLib dialog = new  DialogAboutIMDbLib();
		dialog.setVisible(true);
	}

	/**
	 * The Constructor.
	 **/
	public DialogAboutIMDbLib() {
		
		/*Enables dispose when pushing escape*/
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction()  {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", escapeAction);


		/* Dialog properties...*/
		setTitle("About IMDb library");
		setModal(true);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		JPanel all = createAboutPanel();

		/* Buttons panel...*/
		JPanel panelButtons = new JPanel();
		panelButtons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton buttonOk = new JButton("Close");
		buttonOk.setToolTipText("Close the About dialog");
		buttonOk.setActionCommand("About - OK");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		panelButtons.add(buttonOk);
		/* Adds all and buttonsPanel... */
		getContentPane().add(all,BorderLayout.NORTH);
		getContentPane().add(panelButtons,BorderLayout.SOUTH);
		/* Packs and sets location... */
		pack();
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
		setLocation((int)(screenSize.getWidth() - getSize().getWidth())/2,
                    (int)(screenSize.getHeight() - getSize().getHeight())/2 - 12);
            
	}
	
	
	public static JPanel createAboutPanel() {
		
		/* Info panel...*/
		JPanel panelInfo = new JPanel();
		panelInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Info "),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		JLabel labelInfo = new JLabel("<html><center> MeD's Movie Manager IMDb library <br> version " + IMDbLib.getVersion() + " (Rel. "+ IMDbLib.getRelease() +")</center></html>", JLabel.CENTER);
		labelInfo.setFont(new Font(labelInfo.getFont().getName(),Font.PLAIN,labelInfo.getFont().getSize()));
		panelInfo.add(labelInfo);
		/* Copyright panel... */
		JPanel panelCopyright = new JPanel();
		panelCopyright.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Copyright "),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		JLabel labelCopyright = new JLabel("(C) 2003-2010 Bro",JLabel.CENTER);
		labelCopyright.setFont(new Font(labelCopyright.getFont().getName(),Font.PLAIN,labelCopyright.getFont().getSize()));
		panelCopyright.add(labelCopyright);

		/* Licenses panel... */
		JPanel panelLicenses = new JPanel();
		panelLicenses.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Licenses "),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		JLabel labelLicense = new JLabel("Licensed under The GNU General Public License, Version 2 or later",JLabel.CENTER);
		labelLicense.setFont(new Font(labelLicense.getFont().getName(),Font.PLAIN,labelLicense.getFont().getSize()-2));
		panelLicenses.add(labelLicense);		

		/* Licenses panel... */
		JPanel panelDependency = new JPanel();
		panelDependency.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Dependency "),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		JLabel labelDependency = new JLabel("log4j",JLabel.CENTER);
		labelDependency.setFont(new Font(labelDependency.getFont().getName(),Font.PLAIN,labelDependency.getFont().getSize()-2));
		panelDependency.add(labelDependency);		
		
		/* All stuff together... */
		JPanel all = new JPanel();
		all.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		all.setLayout(new BoxLayout(all,BoxLayout.Y_AXIS));
		all.add(panelInfo);
		all.add(panelCopyright);
		all.add(panelLicenses);
		all.add(panelDependency);
		
		return all;
	}
}
