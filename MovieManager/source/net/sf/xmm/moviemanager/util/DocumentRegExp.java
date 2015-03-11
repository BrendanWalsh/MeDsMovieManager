/**
 * @(#)DocumentRegExp.java
 *
 * Copyright (2003) Mediterranean
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
 * Contact: mediterranean@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.util;

import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

final public class DocumentRegExp extends PlainDocument {

	private int _length = 0;

	private Pattern _pattern;

	public DocumentRegExp(String regExp) {
		_pattern = Pattern.compile(regExp);
	}

	public DocumentRegExp(String regExp, int length) {
		_pattern = Pattern.compile(regExp);
		_length = length;
	}

	public void insertString(int offset, String string, AttributeSet a) throws BadLocationException {
		
		if (string == null) {
			return;
		}
				
		if (_length > 0 && string.length() + getLength() > _length) {
			string = string.substring(0, _length - getLength());
		}
				
		StringBuffer str = new StringBuffer(getText(0, getLength()));
		str.insert(offset, string);
				
		/* Checks for the structure... */
		if (_pattern.matcher(str.toString()).matches()) {
			super.insertString(offset, string, a);
		}
	}
} 
