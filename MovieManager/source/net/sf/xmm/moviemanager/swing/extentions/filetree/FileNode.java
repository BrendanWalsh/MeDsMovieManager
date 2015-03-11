package net.sf.xmm.moviemanager.swing.extentions.filetree;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.sf.xmm.moviemanager.util.FileUtil;

import org.apache.log4j.Logger;

public class FileNode implements Comparable<FileNode> {
	
	Logger log = Logger.getLogger(getClass());
	
	protected File m_file;
	FileTree fileTree;
	
	//ArrayList childrenFiles = new ArrayList();
	FileNode [] currentChildrenFiles = null;
	
	DefaultMutableTreeNode treeNode = null;
	
	public FileNode(File file, FileTree f )	{
		m_file = file;
		fileTree = f;
	}

	public DefaultMutableTreeNode getNode() {
		return treeNode;
	}
	
	public File getFile() { 
		return m_file;
	}

	public boolean isDirectory() {
		return m_file.isDirectory();
	}
	
	public boolean isFile() {
		return m_file.isFile();
	}
	
	public String getName() {
		return m_file.getName();
	}
	
	public String getPath() {
		return m_file.getPath();
	}
	
	// If file name is empty, the path is returned
	public String toString() {
		return m_file.getName().length() > 0 ? m_file.getName() : m_file.getPath();
	}

	public int compareTo(FileNode nodeCompare) {
		
		if (isDirectory() != nodeCompare.isDirectory())
			return isDirectory() && !nodeCompare.isDirectory() ? -1 : 1;
		
		return m_file.getName().compareToIgnoreCase(nodeCompare.m_file.getName());
	}
		
	
	public boolean expand(DefaultMutableTreeNode parent, ArrayList<String> validExtensions)	{
		DefaultMutableTreeNode flag = (DefaultMutableTreeNode)parent.getFirstChild();

		treeNode = parent;
		
		if (flag == null)    // No flag
			return false;

		Object obj = flag.getUserObject();

		if (!(obj instanceof Boolean))
			return false;      // Already expanded

		parent.removeAllChildren();  // Remove Flag
	
		currentChildrenFiles = getValidChildren(validExtensions, true);
		FileTree.IconData idata;
		
		if (currentChildrenFiles == null)
			return true;
		
		for (int i = 0; i < currentChildrenFiles.length; i++)	{

			FileNode nd = (FileNode) currentChildrenFiles[i];
				
			if (nd.getFile().isDirectory()) {
				idata = fileTree.getNewIconData(FileTree.IconType.REGULAR_FOLDER, nd);
				idata.setFolder(true);
			}
			else 
				idata = fileTree.getNewIconData(FileTree.IconType.REGULAR_FILE, nd);
			
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(idata);
				
			parent.add(node);
			
			if (nd.isDirectory()) {
				
				if (nd.hasFiles())
					node.add(new DefaultMutableTreeNode(new Boolean(true)));
			}
			else {
				fileTree.addVisibleNode(node);
			}
		}

		return true;
	}

	/**
	 * Goes through all children and sub-children and updates all the expanded nodes
	 * @param fnode
	 */
	public DefaultMutableTreeNode updateNodesAndExpandedChildren(ArrayList<String> validExtensions) {
				
		// Not yet expanded
		if (treeNode == null)
			return null;
		
		DefaultMutableTreeNode node = updateNodes(validExtensions);
				
		for (int i = 0; i < treeNode.getChildCount(); i++) {
			FileNode fNode = ((FileTree.IconData) ((DefaultMutableTreeNode) treeNode.getChildAt(i)).getUserObject()).getFileNode();
			
			if (fNode.isDirectory()) {
				TreePath path = new TreePath(getNode().getPath());
				
				// if it's not collapsed
				if (!fileTree.fileTree.isCollapsed(path)) {
					fNode.updateNodesAndExpandedChildren(validExtensions);
				}
			}
			else {
				// Only files are left
				break;
			}
		}
		
		return node;
	}
	
