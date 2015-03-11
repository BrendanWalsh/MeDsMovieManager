/**
 * @(#)ExtendedJTree.java 1.0 07.10.05 (dd.mm.yy)
 *
 * Copyright (2003) BRo3
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

package net.sf.xmm.moviemanager.swing.extentions;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import net.sf.xmm.moviemanager.LookAndFeelManager;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.LookAndFeelManager.LookAndFeelType;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.events.NewLookAndFeelLoadedEvent;
import net.sf.xmm.moviemanager.util.events.NewLookAndFeelLoadedEventListener;

import org.apache.log4j.Logger;

/**
 * This class is a type of JTree that uses Drag and Drop, and performs auto-scrolling when dragging images beyond
 * the bounds of the JTrees JScrollPane.
 */
public class ExtendedJTree extends JTree implements Autoscroll /*, DragGestureListener, DragSourceListener*/ {  

	Logger log = Logger.getLogger(getClass());

	private static final int AUTOSCROLL_MARGIN = 25;
	private Insets autoscrollInsets = new Insets( 0, 0, 0, 0 );    // AutoScroll methods.
	private DefaultTreeModel dtModel;
	private DefaultMutableTreeNode root;
	public static final boolean displayFilesInTree = false;
	
	ExtendedJTree extendedTree = this;

	JTree getTree() {
		return this;
	}
	
	public void resetUI() {
		 setUI(new ExtendedJTreeUI(this));
	}
	
   /**
    * Constructor.
    * @param root The Root node for the JTree.
    */
   public ExtendedJTree() {
	   super();
	   
	   setDragEnabled(false);
	   setUI(new ExtendedJTreeUI(this));

	   /*
	   LookAndFeelManager.newLookAndFeelLoadedHandler.addNewLookAndFeelLoadedEventListener(
			   new NewLookAndFeelLoadedEventListener() {
				   public void newLookAndFeelLoaded(NewLookAndFeelLoadedEvent evt) {
					   System.out.println("newLookAndFeelLoaded");
					   setUI(new ExtendedJTreeUI(getTree()));
					   ((ExtendedJTreeUI) getUI()).installDefaults();
				   }
			   });
   */
   }	

   /**
    * Constructor.
    * @param root The Root node for the JTree.
    */
   public ExtendedJTree(DefaultMutableTreeNode root) {

	   this();

	   this.root = root;

	   dtModel = new DefaultTreeModel(root);
	   setModel(dtModel);
   }
   


   ////////////////////
   // AUTO SCROLLING //
   ////////////////////
   /**
    * Handles the scrolling of the JTree.
	 * @param location The location of the mouse.
	 */
	public void autoscroll(Point location)
	{
		int top = 0, left = 0, bottom = 0, right = 0;
		Dimension size = getSize();
		Rectangle rect = getVisibleRect();
		int bottomEdge = rect.y + rect.height;
		int rightEdge = rect.x + rect.width;
		if( location.y - rect.y <= AUTOSCROLL_MARGIN && rect.y > 0 ) top = AUTOSCROLL_MARGIN;
		if( location.x - rect.x <= AUTOSCROLL_MARGIN && rect.x > 0 ) left = AUTOSCROLL_MARGIN;
		if( bottomEdge - location.y <= AUTOSCROLL_MARGIN && bottomEdge < size.height ) bottom = AUTOSCROLL_MARGIN;
		if( rightEdge - location.x <= AUTOSCROLL_MARGIN && rightEdge < size.width ) right = AUTOSCROLL_MARGIN;
		rect.x += right - left;
		rect.y += bottom - top;
		scrollRectToVisible( rect );
	}

	/**
	 * Gets the insets used for the autoscroll.
	 * @return The insets.
	 */
	public Insets getAutoscrollInsets()
	{
		Dimension size = getSize();
		Rectangle rect = getVisibleRect();
		autoscrollInsets.top = rect.y + AUTOSCROLL_MARGIN;
		autoscrollInsets.left = rect.x + AUTOSCROLL_MARGIN;
		autoscrollInsets.bottom = size.height - ( rect.y + rect.height ) + AUTOSCROLL_MARGIN;
		autoscrollInsets.right = size.width - ( rect.x + rect.width ) + AUTOSCROLL_MARGIN;
		return autoscrollInsets;
	}

	
	////////////////////
	// MISC FUNCTIONS //
	////////////////////
	/**
	 * Returns the name of the current selected directory.
	 * @return The name of the current selected directory.
	 */
	public DefaultMutableTreeNode [] getSelectedNodes() {

		TreePath[] tp = getSelectionPaths();

		if( tp != null) {

			DefaultMutableTreeNode [] nodes = new DefaultMutableTreeNode[tp.length];

			for (int i = 0; i < tp.length; i++)
				nodes[i] = (DefaultMutableTreeNode) tp[i].getLastPathComponent();

			return nodes;
		}
		return null;
	}

	
	////////////////////////////////////////////////////////////////////////////
	// Add/Remove Nodes
	////////////////////////////////////////////////////////////////////////////
	/** Remove all nodes except the root node. */
	public void clear()
	{
		root.removeAllChildren();
		dtModel.reload();
	}

	/** Remove the currently selected node. */
	public void removeCurrentNode()
	{
		TreePath currentSelection = getSelectionPath();
		if( currentSelection != null )
		{
			DefaultMutableTreeNode currentNode = ( DefaultMutableTreeNode )
			( currentSelection.getLastPathComponent() );
			MutableTreeNode parent = ( MutableTreeNode ) ( currentNode.getParent() );
			if( parent != null )
			{
				dtModel.removeNodeFromParent( currentNode );
				return;
			}
		}
	}

	/**
	 * Add child to the currently selected node.
	 * @param child The child node.
	 */
	public void addObject( Object child )
	{
		DefaultMutableTreeNode parentNode = null;
		TreePath parentPath = getSelectionPath();

		if( parentPath == null )
		{
			parentNode = root;
		}
		else
		{
			parentNode = ( DefaultMutableTreeNode ) parentPath.getLastPathComponent();
		}
		addObject( parentNode, child, false);//true );
	}

	/**
	 * Add child to a parent node.
	 * @param child The child node.
	 * @param parent The parent node.
	 */
	public void addObject(DefaultMutableTreeNode parent, Object child)
	{
		addObject( parent, child, false );
	}

	/**
	 * Add child to a parent node.
	 * @param child The child node.
	 * @param parent The parent node.
	 * @param shouldBeVisible TRUE to expand the parent folder, FALSE to collapse the parent folder.
	 */
	public void addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible)
	{
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode( child );

		if( parent == null )
		{
			parent = root;
		}
		dtModel.insertNodeInto( childNode, parent, parent.getChildCount() );

		// Make sure the user can see the lovely new node.
		if( shouldBeVisible )
		{
			scrollPathToVisible(new TreePath( childNode.getPath()));
		}
		
	}


	/* Resets the value of the X- coordinate */
	public void scrollPathToVisible2(final TreePath path, final int xCoordinate) {

		if(path != null) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					makeVisible(path);
					Rectangle bounds = getPathBounds(path);

					if(bounds != null) {

						if (xCoordinate != -1)
							bounds.setRect(xCoordinate, bounds.getY(), bounds.getWidth(), bounds.getHeight());

						scrollRectToVisible(bounds);

						if (accessibleContext != null) {
							((AccessibleJTree)accessibleContext).fireVisibleDataPropertyChange();
						}
					}
				}
			});
		}
	}
}

