 /**
 * @(#)HttpSettings.java
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

package net.sf.xmm.moviemanager.http;

public class HttpSettings {

	boolean proxyEnabled = false;
		
	String proxyType = "";
	
	String proxyHost = "";
	String proxyPort = "";
	
		
	boolean proxyAuthenticationEnabled = false;
	
	String proxyUser = "";
	String proxyPassword = "";
	
	boolean IMDbAuthenticationEnabled = false;
	String  IMDbAuthenticationUser = "";
	String IMDbAuthenticationPassword = "";
		
	boolean autoMoveThe = false;
	boolean autoMoveAnAndA = false;
	
	boolean removeQuotesOnSeriesTitles = false;
	
	public String getProxyType() {
		return proxyType;
	}

	public void setProxyType(String proxyType) {
		this.proxyType = proxyType;
	}
	
	
	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUser() {
		return proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public boolean getProxyEnabled() {
		return proxyEnabled;
	}

	public void setProxyEnabled(boolean proxyEnabled) {
		this.proxyEnabled = proxyEnabled;
	}

	public boolean getProxyAuthenticationEnabled() {
		return proxyAuthenticationEnabled;
	}

	public void setProxyAuthenticationEnabled(boolean proxtAuthenticationEnabled) {
		this.proxyAuthenticationEnabled = proxtAuthenticationEnabled;
	}

	
	public boolean getIMDbAuthenticationEnabled() {
		return IMDbAuthenticationEnabled;
	}

	public void setIMDbAuthenticationEnabled(boolean IMDbAuthenticationEnabled) {
		this.IMDbAuthenticationEnabled = IMDbAuthenticationEnabled;
	}
	
	
	public String getIMDbAuthenticationUser() {
		return IMDbAuthenticationUser;
	}
	
	public void setIMDbAuthenticationUser(String IMDbAuthenticationUser) {
		this.IMDbAuthenticationUser = IMDbAuthenticationUser;
	}
	
	public String getIMDbAuthenticationPassword() {
		return IMDbAuthenticationPassword;
	}

	public void setIMDbAuthenticationPassword(String IMDbAuthenticationPassword) {
		this.IMDbAuthenticationPassword = IMDbAuthenticationPassword;
	}
	
	public boolean getAutoMoveThe() {
		return autoMoveThe;
	}

	public void setAutoMoveThe(boolean autoMoveThe) {
		this.autoMoveThe = autoMoveThe;
	}

	public boolean getAutoMoveAnAndA() {
		return autoMoveAnAndA;
	}

	public void setAutoMoveAnAndA(boolean autoMoveAnAndA) {
		this.autoMoveAnAndA = autoMoveAnAndA;
	}
	
	public boolean getRemoveQuotesOnSeriesTitles() {
		return removeQuotesOnSeriesTitles;
	}

	public void setRemoveQuotesOnSeriesTitles(boolean removeQuotesOnSeriesTitles) {
		this.removeQuotesOnSeriesTitles = removeQuotesOnSeriesTitles;
	}
	
	
}
