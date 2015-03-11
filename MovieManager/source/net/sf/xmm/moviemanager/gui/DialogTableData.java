/**
 * @(#)DialogImportTable.java 29.01.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.util.table.ColumnGroup;
import net.sf.xmm.moviemanager.swing.util.table.GroupableTableColumnModel;
import net.sf.xmm.moviemanager.swing.util.table.GroupableTableHeader;
import net.sf.xmm.moviemanager.util.StringUtil;

import org.apache.log4j.Logger;

public abstract class DialogTableData extends JDialog {

	Logger log = Logger.getLogger(getClass());

	final JDialog dialogImportTable = this;
	
	public boolean cancelled = false;
	
	
	JButton buttonDone = new JButton();
	
	JTable table;
	File file;    
	int currentColumn = 0;
	JTableHeader tableHeader;

	public int titleColumnIndex = -1;
	
	ArrayList<String> generalInfoFieldNames = null;
	ArrayList<String> additionalInfoFieldNames = null;
	ArrayList<String> extraInfoFieldNames = null;

	JPopupMenu headerPopupMenu;

	JPopupMenu tablePopupMenu = null;


	public DialogTableData(JFrame parent, ModelImportExportSettings settings) {

		super(parent);

		/* Close dialog... */
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				cancelled = true;
				dispose();
			}
		});

		/* Enables dispose when pushing escape */
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
				dispose();
			}
		};

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", escapeAction);

		setTitle("Import Movies");
		setModal(true);

		setTitle("Import");

		generalInfoFieldNames = MovieManager.getIt().getDatabase().getGeneralInfoMovieFieldNames();
		additionalInfoFieldNames = MovieManager.getIt().getDatabase().getAdditionalInfoFieldNames();
		extraInfoFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);
		
		// Available for MySQL only
		generalInfoFieldNames.remove("CoverData");
		
		headerPopupMenu = makeHeaderPopupMenu();
		
		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());        

		// Setup table
		table = new JTable( /*dm, new GroupableTableColumnModel()*/);
		table.setColumnModel(new GroupableTableColumnModel());
		table.setTableHeader(new GroupableTableHeader((GroupableTableColumnModel) table.getColumnModel()));
	
		
		try {
			
			table.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent event) {
					setTablePopupVisible(event.getX(), event.getY(), event);
				}
			});

			tableHeader = table.getTableHeader();
			tableHeader.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent event) {
					JTableHeader jth = (JTableHeader) event.getSource();
					int col = jth.columnAtPoint(event.getPoint());

					if (!jth.getCursor().equals(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR))) {
						currentColumn = col;
						setHeaderPopupVisible(event.getX(), event.getY(), event, col);
					}
				}
			});

			JPanel tablePanel = new JPanel();

			tablePanel.setLayout(new BorderLayout());
			tablePanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
			tablePanel.add(table, BorderLayout.CENTER);

			tablePanel.add(table);
			JScrollPane scroll = new JScrollPane(tablePanel);

			content.add(scroll);

			JPanel panelButtons = new JPanel();

			JButton buttonCancel = new JButton("Cancel");
			buttonCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelled = true;
					dispose();
				}
			});
			
			panelButtons.add(buttonDone);
			panelButtons.add(buttonCancel);
			
			getContentPane().add(content, BorderLayout.CENTER);
			getContentPane().add(panelButtons, BorderLayout.SOUTH);

			pack();
			setSize(MovieManager.getDialog().getMainSize());

			setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
					(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);

		}
		catch (Exception e) {
			log.error("Exception: ", e);
		}
	}

	public void setHeaderPopupVisible(final int x, final int y, final MouseEvent event, int column) {
		
		if (!SwingUtilities.isRightMouseButton(event))
			return;

		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				headerPopupMenu.show((JTableHeader) event.getSource(), x, y);
			}
		});
	}

	public JPopupMenu makeHeaderPopupMenu() {

		int columnIndex = 0;
		JPopupMenu popupMenu = new JPopupMenu();

		JCheckBoxMenuItem temp;

		JMenu movieInfoMenu = new JMenu("Movie Info");
		popupMenu.add(movieInfoMenu);
		
		for (int i = 0; i < generalInfoFieldNames.size(); i ++) {
			temp = new TableStringCheckBoxMenuItem(new FieldModel("General Info", (String) generalInfoFieldNames.get(i), columnIndex));
			temp.addMouseListener(headerPopupListener);
			movieInfoMenu.add(temp);
			columnIndex++;
		}

		popupMenu.add(new JPopupMenu.Separator());

		JMenu fileInfo = new JMenu("File Info");
		popupMenu.add(fileInfo);

		for (int i = 0; i < additionalInfoFieldNames.size(); i ++) {
			temp = new TableStringCheckBoxMenuItem(new FieldModel("Additional Info", (String) additionalInfoFieldNames.get(i), columnIndex));
			temp.addMouseListener(headerPopupListener);
			fileInfo.add(temp);
			columnIndex++;
		}

		popupMenu.add(new JPopupMenu.Separator());

		JMenu extraInfo = new JMenu("Extra Fields");
		
		for (int i = 0; i < extraInfoFieldNames.size(); i ++) {
			temp = new TableStringCheckBoxMenuItem(new FieldModel("Extra Info", (String) extraInfoFieldNames.get(i), columnIndex));
			temp.addMouseListener(headerPopupListener);
			extraInfo.add(temp);
			columnIndex++;
		}

		if (extraInfoFieldNames.size() > 0)
			popupMenu.add(extraInfo);
		
		return popupMenu;
	}
	

	public void setTablePopupVisible(final int x, final int y, final MouseEvent event) {
		
		if (!SwingUtilities.isRightMouseButton(event))
			return;

		int row = table.rowAtPoint(new Point(x, y));

		if (!table.isRowSelected(row))
			return;

		if (tablePopupMenu == null)
			tablePopupMenu = makeTablePopupMenu();
		
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				tablePopupMenu.show((JTable) event.getSource(), x, y);
			}
		});
	}



	public JPopupMenu makeTablePopupMenu() {

		JPopupMenu popupMenu = new JPopupMenu();

		JMenuItem deleteRow = new JMenuItem("Delete row");
		deleteRow.addMouseListener(tablePopupListener);
		deleteRow.setActionCommand("Delete");
		popupMenu.add(deleteRow);

		JMenuItem originalColumnTitles = new JMenuItem("Original Column Titles");
		originalColumnTitles.addMouseListener(tablePopupListener);
		originalColumnTitles.setActionCommand("OriginalColumnTitles");
		popupMenu.add(originalColumnTitles);

		return popupMenu;
	}


	

	public ModelImportExportSettings getSettings() {
		return new ModelImportExportSettings();
	}


	public class FieldModel {

		String table;
		String field;
		String value;
		
		TableColumn assignedTableColumn = null;
		int assignedTableColumnIndex = -1;
		
		// The initial index this column belongs to in the data matrix
		int columnIndex;
		
		TableStringCheckBoxMenuItem checkBox = null;

		FieldModel(String table, String field, int columnIndex) {
			this.table = table;
			this.field = field;
			this.columnIndex = columnIndex;
		}

		public String getTable() {
			return table;
		}

		public String getField() {
			return field;
		}

		public void setValue(String value) {
			this.value = value == null ? "" : value;
		}

		public String getValue() {
			return value;
		}

		public int getInitialColumnindex() {
			return columnIndex;
		}
				
		public void validateValue() {
			
			if (table.equals("General Info")) {
				
				// Should be an integer
				if (!value.equals("") && field.equalsIgnoreCase("Imdb")) {
					try {
						Integer.parseInt(value);
					} catch (NumberFormatException e) {
						log.warn("Value:" + value + " is not a valid imdb id.\nValue ignored");
						value = "";
					}
					
				}
				// Rating should be a double
				else if (field.equalsIgnoreCase("Rating")) {
					try {
						Double.parseDouble(value);
					} catch (NumberFormatException e) {
						log.warn("Value:" + value + " is not a valid rating.\nValue ignored");
						value = "";
					}					
				}
			}

			else if (table.equals("Additional Info")) {

				// Should be a double
				if (!value.equals("") && (field.equalsIgnoreCase("Video Rate") || field.equalsIgnoreCase("Audio Rate"))) {
					try {
						Double.parseDouble(value);
					} catch (NumberFormatException e) {
						log.warn("Value:" + value + " is not a integer as it should be.");
						value = StringUtil.cleanInt(value, 0);
						log.warn("Value trimmed to:" + value);
					}	
				}
				// Should be an integer
				else if (field.equalsIgnoreCase("Duration") || 
						field.replaceFirst("_", " ").equalsIgnoreCase("File Size") ||
						field.equalsIgnoreCase("CDs") || 
						field.replaceFirst("_", " ").equalsIgnoreCase("CD Cases") || 
						field.replaceFirst("_", " ").equalsIgnoreCase("File Count") ||
						field.replaceFirst("_", " ").equalsIgnoreCase("Video Bit Rate") ||
						field.replaceFirst("_", " ").equalsIgnoreCase("Audio Bit Rate")) {

					String tmpVal = value;
					
					if (tmpVal.equals(""))
						tmpVal = "0";
					
					try {
						Integer.parseInt(tmpVal);
						
						// The fields Duration, File Size, CDs, CD Cases and File Count require a valid integer
						if (!field.replaceFirst("_", " ").equalsIgnoreCase("Video Bit Rate") &&
								!field.replaceFirst("_", " ").equalsIgnoreCase("Audio Bit Rate"))
							value = tmpVal;
						
					} catch (NumberFormatException e) {
						log.warn("Value:" + value + " is not a integer as it should be.");
						value = StringUtil.cleanInt(value, 0);
						log.warn("Value trimmed to:" + value);
					}	
				}

				// Duration is most probably in Minutes, converts to seconds
				if (field.equalsIgnoreCase("Duration")) {

					try {
						int intVal = Integer.parseInt(value);	
						intVal *= 60;
						value = String.valueOf(intVal);
					} catch (NumberFormatException e) {
						log.warn("Value:" + value + " is not a integer as it should be.");
						value = StringUtil.cleanInt(value, 0);
						log.warn("Value trimmed to:" + value);
					}	
				}
			}
		}


		public TableStringCheckBoxMenuItem getCheckBoxMenuItem() {
			return checkBox;
		}


		public String toString() {
			return field;
		}
	}

	class TableStringCheckBoxMenuItem extends JCheckBoxMenuItem {

		FieldModel fieldModel;

		TableStringCheckBoxMenuItem(FieldModel tableString) {
			super(tableString.toString());
			this.fieldModel = tableString;
			tableString.checkBox = this;
		}

		public FieldModel getFieldModel() {
			return fieldModel;
		}
				
		public String toString() {
			return fieldModel.toString();
		}
	}
	
	// Overridden by subclass if needed
	public void updateColumnData(int oldModelIndex, int currentColumn, int initialColumnindex) {
		
	}

	MouseAdapter headerPopupListener = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
			
			GroupableTableColumnModel columnModel = (GroupableTableColumnModel) tableHeader.getColumnModel();
						
			TableColumn newColumn = columnModel.getColumn(currentColumn);
			TableStringCheckBoxMenuItem src = (TableStringCheckBoxMenuItem) e.getSource();
	
			FieldModel oldColumnField = null;
			int oldModelIndex = -1;
			
			FieldModel sourceFieldModel = src.getFieldModel();
			
			
			// Column is already assigned a field
			if (newColumn.getHeaderValue() != null) {
				
				if (newColumn.getHeaderValue() instanceof FieldModel) {
					oldColumnField = (FieldModel) newColumn.getHeaderValue();
					
					if (!oldColumnField.equals(sourceFieldModel)) {
						oldColumnField.assignedTableColumn.setHeaderValue(null);
						oldColumnField.assignedTableColumn = null;
						
						oldModelIndex = oldColumnField.assignedTableColumnIndex;
						oldColumnField.assignedTableColumnIndex = -1;
						oldColumnField.checkBox.setState(false);
					}
				}
			}
			
			
			/* Already assigned on a different column */
			if (src.getState()) {
				
				// It's already assigned on this column. Therefore remove/deselect
				if (newColumn.equals(sourceFieldModel.assignedTableColumn)) {
					newColumn.setHeaderValue(null);
					src.setState(true);
					oldModelIndex = currentColumn;
					currentColumn = -1;
				}
				else {
					
					// Remove header from old column
					if (sourceFieldModel.assignedTableColumn != null) {
	
						sourceFieldModel.assignedTableColumn.setHeaderValue(null);
						oldModelIndex = sourceFieldModel.assignedTableColumnIndex;
						
						// Must set to false, because when it was clicked it was already armed, it will be unchecked automatically
						// This reverses the 'uncheck'
						src.setState(false);
					}

					newColumn.setHeaderValue(src.getFieldModel());			
					src.getFieldModel().assignedTableColumn = newColumn;
					
					src.getFieldModel().assignedTableColumnIndex = currentColumn;
				}
			}
			else {
				newColumn.setHeaderValue(src.getFieldModel());			
				src.getFieldModel().assignedTableColumn = newColumn;
				src.getFieldModel().assignedTableColumnIndex = currentColumn;
			}
			
			updateColumnData(oldModelIndex, currentColumn, src.getFieldModel().getInitialColumnindex());
			
			tableHeader.repaint();
		}
	};




	MouseAdapter tablePopupListener = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {

			if ("Delete".equals(((JMenuItem) e.getSource()).getActionCommand())) {
				int [] rows = table.getSelectedRows();
				DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

				for  (int i = rows.length-1; i >= 0; i--) {
					tableModel.removeRow(rows[i]);  
				}

				table.updateUI();
			}
			else if ("OriginalColumnTitles".equals(((JMenuItem) e.getSource()).getActionCommand())) {
				/* Putting row values in the header */
	
				int columnCount = table.getModel().getColumnCount();
				int row = table.getSelectedRow();

				String tempVal;

				GroupableTableColumnModel cm = (GroupableTableColumnModel) table.getColumnModel();
				ColumnGroup tmpGroup;

				ArrayList<ColumnGroup> columnGroups = cm.getColumnGroups();
	
				for (int i = 0; i < columnCount; i++) {
					
					tmpGroup = columnGroups.get(i);
					
					tempVal = (String) table.getModel().getValueAt(row, i);

					tmpGroup.setText(tempVal);
				}
			}
			tableHeader.repaint();
		}
	};
	
	public String [] getHeaderTitles() {

		String [] titles = null;
		
		try {
			
			//TableModel tableModel = table.getModel();
			TableColumnModel columnModel = table.getColumnModel();
			int columnCount = table.getModel().getColumnCount();

			TableColumn tmpColumn;
			FieldModel fieldModel;
			
			// Finding columns with values
			
			ArrayList<String> columnTitles = new ArrayList<String>();
			
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				tmpColumn = columnModel.getColumn(columnIndex);
				
				Object o = tmpColumn.getHeaderValue();
				
				if (o instanceof String) {
					continue;
				}
					
				fieldModel = (FieldModel) o;
				String title = fieldModel.getField();					
				columnTitles.add(title);
			}
			
			titles = columnTitles.toArray(new String[columnTitles.size()]);
		}
		catch (Exception e) {
			log.error("", e);
		}	
		
		return titles;
	}
	
	
	public String [][] retrieveValuesFromTable() {

		String [][] output = null;
		
		try {
			
			TableModel tableModel = table.getModel();
			TableColumnModel columnModel = table.getColumnModel();
			int columnCount = table.getModel().getColumnCount();

			TableColumn tmpColumn;
			FieldModel fieldModel;
			
			// Finding columns with values
			
			ArrayList<Integer> columns = new ArrayList<Integer>();
			
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				tmpColumn = columnModel.getColumn(columnIndex);
				
				Object o = tmpColumn.getHeaderValue();
				
				if (o instanceof String) {
					continue;
				}
									
				fieldModel = (FieldModel) o;

				// column has been assigned an info field 
				if (!fieldModel.toString().trim().equals("")) {
					columns.add(new Integer(columnIndex));
					
					if (fieldModel.toString().trim().equals("Title")) {
						titleColumnIndex = columnIndex;
					}
				}
			}
						
			output = new String[tableModel.getRowCount()][columns.size()];
			
			for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
				
				for (int row = 0; row < tableModel.getRowCount(); row++) {
					int colIndex = columns.get(columnIndex).intValue();
					output[row][columnIndex] = (String) table.getModel().getValueAt(row, colIndex);
				
					if (colIndex == titleColumnIndex)
						titleColumnIndex = columnIndex;
				}
			}
		}
		catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}	
		
		return output;
	}
	
	
	
	
	public ArrayList<ModelMovie> retrieveMovieListFromTable() {

		ArrayList<ModelMovie> movieList = new ArrayList<ModelMovie>(10);
		
		try {
			
			TableModel tableModel = table.getModel();
			TableColumnModel columnModel = table.getColumnModel();
			int columnCount = tableModel.getColumnCount();

			TableColumn tmpColumn;
			FieldModel fieldModel;
			String tableValue;
			ModelMovie tmpMovie;
			boolean valueStored = false;
			
			for (int row = 0; row < tableModel.getRowCount(); row++) {

				tmpMovie = new ModelMovie();
				valueStored = false;
				
				for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {

					tmpColumn = columnModel.getColumn(columnIndex);
					Object val = tmpColumn.getHeaderValue();
	
					if (!(val instanceof FieldModel)) {
						continue;
					}

					fieldModel = (FieldModel) val;

					// column has been assigned an info field 
					if (!fieldModel.toString().trim().equals("")) {

						tableValue = (String) tableModel.getValueAt(row, columnIndex);
						
						if (tmpMovie.setValue(fieldModel.getField(), tableValue, fieldModel.getTable())) {
							valueStored = true;
						}
					}
					
				}
	
				if (valueStored && tmpMovie.getTitle() != null && !tmpMovie.equals("")) {
					movieList.add(tmpMovie);
				}
				
			}
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
		
		return movieList;
	}
}
