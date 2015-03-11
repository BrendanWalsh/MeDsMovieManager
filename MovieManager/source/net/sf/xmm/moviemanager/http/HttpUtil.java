/**
 * @(#)HttpUtil.java 1.0 29.01.06 (dd.mm.yy)
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

public class HttpUtil {

	static Logger log = Logger.getLogger(HttpUtil.class);

	public boolean imdbAuthenticationSetUp = false;
	public boolean setUp = false;
	
	private HttpClient client = null;
	private HttpSettings httpSettings = new HttpSettings();
		
	public HttpUtil() {setup();}
		
	public HttpUtil(HttpSettings httpSettings) {
		
		if (httpSettings != null)
			this.httpSettings = httpSettings;
		
		setup();
		setProxySettings();
		
		setUpIMDbAuthentication();			
	}
	
	
	public boolean isSetup() {
		return setUp;
	}
	
	
	public void setup() {
		client = new HttpClient(new MultiThreadedHttpConnectionManager());
		client.getHttpConnectionManager().getParams().setConnectionTimeout(7000);
		setUp = true;
	}
	
	
	public boolean isIMDbAuthSetup() {
		return imdbAuthenticationSetUp;
	}
	
	public boolean setUpIMDbAuthentication() {
	
		if (httpSettings == null) {
			log.warn("Authentication could not be set. Missing authentication settings.");
			return false;
		}

		if (httpSettings.getIMDbAuthenticationEnabled()) {

			try {

				if (!isSetup())
					setup();

				client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

				PostMethod postMethod = new PostMethod(("https://secure.imdb.com/register-imdb/login")); 

				NameValuePair[] postData = new NameValuePair[2];
				postData[0] = new NameValuePair("login", httpSettings.getIMDbAuthenticationUser());
				postData[1] = new NameValuePair("password", httpSettings.getIMDbAuthenticationPassword());

				postMethod.setRequestBody(postData);
				
				int statusCode = client.executeMethod(postMethod);

				 if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) 
					 imdbAuthenticationSetUp = true;
				 else
					 imdbAuthenticationSetUp = false;
 
			} catch (Exception e) {
				log.warn("error:" + e.getMessage());
			}
		}
		else
			imdbAuthenticationSetUp = false;

		return imdbAuthenticationSetUp;
	}
	
	
	
	protected void setProxySettings() {

		if (httpSettings.getProxyEnabled()) {

			Credentials defaultcreds = null;

			if (httpSettings.getProxyAuthenticationEnabled()) {
				log.debug("user:" + httpSettings.getProxyUser());
				log.debug("pass:" + httpSettings.getProxyPassword());
				defaultcreds = new UsernamePasswordCredentials(httpSettings.getProxyUser(), httpSettings.getProxyPassword());
			}

			client.getHostConfiguration().setProxy(httpSettings.getProxyHost(), Integer.parseInt(httpSettings.getProxyPort()));
			client.getState().setProxyCredentials(new AuthScope(httpSettings.getProxyHost(), Integer.parseInt(httpSettings.getProxyPort()), AuthScope.ANY_REALM), defaultcreds);
		}
	}

	public class HTTPResult {
		
		URL url;
		StringBuffer data = null;
		StatusLine statusLine = null;
		
		HTTPResult(URL url, StringBuffer data, StatusLine statusLine) {
			this.url = url;
			this.data = data;
			this.statusLine = statusLine;
		}
		
		public URL getUrl() {
			return url;
		}
		
		public StringBuffer getData() {
			return data;
		}
		
		public int getStatusCode() {
			return statusLine.getStatusCode();
		}
		
		public String getStatusMessage() {
			return statusLine.getReasonPhrase();
		}
	}
	
	
	public HTTPResult readData1(URL url) throws Exception {
		
		if (!isSetup())
			setup();
		
		GetMethod method = new GetMethod(url.toString());
		int statusCode = client.executeMethod(method);
		
		if (statusCode != HttpStatus.SC_OK) {
			log.debug("HTTP StatusCode not HttpStatus.SC_OK:(" + statusCode + "):" + method.getStatusLine());
		}
		
		BufferedInputStream stream = new BufferedInputStream(method.getResponseBodyAsStream());

		StringBuffer data = new StringBuffer();
		
		// Saves the page data in a string buffer... 
		int buffer;

		while ((buffer = stream.read()) != -1) {
			data.append((char) buffer);
		}
		
		stream.close();
		
		return new HTTPResult(url, statusCode == HttpStatus.SC_OK ? data : null, method.getStatusLine());
	}
	

	public HTTPResult readData2(URL url) throws Exception {

		if (!isSetup())
			setup();

		GetMethod method = new GetMethod(url.toString());
		int statusCode = client.executeMethod(method);

		if (statusCode != HttpStatus.SC_OK) {
			log.debug("HTTP StatusCode not HttpStatus.SC_OK:(" + statusCode + "):" + method.getStatusLine());
		}

		java.io.BufferedReader stream = new java.io.BufferedReader(new java.io.InputStreamReader(method.getResponseBodyAsStream(), "ISO-8859-1"));

		StringBuffer data = new StringBuffer();

		// Saves the page data in a string buffer... 
		int buffer;

		while ((buffer = stream.read()) != -1) {
			data.append((char) buffer);
		}

		stream.close();

		return new HTTPResult(url, statusCode == HttpStatus.SC_OK ? data : null, method.getStatusLine());
	}

	
	public HTTPResult readData(URL url) throws TimeoutException, Exception {

		if (!isSetup())
			setup();

		GetMethod method = new GetMethod(url.toString());
		int statusCode = client.executeMethod(method);

		if (statusCode != HttpStatus.SC_OK) {
			log.debug("HTTP StatusCode not HttpStatus.SC_OK:" + method.getStatusLine());
			log.debug("For url:" + url.toString());
		}
		
		if (statusCode == HttpStatus.SC_REQUEST_TIMEOUT) {
			throw new TimeoutException();
		}

		//java.io.BufferedReader stream = new java.io.BufferedReader(new java.io.InputStreamReader(method.getResponseBodyAsStream(), "ISO-8859-1"));

		StringBuffer data = new StringBuffer();

		// Saves the page data in a string buffer... 
		String chrSet = method.getResponseCharSet();
		InputStream input = method.getResponseBodyAsStream();
		ByteArrayOutputStream temp = new ByteArrayOutputStream();
		byte [] buff = new byte [1500];
		int read;
		while ( (read = input.read(buff)) >=0) {
			temp.write(buff, 0, read);
		}
		input.close();
		temp.flush();
		buff = temp.toByteArray();
		temp.close();
		data.append(new String(buff, chrSet));

		return new HTTPResult(url, statusCode == HttpStatus.SC_OK ? data : null, method.getStatusLine());
	}
	
	
	public byte [] readDataToByteArray(URL url) throws Exception {
	
		byte[] data = null;

		if (!isSetup())
			setup();
		
		GetMethod method = new GetMethod(url.toString());
	
		try {
			int statusCode = client.executeMethod(method);
						
			if (statusCode == HttpStatus.SC_OK) {

				BufferedInputStream  inputStream = new BufferedInputStream(method.getResponseBodyAsStream());
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream(inputStream.available());

				byte [] tmpBuf = new byte[1000];
				int bytesRead;
				
				while ((bytesRead = inputStream.read(tmpBuf, 0, tmpBuf.length)) != -1) {
					byteStream.write(tmpBuf, 0, bytesRead);
				}
								
				inputStream.close();
				data = byteStream.toByteArray();
			}
			else {
				log.warn("HttpStatus statusCode:" + statusCode);
				log.warn("HttpStatus.SC_OK:" + HttpStatus.SC_OK);
			}

		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
			throw new Exception(e.getMessage());
		} finally {
			method.releaseConnection();
		}

		return data;
	}


	

	/**
	 * Decodes a html string and returns its unicode string.
	 **/
	public static String decodeHTML(String toDecode) {
		String decoded = "";

		try {
			int end = 0;
			for (int i=0; i < toDecode.length(); i++) {
				if (toDecode.charAt(i)=='&' && toDecode.charAt(i+1)=='#' && (end=toDecode.indexOf(";", i))!=-1) {
					
					// May be hex i.e. &#x27; or decimal i.e. &#32;
					
					if (toDecode.charAt(i+2)=='x')
						decoded += (char)Integer.parseInt(toDecode.substring(i+3,end), 16);
					else
						decoded += (char)Integer.parseInt(toDecode.substring(i+2,end), 10);
					
					i = end;
				} else if (toDecode.charAt(i)=='<' && toDecode.indexOf('>', i) != -1) {
					i = toDecode.indexOf('>', i);
				} else {
					decoded += toDecode.charAt(i);
				}
			}

			// replacing html code "&quot;", "&amp;" and "&nbsp;"
			decoded = decoded.replaceAll("&amp;", "&");
			decoded = decoded.replaceAll("&quot;", "\"");
			decoded = decoded.replaceAll("&nbsp;", " ");

		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
			log.debug("Input:" + toDecode);
		} 

		/* Returns the decoded string... */
		return StringUtil.removeDoubleSpace(decoded);
	}


	/**
	 * Decodes a html string 
	 **/
	public static Object [] decodeHTMLtoArray(String toDecode) {
		ArrayList<String> decoded = new ArrayList<String>();
		String tmp = "";

		try {
			int end = 0;
			for (int i=0; i < toDecode.length(); i++) {
				if (toDecode.charAt(i)=='&' && toDecode.charAt(i+1)=='#' && (end=toDecode.indexOf(";", i)) != -1) {
					tmp += (char) Integer.parseInt(toDecode.substring(i+3,end), 16);
					i = end;
				} else if (toDecode.charAt(i)=='<' && toDecode.indexOf('>', i) != -1) {
					i = toDecode.indexOf('>', i);

					if (!tmp.trim().equals(""))
						decoded.add(tmp.trim());

					tmp = "";
				} else {
					tmp += toDecode.charAt(i);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		} 
		/* Returns the decoded string... */
		return decoded.toArray();
	}

	 public static StringBuffer getHtmlNiceFormat(StringBuffer buffer) {

		 int index = 0;

//		 Format html
		 Pattern p = Pattern.compile("</.+?>");
		 Matcher m = p.matcher(buffer);

		 while (m.find(index)) {

			 index = m.start();

			 int index2 = buffer.indexOf(">", index) + 1;

			 buffer.insert(index2, SysUtil.getLineSeparator());
			 index++;
		 }
		 return buffer;
	 }
}
