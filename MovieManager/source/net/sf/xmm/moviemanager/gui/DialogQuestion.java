/**
 * @(#)DialogQuestion.java 1.0 23.04.05 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
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
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.CommandDialogDispose;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;

public class DialogQuestion extends JDialog  {

	Logger log = Logger.getLogger(getClass());

	/**
	 * The choosed answer.
	 */
	private boolean _answer = false;
	private String title = ""; //$NON-NLS-1$
	private String questionMsg = ""; //$NON-NLS-1$
	private Object [] list = null;

	KeyboardShortcutManager shortcutManager = new KeyboardShortcutManager(this);
	
	/**
	 * The Constructor.
	 **/
	public DialogQuestion(String title, String questionMsg) {
		/* Dialog creation...*/
		super(MovieManager.getDialog());

		this.title = title;
		this.questionMsg = questionMsg;

		execute();
	}

	public DialogQuestion(String title, String questionMsg, Object [] list) {
		/* Dialog creation...*/
		super(MovieManager.getDialog());

		this.title = title;
		this.questionMsg = questionMsg;
		this.list = list;

		execute();
	}

	void execute() {

		/* Dialog properties...*/
		setTitle(title);
		setModal(true);
		setResizable(false);

		GUIUtil.enableDisposeOnEscapeKey(shortcutManager);
		
		JPanel panelQuestion;

		/* If only one entry */
		if (list == null) {

			/* Panel question */
			panelQuestion = new JPanel();
			panelQuestion.setBorder(BorderFactory.createEmptyBorder(10,5,5,5));
			JLabel labelQuestion = new JLabel(questionMsg);
			labelQuestion.setIcon(new ImageIcon(FileUtil.getImage("/images/question.png").getScaledInstance(50,50,Image.SCALE_SMOOTH))); //$NON-NLS-1$

			panelQuestion.add(labelQuestion);

			/* If multiple entries */
		} else {

			/* Panel question */
			panelQuestion = new JPanel(new BorderLayout());
			panelQuestion.setBorder(BorderFactory.createEmptyBorder(10,5,5,5));
			JLabel labelQuestion = new JLabel(questionMsg);
			labelQuestion.setIcon(new ImageIcon(FileUtil.getImage("/images/question.png").getScaledInstance(50,50,Image.SCALE_SMOOTH))); //$NON-NLS-1$

			panelQuestion.add(labelQuestion, BorderLayout.NORTH);

			JTextArea area = new JTextArea();
			area.setEditable(false);
			area.setRows(10);

			for (int i = 0; i < list.length; i++) {
				area.append(((ModelEntry) list[i]).getTitle());
				if (!((ModelEntry) list[i]).getDate().equals("")) //$NON-NLS-1$
					area.append("  ("+ ((ModelEntry) list[i]).getDate()+")"); //$NON-NLS-1$ //$NON-NLS-2$
				area.append("\n"); //$NON-NLS-1$
			}

			area.setCaretPosition(0);

			JPanel movieList = new JPanel();
			movieList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), null));
			JScrollPane scrollPane = new JScrollPane(area);
			scrollPane.setPreferredSize(new Dimension(300, 130));

			movieList.add(scrollPane);
			panelQuestion.add(movieList, BorderLayout.SOUTH);
		}

		/* Buttons panel...*/
		JPanel panelButtons = new JPanel();
		panelButtons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
		JButton buttonYes = new JButton(Localizer.get("DialogQuestion.answer.yes")); //$NON-NLS-1$
		buttonYes.setActionCommand("Question - Yes"); //$NON-NLS-1$
		buttonYes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
				_answer = true;
				dispose();
			}});

		buttonYes.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					_answer = true;
					dispose();
				}
			}});

		panelButtons.add(buttonYes);


		JButton buttonNo = new JButton(Localizer.get("DialogQuestion.answer.no")); //$NON-NLS-1$
		buttonNo.setActionCommand("Question - No"); //$NON-NLS-1$
		buttonNo.addActionListener(new CommandDialogDispose(this));
		buttonNo.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					dispose();
				}
			}});

		panelButtons.add(buttonNo);

		/* Adds all and buttonsPanel... */    
		getContentPane().add(panelQuestion,BorderLayout.NORTH);
		getContentPane().add(panelButtons,BorderLayout.SOUTH);
		/* Packs and sets location... */
		pack();
		setLocation((int)MovieManager.getDialog().getLocation().getX()+(MovieManager.getDialog().getWidth()-getWidth())/2,
				(int)MovieManager.getDialog().getLocation().getY()+(MovieManager.getDialog().getHeight()-getHeight())/2);
		buttonYes.requestFocus();
	}

	/**
	 * Getter for _answer.
	 */
	public boolean getAnswer() {
		return _answer;
	}
}

