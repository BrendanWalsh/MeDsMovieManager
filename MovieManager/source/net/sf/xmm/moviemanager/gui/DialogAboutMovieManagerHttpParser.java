/**
 * @(#)DialogAboutMovieManagerHttpParser.java 1.0 17.01.08 (dd.mm.yy)
 *
 * Copyright (2003) Bro
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.sf.xmm.moviemanager.commands.MovieManagerCommandOpenPage;

public class DialogAboutMovieManagerHttpParser extends JFrame {

	/**
	 * The Constructor.
	 **/
	public DialogAboutMovieManagerHttpParser() {
		/* Dialog creation...*/

		/* Close dialog... */
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		/*Enables dispose when pushing escape*/
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", escapeAction);

		/* Dialog properties...*/
		setTitle("About MeD's Movie Manager HTTP parser");
		setResizable(false);
		/* Info panel...*/
		
		JPanel panelInfo = new JPanel();
		panelInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Info "),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		JLabel labelInfo = new JLabel("<html> MeD's Movie Manager HTTP parser v 1.0 <br><br>" +
				"<center><a href=xmm.sf.net>http://xmm.sourceforge.net </a> </center>" +
				" </html>",
				new ImageIcon(getImage("/images/filmFolder.png").getScaledInstance(55,55,Image.SCALE_SMOOTH)),
				JLabel.CENTER);
		
		labelInfo.setFont(new Font(labelInfo.getFont().getName(),Font.PLAIN,labelInfo.getFont().getSize()));
		panelInfo.add(labelInfo);
		
		/* Copyright panel... */
		JPanel panelCopyright = new JPanel();
		panelCopyright.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Copyright "),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		JLabel labelCopyright = new JLabel("(C) 2004-2008 Bro",JLabel.CENTER);
		labelCopyright.setFont(new Font(labelCopyright.getFont().getName(),Font.PLAIN,labelCopyright.getFont().getSize()));
		panelCopyright.add(labelCopyright);
		
		/* Developers panel... */
		JPanel panelDevelopers = new JPanel();
		panelDevelopers.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Developers "),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		JLabel labelDevelopers = new JLabel("<html>Bro (bro3@user.sourceforge.net) </html>",JLabel.CENTER);
		labelDevelopers.setFont(new Font(labelDevelopers.getFont().getName(),Font.PLAIN,labelDevelopers.getFont().getSize()));
		panelDevelopers.add(labelDevelopers);
		
		/* Licenses panel... */
		JPanel panelLicenses = new JPanel();
		panelLicenses.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Licenses "),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		JLabel labelLicense = new JLabel("Licensed under The GNU Lesser General Public License, Version 2 or later", JLabel.CENTER);
		labelLicense.setFont(new Font(labelLicense.getFont().getName(),Font.PLAIN,labelLicense.getFont().getSize()-2));
		labelLicense.addMouseListener(new MovieManagerCommandOpenPage("http://www.fsf.org/licenses/info/GPLv2.html"));
		panelLicenses.add(labelLicense);
		
		/* All stuff together... */
		JPanel all = new JPanel();
		all.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		all.setLayout(new BoxLayout(all,BoxLayout.Y_AXIS));
		all.add(panelInfo);
		all.add(panelCopyright);
		all.add(panelDevelopers);
		all.add(panelLicenses);
		/* Buttons panel...*/
		JPanel panelButtons = new JPanel();
		panelButtons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton buttonOk = new JButton("OK");
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
		setLocationRelativeTo(null);
	}
	
	public static Image getImage(String imageName) {
		Image image = null;

		try {

			String path = "";

			if (!new File(imageName).exists()){
				path = System.getProperty("user.dir");
			}
			if (new File(path + imageName).exists()) {
				image = Toolkit.getDefaultToolkit().getImage(path + imageName);
			}
			else {

				try {
					//URL url = MovieManager.class.getResource(imageName);
					URL url = DialogAboutMovieManagerHttpParser.class.getResource(imageName);
					image = Toolkit.getDefaultToolkit().getImage(url);
				}
				catch (Exception e) {
					System.out.println("Exception:" + e.getMessage()); //$NON-NLS-1$
				}
			}

		} catch (Exception e) {
			System.out.println("Exception:" + e.getMessage()); //$NON-NLS-1$
		}
		return image;
	}

	 public static void main(String [] argv) {
			DialogAboutMovieManagerHttpParser dialog = new DialogAboutMovieManagerHttpParser();
			dialog.setVisible(true);
		}

}
