/**
 * @(#)DialogDatabaseImporter.java 1.0 26.09.06 (dd.mm.yy)
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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.commands.importexport.DatabaseImporterExporter;
import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImportExportHandler;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.log4j.Logger;


public class DialogDatabaseImporterExporter extends JDialog implements ActionListener {

	Logger log = Logger.getLogger(getClass());

	private JProgressBar progressBar;

	public final static int milliseconds = 1;
	private Timer timer;
	private JButton startButton;
	private JButton abortButton;
	private JButton closeButton;

	private DatabaseImporterExporter databaseImporter;
	private JTextArea taskOutput;
	private String newline = "\n"; //$NON-NLS-1$

	int movieCounter = 0;

	int lengthOfTask = 0;
	long conversionStart = 0;
	
	boolean aborted;
	
	public MovieManagerCommandImportExportHandler handler;
	
	public DialogDatabaseImporterExporter outer = this;
	
	ModelImportExportSettings importSettings;

	boolean done = false;
	
	public DialogDatabaseImporterExporter(JFrame parent, MovieManagerCommandImportExportHandler handler, 
			ModelImportExportSettings importSettings) {
		super(parent, Localizer.get("MovieManagerCommandImport.dialog-importer.title"), true); //$NON-NLS-1$
		setup(handler, importSettings);
	}		
	
	public DialogDatabaseImporterExporter(JDialog parent, MovieManagerCommandImportExportHandler handler, 
			ModelImportExportSettings importSettings) {
		super(parent, Localizer.get("MovieManagerCommandImport.dialog-importer.title"), true); //$NON-NLS-1$
		setup(handler, importSettings);
	}
	
	public DialogDatabaseImporterExporter(JDialog parent, 
			final MovieManagerCommandImportExportHandler handler, 
			ModelImportExportSettings importSettings, boolean startprocess) {
		
		this(parent, handler, importSettings);
		
		if (startprocess)
			startProcess();
	}
	
	void setup(final MovieManagerCommandImportExportHandler handler, ModelImportExportSettings importSettings) {
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		this.handler = handler;
		this.importSettings = importSettings;
		
		mainPanel.setOpaque(true);
		
		databaseImporter = new DatabaseImporterExporter(this, handler, importSettings);

		startButton = new JButton(Localizer.get("DialogDatabaseImporter.button.start.text")); //$NON-NLS-1$
		startButton.setActionCommand("Start"); //$NON-NLS-1$
		startButton.addActionListener(this);

		abortButton = new JButton(Localizer.get("DialogDatabaseImporter.button.abort.text")); //$NON-NLS-1$
		abortButton.setActionCommand("Cancel"); //$NON-NLS-1$
		abortButton.setEnabled(true);
		abortButton.addActionListener(this);

		closeButton = new JButton(Localizer.get("DialogDatabaseImporter.button.close.text")); //$NON-NLS-1$
		closeButton.setActionCommand("Close"); //$NON-NLS-1$
		closeButton.setEnabled(false);
		closeButton.addActionListener(this);

		progressBar = new JProgressBar();
		progressBar.setValue(0);
		progressBar.setString("                                 "); //$NON-NLS-1$
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(200, 25));

		taskOutput = new JTextArea(20, 50);
		taskOutput.setMargin(new Insets(5,5,5,5));
		taskOutput.setEditable(false);
		taskOutput.setCursor(null); 

		JPanel panelTop = new JPanel();
		panelTop.add(startButton);
		panelTop.add(abortButton);
		panelTop.add(closeButton);
		panelTop.add(progressBar);

		mainPanel.add(panelTop, BorderLayout.PAGE_START);
		mainPanel.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		
		setContentPane(mainPanel);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
								
				if (aborted || done) {
					dispose();
					MovieManagerCommandSelect.executeAndReload(-1);
				}
			}
		});

		GUIUtil.enableDisposeOnEscapeKey(this, new  AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				
				if (!aborted) {
					databaseImporter.stop();
				}
				else if (aborted || done) {
					dispose();
					MovieManagerCommandSelect.executeAndReload(-1);
				}
			}
		});
		

		pack();
		
		MovieManager mm = MovieManager.getIt();

		setLocation((int) mm.getLocation().getX()+(mm.getWidth()-getWidth())/2,
				(int) mm.getLocation().getY()+(mm.getHeight()-getHeight())/2);
	
		
		/*Create a timer*/
		timer = new Timer(milliseconds, new TimerListener());
	}
	
	

	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {

			if (lengthOfTask == 0) {
				lengthOfTask = databaseImporter.getLengthOfTask();

				if (lengthOfTask != 0) {
					progressBar.setMinimum(0);
					progressBar.setMaximum(lengthOfTask);
				}
			}

			while (databaseImporter.hasMoreTransferred()) {
				
				movieCounter++;
				int percent = ((movieCounter) * 100)/lengthOfTask;

				String msg = percent+ "%  (" + (movieCounter) + Localizer.get("DialogDatabaseImporter.message.out-of") + lengthOfTask+")     "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				progressBar.setValue(movieCounter);
				progressBar.setString(msg);
				taskOutput.append((movieCounter) + " - " + ((String) databaseImporter.getNextTransferred()) + newline); //$NON-NLS-1$
				taskOutput.setCaretPosition(taskOutput.getDocument().getLength());
			}

			if (databaseImporter.isDone() || aborted) {
				timer.stop();

				if (!aborted) {

					taskOutput.append(newline + movieCounter + Localizer.get("DialogDatabaseImporterExporter.message.entries-processed-in") + (millisecondsToString(System.currentTimeMillis() - conversionStart)) + newline); //$NON-NLS-1$
					closeButton.setEnabled(true);
					abortButton.setEnabled(false);
					setDone(true);
				}
				else {
					taskOutput.append(newline + Localizer.get("DialogDatabaseImporter.message.import-canceled") + newline); //$NON-NLS-1$
					setAborted(true);

					databaseImporter = new DatabaseImporterExporter(outer, outer.handler, importSettings);
					timer = new Timer(milliseconds, new TimerListener());

					movieCounter = 0;
				}
			}
		}
	}

	
	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public void dispose() {
		GUIUtil.show(this, false);
	}
	
	
	void startProcess() {
		/*If the conversion was canceled it removes the listed movies to start fresh*/
		taskOutput.setText(""); //$NON-NLS-1$

		startButton.setEnabled(false);
		abortButton.setEnabled(true);
		closeButton.setEnabled(false);

		aborted = false;
		setAborted(false);
		setDone(false);
		databaseImporter.go();

		timer.start();
		conversionStart = System.currentTimeMillis();
		taskOutput.append(Localizer.get("DialogDatabaseImporter.message.processing-import-list") + SysUtil.getLineSeparator()); //$NON-NLS-1$

	}
	
	/**
	 * Called when the user presses the start button.
	 */
	public void actionPerformed(ActionEvent evt) {

		log.debug("ActionPerformed: "+ evt.getActionCommand()); //$NON-NLS-1$

		if (evt.getActionCommand().equals("Start")) { //$NON-NLS-1$
			startProcess();
		}

		if (evt.getSource() == abortButton) { //$NON-NLS-1$
			setAborted(true);
			aborted = true;
			abortButton.setEnabled(false);
			startButton.setEnabled(true);
			closeButton.setEnabled(true);
			databaseImporter.stop();
		}

		if (evt.getActionCommand().equals("Close")) { //$NON-NLS-1$
			dispose();
			MovieManagerCommandSelect.executeAndReload(-1);
		}
	}

	public static String millisecondsToString(long time) {

		int milliseconds = (int)(time % 1000);
		int seconds = (int)((time/1000) % 60);
		int minutes = (int)((time/60000) % 60);
		
		String millisecondsStr = (milliseconds<10 ? "00" : (milliseconds<100 ? "0" : ""))+milliseconds; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String secondsStr = (seconds<10 ? "0" : "")+seconds; //$NON-NLS-1$ //$NON-NLS-2$
		String minutesStr = (minutes<10 ? "0" : "")+minutes; //$NON-NLS-1$ //$NON-NLS-2$
		
		String finalString = ""; //$NON-NLS-1$

		if (!minutesStr.equals("00")) //$NON-NLS-1$
			finalString += minutesStr + Localizer.get("DialogDatabaseImporter.message.minutes"); //$NON-NLS-1$
		finalString += secondsStr + "." + millisecondsStr + Localizer.get("DialogDatabaseImporter.message.seconds"); //$NON-NLS-1$ //$NON-NLS-2$

		return new String(finalString);
	}
}


