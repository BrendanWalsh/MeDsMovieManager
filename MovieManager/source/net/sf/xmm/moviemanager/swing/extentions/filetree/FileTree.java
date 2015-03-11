/**
 * @(#)FileTree.java
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

/**
 * Based on code by Matthew Robinson, Pavel Vorobiev, Swing, Second Edition
 */


package net.sf.xmm.moviemanager.swing.extentions.filetree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.swing.progressbar.ProgressBean;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.log4j.Logger;

public class FileTree extends JPanel implements ProgressBean, Runnable {

	static Logger log = Logger.getLogger(FileTree.class);

	public FileTreeEventsHandler eventHandler = new FileTreeEventsHandler();

	public static final ImageIcon ICON_DISK = getImageIcon("Disk.png");
	public static final ImageIcon ICON_DISK_INCLUDE_CONTENT = getImageIcon("Disk_include_content.png");
	public static final ImageIcon ICON_DISK_INCLUDE_ALL = getImageIcon("Disk_include_all.png");
	public static final ImageIcon ICON_DISK_EXCLUDE_ALL = getImageIcon("Disk_exclude_all.png");

	public static final ImageIcon ICON_FOLDER = getImageIcon("Folder.png");
	public static final ImageIcon ICON_FOLDER_INCLUDE_CONTENT = getImageIcon("Folder_include_content.png");
	public static final ImageIcon ICON_FOLDER_INCLUDE_ALL = getImageIcon("Folder_include_all.png");
	public static final ImageIcon ICON_FOLDER_EXCLUDE_ALL = getImageIcon("Folder_exclude_all.png");

	public static final ImageIcon ICON_MEDIA_FILE = getImageIcon("add.png");

	protected JTree fileTree;
	protected DefaultTreeModel modelTree;
	protected JTextField m_display;

	MatchingOptions matchOptions = new MatchingOptions();
	
	private Color colorMatch = new Color(152, 225, 120); // Green
	private Color colorNoMatch = new Color(233, 180, 180); // Red
	private Color colorExists = new Color(114, 116, 203); // Blue
			
	HashMap<String, ModelEntry> existingMediaFileNames = new HashMap<String, ModelEntry>();
	HashMap<String, ModelEntry> existingMediaFiles = new HashMap<String, ModelEntry>();
		
	boolean filterOutDuplicateFiles = false;
	boolean filterOutDuplicateByEntireFilePath = false;
	
	boolean skipHiddenDirectories = false;
	boolean allowAnyExtension = false;
	
	private boolean threadLock = false;
	
	int numberOfFoldersChosen = 0;
	
	private boolean cancelledJob = false;
	private boolean ready = true;	
	
	public DefaultTreeModel getTreeModel() {
		return (DefaultTreeModel) fileTree.getModel();
	}

	/**
	 * Add a new file path to the collection of already existing files.
	 * @param path
	 * @param model
	 */
	public void addExistingMediaFileInDatabase(File file, ModelEntry model) {
		existingMediaFiles.put(file.getAbsolutePath(), model);
		existingMediaFileNames.put(file.getName(), model);
	}
	
	public HashMap<String, ModelEntry> getExistingMediaFiles() {
		return existingMediaFiles;
	}
	
	public HashMap<String, ModelEntry> getExistingMediaFileNames() {
		return existingMediaFileNames;
	}
	
	
	/* 
	 * Key = filepath, obj = IconData
	 */
	private HashMap<String, IconData> changedNodes = new HashMap<String, IconData>();

	private HashMap<DefaultMutableTreeNode, DefaultMutableTreeNode> visibleNodes = new HashMap<DefaultMutableTreeNode, DefaultMutableTreeNode>();
	private HashMap<FileNode, FileNode> expandedNodes = new HashMap<FileNode, FileNode>();

	public void addVisibleNode(DefaultMutableTreeNode node) {
		visibleNodes.put(node, node);
	}
	
	public void removeVisibleNode(DefaultMutableTreeNode node) {
		visibleNodes.remove(node);
	}
	
	public void addExpandedNode(FileNode node) {
		expandedNodes.put(node, node);
	}
		