	public DefaultMutableTreeNode updateNodes(ArrayList<String> validExtensions) {
		
		boolean changed = false;
		
		currentChildrenFiles = getValidChildren(validExtensions, false);
		
		// Find the index of the first file, to skip directories
		int fileIndex = -1;
			
		for (int i = 0; i < treeNode.getChildCount(); i++) {
			FileNode fNode = ((FileTree.IconData) ((DefaultMutableTreeNode) treeNode.getChildAt(i)).getUserObject()).getFileNode();
			
			if (fNode.isFile()) {
				fileIndex = i;
				break;
			}
		}
		
		// Removing all regular files
		if (currentChildrenFiles.length == 0 && fileIndex != -1) {
						
			while (treeNode.getChildCount() != fileIndex) {
				fileTree.getTreeModel().removeNodeFromParent((DefaultMutableTreeNode) treeNode.getChildAt(fileIndex));
			}
			return treeNode;
		}
			
		int regularFileCount = treeNode.getChildCount() - fileIndex;
		int i = 0;
		
		for (; i < currentChildrenFiles.length; i++) {
			
			FileNode f = currentChildrenFiles[i];
			
			if (fileIndex == -1 || (treeNode.getChildCount() - fileIndex) <= i) {
				FileTree.IconData idata = fileTree.getNewIconData(FileTree.IconType.REGULAR_FILE, f);
				
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(idata);
				fileTree.addVisibleNode(newNode);
				fileTree.getTreeModel().insertNodeInto(newNode, treeNode, treeNode.getChildCount());
				
				changed = true;
				continue;
			}
			
			
			FileNode fNode = ((FileTree.IconData) ((DefaultMutableTreeNode) treeNode.getChildAt(i + fileIndex)).getUserObject()).getFileNode();
			
			int cmp = f.compareTo(fNode);
			
//			 Node missing, will add
			if (cmp < 0) {
				FileTree.IconData idata = fileTree.getNewIconData(FileTree.IconType.REGULAR_FILE, f);
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(idata);
				
				fileTree.getTreeModel().insertNodeInto(newNode, treeNode, i + fileIndex);
				fileTree.addVisibleNode(newNode);
				changed = true;
			}
			else if (cmp > 0) { 				
				fileTree.removeVisibleNode(fNode.getNode());
				fileTree.getTreeModel().removeNodeFromParent((DefaultMutableTreeNode) treeNode.getChildAt(i + fileIndex));
				
				i--;
				changed = true;
			}
		}
				
		if (fileIndex != -1) {
			if (regularFileCount > currentChildrenFiles.length) {
				// If the node had more children than the new list, some nodes may be left at the end. Rmoved here.
				while (treeNode.getChildCount() > (currentChildrenFiles.length + fileIndex)) 
					fileTree.getTreeModel().removeNodeFromParent((DefaultMutableTreeNode) treeNode.getChildAt(fileIndex + currentChildrenFiles.length));
			}
		}
		
		return changed ? treeNode : null;
	}

	/*
	 * If validExtensions is null, any extension is allowed
	 */
	FileNode [] getValidChildren(ArrayList<String> validExtensions, boolean includeDirectories) {
		File[] files = listFiles();

		if (files == null)
			return new FileNode[0];

		ArrayList<FileNode> validFiles = new ArrayList<FileNode>();

		for (int i = 0; i < files.length; i++)	{
			
			if ((files[i].isFile() && 
					!(validExtensions != null && !validExtensions.contains(FileUtil.getExtension(files[i].getName()))))
					|| (includeDirectories && files[i].isDirectory())) {
				validFiles.add(new FileNode(files[i], fileTree));
			}
		}

		FileNode [] allFiles = new FileNode[validFiles.size()];
		allFiles = (FileNode []) validFiles.toArray(allFiles);
		Arrays.sort(allFiles);

		return allFiles;
	}

	
	public boolean hasFiles() {
		File[] files = listFiles();
		if (files == null)
			return false;

		if (files.length > 0)
			return true;

		for (int k=0; k<files.length; k++)
			{
				if (files[k].isDirectory())
					return true;
			}
		return false;
	}
  

	protected File[] listFiles()	{

		if (!m_file.isDirectory())
			return null;
		
		try {
			return m_file.listFiles();
		}
		catch (Exception e)	{
			log.error("Exception:" + e.getMessage(), e);
		}
		return null;
	}

}

