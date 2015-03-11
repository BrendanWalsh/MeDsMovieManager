/**
 * @(#)DialogFolders.java
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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.CommandDialogDispose;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.database.DatabaseAccess;
import net.sf.xmm.moviemanager.database.DatabaseHSQL;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.log4j.Logger;

public class DialogFolders extends JDialog implements ItemListener, DocumentListener {
    
	Logger log = Logger.getLogger(getClass());
    
    private JCheckBox setPermanentDatabase;
    
    private JRadioButton relativeQueriesProgram;
    private JRadioButton relativeCoversProgram;
    private JRadioButton relativeDatabaseProgram;
    private JRadioButton relativeQueriesDatabase;
    private JRadioButton relativeCoversDatabase;
    
    private JCheckBox relativeQueriesEnabled;
    private JCheckBox relativeCoversEnabled;
    private JCheckBox relativeDatabaseEnabled;
    
    private JLabel optionQueries;
    private JLabel optionCovers;
    private JLabel optionDatabase;
    
    private JTextField textFieldCovers;
    private JTextField textFieldQueries;
    private JTextField textFieldLoadDatabase;
    private JTextField textFieldDatabase;
    private JTextField textFieldConfigLocation;
    
    JButton buttonSave;
    JButton buttonCancel;
    
    JPanel panelFolders;
    
    KeyboardShortcutManager shortcutManager = new KeyboardShortcutManager(this);
    
    private Color invalidPathColor = MovieManager.getConfig().getInvalidPathColor();
    
  
    /**
     * The Constructor.
     **/
    public DialogFolders() {
        /* Dialog creation...*/
        super(MovieManager.getDialog());
        
        GUIUtil.enableDisposeOnEscapeKey(shortcutManager);
        
        /* Dialog properties...*/
        setTitle(Localizer.get("DialogFolders.title")); //$NON-NLS-1$
        setModal(true);
        setResizable(false);
        
        createComponents();
        loadConfigData();
    }
    
    void loadConfigData() {
        
    	textFieldCovers.setText(MovieManager.getConfig().getCoversFolder());
    	textFieldQueries.setText(MovieManager.getConfig().getQueriesFolder());
    	textFieldDatabase.setText(MovieManager.getConfig().getDatabasePath(true));
    	textFieldLoadDatabase.setText(MovieManager.getConfig().getDatabasePath(!MovieManager.getConfig().getDatabasePathPermanent()));
    	
    	boolean isMySQL = MovieManager.getIt().getDatabase().isMySQL();

    	relativeDatabaseProgram.setSelected(!isMySQL);
    	relativeDatabaseEnabled.setEnabled(!isMySQL);
             
    	
        if (MovieManager.getConfig().getDatabasePathPermanent())
            setPermanentDatabase.setSelected(true);
        
        if (MovieManager.isApplet())
            setPermanentDatabase.setEnabled(false);
    	
        
        try {
			textFieldConfigLocation.setText(SysUtil.getConfigDir().getAbsolutePath() + SysUtil.getDirSeparator());
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e); //$NON-NLS-1$
		}
                

        int coverOption = MovieManager.getConfig().getUseRelativeCoversPath();
        
        if (coverOption != 0) {
            relativeCoversEnabled.setSelected(true);
            
            if (coverOption == 1)
                relativeCoversDatabase.setSelected(true);
            else
                relativeCoversProgram.setSelected(true);
        }
        else {
        	relativeCoversDatabase.setEnabled(false);
        	relativeCoversProgram.setEnabled(false);
        }
        
        int queriesOption = MovieManager.getConfig().getUseRelativeQueriesPath();
        
        if (queriesOption != 0) {
            relativeQueriesEnabled.setSelected(true);
            
            if (queriesOption == 1)
                relativeQueriesDatabase.setSelected(true);
            else
                relativeQueriesProgram.setSelected(true);
        }
        else {
        	relativeQueriesDatabase.setEnabled(false);
        	relativeQueriesProgram.setEnabled(false);
        }
        
        int databaseOption = MovieManager.getConfig().getUseRelativeDatabasePath();
        
        // Not absolute database
        if (databaseOption != 0) {
            relativeDatabaseEnabled.setSelected(true);
            relativeDatabaseProgram.setSelected(true);
        }
        
    }
    
    
    void createComponents() {
        
        boolean isMySQL = MovieManager.getIt().getDatabase().isMySQL();
    	
        /* Folders panel...*/
        panelFolders = new JPanel();
        panelFolders.setBorder(BorderFactory.createEmptyBorder(5,-3,0,-3));
        panelFolders.setLayout(new GridBagLayout());
        GridBagConstraints constraints;
        
        /*Covers*/
        JLabel labelCovers = new JLabel(Localizer.get("DialogFolders.label-covers")); //$NON-NLS-1$
        labelCovers.setFont(new Font(labelCovers.getFont().getName(),Font.PLAIN,labelCovers.getFont().getSize()));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(5,5,5,5);
        constraints.anchor = GridBagConstraints.WEST;
        panelFolders.add(labelCovers,constraints);
        
        textFieldCovers = new JTextField(30);
		constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.insets = new Insets(5,5,5,5);
        panelFolders.add(textFieldCovers,constraints);
        textFieldCovers.getDocument().addDocumentListener(this);
        		
        JButton buttonCovers = new JButton(Localizer.get("DialogFolders.browse-covers")); //$NON-NLS-1$
        buttonCovers.setToolTipText(Localizer.get("DialogFolders.browse-covers-tooltip")); //$NON-NLS-1$
        buttonCovers.setActionCommand("Folders - Browse Covers"); //$NON-NLS-1$
        buttonCovers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
                executeCommandBrowse(Localizer.get("DialogFolders.selectCoversDir")); //$NON-NLS-1$
                processPathValidation("Covers", relativeCoversEnabled.isSelected());
            }});
                 
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.insets = new Insets(5,5,5,5);
        panelFolders.add(buttonCovers,constraints); 
        
        /*Queries*/
        JLabel labelQueries = new JLabel(Localizer.get("DialogFolders.label-queries")); //$NON-NLS-1$
        labelQueries.setFont(new Font(labelQueries.getFont().getName(),Font.PLAIN,labelQueries.getFont().getSize()));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        
        constraints.insets = new Insets(5,5,5,5);
        constraints.anchor = GridBagConstraints.WEST;
        panelFolders.add(labelQueries,constraints);
        
        textFieldQueries = new JTextField(30);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.insets = new Insets(5,5,0,5);
        panelFolders.add(textFieldQueries,constraints);
        textFieldQueries.getDocument().addDocumentListener(this);
    
        JButton buttonQueries = new JButton(Localizer.get("DialogFolders.browse-queries")); //$NON-NLS-1$
        buttonQueries.setToolTipText(Localizer.get("DialogFolders.browse-queries-tooltip")); //$NON-NLS-1$
        buttonQueries.setActionCommand("Folders - Browse Queries"); //$NON-NLS-1$
        buttonQueries.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
                executeCommandBrowse(Localizer.get("DialogFolders.selectQueriesDir")); //$NON-NLS-1$
                processPathValidation("Queries", relativeQueriesEnabled.isSelected());
            }});
                
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 1;
        constraints.insets = new Insets(5,5,5,5);   
        panelFolders.add(buttonQueries,constraints);
        
        /* Database */
        JLabel labelDatabase = new JLabel(Localizer.get("DialogFolders.current-database")); //$NON-NLS-1$
        labelDatabase.setFont(new Font(labelDatabase.getFont().getName(),Font.PLAIN,labelDatabase.getFont().getSize()));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.insets = new Insets(5,5,5,5);
        constraints.anchor = GridBagConstraints.WEST;
        panelFolders.add(labelDatabase,constraints);
        
        textFieldDatabase = new JTextField(30);
        textFieldDatabase.setEditable(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.insets = new Insets(5,5,5,5);
        panelFolders.add(textFieldDatabase,constraints);
        
        
        
        JLabel labelDatabaseType = new JLabel();
        if (MovieManager.getIt().getDatabase() instanceof DatabaseAccess)
            labelDatabaseType.setText("  MS Access Database"); //$NON-NLS-1$
        else if (MovieManager.getIt().getDatabase() instanceof DatabaseHSQL)
            labelDatabaseType.setText("     HSQL Database"); //$NON-NLS-1$
        else if (isMySQL)
            labelDatabaseType.setText("     MySQL Database"); //$NON-NLS-1$
        else
            labelDatabaseType.setText(Localizer.get("DialogFolders.database-label")); //$NON-NLS-1$
        
        labelDatabaseType.setFont(new Font(labelDatabase.getFont().getName(),Font.BOLD,labelDatabase.getFont().getSize()));
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 3;
        constraints.insets = new Insets(5,5,5,5);
        constraints.anchor = GridBagConstraints.WEST;
        panelFolders.add(labelDatabaseType,constraints);
        
        
        
        JLabel labelLoadDatabase = new JLabel(Localizer.get("DialogFolders.load-database")); //$NON-NLS-1$
        labelLoadDatabase.setFont(new Font(labelLoadDatabase.getFont().getName(),Font.PLAIN, labelLoadDatabase.getFont().getSize()));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.insets = new Insets(5,5,5,5);
        constraints.anchor = GridBagConstraints.WEST;
        panelFolders.add(labelLoadDatabase, constraints);
        
        textFieldLoadDatabase = new JTextField(30);
        
        textFieldLoadDatabase.setEditable(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 4;
        constraints.insets = new Insets(5,5,5,5);
        panelFolders.add(textFieldLoadDatabase, constraints);
        
        setPermanentDatabase = new JCheckBox(Localizer.get("DialogFolders.set-permanent")); //$NON-NLS-1$
        setPermanentDatabase.setToolTipText(Localizer.get("DialogFolders.set-permanent-tooltip")); //$NON-NLS-1$
        setPermanentDatabase.addItemListener(this);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 4;
        constraints.insets = new Insets(5,5,5,5);
        constraints.anchor = GridBagConstraints.WEST;
        panelFolders.add(setPermanentDatabase, constraints);
        
        
        /* Config location */
        
        JLabel labelConfigLocation = new JLabel("Config location"); //$NON-NLS-1$
        labelConfigLocation.setFont(new Font(labelConfigLocation.getFont().getName(),Font.PLAIN, labelConfigLocation.getFont().getSize()));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.insets = new Insets(5,5,5,5);
        constraints.anchor = GridBagConstraints.WEST;
        panelFolders.add(labelConfigLocation, constraints);
        
        textFieldConfigLocation = new JTextField(30);
        
        textFieldConfigLocation.setEditable(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.insets = new Insets(5,5,5,5);
        panelFolders.add(textFieldConfigLocation, constraints);
             
        
        
        /* All stuff together... */
        JPanel all = new JPanel();
        all.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        all.setLayout(new BoxLayout(all,BoxLayout.Y_AXIS));
        all.add(panelFolders);
        
        
        JPanel panelOptions = new JPanel(new GridBagLayout());
        panelOptions.setBorder(BorderFactory.createEmptyBorder(0,20,0,0));
        
        JLabel optionTitle = new JLabel(Localizer.get("DialogFolders.save-paths-relative-to")); //$NON-NLS-1$
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        // constraints.gridwidth = 3;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.WEST;
        panelOptions.add(optionTitle, constraints);
        
        optionCovers = new JLabel(Localizer.get("DialogFolders.covers")); //$NON-NLS-1$
        optionCovers.setEnabled(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.WEST;
        panelOptions.add(optionCovers, constraints);
        
        optionQueries = new JLabel(Localizer.get("DialogFolders.queries")); //$NON-NLS-1$
        optionQueries.setEnabled(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.WEST;
        panelOptions.add(optionQueries, constraints);
        
        optionDatabase = new JLabel(Localizer.get("DialogFolders.database")); //$NON-NLS-1$
        optionDatabase.setEnabled(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.WEST;
        panelOptions.add(optionDatabase, constraints);
        
        JLabel optionProgramLocation = new JLabel(Localizer.get("DialogFolders.program-location")); //$NON-NLS-1$
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.WEST;
        panelOptions.add(optionProgramLocation, constraints);
        
        relativeCoversProgram = new JRadioButton();
        relativeCoversProgram.setEnabled(false);
        relativeCoversProgram.setSelected(true);
        relativeCoversProgram.addItemListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.CENTER;
        panelOptions.add(relativeCoversProgram, constraints);
        
        relativeQueriesProgram = new JRadioButton();
        relativeQueriesProgram.setEnabled(false);
        relativeQueriesProgram.setSelected(true);
        relativeQueriesProgram.addItemListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.CENTER;
        panelOptions.add(relativeQueriesProgram, constraints);
        
        relativeDatabaseProgram = new JRadioButton();
        relativeDatabaseProgram.setEnabled(false);
        relativeDatabaseProgram.addItemListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 1;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.CENTER;
        panelOptions.add(relativeDatabaseProgram, constraints);
        
        JLabel optionDatabaseLocation = new JLabel(Localizer.get("DialogFolders.database-location")); //$NON-NLS-1$
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.WEST;
        panelOptions.add(optionDatabaseLocation, constraints);
        
        relativeCoversDatabase = new JRadioButton();
        relativeCoversDatabase.setEnabled(false);
        relativeCoversDatabase.addItemListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.CENTER;
        panelOptions.add(relativeCoversDatabase, constraints);
        
        relativeQueriesDatabase = new JRadioButton();
        relativeQueriesDatabase.setEnabled(false);
        relativeQueriesDatabase.addItemListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 2;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.CENTER;
        panelOptions.add(relativeQueriesDatabase, constraints);
        
        
        JLabel relativeEnable = new JLabel(Localizer.get("DialogFolders.enable")); //$NON-NLS-1$
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.WEST;
        panelOptions.add(relativeEnable, constraints);
        
        relativeCoversEnabled = new JCheckBox();
        relativeCoversEnabled.addItemListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.CENTER;
        panelOptions.add(relativeCoversEnabled, constraints);
        
        relativeQueriesEnabled = new JCheckBox();
        relativeQueriesEnabled.addItemListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 3;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.CENTER;
        panelOptions.add(relativeQueriesEnabled, constraints);
        
        relativeDatabaseEnabled = new JCheckBox();
        relativeDatabaseEnabled.addItemListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 3;
        constraints.insets = new Insets(2,3,2,3);
        constraints.anchor = GridBagConstraints.CENTER;
        panelOptions.add(relativeDatabaseEnabled, constraints);
       
        /* Setting up groups */
        
        ButtonGroup coverGroup = new ButtonGroup();
        coverGroup.add(relativeCoversProgram);
        coverGroup.add(relativeCoversDatabase);
        
        ButtonGroup queriesGroup = new ButtonGroup();
        queriesGroup.add(relativeQueriesProgram);
        queriesGroup.add(relativeQueriesDatabase);
        
        ButtonGroup databaseGroup = new ButtonGroup();
        databaseGroup.add(relativeDatabaseProgram);
        
        
        
        /* Buttons panel...*/
        JPanel panelButtons = new JPanel();
        panelButtons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        buttonSave = new JButton(Localizer.get("DialogFolders.save")); //$NON-NLS-1$
        buttonSave.setToolTipText(Localizer.get("DialogFolders.save-changes")); //$NON-NLS-1$
        buttonSave.setActionCommand("Folders - Save"); //$NON-NLS-1$
        buttonSave.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
                executeCommandSave();
            }});
        
        if (MovieManager.isApplet())
        	buttonSave.setEnabled(false);
        
        panelButtons.add(buttonSave);
        
        buttonCancel = new JButton(Localizer.get("DialogFolders.cancel")); //$NON-NLS-1$
        buttonCancel.setToolTipText(Localizer.get("DialogFolders.cancel-tooltip")); //$NON-NLS-1$
        buttonCancel.setActionCommand("Folders - Cancel"); //$NON-NLS-1$
        buttonCancel.addActionListener(new CommandDialogDispose(this));
        panelButtons.add(buttonCancel);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        bottomPanel.add(panelOptions, BorderLayout.WEST);
        bottomPanel.add(panelButtons, BorderLayout.PAGE_END);
        
        /* Adds all and buttonsPanel... */    
        getContentPane().add(all, BorderLayout.NORTH);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        
        processPathValidation("Covers&Queries", false); //$NON-NLS-1$
        
        setHotkeyModifiers();
        
        /* Packs and sets location... */
        pack();
        setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
                (int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
        
    }
    
    /**
     * Returns the text in the covers textfield.
     **/
    protected String getCoversPath() {
        return textFieldCovers.getText();
    }
    
    /**
     * Returns the text in the queries textfield.
     **/
    protected String getQueriesPath() {
        return textFieldQueries.getText();
    }
    
    /**
     * Returns the covers textfield.
     **/
    protected JTextField getCovers() {
        return textFieldCovers;
    }
    
    /**
     * Returns the queries textfield.
     **/
    protected JTextField getQueries() {
        return textFieldQueries;
    }
    
    /**
     * Saves and exits...
     **/
    private void executeCommandSave() {
    	/* Checks if the specified paths exist and if so sets the new folders... */

    	String coversPath = getCoversPath();
    	String queriesPath = getQueriesPath();

    	/* Relative covers path enabled */
    	if (relativeCoversEnabled.isSelected()) {

    		// Program location 
    		if (relativeCoversProgram.isSelected()) {

    			if (coversPath.indexOf(SysUtil.getUserDir()) != -1)
    				coversPath = coversPath.substring(SysUtil.getUserDir().length(), coversPath.length());

    			MovieManager.getConfig().setUseRelativeCoversPath(2);
    		}
    		// Database
    		else {
    			String dbPath = MovieManager.getConfig().getDatabasePath(true);
    			dbPath = dbPath.substring(0, dbPath.lastIndexOf(SysUtil.getDirSeparator()));

    			if (coversPath.indexOf(dbPath) != -1)
    				coversPath = coversPath.substring(dbPath.length(), coversPath.length());

    			MovieManager.getConfig().setUseRelativeCoversPath(1);
    		}
    	}
    	else {
    		MovieManager.getConfig().setUseRelativeCoversPath(0);
    	}



    	if (relativeQueriesEnabled.isSelected()) {

    		if (relativeQueriesProgram.isSelected()) {

    			if (queriesPath.indexOf(SysUtil.getUserDir()) != -1)
    				queriesPath = queriesPath.substring(SysUtil.getUserDir().length(), queriesPath.length());

    			MovieManager.getConfig().setUseRelativeQueriesPath(2);
    		}
    		else {
    			String dbPath = MovieManager.getConfig().getDatabasePath(true);
    			dbPath = dbPath.substring(0, dbPath.lastIndexOf(SysUtil.getDirSeparator()));

    			if (queriesPath.indexOf(dbPath) != -1)
    				queriesPath = queriesPath.substring(dbPath.length(), queriesPath.length());

    			MovieManager.getConfig().setUseRelativeQueriesPath(1);
    		}
    	}
    	else {
    		MovieManager.getConfig().setUseRelativeQueriesPath(0);
    	}

    	if (relativeDatabaseEnabled.isSelected()) {

    		String databasePath = MovieManager.getConfig().getDatabasePath(true);

    		if (databasePath.indexOf(SysUtil.getUserDir()) == -1) {
    			DialogAlert alert = new DialogAlert(this, Localizer.get("DialogFolders.alert.title"), Localizer.get("DialogFolders.alert.database-relative-install.message"));  //$NON-NLS-1$ //$NON-NLS-2$
    			GUIUtil.showAndWait(alert, true);
    			return;
    		}
    		else {
    			MovieManager.getConfig().setUseRelativeDatabasePath(2);
    		}
    	}
    	else {
    		MovieManager.getConfig().setUseRelativeDatabasePath(0);
    	}

    	if (setPermanentDatabase.isSelected()) {
    		MovieManager.getConfig().setDatabasePathPermanent(true);
    		MovieManager.getConfig().setDatabasePath(textFieldLoadDatabase.getText());
    	}
    	else
    		MovieManager.getConfig().setDatabasePathPermanent(false);

    	
    	String originalCover = MovieManager.getConfig().getCoversFolder();
    	
    	// Sets cover and queries directory
    	MovieManager.getConfig().setCoverAndQueriesPaths(coversPath, queriesPath);

    	// Covers must be reloaded in movie list
    	if (originalCover != null && !originalCover.equals(coversPath)) {
    		MovieManager.getDatabaseHandler().getNewDatabaseLoadedHandler().newDatabaseLoaded(this);
    	}
    	
    	MovieManagerCommandSelect.execute();
    	dispose();
    }



    public void itemStateChanged(ItemEvent event) {
        
        Object source = event.getItemSelectable();
        
        if (source.equals(setPermanentDatabase)) {
            
            String databaseType = MovieManager.getIt().getDatabase().getDatabaseType();
            textFieldLoadDatabase.setText(databaseType+ ">" +textFieldDatabase.getText()); //$NON-NLS-1$
        }
        
        boolean value = false;
        
        /* Enable/Disable relative paths  */
        
        if (source.equals(relativeQueriesEnabled)) {
            
            if (relativeQueriesEnabled.isSelected())
                value = true;
            else
                value = false;
            
            relativeQueriesProgram.setEnabled(value);
            optionQueries.setEnabled(value);
            
            if (MovieManager.getIt().getDatabase().isMySQL())
            	value = false;
            
            relativeQueriesDatabase.setEnabled(value);
            processPathValidation("Queries", true); //$NON-NLS-1$
        }
        
        if (source.equals(relativeCoversEnabled)) {
                    	
            if (relativeCoversEnabled.isSelected())
                value = true;
            else
                value = false;
            
            relativeCoversProgram.setEnabled(value);
            optionCovers.setEnabled(value);
            
            if (MovieManager.getIt().getDatabase().isMySQL())
            	value = false;
            	
            relativeCoversDatabase.setEnabled(value);
            
            processPathValidation("Covers", true); //$NON-NLS-1$
        }
        
        if (source.equals(relativeDatabaseEnabled)) {
            
            if (relativeDatabaseEnabled.isSelected())
                value = true;
            else
                value = false;
            
            relativeDatabaseProgram.setEnabled(value);
            optionDatabase.setEnabled(value);
        }
        
        
        if (source.equals(relativeCoversProgram)) {
            
            if (relativeCoversProgram.isSelected())
            	processPathValidation("Covers", false); //$NON-NLS-1$
        }
        
        if (source.equals(relativeCoversDatabase)) {
            
            if (relativeCoversDatabase.isSelected())
            	processPathValidation("Covers", false); //$NON-NLS-1$
        }
        
        if (source.equals(relativeQueriesProgram)) {
            
            if (relativeQueriesProgram.isSelected())
            	processPathValidation("Queries", false); //$NON-NLS-1$
        }
        
        if (source.equals(relativeQueriesDatabase)) {
            
            if (relativeQueriesDatabase.isSelected())
            	processPathValidation("Queries", false); //$NON-NLS-1$
        }
        
    }
  
     
    //Gives notification that an attribute or set of attributes changed.
    public void changedUpdate(DocumentEvent e) {
	}

    // Gives notification that there was an insert into the document. The range given by the DocumentEvent bounds the freshly inserted region.
    public void insertUpdate(DocumentEvent e) {

    	if (e.getDocument().equals(textFieldCovers.getDocument()))
    		processPathValidation("Covers", false); //$NON-NLS-1$
    	else
    		processPathValidation("Queries", false); //$NON-NLS-1$
    }
    	

//  Gives notification that a portion of the document has been removed. The range is given in terms of what the view last saw (that is, before updating sticky positions).
    public void removeUpdate(DocumentEvent e){

    	if (e.getDocument().equals(textFieldCovers.getDocument()))
    		processPathValidation("Covers", false); //$NON-NLS-1$
    	else
    		processPathValidation("Queries", false); //$NON-NLS-1$
	}

    

   
    
    
    public void processPathValidation(String option, boolean buttonEnabledChanged) {

    	String coversPath = getCoversPath();
    	String queriesPath = getQueriesPath();
	
    	try {
    		    		
    		if (option.indexOf("Covers") != -1) { //$NON-NLS-1$
    			pathValidation(textFieldCovers, relativeCoversEnabled, relativeCoversProgram, coversPath, buttonEnabledChanged);
    		}

    		if (option.indexOf("Queries") != -1) { //$NON-NLS-1$
    			pathValidation(textFieldQueries, relativeQueriesEnabled, relativeQueriesProgram, queriesPath, buttonEnabledChanged);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    
    void pathValidation(JTextField textField, JCheckBox relativePathEnabled, JRadioButton pathRelativeToProgram, String path, boolean buttonEnabledChanged) {
		
		/* Relative covers path enabled */
		if (relativePathEnabled.isSelected()) {

			String newPath = null;
			
			// Relative to program location
			if (pathRelativeToProgram.isSelected()) {

				if (new File(path).isDirectory() && (new File(path).getAbsolutePath().indexOf(SysUtil.getUserDir()) == -1)) {
					textField.setBackground(invalidPathColor);
					textField.setToolTipText(Localizer.get("DialogFolders.alert.folder-inside-install.message")); //$NON-NLS-1$
				}
				else if (!new File(path).isDirectory() && !(new File(SysUtil.getUserDir(), path)).isDirectory()) { //$NON-NLS-1$
					textField.setBackground(invalidPathColor);
					textField.setToolTipText(Localizer.get("DialogFolders.alert.folder-doesnt-exist.message")); //$NON-NLS-1$
				}
				else {
					textField.setBackground(Color.WHITE);
					textField.setToolTipText("Full path: " + new File(SysUtil.getUserDir(), path).getAbsolutePath());
				}
				    					
				if (buttonEnabledChanged && path.startsWith(SysUtil.getUserDir())) {
					newPath = path.substring(SysUtil.getUserDir().length(), path.length());
				}
				
			}
			// Relative to database
			else {
				String dbPath = MovieManager.getConfig().getDatabaseFolder(true);
    					
				if ((new File(path)).isDirectory()) {

					if (path.indexOf(dbPath) == -1) {
						textField.setBackground(invalidPathColor);
						textField.setToolTipText(Localizer.get("DialogFolders.alert.folder-relative-database")); //$NON-NLS-1$
					}
					else {
						textField.setBackground(Color.WHITE);
						textField.setToolTipText("Full path: " + path);
					}
				}
				else if (!(new File(dbPath, path)).isDirectory()) {
					textField.setBackground(invalidPathColor);
					textField.setToolTipText(Localizer.get("DialogFolders.alert.folder-doesnt-exist.message")); //$NON-NLS-1$
				}
				else {
					textField.setBackground(Color.WHITE);
					textField.setToolTipText("Full path: " + new File(dbPath, path).getAbsolutePath());
				}
				
				if (buttonEnabledChanged && path.startsWith(dbPath)) {
					newPath = path.substring(dbPath.length(), path.length());
				}
			}

			// Modifying the text. When covers enable is selected/deselected the text is modified if possible.
			if (buttonEnabledChanged && newPath != null) {
				textField.setText(newPath);
			}
		}
		// relativePathEnabled is deSelected
		else {
			File folder = new File(path);
			
			if (buttonEnabledChanged) {

				String prePath;

				if (pathRelativeToProgram.isSelected())
					prePath = SysUtil.getUserDir();
				else
					prePath = MovieManager.getConfig().getDatabaseFolder(true);

				if (!path.startsWith(prePath)) {

					File f = new File(prePath, path);

					if (f.isDirectory()) {
						folder = f;
						textField.setText(f.getAbsolutePath());
					}
				}
			}

			if (!folder.isDirectory()) {
				textField.setBackground(invalidPathColor);
				textField.setToolTipText(Localizer.get("DialogFolders.alert.folder-doesnt-exist.message")); //$NON-NLS-1$
			}
			else {
				textField.setBackground(Color.WHITE);
				textField.setToolTipText(null);
			}
		}
	}
    
    
    /**
     * Gets a folder and updates the textfield...
     **/
    private void executeCommandBrowse(String title) {
    	JTextField textField;
    	/* Gets the right JTextField. */
    	if (title.equals(Localizer.get("DialogFolders.selectCoversDir"))) { //$NON-NLS-1$
    		textField = getCovers();
    	} else {
    		textField = getQueries();
    	}

    	/*The Oyoaha theme wouldn't set the file name in the name texField so a contructor accepting current dir and selection mode takes care of that*/
    	ExtendedFileChooser fileChooser;

    	if (title.equals(Localizer.get("DialogFolders.selectCoversDir"))) //$NON-NLS-1$
    		fileChooser = new ExtendedFileChooser(textField.getText(), ExtendedFileChooser.DIRECTORIES_ONLY);
    	else
    		fileChooser = new ExtendedFileChooser(textField.getText(), ExtendedFileChooser.DIRECTORIES_ONLY);

    	fileChooser.setDialogTitle(title);
    	fileChooser.setApproveButtonText(Localizer.get("DialogFolders.fileChooser.approve.text")); //$NON-NLS-1$
    	fileChooser.setApproveButtonToolTipText(Localizer.get("DialogFolders.filechooser.approve.tooltip")); //$NON-NLS-1$
    	fileChooser.setAcceptAllFileFilterUsed(false);

    	int returnVal = fileChooser.showOpenDialog(this);
    	if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {

    		/* Verifies that it's a directory... */
    		if (fileChooser.getSelectedFile() != null && fileChooser.getSelectedFile().isDirectory()) {
    			/* Sets the new dir... */
    			textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
    		}
    	}
    }
    
    
    void setHotkeyModifiers() {
    	
    	try {
			// ALT+S for Save and close
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Save & Close", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					buttonSave.doClick();
				}
			}, buttonSave);
			
			
			// ALT+C for Cancel
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyboardShortcutManager.getToolbarShortcutMask()), 
					"Cancel", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					buttonCancel.doClick();
				}
			}, buttonCancel);
		
			shortcutManager.setKeysToolTipComponent(panelFolders);
			
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
	}
}
