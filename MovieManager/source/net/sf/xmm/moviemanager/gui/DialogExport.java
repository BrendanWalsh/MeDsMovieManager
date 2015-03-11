/**
 * @(#)DialogExport.java
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
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ExportMode;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.util.CustomFileFilter;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;

public class DialogExport extends JDialog implements ActionListener {
    
	static Logger log = Logger.getLogger(DialogExport.class);
         
    JRadioButton simpleExport;
    JRadioButton fullExport;
    
    JCheckBox enableAlphabeticSplit;
    
    JTextField titleTextField;
    
    JButton closeButton;
    JButton exportButton;
    
    JTextField csvFilePath;
    JTextField csvSeparator;
    JComboBox csvEncoding;
    JButton browseForCSVFile;
    
    JTextField xmlDbFilePath;
    JButton browseForXMLDbFile;
    
    JTextField xmlFilePath;
    JComboBox xmlEncoding;
    JButton browseForXMLFile;
    
    JTextField excelFilePath;
    JButton browseForEXCELFile;
    
    KeyboardShortcutManager shortcutManager = new KeyboardShortcutManager(this);
    
    boolean cancelled = false;
        
    ModelImportExportSettings settings = new ModelImportExportSettings();
        
    JTabbedPane tabs = null;
    
    boolean verifyMoviesAvailable() {
    	
    	ArrayList<ModelMovie> movies = MovieManager.getDialog().getCurrentMoviesList();
    	    	
    	if (movies.size() == 0) {
    		
    		DialogAlert alert = new DialogAlert(MovieManager.getDialog(), "No titles", "There are no titles to export!");
    		GUIUtil.show(alert, true);
    		return false;
    	}
    	return true;
    }
    
    public DialogExport() {
        /* Dialog creation...*/
        super(MovieManager.getDialog());
        /* Close dialog... */
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	cancelled = true;
            }
        });
       
        GUIUtil.enableDisposeOnEscapeKey(shortcutManager, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	cancelled = true;
            }
        });
                        
        KeyStroke left_arrow = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false);
		KeyStroke right_arrow = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false);
		Action left_action = new AbstractAction()  {
			public void actionPerformed(ActionEvent e) {
				int sel = tabs.getSelectedIndex();
				if (--sel == -1)
					sel = tabs.getTabCount()-1;
			
				tabs.setSelectedIndex(sel);
			}
		};
		Action right_action = new AbstractAction()  {
			public void actionPerformed(ActionEvent e) {
				int sel = tabs.getSelectedIndex();
				if (++sel == tabs.getTabCount())
					sel = 0;
			
				tabs.setSelectedIndex(sel);
			}
		};
		
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(left_arrow, "LEFT");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(right_arrow, "RIGHT");
		getRootPane().getActionMap().put("LEFT", left_action);
		getRootPane().getActionMap().put("RIGHT", right_action);
        
        setTitle(Localizer.get("DialogExport.title")); //$NON-NLS-1$
        setResizable(false);
        setModal(true);
        
        createGUI();
        
        if (!verifyMoviesAvailable()) {
        	exportButton.setEnabled(false);
        }
    }
    
    void createGUI() {

    	tabs = new JTabbedPane();
    	tabs.add(createCSVPanel(), ExportMode.CSV.getTitle());
    	tabs.add(createExcelPanel(), ExportMode.EXCEL.getTitle());
    	tabs.add(createXMLDatabasePanel(), ExportMode.XML_DATABASE.getTitle());
    	tabs.add(createXMLPanel(), ExportMode.XML.getTitle());
    	tabs.add(createHTMLPanel(), ExportMode.HTML.getTitle());

    	String lastTitle = MovieManager.getConfig().getLastDialogImportType().toString();

    	int index = tabs.indexOfTab(lastTitle);

    	if (index != -1)
    		tabs.setSelectedIndex(index);

    	Container container = getContentPane();
    	container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));
    	container.add(tabs);
    	container.add(createButtonsPanel());

    	pack();
    	setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
    			(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
    }

    
    JPanel createCSVPanel() {
    	// CSV panel

    	JLabel csvLabel = new JLabel("Export movies to CSV");
    	JPanel csvLabelPanel = new JPanel();
    	csvLabelPanel.add(csvLabel);

    	JLabel csvSeparatorLabel = new JLabel("Separator:");
    	csvSeparator = new JTextField(5);
    	csvSeparator.setText(MovieManager.getConfig().getExportCSVseparator());

    	JLabel csvEncodingLabel = new JLabel("File encoding:");
    	csvEncoding = new JComboBox(new DefaultComboBoxModel(ModelImportExportSettings.encodings));

    	JPanel csvOpt = new JPanel();
    	csvOpt.add(csvSeparatorLabel);
    	csvOpt.add(csvSeparator);
    	csvOpt.add(csvEncodingLabel);
    	csvOpt.add(csvEncoding);


    	/* CSV option panel */
    	JPanel csvOptionPanel = new JPanel();
    	csvOptionPanel.setLayout(new BoxLayout(csvOptionPanel, BoxLayout.Y_AXIS));
    	csvOptionPanel.add(csvOpt);

    	csvFilePath = new JTextField(27);
    	csvFilePath.setText(MovieManager.getConfig().getExportCSVFilePath());

    	browseForCSVFile = new JButton("Browse");
    	browseForCSVFile.setToolTipText("Browse for a CSV file");
    	browseForCSVFile.setActionCommand("Browse CSV File");
    	browseForCSVFile.addActionListener(this);

    	JPanel csvPathPanel = new JPanel();
    	csvPathPanel.setLayout(new FlowLayout());
    	csvPathPanel.add(csvFilePath);
    	csvPathPanel.add(browseForCSVFile);

    	csvPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"  File to Export "), BorderFactory.createEmptyBorder(0,5,0,5))));

    	JPanel csvFilePanel = new JPanel(new BorderLayout());
    	csvFilePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createTitledBorder(
    			BorderFactory.createEtchedBorder(), " CSV ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(csvFilePanel.getFont().getName(),Font.BOLD, csvFilePanel.getFont().getSize()) //$NON-NLS-1$
    	)), BorderFactory.createEmptyBorder(0,2,2,2)));

    	csvFilePanel.add(csvLabelPanel, BorderLayout.NORTH);
    	csvFilePanel.add(csvOptionPanel, BorderLayout.CENTER);
    	csvFilePanel.add(csvPathPanel, BorderLayout.SOUTH);

    	return csvFilePanel;
    }

    
    JPanel createExcelPanel() {

    	// Excel panel
    	JLabel excelLabel = new JLabel("Export movies to excel");
    	JPanel excelLabelPanel = new JPanel();
    	excelLabelPanel.add(excelLabel);

    	excelFilePath = new JTextField(27);
    	excelFilePath.setText(MovieManager.getConfig().getExportExcelFilePath());

    	browseForEXCELFile = new JButton("Browse");
    	browseForEXCELFile.setToolTipText("Browse for a excel file");
    	browseForEXCELFile.setActionCommand("Browse excel File");
    	browseForEXCELFile.addActionListener(this);

    	JPanel excelPathPanel = new JPanel();
    	excelPathPanel.setLayout(new FlowLayout());
    	excelPathPanel.add(excelFilePath);
    	excelPathPanel.add(browseForEXCELFile);

    	excelPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"  File to Export "), BorderFactory.createEmptyBorder(0,5,0,5))));

    	JPanel excelFilePanel = new JPanel(new BorderLayout());
    	excelFilePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createTitledBorder(
    			BorderFactory.createEtchedBorder(), " Excel ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(excelPathPanel.getFont().getName(),Font.BOLD, excelPathPanel.getFont().getSize()) //$NON-NLS-1$
    	)), BorderFactory.createEmptyBorder(0,2,2,2)));

    	excelFilePanel.add(excelLabelPanel, BorderLayout.NORTH);
    	excelFilePanel.add(excelPathPanel, BorderLayout.SOUTH);

    	return excelFilePanel;
    }
    
    
    JPanel createXMLDatabasePanel() {
    	// XML database panel

    	// XML panel
    	JLabel xmlDbLabel = new JLabel("Export current movie list to XML Database");
    	JPanel xmlDbLabelPanel = new JPanel();
    	xmlDbLabelPanel.add(xmlDbLabel);

    	xmlDbFilePath = new JTextField(27);
    	xmlDbFilePath.setText(MovieManager.getConfig().getExportXMLDbFilePath());

    	browseForXMLDbFile = new JButton("Browse");
    	browseForXMLDbFile.setToolTipText("Browse for a XML file");
    	browseForXMLDbFile.setActionCommand("Browse XML File");
    	browseForXMLDbFile.addActionListener(this);

    	JPanel xmlDbPathPanel = new JPanel();
    	xmlDbPathPanel.setLayout(new FlowLayout());
    	xmlDbPathPanel.add(xmlDbFilePath);
    	xmlDbPathPanel.add(browseForXMLDbFile);

    	xmlDbPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"  File to Export "), BorderFactory.createEmptyBorder(0,5,0,5))));

    	JPanel xmlDbPanel = new JPanel(new BorderLayout());
    	xmlDbPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createTitledBorder(
    			BorderFactory.createEtchedBorder(), " XML Database ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(xmlDbPathPanel.getFont().getName(),Font.BOLD, xmlDbPathPanel.getFont().getSize()) //$NON-NLS-1$
    	)), BorderFactory.createEmptyBorder(0,2,2,2)));

    	xmlDbPanel.add(xmlDbLabelPanel, BorderLayout.NORTH);
    	xmlDbPanel.add(xmlDbPathPanel, BorderLayout.SOUTH);

    	return xmlDbPanel;
    }


    JPanel createXMLPanel() {
    	// XML panel

    	/*
    	JPanel xmlInfoPanel = new JPanel();
    	xmlInfoPanel.setLayout(new BoxLayout(xmlInfoPanel, BoxLayout.PAGE_AXIS));

    	xmlInfoPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createTitledBorder(
    			BorderFactory.createEtchedBorder(), " XML " , TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(xmlInfoPanel.getFont().getName(),Font.BOLD, xmlInfoPanel.getFont().getSize()) //$NON-NLS-1$
    	)), BorderFactory.createEmptyBorder(0,2,2,2)));

    	JLabel xmllabelInfo = new JLabel("Export current movie list to XML Database");
    	xmlInfoPanel.add(xmllabelInfo);
*/

    	// XML label
    	JLabel xmlLabel = new JLabel("Export current movie list to XML ");
    	JPanel xmlLabelPanel = new JPanel();
    	xmlLabelPanel.add(xmlLabel);

    	JLabel xmlEncodingLabel = new JLabel("File encoding:");
    	xmlEncoding = new JComboBox(new DefaultComboBoxModel(ModelImportExportSettings.encodings));
    	xmlEncoding.setSelectedItem("UTF-8");

    	JPanel xmlOpt = new JPanel();
    	xmlOpt.add(xmlEncodingLabel);
    	xmlOpt.add(xmlEncoding);


    	// XML option panel 
    	JPanel xmlOptionPanel = new JPanel();
    	xmlOptionPanel.setLayout(new BoxLayout(xmlOptionPanel, BoxLayout.Y_AXIS));
    	xmlOptionPanel.add(xmlOpt);

    	xmlFilePath = new JTextField(27);
    	xmlFilePath.setText(MovieManager.getConfig().getExportXMLFilePath());

    	browseForXMLFile = new JButton("Browse");
    	browseForXMLFile.setToolTipText("Browse for a XML file");
    	browseForXMLFile.setActionCommand("Browse XML File");
    	browseForXMLFile.addActionListener(this);

    	JPanel xmlPathPanel = new JPanel();
    	xmlPathPanel.setLayout(new FlowLayout());
    	xmlPathPanel.add(xmlFilePath);
    	xmlPathPanel.add(browseForXMLFile);

    	xmlPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,3,1,2) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"  File to Export "), BorderFactory.createEmptyBorder(0,5,0,5))));

    	JPanel xmlPanel = new JPanel(new BorderLayout());
    	xmlPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createTitledBorder(
    			BorderFactory.createEtchedBorder(), " XML ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(xmlPanel.getFont().getName(),Font.BOLD, xmlPanel.getFont().getSize()) //$NON-NLS-1$
    	)), BorderFactory.createEmptyBorder(0,2,2,2)));

    	xmlPanel.add(xmlLabelPanel, BorderLayout.NORTH);
    	xmlPanel.add(xmlOptionPanel, BorderLayout.CENTER);
    	xmlPanel.add(xmlPathPanel, BorderLayout.SOUTH);

    	return xmlPanel;
    }


    JPanel createHTMLPanel() {
    	/*Export options*/
    	simpleExport = new JRadioButton(Localizer.get("DialogExport.panel-export-options.button.simple-export")); //$NON-NLS-1$
    	simpleExport.setActionCommand("Simple Export"); //$NON-NLS-1$

    	fullExport = new JRadioButton(Localizer.get("DialogExport.panel-export-options.button.full-export")); //$NON-NLS-1$
    	fullExport.setActionCommand("Full Export"); //$NON-NLS-1$

    	if (MovieManager.getConfig().getHTMLExportType().equals("full")) //$NON-NLS-1$
    		fullExport.setSelected(true);
    	else
    		simpleExport.setSelected(true);

    	/*Group the radio buttons.*/
    	ButtonGroup exportGroup = new ButtonGroup();
    	exportGroup.add(simpleExport);
    	exportGroup.add(fullExport);

    	/*Register a listener for the radio buttons.*/
    	simpleExport.addActionListener(this);
    	fullExport.addActionListener(this);

    	enableAlphabeticSplit = new JCheckBox(Localizer.get("DialogExport.panel-export-options.button.divide-alphabetically")); //$NON-NLS-1$
    	enableAlphabeticSplit.setActionCommand("Divide alphabetically"); //$NON-NLS-1$
    	enableAlphabeticSplit.setEnabled(false);

    	/*Put the radio buttons in a column in a panel.*/
    	JPanel exportOptionPanel = new JPanel(new GridLayout(2, 1));

    	exportOptionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,0,5,0) ,BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
    			Localizer.get("DialogExport.panel-export-options.title"), //$NON-NLS-1$
    			TitledBorder.DEFAULT_JUSTIFICATION,
    			TitledBorder.DEFAULT_POSITION,
    			new Font(exportOptionPanel.getFont().getName(),Font.BOLD, exportOptionPanel.getFont().getSize())),
    			BorderFactory.createEmptyBorder(5,5,5,5))));


    	exportOptionPanel.add(simpleExport);
    	exportOptionPanel.add(fullExport);
    	exportOptionPanel.add(enableAlphabeticSplit);

    	JPanel htmlExportPanel = new JPanel();
    	htmlExportPanel.setLayout(new BoxLayout(htmlExportPanel, BoxLayout.PAGE_AXIS));

    	htmlExportPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createTitledBorder(
    			BorderFactory.createEtchedBorder(), Localizer.get("DialogExport.panel-html-export.title") , TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(htmlExportPanel.getFont().getName(),Font.BOLD, htmlExportPanel.getFont().getSize()) //$NON-NLS-1$
    	)), BorderFactory.createEmptyBorder(0,2,2,2)));

    	JPanel titlePanel = new JPanel();

    	titleTextField = new JTextField(16);
    	titleTextField.setEditable(true);

    	JLabel titleLabel = new JLabel(Localizer.get("DialogExport.title-text-field") + ": "); //$NON-NLS-1$
    	titleLabel.setLabelFor(titleTextField);

    	titlePanel.add(titleLabel);
    	titlePanel.add(titleTextField);

    	htmlExportPanel.add(new JLabel("The movies currently displayed in the movie list will be exported."));
    	htmlExportPanel.add(exportOptionPanel);
    	htmlExportPanel.add(titlePanel);

    	return htmlExportPanel;
    }

    JPanel createButtonsPanel() {
    	JPanel buttonPanel = new JPanel();
    	buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    	exportButton = new JButton(Localizer.get("DialogExport.button.export.text")); //$NON-NLS-1$
    	exportButton.setActionCommand("Export"); //$NON-NLS-1$
    	exportButton.addActionListener(this);

    	closeButton = new JButton(Localizer.get("DialogExport.button.close.text")); //$NON-NLS-1$
    	closeButton.setActionCommand("Close"); //$NON-NLS-1$
    	closeButton.addActionListener(this);

    	buttonPanel.add(exportButton);
    	buttonPanel.add(closeButton);

    	return buttonPanel;
    }


    public boolean isCancelled() {
    	return cancelled;
    }

    /* Returns the string in the path textfield */
    public String getPath() {

    	switch (getExportMode()) {
    	case EXCEL : return excelFilePath.getText();
    	case XML_DATABASE : return xmlDbFilePath.getText();
    	case XML : return xmlFilePath.getText();
    	case CSV : return csvFilePath.getText();
    	}
    	return "";
    }


    public ExportMode getExportMode() {

    	String title = tabs.getTitleAt(tabs.getSelectedIndex());

    	if (title.equals(ExportMode.CSV.getTitle()))
    		return ExportMode.CSV;
    	else if (title.equals(ExportMode.EXCEL.getTitle()))
    		return ExportMode.EXCEL;
    	else if (title.equals(ExportMode.XML.getTitle()))
    		return ExportMode.XML;
    	else if (title.equals(ExportMode.XML_DATABASE.getTitle()))
    		return ExportMode.XML_DATABASE;
    	else if (title.equals(ExportMode.HTML.getTitle()))
    		return ExportMode.HTML;

    	return ExportMode.CSV;
    }

    public ModelImportExportSettings getSettings() {
    	executeSave();
    	return settings;
    }

    /*Saves the options to the MovieManager object*/
    void executeSave() {

    	MovieManager.getConfig().setLastDialogExportType(getExportMode());

    	MovieManager.getConfig().setExportCSVFilePath(csvFilePath.getText());
    	MovieManager.getConfig().setExportExcelFilePath(excelFilePath.getText());
    	MovieManager.getConfig().setExportXMLDbFilePath(xmlDbFilePath.getText());
    	MovieManager.getConfig().setExportXMLFilePath(xmlFilePath.getText());
    	
    	// Save CSV separator
    	if (csvSeparator.getText().trim().length() > 0)
    		settings.csvSeparator = csvSeparator.getText().trim().charAt(0);

    	settings.exportMode = getExportMode();

    	settings.htmlTitle = titleTextField.getText();
    	settings.htmlAlphabeticSplit = enableAlphabeticSplit.isSelected();
    	settings.htmlSimpleMode = simpleExport.isSelected();

    	switch (settings.exportMode) {
    	case CSV: {
    		settings.filePath = csvFilePath.getText();
    		settings.textEncoding = (String) csvEncoding.getSelectedItem();
    		break;
    	}
    	case EXCEL: {
    		settings.filePath = excelFilePath.getText();
    		break;
    	}
    	case XML_DATABASE: {
    		settings.filePath = xmlDbFilePath.getText();
    		break;
    	}
    	case XML: {
    		settings.filePath = xmlFilePath.getText();
    		settings.textEncoding = (String) xmlEncoding.getSelectedItem();
    		break;
    	}
    	case HTML: {
    		// No path 
    	}
    	}
    }


    public void actionPerformed(ActionEvent event) {

    	log.debug("ActionPerformed: "+event.getActionCommand()); //$NON-NLS-1$

    	if (event.getSource().equals(closeButton)) {
    		cancelled = true;
    		executeSave();
    		dispose();
    		return;
    	}

    	if (event.getSource().equals(exportButton)) {

    		cancelled = false;

    		ExportMode exportMode = getExportMode();

    		/* HTMl export */
    		//if (tabs.getSelectedIndex() == ModelImportExportSettings.EXPORT_MODE_HTML) {
    		if (exportMode == ExportMode.HTML) {
    			executeSave();
    		}
    		// CSV or Excel
    		else if (exportMode == ExportMode.CSV || 
    				exportMode == ExportMode.EXCEL ||
    				exportMode == ExportMode.XML_DATABASE||
    				exportMode == ExportMode.XML) {

    			boolean execute = true;
    			String filePath = getPath();

    			// Check separator for CSV
    			if (exportMode == ExportMode.CSV && 
    					csvSeparator.getText().trim().equals("")) {
    				DialogAlert alert = new DialogAlert(this, "Separator missing","Please provide a separator.");
    				GUIUtil.showAndWait(alert, true);
    				execute = false;
    			}
    			else if (filePath.equals("")) {
    				DialogAlert alert = new DialogAlert(this, "Alert","Please specify a file path.");
    				GUIUtil.showAndWait(alert, true);
    				execute = false;
    			}
    			else if (new File(filePath).exists()) {
    				DialogQuestion question = new DialogQuestion("File exists", "<html>The specified file already exists.<br>"+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    				"Overwrite file?</html>"); //$NON-NLS-1$
    				GUIUtil.showAndWait(question, true);

    				if (!question.getAnswer())
    					execute = false;
    			}
    			else if (new File(filePath).getParentFile() == null || !new File(filePath).getParentFile().isDirectory()) {
    				DialogAlert alert = new DialogAlert(this, "Alert","The parent directory does not exist.");
    				GUIUtil.show(alert, true);
    				execute = false;
    			}

    			if (execute) {
    				executeSave();
    			}
    			else
    				cancelled = true;
    		}

    		if (!cancelled)
    			dispose();

    		return;
    	}

    	if (event.getSource().equals(simpleExport)) {
    		enableAlphabeticSplit.setEnabled(false);
    		return;
    	}

    	if (event.getSource().equals(fullExport)) {
    		enableAlphabeticSplit.setEnabled(true );
    		return;
    	}


    	ExportMode saveExportFile = null;
    	String extension = null;

    	if (event.getSource().equals(browseForCSVFile)) {
    		saveExportFile = ExportMode.CSV;
    		extension = ".csv";
    	}
    	else if (event.getSource().equals(browseForEXCELFile)) {
    		saveExportFile = ExportMode.EXCEL;
    		extension = ".xls";
    	}
    	else if (event.getSource().equals(browseForXMLDbFile)) {
    		saveExportFile = ExportMode.XML_DATABASE;
    		extension = ".xml";
    	}
    	else if (event.getSource().equals(browseForXMLFile)) {
    		saveExportFile = ExportMode.XML;
    		extension = ".xml";
    	}        

    	if (saveExportFile != null) {

    		String path = getPath();
    		
    		ExtendedFileChooser fileChooser = new ExtendedFileChooser();
    		fileChooser.setFileSelectionMode(ExtendedFileChooser.FILES_ONLY);

			if (!path.equals("") && new File(path).isFile() && !new File(path).isDirectory()) {
				path = new File(path).getParent();
			}

			fileChooser.setCurrentDirectory(new File(path));

			String title = "";

			if (saveExportFile == ExportMode.CSV) {
				title = "Save CSV file";
				fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[] {
						"csv"
				}, new String("Exported CSV file (*.csv)")));
			}
			else if (saveExportFile == ExportMode.EXCEL) {
				title = "Save excel file";
				fileChooser.setFileFilter(new CustomFileFilter(new String[]{"*.*"}, new String("All Files (*.*)")));
				fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"xls"},new String("Excel spreadsheet (*.xls)")));
			}
			else if (saveExportFile == ExportMode.XML_DATABASE) {
				title = "Save XML file";
				fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"xml"},new String("XML Database file (*.xml)")));
			}
			else if (saveExportFile == ExportMode.XML) {
				title = "Save XML file";
				fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[]{"xml"},new String("XML file (*.xml)")));
			}
			
			fileChooser.setDialogTitle(title);
			fileChooser.setApproveButtonText("Select");
			fileChooser.setApproveButtonToolTipText("Select file");
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setApproveFileSelection(false);    		
			
    		int returnVal = fileChooser.showDialog(this, "Save output file");

    		if (returnVal != JFileChooser.APPROVE_OPTION) {
    			log.warn("Failed to retrieve output file");
    		}
    		else {
    			try {
    				String outputFile = fileChooser.getSelectedFile().getCanonicalPath();
    				    				
    				if (!outputFile.toLowerCase().endsWith(extension))
    					outputFile += extension;

    				settings.filePath = outputFile;
    				    				
    				if (saveExportFile == ExportMode.CSV)
    					csvFilePath.setText(outputFile);
    				else if (saveExportFile == ExportMode.EXCEL)
    					excelFilePath.setText(outputFile);
    				else if (saveExportFile == ExportMode.XML_DATABASE)
    					xmlDbFilePath.setText(outputFile);
    				else if (saveExportFile == ExportMode.XML)
    					xmlFilePath.setText(outputFile);
    				
    				return;
    			} catch (IOException e) {
    				log.warn("Failed to retrieve output file");
    			}
    		}
    	}
    	MovieManager.getDialog().getMoviesList().requestFocus(true);
    }
}
