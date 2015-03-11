/**
 * @(#)MovieManagerCommandExportCSV.java
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

package net.sf.xmm.moviemanager.commands.importexport;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogTableExport;
import net.sf.xmm.moviemanager.models.ModelImportExportSettings;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.GUIUtil;

import org.apache.log4j.Logger;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

//DOM classes.
import org.w3c.dom.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

//JAXP 1.1
import javax.xml.parsers.*;


public class MovieManagerCommandExportXML extends MovieManagerCommandExportHandler {

	static Logger log = Logger.getLogger(MovieManagerCommandExportXML.class);
	
	Object [][] data = null;
	
	ModelImportExportSettings settings;
	DialogTableExport dialogExportTable = null;
	
	StringWriter writer = new StringWriter(100);
	
	ModelMovieInfo modelMovieInfo = new ModelMovieInfo(false, true);
	
	Object [][] tableData = null;
	
	String [] headerTitles;
	
	int titleColumnIndex = -1;	
	
	
	OutputStreamWriter outStream;
	XMLSerializer serializer;
	
	// DOM
	Document xmldoc = null;
	DocumentBuilderFactory factory;
	DocumentBuilder builder;
	DOMImplementation impl;
	Element root;
	
	// SAX
	AttributesImpl atts;
	// SAX2.0 ContentHandler.
	ContentHandler hd;
		
	MovieManagerCommandExportXML(ModelImportExportSettings exportSettings) {
		settings = exportSettings;
	}
		
	public void execute() {
		
		data = getDatabaseData();
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					dialogExportTable = new DialogTableExport(MovieManager.getDialog(), data, settings);
					GUIUtil.showAndWait(dialogExportTable, true);
				}
			});
		} catch (InterruptedException e) {
			log.error("InterruptedException:" + e.getMessage(), e);
		} catch (InvocationTargetException e) {
			log.error("InvocationTargetException:" + e.getMessage(), e);
		}
		
		if (dialogExportTable.cancelled)
			setCancelled(true);
		
		headerTitles = dialogExportTable.getHeaderTitles();
		
		
		try {
			outStream = new OutputStreamWriter(new FileOutputStream(settings.getFile()), settings.textEncoding);

			// XERCES 1 or 2 additionnal classes.
			OutputFormat of = new OutputFormat("XML",settings.textEncoding,true);
			of.setIndent(1);
			of.setIndenting(true);
			of.setDoctype(null,"users.dtd");
			serializer = new XMLSerializer(outStream, of);


			// DOM
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			impl = builder.getDOMImplementation();
			xmldoc = new DocumentImpl();
			root = xmldoc.createElement("Movies");

			// SAX
			atts = new AttributesImpl();

			hd = serializer.asContentHandler();
			hd.startDocument();
			hd.startElement("","","Movies", atts);

		} catch (UnsupportedEncodingException e) {
			log.warn("Exception:" + e.getMessage(), e);
		} catch (FileNotFoundException e) {
			log.warn("Exception:" + e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			log.warn("Exception:" + e.getMessage(), e);
		} catch (IOException e) {
			log.warn("Exception:" + e.getMessage(), e);
		} catch (SAXException e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
	}
	
		
	public void retrieveMovieList() throws Exception {
		tableData = dialogExportTable.retrieveValuesFromTable();		
		titleColumnIndex = dialogExportTable.titleColumnIndex;
	}

	public String getTitle(int i) {
				
		try {

			if (tableData == null)
				retrieveMovieList();

			if (tableData == null) {
				return null;
			}
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}
				
		if (titleColumnIndex == -1) {
			String ret = "";
			
			for (int u = 0; u < tableData[0].length; u++) {
				ret += (String) tableData[i][u] + ",  ";
			}
			return ret;
		}
		else
			return (String) tableData[i][titleColumnIndex];
	}
	
	public int getMovieListSize() {
				
		try {
			if (tableData == null)
				retrieveMovieList();

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
		}

		return tableData.length;
	}

	public void done() throws Exception {
	
		hd.endElement("","","Movies");
		hd.endDocument();
		outStream.close();
	}
	
	public void done1() throws Exception {
		
		xmldoc.appendChild(root);
		
		// As a DOM Serializer
		serializer.asDOMSerializer();
		serializer.serialize( xmldoc.getDocumentElement() );
		outStream.close();
	}
		
	
	
	public ImportExportReturn addMovie(int i) {
		
		try {
			
			System.out.println("header titles:" + headerTitles.length);
			for (int u = 0; u < headerTitles.length; u++) {
				System.out.println("headerTitles:" + headerTitles[u]);
			}
			
			String [] data = new String[tableData[0].length];
						
			for (int u = 0; u < data.length; u++) {
				data[u] = (String) tableData[i][u];
			}
						
			for (int u = 0; u < data.length; u++) {
				System.out.println("data:" + data[u]);
			}
						
			Element e = xmldoc.createElementNS(null, "Movie");
			
			int titleIndex = 0;
			
			atts.clear();
			
			for (int u = 0; u < data.length; u++) {
				
				if (headerTitles[u].equals("Title"))
					titleIndex = u;
				
				//e.setAttributeNS(null, headerTitles[u], data[u]);
				atts.addAttribute("","",headerTitles[u],"CDATA", data[u]);
			}
			
			 hd.startElement("","","Movie",atts);
			 hd.characters(headerTitles[titleIndex].toCharArray(),0,headerTitles[titleIndex].length());
			 hd.endElement("","","Movie");
			 
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
			return ImportExportReturn.error;
		}
		         
		return ImportExportReturn.success;
	}
	
	public int addMovie1(int i) {
		
		try {
			
			System.out.println("header titles:" + headerTitles.length);
			for (int u = 0; u < headerTitles.length; u++) {
				System.out.println("headerTitles:" + headerTitles[u]);
			}
			
			String [] data = new String[tableData[0].length];
						
			for (int u = 0; u < data.length; u++) {
				data[u] = (String) tableData[i][u];
			}
						
			for (int u = 0; u < data.length; u++) {
				System.out.println("data:" + data[u]);
			}
			
			//Element root = xmldoc.getDocumentElement();
			
			Element e = xmldoc.createElementNS(null, "Movie");
			
			int titleIndex = 0;
			
			for (int u = 0; u < data.length; u++) {
				
				if (headerTitles[u].equals("Title"))
					titleIndex = u;
				
				e.setAttributeNS(null, headerTitles[u], data[u]);
			}
			
			Node n = xmldoc.createTextNode(headerTitles[titleIndex]);
			
			e.appendChild(n);
			root.appendChild(e);
			
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
			return -1;
		}
		return 0;
	}
}
