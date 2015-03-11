package net.sf.xmm.moviemanager.swing.extentions;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;

public class ExtendedJTreeUI extends BasicTreeUI {

	JTree tree;
	private RowSelectionListener sf = new RowSelectionListener();
	private Color backgroundSelectionColor = null;

	ExtendedJTreeUI(JTree tree) {
		this.tree = tree;
	}
	
	@Override
	protected void installDefaults() {
		super.installDefaults();
		backgroundSelectionColor = UIManager.getColor("Tree.selectionBackground");
	}
	
	@Override
	protected void installListeners() {
		super.installListeners();
		tree.addMouseListener(sf);
	}

	@Override
	protected void uninstallListeners() {
		tree.removeMouseListener(sf);
		super.uninstallListeners();
	}
	
	@Override
	protected void paintRow(Graphics g, Rectangle clipBounds,
			Insets insets, Rectangle bounds, TreePath path,
			int row, boolean isExpanded,
			boolean hasBeenExpanded, boolean isLeaf) {
		// Don't paint the renderer if editing this row.
		if (editingComponent != null && editingRow == row)
			return;

		if (tree.isRowSelected(row)) {
			int h = tree.getRowHeight();
			g.setColor(backgroundSelectionColor);
			g.fillRect(clipBounds.x, h*row, clipBounds.width, h);
		}

		super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded,
				hasBeenExpanded, isLeaf);
	}
	
	private class RowSelectionListener extends MouseAdapter {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
						
			if (!tree.isEnabled())
				return;
			
			TreePath closestPath = tree.getClosestPathForLocation(e.getX(), e.getY());
			
			if (closestPath == null)
				return;
			
			Rectangle bounds = tree.getPathBounds(closestPath);
			// Process events outside the immediate bounds - fix for defect
			// 19 on substance-netbeans. This properly handles Ctrl and Shift
			// selections on trees.
			if ((e.getY() >= bounds.y)
					&& (e.getY() < (bounds.y + bounds.height))
					&& ((e.getX() < bounds.x) || (e.getX() > (bounds.x + bounds.width)))) {
				
				if (getModel() == null) {
					installUI(tree);
					setModel(tree.getModel());
				}
				
				// fix - don't select a node if the click was on the
				// expand control
				if (isLocationInExpandControl(closestPath, e.getX(), e.getY())) {
					return;
				}
				
				selectPathForEvent(closestPath, e);
			}
		}
	}
	
}
