/**
 * @(#)ModelChangedEvent.java 1.0 26.09.06 (dd.mm.yy)
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

import java.util.EventObject;

public class ModelUpdatedEvent extends EventObject {
	
	String eventType;
	
	public ModelUpdatedEvent(Object source, String updateEventType) throws IllegalEventTypeException {
		super(source);

		eventType = updateEventType;
		
		if (!"GeneralInfo".equals(updateEventType) && !"AdditionalInfo".equals(updateEventType))
			throw new IllegalEventTypeException("Type must be either GeneralInfo or AdditionalInfo");
	}

	public String getUpdateType() {
		return eventType;
	}
	
	public class IllegalEventTypeException extends Exception {

		public IllegalEventTypeException(String type) {
			super(type);
		}
	}
} 