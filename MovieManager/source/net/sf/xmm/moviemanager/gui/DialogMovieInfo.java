/**
 * @(#)DialogMovieInfo.java
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandIMDBSearch;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandSelect;
import net.sf.xmm.moviemanager.fileproperties.FilePropertiesMovie;
import net.sf.xmm.moviemanager.models.AdditionalInfoFieldDefaultValues;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.models.ModelSeries;
import net.sf.xmm.moviemanager.swing.extentions.CoverTransferHandler;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedFileChooser;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedTreeCellRenderer;
import net.sf.xmm.moviemanager.swing.extentions.SteppedComboBox;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager;
import net.sf.xmm.moviemanager.swing.util.KeyboardShortcutManager.KeyMapping;
import net.sf.xmm.moviemanager.util.CustomFileFilter;
import net.sf.xmm.moviemanager.util.DocumentRegExp;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.events.ModelUpdatedEvent;
import net.sf.xmm.moviemanager.util.events.ModelUpdatedEventListener;
import net.sf.xmm.moviemanager.util.events.ModelUpdatedEvent.IllegalEventTypeException;

import net.sf.xmm.moviemanager.MovieManagerConfig.MediaInfoOption;

import org.apache.log4j.Logger;

public class DialogMovieInfo extends JDialog implements ModelUpdatedEventListener {

	Logger log = Logger.getLogger(getClass());
	
	private int fontSize = 12;

	private int valueComboBoxWidth = -1;

	private int valueComboBoxHeight = -1;

	private int EXTRA_START = 17;

	public List<PlainDocument> _fieldDocuments = new ArrayList<PlainDocument>();
	public List<String> _fieldUnits = new ArrayList<String>();

	KeyboardShortcutManager shortcutManager = new KeyboardShortcutManager(this);
	
	public ModelMovieInfo movieInfoModel;

	DialogMovieInfo dialogMovieInfo = this;
	
	private int _lastFieldIndex = -1; // the last additional info field that was displayed
	
	JTextField date;
	JTextField imdb;
	JTextField colour;
	JTextField movieTitle;
	JTextField directed;
	JTextField written;
	JTextField genre;
	JTextField rating;
	JTextField personalRating;
	JTextField country;
	JTextField language;
	JCheckBox seenBox;

	JLabel cover;

	private JTextArea textAreaPlot;
	private JTextArea textAreaCast;
	private JTextArea textAreaAka;
	private JTextArea textAreaCertification;
	private JTextField textFieldSoundMix;
	private JTextField textFieldAwards;
	private JTextField textFieldMpaa;
	private JTextField textFieldWebRuntime;

	private JComboBox additionalInfoFields;
	private JPanel additionalInfoValuePanel;
	private JPanel panelAdditionalInfo;
	private JLabel additionalInfoUnit;
	private JTextArea textAreaNotes;
	
	private JButton buttonSaveAndClose;
	JButton buttonSave;
	JButton buttonGetDVDInfo;
	JButton buttonGetFileInfo;
	JButton buttonGetIMDBInfo;
	JButton buttonCancel;
	
	JPanel panelMovieInfo;
	
	/**
	 * The Constructor - Add Movie.
	 */
	public DialogMovieInfo() {
		/* Dialog creation... */
		super(MovieManager.getDialog());
		
		setModelMovieInfo(new ModelMovieInfo(false));

		setUp(Localizer.get("DialogMovieInfo.title.add-movie"));
		loadEmptyAdditionalFields();

		updateGeneralInfoFromModel();
	}

	/**
	 * Add Episode.
	 */
	public DialogMovieInfo(ModelMovie model) throws Exception {
		/* Dialog creation... */
		super(MovieManager.getDialog());

		if (model.getKey() == -1)
			throw new Exception("MovieKey cannot be -1");

		setModelMovieInfo(new ModelMovieInfo(new ModelSeries(model)));
		
		setUp(Localizer.get("DialogMovieInfo.title.add-episode"));

		loadEmptyAdditionalFields();

		movieInfoModel.setTitle(model.getTitle());
	}

	/**
	 * Edit Movie/Episode This constructor initializes the fields with the info
	 * of the movie model.
	 */
	public DialogMovieInfo(ModelEntry model) {
		/* Dialog creation... */
		super(MovieManager.getDialog());

		setModelMovieInfo(new ModelMovieInfo(model, false));

		if (movieInfoModel.isEpisode)
			setUp(Localizer.get("DialogMovieInfo.title.edit-episode"));
		else
			setUp(Localizer.get("DialogMovieInfo.title.edit-movie"));

		/* Loads the movie info... */
		loadMovieInfo();
	}

	public void setModelMovieInfo(ModelMovieInfo model) {
		movieInfoModel = model;
		movieInfoModel.addModelChangedEventListenener(this);
	}
	
	/**
	 * Sets up the dialog...
	 */
	/**
	 * @param dialogTitle
	 */
	private void setUp(final String dialogTitle) {

		/* Dialog properties... */
		setTitle(dialogTitle);
		setModal(true);
		setResizable(true);

		/* Save before closing dialog... */
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				saveWindowSize();
			}
		});
		
		fontSize = MovieManager.getIt().getFontSize();
		
		setUpGUI();
				
		setHotkeyModifiers();
	}
		
	private void setUpGUI() {
	
		panelMovieInfo = new JPanel();

		panelMovieInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,7,0,7), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Localizer.get("DialogMovieInfo.panel-movie-info.title."), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(panelMovieInfo.getFont().getName(),Font.BOLD, fontSize))), BorderFactory.createEmptyBorder(0,5,0,5))); //$NON-NLS-1$
		
		panelMovieInfo.setLayout(new GridBagLayout());
		/* Creates the general info... */
		JPanel panelGeneralInfo = new JPanel();
		panelGeneralInfo.setLayout(new GridBagLayout());
		
		GridBagConstraints constraints;
		int textFieldInsets = 4;
		int insetsTopBottom = 2;

		JLabel dateID = new JLabel(Localizer.get("DialogMovieInfo.field.date") + ": "); //$NON-NLS-1$
		dateID.setFont((new Font(dateID.getFont().getName(), 1, fontSize)));
		
		// YYYYMMDD, or YYYY/MM/DD or even YYYY-MM-DD, YYYY.MM.DD
		date = new JTextField(6);
		
		/* IMDb */
		JLabel imdbID = new JLabel("IMDb id: "); //$NON-NLS-1$
		imdbID.setFont((new Font(imdbID.getFont().getName(), 1, fontSize)));

		imdb = new JTextField(6);
		imdb.setDocument(new DocumentRegExp("(\\d)*"));
		
		imdb.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (!getIMDb().getText().equals("")) { //$NON-NLS-1$
						if (!movieInfoModel.isEpisode)
							executeCommandGetIMDBInfo(true);
					}
				}
			}
		});

		JLabel colourID = new JLabel(Localizer.get("DialogMovieInfo.field.colour") + ": "); //$NON-NLS-1$
		colourID.setFont((new Font(colourID.getFont().getName(), 1, fontSize)));

		colour = new JTextField(12);

		JPanel imdbAndColour = new JPanel(new BorderLayout());

		JPanel imdbPanel = new JPanel();
		imdbPanel.add(imdbID);
		imdbPanel.add(imdb);
		imdbAndColour.add(imdbPanel, BorderLayout.WEST);

		JPanel colourPanel = new JPanel();
		colourPanel.add(colourID);
		colourPanel.add(colour);

		imdbAndColour.add(colourPanel, BorderLayout.EAST);
		
		JLabel movieTitleID = new JLabel(Localizer.get("DialogMovieInfo.field.title") + ": "); //$NON-NLS-1$
		movieTitleID.setFont((new Font(movieTitleID.getFont().getName(), 1, fontSize)));
		
		movieTitle = new JTextField(28);
		final JTextField movieTitle2 = movieTitle;

		/* This makes sure the focus will be on the movie title by default */
		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				movieTitle2.requestFocus();
			}
		});

		movieTitle.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent event) {

				if (event.getKeyCode() == KeyEvent.VK_ENTER) {

					try {
						if (getMovieTitle().getText().equals("")) { //$NON-NLS-1$
							File[] file = executeGetFile();
							
							if (file != null)
								movieInfoModel.getFileInfo(file);
						} else {
							if (movieInfoModel.isEpisode)
								executeCommandGetEpisodesInfo(false);
							else
								executeCommandGetIMDBInfo(false);
						}
					} catch (Exception e) {
						log.warn("Exception:" + e.getMessage(), e);
					}
				}
			}
		});

		directed = new JTextField(28);
		written = new JTextField(28);
		genre = new JTextField(28);
		language = new JTextField(2);
		
		JLabel directedID = new JLabel(Localizer.get("DialogMovieInfo.field.directed-by") + ": "); //$NON-NLS-1$
		directedID.setFont((new Font(directedID.getFont().getName(), 1, fontSize)));
				
		JLabel writtenID = new JLabel(Localizer.get("DialogMovieInfo.field.written-by") + ": "); //$NON-NLS-1$
		writtenID.setFont((new Font(writtenID.getFont().getName(), 1, fontSize)));
				
		JLabel genreID = new JLabel(Localizer.get("DialogMovieInfo.field.genre") + ": "); //$NON-NLS-1$
		genreID.setFont((new Font(genreID.getFont().getName(), 1, fontSize)));
				
		JLabel countryID = new JLabel(Localizer.get("DialogMovieInfo.field.country") + ": "); //$NON-NLS-1$
		countryID.setFont((new Font(countryID.getFont().getName(), 1, fontSize)));
		
		JLabel languageID = new JLabel(Localizer.get("DialogMovieInfo.field.language") + ":"); //$NON-NLS-1$
		languageID.setFont((new Font(languageID.getFont().getName(), 1,	fontSize)));
				
		JLabel seenID = new JLabel(Localizer.get("DialogMovieInfo.field.seen") + ": "); //$NON-NLS-1$
		seenID.setFont((new Font(seenID.getFont().getName(), 1, fontSize)));
		
		/* Will only change value if seen option is set to editable */
		seenBox = new JCheckBox() {
			protected void processMouseEvent(MouseEvent event) {

				if (event.getID() == MouseEvent.MOUSE_CLICKED)
					executeCommandSeen();
			}
		};

		seenBox.setMinimumSize(new Dimension(21, 21));

		// ImageIcon

		if (MovieManager.getConfig().getUseRegularSeenIcon()) {
			seenBox.setIcon(new ImageIcon(FileUtil.getImage("/images/unseen.png").getScaledInstance(18, 18, Image.SCALE_SMOOTH))); //$NON-NLS-1$
			seenBox.setSelectedIcon(new ImageIcon(FileUtil.getImage("/images/seen.png").getScaledInstance(18, 18, Image.SCALE_SMOOTH))); //$NON-NLS-1$
		}

		// Rating
		JLabel ratingID = new JLabel(Localizer.get("DialogMovieInfo.field.rating") + ": "); //$NON-NLS-1$
		ratingID.setFont((new Font(ratingID.getFont().getName(), 1, fontSize)));
		
		rating = new JTextField(3);
		rating.setDocument(new DocumentRegExp("(10|([1-9])|([1-9]\\.(\\d{1})?))", 3)); //$NON-NLS-1$
		
		// Personal rating
		JLabel personalRatingID = new JLabel("Personal rating" + ": "); //$NON-NLS-1$
		personalRatingID.setFont((new Font(personalRatingID.getFont().getName(), 1, fontSize)));
		
		personalRating = new JTextField(3);
		personalRating.setDocument(new DocumentRegExp("(10|([1-9])|([1-9]\\.(\\d{1})?))", 3)); //$NON-NLS-1$
		
		cover = new JLabel();
		cover.setBorder(BorderFactory.createEtchedBorder());
		cover.setPreferredSize(new Dimension(97, 145));
		cover.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				log.debug("actionPerformed: MovieInfo - Cover"); //$NON-NLS-1$
				executeCommandCover();
			}
		});
		cover.setTransferHandler(new CoverTransferHandler(this));

		
		// Add components to panelGeneralInfo
		
		// Date
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(2, 5, 2, 5);
		constraints.anchor = GridBagConstraints.WEST;
		panelGeneralInfo.add(dateID, constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(2, textFieldInsets, 2, 0);
		constraints.anchor = GridBagConstraints.WEST;
		panelGeneralInfo.add(date, constraints);
		
		// Imdb and color
		constraints = new GridBagConstraints();
		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.gridwidth = 7;
		constraints.insets = new Insets(0, 0, 0, 0);
		constraints.anchor = GridBagConstraints.EAST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panelGeneralInfo.add(imdbAndColour, constraints);
		
		// Title
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(insetsTopBottom, 5, insetsTopBottom, textFieldInsets);
		constraints.anchor = GridBagConstraints.WEST;
		panelGeneralInfo.add(movieTitleID, constraints);
				
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 7;
		constraints.insets = new Insets(insetsTopBottom, textFieldInsets, insetsTopBottom, 5);
		//constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panelGeneralInfo.add(movieTitle, constraints);
		
		// Directed by
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(insetsTopBottom, 5, insetsTopBottom, textFieldInsets);
		constraints.anchor = GridBagConstraints.WEST;
		panelGeneralInfo.add(directedID, constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.gridwidth = 7;
		constraints.insets = new Insets(1, textFieldInsets, 1, 5);
		//constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panelGeneralInfo.add(directed, constraints);
		
		// Written by
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(insetsTopBottom, 5, insetsTopBottom, textFieldInsets);
		constraints.anchor = GridBagConstraints.WEST;
		panelGeneralInfo.add(writtenID, constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 3;
		constraints.gridwidth = 7;
		constraints.insets = new Insets(insetsTopBottom, textFieldInsets, insetsTopBottom, 5);
		//constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panelGeneralInfo.add(written, constraints);
		
		// Genre
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(insetsTopBottom, 5, insetsTopBottom, textFieldInsets);
		constraints.anchor = GridBagConstraints.WEST;
		panelGeneralInfo.add(genreID, constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 4;
		constraints.gridwidth = 7;
		constraints.insets = new Insets(insetsTopBottom, textFieldInsets, insetsTopBottom, 5);
		//constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panelGeneralInfo.add(genre, constraints);
		
		
		// Country
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(insetsTopBottom, 5, insetsTopBottom, textFieldInsets);
		constraints.anchor = GridBagConstraints.WEST;
		panelGeneralInfo.add(countryID, constraints);

		country = new JTextField(2);
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 5;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(insetsTopBottom, textFieldInsets, insetsTopBottom, 5);
		//constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panelGeneralInfo.add(country, constraints);
		
		// Language
		constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.gridy = 5;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(insetsTopBottom, 5, 0, textFieldInsets);
		constraints.anchor = GridBagConstraints.WEST;
		panelGeneralInfo.add(languageID, constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx = 4;
		constraints.gridy = 5;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(insetsTopBottom, textFieldInsets, 0, 5);
		//constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panelGeneralInfo.add(language, constraints);
		
		// Seen
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.insets = new Insets(insetsTopBottom, 5, 0, textFieldInsets);
		constraints.anchor = GridBagConstraints.WEST;
		panelGeneralInfo.add(seenID, constraints);
				
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 6;
		constraints.insets = new Insets(insetsTopBottom, textFieldInsets, 0, 5);
		constraints.anchor = GridBagConstraints.WEST;
		//constraints.fill = GridBagConstraints.HORIZONTAL;
		panelGeneralInfo.add(seenBox, constraints);
		
		// Rating
		constraints = new GridBagConstraints();
		constraints.gridx = 2;
		constraints.gridy = 6;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(insetsTopBottom, 5, insetsTopBottom, textFieldInsets);
		constraints.anchor = GridBagConstraints.WEST;
		panelGeneralInfo.add(ratingID, constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx = 3;
		constraints.gridy = 6;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(insetsTopBottom, textFieldInsets, insetsTopBottom, 5);
		constraints.anchor = GridBagConstraints.WEST;
		panelGeneralInfo.add(rating, constraints);
		
		// Personal rating
		constraints = new GridBagConstraints();
		constraints.gridx = 4;
		constraints.gridy = 6;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(insetsTopBottom, 5, insetsTopBottom, textFieldInsets);
		constraints.anchor = GridBagConstraints.WEST;
		panelGeneralInfo.add(personalRatingID, constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx = 5;
		constraints.gridy = 6;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(insetsTopBottom, textFieldInsets, insetsTopBottom, 5);
		constraints.anchor = GridBagConstraints.WEST;
		panelGeneralInfo.add(personalRating, constraints);
		
		// Cover
		constraints = new GridBagConstraints();
		constraints.gridx = 8;
		constraints.gridy = 1;
		constraints.gridheight = 6;
		constraints.insets = new Insets(0, 5, 0, 5);
		constraints.anchor = GridBagConstraints.CENTER;
		panelGeneralInfo.add(cover, constraints);
				
		
		// Adding panelGeneralInfo
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(0, -1, 0, -1);
		//constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		panelMovieInfo.add(panelGeneralInfo, constraints);

				
		
		/* Creates the plot... */
		JPanel panelPlot = new JPanel();
		panelPlot.setLayout(new GridLayout(1,1));
		
		panelPlot.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                Localizer.get("DialogMovieInfo.panel-plot.title."), //$NON-NLS-1$
												TitledBorder.DEFAULT_JUSTIFICATION,
												TitledBorder.DEFAULT_POSITION,
												new Font(panelPlot.getFont()
														.getName(), Font.PLAIN,
														fontSize)),
								BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		this.textAreaPlot = new JTextArea("", 4, 43); //$NON-NLS-1$
		textAreaPlot.setLineWrap(true);
		textAreaPlot.setWrapStyleWord(true);
				
		JScrollPane scrollPanePlot = new JScrollPane(textAreaPlot);
		scrollPanePlot.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panelPlot.add(scrollPanePlot);

		/* Creates the cast... */
		JPanel panelCast = new JPanel();
		panelCast.setLayout(new GridLayout(1,1));
		
		panelCast.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                Localizer.get("DialogMovieInfo.panel-cast.title."), //$NON-NLS-1$
												TitledBorder.DEFAULT_JUSTIFICATION,
												TitledBorder.DEFAULT_POSITION,
												new Font(panelCast.getFont()
														.getName(), Font.PLAIN,
														fontSize)),
								BorderFactory.createEmptyBorder(0, 5, 5, 5)));

		this.textAreaCast = new JTextArea("", 4, 43); //$NON-NLS-1$
		textAreaCast.setLineWrap(true);
		textAreaCast.setWrapStyleWord(true);
				
		JScrollPane scrollPaneCast = new JScrollPane(textAreaCast);
		scrollPaneCast.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panelCast.add(scrollPaneCast);

		JPanel panelPlotAndCast = new JPanel();
		panelPlotAndCast.setLayout(new BoxLayout(panelPlotAndCast, BoxLayout.Y_AXIS));
		panelPlotAndCast.setBorder(BorderFactory.createEmptyBorder(5, 3, 2, 3));
		panelPlotAndCast.add(panelPlot);
		panelPlotAndCast.add(panelCast);
		
		/* Miscellaneous */

		JPanel panelMisc = new JPanel();
		panelMisc.setLayout(new GridBagLayout());
		panelMisc.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
				
		JLabel webRuntimeID = new JLabel(Localizer.get("DialogMovieInfo.field.web-runtime") + ": "); //$NON-NLS-1$
		webRuntimeID.setFont((new Font(webRuntimeID.getFont().getName(), 1,	fontSize)));
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		// constraints.insets = new Insets(1,5,1,5);
		//constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.WEST;
		panelMisc.add(webRuntimeID, constraints);

		this.textFieldWebRuntime = new JTextField();
		textFieldWebRuntime.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		textFieldWebRuntime.setMinimumSize(textFieldWebRuntime.getPreferredSize());

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		constraints.weightx = 1.0;
		constraints.insets = new Insets(0, 0, 1, 0);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.EAST;
		panelMisc.add(textFieldWebRuntime, constraints);

		JLabel soundMixID = new JLabel(Localizer.get("DialogMovieInfo.field.sound-mix") + ": "); //$NON-NLS-1$
		soundMixID.setFont((new Font(soundMixID.getFont().getName(), 1,	fontSize)));
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		// constraints.insets = new Insets(1,5,1,5);
		//constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.WEST;
		panelMisc.add(soundMixID, constraints);

		this.textFieldSoundMix = new JTextField();
		textFieldSoundMix.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		textFieldSoundMix.setMinimumSize(textFieldSoundMix.getPreferredSize());

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 3;
		constraints.weightx = 1.0;
		constraints.insets = new Insets(1, 0, 1, 0);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.EAST;
		panelMisc.add(textFieldSoundMix, constraints);

		JLabel awardsID = new JLabel(Localizer.get("DialogMovieInfo.field.awards") + ": "); //$NON-NLS-1$
		awardsID.setFont((new Font(awardsID.getFont().getName(), 1, fontSize)));
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		// constraints.insets = new Insets(1,5,1,5);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.WEST;
		panelMisc.add(awardsID, constraints);

		this.textFieldAwards = new JTextField();
		textFieldAwards.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		textFieldAwards.setMinimumSize(textFieldAwards.getPreferredSize());

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.gridwidth = 3;
		constraints.weightx = 1.0;
		constraints.insets = new Insets(1, 0, 1, 0);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.EAST;
		panelMisc.add(textFieldAwards, constraints);

		JLabel mpaaID = new JLabel(Localizer.get("DialogMovieInfo.field.MPAA") + ": "); //$NON-NLS-1$
		mpaaID.setFont((new Font(mpaaID.getFont().getName(), 1, fontSize)));
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 1;
		// constraints.insets = new Insets(1,5,1,5);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.WEST;
		panelMisc.add(mpaaID, constraints);

		this.textFieldMpaa = new JTextField();
		textFieldMpaa.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		textFieldMpaa.setMinimumSize(textFieldMpaa.getPreferredSize());

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 3;
		constraints.gridwidth = 3;
		constraints.weightx = 1.0;
		constraints.insets = new Insets(1, 0, 1, 0);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.EAST;
		panelMisc.add(textFieldMpaa, constraints);

		JLabel akaID = new JLabel(Localizer.get("DialogMovieInfo.field.also-known-as") + ": "); //$NON-NLS-1$
		akaID.setFont((new Font(akaID.getFont().getName(), 1, fontSize)));
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 1;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(0, 0, 0, 4);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.WEST;
		panelMisc.add(akaID, constraints);

		this.textAreaAka = new JTextArea("", 4, 10);
		// textAreaAka.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		textAreaAka.setLineWrap(true);
		textAreaAka.setWrapStyleWord(true);
		JScrollPane scrollPaneAka = new JScrollPane(textAreaAka);
		scrollPaneAka.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		scrollPaneAka.setMinimumSize(scrollPaneAka.getPreferredSize());

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 4;
		constraints.gridwidth = 3;
		constraints.gridheight = 4;
		constraints.weightx = 1.0;
		constraints.insets = new Insets(1, 0, 1, 0);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.EAST;
		panelMisc.add(scrollPaneAka, constraints);

		JLabel certificationID = new JLabel(Localizer.get("DialogMovieInfo.field.certification") + ": "); //$NON-NLS-1$
		certificationID.setFont((new Font(certificationID.getFont().getName(), 1, fontSize)));
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 8;
		constraints.gridwidth = 1;
		// constraints.insets = new Insets(1,5,1,5);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.WEST;
		panelMisc.add(certificationID, constraints);

		this.textAreaCertification = new JTextArea("", 4, 30);
		// textAreaCertification.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		textAreaCertification.setLineWrap(true);
		textAreaCertification.setWrapStyleWord(true);

		JScrollPane scrollPaneCertification = new JScrollPane(textAreaCertification);
		scrollPaneCertification.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneCertification.setMinimumSize(scrollPaneCertification.getPreferredSize());

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 8;
		constraints.gridwidth = 3;
		constraints.gridheight = 3;
		constraints.weightx = 1.0;
		constraints.insets = new Insets(1, 0, 1, 0);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.EAST;
		panelMisc.add(scrollPaneCertification, constraints);

		JTabbedPane allTabbedInfo = new JTabbedPane();
		allTabbedInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		allTabbedInfo.add(Localizer.get("DialogMovieInfo.tab.plot-and-cast"), panelPlotAndCast); //$NON-NLS-1$
		allTabbedInfo.add(Localizer.get("DialogMovieInfo.tab.miscellaneous"), panelMisc); //$NON-NLS-1$

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(0, -1, 5, -1);
		//constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		panelMovieInfo.add(allTabbedInfo, constraints);

		/* Creates the Additional Info... */
		panelAdditionalInfo = new JPanel();

		new net.sf.xmm.moviemanager.swing.util.FileDrop(panelAdditionalInfo, new net.sf.xmm.moviemanager.swing.util.FileDrop.Listener() {   
        	public void filesDropped(final File[] files) {
        		
        		for (int i = 0; i < files.length; i++) {
        			        			
        			if (files[i].isDirectory()) {
        				DialogAlert alert = new DialogAlert(dialogMovieInfo,
        						net.sf.xmm.moviemanager.util.Localizer.get("DialogMovieInfo.alert.title.alert"), //$NON-NLS-2$
								"Please provide a valid media file:" + files[i]); //$NON-NLS-1$ /
        				GUIUtil.showAndWait(alert, true);
        				return;
        			}
        			
        			if (!files[i].isFile()) {
        				DialogAlert alert = new DialogAlert(dialogMovieInfo,
        						net.sf.xmm.moviemanager.util.Localizer.get("DialogMovieInfo.alert.title.alert"), //$NON-NLS-2$
								"File not found:" + files[i]); //$NON-NLS-1$ /
        				GUIUtil.showAndWait(alert, true);
        				return;
        			}
				}
        		
        		try {
					movieInfoModel.getFileInfo(files);
				} catch (Exception e) {
					log.warn("Exception:" + e.getMessage(), e);
					
					DialogAlert alert = new DialogAlert(MovieManager.getDialog(), "Error occured", "<html>An error occured while retrieving file info:<br>" + e.getMessage() + "</html>", true);
					GUIUtil.show(alert, true);
				}
        	}
		});
		
		panelAdditionalInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
												Localizer.get("DialogMovieInfo.panel-additional-info.title"), //$NON-NLS-1$
												TitledBorder.DEFAULT_JUSTIFICATION,
												TitledBorder.DEFAULT_POSITION,
												new Font(panelAdditionalInfo.getFont().getName(),Font.PLAIN, fontSize)),
								BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		panelAdditionalInfo.setLayout(new GridBagLayout());

		JLabel fieldsID = new JLabel(Localizer.get("DialogMovieInfo.panel-additional-info.field") + ": "); //$NON-NLS-1$
		fieldsID.setFont(new Font(fieldsID.getFont().getName(), Font.PLAIN,fontSize));
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(1, 0, 7, 5);
		constraints.anchor = GridBagConstraints.WEST;
		panelAdditionalInfo.add(fieldsID, constraints);

		additionalInfoFields = new JComboBox(new String[] { "", "" }); //$NON-NLS-1$ //$NON-NLS-2$

		additionalInfoFields.setFont(new Font(additionalInfoFields.getFont().getName(), Font.PLAIN,fontSize));
		additionalInfoFields.setEditable(false);
		additionalInfoFields.setMaximumRowCount(6);
		additionalInfoFields.setActionCommand("MovieInfo - Additional Info"); //$NON-NLS-1$
		additionalInfoFields.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.debug("actionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
				executeCommandAdditionalInfo();
			}
		});
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(1, 5, 7, 0);
		constraints.anchor = GridBagConstraints.WEST;
		panelAdditionalInfo.add(additionalInfoFields, constraints);

		JLabel valueID = new JLabel(Localizer.get("DialogMovieInfo.panel-additional-info.value") + ": "); //$NON-NLS-1$
		valueID.setFont(new Font(valueID.getFont().getName(), Font.PLAIN,fontSize));
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.insets = new Insets(7, 0, 1, 3);
		constraints.anchor = GridBagConstraints.WEST;
		panelAdditionalInfo.add(valueID, constraints);

		additionalInfoValuePanel = new JPanel();
		additionalInfoValuePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		additionalInfoValuePanel.setLayout(new FlowLayout(0, 0, 0));

		JTextField value = new JTextField(14);

		Font font = value.getFont();

		font = new Font(font.getName(), font.getStyle(), 11);

		value.setFont(font);

		additionalInfoValuePanel.add(value);

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.insets = new Insets(7, 5, 1, 0);
		constraints.anchor = GridBagConstraints.WEST;
		panelAdditionalInfo.add(additionalInfoValuePanel, constraints);

		additionalInfoUnit = new JLabel(" units", JLabel.RIGHT); //$NON-NLS-1$
		additionalInfoUnit.setFont(new Font(additionalInfoUnit.getFont().getName(), Font.PLAIN, fontSize));
		constraints = new GridBagConstraints();
		constraints.gridx = 2;
		constraints.gridy = 1;
		constraints.insets = new Insets(7, 0, 1, 0);
		constraints.anchor = GridBagConstraints.EAST;
		panelAdditionalInfo.add(additionalInfoUnit, constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.insets = new Insets(0, -1, 5, -1);
		constraints.anchor = GridBagConstraints.WEST;
		panelMovieInfo.add(panelAdditionalInfo, constraints);

		createAdditionalInfoPopup();
		
		/* Creates the notes... */
		JPanel panelNotes = new JPanel();
		panelNotes.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
												BorderFactory.createEtchedBorder(),Localizer.get("DialogMovieInfo.panel-notes.title"), //$NON-NLS-1$
												TitledBorder.DEFAULT_JUSTIFICATION,
												TitledBorder.DEFAULT_POSITION,
												new Font(panelNotes.getFont()
														.getName(), Font.PLAIN,fontSize)),
								BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		textAreaNotes = new JTextArea("", 4, 20); //$NON-NLS-1$
		textAreaNotes.setLineWrap(true);
		textAreaNotes.setWrapStyleWord(true);
		JScrollPane scrollPaneNotes = new JScrollPane(textAreaNotes);
		scrollPaneNotes.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panelNotes.add(scrollPaneNotes);
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 3;
		constraints.insets = new Insets(0, -1, 5, -1);
		constraints.anchor = GridBagConstraints.EAST;
		panelMovieInfo.add(panelNotes, constraints);


		/* Buttons panel... */
		JPanel panelButtons = new JPanel();
		panelButtons.setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));
		panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));

		buttonSaveAndClose = new JButton("Save & Close");
		buttonSaveAndClose.setToolTipText("Save and Close the window"); //$NON-NLS-1$
		buttonSaveAndClose.setActionCommand("MovieInfo - Save and Close"); //$NON-NLS-1$
		buttonSaveAndClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.debug("actionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
				reloadMovieListAndClose(executeCommandSave());
			}
		});
		panelButtons.add(buttonSaveAndClose);

		
		buttonSave = new JButton(Localizer.get("DialogMovieInfo.button-save.text.save")); //$NON-NLS-1$
		buttonSave.setToolTipText(Localizer.get("DialogMovieInfo.button-save.tooltip")); //$NON-NLS-1$
		// Disabled if edit
		buttonSave.setEnabled(!movieInfoModel.isEditMode());
		buttonSave.setActionCommand("MovieInfo - Save"); //$NON-NLS-1$
		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.debug("actionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
				reloadMovieList(executeCommandSave());
				movieInfoModel.clearModel(true);
				movieTitle2.requestFocus();
				
			}
		});
		panelButtons.add(buttonSave);

		buttonGetDVDInfo = new JButton(Localizer.get("DialogMovieInfo.button-get-DVD-info.text")); //$NON-NLS-1$
		buttonGetDVDInfo.setToolTipText(Localizer.get("DialogMovieInfo.button-get-DVD-info.tooltip")); //$NON-NLS-1$
		buttonGetDVDInfo.setActionCommand("MovieInfo - GetDVDInfo"); //$NON-NLS-1$
		buttonGetDVDInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.debug("actionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
				executeGetDVDInfo();
			}
		});

		panelButtons.add(buttonGetDVDInfo);

		buttonGetFileInfo = new JButton(Localizer.get("DialogMovieInfo.button-get-file-info.text")); //$NON-NLS-1$
		buttonGetFileInfo.setToolTipText(Localizer.get("DialogMovieInfo.button-get-file-info.tooltip")); //$NON-NLS-1$
		buttonGetFileInfo.setActionCommand("MovieInfo - GetFileInfo"); //$NON-NLS-1$
		buttonGetFileInfo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					log.debug("actionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
					
					try {
						File[] file = executeGetFile();
						if (file != null) {
							updateModelFromGeneralInfo();
							movieInfoModel.getFileInfo(file);
						}
					} catch (Exception e) {
						log.warn("Exception:" + e.getMessage(), e);
					}
				}
			});


		panelButtons.add(buttonGetFileInfo);

		
		if (movieInfoModel.isEpisode)
			buttonGetIMDBInfo = new JButton(Localizer.get("DialogMovieInfo.button-get-web-info.text.get-episode-info")); //$NON-NLS-1$
		else
			buttonGetIMDBInfo = new JButton(Localizer.get("DialogMovieInfo.button-get-web-info.text.get-imdb-info")); //$NON-NLS-1$

		buttonGetIMDBInfo.setToolTipText(Localizer.get("DialogMovieInfo.button-get-web-info.tooltip.get-imdb-info")); //$NON-NLS-1$
		buttonGetIMDBInfo.setActionCommand("MovieInfo - GetIMDBInfo"); //$NON-NLS-1$
		buttonGetIMDBInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.debug("actionPerformed: " + event.getActionCommand()); //$NON-NLS-1$
				
				boolean useIMDbKey = false;
				
				if (getMovieTitle().getText().equals("") && !getIMDb().getText().equals(""))
					useIMDbKey = true;
				
				if (movieInfoModel.isEpisode)
					executeCommandGetEpisodesInfo(useIMDbKey);
				else {
					executeCommandGetIMDBInfo(useIMDbKey);
				}
			}
		});
		
		panelButtons.add(buttonGetIMDBInfo);

		buttonCancel = new JButton(Localizer.get("DialogMovieInfo.button-cancel.text")); //$NON-NLS-1$
		buttonCancel.setToolTipText(Localizer.get("DialogMovieInfo.button-cancel.tooltip")); //$NON-NLS-1$
		buttonCancel.setActionCommand("MovieInfo - Cancel"); //$NON-NLS-1$
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveWindowSize();
				dispose();
			}
		});
		
		panelButtons.add(buttonCancel);

		/* Reduces the width of the buttons (less empty space between borders and text) to avoid the window getting to wide */
		int sideMargin = 6;

		buttonSaveAndClose.setMargin(new java.awt.Insets(buttonSaveAndClose.getMargin().top, 
				sideMargin, buttonSaveAndClose.getMargin().bottom, sideMargin));

		buttonSave.setMargin(new java.awt.Insets(buttonSave.getMargin().top, 
				sideMargin, buttonSave.getMargin().bottom, sideMargin));

		buttonGetDVDInfo.setMargin(new java.awt.Insets(buttonGetDVDInfo.getMargin().top, 
				sideMargin, buttonGetDVDInfo.getMargin().bottom, sideMargin));

		buttonGetFileInfo.setMargin(new java.awt.Insets(buttonGetFileInfo.getMargin().top, 
				sideMargin, buttonGetFileInfo.getMargin().bottom, sideMargin));

		buttonGetIMDBInfo.setMargin(new java.awt.Insets(buttonGetIMDBInfo.getMargin().top, 
				sideMargin, buttonGetIMDBInfo.getMargin().bottom, sideMargin));

		buttonCancel.setMargin(new java.awt.Insets(buttonCancel.getMargin().top, 
				sideMargin, buttonCancel.getMargin().bottom, sideMargin));

		
		JScrollPane movieInfoScroll = new JScrollPane(panelMovieInfo);
		movieInfoScroll.setBorder(null);
		movieInfoScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		movieInfoScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		
		JPanel all = new JPanel();
		all.setLayout(new BorderLayout());
		all.add(movieInfoScroll, BorderLayout.CENTER);
		all.add(panelButtons, BorderLayout.SOUTH);
		
		
		/* Adds all and buttonsPanel... */
		getContentPane().add(all);
		
		additionalInfoFields.setPreferredSize(new Dimension((int) additionalInfoFields.getPreferredSize().getWidth() + 60, (int) additionalInfoFields.getPreferredSize().getHeight() + 15));
		/* Packs and sets location... */
		pack();

		panelMovieInfo.setMinimumSize(panelMovieInfo.getSize());
		
		textFieldWebRuntime.setPreferredSize(new Dimension(panelMovieInfo.getWidth() - 150, 22));
		textFieldSoundMix.setPreferredSize(new Dimension(panelMovieInfo.getWidth() - 150, 22));
		textFieldAwards.setPreferredSize(new Dimension(panelMovieInfo.getWidth() - 150, 22));
		textFieldMpaa.setPreferredSize(new Dimension(panelMovieInfo.getWidth() - 150, 22));
		scrollPaneAka.setPreferredSize(new Dimension(panelMovieInfo.getWidth() - 150, 64));
		scrollPaneCertification.setPreferredSize(new Dimension(panelMovieInfo.getWidth() - 150, 52));

		allTabbedInfo.setPreferredSize(new Dimension(panelGeneralInfo.getWidth(), 
													 ((int) allTabbedInfo.getPreferredSize().getHeight())));

		scrollPanePlot.setPreferredSize(new Dimension(panelMovieInfo.getWidth() - 76, 72));
		scrollPaneCast.setPreferredSize(new Dimension(panelMovieInfo.getWidth() - 76, 72));

		panelAdditionalInfo.setPreferredSize(new Dimension(panelMovieInfo.getWidth() / 2 - 20, 
														   (int) panelAdditionalInfo.getPreferredSize().getHeight() + 10));

		scrollPaneNotes.setPreferredSize(new Dimension((panelMovieInfo.getWidth() - 114) / 2, 76));
		panelNotes.setPreferredSize(new Dimension((int) panelNotes.getPreferredSize().getWidth(), 
												  (int) panelAdditionalInfo.getPreferredSize().getHeight() + 1));

		value.setPreferredSize(value.getSize());
		additionalInfoUnit.setPreferredSize(additionalInfoUnit.getSize());

		/* Setting preferred size of the additional info dropdown menu */

		additionalInfoFields.setPreferredSize(new Dimension((int) (panelAdditionalInfo.getPreferredSize().getWidth() - 
													 fieldsID.getPreferredSize().getWidth()) - 50, 
											  (int) additionalInfoFields.getPreferredSize().getHeight()));
		
		valueComboBoxWidth = (int) additionalInfoFields.getPreferredSize().getWidth() - 33;
		valueComboBoxHeight = (int) value.getPreferredSize().getHeight();

		pack();
		
		Dimension size = getSize();
		
		// Set size if valid
		int storedHeight = MovieManager.getConfig().getAddMovieWindowHeight();
				
		if (storedHeight > -1) {
			
			if (storedHeight > size.height)
				storedHeight = size.height;
			
			setSize(size.width, storedHeight);
		}
		
		int x = (int) MovieManager.getIt().getLocation().getX() + (MovieManager.getIt().getWidth() - getWidth()) / 2;
		int y = (int) MovieManager.getIt().getLocation().getY()	+ (MovieManager.getIt().getHeight() - getHeight()) / 2;

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		size = getSize();

		if (x + size.width > screenSize.width)
			x = screenSize.width - size.width;

		if (y + size.height > screenSize.height)
			y = screenSize.height - size.height - 20;

		setLocation(x, y);
	}

	
	/**
	 * Creates popup to clear the additional info data
	 */
	void createAdditionalInfoPopup() {
		
		final JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem resetData = new JMenuItem("Clear Additional Info data"); //$NON-NLS-1$
		resetData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				movieInfoModel.setAdditionalInfoFieldsEmpty();
			}
		});
		
		popupMenu.add(resetData);
		
		panelAdditionalInfo.addMouseListener(new MouseListener() {
			
			public void mousePressed(MouseEvent event) {
				if (GUIUtil.isRightMouseButton(event)) {
					popupMenu.show(panelAdditionalInfo, event.getX(), event.getY());
				}
			}
			
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
	}

	public void setLastFieldIndex(int index) {
		_lastFieldIndex = index;
	}

	public int getLastFieldIndex() {
		return _lastFieldIndex;
	}

	/**
	 * Gets the date JTextField...
	 */
	public JTextField getDate() {
		return date;
	}

	/**
	 * Gets the imdb JTextField...
	 */
	public JTextField getIMDb() {
		return imdb;
	}

	/**
	 * Gets the colour JTextField...
	 */
	public JTextField getColour() {
		return colour;
	}

	/**
	 * Gets the movie title JTextField...
	 */
	public JTextField getMovieTitle() {
		return movieTitle;
	}

	/**
	 * Gets the directed by JTextField...
	 */
	public JTextField getDirectedBy() {
		return directed;
	}

	/**
	 * Gets the written by JTextField...
	 */
	public JTextField getWrittenBy() {
		return written;
	}

	/**
	 * Gets the genre JTextField...
	 */
	public JTextField getGenre() {
		return genre;
	}

	/**
	 * Gets the rating JTextField...
	 */
	public JTextField getRating() {
		return rating;
	}
	
	public JTextField getPersonalRating() {
		return personalRating;
	}

	/**
	 * Gets the country JTextField...
	 */
	public JTextField getCountry() {
		return country;
	}

	/**
	 * Gets the seen JLabel...
	 */
	protected JCheckBox getSeen() {
		return seenBox;
	}

	/**
	 * Gets the language JTextField...
	 */
	public JTextField getLanguage() {
		return language;
	}

	/**
	 * Gets the cover JLabel...
	 */
	public JLabel getCover() {
		return cover;
	}

	/**
	 * Gets the plot JTextArea...
	 */
	public JTextArea getPlot() {
		return this.textAreaPlot;
	}

	/**
	 * Gets the cast JTextArea...
	 */
	public JTextArea getCast() {
		return this.textAreaCast;
	}

	/**
	 * Gets the web-runtime JTextArea...
	 */
	public JTextField getWebRuntime() {
		return this.textFieldWebRuntime;
	}

	/**
	 * Gets the web-sound mix JTextArea...
	 */
	protected JTextField getWebSoundMix() {
		return this.textFieldSoundMix;
	}

	/**
	 * Gets the awards JTextArea...
	 */
	protected JTextField getAwards() {
		return this.textFieldAwards;
	}

	/**
	 * Gets the MPAA JTextArea...
	 */
	public JTextField getMpaa() {
		return this.textFieldMpaa;
	}

	/**
	 * Gets the Also known as JTextArea...
	 */
	public JTextArea getAka() {
		return this.textAreaAka;
	}

	/**
	 * Gets the Also known as JTextArea...
	 */
	protected JTextArea getCertification() {
		return this.textAreaCertification;
	}

	/**
	 * Gets the additional info fields JComboBox...
	 */
	protected JPanel getAdditionalInfoPanel() {
		return panelAdditionalInfo;
	}

	/**
	 * Gets the additional info fields JComboBox...
	 */
	protected JComboBox getAdditionalInfoFields() {
		return additionalInfoFields;
	}

	/**
	 * Gets the additional info value JPanel...
	 */
	protected JPanel getAdditionalInfoValuePanel() {
		return additionalInfoValuePanel;
	}

	/**
	 * Gets the additional info units JLabel...
	 */
	protected JLabel getAdditionalInfoUnits() {
		return additionalInfoUnit;
	}

	/**
	 * Gets the notes JTextArea...
	 */
	public JTextArea getNotes() {
		return textAreaNotes;
	}

	/**
	 * Removes the cover.
	 */
	public void removeCover() {
		movieInfoModel.setSaveCover(false);
		movieInfoModel.setCover("", null); //$NON-NLS-1$
	}

	/**
	 * Sets _cover and _coverData.
	 */
	public void setCover(String cover, byte[] coverData) {
		updateModelFromGeneralInfo();
		movieInfoModel.setSaveCover(true);
		movieInfoModel.setCover(cover, coverData);
	}

	/**
	 * Loads the info from the model
	 */
	private void loadMovieInfo() {

		loadAdditionalFields();

		/* Gets the general info... */
		updateGeneralInfoFromModel();
		updateCurrentAdditionalInfoFieldFromModel();
	}

	/**
	 * Loads an empty additional fields model...
	 */
	protected void loadEmptyAdditionalFields() {
		loadAdditionalFields();
	}

	protected void loadAdditionalFields() {
	
		/* loads the additional info... */

		_fieldUnits.clear();
		_fieldDocuments.clear();
		
		_fieldUnits.add(""); //$NON-NLS-1$
		_fieldDocuments.add(new PlainDocument());

		_fieldUnits.add(""); //$NON-NLS-1$
		_fieldDocuments.add(new DocumentRegExp("(\\d)*", 2)); //$NON-NLS-1$

		_fieldUnits.add("MiB"); //$NON-NLS-1$
		_fieldDocuments.add(new DocumentRegExp("(\\d)*", 9)); //$NON-NLS-1$

		_fieldUnits.add(""); //$NON-NLS-1$
		_fieldDocuments.add(new DocumentRegExp("(\\d)*", 9)); //$NON-NLS-1$

		_fieldUnits.add(""); //$NON-NLS-1$
		_fieldDocuments.add(new DocumentRegExp("(\\d)*(\\.)?(\\d)*")); //$NON-NLS-1$

		_fieldUnits.add(""); //$NON-NLS-1$
		_fieldDocuments.add(new DocumentRegExp("(\\d)*(x)?(\\d)*")); //$NON-NLS-1$

		_fieldUnits.add(""); //$NON-NLS-1$
		_fieldDocuments.add(new PlainDocument());

		_fieldUnits.add("fps"); //$NON-NLS-1$
		_fieldDocuments.add(new DocumentRegExp("(\\d)*(\\.)?(\\d)*")); //$NON-NLS-1$

		_fieldUnits.add("kbps"); //$NON-NLS-1$
		_fieldDocuments.add(new DocumentRegExp("(\\d)*")); //$NON-NLS-1$

		_fieldUnits.add(""); //$NON-NLS-1$
		_fieldDocuments.add(new PlainDocument());

		_fieldUnits.add("Hz"); //$NON-NLS-1$
		_fieldDocuments.add(new PlainDocument());

		_fieldUnits.add("kbps"); //$NON-NLS-1$
		_fieldDocuments.add(new PlainDocument());

		_fieldUnits.add(""); //$NON-NLS-1$
		_fieldDocuments.add(new PlainDocument());

		_fieldUnits.add(""); //$NON-NLS-1$
		_fieldDocuments.add(new PlainDocument());

		_fieldUnits.add(""); //$NON-NLS-1$
		_fieldDocuments.add(new PlainDocument());

		_fieldUnits.add(""); //$NON-NLS-1$
		_fieldDocuments.add(new PlainDocument());

		_fieldUnits.add(""); //$NON-NLS-1$
		_fieldDocuments.add(new PlainDocument());

		for (int i = EXTRA_START; i < movieInfoModel.getFieldNames().size(); i++) {
			_fieldUnits.add(""); //$NON-NLS-1$
			_fieldDocuments.add(new PlainDocument());
		}

		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
		int[] activeAdditionalInfoFields;

		activeAdditionalInfoFields = MovieManager.getDatabaseHandler().getActiveAdditionalInfoFields();

		String name;

		for (int i = 0; i < activeAdditionalInfoFields.length; i++) {

			switch (activeAdditionalInfoFields[i]) {

			case 0:
				comboBoxModel.addElement("Subtitles");break; //$NON-NLS-1$
			case 1:
				comboBoxModel.addElement("Duration");break; //$NON-NLS-1$
			case 2:
				comboBoxModel.addElement("File Size");break; //$NON-NLS-1$
			case 3:
				comboBoxModel.addElement("CDs");break; //$NON-NLS-1$
			case 4:
				comboBoxModel.addElement("CD Cases");break; //$NON-NLS-1$
			case 5:
				comboBoxModel.addElement("Resolution");break; //$NON-NLS-1$
			case 6:
				comboBoxModel.addElement("Video Codec");break; //$NON-NLS-1$
			case 7:
				comboBoxModel.addElement("Video Rate");break; //$NON-NLS-1$
			case 8:
				comboBoxModel.addElement("Video Bit Rate");break; //$NON-NLS-1$
			case 9:
				comboBoxModel.addElement("Audio Codec");break; //$NON-NLS-1$
			case 10:
				comboBoxModel.addElement("Audio Rate");break; //$NON-NLS-1$
			case 11:
				comboBoxModel.addElement("Audio Bit Rate");break; //$NON-NLS-1$
			case 12:
				comboBoxModel.addElement("Audio Channels");break; //$NON-NLS-1$
			case 13:
				comboBoxModel.addElement("Location");break; //$NON-NLS-1$
			case 14:
				comboBoxModel.addElement("File Count");break; //$NON-NLS-1$
			case 15:
				comboBoxModel.addElement("Container");break; //$NON-NLS-1$
			case 16:
				comboBoxModel.addElement("Media Type");break; //$NON-NLS-1$

			default: {
				name = (String) movieInfoModel.getFieldNames().get(activeAdditionalInfoFields[i]);
				comboBoxModel.addElement(name);
			}
			}
		}

		getAdditionalInfoFields().setModel(comboBoxModel);

		executeCommandAdditionalInfo();
	}

	/**
	 * Changes the seen status...
	 */
	private void executeCommandSeen() {
		updateModelFromGeneralInfo();
		movieInfoModel.setSeen(!movieInfoModel.model.getSeen());
	}

	/**
	 * Changes the cover...
	 */
	private void executeCommandCover() {
		
		try {
			/* Opens the Open dialog... */
			ExtendedFileChooser fileChooser = new ExtendedFileChooser();

			fileChooser.setFileFilter(new CustomFileFilter(new String[] { "gif", "png", "jpg" }, 
														   new String("Image Files (*.gif, *.png, *.jpg)"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

			if (MovieManager.getConfig().getLastCoversDir() != null) {
				fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastCoversDir());
			}

			fileChooser.setDialogTitle(Localizer.get("DialogMovieInfo.filechooser.select-cover.text")); //$NON-NLS-1$
			fileChooser.setApproveButtonText(Localizer.get("DialogMovieInfo.filechooser.select-cover.tooltip")); //$NON-NLS-1$
			fileChooser.setApproveButtonToolTipText("Select cover"); //$NON-NLS-1$
			fileChooser.setAcceptAllFileFilterUsed(false);

			int returnVal = fileChooser.showOpenDialog(this);

			if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
				/* Gets the path... */
				String coverFolder = fileChooser.getSelectedFile().getAbsolutePath().replaceAll(fileChooser.getSelectedFile().getName(), ""); //$NON-NLS-1$
				String cover = fileChooser.getSelectedFile().getName();
				/* Verifies extension... */
				String extension = cover.substring(cover.lastIndexOf('.') + 1);
				if (extension.compareToIgnoreCase("gif") != 0 && extension.compareToIgnoreCase("png") != 0 && extension.compareToIgnoreCase("jpg") != 0) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					throw new Exception("Image extension not supported."); //$NON-NLS-1$
				}

				if (!(new File(coverFolder + cover).exists())) {
					throw new Exception("Image file not found."); //$NON-NLS-1$
				}
				/* Saves info... */
								
				byte[] _coverData = FileUtil.readFromFile(new File(coverFolder, cover));
				setCover(cover, _coverData);

				/* Sets the last path... */
				MovieManager.getConfig().setLastCoversDir(fileChooser.getCurrentDirectory());
			}
		} catch (Exception e) {
			log.error("", e); //$NON-NLS-1$
		}
	}

	
	/**
	 * Changes the value JPanel and the JLabel unit and the...
	 */
	private void executeCommandAdditionalInfo() {
		
		int [] activeAdditionalInfoFields;

		activeAdditionalInfoFields = MovieManager.getDatabaseHandler().getActiveAdditionalInfoFields();
		
		if (getLastFieldIndex() != -1) {
	
			int oldIndex = activeAdditionalInfoFields[getLastFieldIndex()];
			
			/* Duration... */
			if (oldIndex == 1) {

				/* Saves the duration... */
				String hours = ((JTextField) getAdditionalInfoValuePanel().getComponent(0)).getText();
				String mints = ((JTextField) getAdditionalInfoValuePanel().getComponent(2)).getText();
				String secds = ((JTextField) getAdditionalInfoValuePanel().getComponent(4)).getText();

				if (!hours.equals("") || !mints.equals("") || !secds.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					int time = 0;
					if (!hours.equals("")) //$NON-NLS-1$
						time += Integer.parseInt(hours) * 3600;
					if (!mints.equals("")) //$NON-NLS-1$
						time += Integer.parseInt(mints) * 60;
					if (!secds.equals("")) //$NON-NLS-1$
						time += Integer.parseInt(secds);

					movieInfoModel.getFieldValues().set(activeAdditionalInfoFields[getLastFieldIndex()], String.valueOf(time));
				}

				/* Recreates the JPanel... */
				getAdditionalInfoValuePanel().removeAll();
				JTextField textfield = new JTextField(14);
				Font font = textfield.getFont();
				font = new Font(font.getName(), font.getStyle(), 11);
				textfield.setFont(font);
				getAdditionalInfoValuePanel().add(textfield);

			} /* Subtitle, Resolution, video codec, video rate, video bit rate, audio codec, autio rate, 
				autio bit rate, audio channels, container, media info, and all the extra info fields */
			else if (oldIndex == 0 || (oldIndex >=5 && oldIndex <= 12) || oldIndex >= 15) {
				/* Current value in combobox */
				String value = (String) ((JComboBox) getAdditionalInfoValuePanel().getComponent(0)).getSelectedItem();

				/* Old value */
				String oldValue = (String) movieInfoModel.getFieldValues().get(activeAdditionalInfoFields[getLastFieldIndex()]);

				movieInfoModel.getFieldValues().set(activeAdditionalInfoFields[getLastFieldIndex()], value);

				AdditionalInfoFieldDefaultValues valuesObj = (AdditionalInfoFieldDefaultValues) 
					MovieManager.getConfig().getAdditionalInfoDefaultValues().get(movieInfoModel.getFieldNames().get(activeAdditionalInfoFields[getLastFieldIndex()]));

				/* Creating a new entry in the values hashmap */
				if (valuesObj == null) {
					valuesObj = new AdditionalInfoFieldDefaultValues((String) movieInfoModel.getFieldNames().get(activeAdditionalInfoFields[getLastFieldIndex()]));
					
					valuesObj.addValue(oldValue);
					MovieManager.getConfig().getAdditionalInfoDefaultValues().put(movieInfoModel.getFieldNames().get(activeAdditionalInfoFields[getLastFieldIndex()]), valuesObj);
				}

				if (!value.trim().equals("")) {
					valuesObj.insertValue(value);
				}
				
				/* Recreates the JPanel... */
				getAdditionalInfoValuePanel().removeAll();
				JTextField textfield = new JTextField(14);
				Font font = textfield.getFont();
				font = new Font(font.getName(), font.getStyle(), 11);
				textfield.setFont(font);
				getAdditionalInfoValuePanel().add(textfield);
				
			} else {
				
				/* Saves the field... */
				movieInfoModel.getFieldValues().set(activeAdditionalInfoFields[getLastFieldIndex()], 
												((JTextField) getAdditionalInfoValuePanel().getComponent(0)).getText());
			}
		}
		
		updateCurrentAdditionalInfoFieldFromModel();
	}

	public void updateCurrentAdditionalInfoFieldFromModel() {

		int [] activeAdditionalInfoFields = MovieManager.getDatabaseHandler().getActiveAdditionalInfoFields();

		int currentFieldIndex = getAdditionalInfoFields().getSelectedIndex();

		String currentFieldIndexValue = ""; //$NON-NLS-1$

		if (currentFieldIndex != -1) {
			currentFieldIndexValue = (String) movieInfoModel.getFieldValues().get(activeAdditionalInfoFields[currentFieldIndex]);

			if (currentFieldIndexValue == null)
				currentFieldIndexValue = "";

			int oldIndex = activeAdditionalInfoFields[currentFieldIndex];

			/* Duration - Displays the new info... */
			if (oldIndex == 1) {
				/* Recreates the panel... */
				getAdditionalInfoValuePanel().removeAll();
				JTextField hoursText = new JTextField(3);

				/* Get the correct fontsize */
				Font font = hoursText.getFont();
				font = new Font(font.getName(), font.getStyle(), 11);
				hoursText.setFont(font);

				hoursText.setDocument(new DocumentRegExp("(\\d)*", 2)); //$NON-NLS-1$
				getAdditionalInfoValuePanel().add(hoursText);
				JLabel separatorOne = new JLabel(" : "); //$NON-NLS-1$
				separatorOne.setFont(new Font(separatorOne.getFont().getName(), Font.PLAIN, separatorOne.getFont().getSize()));
				getAdditionalInfoValuePanel().add(separatorOne);
				JTextField mintsText = new JTextField(3);
				mintsText.setFont(font);
				mintsText.setDocument(new DocumentRegExp("(\\d)*", 2)); //$NON-NLS-1$
				getAdditionalInfoValuePanel().add(mintsText);
				JLabel separatorTwo = new JLabel(" . "); //$NON-NLS-1$
				separatorTwo.setFont(new Font(separatorTwo.getFont().getName(),	Font.PLAIN, separatorTwo.getFont().getSize()));
				getAdditionalInfoValuePanel().add(separatorTwo);
				JTextField secdsText = new JTextField(3);
				secdsText.setFont(font);
				secdsText.setDocument(new DocumentRegExp("(\\d)*", 2)); //$NON-NLS-1$
				getAdditionalInfoValuePanel().add(secdsText);

				JLabel separatorThree = new JLabel("  "); //$NON-NLS-1$
				separatorThree.setFont(new Font(separatorThree.getFont().getName(), Font.PLAIN, separatorThree.getFont().getSize()));
				getAdditionalInfoValuePanel().add(separatorThree);

				/* Displays... */
				if (!movieInfoModel.getFieldValues().get(activeAdditionalInfoFields[currentFieldIndex]).equals("")) { //$NON-NLS-1$

					int time = Integer.parseInt((String) movieInfoModel.getFieldValues().get(activeAdditionalInfoFields[currentFieldIndex]));
					int hours = time / 3600;
					int mints = time / 60 - hours * 60;
					int secds = time - hours * 3600 - mints * 60;
					hoursText.setText(String.valueOf(hours));
					mintsText.setText(String.valueOf(mints));
					secdsText.setText(String.valueOf(secds));
				}
				// Any extra info fields 

				/* Subtitle, Resolution, video codec, video rate, video bit rate, audio codec, autio rate, 
				autio bit rate, audio channels, container, media info, and all the extra info fields */
			}	
			else if (oldIndex == 0 || (oldIndex >=5 && oldIndex <= 12) || oldIndex >= 15) {

				getAdditionalInfoValuePanel().removeAll();

				SteppedComboBox fields;

				fields = new SteppedComboBox(new String[] { "", "" }); //$NON-NLS-1$ //$NON-NLS-2$
				fields.setFont(new Font(fields.getFont().getName(), Font.PLAIN,	fontSize));

				fields.setEditable(true);

				fields.setPreferredSize(new Dimension(valueComboBoxWidth, valueComboBoxHeight));

				DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
	
				/* Getting the stored additional info values */
				AdditionalInfoFieldDefaultValues valuesObj = (AdditionalInfoFieldDefaultValues) 
				MovieManager.getConfig().getAdditionalInfoDefaultValues().get(movieInfoModel.getFieldNames().get(activeAdditionalInfoFields[currentFieldIndex]));

				comboBoxModel.addElement(currentFieldIndexValue);

				FontMetrics fontmetrics = getFontMetrics(getFont());
				int line_widths;

				int maxPopupWidth = fontmetrics.stringWidth(currentFieldIndexValue);

				if (valuesObj != null) {

					ArrayList<String> values = valuesObj.getDefaultValues();
					String temp;

					while (!values.isEmpty()) {
						temp = values.remove(0);
						if (!temp.equals(currentFieldIndexValue)) {
							comboBoxModel.addElement(temp);

							line_widths = fontmetrics.stringWidth(temp);

							if (line_widths > maxPopupWidth)
								maxPopupWidth = line_widths;
						}
					}
				}

				Dimension d = fields.getPreferredSize();

				maxPopupWidth += 20;

				/* Minimum size should be width of combobox */
				if (maxPopupWidth < d.width)
					maxPopupWidth = d.width;

				/* Setting width of the popup menu */

				fields.setPopupWidth(maxPopupWidth);

				fields.setModel(comboBoxModel);

				/* Setting the caret position of the combobox */
				fields.getEditorComponent().setCaretPosition(0);

				getAdditionalInfoValuePanel().add(fields);

				getAdditionalInfoPanel().validate();

				// any regular additional info fields
			} else {
				/* Adds document... */
				((JTextField) getAdditionalInfoValuePanel().
						getComponent(0)).setDocument((Document) _fieldDocuments.get(activeAdditionalInfoFields[currentFieldIndex]));

				((JTextField) getAdditionalInfoValuePanel().getComponent(0)).setText(currentFieldIndexValue);
				((JTextField) getAdditionalInfoValuePanel().getComponent(0)).setCaretPosition(0);
			}
		}

		/* Changes units... */
		getAdditionalInfoUnits().setText((String) _fieldUnits.get(activeAdditionalInfoFields[currentFieldIndex]));
		/* Revalidates... */
		getAdditionalInfoUnits().revalidate();

		/* updates index... */
		setLastFieldIndex(currentFieldIndex);
	}

	/**
	 * If option to add movie to current list on enabled. Use lists, else null
	 * @return
	 */
	public ModelEntry executeCommandSave() {
		
		saveWindowSize();
		
		ArrayList <String> listNames = null;
				
		if (MovieManager.getConfig().getAddNewMoviesToCurrentLists()) {
			listNames = MovieManager.getConfig().getCurrentLists();
		}
				
		return executeCommandSave(listNames);
	}
	
	public void saveWindowSize() {
		MovieManager.getConfig().setAddMovieWindowHeight(getSize().height);
	}
	
	/**
	 * Saves and exits... If column is not null, the movie should be added to
	 * the list named column
	 */
	public ModelEntry executeCommandSave(ArrayList<String> listNames) {

		movieInfoModel._hasReadProperties = false;

		/* Checks the movie title... */
		if (!getMovieTitle().getText().equals("")) { //$NON-NLS-1$
			/* Saves the current field... */
			executeCommandAdditionalInfo();

			try {
				if (!"".equals(movieInfoModel.model.getCover()))
					movieInfoModel.saveCoverToFile();
			} catch (Exception e) {
				log.warn("Error when saving cover to file: " + movieInfoModel.model.getCover());
				log.error("Exception: " + e.getMessage(), e); //$NON-NLS-1$

				DialogAlert alert;

				if (isDisplayable()) {
					alert = new DialogAlert(
							this,
							Localizer.get("DialogMovieInfo.alert.title.access-denied"), 
							Localizer.get("DialogMovieInfo.alert.message.error-when-saving-cover"), e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
				}
				else {
					alert = new DialogAlert(MovieManager.getDialog(),
											Localizer.get("DialogMovieInfo.alert.title.access-denied"), 
											Localizer.get("DialogMovieInfo.alert.message.error-when-saving-cover"), e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
				}
				GUIUtil.showAndWait(alert, true);

				removeCover();

				DialogFolders dialogFolders = new DialogFolders();
				GUIUtil.show(dialogFolders, true);
			}

			updateModelFromGeneralInfo();
			movieInfoModel.saveAdditionalInfoData();
	
			try {
				movieInfoModel.saveToDatabase(listNames);
			} catch (Exception e) {
				log.error("Saving to database failed.", e);
			}

		} else {
			DialogAlert alert = new DialogAlert(this,
												Localizer.get("DialogMovieInfo.alert.title.alert"), 
												Localizer.get("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
			GUIUtil.showAndWait(alert, true);
		}

		/* Remove old cover possibly cached by JTree cellrenderer */
		((ExtendedTreeCellRenderer) MovieManager.getDialog().getMoviesList().getCellRenderer()).removeCoverFromCache(movieInfoModel.model.getCover());

		return movieInfoModel.model;
	}

	/**
	 * 
	 */
	private void updateModelFromGeneralInfo() {
		/*
		 * Updates the general info on the already existing movie
		 */
		movieInfoModel.model.setDate(getDate().getText());
		movieInfoModel.model.setUrlKey(getIMDb().getText());
		movieInfoModel.model.setTitle(getMovieTitle().getText());
		movieInfoModel.model.setDirectedBy(getDirectedBy().getText());
		movieInfoModel.model.setWrittenBy(getWrittenBy().getText());
		movieInfoModel.model.setGenre(getGenre().getText());
		movieInfoModel.model.setRating(getRating().getText());
		movieInfoModel.model.setPersonalRating(getPersonalRating().getText());
		movieInfoModel.model.setPlot(getPlot().getText());
		movieInfoModel.model.setCast(getCast().getText());
		movieInfoModel.model.setNotes(getNotes().getText());
		movieInfoModel.model.setAka(getAka().getText());
		movieInfoModel.model.setCountry(getCountry().getText());
		movieInfoModel.model.setLanguage(getLanguage().getText());
		movieInfoModel.model.setColour(getColour().getText());
		movieInfoModel.model.setCertification(getCertification().getText());
		movieInfoModel.model.setWebSoundMix(getWebSoundMix().getText());
		movieInfoModel.model.setWebRuntime(getWebRuntime().getText());
		movieInfoModel.model.setAwards(getAwards().getText());
		movieInfoModel.model.setMpaa(getMpaa().getText());
	}

	public void reloadMovieList(ModelEntry reloadEntry) {

		if (reloadEntry != null && reloadEntry.getKey() != -1) {

			if (movieInfoModel.isEpisode)
				reloadEntry = new ModelEpisode((ModelEpisode) reloadEntry);
			else
				reloadEntry = new ModelMovie((ModelMovie) reloadEntry);

			/* Reloads... */

			// long time = System.currentTimeMillis();
			MovieManagerCommandSelect.executeAndReload(reloadEntry,	movieInfoModel.isEditMode(), movieInfoModel.isEpisode, true);
		}
	}

	public void reloadMovieListAndClose(ModelEntry reloadEntry) {
		
		if (reloadEntry != null && reloadEntry.getKey() != -1) {
			
			// long time = System.currentTimeMillis();
			MovieManagerCommandSelect.executeAndReload(reloadEntry, movieInfoModel.isEditMode(), movieInfoModel.isEpisode, true);

			saveWindowSize();
			/* Exits... */
			dispose();
		}
	}

	private File[] executeGetFile() {

		File[] file = null;

		try {
			/* Opens the Open dialog... */
			ExtendedFileChooser fileChooser = new ExtendedFileChooser();
			// JFileChooser fileChooser = new JFileChooser();

			fileChooser.setFileSelectionMode(ExtendedFileChooser.FILES_ONLY);

			String[] filterChoices = new String[] { "All Files (*.*)", //$NON-NLS-1$
													"All Files (*.*) Parse media files with MediaInfo library", //$NON-NLS-1$
													"Media files (*.avi, *.ogm, *.mpeg, *.divx, *.ifo)" }; //$NON-NLS-1$
			
			fileChooser.setFileFilter(new CustomFileFilter(new String[] { "*.*" }, filterChoices[0])); //$NON-NLS-1$

			if (!SysUtil.isMac())
				fileChooser.setFileFilter(new CustomFileFilter(new String[] { "*.*" }, filterChoices[1])); //$NON-NLS-1$

			fileChooser.addChoosableFileFilter(new CustomFileFilter(new String[] {
				"avi", "mpg", "mpeg", "ogm", "ogg", "ifo", "divx" }, filterChoices[2])); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

			javax.swing.filechooser.FileFilter[] filters = fileChooser.getChoosableFileFilters();

			for (int i = 0; i < filters.length; i++) {
				if (filters[i].getDescription().equals(MovieManager.getConfig().getLastFileFilterUsed()))
					fileChooser.setFileFilter(filters[i]);
			}

			// fileChooser.setFileFilter(new CustomFileFilter(new
			// String[]{"*.*"}, new String("All Files (*.*)")));
			// Moviemanager.getConfig().setLastFilterUsed(fileChooser.getFileFilter().getDescription());

			if (MovieManager.getConfig().getLastFileDir() != null)
				fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastFileDir());

			fileChooser.setDialogTitle(Localizer.get("DialogMovieInfo.filechooser.title.select-movie-file")); //$NON-NLS-1$
			fileChooser.setApproveButtonText(Localizer.get("DialogMovieInfo.filechooser.approve-button.text")); //$NON-NLS-1$
			fileChooser.setApproveButtonToolTipText(Localizer.get("DialogMovieInfo.filechooser.approve-button.tooltip")); //$NON-NLS-1$
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setMultiSelectionEnabled(true);

			int returnVal = fileChooser.showOpenDialog(this);
			if ((returnVal == JFileChooser.APPROVE_OPTION)) {

				file = fileChooser.getSelectedFiles();

				String lastFileFilter = fileChooser.getFileFilter().getDescription();
				MovieManager.getConfig().setLastFileFilterUsed(lastFileFilter);

				/* Use media info library */
				if (lastFileFilter.equals(filterChoices[1]))
					MovieManager.getConfig().setUseMediaInfoDLL(MediaInfoOption.MediaInfo_Yes);
				else if (SysUtil.isWindows()) /*
												 * Use media info library only
												 * if java parser not available
												 */
					MovieManager.getConfig().setUseMediaInfoDLL(MediaInfoOption.MediaInfo_yesifnojava);
				else
					MovieManager.getConfig().setUseMediaInfoDLL(MediaInfoOption.MediaInfo_No);

				/* Sets the last path... */
				MovieManager.getConfig().setLastFileDir(fileChooser.getCurrentDirectory());
			}

		} catch (Exception e) {
			log.error("" + e); //$NON-NLS-1$
		}
		
		return file;
	}

	private void executeGetDVDInfo() {

		/* Opens the Open dialog... */
		ExtendedFileChooser fileChooser = new ExtendedFileChooser();

		try {
			fileChooser.setFileSelectionMode(ExtendedFileChooser.DIRECTORIES_ONLY);
			
			fileChooser.setFileFilter(new CustomFileFilter(CustomFileFilter.DIRECTORIES_ONLY,
														   Localizer.get("DialogMovieInfo.filechooser.filter.dvd-drive"))); //$NON-NLS-1$
			
			fileChooser.setDialogTitle(Localizer.get("DialogMovieInfo.filechooser.title.select-dvd-drive")); //$NON-NLS-1$
			fileChooser.setApproveButtonText(Localizer.get("DialogMovieInfo.filechooser.approve-button.text")); //$NON-NLS-1$
			fileChooser.setApproveButtonToolTipText(Localizer.get("DialogMovieInfo.filechooser.approve-button.select-dvd-drive.tooltip")); //$NON-NLS-1$
			fileChooser.setAcceptAllFileFilterUsed(false);
			
			if (MovieManager.getConfig().getLastDVDDir() != null)
				fileChooser.setCurrentDirectory(MovieManager.getConfig().getLastDVDDir());

			int returnVal = fileChooser.showOpenDialog(this);

			if (returnVal == ExtendedFileChooser.APPROVE_OPTION) {
				/* Gets the path... */
				File selectedFile = fileChooser.getSelectedFile();

				if (selectedFile.getName().equalsIgnoreCase("AUDIO_TS") || selectedFile.getName().equalsIgnoreCase("VIDEO_TS")) { //$NON-NLS-1$
					selectedFile = selectedFile.getParentFile();
				} 

				File [] list = selectedFile.listFiles();
				
				String video_ts = "";
								
				for (int i = 0; i < list.length && !video_ts.equalsIgnoreCase("VIDEO_TS"); i++)
					video_ts = list[i].getName();
				
				if (!video_ts.equalsIgnoreCase("VIDEO_TS"))
					throw new Exception("DVD drive not found:" + fileChooser.getSelectedFile()); //$NON-NLS-1$
					
				MovieManager.getConfig().setLastDVDDir(selectedFile.getParentFile());

				selectedFile = new File(selectedFile.getAbsolutePath(), video_ts);
				
				/* Get the ifo files */
				list = selectedFile.listFiles();

				ArrayList<File> ifoList = new ArrayList<File>(4);

				for (int i = 0; i < list.length; i++) {
					
					if (list[i].getName().regionMatches(true, list[i].getName().lastIndexOf("."), ".ifo", 0, 4) 
						&& !"VIDEO_TS.IFO".equalsIgnoreCase(list[i].getName())) {//$NON-NLS-1$ //$NON-NLS-2$
						ifoList.add(list[i]);
					}		
				}

				File[] ifo = (File[]) ifoList.toArray(new File[ifoList.size()] );

				if (ifo == null || ifo.length == 0) {
					DialogAlert alert = new DialogAlert(
							this,
							Localizer.get("DialogMovieInfo.alert.title.alert"), 
							Localizer.get("DialogMovieInfo.alert.message.failed-to-locate-the-drive")); //$NON-NLS-1$ //$NON-NLS-2$
					GUIUtil.showAndWait(alert, true);
				} else {

					int biggestSize = 0;
					int biggestSizeIndex = -1;
					int longestDuration = 0;
					int longestDurationIndex = -1;

					int mainIfoIndex = -1;

					FilePropertiesMovie[] fileProperties = new FilePropertiesMovie[ifo.length];

					for (int i = 0; i < ifo.length; i++) {
						try {
							fileProperties[i] = new FilePropertiesMovie(ifo[i].getAbsolutePath(), MovieManager.getConfig().getUseMediaInfoDLL());

							if (ifo[i].length() > biggestSize) {
								biggestSize = (int) ifo[i].length();
								biggestSizeIndex = i;
							}

							if (fileProperties[i].getDuration() > longestDuration) {
								longestDuration = fileProperties[i].getDuration();
								longestDurationIndex = i;
							}

						} catch (Exception e) {
							log.warn("Error when parsing file:" + ifo[i]); //$NON-NLS-1$
						}
					}

					/*
					 * If duration less than 30 minutes, will check the other
					 * ifo files
					 */
					if (fileProperties[biggestSizeIndex].getDuration() < 1800) {

						if (longestDurationIndex != biggestSizeIndex && longestDuration > 1800)
							mainIfoIndex = longestDurationIndex;
					} else {
						mainIfoIndex = biggestSizeIndex;
					}
					
					if (mainIfoIndex == -1) {
						log.debug("Main ifo not found");
						return;
					}
					
					FileSystemView fsv = fileChooser.getFileSystemView();
					File tmp;
					
					if (fsv != null) {
						
						tmp = ifo[mainIfoIndex];

						while (tmp.getParentFile() != null)
							tmp = tmp.getParentFile();

						String displayName = fsv.getSystemDisplayName(tmp);
						displayName = StringUtil.performExcludeParantheses(displayName, false);

						if (!displayName.trim().equals(""))
							fileProperties[mainIfoIndex].setFileName(displayName.trim());
					}

					movieInfoModel.getFileInfo(fileProperties[mainIfoIndex]);

					/* Calculating the size */

					long size = 0;

					for (int i = 0; i < list.length; i++) {
						size += list[i].length();
					}

					size = size / (1024 * 1024);
					
					movieInfoModel.getFieldValues().set(2, String.valueOf((int) size));
				}
			}
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e); //$NON-NLS-1$
		}
	}

	/**
	 * Gets the IMDB info for this movie...
	 */
	protected void executeCommandGetIMDBInfo(boolean useImdbKey) {

		/* Checks the movie title... */
		if (!getMovieTitle().getText().equals("") || !getIMDb().getText().equals("")) { //$NON-NLS-1$

			DialogIMDB dialogIMDB;

			movieInfoModel.model.setTitle(getMovieTitle().getText());

			if (useImdbKey && !getIMDb().getText().equals("")) {
				DialogIMDB.getIMDbInfo(movieInfoModel.model, getIMDb().getText());
			} else {
				dialogIMDB = new DialogIMDB(movieInfoModel.model, null, true);
				GUIUtil.showAndWait(dialogIMDB, true);
			}
			
			try {
				movieInfoModel.modelChanged(this, "GeneralInfo");
	    	} catch (IllegalEventTypeException e) {
	    		log.error("IllegalEventTypeException:" + e.getMessage());
	    	}
		} else {
			DialogAlert alert = new DialogAlert(
					this,
					Localizer.get("DialogMovieInfo.alert.title.alert"), 
					Localizer.get("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
			GUIUtil.showAndWait(alert, true);
		}
	}

	/**
	 * Gets the IMDb for this episode...
	 */
	protected void executeCommandGetEpisodesInfo(boolean useImdbKey) {
		
		/* Checks the movie title... */
		if (!getMovieTitle().getText().equals("") || !getIMDb().getText().equals("")) { //$NON-NLS-1$

			MovieManagerCommandIMDBSearch search = new MovieManagerCommandIMDBSearch(movieInfoModel);
			
			// Search for seasons directly
			if (useImdbKey) {
				movieInfoModel.model.setUrlKey(getIMDb().getText());
				search.executeSeasonSearch(this);
			}
			else {
				movieInfoModel.model.setTitle(getMovieTitle().getText());
				search.executeSeriesSearch(this);
			}
		} else {
			DialogAlert alert = new DialogAlert(
					this,
					Localizer.get("DialogMovieInfo.alert.title.alert"), 
					Localizer.get("DialogMovieInfo.alert.message.please-specify-movie-title")); //$NON-NLS-1$ //$NON-NLS-2$
			GUIUtil.showAndWait(alert, true);
		}
	}


	public void modelUpdatedEvent(ModelUpdatedEvent event) {
		
		if (event.getUpdateType().equals("GeneralInfo"))
			updateGeneralInfoFromModel();
		else
			updateCurrentAdditionalInfoFieldFromModel();
	}

	public void updateGeneralInfoFromModel() {
	
		getMovieTitle().setText(movieInfoModel.model.getTitle());
		getMovieTitle().setCaretPosition(0);

		getDate().setText(movieInfoModel.model.getDate());
		getDate().setCaretPosition(0);

		getIMDb().setText(movieInfoModel.model.getUrlKey());
		getIMDb().setCaretPosition(0);

		getColour().setText(movieInfoModel.model.getColour());
		getColour().setCaretPosition(0);

		getDirectedBy().setText(movieInfoModel.model.getDirectedBy());
		getDirectedBy().setCaretPosition(0);

		getWrittenBy().setText(movieInfoModel.model.getWrittenBy());
		getWrittenBy().setCaretPosition(0);

		getGenre().setText(movieInfoModel.model.getGenre());
		getGenre().setCaretPosition(0);

		getRating().setText(movieInfoModel.model.getRating());
		getRating().setCaretPosition(0);
		
		getPersonalRating().setText(movieInfoModel.model.getPersonalRating());
		getPersonalRating().setCaretPosition(0);

		getCountry().setText(movieInfoModel.model.getCountry());
		getCountry().setCaretPosition(0);

		getLanguage().setText(movieInfoModel.model.getLanguage());
		getLanguage().setCaretPosition(0);

		getPlot().setText(movieInfoModel.model.getPlot());
		getPlot().setCaretPosition(0);

		getCast().setText(movieInfoModel.model.getCast());
		getCast().setCaretPosition(0);

		getAka().setText(movieInfoModel.model.getAka());
		getAka().setCaretPosition(0);

		getCertification().setText(movieInfoModel.model.getCertification());
		getCertification().setCaretPosition(0);

		getWebSoundMix().setText(movieInfoModel.model.getWebSoundMix());
		getWebSoundMix().setCaretPosition(0);

		getWebRuntime().setText(movieInfoModel.model.getWebRuntime());
		getWebRuntime().setCaretPosition(0);

		getAwards().setText(movieInfoModel.model.getAwards());
		getAwards().setCaretPosition(0);

		getMpaa().setText(movieInfoModel.model.getMpaa());
		getMpaa().setCaretPosition(0);

		getNotes().setText(movieInfoModel.model.getNotes());
		getNotes().setCaretPosition(0);

		/* Loads the cover... */
		Image cover = movieInfoModel.getCoverImage();

		if (cover != null) {
			getCover().setIcon(new ImageIcon(cover));
			movieInfoModel.setSaveCover(!movieInfoModel.getNoCover());
		}
		getSeen().setSelected(movieInfoModel.model.getSeen());
	}
	
	
	void setHotkeyModifiers() {
			
		try {			
			
			GUIUtil.enableDisposeOnEscapeKey(shortcutManager, "Close Window", new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					saveWindowSize();
				}
			});		
			
			shortcutManager.registerShowKeysKey();
			
			// ALT+S for Save and close
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Save & Close", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					buttonSaveAndClose.doClick();
				}
			}, buttonSaveAndClose);
			
			
			// ALT+A for Save and Clear
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyboardShortcutManager.getToolbarShortcutMask()), 
					"Save & Clear", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					buttonSave.doClick();
				}
			}, buttonSave);
					
			
			// ALT+D for DVD Info
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Get DVD Info", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					buttonGetDVDInfo.doClick();
				}
			}, buttonGetDVDInfo);
			
			
			// ALT+F for File info
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Get File Info", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					buttonGetFileInfo.doClick();
				}
			}, buttonGetFileInfo);
			
			
			// ALT+M for IMDb info
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Get IMDb Info", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					buttonGetIMDBInfo.doClick();
				}
			}, buttonGetIMDBInfo);
			
			
			// ALT+C for Cancel
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Cancel", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					buttonCancel.doClick();
				}
			}, buttonCancel);
					
			// ALT+N for Notes field 
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Give focus to Notes text area", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					textAreaNotes.requestFocusInWindow();
				}
			});

			// ALT+I for additionalInfo combobox 
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Give focus to additional info dropdown", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					additionalInfoFields.requestFocusInWindow();
				}
			});


			// ALT+V for additionalInfo value field 
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Give focus to additional info value field", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					getAdditionalInfoValuePanel().getComponent(0).requestFocusInWindow();
				}
			});

			// ALT+T for title field 
			shortcutManager.registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyboardShortcutManager.getToolbarShortcutMask()),
					"Give focus to the title field", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					movieTitle.requestFocusInWindow();
				}
			});

			shortcutManager.setKeysToolTipComponent(panelMovieInfo);
			
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
	}
}
