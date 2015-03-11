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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.swing.util.table.ColumnGroup;
import net.sf.xmm.moviemanager.swing.util.table.GroupableTableColumnModel;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;

public class DialogTableImport extends DialogTableData {

	Logger log = Logger.getLogger(getClass());

	int importType;
	
	Object [][] data;

	public DialogTableImport(JFrame parent, Object [][] data, ModelImportExportSettings settings) {

		super(parent, settings);

		setTitle("Import Movies");
		setModal(true);

		this.file = settings.getFile();

		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());        

		try {
	
			int cols = data[0].length;
						
			Object [] emptyColumnNames = new Object[cols];

			for (int i = 0; i < emptyColumnNames.length; i++)
				emptyColumnNames[i] = " ";

			DefaultTableModel dm = new DefaultTableModel();
			dm.setDataVector(data, emptyColumnNames);
			table.setModel(dm);

			GroupableTableColumnModel cm = (GroupableTableColumnModel) table.getColumnModel();
			ColumnGroup tmpGroup;

			for (int i = 0; i < cols; i++) {
				tmpGroup = new ColumnGroup(" ");
				tmpGroup.add(cm.getColumn(i));
				cm.addColumnGroup(tmpGroup);
			}

			buttonDone.setText("Import Data");
			buttonDone.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					
					GroupableTableColumnModel columnModel = (GroupableTableColumnModel) tableHeader.getColumnModel();
					int columnCount = table.getModel().getColumnCount();
					
					TableColumn newColumn = columnModel.getColumn(currentColumn);
					boolean titleFound = false;
					
					for (int i = 0; i < columnCount; i++ ) {
						newColumn = columnModel.getColumn(i);
						
						if (newColumn.getHeaderValue() != null && newColumn.getHeaderValue() instanceof FieldModel && ((FieldModel) newColumn.getHeaderValue()).table.equals("General Info") && "Title".equals(newColumn.getHeaderValue().toString()))
							titleFound = true;
					}
					
					if (!titleFound) {
						DialogAlert alert = new DialogAlert(dialogImportTable, "Title column missing", "Title column must be specified");
						GUIUtil.show(alert, true);
						return;
					}
										
					dispose();
				}
			});
			
		}
		catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
	}
	
	public ArrayList<ModelMovie> retrieveMovieListFromTable() {

		ArrayList<ModelMovie> movieList = new ArrayList<ModelMovie>(10);
		
		try {
			
			TableModel tableModel = table.getModel();
			TableColumnModel columnModel = table.getColumnModel();
			int columnCount = table.getModel().getColumnCount();

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

						tableValue = (String) table.getModel().getValueAt(row, columnIndex);
						fieldModel.setValue(tableValue);

						fieldModel.validateValue();

						if (tmpMovie.setValue(fieldModel.getField(), fieldModel.getValue(), fieldModel.getTable())) {
							valueStored = true;
						}
					}
				}

				if (valueStored && tmpMovie.getTitle() != null && !tmpMovie.equals("")) {
					movieList.add(tmpMovie);
				}
			}
		}
		catch (Exception e) {
			log.error("", e);
		}	
		
		return movieList;
	}
	
}
