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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.swing.util.table.ColumnGroup;
import net.sf.xmm.moviemanager.swing.util.table.GroupableTableColumnModel;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;

public class DialogTableExport extends DialogTableData {

	Logger log = Logger.getLogger(getClass());
	
	DefaultTableModel tableModel;
	Object [][] databaseData;
	Object [][] tableData;
	
	public int titleColumnIndex = -1;
	
	public DialogTableExport(JFrame parent, Object [][] data, ModelImportExportSettings settings) {

		super(parent, settings);
		this.databaseData = data;
		
		setTitle("Export " + settings.exportMode);
		setModal(true);
		//table.getTableHeader().setReorderingAllowed(true);
		
		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());        
		
		try {

			int rowLen = data.length;
			int colsLen = data[0].length;
			
			Object [] emptyColumnNames = new Object[colsLen];

			for (int i = 0; i < emptyColumnNames.length; i++)
				emptyColumnNames[i] = " ";

			tableData = new Object[rowLen][colsLen];
			tableModel = new DefaultTableModel();
			tableModel.setDataVector(tableData, emptyColumnNames);

			table.setModel(tableModel);
	
			GroupableTableColumnModel cm = (GroupableTableColumnModel) table.getColumnModel();
			ColumnGroup tmpGroup;
			
			for (int i = 0; i < colsLen; i++) {
				tmpGroup = new ColumnGroup(" ");
				tmpGroup.add(cm.getColumn(i));
				cm.addColumnGroup(tmpGroup);
			}

			buttonDone.setText("Export Data");
			buttonDone.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
										
					GroupableTableColumnModel columnModel = (GroupableTableColumnModel) tableHeader.getColumnModel();
					int columnCount = table.getModel().getColumnCount();
					
					TableColumn newColumn;
					boolean columnDataSpecified = false;
					
					for (int i = 0; i < columnCount; i++ ) {
						newColumn = columnModel.getColumn(i);
						
						// Has value other than string
						if (!(newColumn.getHeaderValue() instanceof String)) {
							columnDataSpecified = true;					
						}
					}
					
					if (!columnDataSpecified) {
						DialogAlert alert = new DialogAlert(dialogImportTable, "No data fields specified", "One or more data fields must be chosen to be exported");
						GUIUtil.show(alert, true);
						return;
					}
										
					dispose();
				}
			}
			);
			
			/*
			table.getTableHeader().getColumnModel().addColumnModelListener(new TableColumnModelListener() {

				public void columnAdded(TableColumnModelEvent arg0) {}
				public void columnMarginChanged(ChangeEvent arg0) {}
				public void columnRemoved(TableColumnModelEvent arg0) {}
				public void columnSelectionChanged(ListSelectionEvent arg0) {}
				
				public void columnMoved(TableColumnModelEvent arg0) {

					if (arg0.getFromIndex() != arg0.getToIndex()) {
						
						//table.getTableHeader().getColumnModel().moveColumn(arg0.getFromIndex(), arg0.getToIndex());
						
						switchColumnData(arg0.getFromIndex(), arg0.getToIndex());
						printTable();
						tableHeader.repaint();
					}
				}
			});
			*/
		}
		catch (Exception e) {
			log.error("", e);
		}
	}
	
	/*
	void printTable() {
		String [] titles = getHeaderTitles();
		String [][] tData = retrieveValuesFromTable();
		
		for (int i = 0; i < titles.length; i++) {
			System.out.print(titles[i] + "  ");
		}
				
		for (int i = 0; i < tData[0].length; i++) {
			System.out.print(tData[0][i] + "  ");
		}
	}
	
	
	public void switchColumnData(int index1, int index2) {
		
		System.out.println("switch column data:" + index1 + " and " + index2);
		
		if (index1 != -1 && index2 != -1) {
			// Removes data in old column
			for (int i = 0; i < databaseData.length; i++) {
				Object tmp1 = tableModel.getValueAt(i, index1);
				Object tmp2 = tableModel.getValueAt(i, index2);
				System.out.println("tmp1:" + tmp1);	
				System.out.println("tmp2:" + tmp2);	
				
				tableModel.setValueAt(tmp2, i, index1);
				tableModel.setValueAt(tmp1, i, index2);
			}
		}
	}
	*/
	
	public void updateColumnData(int oldModelIndex, int currentColumn, int initialColumnindex) {
		
		if (oldModelIndex != -1) {
			// Removes data in old column
			for (int i = 0; i < databaseData.length; i++) {
				tableModel.setValueAt("", i, oldModelIndex);
			}
		}
		
		if (currentColumn != -1) {
	
			// Adds data to new column
			for (int i = 0; i < databaseData.length; i++) {
				tableModel.setValueAt(databaseData[i][initialColumnindex], i, currentColumn);
			}
		}
	}
}
