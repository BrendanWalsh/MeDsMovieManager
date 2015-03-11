/**
 * @(#)SimpleMailbox.java 1.0 23.03.05 (dd.mm.yy)
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


package net.sf.xmm.moviemanager.util.tools;

import java.util.ArrayList;

public class SimpleMailbox<E> {

	ArrayList<E> msgs = new ArrayList<E>();
	
	synchronized public int getMessageCount()  {
		return msgs.size();
	}
	
	synchronized public E getMessage() {
		
		if (msgs.size() == 0)
			return null;
		
		return msgs.remove(0);
	}
	
	synchronized public void setMessage(E msg) throws InterruptedException {
		msgs.add(msg);
		notify_message();
	}
	
	synchronized public void wait_for_message() throws InterruptedException {
		
		// If no messages available, wait
		if (msgs.size() == 0)
			wait();
	}
	
	public void notify_message() throws InterruptedException {
		notify();
	}
	
}
