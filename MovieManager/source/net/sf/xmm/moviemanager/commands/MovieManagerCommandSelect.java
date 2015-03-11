/**
 * @(#)MovieManagerCommandSelect.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.LookAndFeelManager.LookAndFeelType;
import net.sf.xmm.moviemanager.MovieManagerConfig.NoCoverType;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandAddEpisode;
import net.sf.xmm.moviemanager.commands.guistarters.MovieManagerCommandEdit;
import net.sf.xmm.moviemanager.http.HttpUtil;
import net.sf.xmm.moviemanager.models.ModelAdditionalInfo;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelHTMLTemplateStyle;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedJTree;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedTreeNode;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.tools.BrowserOpener;

import org.apache.log4j.Logger;
import org.dotuseful.ui.tree.AutomatedTreeNode;
import org.lobobrowser.html.HttpRequest;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.parser.InputSourceImpl;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;


public class MovieManagerCommandSelect extends KeyAdapter implements TreeSelectionListener, MouseListener, ActionListener {

	static Logger log = Logger.getLogger(MovieManagerCommandSelect.class);
	private JMenuItem change, setAllToSeen, setAllToUnseen, addEpisode;
	private JMenu menuApplyToLists, menuRemoveFromLists;

	private static boolean ignoreValueChanged = false;

	static Dimension coverAreaSize = MovieManager.getConfig().getCoverAreaSize();
	
	private static ModelEntry lastSelectedEntry = null;
	
	static File lastTemplateFile = null;
	static StringBuffer lastTemplate = null;
	
	static NoCoverType noCoverType = null;
	static byte [] nocoverData = null;
	
	public static void reloadCurrentModel() {
		Thread t = new Thread() {
    		public void run() {
    			execute();
    		}
    	};
    	t.start();
	}
	
	/**
	 * Executes the command, and reloads the list with the selectedIndex select. 
	 **/
	public static void executeAndReload(int selectedIndex) {

		 ArrayList<ModelMovie> list = MovieManager.getIt().getDatabase().getMoviesList(
				MovieManager.getConfig().getSortOption(), 
				MovieManager.getConfig().getCurrentLists(),
				MovieManager.getConfig().getShowUnlistedEntries());

		MovieManager.getDialog().getMoviesList().setModel(MovieManager.getDialog().createTreeModel(list, MovieManager.getIt().getDatabase().getEpisodeList())); //$NON-NLS-1$

		if (selectedIndex < 0 || selectedIndex > list.size())
			selectedIndex = 0;

		final int index = selectedIndex;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					ignoreValueChanged = true;
					MovieManager.getDialog().getMoviesList().setSelectionRow(index);
					MovieManager.getDialog().setAndShowEntries();
					execute();

					ignoreValueChanged = false;
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		});
	}



	/**
	 * Executes the command, and reloads the list (maintaining the current selected
	 * index visible).
	 **/
	public static void executeAndReload(ModelEntry reloadEntry, boolean edit, boolean isEpisode, boolean execute) {

		ExtendedJTree movieList = (ExtendedJTree) MovieManager.getDialog().getMoviesList();

		/* If Adding */
		if (!edit) {
			
			/* If movie */
			if (!isEpisode) {
				
				ModelEntry.sortCategory = MovieManager.getConfig().getSortOption();

				if ("Title".equals(MovieManager.getConfig().getSortOption())) //$NON-NLS-1$
					ModelEntry.sort = 1;
				else if ("directed".equals(MovieManager.getConfig().getSortOption())) //$NON-NLS-1$
					ModelEntry.sort = 2;
				else if ("Rating".equals(MovieManager.getConfig().getSortOption())) //$NON-NLS-1$
					ModelEntry.sort = 3;
				else if ("Date".equals(MovieManager.getConfig().getSortOption())) //$NON-NLS-1$
					ModelEntry.sort = 4;
				else if ("Duration".equals(MovieManager.getConfig().getSortOption())) //$NON-NLS-1$
					ModelEntry.sort = 5;

				ExtendedTreeNode root = ((ExtendedTreeNode) movieList.getModel().getRoot());

				if (root.getChildCount() == 1 && ((ModelEntry) ((ExtendedTreeNode) root.getFirstChild()).getUserObject()).getKey() == -1)
					root.removeAllChildren();


				ModelMovie newMovie = new ModelMovie((ModelMovie) reloadEntry);
				ExtendedTreeNode newNode = new ExtendedTreeNode(newMovie);
				root.addNode(newNode);

				TreePath newTreePath = new TreePath(newNode.getPath());
				
				movieList.scrollPathToVisible(newTreePath);
				movieList.setSelectionPath(newTreePath);
				
				MovieManager.getDialog().getCurrentMoviesList().add(newMovie);
			}
			else {
				/* If adding episode */
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) movieList.getLastSelectedPathComponent();

				if (node != null && node.getParent() != null) {

					if (!((DefaultMutableTreeNode) node.getParent()).isRoot())
						node = (DefaultMutableTreeNode) node.getParent();
					
					ExtendedTreeNode child = new ExtendedTreeNode(new ModelEpisode((ModelEpisode) reloadEntry));

					int index = 0;
					Enumeration<ExtendedTreeNode> e = node.children();
					
					while (e.hasMoreElements()) {
					
						ExtendedTreeNode child2 = e.nextElement();
						
						if (child.compareTo(child2) < 0)
							break;
						
						index++;
					}
					
					/* Adds the new child to the node */
					node.insert(child, index);
					
					/* expands the node and selects the new child */
					if (execute) {
						TreePath newTreePath = new TreePath(new Object[] {node.getParent(), node, child});
						movieList.setSelectionPath(newTreePath);
						movieList.scrollPathToVisible(newTreePath);
					}
				}
			}
		}
		/* If editing */
		else {
			
			AutomatedTreeNode node = (AutomatedTreeNode) movieList.getLastSelectedPathComponent();

			node.setUserObject(reloadEntry);
			((DefaultTreeModel) MovieManager.getDialog().getMoviesList().getModel()).nodeChanged(node.getParent());
			
			TreePath newTreePath = new TreePath(node.getPath());
			movieList.setSelectionPath(newTreePath);
			movieList.scrollPathToVisible(newTreePath);
		}

		MovieManager.getDialog().setAndShowEntries();

		/* Saves time not to execute when multiadding episodes */
		if (execute)
			execute();
		
		MovieManager.getDialog().getMoviesList().requestFocus(true);

		/*Updates the entries value shown in the right side of the toolbars.*/
		MovieManager.getDialog().setAndShowEntries();
	}

	/**
	 * Fixes the cover data and makes sure the info panels are populated with the movie info.
	 */
	public static void execute() {
		
		
		Dimension coverDim = null;
		//Dimension bigCoverDim = null;
		boolean nocover = true;
		
		byte [] coverData = null;
		
		File coverFile = null;
		BufferedImage image = null;

		ModelEntry model = new ModelMovie();
		JTree movieList = MovieManager.getDialog().getMoviesList();

		/* Makes sure the list is not empty and an object is selected... */
		if (movieList.getModel() != null && movieList.getModel().getChildCount(movieList.getModel().getRoot()) > 0 && movieList.getMaxSelectionRow() != -1) {

			int selectedIndex = movieList.getMaxSelectionRow();

			if (selectedIndex >= movieList.getModel().getChildCount(movieList.getModel().getRoot())) {
				selectedIndex = movieList.getModel().getChildCount(movieList.getModel().getRoot()) -1;
			}

			model = (ModelEntry) ((DefaultMutableTreeNode) movieList.getLastSelectedPathComponent()).getUserObject();

			if (model == null)
				return;

			if (model.getKey() != -1) {

				// Applet uses MySQL
				if (MovieManager.isApplet()) {
					
					if (!model.getHasGeneralInfoData()) {
						model.updateGeneralInfoData(true);
					}
					
					coverData = model.getCoverData();
					nocover = (coverData == null);
				}
				else if (MovieManager.getIt().getDatabase().isMySQL()) {

					File cover = new File(MovieManager.getConfig().getCoversPath(), model.getCover());

					boolean storeLocally = MovieManager.getConfig().getStoreCoversLocally();
					boolean getCoverFromDatabase = !storeLocally ? true : false;

					// If the coverData is not already present in the Model
					if (model.getCoverData() == null) {

						// If the covers can be stored locally the path is checked
						// If the cover exists there is no need to get it from the database
						if (storeLocally && cover.isFile()) {
							getCoverFromDatabase = false;
						}
						else
							getCoverFromDatabase = true;
					}
					
					// If the model does not contain all the general info data it is retrieved
					if (!model.getHasGeneralInfoData()) {
						model.updateGeneralInfoData(getCoverFromDatabase);
					}// has general info
					else if (getCoverFromDatabase) {
						model.updateCoverData();
					}
					
					if (!getCoverFromDatabase && model.getCoverData() == null) {
						byte [] byteBuffer = FileUtil.getResourceAsByteArray(cover);
						model.setCoverData(byteBuffer);
					}
										
					if (model.getCoverData() != null) {

						coverData = model.getCoverData();
						nocover = false;
						
						//Saving the cover to covers directory 
						if (storeLocally) {
							if (!cover.isFile()) 
								FileUtil.writeToFile(model.getCoverData(), cover);
							
							// Used with html debug mode
							coverFile = cover;
						}
					}
				} // Not MySQL
				else {
					
					if (!model.getHasGeneralInfoData()) {
						model.updateGeneralInfoData();
					}
					
					//	if cover available
					if (!model.getCover().equals("")) {
						File cover = new File(MovieManager.getConfig().getCoversPath(), model.getCover());
						
						if (cover.isFile()) {
							coverFile = cover;

							if (model.getCoverData() == null) {
								byte [] byteBuffer = FileUtil.getResourceAsByteArray(cover);
								model.setCoverData(byteBuffer);
							}
						}
					}
										
					coverData = model.getCoverData();
					nocover = (coverData == null);
				}
				
				
				// Getting additional info
				if (!model.getHasAdditionalInfoData())
					model.updateAdditionalInfoData();
	
				ModelAdditionalInfo additionalInfo = model.getAdditionalInfo();

				StringTokenizer tokenizer = new StringTokenizer(additionalInfo.getFileLocation(), "*");
				boolean enable = true;
								
				// Checks the media files and enables play button if files exist
				if (!MovieManager.getConfig().getInternalConfig().getPlayButtonNeverDisabled()) {
					
					enable = false;
					
					while (tokenizer.hasMoreElements()) {
						
						if (MovieManager.getConfig().getInternalConfig().getPlayButtonNeverDisabled()) {
							enable = true;
							break;
						}
						
						String tmp = tokenizer.nextToken();
						
						if (tmp.startsWith("\\\\")) {
						
							//File f = new File(tmp);
							
						//	String tmp2 = "\\\\" + tmp;
							//f = new File(tmp2);
							
							/* Does not check anything at the moment. 
							   Must test this more thoroughly.
							  
							  Set to true no matter
							   */	
							//if (f.isFile()) {
								enable = true;
								break;
							//}
						}
						else if (SysUtil.isWindows()){
							
							String drive = tmp.substring(0, tmp.indexOf(":") + 1);
							
							if (drive.length() == 0)
								continue;
						}

						if (new File(tmp).isFile()) {
							enable = true;
							break;
						}
					}
				}
				MovieManager.getDialog().toolBar.setEnablePlayButton(enable);
			}

			
			// If no cover available, the "no cover image" is used
			if (nocover) {
				
				if (nocoverData == null || noCoverType != MovieManager.getConfig().getNoCoverType()) {
					String nocoverName = MovieManager.getConfig().getNoCoverFilename();
					nocoverData = FileUtil.getResourceAsByteArray("/images/" + nocoverName);
				}
				
				coverData = nocoverData;

				// Writes the no cover image to cover directory
				if (MovieManager.getConfig().getHTMLViewDebugMode() && 
						!(MovieManager.getIt().getDatabase().isMySQL() && !MovieManager.getConfig().getStoreCoversLocally())) {

					File cover = new File(MovieManager.getConfig().getCoversPath());

					// Valid cover dir
					if (cover.isDirectory()) {
						cover = new File(cover, MovieManager.getConfig().getNoCoverFilename());

						// Create nocover file on disk
						if (!cover.isFile()) {
							FileUtil.writeToFile(nocoverData, cover);
						}
						coverFile = cover;
					}
				}
			}

			// Gets the cover height, and creates the image object
			try {
				InputStream in = new ByteArrayInputStream(coverData);

				if (in != null) {
					
					coverDim = coverAreaSize.getSize();
					
					image = javax.imageio.ImageIO.read(in);
					
					if (nocover){
						coverDim.width = image.getWidth();
						coverDim.height = image.getHeight();
					}
					else if (MovieManager.getConfig().getPreserveCoverAspectRatio() != 0) {														

						if (MovieManager.getConfig().getPreserveCoverAspectRatio() == 1 || model.isEpisode() || nocover)
							coverDim.height = ((coverAreaSize.width * image.getHeight())/image.getWidth());
					} 

					if (coverDim.height > coverAreaSize.height)
						coverDim.height = coverAreaSize.height;

				}
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage(), e); //$NON-NLS-1$
			} 
		}
		
		Image cover = null;

		if (image != null)
			cover = image.getScaledInstance(coverAreaSize.width, coverDim.height, Image.SCALE_SMOOTH);

		// Find cover dimension
		if (nocover && image != null)
			coverDim =  new Dimension(image.getWidth(), image.getHeight());
				
		if (MovieManager.getDialog().getCurrentMainTabIndex() == 0) {
			updateStandardPanel(model, cover);
		}
		else {
			updateHTMLPanel(model, coverFile, coverData, coverDim, nocover);
		}
	}


	static java.awt.Color textfieldForegroundColor = null;		
	
	/**
	 * Updates the standard panel with the movie info of the current model
	 * 
	 * @param model
	 * @param cover
	 */
	public static void updateStandardPanel(ModelEntry model, Image cover) {

		if (cover != null)
			MovieManager.getDialog().getCover().setIcon(new ImageIcon(cover));
		else {
			MovieManager.getDialog().getCover().setIcon(new ImageIcon(FileUtil.getImage("/images/" + MovieManager.getConfig().getNoCoverFilename()).getScaledInstance(coverAreaSize.width, coverAreaSize.height, Image.SCALE_SMOOTH))); //$NON-NLS-1$
		}

		/* Removes mouse listeners */
		for (int i = 0; i < MovieManager.getDialog().getCover().getMouseListeners().length; i++) {
			MovieManager.getDialog().getCover().removeMouseListener(MovieManager.getDialog().getCover().getMouseListeners()[i]);
			i--;
		}

		if (model != null && !model.getUrlKey().equals("")) { //$NON-NLS-1$
			MovieManager.getDialog().getCover().addMouseListener(new MovieManagerCommandOpenPage(model.getCompleteUrl())); //$NON-NLS-1$ //$NON-NLS-2$
			MovieManager.getDialog().getCover().setToolTipText(Localizer.get("MovieManagerCommandSelect.show-cover.tooltip.open-in-browser")); //$NON-NLS-1$
		} else {
			MovieManager.getDialog().getCover().setToolTipText(null);
		}

		if (model == null || model.getDate().equals("")) //$NON-NLS-1$
			MovieManager.getDialog().getDateField().setText(""); //$NON-NLS-1$
		else {
			MovieManager.getDialog().getDateField().setText("("+ model.getDate() +") "); //$NON-NLS-1$ //$NON-NLS-2$
			MovieManager.getDialog().getDateField().setCaretPosition(0);
		}

		if (model != null) {

			MovieManager.getDialog().getTitleField().setText(model.getTitle());
			MovieManager.getDialog().getTitleField().setCaretPosition(0);

			MovieManager.getDialog().getDirectedByField().setText(model.getDirectedBy());
			MovieManager.getDialog().getDirectedByField().setCaretPosition(0);

			MovieManager.getDialog().getWrittenByField().setText(model.getWrittenBy());
			MovieManager.getDialog().getWrittenByField().setCaretPosition(0);

			MovieManager.getDialog().getGenreField().setText(model.getGenre());
			MovieManager.getDialog().getGenreField().setCaretPosition(0);

			String rating = model.getRating();
						
			if (model.getPersonalRating() != null && !model.getPersonalRating().equals(""))
				rating += " ("+model.getPersonalRating()+")";
				
			MovieManager.getDialog().getRatingField().setText(rating);

			StringBuffer misc = new StringBuffer();

			Object font = UIManager.get("TextField.font"); //$NON-NLS-1$
			String fontname = "Dialog.plain"; //$NON-NLS-1$

			if (font != null)
				fontname = ((Font) font).getFontName();

			String fontSize = "3";
			
			if (MovieManager.getLookAndFeelManager().getLookAndFeelType() == LookAndFeelType.CustomLaF &&
							("Nimbus".equals(MovieManager.getLookAndFeelManager().getCustomLookAndFeel()) ||
									(MovieManager.getLookAndFeelManager().getCustomLookAndFeel().indexOf("Synthetica") != -1)))
				fontSize = "4";
			
			/*
			java.util.Map<java.awt.font.TextAttribute, ?> m = new javax.swing.JTextArea().getFont().getAttributes();
			
			java.util.Set<java.awt.font.TextAttribute> keys = m.keySet();
			
			for (java.awt.font.TextAttribute key : keys) {
				System.err.println(m.get(key));
			}
			*/
			
			if (textfieldForegroundColor == null) {

				try {
					GUIUtil.invokeAndWait(new Runnable() {

						public void run() {
							textfieldForegroundColor = new javax.swing.JTextArea().getForeground();
						}
					});
				} catch (Exception e) {
					log.warn("Exception:" + e.getMessage());
				}
			}
						
			misc.append("<html><font size="+fontSize+" face=\""+ fontname + "\" color=\"#"+ SysUtil.colourToString(textfieldForegroundColor) + "\">"); //$NON-NLS-1$ //$NON-NLS-2$

			if (!model.getWebSoundMix().equals("")) //$NON-NLS-1$
				misc.append("<b>" + Localizer.get("MovieManagerCommandSelect.miscellaneous-panel.field.sound-mix.title") + "</b><br>" + model.getWebSoundMix() + "<br><br>"); //$NON-NLS-1$ //$NON-NLS-2$

			if (!model.getWebRuntime().equals("")) //$NON-NLS-1$
				misc.append("<b>" + Localizer.get("MovieManagerCommandSelect.miscellaneous-panel.field.runtime.title") + ":</b><br>" + model.getWebRuntime() + "<br><br>"); //$NON-NLS-1$ //$NON-NLS-2$

			if (!model.getAwards().equals("")) //$NON-NLS-1$
				misc.append("<b>" + Localizer.get("MovieManagerCommandSelect.miscellaneous-panel.field.awards.title") +":</b><br>" + model.getAwards() + "<br><br>"); //$NON-NLS-1$ //$NON-NLS-2$

			if (!model.getMpaa().equals("")) //$NON-NLS-1$
				misc.append("<b>" + Localizer.get("MovieManagerCommandSelect.miscellaneous-panel.field.mpaa.title") + ":</b><br>"  + model.getMpaa() + "<br><br>"); //$NON-NLS-1$ //$NON-NLS-2$

			if (!model.getCertification().equals("")) //$NON-NLS-1$
				misc.append("<b>" + Localizer.get("MovieManagerCommandSelect.miscellaneous-panel.field.certification.title") + ":</b><br>" + model.getCertification() + "<br><br>"); //$NON-NLS-1$

			if (!model.getAka().equals("")) //$NON-NLS-1$
				misc.append("<b>" + Localizer.get("MovieManagerCommandSelect.miscellaneous-panel.field.also-known-as.title") + ":</b><br>"  + model.getAka().replaceAll("\r\n|\n", "<br>") ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

			misc.append("</font></html>"); //$NON-NLS-1$
			
			MovieManager.getDialog().getMiscellaneous().setText(misc.toString());
			MovieManager.getDialog().getMiscellaneous().setCaretPosition(0);

			if (model.getCountry().equals("")) { //$NON-NLS-1$
				MovieManager.getDialog().getCountryLabel().setText(""); //$NON-NLS-1$
				MovieManager.getDialog().getCountryTextField().setText(""); //$NON-NLS-1$
			}
			else {
				MovieManager.getDialog().getCountryLabel().setText(Localizer.get("DialogMovieManager.movie-info-panel.country")+ ": "); //$NON-NLS-1$
				MovieManager.getDialog().getCountryTextField().setText(model.getCountry());
				MovieManager.getDialog().getCountryTextField().setCaretPosition(0);
			}

			if (model.getLanguage().equals("")) { //$NON-NLS-1$
				MovieManager.getDialog().getLanguageLabel().setText(""); //$NON-NLS-1$
				MovieManager.getDialog().getLanguageTextField().setText(""); //$NON-NLS-1$
			}
			else {
				MovieManager.getDialog().getLanguageLabel().setText(Localizer.get("DialogMovieManager.movie-info-panel.language")+ ": "); //$NON-NLS-1$
				MovieManager.getDialog().getLanguageTextField().setText(model.getLanguage());
				MovieManager.getDialog().getLanguageTextField().setCaretPosition(0);
			}

			/* Must be a spaces to avoid the top row from collapsing */
			if (model.getColour().equals("")) { //$NON-NLS-1$
				MovieManager.getDialog().getColourLabel().setText(" "); //$NON-NLS-1$
				MovieManager.getDialog().getColourField().setText(" "); //$NON-NLS-1$
			}
			else {
				MovieManager.getDialog().getColourLabel().setText(""); //$NON-NLS-1$
				MovieManager.getDialog().getColourField().setText(model.getColour() + " "); //$NON-NLS-1$
			}

			MovieManager.getDialog().getSeen().setSelected(model.getSeen());

			MovieManager.getDialog().getPlot().setText(model.getPlot());
			MovieManager.getDialog().getPlot().setCaretPosition(0);

			MovieManager.getDialog().getCast().setText(model.getCast());
			MovieManager.getDialog().getCast().setCaretPosition(0);

			String additionalInfoString = model.getAdditionalInfo().getAdditionalInfoString();

			/* Stores the additional info scollBar position */
			if (MovieManager.getDialog().getAdditionalInfoScrollPane() != null) {
				final int verticalPosition = MovieManager.getDialog().getAdditionalInfoScrollPane().getVerticalScrollBar().getValue();
				MovieManager.getDialog().getAdditionalInfo().setText(additionalInfoString);

				Runnable restoreScrollBarPosition = new Runnable() {
					public void run() {
						MovieManager.getDialog().getAdditionalInfoScrollPane().getVerticalScrollBar().setValue(verticalPosition);
					}
				};
				
				/* Restores the additional info scollBar position */
				SwingUtilities.invokeLater(restoreScrollBarPosition);
			}

			if (MovieManager.getDialog().getNotes() != null) {
				MovieManager.getDialog().getNotes().setText(model.getNotes());
				MovieManager.getDialog().getNotes().setCaretPosition(0);
			}
		}
		
		JTree movieList = MovieManager.getDialog().getMoviesList();
		TreeNode selected = ((TreeNode) movieList.getLastSelectedPathComponent());
		int horizontalPosition = MovieManager.getDialog().getMoviesListScrollPane().getHorizontalScrollBar().getValue();
		
		if (selected != null && movieList instanceof ExtendedJTree) {
			// Scrolls to the selected row
	 
			if (movieList.getLastSelectedPathComponent() != null && 
					((DefaultMutableTreeNode) movieList.getLastSelectedPathComponent()).getUserObject() instanceof ModelEpisode && movieList.getSelectionCount() == 1) {
				((ExtendedJTree) movieList).scrollPathToVisible2(movieList.getSelectionPath(), horizontalPosition);
			}
			else
				((ExtendedJTree) movieList).scrollPathToVisible2(movieList.getSelectionPath(), horizontalPosition);
		}
	}

	
	public static void resetHTMLTemplateChace() {
		lastTemplateFile = null;
	}

	/**
	 * Updates the HTML panel with the movie info of the current model
	 * 
	 * @param model
	 * @param coverFile
	 * @param coverDim
	 */
	//public static void updateHTMLPanel(ModelEntry model, File coverFile, Dimension coverDim, boolean nocover) {
	public static void updateHTMLPanel(ModelEntry model, File coverFile, byte [] coverData, Dimension coverDim, boolean nocover) {
		
		// Disabled in internal config
		if (MovieManager.getConfig().getInternalConfig().getDisableHTMLView())
			return;
		
		try {
						
			File templateFile = MovieManager.getConfig().getHTMLTemplateHandler().getHTMLTemplateFile();
			
			if (templateFile == null || !templateFile.isFile()) {
				log.warn("Current template file is missing:" + templateFile);
				return;
			}
			
			
			// Caches the template
			if (MovieManager.getConfig().getHTMLViewDebugMode() || lastTemplateFile == null || 
					!templateFile.getAbsolutePath().equals(lastTemplateFile.getAbsolutePath())) {
				lastTemplate = FileUtil.readFileToStringBuffer(templateFile);
				lastTemplateFile = templateFile;
			}
			
			// Copy previous template data
			StringBuffer template = new StringBuffer(lastTemplate);
			
			processTemplateData(template, model);
			
		
			String coverPath = null;
			
			if (coverFile != null)
				coverPath = coverFile.toURI().toString();
			
			// If coverPath is null, the cover will be set to $coverSmall$ and loaded from memory
			processTemplateCover(template, coverPath, coverDim, nocover);
			//processTemplateBigCover(template, coverPath, coverDim);
			
			processTemplateCssStyle(template);
							
			template = HttpUtil.getHtmlNiceFormat(template);
			
			if (MovieManager.getConfig().getHTMLViewDebugMode()) {
				
				String debugName = "debug_template.html";
				
				File path;
								
				if (SysUtil.isMac() || SysUtil.isWindowsVista() || SysUtil.isWindows7())
					path = new File(SysUtil.getConfigDir(), debugName);
				else
					path = new File(templateFile.getParentFile(), debugName); 
					
				FileUtil.writeToFile(path, template.toString());
			}
			
			Reader r = new StringReader(template.toString());
			InputSourceImpl impl = new InputSourceImpl(r, templateFile.getParentFile().toURI().toString());
						
			final SimpleHtmlRendererContext rcontext = new SimpleHtmlRendererContext(MovieManager.getDialog().htmlPanel, new XMMUserAgentContext(coverData)) {
				// Opens browser when link is clicked
				public void navigate(java.net.URL url, java.lang.String target) {
					BrowserOpener opener = new BrowserOpener(url.toString());
					opener.executeOpenBrowser(MovieManager.getConfig().getSystemWebBrowser(), MovieManager.getConfig().getBrowserPath());
				}
			};
			UserAgentContext ucontext = rcontext.getUserAgentContext();
			DocumentBuilderImpl dbi = new DocumentBuilderImpl(ucontext, rcontext);
			Document document = dbi.parse(impl);
			
			if (document != null)
				MovieManager.getDialog().setHTMLData(document, rcontext);
			
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
	}
	
	
	
	public static void processTemplateCover(StringBuffer template, String coverPath, Dimension coverDim, boolean preserveAspect) {
			
		if (coverPath == null)
			coverPath = "$CoverSmall$";

		if (template.indexOf("$Cover$") != -1) {

			String cover = "style=\"width:" + coverDim.width + "px;height:" + coverDim.height + "px;\" src=\""+coverPath+"\" alt=\"Cover\"";

			// ReplaceAll takes a regular expression.
			// Therefore every "\" must be duplicated
			cover = cover.replaceAll("\\\\", "\\\\\\\\");
			StringUtil.replaceAll(template, "$Cover$", cover);
		}
		
		// <img $Cover-width:200-height:300$> 
		
		Pattern p = Pattern.compile("\\$Cover-width:(\\d+)-height:(\\d+)\\$");
		Matcher m = p.matcher(template);

		if (m.find()) {

			//String coverAndLink = "<a href=\"$movie-episode-Url$\"> <img $Cover-width:"+m.group(1)+"-height:"+m.group(2)+"$> </a>";
			//StringUtil.replaceAll(template , m.group(0), coverAndLink);
			
			preserveAspect = false;
			
			String cover = "style=\"width:" + (preserveAspect ? coverDim.width : m.group(1)) + "px;height:" + (preserveAspect ? coverDim.height : m.group(2)) + "px;\" src=\""+coverPath+"\" alt=\"Cover\"";

			// ReplaceAll takes a regular expression.
			// Therefore every "\" must be duplicated
			cover = cover.replaceAll("\\\\", "\\\\\\\\");
			StringUtil.replaceAll(template, m.group(0), cover);
		}
	}
		
	
	/**
	 * 	@param 		html template
	 *  @Return		Css style file.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void processTemplateCssStyle(StringBuffer data) throws FileNotFoundException, IOException {
				
		ModelHTMLTemplateStyle style = MovieManager.getConfig().getHTMLTemplateHandler().getHTMLTemplateStyle();
		
		if (style == null) {
			log.debug("No style for template " + MovieManager.getConfig().getHTMLTemplateHandler().getHTMLTemplate());
			return;
		}
		
		String templateStyle = "Styles/" + style.getCssFileName();
		StringUtil.replaceAll(data, "$css-style$", templateStyle);
	
	}
	
	public static void processTemplateData(StringBuffer template, ModelEntry model) throws FileNotFoundException, IOException {
		
		StringUtil.replaceAll(template, "$ReleaseDate$", "" + model.getDate());
		StringUtil.replaceAll(template, "$MovieTitle$", model.getTitle());
		StringUtil.replaceAll(template, "$DirectedBy$", model.getDirectedBy());
		StringUtil.replaceAll(template, "$WrittenBy$", model.getWrittenBy());
		StringUtil.replaceAll(template, "$Genre$", model.getGenre());
		
		String rating = model.getRating();
		
		if (model.getPersonalRating() != null && !model.getPersonalRating().equals(""))
			rating += " ("+model.getPersonalRating()+")";
		
		StringUtil.replaceAll(template, "$Rating$", rating);
		
		StringUtil.replaceAll(template, "$Seen$", model.getSeen() ? "Yes" : "No");
		StringUtil.replaceAll(template, "$Country$", model.getCountry());
		StringUtil.replaceAll(template , "$Language$", model.getLanguage());
		
		String aka = model.getAka();
		aka = aka.replaceAll("\\n", "<br>");
		
		StringUtil.replaceAll(template , "$Aka$", aka);
		StringUtil.replaceAll(template , "$Plot$", model.getPlot());
		StringUtil.replaceAll(template , "$Cast$", model.getCast());
		StringUtil.replaceAll(template , "$Notes$", model.getNotes());
		StringUtil.replaceAll(template , "$Mpaa$", model.getMpaa());
		StringUtil.replaceAll(template , "$Certification$", model.getCertification());
		StringUtil.replaceAll(template , "$SoundMix$", model.getWebSoundMix());
		StringUtil.replaceAll(template , "$WebRuntime$", model.getWebRuntime());
		StringUtil.replaceAll(template , "$Awards$", model.getAwards());
		StringUtil.replaceAll(template , "$Colour$", model.getColour());
		
		// $movie-episode-coverAndLink-width:300-height:200$
		Pattern p = Pattern.compile("\\$coverAndLink-width:(\\d+)-height:(\\d+)\\$");
		
		Matcher m = p.matcher(template);

		if (m.find()) {
			String coverAndLink = "<a href=\"$movie-episode-Url$\"> <img $Cover-width:"+ m.group(1) +"-height:"+ m.group(2) +"$ class=\"center\" /> </a>";
			StringUtil.replaceAll(template , m.group(0), coverAndLink);
		}
		
		if (!model.getUrlKey().equals("")) {
			String coverAndLink = "<a href=\"$movie-episode-Url$\"> <img $Cover$>  </a>";
			StringUtil.replaceAll(template , "$coverAndLink$", coverAndLink);
		}
		else {
			String coverAndNoLink = "<img $Cover$>";
			StringUtil.replaceAll(template , "$coverAndLink$", coverAndNoLink);
		}
		
		StringUtil.replaceAll(template , "$UrlKey$", model.getUrlKey());
		StringUtil.replaceAll(template , "$movie-episode-Url$", model.getCompleteUrl());
				
		StringUtil.replaceAll(template , "$subtitles$", model.getAdditionalInfo().getSubtitles());
				
		int duration = model.getAdditionalInfo().getDuration();
		
		int hours = (duration/3600);
		int minutes = duration/60 - (hours * 60);
		int seconds = duration - minutes * 60 - hours * 3600;
		String durationString = hours  + ":" + minutes + ":" + seconds;
		
		StringUtil.replaceAll(template , "$duration$", durationString);
		StringUtil.replaceAll(template , "$fileSize$", "" + model.getAdditionalInfo().getFileSize());
		StringUtil.replaceAll(template , "$cDs$", "" + model.getAdditionalInfo(). getCDs());
		StringUtil.replaceAll(template , "$cDCases$", "" + model.getAdditionalInfo().getCDCases());
		StringUtil.replaceAll(template , "$resolution$", model.getAdditionalInfo().getResolution());
		StringUtil.replaceAll(template , "$videoCodec$", model.getAdditionalInfo().getVideoCodec());
		StringUtil.replaceAll(template , "$videoRate$", model.getAdditionalInfo().getVideoRate());
		StringUtil.replaceAll(template , "$videoBitrate$", model.getAdditionalInfo().getVideoBitrate());
		StringUtil.replaceAll(template , "$audioCodec$", model.getAdditionalInfo().getAudioCodec());
		StringUtil.replaceAll(template , "$audioRate$", model.getAdditionalInfo().getAudioRate());
		StringUtil.replaceAll(template , "$audioBitrate$", model.getAdditionalInfo().getAudioBitrate());
		StringUtil.replaceAll(template , "$audioChannels$", model.getAdditionalInfo().getAudioChannels());
		StringUtil.replaceAll(template , "$fileLocation$", model.getAdditionalInfo().getFileLocation());
		StringUtil.replaceAll(template , "$fileCount$", "" + model.getAdditionalInfo().getFileCount());
		StringUtil.replaceAll(template , "$container$", model.getAdditionalInfo().getContainer());
		StringUtil.replaceAll(template , "$mediaType$", model.getAdditionalInfo().getMediaType());
		
		StringUtil.replaceAll(template , "$AdditionalInfoString$", model.getAdditionalInfo().getAdditionalInfoString("<br>"));
	}
	
	
	public void mouseClicked(MouseEvent event) {
		
		/* If Button1 and more than 1 click the node is expanded/collapsed */
		if (GUIUtil.isLeftMouseButton(event)) {
			if (event.getClickCount() >= 2) {
				JTree movieList = MovieManager.getDialog().getMoviesList();
				int rowForLocation = movieList.getRowForLocation(event.getX(), event.getY());

				if (rowForLocation == -1) {
					int width = movieList.getWidth();
					// In JTree, the "row" stops where the text of the entry ends. Therefore we have to ignore the x value.
					// Since the roothandle the icon also disturbs the getRowForLocation method we have to try different values of X.
					for (int i = 0; rowForLocation == -1 && i < width; i++)
						rowForLocation = movieList.getRowForLocation(i, event.getY());
				}
				
				Object o = movieList.getLastSelectedPathComponent();
				
				// Open edit dialog on double click, if clicked entry has no children
				if (o != null && ((DefaultMutableTreeNode) o).isLeaf())
					MovieManagerCommandEdit.execute();

				// otherwise, expand/collapse row (also works without Ctrl pressed)
				else if (SysUtil.isCtrlPressed(event)) {
					if (movieList.isCollapsed(rowForLocation))
						movieList.expandRow(rowForLocation);
					else
						movieList.collapseRow(rowForLocation);
				}
			}
		}
	}
	
	
	/**
	 * Checks the value of the seenEditable(in MainWindow) variable in MovieManager 
	 * and performes the appropriate action. A popup menu window is shown at 
	 * the selected index when clicking right mouse button.
	 **/
	public void mousePressed(MouseEvent event) {

		JTree movieList = MovieManager.getDialog().getMoviesList();
		int rowForLocation = movieList.getRowForLocation(event.getX(), event.getY());

		if (rowForLocation == -1) {
			int width = movieList.getWidth();
			// In JTree, the "row" stops where the text of the entry ends. Therefore we have to ignore the x value.
			// Since the roothandle the icon also disturbs the getRowForLocation method we have to try different values of X.
			for (int i = 0; rowForLocation == -1 && i < width; i++)
				rowForLocation = movieList.getRowForLocation(i, event.getY());
		}
	
				
		TreePath selectionRow =  null;
		
		if (rowForLocation == -1 || ((selectionRow = movieList.getPathForRow(rowForLocation)) == null)) {
			return;
		}
				
		ModelEntry leadSelectionObject = (ModelEntry) ((DefaultMutableTreeNode) selectionRow.getLastPathComponent()).getUserObject();
		int selectionCount = movieList.getSelectionCount();
				
		/* Button 2 */
		if (GUIUtil.isRightMouseButton(event)) {

			if (!movieList.isRowSelected(rowForLocation)) {
				movieList.setSelectionRow(rowForLocation);
				selectionCount = 1;
			}

			makeMovieListPopupMenu(event.getX(), event.getY(), event, leadSelectionObject, selectionCount);
		}

		
		/* Button 1 */
		else if (GUIUtil.isLeftMouseButton(event)) {
			
			if (MovieManager.getConfig().getEnableCtrlMouseRightClick() && SysUtil.isCtrlPressed(event)) {
				makeMovieListPopupMenu(event.getX(), event.getY(), event, leadSelectionObject, selectionCount);
			}
		}
	}
		
	
	public void actionPerformed(ActionEvent event) {

		if (event.getSource().equals(change))
			MovieManager.getDialog().updateSeen(0);

		else if (event.getSource().equals(setAllToSeen))
			MovieManager.getDialog().updateSeen(1);

		else if (event.getSource().equals(setAllToUnseen))
			MovieManager.getDialog().updateSeen(2);

		else if (menuApplyToLists != null) {
			MovieManager.getDialog().getPanelMovieList().setCursor(new Cursor(Cursor.WAIT_CURSOR));
			handleAppyToLists(event);
			MovieManager.getDialog().getPanelMovieList().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	void handleAppyToLists(ActionEvent event) {

		log.debug("ActionPerformed - handleAppyToLists");
		
		Component[] applyComponents = menuApplyToLists.getMenuComponents();
		Component[] removeComponents = menuRemoveFromLists.getMenuComponents();

		String columnName = event.getActionCommand();
		
		/* = false; */
		int mode = -1;

		for (int i = 0; i < applyComponents.length; i++) {
			if (applyComponents[i].equals(event.getSource())) {
				mode = 1;
				break;
			}
			else if (removeComponents[i].equals(event.getSource())) {
				mode = 0;
				break;
			}
		}

		boolean apply = false;

		if (mode == -1)
			return;
		else if (mode == 1)
			apply = true;

		TreePath[] selectedPaths = MovieManager.getDialog().getMoviesList().getSelectionPaths();
		ModelEntry temp;
		
		for (int i = 0; i < selectedPaths.length; i++) {
			temp = (ModelEntry) ((DefaultMutableTreeNode) selectedPaths[i].getLastPathComponent()).getUserObject();

			if (temp.isMovie()) {
				MovieManager.getIt().getDatabase().setLists(temp.getKey(), columnName, new Boolean(apply));
				
				if (apply)
					temp.addToMemberOfList(columnName);
				else
					temp.removeAsMemberOfList(columnName);
			}
		}
	}
	
	
	public void keyPressed(KeyEvent e) {

		/* 127 == delete key */
		if (KeyStroke.getKeyStrokeForEvent(e).getKeyCode() == 127 && !MovieManager.getConfig().getInternalConfig().isRemoveMovieDisabled())
			MovieManagerCommandRemove.execute();
	}

	public void mouseReleased(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	

	
	public void makeMovieListPopupMenu(int x, int y, MouseEvent event, ModelEntry selected, int selectionCount) {

		JTree movieList = MovieManager.getDialog().getMoviesList();
		boolean isSeenEditable = MovieManager.getConfig().getSeenEditable();
		JPopupMenu popupMenu = null;

		// Disabled in internal config
		if (MovieManager.getConfig().getInternalConfig().getMovieListPopupDisabled())
			return;
		
		if (selectionCount <= 1) {

			popupMenu = new JPopupMenu();

			int key = selected.getKey();

			/* Not a valid movie/episode entry */
			if (key == -1)
				return;

			boolean seen = selected.getSeen();

			if (selected instanceof ModelMovie) {
				popupMenu.add(addEpisode = new JMenuItem(Localizer.get("MovieManagerCommandSelect.movie-list-popup.add-episodes"))); //$NON-NLS-1$
				addEpisode.addActionListener(new MovieManagerCommandAddEpisode());
			}

			if (isSeenEditable) {

				if (popupMenu.getSubElements().length > 0)
					popupMenu.add(new JPopupMenu.Separator());

				if (seen)
					popupMenu.add(change = new JMenuItem(Localizer.get("MovieManagerCommandSelect.movie-list-popup.change-to-seen"))); //$NON-NLS-1$
				else 
					popupMenu.add(change = new JMenuItem(Localizer.get("MovieManagerCommandSelect.movie-list-popup.change-to-unseen"))); //$NON-NLS-1$

				change.addActionListener(this);
			}
		}
		else {
			popupMenu = new JPopupMenu();

			if (isSeenEditable) {
				popupMenu.add(setAllToSeen = new JMenuItem(Localizer.get("MovieManagerCommandSelect.movie-list-popup.set-selected-to-seen"))); //$NON-NLS-1$
				popupMenu.add(setAllToUnseen = new JMenuItem(Localizer.get("MovieManagerCommandSelect.movie-list-popup.set-selected-to-unseen"))); //$NON-NLS-1$
				setAllToSeen.addActionListener(this);
				setAllToUnseen.addActionListener(this);
			}
		}


		if (popupMenu.getSubElements().length > 0)
			popupMenu.add(new JPopupMenu.Separator());

		ArrayList<String> listcolumns = MovieManager.getIt().getDatabase().getListsColumnNames();
	
		
		if (listcolumns.size() > 0 && !selected.isEpisode()) {
			
			menuApplyToLists = new JMenu(Localizer.get("MovieManagerCommandSelect.movie-list-popup.apply-to-list")); //$NON-NLS-1$
			menuRemoveFromLists = new JMenu(Localizer.get("MovieManagerCommandSelect.movie-list-popup.remove-from-list")); //$NON-NLS-1$
			JMenuItem temp, temp2;
			
			while (!listcolumns.isEmpty()) {
				temp = new JCheckBoxMenuItem((String) listcolumns.get(0));
				temp2 = new JCheckBoxMenuItem((String) listcolumns.get(0));
				
				if (selected.isMemberOfList((String) listcolumns.get(0))) {
					temp.setSelected(true);
					temp2.setSelected(true);
				}
				
				listcolumns.remove(0);
				temp.addActionListener(this);
				temp2.addActionListener(this);
				menuApplyToLists.add(temp);
				menuRemoveFromLists.add(temp2);
			}
			popupMenu.add(menuApplyToLists);
			popupMenu.add(menuRemoveFromLists);
		}


		// Disable when in applet mode 
		if (MovieManager.isApplet()) {
			ArrayList<MenuElement> menuItems = new ArrayList<MenuElement>();

			MenuElement [] elements  = popupMenu.getSubElements();
			menuItems.addAll(Arrays.asList(elements));

			while (!menuItems.isEmpty()) {

				if (menuItems.get(0) instanceof JMenuItem)
					((JMenuItem) menuItems.remove(0)).setEnabled(false);
				else {
					elements = ((MenuElement) menuItems.remove(0)).getSubElements();
					menuItems.addAll(Arrays.asList(elements));
				}
			}
		}

		popupMenu.setInvoker(movieList);
		popupMenu.setLocation(x, y);

		popupMenu.show(movieList, x, y);
	}

	
	/**
	 * Invoked when an action occurs.
	 **/
	public void valueChanged(TreeSelectionEvent event) {

		if (ignoreValueChanged)
			return;

		TreePath [] paths = event.getPaths();

		/* If less than 2 changed no need to call execute */
		if (paths != null && paths.length <= 1) {
			return;
		}

		/* Saving the node's changed notes value */
		if (lastSelectedEntry != null && lastSelectedEntry.getNotes() != null && MovieManager.getDialog().getNotes() != null) {
			
			if (!lastSelectedEntry.getNotes().equals(MovieManager.getDialog().getNotes().getText())) {
				lastSelectedEntry.setNotes(MovieManager.getDialog().getNotes().getText());
				lastSelectedEntry.hasChangedNotes = true;
				
				if (lastSelectedEntry.isEpisode())
					ModelEpisode.notesHaveBeenChanged = true;
				else
					ModelMovie.notesHaveBeenChanged = true;
			}
		}
		
		TreePath path = (TreePath) MovieManager.getDialog().getMoviesList().getSelectionPath();
		
		if (path != null) {
			ModelEntry entry = (ModelEntry) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
			lastSelectedEntry = entry;
		}
			
		if (!MovieManager.getIt().isDeleting()) {
			execute();
		}
	}
}

/**
 * This class is used to make loading cover image from memory instead of disk from HTML panel
 * @author Bro
 */
class XMMUserAgentContext extends SimpleUserAgentContext {
	
	byte [] imageData;
	
	XMMUserAgentContext(byte [] imageData) {
		this.imageData = imageData;
	}
	
	public HttpRequest createHttpRequest() {
		return new XMMHttpRequest(this, this.getProxy(), imageData);
	}	
}

/**
 * This class is used to make loading cover image from memory instead of disk from HTML panel
 * @author Bro
 */
class XMMHttpRequest extends net.sf.xmm.moviemanager.swing.extentions.XMMSimpleHttpRequest {
		
	URL url;
	byte [] imageData;
	
	public XMMHttpRequest(UserAgentContext context, Proxy proxy, byte [] imageData) {
		super(context, proxy);
		this.imageData = imageData;
	}
	
	public void open(String method,  java.net.URL url, boolean asyncFlag,
			 String userName, String password) throws java.io.IOException {
		super.open(method, url, asyncFlag, userName, password);
		this.url = url;
	}
	
	protected void sendSync(String content) throws IOException {
			
		if (url.toString().endsWith("$CoverSmall$")) {	
			changeState(HttpRequest.STATE_COMPLETE, 0, "", imageData);
		}
		else
			super.sendSync(content);
	}
}
