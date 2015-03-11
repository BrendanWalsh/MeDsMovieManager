/**
 * @(#)AdditionalInfoFieldDefaultValues.java 23.01.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.models;

import java.util.ArrayList;

public class AdditionalInfoFieldDefaultValues {

	private String fieldName = "";
	private ArrayList<String> values;

	public AdditionalInfoFieldDefaultValues(String fieldName) {

		this.fieldName = fieldName;
		this.values = new ArrayList<String>();
	}

	/* Adds to the start of the list */
	public void insertValue(String value) {

		if (!values.contains(value) && !value.equals("")) {
			values.add(0, value);
		}
	}

	/* Adds to the end of the list */
	public void addValue(String value) {

		if (!values.contains(value) && !value.equals("")) {
			values.add(value);
		}
	}

	public String getFieldName() {
		return fieldName;
	}

	public ArrayList<String> getDefaultValues() {
		return new ArrayList<String>(values);
	}

	public String getDefaultValuesString(String separator) {

		StringBuffer retString = new StringBuffer(separator);

		for (int i = 0; i < values.size(); i++) {
			retString.append(values.get(i)+separator);
		}
		return retString.toString();
	}
}
