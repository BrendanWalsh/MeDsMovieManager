/**
 * @(#)DialogImport.java 1.0 26.09.06 (dd.mm.yy)
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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import net.sf.networktools.proportionlayout.ProportionLayout;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandLists;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ExportMode;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImportMode;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.util.CustomFileFilter;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.log4j.Logger;


public class DialogImport extends JDialog implements ActionListener {

	Logger log = Logger.getLogger(getClass());
	
	private JTextField textFilePath;
	private JTextField excelFilePath;
	private JTextField xmlDatabaseFilePath;
	private JTextField xmlFilePath;
	private JTextField csvFilePath;
	public JTextField csvSeparator;
	public JComboBox csvEncoding; 
	
	private JButton browseForTextFile;
	private JButton browseForExcelFile;
	private JButton browseForXMLDatabaseFile;
	private JButton browseForXMLFile;
	private JButton browseForCSVFile;

	private JButton buttonCancel;
	private JButton buttonAddMovies;
	private JButton buttonAddList;

	private JRadioButton askButton;
	private JRadioButton selectIfOnlyOneHitButton;
	private JRadioButton selectFirstHitButton;

	private JCheckBox imdbSearchAddToSkippedList;
	
	protected JCheckBox enableSearchForImdbInfo;

	public JCheckBox enableAddMoviesToList;
	public JComboBox listChooser;

	protected JTabbedPane tabbedPane;

	protected JPanel all;

	boolean cancel = false;
	public boolean cancelAll = false;

	KeyboardShortcutManager shortcutManager = new KeyboardShortcutManager(this);
	
	ModelImportExportSettings settings = new ModelImportExportSettings();

	private ImdbImportOption multiAddSelectOption = ImdbImportOption.off; 
	
	public DialogImport() {
		/* Dialog creation...*/
		super(MovieManager.getDialog());
		
		/* Close dialog... */
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
		});

		setTitle("Import Movies");
		setModal(true);
		setResizable(false);
	
		createGUI();
	
		setHotkeyModifiers();
	}
	
	void closeWindow() {
		cancelAll = true;
		MovieManager.getConfig().setLastDialogImportType(getImportMode());
	}

	void createGUI() {
		/* Tabbed pane */
		tabbedPane = new JTabbedPane();
		tabbedPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		tabbedPane.add(createTextPanel(), ImportMode.TEXT.getTitle());
		tabbedPane.add(createExcelPanel(), ImportMode.EXCEL.getTitle());
		tabbedPane.add(createXMLPanel(), ImportMode.XML_DATABASE.getTitle());
		tabbedPane.add(createCSVPanel(), ImportMode.CSV.getTitle());

		ImportMode lastImportType = MovieManager.getConfig().getLastDialogImportType();

		int index = tabbedPane.indexOfTab(lastImportType.getTitle());
		if (index >= 0)
			tabbedPane.setSelectedIndex(index);
		
		all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
		all.add(createIMDbOptions());
		all.add(tabbedPane);
		all.add(createListPanel());
		all.add(createButtonPanel());

		KeyStroke left_arrow = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false);
		KeyStroke right_arrow = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false);
		Action left_action = new AbstractAction()  {
			public void actionPerformed(ActionEvent e) {
				int sel = tabbedPane.getSelectedIndex();
				if (--sel == -1)
					sel = tabbedPane.getTabCount()-1;

				tabbedPane.setSelectedIndex(sel);
			}
		};
		Action right_action = new AbstractAction()  {
			public void actionPerformed(ActionEvent e) {
				int sel = tabbedPane.getSelectedIndex();
				if (++sel == tabbedPane.getTabCount())
					sel = 0;

				tabbedPane.setSelectedIndex(sel);
			}
		};

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(left_arrow, "LEFT");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(right_arrow, "RIGHT");
		getRootPane().getActionMap().put("LEFT", left_action);
		getRootPane().getActionMap().put("RIGHT", right_action);


		getContentPane().add(all, BorderLayout.NORTH);
		/* Packs and sets location... */
		pack();

		setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
				(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);


		if (MovieManager.getConfig().getImportIMDbInfoEnabled())
			enableSearchForImdbInfo.doClick();

		switch (MovieManager.getConfig().getImportIMDbSelectOption()) {
		case displayList: {askButton.setSelected(true); break;}
		case selectFirst: {selectFirstHitButton.setSelected(true); break;}
		case selectFirstOrAddToSkippedList: {selectFirstHitButton.setSelected(true); imdbSearchAddToSkippedList.setSelected(true); break;}
		case selectIfOnlyOneHit: {selectIfOnlyOneHitButton.setSelected(true); break;}
		case selectIfOnlyOneHitOrAddToSkippedList: {selectIfOnlyOneHitButton.setSelected(true); imdbSearchAddToSkippedList.setSelected(true); break;}
		}

		updateIMDbSearchButtonSettings();
	}
	
	JPanel createButtonPanel() {
		buttonCancel = new JButton("Cancel");
		buttonCancel.setActionCommand("DialogAddMultipleMovies - Cancel");
		buttonCancel.addActionListener(this);

		buttonAddMovies = new JButton("Add Movies");
		buttonAddMovies.setToolTipText("Add movies in the selected directory");
		buttonAddMovies.setActionCommand("DialogAddMultipleMovies - Add Movies");
		buttonAddMovies.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(buttonAddMovies);
		buttonPanel.add(buttonCancel);

		return buttonPanel;
	}
	
	JPanel createIMDbOptions() {
		/*Radio buttons, choses if the list of hits should apear or not*/
		askButton = new JRadioButton("Display list of hits");
		askButton.setActionCommand("Display list of hits");
		askButton.addActionListener(this);
		askButton.setSelected(true);

		selectFirstHitButton = new JRadioButton("Select First Hit (if there are any)");
		selectFirstHitButton.setActionCommand("Select First Hit");
		selectFirstHitButton.addActionListener(this);


		selectIfOnlyOneHitButton = new JRadioButton("Select If Only One Hit, else display list of hits");
		selectIfOnlyOneHitButton.setActionCommand("Select If Only One Hit");
		selectIfOnlyOneHitButton.addActionListener(this);

		imdbSearchAddToSkippedList = new JCheckBox("If no hits, add to skipped-list instead");
		imdbSearchAddToSkippedList.setToolTipText("The movie will be added to a list named 'Importer-skipped'");

		imdbSearchAddToSkippedList.addActionListener(this);

		askButton.setEnabled(false);
		selectIfOnlyOneHitButton.setEnabled(false);
		selectFirstHitButton.setEnabled(false);

		ButtonGroup radioButtonGroup = new ButtonGroup();
		radioButtonGroup.add(askButton);
		radioButtonGroup.add(selectFirstHitButton);
		radioButtonGroup.add(selectIfOnlyOneHitButton);

		JPanel radioButtonPanel = new JPanel(new GridLayout(0, 1));

		radioButtonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5,5,5,5)));

		radioButtonPanel.add(askButton);
		radioButtonPanel.add(selectFirstHitButton);
		radioButtonPanel.add(selectIfOnlyOneHitButton);

		radioButtonPanel.add(imdbSearchAddToSkippedList);

		enableSearchForImdbInfo = new JCheckBox("Get IMDb info");
		enableSearchForImdbInfo.addActionListener(this);

		JPanel imdbPanel = new JPanel();

		imdbPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," IMDb Dialog "), BorderFactory.createEmptyBorder(5,5,5,5)));

		imdbPanel.add(enableSearchForImdbInfo);
		imdbPanel.add(radioButtonPanel);

		return imdbPanel;
	}


	JPanel createTextPanel() {
		/* Textfile */

		JLabel textlabel = new JLabel("Import movies from a textfile containing movie titles only");
		JPanel textLabelPanel = new JPanel();
		textLabelPanel.add(textlabel);

		/* textfile path */
		textFilePath = new JTextField(27);
		textFilePath.setText(MovieManager.getConfig().getImportTextFilePath());

		browseForTextFile = new JButton("Browse");
		browseForTextFile.setToolTipText("Browse for a text file");
		browseForTextFile.setActionCommand("Browse text File");
		browseForTextFile.addActionListener(this);

		JPanel textPathPanel = new JPanel();
		textPathPanel.setLayout(new FlowLayout());
		textPathPanel.add(textFilePath);
		textPathPanel.add(browseForTextFile);

		textPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," File to Import "), BorderFactory.createEmptyBorder(0,0,0,0))));

		JPanel textFilePanel = new JPanel(new BorderLayout());
		textFilePanel.add(textLabelPanel, BorderLayout.NORTH);
		textFilePanel.add(textPathPanel, BorderLayout.SOUTH);

		return textFilePanel;
	}

	JPanel createExcelPanel() {

		/* Excel spreadsheet */
		JLabel excelLabel = new JLabel("Import movies from an excel spreadsheet");
		JPanel excelLabelPanel = new JPanel();
		excelLabelPanel.add(excelLabel);

		JPanel excelOptionPanel = new JPanel();
		excelOptionPanel.setLayout(new BoxLayout(excelOptionPanel, BoxLayout.Y_AXIS));

		ProportionLayout propLayout = new ProportionLayout();


		propLayout.appendColumn(10);                                // column 0
		// Column 0 will be an empty space of width 10
		propLayout.appendColumn(0, ProportionLayout.NO_PROPORTION); // column 1
		// Column 1 will always be the greatest preferred width of all it's components

		propLayout.appendColumn(10); // column 1

		// Then add all the rows to the ProportionLayout
		propLayout.appendRow(10);                                // row 0
		// Row 0 will be an empty space of width 10
		propLayout.appendRow(0, ProportionLayout.NO_PROPORTION); // row 1
		// Row 1 will always be the greatest preferred height of all it's components
		// Row 1 will never get any of the additional height
		propLayout.appendRow(10);                                // row 2


		excelOptionPanel.setLayout(propLayout);


		/* Excel file path */
		excelFilePath = new JTextField(27);
		excelFilePath.setText(MovieManager.getConfig().getImportExcelFilePath());

		browseForExcelFile = new JButton("Browse");
		browseForExcelFile.setToolTipText("Browse for a excel file");
		browseForExcelFile.setActionCommand("Browse excel File");
		browseForExcelFile.addActionListener(this);

		JPanel excelPathPanel = new JPanel();
		excelPathPanel.setLayout(new FlowLayout());
		excelPathPanel.add(excelFilePath);
		excelPathPanel.add(browseForExcelFile);

		excelPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," File to Import "), BorderFactory.createEmptyBorder(0,5,0,5))));

		JPanel excelFilePanel = new JPanel(new BorderLayout());
		excelFilePanel.add(excelLabelPanel, BorderLayout.NORTH);
		excelFilePanel.add(excelOptionPanel, BorderLayout.CENTER);
		excelFilePanel.add(excelPathPanel, BorderLayout.SOUTH);

		return excelFilePanel;
	}

	JPanel createXMLPanel() {
		/* XML file path */
		xmlDatabaseFilePath = new JTextField(27);
		xmlDatabaseFilePath.setText(MovieManager.getConfig().getImportXMLFilePath());

		browseForXMLDatabaseFile = new JButton("Browse");
		browseForXMLDatabaseFile.setToolTipText("Browse for an XML file");
		browseForXMLDatabaseFile.setActionCommand("Browse XML File");
		browseForXMLDatabaseFile.addActionListener(this);

		JPanel xmlDatabasePathPanel = new JPanel();
		xmlDatabasePathPanel.setLayout(new FlowLayout());
		xmlDatabasePathPanel.add(xmlDatabaseFilePath);
		xmlDatabasePathPanel.add(browseForXMLDatabaseFile);

		xmlDatabasePathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," File to Import "), BorderFactory.createEmptyBorder(0,0,0,0))));

		JLabel xmlDatabaseLabel = new JLabel("<html>Import movies from a XML Database file (Must be exported by <br>MeD's Movie Manager) The necessary additional info fields will <br>be automatically added if needed.</p></html>");
		JPanel xmlDatabaseLabelPanel = new JPanel();
		xmlDatabaseLabelPanel.add(xmlDatabaseLabel);

		JPanel xmlDatabaseFilePanel = new JPanel(new BorderLayout());
		xmlDatabaseFilePanel.add(xmlDatabaseLabelPanel, BorderLayout.NORTH);
		xmlDatabaseFilePanel.add(xmlDatabasePathPanel, BorderLayout.SOUTH);

		return xmlDatabaseFilePanel;
	}

	JPanel createCSVPanel() {
		JLabel csvLabel = new JLabel("Import movies from a CSV file");
		JPanel csvLabelPanel = new JPanel();
		csvLabelPanel.add(csvLabel);


		JLabel csvSeparatorLabel = new JLabel("Separator:");
		csvSeparator = new JTextField(5);
		csvSeparator.setText(MovieManager.getConfig().getImportCSVseparator());

		JLabel csvEncodingLabel = new JLabel("File encoding:");
		csvEncoding = new JComboBox(new DefaultComboBoxModel(ModelImportExportSettings.encodings));

		JPanel csvOpt = new JPanel();
		csvOpt.add(csvSeparatorLabel);
		csvOpt.add(csvSeparator);
		csvOpt.add(csvEncodingLabel);
		csvOpt.add(csvEncoding);

		/* CSV option panel */
		JPanel csvOptionPanel1 = new JPanel();
		csvOptionPanel1.setLayout(new BoxLayout(csvOptionPanel1, BoxLayout.Y_AXIS));
		csvOptionPanel1.add(csvOpt);

		csvFilePath = new JTextField(27);
		csvFilePath.setText(MovieManager.getConfig().getImportCSVFilePath());

		browseForCSVFile = new JButton("Browse");
		browseForCSVFile.setToolTipText("Browse for a CSV file");
		browseForCSVFile.setActionCommand("Browse CSV File");
		browseForCSVFile.addActionListener(this);

		JPanel csvPathPanel = new JPanel();
		csvPathPanel.setLayout(new FlowLayout());
		csvPathPanel.add(csvFilePath);
		csvPathPanel.add(browseForCSVFile);

		csvPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"  File to Import "), BorderFactory.createEmptyBorder(0,5,0,5))));

		JPanel csvFilePanel = new JPanel(new BorderLayout());
		csvFilePanel.add(csvLabelPanel, BorderLayout.NORTH);
		csvFilePanel.add(csvOptionPanel1, BorderLayout.CENTER);
		csvFilePanel.add(csvPathPanel, BorderLayout.SOUTH);

		return csvFilePanel;
	}


	

	JPanel createListPanel() {

		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		listPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Add to list "), BorderFactory.createEmptyBorder(5,5,5,5)));

		ArrayList<String> columnListNames = MovieManager.getIt().getDatabase().getListsColumnNames();
		Object [] listNames = columnListNames.toArray();

		if (listNames.length == 0) {

			JLabel label = new JLabel("To add the movies to a list you need need to create a list");
			listPanel.add(label, BorderLayout.WEST);

			buttonAddList = new JButton("Add list");
			buttonAddList.addActionListener(this);
			listPanel.add(buttonAddList, BorderLayout.CENTER);

		}
		else {
			buttonAddList = new JButton("Add list");
			buttonAddList.addActionListener(this);
			listPanel.add(buttonAddList, BorderLayout.WEST);

			String list = MovieManager.getConfig().getMultiAddList();

			listChooser = new JComboBox(listNames);

			enableAddMoviesToList = new JCheckBox("Enable");
			enableAddMoviesToList.setActionCommand("enableAddMoviesToList");
			enableAddMoviesToList.setToolTipText("Enable applying added movies to a list.");
			enableAddMoviesToList.addActionListener(this);

			listChooser.setSelectedItem(list);

			if (MovieManager.getConfig().getMultiAddListEnabled()) {
				enableAddMoviesToList.setSelected(true);
				listChooser.setEnabled(true);
			}
			else {
				enableAddMoviesToList.setSelected(false);
				listChooser.setEnabled(false);
			}

			if (listChooser.getSelectedIndex() == -1)
				listChooser.setSelectedIndex(0);

			JPanel listChooserPanel = new JPanel(); 
			listChooserPanel.add(enableAddMoviesToList);
			listChooserPanel.add(listChooser);

			listPanel.add(listChooserPanel, BorderLayout.EAST);
		}
		return listPanel;
	}

	/*Opens a filechooser and returns the absolute path to the selected file*/
	private String executeCommandGetFile(ImportMode importMode) {


		/* Opens the Open dialog... */
		ExtendedFileChooser fileChooser = new ExtendedFileChooser();
		try {
			fileChooser.setFileSelectionMode(ExtendedFileChooser.FILES_ONLY);

			String path = getPath();

			if (!path.equals("") && new File(path).isFile() && !new File(path).isDirectory()) {
				path = new File(path).getParent();
			}

			fileChooser.setCurrentDirectory(new File(path));

			String title = "";

			if (importMode == ImportMode.TEXT) {
				title = "Select text file";
				fileChooser.setFileFilter(new CustomFileFilter(new String[]{"*.*"}, new String("All Files (*.*)")));
				fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"txt"},new String("Textfile (*.txt)")));
			} 
			else if (importMode == ImportMode.EXCEL) {
				title = "Select excel file";
				fileChooser.setFileFilter(new CustomFileFilter(new String[]{"*.*"}, new String("All Files (*.*)")));
				fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"xls"},new String("Excel spreadsheet (*.xls)")));
			}
			else if (importMode == ImportMode.XML_DATABASE) {
				title = "Select XML file";
				fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"xml"},new String("Exported XML Database file (*.xml)")));
			}
			else if (importMode == ImportMode.CSV) {
				title = "Select CSV file";
				fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[] {
						"csv"
				}, new String("Exported CSV file (*.csv)")));
			}

			fileChooser.setDialogTitle(title);
			fileChooser.setApproveButtonText("Select");
			fileChooser.setApproveButtonToolTipText("Select file");
			fileChooser.setAcceptAllFileFilterUsed(false);

			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
				/* Gets the path... */
				String filepath = fileChooser.getSelectedFile().getAbsolutePath();

				if (!(new File(filepath).exists())) {
					throw new Exception("File not found!");
				}

				MovieManager.getConfig().setLastMiscDir(new File(filepath));

				return filepath;
			}
		}
		catch (Exception e) {
			log.error("", e);
		}

		return "";
	}

	/*Saves the options to the settings object*/
	void executeSave() {

		MovieManager.getConfig().setImportIMDbInfoEnabled(enableSearchForImdbInfo.isSelected());

		MovieManager.getConfig().setImportIMDbSelectOption(getMultiAddSelectOption());

		MovieManager.getConfig().setLastDialogImportType(getImportMode());

		MovieManager.getConfig().setImportTextFilePath(textFilePath.getText());
		MovieManager.getConfig().setImportExcelFilePath(excelFilePath.getText());
		MovieManager.getConfig().setImportXMLFilePath(xmlDatabaseFilePath.getText());
		MovieManager.getConfig().setImportCSVFilePath(csvFilePath.getText());
		MovieManager.getConfig().setImportCSVseparator(csvSeparator.getText());

		if (listChooser != null) {
			MovieManager.getConfig().setMultiAddList((String) listChooser.getSelectedItem());

			if (enableAddMoviesToList.isSelected())
				MovieManager.getConfig().setMultiAddListEnabled(true);
			else
				MovieManager.getConfig().setMultiAddListEnabled(false);
		}

		MovieManager.getConfig().setMultiAddSelectOption(multiAddSelectOption);

		settings.multiAddIMDbSelectOption = multiAddSelectOption;

		if (csvSeparator.getText().trim().length() > 0)
			settings.csvSeparator = csvSeparator.getText().trim().charAt(0);

		settings.textEncoding = (String) csvEncoding.getSelectedItem();
		settings.filePath = csvFilePath.getText();
		settings.importMode = getImportMode();


		settings.multiAddIMDbSelectOption = getMultiAddSelectOption();

		// File path depending on import mode
		settings.filePath = getPath();

		if (enableAddMoviesToList != null && enableAddMoviesToList.isSelected()) {
			settings.addToThisList = (String) listChooser.getSelectedItem();
		}
	}

	public ModelImportExportSettings getSettings() {
		executeSave();
		return settings;
	}


	public ImportMode getImportMode() {

		String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());

		if (title.equals(ImportMode.TEXT.getTitle()))
			return ImportMode.TEXT;
		else if (title.equals(ImportMode.EXCEL.getTitle()))
			return ImportMode.EXCEL;
		else if (title.equals(ImportMode.XML_DATABASE.getTitle()))
			return ImportMode.XML_DATABASE;
		else if (title.equals(ImportMode.XML.getTitle()))
			return ImportMode.XML;
		else if (title.equals(ImportMode.CSV.getTitle()))
			return ImportMode.CSV;

		return ImportMode.TEXT;
	}

	/*Returns the string in the path textfield*/
	public String getPath() {

		switch (getImportMode()) {
		case TEXT : return textFilePath.getText();
		case EXCEL : return excelFilePath.getText();
		case XML_DATABASE : return xmlDatabaseFilePath.getText();
		case XML : return xmlFilePath.getText();
		case CSV : return csvFilePath.getText();
		}
		return "";
	}


	public ImdbImportOption getMultiAddSelectOption() {

		if (!enableSearchForImdbInfo.isSelected()) {
			return ImdbImportOption.off;
		}

		if (askButton.isSelected())
			return ImdbImportOption.displayList;

		if (selectFirstHitButton.isSelected())
			return !imdbSearchAddToSkippedList.isSelected() ? 
					ImdbImportOption.selectFirst : ImdbImportOption.selectFirstOrAddToSkippedList;

		if (selectIfOnlyOneHitButton.isSelected())
			return !imdbSearchAddToSkippedList.isSelected() ? 
					ImdbImportOption.selectIfOnlyOneHit : ImdbImportOption.selectIfOnlyOneHitOrAddToSkippedList;

		return ImdbImportOption.off;
	}

	public void updateIMDbSearchButtonSettings() {

		if (!enableSearchForImdbInfo.isSelected()) {
			askButton.setEnabled(false);
			selectIfOnlyOneHitButton.setEnabled(false);
			selectFirstHitButton.setEnabled(false);

			imdbSearchAddToSkippedList.setEnabled(false);
		}
		else {
			askButton.setEnabled(true);
			selectIfOnlyOneHitButton.setEnabled(true);
			selectFirstHitButton.setEnabled(true);

			if (askButton.isSelected()) {
				imdbSearchAddToSkippedList.setEnabled(false);
			}
			else {
				imdbSearchAddToSkippedList.setEnabled(true);
			}
		}
	}

	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: "+ event.getActionCommand());

		if (event.getSource().equals(browseForTextFile)) {
			String ret = executeCommandGetFile(ImportMode.TEXT);
			if (!ret.equals(""))
				textFilePath.setText(ret);
		}

		if (event.getSource().equals(browseForExcelFile)) {
			String ret = executeCommandGetFile(ImportMode.EXCEL);
			if (!ret.equals(""))
				excelFilePath.setText(ret);
		}

		if (event.getSource().equals(browseForXMLDatabaseFile)) {
			String ret = executeCommandGetFile(ImportMode.XML_DATABASE);
			if (!ret.equals(""))
				xmlDatabaseFilePath.setText(ret);
		}

		if (event.getSource().equals(browseForXMLFile)) {
			String ret = executeCommandGetFile(ImportMode.XML);
			if (!ret.equals(""))
				xmlFilePath.setText(ret);
		}

		if(event.getSource().equals(browseForCSVFile)) {
			String s2 = executeCommandGetFile(ImportMode.CSV);
			if(!s2.equals(""))
				csvFilePath.setText(s2);
		}

		if (event.getSource().equals(buttonCancel)) {
			log.debug("ActionPerformed: " + event.getActionCommand());
			executeSave();
			cancelAll = true;
			dispose();
		}

		if (event.getSource().equals(buttonAddMovies)) {
			log.debug("ActionPerformed: " + event.getActionCommand());

			if (getPath().equals("")) {
				DialogAlert alert = new DialogAlert(this, "Alert","Please specify a file path.");
				GUIUtil.showAndWait(alert, true);
			}
			else if (!new File(getPath()).exists()) {
				DialogAlert alert = new DialogAlert(this, "Alert","The specified file does not exist.");
				GUIUtil.showAndWait(alert, true);
			}
			else {
				executeSave();

				if (imdbSearchAddToSkippedList.isSelected()) {
					if (!MovieManager.getIt().getDatabase().listColumnExist(settings.skippedListName)) {
						if (MovieManager.getIt().getDatabase().addListsColumn(settings.skippedListName) != 0) {
							DialogAlert alert = new DialogAlert(this, "Database error", "Failed to create new list " + settings.skippedListName);
							GUIUtil.show(alert, true);
							cancelAll = true;
						}
					}		
				}
				dispose();
			}
		}


		if (event.getSource().equals(buttonAddList)) {

			MovieManagerCommandLists.execute(this);

			all.remove(2);
			all.add(createListPanel(), 2);
			pack();

			GUIUtil.show(this, true);
		}

		if (event.getSource().equals(enableSearchForImdbInfo)) {
			updateIMDbSearchButtonSettings();
		}

		if (event.getSource().equals(askButton)) {
			updateIMDbSearchButtonSettings();
		}

		if (event.getSource().equals(selectFirstHitButton)) {
			updateIMDbSearchButtonSettings();
		}

		if (event.getSource().equals(selectIfOnlyOneHitButton)) {
			updateIMDbSearchButtonSettings();
		}

		if (event.getSource().equals(enableAddMoviesToList)) {

			if (enableAddMoviesToList.isSelected())
				listChooser.setEnabled(true);
			else
				listChooser.setEnabled(false);
		}
	}
	
	void setHotkeyModifiers() {
		
		try {			
			
			GUIUtil.enableDisposeOnEscapeKey(shortcutManager, "Close Window", new AbstractAction()	{
				public void actionPerformed(ActionEvent e) {
					closeWindow();
				}
			});
					
			shortcutManager.registerShowKeysKey();
			
			shortcutManager.setKeysToolTipComponent(tabbedPane);
			
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
	}
}
