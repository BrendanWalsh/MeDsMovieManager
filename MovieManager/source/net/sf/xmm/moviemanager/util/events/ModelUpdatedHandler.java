/**
 * @(#)NewDatabaseLoadedHandler.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.util.events;

import net.sf.xmm.moviemanager.util.events.ModelUpdatedEvent.IllegalEventTypeException;

// Add the event registration and notification code to a class.
public class ModelUpdatedHandler {
    
    // Create the listener list
    protected javax.swing.event.EventListenerList listenerList =
	new javax.swing.event.EventListenerList();
    
    // This methods allows classes to register for MyEvents
    public void addModelChangedEventListenener(ModelUpdatedEventListener listener) {
	listenerList.add(ModelUpdatedEventListener.class, listener);
    }
    
    // This methods allows classes to unregister for MyEvents
    public void removeModelChangedEventListenener(ModelUpdatedEventListener listener) {
	listenerList.remove(ModelUpdatedEventListener.class, listener);
    }
    
    // This private class is used to fire MyEvents
    void fireModelChangedEvent(ModelUpdatedEvent evt) {
	Object[] listeners = listenerList.getListenerList();
	// Each listener occupies two elements - the first is the listener class
	// and the second is the listener instance
	for (int i=0; i<listeners.length; i += 2) {
	    if (listeners[i] == ModelUpdatedEventListener.class) {
		((ModelUpdatedEventListener) listeners[i + 1]).modelUpdatedEvent(evt);
	    }
	}
    }

    public void modelChanged(Object source, String type) throws IllegalEventTypeException {
    	fireModelChangedEvent(new ModelUpdatedEvent(source, type));
    }
}
