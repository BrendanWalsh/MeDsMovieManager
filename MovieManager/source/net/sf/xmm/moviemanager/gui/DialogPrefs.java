/**
 * @(#)DialogPrefs.java
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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.xmm.moviemanager.LookAndFeelManager;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.LookAndFeelManager.LookAndFeelType;
import net.sf.xmm.moviemanager.MovieManagerConfig.InternalConfig;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedTreeCellRenderer;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager.KeyMapping;
import net.sf.xmm.moviemanager.util.DocumentRegExp;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SysUtil;

import static net.sf.xmm.moviemanager.MovieManagerConfig.NoCoverType;

import org.apache.log4j.Logger;

import com.l2fprod.gui.plaf.skin.Skin;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;



public class DialogPrefs extends JDialog implements ActionListener, ItemListener {

	Logger log = Logger.getLogger(getClass());

	private static MovieManagerConfig config = MovieManager.getConfig();

	InternalConfig disabledFeatures = MovieManager.getConfig().getInternalConfig();

	Color invalidPathColor = MovieManager.getConfig().getInvalidPathColor();
		
	private Container contentPane;
	private JTabbedPane tabbedPane;

	JButton buttonSave;
	JButton buttonCancel;
	
	private JCheckBox enableLafChooser;
	private JCheckBox enableSkinlf;
	private JCheckBox enableSubstanceChooser;
	private JCheckBox enableNimrodChooser;
	
	private JComboBox lafChooser;
	private JComboBox skinlfThemePackChooser;
	private JComboBox substanceChooser;
	private JComboBox nimrodChooser;
	
	private JRadioButton regularSeenIcon;
	private JRadioButton currentLookAndFeelIcon;

	private JRadioButton regularDecoratedButton;
	private JRadioButton defaultLafDecoratedButton;

	private JCheckBox enableProxyButton;
	private JCheckBox enableAuthenticationButton;
	private JComboBox proxyType = new JComboBox();

	private JTextField portTextField;
	private JTextField userNameTextField;
	private JPasswordField passwordTextField;
	private JTextField hostTextField;

	private JLabel proxyTypeLabel;
	private JLabel hostLabel;
	private JLabel portLabel;
	private JLabel userNameLabel;
	private JLabel passwordLabel;


	// IMDb login
	private JCheckBox enableIMDbAuthenticationButton;

	private JTextField IMDbUserNameTextField;
	private JPasswordField IMDbPasswordTextField;

	private JLabel IMDbUserNameLabel;
	private JLabel IMDbPasswordLabel;

	private JCheckBox loadDatabaseOnStartUp;
	private JCheckBox enableAutoMoveThe;
	private JCheckBox enableAutoMoveAnAndA;
	private JCheckBox removeQuotesFromSeriesTitle;

	private JCheckBox storeAllAvailableAkaTitles;
	private JCheckBox includeAkaLanguageCodes;
	private JCheckBox useLanguageSpecificTitle;

	private JComboBox languageCodeSelector;

	private JCheckBox displayQueriesInTree;

	private JCheckBox checkForProgramUpdates;
	private JCheckBox enablePlayButton;	
	private JCheckBox checkEnableHTMLViewDebugMode;
	private JCheckBox checkEnableMySQLSocketTimeout;

	private JCheckBox enableSeenEditable;
	private JCheckBox enableRightclickByCtrl;
	private JCheckBox enableLoadLastUsedList;
	private JCheckBox enableAddNewMoviesToCurrentLists;

	private JCheckBox enableUseJTreeIcons;
	private JCheckBox enableUseJTreeCovers;

	private JCheckBox enablePreserveCoverRatioEpisodesOnly;
	private JCheckBox enablePreserveCoverRatio;

	private JRadioButton pumaCover;
	private JRadioButton jaguarCover;
	private JRadioButton tigerCover;

	private JCheckBox enableStoreCoversLocally;

	private JTextField makeBackupEveryLaunchField;
	private JTextField deleteOldestWhenSizeExcedesMBField;
	private JTextField backupDirField;
	private JCheckBox warnAboutInvalidBackupDir;
	
	private JComboBox langauges;

	private JButton browserBrowse;
	private JTextField mediaPlayerPathField;
	private JTextField mediaPlayerCmdArgument;
	private JTextField customBrowserPathField;

	private JRadioButton browserOptionOpera;
	private JRadioButton browserOptionMozilla;
	private JRadioButton browserOptionFirefox;
	private JRadioButton browserOptionNetscape;
	private JRadioButton browserOptionSafari;
	//private JRadioButton browserOptionGoogleChrome;
	private JRadioButton browserOptionIE;


	private JCheckBox enableUseDefaultWindowsPlayer;
	private JRadioButton enableUseDefaultWindowsBrowser;
	private JRadioButton enableCustomBrowser;

	private JButton externCommandInfo;
	private JCheckBox externalCmd;

	private JSlider rowHeightSlider;
	private JTree exampleTree;
	private MovieManagerConfig exampleConfig = new MovieManagerConfig(true);

	KeyboardShortcutManager shortcutManager = new KeyboardShortcutManager(this);

	LookAndFeelManager lafManager = MovieManager.getLookAndFeelManager();
	
	public DialogPrefs() {
		/* Dialog creation...*/
		super(MovieManager.getDialog());

		setTitle(Localizer.get("DialogPrefs.title")); //$NON-NLS-1$
		setModal(true);
		setResizable(false);
		
		createGUI();
		setHotkeyModifiers();
	}
	
	void createGUI() {
		tabbedPane = new JTabbedPane();
		tabbedPane.setBorder(BorderFactory.createEmptyBorder(8,8,5,8));

		// handle mouse scrolling to change tab
		tabbedPane.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				handleMouseScrolling(e);			
			}
		});

		if (!disabledFeatures.isPreferencesLookAndFeelDisabled())
			tabbedPane.add(Localizer.get("DialogPrefs.panel.look-and-feel.title"), createLookAndFeelPanel()); //$NON-NLS-1$

		if (!disabledFeatures.isPreferencesProxySettingsDisabled())
			tabbedPane.add(Localizer.get("DialogPrefs.panel.proxy.title"), createProxyPanel()); //$NON-NLS-1$

		if (!disabledFeatures.isPreferencesMiscellaneousDisabled())
			tabbedPane.add(Localizer.get("DialogPrefs.panel.miscellaneous.title"), createMiscellaneousPanel()); //$NON-NLS-1$

		if (!disabledFeatures.isPreferencesCoverSettingsDisabled())
			tabbedPane.add(Localizer.get("DialogPrefs.panel.cover-settings.title"), createCoverPanel()); //$NON-NLS-1$

		if (!disabledFeatures.isPreferencesMovieListDisabled())
			tabbedPane.add(Localizer.get("DialogPrefs.panel.movie-list.title"), createMovieListPanel()); //$NON-NLS-1$

		if (!disabledFeatures.isPreferencesExternalProgramsDisabled())
			tabbedPane.add(Localizer.get("DialogPrefs.panel.external-programs.title"), createExternalProgramsPanel()); //$NON-NLS-1$

		if (!disabledFeatures.isPreferencesDatabaseBackupDisabled())
			tabbedPane.add(Localizer.get("DialogPrefs.panel.database-backup.title"), createBackupPanel()); //$NON-NLS-1$

		if (!disabledFeatures.isPreferencesIMDbSettingsDisabled())
			tabbedPane.add(Localizer.get("DialogPrefs.panel.imdb-settings.title"), createIMDbPanel()); //$NON-NLS-1$


		int selectTab = config.getLastPreferencesTabIndex();

		if (selectTab == -1 || selectTab >= tabbedPane.getTabCount())
			selectTab = 0;

		if (tabbedPane.getComponentCount() > 0)
			tabbedPane.setSelectedIndex(selectTab);

		contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));

		contentPane.add(tabbedPane);
		contentPane.add(createButtonPanel());

		/*Display the window.*/
		pack();
		setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
				(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);

		updateEnabling();
	}
	

	JPanel createLookAndFeelPanel() {	

		/* LookAndFeel panel */
		JPanel lafPanel = new JPanel(new BorderLayout());

		lafPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogPrefs.panel.look-and-feel.title")), BorderFactory.createEmptyBorder(12,0,16,0))); //$NON-NLS-1$

		/* Seen button icon */
		JLabel seenIconLabel = new JLabel(Localizer.get("DialogPrefs.panel.look-and-feel.seen-unseen-icon")); //$NON-NLS-1$
		regularSeenIcon = new JRadioButton(Localizer.get("DialogPrefs.panel.look-and-feel.seen-unseen-icon.regular")); //$NON-NLS-1$
		currentLookAndFeelIcon = new JRadioButton(Localizer.get("DialogPrefs.panel.look-and-feel.seen-unseen-icon.look-and-feel") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		regularSeenIcon.setActionCommand("SeenIcon"); //$NON-NLS-1$
		currentLookAndFeelIcon.setActionCommand("SeenIcon"); //$NON-NLS-1$

		ButtonGroup seenIconGroup = new ButtonGroup();
		seenIconGroup.add(regularSeenIcon);
		seenIconGroup.add(currentLookAndFeelIcon);

		regularSeenIcon.addActionListener(this);
		currentLookAndFeelIcon.addActionListener(this);

		if (config.getUseRegularSeenIcon())
			regularSeenIcon.setSelected(true);
		else
			currentLookAndFeelIcon.setSelected(true);

		JPanel seenIconPanel = new JPanel(new BorderLayout());
		seenIconPanel.setBorder(BorderFactory.createEmptyBorder(0,20,7,20));


		JPanel buttonOptions = new JPanel(new GridLayout(2, 3));
		buttonOptions.setBorder(BorderFactory.createEmptyBorder(0,30,50,20));

		buttonOptions.add(seenIconLabel);
		buttonOptions.add(regularSeenIcon);
		buttonOptions.add(currentLookAndFeelIcon);


		/* DefaultLookAndFeelDecorated */
		JLabel defaultLafDecoratedLabel = new JLabel(Localizer.get("DialogPrefs.panel.look-and-feel.title-bar-decoration") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		regularDecoratedButton = new JRadioButton(Localizer.get("DialogPrefs.panel.look-and-feel.title-bar-decoration.regular")); //$NON-NLS-1$
		defaultLafDecoratedButton = new JRadioButton(Localizer.get("DialogPrefs.panel.look-and-feel.title-bar-decoration.look-and-feel")); //$NON-NLS-1$
		regularDecoratedButton.setActionCommand("DefaultLafDecorated"); //$NON-NLS-1$
		defaultLafDecoratedButton.setActionCommand("DefaultLafDecorated"); //$NON-NLS-1$

		ButtonGroup defaultLafDecoratedGroup = new ButtonGroup();
		defaultLafDecoratedGroup.add(regularDecoratedButton);
		defaultLafDecoratedGroup.add(defaultLafDecoratedButton);

		regularDecoratedButton.addActionListener(this);
		defaultLafDecoratedButton.addActionListener(this);

		if (lafManager.getDefaultLookAndFeelDecorated())
			defaultLafDecoratedButton.setSelected(true);
		else
			regularDecoratedButton.setSelected(true);

		JPanel defaultLafDecoratedPanel = new JPanel(new BorderLayout());
		defaultLafDecoratedPanel.setBorder(BorderFactory.createEmptyBorder(0,20,7,20));

		buttonOptions.add(defaultLafDecoratedLabel);
		buttonOptions.add(regularDecoratedButton);
		buttonOptions.add(defaultLafDecoratedButton);

		lafPanel.add(buttonOptions, BorderLayout.NORTH);

		
		JLabel noteLabel = new JLabel();
		noteLabel.setText("<html><center><p>After changing the Look & Feel, the user interface might<br> not look exactly as it will after restarting the application.</p></center></html>");
		JPanel noteLabelPanel = new JPanel();
		noteLabelPanel.add(noteLabel);
		
		lafPanel.add(noteLabelPanel, BorderLayout.CENTER);
		
		/* Laf choosers */

		JPanel lafChooserPanel = new JPanel(new GridLayout(4, 1));
		
		String [] lookAndFeelStrings = lafManager.getLaFListArray();
		
		lafChooser = new JComboBox(lookAndFeelStrings);
		enableLafChooser = new JCheckBox(Localizer.get("DialogPrefs.panel.look-and-feel.enable-custom-laf.text")); //$NON-NLS-1$
		enableLafChooser.setActionCommand("Enable LookAndFeel"); //$NON-NLS-1$
		
		String currentLookAndFeel = lafManager.getCustomLookAndFeel();

		lafChooser.setSelectedItem(currentLookAndFeel);
		lafChooser.setEnabled(false);
		lafChooser.addActionListener(this);
		enableLafChooser.addActionListener(this);

		lafChooser.setPreferredSize(new Dimension(250, (int) lafChooser.getPreferredSize().getHeight()));

		JPanel customLafChooserPanel = new JPanel(new BorderLayout());
		customLafChooserPanel.setBorder(BorderFactory.createEmptyBorder(4,20,4,20));

		customLafChooserPanel.add(enableLafChooser, BorderLayout.WEST);
		customLafChooserPanel.add(lafChooser, BorderLayout.EAST);

		customLafChooserPanel.setMaximumSize(new Dimension(250,30));
		customLafChooserPanel.setPreferredSize(new Dimension(250,30));

		lafChooserPanel.add(customLafChooserPanel);


		/* Skinlf */

		String [] skinlfThemePackList = lafManager.getSkinlfThemepackList();

		if (skinlfThemePackList != null) {
			enableSkinlf = new JCheckBox(Localizer.get("DialogPrefs.panel.look-and-feel.enable-skinlf.text")); //$NON-NLS-1$
			enableSkinlf.setActionCommand("Enable LookAndFeel"); //$NON-NLS-1$
			
			// Prettify the names by removing zip
			for (int i = 0; i < skinlfThemePackList.length; i++) {
				skinlfThemePackList[i] = skinlfThemePackList[i].replace(".zip", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}

			Arrays.sort(skinlfThemePackList);

			skinlfThemePackChooser = new JComboBox(skinlfThemePackList);
			skinlfThemePackChooser.setEnabled(false);

			String currentSkinlfThemePack = lafManager.getSkinlfThemePack().replace(".zip", ""); //$NON-NLS-1$ //$NON-NLS-2$
			skinlfThemePackChooser.setSelectedItem(currentSkinlfThemePack);

			if (skinlfThemePackChooser.getSelectedIndex() == -1)
				skinlfThemePackChooser.setSelectedIndex(0);


			JPanel skinlfPanel = new JPanel(new BorderLayout());
			skinlfPanel.setBorder(BorderFactory.createEmptyBorder(4,20,4,20));

			skinlfPanel.add(enableSkinlf, BorderLayout.WEST);
			skinlfPanel.add(skinlfThemePackChooser, BorderLayout.EAST);

			lafChooserPanel.add(skinlfPanel);
			skinlfThemePackChooser.addActionListener(this);
			enableSkinlf.addActionListener(this);
		}

		// Add substance

		String [] substanceSkins = lafManager.getSubstanceSkinListArray();

		substanceChooser = new JComboBox(substanceSkins);
		enableSubstanceChooser = new JCheckBox("Substance"); //$NON-NLS-1$
		enableSubstanceChooser.setActionCommand("Enable LookAndFeel"); //$NON-NLS-1$
		

		String currentSubstance = lafManager.getSubstanceSkin();

		substanceChooser.setSelectedItem(currentSubstance);
		substanceChooser.setEnabled(false);
		substanceChooser.addActionListener(this);
		enableSubstanceChooser.addActionListener(this);

		substanceChooser.setPreferredSize(new Dimension(250, (int) lafChooser.getPreferredSize().getHeight()));

		/* Group the radio buttons. */
		ButtonGroup lafGroup = new ButtonGroup();
		lafGroup.add(enableSubstanceChooser);
		lafGroup.add(enableSkinlf);
		lafGroup.add(enableLafChooser);
		

		JPanel substanceLafChooserPanel = new JPanel(new BorderLayout());
		substanceLafChooserPanel.setBorder(BorderFactory.createEmptyBorder(4,20,4,20));

		substanceLafChooserPanel.add(enableSubstanceChooser, BorderLayout.WEST);
		substanceLafChooserPanel.add(substanceChooser, BorderLayout.EAST);

		substanceLafChooserPanel.setMaximumSize(new Dimension(250,30));
		substanceLafChooserPanel.setPreferredSize(new Dimension(250,30));

		if (substanceSkins.length > 0) {
			lafChooserPanel.add(substanceLafChooserPanel);
		}
		
		
		// Nimrod
		
		String [] nimrodThemes = lafManager.getNimrodThemeList();

		nimrodChooser = new JComboBox(nimrodThemes);
		enableNimrodChooser = new JCheckBox("NimRod"); //$NON-NLS-1$
		enableNimrodChooser.setActionCommand("Enable LookAndFeel"); //$NON-NLS-1$
		lafGroup.add(enableNimrodChooser);

		String currentNimrod = lafManager.getNimRODTheme();
		
		nimrodChooser.setSelectedItem(currentNimrod);
		nimrodChooser.setEnabled(false);
		nimrodChooser.addActionListener(this);
		enableNimrodChooser.addActionListener(this);

		nimrodChooser.setPreferredSize(new Dimension(250, (int) lafChooser.getPreferredSize().getHeight()));

		JPanel nimrodLafChooserPanel = new JPanel(new BorderLayout());
		nimrodLafChooserPanel.setBorder(BorderFactory.createEmptyBorder(4,20,4,20));

		nimrodLafChooserPanel.add(enableNimrodChooser, BorderLayout.WEST);
		nimrodLafChooserPanel.add(nimrodChooser, BorderLayout.EAST);

		nimrodLafChooserPanel.setMaximumSize(new Dimension(250,30));
		nimrodLafChooserPanel.setPreferredSize(new Dimension(250,30));

		if (nimrodThemes.length > 0) {
			lafChooserPanel.add(nimrodLafChooserPanel);
		}
				
		
		setLafChooserPreferredSize();

		
		if ((skinlfThemePackList != null) && (lafManager.getLookAndFeelType() == LookAndFeelType.SkinlfLaF)) {
			enableSkinlf.setSelected(true);
			skinlfThemePackChooser.setEnabled(true);
		}
		else if (lafManager.getLookAndFeelType() == LookAndFeelType.Substance && substanceChooser != null) {
			enableSubstanceChooser.setSelected(true);
			substanceChooser.setEnabled(true);
		}
		else if (lafManager.getLookAndFeelType() == LookAndFeelType.NimROD && nimrodChooser != null) {
			enableNimrodChooser.setSelected(true);
			nimrodChooser.setEnabled(true);
		}
		else {
			enableLafChooser.setSelected(true);
			lafChooser.setEnabled(true);
			
			if (skinlfThemePackList == null)
				skinlfThemePackChooser.setEnabled(false);
		}

		lafPanel.add(lafChooserPanel, BorderLayout.SOUTH);

		return lafPanel;
	}

	JPanel createProxyPanel() {

		/* Proxy settings */

		enableProxyButton = new JCheckBox(Localizer.get("DialogPrefs.panel.proxy.enable-proxy")); //$NON-NLS-1$
		enableProxyButton.setActionCommand("Enable Proxy"); //$NON-NLS-1$
		enableProxyButton.addItemListener(this);

		String[] proxyTypeString = { "HTTP", "SOCKS" }; //$NON-NLS-1$ //$NON-NLS-2$
		proxyType = new JComboBox(proxyTypeString);
		proxyType.setSelectedItem(config.getProxyType());
		proxyType.setEnabled(false);
		proxyTypeLabel = new JLabel(Localizer.get("DialogPrefs.panel.proxy.proxy-type") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		proxyTypeLabel.setEnabled(false);


		hostLabel = new JLabel(Localizer.get("DialogPrefs.panel.proxy.host") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		hostLabel.setEnabled(false);
		hostTextField = new JTextField(18);
		hostTextField.setText(""); //$NON-NLS-1$
		hostTextField.setEnabled(false);

		JPanel hostPanel = new JPanel();
		hostPanel.add(hostLabel);
		hostPanel.add(hostTextField);

		portLabel = new JLabel(Localizer.get("DialogPrefs.panel.proxy.port") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		portLabel.setEnabled(false);
		portTextField = new JTextField(4);
		portTextField.setText(""); //$NON-NLS-1$
		portTextField.setEnabled(false);
		portTextField.setDocument(new DocumentRegExp("(\\d)*",5)); //$NON-NLS-1$

		JPanel portPanel = new JPanel();
		portPanel.add(portLabel);
		portPanel.add(portTextField);

		JPanel proxyServerPanel = new JPanel(new GridBagLayout());

		GridBagConstraints constraints;

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(1,5,12,5);
		proxyServerPanel.add(enableProxyButton,constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(1,5,12,10);
		constraints.anchor = GridBagConstraints.EAST;
		proxyServerPanel.add(proxyTypeLabel,constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(1,1,12,5);
		constraints.anchor = GridBagConstraints.EAST;
		proxyServerPanel.add(proxyType,constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 3;
		constraints.insets = new Insets(1,5,1,5);
		proxyServerPanel.add(hostPanel,constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(1,1,1,1);
		constraints.anchor = GridBagConstraints.EAST;
		proxyServerPanel.add(portPanel,constraints);

		enableAuthenticationButton = new JCheckBox(Localizer.get("DialogPrefs.panel.proxy.enable-authentication")); //$NON-NLS-1$
		enableAuthenticationButton.setActionCommand("Enable Authentication"); //$NON-NLS-1$
		enableAuthenticationButton.setEnabled(false);
		enableAuthenticationButton.addItemListener(this);

		JPanel enableAuthenticationPanel = new JPanel();
		enableAuthenticationPanel.add(enableAuthenticationButton);

		userNameLabel = new JLabel(Localizer.get("DialogPrefs.panel.proxy.username") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		userNameLabel.setEnabled(false);
		userNameTextField = new JTextField(7);
		userNameTextField.setText(""); //$NON-NLS-1$
		userNameTextField.setEnabled(false);

		JPanel userNamePanel = new JPanel();
		userNamePanel.add(userNameLabel);
		userNamePanel.add(userNameTextField);

		passwordLabel = new JLabel(Localizer.get("DialogPrefs.panel.proxy.password") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		passwordLabel.setEnabled(false);
		passwordTextField = new JPasswordField(7);
		passwordTextField.setText(""); //$NON-NLS-1$
		passwordTextField.setEnabled(false);

		JPanel passwordPanel = new JPanel() ;
		passwordPanel.add(passwordLabel);
		passwordPanel.add(passwordTextField);

		JPanel proxyAuthenticationPanel = new JPanel(new GridBagLayout());
		proxyAuthenticationPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogPrefs.panel.proxy.authentication")), BorderFactory.createEmptyBorder(0,5,5,5))); //$NON-NLS-1$

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		proxyAuthenticationPanel.add(enableAuthenticationPanel, constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.LAST_LINE_START;
		proxyAuthenticationPanel.add(userNamePanel, constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.LAST_LINE_END;
		proxyAuthenticationPanel.add(passwordPanel,constraints);


		JPanel proxyPanel = new JPanel(new GridLayout(0, 1));
		proxyPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogPrefs.panel.proxy.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

		proxyPanel.add(proxyServerPanel);
		proxyPanel.add(proxyAuthenticationPanel);

		String temp;

		if (((temp = config.getProxyHost()) != null) && !temp.equals("null")) //$NON-NLS-1$
			hostTextField.setText(temp);

		if (((temp = config.getProxyPort()) != null) && !temp.equals("null")) //$NON-NLS-1$
			portTextField.setText(temp);

		if (((temp = config.getProxyUser()) != null)&& !temp.equals("null")) //$NON-NLS-1$
			userNameTextField.setText(temp);

		if (((temp = config.getProxyPassword()) != null) && !temp.equals("null")) //$NON-NLS-1$
			passwordTextField.setText(temp);

		if (config.getProxyEnabled())
			enableProxyButton.setSelected(true);

		if (config.getIMDbAuthenticationEnabled())
			enableAuthenticationButton.setSelected(true);

		return proxyPanel;
	}




	JPanel createIMDbPanel() {
		
		/* IMDb login panel */
		enableIMDbAuthenticationButton = new JCheckBox("Enable authentication"); //$NON-NLS-1$
		enableIMDbAuthenticationButton.setActionCommand("Enable Authentication"); //$NON-NLS-1$
		enableIMDbAuthenticationButton.addItemListener(this);

		JPanel enableIMDbAuthenticationPanel = new JPanel();
		enableIMDbAuthenticationPanel.add(enableIMDbAuthenticationButton);

		IMDbUserNameLabel = new JLabel("Username" + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		IMDbUserNameLabel.setEnabled(false);
		IMDbUserNameTextField = new JTextField(7);
		IMDbUserNameTextField.setEnabled(false);

		JPanel IMDbUserNamePanel = new JPanel();
		IMDbUserNamePanel.add(IMDbUserNameLabel);
		IMDbUserNamePanel.add(IMDbUserNameTextField);

		IMDbPasswordLabel = new JLabel("Password" + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		IMDbPasswordLabel.setEnabled(false);
		IMDbPasswordTextField = new JPasswordField(7);
		IMDbPasswordTextField.setEnabled(false);

		JPanel IMDbPasswordPanel = new JPanel() ;
		IMDbPasswordPanel.add(IMDbPasswordLabel);
		IMDbPasswordPanel.add(IMDbPasswordTextField);

		JPanel IMDbAuthenticationPanel = new JPanel(new GridBagLayout());

		GridBagConstraints constraints;
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		IMDbAuthenticationPanel.add(enableIMDbAuthenticationButton, constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.LAST_LINE_START;
		IMDbAuthenticationPanel.add(IMDbUserNamePanel, constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(1,5,1,5);
		constraints.anchor = GridBagConstraints.LAST_LINE_END;
		IMDbAuthenticationPanel.add(IMDbPasswordPanel,constraints);
		IMDbAuthenticationPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"IMDb Authentication"), BorderFactory.createEmptyBorder(0,5,5,5))); //$NON-NLS-1$
		
		String temp;
		
		if (((temp = config.getIMDbAuthenticationUser()) != null) && !temp.equals("null")) //$NON-NLS-1$
			IMDbUserNameTextField.setText(temp);

		if (((temp = config.getIMDbAuthenticationPassword()) != null) && !temp.equals("null")) //$NON-NLS-1$
			IMDbPasswordTextField.setText(temp);

		if (config.getIMDbAuthenticationEnabled())
			enableIMDbAuthenticationButton.setSelected(true);


		//		title options

		JPanel titlePanel = new JPanel();

		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
		titlePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Imported Movie Titles"), BorderFactory.createEmptyBorder(0,5,5,5))); //$NON-NLS-1$

		JPanel autoMovieToEndOfTitlePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;

		removeQuotesFromSeriesTitle = new JCheckBox("Remove quotes from series titles"); //$NON-NLS-1$
		removeQuotesFromSeriesTitle.setSelected(config.getRemoveQuotesOnSeriesTitle());

		JPanel removeQuotesPanel = new JPanel();
		removeQuotesPanel.setLayout(new GridLayout(1, 1));
		removeQuotesPanel.add(removeQuotesFromSeriesTitle);
		titlePanel.add(removeQuotesPanel);

		JLabel autoMoveLabel = new JLabel(Localizer.get("DialogPrefs.panel.miscellaneous.auto-move-to-end-of-title") + ":"); //$NON-NLS-1$ //$NON-NLS-2$

		autoMovieToEndOfTitlePanel.add(autoMoveLabel, c);

		/* Enable Automatic placement of 'The' at the end of title */
		enableAutoMoveThe = new JCheckBox("'The '"); //$NON-NLS-1$
		enableAutoMoveThe.setActionCommand("Enable auto move"); //$NON-NLS-1$

		if (config.getAutoMoveThe())
			enableAutoMoveThe.setSelected(true);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;

		autoMovieToEndOfTitlePanel.add(enableAutoMoveThe, c);

		/* Enable Automatic placement of 'The' at the end of title */
		enableAutoMoveAnAndA = new JCheckBox("'A ' and 'An '"); //$NON-NLS-1$
		enableAutoMoveAnAndA.setActionCommand("Enable auto move"); //$NON-NLS-1$

		if (config.getAutoMoveAnAndA())
			enableAutoMoveAnAndA.setSelected(true);

		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;

		autoMovieToEndOfTitlePanel.add(enableAutoMoveAnAndA, c);
		autoMovieToEndOfTitlePanel.setMaximumSize(new Dimension((int) autoMovieToEndOfTitlePanel.getMaximumSize().getWidth(), (int) autoMovieToEndOfTitlePanel.getPreferredSize().getHeight()));

		titlePanel.add(autoMovieToEndOfTitlePanel);

		/* Insert title in specific language */

		JPanel akaTitlePanel = new JPanel();
		akaTitlePanel.setLayout(new GridLayout(4, 1));

		storeAllAvailableAkaTitles = new JCheckBox(Localizer.get("DialogPrefs.panel.imdb-settings.store-all-available-aka-titles")); //$NON-NLS-1$
		includeAkaLanguageCodes = new JCheckBox(Localizer.get("DialogPrefs.panel.imdb-settings.include-comments-and-language-codes")); //$NON-NLS-1$
		useLanguageSpecificTitle = new JCheckBox(Localizer.get("DialogPrefs.panel.imdb-settings.replace-original-title-with-aka-title-with-the-following-language")); //$NON-NLS-1$

		
		
		ArrayList<String> langCodesList = new ArrayList<String>(150);
		int index = 0;

		try {

			InputStream inputStream = FileUtil.getResourceAsStream("/codecs/LanguageCodes.txt"); //$NON-NLS-1$
						
			if (inputStream != null) {

				BufferedInputStream stream = new BufferedInputStream(inputStream);
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8")); //$NON-NLS-1$

				String currentLangCode = config.getTitleLanguageCode();

				if (currentLangCode.equals("")) //$NON-NLS-1$
					currentLangCode = System.getProperty("user.language"); //$NON-NLS-1$

				String line;

				while ((line = reader.readLine()) != null) {
					
					if (line.startsWith(currentLangCode)) {
						index = langCodesList.size();
					}

					line = " " + line.replaceFirst("\t", " - "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

					langCodesList.add(line);
				}
				reader.close();
			}
		} catch (IOException e) {
			log.warn("Exception:" + e.getMessage(), e);
		}

		akaTitlePanel.add(storeAllAvailableAkaTitles);
		akaTitlePanel.add(includeAkaLanguageCodes);
		akaTitlePanel.add(useLanguageSpecificTitle);

		Object [] languageCodes = langCodesList.toArray();
	
		if (languageCodes.length > 0) {

			languageCodeSelector = new JComboBox(languageCodes);

			languageCodeSelector.setMaximumSize(languageCodeSelector.getPreferredSize());

			if (languageCodeSelector.getItemCount() > index)  
				languageCodeSelector.setSelectedIndex(index);

			storeAllAvailableAkaTitles.addItemListener(this);
			useLanguageSpecificTitle.addItemListener(this);

			if (config.getStoreAllAkaTitles())
				storeAllAvailableAkaTitles.setSelected(true);

			if (config.getIncludeAkaLanguageCodes())
				includeAkaLanguageCodes.setSelected(true);

			if (config.getUseLanguageSpecificTitle()) {
				useLanguageSpecificTitle.setSelected(true);
			}
			else
				languageCodeSelector.setEnabled(false);

			akaTitlePanel.add(languageCodeSelector);
		}

		titlePanel.add(akaTitlePanel);

		JPanel IMDbPanel = new JPanel();
		IMDbPanel.setLayout(new BorderLayout());
		
		IMDbPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"IMDb settings"), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$

		IMDbPanel.add(IMDbAuthenticationPanel, BorderLayout.NORTH);
		IMDbPanel.add(titlePanel, BorderLayout.CENTER);

		return IMDbPanel;
	}

	JPanel createMiscellaneousPanel() {
		/* Miscellaneous panel */
		JPanel miscPanel = new JPanel();
		miscPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogPrefs.panel.miscellaneous.title")), BorderFactory.createEmptyBorder(12,1,16,1))); //$NON-NLS-1$
		miscPanel.setLayout(new BorderLayout());

		//miscPanel.setBackground(Color.black);

		JPanel miscCheckBoxes = new JPanel(new GridLayout(5, 1));
		miscCheckBoxes.setBorder(BorderFactory.createEmptyBorder(10,15,35,5));
		miscCheckBoxes.setLayout(new BoxLayout(miscCheckBoxes, BoxLayout.PAGE_AXIS));
		//miscCheckBoxes.setLayout(new BorderLayout());


		loadDatabaseOnStartUp = new JCheckBox(Localizer.get("DialogPrefs.panel.miscellaneous.auto-load-database")); //$NON-NLS-1$
		loadDatabaseOnStartUp.setActionCommand("Load databse"); //$NON-NLS-1$

		if (config.getLoadDatabaseOnStartup())
			loadDatabaseOnStartUp.setSelected(true);

		miscCheckBoxes.add(loadDatabaseOnStartUp);


		/* Enable seen editable */
		enableSeenEditable = new JCheckBox(Localizer.get("DialogPrefs.panel.miscellaneous.enable-seen-editable-main-window")); //$NON-NLS-1$

		enableSeenEditable.setActionCommand("Enable Seen"); //$NON-NLS-1$

		if (config.getSeenEditable())
			enableSeenEditable.setSelected(true);

		miscCheckBoxes.add(enableSeenEditable);


		displayQueriesInTree = new JCheckBox(Localizer.get("DialogPrefs.panel.miscellaneous.use-directory-structure-to-group-queries")); //$NON-NLS-1$

		if (config.getUseDisplayQueriesInTree())
			displayQueriesInTree.setSelected(true);

		miscCheckBoxes.add(displayQueriesInTree);

		checkForProgramUpdates = new JCheckBox("Check for version updates at startup"); //$NON-NLS-1$

		if (config.getCheckForProgramUpdates())
			checkForProgramUpdates.setSelected(true);

		miscCheckBoxes.add(checkForProgramUpdates);


		checkEnableHTMLViewDebugMode = new JCheckBox("Enable debug mode for HTML View"); //$NON-NLS-1$

		if (config.getHTMLViewDebugMode())
			checkEnableHTMLViewDebugMode.setSelected(true);

		miscCheckBoxes.add(checkEnableHTMLViewDebugMode);


		enablePlayButton = new JCheckBox("Enable play button in toolbar"); //$NON-NLS-1$
		enablePlayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MovieManager.getDialog().getToolBar().showPlayButton(enablePlayButton.isSelected());
			}
		});


		if (config.getDisplayPlayButton())
			enablePlayButton.setSelected(true);

		miscCheckBoxes.add(enablePlayButton);

		// Lists
		JPanel listsPanel = new JPanel();
		listsPanel.setLayout(new BoxLayout(listsPanel, BoxLayout.Y_AXIS));
		listsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,0,10,0), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Lists"), BorderFactory.createEmptyBorder(5,5,5,5)))); //$NON-NLS-1$


		/* Enable load last used list */
		enableLoadLastUsedList = new JCheckBox(Localizer.get("DialogPrefs.panel.movie-list.load-last-used-list")); //$NON-NLS-1$
		enableLoadLastUsedList.setActionCommand("Enable lastLoadedList"); //$NON-NLS-1$

		if (config.getLoadLastUsedListAtStartup())
			enableLoadLastUsedList.setSelected(true);

		if (!MovieManager.getConfig().getInternalConfig().getDisableLoadLastUsedList())
			listsPanel.add(enableLoadLastUsedList);

		/* Enable load last used list */
		enableAddNewMoviesToCurrentLists = new JCheckBox(Localizer.get("DialogPrefs.panel.miscellaneous.add-new-movies-to-currently-selected-lists"));  //$NON-NLS-1$
		enableAddNewMoviesToCurrentLists.setActionCommand("Enable Add new movies to current lists"); //$NON-NLS-1$

		if (config.getAddNewMoviesToCurrentLists())
			enableAddNewMoviesToCurrentLists.setSelected(true);

		listsPanel.add(enableAddNewMoviesToCurrentLists);		
		
		JPanel languagePanel = new JPanel();	
		languagePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,0,3,0), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Language "), BorderFactory.createEmptyBorder(5,5,5,5)))); //$NON-NLS-1$
		
		
		String [] langs = Localizer.getAvailableLanguages();
		
		langauges = new JComboBox(langs);
		langauges.setSelectedItem(MovieManager.getConfig().getLocale());
		
		languagePanel.add(langauges);
		langauges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//System.out.println("ActionListener:" + langauges.getSelectedItem());
			}
		});
		
		// Only if MySQL database
		if (MovieManager.getIt().getDatabase() != null && MovieManager.getIt().getDatabase().isMySQL()) {
			checkEnableMySQLSocketTimeout = new JCheckBox("<html>"+Localizer.get("DialogPrefs.enable-mysql-socket-timout")+"</html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			if (config.getMySQLSocketTimeoutEnabled())
				checkEnableMySQLSocketTimeout.setSelected(true);

			miscCheckBoxes.add(checkEnableMySQLSocketTimeout);
		}

		miscPanel.add(miscCheckBoxes, BorderLayout.NORTH);
		miscPanel.add(listsPanel, BorderLayout.SOUTH);
		//miscPanel.add(languagePanel, BorderLayout.SOUTH);
		
		return miscPanel;
	}


	JPanel createCoverPanel() {
		/* Cover settings */
		JPanel coverPanel = new JPanel();
		//Box coverPanel = Box.createVerticalBox();
		coverPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogPrefs.panel.cover-settings.title")), BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
		coverPanel.setLayout(new BoxLayout(coverPanel, BoxLayout.Y_AXIS));
		//coverPanel.setLayout(new BorderLayout());

		enablePreserveCoverRatio = new JCheckBox(Localizer.get("DialogPrefs.panel.cover-settings.preserve-aspect-ratio")); //$NON-NLS-1$
		enablePreserveCoverRatio.setActionCommand("Preserve Cover ratio"); //$NON-NLS-1$
		enablePreserveCoverRatio.addItemListener(this);

		if (config.getPreserveCoverAspectRatio() == 1)
			enablePreserveCoverRatio.setSelected(true);

		//coverPanel.add(enablePreserveCoverRatio);

		enablePreserveCoverRatioEpisodesOnly = new JCheckBox(Localizer.get("DialogPrefs.panel.cover-settings.preserve-aspect-ratio-episodes-only")); //$NON-NLS-1$
		enablePreserveCoverRatioEpisodesOnly.setActionCommand("Preserve Cover ratio episodes"); //$NON-NLS-1$
		enablePreserveCoverRatioEpisodesOnly.addItemListener(this);

		if (config.getPreserveCoverAspectRatio() == 2) {
			enablePreserveCoverRatioEpisodesOnly.setSelected(true);
			enablePreserveCoverRatio.setSelected(false);
		}

		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.PAGE_AXIS));
		checkBoxPanel.setAlignmentX(LEFT_ALIGNMENT);
		checkBoxPanel.add(enablePreserveCoverRatio);
		checkBoxPanel.add(enablePreserveCoverRatioEpisodesOnly);

		coverPanel.add(checkBoxPanel);

		JPanel nocoverImagePanel = new JPanel();
		//nocoverImagePanel.setLayout(new BorderLayout());
		nocoverImagePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(20,30,0,0), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogPrefs.panel.cover-settings.nocover.title")), BorderFactory.createEmptyBorder(0,5,5,5)))); //$NON-NLS-1$
		nocoverImagePanel.setLayout(new BoxLayout(nocoverImagePanel, BoxLayout.LINE_AXIS));
		nocoverImagePanel.setAlignmentX(LEFT_ALIGNMENT);


		JPanel nocoverCheckBoxPanel = new JPanel();
		nocoverCheckBoxPanel.setLayout(new BoxLayout(nocoverCheckBoxPanel, BoxLayout.PAGE_AXIS));

		ButtonGroup nocoverGroup = new ButtonGroup();

		pumaCover = new JRadioButton(Localizer.get("DialogPrefs.panel.cover-settings.nocover.use-puma")); //$NON-NLS-1$
		jaguarCover = new JRadioButton(Localizer.get("DialogPrefs.panel.cover-settings.nocover.use-jaguar")); //$NON-NLS-1$
		tigerCover = new JRadioButton(Localizer.get("DialogPrefs.panel.cover-settings.nocover.use-tiger"));  //$NON-NLS-1$

		if (config.getNoCoverType() == NoCoverType.Jaguar)
			jaguarCover.setSelected(true);
		else if (config.getNoCoverType() == NoCoverType.Tiger)
			tigerCover.setSelected(true);
		else
			pumaCover.setSelected(true);

		nocoverGroup.add(pumaCover);
		nocoverGroup.add(jaguarCover);
		nocoverGroup.add(tigerCover);

		nocoverCheckBoxPanel.add(pumaCover);
		nocoverCheckBoxPanel.add(jaguarCover);
		nocoverCheckBoxPanel.add(tigerCover);


		final JLabel coverLabel = new JLabel(new ImageIcon(FileUtil.getImage("/images/" + config.getNoCoverFilename()).getScaledInstance(config.getCoverAreaSize().width, config.getCoverAreaSize().height,Image.SCALE_SMOOTH))); //$NON-NLS-1$
		coverLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,0,0,0), BorderFactory.createEtchedBorder()));

		nocoverImagePanel.add(nocoverCheckBoxPanel);
		nocoverImagePanel.add(coverLabel);

		coverPanel.add(nocoverImagePanel);

		ActionListener coverListener = new ActionListener() {

			public void actionPerformed(ActionEvent a) {
				if (a.getSource().equals(jaguarCover)) {
					config.setNoCoverType(NoCoverType.Jaguar);
				}
				else if (a.getSource().equals(tigerCover)) {
					config.setNoCoverType(NoCoverType.Tiger);
				}
				else if (a.getSource().equals(pumaCover)) {
					config.setNoCoverType(NoCoverType.Puma);
				}
				coverLabel.setIcon(new ImageIcon(FileUtil.getImage("/images/" + config.getNoCoverFilename()).getScaledInstance(config.getCoverAreaSize().width, config.getCoverAreaSize().height,Image.SCALE_SMOOTH))); //$NON-NLS-1$
			}
		};

		jaguarCover.addActionListener(coverListener);
		tigerCover.addActionListener(coverListener);
		pumaCover.addActionListener(coverListener);

		if (MovieManager.getIt().getDatabase() != null && MovieManager.getIt().getDatabase().isMySQL()) {

			enableStoreCoversLocally = new JCheckBox(Localizer.get("DialogPrefs.panel.cover-settings.store-covers-locally")); //$NON-NLS-1$
			enableStoreCoversLocally.setActionCommand("Store covers locally"); //$NON-NLS-1$

			if (config.getStoreCoversLocally())
				enableStoreCoversLocally.setSelected(true);

			// Disabled in applet mode
			if (MovieManager.isApplet())
				enableStoreCoversLocally.setEnabled(false);

			coverPanel.add(enableStoreCoversLocally);
		}
		
		return coverPanel;
	}

	JPanel createMovieListPanel() {

		/* Movie List Options  */
		JPanel movieListPanel = new JPanel();
		movieListPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogPrefs.panel.movie-list.title")), BorderFactory.createEmptyBorder(0,10,5,10))); //$NON-NLS-1$
		movieListPanel.setLayout(new BoxLayout(movieListPanel, BoxLayout.PAGE_AXIS));

		/* Enable rightclick by ctrl key */
		enableRightclickByCtrl = new JCheckBox(Localizer.get("DialogPrefs.panel.movie-list.enable-right-click-by-ctrl-in-movie-list")); //$NON-NLS-1$
		enableRightclickByCtrl.setActionCommand("Enable right click by ctrl"); //$NON-NLS-1$

		if (config.getEnableCtrlMouseRightClick())
			enableRightclickByCtrl.setSelected(true);

		movieListPanel.add(enableRightclickByCtrl);


		/* Enable Use JTree Icons */
		enableUseJTreeIcons = new JCheckBox(Localizer.get("DialogPrefs.panel.movie-list.enable-icons-in-movie-list")); //$NON-NLS-1$
		enableUseJTreeIcons.setActionCommand("Enable JTree Icons"); //$NON-NLS-1$
		enableUseJTreeIcons.addActionListener(this);

		if (config.getUseJTreeIcons())
			enableUseJTreeIcons.setSelected(true);

		movieListPanel.add(enableUseJTreeIcons);

		/* Enable Use JTree Covers */
		enableUseJTreeCovers = new JCheckBox(Localizer.get("DialogPrefs.panel.movie-list.enable-covers-in-movie-list")); //$NON-NLS-1$
		enableUseJTreeCovers.setActionCommand("Enable JTree Covers"); //$NON-NLS-1$
		enableUseJTreeCovers.addActionListener(this);

		if (config.getUseJTreeCovers())
			enableUseJTreeCovers.setSelected(true);

		movieListPanel.add(enableUseJTreeCovers);


		// Rowheight including example
		JPanel rowHeightPanel = new JPanel();
		rowHeightPanel.setLayout(new BorderLayout());
		rowHeightPanel.setMinimumSize(new Dimension(0, 150));
		rowHeightPanel.setPreferredSize(new Dimension(0, 150));

		JTree movieList = MovieManager.getDialog().getMoviesList();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) movieList.getModel().getRoot();

		int limit = movieList.getModel().getChildCount(root);

		if (limit > 7)
			limit = 7;

		DefaultMutableTreeNode exampleRoot = new DefaultMutableTreeNode("root"); //$NON-NLS-1$

		// Seems like a treenode cannot be added to more than one treemodel at a time.....???
		for (int i = 0; i < limit; i++)
			exampleRoot.add(new DefaultMutableTreeNode(new ModelMovie((ModelMovie) ((DefaultMutableTreeNode) movieList.getModel().getChild(root, i)).getUserObject()))); //$NON-NLS-1$ //$NON-NLS-2$

		exampleTree = new JTree(exampleRoot) {

			protected void paintComponent(Graphics g) {

				int[] rows = getSelectionRows();

				if (rows != null && rows.length > 0) {
					Rectangle b = getRowBounds(rows[0]);

					g.setColor(UIManager.getColor("Tree.selectionBackground")); //$NON-NLS-1$
					g.fillRect(0, b.y, getWidth(), b.height);
				}
				super.paintComponent(g);
			}
		};

		JScrollPane scroller = new JScrollPane(exampleTree);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scroller.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		scroller.setWheelScrollingEnabled(false);

		// handle mouse scrolling
		scroller.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				handleMouseScrolling(e);			
			}
		});

		exampleConfig.setUseRelativeCoversPath(config.getUseRelativeCoversPath());

		exampleTree.setRootVisible(false);
		exampleTree.setShowsRootHandles(true);
		exampleTree.setCellRenderer(new ExtendedTreeCellRenderer(exampleTree, scroller, exampleConfig));
		exampleTree.setOpaque(false);

		//Avoids NullPointer on Synthetica L&F.
		//scroller.getViewport().setBackground(UIManager.getColor("ScrollPane.background"));

		rowHeightSlider = new JSlider(6, 300, config.getMovieListRowHeight());
		rowHeightSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateRowHeightExample();
			}
		});
		JPanel p = new JPanel(new BorderLayout());
		p.add(new JLabel("Scale"), BorderLayout.WEST); //$NON-NLS-1$
		p.add(rowHeightSlider, BorderLayout.CENTER);
		rowHeightPanel.add(p, BorderLayout.SOUTH);
		rowHeightPanel.add(scroller, BorderLayout.CENTER);
		movieListPanel.add(rowHeightPanel);
		updateRowHeightExample();

		return movieListPanel;
	}

	JPanel createMediaPlayerPanel() {
		
		/* Player panel */
		JPanel playerPanel = new JPanel();
		playerPanel.setLayout(new GridBagLayout());

		playerPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Localizer.get("DialogPrefs.panel.external-programs.media-player.title")), //$NON-NLS-1$
				BorderFactory.createEmptyBorder(0,5,0,5)));

		enableUseDefaultWindowsPlayer = new JCheckBox(Localizer.get("DialogPrefs.panel.external-programs.media-player.use-default-windows-player")); //$NON-NLS-1$
		enableUseDefaultWindowsPlayer.setSelected(config.getUseDefaultWindowsPlayer());

		if (!SysUtil.isWindows())
			enableUseDefaultWindowsPlayer.setEnabled(false);

		GridBagConstraints c;
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		playerPanel.add(enableUseDefaultWindowsPlayer, c);

		JLabel playerLabel = new JLabel(Localizer.get("DialogPrefs.panel.external-programs.media-player.player-location")); //$NON-NLS-1$

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		playerPanel.add(playerLabel, c);


		mediaPlayerPathField = new JTextField(30);
		mediaPlayerPathField.setText(config.getMediaPlayerPath());

		JButton mediaPlayerBrowse = new JButton(Localizer.get("common.button.browse")); //$NON-NLS-1$

		mediaPlayerBrowse.setActionCommand(Localizer.get("DialogPrefs.panel.external-programs.media-player.browse-player-path")); //$NON-NLS-1$
		mediaPlayerBrowse.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				// check if there is a file selected

				String path = mediaPlayerPathField.getText();
				File parent = new File(path).getParentFile();

				if (parent != null && parent.isDirectory())
					path = parent.getParent();
				else 
					path = ""; //$NON-NLS-1$

				JFileChooser chooser = new JFileChooser(path);
				int returnVal = chooser.showDialog(null, Localizer.get("common.button.choose")); //$NON-NLS-1$
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				try {
					String location = chooser.getSelectedFile().getCanonicalPath();

					if (location != null)
						mediaPlayerPathField.setText(location);
				} catch (IOException e) {
					log.warn(Localizer.get("DialogPrefs.panel.external-programs.media-player.failed-to-retrieve-player-path")); //$NON-NLS-1$
				}

			}});

		JPanel mediaPlayerFilePanel = new JPanel ();
		mediaPlayerFilePanel.setLayout(new BoxLayout(mediaPlayerFilePanel, BoxLayout.X_AXIS));
		mediaPlayerFilePanel.add(mediaPlayerPathField);
		mediaPlayerFilePanel.add(mediaPlayerBrowse);

		c.gridx = 0;
		c.gridy = 2;
		playerPanel.add(mediaPlayerFilePanel, c);


		JLabel cmdArgLabel = new JLabel(Localizer.get("DialogPrefs.panel.external-programs.media-player.command-line-arguments")); //$NON-NLS-1$
		mediaPlayerCmdArgument = new JTextField(15);
		mediaPlayerCmdArgument.setText(config.getMediaPlayerCmdArgument());

		JPanel cmdArg = new JPanel();
		cmdArg.add(cmdArgLabel);
		cmdArg.add(mediaPlayerCmdArgument);

		c.gridx = 0;
		c.gridy = 3;
		playerPanel.add(cmdArg, c);


		externalCmd = new JCheckBox(Localizer.get("common.button.enable")); //$NON-NLS-1$
		externalCmd.setSelected(config.getExecuteExternalPlayCommand());

		JLabel externalCmdLabel = new JLabel("<html>"+Localizer.get("DialogPrefs.panel.external-programs.media-player.execute-external-command.label.text")+"</html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		externCommandInfo = new JButton(Localizer.get("common.button.read-more")); //$NON-NLS-1$

		externCommandInfo.addActionListener(this);


		// External command
		JPanel externalCommand = new JPanel();

		//externalCommand.setBorder(BorderFactory.createCompoundBorder(
		//		BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Localizer.get("DialogPrefs.panel.external-programs.media-player.execute-external-command.enable")), //$NON-NLS-1$
		//		BorderFactory.createEmptyBorder(0,0,0,0)));
		externalCommand.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Localizer.get("DialogPrefs.panel.external-programs.media-player.execute-external-command.enable"))); //$NON-NLS-1$
		
		externalCommand.add(externalCmd);
		externalCommand.add(externalCmdLabel);
		externalCommand.add(externCommandInfo);

		c.gridx = 0;
		c.gridy = 4;
		playerPanel.add(externalCommand, c);
		
		return playerPanel;
	}
	
	JPanel createBrowserPanel() {
		
		/* Browser Path */
		JPanel browserPanel = new JPanel();
		browserPanel.setLayout(new GridBagLayout());
		
		browserPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Localizer.get("DialogPrefs.panel.external-programs.web-browser.title")), //$NON-NLS-1$
				BorderFactory.createEmptyBorder(0,0,5,0)));

		JPanel browserOptionPanel = new JPanel(new GridLayout(3,4));

		enableUseDefaultWindowsBrowser = new JRadioButton(Localizer.get("DialogPrefs.panel.external-programs.web-browser.windows-default")); //$NON-NLS-1$
		enableCustomBrowser =            new JRadioButton(Localizer.get("DialogPrefs.panel.external-programs.web-browser.custom-browser")); //$NON-NLS-1$
		browserOptionOpera =             new JRadioButton("Opera"); //$NON-NLS-1$
		browserOptionFirefox =           new JRadioButton("Firefox"); //$NON-NLS-1$
		browserOptionMozilla =           new JRadioButton("Mozilla"); //$NON-NLS-1$
		browserOptionSafari =            new JRadioButton("Safari"); //$NON-NLS-1$
		browserOptionNetscape =          new JRadioButton("Netscape"); //$NON-NLS-1$
		browserOptionIE =                new JRadioButton("Internet Explorer"); //$NON-NLS-1$


		String browser = config.getSystemWebBrowser();

		if (browser.equals("Default")) //$NON-NLS-1$
			enableUseDefaultWindowsBrowser.setSelected(true);
		else if (browser.equals("Custom")) //$NON-NLS-1$
			enableCustomBrowser.setSelected(true);
		else if (browser.equals("Opera")) //$NON-NLS-1$
			browserOptionOpera.setSelected(true);
		else if (browser.equals("Firefox")) //$NON-NLS-1$
			browserOptionFirefox.setSelected(true);
		else if (browser.equals("Mozilla")) //$NON-NLS-1$
			browserOptionMozilla.setSelected(true);
		else if (browser.equals("Safari")) //$NON-NLS-1$
			browserOptionSafari.setSelected(true);
		else if (browser.equals("Google Chrome")) //$NON-NLS-1$
			browserOptionSafari.setSelected(true);
		else if (browser.equals("Netscape")) //$NON-NLS-1$
			browserOptionNetscape.setSelected(true);
		else if (browser.equals("IE")) //$NON-NLS-1$
			browserOptionIE.setSelected(true);


		ButtonGroup browserOptionGroup = new ButtonGroup();
		browserOptionGroup.add(browserOptionOpera);
		browserOptionGroup.add(browserOptionMozilla);
		browserOptionGroup.add(browserOptionFirefox);
		browserOptionGroup.add(browserOptionNetscape);
		browserOptionGroup.add(browserOptionSafari);
		//browserOptionGroup.add(browserOptionGoogleChrome);
		browserOptionGroup.add(enableCustomBrowser);
		browserOptionGroup.add(enableUseDefaultWindowsBrowser);
		browserOptionGroup.add(browserOptionIE);

		browserOptionPanel.add(browserOptionOpera);
		browserOptionPanel.add(browserOptionMozilla);
		browserOptionPanel.add(browserOptionFirefox);
		browserOptionPanel.add(browserOptionNetscape);
		browserOptionPanel.add(browserOptionSafari);
		//browserOptionPanel.add(browserOptionGoogleChrome);
		browserOptionPanel.add(enableCustomBrowser);
		browserOptionPanel.add(enableUseDefaultWindowsBrowser);
		browserOptionPanel.add(browserOptionIE);

		browserOptionOpera.addItemListener(this);
		browserOptionMozilla.addItemListener(this);
		browserOptionFirefox.addItemListener(this);
		browserOptionNetscape.addItemListener(this);
		browserOptionSafari.addItemListener(this);
		//browserOptionGoogleChrome.addItemListener(this);
		enableCustomBrowser.addItemListener(this);
		enableUseDefaultWindowsBrowser.addItemListener(this);
		browserOptionIE.addItemListener(this);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		browserPanel.add(browserOptionPanel, c);


		JLabel browserLabel = new JLabel(Localizer.get("DialogPrefs.panel.external-programs.web-browser.custom-browser.location")); //$NON-NLS-1$

		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		browserPanel.add(browserLabel, c);

		customBrowserPathField = new JTextField(30);
		customBrowserPathField.setText(config.getBrowserPath());

		browserBrowse = new JButton(Localizer.get("common.button.browse")); //$NON-NLS-1$
		browserBrowse.setActionCommand("Browse Path"); //$NON-NLS-1$

		browserBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// check if there is a file selected

				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showDialog(null, Localizer.get("common.button.choose")); //$NON-NLS-1$
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				try {
					String location = chooser.getSelectedFile().getCanonicalPath();

					if (location != null)
						customBrowserPathField.setText(location);
				} catch (IOException e) {
					log.warn("Failed to retrieve browser path"); //$NON-NLS-1$
				}

			}});

		JPanel browserFilePanel = new JPanel();
		browserFilePanel.setLayout(new BoxLayout(browserFilePanel, BoxLayout.X_AXIS));
		browserFilePanel.add(customBrowserPathField);
		browserFilePanel.add(browserBrowse);

		c.gridx = 0;
		c.gridy = 2;
		browserPanel.add(browserFilePanel, c);
		browserPanel.add(Box.createVerticalGlue());


		setBrowserComponentsEnabled();
		
		return browserPanel;
	}
	
	
	
	JPanel createExternalProgramsPanel() {

		JPanel playerPanel = createMediaPlayerPanel();
		JPanel browserPanel = createBrowserPanel();
		
		/* Program Paths */

		JPanel programPathsPanel = new JPanel();
		programPathsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Localizer.get("DialogPrefs.panel.external-programs.title")), //$NON-NLS-1$
				BorderFactory.createEmptyBorder(5,3,5,3)));

		programPathsPanel.setLayout(new BoxLayout(programPathsPanel, BoxLayout.Y_AXIS));
		
		if (!disabledFeatures.isPreferencesExternalProgramsPlayerDisabled())
			programPathsPanel.add(playerPanel);
		
		programPathsPanel.add(browserPanel);
		
		return programPathsPanel;
	}

	JPanel createBackupPanel() {

		/* Backup panel */
		JPanel backupSettingsPanel = new JPanel();
		backupSettingsPanel.setLayout(new GridBagLayout());


		JLabel makeBackupEveryLaunchLabel = new JLabel(Localizer.get("DialogPrefs.panel.backup.backup-every-xth-time.text")); //$NON-NLS-1$
		makeBackupEveryLaunchField = new JTextField(4);
		makeBackupEveryLaunchField.setToolTipText(Localizer.get("DialogPrefs.panel.backup.backup-every-xth-time.tooltip")); //$NON-NLS-1$

		JLabel deleteOldestWhenSizeExcedesMBLabel = new JLabel(Localizer.get("DialogPrefs.panel.backup.delete-oldest-when-total-excedes-size.text")); //$NON-NLS-1$
		deleteOldestWhenSizeExcedesMBField = new JTextField(4);
		deleteOldestWhenSizeExcedesMBField.setToolTipText(Localizer.get("DialogPrefs.panel.backup.delete-oldest-when-total-excedes-size.tooltip")); //$NON-NLS-1$

		JPanel everyTimePanel = new JPanel();
		everyTimePanel.add(makeBackupEveryLaunchLabel);
		everyTimePanel.add(makeBackupEveryLaunchField);

		GridBagConstraints c;
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		backupSettingsPanel.add(everyTimePanel, c);


		JPanel deleteOldest = new JPanel();
		deleteOldest.add(deleteOldestWhenSizeExcedesMBLabel);
		deleteOldest.add(deleteOldestWhenSizeExcedesMBField);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		backupSettingsPanel.add(deleteOldest, c);

		JLabel directoryLabel = new JLabel(Localizer.get("DialogPrefs.panel.backup.bacup-directory-path")); //$NON-NLS-1$

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		backupSettingsPanel.add(directoryLabel, c);

		backupDirField = new JTextField(32) {
			
			@Override
			public void setText(String text) {
				super.setText(text);
				validateBackupDir();
			}			
		};		
		
		backupDirField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				validateBackupDir();
			}
		});
		
		JButton backupDirBrowse = new JButton(Localizer.get("common.button.browse")); //$NON-NLS-1$

		backupDirBrowse.setActionCommand("Browse Back Directory"); //$NON-NLS-1$
		backupDirBrowse.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg) {

				// check if there is a file selected
				String path = backupDirField.getText();
				File parent = new File(path).getParentFile();

				if (parent != null && parent.isDirectory())
					path = parent.getParent();
				else 
					path = ""; //$NON-NLS-1$

				JFileChooser chooser = new JFileChooser(path);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int returnVal = chooser.showOpenDialog(contentPane);

				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				try {
					String location = chooser.getSelectedFile().getCanonicalPath();

					if (location != null)
						backupDirField.setText(location);
				} catch (IOException e) {
					log.warn("Failed to retrieve player path"); //$NON-NLS-1$
				}
			}});

		JPanel backupDirPanel = new JPanel();
		backupDirPanel.setLayout(new BoxLayout(backupDirPanel, BoxLayout.X_AXIS));
		backupDirPanel.add(backupDirField);
		backupDirPanel.add(backupDirBrowse);

		c.gridx = 0;
		c.gridy = 1;
		backupSettingsPanel.add(backupDirPanel, c);

		makeBackupEveryLaunchField.setText(config.getDatabaseBackupEveryLaunch());  
		deleteOldestWhenSizeExcedesMBField.setText(config.getDatabaseBackupDeleteOldest());
		backupDirField.setText(config.getDatabaseBackupDirectory());
		validateBackupDir();
				
		warnAboutInvalidBackupDir = new JCheckBox("Warn me about invalid backup dir");
		warnAboutInvalidBackupDir.setSelected(config.getDatabaseBackupWarnInvalidDir());
		
		c.gridx = 0;
		c.gridy = 5;
		backupSettingsPanel.add(warnAboutInvalidBackupDir, c);
		
		/* Backup settings */

		JPanel backupPanel = new JPanel();
		backupPanel.setLayout(new BoxLayout(backupPanel, BoxLayout.Y_AXIS));

		backupPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Localizer.get("DialogPrefs.panel.backup.title")), //$NON-NLS-1$
				BorderFactory.createEmptyBorder(12,5,16,5)));

		backupPanel.add(backupSettingsPanel);

		return backupPanel;
	}

	void validateBackupDir() {
		File f = new File(backupDirField.getText());
				
		if (f.isDirectory())
			backupDirField.setBackground(Color.white);
		else
			backupDirField.setBackground(invalidPathColor);
	}
	
	JPanel createButtonPanel() {

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,5));
		
		buttonSave = new JButton(Localizer.get("DialogPrefs.panel.button-save.text")); //$NON-NLS-1$
		buttonSave.setToolTipText(Localizer.get("DialogPrefs.panel.button-save.tooltip")); //$NON-NLS-1$
		buttonSave.addActionListener(this);
		buttonSave.setActionCommand("Save"); //$NON-NLS-1$
		buttonPanel.add(buttonSave);

		buttonCancel = new JButton("Cancel"); //$NON-NLS-1$
		buttonCancel.setToolTipText("Close without saving settings"); //$NON-NLS-1$
		buttonCancel.addActionListener(this);
		buttonCancel.setActionCommand("Cancel"); //$NON-NLS-1$
		buttonPanel.add(buttonCancel);

		return buttonPanel;
	}

	

	/**
	 * Changes the current selected tab in the tabbed pane of the preferences dialog
	 * @param e
	 */
	void handleMouseScrolling(MouseWheelEvent e) {

		int notches = e.getWheelRotation();

		int index = tabbedPane.getSelectedIndex();
		int count = tabbedPane.getComponentCount();

		// Find new index
		if (notches < 0) {
			index++;
			index %= count;
		} else {
			index = (index == 0 ? count-1 : index-1);
		}

		tabbedPane.setSelectedIndex(index);
	}

	void setBrowserComponentsEnabled() {

		if (SysUtil.isLinux()) 
			browserOptionSafari.setEnabled(false);

		if (!SysUtil.isWindows())
			browserOptionIE.setEnabled(false);

		if (enableCustomBrowser.isSelected()) {
			customBrowserPathField.setEnabled(true);
			browserBrowse.setEnabled(true);
		}
		else {
			customBrowserPathField.setEnabled(false);
			browserBrowse.setEnabled(false);
		}

		if (!SysUtil.isWindows())
			enableUseDefaultWindowsBrowser.setEnabled(false);
	}

	private void updateRowHeightExample() {
		int rowHeight = rowHeightSlider.getValue();
		exampleConfig.setMovieListRowHeight(rowHeight);
		exampleConfig.setUseJTreeIcons(enableUseJTreeIcons.isSelected());
		exampleConfig.setUseJTreeCovers(enableUseJTreeCovers.isSelected());
		exampleConfig.setStoreCoversLocally(config.getStoreCoversLocally());
		exampleTree.setRowHeight(rowHeight + 2);
		exampleTree.updateUI();
	}


	boolean saveSettings() {

		/* Saving the tab index */
		config.setLastPreferencesTabIndex(tabbedPane.getSelectedIndex());

		/* Save proxy settings */
		config.setProxyType((String) proxyType.getSelectedItem());
				
		if (enableProxyButton.isSelected()) {
			
			if (hostTextField.getText().trim().equals("")) {
				showAlert("Missing host", "Proxy host cannot be empty!");
				return false;
			}
			else if (portTextField.getText().trim().equals("")) {
				showAlert("Missing port", "Proxy port cannot be empty!");
				return false;
			}
		
			config.setProxyHost(hostTextField.getText());
			config.setProxyPort(portTextField.getText());
						
			if (enableAuthenticationButton.isSelected()) {
			
				if (userNameTextField.getText().trim().equals("")) {
					showAlert("Missing user", "Proxy user name cannot be empty!");
					return false;
				}
				
				config.setProxyUser(userNameTextField.getText());
				config.setProxyPassword(new String(passwordTextField.getPassword()));
			}
			config.setProxyAuthenticationEnabled(enableAuthenticationButton.isSelected());
		}
		config.setProxyEnabled(enableProxyButton.isSelected());
		
		if (config.getIMDbAuthenticationEnabled() != enableIMDbAuthenticationButton.isSelected() ||
				!config.getIMDbAuthenticationUser().equals(IMDbUserNameTextField.getText()) ||
				!config.getIMDbAuthenticationPassword().equals(new String(IMDbPasswordTextField.getPassword()))) {
			MovieManager.getConfig().resetIMDbAuth();
		}

		config.setIMDbAuthenticationEnabled(enableIMDbAuthenticationButton.isSelected());
		config.setIMDbAuthenticationUser(IMDbUserNameTextField.getText());
		config.setIMDbAuthenticationPassword(new String(IMDbPasswordTextField.getPassword()));

		config.setMediaPlayerPath(mediaPlayerPathField.getText());
		config.setMediaPlayerCmdArgument(mediaPlayerCmdArgument.getText());
		config.setBrowserPath(customBrowserPathField.getText());

		config.setUseDefaultWindowsPlayer(enableUseDefaultWindowsPlayer.isSelected());

		config.setExecuteExternalPlayCommand(externalCmd.isSelected());

		config.setLocale((String) langauges.getSelectedItem());

		/* Web Browser */
		if (enableUseDefaultWindowsBrowser.isSelected())
			config.setSystemWebBrowser("Default"); //$NON-NLS-1$
		else if (browserOptionOpera.isSelected())
			config.setSystemWebBrowser("Opera"); //$NON-NLS-1$
		else if (browserOptionFirefox.isSelected())
			config.setSystemWebBrowser("Firefox"); //$NON-NLS-1$
		else if (browserOptionMozilla.isSelected())
			config.setSystemWebBrowser("Mozilla"); //$NON-NLS-1$
		else if (browserOptionSafari.isSelected())
			config.setSystemWebBrowser("Safari"); //$NON-NLS-1$
		else if (browserOptionNetscape.isSelected())
			config.setSystemWebBrowser("Netscape"); //$NON-NLS-1$
		else if (browserOptionIE.isSelected())
			config.setSystemWebBrowser("IE"); //$NON-NLS-1$
		else if (enableCustomBrowser.isSelected()) {
			config.setSystemWebBrowser("Custom"); //$NON-NLS-1$

			File browser = new File(customBrowserPathField.getText());

			if (!browser.isFile()) {
				DialogAlert alert = new DialogAlert(this, Localizer.get("DialogPrefs.alert.title.alert"), Localizer.get("DialogPrefs.panel.backup.alert.invalid-browser-path.text")); //$NON-NLS-1$ //$NON-NLS-2$ 
				GUIUtil.showAndWait(alert, true);
				return false;
			}
			config.setBrowserPath(browser.getAbsolutePath());
		}

		String tmp;
		tmp = makeBackupEveryLaunchField.getText();

		if (tmp.equals("")) //$NON-NLS-1$
			tmp = "0"; //$NON-NLS-1$

		config.setDatabaseBackupEveryLaunch(tmp);

		tmp = deleteOldestWhenSizeExcedesMBField.getText();

		if (tmp.equals("")) //$NON-NLS-1$
			tmp = "0"; //$NON-NLS-1$

		config.setDatabaseBackupDeleteOldest(tmp);

		config.setDatabaseBackupDirectory(backupDirField.getText());
		config.setDatabaseBackupWarnInvalidDir(warnAboutInvalidBackupDir.isSelected());
		
		
		config.setRemoveQuotesOnSeriesTitle(removeQuotesFromSeriesTitle.isSelected());

		/* automatic move of 'The' */
		config.setAutoMoveThe(enableAutoMoveThe.isSelected());

		/* automatic move of 'An and A' */
		config.setAutoMoveAnAndA(enableAutoMoveAnAndA.isSelected());

		config.setStoreAllAkaTitles(storeAllAvailableAkaTitles.isSelected());

		config.setIncludeAkaLanguageCodes(includeAkaLanguageCodes.isSelected());

		if (useLanguageSpecificTitle.isSelected()) {
			config.setUseLanguageSpecificTitle(true);

			String value = (String) languageCodeSelector.getSelectedItem();
			value = value.substring(1, 3);
			config.setTitleLanguageCode(value);
		}
		else
			config.setUseLanguageSpecificTitle(false);


		/* automatically load database at startup */
		config.setLoadDatabaseOnStartup(loadDatabaseOnStartUp.isSelected());

		/* seen editable */
		config.setSeenEditable(enableSeenEditable.isSelected());

		/* Display Queries In JTree */
		config.setUseDisplayQueriesInTree(displayQueriesInTree.isSelected());

		/* Check for updates */
		config.setCheckForProgramUpdates(checkForProgramUpdates.isSelected());

		config.setDisplayPlayButton(enablePlayButton.isSelected());

		config.setHTMLViewDebugMode(checkEnableHTMLViewDebugMode.isSelected());

		if (checkEnableMySQLSocketTimeout != null)
			config.setMySQLSocketTimeoutEnabled(checkEnableMySQLSocketTimeout.isSelected());

		/* rightclick by ctrl */
		config.setEnableCtrlMouseRightClick(enableRightclickByCtrl.isSelected());

		/* Load Last List At Startup */
		config.setLoadLastUsedListAtStartup(enableLoadLastUsedList.isSelected());

		/* Add new movies to current lists */
		config.setAddNewMoviesToCurrentLists(enableAddNewMoviesToCurrentLists.isSelected());

		/* Icons in JTree */
		config.setUseJTreeIcons(enableUseJTreeIcons.isSelected());

		/* Covers in JTree */
		config.setUseJTreeCovers(enableUseJTreeCovers.isSelected());

		/* Rowheight */
		config.setMovieListRowHeight(rowHeightSlider.getValue());

		if (enablePreserveCoverRatioEpisodesOnly.isSelected())
			config.setPreserveCoverAspectRatio(2);
		else if (enablePreserveCoverRatio.isSelected())
			config.setPreserveCoverAspectRatio(1);
		else
			config.setPreserveCoverAspectRatio(0);

		if (pumaCover.isSelected())
			config.setNoCoverType(NoCoverType.Puma);
		else if (tigerCover.isSelected())
			config.setNoCoverType(NoCoverType.Tiger);
		else
			config.setNoCoverType(NoCoverType.Jaguar);

		if (enableStoreCoversLocally != null && enableStoreCoversLocally.isSelected()) {

			if (!(new File(config.getCoversFolder()).isDirectory())) {

				DialogAlert alert = new DialogAlert(this, Localizer.get("DialogPrefs.alert.title.alert"), Localizer.get("DialogPrefs.alert.message.covers-dir-not-existing")); //$NON-NLS-1$ //$NON-NLS-2$
				GUIUtil.showAndWait(alert, true);

				// need to save enabled option to allow DialogFolders to determine whether to check paths or not
				config.setStoreCoversLocally(true); 

				DialogFolders dialogFolders = new DialogFolders();
				GUIUtil.showAndWait(dialogFolders, true);

				if (!(new File(config.getCoversFolder()).isDirectory())) {
					enableStoreCoversLocally.setSelected(false);
				}
			}

			config.setStoreCoversLocally(enableStoreCoversLocally.isSelected());
		}
		else
			config.setStoreCoversLocally(false);

		return true;
	}

	void showAlert(String title, String message) {
		DialogAlert alert = new DialogAlert(this, title, message); //$NON-NLS-1$ //$NON-NLS-2$ 
		GUIUtil.showAndWait(alert, true);
	}

	void setLookAndFeel(final LookAndFeelType type) {

		final DialogPrefs prefs = this;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				prefs.setCursor(new Cursor(Cursor.WAIT_CURSOR));

				switch (type) {
				case CustomLaF: {setCustomLookAndFeel(0); break;}
				case SkinlfLaF: {setSkinlfLookAndFeel(); break;}
				case Substance: {setSubstanceLookAndFeel(); break;}
				case NimROD:    {setNimRODLookAndFeel(); break;}
				}
				MovieManager.getDialog().updateLookAndFeelValues();
				prefs.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
	}


	void setCustomLookAndFeel(int counter) {

		String selectedLaf = (String) lafChooser.getSelectedItem();
		
		try {
			
			lafManager.setCustomLaf(selectedLaf);
			updateLookAndFeel();

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e); //$NON-NLS-1$

			String lafName = (String) lafChooser.getSelectedItem();
			lafChooser.setSelectedItem(lafManager.getCustomLookAndFeel());

			/* Calls itself recursively to restore the old look and feel */
			if (counter < 1) {
				showErrorMessage(e.getMessage(), lafName);
				setCustomLookAndFeel(counter+1);
			}
			else {
				showErrorMessage("You're advised to restart the application",""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return;
		}
		lafManager.setCustomLookAndFeel(selectedLaf);
	}
	

	void setSkinlfLookAndFeel() {
		String selectedItem = (String) skinlfThemePackChooser.getSelectedItem() + ".zip"; //$NON-NLS-1$
	
		try {
			lafManager.setSkinlfLookAndFeel(selectedItem);
			updateLookAndFeel();
		} catch (Exception e) {
			log.error("Exception: "+ e.getMessage()); //$NON-NLS-1$
			showErrorMessage(e.getMessage(), "SkinLF"); //$NON-NLS-1$
			return;
		}
		lafManager.setSkinlfThemePack(selectedItem);
	}

	void setNimRODLookAndFeel() {
		String selectedItem = (String) nimrodChooser.getSelectedItem(); //$NON-NLS-1$
	
		try {
			lafManager.setNimRODLookAndFeel(selectedItem);
			updateLookAndFeel();
		} catch (Exception e) {
			log.error("Exception: "+ e.getMessage()); //$NON-NLS-1$
			showErrorMessage(e.getMessage(), "NimROD"); //$NON-NLS-1$
			return;
		}
		lafManager.setNimRODTheme(selectedItem);
	}

	
	void setSubstanceLookAndFeel() {
		String selectedItem = (String) substanceChooser.getSelectedItem(); //$NON-NLS-1$
	
		try {
			lafManager.setSubstanceLookAndFeel(selectedItem);
			updateLookAndFeel();

		} catch (Exception e) {
			log.error("Exception: "+ e.getMessage(), e); //$NON-NLS-1$
			showErrorMessage(e.getMessage(), "Substance"); //$NON-NLS-1$
			return;
		}
		lafManager.setSubstanceSkin(selectedItem);
	}

	
	void showErrorMessage(String error, String name) {

		String message = Localizer.get("DialogPrefs.alert.message.laf-improperly-installed-or-not-supported-by-jre")+  //$NON-NLS-1$
		System.getProperty("java.version") + //$NON-NLS-1$ //$NON-NLS-2$
		Localizer.get("DialogPrefs.panel.backup.alert.adviced-to-restart-application"); //$NON-NLS-1$

		if (name.equals("")) //$NON-NLS-1$
			message = Localizer.get("DialogPrefs.alert.message.laf.this")+ message; //$NON-NLS-1$
		else
			message = name + message;

		if (error != null && error.indexOf("not supported on this platform") != -1) //$NON-NLS-1$
			message = Localizer.get("DialogPrefs.alert.message.laf-not-supported"); //$NON-NLS-1$

		if (error != null && error.indexOf("You're advised to restart the application") != -1) { //$NON-NLS-1$
			message = Localizer.get("DialogPrefs.alert.message.advised-to-restart-application"); //$NON-NLS-1$
			error = ""; //$NON-NLS-1$
		}


		DialogAlert alert = new DialogAlert(this, Localizer.get("DialogPrefs.alert.title.laf-error"), message, error); //$NON-NLS-1$
		GUIUtil.showAndWait(alert, true);
	}


	void setLafChooserPreferredSize() {

		if (skinlfThemePackChooser != null)
			skinlfThemePackChooser.setPreferredSize(new Dimension(lafChooser.getPreferredSize()));
	}

	void updateLookAndFeel() throws Exception {
		updateLookAndFeel(this);
	}
	
	public static void updateLookAndFeel(final DialogPrefs pref) throws Exception {

		Runnable r = new Runnable() {

			public void run() {
				MovieManager.getDialog().updateLookAndFeelValues();

				SwingUtilities.updateComponentTreeUI(MovieManager.getDialog());

				MovieManager.getDialog().resetInfoFieldsDisplay();
				MovieManager.getDialog().validate();

				/*If the search dialog is opened it will be updated*/
				if (DialogSearch.getDialogSearch() != null) {
					SwingUtilities.updateComponentTreeUI(DialogSearch.getDialogSearch());
					DialogSearch.getDialogSearch().pack();
				}

				if (pref != null) {
					SwingUtilities.updateComponentTreeUI(pref);
					pref.pack();
					pref.setLafChooserPreferredSize();
				}
			}
		};
		
		if (SwingUtilities.isEventDispatchThread())
			r.run();
		else
			SwingUtilities.invokeLater(r);
	}


	void setDefaultLookAndFeelDecoration(boolean decorated) {

		if (decorated) {
			lafManager.setDefaultLookAndFeelDecorated(true);

			javax.swing.JFrame.setDefaultLookAndFeelDecorated(true);
			javax.swing.JDialog.setDefaultLookAndFeelDecorated(true);

			MovieManager.getDialog().dispose();
			MovieManager.getDialog().setUndecorated(true);
			MovieManager.getDialog().getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.FRAME);
			GUIUtil.show(MovieManager.getDialog(), true);

			/* Updating DialogSearch if open */
			if (DialogSearch.getDialogSearch() != null) {
				DialogSearch.getDialogSearch().dispose();
				DialogSearch.getDialogSearch().setUndecorated(true);
				DialogSearch.getDialogSearch().getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.PLAIN_DIALOG);
				DialogSearch.getDialogSearch().pack();

				GUIUtil.show(DialogSearch.getDialogSearch(), true);
			}

			/* This */
			dispose();
			setUndecorated(true);
			getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.FRAME);
			pack();
			GUIUtil.show(this, true);
		}
		else {
			lafManager.setDefaultLookAndFeelDecorated(false);

			javax.swing.JFrame.setDefaultLookAndFeelDecorated(false);
			javax.swing.JDialog.setDefaultLookAndFeelDecorated(false);

			MovieManager.getDialog().dispose();
			MovieManager.getDialog().setUndecorated(false);
			MovieManager.getDialog().getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.NONE);
			GUIUtil.show(MovieManager.getDialog(), true);

			/* Updating DialogSearch if open */
			if (DialogSearch.getDialogSearch() != null) {
				DialogSearch.getDialogSearch().dispose();
				DialogSearch.getDialogSearch().setUndecorated(false);
				DialogSearch.getDialogSearch().getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.NONE);
				DialogSearch.getDialogSearch().pack();
				GUIUtil.show(DialogSearch.getDialogSearch(), true);
			}

			/* This */
			dispose();
			setUndecorated(false);
			getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.NONE);
			pack();

			GUIUtil.show(this, true);
		}
	}

	private void updateEnabling() {

		if(enableUseJTreeIcons.isSelected()) {
			enableUseJTreeCovers.setEnabled(true);
		}
		else {
			enableUseJTreeCovers.setEnabled(false);
			enableUseJTreeCovers.setSelected(false);
		}
	}

	public void actionPerformed(ActionEvent event) {
		log.debug("ActionPerformed: "+ event.getActionCommand()); //$NON-NLS-1$

		/* OK - Saves settings */
		if (event.getSource().equals(buttonSave)) { //$NON-NLS-1$

			if (saveSettings()) {
				dispose();

				MovieManager.getDialog().updateJTreeIcons();

				/* Necessary to update the icons in the movielist */
				//MovieManager.getDialog().getMoviesList().updateUI();
				MovieManagerCommandSelect.execute();

				return;
			}
		}

		/* Cancel - close */
		if (event.getSource().equals(buttonCancel)) { //$NON-NLS-1$
			dispose();
		}

		if (event.getActionCommand().equals("Enable JTree Icons") || event.getActionCommand().equals("Enable JTree Covers")) { //$NON-NLS-1$ //$NON-NLS-2$
			updateEnabling();
			updateRowHeightExample();
		}

		if (event.getActionCommand().equals("Enable LookAndFeel")) { //$NON-NLS-1$

			if (enableSkinlf != null && enableSkinlf.isSelected()) {

				skinlfThemePackChooser.setEnabled(true);
				lafChooser.setEnabled(false);
				substanceChooser.setEnabled(false);
				nimrodChooser.setEnabled(false);
				
				lafManager.setLookAndFeelType(LookAndFeelType.SkinlfLaF);
				setLookAndFeel(LookAndFeelType.SkinlfLaF);

			}
			else if (enableSubstanceChooser.isSelected()) {
				substanceChooser.setEnabled(true);
				lafChooser.setEnabled(false);
				skinlfThemePackChooser.setEnabled(false);
				nimrodChooser.setEnabled(false);
				
				lafManager.setLookAndFeelType(LookAndFeelType.Substance);
				setLookAndFeel(LookAndFeelType.Substance);
			}
			else if (enableNimrodChooser.isSelected()) {
				substanceChooser.setEnabled(false);
				nimrodChooser.setEnabled(true);
				lafChooser.setEnabled(false);
				skinlfThemePackChooser.setEnabled(false);
				
				lafManager.setLookAndFeelType(LookAndFeelType.NimROD);
				setLookAndFeel(LookAndFeelType.NimROD);
			}
			else {
				
				lafChooser.setEnabled(true);

				substanceChooser.setEnabled(false);
				
				if (skinlfThemePackChooser != null)
					skinlfThemePackChooser.setEnabled(false);

				lafManager.setLookAndFeelType(LookAndFeelType.CustomLaF);
				setLookAndFeel(LookAndFeelType.CustomLaF);
			}
		}

		
		
		if (event.getSource().equals(externCommandInfo)) { //$NON-NLS-1$
			DialogInfo info = new DialogInfo(this, Localizer.get("DialogPrefs.panel.external-programs.media-player.execute-external-command.info.title"),  //$NON-NLS-1$
					"<html>"+"When clicking the play button, the default media player is usually started. <br> " + //$NON-NLS-1$ //$NON-NLS-2$
					"With this option enabled, the directory of the media file is first checked for a file <br>" + //$NON-NLS-1$
					"with the same name as the media file, but ending in \"xmm.sh\" or \"xmm.bat\". If such a file exists, <br>" + //$NON-NLS-1$
					"the file content will be executed as it would be done on the command line. If no such file <br>" + //$NON-NLS-1$
			"is found, the media player will be executed instead."+"</html>"); //$NON-NLS-1$ //$NON-NLS-2$
			GUIUtil.showAndWait(info, true);
		}
		

		if (event.getActionCommand().equals("SeenIcon")) { //$NON-NLS-1$

			if (regularSeenIcon.isSelected()) {
				MovieManager.getDialog().getSeen().setIcon(new ImageIcon(FileUtil.getImage("/images/unseen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
				MovieManager.getDialog().getSeen().setSelectedIcon(new ImageIcon(FileUtil.getImage("/images/seen.png").getScaledInstance(18,18,Image.SCALE_SMOOTH))); //$NON-NLS-1$
				config.setUseRegularSeenIcon(true);

			}
			else {
				MovieManager.getDialog().getSeen().setIcon(null);
				MovieManager.getDialog().getSeen().setSelectedIcon(null);
				config.setUseRegularSeenIcon(false);
			}

			MovieManager.getDialog().getSeen().updateUI();
		}

		if (event.getActionCommand().equals("DefaultLafDecorated")) { //$NON-NLS-1$
			setDefaultLookAndFeelDecoration(defaultLafDecoratedButton.isSelected());
		}

		if (event.getSource().equals(lafChooser)) {
			setLookAndFeel(LookAndFeelType.CustomLaF);
		}
		
		if (event.getSource().equals(skinlfThemePackChooser)) {
			setLookAndFeel(LookAndFeelType.SkinlfLaF);
		}

		if (event.getSource().equals(substanceChooser)) {
			setLookAndFeel(LookAndFeelType.Substance);
		}
		
		if (event.getSource().equals(nimrodChooser)) {
			setLookAndFeel(LookAndFeelType.NimROD);
		}
		
		MovieManager.getDialog().getMoviesList().requestFocus(true);
	}


	public void itemStateChanged(ItemEvent event) {

		Object source = event.getItemSelectable();

		if (source.equals(enableProxyButton)) {

			if (enableProxyButton.isSelected()) {
				proxyTypeLabel.setEnabled(true);
				proxyType.setEnabled(true);
				hostTextField.setEnabled(true);
				hostLabel.setEnabled(true);
				portTextField.setEnabled(true);
				portLabel.setEnabled(true);
				enableAuthenticationButton.setEnabled(true);

				if (enableAuthenticationButton.isSelected()) {
					userNameTextField.setEnabled(true);
					userNameLabel.setEnabled(true);
					passwordTextField.setEnabled(true);
					passwordLabel.setEnabled(true);
				}
				else {
					userNameTextField.setEnabled(false);
					userNameLabel.setEnabled(false);
					passwordTextField.setEnabled(false);
					passwordLabel.setEnabled(false);
				}
			}
			else {
				proxyTypeLabel.setEnabled(false);
				proxyType.setEnabled(false);
				hostTextField.setEnabled(false);
				hostLabel.setEnabled(false);
				portTextField.setEnabled(false);
				portLabel.setEnabled(false);

				enableAuthenticationButton.setEnabled(false);
				userNameTextField.setEnabled(false);
				userNameLabel.setEnabled(false);
				passwordTextField.setEnabled(false);
				passwordLabel.setEnabled(false);
			}
		}

		if (source.equals(enableAuthenticationButton)) {

			if (enableProxyButton.isSelected() && enableAuthenticationButton.isSelected()) {
				userNameTextField.setEnabled(true);
				userNameLabel.setEnabled(true);
				passwordTextField.setEnabled(true);
				passwordLabel.setEnabled(true);
			}
			else {
				userNameTextField.setEnabled(false);
				userNameLabel.setEnabled(false);
				passwordTextField.setEnabled(false);
				passwordLabel.setEnabled(false);
			}
		}

		// IMDb authentication
		if (source.equals(enableIMDbAuthenticationButton)) {
			IMDbUserNameLabel.setEnabled(enableIMDbAuthenticationButton.isSelected());
			IMDbPasswordLabel.setEnabled(enableIMDbAuthenticationButton.isSelected());
			IMDbUserNameTextField.setEnabled(enableIMDbAuthenticationButton.isSelected());
			IMDbPasswordTextField.setEnabled(enableIMDbAuthenticationButton.isSelected());
		}


		if (source.equals(enablePreserveCoverRatioEpisodesOnly)) {

			if (enablePreserveCoverRatioEpisodesOnly.isSelected()) {
				enablePreserveCoverRatio.setSelected(false);
				enablePreserveCoverRatio.setEnabled(false);
			}
			else
				enablePreserveCoverRatio.setEnabled(true);
		}


		/* Misc - Aka title checkboxes */

		if (source.equals(useLanguageSpecificTitle)) {

			if (useLanguageSpecificTitle.isSelected())
				languageCodeSelector.setEnabled(true);
			else
				languageCodeSelector.setEnabled(false);
		}

		if (source.equals(browserOptionOpera)) {
			setBrowserComponentsEnabled();
		}

		if (source.equals(browserOptionMozilla)) {
			setBrowserComponentsEnabled();
		}

		if (source.equals(browserOptionFirefox)) {
			setBrowserComponentsEnabled();
		}

		if (source.equals(browserOptionNetscape)) {
			setBrowserComponentsEnabled();
		}

		if (source.equals(browserOptionSafari)) {
			setBrowserComponentsEnabled();
		}

		if (source.equals(enableCustomBrowser)) {
			setBrowserComponentsEnabled();
		}   
		if (source.equals(enableUseDefaultWindowsBrowser)) {
			setBrowserComponentsEnabled();
		} 
	}
	
	void setHotkeyModifiers() {

		try {
			
			GUIUtil.enableDisposeOnEscapeKey(shortcutManager, new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					config.setLastPreferencesTabIndex(tabbedPane.getSelectedIndex());
				}
			});
			
			shortcutManager.registerShowKeysKey();
			
			shortcutManager.setKeysToolTipComponent(tabbedPane);

			// ALT+S
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Save and Close", new AbstractAction() {
						public void actionPerformed(ActionEvent ae) {
							buttonSave.doClick();
							dispose();
						}
					}, buttonSave);


			// ALT+C
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Close", new AbstractAction() {
						public void actionPerformed(ActionEvent ae) {
							config.setLastPreferencesTabIndex(tabbedPane.getSelectedIndex());
							dispose();
						}
					}, buttonCancel);

		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
	}
}

