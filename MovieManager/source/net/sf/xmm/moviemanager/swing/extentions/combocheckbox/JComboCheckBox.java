/**
 * @(#)JComboCheckBox.java 1.0 21.12.07 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.swing.extentions.combocheckbox;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.ComboBoxEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import net.sf.xmm.moviemanager.MovieManager;


public class JComboCheckBox extends JComboBox {

	public JComboCheckBox(JCheckBox[] items) { super(items); init(items);}

	private boolean selectCheckBoxMouseState = false;
	private boolean displayPopupKeyState = false;

	private boolean enter = false;
	private boolean mouseOrKey = false; // true = mouse, false = key

	private ArrayList<String> enteredValues = new ArrayList<String>();

	private JList combolist = null;
	private ComboCheckBoxRenderer comboBoxRenderer;
	private JComboCheckBoxEditor comboCheckBoxEditor;

	public JComboCheckBox thisComboBox;

	private void init(Object [] values) {
		thisComboBox = this;

		comboBoxRenderer = new ComboCheckBoxRenderer();
		comboCheckBoxEditor = new JComboCheckBoxEditor();

		setRenderer(comboBoxRenderer);
		setEditor(comboCheckBoxEditor);

		// Reset default selection
		setSelectedItem(null);
		setEditable(true);

		// Setting the caret position of the combobox
		((JTextField) getEditor().getEditorComponent()).setCaretPosition(0);

		addValues(values);
	}

	public void addValues(Object [] values) {

		if (values != null) {
			int len = 0;
			for (int i = 0; i < values.length; i++) {
				if (len < ((JCheckBox) values[i]).getText().length()) {
					len = ((JCheckBox) values[i]).getText().length();
				}
			}

			for (int i = 0; i < values.length; i++) {
				((JCheckBox) values[i]).setSelected(true);
				enteredValues.add(((JCheckBox) values[i]).getText());
				addItem(((JCheckBox) values[i]).getText());
			}
		}
	}



	protected void processMouseEvent1(MouseEvent e) {

		//System.out.println("processMouseEvent");

		if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			if (enteredValues.size() > 0) 
				setPopupVisibleForReal(!isPopupVisible());
		}
	}

	// overridden and ignored
	public void setPopupVisible(boolean v) {
		//System.out.println("setPopupVisible");
	}

	public void setPopupVisibleForReal(boolean visible) {
		//System.out.println("setPopupVisibleForReal");
		super.setPopupVisible(visible);
	}

	// Retrieves the text in the combobox text field.
	public String getText() {
		Object item = (( ComboBoxEditor) getEditor()).getItem();
		return(String) item;
	}	


	class ComboCheckBoxRenderer implements ListCellRenderer, MouseListener {
		private JLabel defaultLabel;

		public ComboCheckBoxRenderer() { 
			setOpaque(true); 
		}

		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {

			if (value instanceof Component) {

				//Adds listener to JList 
				if (combolist == null) {
					combolist = list;

					if (combolist != null) {
						combolist.addMouseListener(comboBoxRenderer);
					}
				}

				Component c = (Component) value; 

				if (isSelected) {
					c.setBackground(list.getSelectionBackground());
					c.setForeground(list.getSelectionForeground());
				} else {
					c.setBackground(list.getBackground());
					c.setForeground(list.getForeground());
				}
				return c;

			} else {
				if (value == null)
					defaultLabel = new JLabel("Value is null");
				else {
					// I'm not sure about this stuff	
					if (defaultLabel == null) defaultLabel = new JLabel(value.toString());
					else defaultLabel.setText(value.toString());
				}
				return defaultLabel;
			}
		}

		public void mouseReleased(MouseEvent me) {

			//System.out.println("mouseReleased");

			// Select/deselect the checkbox
			if (selectCheckBoxMouseState) {

				int index = combolist.locationToIndex(me.getPoint());
				JCheckBox box = (JCheckBox) getItemAt(index);
				box.setSelected(!box.isSelected());
				combolist.repaint();

				if (box.isSelected()) {
					MovieManager.getConfig().addMainFilterSearchValue((String)box.getText());
				}
				else {
					MovieManager.getConfig().removeMainFilterSearchValue((String)box.getText());
				}
			}
			else
				setPopupVisibleForReal(false);
		}

		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent arg) {}
		public void mouseClicked(MouseEvent e) {}
		public void mousePressed(MouseEvent me) {

			mouseOrKey = true;
			selectCheckBoxMouseState = false;

			if (SwingUtilities.isLeftMouseButton(me)) {

				String compName = ((Component) me.getComponent()).getName();

				if (compName != null && compName.equals("ComboBox.list")) {

					//int index = combolist.locationToIndex(me.getPoint());
					//JCheckBox box = (JCheckBox) getItemAt(index);

					int x = me.getX();

					// Makes sure it changes only when pressing the checkbox
					if (x <= 20)
						selectCheckBoxMouseState = true;
				}	
			}
		}
	}

	public void addItem(String str) {
		
		if (str != null && !"".equals(str)) {
		
			
			addItem(new JCheckBox(str));
		}
	}

	public void addItem(JCheckBox checkBox) {
		String str = checkBox.getText();

		if (enteredValues.contains(str))
			return;

		checkBox.setToolTipText("Mark the checkbox to save this search");
		
		enteredValues.add(str);
		super.addItem(checkBox);
	}



	public class JComboCheckBoxEditor extends BasicComboBoxEditor implements KeyListener {

		JComboCheckBoxEditor() {
			super();
			getEditorComponent().addKeyListener(this);
		}

		// Override method
		public void setItem(Object anObject) {

			if ((!mouseOrKey && enter) || (mouseOrKey && !selectCheckBoxMouseState))
				setItem2(anObject);
		}

		public void setItem2(Object anObject) {
			String str = "";

			if (anObject != null) {

				if (anObject instanceof JCheckBox) {
					str = ((JCheckBox) anObject).getText();

					if (!isPopupVisible() && enter)
						return;

					if (!selectCheckBoxMouseState || !displayPopupKeyState) {
						super.setItem(str);
					}
				}
			} 
		}

		public void keyReleased(KeyEvent e) {

			mouseOrKey = false;

			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				enter = false;
				comboCheckBoxKeyAction(this);
			}

			if (isPopupVisible() && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				setPopupVisibleForReal(false);
			}
			else if (!isPopupVisible() && (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP)) {
				setPopupVisibleForReal(true);
			}
			else if (isPopupVisible() && (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT)) {

				if (getSelectedIndex() == -1)
					return;

				JCheckBox box = (JCheckBox) getItemAt(getSelectedIndex());
				box.setSelected(!box.isSelected());

				if (box.isSelected()) {
					MovieManager.getConfig().addMainFilterSearchValue((String)box.getText());
				}
				else {
					MovieManager.getConfig().removeMainFilterSearchValue((String)box.getText());
				}

				combolist.repaint();
			}
		}


		public void keyTyped(KeyEvent e) {}
		public void keyPressed(KeyEvent e) {

			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				enter = true;

				if (isPopupVisible() && getSelectedIndex() != -1) {
					JCheckBox box = (JCheckBox) getItemAt(getSelectedIndex());

					displayPopupKeyState = false;
					comboCheckBoxEditor.setItem2(box);

					setPopupVisibleForReal(false);
				}
				else {
					displayPopupKeyState = true;

					String str = getText();
					addItem(str);
				}
			}
			else if (!isPopupVisible() && (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP))
				displayPopupKeyState = true;
		}


//		Create the listener list
		protected javax.swing.event.EventListenerList listenerList =
			new javax.swing.event.EventListenerList();

		// This methods allows classes to register for ComboCheckBoxKeyEvent
		public void addComboCheckBoxKeyEventListener(ComboCheckBoxKeyEventListener listener) {
			listenerList.add(ComboCheckBoxKeyEventListener.class, listener);
		}

		// This methods allows classes to unregister for ComboCheckBoxKeyEvent
		public void removeComboCheckBoxKeyEventListener(ComboCheckBoxKeyEventListener listener) {
			listenerList.remove(ComboCheckBoxKeyEventListener.class, listener);
		}

		// This private class is used to fire ComboCheckBoxKeyEvent
		void fireComboCheckBoxKeyEvent(ComboCheckBoxKeyEvent evt) {
			Object[] listeners = listenerList.getListenerList();
			// Each listener occupies two elements - the first is the listener class
			// and the second is the listener instance
			for (int i=0; i<listeners.length; i+=2) {
				if (listeners[i] == ComboCheckBoxKeyEventListener.class) {
					((ComboCheckBoxKeyEventListener) listeners[i+1]).comboCheckBoxKeyActionPerformed(evt);
				}
			}
		}

		public void comboCheckBoxKeyAction(Object source) {
			fireComboCheckBoxKeyEvent(new ComboCheckBoxKeyEvent(source));
		}
	}
}

