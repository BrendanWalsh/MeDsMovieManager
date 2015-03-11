/**
 * @(#)DatabaseAccess.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.database;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;


public class DatabaseAccess extends Database {
    
	Logger log = Logger.getLogger(getClass());
    
    public DatabaseAccess(String filePath) {
	super(filePath);
	databaseType = "MSAccess";
	
	_sql = new SQL(filePath, databaseType);
    }
    
    
    /* Removes the Movie */
    public int removeMovie(int index) {
	
	int value = 0;
	value += removeMovieRecord(index);
	value += removeListRecord(index);
	
	if (value == 2)
	    value = 0;
	
	return value;
    }
    
    /**
     * Removes the movie record from the database (Additional Info/Extra Info automatically removed with cascade delete).
     **/
    protected int removeMovieRecord(int index) {
	int value = 0;
	try {
	    _sql.executeUpdate("DELETE FROM [General Info] "+
			       "WHERE [General Info]![id]="+index+";");
	    value = 1;
	} catch (Exception e) {
	    log.error("", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	/* Returns the number of removed rows... */
	return value;
    }
    
    
    /**
     * Removes the movie record from the list table and returns number of updated rows.
     **/
    protected int removeListRecord(int index) {
	int value = 0;
	try {
	    _sql.executeUpdate("DELETE FROM [Lists] "+
			       "WHERE [Lists]![id]="+index+";");
	    value = 1;
	} catch (Exception e) {
	    log.error("Exception: ", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
    /* Returns the number of removed rows... */
    return value;
    }
    
    
    /**
     * Removes the episode from the database.
     **/
    public int removeEpisode(int index) {
	int value = 3;
	try {
	    int ret = _sql.executeUpdate("DELETE FROM [General Info Episodes] "+
					 "WHERE [General Info Episodes]![id]="+index+";");
	    
	    value --; 
	    
	    _sql.clear();
	    
	    _sql.executeUpdate("DELETE FROM [Additional Info Episodes] "+
			       "WHERE [Additional Info Episodes]![id]="+index+";");
	    value --; 
	    
	    _sql.clear();

	    _sql.executeUpdate("DELETE FROM [Extra Info Episodes] "+
			       "WHERE [Extra Info Episodes]![id]="+index+";");
	    value --; 
	    
	} catch (Exception e) {
	    log.error("Exception: ", e);
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
    

    private int updateDatabaseVersion180() {
	
	int value = 1;
	try {
	    _sql.executeUpdate("ALTER TABLE [General Info] "+
			       "ADD COLUMN [Aka] TEXT, [Country] TEXT, [Language] TEXT, [Colour] TEXT");
	    
	} catch (Exception e) {
	    log.error("Exception1: " + e.getMessage());
	    value = 2;
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
     * Updates the database to be functional with the version 2.1
     **/
    private int updateDatabaseVersion21() {
	
	int value = 1;
	
	/*Add colums Certification to table General Info*/
	try {
	    _sql.executeUpdate("ALTER TABLE [General Info] "+
			       "ADD COLUMN [Certification] MEMO, [Sound Mix] MEMO");
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    value = 2;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	
	/*Add colums Container, Location, File Count and Media Type to table additional info*/
	try {
	    _sql.executeUpdate("ALTER TABLE [Additional Info] "+
			       "ADD COLUMN [File Location] MEMO, [File Count] NUMBER, [Container] TEXT, [Media Type] MEMO");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    value = 3;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	
	
	try {
	    _sql.executeUpdate("ALTER TABLE [Additional Info] "+
			       "ALTER COLUMN [Audio Channels] MEMO");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    value = 4;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e.getMessage());
	    }
	}
	
	
	try {
	    _sql.executeUpdate("CREATE TABLE [Settings] ([id] AutoIncrement, "+
			       "[Active Additional Info Fields] MEMO)");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    value = 5;
	}		
	finally {
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
     * Updates the database to be functional with the version 2.2
     **/
    private int updateDatabaseVersion22() {
	
	int value = 1;
	
	try {
	    
	    _sql.executeUpdate("CREATE TABLE [Additional Info Episodes] ("+
			       "[id]               COUNTER PRIMARY KEY,"+
			       "[Subtitles]        MEMO,"+
			       "[Duration]         INTEGER,"+
			       "[File Size]        INTEGER,"+
			       "[CDs]              INTEGER,"+
			       "[CD Cases]         DOUBLE,"+
			       "[Resolution]       TEXT,"+
			       "[Video Codec]      MEMO,"+
			       "[Video Rate]       TEXT,"+
			       "[Video Bit Rate]   TEXT,"+
			       "[Audio Codec]      MEMO,"+
			       "[Audio Rate]       TEXT,"+
			       "[Audio Bit Rate]   TEXT,"+
			       "[Audio Channels]   TEXT,"+
			       "[File Location]    MEMO,"+
			       "[File Count]       INTEGER,"+
			       "[Container]        TEXT,"+
			       "[Media Type]       TEXT"+
			       ");");
	    
	    _sql.clear();
	    
	    _sql.executeUpdate("CREATE TABLE [Extra Info Episodes] ("+
			       "[id]               COUNTER PRIMARY KEY"+
			       ");");
	    
	    _sql.clear();
	    
	    _sql.executeUpdate("CREATE TABLE [General Info Episodes] ("+
			       "[id] COUNTER PRIMARY KEY, "+
			       "[movieID]           Integer, "+
			       "[episodeNr]         Integer, "+
			       "[Title]             Memo, "+
			       "[Cover]             Memo, "+
			       "[UrlKey]            Text, "+
			       "[Date]              Text, "+
			       "[Directed By]       Memo, "+
			       "[Written By]        Memo, "+
			       "[Genre]             Memo, "+
			       "[Rating]            Text, "+
			       "[Seen]              BIT, "+
			       "[Plot]              Memo, "+
			       "[Cast]              Memo, "+
			       "[Notes]             Memo, "+
			       "[Aka]               Text, "+
			       "[Country]           Text, "+
			       "[Language]          Text, "+
			       "[Colour]            Text, "+
			       "[Certification]     Memo, "+
			       "[Sound Mix]         Memo"+
			       ");");
	    
	    _sql.clear();

	    _sql.executeUpdate("CREATE TABLE [Lists] ("+
				       "[id] COUNTER PRIMARY KEY "+
				       ");");
	    
	    _sql.clear();
	    
	    _sql.executeUpdate("INSERT INTO [Lists](id)"+
			       "SELECT (id) FROM [Extra Info];");
	    
	    _sql.clear();

	    ArrayList<String> columns = getExtraInfoFieldNames(true);
	    String field;
	    /* Copying existing extra info table columns to the extra info episode table */
	    
	    while (!columns.isEmpty()) {
		
		field = (String) columns.get(0);
		columns.remove(0);
		
		try {
		    _sql.executeUpdate("ALTER TABLE [Extra Info Episodes] "+
				       "ADD COLUMN ["+field+"] TEXT;");
		    
		} catch (Exception e) {
		    log.error("Exception: " + e.getMessage());
		    value = 3;
		} finally {
		    /* Clears the Statement in the dataBase... */
		    try {
			_sql.clear();
		    } catch (Exception e) {
			log.error("Exception: " + e.getMessage());
		    }
		}
	    }
	    
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    value = 5;
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
     * Updates the database to be functional with the version 2.1
     **/
    private int updateDatabaseVersion23() {
	
	int value = 1;
	
	/* Changing data type on column 'Aka' from TEXT to MEMO */
	try {
	    _sql.executeUpdate("ALTER TABLE [General Info] "+
			       "ALTER COLUMN [Aka] MEMO "+
			       ";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    value = 2;
	} finally {
		    /* Clears the Statement in the dataBase... */
		    try {
			_sql.clear();
		    } catch (Exception e) {
			log.error("Exception: " + e.getMessage());
		    }
		}
	
	/* Changing data type on column 'Country' from TEXT to MEMO */
	try {
	    _sql.executeUpdate("ALTER TABLE [General Info] "+
			       "ALTER COLUMN [Country] MEMO "+
			       ";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    value = 4;
	} finally {
		    /* Clears the Statement in the dataBase... */
		    try {
			_sql.clear();
		    } catch (Exception e) {
			log.error("Exception: " + e.getMessage());
		    }
		}
	
	/* Changing data type on column 'Language' from TEXT to MEMO */
	try {
	    _sql.executeUpdate("ALTER TABLE [General Info] "+
			       "ALTER COLUMN [Language] MEMO "+
			       ";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    value = 6;
	} finally {
		    /* Clears the Statement in the dataBase... */
		    try {
			_sql.clear();
		    } catch (Exception e) {
			log.error("Exception: " + e.getMessage());
		    }
		}
	
	/* Changing data type on column 'Colour' from TEXT to MEMO */
	try {
	    _sql.executeUpdate("ALTER TABLE [General Info] "+
			       "ALTER COLUMN [Colour] MEMO "+
			       ";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    value = 8;
	} finally {
		    /* Clears the Statement in the dataBase... */
		    try {
			_sql.clear();
		    } catch (Exception e) {
			log.error("Exception: " + e.getMessage());
		    }
		}
	
	
	/* Changing data type on column 'Aka' from TEXT to MEMO */
	try {
	    _sql.executeUpdate("ALTER TABLE [General Info Episodes] "+
			       "ALTER COLUMN [Aka] MEMO "+
			       ";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    value = 3;
	} finally {
		    /* Clears the Statement in the dataBase... */
		    try {
			_sql.clear();
		    } catch (Exception e) {
			log.error("Exception: " + e.getMessage());
		    }
		}
	
	/* Changing data type on column 'Country' from TEXT to MEMO */
	try {
	    _sql.executeUpdate("ALTER TABLE [General Info Episodes] "+
			       "ALTER COLUMN [Country] MEMO "+
			       ";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    value = 5;
	} finally {
		    /* Clears the Statement in the dataBase... */
		    try {
			_sql.clear();
		    } catch (Exception e) {
			log.error("Exception: " + e.getMessage());
		    }
		}
	
	/* Changing data type on column 'Language' from TEXT to MEMO */
	try {
	    _sql.executeUpdate("ALTER TABLE [General Info Episodes] "+
			       "ALTER COLUMN [Language] MEMO "+
			       ";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    value = 7;
	} finally {
		    /* Clears the Statement in the dataBase... */
		    try {
			_sql.clear();
		    } catch (Exception e) {
			log.error("Exception: " + e.getMessage());
		    }
		}

	/* Changing data type on column 'Colour' from TEXT to MEMO */
	try {
	    _sql.executeUpdate("ALTER TABLE [General Info Episodes] "+
			       "ALTER COLUMN [Colour] MEMO "+
			       ";");
	} catch (Exception e) {
	    log.error("Exception: " + e.getMessage());
	    value = 9;
	} finally {
		    /* Clears the Statement in the dataBase... */
		    try {
			_sql.clear();
		    } catch (Exception e) {
			log.error("Exception: " + e.getMessage());
		    }
		}
	
	
	/* Adding the columns  'Mpaa', 'Web Runtime', 'Awards' and 'Personal Rating' */
	try {
	    _sql.executeUpdate("ALTER TABLE [General Info] "+
			       "ADD COLUMN [Sound Mix] MEMO "+
			       ";");
	    
	} catch (Exception e) {
	    
	    if (e.getMessage().indexOf("already exists in table") == -1) {
		log.error("Exception: " + e.getMessage());
		value = 10;
	    }
	} finally {
		    /* Clears the Statement in the dataBase... */
		    try {
			_sql.clear();
		    } catch (Exception e) {
			log.error("Exception: " + e.getMessage());
		    }
		}
	
	/* Adding the columns  'Mpaa', 'Web Runtime', 'Awards' and 'Personal Rating' */
	try {
	    _sql.executeUpdate("ALTER TABLE [General Info] "+
			       "ADD COLUMN [Mpaa] MEMO, [Web Runtime] MEMO, [Awards] MEMO, [Personal Rating] MEMO "+
			       ";");
	    
	} catch (Exception e) {
	    
	    if (e.getMessage().indexOf("already exists in table") == -1) {
		log.error("Exception: " + e.getMessage());
		value = 10;
	    }
	} finally {
		    /* Clears the Statement in the dataBase... */
		    try {
			_sql.clear();
		    } catch (Exception e) {
			log.error("Exception: " + e.getMessage());
		    }
		}
	
	/* Adding the columns  'Web Runtime', 'Awards' and 'Personal Rating' */
	try {
	    _sql.executeUpdate("ALTER TABLE [General Info Episodes] "+
			       "ADD COLUMN [Web Runtime] MEMO, [Awards] MEMO, [Personal Rating] MEMO "+
			       ";");
	} catch (Exception e) {
	    
	    if (e.getMessage().indexOf("already exists in table") == -1) {
		log.error("Exception: " + e.getMessage());
		value = 11;
	    }
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


    public int makeDatabaseUpToDate() {

    	int ret = 0;

    	if (isDatabaseOldVersion180()) {
    		log.info("Updating database to be v1.80 compatible");

    		if ((ret = updateDatabaseVersion180()) != 1) {
    			errorMessage = "<html>An error occured while updating database to be v1.80 compatible.<br>Errorcode:"+ ret+ "<html>";
    			log.warn(errorMessage);
    			return ret;
    		}
    	}
    	if (isDatabaseOldVersion21()) {
    		log.info("Updating database to be v2.1 compatible");

    		if ((ret = updateDatabaseVersion21()) != 1) {
    			errorMessage = "<html>An error occured while updating database to be v2.1 compatible.<br>Errorcode:"+ ret+ "<html>";
    			log.warn(errorMessage);
    			return ret;
    		}
    	}
    	if (isDatabaseOldVersion22()) {
    		log.info("Updating database to be v2.2 compatible");

    		if ((ret = updateDatabaseVersion22()) != 1) {
    			errorMessage = "<html>An error occured while updating database to be v2.2 compatible.<br>Errorcode:"+ ret+ "<html>";
    			log.warn(errorMessage);
    			return ret;
    		}
    	}

    	if (isDatabaseOldVersion23()) {
    		log.info("Updating database to be v2.3 compatible");

    		if ((ret = updateDatabaseVersion23()) != 1) {
    			errorMessage = "<html>An error occured while updating database to be v2.3 compatible.<br>Errorcode:"+ ret+ "<html>";
    			log.warn(errorMessage);
    			return ret;
    		}
    	}
    	return 1;
    }


    /**
     * Checks if fields added in v1.80 exists
     **/
    private boolean isDatabaseOldVersion180() {
    	boolean old = false;

    	ArrayList<String> fieldNames = getGeneralInfoMovieFieldNames();

    	if (!fieldNames.contains("Aka"))
    		old = true;

    	return old;
    }


    /**
     * Checks if fields added in v2.1 exists
     **/
    private boolean isDatabaseOldVersion21() {
    	boolean old = false;

    	ArrayList<String> columnNames = getAdditionalInfoFieldNames();

    	if (!columnNames.contains("Container"))
    		old = true;

    	return old;
    }

    /**
     * Checks if fields added in v2.2 exists
     **/
    private boolean isDatabaseOldVersion22() {
    	boolean old = false;

    	ArrayList<String> tableNames = getTableNames();

    	if (!tableNames.contains("General Info Episodes"))
    		old = true;

    	return old;
    }

    /**
     * Checks if fields added in v2.3 exists
     **/
    private boolean isDatabaseOldVersion23() {
    	boolean old = false;

    	ArrayList<String> fieldNames = getGeneralInfoMovieFieldNames();

    	if (!fieldNames.contains("Awards") || !fieldNames.contains("Sound Mix"))
    		old = true;

    	return old;
    }

    public boolean isDatabaseOld() {

    	getTableNames();

    	if (isDatabaseOldVersion23() || isDatabaseOldVersion22() || isDatabaseOldVersion21() || isDatabaseOldVersion180())
    		return true;

    	return false;
    }


    public void deleteDatabase() {

    	finalizeDatabase();

    	try {
    		File f = new File(getPath());
    		f.delete();

    	} catch (Exception e) {
    		log.error("", e);
    	}
    }

}






