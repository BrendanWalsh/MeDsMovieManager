/**
 * @(#)DatabaseHSQL.java 1.0 26.09.06 (dd.mm.yy)
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.util.FileUtil;


public class DatabaseHSQL extends Database {
    
	Logger log = Logger.getLogger(getClass());
	
	public DatabaseHSQL(String filePath) {
		super(filePath);
		databaseType = "HSQL";
		_sql = new SQL(filePath, "HSQL");
	}
    
    /**
     * SetUp...
     **/
    public boolean setUp() {

    	String message = "";

    	Exception except = null;
    	try {
    		if (!_initialized) {
    			_sql.setUp();
    			_initialized = true;
    			setUp = true;
    		}
    	} catch (Exception e) {
    		log.warn(e);
    		message = e.getMessage();
    		e.printStackTrace();
    		
    		exception = except = e;
    		errorMessage = e.getMessage();
    		_initialized = false;
    	}

    	/* If the moviemanager was shut down improperly 
	   	the db files can't be opened in a few seconds
	   	Tries again after sleeping. */
    	String eMessage = "The database is already in use by another process";

    	if (!_initialized && except.getMessage().startsWith(eMessage)) {

    		errorMessage = eMessage;

    		for (int i = 2; i < 3; i++) {
    			try {
    				Thread.sleep(2000);
    				if (!_initialized) {
    					_sql.setUp();
    					_initialized = true;
    					errorMessage = "";
    					log.info("Succsesfully connected to HSQL database on the " +i+ "th try");
    					break;
    				}
    			} catch (Exception e) {
    				log.warn("Exception:" + e.getMessage());
    				_initialized = false;
    			}
    		}
    	}
        	
    	return _initialized;
    }

    
    /**
     * Removes the movie from the general info table (Additional info/ extra info nad lists are removed by cascade delete) at index 'index' and returns number of updated rows.
     **/
    public int removeMovie(int index) {
	int value = 1;
	try {
	    value = _sql.executeUpdate("DELETE FROM \"General Info\" "+
				       "WHERE \"General Info\".\"ID\"="+index+";");
	    value = 0;
	} catch (Exception e) {
	    log.error("", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("Exception: " + e);
	    }
	}
    /* Returns the number of removed rows... */
    return value;
    }
    
    /**
     * Removes the movie from the database and returns number of updated rows.
     **/
    public int removeEpisode(int index) {
	int value = 1;
	try {
	    value = _sql.executeUpdate("DELETE FROM \"General Info Episodes\" "+
				       "WHERE \"General Info Episodes\".\"ID\"="+index+";");
	    value = 0;
	    
	} catch (Exception e) {
	    log.error("", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
	    }
	}
    /* Returns the number of removed rows... */
    return value;
    }
    
    
     public int createDatabaseTables() {
	 
	 log.info("Creating database tables.");
	 
	 int result = createGeneralInfoTable();
	 
	 if (result == 0)
	     log.info("Successfully created General Info Table.");
	 else
	     log.error("Failed to create General Info Table.");
	 
	 result = createAdditionalInfoTable();
	 if (result == 0)
	     log.info("Successfully created Additional Info Table.");
	 else
	     log.error("Failed to create Additional Info Table.");
	 
	 result = createExtraInfoTable();
	 if (result == 0)
	     log.info("Successfully created Extra Info Table.");
	 else
	     log.error("Failed to create Extra Info Table.");
	 
	 result = createFoldersTable();
	 if (result == 0)
	     log.info("Successfully created Folders Table.");
	 else
	     log.error("Failed to create Folders Table.");

	 result = createSettingsTable();
	 if (result == 0)
	     log.info("Successfully created Settings Table.");
	 else
	     log.error("Failed to create Settings Table.");
	 
	 result = createGeneralInfoEpisodeTable();
	 if (result == 0)
	     log.info("Successfully created General Info Episodes Table.");
	 else
	     log.error("Failed to create General Info Episodes Table.");
	     
	 result = createAdditionalInfoEpisodeTable();
	 if (result == 0)
	     log.info("Successfully created Additional Info Episodes Table.");
	 else
	     log.error("Failed to create Additional Info Episodes Table.");

	 result = createExtraInfoEpisodeTable();
	 if (result == 0)
	     log.info("Successfully created Extra Info Episodes Table.");
	 else
	     log.error("Failed to create Extra Info Episodes Table.");
	     
	 result = createListsTable();
	 if (result == 0)
	     log.info("Successfully created Lists Table.");
	 else
	     log.error("Failed to create Lists Table.");
	     
	 result = createRelationShipMovie();
	 if (result == 0)
	     log.info("Successfully created movie relationship.");
	 else
	     log.error("Failed to create movie relationship.");
	 
	 result = createRelationShipEpisodes();
	 if (result == 0)
	     log.info("Successfully created episodes relationship.");
	 else
	     log.error("Failed to create episodes relationship.");
	 
	 return 1;
     }
    

    /**
     * Creates the General Info table with columns
     **/
    int createGeneralInfoTable() {
	
	int value = 0;
	
	try {
	    value = _sql.executeUpdate("CREATE TABLE \"General Info\" ("+
				       "ID                    INTEGER NOT NULL IDENTITY,"+
				       "\"Title\"             VARCHAR_IGNORECASE,"+
				       "\"Cover\"             LONGVARCHAR,"+
				       "\"Imdb\"              VARCHAR,"+
				       "\"Date\"              VARCHAR,"+
				       "\"Directed By\"       VARCHAR_IGNORECASE,"+
				       "\"Written By\"        LONGVARCHAR,"+
				       "\"Genre\"             LONGVARCHAR,"+
				       "\"Rating\"            DOUBLE,"+
				       "\"Seen\"              BOOLEAN,"+
				       "\"Plot\"              LONGVARCHAR,"+
				       "\"Cast\"              LONGVARCHAR,"+
				       "\"Notes\"             LONGVARCHAR,"+
				       "\"Aka\"               LONGVARCHAR,"+
				       "\"Country\"           LONGVARCHAR,"+
				       "\"Language\"          LONGVARCHAR,"+
				       "\"Colour\"            LONGVARCHAR,"+
				       "\"Certification\"     LONGVARCHAR,"+
				       "\"Mpaa\"              LONGVARCHAR,"+
				       "\"Sound Mix\"         LONGVARCHAR,"+
				       "\"Web Runtime\"       LONGVARCHAR,"+
				       "\"Awards\"            LONGVARCHAR,"+
				       "\"Personal Rating\"   DOUBLE"+
				       ");");
	    
	} catch (Exception e) {
	    log.error("", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
	    }
	}
    /* Returns the number of altered rows... */
    return value;
    }
    
    
    /**
     * Creates the Additional Info table with columns
     **/
    int createAdditionalInfoTable() {
	
	int value = 0;
	
	try {
	    value = _sql.executeUpdate("CREATE TABLE \"Additional Info\" ("+
				       "ID                   INTEGER NOT NULL IDENTITY,"+
				       "\"Subtitles\"        LONGVARCHAR,"+
				       "\"Duration\"         INTEGER,"+
				       "\"File Size\"        INTEGER,"+
				       "\"CDs\"              INTEGER,"+
				       "\"CD Cases\"         DOUBLE,"+
				       "\"Resolution\"       VARCHAR,"+
				       "\"Video Codec\"      LONGVARCHAR,"+
				       "\"Video Rate\"       VARCHAR,"+
				       "\"Video Bit Rate\"   VARCHAR,"+
				       "\"Audio Codec\"      LONGVARCHAR,"+
				       "\"Audio Rate\"       VARCHAR,"+
				       "\"Audio Bit Rate\"   VARCHAR,"+
				       "\"Audio Channels\"   VARCHAR,"+
				       "\"File Location\"    VARCHAR,"+
				       "\"File Count\"       INTEGER,"+
				       "\"Container\"        VARCHAR,"+
				       "\"Media Type\"       VARCHAR"+
				       ");");
	    
	} catch (Exception e) {
	    log.error("", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
	    }
	}
    /* Returns the number of altered rows... */
    return value;
    }
    
    
    /**
     * Creates the Extra Info table with columns
     **/
    int createExtraInfoTable() {
	int value = 0;
	 
	try {
	    value = _sql.executeUpdate("CREATE TABLE \"Extra Info\" ("+
				       "ID       INTEGER NOT NULL IDENTITY"+
				       ");");
	} catch (Exception e) {
	    log.error("", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
	    }
	}
    /* Returns the number of altered rows... */
    return value;
    }
    
    
     /**
     * Creates the General Info table with columns
     **/
    int createGeneralInfoEpisodeTable() {
	
	int value = 0;
	
	try {
	    value = _sql.executeUpdate("CREATE TABLE \"General Info Episodes\" ("+
				       "ID                    INTEGER NOT NULL IDENTITY,"+
				       "\"movieID\"           INTEGER, "+
				       "\"episodeNr\"         INTEGER, "+
				       "\"Title\"             VARCHAR_IGNORECASE,"+
				       "\"Cover\"             LONGVARCHAR,"+
				       "\"UrlKey\"            VARCHAR,"+
				       "\"Date\"              VARCHAR,"+
				       "\"Directed By\"       VARCHAR_IGNORECASE,"+
				       "\"Written By\"        LONGVARCHAR,"+
				       "\"Genre\"             LONGVARCHAR,"+
				       "\"Rating\"            DOUBLE,"+
				       "\"Seen\"              BOOLEAN,"+
				       "\"Plot\"              LONGVARCHAR,"+
				       "\"Cast\"              LONGVARCHAR,"+
				       "\"Notes\"             LONGVARCHAR,"+
				       "\"Aka\"               LONGVARCHAR,"+
				       "\"Country\"           LONGVARCHAR,"+
				       "\"Language\"          LONGVARCHAR,"+
				       "\"Colour\"            LONGVARCHAR,"+
				       "\"Certification\"     LONGVARCHAR,"+
				      // "\"Mpaa\" 			  LONGVARCHAR,"+ 
				       "\"Sound Mix\"         LONGVARCHAR,"+
				       "\"Web Runtime\"       LONGVARCHAR,"+
				       "\"Awards\"            LONGVARCHAR,"+
				       "\"Personal Rating\"   DOUBLE"+
				       ");");
	    
	} catch (Exception e) {
	    log.error("", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
	    }
	}
    /* Returns the number of altered rows... */
    return value;
    }
    
    
    
    /**
     * Creates the Additional Info table with columns
     **/
    int createAdditionalInfoEpisodeTable() {
	
	int value = 0;
	
	try {
	    value = _sql.executeUpdate("CREATE TABLE \"Additional Info Episodes\" ("+
				       "ID                   INTEGER NOT NULL IDENTITY,"+
				       "\"Subtitles\"        LONGVARCHAR,"+
				       "\"Duration\"         INTEGER,"+
				       "\"File Size\"        INTEGER,"+
				       "\"CDs\"              INTEGER,"+
				       "\"CD Cases\"         DOUBLE,"+
				       "\"Resolution\"       VARCHAR,"+
				       "\"Video Codec\"      LONGVARCHAR,"+
				       "\"Video Rate\"       VARCHAR,"+
				       "\"Video Bit Rate\"   VARCHAR,"+
				       "\"Audio Codec\"      LONGVARCHAR,"+
				       "\"Audio Rate\"       VARCHAR,"+
				       "\"Audio Bit Rate\"   VARCHAR,"+
				       "\"Audio Channels\"   VARCHAR,"+
				       "\"File Location\"    VARCHAR,"+
				       "\"File Count\"       INTEGER,"+
				       "\"Container\"        VARCHAR,"+
				       "\"Media Type\"       VARCHAR"+
				       ");");
	    
	} catch (Exception e) {
	    log.error("", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
	    }
	}
    /* Returns the number of altered rows... */
    return value;
    }
    
    
    
    /**
     * Creates the Extra Info table with columns
     **/
    int createExtraInfoEpisodeTable() {
	int value = 0;
	 
	try {
	    value = _sql.executeUpdate("CREATE TABLE \"Extra Info Episodes\" ("+
				       "ID       INTEGER NOT NULL IDENTITY"+
				       ");");
	} catch (Exception e) {
	    log.error("", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
	    }
	}
    /* Returns the number of altered rows... */
    return value;
    }
    
    
    /**
     * Creates the Folders table with columns
     **/
    int createFoldersTable() {
	
	int value = 0;
	
	try {
	    value = _sql.executeUpdate("CREATE TABLE \"Folders\" ("+
				       "ID                                 INTEGER NOT NULL IDENTITY,"+
				       "\"Covers\"                         VARCHAR,"+
				       "\"Queries\"                        VARCHAR"+
				       ");");
	    
	} catch (Exception e) {
	    log.error("", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
	    }
	}
    /* Returns the number of altered rows... */
    return value;
    }
    
    
    /**
     * Creates the Settings table with columns
     **/
    int createSettingsTable() {
	 
	int value = 0;
	
	try {
	    value = _sql.executeUpdate("CREATE TABLE \"Settings\" ("+
				       "ID                                         INTEGER NOT NULL IDENTITY, "+
				       "\"Active Additional Info Fields\"          VARCHAR, "+
				       "\"General Info Settings\"                  VARCHAR"+
				       ");");
	    
	} catch (Exception e) {
	    log.error("", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
	    }
	}
    /* Returns the number of altered rows... */
    return value;
    }
    
    

    /**
     * Creates the Settings table with columns
     **/
    int createListsTable() {
	 
	int value = 0;
	
	try {
	   value = _sql.executeUpdate("CREATE TABLE \"Lists\" ("+
				      "\"ID\"      INTEGER NOT NULL IDENTITY"+
				      ");");
	    
	} catch (Exception e) {
	    log.error("", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
	    }
	}
    /* Returns the number of altered rows... */
    return value;
    }
    
    
    
     /**
     * Creates the Settings table with columns
     **/
    int createRelationShipMovie() {
	
	int value = 0;
	
	try {
	    value = _sql.executeUpdate("ALTER TABLE \"Lists\" ADD CONSTRAINT lists "+
				       "FOREIGN KEY (ID) REFERENCES \"General Info\"(ID) "+
				       "ON DELETE CASCADE"+
				       ";");
	    
	    _sql.clear();
	    
	    value = _sql.executeUpdate("ALTER TABLE \"Extra Info\" ADD CONSTRAINT extrainfo "+
				       "FOREIGN KEY (ID) REFERENCES \"General Info\"(ID) "+
				       "ON DELETE CASCADE"+
				       ";");
	    
	    _sql.clear();

	    value = _sql.executeUpdate("ALTER TABLE \"Additional Info\" ADD CONSTRAINT additionalinfo "+
				       "FOREIGN KEY (ID) REFERENCES \"General Info\"(ID) "+
				       "ON DELETE CASCADE"+
				       ";");
	    
	    
	} catch (Exception e) {
	    log.error("", e);
	    value = -1;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = -1;
	    }
	}
    /* Returns the number of altered rows... */
    return value;
    }
    
    
     /**
     * Creates the Settings table with columns
     **/
    int createRelationShipEpisodes() {
	
	int value = 0;
	
	try {
	    value = _sql.executeUpdate("ALTER TABLE \"Additional Info Episodes\" ADD CONSTRAINT episodes1 "+
				       "FOREIGN KEY (ID) REFERENCES \"General Info Episodes\"(ID) "+
				       "ON DELETE CASCADE"+
				       ";");
	    
	    _sql.clear();
	    
	    value = _sql.executeUpdate("ALTER TABLE \"Extra Info Episodes\" ADD CONSTRAINT episodes2 "+
				       "FOREIGN KEY (ID) REFERENCES \"General Info Episodes\"(ID) "+
				       "ON DELETE CASCADE"+
				       ";");
	    
	} catch (Exception e) {
	    value = -1;
	    log.error("", e);
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		value = -1;
		log.error("", e);
	    }
	}
    /* Returns the number of altered rows... */
    return value;
    }
    
    
    /**
     * Updates the database to be functional with version 2.3
     **/
    private int updateDatabaseVersion23() {
	
	int value = 1;
	
	/* Adding the column 'Mpaa' */
	try {
	    _sql.executeUpdate("ALTER TABLE \"General Info\" "+
			       "ADD COLUMN \"Mpaa\" LONGVARCHAR "+
			       ";");
	} catch (Exception e) {
	    
	    if (e.getMessage().indexOf("already exists in") == -1) {
		value = 2;
		log.error("", e);
	    }
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		value = -1;
		log.error("", e);
	    }
	}
	
	
	/* Adding the column 'Web Runtime' */
	try {
	    _sql.executeUpdate("ALTER TABLE \"General Info\" "+
			       "ADD COLUMN \"Web Runtime\" LONGVARCHAR "+
			       ";");
	} catch (Exception e) {
	    
	    if (e.getMessage().indexOf("already exists in") == -1) {
		value = 2;
		log.error("", e);
	    }
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		value = -1;
		log.error("", e);
	    }
	}
	
	/* Adding the column 'Awards' */
	try {
	    _sql.executeUpdate("ALTER TABLE \"General Info\" "+
			       "ADD COLUMN \"Awards\" LONGVARCHAR "+
			       ";");
	} catch (Exception e) {
	    
	    if (e.getMessage().indexOf("already exists in") == -1) {
		log.error("", e);
		value = 3;
	    }
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = -1;
	    }
	}
	
	/* Adding the column 'Personal Rating' */
	try {
	    _sql.executeUpdate("ALTER TABLE \"General Info\" "+
			       "ADD COLUMN \"Personal Rating\" DOUBLE "+
			       ";");
	} catch (Exception e) {
	    
	    if (e.getMessage().indexOf("already exists in") == -1) {
		log.error("", e);
		value = 4;
	    }
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = -1;
	    }
	}
	
	/* Adding the columns 'Web Runtime'*/
	try {
	    _sql.executeUpdate("ALTER TABLE \"General Info Episodes\" "+
			       "ADD COLUMN \"Web Runtime\" LONGVARCHAR "+
			       ";");
	} catch (Exception e) {
	    
	    if (e.getMessage().indexOf("already exists") == -1) {
		log.error("", e);
		value = 5;
	    }
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = -1;
	    }
	}
	
	/* Adding the columns 'Awards */
	try {
	    _sql.executeUpdate("ALTER TABLE \"General Info Episodes\" "+
			       "ADD COLUMN \"Awards\" LONGVARCHAR "+
			       ";");
	} catch (Exception e) {
	    
	    if (e.getMessage().indexOf("already exists") == -1) {
		log.error("", e);
		value = 6;
	    }
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = -1;
	    }
	}
	
	/* Adding the column 'Personal Rating' */
	try {
	    _sql.executeUpdate("ALTER TABLE \"General Info Episodes\" "+
			       "ADD COLUMN \"Personal Rating\" DOUBLE "+
			       ";");
	} catch (Exception e) {
	    
	    if (e.getMessage().indexOf("already exists") == -1) {
		log.error("", e);
		value = 7;
	    }
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = -1;
	    }
	}
	
	
	/* Adding the column 'General Info Settings */
	try {
	    _sql.executeUpdate("ALTER TABLE \"Settings\" "+
			       "ADD COLUMN \"General Info Settings\" VARCHAR "+
			       ";");
	} catch (Exception e) {
	    
	    if (e.getMessage().indexOf("already exists") == -1) {
		log.error("", e);
		value = 8;
	    }
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = 8;
	    }
	}
	return value;
    }
    
    
    
     /**
     * Updates the database to be functional with version 2.2
     **/
    private int updateDatabaseVersion22() {
	
	int value = 1;
	
	try {
	    createGeneralInfoEpisodeTable();
	    createAdditionalInfoEpisodeTable();
	    createExtraInfoEpisodeTable();
	    createListsTable();
	    
	    createRelationShipEpisodes();
	    createRelationShipMovie();
	    
	} catch (Exception e) {
	    log.error("", e);
	    value = 2;
	}
	
	ArrayList<String> columns = getExtraInfoFieldNames(true);
	String field;
	
	/* Copying existing extra info table columns to the extra info episode table */
	while (!columns.isEmpty()) {
	    
	    field = (String) columns.get(0);
	    columns.remove(0);
	    
	    try {
		_sql.executeUpdate("ALTER TABLE \"Extra Info Episodes\" "+
				   "ADD COLUMN \""+field+"\" LONGVARCHAR;");
		
	    } catch (Exception e) {
		
		if (e.getMessage().indexOf("already exists") == -1) {
		    log.error("", e);
		    value = 3;
		}
	    } finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = -1;
	    }
	}
	}
	
	/* Adding all the values from the Extra Info ID column to Lists ID column  */
	try {
	    _sql.executeUpdate("INSERT INTO \"Lists\"(id)"+
			       "SELECT (id) FROM \"Extra Info\""+
			       ";");
	    
	} catch (Exception e) {
	    
	    if (e.getMessage().indexOf("already exists") == -1) {
		log.error("", e);
		value = 4;
	    } 
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = 5;
	    }
	}
	return value;
    }
    
    
    
    /**
     * Updates the database to be functional with the version 2.1
     **/
    private int updateDatabaseVersion21() {
	
	int value = 1;
	
	/* Renames the "Folders" table to "Folders" */
	try {
	    _sql.clear();
	    _sql.executeUpdate("ALTER TABLE FOLDERS RENAME TO \"Folders\";");
	    
	} catch (Exception e) {
	    log.error("", e);
	    value = 2;
	    return value;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = 5;
	    }
	}
	
	createSettingsTable();
	
	String [] tableNames = new String[] {"\"General Info\"","\"General Info\"","\"General Info\"","\"General Info\"","\"General Info\"","\"General Info\"","\"General Info\"","\"General Info\"","\"General Info\"","\"General Info\"","\"General Info\"","\"General Info\"","\"General Info\"","\"Additional Info\"","\"Additional Info\"","\"Additional Info\"","\"Additional Info\""," \"Folders\""," \"Folders\""};
	
	String [] columNames = new String[] {"TITLE","COVER", "IMDB","DATE","GENRE","RATING","SEEN","PLOT","NOTES","AKA","COUNTRY","LANGUAGE","COLOUR","SUBTITLES","DURATION","CDS","RESOLUTION","COVERS","QUERIES"};
	
	String [] columReplacementNames = new String[] {"\"Title\"", "\"Cover\"", "\"Imdb\"","\"Date\"","\"Genre\"","\"Rating\"","\"Seen\"","\"Plot\"","\"Notes\"","\"Aka\"","\"Country\"","\"Language\"","\"Colour\"","\"Subtitles\"", "\"Duration\"", "\"CDs\"", "\"Resolution\"","\"Covers\"","\"Queries\""};
	
	for (int i = 0; i < columNames.length; i++) {
	    
	    /* Renames the columns" */
	    try {
		_sql.clear();
		_sql.executeUpdate("ALTER TABLE "+ tableNames[i]+ " "+
				   "ALTER COLUMN " + columNames[i] +" RENAME TO "+ columReplacementNames[i] +";");
	    } catch (Exception e) {
		log.error("", e);
		value = 3;
		return value;
	    } finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = 5;
	    }
	}
	}
	
	/*Add colum Certification to table General Info*/
	try {
	    _sql.executeUpdate("ALTER TABLE \"General Info\" "+
			       "ADD COLUMN \"Certification\" VARCHAR");
	} catch (Exception e) {
	    log.error("", e);
	    value = 4;
	    return value;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = 5;
	    }
	}
	
	/*Add colum Sound Mix to table General Info*/
	try {
	    _sql.executeUpdate("ALTER TABLE \"General Info\" "+
			       "ADD COLUMN \"Sound Mix\" VARCHAR");
	    
	} catch (Exception e) {
	    log.error("", e);
	    value = 5;
	    return value;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = 5;
	    }
	}
	
	/*Add colums "File Location", "Container", "File count" and "Media Type" to table additional info*/
	try {
	    _sql.clear();
	    
	    _sql.executeUpdate("ALTER TABLE \"Additional Info\" "+
			       "ADD COLUMN \"File Location\" VARCHAR");
	    
	    _sql.clear();
	    _sql.executeUpdate("ALTER TABLE \"Additional Info\" "+
			       "ADD COLUMN \"File Count\" INTEGER");
	    
	    _sql.clear();
	    _sql.executeUpdate("ALTER TABLE \"Additional Info\" "+
			       "ADD COLUMN \"Container\" VARCHAR");
	    
	    _sql.clear();
	    _sql.executeUpdate("ALTER TABLE \"Additional Info\" "+
			       "ADD COLUMN \"Media Type\" VARCHAR");
	    
	} catch (Exception e) {
	    log.error("", e);
	    value = 6;
	    return value;
	} finally {
	    /* Clears the Statement in the dataBase... */
	    try {
		_sql.clear();
	    } catch (Exception e) {
		log.error("", e);
		value = 7;
	    }
	}
	return value;
    }

    
    /**
     * Checks if the container field which is added in v2.1 exists.
     **/
    private boolean isDatabaseOldVersion21() {
    	boolean old = false;

    	ArrayList<String> fieldNames = getAdditionalInfoFieldNames();

    	if (!fieldNames.contains("Container")) {
    		old = true;
    		log.debug("isDatabaseOldVersion21:" + old);
    	}

    	return old;
    }
    
     /**
     * Checks if the container field which is added in v2.1 exists.
     **/
    private boolean isDatabaseOldVersion22() {
    	boolean old = false;

    	ArrayList<String> tableNames = getTableNames();

    	if (!tableNames.contains("General Info Episodes")) {
    		old = true;
    		log.debug("isDatabaseOldVersion22:" + old);
    	}

    	return old;
    }
    
    /**
     * Checks if the container field which is added in v2.1 exists.
     **/
    private boolean isDatabaseOldVersion23() {
    	boolean old = false;

    	ArrayList<String> fieldNames = getGeneralInfoMovieFieldNames();

    	if (!fieldNames.contains("Awards")) {
    		old = true;
    		log.debug("isDatabaseOldVersion23:" + old);
    	}

    	return old;
    }
    
    /** 
     * Checks if the database needs an update
     **/
    public boolean isDatabaseOld() {

    	if (isDatabaseOldVersion23() || isDatabaseOldVersion22() || isDatabaseOldVersion21())
    		return true;

    	return false;
    }
    
    public int makeDatabaseUpToDate() {

    	int ret = 0;

    	if (isDatabaseOldVersion21()) {
    		log.info("Updating database to be v2.1 compatible");

    		if ((ret = updateDatabaseVersion21()) != 1) {
    			errorMessage = "<html>An error occured while updating database to be v2.1 compatible.<br>Errorcode:"+ ret+ "<html>";
    			log.error(errorMessage);
    			return ret;
    		}
    	}

    	if (isDatabaseOldVersion22()) {
    		log.info("Updating database to be v2.2 compatible");

    		if ((ret = updateDatabaseVersion22()) != 1) {
    			errorMessage = "<html>An error occured while updating database to be v2.2 compatible.<br>Errorcode:"+ ret+ "<html>";
    			log.error(errorMessage);
    			return ret;
    		}
    	}

    	if (isDatabaseOldVersion23()) {
    		log.info("Updating database to be v2.3 compatible");

    		if ((ret = updateDatabaseVersion23()) != 1) {
    			errorMessage = "<html>An error occured while updating database to be v2.3 compatible.<br>Errorcode:"+ ret+ "<html>";
    			log.error(errorMessage);
    			return ret;
    		}
    	}
    	return 1;
    }
    
    public boolean updateScriptFile() {

    	String filePath = getPath();

    	try {
    		File dbPropertiesFile = new File(filePath + ".properties");
    		File dbScriptFile = new File(filePath + ".script");

    		if (!dbPropertiesFile.exists() || !dbScriptFile.exists())
    			throw new Exception("Database files do not exist.");

    		FileInputStream stream = new FileInputStream(dbScriptFile);
    		StringBuffer stringBuffer = new StringBuffer();
    		int buffer;

    		while ((buffer = stream.read()) != -1)
    			stringBuffer.append((char)buffer);
    		stream.close();

    		String [] columName = new String[] {"CAST LONGVARCHAR", "\"Audio Channels\" INTEGER"};
    		String [] columReplacementName = new String[] {"\"Cast\" LONGVARCHAR", "\"Audio Channels\" VARCHAR"};

    		int index = -1;
    		boolean updated = false;

    		for (int i = 0; i < columName.length; i++) {
    			index = stringBuffer.indexOf(columName[i]);

    			if (index != -1) {
    				stringBuffer.replace(index, index+columName[i].length(), columReplacementName[i]);
    				updated = true;
    			}
    		}

    		/* If no update it will return instead of overwriting the old file with the same data */
    		if (!updated)
    			return true;

    		if (!dbScriptFile.delete())
    			throw new Exception("Cannot delete script file.");

    		/* Recreates file... */
    		if (!dbScriptFile.createNewFile())
    			throw new Exception("Cannot create script file.");

    		/* Writes to the new script... */
    		FileOutputStream outputStream = new FileOutputStream(dbScriptFile);

    		for (int i = 0; i < stringBuffer.length(); i++)
    			outputStream.write(stringBuffer.charAt(i));
    		outputStream.close();

    	} catch (Exception e) {
    		log.error("Exception:" + e.getMessage());
    		return false;
    	}
    	return true;
    }
    
    
    public boolean isDriverOld() throws Exception {
    	
    	String filePath = getPath();
    	
    	File dbPropertiesFile = new File(filePath + ".properties");
    	
    	if (!dbPropertiesFile.exists())
			throw new Exception("properies file does not exist:" + dbPropertiesFile);
    	
    	StringBuffer properties = FileUtil.readFileToStringBuffer(dbPropertiesFile);

		// Updating from hsql v1.7 to 1.8
		if (properties.indexOf("compatible_version=1.7") != -1)
			return true;
		
		return false;
		//	MovieManager.getDatabaseHandler().makeDatabaseBackup(this, "Update_from_HSQL_1.7_to_1.8");
    }
    
    /* Have to update the script manually since hsqldb driver v1.7.3 doesn't support changing the datatype */
    public boolean isScriptOutOfDate() {

    	String filePath = getPath();

    	try {
    		File dbPropertiesFile = new File(filePath + ".properties");
    		File dbScriptFile = new File(filePath + ".script");

    		if (!dbPropertiesFile.exists())
    			throw new Exception("properies file does not exist.");
    		else if (!dbScriptFile.exists())
    			throw new Exception("script file does not exist.");

    		/*
    		StringBuffer properties = FileUtil.readFileToStringBuffer(dbPropertiesFile);

    		// Updating from hsql v1.7 to 1.8
    		if (properties.indexOf("compatible_version=1.7") != -1)
    			MovieManager.getDatabaseHandler().makeDatabaseBackup(this, "Update_from_HSQL_1.7_to_1.8");
*/
    		
    		FileInputStream stream = new FileInputStream(dbScriptFile);
    		StringBuffer stringBuffer = new StringBuffer();
    		int buffer;

    		boolean check = true;
    		int counter = 0;
    		while ((buffer = stream.read()) != -1 && counter < 1000) {
    			stringBuffer.append((char)buffer);
    		}
    		stream.close();

    		if (check && stringBuffer.indexOf("INSERT INTO") != -1) {
				
    			if ((stringBuffer.indexOf(",CAST LONGVARCHAR,") != -1) || 
    					(stringBuffer.indexOf("\"Audio Channels\" INTEGER") != -1)) {
    				return true;
    			}
    			else
    				return false;
    		}
    			
    	} catch (Exception e) {
    		log.warn("Exception:" + e.getMessage());
    	}
    	return false;
    }
    
    
    /**
     * Deletes the database files
     **/
    public void deleteDatabase() {

    	try {
    		/*Avoids possible exceptions when trying to delete the canceled database files*/
    		Thread.sleep(200);
    	} catch (Exception e) {
    		log.error("", e);
    	}

    	/*Shutting down the HSQL database to be able to delete the files*/
    	shutDownDatabase("SHUTDOWN IMMEDIATELY;");

    	super.finalizeDatabase();

    	try {
    		File f = new File(getPath()+".lck");
    		f.delete();
    		f = new File(getPath()+".properties");
    		f.delete();
    		f = new File(getPath()+".script");
    		f.delete();
    		f = new File(getPath()+".log");
    		f.delete();

    	} catch (Exception e) {
    		log.error("", e);
    	}
    }

    /* Called with query = "SHUTDOWN;" or "SHUTDOWN COMPACT; "*/
    public void shutDownDatabase(String query) {

    	try {
    		_sql.executeQuery(query);
    	} catch (Exception e) {
    		log.error("", e);
    	} finally {
    		/* Clears the Statement in the dataBase... */
    		try {
    			_sql.clear();
    		} catch (Exception e) {
    			log.error("", e);
    		}
    	}
    }
    
    /**
     * Finalize...
     **/
    public void finalizeDatabase() {
    	shutDownDatabase("SHUTDOWN COMPACT;");
    	super.finalizeDatabase();
    }
}