	protected JPopupMenu dir_popup;
	protected JPopupMenu file_popup;
	protected Action dir_action;
	protected Action file_action;
	protected TreePath clickedPath;
	protected JMenuItem fileItem;
	protected JMenuItem playFileItem;

	JMenuItem excludeAll, includeAll, folder, includeContent, addFiles = null;
	JMenuItem addDirAsRootDevice, removeDirAsRootDevice;
	
	ArrayList<String> validExtensions = new ArrayList<String>();
	
	// Contains all the custom root devices
	ArrayList<String> rootDevices = new ArrayList<String>();
	
	DefaultMutableTreeNode top;
	
	public void setValidExtension(ArrayList<String> validExtensions) {
		this.validExtensions = validExtensions;
		updateNodes();
	}
		
	public void addValidExtension(String validExtension) {
		validExtensions.add(validExtension);
		//Update all expanded Nodes
		updateNodes();
	}
	
	public void removeValidExtension(String validExtension) {
		validExtensions.remove(validExtension);
		updateNodes();
	}
	
	public void updateNodes() {
		
		Thread t = new Thread(new Runnable() {

			public void run() {

				Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
								
				eventHandler.fireFileTreeWorkingEvent(new FileTreeEvent(false));
				
				Iterator<FileNode> it = expandedNodes.keySet().iterator();
				ArrayList<FileNode> list = new ArrayList<FileNode>();
				
				while (it.hasNext()) {
					list.add(it.next());
				}
				
				for (int i = 0; i < list.size(); i++) {
					
					FileNode fileNode = list.get(i);
					TreePath path = new TreePath(fileNode.getNode().getPath());
			
					// if it's not collapsed
					if (!fileTree.isCollapsed(path)) {
					
						// If children were changed
						//DefaultMutableTreeNode node = fileNode.updateNodes(allowAnyExtension ? null : validExtensions);
						DefaultMutableTreeNode node = fileNode.updateNodesAndExpandedChildren(allowAnyExtension ? null : validExtensions);

						if (node != null) {
							((DefaultTreeModel) fileTree.getModel()).nodeChanged(node);
						}
					}
				}
				eventHandler.fireFileTreeReadyEvent(new FileTreeEvent(false));
			}
		});
		t.start();
	}
	
	public ArrayList<String> getValidExtension() {
		return validExtensions;
	}
	
	public void setFilterOutDuplicates(boolean duplicatesEnabled) {
		filterOutDuplicateFiles = duplicatesEnabled;
		updateCurrentCells();
	}
	
	public void setFilterOutDuplicatesByEntireFilePath(boolean entireFilePath) {
		filterOutDuplicateByEntireFilePath = entireFilePath;
		updateCurrentCells();
	}
	
	public void setSkipHiddenDirectories(boolean skip) {
		skipHiddenDirectories = skip;
	}
	
	public void setAllowAnyExtension(boolean any) {
		allowAnyExtension = any;
	}
		
	
	public FileTree(HashMap<String, ModelEntry> existingMediaFiles) {
		this();
		this.existingMediaFiles = existingMediaFiles;
	}

	
	public FileTree() {
		
		setSize(600, 1000);

		top = new DefaultMutableTreeNode(new IconData(new FileNode(new File("Computer"), this)));
		
		// If windows, all the hard drives are listed.
		// On Linux, only the home directory and root
		
		File[] roots = File.listRoots();
		
		// Getting home $HOME variable
		if (!SysUtil.isWindows()) {
			String home = System.getProperty("user.home");
			roots = new File[2];
			roots[1] = new File(home);
			roots[0] = new File("/");
		}

		for (int k = 0; k < roots.length; k++) {

			IconData tmp = new IconData(new FileNode(roots[k], this));
			tmp.setDisk(true);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(tmp);

			top.add(node);
			node.add(new DefaultMutableTreeNode(new Boolean(true)));
		}

		modelTree = new DefaultTreeModel(top);
		fileTree = new JTree(modelTree);

		fileTree.putClientProperty("JTree.lineStyle", "Angled");
		fileTree.setCellRenderer(new IconCellRenderer(colorMatch, colorNoMatch, colorExists));
		fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		
		fileTree.addTreeExpansionListener(new DirExpansionListener());
		fileTree.addTreeSelectionListener(new DirSelectionListener());
		
		fileTree.setRootVisible(false);
		fileTree.setShowsRootHandles(true); 
		fileTree.setEditable(false);
		fileTree.setLargeModel(true);
		
		setLayout(new BorderLayout());

		JScrollPane treeScroll = new JScrollPane();
		treeScroll.getViewport().add(fileTree);
		add(treeScroll, BorderLayout.CENTER);
			
		fileTree.addMouseListener(new PopupTrigger());

		createPopupMenus();
	}
	
	
	void createPopupMenus() {
		createPopupItems();
		
		// Popup for file
		file_popup = new JPopupMenu();
		file_popup.add(fileItem);
		file_popup.add(playFileItem);
				
		// Popup for directories
		JPopupMenu popup;
		popup = new JPopupMenu();
		popup.add(folder);
		popup.add(includeContent);
		popup.add(includeAll);
		popup.add(excludeAll);
		
		dir_popup = popup;
	}
	
