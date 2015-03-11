/**
 * @(#)DialogUpdateIMDbInfo.java
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

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandUpdateIMDBInfo;
import net.sf.xmm.moviemanager.commands.importexport.IMDbInfoUpdater;
import net.sf.xmm.moviemanager.swing.extentions.ButtonGroupNoSelection;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.util.DocumentRegExp;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;

public class DialogUpdateIMDbInfo extends JPanel implements ActionListener, ItemListener {

	Logger log = Logger.getLogger(getClass());

	private JProgressBar progressBar;

	public final static int milliseconds = 400;
	private Timer timer;
	private JButton startButton;
	private JButton cancelButton;
	private JButton closeButton;

	private IMDbInfoUpdater imdbInfoUpdater;
	private JTextArea taskOutput;
	private String newline = "\n";

	
	int movieCounter = 0;
	int processedCounter = 0;
	int lengthOfTask = 0;
	long conversionStart = 0;
	boolean canceled;
	ArrayList<String> transferred;
	MovieManagerCommandUpdateIMDBInfo parent;

	JCheckBox skipEntriesWithIMDbID;
	JCheckBox skipEntriesWithoutIMDbID;

	/* update settings buttons */
	JCheckBox titleUpdate;
	JCheckBox titleUpdateIfEmpty;
	JCheckBox coverUpdate;
	JCheckBox coverUpdateIfEmpty;
	JCheckBox dateUpdate;
	JCheckBox dateUpdateIfEmpty;
	JCheckBox colourUpdate;
	JCheckBox colourUpdateIfEmpty;
	JCheckBox directedByUpdate;
	JCheckBox directedByUpdateIfEmpty;
	JCheckBox writtenByUpdate;
	JCheckBox writtenByUpdateIfEmpty;
	JCheckBox genreUpdate;
	JCheckBox genreUpdateIfEmpty;
	JCheckBox ratingUpdate;
	JCheckBox ratingUpdateIfEmpty;
	JCheckBox countryUpdate;
	JCheckBox countryUpdateIfEmpty;
	JCheckBox languageUpdate;
	JCheckBox languageUpdateIfEmpty;
	JCheckBox plotUpdate;
	JCheckBox plotUpdateIfEmpty;
	JCheckBox castUpdate;
	JCheckBox castUpdateIfEmpty;
	JCheckBox akaUpdate;
	JCheckBox akaUpdateIfEmpty;
	JCheckBox soundMixUpdate;
	JCheckBox soundMixUpdateIfEmpty;
	JCheckBox runtimeUpdate;
	JCheckBox runtimeUpdateIfEmpty;
	JCheckBox awardsUpdate;
	JCheckBox awardsUpdateIfEmpty;
	JCheckBox mpaaUpdate;
	JCheckBox mpaaUpdateIfEmpty;
	JCheckBox certificationUpdate;
	JCheckBox certificationUpdateIfEmpty;

	JCheckBox markAll;
	JCheckBox markAllIfEmpty;

	JTextField threadCountField;
	
	JDialog dialog;

	KeyboardShortcutManager shortcutManager;
	
	public DialogUpdateIMDbInfo(final MovieManagerCommandUpdateIMDBInfo parent, JDialog dialog) {
		super(new BorderLayout());
		this.parent = parent;
		this.dialog = dialog;

		imdbInfoUpdater = new IMDbInfoUpdater();

		createComponents();
		
		shortcutManager = new KeyboardShortcutManager(dialog);
		setHotkeyModifiers();
	}
	
	
	void createComponents() {
		
		startButton = new JButton("Start");
		startButton.setActionCommand("Start");
		startButton.addActionListener(this);

		cancelButton = new JButton("Abort");
		cancelButton.setActionCommand("Cancel");
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(this);

		closeButton = new JButton("Close");
		closeButton.setActionCommand("Close");
		closeButton.setEnabled(true);
		closeButton.addActionListener(this);

		threadCountField = new JTextField(2);
		threadCountField.setToolTipText("The number of concurrent threads performing updates. Remember, each thread creates multiple HTTP connections");
		JLabel threadLabel = new JLabel("Thread count:");
		threadLabel.setLabelFor(threadCountField);
		
		threadCountField.setDocument(new DocumentRegExp("(\\d)*", 2));
		threadCountField.setText(""+imdbInfoUpdater.getThreadCount());
		
		
		progressBar = new JProgressBar();
		progressBar.setValue(0);
		progressBar.setString("                                 ");
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(200, 25));

		taskOutput = new JTextArea(20, 50);
		taskOutput.setMargin(new Insets(5,5,5,5));
		taskOutput.setEditable(false);
		taskOutput.setCursor(null); 

		JPanel panel = new JPanel();
		panel.add(startButton);
		panel.add(cancelButton);
		panel.add(closeButton);
		panel.add(threadLabel);		
		panel.add(threadCountField);
		panel.add(progressBar);


		/* Creating update options */

		JPanel updateSettingsPanel = new JPanel();
		updateSettingsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,5,2,5), 
				BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
						" Update settings ",
						TitledBorder.DEFAULT_JUSTIFICATION,
						TitledBorder.DEFAULT_POSITION,
						new Font(updateSettingsPanel.getFont().getName(),Font.PLAIN, updateSettingsPanel.getFont().getSize())),
						BorderFactory.createEmptyBorder(2,5,3,5))));

		updateSettingsPanel.setLayout(new BoxLayout(updateSettingsPanel, BoxLayout.Y_AXIS));

		JPanel settings = new JPanel();
		settings.setBorder(BorderFactory.createEmptyBorder(0,0,6,0));
		settings.setLayout(new BorderLayout());


		skipEntriesWithIMDbID = new JCheckBox("Skip entries with IMDb ID");
		skipEntriesWithoutIMDbID = new JCheckBox("Skip entries without IMDb ID");
		settings.add(skipEntriesWithIMDbID, BorderLayout.NORTH);
		settings.add(skipEntriesWithoutIMDbID, BorderLayout.SOUTH);

		// Regular ButtonGroup doesn't allow for both to be unselected.
		ActionListener skipButtonsListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (arg0.getSource() == skipEntriesWithIMDbID)
					skipEntriesWithoutIMDbID.setSelected(false);
				else
					skipEntriesWithIMDbID.setSelected(false);
			}
		};
		skipEntriesWithIMDbID.addActionListener(skipButtonsListener);
		skipEntriesWithoutIMDbID.addActionListener(skipButtonsListener);

		JPanel infoOptions = new JPanel();

		updateSettingsPanel.add(settings);
		updateSettingsPanel.add(infoOptions);

		double size[][] = {{TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 10, TableLayout.PREFERRED}, 
				{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 10, TableLayout.PREFERRED}};

		infoOptions.setLayout(new TableLayout(size));

		/* Header */

		JLabel headerUpdateInfo = new JLabel("Update");
		infoOptions.add(headerUpdateInfo, "0, 0, CENTER, CENTER");

		JLabel headerIfEmpty = new JLabel("If empty");
		infoOptions.add(headerIfEmpty, "2, 0, CENTER, CENTER");

		JLabel headerFiled = new JLabel("Field");
		infoOptions.add(headerFiled, "4, 0, CENTER, CENTER");


		/* Title */
		ButtonGroupNoSelection titleButtonGroup = new ButtonGroupNoSelection();

		titleUpdate = new JCheckBox();
		infoOptions.add(titleUpdate, "0, 1, CENTER, CENTER");
		titleButtonGroup.add(titleUpdate);

		titleUpdateIfEmpty = new JCheckBox();
		infoOptions.add(titleUpdateIfEmpty, "2, 1, CENTER, CENTER");
		titleButtonGroup.add(titleUpdateIfEmpty);

		JLabel titleLabel = new JLabel("Title");
		infoOptions.add(titleLabel, "4, 1");


		/* Cover */
		ButtonGroupNoSelection coverButtonGroup = new ButtonGroupNoSelection();

		coverUpdate = new JCheckBox();
		infoOptions.add(coverUpdate, "0, 2, CENTER, CENTER");
		coverButtonGroup.add(coverUpdate);

		coverUpdateIfEmpty = new JCheckBox();
		infoOptions.add(coverUpdateIfEmpty, "2, 2, CENTER, CENTER");
		coverButtonGroup.add(coverUpdateIfEmpty);

		JLabel coverLabel = new JLabel("Cover");
		infoOptions.add(coverLabel, "4, 2");


		/* date */
		ButtonGroupNoSelection dateButtonGroup = new ButtonGroupNoSelection();

		dateUpdate = new JCheckBox();
		infoOptions.add(dateUpdate, "0, 3, CENTER, CENTER");
		dateButtonGroup.add(dateUpdate);

		dateUpdateIfEmpty = new JCheckBox();
		infoOptions.add(dateUpdateIfEmpty, "2, 3, CENTER, CENTER");
		dateButtonGroup.add(dateUpdateIfEmpty);

		JLabel dateLabel = new JLabel("Date");
		infoOptions.add(dateLabel, "4, 3");


		/* colour */
		ButtonGroupNoSelection colourButtonGroup = new ButtonGroupNoSelection();

		colourUpdate = new JCheckBox();
		infoOptions.add(colourUpdate, "0, 4, CENTER, CENTER");
		colourButtonGroup.add(colourUpdate);

		colourUpdateIfEmpty = new JCheckBox();
		infoOptions.add(colourUpdateIfEmpty, "2, 4, CENTER, CENTER");
		colourButtonGroup.add(colourUpdateIfEmpty);

		JLabel colourLabel = new JLabel("Colour");
		infoOptions.add(colourLabel, "4, 4");


		/* Directed By */
		ButtonGroupNoSelection directedByButtonGroup = new ButtonGroupNoSelection();

		directedByUpdate = new JCheckBox();
		infoOptions.add(directedByUpdate, "0, 5, CENTER, CENTER");
		directedByButtonGroup.add(directedByUpdate);

		directedByUpdateIfEmpty = new JCheckBox();
		infoOptions.add(directedByUpdateIfEmpty, "2, 5, CENTER, CENTER");
		directedByButtonGroup.add(directedByUpdateIfEmpty);

		JLabel directedByLabel = new JLabel("Directed By");
		infoOptions.add(directedByLabel, "4, 5");


		/* writtenBy */
		ButtonGroupNoSelection writtenByButtonGroup = new ButtonGroupNoSelection();

		writtenByUpdate = new JCheckBox();
		infoOptions.add(writtenByUpdate, "0, 6, CENTER, CENTER");
		writtenByButtonGroup.add(writtenByUpdate);

		writtenByUpdateIfEmpty = new JCheckBox();
		infoOptions.add(writtenByUpdateIfEmpty, "2, 6, CENTER, CENTER");
		writtenByButtonGroup.add(writtenByUpdateIfEmpty);

		JLabel writtenByLabel = new JLabel("Written By");
		infoOptions.add(writtenByLabel, "4, 6");


		/* genre */
		ButtonGroupNoSelection genreButtonGroup = new ButtonGroupNoSelection();

		genreUpdate = new JCheckBox();
		infoOptions.add(genreUpdate, "0, 7, CENTER, CENTER");
		genreButtonGroup.add(genreUpdate);

		genreUpdateIfEmpty = new JCheckBox();
		infoOptions.add(genreUpdateIfEmpty, "2, 7, CENTER, CENTER");
		genreButtonGroup.add(genreUpdateIfEmpty);

		JLabel genreLabel = new JLabel("Genre");
		infoOptions.add(genreLabel, "4, 7");


		/* rating */
		ButtonGroupNoSelection ratingButtonGroup = new ButtonGroupNoSelection();

		ratingUpdate = new JCheckBox();
		infoOptions.add(ratingUpdate, "0, 8, CENTER, CENTER");
		ratingButtonGroup.add(ratingUpdate);

		ratingUpdateIfEmpty = new JCheckBox();
		infoOptions.add(ratingUpdateIfEmpty, "2, 8, CENTER, CENTER");
		ratingButtonGroup.add(ratingUpdateIfEmpty);

		JLabel ratingLabel = new JLabel("Rating");
		infoOptions.add(ratingLabel, "4, 8");

		/* country */
		ButtonGroupNoSelection countryButtonGroup = new ButtonGroupNoSelection();

		countryUpdate = new JCheckBox();
		infoOptions.add(countryUpdate, "0, 9, CENTER, CENTER");
		countryButtonGroup.add(countryUpdate);

		countryUpdateIfEmpty = new JCheckBox();
		infoOptions.add(countryUpdateIfEmpty, "2, 9, CENTER, CENTER");
		countryButtonGroup.add(countryUpdateIfEmpty);

		JLabel countryLabel = new JLabel("Country");
		infoOptions.add(countryLabel, "4, 9");

		/* language */
		ButtonGroupNoSelection languageButtonGroup = new ButtonGroupNoSelection();

		languageUpdate = new JCheckBox();
		infoOptions.add(languageUpdate, "0, 10, CENTER, CENTER");
		languageButtonGroup.add(languageUpdate);

		languageUpdateIfEmpty = new JCheckBox();
		infoOptions.add(languageUpdateIfEmpty, "2, 10, CENTER, CENTER");
		languageButtonGroup.add(languageUpdateIfEmpty);

		JLabel languageLabel = new JLabel("Language");
		infoOptions.add(languageLabel, "4, 10");


		/* plot */
		ButtonGroupNoSelection plotButtonGroup = new ButtonGroupNoSelection();

		plotUpdate = new JCheckBox();
		infoOptions.add(plotUpdate, "0, 11, CENTER, CENTER");
		plotButtonGroup.add(plotUpdate);

		plotUpdateIfEmpty = new JCheckBox();
		infoOptions.add(plotUpdateIfEmpty, "2, 11, CENTER, CENTER");
		plotButtonGroup.add(plotUpdateIfEmpty);

		JLabel plotLabel = new JLabel("Plot");
		infoOptions.add(plotLabel, "4, 11");


		/* cast */
		ButtonGroupNoSelection castButtonGroup = new ButtonGroupNoSelection();

		castUpdate = new JCheckBox();
		infoOptions.add(castUpdate, "0, 12, CENTER, CENTER");
		castButtonGroup.add(castUpdate);

		castUpdateIfEmpty = new JCheckBox();
		infoOptions.add(castUpdateIfEmpty, "2, 12, CENTER, CENTER");
		castButtonGroup.add(castUpdateIfEmpty);

		JLabel castLabel = new JLabel("Cast");
		infoOptions.add(castLabel, "4, 12");


		/* aka */
		ButtonGroupNoSelection akaButtonGroup = new ButtonGroupNoSelection();

		akaUpdate = new JCheckBox();
		infoOptions.add(akaUpdate, "0, 13, CENTER, CENTER");
		akaButtonGroup.add(akaUpdate);

		akaUpdateIfEmpty = new JCheckBox();
		infoOptions.add(akaUpdateIfEmpty, "2, 13, CENTER, CENTER");
		akaButtonGroup.add(akaUpdateIfEmpty);

		JLabel akaLabel = new JLabel("Also Know As");
		infoOptions.add(akaLabel, "4, 13");


		/* soundMix */
		ButtonGroupNoSelection soundMixButtonGroup = new ButtonGroupNoSelection();

		soundMixUpdate = new JCheckBox();
		infoOptions.add(soundMixUpdate, "0, 14, CENTER, CENTER");
		soundMixButtonGroup.add(soundMixUpdate);

		soundMixUpdateIfEmpty = new JCheckBox();
		infoOptions.add(soundMixUpdateIfEmpty, "2, 14, CENTER, CENTER");
		soundMixButtonGroup.add(soundMixUpdateIfEmpty);

		JLabel soundMixLabel = new JLabel("Sound Mix");
		infoOptions.add(soundMixLabel, "4, 14");


		/* runtime */
		ButtonGroupNoSelection runtimeButtonGroup = new ButtonGroupNoSelection();

		runtimeUpdate = new JCheckBox();
		infoOptions.add(runtimeUpdate, "0, 15, CENTER, CENTER");
		runtimeButtonGroup.add(runtimeUpdate);

		runtimeUpdateIfEmpty = new JCheckBox();
		infoOptions.add(runtimeUpdateIfEmpty, "2, 15, CENTER, CENTER");
		runtimeButtonGroup.add(runtimeUpdateIfEmpty);

		JLabel runtimeLabel = new JLabel("Runtime");
		infoOptions.add(runtimeLabel, "4, 15");


		/* awards */
		ButtonGroupNoSelection awardsButtonGroup = new ButtonGroupNoSelection();

		awardsUpdate = new JCheckBox();
		infoOptions.add(awardsUpdate, "0, 16, CENTER, CENTER");
		awardsButtonGroup.add(awardsUpdate);

		awardsUpdateIfEmpty = new JCheckBox();
		infoOptions.add(awardsUpdateIfEmpty, "2, 16, CENTER, CENTER");
		awardsButtonGroup.add(awardsUpdateIfEmpty);

		JLabel awardsLabel = new JLabel("Awards");
		infoOptions.add(awardsLabel, "4, 16");



		/* mpaa */
		ButtonGroupNoSelection mpaaButtonGroup = new ButtonGroupNoSelection();

		mpaaUpdate = new JCheckBox();
		infoOptions.add(mpaaUpdate, "0, 17, CENTER, CENTER");
		mpaaButtonGroup.add(mpaaUpdate);

		mpaaUpdateIfEmpty = new JCheckBox();
		infoOptions.add(mpaaUpdateIfEmpty, "2, 17, CENTER, CENTER");
		mpaaButtonGroup.add(mpaaUpdateIfEmpty);

		JLabel mpaaLabel = new JLabel("MPAA");
		infoOptions.add(mpaaLabel, "4, 17");


		/* certification */
		ButtonGroupNoSelection certificationButtonGroup = new ButtonGroupNoSelection();

		certificationUpdate = new JCheckBox();
		infoOptions.add(certificationUpdate, "0, 18, CENTER, CENTER");
		certificationButtonGroup.add(certificationUpdate);

		certificationUpdateIfEmpty = new JCheckBox();
		infoOptions.add(certificationUpdateIfEmpty, "2, 18, CENTER, CENTER");
		certificationButtonGroup.add(certificationUpdateIfEmpty);

		JLabel certificationLabel = new JLabel("Certification");
		infoOptions.add(certificationLabel, "4, 18");


		/* mark all */
		ButtonGroupNoSelection markAllButtonGroup = new ButtonGroupNoSelection();

		markAll = new JCheckBox();
		infoOptions.add(markAll, "0, 20, CENTER, CENTER");
		markAllButtonGroup.add(markAll);
		markAll.addItemListener(this);

		markAllIfEmpty = new JCheckBox();
		infoOptions.add(markAllIfEmpty, "2, 20, CENTER, CENTER");
		markAllButtonGroup.add(markAllIfEmpty);
		markAllIfEmpty.addItemListener(this);

		JLabel markAllLabel = new JLabel("(de)select All");
		infoOptions.add(markAllLabel, "4, 20");



		JPanel updatePanel = new JPanel(new BorderLayout());

		updatePanel.add(panel, BorderLayout.PAGE_START);
		updatePanel.add(new JScrollPane(taskOutput), BorderLayout.CENTER);

		add(updatePanel, BorderLayout.WEST);
		add(updateSettingsPanel, BorderLayout.EAST);

		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		/*Create a timer*/
		timer = new Timer(milliseconds, new TimerListener());
	}

	
	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			
			if (transferred == null) {
				transferred = imdbInfoUpdater.getTransferred();
			}

			if (lengthOfTask == 0) {
				lengthOfTask = imdbInfoUpdater.getLengthOfTask();

				if (lengthOfTask != 0) {
					progressBar.setMinimum(0);
					progressBar.setMaximum(lengthOfTask);
				}
			}
			
			while (transferred != null && processedCounter < lengthOfTask && transferred.size() > 0) {
				
				movieCounter++;
				int percent = ((processedCounter+1) * 100)/lengthOfTask;

				String msg = percent+ "%  (" + (processedCounter+1) + " out of " + lengthOfTask+")     ";
				progressBar.setValue(processedCounter+1);
				progressBar.setString(msg);

				String appendText = String.format("%-3d - %s", movieCounter, ((String) transferred.remove(0)) + newline);
				taskOutput.append(appendText);
				taskOutput.setCaretPosition(taskOutput.getDocument().getLength());
				processedCounter++;
			}

			if (imdbInfoUpdater.isDone() || canceled) {
				timer.stop();

				if (!canceled) {

					taskOutput.append(newline + "Update process finished" + newline);

					taskOutput.append(movieCounter + " entries processed in " + (millisecondsToString(System.currentTimeMillis() - conversionStart)) + newline);
					closeButton.setEnabled(true);
					cancelButton.setEnabled(false);
					startButton.setEnabled(true);
					parent.setDone(true);
				}
				else {
					taskOutput.append(newline + "Import aborted!" + newline);
					parent.setCanceled(true);

					imdbInfoUpdater = new IMDbInfoUpdater();
					timer = new Timer(milliseconds, new TimerListener());

					processedCounter = 0;
					movieCounter = 0;
					transferred = null;
				}
			}
		}
	}


	public void itemStateChanged(ItemEvent e) {

		if (e.getSource().equals(markAll)) {

			boolean value = markAll.isSelected();

			titleUpdate.setSelected(value);
			coverUpdate.setSelected(value);
			dateUpdate.setSelected(value);
			colourUpdate.setSelected(value);
			directedByUpdate.setSelected(value);
			writtenByUpdate.setSelected(value);
			genreUpdate.setSelected(value);
			ratingUpdate.setSelected(value);
			countryUpdate.setSelected(value);
			languageUpdate.setSelected(value);
			plotUpdate.setSelected(value);
			castUpdate.setSelected(value);
			akaUpdate.setSelected(value);
			soundMixUpdate.setSelected(value);
			runtimeUpdate.setSelected(value);
			awardsUpdate.setSelected(value);
			mpaaUpdate.setSelected(value);
			certificationUpdate.setSelected(value);
		}

		if (e.getSource().equals(markAllIfEmpty)) {

			boolean value = markAllIfEmpty.isSelected();

			titleUpdateIfEmpty.setSelected(value);
			coverUpdateIfEmpty.setSelected(value);
			dateUpdateIfEmpty.setSelected(value);
			colourUpdateIfEmpty.setSelected(value);
			directedByUpdateIfEmpty.setSelected(value);
			writtenByUpdateIfEmpty.setSelected(value);
			genreUpdateIfEmpty.setSelected(value);
			ratingUpdateIfEmpty.setSelected(value);
			countryUpdateIfEmpty.setSelected(value);
			languageUpdateIfEmpty.setSelected(value);
			plotUpdateIfEmpty.setSelected(value);
			castUpdateIfEmpty.setSelected(value);
			akaUpdateIfEmpty.setSelected(value);
			soundMixUpdateIfEmpty.setSelected(value);
			runtimeUpdateIfEmpty.setSelected(value);
			awardsUpdateIfEmpty.setSelected(value);
			mpaaUpdateIfEmpty.setSelected(value);
			certificationUpdateIfEmpty.setSelected(value);
		}
	}




	void handleStartUpdate() {
		
		boolean anySelectedButton = false;
		
		imdbInfoUpdater.setSkipEntriesWithIMDbID(skipEntriesWithIMDbID.isSelected());
		imdbInfoUpdater.setSkipEntriesWithNoIMDbID(skipEntriesWithoutIMDbID.isSelected());

		if (titleUpdate.isSelected()) {
			imdbInfoUpdater.title = 1;
			anySelectedButton = true;
		}
		else if (titleUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.title = 2;
			anySelectedButton = true;
		}


		if (coverUpdate.isSelected()) {
			imdbInfoUpdater.cover = 1;
			anySelectedButton = true;
		}
		else if (coverUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.cover = 2;
			anySelectedButton = true;
		}

		if (dateUpdate.isSelected()) {
			imdbInfoUpdater.date = 1;
			anySelectedButton = true;
		}
		else if (dateUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.date = 2;
			anySelectedButton = true;
		}

		if (colourUpdate.isSelected()) {
			imdbInfoUpdater.colour = 1;
			anySelectedButton = true;
		}
		else if (colourUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.colour = 2;
			anySelectedButton = true;
		}
		if (directedByUpdate.isSelected()) {
			imdbInfoUpdater.directedBy = 1;
			anySelectedButton = true;
		}
		else if (directedByUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.directedBy = 2;
			anySelectedButton = true;
		}

		if (writtenByUpdate.isSelected()) {
			imdbInfoUpdater.writtenBy = 1;
			anySelectedButton = true;
		}
		else if (writtenByUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.writtenBy = 2;
			anySelectedButton = true;
		}

		if (genreUpdate.isSelected()) {
			imdbInfoUpdater.genre = 1;
			anySelectedButton = true;
		}
		else if (genreUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.genre = 2;
			anySelectedButton = true;
		}

		if (ratingUpdate.isSelected()) {
			imdbInfoUpdater.rating = 1;
			anySelectedButton = true;
		}
		else if (ratingUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.rating = 2;
			anySelectedButton = true;
		}

		if (countryUpdate.isSelected()) {
			imdbInfoUpdater.country = 1;
			anySelectedButton = true;
		}
		else if (countryUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.country = 2;
			anySelectedButton = true;
		}

		if (languageUpdate.isSelected()) {
			imdbInfoUpdater.language = 1;
			anySelectedButton = true;
		}
		else if (languageUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.language = 2;
			anySelectedButton = true;
		}

		if (plotUpdate.isSelected()) {
			imdbInfoUpdater.plot = 1;
			anySelectedButton = true;
		}
		else if (plotUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.plot = 2;
			anySelectedButton = true;
		}

		if (castUpdate.isSelected()) {
			imdbInfoUpdater.cast = 1;
			anySelectedButton = true;
		}
		else if (castUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.cast = 2;
			anySelectedButton = true;
		}

		if (akaUpdate.isSelected()) {
			imdbInfoUpdater.aka = 1;
			anySelectedButton = true;
		}
		else if (akaUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.aka = 2;
			anySelectedButton = true;
		}

		if (soundMixUpdate.isSelected()) {
			imdbInfoUpdater.soundMix = 1;
			anySelectedButton = true;
		}
		else if (soundMixUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.soundMix = 2;
			anySelectedButton = true;
		}

		if (runtimeUpdate.isSelected()) {
			imdbInfoUpdater.runtime = 1;
			anySelectedButton = true;
		}
		else if (runtimeUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.runtime = 2;
			anySelectedButton = true;
		}

		if (awardsUpdate.isSelected()) {
			imdbInfoUpdater.awards = 1;
			anySelectedButton = true;
		}
		else if (awardsUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.awards = 2;
			anySelectedButton = true;
		}

		if (mpaaUpdate.isSelected()) {
			imdbInfoUpdater.mpaa = 1;
			anySelectedButton = true;
		}
		else if (mpaaUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.mpaa = 2;
			anySelectedButton = true;
		}

		if (certificationUpdate.isSelected()) {
			imdbInfoUpdater.certification = 1;
			anySelectedButton = true;
		}
		else if (certificationUpdateIfEmpty.isSelected()) {
			imdbInfoUpdater.certification = 2;
			anySelectedButton = true;
		}

		// Setting thread count
		imdbInfoUpdater.setThreadCount(Integer.parseInt(threadCountField.getText()));
		
		if (anySelectedButton) {
		
			timer = new Timer(milliseconds, new TimerListener());
			processedCounter = 0;
			movieCounter = 0;
			
			imdbInfoUpdater.go();

			timer.start();
			conversionStart = System.currentTimeMillis();
			taskOutput.append("Update process started using "+ threadCountField.getText() +" threads\n");
			taskOutput.append("Processing movie list...\n");
		}
		else {
			DialogAlert alert = new DialogAlert(dialog, "Alert", "At least one of the fields should be checked.");
			GUIUtil.showAndWait(alert, true);

			startButton.setEnabled(true);
			cancelButton.setEnabled(false);
			closeButton.setEnabled(true);

			imdbInfoUpdater = new IMDbInfoUpdater();
		}
		
	}

	
	/**
	 * Called when the user presses the start button.
	 */
	public void actionPerformed(ActionEvent evt) {

		log.debug("ActionPerformed: "+ evt.getActionCommand());

		if (evt.getActionCommand().equals("Start")) {

			/*If the conversion was canceled it removes the listed movies to start fresh*/
			if (!taskOutput.getText().equals(""))
				taskOutput.setText("");

			startButton.setEnabled(false);
			cancelButton.setEnabled(true);
			closeButton.setEnabled(false);

			canceled = false;
			parent.setCanceled(false);
			parent.setDone(false);

			progressBar.setValue(0);
			progressBar.setString("");
			
			handleStartUpdate();
		}


		if (evt.getActionCommand().equals("Cancel")) {

			parent.setCanceled(true);
			canceled = true;
			cancelButton.setEnabled(false);
			startButton.setEnabled(true);
			closeButton.setEnabled(true);
			imdbInfoUpdater.stop();
		}

		if (evt.getActionCommand().equals("Close")) {
			parent.dispose();
			MovieManagerCommandSelect.execute();
		}
	}

	public static String millisecondsToString(long time) {

		int milliseconds = (int)(time % 1000);
		int seconds = (int)((time/1000) % 60);
		int minutes = (int)((time/60000) % 60);
		//int hours = (int)((time/3600000) % 24);
		String millisecondsStr = (milliseconds<10 ? "00" : (milliseconds<100 ? "0" : ""))+milliseconds;
		String secondsStr = (seconds<10 ? "0" : "")+seconds;
		String minutesStr = (minutes<10 ? "0" : "")+minutes;
		//String hoursStr = (hours<10 ? "0" : "")+hours;

		String finalString = "";

		if (!minutesStr.equals("00"))
			finalString += minutesStr+" min ";
		finalString += secondsStr+"."+millisecondsStr + " seconds.";

		return new String(finalString);
	}
	
	void setHotkeyModifiers() {
		
		try {			
						
			GUIUtil.enableDisposeOnEscapeKey(shortcutManager, "Close Window", new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					
				}
			});		
			
			shortcutManager.registerShowKeysKey();
			
			// ALT+S for Start
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Start", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					startButton.doClick();
				}
			}, startButton);
			
			
			// ALT+A for Abort
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyboardShortcutManager.getToolbarShortcutMask()), 
					"Abort", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					cancelButton.doClick();
				}
			}, cancelButton);
					
			
			// ALT+C for Close
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Close", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					closeButton.doClick();
				}
			}, closeButton);
					
			shortcutManager.setKeysToolTipComponent(taskOutput);
			
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
	}
}


