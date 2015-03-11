/**
 * @(#)DialogIMDbImport.java
 *
 * Copyright (2010) Bro
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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;

import org.apache.log4j.Logger;

public class DialogIMDbImport extends DialogIMDbUpdate {
    
	private static final long serialVersionUID = 9074815790929713958L;

	static Logger log = Logger.getLogger(DialogIMDbImport.class);
	
	protected JButton addWithoutIMDBInfoButton;
        
    public boolean dropImdbInfoSet = false;
            
    public DialogIMDbImport(JDialog parent, ModelEntry modelEntry, String alternateTitle, ArrayList<ModelIMDbSearchHit> hits) {
    	super(parent, modelEntry, alternateTitle, false);
    	setup(modelEntry, alternateTitle, hits);
    }
      
    public DialogIMDbImport(ModelEntry modelEntry, String alternateTitle, ArrayList<ModelIMDbSearchHit> hits) {
    	super(modelEntry, alternateTitle, false);
    	setup(modelEntry, alternateTitle, hits);
    }
    
    void setup(ModelEntry modelEntry, String alternateTitle, ArrayList<ModelIMDbSearchHit> hits) {
    	
    	if (alternateTitle != null)
    		setTitle(alternateTitle);

    	createDialogImportComponents();

    	getSearchField().setText(alternateTitle);
    
    	if (hits != null)
    		handleSearchResults(hits);
    	else
    		super.executeSearch();
    }
    
    // May not want to execute method in parent, therefore override since it'll be called by the DialogIMDb constructor,
    @Override
    public void callSearch() {
    	
    }
    
    // Override parent create component method
    @Override
    public void createDialogUpdateComponents() {}
    
    
    /**
     * Creates the components for the Import dialog
     */
    void createDialogImportComponents() {
    	
    	JPanel multipleMovieButtons = new JPanel();
    	multipleMovieButtons.setLayout(new FlowLayout());

    	abortButton = createAbortButton();
    	addWithoutIMDBInfoButton = createAddWithoutIMDBInfoButton();
    	multipleMovieButtons.add(addWithoutIMDBInfoButton);
    	multipleMovieButtons.add(abortButton);
    	    	    	
    	subclassButtons.add(multipleMovieButtons, BorderLayout.CENTER);
    	
    	getButtonCancel().setText(Localizer.get("DialogIMDB.button.cancel.text.skip-movie")); //$NON-NLS-1$
        
    	pack();
    	
    	Dimension dim = MovieManager.getConfig().getMultiAddIMDbDialogWindowSize();
    	
    	if (dim != null && dim.height > 0 && dim.width > 0) {
    		setSize(dim);
    	}

    	setHotkeyModifiers();
    }


    JButton createAddWithoutIMDBInfoButton() {

    	JButton addWithoutIMDBInfo = new JButton(Localizer.get("DialogIMDbMultiAdd.button.add-without-web-info.text")); //$NON-NLS-1$
    	addWithoutIMDBInfo.setToolTipText(Localizer.get("DialogIMDbMultiAdd.button.add-without-web-info.tooltip")); //$NON-NLS-1$
    	addWithoutIMDBInfo.setActionCommand("GetIMDBInfo - addWithoutIMDBInfo"); //$NON-NLS-1$

    	final DialogIMDbImport thisDialog = this;
    	
    	addWithoutIMDBInfo.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {

    			log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$

    			if (getSearchField().getText().trim().equals("")) { //$NON-NLS-1$
    				DialogAlert alert = new DialogAlert(thisDialog, Localizer.get("DialogIMDbMultiAdd.alert.no-title-specified.title"), "<html>" + Localizer.get("DialogIMDbMultiAdd.alert.no-title-specified.message") + "</html>", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    				GUIUtil.show(alert, true);
    				return;
    			}

    			modelEntry.setTitle(getSearchField().getText());

    			dropImdbInfoSet = true;
    			dispose();
    			return;
    		}});

    	return addWithoutIMDBInfo;
    }
    
    public boolean getDropIMDbInfo() {
    	return dropImdbInfoSet;
    }
    
    public void resetFeedbackValues() {
    	super.resetFeedbackValues();
    	dropImdbInfoSet = false;
    }
    
       
    private void setHotkeyModifiers() {
    	    	    	
    	try {
			// ALT+W for add without IMDb info
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Add without IMDb info", new AbstractAction() {
						public void actionPerformed(ActionEvent ae) {
							addWithoutIMDBInfoButton.doClick();
						}
					}, addWithoutIMDBInfoButton);
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
    }
}
