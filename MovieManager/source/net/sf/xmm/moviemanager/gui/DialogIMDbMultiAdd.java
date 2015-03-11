/**
 * @(#)DialogIMDbMultiAdd.java
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandPlay;
import net.sf.xmm.moviemanager.gui.DialogAddMultipleMovies.Files;
import net.sf.xmm.moviemanager.http.HttpUtil.HTTPResult;
import net.sf.xmm.moviemanager.imdblib.IMDb;
import net.sf.xmm.moviemanager.imdblib.IMDbLib;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings.ImdbImportOption;
import net.sf.xmm.moviemanager.models.imdb.ModelIMDbSearchHit;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

public class DialogIMDbMultiAdd extends DialogIMDbImport {
    
	private static final long serialVersionUID = 9074815790929713958L;

	static Logger log = Logger.getLogger(DialogIMDbMultiAdd.class);
	
	JButton playMediaFiles;
	JTextArea fileLocation;
	
    boolean multiAddByFile = false;
    boolean addInfoToExistingMovie = false;
    boolean switchBetweenIMDBAndDatabase = false;
    
    Files multiAddFile = null;
    String imdbId = null;
    String addToThisList = null; 
    
    ImdbImportOption multiAddSelectOption = ImdbImportOption.displayList;
    
     
    public DialogIMDbMultiAdd(JDialog parent, ModelEntry modelEntry, String searchString, 
    		String year, String filename, Files multiAddFile, String _imdbId) {
    
    	super(parent, modelEntry, searchString, null);
    	
    	createMultiAddComponents();
    	
        imdbId = (_imdbId == null || _imdbId.equals("")) ? null : null; //$NON-NLS-1$
        this.multiAddFile = multiAddFile;
        this.multiAddByFile = true;
        
        setTitle(filename);
          
        setFileLocationContent();
        
        performSearch(searchString, year);
    }

    /**
     *  Override parent create component method
     */
    @Override
    void createDialogImportComponents() {
    	
    }
    
    void createMultiAddComponents() {

    	/*
    	if (switchBetweenIMDBAndDatabase) {

    		if (multiAddFile != null) {
    			JButton chooseBetweenImdbAndLocalDatabase = createChooseBetweenImdbAndLocalDatabaseButton();
    			multipleMovieButtons.add(chooseBetweenImdbAndLocalDatabase);
    		}
    	}
*/
    	addWithoutIMDBInfoButton = createAddWithoutIMDBInfoButton();
    	abortButton = createAbortButton();
    	playMediaFiles = createPlayButton();
    	    	
    	JPanel multipleMovieButtons = new JPanel();
    	multipleMovieButtons.add(addWithoutIMDBInfoButton);
    	multipleMovieButtons.add(playMediaFiles);
    	multipleMovieButtons.add(abortButton);

    	JPanel multiAddButtonsPanel = new JPanel();
    	multiAddButtonsPanel.setLayout(new BorderLayout());
    	multiAddButtonsPanel.add(createFileLocationPanel(), BorderLayout.CENTER);
    	multiAddButtonsPanel.add(multipleMovieButtons, BorderLayout.SOUTH);
    	
    	subclassButtons.setLayout(new BorderLayout());
    	subclassButtons.add(multiAddButtonsPanel, BorderLayout.CENTER);

    	getButtonSelect().setEnabled(false);
    	getButtonCancel().setText(Localizer.get("DialogIMDB.button.cancel.text.skip-movie")); //$NON-NLS-1$

    	pack();

    	Dimension dim = MovieManager.getConfig().getMultiAddIMDbDialogWindowSize();

    	if (dim != null && dim.height > 0 && dim.width > 0) {
    		setSize(dim);
    	}

    	setHotkeyModifiers();
    }
    
       
    JButton createChooseBetweenImdbAndLocalDatabaseButton() {
    	
    	/*This button choses between IMDB and local movie database*/
		final JButton chooseBetweenImdbAndLocalDatabase  = new JButton(Localizer.get("DialogIMDbMultiAdd.button.add-to-existing-movie.text")); //$NON-NLS-1$
		chooseBetweenImdbAndLocalDatabase.setToolTipText(Localizer.get("DialogIMDbMultiAdd.button.add-to-existing-movie.tooltip")); //$NON-NLS-1$
		chooseBetweenImdbAndLocalDatabase.setActionCommand("GetIMDBInfo - chooseBetweenImdbAndLocalDatabase"); //$NON-NLS-1$
		chooseBetweenImdbAndLocalDatabase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.debug("ActionPerformed: " + event.getActionCommand()); //$NON-NLS-1$

				if (addInfoToExistingMovie) {
					getPanelMoviesList().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogIMDB.panel-movie-list.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
					chooseBetweenImdbAndLocalDatabase.setText(Localizer.get("DialogIMDbMultiAdd.button.add-to-existing-movie.text")); //$NON-NLS-1$
					chooseBetweenImdbAndLocalDatabase.setToolTipText(Localizer.get("DialogIMDbMultiAdd.button.add-to-existing-movie.tooltip")); //$NON-NLS-1$
					addInfoToExistingMovie = false;
					executeSearchMultipleMovies();
				}

				else {
					executeEditExistingMovie(""); //$NON-NLS-1$
					chooseBetweenImdbAndLocalDatabase.setText(Localizer.get("DialogIMDbMultiAdd.button.search-on-IMDb.text")); //$NON-NLS-1$
					chooseBetweenImdbAndLocalDatabase.setToolTipText(Localizer.get("DialogIMDbMultiAdd.button.search-on-IMDb.tooltip")); //$NON-NLS-1$
					addInfoToExistingMovie = true;

					getPanelMoviesList().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogIMDB.panel-your-movie-list.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
				}
			}});
		
		return chooseBetweenImdbAndLocalDatabase;
    }
    
    
    JButton createPlayButton() {
    	
    	JButton playMediaFiles = new JButton("Play");
    	playMediaFiles.setActionCommand("DialogIMDbMultiAdd - Play"); //$NON-NLS-1$

    	playMediaFiles.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent event) {
    			log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$
    			
    			if (multiAddFile != null) {
    				
    				String [] files = multiAddFile.toStringArray();
    				
    				try {
    					MovieManagerCommandPlay.executePlay(files);
    				} catch (IOException e) {
    					log.warn("Exception:" + e.getMessage(), e);
    				} catch (InterruptedException e) {
    					log.warn("Exception:" + e.getMessage(), e);
    				}
    			}
    		}
    	});
    	
    	return playMediaFiles;
    }
    
    
    JPanel createFileLocationPanel() {
    	// Panel file location
    	JPanel fileLocationPanel = new JPanel();
    	fileLocationPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Movie parts "), BorderFactory.createEmptyBorder(0,3,3,3)));
    	fileLocationPanel.setLayout(new BorderLayout());
    	fileLocation = new JTextArea();
    	fileLocation.setEditable(false);
    	
    	JScrollPane fileLocaScroll = new JScrollPane(fileLocation);
    	
    	fileLocationPanel.add(fileLocaScroll, BorderLayout.CENTER);
    	fileLocation.addMouseListener(new MouseAdapter() {
    		public void mouseClicked(MouseEvent e) {
    			//handleFileLocationPopup(e);
    		}
    	});
    	return fileLocationPanel;
    }
    
    
    /**
     * Not yet fully implemented
     * @param e
     */
    void handleFileLocationPopup(MouseEvent e) {
    	
    	if (!GUIUtil.isRightMouseButton(e))
    		return;
    	
    	JPopupMenu fileLocationPopup = new JPopupMenu();
    	JMenuItem fileLocationItem = new JMenuItem("Open content folder");
    	fileLocationPopup.add(fileLocationItem);
    	
    	fileLocationPopup.show(fileLocation, e.getX(), e.getY());
    }
    
    
    
    void performSearch(String searchString, String year) {
		    	
    	try {
    		
    		getSearchField().setText(searchString);
    		    		
    		ArrayList<ModelIMDbSearchHit> hits = performSearch();
    		
    		int hitCount = hits.size();
    		int pos = -1;
    		
    		for (int i = 0; i < hitCount; i++) {

    			// Use the first hit with the matching date
    			if (pos == -1 && year != null && year.equals(hits.get(i).getDate())) {
    				pos = i;
    			}
    		}
    		    		
    		if (pos == -1)
    			pos = 0;
    		    		
    		getMoviesList().setSelectedIndex(pos);
    		    		
    	} catch (Exception e) {
    		executeErrorMessage(e);
    		setListModel(null);
    	}
    }


    /**
     * Checks if the movie list should be retrived from IMDB or the local movie Database
     */
    void executeSearchMultipleMovies() {
	    	
    	if (addInfoToExistingMovie) {
    		executeEditExistingMovie(getSearchField().getText());
    	}
    	else {
    		final DefaultListModel listModel = new DefaultListModel();
	
    		int setSelectedIndex = 0;
    		
    		try {
    			    			
    			IMDb imdb = IMDbLib.newIMDb(MovieManager.getConfig().getHttpSettings());
    			ArrayList<ModelIMDbSearchHit> hits = imdb.getSimpleMatches(getSearchField().getText());
    			        		
    			// Error
    			if (hits == null) {
    				HTTPResult res = imdb.getLastHTTPResult();
    				
    				if (res.getStatusCode() == HttpStatus.SC_REQUEST_TIMEOUT) {
    					listModel.addElement(new ModelIMDbSearchHit("Connection timed out...")); //$NON-NLS-1$
    				}
    			}
    			
    			for (int i = 0; i < hits.size(); i++) {
    				listModel.addElement(hits.get(i));
    			}

    		} catch (Exception e) {
    			executeErrorMessage(e);

    			e.printStackTrace();
    			dispose();
    		}
    		    		
    		if (listModel.getSize() == 0)
    			listModel.addElement(new ModelIMDbSearchHit(Localizer.get("DialogIMDB.list-element.messsage.no-hits-found"))); //$NON-NLS-1$
    		    		
    		    		
    		getMoviesList().setModel(listModel);
    		getMoviesList().setSelectedIndex(setSelectedIndex);
    		    		
    		// This delays the execution of requestFocusInWindow.
    		// The reason is to avoid that the actionlistener for the choose button 
    		// is invoked, which is would be if invokelater isn't used. (Experienced on Ubuntu).
    		SwingUtilities.invokeLater(new Runnable() {
    			public void run() {
    				getMoviesList().requestFocusInWindow();
    			}
    		});
    	}
    }
    
    
    
    /**
     * The MovieManagerCommandFilter gets the movielist from the database ordered by movie title
     * Then uses the searchstring to remove unwanted hits
     * The last boolean argument states if the filter is called from the main search or the IMDB search.
     * If called from the main search, it will take in consideration all the advanced search options.
     */
    void executeEditExistingMovie(String searchString) {

    	DefaultListModel listModel;

    	ArrayList<String> lists = new ArrayList<String>();
    	
    	if (addToThisList != null)
    		lists.add(addToThisList);
    	    	
    	ArrayList<ModelMovie> list = MovieManager.getIt().getDatabase().getMoviesList("Title", lists,  //$NON-NLS-1$
    			MovieManager.getConfig().getShowUnlistedEntries());
    	listModel = GUIUtil.toDefaultListModel(list);
    			
    	setListModel(listModel);
    }

    
    /**
     * Gets info...
     **/
    public void executeCommandSelect() {
    	    	
    	int index = getMoviesList().getSelectedIndex();
    	
    	/*
    	 * When adding the file info the an existing movie, a new ModelMovieInfo object is created. 
    	 * When done, the old ModelMovieInfo object created in the 
    	 * MovieManagerCommandAddMultipleMovies object needs not to save the file as a new movie,
    	 * therefore setCAncel method with true is called at the end of the if scoop.
    	 */	

    	DefaultListModel listModel = (DefaultListModel) getMoviesList().getModel();

    	if (index == -1 || index > listModel.size())
    		return;

    	if (addInfoToExistingMovie) {

    		ModelMovie model = ((ModelMovie) listModel.getElementAt(index));

    		if (model.getKey() == -1)
    			return;

    		if (!model.getHasAdditionalInfoData()) {
    			model.updateAdditionalInfoData();
    		}

    		ModelMovieInfo modelInfoTmp = new ModelMovieInfo(model, false);

    		/* Need to set the hasReadProperties variable because when normally 
             calling the getfileinfo the first time it replaces the old additional values with the new ones
             Then the second time it plusses the time and size to match.
             When multiadding the next file info should be directly added to the old, not replace it
    		 */
    		
    		modelInfoTmp._hasReadProperties = true;
    		try {
				modelInfoTmp.getFileInfo(new File[] {multiAddFile.getFile()});
			} catch (Exception e) {
				log.error("Error occured while retrieving file info.", e); //$NON-NLS-1$
			}

    		try {
    			modelInfoTmp.saveToDatabase(null);
    		} catch (Exception e) {
    			log.error("Saving to database failed.", e); //$NON-NLS-1$
    		}

    		setCanceled(true);
    		dispose();
    	}
    	else {
    		ModelIMDbSearchHit model = ((ModelIMDbSearchHit) listModel.getElementAt(index));
	    		
    		if (model.getUrlID() == null)
    			return;

    		if (getUrlKeyOnly) {
    			modelEntry.setUrlKey(model.getUrlID());
    			dispose();
    			return;
    		}

    		if (multiAddSelectOption == ImdbImportOption.selectFirst && imdbId != null && !imdbId.equals("")) //$NON-NLS-1$
				// Use previously fetched imdb id
				getIMDbInfo(modelEntry, imdbId);
			else
				getIMDbInfo(modelEntry, model.getUrlID());

			ModelMovieInfo.executeTitleModification(modelEntry);
			
			dispose();
    	}
    }

    /**
     * Takes all the media files in a Files object and generated a string 
     * listing all the files categorized by parent directory.
     */
    void setFileLocationContent() {
        
      	if (multiAddFile != null && multiAddFile.getFile() != null) {
      		
      		int height = 0;
      		
      		ArrayList<Files> files = multiAddFile.getFiles();
      		String str = "";
      		
      		while (files.size() > 0) {
      			      			
      			String parent = files.get(0).getParent();
      			
      			if (str.length() > 0)
      				str += SysUtil.getLineSeparator();
      				
      			// Show directory of the file(s)
      			str += parent + SysUtil.getDirSeparator();
      			height++;
      			
      			int fileNumber = 1;
      			
      			for (int i = 0; i < files.size(); i++) {
          			      				
      				// Find all files in the current parent
      				if (files.get(i).getParent().equals(parent)) {
      					height++;
      					str += SysUtil.getLineSeparator() + String.format("  %-3d - %s", fileNumber++, files.get(i).getName());
      					files.remove(i);
      					i--;
      				}
      			}
      		}
      		fileLocation.setText(str);
      		fileLocation.setRows(height);
      	}
    }
       
    
    private void setHotkeyModifiers() {
    	    	
    	try {
			// ALT+P for Play
    		shortcutManager.registerKeyboardShortcut(
    				KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyboardShortcutManager.getToolbarShortcutMask()),
    				"Play file", new AbstractAction() {
    					public void actionPerformed(ActionEvent ae) {
    						playMediaFiles.doClick();
    					}
    				}, playMediaFiles);

    	} catch (Exception e) {
    		log.warn("Exception:" + e.getMessage(), e);
		}
    }
}