	void createPopupItems() {
		
		// file popup
		fileItem = new JMenuItem();
		fileItem.setIcon(getImageIcon("add.png", 20));
		fileItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				eventHandler.fireAddSelectedFilesEvent(new FileTreeEvent((Object) fileItem));
			}
		});
		
		playFileItem = new JMenuItem("Play");
		playFileItem.setIcon(getImageIcon("play.png", 20));
		playFileItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				eventHandler.firePlaySelectedFilesEvent(new FileTreeEvent((Object) playFileItem));
			}
		});
		
		
		folder = new JMenuItem("Folder (no action)");
		folder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileTree.repaint();
				setFolderIcon(IconType.REGULAR_FOLDER);
			}
		});
				
		includeContent = new JMenuItem("Include only folder content");
		includeContent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileTree.repaint();
				setFolderIcon(IconType.INCLUDE_CONTENT);
			}
		});
		
		includeAll = new JMenuItem("Include folder content and subdirectories");
		includeAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				fileTree.repaint();
				setFolderIcon(IconType.INCLUDE_ALL);		
			}
		});
		
		excludeAll = new JMenuItem("Exclude folder content and subdirectories");
		excludeAll.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				fileTree.repaint();
				setFolderIcon(IconType.EXCLUDE_ALL);					
			}
		});
		
		addDirAsRootDevice = new JMenuItem("Add directory as root device to tree");
		addDirAsRootDevice.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				addSelectedFolderAsRootDevice();
			}
		});
		
		removeDirAsRootDevice = new JMenuItem("Remove as root device");
		removeDirAsRootDevice.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				removeSelectedFolderAsRootDevice();
			}
		});
	}


	public boolean addRootDevice(File file) {
		
		// Don't make duplicates
		if (rootDevices.contains(file.getAbsolutePath()))
			return false;
				
		rootDevices.add(file.getAbsolutePath());
		
		IconData tmp = new IconData(new FileNode(file, this));
		tmp.setRootElement(true);
		tmp.setFolder(true);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(tmp);

		// Update tree
		top.add(node);
		node.add(new DefaultMutableTreeNode(new Boolean(true)));
		((DefaultTreeModel) fileTree.getModel()).nodeStructureChanged(top);
		
		return true;
	}
	
	public void removeCurrentAsRootDevice(TreePath path) {
				
		Object o = path.getLastPathComponent();
				
		if (o instanceof DefaultMutableTreeNode) {				
			IconData icon = (IconData) ((DefaultMutableTreeNode)o).getUserObject();
			
			// Remove from list of root devices
			rootDevices.remove(icon.getFile().getAbsolutePath());
			
			// Update tree
			top.remove((DefaultMutableTreeNode) o);
			((DefaultTreeModel) fileTree.getModel()).nodeStructureChanged(top);
		}
	}
	

	void updateCurrentCells() {

		if (threadLock)
			return;
		
		threadLock = true;
		
		Runnable run = new Runnable() {

			public void run() {

				Set<DefaultMutableTreeNode> set = visibleNodes.keySet();
				Iterator<DefaultMutableTreeNode> it = set.iterator();

				while (it.hasNext()) {

					DefaultMutableTreeNode node = (DefaultMutableTreeNode) visibleNodes.get(it.next());
					TreeNode [] p = node.getPath();
						
					DefaultTreeModel treeModel = (DefaultTreeModel) fileTree.getModel();

					TreePath tp = new TreePath(p);
					if (fileTree.isVisible(tp)) {
						treeModel.nodeChanged(node); 
					}
				}
				threadLock = false;
			}
		};
		
		Thread t = new Thread(run);
		t.start();
	}
		


	public static ImageIcon getImageIcon(String name) {
		return getImageIcon(name, 27);
	}

	public static ImageIcon getImageIcon(String name, int scale) {

		try {
			Image i = FileUtil.getImage("/images/" + name);
			return new ImageIcon(i.getScaledInstance(scale, scale, Image.SCALE_SMOOTH));
		} catch (Exception e) {
			log.error("Eception:" + e.getMessage(), e);
		}
		return null;
	}

	public FileNode [] getSelectedFiles1() {

		int selectCount = fileTree.getSelectionCount();
		FileNode [] selectedFiles = new FileNode[selectCount];
		TreePath [] selectPaths = fileTree.getSelectionPaths();

		for (int i = 0; i < selectPaths.length; i++) {
			FileNode node = getFileNode(selectPaths[i]);
			selectedFiles[i] = node;
		}

		return selectedFiles;
	}

	public ArrayList<FileNode> getSelectedFiles() {

		ArrayList<FileNode> selectedFiles = new ArrayList<FileNode>();
		TreePath [] selectPaths = fileTree.getSelectionPaths();

		for (int i = 0; i < selectPaths.length; i++) {
			FileNode node = getFileNode(selectPaths[i]);
			selectedFiles.add(node);
		}

		return selectedFiles;
	}

	public int getChosenFoldersCount() {
		return changedNodes.size();
	}
	
	/*
	 * Searches the entire directory tree and returns the files according to the users selections.
	 */
	public ArrayList<FileNode> getFilesFromDirectoryTree(boolean includeMatchesOnly) {

		eventHandler.fireFileTreeWorkingEvent(new FileTreeEvent(true));
		ready = false;
		
		ArrayList<FileNode> files = new ArrayList<FileNode>();
		Set<String> keySet = changedNodes.keySet();
		String [] keys = keySet.toArray(new String[keySet.size()]);
		
		for (int i = 0; i < keys.length; i++) {
			IconData r = (IconData) changedNodes.get((String) keys[i]);
			addFiles(r, files, keys, false);
		}

		eventHandler.fireFileTreeReadyEvent(new FileTreeEvent(true));
		
		cancelledJob = false;
		ready = true;
		
		/*
		Arrays.sort(keys, new Comparator<String>() {

			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}	

			public boolean equals(Object obj) {
				return this == obj;
			}
		});
		*/
		
		Collections.sort(files, new Comparator<FileNode>() {

			public int compare(FileNode f1, FileNode f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});
		
		return files;
	}


	//void addFiles(Object dir, ArrayList<FileNode> files, Object [] keys, boolean includeAll) {
	void addFiles(Object dir, ArrayList<FileNode> files, Object [] keys, boolean includeAll) {
		
		if (cancelledJob) {
			files.clear();
			return;
		}
		
		IconType iconType = IconType.REGULAR_FOLDER;
		File directory;

		if (dir instanceof IconData) {
			iconType = ((IconData) dir).getIconType();
			directory = ((IconData) dir).getFile();
		}
		else {
			directory = (File) dir;
		}
		
		if (skipHiddenDirectories && directory.isHidden()) {
			return;
		}
		
		// Include all media files in the directory only
		if (iconType == IconType.INCLUDE_CONTENT) {
			addValidMediaFiles(directory, files);
		} // Include media files in directory and all subdirectories
		else if (iconType == IconType.INCLUDE_ALL || (iconType == IconType.REGULAR_FOLDER && includeAll)) {

			File [] fileList = directory.listFiles();

//			Some directories e.g. "System Volume Information" will return null
			if (fileList == null)
				return;

//			Add media files in directory
			addValidMediaFiles(directory,  files);

			// GO through all subdirectories
			for (int i = 0; i< fileList.length; i++) {

				if (fileList[i].isDirectory()) {

					//Check to see if it's already marked
					IconData subIconData = (IconData) changedNodes.get(fileList[i].getAbsolutePath());

					// It's NOT saved as marked, i.e. it's a regular directory
					if (subIconData == null) {
						addFiles(fileList[i], files, keys, true);
						
						if (cancelledJob) {
							files.clear();
							return;
						}
					}
				}
			}
		} // Will not proceed.
		else if (iconType == IconType.EXCLUDE_ALL) {

		}
	}

	// Adds all the media files with valid extension to the array list
	void addValidMediaFiles(File dir, ArrayList<FileNode> files) {
		
		File [] dirFiles = dir.listFiles();

		// Some directories e.g. "System Volume Information" will return null
		if (dirFiles == null)
			return;

		String tmp;

		for (int i = 0; i < dirFiles.length; i++) {

			if (!dirFiles[i].isFile())
				continue;

			tmp = dirFiles[i].getName();

			// Files doesn't have a file extension
			if (!allowAnyExtension && tmp.indexOf(".") == -1)
				continue;

			String ext = tmp.substring(tmp.lastIndexOf(".") +1, tmp.length());

			if (allowAnyExtension || validExtensions.contains(ext.toLowerCase())) {
				
				FileMatch res = checkFileMatch(dirFiles[i]);
				
				if (res == FileMatch.REGULAR || res ==FileMatch.MATCH) {	
					files.add(new FileNode(dirFiles[i], this));	
				}
			}
		}
	}

	public void setFolderIcon(IconType iconType) {

		TreePath[] paths = fileTree.getSelectionPaths();

		for (int i = 0; i < paths.length; i++) {

			IconData icon = (IconData) getIconData((paths[i]));

			if (icon != null) {

				if (!icon.isFolder())
					continue;

				icon.setIconType(iconType);

				File file = icon.getFile();

				if (iconType == IconType.REGULAR_FOLDER) {
					if (changedNodes.containsKey(file.getAbsolutePath()))
						changedNodes.remove(file.getAbsolutePath());
				}
				else {
					if (!changedNodes.containsKey(file.getAbsolutePath()))
						changedNodes.put(file.getAbsolutePath(), icon);
				}
			}
		}
	}

	public void addSelectedFolderAsRootDevice() {

		TreePath[] paths = fileTree.getSelectionPaths();

		if (paths != null) {
			IconData node = getIconData(paths[0]);
			
			if (addRootDevice(node.getFile()))
				eventHandler.fireRootDeviceAddedEvent(new FileTreeEvent(node.getFile()));
		}
	}

	public void removeSelectedFolderAsRootDevice() {

		TreePath[] paths = fileTree.getSelectionPaths();

		if (paths != null) {
			IconData node = getIconData(paths[0]);
			
			removeCurrentAsRootDevice(paths[0]);
			eventHandler.fireRootDeviceRemovedEvent(new FileTreeEvent(node.getFile()));
		}
	}

	IconData getIconData(TreePath path) {

		DefaultMutableTreeNode node = getTreeNode(path);
		Object obj = node.getUserObject();

		if (obj instanceof IconData)
			return (IconData) obj;
		else 
			return null;
	}

	FileNode getFileNode(TreePath path) {
		return getFileNode(getTreeNode(path));

	}

	DefaultMutableTreeNode getTreeNode(TreePath path)	{
		return (DefaultMutableTreeNode)(path.getLastPathComponent());
	}

	FileNode getFileNode(DefaultMutableTreeNode node)	{

		if (node == null)
			return null;

		Object obj = node.getUserObject();

		if (obj instanceof IconData) {
			obj = ((IconData)obj).getObject();
		}
		if (obj instanceof FileNode) {
			return (FileNode)obj;
		}
		else
			return null;
	}

	
	class PopupTrigger extends MouseAdapter	{

		public void mouseReleased(MouseEvent event)	{

			if (SwingUtilities.isRightMouseButton(event)) {
			
				final int x = event.getX();
				final int y = event.getY();

				TreePath[] selectedPaths = fileTree.getSelectionPaths();
				TreePath path = fileTree.getPathForLocation(x, y);
					
				// Sets the row selected
				if (selectedPaths == null) {
					int rowForLocation = fileTree.getRowForLocation(x, y);
					
					fileTree.setSelectionRow(rowForLocation);
					selectedPaths = fileTree.getSelectionPaths();
				}
				
				if (path != null)	{
					IconData icon = getIconData(path);
						
					if (icon.isFolder()) {
						
						if (icon.isDisk()) {
							excludeAll.setIcon(ICON_DISK_EXCLUDE_ALL);
							includeAll.setIcon(ICON_DISK_INCLUDE_ALL);
							folder.setIcon(ICON_DISK);
							includeContent.setIcon(ICON_DISK_INCLUDE_CONTENT);
						}
						else {
							excludeAll.setIcon(ICON_FOLDER_EXCLUDE_ALL);
							includeAll.setIcon(ICON_FOLDER_INCLUDE_ALL);
							folder.setIcon(ICON_FOLDER);
							includeContent.setIcon(ICON_FOLDER_INCLUDE_CONTENT);	
						}
												
						dir_popup.remove(addDirAsRootDevice);
						dir_popup.remove(removeDirAsRootDevice);
						
						if (selectedPaths != null && selectedPaths.length == 1 && !icon.isDisk()) {
							if (icon.isRootElement()) {
								dir_popup.add(removeDirAsRootDevice);
							}
							else  {
								// If it isn't already a root device (but shown in the regular directory tree)
								if (!rootDevices.contains(icon.getFile().getAbsolutePath()))
									dir_popup.add(addDirAsRootDevice);
							}
						}
						
						dir_popup.show(fileTree, x, y);
					}
					else {

						if (selectedPaths == null)
							return;

						String txt = selectedPaths.length > 1 ? "Add files to list" : "Add file to list";

						fileItem.setText(txt);
						
						file_popup.remove(playFileItem);
						
						if (selectedPaths.length == 1)
							file_popup.add(playFileItem);
							
						file_popup.show(fileTree, x, y);
					}

					if (!fileTree.getSelectionModel().isPathSelected(path))
						fileTree.setSelectionPath(path);

					clickedPath = path;
				}
			}
		}
	}
	
	

	// Make sure expansion is threaded and updating the tree model
	// only occurs within the event dispatching thread.
	class DirExpansionListener implements TreeExpansionListener {

		public void treeExpanded(TreeExpansionEvent event) {

			// Fire busy event
			eventHandler.fireFileTreeWorkingEvent(new FileTreeEvent(false));
			
			final DefaultMutableTreeNode node = getTreeNode(event.getPath());
			final FileNode fnode = getFileNode(node);

			Thread runner = new Thread()	{
				public void run() {
										
					ready = false;
					
					if (fnode != null && fnode.expand(node, allowAnyExtension ? null : validExtensions)) {
												
						addExpandedNode(fnode);
						
						Runnable runnable = new Runnable() 	{
							public void run() 	{
								modelTree.reload(node);
							}
						};
						try {
							SwingUtilities.invokeAndWait(runnable);
						} catch (Exception e) {
							log.warn("Exception:" + e.getMessage(), e);
						}
					}
					else {
						fnode.updateNodesAndExpandedChildren(allowAnyExtension ? null : validExtensions);
					}
					ready = true;
					// Fire ready event
					eventHandler.fireFileTreeReadyEvent(new FileTreeEvent(false));
				}
			};
			runner.start();
		}

		public void treeCollapsed(TreeExpansionEvent event) {
			//final DefaultMutableTreeNode node = getTreeNode(event.getPath());
			//final FileNode fnode = getFileNode(node);
			//System.out.println("collapsed:" + fnode.getPath());
		}
	}

	class DirSelectionListener 	implements TreeSelectionListener 	{
		public void valueChanged(TreeSelectionEvent event)	{
			//DefaultMutableTreeNode node = getTreeNode(event.getPath());
			//FileNode fnode = getFileNode(node);
		}
	}

	public IconData getNewIconData(IconType icon, FileNode f) {
		return new IconData(icon, f);
	}

	
	public class IconCellRenderer extends JLabel implements TreeCellRenderer {

		protected Color m_textSelectionColor;
		protected Color m_textNonSelectionColor;
		protected Color m_bkSelectionColor;
		protected Color m_bkNonSelectionColor;
		protected Color m_borderSelectionColor;

		protected boolean m_selected;
		
		Color noMatchColor;
		Color matchColor;
		Color colorExists;
		
		public IconCellRenderer(Color match, Color noMatch, Color colorExists)	{
			super();
			m_textSelectionColor = UIManager.getColor   ("Tree.selectionForeground");
			m_textNonSelectionColor = UIManager.getColor("Tree.textForeground");
			m_bkSelectionColor = UIManager.getColor     ("Tree.selectionBackground");
			m_bkNonSelectionColor = UIManager.getColor  ("Tree.textBackground");
			m_borderSelectionColor = UIManager.getColor ("Tree.selectionBorderColor");
			setOpaque(false);

			this.matchColor = match;
			this.noMatchColor = noMatch;
			this.colorExists = colorExists;
		}

		public Component getTreeCellRendererComponent(JTree tree,  Object value, boolean sel, boolean expanded, boolean leaf, 
				int row, boolean hasFocus) 	{

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

			Object obj = node.getUserObject();
			
			boolean regularFile = false;
			
			if (obj instanceof Boolean) {
				setText("Retrieving file list...");
				setIcon(null);
			}
			else if (obj instanceof IconData) {

				setText(obj.toString());
				
				IconData idata = (IconData)obj;
				regularFile = !idata.isFolder();

				if (expanded)
					setIcon(idata.getExpandedIcon());
				else
					setIcon(idata.getIcon());
			}
			else {
				setIcon(null);
				setText(obj.toString());
			}

			setFont(tree.getFont());

			TreePath path = new TreePath(node.getPath());
			
//			 Do nothing if the row isn't visible
			if (tree.isVisible(path)) {

				if (regularFile) {
					
					FileMatch match = checkFileMatch((IconData) obj);

					switch (match) {
					case REGULAR: {setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor); break;}
					case MATCH: {setBackground(matchColor); break;}	
					case NO_MATCH: {setBackground(noMatchColor); break;}	
					case EXISTS_IN_DB: {setBackground(colorExists); break;}	
					}

				}
				else {
					setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor);
				}
				setForeground(sel ? m_textSelectionColor : m_textNonSelectionColor);
			}
			m_selected = sel;

			return this;
		}
 

		public void paintComponent(Graphics g) 	{
			Color bColor = getBackground();
			Icon icon = getIcon();

			g.setColor(bColor);
			int offset = 0;

			if(icon != null && getText() != null) 
				offset = (icon.getIconWidth() + getIconTextGap());

			g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);

			if (m_selected) 		{
				g.setColor(m_borderSelectionColor);
				g.drawRect(offset, 0, getWidth()-1-offset, getHeight()-1);
			}

			super.paintComponent(g);
		}

	}

	public enum FileMatch {ERROR, REGULAR, MATCH, NO_MATCH, EXISTS_IN_DB};

	
	public FileMatch checkFileMatch(Object fileObj) {

		File file = null;
		
		if (fileObj instanceof FileNode) {
			// SHOULD NEVER BE FILENODE
			file = ((FileNode) fileObj).getFile();
		} else if (fileObj instanceof File) {
			file = ((File) fileObj);
		} else if (fileObj instanceof IconData) {
			file = ((IconData) fileObj).getFile();
		} else {
			return FileMatch.ERROR;
		}
		
		if (filterOutDuplicateFiles) {
			if (existingMediaFiles != null && !existingMediaFiles.isEmpty()) {
				
				if (filterOutDuplicateByEntireFilePath) {
					if (existingMediaFiles.containsKey(file.getAbsolutePath())) {
						return FileMatch.EXISTS_IN_DB;
					}
				}
				else {
					if (existingMediaFileNames.containsKey(file.getName())) {
						return FileMatch.EXISTS_IN_DB;
					}					
				}
			}
		}
				
		
		FileMatch regexResult = FileMatch.REGULAR;

		// Check regex
		if (matchOptions.regexPattern != null) {
			
			Matcher m = matchOptions.regexPattern.matcher(file.getName());

			boolean match = m.find();
			
			if (matchOptions.regexNegate)
				match = !match;
				
			regexResult = match ? FileMatch.MATCH : FileMatch.NO_MATCH;
		}
				
		return regexResult;
	}
	
	public enum IconType {REGULAR_FOLDER, INCLUDE_CONTENT, INCLUDE_ALL, EXCLUDE_ALL, REGULAR_FILE};
		
	public class IconData {
		
		public Icon m_icon;
		public Icon m_expandedIcon;

		protected FileNode m_data;

		private IconType iconType = IconType.REGULAR_FOLDER;
		private boolean isDisk = false;
		private boolean isRootElement = false;
		private boolean isFolder = false;

		public IconData(FileNode data)	{
			m_data = data;
		}
		
		public IconData(IconType icon, FileNode data)	{
			iconType = icon;
			m_data = data;
		}

		public void setIconType(IconType t) {
			iconType = t;
		}

		public boolean isFolder() {
			return isFolder;
		}

		public void setFolder(boolean isFolder) {
			this.isFolder = isFolder;
		}

		public boolean isDisk() {
			return isDisk;
		}

		public void setDisk(boolean isDisk) {
			this.isDisk = isDisk;
			isFolder = isDisk;
			isRootElement = isDisk;
		}
		
		public boolean isRootElement() {
			return isRootElement;
		}

		public void setRootElement(boolean isRootElement) {
			this.isRootElement = isRootElement;
		}

		public Icon getIcon() {
			Icon tmpIcon = null;
			
			switch (iconType) {
			
			case REGULAR_FOLDER : 
				tmpIcon = isDisk ? FileTree.ICON_DISK : FileTree.ICON_FOLDER;
				break;
			case INCLUDE_CONTENT :
				tmpIcon = isDisk ? FileTree.ICON_DISK_INCLUDE_CONTENT : FileTree.ICON_FOLDER_INCLUDE_CONTENT;
				break;
			case INCLUDE_ALL :
				tmpIcon = isDisk ? FileTree.ICON_DISK_INCLUDE_ALL : FileTree.ICON_FOLDER_INCLUDE_ALL;
				break;
			case EXCLUDE_ALL :
				tmpIcon = isDisk ? FileTree.ICON_DISK_EXCLUDE_ALL : FileTree.ICON_FOLDER_EXCLUDE_ALL;
				break;
			case REGULAR_FILE :
				tmpIcon = FileTree.ICON_MEDIA_FILE;
			}

			return tmpIcon;
		}

		public IconType getIconType() {
			return iconType;
		}

		public Icon getExpandedIcon() 	{ 
			return getIcon();
		}

		public File getFile() {
			return ((FileNode) m_data).getFile();
		}
		
		public FileNode getFileNode() {
			return m_data;
		}

		public Object getObject() 	{ 
			return m_data;
		}

		public String toString() 	{ 
			
			if (isRootElement)
				return m_data.getPath();
			else
				return m_data.toString();
		}
		
		String getFilePath() { 
			return ((FileNode) m_data).getFile().getAbsolutePath();
		}		
	}

		
	public void setRegexPattern(String expression, boolean caseSensitive) {
		
		try {
			
			if (expression != null) {
				Pattern compiledRegex;
				
				if (caseSensitive)
					compiledRegex = Pattern.compile(expression);
				else
					compiledRegex = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
				
				matchOptions.regexPattern = compiledRegex;
			}
			else
				matchOptions.regexPattern = null;
			
			updateCurrentCells();
		} catch (Exception ex) {
			log.warn("Invalid regex expression:" + ex.getMessage());
		}
	}
	
	public void setRegexNegate(boolean regexNegate) {
		matchOptions.regexNegate = regexNegate;
		updateCurrentCells();
	}
			
	private class MatchingOptions {
		
		Pattern regexPattern = null;
		boolean regexNegate = false;			
	}

	public synchronized void cancel() {
		cancelledJob = true;
	}

	public boolean getCancelled() {
		return cancelledJob;
	}

	// Not used, but required by ProgressBean
	public double getStatus() {
		return 0;
	}

	// Not used, but required by ProgressBean
	public void start() {
	}

	// Not used, but required by ProgressBean
	public void run() {
	}
	
	public boolean isReady() {
		return ready;
	}
}
