 /**
 * @(#)DefaultMenuBar.java
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

package net.sf.xmm.moviemanager.gui.menubar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.plaf.ColorUIResource;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.MovieManagerConfig.InternalConfig;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandAddMultipleMoviesByFile;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandCloseDatabase;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandConvertDatabase;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandExit;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandFilter;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandOpenPage;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandAbout;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandAdditionalInfoFields;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandFolders;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandLists;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandNew;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandOpen;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandPrefs;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandQueries;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandReportGenerator;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandUpdateIMDBInfo;
import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandExport;
import net.sf.xmm.moviemanager.commands.importexport.MovieManagerCommandImport;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplate;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplateStyle;
import net.sf.xmm.moviemanager.swing.extentions.JMultiLineToolTip;
import net.sf.xmm.moviemanager.updater.AppUpdater;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.events.NewDatabaseLoadedEvent;
import net.sf.xmm.moviemanager.util.events.NewDatabaseLoadedEventListener;
import net.sf.xmm.moviemanager.util.events.UpdatesAvailableEvent;
import net.sf.xmm.moviemanager.util.events.UpdatesAvailableEventListener;


public class DefaultMenuBar extends JMenuBar implements MovieManagerMenuBar {

	Logger log = Logger.getLogger(getClass());

	JMenu menuFile = null;
	JMenu menuDatabase = null;
	JMenu menuTools = null;
	MenuLists menuLists = null;
	JMenu menuView = null;
	JMenu menuHelp = null;
	JMenu menuUpdate = null;

	JMenuItem menuItemNew = null;
	JMenuItem menuItemOpen = null;
	JMenuItem menuItemClose = null;
	JMenuItem menuImport = null;
	JMenuItem menuExport = null;
	JMenuItem menuItemExit = null;

	JMenuItem menuItemQueries = null;
	JMenuItem menuItemFolders = null;
	JMenuItem menuItemAddField = null;
	JMenuItem menuItemAddList = null;
	JMenuItem menuItemConvertDatabase = null;

	JMenuItem menuItemPrefs = null;
	JMenuItem menuItemAddMultipleMovies = null;
	JMenuItem menuItemUpdateIMDbInfo = null;
	JMenuItem menuItemReportGenerator = null;

	JMenuItem menuItemAbout = null;
	
	
	InternalConfig internalConfig;
	MovieManagerConfig config;
	
	
	public JMenu getMenuFile() {
        return menuFile;
    }
    
	public JMenu getMenuDatabase() {
        return menuDatabase;
    }
    
	public JMenu getMenuTools() {
        return menuTools;
    }
    
	public JMenu getMenuLists() {
        return menuLists;
    }
    
	public JMenu getMenuView() {
        return menuView;
    }
    
	public JMenu getMenuHelp() {
        return menuHelp;
    }
    
	public JMenu getMenuUpdate() {
        return menuUpdate;
    }
	
	public JMenuBar getNewInstance(InternalConfig internalConfig, MovieManagerConfig config) {
		this.internalConfig = internalConfig;
		this.config = config;
		return createMenuBar();
	} 
	
	public DefaultMenuBar(InternalConfig internalConfig, MovieManagerConfig config) {
		this.internalConfig = internalConfig;
		this.config = config;
		createMenuBar();
	} 
		
	
	/**
	 * Creates the menuBar.
	 *
	 * @return The menubar.
	 **/
	protected JMenuBar createMenuBar() {
		log.debug("Start creation of the MenuBar."); //$NON-NLS-1$
		//JMenuBar menuBar = new JMenuBar();
		JMenuBar menuBar = this;
		
		menuBar.setBorder(BorderFactory.createEmptyBorder(2,0,8,0));
		/* Creation of the file menu. */
		menuBar.add(createMenuFile());

		menuBar.add(createMenuDatabase());
		/* Creation of the options menu. */
		menuBar.add(createMenuTools());
		menuBar.add(createMenuLists());

		if (!MovieManager.isApplet())
			menuBar.add(createMenuView());

		/* Creation of the help menu. */
		menuBar.add(createMenuHelp());

		AppUpdater.updatesAvailableHandler.addUpdatesAvailableEventListener(new UpdatesAvailableEventListener() {
			public void updatesAvailableEvent(UpdatesAvailableEvent evt) {
				newVersionAvailable();
			}
		});
		
		log.debug("Creation of the MenuBar done."); //$NON-NLS-1$
		return menuBar;
	}

	public void newVersionAvailable() {
		final JMenuBar menuBar = MovieManager.getDialog().getJMenuBar();

		log.debug("Updates are available!");

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				menuUpdate = createMenuUpdate();
				menuBar.add(menuUpdate);
				menuBar.updateUI();
			}
		});
	}
	
	
	/**
	 * Creates the update menu.
	 *
	 * @return The update menu.
	 **/
	protected JMenu createMenuUpdate() {
		log.debug("Start creation of the Update menu."); //$NON-NLS-1$

		JMenu mUpdate = new JMenu("Update Available"); //$NON-NLS-1$
		
		/*
		Font f = mUpdate.getFont();
		Map map = f.getAttributes();
		
		// Set bold text
		map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_EXTRABOLD);
		
		Set<TextAttribute> keys = map.keySet();
				
		
		  Object o = map.get(TextAttribute.SIZE);
		Float fontSize = 0.0f;
		if (o instanceof Float)
			fontSize = (Float) o;
		
		//fontSize = 12.0f;
		map.put(TextAttribute.SIZE, fontSize);
	
		
		mUpdate.setFont(new Font(map));
			*/
		Object color = UIManager.get("Menu.selectionBackground");
		
		Color defaultBackgroundColor = null;
		
		if (color != null && color instanceof ColorUIResource) {
			defaultBackgroundColor = (ColorUIResource) color;
		}
				
		final Color finalBackgroundColor = defaultBackgroundColor;
				
		mUpdate.setMinimumSize(new Dimension(120, MovieManager.getDialog().getSize().height));
				
		class MyMenuItemUI extends javax.swing.plaf.basic.BasicMenuItemUI {

			public MyMenuItemUI() {
				super();
				
				if (finalBackgroundColor != null) {
					selectionBackground = finalBackgroundColor;
					selectionForeground = Color.WHITE;
				}
			}			
			
			public Dimension getMaximumSize(JComponent c) {
				return c.getPreferredSize();
			}
		}	

		mUpdate.setUI(new MyMenuItemUI());

		mUpdate.setBackground(finalBackgroundColor);	
		mUpdate.setForeground(Color.WHITE);
		
		mUpdate.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						menuUpdate.setPopupMenuVisible(!menuUpdate.isPopupMenuVisible());
					}
				});				
			}

			public void mouseEntered(MouseEvent e) {
				if (finalBackgroundColor != null) {
					menuUpdate.setBackground(finalBackgroundColor);
				}
			}
		});

		mUpdate.addMenuListener(new MenuListener() {
			
			public void menuSelected(MenuEvent e) {
				menuUpdate.doClick();
			}
			
			public void menuDeselected(MenuEvent e) {}
			public void menuCanceled(MenuEvent e) {}
		});
		
		/* MenuItem VersionInfo. */
		JMenuItem menuItemVersionInfo = new JMenuItem("Show available updates"); //$NON-NLS-1$
		mUpdate.add(menuItemVersionInfo);

		menuItemVersionInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				AppUpdater.showUpdateWindow();
			}
		});

		/* MenuItem topCheck. */
		JMenuItem menuItemStopCheck = new JMenuItem("Do not check for updates"); //$NON-NLS-1$
		mUpdate.add(menuItemStopCheck);

		menuItemStopCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				config.setCheckForProgramUpdates(false);
				JMenuBar menuBar = MovieManager.getDialog().getJMenuBar();
				menuBar.remove(menuUpdate);
				menuBar.updateUI();
			}
		});

		/* All done. */
		log.debug("Creation of the Help menu done."); //$NON-NLS-1$
		return mUpdate;
	}


	
	

	/**
	 * Creates the file menu.
	 *
	 * @return The file menu.
	 **/
	protected JMenu createMenuFile() {
		log.debug("Start creation of the File menu."); //$NON-NLS-1$
		menuFile = new JMenu(Localizer.get("DialogMovieManager.menu.file")); //$NON-NLS-1$
		menuFile.setMnemonic('F');

		/* MenuItem New. */
		menuItemNew = new JMenuItem(Localizer.get("DialogMovieManager.menu.file.newdb"),'N'); //$NON-NLS-1$
		menuItemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemNew.setActionCommand("New"); //$NON-NLS-1$
		menuItemNew.addActionListener(new MovieManagerCommandNew());

		menuFile.addSeparator();
		menuFile.add(menuItemNew);
		
		/* MenuItem Open. */
		menuItemOpen = new JMenuItem(Localizer.get("DialogMovieManager.menu.file.opendb"),'O'); //$NON-NLS-1$
		menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemOpen.setActionCommand("Open"); //$NON-NLS-1$
		menuItemOpen.addActionListener(new MovieManagerCommandOpen());

		menuFile.addSeparator();
		menuFile.add(menuItemOpen);
		
		/* MenuItem Close. */
		menuItemClose = new JMenuItem(Localizer.get("DialogMovieManager.menu.file.closedb"),'C'); //$NON-NLS-1$
		menuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
		menuItemClose.setActionCommand("Open"); //$NON-NLS-1$
		menuItemClose.addActionListener(new MovieManagerCommandCloseDatabase());

		menuFile.addSeparator();
		menuFile.add(menuItemClose);
		
		/* The Import menuItem. */
		menuImport = new JMenuItem(Localizer.get("DialogMovieManager.menu.file.import"),'I'); //$NON-NLS-1$
		menuImport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
		menuImport.addActionListener(new MovieManagerCommandImport());

		menuFile.addSeparator();
		menuFile.add(menuImport);
		
		/* The Export menuItem. */
		menuExport = new JMenuItem(Localizer.get("DialogMovieManager.menu.file.export"),'E'); //$NON-NLS-1$
		menuExport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuExport.addActionListener(new MovieManagerCommandExport());

		menuFile.addSeparator();
		menuFile.add(menuExport);
		
		/* MenuItem Exit. */
		menuItemExit = new JMenuItem(Localizer.get("DialogMovieManager.menu.file.quit"),'Q'); //$NON-NLS-1$
		menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemExit.setActionCommand("Quit"); //$NON-NLS-1$
		menuItemExit.addActionListener(new MovieManagerCommandExit());

		menuFile.addSeparator();
		menuFile.add(menuItemExit);
		
		/* All done. */
		log.debug("Creation of the File menu done."); //$NON-NLS-1$
		return menuFile;
	}


	/**
	 * Creates the database menu.
	 *
	 * @return The database menu.
	 **/
	protected JMenu createMenuDatabase() {
		log.debug("Start creation of the Database menu."); //$NON-NLS-1$
		menuDatabase = new JMenu(Localizer.get("DialogMovieManager.menu.database")); //$NON-NLS-1$
		menuDatabase.setMnemonic('D');

		/* MenuItem Queries. */
		menuItemQueries = new JMenuItem(Localizer.get("DialogMovieManager.menu.database.queries"),'Q'); //$NON-NLS-1$
		menuItemQueries.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
		menuItemQueries.setActionCommand("Queries"); //$NON-NLS-1$
		menuItemQueries.addActionListener(new MovieManagerCommandQueries());
		menuDatabase.add(menuItemQueries);

		/* A separator. */
		menuDatabase.addSeparator();

		/* MenuItem Folders. */
		menuItemFolders = new JMenuItem(Localizer.get("DialogMovieManager.menu.database.folders"),'F'); //$NON-NLS-1$
		menuItemFolders.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemFolders.setActionCommand("Folders"); //$NON-NLS-1$
		menuItemFolders.addActionListener(new MovieManagerCommandFolders());
		menuDatabase.add(menuItemFolders);

		/* MenuItem AddField. */
		menuItemAddField = new JMenuItem(Localizer.get("DialogMovieManager.menu.database.additionalinfofields"),'I'); //$NON-NLS-1$
		menuItemAddField.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, (java.awt.event.InputEvent.ALT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
		menuItemAddField.setActionCommand("AdditionalInfoFields"); //$NON-NLS-1$
		menuItemAddField.addActionListener(new MovieManagerCommandAdditionalInfoFields());
		menuDatabase.add(menuItemAddField);

		/* MenuItem AddList. */
		menuItemAddList = new JMenuItem(Localizer.get("DialogMovieManager.menu.database.lists")); //$NON-NLS-1$
		menuItemAddList.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemAddList.setActionCommand("setLists"); //$NON-NLS-1$
		menuItemAddList.addActionListener(new MovieManagerCommandLists(MovieManager.getDialog()));
		menuDatabase.add(menuItemAddList);

		/* MenuItem Convert Database. */
		menuItemConvertDatabase = new JMenuItem(Localizer.get("DialogMovieManager.menu.database.covertdb")); //$NON-NLS-1$
		menuItemConvertDatabase.setActionCommand("Convert Database"); //$NON-NLS-1$
		menuItemConvertDatabase.addActionListener(new MovieManagerCommandConvertDatabase());
		menuItemConvertDatabase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		
		
		menuDatabase.add(menuItemConvertDatabase);

		// When a new database is loaded, update list title
        MovieManager.getDatabaseHandler().getNewDatabaseLoadedHandler().addNewDatabaseLoadedEventListener(new NewDatabaseLoadedEventListener() {
        	public void newDatabaseLoaded(NewDatabaseLoadedEvent evt) {
        		validateConvertDatabaseItem();
			}
        });
		
		/* All done. */
		log.debug("Creation of the Database menu done."); //$NON-NLS-1$
		return menuDatabase;
	}
	
	/**
	 * Validates if the convert item should be available
	 */
	public void validateConvertDatabaseItem() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				boolean enabled = false;

				// If Windows, always yes.
				if (SysUtil.isWindows()) {
					enabled = true;
					// If Running Linux or Mac, must use MySQL to be able to convert to HSQL
				} else if (MovieManager.getIt().getDatabase().isMySQL()) {
					enabled = true;
				}

				menuItemConvertDatabase.setEnabled(enabled);
			}
		});		
	}


	/**
	 * Creates the tools menu.
	 *
	 * @return The tools menu.
	 **/
	protected JMenu createMenuTools() {
		log.debug("Start creation of the Tools menu."); //$NON-NLS-1$
		menuTools = new JMenu(Localizer.get("DialogMovieManager.menu.tools")); //$NON-NLS-1$
		menuTools.setMnemonic('T');
	
		/* MenuItem Preferences.
	         For some reason, addMovie KeyEvent.VK_A doesn't work when focused
	         on the selected movie or the filter*/
		
		menuItemPrefs = new JMenuItem(Localizer.get("DialogMovieManager.menu.tools.preferences"),'P'); //$NON-NLS-1$
		menuItemPrefs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
		menuItemPrefs.setActionCommand("Preferences"); //$NON-NLS-1$
		menuItemPrefs.addActionListener(new MovieManagerCommandPrefs());
	
		menuTools.add(menuItemPrefs);
		
		JMenuItem menuItemAddMultipleMovies = new JMenuItem(Localizer.get("DialogMovieManager.menu.tools.addmultiplemovies"),'M'); //$NON-NLS-1$
		menuItemAddMultipleMovies.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemAddMultipleMovies.setActionCommand("Add Multiple Movies"); //$NON-NLS-1$
		menuItemAddMultipleMovies.addActionListener(new MovieManagerCommandAddMultipleMoviesByFile());

		menuTools.addSeparator();
		menuTools.add(menuItemAddMultipleMovies);
		this.menuItemAddMultipleMovies = menuItemAddMultipleMovies;
	
		JMenuItem menuItemUpdateIMDbInfo = new JMenuItem(Localizer.get("DialogMovieManager.menu.tools.updateIMDbInfo"),'U'); //$NON-NLS-1$
		menuItemUpdateIMDbInfo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemUpdateIMDbInfo.setActionCommand("Update IMDb Info"); //$NON-NLS-1$
		menuItemUpdateIMDbInfo.addActionListener(new MovieManagerCommandUpdateIMDBInfo());

		menuTools.add(menuItemUpdateIMDbInfo);
		this.menuItemUpdateIMDbInfo = menuItemUpdateIMDbInfo;
		
		JMenuItem menuItemReportGenerator = new JMenuItem(Localizer.get("DialogMovieManager.menu.tools.reportgenerator"),'R'); //$NON-NLS-1$
		menuItemReportGenerator.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItemReportGenerator.setActionCommand("Report Generator"); //$NON-NLS-1$
		menuItemReportGenerator.addActionListener(new MovieManagerCommandReportGenerator());

		menuTools.addSeparator();
		menuTools.add(menuItemReportGenerator);
		this.menuItemReportGenerator = menuItemReportGenerator;
		
		/* All done. */
		log.debug("Creation of the Tools menu done."); //$NON-NLS-1$
		return menuTools;
	}

	
	protected JMenu createMenuLists() {
		log.debug("Start creation of the Lists menu."); //$NON-NLS-1$
		menuLists = new MenuLists(Localizer.get("DialogMovieManager.menu.lists")); //$NON-NLS-1$
		menuLists.setMnemonic('L');
		
		log.debug("Creation of the Lists menu done."); //$NON-NLS-1$
		return menuLists;
	}

	/**
	 * @param listColumns 	a list containing all the lists in the database.
	 */
	public void loadDefaultMenuLists(final ArrayList<String> listColumns) {
		SwingUtilities.invokeLater(new Runnable() {
        	public void run() {
        		menuLists.loadDefaultMenuLists(listColumns);
        	}
        });
	}
	
	/**
	 * Handles the Lists menu. Loads the lists and manages the listeners
	 * @author Bro3
	 *
	 */
	class MenuLists extends JMenu implements MouseListener, ActionListener {

		Logger log = Logger.getLogger(getClass());
		
		ArrayList<JCheckBoxMenuItem> menuItemsList;
		
		JMenuItem showAll = null;
		JCheckBoxMenuItem showUnlisted = null;
				
		MenuLists(String name) {
			super(name);
		}

		
		public void loadDefaultMenuLists(ArrayList<String> listColumns) {

			JMenu menuLists = getMenuLists();

			if (menuLists != null) {

				ArrayList<String> currentLists = config.getCurrentLists();

				JCheckBoxMenuItem menuItem;

				menuLists.removeAll();

				menuItemsList = new ArrayList<JCheckBoxMenuItem>();
				
				// If no lists available, add shortcut for creating lists instead
				if (listColumns.size() == 0) {
					JMenuItem menuItemAddList = new JMenuItem("Adds lists"); //$NON-NLS-1$
					menuItemAddList.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
					menuItemAddList.addActionListener(new MovieManagerCommandLists(MovieManager.getDialog()));
					menuLists.add(menuItemAddList);
					return;	
				}

				boolean enableAllLists = false;
				
				// Cannot have everything deselected, therefore enable all
				if (currentLists.size() == 0 && !config.getShowUnlistedEntries()) {
					enableAllLists = true;
					config.setShowUnlistedEntries(true);
				}
				
				int indexCounter = 0;
				
				while (!listColumns.isEmpty()) {

					menuItem = new JCheckBoxMenuItem(listColumns.get(0));
					menuItem.setActionCommand((String) listColumns.get(0));
					menuItem.setToolTipText("Right click to uniquely select");
					
					menuItem.addActionListener(this);
					menuItem.addMouseListener(this);
					menuLists.add(menuItem);

					if (enableAllLists) {
						config.addToCurrentLists(listColumns.get(0));
					}
					
					if (currentLists.contains(listColumns.get(0)))
						menuItem.setSelected(true);

					listColumns.remove(0);
					indexCounter++;

					menuItemsList.add(menuItem);
				}
				menuLists.addSeparator();
		
				/* Adds 'Show Unlisted' in the list */
				showUnlisted = new JCheckBoxMenuItem("Show Unlisted"); //$NON-NLS-1$
				showUnlisted.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
				showUnlisted.setActionCommand("Show Unlisted"); //$NON-NLS-1$
				showUnlisted.setToolTipText("Right click to uniquely select");
				showUnlisted.addActionListener(this);
				showUnlisted.addMouseListener(this);
				menuLists.add(showUnlisted);
	
				showUnlisted.setSelected(config.getShowUnlistedEntries());
				
				menuLists.addSeparator();

				/* Adds 'Show all' in the list */
				showAll = new JMenuItem("Show All"); //$NON-NLS-1$
				showAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
				showAll.setActionCommand("Show All"); //$NON-NLS-1$
				showAll.addActionListener(this);
				
				menuLists.add(showAll);
			}
		}
		
		
		public void setMenuItemEnabled(JCheckBoxMenuItem checkBox, boolean b) {
						
			checkBox.setSelected(b);
			
			if (checkBox.isSelected())
				config.addToCurrentLists(checkBox.getText());
			else
				config.getCurrentLists().remove(checkBox.getText());
		}
		
		
		void execute(JMenuItem source, boolean exclusive) {
			
			String column = source.getText();
						
			// Either "show all" or "Show Unlisted"
			if (!menuItemsList.contains(source)) {
				
				if (column.equals("Show All")) {
					
					showUnlisted.setSelected(true);
					config.setShowUnlistedEntries(true);
					
					for (int i = 0; i < menuItemsList.size(); i++) {
						setMenuItemEnabled((JCheckBoxMenuItem) menuItemsList.get(i), true);
					}
				}
				else if (column.equals("Show Unlisted")) {
					
					if (!exclusive)
						config.setShowUnlistedEntries(source.isSelected());
					else {
						config.setShowUnlistedEntries(true);
						showUnlisted.setSelected(true);
						
						for (int i = 0; i < menuItemsList.size(); i++) {
							setMenuItemEnabled((JCheckBoxMenuItem) menuItemsList.get(i), false);
						}
					}
				}
			} // Any of the lists
			else {				
				
				if (!exclusive)
					setMenuItemEnabled((JCheckBoxMenuItem) source, source.isSelected());
				else {
										
					setMenuItemEnabled((JCheckBoxMenuItem) source, true);
					
					showUnlisted.setSelected(false);
					config.setShowUnlistedEntries(false);
					
					for (int i = 0; i < menuItemsList.size(); i++) {
						
						if (source != ((JCheckBoxMenuItem) menuItemsList.get(i)))
							setMenuItemEnabled((JCheckBoxMenuItem) menuItemsList.get(i), false);
					}
				}
			}
			
			boolean showNone = true; // None is if no lists or unlisted is chosen

			if (showUnlisted.isSelected())
				showNone = false;
			
			for (int i = 0; i < menuItemsList.size(); i++) {
				if (((JCheckBoxMenuItem) menuItemsList.get(i)).isSelected()) {
					showNone = false;
					break;
				}
			}
			
			// This is the same as showing all the lists and unlisted entries.
			if (showNone) {
				
				for (int i = 0; i < menuItemsList.size(); i++) {
					setMenuItemEnabled((JCheckBoxMenuItem) menuItemsList.get(i), true);
				}
				
				config.setShowUnlistedEntries(true);
				showUnlisted.setSelected(true);
			}
			
			MovieManager.getDialog().setListTitle();
				
			new MovieManagerCommandFilter(null, true, true).execute();
		}

		
		/*
		 * For some reason, KeyListener doesn't seem to work on the JMenuItems
		 */
		public void actionPerformed(final ActionEvent event) {
						
			if (event.getSource() instanceof JMenuItem) {
			
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						
						// Use global exclusive variable
						execute(((JMenuItem) event.getSource()), exclusive);
						exclusive = false;
					}
				});
			}
		}
		
		boolean exclusive = false;
		
		public void mouseReleased(MouseEvent event) {
						
			if (event.getSource() instanceof JMenuItem) {
				exclusive = SwingUtilities.isRightMouseButton(event);
			}
		}

		public void mouseClicked(MouseEvent event) {}
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}		
	}

	
	
	
	/**
	 * Creates the views menu.
	 *
	 * @return The views menu.
	 **/
	protected JMenu createMenuView() {

		log.debug("Start creation of the View menu."); //$NON-NLS-1$
		menuView = new JMenu("View"); //$NON-NLS-1$
		menuView.setMnemonic('V');

		final HashMap<String, ModelHTMLTemplate> templates = MovieManager.getTemplateHandler().getHTMLTemplates();
		String currentTemplateName = MovieManager.getConfig().getHTMLTemplateHandler().getHTMLTemplateName();
		String currentTemplateStyleName = MovieManager.getConfig().getHTMLTemplateHandler().getHTMLTemplateStyleName();
		
		try {
			ButtonGroup buttonGroup = new ButtonGroup();

			Set<String> keys = templates.keySet();
			Iterator<String> keysIt = keys.iterator();

			while (keysIt.hasNext()) {
				String templateName = keysIt.next();
				ModelHTMLTemplate template = (ModelHTMLTemplate) templates.get(templateName);
				ArrayList<ModelHTMLTemplateStyle> styles = template.getStyles();

				JMenu templateMenu = new JMenu(template.getName()) {
					public JToolTip createToolTip() {
						JMultiLineToolTip tooltip = new JMultiLineToolTip();
						tooltip.setComponent(this);
						return tooltip;
					}
				};
				templateMenu.setToolTipText(template.getInfo());

				boolean itemAdded = false;

				for (int u = 0; u < styles.size(); u++) {
					ModelHTMLTemplateStyle style = (ModelHTMLTemplateStyle) styles.get(u);

					boolean selected = style.getName().equals(currentTemplateStyleName) &&
										template.getName().equals(currentTemplateName);
										
					final JMenuItem m = new JRadioButtonMenuItem(style.getName(), selected)  {
						public JToolTip createToolTip() {
							JMultiLineToolTip tooltip = new JMultiLineToolTip();
							tooltip.setComponent(this);
							return tooltip;
						}
					};
					m.setToolTipText(style.getInfo());

					m.setActionCommand(template.getName());
					buttonGroup.add(m);
					m.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String styleName = ((JRadioButtonMenuItem) e.getSource()).getText();
							String command = m.getActionCommand();
							ModelHTMLTemplate t = (ModelHTMLTemplate) templates.get(command);

							MovieManager.getConfig().getHTMLTemplateHandler().setHTMLTemplate(t, t.getStyle(styleName));
							MovieManager.getDialog().setTabbedMovieInfoTitle();

							MovieManagerCommandSelect.execute();
						}
					});

					templateMenu.add(m);	
					itemAdded = true;
				}

				if (itemAdded) 
					menuView.add(templateMenu);
				else {

					boolean selected = template.getName().equals(currentTemplateName);
					
					JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(template.getName(), selected) {
						public JToolTip createToolTip() {
							JMultiLineToolTip tooltip = new JMultiLineToolTip();
							tooltip.setComponent(this);
							return tooltip;
						}
					};
					menuItem.setToolTipText(template.getInfo());

					buttonGroup.add(menuItem);
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String styleName = ((JRadioButtonMenuItem) e.getSource()).getText();
							String command = e.getActionCommand();
							ModelHTMLTemplate t = (ModelHTMLTemplate) templates.get(command);

							MovieManager.getConfig().getHTMLTemplateHandler().setHTMLTemplate(t, t.getStyle(styleName));

							String tabName;

							if (MovieManager.getConfig().getHTMLTemplateHandler().getHTMLTemplate().hasStyles())
								tabName = MovieManager.getConfig().getHTMLTemplateHandler().getHTMLTemplateStyleName();
							else
								tabName = MovieManager.getConfig().getHTMLTemplateHandler().getHTMLTemplate().getName();

							// Setting the style name as title of tab bar.
							MovieManager.getDialog().setTabbedMovieInfoTitle(1, tabName);
							MovieManagerCommandSelect.execute();
						}
					});
					menuView.add(menuItem);
				}
			}

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e); //$NON-NLS-1$
		}

		log.debug("Creation of the View done."); //$NON-NLS-1$
		return menuView;
	}


	/**
	 * Creates the help menu.
	 *
	 * @return The help menu.
	 **/
	protected JMenu createMenuHelp() {
		log.debug("Start creation of the Help menu."); //$NON-NLS-1$
		menuHelp = new JMenu(Localizer.get("DialogMovieManager.menu.help")); //$NON-NLS-1$
		menuHelp.setMnemonic('H');
		
		/* MenuItem Online Help. */
		JMenuItem menuItemOnlineHelp = new JMenuItem(Localizer.get("DialogMovieManager.menu.help.onlinehelp"),'O'); //$NON-NLS-1$
		menuItemOnlineHelp.setActionCommand("OpenPage (Online Help)"); //$NON-NLS-1$
		menuItemOnlineHelp.addActionListener(new MovieManagerCommandOpenPage("http://xmm.sourceforge.net/help.html")); //$NON-NLS-1$
		menuHelp.add(menuItemOnlineHelp);
		
		/* MenuItem HomePage. */
		JMenuItem menuItemHomePage = new JMenuItem(Localizer.get("DialogMovieManager.menu.help.homepage")); //$NON-NLS-1$
		menuItemHomePage.setActionCommand("OpenPage (Home Page)"); //$NON-NLS-1$
		menuItemHomePage.addActionListener(new MovieManagerCommandOpenPage("http://xmm.sourceforge.net/")); //$NON-NLS-1$
		menuHelp.add(menuItemHomePage);
		
		/* A Separator. */
		menuHelp.addSeparator();
		
		/* check for updates. */
		JMenuItem menuItemUpdate = new JMenuItem("Check for updates"); //$NON-NLS-1$
		menuItemUpdate.setActionCommand("Check for updates"); //$NON-NLS-1$
		menuItemUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.debug("ActionPerformed:" + e.getActionCommand());
				AppUpdater.handleVersionUpdate(true);
			}
		});
		menuHelp.add(menuItemUpdate);
		
		
		
		/* A Separator. */
		menuHelp.addSeparator();
		/* MenuItem About. */
		menuItemAbout = new JMenuItem(Localizer.get("DialogMovieManager.menu.help.about")); //$NON-NLS-1$
		menuItemAbout.setActionCommand("About"); //$NON-NLS-1$
		menuItemAbout.addActionListener(new MovieManagerCommandAbout());
		menuHelp.add(menuItemAbout);
		/* All done. */
		log.debug("Creation of the Help menu done."); //$NON-NLS-1$
		return menuHelp;
	}



	/**
	 * Wrapper method for calling setDatabaseComponentsEnable(boolean, boolean) method on the EDT
	 **/
	public void setDatabaseComponentsEnable(final boolean enable) {
		SwingUtilities.invokeLater(new Runnable() {
        	public void run() {
        		setDatabaseComponentsEnable(enable, enable);
        	}
        });
	}
	
	
	/**
	 * Sets enabled/disabled the related database components.
	 **/
	private void setDatabaseComponentsEnable(boolean enable, boolean notUsed) {
	
		if (menuItemClose != null)
			menuItemClose.setEnabled(enable);

		if (menuImport != null)
			menuImport.setEnabled(enable);

		if (menuExport != null)
			menuExport.setEnabled(enable);

		
		if (menuDatabase != null) {

			if (menuItemQueries != null)
				menuItemQueries.setEnabled(enable);

			if (menuItemFolders != null)
				menuItemFolders.setEnabled(enable);

			if (menuItemAddField != null)
				menuItemAddField.setEnabled(enable);

			if (menuItemAddList != null)
				menuItemAddList.setEnabled(enable);

			if (menuItemConvertDatabase != null)
				menuItemConvertDatabase.setEnabled(enable);
			
		}

		if (menuItemPrefs != null)
			menuItemPrefs.setEnabled(enable);

		if (menuItemAddMultipleMovies != null)
			menuItemAddMultipleMovies.setEnabled(enable);

		if (menuItemUpdateIMDbInfo != null)
			menuItemUpdateIMDbInfo.setEnabled(enable);

		if (menuItemReportGenerator != null)
			menuItemReportGenerator.setEnabled(enable);

		MovieManager.getDialog().getToolBar().setEnableButtons(enable, MovieManager.isApplet());

		/* The JTree. */
		MovieManager.getDialog().getMoviesList().setEnabled(enable);

		/* Filter textField. */
		MovieManager.getDialog().getFilter().setEnabled(enable);

		/* Makes the list selected. */
		MovieManager.getDialog().getMoviesList().requestFocus(true);
	}

	public JMenuItem getAboutButton() {
		return menuItemAbout;
	}

	public JMenuItem getPreferencesButton() {
		return menuItemPrefs;
	}

	public JMenuItem getExitButton() {
		return menuItemExit;
	}
}
