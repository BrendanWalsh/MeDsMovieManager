/**
 * @(#)ButtonGroupNoSelection.java 1.0 03.11.05 (dd.mm.yy)
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


package net.sf.xmm.moviemanager.swing.extentions;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

/* This button group allows for 0 or more buttons selected */
public class ButtonGroupNoSelection extends ButtonGroup implements Serializable {

    // the list of buttons participating in this group
    protected Vector<AbstractButton> buttons = new Vector<AbstractButton>();

    /**
     * The current selection.
     */
    ButtonModel selection = null;

    /**
     * Creates a new <code>ButtonGroup</code>.
     */
    public ButtonGroupNoSelection() {
        super();
    }

    /**
     * Adds the button to the group.
     * @param b the button to be added
     */ 
    public void add(AbstractButton b) {
        if(b == null) {
            return;
        }
        buttons.addElement(b);

        if (b.isSelected()) {
            if (selection == null) {
                selection = b.getModel();
            } else {
                b.setSelected(false);
            }
        }

        b.getModel().setGroup(this);
    }
 
    /**
     * Removes the button from the group.
     * @param b the button to be removed
     */ 
    public void remove(AbstractButton b) {
        if(b == null) {
            return;
        }
        buttons.removeElement(b);
        if(b.getModel() == selection) {
            selection = null;
        }
        b.getModel().setGroup(null);
    }

    /**
     * Returns all the buttons that are participating in
     * this group.
     * @return an <code>Enumeration</code> of the buttons in this group
     */
    public Enumeration<AbstractButton> getElements() {
        return buttons.elements();
    }

    /**
     * Returns the model of the selected button.
     * @return the selected button model
     */
    public ButtonModel getSelection() {
        return selection;
    }

    /**
     * Sets the selected value for the <code>ButtonModel</code>.
     * Only one button in the group may be selected at a time.
     * @param m the <code>ButtonModel</code>
     * @param b <code>true</code> if this button is to be
     *   selected, otherwise <code>false</code>
     */
    public void setSelected(ButtonModel m, boolean b) {
	
	if (m != null) {
	    
	    if (m == selection && !b) {
		selection = null;
		m.setSelected(false);
	    }	    
	    else if (b && m != selection) {
		ButtonModel oldSelection = selection;
		selection = m;
		if (oldSelection != null) {
		    oldSelection.setSelected(false);
		}
		m.setSelected(true);
	    } 
	}
    }

    /**
     * Returns whether a <code>ButtonModel</code> is selected.
     * @return <code>true</code> if the button is selected,
     *   otherwise returns <code>false</code>
     */
    public boolean isSelected(ButtonModel m) {
        return (m == selection);
    }

    /**
     * Returns the number of buttons in the group.
     * @return the button count
     * @since 1.3
     */
    public int getButtonCount() {
	if (buttons == null) {
	    return 0;
	} else {
	    return buttons.size();
	}
    }
}
