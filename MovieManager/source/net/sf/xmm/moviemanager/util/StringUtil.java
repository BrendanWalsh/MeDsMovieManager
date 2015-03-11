/**
 * @(#)StringUtil.java 1.0 05.06.07 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.util;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class StringUtil {

	static Logger log = Logger.getLogger(StringUtil.class);

	public static String cleanInt(String toBeCleaned, int debugMode) {

		boolean start = false;
		String result = "";

		if (debugMode == 1)
			log.debug("toBeCleaned:" + toBeCleaned);

		for (int i = 0; i < toBeCleaned.length(); i++) {

			if (Character.isDigit(toBeCleaned.charAt(i)))
				start = true;

			if (start) {

				if (debugMode == 1 && toBeCleaned.charAt(i) == ',')
					log.debug("NumValComma:"+ Character.getNumericValue(toBeCleaned.charAt(i)));

				if (Character.getNumericValue(toBeCleaned.charAt(i)) != -1 || toBeCleaned.charAt(i) == ',' || toBeCleaned.charAt(i) == '.') {

					if (Character.isDigit(toBeCleaned.charAt(i))) {
						result += toBeCleaned.charAt(i);
					}
					else {
						break;
					}
				}
			}
		}

		if (debugMode == 1)
			log.debug("result:" + result);

		return result;
	}

	public static String removeDoubleSpace(String str) {

		/* Remove all double white space */
		StringBuffer stringBuff = new StringBuffer();
		for(int x = 0; x < str.length(); x++)
			if (str.charAt(x) != ' ' || (str.length() != x+1 && str.charAt(x+1) != ' '))
				stringBuff = stringBuff.append(str.charAt(x));

		return stringBuff.toString();
	}

	String removeDoubleSpace2(String searchString) {

		int index;

		/*Removes all double spaces*/
		while ((index = searchString.indexOf("  ")) != -1) {
			searchString = removeCharAt(searchString, index);
		}

		return searchString;
	}
	
	public static String removeAtEnd(String input, String remove) {
		
		if (input.trim().endsWith(remove))
			input = input.substring(0, input.indexOf(remove)).trim();
		
		return input;
	}
	

	
	public static void replaceAll(StringBuffer sb, String search, String replace) {

		if (replace == null)
			replace = "";

		for (int index = 0, l1 = search.length(), l2 = replace.length(); (index = sb.indexOf(search, index)) != -1; sb.replace(index, index + l1, replace), index += l2);
	}


	
	/**
	 *  Removes all parantheses including it's content 
	 * @throws Exception 
	 */
	public static String performExcludeParantheses(String searchString, boolean toLowerCase) throws Exception {

		if (searchString == null) {
			throw new Exception("Input string cannot be null");
		}
		
		if (toLowerCase)
			searchString = searchString.toLowerCase();
		
		StringBuffer searchStringBuf = new StringBuffer(searchString);
		
		int start;
		int end;

		String [] paranthese1 = new String[] {"(", "[", "{"};
		String [] paranthese2 = new String[] {")", "]", "}"};

		for (int i = 0; i < 3; i++) {

			while (true) {

				start = searchStringBuf.indexOf(paranthese1[i]);
				end = searchStringBuf.indexOf(paranthese2[i]);
				
				if (start != -1 && end != -1) {

					if (start > end) {
						searchStringBuf.replace(end, end+1, " ");
					}
					else {
						searchStringBuf.delete(start, end+1);
					}
				}
				else if (start != -1)
					searchStringBuf.replace(start, start+1, " ");
				else if (end != -1) 
					searchStringBuf.replace(end, end+1, " ");
				else
					break;
			}
		}
		return searchStringBuf.toString();
	}
	
	/**
	 * Remove the strings in the array from the input string.
	 * The case of the searchString is preserved.
	 */
	public static String performExcludeStrings(String searchString, String [] excludeStrings) {
				
		for (int u = 0; u < excludeStrings.length; u++) {
			searchString = removeMatches(searchString, excludeStrings[u].toLowerCase());
		}
		return searchString;
	}

	/**
	 * Removes a match on the (lowercased) regex, keeping the case of the original string.
	 * @param searchString
	 * @param regex
	 * @return
	 */
	static String removeMatches(String searchString, String regex) {
		
		Pattern excludeFilter = Pattern.compile(regex.toLowerCase());
		String searchStringLower = searchString.toLowerCase();
		
		Matcher m = excludeFilter.matcher(searchStringLower);
				
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			String toRemove = searchString.substring(start, end);
			
			searchString = searchString.replaceAll(toRemove, " ");
			searchStringLower = searchString.toLowerCase();
			m = excludeFilter.matcher(searchStringLower);
		}
		
		return searchString;
	}
	
	/**
	 * Keep only string content before a year
	 * Will not remove if the year is at the beginning of the string
	 * @param input
	 * @return
	 */
	public static String removeYearAndAllAfter(String input) {
		return removeYearAndAllAfter(input, null);
	}
	
	public static String removeYearAndAllAfter(String input, String [] yearOutput) {
			
		Pattern excludeFilter = Pattern.compile("\\d{4}");

		Matcher m = excludeFilter.matcher(input);

		int lastIndexToKeep = input.length();
		
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			
			// If date is in the beginning, do not remove
			if (start == 0)
				continue;
				
			String year = input.substring(start, end);
					
			int y = Integer.parseInt(year);
			
			// Verify it's a valid movie year
			if (y > 1895 && y < 2100) {
				lastIndexToKeep = start;
				
				if (yearOutput != null && yearOutput.length > 0)
					yearOutput[0] = year;
			}
		}
		
		input = input.substring(0, lastIndexToKeep);
		
		return input;
	}
	
	/**
	 * Deletes everything which occurs behind any of the found Strings
	 * @param searchString
	 * @param excludeStrings
	 * @return
	 */
	public static String performExcludeUserdefinedInfo(String searchString, String [] excludeStrings) {
		
		String searchStringLower = searchString.toLowerCase();
		
		int pos;
		for (int u = 0; u < excludeStrings.length; u++) {
			pos = searchStringLower.indexOf(excludeStrings[u]);
			if (pos != -1) {
				searchString = searchString.substring(0, pos);
				searchStringLower = searchString.toLowerCase();
			}
		}
		return searchString;
	}
	


	/*Removes cd notations from the search string*/
	public static String performExcludeCDNotations(String searchString) {

		searchString = removeMatches(searchString, "cd\\dof\\d");
		searchString = removeMatches(searchString, "cd\\d");
		searchString = removeMatches(searchString, "\\dof\\d");
		
		return searchString;
	}

	/*Removes all integers from the search string*/
	public static String performExcludeIntegers(String searchString) {
		/* If folder, name is extension, no '.' in file */
		int length = searchString.lastIndexOf('.');
		if (length == -1) {
			length = searchString.length();
		}
		int index = 0;

		while (index < length) {

			if (Character.isDigit(searchString.charAt(index))) {
				searchString = replaceCharAt(searchString, index, ' ');
				length--;
			}
			else
				index++;
		}
		return searchString;
	}

	public static String removeExtension(String searchString, String ... extensions) {
		
		/*Removes extension*/
		if (searchString.lastIndexOf('.') > -1) {
		
			String extension = searchString.substring(searchString.lastIndexOf('.') +1, searchString.length());
			
			for (String ext : extensions) {
				if (extension.equals(ext)) {
					return searchString.substring(0, searchString.lastIndexOf('.'));
				}
			}
		}
		
		return searchString;
	}
	
	/**
	 * Removes the extension of a file name and dot, comma, underscore and equal sign and double spaces
	 * @param searchString  the string to be modified
	 * @return
	 */
	public static String removeVarious(String searchString) {

		/*Removes various*/
		searchString = searchString.replace('.', ' ');
		searchString = searchString.replace(',', ' ');
		searchString = searchString.replace('=', ' ');
		searchString = searchString.replace('_', ' ');
		searchString = searchString.replace('(', ' ');
		searchString = searchString.replace(')', ' ');
		searchString = searchString.replace('[', ' ');
		searchString = searchString.replace(']', ' ');
		
		searchString = " " + searchString + " ";
		
		searchString = searchString.replace(" - ", " ");
		
		/*Removes all double spaces*/
		searchString = removeDoubleSpace(searchString);
		searchString = searchString.trim();
		
		return searchString;
	}

	public static String removeCharAt(String s, int pos) {
		return s.substring(0,pos)+s.substring(pos+1);
	}

	public static String replaceCharAt(String s, int pos, char c) {
		return s.substring(0,pos) + c + s.substring(pos+1);
	}
	
	
	
	/**
	 * Calculates the edit ditstance of two strings.
	 * @param the strings to be compared
	 * @author Chas Emerick
	 * @return edit distance
	 */
	public static int getLevenshteinDistance(String s, String t) {
		 
		if (s == null || t == null) {
		    throw new IllegalArgumentException("Strings must not be null");
		  }
							
		  int n = s.length();
		  int m = t.length();
				
		  if (n == 0) {
		    return m;
		  } else if (m == 0) {
		    return n;
		  }

		  int p[] = new int[n+1]; //'previous' cost array, horizontally
		  int d[] = new int[n+1]; // cost array, horizontally
		  int _d[]; //placeholder to assist in swapping p and d

		  // indexes into strings s and t
		  int i; // iterates through s
		  int j; // iterates through t

		  char t_j; // jth character of t

		  int cost;

		  for (i = 0; i<=n; i++) {
		     p[i] = i;
		  }
				
		  for (j = 1; j<=m; j++) {
		     t_j = t.charAt(j-1);
		     d[0] = j;
				
		     for (i=1; i<=n; i++) {
		        cost = s.charAt(i-1)==t_j ? 0 : 1;
		        // minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
		        d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
		     }

		     // copy current distance counts to 'previous row' distance counts
		     _d = p;
		     p = d;
		     d = _d;
		  } 
				
		  // our last action in the above loop was to switch d and p, so p now 
		  // actually has the most recent cost counts
		  return p[n];
	}

	/**
	 * Calculates the length of the longest comon substring
	 * @author: http://en.wikibooks.org/wiki/Algorithm_implementation/Strings/Longest_common_substring
	 */
	public static int longestSubstr(String str_, String toCompare_) {
		
		if (str_ == null || toCompare_ == null) {
			return -1;
		} else if (str_.equals("") || toCompare_.equals("")) {
			return -1;	
		}
	
		int[][] compareTable = new int[str_.length()][toCompare_.length()];
		int maxLen = 0;

		for (int m = 0; m < str_.length(); m++) {
			for (int n = 0; n < toCompare_.length(); n++) {
				compareTable[m][n] = (str_.charAt(m) != toCompare_.charAt(n)) ? 0
						: (((m == 0) || (n == 0)) ? 1
								: compareTable[m - 1][n - 1] + 1);
				maxLen = (compareTable[m][n] > maxLen) ? compareTable[m][n] : maxLen;
			}
		}
		return maxLen;
	}
	
	public static int commonStart(String str1, String str2) {
		int i = 0;
		int max = Math.min(str1.length(), str2.length());
		
		for (; i < max; i++) {
			if (str1.charAt(i) != str2.charAt(i))
				break;
		}
		return i;
	}
	
	
	public enum FilenameCloseness {error, no, litte, some, much, almostidentical};
	
	/**
	 * Computes the closeness between to file names 
	 * @param file1
	 * @param file2
	 * @return enum FilenameCloseness
	 */
	public static FilenameCloseness compareFileNames(String fName1, String fName2) {

		try {
			String output = "";
			
			int smallestLength = Math.min(fName1.length(), fName2.length());
			
			String fNameSub1 = fName1.substring(0, smallestLength);
			String fNameSub2 = fName2.substring(0, smallestLength);
			
			output += fName1 +  "  "+ fName1.length() + "\n";
			output += fName2 + "  "+ fName2.length() + "\n";
						
			int distance = StringUtil.getLevenshteinDistance(fName1, fName2);
			int subDistance = StringUtil.getLevenshteinDistance(fNameSub1, fNameSub2);
			int longestSub = StringUtil.longestSubstr(fName1, fName2);			
			int comonStart = StringUtil.commonStart(fName1, fName2);	
			
			output += "edit distance :" + distance + "\n";
			output += "sub edit dist :" + subDistance + "\n";
			output += "longest substr:" + longestSub + "\n";
			output += "comonStart    :" + comonStart + "\n\n";
						
			double editRating = (double) /*100**/distance/smallestLength;
			double subEditRating = (double) /*100**/subDistance/smallestLength;
			double subRating = (double) /*100**/longestSub/smallestLength;
			double comonStartRating = (double) comonStart/smallestLength;
			
			output += "edit rating     :" + editRating + "\n";
			output += "subedit rating  :" + subEditRating + "\n";
			output += "sub rating      :" + subRating + "\n";
			output += "comonStartRating:" + comonStartRating + "\n";
			
			if (editRating < 0.1) {
				output += "files do not pair";
				return FilenameCloseness.almostidentical;
			} else if (editRating < 0.3) { 
				
				if (subRating < 0.20) {
					output += "names have small comon substring";
				}
				else
					output += "files pair together quite good";
				
				return FilenameCloseness.much;
			}
			else if (editRating < 0.50 || subRating > 0.30) {
				output += "names almost pair together";
				return FilenameCloseness.some;
			}	
			else if (comonStartRating > 0.3) {
				output += "common start";
				return FilenameCloseness.litte;
			}
			else {
				output += "names do not pair together";
				return FilenameCloseness.no;
			}

		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
		
		return FilenameCloseness.error;
	}
	
	
	/**
	 * This method ensures that the output String has only valid XML unicode characters as specified by the
	 * XML 1.0 standard. For reference, please see the
	 * standard. This method will return an empty String if the input is null or empty.
	 *
	 * @author Donoiu Cristian, GPL
	 * @param  The String whose non-valid characters we want to remove.
	 * @return The in String, stripped of non-valid characters.
	 */
	public static String removeInvalidXMLCharacters(String s) {

		StringBuilder out = new StringBuilder();                // Used to hold the output.
		int codePoint;                                          // Used to reference the current character.

		//String ss = "\ud801\udc00";                           // This is actualy one unicode character, represented by two code units!!!.
		
		//System.out.println(ss.codePointCount(0, ss.length()));// See: 1

		int i=0;

		while(i<s.length()) {

			//System.out.println("i=" + i);

			codePoint = s.codePointAt(i);                       // This is the unicode code of the character.

			if ((codePoint == 0x9) ||          				    // Consider testing larger ranges first to improve speed. 

					(codePoint == 0xA) ||

					(codePoint == 0xD) ||

					((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||

					((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||

					((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {

				out.append(Character.toChars(codePoint));

			}
			else {
				System.out.println("removeInvalidXMLCharacters Not including:" + codePoint + " :");
				System.out.println("removeInvalidXMLCharacters input:" + s);
			}

			i+= Character.charCount(codePoint);                 // Increment with the number of code units(java chars) needed to represent a Unicode char.  

		}
		return out.toString();
	} 
	
	public static boolean isInteger(String val) {
		
		try {
			Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
}
