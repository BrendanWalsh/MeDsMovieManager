/**
 * @(#)DialogDatabase.java 1.0 26.09.06 (dd.mm.yy)
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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.database.DatabaseAccess;
import net.sf.xmm.moviemanager.database.DatabaseHSQL;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.swing.progressbar.ProgressBean;
import net.sf.xmm.moviemanager.swing.progressbar.ProgressBeanImpl;
import net.sf.xmm.moviemanager.swing.progressbar.SimpleProgressBar;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.swing.util.SwingWorker;
import net.sf.xmm.moviemanager.util.CustomFileFilter;
import net.sf.xmm.moviemanager.util.DocumentRegExp;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.log4j.Logger;

public class DialogDatabase extends JDialog implements ActionListener {

	static Logger log = Logger.getLogger(DialogDatabase.class);

	static private JTextField hsqlFilePath;
	static private JTextField accessFilePath;

	private JButton browseForHSQLFile;
	private JButton browseForAccessFile;

	private JButton buttonConfirm;
	private JButton buttonCancel;

	static private boolean newDatabase = false;

	KeyboardShortcutManager shortcutManager = new KeyboardShortcutManager(this);
	
	/* MySQL */

	static private JTextField databaseNameField;
	static private JTextField hostTextField;
	static private JTextField portTextField;
	static private JTextField userNameTextField;
	static private JTextField passwordTextField;


	static protected JTabbedPane tabbedPane;

	protected JPanel all;

	static SimpleProgressBar progressBar;


	static private DialogDatabase dialogDatabase;

	public DialogDatabase(boolean mode) {
		/* Dialog creation...*/
		super(MovieManager.getDialog());

		dialogDatabase = this;

		newDatabase = mode;

		GUIUtil.enableDisposeOnEscapeKey(shortcutManager);

		if (newDatabase)
			setTitle(Localizer.get("DialogDatabase.title.new-database")); //$NON-NLS-1$
		else
			setTitle(Localizer.get("DialogDatabase.title.open-database")); //$NON-NLS-1$

		setModal(true);
		setResizable(false);

		JList recentDatabases = new JList();

		JScrollPane dbList = new JScrollPane(recentDatabases);

		JPanel recentDBPanel = new JPanel();

		recentDBPanel.add(dbList);


		/* HSQL database */

		/* Label */
		JLabel hsqlLabel;

		if (newDatabase)
			hsqlLabel = new JLabel(Localizer.get("DialogDatabase.create-new-hsql-database")); //$NON-NLS-1$
		else
			hsqlLabel = new JLabel(Localizer.get("DialogDatabase.create-existing-hsql-database")); //$NON-NLS-1$

		JPanel hsqlLabelPanel = new JPanel();
		hsqlLabelPanel.add(hsqlLabel);

		/* HSQL file path */
		hsqlFilePath = new JTextField(27);
		hsqlFilePath.setText(""); //$NON-NLS-1$

		browseForHSQLFile = new JButton(Localizer.get("DialogDatabase.browse-text")); //$NON-NLS-1$
		browseForHSQLFile.setToolTipText(Localizer.get("DialogDatabase.browse-hsql-tooltip")); //$NON-NLS-1$
		browseForHSQLFile.setActionCommand("Browse HSQL"); //$NON-NLS-1$
		browseForHSQLFile.addActionListener(this);

		JPanel hsqlPathPanel = new JPanel();
		hsqlPathPanel.setLayout(new FlowLayout());
		hsqlPathPanel.add(hsqlFilePath);
		hsqlPathPanel.add(browseForHSQLFile);

		hsqlPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogDatabase.hsql-database-path")), BorderFactory.createEmptyBorder(0,5,0,5)))); //$NON-NLS-1$

		JPanel hsqlPanel = new JPanel(new BorderLayout());
		hsqlPanel.add(hsqlLabelPanel, BorderLayout.NORTH);
		hsqlPanel.add(hsqlPathPanel, BorderLayout.SOUTH);


		/* MySQL Database */
		JLabel mysqlLabel;

		if (newDatabase)
			mysqlLabel = new JLabel(Localizer.get("DialogDatabase.create-new-mysql-database")); //$NON-NLS-1$
		else
			mysqlLabel = new JLabel(Localizer.get("DialogDatabase.create-existing-mysql-database")); //$NON-NLS-1$

		JPanel mysqlLabelPanel = new JPanel();
		mysqlLabelPanel.add(mysqlLabel);


		JLabel databaseNameLabel = new JLabel(Localizer.get("DialogDatabase.mysql.schema-name") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		databaseNameField = new JTextField(10);
		
		// If creating new database, limit the allowed characters
		if (newDatabase)
			databaseNameField.setDocument(new DocumentRegExp("[a-z][a-z0-9\\_]*")); //$NON-NLS-1$
		
		databaseNameField.setText(""); //$NON-NLS-1$

		JPanel databaseNamePanel = new JPanel();
		databaseNamePanel.add(databaseNameLabel);
		databaseNamePanel.add(databaseNameField);

		JLabel hostLabel = new JLabel(Localizer.get("DialogDatabase.mysql.host-address") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		hostTextField = new JTextField(15);
		hostTextField.setText(""); //$NON-NLS-1$

		if (MovieManager.isApplet()) {
			URL base = DialogMovieManager.applet.getCodeBase();
			hostTextField.setText(base.getHost());
			hostTextField.setEditable(false);
		}

		JPanel hostPanel = new JPanel();
		hostPanel.add(hostLabel);
		hostPanel.add(hostTextField);

		JLabel portLabel = new JLabel(Localizer.get("DialogDatabase.mysql.port") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		portTextField = new JTextField(4);

		portTextField.setDocument(new DocumentRegExp("(\\d)*",8)); //$NON-NLS-1$
		portTextField.setText("3306"); //$NON-NLS-1$

		JPanel portPanel = new JPanel();
		portPanel.add(portLabel);
		portPanel.add(portTextField);

		JPanel mysqlServerPanel = new JPanel(new GridBagLayout());

		GridBagConstraints constraints;

		/* database name */
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(1,5,1,5);
		mysqlServerPanel.add(databaseNameLabel,constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(1,5,1,5);
		mysqlServerPanel.add(databaseNameField,constraints);


		/* host */
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(1,5,1,5);
		mysqlServerPanel.add(hostLabel,constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(1,5,1,5);
		mysqlServerPanel.add(hostTextField,constraints);

		/* port */
		constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(1,1,1,1);
		constraints.anchor = GridBagConstraints.EAST;
		mysqlServerPanel.add(portPanel,constraints);

		JLabel userNameLabel = new JLabel(Localizer.get("DialogDatabase.mysql.username") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		userNameTextField = new JTextField(7);
		userNameTextField.setText(""); //$NON-NLS-1$

		JPanel userNamePanel = new JPanel();
		userNamePanel.add(userNameLabel);
		userNamePanel.add(userNameTextField);

		JLabel passwordLabel = new JLabel(Localizer.get("DialogDatabase.mysql.password.text") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		passwordTextField = new JPasswordField(7);
		passwordTextField.setText(""); //$NON-NLS-1$
		passwordTextField.setToolTipText(Localizer.get("DialogDatabase.mysql.password.tooltip")); //$NON-NLS-1$

		JPanel passwordPanel = new JPanel() ;
		passwordPanel.add(passwordLabel);
		passwordPanel.add(passwordTextField);

		JPanel authenticationPanel = new JPanel(new GridBagLayout());
		authenticationPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogDatabase.mysql.authentication")), BorderFactory.createEmptyBorder(0,5,5,5))); //$NON-NLS-1$


		/* username */
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.LAST_LINE_START;
		authenticationPanel.add(userNamePanel, constraints);

		/* password */
		constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.LAST_LINE_END;
		authenticationPanel.add(passwordPanel,constraints);

		
		if (MovieManager.getIt().getDatabase() != null && MovieManager.getIt().getDatabase().isMySQL()) {
			String path = MovieManager.getConfig().getDatabasePath(false);
		}

		JPanel mysqlOptionPanel = new JPanel(new GridLayout(0, 1));
		mysqlOptionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogDatabase.mysql.settings")), BorderFactory.createEmptyBorder(0,5,5,5))); //$NON-NLS-1$

		mysqlOptionPanel.add(mysqlServerPanel);
		mysqlOptionPanel.add(authenticationPanel);

		JPanel mysqlPanel = new JPanel(new BorderLayout());
		mysqlPanel.add(mysqlLabelPanel, BorderLayout.NORTH);
		mysqlPanel.add(mysqlOptionPanel, BorderLayout.CENTER);


		/* MS Access Database */

		JLabel accessLabel;

		if (newDatabase)
			accessLabel = new JLabel(Localizer.get("DialogDatabase.create-new-msaccess-database")); //$NON-NLS-1$
		else
			accessLabel = new JLabel(Localizer.get("DialogDatabase.create-existing-msaccess-database")); //$NON-NLS-1$

		JPanel accessLabelPanel = new JPanel();
		accessLabelPanel.add(accessLabel);

		/* ms Access database */
		accessFilePath = new JTextField(27);
		accessFilePath.setText(""); //$NON-NLS-1$

		browseForAccessFile = new JButton(Localizer.get("DialogDatabase.browse-text")); //$NON-NLS-1$
		browseForAccessFile.setToolTipText(Localizer.get("DialogDatabase.browse-msaccess-tooltip")); //$NON-NLS-1$
		browseForAccessFile.setActionCommand("Browse MS Access"); //$NON-NLS-1$
		browseForAccessFile.addActionListener(this);

		JPanel accessPathPanel = new JPanel();
		accessPathPanel.setLayout(new FlowLayout());
		accessPathPanel.add(accessFilePath);
		accessPathPanel.add(browseForAccessFile);

		accessPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogDatabase.msaccess-database-path")), BorderFactory.createEmptyBorder(0,0,0,0)))); //$NON-NLS-1$

		JPanel accessPanel = new JPanel(new BorderLayout());
		accessPanel.add(accessLabelPanel, BorderLayout.NORTH);
		accessPanel.add(accessPathPanel, BorderLayout.SOUTH);


		/* Tabbed pane */
		tabbedPane = new JTabbedPane();

		tabbedPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		tabbedPane.add(Localizer.get("DialogDatabase.tabbed-pane.hsql-database"), hsqlPanel); //$NON-NLS-1$

		if (SysUtil.isWindows())
			tabbedPane.add(Localizer.get("DialogDatabase.tabbed-pane.msaccess-database"), accessPanel); //$NON-NLS-1$

		tabbedPane.add(Localizer.get("DialogDatabase.tabbed-pane.mysql-database"), mysqlPanel); //$NON-NLS-1$

		// Disable MS Access and HSQL when running in applet mode
		if (MovieManager.isApplet()) {
			tabbedPane.setEnabledAt(0, false);
			
			if (SysUtil.isWindows()) {
				tabbedPane.setEnabledAt(1, false);
				tabbedPane.setSelectedIndex(2);
			}
			else
				tabbedPane.setSelectedIndex(1);
		}

		/* Buttons */
		if (newDatabase)
			buttonConfirm = new JButton(Localizer.get("DialogDatabase.button-confirm-text.create-database")); //$NON-NLS-1$
		else
			buttonConfirm = new JButton(Localizer.get("DialogDatabase.button-confirm-text.open-database")); //$NON-NLS-1$

		buttonConfirm.setToolTipText(Localizer.get("DialogDatabase.button-confirm-tooltip")); //$NON-NLS-1$
		buttonConfirm.setActionCommand("DialogAddMultipleMovies - OK"); //$NON-NLS-1$
		buttonConfirm.addActionListener(this);

		buttonCancel = new JButton(Localizer.get("DialogDatabase.button-cancel-text")); //$NON-NLS-1$
		buttonCancel.setToolTipText(Localizer.get("DialogDatabase.button-cancel-tooltip")); //$NON-NLS-1$
		buttonCancel.setActionCommand("DialogAddMultipleMovies - Cancel"); //$NON-NLS-1$
		buttonCancel.addActionListener(this);


		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(buttonConfirm);
		buttonPanel.add(buttonCancel);

		all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));

		all.add(tabbedPane);
		all.add(buttonPanel);

		getContentPane().add(all,BorderLayout.NORTH);
		/* Packs and sets location... */
		pack();

		setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
				(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
	}


	/* Opens a filechooser and returns the absolute path to the selected file */
	private String executeCommandGetFile(int databaseMode, File currentDir) {

		/* Opens the Open dialog... */
		ExtendedFileChooser fileChooser = new ExtendedFileChooser(currentDir);
		try {
			fileChooser.setFileSelectionMode(ExtendedFileChooser.FILES_ONLY);

			if (databaseMode == 0) {
				fileChooser.setFileFilter(new CustomFileFilter(new String[]{"properties", "script", "lck"},new String("HSQL Database Files (*.properties, *.script, *.lck)"), "HSQL")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			} 
			else if (databaseMode == 1) {
				fileChooser.setFileFilter(new CustomFileFilter(new String[]{"mdb", "accdb"},new String("MS Access Database Files (*.mdb, *.accdb)"), "MSAccess")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}

			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.requestFocusInWindow();
			fileChooser.setFocusTraversalKeysEnabled(false);

			int returnVal;

			if (newDatabase) {
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setFileAlreadyExistWarningMessage("Database "); //$NON-NLS-1$

				fileChooser.setApproveButtonText(Localizer.get("DialogDatabase.filechooser.approve-button-create-database.text")); //$NON-NLS-1$
				fileChooser.setApproveButtonToolTipText(Localizer.get("DialogDatabase.filechooser.approve-button-create-database.tooltip")); //$NON-NLS-1$
				fileChooser.setDialogTitle(Localizer.get("DialogDatabase.filechooser.title.create-database")); //$NON-NLS-1$

				returnVal = fileChooser.showSaveDialog(MovieManager.getDialog());
			}
			else {
				fileChooser.setApproveButtonText(Localizer.get("DialogDatabase.filechooser.approve-button-open-database.text")); //$NON-NLS-1$
				fileChooser.setApproveButtonToolTipText(Localizer.get("DialogDatabase.filechooser.approve-button-open-database.tooltip")); //$NON-NLS-1$
				fileChooser.setDialogTitle(Localizer.get("DialogDatabase.filechooser.title.open-database")); //$NON-NLS-1$

				returnVal = fileChooser.showOpenDialog(MovieManager.getDialog());
			}

			if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {

				/* Gets the path... */
				String filepath = fileChooser.getSelectedFile().getAbsolutePath();

				if (!newDatabase && !(new File(filepath).exists()))
					return ""; //$NON-NLS-1$
				else {
					if (((CustomFileFilter) fileChooser.getFileFilter()).getIdentifier().equals("MSAccess")) { //$NON-NLS-1$
						if (!filepath.endsWith(".mdb") && !filepath.endsWith(".accdb")) //$NON-NLS-1$ //$NON-NLS-2$
							filepath += ".mdb"; //$NON-NLS-1$
					}
				}

				return filepath;
			}
		}
		catch (Exception e) {
			log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
		}

		return ""; //$NON-NLS-1$
	}

	/*Saves the options to the MovieManager object*/
	void executeSave() {
		;
	}

	protected int getImportMode() {
		return tabbedPane.getSelectedIndex();
	}


	/* Returns the path for each database type */
	static protected String getPath() {

		String type = getType();

		if (type.equals("MSAccess")) //$NON-NLS-1$
			return accessFilePath.getText();
		else if (type.equals("HSQL")) //$NON-NLS-1$
			return hsqlFilePath.getText();
		else if (type.equals("MySQL")) //$NON-NLS-1$
			return createMySQLPath(""); //$NON-NLS-1$

		return ""; //$NON-NLS-1$
	}


	/*Returns the string in the path textfield*/
	static protected String getType() {

		String type = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());

		if (type.equals(Localizer.get("DialogDatabase.tabbed-pane.msaccess-database"))) //$NON-NLS-1$
			return "MSAccess"; //$NON-NLS-1$
		else if (type.equals(Localizer.get("DialogDatabase.tabbed-pane.hsql-database"))) //$NON-NLS-1$
			return "HSQL"; //$NON-NLS-1$
		else if (type.equals(Localizer.get("DialogDatabase.tabbed-pane.mysql-database"))) //$NON-NLS-1$
			return "MySQL"; //$NON-NLS-1$

		return "";  //$NON-NLS-1$
	}


	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$

		if (event.getSource().equals(browseForHSQLFile)) {
			log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$

			String ret = executeCommandGetFile(0, MovieManager.getConfig().getLastDatabaseDir());
			if (!ret.equals("")) { //$NON-NLS-1$
				hsqlFilePath.setText(ret);
				executeConfirm();
			}
		}

		if (event.getSource().equals(browseForAccessFile)) {
			log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
			String ret = executeCommandGetFile(1, MovieManager.getConfig().getLastDatabaseDir());

			if (!ret.equals("")) { //$NON-NLS-1$
				accessFilePath.setText(ret);
				executeConfirm();
			}
		}

		if (event.getSource().equals(buttonConfirm)) {
			log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$

			executeConfirm();
		}

		if (event.getSource().equals(buttonCancel)) {
			log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
			dispose();
		}
	}

	protected void executeConfirm() {

		String databaseType = getType();

		if (databaseType.equals("MySQL")) { //$NON-NLS-1$

			if (databaseNameField.getText().equals("")) { //$NON-NLS-1$
				DialogAlert alert = new DialogAlert(this, Localizer.get("DialogDatabase.alert.database-name.title"), Localizer.get("DialogDatabase.alert.database-name.message")); //$NON-NLS-1$ //$NON-NLS-2$
				GUIUtil.show(alert, true);
				return;
			}
			if (hostTextField.getText().equals("")) { //$NON-NLS-1$
				DialogAlert alert = new DialogAlert(this, Localizer.get("DialogDatabase.alert.host-address.title"), Localizer.get("DialogDatabase.alert.host-address.message")); //$NON-NLS-1$ //$NON-NLS-2$
				GUIUtil.show(alert, true);
				return;
			}

			if (portTextField.getText().equals("")) { //$NON-NLS-1$
				DialogAlert alert = new DialogAlert(this, Localizer.get("DialogDatabase.alert.port.title"), Localizer.get("DialogDatabase.alert.port.message")); //$NON-NLS-1$ //$NON-NLS-2$
				GUIUtil.show(alert, true);
				return;
			}
		}
		else if (databaseType.equals("HSQL")) { //$NON-NLS-1$

			if (hsqlFilePath.getText().equals("")) { //$NON-NLS-1$
				DialogAlert alert = new DialogAlert(this, Localizer.get("DialogDatabase.alert.database-path.title"), Localizer.get("DialogDatabase.alert.database-path.message")); //$NON-NLS-1$ //$NON-NLS-2$
				GUIUtil.show(alert, true);
				return;
			}
		}
		else {
			if (accessFilePath.getText().equals("")) { //$NON-NLS-1$
				DialogAlert alert = new DialogAlert(this, Localizer.get("DialogDatabase.alert.database-path.title"), Localizer.get("DialogDatabase.alert.database-path.message")); //$NON-NLS-1$ //$NON-NLS-2$
				GUIUtil.show(alert, true);
				return;
			}
		}

		executeSave();

		final ProgressBean worker = new ProgressBeanImpl() {

			public void run() {

				try {
					Thread.currentThread().setPriority(3);
					Database database = connectToDatabase(dialogDatabase, null);

					if (database != null) {

						/* Loads the database... */
						updateProgress(progressBar, Localizer.get("DialogDatabase.progress.connecting-to-database")); //$NON-NLS-1$
						MovieManager.getDatabaseHandler().setDatabase(database, this, true);

						if (database.isSetUp()) {
							GUIUtil.invokeLater(new Runnable() {public void run() {
								listener.propertyChange(new PropertyChangeEvent(this, "value", null, null)); //$NON-NLS-1$
							}});
							dispose();
						}

						/* Sets the last path... */
						if ((!MovieManager.isApplet()) && new File(getPath()).exists())
							MovieManager.getConfig().setLastDatabaseDir(new File(getPath()).getParentFile());
					}
				}
				catch (Exception e) {
					log.error("Exception:" + e.getMessage(), e); //$NON-NLS-1$
					GUIUtil.invokeLater(new Runnable() {public void run() {
						listener.propertyChange(new PropertyChangeEvent(this, "value", null, null)); //$NON-NLS-1$
					}});
				}
				GUIUtil.invokeLater(new Runnable() {public void run() {
					listener.propertyChange(new PropertyChangeEvent(this, "value", null, null)); //$NON-NLS-1$
				}});
			}
		};

		SimpleProgressBar progressBar = new SimpleProgressBar(MovieManager.getDialog(), Localizer.get("DialogDatabase.progress.loading-database"), true, worker); //$NON-NLS-1$
		DialogDatabase.progressBar = progressBar;
		GUIUtil.show(progressBar, true);        


		final SwingWorker swingWorker = new SwingWorker() {
			public Object construct() {
				worker.start();
				return worker;
			}
		};
		swingWorker.start();

	}


	
	
    static synchronized void updateProgress(final SimpleProgressBar progressBar, final String str) {

    	Runnable updateProgress = new Runnable() {
    		public void run() {
    			try {
    				progressBar.setString(str);

    			} catch (Exception e) {
    				//log.error(e.getMessage());
    			}
    		}};
    		SwingUtilities.invokeLater(updateProgress);
    }
    	

    static protected Database connectToDatabase(Window parent, String path) throws Exception {

    	if (getType().equals("MySQL")) //$NON-NLS-1$
    		updateProgress(progressBar, Localizer.get("DialogDatabase.progress.connecting-to-database")); //$NON-NLS-1$
    	else
    		updateProgress(progressBar, Localizer.get("DialogDatabase.progress.creating-database-connection")); //$NON-NLS-1$

    	String databaseType = getType();
    	Database database = null;

    	if (path == null || "".equals(path)) //$NON-NLS-1$
    		path = getPath();

    	/* New Database */
    	if (newDatabase) {
    		database = createNewDatabase(databaseType);
    	}
    	else {
    		/* open Database */
    		if (databaseType.equals("MySQL")) { //$NON-NLS-1$
    			database = new DatabaseMySQL(path, MovieManager.getConfig().getMySQLSocketTimeoutEnabled());
    		}
    		else if (!new File(path).exists()) {
    			throw new Exception("File does not exist"); //$NON-NLS-1$
    		}
    		else if (databaseType.equals("MSAccess")) { //$NON-NLS-1$
    			if (!path.endsWith(".mdb")) //$NON-NLS-1$
    				path += ".mdb"; //$NON-NLS-1$

    			database = new DatabaseAccess(path);
    		}
    		else if (databaseType.equals("HSQL")) { //$NON-NLS-1$

    			if (path.endsWith(".properties") || path.endsWith(".script") || path.endsWith(".lck")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    				path = path.substring(0, path.lastIndexOf(".")); //$NON-NLS-1$

    			try {
    				database = new DatabaseHSQL(path);

    			} catch (Exception e) {
    				log.error("", e); //$NON-NLS-1$
    				return null;
    			}
    		}
    	}
    	return database;
    }

    /**
     * Creates a new database and loads it...
     **/
    static protected Database createNewDatabase(String databaseType) throws Exception {
	
    	Database database = null;

    	String path = getPath();
    	String parentPath = path.substring(0, path.lastIndexOf(File.separator) +1);
    	
    	if (!databaseType.equals("MySQL")) { //$NON-NLS-1$
    		
    		File testFile = new File(parentPath, "MMM.test");
        	
        	try {
    			testFile.createNewFile();
    			testFile.delete();
    		} catch (Exception e) {
    			log.error("Exception:" + e.getMessage(), e);
    			showDatabaseMessage(dialogDatabase, database, "Failed to create new database"); //$NON-NLS-1$
				return null;
    		}
        	
    		/* Creates the covers folder... */
    		File coversDir = new File(parentPath + "Covers"); //$NON-NLS-1$
    		if (!coversDir.exists() && !coversDir.mkdir()) {
    			throw new Exception("Cannot create the covers directory."); //$NON-NLS-1$
    		}
    		/* Creates the queries folder... */
    		File queriesDir = new File(parentPath + "Queries"); //$NON-NLS-1$
    		if (!queriesDir.exists() && !queriesDir.mkdir()) {
    			throw new Exception("Cannot create the queries directory."); //$NON-NLS-1$
    		}
    	}

    	/* Creates a new HSQL database... */
    	if (databaseType.equals("HSQL")) { //$NON-NLS-1$
    		database = new DatabaseHSQL(path);
    		database.setUp();

    		if (database.isSetUp()) {
    			updateProgress(progressBar, Localizer.get("DialogDatabase.progress.creating-database")); //$NON-NLS-1$
    			((DatabaseHSQL)database).createDatabaseTables();
    		}	
    	}

    	/* Creates a new MySQL database... */
    	else if (databaseType.equals("MySQL")) { //$NON-NLS-1$

    		boolean success = true;

    		database = new DatabaseMySQL(path, MovieManager.getConfig().getMySQLSocketTimeoutEnabled());
    		database.setUp();

    		/* Could not connect to the database */
    		if (!database.isSetUp()) {

    			success = false;

    			String newDatabaseMsg = database.getErrorMessage();
    			String message = newDatabaseMsg;

    			log.debug("Datbase error message:" + message); //$NON-NLS-1$
    			
    			if (message.indexOf("Connection refused") != -1) { //$NON-NLS-1$
    				log.debug("Connection refused"); //$NON-NLS-1$
    				progressBar.close();
    				showDatabaseMessage(dialogDatabase, database, "Connection refused"); //$NON-NLS-1$
    				return null;
    			}
    			else if (message.indexOf("Connection timed out") != -1) { //$NON-NLS-1$
    				log.debug("Connection timed out"); //$NON-NLS-1$
    				progressBar.close();
    				showDatabaseMessage(dialogDatabase, database, "Connection timed out"); //$NON-NLS-1$
    				return null;
    			}

    			if (message.indexOf("denied") != -1) { //$NON-NLS-1$
    				log.debug("Denied:" + message); //$NON-NLS-1$
    			}

    			/* If the database doesn't already exists, a connection must be made through a default database,
		   tries "mysql" and "information_schema" */
    			if ((message.indexOf("denied") != -1) || (message.indexOf("Unknown") != -1)) { //$NON-NLS-1$ //$NON-NLS-2$

    				/* Default mysql database */
    				database = new DatabaseMySQL(createMySQLPath("mysql"), MovieManager.getConfig().getMySQLSocketTimeoutEnabled()); //$NON-NLS-1$
    				database.setUp();

    				if (!database.isSetUp()) {
    					/* Default mysql database */
    					database = new DatabaseMySQL(createMySQLPath("information_schema"), MovieManager.getConfig().getMySQLSocketTimeoutEnabled()); //$NON-NLS-1$
    					database.setUp();
    				}

    				if (!database.isSetUp()) {

    					log.debug("Failed to connect to database:" + database.getErrorMessage()); //$NON-NLS-1$
    					progressBar.close();

    					if (database.getErrorMessage() != null && database.getErrorMessage().indexOf("information_schema") != -1) { //$NON-NLS-1$
    						showDatabaseMessage(dialogDatabase, database, "Unknown database"); //$NON-NLS-1$
    					}
    					else
    						showDatabaseMessage(dialogDatabase, database, database.getErrorMessage());

    					return null;
    				}
    				else
    					success = true;
    			}

    			/* If a connection is successfully established - creating database*/

    			if (success && database.isSetUp()) {

    				log.debug("Creating database"); //$NON-NLS-1$

    				/* Creating the database */
    				if (((DatabaseMySQL) database).createDatabase(databaseNameField.getText()) == 1) {
    					database.finalizeDatabase();
    					database = new DatabaseMySQL(getPath(), MovieManager.getConfig().getMySQLSocketTimeoutEnabled());
    					database.setUp();
    				}
    				else {
    					success = false;

    					progressBar.close();
    					showDatabaseMessage(dialogDatabase, database, "create database denied"); //$NON-NLS-1$
    				}
    			}
    		}

    		/* A connection to the given database is now established */
    		if (success) {
    			log.debug("Creating database tables"); //$NON-NLS-1$

    			if (((DatabaseMySQL) database).createDatabaseTables() == -1) {
    				success = false;

    				progressBar.close();
    				showDatabaseMessage(dialogDatabase, database, null);
    				return null;
    			}
    		}
    		else {
    			log.warn("Failed to connect to database"); //$NON-NLS-1$
    			progressBar.close();
    			showDatabaseMessage(dialogDatabase, database, "Failed to connect to database"); //$NON-NLS-1$
    			return null;
    		}
    	}
    	else {

    		log.debug("Creates the MS Access database file"); //$NON-NLS-1$

    		/* Creates the MS Access database file... */
    		File databaseFile = new File(path);
    		
    		
    		try {
    			if (!databaseFile.createNewFile()) {
    				showDatabaseMessage(dialogDatabase, database, "Failed to create database file"); //$NON-NLS-1$
    				throw new Exception("Cannot create database file."); //$NON-NLS-1$
    			}
    		} catch (Exception e) {
    			showDatabaseMessage(dialogDatabase, database, "Failed to create database file"); //$NON-NLS-1$
				return null;
			}
    		
    		/* Copies the empty database file in the package to the new file... */
    		byte[] data;
    		InputStream inputStream;
    		OutputStream outputStream;

    		inputStream = new FileInputStream(FileUtil.getFile("config/Temp.mdb")); //$NON-NLS-1$

    		outputStream = new FileOutputStream(databaseFile);
    		data = new byte[inputStream.available()];
    		while (inputStream.read(data) != -1) {
    			outputStream.write(data);
    		}

    		outputStream.close();
    		inputStream.close();

    		database = new DatabaseAccess(path);
    		database.setUp();
    	}	

    	/* Sets the folders path in the database... */
    	if (database.setFolders(parentPath + "Covers", parentPath + "Queries") != 1) { //$NON-NLS-1$ //$NON-NLS-2$
    		throw new Exception("Could not set the covers and queries paths in the database."); //$NON-NLS-1$
    	}

    	/* Closes the open database if any... */
    	if (MovieManager.getIt().getDatabase() != null) {
    		MovieManager.getIt().getDatabase().finalizeDatabase();
    	}

    	return database;
    }

    static String createMySQLPath(String optionalDatabase) {

    	String mysqlPath = hostTextField.getText();

    	if (!portTextField.getText().equals("")) //$NON-NLS-1$
    		mysqlPath += ":" + portTextField.getText(); //$NON-NLS-1$

    	if (!optionalDatabase.equals("")) //$NON-NLS-1$
    		mysqlPath += "/" + optionalDatabase + "?"; //$NON-NLS-1$ //$NON-NLS-2$
    	else
    		mysqlPath += "/" + databaseNameField.getText() + "?"; //$NON-NLS-1$ //$NON-NLS-2$

    	if (!userNameTextField.getText().equals("")) { //$NON-NLS-1$
    		mysqlPath += "user=" + userNameTextField.getText(); //$NON-NLS-1$

    		if (!passwordTextField.getText().equals("")) //$NON-NLS-1$
    			mysqlPath += "&password=" + passwordTextField.getText(); //$NON-NLS-1$
    	}

    	return mysqlPath;
    }

    
    public static boolean showDatabaseMessage(final Window parent, Database _database, String msg) {
    	
    	try {

    		String title = ""; //$NON-NLS-1$

    		String message = "";
    		Exception exception = null;
    		
    		if(_database != null) {
    			message = _database.getErrorMessage();
    			exception = _database.getException();
        		_database.resetError();
    		}

    		if (msg != null && !msg.equals("")) //$NON-NLS-1$
    			message = msg;

    		if (message == null) {
    			message = ""; //$NON-NLS-1$
    		}
    		
    		if (message.indexOf("Failed to connect to database") != -1 && _database.isMySQL()) { //$NON-NLS-1$
    			message = Localizer.get("DialogDatabase.mysql.message.failed-to-connect"); //$NON-NLS-1$

    			if (_database.getException() != null)
    				message += SysUtil.getLineSeparator() + _database.getException().getMessage();

    			title = Localizer.get("DialogDatabase.mysql.title.connection-alert"); //$NON-NLS-1$
    		}
    		else if (message.indexOf("Connection refused") != -1 && _database.isMySQL()) { //$NON-NLS-1$
    			message = Localizer.get("DialogDatabase.mysql.message.could-not-connect-connection-refused"); //$NON-NLS-1$
    			title = Localizer.get("DialogDatabase.mysql.title.connection-alert"); //$NON-NLS-1$
    		}
    		else if (message.indexOf("Failed to create database file") != -1) { //$NON-NLS-1$
    			message = "Failed to create database file";
    			title = "Database creation failed";
    		}
    		else if (message.indexOf("access denied (java.net.SocketPermission") != -1 && _database.isMySQL() && MovieManager.isApplet()) { //$NON-NLS-1$
    			message = Localizer.get("DialogDatabase.applet.message.jar-must-be-signed"); //$NON-NLS-1$
    			title = Localizer.get("DialogDatabase.applet.title.code-execution-error"); //$NON-NLS-1$
    		}
    		else if (message.indexOf("java.net.UnknownHostException") != -1 && _database.isMySQL()) { //$NON-NLS-1$
    			message = Localizer.get("DialogDatabase.mysql.message.unknow-host"); //$NON-NLS-1$
    			title = Localizer.get("DialogDatabase.mysql.title.connection-alert"); //$NON-NLS-1$
    		}
    		else if (message.indexOf("Network is unreachable") != -1 && _database.isMySQL()) { //$NON-NLS-1$
    			message = "Network is unreachable"; //$NON-NLS-1$
    			title = Localizer.get("DialogDatabase.mysql.title.connection-alert"); //$NON-NLS-1$
    		}
    		else if (message.indexOf("Connection timed out") != -1 || message.indexOf("Connection refused: connect") != -1 && _database.isMySQL()) { //$NON-NLS-1$ //$NON-NLS-2$
    			message = "<html> Connection timed out...<br>Make sure you have network access and that the IP and port is correct.</html>"; //$NON-NLS-1$
    			title = Localizer.get("DialogDatabase.mysql.title.connection-alert"); //$NON-NLS-1$
    		}
    		else if (message.indexOf("CREATE command denied") != -1) { //$NON-NLS-1$
    			String tmp = Localizer.get("DialogDatabase.mysql.message.create-tables-denied"); //$NON-NLS-1$
    			message = "<html>"+ message + "<br>"+ tmp +"</html>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    			title = Localizer.get("DialogDatabase.mysql.title.database-creation.failed"); //$NON-NLS-1$
    		}
    		else if (message.indexOf("Access denied") != -1 && _database.isMySQL()) { //$NON-NLS-1$
    			message = "<html>"+ message + "<br>Access was denied, permission not sufficient.</html>"; //$NON-NLS-1$ //$NON-NLS-2$
    			title = Localizer.get("DialogDatabase.mysql.title.access-denied"); //$NON-NLS-1$
    		}
    		else if (message.indexOf("Authentication failed") != -1 && _database.isMySQL()) { //$NON-NLS-1$
    			message = "<html>"+ message + "<br>The authentication process failed.</html>"; //$NON-NLS-1$ //$NON-NLS-2$
    			title = Localizer.get("DialogDatabase.mysql.title.authentication-failed"); //$NON-NLS-1$
    		}
    		else if (message.indexOf("Unknown database") != -1 && _database.isMySQL()) { //$NON-NLS-1$
    			message = "<html> Either the user doesn't have sufficient privileges <br> or the given Schema doesn't exist.</html>"; //$NON-NLS-1$
    			title = Localizer.get("DialogDatabase.mysql.title.connection-attempt-failed"); //$NON-NLS-1$
    		}
    		else if (message.equals("org.hsqldb.jdbcDriver")) { //$NON-NLS-1$
    			title = Localizer.get("DialogDatabase.mysql.title.failed-to-load-hsql-driver"); //$NON-NLS-1$
    			message = "<html> The HSQL database driver should be placed in the \"lib/drivers\" directory.</html>"; //$NON-NLS-1$
    		}
    		else if (message.indexOf("settings' doesn't exist") != -1) { //$NON-NLS-1$
    			title = Localizer.get("DialogDatabase.alert.title.database-error"); //$NON-NLS-1$
    			message = "<html>" + message + " <br> The database does not contain the necessary tables for MeD's Movie Manager to function properly." ; //$NON-NLS-1$ //$NON-NLS-2$
    		}
    		else if (message.indexOf("already exists") != -1) { //$NON-NLS-1$
    			title = Localizer.get("DialogDatabase.alert.title.database-error"); //$NON-NLS-1$
    			message = "<html> Table creation was denied. <br>" + message; //$NON-NLS-1$
    		}
    		else if (message.equals("The database is already in use by another process")) { //$NON-NLS-1$
    			message = "The database is already in use by another process."; //$NON-NLS-1$
    			title = Localizer.get("DialogDatabase.mysql.title.connection-attempt-failed"); //$NON-NLS-1$
    		
    			DialogQuestion question = new DialogQuestion("Database already in use!", 
    					"<html>"+ message +"<br>"+ //$NON-NLS-1$ //$NON-NLS-2$
    			"<center>Retry?</center></html>"); //$NON-NLS-1$

    			GUIUtil.showAndWait(question, true);

    			if (question.getAnswer()) {
    				return true;
    			}
    			message = "";
    		}
    		else if (message.equals("Connection reset")) { //$NON-NLS-1$

    			MovieManager.getDialog().getAppMenuBar().setDatabaseComponentsEnable(false);

    			DialogQuestion question = new DialogQuestion(Localizer.get("DialogDatabase.alert.title.connection-reset"), "<html>The connection to the MySQL server has been reset.<br>"+ //$NON-NLS-1$ //$NON-NLS-2$
    			"Reconnect now?</html>"); //$NON-NLS-1$

    			GUIUtil.showAndWait(question, true);

    			if (question.getAnswer()) {

    				try {
    					Database db = connectToDatabase(parent, _database.getPath());

    					if (db != null && _database.isSetUp()) {
    						updateProgress(progressBar, Localizer.get("DialogDatabase.progress.retrieving-movie-list")); //$NON-NLS-1$
    						MovieManager.getDatabaseHandler().setDatabase(_database, true);
    						return false;
    					}
    					else {
    						//swingWorker.interrupt();
    						progressBar.close();
    						showDatabaseMessage(parent, _database, null);
    						return false;
    					}
    				} catch (Exception e) {

    					final String finalTitle = title;
    					final String finalMessage = message;
    					
    					SwingUtilities.invokeAndWait(new Runnable() {

    						public void run() {
    							JDialog alert;

    							if (parent instanceof Frame)
    								alert = new DialogAlert((Frame) parent, finalTitle, finalMessage);
    							else
    								alert = new DialogAlert((Dialog) parent, finalTitle, finalMessage);

    							GUIUtil.showAndWait(alert, true);
    						}
    					});
    				}
    			}
    		}
    		else if (message.equals("MySQL server is out of space")) { //$NON-NLS-1$
    			title = Localizer.get("DialogDatabase.alert.mysql.title.server-out-of-space"); //$NON-NLS-1$
    			message = Localizer.get("DialogDatabase.alert.mysql.message.server-out-of-space"); //$NON-NLS-1$
    		}
    		
    		if (!message.equals("")) { //$NON-NLS-1$

    			String msg2 = ""; //$NON-NLS-1$

    			if (exception != null) {
    				msg2 = exception.getMessage();
    			}

    			final String finalTitle = title;
    			final String finalMessage = message;

    			SwingUtilities.invokeAndWait(new Runnable() {

    				public void run() {
    					JDialog alert;
    					
    					if (parent instanceof Frame)
    						alert = new DialogAlert((Frame) parent, finalTitle, finalMessage, true);
    					else
    						alert = new DialogAlert((Dialog) parent, finalTitle, finalMessage, true);

    					GUIUtil.showAndWait(alert, true);
    				}
    			});

    		}

    	} catch (InterruptedException err) {
    		log.error("Exception:" + err.getMessage(), err); //$NON-NLS-1$
    	} catch (InvocationTargetException err) {
    		log.error("Exception:" + err.getMessage(), err); //$NON-NLS-1$
    	} catch (Exception err) {
    		log.error("Exception:" + err.getMessage(), err); //$NON-NLS-1$
    	}
    	return false;
    }
}
