/**
 * @(#)Database.java
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

package net.sf.xmm.moviemanager.database;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelAdditionalInfo;
import net.sf.xmm.moviemanager.models.ModelDatabaseSearch;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelMovie;

import org.apache.log4j.Logger;

abstract public class Database {

	Logger log = Logger.getLogger(getClass());

	SQL _sql;

	protected boolean _initialized = false;

	protected boolean setUp = false;

	protected String _path;

	protected String databaseType = ""; /* Values: "MSAccess", "HSQL" ,"MySQL" */

	protected String errorMessage = "";
	protected Exception exception = null;

	protected boolean fatalError = false;

	/* Used to return the record count. */
	private int recordCount = 0;

	/* Can be either: '\"' (doublequote) or: '`' (backtick) */
	protected String quote = "\"";

	protected String generalInfoString = "General Info";
	protected String additionalInfoString = "Additional Info";
	protected String extraInfoString = "Extra Info";

	protected String quotedGeneralInfoString = quote + generalInfoString + quote;
	protected String quotedAdditionalInfoString = quote + additionalInfoString + quote;
	protected String quotedExtraInfoString = quote + extraInfoString + quote;

	protected String generalInfoEpisodeString = "General Info Episodes";
	protected String additionalInfoEpisodeString = "Additional Info Episodes";
	protected String extraInfoEpisodeString = "Extra Info Episodes";

	protected String quotedGeneralInfoEpisodeString = quote + generalInfoEpisodeString + quote;
	protected String quotedAdditionalInfoEpisodeString = quote + additionalInfoEpisodeString + quote;
	protected String quotedExtraInfoEpisodeString = quote + extraInfoEpisodeString + quote;

	protected String quotedListsString = quote + "Lists" + quote;

	String directedByString = "Directed By";
	String quotedDirectedByString = quote + directedByString + quote;

	String writtenByString = "Written By";
	String quotedWrittenByString = quote + writtenByString + quote;

	String soundMixString = "Sound Mix";
	String quotedSoundMixString = quote + soundMixString + quote;

	String webRuntimeString = "Web Runtime";
	String quotedWebRuntimeString = quote + webRuntimeString + quote;

	ArrayList<String> extraInfoFieldNames = null;

	String coversFolder = null;
	
	// Used when finding which lists a movie is memeber of.
	public static final String listsAliasPrefix = "lists_";
	
	/**
	 * The constructor. Initialized _sql;
	 *
	 * filePath should contain the file path for the database.
	 **/
	protected Database(String filePath) {
		_path = filePath;

		if (!MovieManager.getConfig().getInternalConfig().getSensitivePrintMode())
			log.debug("Debug - filePath:" + filePath);
	}

	public boolean isSetUp() {
		return setUp;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public Exception getException() {
		return exception;
	}

	public void resetError() {
		errorMessage = "";
		exception = null;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public boolean isMySQL() {
		return databaseType.equals("MySQL");
	}

	public boolean isHSQL() {
		return databaseType.equals("HSQL");
	}

	public boolean isMSAccess() {
		return databaseType.equals("MSAccess");
	}

	/**
	 * SetUp...
	 **/
	public boolean setUp() {

		try {
			if (!_initialized) {
				_sql.setUp();
				_initialized = true;
				setUp = true;
			}
		} catch (Exception e) {
			//log.error("Exception: ", e);
			log.error("Exception: " + e.getMessage());
			checkErrorMessage(e);
			_initialized = false;
			fatalError = true;
		}

		return _initialized;
	}


	/**
	 * Finalize...
	 **/
	public void finalizeDatabase() {

		try {
			_sql.finalize();
			_initialized = false;
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		}
	}


	/**
	 * Returns _initialized...
	 **/
	public boolean isInitialized() {
		return _initialized;
	}


	public boolean getFatalError() {
		return fatalError;
	}

	/**
	 * Returns the path...
	 **/
	public String getPath() {
		return _path;
	}


	/**
	 * Returns the recordCount...
	 **/
	public int getRecordCount() {
		return recordCount;
	}


	/**
	 * Not really in use
	 **/
	public synchronized String getSQLReservedKeywords() {

		DatabaseMetaData metaData = _sql.getMetaData();

		String sqlKeywords = "";

		if (metaData == null)
			return "";

		try {
			sqlKeywords = metaData.getSQLKeywords();
		}
		catch (SQLException e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		}
		return sqlKeywords;
	}


	/**
	 * Returns the result of the query (formated) in a string.
	 **/
	public synchronized String getQueryResult(String query) {

		//long t1 = System.currentTimeMillis();
		String queryResult = "";
		StringBuffer data = new StringBuffer(10000);
		String tempData = "";

		recordCount = 0;

		try {
			ResultSet resultSet = _sql.executeQueryForwardOnly(query);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			ArrayList<String> names = new ArrayList<String>();

			data.append("  |-----\n");

			for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
				names.add(resultSetMetaData.getColumnName(i));
			}

			if (resultSet.next()) {
				do {
					recordCount++;

					for (int i=0; i<names.size(); i++) {
						data.append("  |   ");
						data.append(names.get(i));
						data.append(": ");

						if ((tempData = resultSet.getString(i+1)) != null)
							data.append(tempData);
						data.append("\n");
					}
					data.append("  |-----\n");
				} while (resultSet.next());
			} else {
				for (int i=0; i<names.size(); i++) {
					data.append("  |   ");
					data.append((String)names.get(i));
					data.append(": \n");
				}
				data.append("  |-----\n");
			}

			queryResult = data.toString();

		} catch (Exception e) {
			log.error("Exception: " + e.getMessage());
			queryResult = e.getMessage();
			recordCount = 0;
		} finally {

			/* Clears the Statement in the database... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: "+ e.getMessage());
			}
		}
		/* Returns the data... */
		return queryResult;
	}


	/**
     /**
	 * Returns the number of rows in the General Info id column
	 **/
	public synchronized int getDatabaseSize() {

		int size = -1;

		try {
			/* Gets the number of rows */
			ResultSet resultSet = _sql.executeQuery("SELECT COUNT(*) FROM (SELECT id FROM \"General Info\") "+
			";");

			if (resultSet.next())
				size = resultSet.getInt(1);

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
			size = -1;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		return size;
	}


	void checkErrorMessage(Exception e) {

		String message =  e.getMessage();
		errorMessage = message;

		exception = e;

		if (message == null) {
			errorMessage = "";
			return;
		}

		if (message.indexOf("Access denied") != -1) {
			errorMessage = message;
			MovieManager.getDatabaseHandler().processDatabaseError(this);
		} 
		else if (message.indexOf("Connection refused: connect") != -1) {
			errorMessage = "Connection refused: connect";
			// This error happens only on connect and is checked for when connecting
		//	MovieManager.getIt().processDatabaseError(this);
		}
		else if (message.indexOf("Connection reset") != -1) {
			errorMessage = "Connection reset";
			MovieManager.getDatabaseHandler().processDatabaseError(this);
		}
		//    	Software caused connection abort: recv failed
		else if (message.indexOf("recv failed") != -1) {
			errorMessage = "Connection closed";
			MovieManager.getDatabaseHandler().processDatabaseError(this);
		}
		//    	Software caused connection abort: socket write error
		else if (message.indexOf("socket write error") != -1) {
			errorMessage = "Socket Write Error";
			MovieManager.getDatabaseHandler().processDatabaseError(this);
		}
		else if (message.indexOf("Server shutdown in progress") != -1) {
			errorMessage = "Server shutdown in progress";
			MovieManager.getDatabaseHandler().processDatabaseError(this);
		}
		else if (message.indexOf("Connection timed out: connect") != -1) {
			errorMessage = "Connection timed out: connect";
			MovieManager.getDatabaseHandler().processDatabaseError(this);
		}
		else if (message.indexOf("UnknownHostException") != -1) {
			errorMessage = "UnknownHostException";
			MovieManager.getDatabaseHandler().processDatabaseError(this);
		}
		else if (message.indexOf("Communications link failure") != -1) {
			errorMessage = "Communications link failure";
			MovieManager.getDatabaseHandler().processDatabaseError(this);
		}
		else if ((message.indexOf("is full") != -1) || (message.indexOf("Error writing file") != -1)) {
			errorMessage = "MySQL server is out of space";
			MovieManager.getDatabaseHandler().processDatabaseError(this);
		}
		else if ((message.indexOf("Data truncation: Data too long for column 'CoverData'") != -1)) {
			errorMessage = "Data truncation cover";
		}
		else if ((message.indexOf("You have an error in your SQL syntax") != -1)) {
			errorMessage = "SQL Syntax error";
		}
		else if (message.indexOf("settings' doesn't exist") != -1) {
			errorMessage = message;
			fatalError = true;
		}
		else if (!fatalError) {
			message = "";
		}
		else if (!message.equals(""))
			MovieManager.getDatabaseHandler().processDatabaseError(this);
		
	}


	/**
	 * Returns the String result of the query
	 */
	protected String getString(String query, String field) {

		String data = "";
		try {
			
			ResultSet resultSet = _sql.executeQuery(query);
			
			if (resultSet.next() && resultSet.getString(field) != null) {
				data = resultSet.getString(field);
			}
		} catch (Exception e) {
			log.error("Query failed: " + query, e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the data... */
		return data;
	}

/*	private int getInt(String query, String field) {

		int data = -1;
		try {
			ResultSet resultSet = _sql.executeQuery(query);

			if (resultSet.next() && resultSet.getString(field) != null) {
				data = resultSet.getInt(field);
			}

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
	
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
	
		return data;
	}
*/


	/**
	 * Get the boolean result of the query
	 **/
	protected boolean getBoolean(String query, String field) {

		boolean data = false;
		try {
			ResultSet resultSet = _sql.executeQuery(query);

			if (resultSet.next() && resultSet.getString(field) != null) {
				data = resultSet.getBoolean(field);
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the data... */
		return data;
	}


	
/*	private double getDouble(String query, String field) {

		double data = -1;
		try {
			ResultSet resultSet = _sql.executeQuery(query);

			if (resultSet.next() && resultSet.getString(field) != null) {
				data = resultSet.getDouble(field);
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
		
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
	
		return data;
	}
*/


	/**
	 * Returns the active additional info fields.
	 **/
	public synchronized int [] getActiveAdditionalInfoFields() {

		String data = "";
		String columnName = "Active Additional Info Fields";
		String quote = "\"";

		if (this instanceof DatabaseMySQL) {
			columnName = "Active_Additional_Info_Fields";
			quote = "";
		}

		try {
			ResultSet resultSet = _sql.executeQuery("SELECT " +quote+ "Settings" +quote+ "." +quote+ columnName +quote+ " "+
					"FROM " +quote+ "Settings" +quote+ " "+
					"WHERE " +quote+ "Settings" +quote+ ".id=1;");

			if (resultSet.next() && resultSet.getString(columnName) != null) {
				data = resultSet.getString(columnName);
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		StringTokenizer tokenizer = new StringTokenizer(data, ":");

		ArrayList<String> extraFields = getExtraInfoFieldNames();

		int fieldCount = ModelAdditionalInfo.additionalInfoFieldCount() + extraFields.size();

		ArrayList<Integer> activeFields = new ArrayList<Integer>(20);

		/* Nothing saved to 'active additional info fields' */
		if (tokenizer.countTokens() == 0) {

			for (int i = 0; i < fieldCount; i++) {
				activeFields.add(new Integer(i));
			}
		}
		else {
			try {
				
				int tmp;

				while (tokenizer.hasMoreTokens()) {
					tmp = Integer.parseInt(tokenizer.nextToken());

					if ((tmp >= fieldCount) || (tmp < 0))
						log.error("Index:" + tmp + " ignored, value is invalid: " + tmp);
					else {
						activeFields.add(new Integer(tmp));
					}
				}
			} catch (NumberFormatException n) {
				log.error("NumberFormatException: Invalid format in active additional info fields");
			}
		}

		int [] toReturn = new int[activeFields.size()];

		for (int i = 0; i < toReturn.length; i++)
			toReturn[i] = ((Integer) activeFields.get(i)).intValue();

		/* Returns the data... */
		return toReturn;
	}


	/**
	 * Returns the additional info result set...
	 * _sql.clear() is not performed and is the responsibility of the
	 * method calling this.
	 **/
	protected ResultSet getAdditionalInfoMovieResultSet(int index) {

		ResultSet resultSet = null;

		try {
			/* Gets the fixed additional info... */
			resultSet = _sql.executeQuery("SELECT \"Additional Info\".* "+
					"FROM \"Additional Info\" "+
					"WHERE \"Additional Info\".\"ID\"="+index+";");

		} catch (Exception e) {
			log.error("Exception: " + e);
			checkErrorMessage(e);
		}

		/* Returns the data... */
		return resultSet;
	}

	/**
	 * Returns the additional_info with index index...
	 * _sql.clear() is not performed and is the responsibility of the
	 * method calling this.
	 **/
	protected ResultSet getAdditionalInfoEpisodeResultSet(int index) {

		ResultSet resultSet = null;

		try {
			/* Gets the fixed additional info... */
			resultSet = _sql.executeQuery("SELECT \"Additional Info Episodes\".* "+
					"FROM \"Additional Info Episodes\" "+
					"WHERE \"Additional Info Episodes\".\"ID\"="+index+";");

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		}

		/* Returns the data... */
		return resultSet;
	}

	/**
	 * Returns the additional_info with index index...
	 * _sql.clear() is not performed and is the responsibility of the
	 * method calling this.
	 **/
	protected ResultSet getExtraInfoMovieResultSet(int index) {

		ResultSet resultSet = null;

		try {
			/* Gets the fixed additional info... */
			resultSet = _sql.executeQuery("SELECT \"Extra Info\".* "+
					"FROM \"Extra Info\" "+
					"WHERE \"Extra Info\".\"ID\"="+index+";");
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		}

		/* Returns the data... */
		return resultSet;
	}

	/**
	 * Returns the additional_info with index index...
	 * _sql.clear() is not performed and is the responsibility of the
	 * method calling this.
	 **/
	protected ResultSet getExtraInfoEpisodeResultSet(int index) {

		ResultSet resultSet = null;

		try {
			/* Gets the fixed additional info... */
			resultSet = _sql.executeQuery("SELECT \"Extra Info Episodes\".* "+
					"FROM \"Extra Info Episodes\" "+
					"WHERE \"Extra Info Episodes\".\"ID\"="+index+";");
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		}

		/* Returns the data... */
		return resultSet;
	}




	/**
	 * Returns a ModelAdditionalInfo on a specific movie/episode
	 **/
	public synchronized ModelAdditionalInfo getAdditionalInfo(int index, boolean episode) {

		ModelAdditionalInfo additionalInfo = null;

		String subtitles = "";
		int duration = 0;
		int fileSize = 0;
		int cDs = 0;
		double cDCases = 0;
		String resolution = "";
		String videoCodec = "";
		String videoRate = "";
		String videoBitrate = "";
		String audioCodec = "";
		String audioRate ="";
		String audioBitrate = "";
		String audioChannels = "";
		String fileLocation = "";
		int fileCount = 0;
		String container = "";
		String mediaType = "";

		try {

			ResultSet resultSet;

			if (episode)
				resultSet = getAdditionalInfoEpisodeResultSet(index);
			else
				resultSet = getAdditionalInfoMovieResultSet(index);

			/* Processes the result set till the end... */
			if (resultSet.next()) {

				if ((subtitles = resultSet.getString("Subtitles")) == null)
					subtitles = "";

				duration = resultSet.getInt("Duration");
				fileSize = resultSet.getInt("File Size");
				cDs = resultSet.getInt("CDs");
				cDCases = resultSet.getInt("CD Cases");

				if ((resolution = resultSet.getString("Resolution")) == null)
					resolution = "";

				if ((videoCodec = resultSet.getString("Video Codec")) == null)
					videoCodec = "";

				if ((videoRate = resultSet.getString("Video Rate")) == null)
					videoRate = "";

				if ((videoBitrate = resultSet.getString("Video Bit Rate")) == null)
					videoBitrate = "";

				if ((audioCodec = resultSet.getString("Audio Codec")) == null)
					audioCodec = "";

				if ((audioRate = resultSet.getString("Audio Rate")) == null)
					audioRate = "";

				if ((audioBitrate = resultSet.getString("Audio Bit Rate")) == null)
					audioBitrate = "";

				audioChannels = resultSet.getString("Audio Channels");

				if ((fileLocation = resultSet.getString("File Location")) == null)
					fileLocation = "";

				fileCount = resultSet.getInt("File Count");

				if ((container = resultSet.getString("Container")) == null)
					container = "";

				if ((mediaType = resultSet.getString("Media Type")) == null)
					mediaType = "";



				/* Getting extra info fields */

				_sql.clear();

				ArrayList<String> extraInfoFieldNames = getExtraInfoFieldNames(false);
				ArrayList<String> extraInfoFieldValues = new ArrayList<String>();

				if (episode)
					resultSet = getExtraInfoEpisodeResultSet(index);
				else
					resultSet = getExtraInfoMovieResultSet(index);

				String tempValue;

				//boolean next = resultSet.next();
				boolean next = resultSet.first();

				// If false, no rows with the expected ID exists
				// Caused by a serious bug in v2.5 beta 4 where extra info values weren't added
				// if the values were empty (and no fields existed).

				if (next == false) {
					log.debug("Updating the extra info table with missing extra info row, ID=" + index);

					for (int i = 0; i < extraInfoFieldNames.size(); i++)
						extraInfoFieldValues.add("");

					if (episode)
						addExtraInfoEpisode(index, extraInfoFieldNames, extraInfoFieldValues);
					else
						addExtraInfoMovie(index, extraInfoFieldNames, extraInfoFieldValues);

					extraInfoFieldValues.clear();
				}


				/* Getting the value for each field */
				for (int i = 0; next && i < extraInfoFieldNames.size(); i++) {

					/* First column after the ID column is at index 2 */
					tempValue = resultSet.getString(i+2);

					if (tempValue == null)
						tempValue = "";

					extraInfoFieldValues.add(tempValue);
				}

				additionalInfo = new ModelAdditionalInfo(subtitles, duration, fileSize, cDs, cDCases, resolution, videoCodec, videoRate, videoBitrate, audioCodec, audioRate, audioBitrate, audioChannels, fileLocation, fileCount, container, mediaType);
				additionalInfo.setExtraInfoFieldValues(extraInfoFieldValues);
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}

		/* Returns the list model... */
		return additionalInfo;
	}


	public synchronized boolean listColumnExist(String columnName) {

		ArrayList<String> columnNames = getListsColumnNames();

		while (!columnNames.isEmpty()) {
			if (columnNames.get(0).equals(columnName))
				return true;
			columnNames.remove(0);
		}
		return false;
	}


	ArrayList<String> listsColumnNames = null;
	
	/**
	 * Returns the names of the columns in the lists table.
	 **/
	public synchronized ArrayList<String> getListsColumnNames() {
		
		if (listsColumnNames != null)
			return new ArrayList<String>(listsColumnNames);
		
		listsColumnNames = new ArrayList<String>();
		try {
			ResultSetMetaData metaData = _sql.executeQuery("SELECT "+ quote + "Lists" +quote + ".* FROM "+ quote + "Lists"+ quote + " WHERE 1=0;").getMetaData();

			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				if (!metaData.getColumnName(i).equalsIgnoreCase("ID")) {
					listsColumnNames.add(metaData.getColumnName(i));
				}
			}

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: ", e);
			}
		}
		/* Returns the list model... */
		return new ArrayList<String>(listsColumnNames);
	}

	public synchronized int getExtraInfoColumnCount() {

		if (extraInfoFieldNames == null)
			getExtraInfoFieldNames(true);

		return extraInfoFieldNames.size();
	}

	public synchronized ArrayList<String> getExtraInfoFieldNames(boolean fromDatabase) {
	
		if (fromDatabase || extraInfoFieldNames == null) {
			extraInfoFieldNames = getExtraInfoFieldNames();
		}
		return new ArrayList<String>(extraInfoFieldNames);
	}

	/**
	 * Returns the Extra Info field names in a ArrayList.
	 **/
	private synchronized ArrayList<String> getExtraInfoFieldNames() {

		ArrayList<String> list = new ArrayList<String>();

		try {
			String query = "SELECT " + quotedExtraInfoString +".* "+ "FROM "+ quotedExtraInfoString +" WHERE 1=0;";

			ResultSetMetaData metaData = _sql.executeQuery(query).getMetaData();

			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				if (!metaData.getColumnName(i).equalsIgnoreCase("ID")) {
					list.add(metaData.getColumnName(i));
				}
			}

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
			/* Returns the list model... */
		}
		return list;
	}


	/**
	 * Returns the Extra Info field names in a ArrayList.
	 **/
	public synchronized ArrayList<String> getExtraInfoEpisodesFieldNames() {

		ArrayList<String> list = new ArrayList<String>();
		try {
			String query = "SELECT " + quotedExtraInfoEpisodeString +".* "+ "FROM "+ quotedExtraInfoEpisodeString +" WHERE 1=0;";

			ResultSetMetaData metaData = _sql.executeQuery(query).getMetaData();

			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				if (!metaData.getColumnName(i).equalsIgnoreCase("ID")) {
					list.add(metaData.getColumnName(i));
				}
			}

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
			/* Returns the list model... */
		}
		return list;
	}


	/**
	 * Returns the additional info field names in a ArrayList.
	 **/
	public synchronized ArrayList<String> getAdditionalInfoFieldNames() {
		ArrayList<String> list = new ArrayList<String>();
		String query = "SELECT " + quotedAdditionalInfoString + ".* "+ "FROM "+ quotedAdditionalInfoString +" WHERE 1=0;";

		try {
			ResultSetMetaData metaData = _sql.executeQuery(query).getMetaData();

			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				if(!metaData.getColumnName(i).equalsIgnoreCase("ID")) {
					list.add(metaData.getColumnName(i));
				}
			}

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
			/* Returns the list model... */

		}
		return list;
	}


	/**
	 * Returns the additional info field names in a ArrayList.
	 **/
	public synchronized ArrayList<String> getGeneralInfoMovieFieldNames() {

		ArrayList<String> list = new ArrayList<String>();
		String query = "SELECT " + quotedGeneralInfoString +".* "+ "FROM "+ quotedGeneralInfoString +" WHERE 1=0;";

		try {
			ResultSet rs = _sql.executeQuery(query);
			ResultSetMetaData metaData = rs.getMetaData();

			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				if (!metaData.getColumnName(i).equalsIgnoreCase("ID")) {
					list.add(metaData.getColumnName(i));
				}
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
			/* Returns the list model... */

		}
		return list;
	}


	/**
	 * Returns the table names.
	 **/
	public synchronized ArrayList<String> getTableNames() {
		ArrayList<String> list = new ArrayList<String>();
		try {

			String[] tableTypes = { "TABLE" };
			DatabaseMetaData dbMetadata = _sql.getMetaData();

			ResultSet allTables = dbMetadata.getTables(null, null, null, tableTypes);

			while (allTables.next())
				list.add(allTables.getString("TABLE_NAME"));

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		return list;
	}


	/**
	 * Adds the fields of movie with 'index' to the \"Additional Info\" table and
	 * returns the number of updated rows.
	 **/
	public synchronized int addAdditionalInfo(ModelAdditionalInfo model) {
		return addAdditionalInfo(model.getKey(), model);
	}


	/**
	 * Adds the fields of movie with 'index' to the \"Additional Info\" table and
	 * returns the number of updated rows.
	 **/
	public synchronized int addAdditionalInfo(int index, ModelAdditionalInfo model) {
		int value = 0;

		try {

			PreparedStatement statement;

			if (isMySQL()) {
				statement = _sql.prepareStatement("INSERT INTO Additional_Info "+
						"(ID,Subtitles,Duration,File_Size,CDs,CD_Cases,"+
						"Resolution,Video_Codec,Video_Rate,"+
						"Video_Bit_Rate,Audio_Codec,Audio_Rate,"+
						"Audio_Bit_Rate,Audio_Channels,File_Location,"+
						"File_Count,Container, Media_Type) "+
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
			}
			else {
				statement = _sql.prepareStatement("INSERT INTO \"Additional Info\" "+
						"(\"ID\",\"Subtitles\",\"Duration\",\"File Size\",\"CDs\",\"CD Cases\","+
						"\"Resolution\",\"Video Codec\",\"Video Rate\","+
						"\"Video Bit Rate\",\"Audio Codec\",\"Audio Rate\","+
						"\"Audio Bit Rate\",\"Audio Channels\",\"File Location\","+
						"\"File Count\",\"Container\", \"Media Type\") "+
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
			}

			statement.setInt(1, index);
			statement.setString(2, model.getSubtitles());

			if (model.getDuration() != -1) {
				statement.setInt(3, model.getDuration());
			} else {
				statement.setNull(3, Types.INTEGER);
			}
			if (model.getFileSize() != -1) {
				statement.setInt(4, model.getFileSize());
			} else {
				statement.setNull(4, Types.INTEGER);
			}
			if (model.getCDs() != -1) {
				statement.setInt(5, model.getCDs());
			} else {
				statement.setNull(5, Types.INTEGER);
			}
			if (model.getCDCases() != -1) {
				statement.setDouble(6, model.getCDCases());
			} else {
				statement.setNull(6, Types.DOUBLE);
			}
			statement.setString(7, model.getResolution());
			statement.setString(8, model.getVideoCodec());
			statement.setString(9, model.getVideoRate());
			statement.setString(10, model.getVideoBitrate());
			statement.setString(11, model.getAudioCodec());
			statement.setString(12, model.getAudioRate());
			statement.setString(13, model.getAudioBitrate());
			statement.setString(14, model.getAudioChannels());
			statement.setString(15, model.getFileLocation());

			if (model.getFileCount() != -1) {
				statement.setDouble(16, model.getFileCount());
			} else {
				statement.setNull(16, Types.DOUBLE);
			}
			statement.setString(17, model.getContainer());
			statement.setString(18, model.getMediaType());

			value = statement.executeUpdate();

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return value;
	}


	public synchronized int addAdditionalInfoEpisode(ModelAdditionalInfo model) {
		return addAdditionalInfoEpisode(model.getKey(), model);
	}


	/**
	 * Adds the fields of movie with 'index' to the additional info table and
	 * returns the number of updated rows.
	 **/
	public synchronized int addAdditionalInfoEpisode(int index, ModelAdditionalInfo model) {
		int value = 0;

		try {
			PreparedStatement statement;

			if (isMySQL()) {
				statement = _sql.prepareStatement("INSERT INTO Additional_Info_Episodes "+
						"(ID, Subtitles,Duration,File_Size,CDs, "+
						"CD_Cases,Resolution,Video_Codec,"+
						"Video_Rate,Video_Bit_Rate,Audio_Codec, "+
						"Audio_Rate,Audio_Bit_Rate,Audio_Channels, "+
						"File_Location, File_Count, Container, Media_Type) "+
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
			}
			else {
				statement = _sql.prepareStatement("INSERT INTO \"Additional Info Episodes\" "+
						"(\"ID\", \"Subtitles\",\"Duration\",\"File Size\",\"CDs\", "+
						"\"CD Cases\",\"Resolution\",\"Video Codec\","+
						"\"Video Rate\",\"Video Bit Rate\",\"Audio Codec\", "+
						"\"Audio Rate\",\"Audio Bit Rate\",\"Audio Channels\", "+
						"\"File Location\", \"File Count\", \"Container\", \"Media Type\") "+
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
			}

			statement.setInt(1, index);
			statement.setString(2, model.getSubtitles());

			if (model.getDuration() != -1)
				statement.setInt(3, model.getDuration());
			else
				statement.setNull(3, Types.INTEGER);

			if (model.getFileSize() != -1)
				statement.setInt(4, model.getFileSize());
			else
				statement.setNull(4, Types.INTEGER);

			if (model.getCDs() != -1)
				statement.setInt(5, model.getCDs());
			else
				statement.setNull(5, Types.INTEGER);

			if (model.getCDCases() != -1)
				statement.setDouble(6, model.getCDCases());
			else
				statement.setNull(6, Types.DOUBLE);

			statement.setString(7, model.getResolution());
			statement.setString(8, model.getVideoCodec());
			statement.setString(9, model.getVideoRate());
			statement.setString(10, model.getVideoBitrate());
			statement.setString(11, model.getAudioCodec());
			statement.setString(12, model.getAudioRate());
			statement.setString(13, model.getAudioBitrate());
			statement.setString(14, model.getAudioChannels());
			statement.setString(15, model.getFileLocation());

			if (model.getFileCount() != -1)
				statement.setInt(16, model.getFileCount());
			else
				statement.setNull(16, Types.INTEGER);

			statement.setString(17, model.getContainer());
			statement.setString(18, model.getMediaType());

			value = statement.executeUpdate();

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return value;
	}




	/**
	 * Sets the fields of movie index to the \"Additional Info\" table and returns the number of
	 * updated rows.
	 **/
	public synchronized int setAdditionalInfo(int index, ModelAdditionalInfo model) {
		int value = 0;

		try {

			PreparedStatement statement;

			if (isMySQL()) {

				statement = _sql.prepareStatement("UPDATE Additional_Info "+
						"SET Additional_Info.Subtitles=?, "+
						"Additional_Info.Duration=?, "+
						"Additional_Info.File_Size=?, "+
						"Additional_Info.CDs=?, "+
						"Additional_Info.CD_Cases=?, "+
						"Additional_Info.Resolution=?, "+
						"Additional_Info.Video_Codec=?, "+
						"Additional_Info.Video_Rate=?, "+
						"Additional_Info.Video_Bit_Rate=?, "+
						"Additional_Info.Audio_Codec=?, "+
						"Additional_Info.Audio_Rate=?, "+
						"Additional_Info.Audio_Bit_Rate=?, "+
						"Additional_Info.Audio_Channels=?, "+
						"Additional_Info.File_Location=?, "+
						"Additional_Info.File_Count=?, "+
						"Additional_Info.Container=?, "+
						"Additional_Info.Media_Type=? "+
				"WHERE Additional_Info.ID=?;");

			}
			else {
				statement = _sql.prepareStatement("UPDATE \"Additional Info\" "+
						"SET \"Additional Info\".\"Subtitles\"=?, "+
						"\"Additional Info\".\"Duration\"=?, "+
						"\"Additional Info\".\"File Size\"=?, "+
						"\"Additional Info\".\"CDs\"=?, "+
						"\"Additional Info\".\"CD Cases\"=?, "+
						"\"Additional Info\".\"Resolution\"=?, "+
						"\"Additional Info\".\"Video Codec\"=?, "+
						"\"Additional Info\".\"Video Rate\"=?, "+
						"\"Additional Info\".\"Video Bit Rate\"=?, "+
						"\"Additional Info\".\"Audio Codec\"=?, "+
						"\"Additional Info\".\"Audio Rate\"=?, "+
						"\"Additional Info\".\"Audio Bit Rate\"=?, "+
						"\"Additional Info\".\"Audio Channels\"=?, "+
						"\"Additional Info\".\"File Location\"=?, "+
						"\"Additional Info\".\"File Count\"=?, "+
						"\"Additional Info\".\"Container\"=?, "+
						"\"Additional Info\".\"Media Type\"=? "+
				"WHERE \"Additional Info\".\"ID\"=?;");
			}

			statement.setString(1, model.getSubtitles());

			if (model.getDuration() != -1) {
				statement.setInt(2, model.getDuration());
			} else {
				statement.setNull(2, Types.INTEGER);
			}
			if (model.getFileSize() != -1) {
				statement.setInt(3, model.getFileSize());
			} else {
				statement.setNull(3, Types.INTEGER);
			}
			if (model.getCDs() != -1) {
				statement.setInt(4, model.getCDs());
			} else {
				statement.setNull(4, Types.INTEGER);
			}
			if (model.getCDCases() != -1) {
				statement.setDouble(5, model.getCDCases());
			} else {
				statement.setNull(5, Types.DOUBLE);
			}
			statement.setString(6, model.getResolution());
			statement.setString(7, model.getVideoCodec());
			statement.setString(8, model.getVideoRate());
			statement.setString(9, model.getVideoBitrate());
			statement.setString(10, model.getAudioCodec());
			statement.setString(11, model.getAudioRate());
			statement.setString(12, model.getAudioBitrate());
			statement.setString(13, model.getAudioChannels());
			statement.setString(14, model.getFileLocation());

			if (model.getFileCount() != -1) {
				statement.setDouble(15, model.getFileCount());
			} else {
				statement.setNull(15, Types.INTEGER);
			}
			statement.setString(16, model.getContainer());
			statement.setString(17, model.getMediaType());
			statement.setInt(18, index);

			value = statement.executeUpdate();

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return value;
	}


	/**
	 * Sets the fields of movie index to the \"Additional Info\" table and returns the number of
	 * updated rows.
	 **/
	public synchronized int setAdditionalInfoEpisode(int index, ModelAdditionalInfo model) {
		int value = 0;

		try {
			PreparedStatement statement;

			if (isMySQL()) {
				statement = _sql.prepareStatement("UPDATE Additional_Info_Episodes "+
						"SET Additional_Info_Episodes.Subtitles=?, "+
						"Additional_Info_Episodes.Duration=?, "+
						"Additional_Info_Episodes.File_Size=?, "+
						"Additional_Info_Episodes.CDs=?, "+
						"Additional_Info_Episodes.CD_Cases=?, "+
						"Additional_Info_Episodes.Resolution=?, "+
						"Additional_Info_Episodes.Video_Codec=?, "+
						"Additional_Info_Episodes.Video_Rate=?, "+
						"Additional_Info_Episodes.Video_Bit_Rate=?, "+
						"Additional_Info_Episodes.Audio_Codec=?, "+
						"Additional_Info_Episodes.Audio_Rate=?, "+
						"Additional_Info_Episodes.Audio_Bit_Rate=?, "+
						"Additional_Info_Episodes.Audio_Channels=?, "+
						"Additional_Info_Episodes.File_Location=?, "+
						"Additional_Info_Episodes.File_Count=?, "+
						"Additional_Info_Episodes.Container=?, "+
						"Additional_Info_Episodes.Media_Type=? "+
				"WHERE Additional_Info_Episodes.ID=?;");
			}
			else {
				statement = _sql.prepareStatement("UPDATE \"Additional Info Episodes\" "+
						"SET \"Additional Info Episodes\".\"Subtitles\"=?, "+
						"\"Additional Info Episodes\".\"Duration\"=?, "+
						"\"Additional Info Episodes\".\"File Size\"=?, "+
						"\"Additional Info Episodes\".\"CDs\"=?, "+
						"\"Additional Info Episodes\".\"CD Cases\"=?, "+
						"\"Additional Info Episodes\".\"Resolution\"=?, "+
						"\"Additional Info Episodes\".\"Video Codec\"=?, "+
						"\"Additional Info Episodes\".\"Video Rate\"=?, "+
						"\"Additional Info Episodes\".\"Video Bit Rate\"=?, "+
						"\"Additional Info Episodes\".\"Audio Codec\"=?, "+
						"\"Additional Info Episodes\".\"Audio Rate\"=?, "+
						"\"Additional Info Episodes\".\"Audio Bit Rate\"=?, "+
						"\"Additional Info Episodes\".\"Audio Channels\"=?, "+
						"\"Additional Info Episodes\".\"File Location\"=?, "+
						"\"Additional Info Episodes\".\"File Count\"=?, "+
						"\"Additional Info Episodes\".\"Container\"=?, "+
						"\"Additional Info Episodes\".\"Media Type\"=? "+
				"WHERE \"Additional Info Episodes\".\"ID\"=?;");
			}

			statement.setString(1,  model.getSubtitles());

			if (model.getDuration() != -1) {
				statement.setInt(2, model.getDuration());
			} else {
				statement.setNull(2, Types.INTEGER);
			}
			if (model.getFileSize() != -1) {
				statement.setInt(3, model.getFileSize());
			} else {
				statement.setNull(3, Types.INTEGER);
			}
			if (model.getCDs() != -1) {
				statement.setInt(4, model.getCDs());
			} else {
				statement.setNull(4, Types.INTEGER);
			}
			if (model.getCDCases() != -1) {
				statement.setDouble(5,model.getCDCases());
			} else {
				statement.setNull(5, Types.DOUBLE);
			}
			statement.setString(6, model.getResolution());
			statement.setString(7, model.getVideoCodec());
			statement.setString(8, model.getVideoRate());
			statement.setString(9, model.getVideoBitrate());
			statement.setString(10, model.getAudioCodec());
			statement.setString(11, model.getAudioRate());
			statement.setString(12, model.getAudioBitrate());
			statement.setString(13, model.getAudioChannels());
			statement.setString(14, model.getFileLocation());

			if (model.getFileCount() != -1) {
				statement.setDouble(15, model.getFileCount());
			} else {
				statement.setNull(15, Types.INTEGER);
			}
			statement.setString(16, model.getContainer());
			statement.setString(17, model.getMediaType());
			statement.setInt(18, index);

			value = statement.executeUpdate();

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return value;
	}




	/**
	 * Removes the movie from the database and returns number of updated rows.
	 **/
	public abstract int removeMovie(int index);

	public abstract int removeEpisode(int index);

	/**
	 * Deletes the database files.
	 **/
	public abstract void deleteDatabase();

	/**
	 * Checks if the current database is outdated.
	 **/
	public abstract boolean isDatabaseOld();

	/**
	 * Updates the database. Returns 1 if successfull.
	 **/
	public abstract int makeDatabaseUpToDate();

	
	/**
	 * Used when converting database to keep the same ID values
	 * Adds the fields to the general info table and returns the index added
	 **/
	protected synchronized int addGeneralInfo(int index, String title, String cover, byte [] coverData, String IMDB,
			String date, String directedBy, String writtenBy,
			String genre, String rating, String personalRating, boolean seen, String aka,
			String country, String language, String colour,
			String plot, String cast, String notes, String certification,
			String mpaa, String webSoundMix, String webRuntime, String awards) {

		try {

			_sql.clear();

			/* Adds the info... */
			if (index != -1) {

				PreparedStatement statement;
				statement = _sql.prepareStatement("INSERT INTO \"General Info\" "+
						"(\"ID\",\"Title\",\"Cover\",\"Imdb\",\"Date\",\"Directed By\",\"Written By\",\"Genre\",\"Rating\",\"Personal Rating\",\"Seen\",\"Aka\",\"Country\",\"Language\",\"Colour\",\"Plot\",\"Cast\",\"Notes\",\"Certification\",\"Mpaa\",\"Sound Mix\",\"Web Runtime\",\"Awards\") "+
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");

				statement.setInt(1,index);
				statement.setString(2,title);
				statement.setString(3,cover);
				statement.setString(4,IMDB);
				statement.setString(5,date);
				statement.setString(6,directedBy);
				statement.setString(7,writtenBy);
				statement.setString(8,genre);

				if (isMSAccess()) {
					statement.setString(9, rating);
					statement.setString(10, personalRating);
				}
				else {
					try {
						statement.setDouble(9,Double.parseDouble(rating));
					}
					catch (NumberFormatException e) {
						statement.setDouble(9,-1);
					}
					
					try {
						statement.setDouble(10, Double.parseDouble(personalRating));
					}
					catch (Exception e) {
						statement.setDouble(10, -1);
					}
				}

				statement.setBoolean(11,seen);
				statement.setString(12,aka);
				statement.setString(13,country);
				statement.setString(14,language);
				statement.setString(15,colour);
				statement.setString(16,plot);
				statement.setString(17,cast);
				statement.setString(18,notes);
				statement.setString(19,certification);
				statement.setString(20,mpaa);
				statement.setString(21,webSoundMix);
				statement.setString(22,webRuntime);
				statement.setString(23,awards);
				statement.executeUpdate();
			}

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
			index = -1;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return index;
	}


	/**
	 * Adds the fields to the general info table and returns the index added or
	 * -1 if insert failed.
	 **/
	public synchronized int addGeneralInfo(ModelMovie model) {
		int index = -1;

		try {
			/* Gets the next index... */
			ResultSet resultSet = _sql.executeQuery("SELECT MAX(ID) "+
					"FROM " + quotedGeneralInfoString + ";");

			if (resultSet.next()) {
				index = resultSet.getInt(1) + 1;
			} else {
				index = 0;
			}
			_sql.clear();
			/* Adds the info... */
			if (index != -1) {

				PreparedStatement statement;
				statement = _sql.prepareStatement("INSERT INTO \"General Info\" "+
						"(\"ID\",\"Title\",\"Cover\",\"Imdb\",\"Date\",\"Directed By\",\"Written By\",\"Genre\",\"Rating\",\"Personal Rating\",\"Seen\",\"Aka\",\"Country\",\"Language\",\"Colour\",\"Plot\",\"Cast\",\"Notes\",\"Certification\",\"Mpaa\",\"Sound Mix\",\"Web Runtime\",\"Awards\") "+
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");

				statement.setInt(1, index);
				statement.setString(2, model.getTitle());
				statement.setString(3, model.getCover());
				statement.setString(4, model.getUrlKey());
				statement.setString(5, model.getDate());
				statement.setString(6, model.getDirectedBy());
				statement.setString(7, model.getWrittenBy());
				statement.setString(8, model.getGenre());

				if (isMSAccess()) {
					statement.setString(9, model.getRating());
					statement.setString(10, model.getPersonalRating());
				}
				else {

					try {
						statement.setDouble(9, Double.parseDouble(model.getRating()));
					}
					catch (NumberFormatException e) {
						statement.setDouble(9, -1);
					}
					
					try {
						statement.setDouble(10, Double.parseDouble(model.getPersonalRating()));
					}
					catch (Exception e) {
						statement.setDouble(10, -1);
					}
				}

				statement.setBoolean(11, model.getSeen());
				statement.setString(12, model.getAka());
				statement.setString(13, model.getCountry());
				statement.setString(14, model.getLanguage());
				statement.setString(15, model.getColour());
				statement.setString(16, model.getPlot());
				statement.setString(17, model.getCast());
				statement.setString(18, model.getNotes());
				statement.setString(19, model.getCertification());
				statement.setString(20, model.getMpaa());
				statement.setString(21, model.getWebSoundMix());
				statement.setString(22, model.getWebRuntime());
				statement.setString(23, model.getAwards());
				statement.executeUpdate();
			}

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
			index = -1;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return index;
	}

	/**
	 * Adds the fields to the general info table and returns the index added
	 * Returns -1 if insert failed.
	 **/
	public synchronized int addGeneralInfoEpisode(ModelEpisode model) {

		int index = -1;

		try {
			/* Gets the next index... */
			ResultSet resultSet = _sql.executeQuery("SELECT MAX(ID) "+
					"FROM " + quote + generalInfoEpisodeString + quote + ";");
			if (resultSet.next()) {
				index = resultSet.getInt(1) + 1;
			} else {
				index = 0;
			}
			_sql.clear();

			/* Adds the info... */
			if (index != -1) {

				PreparedStatement statement;
				statement = _sql.prepareStatement("INSERT INTO \"General Info Episodes\" "+
						"(\"ID\",\"Title\",\"Cover\",\"UrlKey\",\"Date\",\"Directed By\",\"Written By\",\"Genre\",\"Rating\",\"Personal Rating\",\"Seen\",\"Aka\",\"Country\",\"Language\",\"Colour\",\"Plot\",\"Cast\",\"Notes\",\"movieID\",\"episodeNr\",\"Certification\",\"Sound Mix\",\"Web Runtime\",\"Awards\") "+
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");

				statement.setInt(1, index);
				statement.setString(2, model.getTitle());
				statement.setString(3, model.getCover());
				statement.setString(4, model.getUrlKey());
				statement.setString(5, model.getDate());
				statement.setString(6, model.getDirectedBy());
				statement.setString(7, model.getWrittenBy());
				statement.setString(8, model.getGenre());

				if (isMSAccess()) {
					statement.setString(9, model.getRating());
					statement.setString(10, model.getPersonalRating());
				}
				else {

					try {
						statement.setDouble(9, Double.parseDouble(model.getRating()));
					}
					catch (NumberFormatException e) {
						statement.setDouble(9, -1);
					}
					
					try {
						statement.setDouble(10, Double.parseDouble(model.getPersonalRating()));
					}
					catch (Exception e) {
						statement.setDouble(10, -1);
					}
				}

				statement.setBoolean(11, model.getSeen());
				statement.setString(12, model.getAka());
				statement.setString(13, model.getCountry());
				statement.setString(14, model.getLanguage());
				statement.setString(15, model.getColour());
				statement.setString(16, model.getPlot());
				statement.setString(17, model.getCast());
				statement.setString(18, model.getNotes());
				statement.setInt(19, model.getMovieKey());
				statement.setInt(20, model.getEpisodeKey());
				statement.setString(21, model.getCertification());
				statement.setString(22, model.getWebSoundMix());
				statement.setString(23, model.getWebRuntime());
				statement.setString(24, model.getAwards());
				statement.executeUpdate();
			}

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
			index = -1;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return index;
	}





	public synchronized int setGeneralInfo(ModelMovie model) {
		return setGeneralInfo(model.getKey(), model);
	}

	/**
	 * Sets the fields of movie index to the general info table and returns the number of
	 * updated rows.
	 **/
	public synchronized int setGeneralInfo(int index, ModelMovie model) {
		int value = 0;
		try {
			PreparedStatement statement;
			statement = _sql.prepareStatement("UPDATE \"General Info\" "+
					"SET \"General Info\".\"Title\"=?, "+
					"\"General Info\".\"Cover\"=?, "+
					"\"General Info\".\"Imdb\"=?, "+
					"\"General Info\".\"Date\"=?, "+
					"\"General Info\".\"Directed By\"=?, "+
					"\"General Info\".\"Written By\"=?, "+
					"\"General Info\".\"Genre\"=?, "+
					"\"General Info\".\"Rating\"=?, "+
					"\"General Info\".\"Personal Rating\"=?, "+
					"\"General Info\".\"Seen\"=?, "+
					"\"General Info\".\"Aka\"=?, "+
					"\"General Info\".\"Country\"=?, "+
					"\"General Info\".\"Language\"=?, "+
					"\"General Info\".\"Colour\"=?, "+
					"\"General Info\".\"Plot\"=?, "+
					"\"General Info\".\"Cast\"=?, "+
					"\"General Info\".\"Notes\"=?, "+
					"\"General Info\".\"Certification\"=?, "+
					"\"General Info\".\"Mpaa\"=?, "+
					"\"General Info\".\"Sound Mix\"=?, "+
					"\"General Info\".\"Web Runtime\"=?, "+
					"\"General Info\".\"Awards\"=? "+
			"WHERE \"General Info\".\"ID\"=?;");

			statement.setString(1, model.getTitle());
			statement.setString(2, model.getCover());
			statement.setString(3, model.getUrlKey());
			statement.setString(4, model.getDate());
			statement.setString(5, model.getDirectedBy());
			statement.setString(6, model.getWrittenBy());
			statement.setString(7, model.getGenre());

			if (isMSAccess()) {
				statement.setString(8,  model.getRating());
				statement.setString(9,  model.getPersonalRating());
			}
			else {

				try {
					statement.setDouble(8,Double.parseDouble(model.getRating()));
				}
				catch (NumberFormatException e) {
					statement.setDouble(8,-1);
				}
				
				try {
					statement.setDouble(9,Double.parseDouble(model.getPersonalRating()));
				}
				catch (Exception e) {
					statement.setDouble(9,-1);
				}
			}

			statement.setBoolean(10, model.getSeen());
			statement.setString(11, model.getAka());
			statement.setString(12, model.getCountry());
			statement.setString(13, model.getLanguage());
			statement.setString(14, model.getColour());
			statement.setString(15, model.getPlot());
			statement.setString(16, model.getCast());
			statement.setString(17, model.getNotes());
			statement.setString(18, model.getCertification());
			statement.setString(19, model.getMpaa());
			statement.setString(20, model.getWebSoundMix());
			statement.setString(21, model.getWebRuntime());
			statement.setString(22, model.getAwards());
			statement.setInt(23, index);

			value = statement.executeUpdate();
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return value;
	}





	public synchronized int setGeneralInfoEpisode(ModelEpisode model) {
		return setGeneralInfoEpisode(model.getKey(), model);
	}

	/**
	 * Sets the fields of movie index to the general info table and returns the number of
	 * updated rows.
	 **/
	public synchronized int setGeneralInfoEpisode(int index, ModelEpisode model) {
		int value = 0;
		try {
			PreparedStatement statement;
			statement = _sql.prepareStatement("UPDATE \"General Info Episodes\" "+
					"SET \"General Info Episodes\".\"Title\"=?, "+
					"\"General Info Episodes\".\"Cover\"=?, "+
					"\"General Info Episodes\".\"UrlKey\"=?, "+
					"\"General Info Episodes\".\"Date\"=?, "+
					"\"General Info Episodes\".\"Directed By\"=?, "+
					"\"General Info Episodes\".\"Written By\"=?, "+
					"\"General Info Episodes\".\"Genre\"=?, "+
					"\"General Info Episodes\".\"Rating\"=?, "+
					"\"General Info Episodes\".\"Personal Rating\"=?, "+
					"\"General Info Episodes\".\"Seen\"=?, "+
					"\"General Info Episodes\".\"Aka\"=?, "+
					"\"General Info Episodes\".\"Country\"=?, "+
					"\"General Info Episodes\".\"Language\"=?, "+
					"\"General Info Episodes\".\"Colour\"=?, "+
					"\"General Info Episodes\".\"Plot\"=?, "+
					"\"General Info Episodes\".\"Cast\"=?, "+
					"\"General Info Episodes\".\"Notes\"=?, "+
					"\"General Info Episodes\".\"movieID\"=?, "+
					"\"General Info Episodes\".\"episodeNr\"=?, "+
					"\"General Info Episodes\".\"Certification\"=?, "+
					"\"General Info Episodes\".\"Sound Mix\"=?, "+
					"\"General Info Episodes\".\"Web Runtime\"=?, "+
					"\"General Info Episodes\".\"Awards\"=? "+
			"WHERE \"General Info Episodes\".\"ID\"=?;");

			statement.setString(1, model.getTitle());
			statement.setString(2, model.getCover());
			statement.setString(3, model.getUrlKey());
			statement.setString(4, model.getDate());
			statement.setString(5, model.getDirectedBy());
			statement.setString(6, model.getWrittenBy());
			statement.setString(7, model.getGenre());

			if (isMSAccess()) {
				statement.setString(8,  model.getRating());
				statement.setString(9,  model.getPersonalRating());
			}
			else {

				try {
					statement.setDouble(8,Double.parseDouble(model.getRating()));
				}
				catch (NumberFormatException e) {
					statement.setDouble(8,-1);
				}
				
				try {
					statement.setDouble(9,Double.parseDouble(model.getPersonalRating()));
				}
				catch (Exception e) {
					statement.setDouble(9,-1);
				}
			}

			statement.setBoolean(10, model.getSeen());
			statement.setString(11, model.getAka());
			statement.setString(12, model.getCountry());
			statement.setString(13, model.getLanguage());
			statement.setString(14, model.getColour());
			statement.setString(15, model.getPlot());
			statement.setString(16, model.getCast());
			statement.setString(17, model.getNotes());
			statement.setInt(18, model.getMovieKey());
			statement.setInt(19, model.getEpisodeKey());
			statement.setString(20, model.getCertification());
			statement.setString(21, model.getWebSoundMix());
			statement.setString(22, model.getWebRuntime());
			statement.setString(23, model.getAwards());
			statement.setInt(24, index);

			value = statement.executeUpdate();
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return value;
	}




	/**
	 * Adds the values in fieldValuesList with names in fieldNamesList to movie
	 * index in the Extra Info table.
	 **/
	public synchronized void addExtraInfoMovie(int index, ArrayList<String> fieldNamesList, ArrayList<String> fieldValuesList) {

		try {
			String query = "INSERT INTO " + quotedExtraInfoString +
			"(ID) "+
			"VALUES("+index+");";

			/* Creates an empty row... */
			int value = _sql.executeUpdate(query);

			_sql.clear();

			if (value == 0) {
				throw new Exception("Failed to add row in table 'Extra Info'.");
			}

			if (setExtraInfoMovie(index, fieldNamesList, fieldValuesList) == -1) {
				throw new Exception("Error occured while updating extra info fields");
			}

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
	}


	/**
	 * Sets the fieldName of movie index in the Extra Info table to fieldValue and
	 * returns the number of updated rows.
	 **/
	public synchronized int setExtraInfoMovie(int index, ArrayList<String> fieldNamesList, ArrayList<String> fieldValuesList) {

		if (fieldNamesList == null || fieldValuesList == null)
			return 1;

		int value = 1;
		PreparedStatement statement;
		String query = "";

		try {

			for (int i = 0; i < fieldNamesList.size(); i++) {

				_sql.clear();

				query = "UPDATE " + quotedExtraInfoString + " "+
				"SET " + quotedExtraInfoString + "." +
				quote + (String) fieldNamesList.get(i) + quote + "=? "+
				"WHERE " + quotedExtraInfoString + ".ID=?;";

				statement = _sql.prepareStatement(query);

				statement.setString(1, (String) fieldValuesList.get(i));
				statement.setInt(2, index);
				value = statement.executeUpdate();
			}

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
			value = -1;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return value;
	}


	/**
	 * Adds the values in fieldValuesList with names in fieldNamesList to movie
	 * index in the Extra Info table.
	 * Return values. -1 error, 0 = success, 1 = aborted
	 **/
	public synchronized int addExtraInfoEpisode(int index, ArrayList<String> fieldNamesList, ArrayList<String> fieldValuesList) {

		int ret = 0;

		try {
			/* Creates an empty row... */
			int value = _sql.executeUpdate("INSERT INTO " + quotedExtraInfoEpisodeString + " "+
					"(ID) "+
					"VALUES("+index+");");

			_sql.clear();

			if (value == 0) {
				throw new Exception("Failed to add row in table 'Extra Info Episodes'.");
			}

			if (setExtraInfoEpisode(index, fieldNamesList, fieldValuesList) == -1)
				throw new Exception("Error occured while adding info to extra info fields");

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
			ret = -1;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		return ret;
	}


	/**
	 * Sets the fieldName of movie index in the Extra Info table to fieldValue and
	 * returns the number of updated rows.
	 **/
	public synchronized int setExtraInfoEpisode(int index, ArrayList<String> fieldNamesList, ArrayList<String> fieldValuesList) {

		if (fieldNamesList == null || fieldValuesList == null)
			return 1;

		int value = 0;
		PreparedStatement statement;

		try {

			for (int i = 0; i < fieldNamesList.size(); i++) {

				_sql.clear();

				statement = _sql.prepareStatement("UPDATE " + quotedExtraInfoEpisodeString + " "+
						"SET " + quotedExtraInfoEpisodeString + "."+ quote + fieldNamesList.get(i) + quote + "=? "+
						"WHERE " + quotedExtraInfoEpisodeString + ".ID=?;");

				statement.setString(1, fieldValuesList.get(i));
				statement.setInt(2, index);
				value = statement.executeUpdate();
			}

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
			value = -1;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return value;
	}


	/**
	 * Adds the values in fieldValuesList with names in fieldNamesList to movie
	 * index in the extra info table.
	 **/
	public synchronized void addLists(int index, ArrayList<String> columnNamesList, ArrayList<Boolean> fieldValuesList) {
		try {
			/* Creates an empty row... */
			int value = _sql.executeUpdate("INSERT INTO " + quote + "Lists" + quote + " "+
					"(" + quote + "ID" + quote +") "+
					"VALUES("+index+");");
			_sql.clear();

			if (value == 0) {
				throw new Exception("Can't add row.");
			}

			for (int i = 0; i < columnNamesList.size(); i++) {

				if (setLists(index,(String) columnNamesList.get(i),(Boolean) fieldValuesList.get(i)) == 0) {
					throw new Exception("Can't add field name "+(String) columnNamesList.get(i)+".");
				}
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
	}


	/**
	 * Sets the fieldName of movie index in the extra info table to fieldValue and
	 * returns the number of updated rows.
	 **/
	public synchronized int setLists(int index, String fieldName, Boolean fieldValue) {
		int value = 0;

		try {
			PreparedStatement statement = _sql.prepareStatement("UPDATE " + quote + "Lists" + quote + " "+
					"SET " + quote + "Lists" + quote + "." + quote + fieldName + quote + "=? "+
					"WHERE " + quote + "Lists" + quote + ".ID=?;");
			statement.setBoolean(1, fieldValue.booleanValue());
			statement.setInt(2, index);
			value = statement.executeUpdate();

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return value;
	}


	/**
	 * Sets the fields of movie index to the general info table and returns the number of
	 * updated rows.
	 **/
	public synchronized int setSeen(int index, boolean seen) {

		int value = 0;
		try {
			PreparedStatement statement = _sql.prepareStatement("UPDATE \"General Info\" "+
					"SET \"General Info\".\"Seen\"=? "+
			"WHERE \"General Info\".\"ID\"=?;");
			statement.setBoolean(1,seen);
			statement.setInt(2,index);

			value = statement.executeUpdate();
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return value;
	}

	/**
	 * Sets the fields of movie index to the general info table and returns the number of
	 * updated rows.
	 **/
	public synchronized int setSeenEpisode(int index, boolean seen) {

		int value = 0;
		try {
			PreparedStatement statement = _sql.prepareStatement("UPDATE \"General Info Episodes\" "+
					"SET \"General Info Episodes\".\"Seen\"=? "+
			"WHERE \"General Info Episodes\".\"ID\"=?;");
			statement.setBoolean(1,seen);
			statement.setInt(2,index);

			value = statement.executeUpdate();
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return value;
	}


	/**
	 * Adds a new column to the Lists table
	 **/
	public synchronized int addListsColumn(String field) {
		int value = 0;

		listsColumnNames = null;
		
		String fieldType = "BOOLEAN DEFAULT 0 NOT NULL";

		if (isMSAccess())
			fieldType = "BIT";

		try {
			value = _sql.executeUpdate("ALTER TABLE " + quote + "Lists" + quote + " "+
					"ADD COLUMN " + quote +field+ quote +" "+ fieldType +";");
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the number of altered rows... */
		return value;
	}

	/**
	 * Removes and Extra Info field from the database with name field
	 * and returns number of updated rows (none (-1)...).
	 **/
	public synchronized int removeListsColumn(String field) {

		listsColumnNames = null;
		
		int value = 0;
		try {
			value = _sql.executeUpdate("ALTER TABLE " + quote + "Lists" + quote + " "+
					"DROP COLUMN "+ quote +field+ quote +";");
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the number of altered rows... */
		return value;
	}


	public synchronized int addExtraInfoFieldName(String field) {

		if (addExtraInfoMovieFieldName(field) == -2)
			return -1;

		if (addExtraInfoEpisodeFieldName(field) == -2)
			return -2;

		extraInfoFieldNames = null;
		
		return 1;
	}


	/**
	 * Adds an Extra Info field from the database with name field
	 * and returns -2 if an exception occurs.
	 **/
	protected synchronized int addExtraInfoMovieFieldName(String field) {
		int value = 0;

		String fieldType = "TEXT";

		if (this instanceof DatabaseHSQL)
			fieldType = "LONGVARCHAR";

		try {
			value = _sql.executeUpdate("ALTER TABLE " + quotedExtraInfoString + " "+
					"ADD COLUMN " +quote+ field +quote+" "+ fieldType +";");

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
			value = -2;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}

		return value;
	}

	/**
	 * Adds and Extra Info field from the database with name field
	 * and returns -2 if an exception occurs.
	 **/
	protected synchronized int addExtraInfoEpisodeFieldName(String field) {
		int value = 0;

		String fieldType = "TEXT";

		if (this instanceof DatabaseHSQL)
			fieldType = "LONGVARCHAR";

		try {
			value = _sql.executeUpdate("ALTER TABLE " + quotedExtraInfoEpisodeString + " "+
					"ADD COLUMN " +quote+ field +quote+" "+ fieldType +";");

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
			value = -2;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the number of altered rows... */
		return value;
	}



	public synchronized int removeExtraInfoFieldName(String field) {

		if (removeExtraInfoMovieFieldName(field) == -2)
			return -1;

		if (removeExtraInfoEpisodeFieldName(field) == -2)
			return -1;

		extraInfoFieldNames = null;
		
		return 1;
	}


	/**
	 * Removes an Extra Info field from the database with name field
	 * and returns number of updated rows (none (-1)...).
	 **/
	protected synchronized int removeExtraInfoMovieFieldName(String field) {

		int value = 0;
		try {
			value = _sql.executeUpdate("ALTER TABLE " + quotedExtraInfoString + " "+
					"DROP COLUMN " +quote+ field +quote+";");
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
			value = -2;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the number of altered rows... */
		return value;
	}

	/**
	 * Removes and Extra Info field from the database with name field
	 * and returns number of updated rows (none (-1)...).
	 **/
	protected synchronized int removeExtraInfoEpisodeFieldName(String field) {

		int value = 0;
		try {
			value = _sql.executeUpdate("ALTER TABLE " + quotedExtraInfoEpisodeString + " "+
					"DROP COLUMN " +quote+ field +quote+";");
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
			value = -2;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the number of altered rows... */
		return value;
	}


	/**
	 * Sets the active additional info fields.
	 **/
	public synchronized void setActiveAdditionalInfoFields(int [] activeAdditionalInfoFields) {

		String activeFields = "";

		String columnName = "\"Active Additional Info Fields\"";
		String settings = quote + "Settings" + quote;

		if (this instanceof DatabaseMySQL)
			columnName = "Active_Additional_Info_Fields";

		for (int i = 0; i < activeAdditionalInfoFields.length; i++) {
			if (!activeFields.equals(""))
				activeFields += ":";
			activeFields += activeAdditionalInfoFields[i];
		}

		try {
			/* Tries to find if it should be an insert or an update... */
			ResultSet resultSet = _sql.executeQuery("SELECT " + settings +".* "+
					"FROM " + settings + " "+
					"WHERE " + settings + ".ID=1;");

			if (resultSet.next()) {
				_sql.clear();

				/* It's an update... */
				PreparedStatement statement = _sql.prepareStatement("UPDATE " + settings +" "+
						"SET " + settings + "."+ columnName +"=? "+
						"WHERE " + settings + ".ID=1;");
				statement.setString(1, activeFields);
				statement.executeUpdate();
			} else {
				_sql.clear();

				/* It's an insert...*/
				PreparedStatement statement = _sql.prepareStatement("INSERT INTO " + settings + " "+
						"(ID,"+ columnName +") "+
				"VALUES(1,?)");
				statement.setString(1, activeFields);
				statement.executeUpdate();
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
	}


	/**
	 * Sets the folders Covers and Queries for this database.
	 **/
	public synchronized int setFolders(String bewCoversFolder, String queriesFolder) {

		int value = 0;
		String folders = quote + "Folders" + quote;
		String covers = quote + "Covers" + quote;
		String queries = quote + "Queries" + quote;

		// Reset cached coversFolder
		coversFolder = null;
				
		try {
			/* Tries to find if it's an insert or an update... */
			ResultSet resultSet = _sql.executeQuery("SELECT " + folders + ".* "+
					"FROM " + folders + " "+
					"WHERE " + folders + ".ID=1;");

			if (resultSet.next()) {
				_sql.clear();
				/* Is an update... */
				PreparedStatement statement = _sql.prepareStatement("UPDATE " + folders +" "+
						"SET " + folders + "." + covers + "=?, " + folders + "."+ queries + "=? "+
						"WHERE " + folders + ".ID=1;");
				statement.setString(1, bewCoversFolder);
				statement.setString(2, queriesFolder);
				value = statement.executeUpdate();
			} else {
				_sql.clear();
				/* Ist's a insert...*/
				PreparedStatement statement = _sql.prepareStatement("INSERT INTO " + folders +" "+
						"(ID,"+ covers +","+ queries +") "+
				"VALUES(1,?,?)");
				statement.setString(1, bewCoversFolder);
				statement.setString(2, queriesFolder);
				value = statement.executeUpdate();
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the updated records... */
		return value;
	}


	/**
	 * Returns the Covers folder for this database.
	 **/
	public synchronized String getCoversFolder() {
		
		// Use cache instead.
		if (coversFolder != null) {
			return coversFolder;
		}
			
		String data = getString("SELECT " + quote + "Folders" + quote +"." + quote + "Covers" + quote + " "+
				"FROM " + quote + "Folders" + quote + " "+
				"WHERE " + quote + "Folders" + quote + ".ID=1;", "Covers");
		
		coversFolder = data;
		
		return data;
	}

	/**
	 * Returns the Queries folder for this database.
	 **/
	public synchronized String getQueriesFolder() {

		String data = getString("SELECT " + quote + "Folders" + quote + "." + quote + "Queries" + quote + " "+
				"FROM " + quote + "Folders" + quote + " "+
				"WHERE " + quote + "Folders" + quote + ".ID=1;", "Queries");
		/* Returns the data... */
		return data;
	}


		
	public synchronized String getMoviesSelectStatement(ModelDatabaseSearch options) {

		StringBuffer buf = new StringBuffer();
		
		if (options.getFullGeneralInfo) {
			buf.append("SELECT " + quotedGeneralInfoString + ".\"ID\", " +
					quotedGeneralInfoString + ".\"Imdb\", "+
					quotedGeneralInfoString + ".\"Cover\", "+
					quotedGeneralInfoString + ".\"Date\", "+
					quotedGeneralInfoString + ".\"Title\", "+
					quotedGeneralInfoString + ".\"Directed By\", "+
					quotedGeneralInfoString + ".\"Written By\", "+
					quotedGeneralInfoString + ".\"Genre\", "+
					quotedGeneralInfoString + ".\"Rating\", "+
					quotedGeneralInfoString + ".\"Personal Rating\", "+
					quotedGeneralInfoString + ".\"Plot\", "+
					quotedGeneralInfoString + ".\"Cast\", "+
					quotedGeneralInfoString + ".\"Notes\", "+
					quotedGeneralInfoString + ".\"Seen\", "+
					quotedGeneralInfoString + ".\"Aka\", "+
					quotedGeneralInfoString + ".\"Country\", "+
					quotedGeneralInfoString + ".\"Language\", "+
					quotedGeneralInfoString + ".\"Colour\", "+
					quotedGeneralInfoString + ".\"Certification\", "+
					quotedGeneralInfoString + ".\"Mpaa\", "+
					quotedGeneralInfoString + ".\"Sound Mix\", "+
					quotedGeneralInfoString + ".\"Web Runtime\", "+
					quotedGeneralInfoString + ".\"Awards\" ");
		}
		else {
			buf.append("SELECT " + quotedGeneralInfoString + ".ID, " + 
					quotedGeneralInfoString + "." + quote + "Title" + quote + ", " + 
					quotedGeneralInfoString + "." + quote + "Imdb" + quote + ", " + 
					quotedGeneralInfoString + "." + quote + "Cover" + quote +  ", " + 
					quotedGeneralInfoString + "." + quote + "Date" + quote);
		}
		
		ArrayList<String> lists = getListsColumnNames();
		
		if (lists != null && lists.size() > 0) {
			
			for (int i = 0; i < lists.size(); i++)
				buf.append(", " + quotedListsString + "." +quote+ lists.get(i) + quote+ " AS " +quote+ listsAliasPrefix + lists.get(i) + quote + " ");
		}	
			
		return buf.toString();
	}
	


	public synchronized ArrayList<ModelMovie> getMoviesList() {
		return getMoviesList("Title");
	}

	public synchronized ArrayList<ModelMovie> getMoviesList(String orderBy) {

		ModelDatabaseSearch options = new ModelDatabaseSearch();

		options.setOrderCategory(orderBy);
		options.setListOption(0);
		options.getFullGeneralInfo = !isMySQL();
		
		return getMoviesList(options);
	}

	public synchronized ArrayList<ModelMovie> getMoviesList(String orderBy, ArrayList<String> lists, boolean showUnlistedMovies) {

		log.debug("getMoviesList(String orderBy, ArrayList lists, boolean showUnlistedMovies)");
		
		 ModelDatabaseSearch options = new ModelDatabaseSearch();

		 options.setOrderCategory(orderBy);
		 options.setCurrentListNames(lists);
		 options.setShowUnlistedEntries(showUnlistedMovies);

		 options.getFullGeneralInfo = !isMySQL();
		 
		 if (getListsColumnNames().size() == lists.size() && showUnlistedMovies)
	    		options.setListOption(0);
		 else if (lists.size() > 0 || showUnlistedMovies) 
			 options.setListOption(1);
		
		return getMoviesList(options);
	}


	/**
	 * Part of the advanced search function
	 * Used by method getMoviesList(ModelDatabaseSearch options)
	 */
	private Object[] getFilterValues(String filter) {

		ArrayList<String> matchValues = new ArrayList<String>(10);

		/* The regular expression will divide by every white space except if (multiple) words are encapsulated by " and " or { and } */
		//Pattern pattern = Pattern.compile("([\\p{Graph}&&[^\"]]+?)\\s|(\".+?\"+)");
		//Pattern pattern = Pattern.compile("(\\{.+?\\})|([\\p{Graph}&&[^\"]]+?)\\s|(\".*?\"+)");

		//Pattern pattern = Pattern.compile("(\\{.+?\\})|([(\\p{L}|\\d)&&[^\"]]+?)\\s|(\".*?\"+)");
		//Pattern pattern = Pattern.compile("(\\{.+?\\})|([(\\p{Graph}|.)&&[^\"]]+?)\\s|(\".*?\"+)");
		
		//\\p{Graph}|\\p{Sc} - supports both Unicode characters as well as currency symbols
		Pattern pattern = Pattern.compile("(\\{.+?\\})|([(\\p{Graph}|\\p{Sc})&&[^\"]]+?)\\s|(\".*?\"+)");
			
		
		String tmp = "";
	
		for (Matcher m = pattern.matcher(filter+" "); m.find();) {

			tmp = (m.group(0)).trim();

			if (tmp.charAt(0) == '"' && tmp.charAt(tmp.length()-1) == '"') {
				tmp = tmp.substring(1, tmp.length()-1);
			}
			
			if (tmp.indexOf("\\") != -1) {
				// Need to double escape, first Java, and then regex escape
				tmp = tmp.replaceAll("\\\\", "\\\\\\\\");
			}
			
			if (tmp.indexOf("%") != -1) {
				tmp = tmp.replaceAll("%", "\\\\%");
			}
			
			matchValues.add(tmp);
		}

		return matchValues.toArray();
	}


	/**
	 * Part of the advanced search function
	 * Used by method getMoviesList(ModelDatabaseSearch options)
	 */
	private String processFilterValues(String table, String filterColumn, Object [] values, ModelDatabaseSearch options, boolean recursive) {

		String queryTemp = "";
		String value = "";

		for (int i = 0; i < values.length; i++) {

			value = (String) values[i];

			if (value.equals("AND") || value.equals("OR") || value.equals("XOR") || value.equals("NOT")) {

				log.debug("value:" + value);

				if (values.length > i+1)
					log.debug("values[i+1]:" + values[i+1]);
				else {
					errorMessage = "Invalid query. No expression after " + value;
					return null;
				}

				/* Checking if next value is invalid */
				if ((values.length-1 > i) && !values[i+1].equals(")") &&
						!values[i+1].equals("AND") && !values[i+1].equals("OR") && !values[i+1].equals("XOR")) {

					if (values[i+1].equals("(") && (values.length-2 > i)) {

						/* If i+2 value is valid */
						if (!(values[i+2].equals(")") || values[i+2].equals("AND") ||
								values[i+2].equals("OR") || values[i+2].equals("XOR")))
							queryTemp += " " + value + " ";
					}
					else if (!values[i+1].equals("(")) {

						if (value.equals("NOT")) {
							if ((i > 0) && !(values[i-1].equals("AND") || values[i-1].equals("OR") || values[i-1].equals(")"))) {
								queryTemp += " " + options.getDefaultOperator(); // AND or OR
							}
						}
						queryTemp += " " + value + " ";
					}
				}
				else {
					errorMessage = "Syntax error - parentheses mismatch";
					return null;
				}
			}

			else if (value.equals("("))
				queryTemp += " ( ";

			else if (value.equals(")"))
				queryTemp += " ) ";


			else if (value.startsWith("{")) {

				value = value.substring(1, value.length()-1);

				/* Invalid */
				if (value.indexOf(":") == -1) {
					errorMessage = "Syntax error (missing ':')";
					return null;
				}


				String [] tableField = value.substring(0, value.indexOf(":")).split(",");
				Object [] values2 = getFilterValues(value.substring(value.indexOf(":")+1, value.length()));

				for (int u = 0; u < tableField.length; u++)
					log.debug("tablefield:" + tableField[u] + ":");

				for (int u = 0; u < values2.length; u++)
					log.debug("values:" + values2[u] + ":");

				log.debug("tableField.length:" + tableField.length);

				/* Must look up the alias and find the table and column */
				if (tableField.length == 1) {

					if (options.getSearchAlias().containsValue(tableField[0])) {

						Set<Entry<String, String>> map = (options.getSearchAlias()).entrySet();
						String setkey;
						String setValue;

						for (Iterator<Map.Entry<String, String>> iterator = map.iterator(); iterator.hasNext();) {

							Map.Entry<String, String> entry = iterator.next();
							setkey = (String) entry.getKey();
							setValue = (String) entry.getValue();

							if (setValue.equals(tableField[0])) {

								log.debug("setkey:" + setkey);
								log.debug("setValue:" + setValue);

								String table2 = setkey.substring(0, setkey.indexOf("."));
								String column2 = setkey.substring(setkey.indexOf(".")+1, setkey.length());

								if (table2.replaceAll("_", " ").equalsIgnoreCase("General Info"))
									table2 = generalInfoString;
								else if (table2.replaceAll("_", " ").equalsIgnoreCase("Additional Info"))
									table2 = additionalInfoString;
								else if (table2.replaceAll("_", " ").equalsIgnoreCase("Extra Info"))
									table2 = extraInfoString;

								if (isMySQL()) {

									if (!table2.equals("Extra_Info"))
										column2 = column2.replaceAll(" ", "_");
									else
										column2 = quote+ column2 +quote;
								}
								else {
									table2 = quote+ table2 +quote;
									column2 = quote+ column2 +quote;
								}
								tableField = new String[]{table2, column2};
								break;
							}

						}
					}
					else {
						errorMessage = "Alias '"+ tableField[0] + "' is invalid";
						return null;
					}
				}

				int len;
				String filterValues = null;

				if (tableField.length > 0) {
					tableField[0] = tableField[0].trim();
					len = tableField[0].length();

					if (len > 0 && tableField[0].charAt(0) == '\"' && tableField[0].charAt(len-1) == '\"')
						tableField[0] = quote + tableField[0].substring(1, len-1) + quote;

					if (tableField.length > 1) {
						tableField[1] = tableField[1].trim();
						len = tableField[1].length();

						if (len > 0 && tableField[1].charAt(0) == '\"' && tableField[1].charAt(len-1) == '\"')
							tableField[1] = quote + tableField[1].substring(1, len-1) + quote;
					}

					filterValues = processFilterValues(tableField[0].trim(), tableField[1].trim(), values2, options, true);
				}

				if (filterValues == null) {
					errorMessage = "Syntax error (parantheses)";
					return null;
				}


				if ((i > 0) && !(values[i-1].equals("AND") || values[i-1].equals("OR") ||
						values[i-1].equals("XOR") || values[i-1].equals("NOT") ||
						values[i-1].equals("(") || values[i-1].equals(")"))) {
					queryTemp += " " + options.getDefaultOperator() + " ";
				}

				queryTemp += " ( " + filterValues + " ) ";
			}
			else {
				// Value is not a parenthesis or keyword (AND, OR, NOT, XOR)
				
				log.debug("i:" + i);
				if (i > 0)
					log.debug("values[i-1]:" + values[i-1]);


				if ((i > 0) && !(values[i-1].equals("AND") || values[i-1].equals("OR") ||
						values[i-1].equals("XOR") || values[i-1].equals("NOT") ||
						values[i-1].equals("(") || values[i-1].equals(")"))) {
					queryTemp += " " + options.getDefaultOperator() + " ";
				}

				boolean caseinsensitive = true;

				if (value.equals("")) {

					queryTemp += "("+ table +"."+ filterColumn +" LIKE '' ";

					/* If include aka titles */
					if (options.getIncludeAkaTitlesInFilter() && (filterColumn.indexOf("Title") != -1) && !recursive) {
						queryTemp += "OR "+ table +".Aka LIKE '' ";
					}
					queryTemp += ") ";
				}
				else if (caseinsensitive && isHSQL()) {
					/* Edit */
					queryTemp += "(UPPER("+ table + "." + filterColumn + ") LIKE ? ";

					options.addSearchTerm(value.toUpperCase());

					/* If include aka titles */
					if (options.getIncludeAkaTitlesInFilter() && (filterColumn.indexOf("Title") != -1) && !recursive) {

						queryTemp += "OR UPPER("+ table +".\"Aka\") LIKE ? ";
						options.addSearchTerm(value.toUpperCase());
					}
					queryTemp += ") ";
				}
				else {
					queryTemp += "(" + table + "."+ filterColumn +" LIKE ? ";

					options.addSearchTerm(value);

					/* If include aka titles */
					if (options.getIncludeAkaTitlesInFilter() && !recursive) {
						queryTemp += "OR "+ table +".Aka LIKE ? ";
						options.addSearchTerm(value);
					}
					queryTemp += ") ";
				}
			}
		}
		return queryTemp;
	}


	/**
	 * Part of the advanced search function
	 * Used by method getMoviesList(ModelDatabaseSearch options)
	 */
	private String processFilter(ModelDatabaseSearch options) {

		String table = quotedGeneralInfoString;
		String filterColumn = quote + options.getFilterCategory() + quote;

		if (isMySQL())
			filterColumn = filterColumn.replaceAll(" ", "_");

		String filter = options.getFilterString();

		if (filter.trim().startsWith("DUPLICATES") && !isMSAccess()) {
			options.duplicates = true;
			return "";
		}
		
		filter = filter.replaceAll("\\("," \\( ");
		filter = filter.replaceAll("\\)"," \\) ");

		if (isHSQL() && filter.indexOf(" XOR ") != -1) {
			errorMessage = "XOR is not a supported operator in HSQLDB";
			return null;
		}

		/* Remove all double white space */
		//filter = removeDoubleSpace(filter);

		filter = filter.trim();

		if (filter.startsWith("AND ") || filter.startsWith("OR "))
			filter = filter.substring(filter.indexOf(" ")+1, filter.length());

		if (filter.endsWith(" AND") || filter.endsWith(" OR") || filter.endsWith(" NOT"))
			filter = filter.substring(0, filter.lastIndexOf(" "));


		/* Check if number and placement of parentheses is correct */

		int par1 = 0; /* '(' */
		int par2 = 0; /* ')' */

		for (int i = 0; i < filter.length(); i++) {

			if (filter.charAt(i) == '(') {
				par1++;
			}
			else if (filter.charAt(i) == ')') {
				if (par1 > par2)
					par2++;
				else {
					errorMessage = "Syntax error - parantheses mismatch";
					return null;
				}
			}
		}

		if (par1 > par2) {
			errorMessage = "Syntax error - parantheses mismatch";
			return null;
		}

		Object[] values = getFilterValues(filter);
				
		if (values.length == 0)
			return null;
			
		String filterTemp = processFilterValues(table, filterColumn, values, options, false);
		
		if (filterTemp == null)
			return null;

		if ((filterTemp.indexOf(" AND ") != -1) || (filterTemp.indexOf(" OR ") != -1) || (filterTemp.indexOf(" XOR ") != -1))
			filterTemp = "( "+ filterTemp +") ";
		
		if (options.where)
			filter = "AND " + filterTemp;
		else
			filter = "WHERE (" + filterTemp;

		if (!options.where) {
			if (!filter.equals(""))
				filter += ")";
		
			options.where = true;
		}
				
		return filter;
	}


	/**
	 * Part of the advanced search function
	 * Used by method getMoviesList(ModelDatabaseSearch options)
	 */
	protected String setTableJoins(String filter, ModelDatabaseSearch options) {

		String selectAndJoin;

		selectAndJoin = getMoviesSelectStatement(options);
		
		String orderBy = options.getOrderCategory();
		String joinTemp = "";

		if (isMSAccess() && 
				((options.getListOption() == 1) || orderBy.equals("Duration") || 
						(filter.indexOf(additionalInfoString) != -1) || (filter.indexOf(extraInfoString) != -1))) {
			
			joinTemp = "\"General Info\" INNER JOIN \"Lists\" ON \"General Info\".ID=\"Lists\".ID ";
							
			if (orderBy.equals("Duration") || filter.indexOf(additionalInfoString) != -1) {
				joinTemp = "(" + joinTemp + ") INNER JOIN \"Additional Info\" ON \"Additional Info\".ID=\"General Info\".ID";
			}
			
			if (filter.indexOf(extraInfoString) != -1) {
				joinTemp = "(" + joinTemp + ") INNER JOIN \"Extra Info\" ON \"Extra Info\".ID=\"General Info\".ID";
			}
						
			selectAndJoin += "FROM " + joinTemp;
		}
		else {

			selectAndJoin += "FROM " + quotedGeneralInfoString;
			
			selectAndJoin += " INNER JOIN "+ quote+ "Lists"+ quote+ " ON "+ quotedGeneralInfoString + ".ID = " + quote + "Lists" + quote + ".ID ";
			
			if (orderBy.equals("Duration") || filter.indexOf(additionalInfoString) != -1) {
				orderBy = quotedAdditionalInfoString + "." + quote + "Duration" + quote;
				selectAndJoin += "INNER JOIN "+ quotedAdditionalInfoString + " ON "+ quotedGeneralInfoString + ".ID = "+ quotedAdditionalInfoString +".ID ";
			}

			if (filter.indexOf(extraInfoString) != -1) {
				selectAndJoin += "INNER JOIN " + quotedExtraInfoString + " ON " + quotedGeneralInfoString + ".ID=" + quotedExtraInfoString + ".ID ";
			}
		}
		return selectAndJoin;
	}


	/**
	 * Part of the advanced search function
	 * Used by method getMoviesList(ModelDatabaseSearch options)
	 */
	private String processAdvancedOptions(ModelDatabaseSearch options) {

		String sqlQuery = "";
		ArrayList<String> currentLists = options.getCurrentListNames();
		int option = 0;

		/* List */
		if ((option = options.getListOption()) == 1) {
			
			// Extra verification test
			if (currentLists.size() == 0 && 
					(!options.getShowUnlistedEntries() || (options.getShowUnlistedEntries() && getListsColumnNames().size() == 0))) {
				
				if (!options.getShowUnlistedEntries())
					log.warn("Invalid database options! getListOption == 1 when currentLists.size() == 0 and options.getShowUnlistedEntries() == false");
				else {
					log.warn("Invalid database options! getShowUnlistedEntries is true while getListsColumnNames().size() == 0");
				}
			} // Showing all the lists and those not on lists is the same as disabling the lists.
			else if (currentLists.size() == getListsColumnNames().size() && options.getShowUnlistedEntries()) {
				log.warn("Invalid database options! getListOption == 1 when currentLists.size ("+ currentLists.size() +") == getListsColumnNames().size ("+ getListsColumnNames().size() +") and options.getShowUnlistedEntries() == true");
			}
			else {
				
				if (!options.where)
					sqlQuery += "WHERE (";

				if (currentLists.size() > 0) {

					sqlQuery += "(";

					for (int i = 0; i < currentLists.size(); i++) {

						if (i > 0)
							sqlQuery += " OR ";

						sqlQuery += quote + "Lists"+ quote + "." +quote+ currentLists.get(i) +quote+ "=1 ";
					}
					sqlQuery += ")";
				}

				if (options.getShowUnlistedEntries()) {

					ArrayList<String> listNames = getListsColumnNames();

					if (listNames.size() > 0) {

						if (currentLists.size() > 0)
							sqlQuery += " OR ";

						sqlQuery += "(";

						for (int i = 0; i < listNames.size(); i++) {

							if (i > 0)
								sqlQuery += " AND ";

							if (isMSAccess())
								sqlQuery += quote + "Lists"+ quote + "." +quote+ listNames.get(i) +quote+ "<>1 ";
							//sqlQuery += "Iif(IsNull(\"Lists\".\"" + listNames.get(i) + "\"),false, \"Lists\".\"" + listNames.get(i) + "\")<>1 ";
							else // must use a function to handle possible null values
								sqlQuery += "COALESCE(" +quote+ "Lists" +quote+ "." +quote+ listNames.get(i) + quote+ ",false)<>1 ";

						}

						sqlQuery += ")";
					}
				}

				options.where = true;
			}
		}
		
		/* seen */
		if ((option = options.getSeen()) > 1) {

			if (options.where)
				sqlQuery += "AND ";
			else
				sqlQuery += "WHERE (";

			if (option == 2)
				sqlQuery += "("+ quotedGeneralInfoString +"." +quote+ "Seen" +quote+ " = 1) ";
			else
				sqlQuery += "("+ quotedGeneralInfoString +"." +quote+ "Seen" +quote+ " = 0) ";

			options.where = true;
		}

		/* Rating */
		if ((option = options.getRatingOption()) > 1) {

			double rating = options.getRating();

			if (options.where)
				sqlQuery += "AND ";
			else
				sqlQuery += "WHERE (";

			/* if MSAccess, have to convert rating with the Val function */
			if (isMSAccess()) {

				if (option == 2)
					sqlQuery += "(Val(Rating) >= "+rating+") ";
				else
					sqlQuery += "(Val(Rating) <= "+rating+") ";
			}
			else {
				if (option == 2)
					sqlQuery += "(" +quote+ "Rating" +quote+ " >= "+rating+") ";
				else
					sqlQuery += "(" +quote+ "Rating" +quote+ " <= "+rating+") ";
			}

			options.where = true;
		}

		/* Date */
		if ((option = options.getDateOption()) > 1) {

			String date = options.getDate();

			if (!date.equals("")) {

				if (options.where)
					sqlQuery += "AND ";
				else
					sqlQuery += "WHERE (";

				/* if MSAccess, have to convert date with the Val function */
				if (isMSAccess()) {
					if (option == 2)
						sqlQuery += "(Val(Date) >= "+ date +") ";
					else
						sqlQuery += "(Val(Date) <= "+ date +") ";
				}	
				else {
					if (option == 2)
						sqlQuery += "(" +quote+ "Date" +quote+ " >= "+ date +") ";
					else
						sqlQuery += "(" +quote+ "Date" +quote+ " <= "+ date +") ";
				}

				options.where = true;
			}
		}
		
		if (!sqlQuery.equals(""))
			sqlQuery += ")";
		
		return sqlQuery;
	}

	
	
	/**
	 * Part of the advanced search function
	 * Returns a List of MovieModels according to the search options
	 **/
	public synchronized ArrayList<ModelMovie> getMoviesList(ModelDatabaseSearch options) {

		log.debug("getMoviesList(ModelDatabaseSearch options)");
		
		ArrayList<ModelMovie> list = new ArrayList<ModelMovie>();

		String sqlAdcancedOptions = processAdvancedOptions(options);

		/* Filter */
		String sqlFilter = "";
	
		if (!options.getFilterString().trim().equals("")) {
			sqlFilter = processFilter(options);
			
			if (sqlFilter == null)
				return list;
		}

		/* Sets the right table joins */
		String selectAndJoin = setTableJoins(sqlFilter, options);
		
		String sqlQuery = selectAndJoin + " " + sqlAdcancedOptions + " " + sqlFilter + " ";
			
		log.debug("selectAndJoin:" + selectAndJoin);
		log.debug("sqlAdcancedOptions:" + sqlAdcancedOptions);
		log.debug("sqlFilter:" + sqlFilter);
		log.debug("sqlQuery:" + sqlQuery);
			
		if (options.duplicates) {
			
			String dupQuery = getDuplicateQueryString(options);
			
			log.debug("DupQuery:" + dupQuery);
			
			if (!dupQuery.trim().equals("")) {
				sqlQuery += (options.where ? " AND " : " WHERE ") + dupQuery;
			}
		}
		
		
		String orderBy = options.getOrderCategory();

		if (isMySQL())
			orderBy = orderBy.replaceAll(" ", "_");

		/* if MSAccess, have to convert rating with the Val function */
		if (isMSAccess() && orderBy.equals("Rating")) {
			orderBy = "ORDER BY Val("+ orderBy +"), \"Title\"";
		}
		else
			orderBy = "ORDER BY " +quote+ orderBy +quote+ ", " +quote+ "Title" +quote;

		sqlQuery += orderBy + ";";
	
		PreparedStatement statement = null;
		
		try {
			statement = _sql.prepareStatement(sqlQuery);

			for (int i = 0; i < options.searchTerms.size(); i++) {
				statement.setString(i+1, "%" + ((String) options.searchTerms.get(i)) + "%");
			}

			options.searchTerms.clear();

			if (isMSAccess())
				log.debug("sqlQuery:" + sqlQuery);
			else
				log.debug("Statement:" + statement);
			
			
			/* Gets the list in a result set... */
			ResultSet resultSet = statement.executeQuery();

			/* Processes the result set till the end... */
			while (resultSet.next()) {
				ModelMovie model;
								
				if (options.getFullGeneralInfo) {
					model = new ModelMovie(resultSet.getInt("ID"), 
							resultSet.getString("Imdb"), resultSet.getString("Cover"), 
							resultSet.getString("Date"), resultSet.getString("Title"), 
							resultSet.getString("Directed By"), resultSet.getString("Written By"), 
							resultSet.getString("Genre"), resultSet.getString("Rating"), 
							resultSet.getString("Personal Rating"), 
							resultSet.getString("Plot"), resultSet.getString("Cast"),
							resultSet.getString("Notes"), resultSet.getBoolean("Seen"), 
							resultSet.getString("Aka"), resultSet.getString("Country"),
							resultSet.getString("Language"), resultSet.getString("Colour"),
							resultSet.getString("Certification"), resultSet.getString("Mpaa"),
							resultSet.getString("Sound Mix"), resultSet.getString("Web Runtime"),
							resultSet.getString("Awards"));
				}
				else {
					model = new ModelMovie(resultSet.getInt("ID"), resultSet.getString("Title"),
							resultSet.getString("Imdb"), resultSet.getString("Cover"), 
							resultSet.getString("Date"));
				}
								
				ArrayList<String> listNames = getListsColumnNames();
				int count = listNames.size();
								
				if (count > 0) {
					for (int i = 0; i < count; i++) {
						if (resultSet.getBoolean(listsAliasPrefix + listNames.get(i))) {
							model.addToMemberOfList((String) listNames.get(i));
						}
					}
				}
				list.add(model);
			}
		} catch (Exception e) {
			log.error("Exception:" + e.getMessage(), e);
			log.debug("sqlQuery:" + sqlQuery);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the list model... */
		return list;
	}


	private String getDuplicateQueryString(ModelDatabaseSearch options) {

		String dupQuery = "";
		
		String dupString = options.getFilterString().trim();
		dupString = dupString.substring("DUPLICATES".length());

		String category = dupString.trim();

		// Default values
		String categoriesQuery = quote + "Title" + quote+", "+ quote + "Date" + quote;
		String mainCategory = quote + "Title" + quote;

		if (category.equalsIgnoreCase("Title")) {
			mainCategory = quote + "Title" + quote;
			categoriesQuery = quote + "Title" + quote;
		}
		else if (category.equalsIgnoreCase("Imdb")) {
			mainCategory = quote + "Imdb" + quote;
			categoriesQuery = quote + "Imdb" + quote;
			// change the main order category of the entire query
			options.setOrderCategory("Imdb");
		}
		
		/*
			dupQuery += "\"Imdb\" in (" +
			   "select \"Imdb\" "+
			   "FROM " + quotedGeneralInfoString +
			   " GROUP BY \"Imdb\" "+
			   "HAVING COUNT(\"Imdb\") > 1 " +
	           ") ";
		 */

		/*
			dupQuery += "\"Title\" in (" +
					   " select \"Title\""+
					   " FROM " + quotedGeneralInfoString +
					   " GROUP BY \"Title\", \"Date\" "+
					   "HAVING COUNT(\"Title\") > 1" +
			           ") ";
		 */
/*
		dupQuery += quote + "Imdb" + quote +" in (" +
		" SELECT " +quote+ "Imdb" + quote +
		" FROM " + quotedGeneralInfoString +
		" GROUP BY " +quote+ "Imdb" + quote + 
		" HAVING COUNT(" +quote+ "Imdb" +quote+ ") > 1" +
		") ";
*/
/*
		dupQuery += quote + "Title" + quote +" in (" +
		" SELECT " +quote+ "Title" + quote +
		" FROM " + quotedGeneralInfoString +
		" GROUP BY " + categoriesQuery + 
		" HAVING COUNT(" +quote+ "Title" +quote+ ") > 1" +
		") ";
	*/	
		
		dupQuery += mainCategory +" in (" +
		" SELECT " + mainCategory +
		" FROM " + quotedGeneralInfoString +
		" WHERE " + mainCategory + " NOT LIKE ''" +
		" GROUP BY " + categoriesQuery + 
		" HAVING COUNT(" + mainCategory + ") > 1" +
		") ";
		
		
		return dupQuery;

		/*
		SELECT "Title"
FROM "General Info"

WHERE "Title" in (
   select "Title"
   from "General Info"
   group by "Title"
   having count("Title") > 1
)
order by "Title"
		 */
	}

	public synchronized ArrayList<ModelEpisode> getEpisodeList() {
		return getEpisodeList("movieID");
	}

	/**
	 * Returns an ArrayList that contains all the movies in the
	 * current database.
	 **/
	public synchronized ArrayList<ModelEpisode> getEpisodeList(String orderBy) {
		ArrayList<ModelEpisode> list = new ArrayList<ModelEpisode>(100);

		try {
			/* Gets the list in a result set... */
			ResultSet resultSet = _sql.executeQuery("SELECT \"General Info Episodes\".\"ID\","+
					"\"General Info Episodes\".\"movieID\","+
					"\"General Info Episodes\".\"episodeNr\","+
					"\"General Info Episodes\".\"UrlKey\","+
					"\"General Info Episodes\".\"Cover\","+
					"\"General Info Episodes\".\"Date\","+
					"\"General Info Episodes\".\"Title\","+
					"\"General Info Episodes\".\"Directed By\","+
					"\"General Info Episodes\".\"Written By\","+
					"\"General Info Episodes\".\"Genre\","+
					"\"General Info Episodes\".\"Rating\","+
					"\"General Info Episodes\".\"Personal Rating\","+
					"\"General Info Episodes\".\"Plot\","+
					"\"General Info Episodes\".\"Cast\","+
					"\"General Info Episodes\".\"Notes\","+
					"\"General Info Episodes\".\"Seen\","+
					"\"General Info Episodes\".\"Aka\","+
					"\"General Info Episodes\".\"Country\","+
					"\"General Info Episodes\".\"Language\","+
					"\"General Info Episodes\".\"Colour\","+
					"\"General Info Episodes\".\"Certification\","+
					//"\"General Info Episodes\".\"Mpaa\","+
					"\"General Info Episodes\".\"Sound Mix\","+
					"\"General Info Episodes\".\"Web Runtime\","+
					"\"General Info Episodes\".\"Awards\" "+
					"FROM \"General Info Episodes\" "+
					"ORDER BY \"General Info Episodes\".\""+ orderBy +"\", \"General Info Episodes\".\"episodeNr\";");


			/* Processes the result set till the end... */
			while (resultSet.next()) {
				list.add(new ModelEpisode(resultSet.getInt("ID"), resultSet.getInt("movieID"), 
						resultSet.getInt("episodeNr"), resultSet.getString("UrlKey"), 
						resultSet.getString("Cover"), resultSet.getString("Date"), 
						resultSet.getString("Title"), resultSet.getString("Directed By"), 
						resultSet.getString("Written By"), resultSet.getString("Genre"), 
						resultSet.getString("Rating"), resultSet.getString("Personal Rating"), 
						resultSet.getString("Plot"), 
						resultSet.getString("Cast"), resultSet.getString("Notes"), 
						resultSet.getBoolean("Seen"), resultSet.getString("Aka"), 
						resultSet.getString("Country"), resultSet.getString("Language"), 
						resultSet.getString("Colour"), resultSet.getString("Certification"), 
						/*resultSet.getString("Mpaa"),*/ resultSet.getString("Sound Mix"), 
						resultSet.getString("Web Runtime"), resultSet.getString("Awards")));
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the list model... */
		return list;
	}


	/** Returns a MovieModel that contains the movie at the specified index
	 * in the database.
	 **/
	public synchronized ModelMovie getMovie(int index) {
		ModelMovie movie = null;
		
		try {
		
			ModelDatabaseSearch options = new ModelDatabaseSearch();

			options.setOrderCategory("");
			options.setListOption(0);
						
			String sqlQuery = setTableJoins("", options);
			sqlQuery += " WHERE \"General Info\".\"ID\"="+index+";";
			
			/* Gets the list in a result set... */
			ResultSet resultSet = _sql.executeQuery(sqlQuery);

			/* Processes the result set till the end... */
			if (resultSet.next()) {
				movie = new ModelMovie(resultSet.getInt("id"), resultSet.getString("Imdb"), 
						resultSet.getString("Cover"), resultSet.getString("Date"), 
						resultSet.getString("Title"), resultSet.getString("Directed By"), 
						resultSet.getString("Written By"), resultSet.getString("Genre"), 
						resultSet.getString("Rating"), resultSet.getString("Personal Rating"), 
						resultSet.getString("Plot"),
						resultSet.getString("Cast"), resultSet.getString("Notes"), 
						resultSet.getBoolean("Seen"), resultSet.getString("Aka"), 
						resultSet.getString("Country"), resultSet.getString("Language"),
						resultSet.getString("Colour"), resultSet.getString("Certification"),
						resultSet.getString("Mpaa"), resultSet.getString("Sound Mix"), 
						resultSet.getString("Web Runtime"), resultSet.getString("Awards"));
				
				ArrayList<String> listNames = getListsColumnNames();
				int count = listNames.size();
				
				if (count > 0) {
										
					for (int i = 0; i < count; i++) {
						if (resultSet.getBoolean(listsAliasPrefix + listNames.get(i)))
							movie.addToMemberOfList((String) listNames.get(i));
					}
				}
			}

		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the list model... */
		return movie;
	}


	/**
	 * Returns a ModelMovie with the general info on a specific episode
	 **/
	public synchronized ModelEpisode getEpisode(int index) {
		ModelEpisode episode = null;

		try {

			/* Gets the list in a result set... */
			ResultSet resultSet = _sql.executeQuery("SELECT \"General Info Episodes\".\"ID\","+
					"\"General Info Episodes\".\"movieID\","+
					"\"General Info Episodes\".\"episodeNr\","+
					"\"General Info Episodes\".\"UrlKey\","+
					"\"General Info Episodes\".\"Cover\","+
					"\"General Info Episodes\".\"Date\","+
					"\"General Info Episodes\".\"Title\","+
					"\"General Info Episodes\".\"Directed By\","+
					"\"General Info Episodes\".\"Written By\","+
					"\"General Info Episodes\".\"Genre\","+
					"\"General Info Episodes\".\"Rating\","+
					"\"General Info Episodes\".\"Personal Rating\","+
					"\"General Info Episodes\".\"Plot\","+
					"\"General Info Episodes\".\"Cast\","+
					"\"General Info Episodes\".\"Notes\","+
					"\"General Info Episodes\".\"Seen\","+
					"\"General Info Episodes\".\"Aka\","+
					"\"General Info Episodes\".\"Country\","+
					"\"General Info Episodes\".\"Language\","+
					"\"General Info Episodes\".\"Colour\", "+
					"\"General Info Episodes\".\"Certification\", "+
					//  "\"General Info Episodes\".\"Mpaa\", "+
					"\"General Info Episodes\".\"Sound Mix\", "+
					"\"General Info Episodes\".\"Web Runtime\", "+
					"\"General Info Episodes\".\"Awards\" "+
					"FROM \"General Info Episodes\" "+ "WHERE \"General Info Episodes\".\"ID\"="+index+";");

			/* Processes the result set till the end... */
			if (resultSet.next()) {
				episode = new ModelEpisode(resultSet.getInt("ID"), resultSet.getInt("movieID"), 
						resultSet.getInt("episodeNr"), resultSet.getString("UrlKey"), 
						resultSet.getString("Cover"), resultSet.getString("Date"), 
						resultSet.getString("Title"), resultSet.getString("Directed By"), 
						resultSet.getString("Written By"), resultSet.getString("Genre"), 
						resultSet.getString("Rating"), resultSet.getString("Personal Rating"), 
						resultSet.getString("Plot"), resultSet.getString("Cast"), 
						resultSet.getString("Notes"), resultSet.getBoolean("Seen"), 
						resultSet.getString("Aka"), resultSet.getString("Country"), 
						resultSet.getString("Language"), resultSet.getString("Colour"), 
						resultSet.getString("Certification"), 
						/*resultSet.getString("Mpaa"), */resultSet.getString("Sound Mix"), 
						resultSet.getString("Web Runtime"), resultSet.getString("Awards"));
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("Exception: " + e.getMessage());
			}
		}
		/* Returns the list model... */
		return episode;
	}


	/**
	 * Returns the Extra Info field value with index index named name...
	 **/
	public synchronized String getExtraInfoMovieField(int index, String name) {

		String data = getString("SELECT \"Extra Info\".\""+name+"\" "+
				"FROM \"Extra Info\" "+
				"WHERE \"Extra Info\".\"ID\"="+index+";", name);
		/* Returns the data... */
		return data;
	}

	/**
	 * Returns the Extra Info field with index index named name...
	 **/
	public synchronized String getExtraInfoEpisodeField(int index, String name) {

		String data = getString("SELECT \"Extra Info Episodes\".\""+name+"\" "+
				"FROM \"Extra Info Episodes\" "+
				"WHERE \"Extra Info Episodes\".\"ID\"="+index+";", name);
		/* Returns the data... */
		return data;
	}


	/**
	 * Returns true if the movie at the specific index is a member of the specified list with name name...
	 **/
	protected synchronized boolean getList(int index, String name) {

		boolean data = getBoolean("SELECT \"Lists\".\""+name+"\" "+
				"FROM \"Lists\" "+
				"WHERE \"Lists\".\"ID\"="+index+";", name);
		/* Returns the data... */
		return data;
	}
}
