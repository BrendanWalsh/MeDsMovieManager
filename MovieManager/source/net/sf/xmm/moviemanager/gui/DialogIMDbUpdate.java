/**
 * @(#)DialogIMDbUpdate.java
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

import javax.swing.*;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.util.Localizer;
import org.apache.log4j.Logger;

public class DialogIMDbUpdate extends DialogIMDB {
    
	private static final long serialVersionUID = 9074815790929713958L;

	static Logger log = Logger.getLogger(DialogIMDbUpdate.class);
	
	JButton abortButton;
	    
    boolean getUrlKeyOnly = false;
    public boolean aborted = false;
        
    
    public DialogIMDbUpdate(JDialog parent, ModelEntry modelEntry, String alternateTitle, boolean getUrlKeyOnly) {
    	this(parent, modelEntry, alternateTitle, null, getUrlKeyOnly);
    }
    
    
    public DialogIMDbUpdate(ModelEntry modelEntry, String alternateTitle, boolean getUrlKeyOnly) {
    	this(modelEntry, alternateTitle, null, getUrlKeyOnly);
    }
    
    // Used by DialogIMDbMultiAdd
    public DialogIMDbUpdate(JDialog parent, ModelEntry modelEntry, String alternateTitle, ArrayList<ModelIMDbSearchHit> hits, boolean getUrlKeyOnly) {
    	super(parent, modelEntry, alternateTitle, false);
    	setup(modelEntry, alternateTitle, hits, getUrlKeyOnly);
    }
    
    // Used by DialogIMDbMultiAdd
    public DialogIMDbUpdate(ModelEntry modelEntry, String alternateTitle, ArrayList<ModelIMDbSearchHit> hits, boolean getUrlKeyOnly) {
    	super(modelEntry, alternateTitle, false);
    	setup(modelEntry, alternateTitle, hits, getUrlKeyOnly);
    }
        
    void setup(ModelEntry modelEntry, String alternateTitle, ArrayList<ModelIMDbSearchHit> hits, boolean getUrlKeyOnly) {
    	
    	if (alternateTitle != null)
    		setTitle(alternateTitle);

    	this.getUrlKeyOnly = getUrlKeyOnly;

    	createDialogUpdateComponents();

    	getSearchField().setText(alternateTitle);
    }
    
    
    void createDialogUpdateComponents() {
    	
    	JPanel multipleMovieButtons = new JPanel();
    	multipleMovieButtons.setLayout(new FlowLayout());

    	abortButton = createAbortButton();
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

        
    JButton createAbortButton() {
    	JButton abortButton = new JButton(Localizer.get("DialogIMDbMultiAdd.button.abort.text")); //$NON-NLS-1$
    	abortButton.setToolTipText(Localizer.get("DialogIMDbMultiAdd.button.abort.tooltip")); //$NON-NLS-1$
    	abortButton.setActionCommand("GetIMDBInfo - abort"); //$NON-NLS-1$

    	abortButton.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {
    			log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$
    			aborted = true;
    			dispose();
    		}});

    	return abortButton;
    }
        
    public boolean getAborted() {
    	return aborted;
    }
    
    public void resetFeedbackValues() {
    	setCanceled(false);
    	aborted = false;
    }
    
    
    public void dispose() {
    	MovieManager.getConfig().setMultiAddIMDbDialogWindowSize(getSize());
    	super.dispose();
    }
    
    
    /**
     * Overrides super class method to handel getUrlKeyOnly
     **/
    @Override
    public void executeCommandSelect() {
    	    	
    	int index = getMoviesList().getSelectedIndex();
    	
    	DefaultListModel listModel = (DefaultListModel) getMoviesList().getModel();

    	if (index == -1 || index > listModel.size())
    		return;

    	ModelIMDbSearchHit model = ((ModelIMDbSearchHit) listModel.getElementAt(index));
    	    	
    	if (model.getUrlID() == null)
    		return;

    	if (getUrlKeyOnly) {
    		modelEntry.setUrlKey(model.getUrlID());
    		dispose();
    		return;
    	}

    	getIMDbInfo(modelEntry, model.getUrlID());
    	ModelMovieInfo.executeTitleModification(modelEntry);

    	dispose();
    }
    
    
    private void setHotkeyModifiers() {
    	    	
    	try {
			// ALT+A for abort
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Abort import", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					abortButton.doClick();
				}
			}, abortButton);
					
			// ALT+L for list focus
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyboardShortcutManager.getToolbarShortcutMask()),
					"List Focus", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					getMoviesList().requestFocusInWindow();
				}
			});
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
    }
}
