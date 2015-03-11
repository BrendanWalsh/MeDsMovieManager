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

public class AdvancedMailbox {

	ArrayList<Object> list = new ArrayList<Object>();
	
	// The number of currently active threads
	int threadCount = 0;
	
	// The total number of threads that have been used
	int totalThreads = 0;
	
	synchronized public void increaseThreadCount() {
		threadCount++;
		totalThreads++;
	}
	
	synchronized public void decreaseThreadCount() throws Exception  {
		threadCount--;
		
		if (getSize() == 0 && threadCount == 0)
			notify();
	}
	
	synchronized public int getThreadCount() {
		return threadCount;
	}
	
	synchronized public int getTotalThreadCount() {
		return totalThreads;
	}
		
	synchronized public void removeElement(Object obj) throws Exception {
		
		if (!list.contains(obj))
			throw new Exception("Mailbox does not contain object " + obj);
		
		int index = list.indexOf(obj);
		
		list.remove(index);
	}
	
	synchronized public Object pop() throws Exception {
		
		if (list.size() == 0)
			throw new Exception("Mailbox is empty");
		
		return list.remove(0);
	}
	
	synchronized public void addElement(Object obj) throws Exception {
		
		if (list.contains(obj))
			throw new Exception("Mailbox already contains object " + obj);
		
		list.add(obj);
	}
	
	synchronized public int getSize() throws Exception {
		return list.size();
	}
	
	public void waitOnThreads() throws Exception {
		
		synchronized(this) {
			wait();
		}
	}
}
