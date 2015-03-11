/**
 * @(#)SQL.java 1.0 29.01.06 (dd.mm.yy)
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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sf.xmm.moviemanager.MovieManager;

import org.apache.log4j.Logger;

class SQL {

	Logger log = Logger.getLogger(getClass());

	private String _url;

	private Connection _conn;

	private Statement _statement;

	private String databaseType;

	private ResultSet lastResultSet = null;

	/**
	 * States if a connection has already been made.
	 **/
	private boolean _connected = false;

	protected SQL(String path, String databaseType) {
		this(path, databaseType, false);
	}
	
	/**
	 * Construtor. 
	 */
	protected SQL(String path, String databaseType, boolean enableSocketTimeout) {

		this.databaseType = databaseType;

		if (databaseType.equals("MSAccess"))
			_url = "jdbc:odbc:;DBQ="+ path +";DRIVER={Microsoft Access Driver (*.mdb)}";

		else if (databaseType.equals("HSQL")) {
			_url = "jdbc:hsqldb:file:"+ path;
		}

		else if (databaseType.equals("MySQL")) {
			_url = "jdbc:mysql://"+ path;
			
			//_url += "&autoReconnect=true&inactivity-timeout=10&wait-timeout=10";
			
			// Add socket timeout of 15 minutes
			if (enableSocketTimeout)
				_url += "&socketTimeout=960";

			//useUnicode=true&characterEncoding=UTF-8
		}
	}

		
	/**
	 * Loads the driver and initiates a connection.
	 **/
	protected void setUp() throws Exception {

		if (!_connected) {

			if (databaseType.equals("MSAccess")) {
				Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
				_conn = DriverManager.getConnection(_url,"","");
				log.info("Connected to MS Access database");
				_connected = true;
			}
			else if (databaseType.equals("HSQL")) {
				Class.forName("org.hsqldb.jdbcDriver");

				_conn = DriverManager.getConnection(_url,"sa","");

				log.info("Connected to HSQL database");
				_connected = true;
			}
			else if (databaseType.equals("MySQL")) {

				Class.forName("com.mysql.jdbc.Driver").newInstance();

				if (!MovieManager.getConfig().getInternalConfig().getSensitivePrintMode())
					log.debug("SQL:" + _url);
				
				//DriverManager.setLoginTimeout(5);
				_conn = DriverManager.getConnection(_url);

				log.info("Connected to MySQL database");
				_connected = true;
			}
		}
	}


	/**
	 * Closes the connection.
	 **/
	protected void finalize() throws Exception {
		_conn.close();
		_connected = false;
	}

	/**
	 * Returns _connected.
	 **/
	protected boolean isConnected() {
		return _connected;
	}

	/**
	 * Starts a transaction (autocommint to false).
	 **/
	protected void startTransaction() throws Exception {
		_conn.setAutoCommit(false);
	}

	/**
	 * Ends a transaction (sends commit statement and autocommint is set to true).
	 **/
	protected void endTransaction() throws Exception {
		_conn.commit();
		_conn.setAutoCommit(true);
	}

	/**
	 * Aborts the current transaction (sends rollback and autocommit is set to true).
	 **/
	protected void abortTransaction() throws Exception {
		_conn.rollback();
		_conn.setAutoCommit(true);
	}

	/**
	 * Returns a prepared statement with string.
	 **/
	public PreparedStatement prepareStatement(String string) throws Exception {
		_statement = _conn.prepareStatement(string,ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
		return (PreparedStatement)_statement;
	}

	/**
	 * Executes an update stated in string.
	 *
	 * @param update String with the update to execute.
	 *
	 * @return The number of updated rows.
	 **/
	protected int executeUpdate(String update) throws Exception{
		_statement = _conn.createStatement();
		return _statement.executeUpdate(update);
	}

	/**
	 * Executes a query from a string.
	 *
	 * @param query String with the query to execute.
	 *
	 * @return The result of the query.
	 **/
	protected ResultSet executeQuery(String query) throws Exception {
		_statement = _conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);

		ResultSet result = _statement.executeQuery(query);
		lastResultSet = result;
		return result;
	}

	/**
	 * Executes a query from a string (forward only).
	 *
	 * @param query String with the query to execute.
	 *
	 * @return The result of the query.
	 **/
	protected ResultSet executeQueryForwardOnly(String query) throws Exception {
		_statement = _conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		return _statement.executeQuery(query);
	}


	/**
	 * @return The Database metaData.
	 **/
	protected DatabaseMetaData getMetaData() {

		try {
			return _conn.getMetaData();
		}
		catch (SQLException e) {
			log.error("Exception:"+ e.getMessage());
		}
		return null;
	}

	/**
	 * Closes the last open statement. Must be called after every
	 * update or every query!
	 **/
	protected void clear() throws Exception {

		try {
			if (lastResultSet != null) {
				lastResultSet.close();
				lastResultSet = null;
			}
		} catch (java.sql.SQLException e) {
			log.error("Exception: " + e.getMessage());
		}

		if (_statement != null)
			_statement.close();
	}
}
