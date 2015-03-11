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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.models.ModelAdditionalInfo;
import net.sf.xmm.moviemanager.models.ModelDatabaseSearch;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelMovie;

public class DatabaseMySQL extends Database {

	Logger log = Logger.getLogger(getClass());

	public DatabaseMySQL(String filePath) {
		this(filePath, false);
	}

	public DatabaseMySQL(String filePath, boolean enableSocketTimeout) {
		super(filePath);

		if (!MovieManager.getConfig().getInternalConfig().getSensitivePrintMode())
			log.debug("DatabaseMySQL - filePath:" + filePath);

		databaseType = "MySQL";
		_sql = new SQL(filePath, "MySQL", enableSocketTimeout);


		quote = "`";
		generalInfoString = "General_Info";
		additionalInfoString = "Additional_Info";
		extraInfoString = "Extra_Info";

		generalInfoEpisodeString = "General_Info_Episodes";
		additionalInfoEpisodeString = "Additional_Info_Episodes";
		extraInfoEpisodeString = "Extra_Info_Episodes";

		quotedGeneralInfoString = quote + generalInfoString + quote;
		quotedAdditionalInfoString = quote + additionalInfoString + quote;
		quotedExtraInfoString = quote + extraInfoString + quote;

		quotedGeneralInfoEpisodeString = quote + generalInfoEpisodeString + quote;
		quotedAdditionalInfoEpisodeString = quote + additionalInfoEpisodeString + quote;
		quotedExtraInfoEpisodeString = quote + extraInfoEpisodeString + quote;

		quotedListsString = quote + "Lists" + quote;

		directedByString = "Directed_By";
		writtenByString = "Written_By";

		soundMixString = "Sound_Mix";
		webRuntimeString = "Web_Runtime";
	}



	/**
	 * Returns the number of rows in the General_Info id column
	 **/
	public int getDatabaseSize() {

		int size = -1;

		try {
			/* Gets the number of rows */
			ResultSet resultSet = _sql.executeQuery("SELECT COUNT(*) FROM (SELECT alias.id FROM General_Info alias) alias2;");

			if (resultSet.next())
				size = resultSet.getInt(1);

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			size = -1;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return size;
	}


	/**
	 * Returns the Queries folder for this database.
	 **/
	public String getQueriesFolder() {

		String data = getString("SELECT Folders.Queries "+
				"FROM Folders "+
				"WHERE Folders.ID=1;", "Queries");
		/* Returns the data... */
		return data;
	}



	/**
	 * Removes the movie from the General_Info table at index 'index' and returns number of updated rows.
	 * (Additional_Info/ Extra_Info and lists are removed automatically by cascade delete)
	 **/
	public int removeMovie(int index) {
		int value = 1;
		try {
			value = _sql.executeUpdate("DELETE FROM General_Info "+
					"WHERE General_Info.ID="+index+";");
			value = 0;
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
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

	/**
	 * Removes the episode from the database.
	 **/
	public int removeEpisode(int index) {
		int value = 1;
		try {
			value = _sql.executeUpdate("DELETE FROM General_Info_Episodes "+
					"WHERE General_Info_Episodes.ID="+index+";");
			value = 0;
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
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



	/**
	 * Removes the tables
	 **/
	int removeTables() {

		int value = 0;

		try {
			value = _sql.executeUpdate("DROP TABLE additional_info, additional_info_episodes, "+
					"folders, general_info, general_info_episodes, settings,"+
					"extra_info, extra_info_episodes, lists"+
			";");
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
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
	 * Creates the database
	 **/
	public int createDatabase(String databaseName) {

		int value = 0;

		try {
			value = _sql.executeUpdate("CREATE DATABASE "+ databaseName +
			";");
			value = 1;
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			value = 0;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return value;
	}



	public int createDatabaseTables() {

		int result = createGeneralInfoTable();

		if (result == 0)
			log.info("Successfully created General_Info Table.");
		else {
			log.error("Failed to create General_Info Table.");
			return -1;
		}

		result = createAdditionalInfoTable();
		if (result == 0)
			log.info("Successfully created Additional_Info Table.");
		else {
			log.error("Failed to create Additional_Info Table.");
			return -1;
		}

		result = createExtraInfoTable();
		if (result == 0)
			log.info("Successfully created Extra_Info Table.");
		else {
			log.error("Failed to create Extra_Info Table.");
			return -1;
		}

		result = createFoldersTable();
		if (result == 0)
			log.info("Successfully created Folders Table.");
		else {
			log.error("Failed to create Folders Table.");
			return -1;
		}

		result = createSettingsTable();
		if (result == 0)
			log.info("Successfully created Settings Table.");
		else {
			log.error("Failed to create Settings Table.");
			return -1;
		}

		result = createGeneralInfoEpisodeTable();
		if (result == 0)
			log.info("Successfully created General Info_Episodes Table.");
		else {
			log.error("Failed to create General Info_Episodes Table.");
			return -1;
		}

		result = createAdditionalInfoEpisodeTable();
		if (result == 0)
			log.info("Successfully created Additional_Info Episodes Table.");
		else {
			log.error("Failed to create Additional_Info Episodes Table.");
			return -1;
		}

		result = createExtraInfoEpisodeTable();
		if (result == 0)
			log.info("Successfully created Extra_Info Episodes Table.");
		else {
			log.error("Failed to create Extra_Info Episodes Table.");
			return -1;
		}

		result = createListsTable();
		if (result == 0)
			log.info("Successfully created Lists Table.");
		else {
			log.error("Failed to created Lists Table.");
			return -1;
		}

		result = createRelationShipMovie();
		if (result == 0)
			log.info("Successfully created movie relationship.");
		else {
			log.error("Failed to create movie relationship.");
			return -1;
		}

		result = createRelationShipEpisodes(); 
		if (result == 0)
			log.info("Successfully created episodes relationship.");
		else {
			log.error("Failed to create episodes relationship.");
			return -1;
		}

		return 1;
	}


	/**
	 * Creates the General Info table with columns
	 **/
	int createGeneralInfoTable() {

		int value = 0;

		try {
			_sql.executeUpdate("CREATE TABLE General_Info ("+
					"ID                INTEGER AUTO_INCREMENT PRIMARY KEY, "+
					"Title             TEXT, "+
					"Cover             TEXT, "+
					"CoverData         BLOB, "+
					"Imdb              TEXT, "+
					"Date              TEXT, "+
					"Directed_By       TEXT, "+
					"Written_By        TEXT, "+
					"Genre             TEXT, "+
					"Rating            DOUBLE, "+
					"Seen              TINYINT(1), "+
					"Plot              TEXT, "+
					"Cast              TEXT, "+
					"Notes             TEXT, "+
					"Aka               TEXT, "+
					"Country           TEXT, "+
					"Language          TEXT, "+
					"Colour            TEXT, "+
					"Certification     TEXT, "+
					"Mpaa              TEXT, "+
					"Sound_Mix         TEXT, "+
					"Web_Runtime       TEXT, "+
					"Awards            TEXT, "+
					"Personal_Rating   DOUBLE"+
			") ENGINE=InnoDB;");

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			value = -1;
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
	 * Creates the Additional_Info table with columns
	 **/
	int createAdditionalInfoTable() {

		int value = 0;

		try {
			value = _sql.executeUpdate("CREATE TABLE Additional_Info ("+
					"ID               INTEGER AUTO_INCREMENT PRIMARY KEY, "+
					"Subtitles        TEXT, "+
					"Duration         INTEGER, "+
					"File_Size        INTEGER, "+
					"CDs              INTEGER, "+
					"CD_Cases         DOUBLE, "+
					"Resolution       TEXT, "+
					"Video_Codec      TEXT, "+
					"Video_Rate       TEXT, "+
					"Video_Bit_Rate   TEXT, "+
					"Audio_Codec      TEXT, "+
					"Audio_Rate       TEXT, "+
					"Audio_Bit_Rate   TEXT, "+
					"Audio_Channels   TEXT, "+
					"File_Location    TEXT, "+
					"File_Count       INTEGER, "+
					"Container        TEXT, "+
					"Media_Type       TEXT"+
			") ENGINE=InnoDB;");

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			value = -1;
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
	 * Creates the Extra_Info table with columns
	 **/
	int createExtraInfoTable() {
		int value = 0;

		try {
			value = _sql.executeUpdate("CREATE TABLE Extra_Info ("+
					"ID                    INTEGER AUTO_INCREMENT PRIMARY KEY"+
			") ENGINE=InnoDB;");
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			value = -1;
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
			value = _sql.executeUpdate("CREATE TABLE General_Info_Episodes ("+
					"ID                INTEGER AUTO_INCREMENT PRIMARY KEY, "+
					"movieID           INTEGER, "+
					"episodeNr         INTEGER, "+
					"Title             TEXT, "+
					"Cover             TEXT, "+
					"CoverData         BLOB, "+
					"UrlKey            TEXT, "+
					"Date              TEXT, "+
					"Directed_By       TEXT, "+
					"Written_By        TEXT, "+
					"Genre             TEXT, "+
					"Rating            DOUBLE, "+
					"Seen              TINYINT(1), "+
					"Plot              TEXT, "+
					"Cast              TEXT, "+
					"Notes             TEXT, "+
					"Aka               TEXT, "+
					"Country           TEXT, "+
					"Language          TEXT, "+
					"Colour            TEXT, "+
					"Certification     TEXT, "+
					//"Mpaa              TEXT, "+
					"Sound_Mix         TEXT, "+
					"Web_Runtime       TEXT, "+
					"Awards            TEXT," +
					"Personal_Rating   DOUBLE"+
			") ENGINE=InnoDB;");

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			value = -1;
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
	 * Creates the Additional_Info table with columns
	 **/
	int createAdditionalInfoEpisodeTable() {

		int value = 0;

		try {
			value = _sql.executeUpdate("CREATE TABLE Additional_Info_Episodes ("+
					"ID               INTEGER AUTO_INCREMENT PRIMARY KEY, "+
					"Subtitles        TEXT, "+
					"Duration         INTEGER, "+
					"File_Size        INTEGER, "+
					"CDs              INTEGER, "+
					"CD_Cases         DOUBLE, "+
					"Resolution       TEXT, "+
					"Video_Codec      TEXT, "+
					"Video_Rate       TEXT, "+
					"Video_Bit_Rate   TEXT, "+
					"Audio_Codec      TEXT, "+
					"Audio_Rate       TEXT, "+
					"Audio_Bit_Rate   TEXT, "+
					"Audio_Channels   TEXT, "+
					"File_Location    TEXT, "+
					"File_Count       INTEGER, "+
					"Container        TEXT, "+
					"Media_Type       TEXT"+
			") ENGINE=InnoDB;");

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			value = -1;
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
	 * Creates the Extra_Info table with columns
	 **/
	int createExtraInfoEpisodeTable() {
		int value = 0;

		try {
			value = _sql.executeUpdate("CREATE TABLE Extra_Info_Episodes ("+
					"ID                    INTEGER AUTO_INCREMENT PRIMARY KEY"+
			") ENGINE=InnoDB;");
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			value = -1;
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
			value = _sql.executeUpdate("CREATE TABLE Folders ("+
					"ID                  INTEGER AUTO_INCREMENT PRIMARY KEY, "+
					"Covers          TEXT, "+
					"Queries         TEXT"+
			") ENGINE=InnoDB;");

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			value = -1;
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
			value = _sql.executeUpdate("CREATE TABLE Settings ("+
					"ID                   INTEGER AUTO_INCREMENT PRIMARY KEY, "+
					"Active_Additional_Info_Fields          TEXT, "+
					"General_Info_Settings                  TEXT"+
			") ENGINE=InnoDB;");

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			value = -1;
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
			value = _sql.executeUpdate("CREATE TABLE Lists ("+
					"ID               INTEGER AUTO_INCREMENT PRIMARY KEY"+
			") ENGINE=InnoDB;");

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			value = -1;
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
			value = _sql.executeUpdate("ALTER TABLE Lists ADD CONSTRAINT lists "+
					"FOREIGN KEY (ID) REFERENCES General_Info(ID) "+
					"ON DELETE CASCADE"+
			";");

			_sql.clear();

			value = _sql.executeUpdate("ALTER TABLE Extra_Info ADD CONSTRAINT extrainfo "+
					"FOREIGN KEY (ID) REFERENCES General_Info(ID) "+
					"ON DELETE CASCADE"+
			";");

			_sql.clear();

			value = _sql.executeUpdate("ALTER TABLE Additional_Info ADD CONSTRAINT additionalinfo "+
					"FOREIGN KEY (ID) REFERENCES General_Info(ID) "+
					"ON DELETE CASCADE"+
			";");


		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			value = -1;

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
	int createRelationShipEpisodes() {

		int value = 0;

		try {
			value = _sql.executeUpdate("ALTER TABLE Additional_Info_Episodes ADD CONSTRAINT episodes1 "+
					"FOREIGN KEY (ID) REFERENCES General_Info_Episodes(ID) "+
					"ON DELETE CASCADE"+
			";");

			_sql.clear();

			value = _sql.executeUpdate("ALTER TABLE Extra_Info_Episodes ADD CONSTRAINT episodes2 "+
					"FOREIGN KEY (ID) REFERENCES General_Info_Episodes(ID) "+
					"ON DELETE CASCADE"+
			";");

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			value = -1;
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
	 * Checks if the database needs an update
	 **/
	public boolean isDatabaseOld() {
		return false;
	}

	public int makeDatabaseUpToDate() {
		return 1;
	}


	public void deleteDatabase() {
		;
	}


	/**
	 * Returns the additional_info with index index...
	 **/
	@Override
	public ResultSet getAdditionalInfoMovieResultSet(int index) {

		ResultSet resultSet = null;

		try {
			/* Gets the fixed Additional_Info... */
			resultSet = _sql.executeQuery("SELECT Additional_Info.* "+
					"FROM Additional_Info "+
					"WHERE Additional_Info.ID="+index+";");

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
		} 

		/* Returns the data... */
		return resultSet;
	}

	/**
	 * Returns the additional_info with index index...
	 **/
	@Override
	public ResultSet getAdditionalInfoEpisodeResultSet(int index) {

		ResultSet resultSet = null;

		try {
			/* Gets the fixed Additional_Info... */
			resultSet = _sql.executeQuery("SELECT Additional_Info_Episodes.* "+
					"FROM Additional_Info_Episodes "+
					"WHERE Additional_Info_Episodes.ID="+index+";");

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
		} 

		/* Returns the data... */
		return resultSet;
	}

	/**
	 * Returns the additional_info with index index...
	 **/
	@Override
	public ResultSet getExtraInfoMovieResultSet(int index) {

		ResultSet resultSet = null;

		try {
			/* Gets the fixed Additional_Info... */
			resultSet = _sql.executeQuery("SELECT Extra_Info.* "+
					"FROM Extra_Info "+
					"WHERE Extra_Info.ID="+index+";");
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
		}

		/* Returns the data... */
		return resultSet;
	}

	/**
	 * Returns the additional_info with index index...
	 **/
	@Override
	public ResultSet getExtraInfoEpisodeResultSet(int index) {

		ResultSet resultSet = null;

		try {
			/* Gets the fixed Additional_Info... */
			resultSet = _sql.executeQuery("SELECT Extra_Info_Episodes.* "+
					"FROM Extra_Info_Episodes "+
					"WHERE Extra_Info_Episodes.ID="+index+";");
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
		} 

		/* Returns the data... */
		return resultSet;
	}




	/** 
	 * Returns a ModelAdditionalInfo on a specific movie/episode
	 **/
	@Override
	public ModelAdditionalInfo getAdditionalInfo(int index, boolean episode) {
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
				fileSize = resultSet.getInt("File_Size");
				cDs = resultSet.getInt("CDs");
				cDCases = resultSet.getInt("CD_Cases");

				if ((resolution = resultSet.getString("Resolution")) == null)
					resolution = "";

				if ((videoCodec = resultSet.getString("Video_Codec")) == null)
					videoCodec = "";

				if ((videoRate = resultSet.getString("Video_Rate")) == null)
					videoRate = "";

				if ((videoBitrate = resultSet.getString("Video_Bit_Rate")) == null)
					videoBitrate = "";

				if ((audioCodec = resultSet.getString("Audio_Codec")) == null)
					audioCodec = "";

				if ((audioRate = resultSet.getString("Audio_Rate")) == null)
					audioRate = "";

				if ((audioBitrate = resultSet.getString("Audio_Bit_Rate")) == null)
					audioBitrate = "";

				audioChannels = resultSet.getString("Audio_Channels");

				if ((fileLocation = resultSet.getString("File_Location")) == null)
					fileLocation = "";

				fileCount = resultSet.getInt("File_Count");

				if ((container = resultSet.getString("Container")) == null)
					container = "";

				if ((mediaType = resultSet.getString("Media_Type")) == null)
					mediaType = "";


				/* Getting extra info fields */

				_sql.clear();

				String tempValue = "";

				ArrayList<String> extraInfoFieldNames = getExtraInfoFieldNames(true);
				ArrayList<String> extraInfoFieldValues = new ArrayList<String>();

				if (episode)
					resultSet = getExtraInfoEpisodeResultSet(index);
				else
					resultSet = getExtraInfoMovieResultSet(index);

				boolean next = resultSet.next();

				// Fieldnames and Fieldvalues don't match
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
				for (int i = 0; i < extraInfoFieldNames.size(); i++) {

					if (next)/* First column after the ID column is at index 2 */
						tempValue = resultSet.getString(i+2);

					if (tempValue == null)
						tempValue = "";

					extraInfoFieldValues.add(tempValue);
				}

				additionalInfo = new ModelAdditionalInfo(subtitles, duration, fileSize, cDs, cDCases, resolution, videoCodec, videoRate, videoBitrate, audioCodec, audioRate, audioBitrate, audioChannels, fileLocation, fileCount, container, mediaType);
				additionalInfo.setExtraInfoFieldValues(extraInfoFieldValues);
			}
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		/* Returns the list model... */
		return additionalInfo;
	}

	public boolean listColumnExist(String columnName) {

		ArrayList<String> columnNames = getListsColumnNames();

		while (!columnNames.isEmpty()) {
			if (columnNames.get(0).equals(columnName))
				return true;
			columnNames.remove(0);
		}
		return false;
	}


	/**
	 * Adds the fields to the general info table and returns the index added or
	 * -1 if insert failed.
	 **/
	@Override
	public int addGeneralInfo(ModelMovie model) {
		int index = -1;

		try {
			/* Gets the next index... */
			ResultSet resultSet = _sql.executeQuery("SELECT MAX(ID) "+
			"FROM General_Info ;");

			if (resultSet.next()) {
				index = resultSet.getInt(1)+1;
			} else {
				index = 0;
			}
			_sql.clear();

			/* Adds the info... */      
			if (index != -1) {

				PreparedStatement statement;
				statement = _sql.prepareStatement("INSERT INTO General_Info "+
						"(ID,Title,Cover,Imdb,Date,Directed_By,Written_By,Genre,Rating,Personal_Rating,Seen,Aka,Country,Language,Colour,Plot,Cast,Notes,CoverData,Certification,Mpaa,Sound_Mix,Web_Runtime,Awards) "+
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");


				statement.setInt(1, index);
				statement.setString(2, model.getTitle());
				statement.setString(3, model.getCover());
				statement.setString(4, model.getUrlKey());
				statement.setString(5, model.getDate());
				statement.setString(6, model.getDirectedBy());
				statement.setString(7, model.getWrittenBy());
				statement.setString(8, model.getGenre());

				try {
					statement.setDouble(9, Double.parseDouble(model.getRating()));
				} catch (NumberFormatException e) {
					statement.setDouble(9, -1);
				}

				try {
					statement.setDouble(10, Double.parseDouble(model.getPersonalRating()));
				} catch (NumberFormatException e) {
					statement.setDouble(10, -1);
				}

				statement.setBoolean(11, model.getSeen());
				statement.setString(12, model.getAka());
				statement.setString(13, model.getCountry());
				statement.setString(14, model.getLanguage());
				statement.setString(15, model.getColour());
				statement.setString(16, model.getPlot());
				statement.setString(17, model.getCast());
				statement.setString(18, model.getNotes());
				statement.setBytes( 19, model.getCoverData());
				statement.setString(20, model.getCertification());
				statement.setString(21, model.getMpaa());
				statement.setString(22, model.getWebSoundMix());
				statement.setString(23, model.getWebRuntime());
				statement.setString(24, model.getAwards());
				statement.executeUpdate();
			}

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			index = -1;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("", e);
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
	@Override
	public synchronized int setGeneralInfo(int index, ModelMovie model) {
		int value = 0;
		try {
			PreparedStatement statement;
			statement = _sql.prepareStatement("UPDATE General_Info "+
					"SET General_Info.Title=?, "+
					"General_Info.Cover=?, "+
					"General_Info.Imdb=?, "+
					"General_Info.Date=?, "+
					"General_Info.Directed_By=?, "+
					"General_Info.Written_By=?, "+
					"General_Info.Genre=?, "+
					"General_Info.Rating=?, "+
					"General_Info.Personal_Rating=?, "+
					"General_Info.Seen=?, "+
					"General_Info.Aka=?, "+
					"General_Info.Country=?, "+
					"General_Info.Language=?, "+
					"General_Info.Colour=?, "+
					"General_Info.Plot=?, "+
					"General_Info.Cast=?, "+
					"General_Info.Notes=?, "+
					"General_Info.CoverData=?, "+
					"General_Info.Certification=?, "+
					"General_Info.Mpaa=?, "+
					"General_Info.Sound_Mix=?, "+
					"General_Info.Web_Runtime=?, "+
					"General_Info.Awards=? "+
			"WHERE General_Info.ID=?;");

			statement.setString(1, model.getTitle());
			statement.setString(2, model.getCover());
			statement.setString(3, model.getUrlKey());
			statement.setString(4, model.getDate());
			statement.setString(5, model.getDirectedBy());
			statement.setString(6, model.getWrittenBy());
			statement.setString(7, model.getGenre());

			try {
				statement.setDouble(8,Double.parseDouble(model.getRating()));
			} catch (NumberFormatException e) {
				statement.setDouble(8,-1);
			}

			try {
				statement.setDouble(9,Double.parseDouble(model.getPersonalRating()));
			} catch (NumberFormatException e) {
				statement.setDouble(9,-1);
			}

			statement.setBoolean(10, model.getSeen());
			statement.setString(11, model.getAka());
			statement.setString(12, model.getCountry());
			statement.setString(13, model.getLanguage());
			statement.setString(14, model.getColour());
			statement.setString(15, model.getPlot());
			statement.setString(16, model.getCast());
			statement.setString(17, model.getNotes());
			statement.setBytes( 18, model.getCoverData());
			statement.setString(19, model.getCertification());
			statement.setString(20, model.getMpaa());
			statement.setString(21, model.getWebSoundMix());
			statement.setString(22, model.getWebRuntime());
			statement.setString(23, model.getAwards());
			statement.setInt(24, index);

			value = statement.executeUpdate();
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
	@Override
	public synchronized int setGeneralInfoEpisode(int index, ModelEpisode model) {
		int value = 0;
		try {
			PreparedStatement statement;
			statement = _sql.prepareStatement("UPDATE General_Info_Episodes "+
					"SET General_Info_Episodes.Title=?, "+
					"General_Info_Episodes.Cover=?, "+
					"General_Info_Episodes.UrlKey=?, "+
					"General_Info_Episodes.Date=?, "+
					"General_Info_Episodes.Directed_By=?, "+
					"General_Info_Episodes.Written_By=?, "+
					"General_Info_Episodes.Genre=?, "+
					"General_Info_Episodes.Rating=?, "+
					"General_Info_Episodes.Personal_Rating=?, "+
					"General_Info_Episodes.Seen=?, "+
					"General_Info_Episodes.Aka=?, "+
					"General_Info_Episodes.Country=?, "+
					"General_Info_Episodes.Language=?, "+
					"General_Info_Episodes.Colour=?, "+
					"General_Info_Episodes.Plot=?, "+
					"General_Info_Episodes.Cast=?, "+
					"General_Info_Episodes.Notes=?, "+
					"General_Info_Episodes.movieID=?, "+
					"General_Info_Episodes.episodeNr=?, "+
					"General_Info_Episodes.CoverData=?, "+
					"General_Info_Episodes.Certification=?, "+
					"General_Info_Episodes.Sound_Mix=?, "+
					"General_Info_Episodes.Web_Runtime=?, "+
					"General_Info_Episodes.Awards=? "+
			"WHERE General_Info_Episodes.ID=?;");


			statement.setString(1, model.getTitle());
			statement.setString(2, model.getCover());
			statement.setString(3, model.getUrlKey());
			statement.setString(4, model.getDate());
			statement.setString(5, model.getDirectedBy());
			statement.setString(6, model.getWrittenBy());
			statement.setString(7, model.getGenre());

			try {
				statement.setDouble(8,Double.parseDouble(model.getRating()));
			} catch (NumberFormatException e) {
				statement.setDouble(8,-1);
			}

			try {
				statement.setDouble(9,Double.parseDouble(model.getPersonalRating()));
			} catch (NumberFormatException e) {
				statement.setDouble(9,-1);
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
			statement.setBytes(20, model.getCoverData());
			statement.setString(21, model.getCertification());
			statement.setString(22, model.getWebSoundMix());
			statement.setString(23, model.getWebRuntime());
			statement.setString(24, model.getAwards());
			statement.setInt(25, index);

			value = statement.executeUpdate();
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		/* Returns the updated records... */
		return value;
	}


	/**
	 * Adds the fields to the general info table and returns the index added
	 * Returns -1 if insert failed.
	 **/
	@Override
	public int addGeneralInfoEpisode(ModelEpisode model) {

		int index = -1;

		try {
			/* Gets the next index... */
			ResultSet resultSet = _sql.executeQuery("SELECT MAX(ID) "+
			"FROM General_Info_Episodes;");
			if (resultSet.next()) {
				index = resultSet.getInt(1) + 1;
			} else {
				index = 0;
			}

			_sql.clear();

			/* Adds the info... */      
			if (index != -1) {

				PreparedStatement statement;
				statement = _sql.prepareStatement("INSERT INTO General_Info_Episodes "+
						"(ID,Title,Cover,UrlKey,Date,Directed_By,Written_By,Genre,Rating,Personal_Rating,Seen,Aka,Country,Language,Colour,Plot,Cast,Notes,movieID,episodeNr,CoverData,Certification,Sound_Mix,Web_Runtime,Awards) "+
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");

				statement.setInt(1, index);
				statement.setString(2, model.getTitle());
				statement.setString(3, model.getCover());
				statement.setString(4, model.getUrlKey());
				statement.setString(5, model.getDate());
				statement.setString(6, model.getDirectedBy());
				statement.setString(7, model.getWrittenBy());
				statement.setString(8, model.getGenre());

				try {
					statement.setDouble(9,Double.parseDouble(model.getRating()));
				} catch (NumberFormatException e) {
					statement.setDouble(9, -1);
				}

				try {
					statement.setDouble(10,Double.parseDouble(model.getPersonalRating()));
				} catch (NumberFormatException e) {
					statement.setDouble(10, -1);
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
				statement.setBytes(21, model.getCoverData());
				statement.setString(22, model.getCertification());
				statement.setString(23, model.getWebSoundMix());
				statement.setString(24, model.getWebRuntime());
				statement.setString(25, model.getAwards());
				statement.executeUpdate();
			}

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
			index = -1;
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		/* Returns the updated records... */
		return index;
	}


	/**
	 * Sets the fields of movie index to the general info table and returns the number of
	 * updated rows.
	 **/
	@Override
	public int setSeen(int index, boolean seen) {

		int value = 0;
		try {
			PreparedStatement statement = _sql.prepareStatement("UPDATE General_Info "+
					"SET General_Info.Seen=? "+
			"WHERE General_Info.ID=?;");
			statement.setBoolean(1,seen);
			statement.setInt(2,index);

			value = statement.executeUpdate();
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		/* Returns the updated records... */
		return value;
	}


	/**
	 * Sets the fields of movie index to the general info table and returns the number of
	 * updated rows.
	 **/
	@Override
	public int setSeenEpisode(int index, boolean seen) {

		int value = 0;
		try {
			PreparedStatement statement = _sql.prepareStatement("UPDATE General_Info_Episodes "+
					"SET General_Info_Episodes.Seen=? "+
			"WHERE General_Info_Episodes.ID=?;");
			statement.setBoolean(1,seen);
			statement.setInt(2,index);

			value = statement.executeUpdate();
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		/* Returns the updated records... */
		return value;
	}

	@Override
	public synchronized String getMoviesSelectStatement(ModelDatabaseSearch options) {
		
		ArrayList<String> lists = getListsColumnNames();

		StringBuffer buf = new StringBuffer();

		if (options.getFullGeneralInfo) {
			buf.append("SELECT " + quotedGeneralInfoString + ".`ID`, " +
					quotedGeneralInfoString + ".`Imdb`, "+
					quotedGeneralInfoString + ".`Cover`, ");

			if (options.getCoverData)
				buf.append(quotedGeneralInfoString + ".`CoverData`, ");

			buf.append(
					quotedGeneralInfoString + ".`Date`, "+
					quotedGeneralInfoString + ".`Title`, "+
					quotedGeneralInfoString + ".`Directed_By`, "+
					quotedGeneralInfoString + ".`Written_By`, "+
					quotedGeneralInfoString + ".`Genre`, "+
					quotedGeneralInfoString + ".`Rating`, "+
					quotedGeneralInfoString + ".`Personal_Rating`, "+
					quotedGeneralInfoString + ".`Plot`, "+
					quotedGeneralInfoString + ".`Cast`, "+
					quotedGeneralInfoString + ".`Notes`, "+
					quotedGeneralInfoString + ".`Seen`, "+
					quotedGeneralInfoString + ".`Aka`, "+
					quotedGeneralInfoString + ".`Country`, "+
					quotedGeneralInfoString + ".`Language`, "+
					quotedGeneralInfoString + ".`Colour`, "+
					quotedGeneralInfoString + ".`Certification`, "+
					quotedGeneralInfoString + ".`Mpaa`, "+
					quotedGeneralInfoString + ".`Sound_Mix`, "+
					quotedGeneralInfoString + ".`Web_Runtime`, "+
					quotedGeneralInfoString + ".`Awards` ");
		}
		else {
			buf.append("SELECT " + quotedGeneralInfoString + ".ID, " + 
					quotedGeneralInfoString + "." + quote + "Title" + quote + ", " + 
					quotedGeneralInfoString + "." + quote + "Imdb" + quote + ", " + 
					quotedGeneralInfoString + "." + quote + "Cover" + quote +  ", " + 
					quotedGeneralInfoString + "." + quote + "Date" + quote);
		}

		if (lists != null && lists.size() > 0) {

			for (int i = 0; i < lists.size(); i++)
				buf.append(", " + quotedListsString + "." +quote+ lists.get(i) + quote+ " AS " +quote+ listsAliasPrefix + lists.get(i) + quote + " ");
		}	

		return buf.toString();
	}



	/** 
	 * Returns a DefaultListModel that contains all the movies in the
	 * current database.
	 **/
	public ArrayList<ModelEpisode> getEpisodeList(String sortBy) {
		ArrayList<ModelEpisode> list = new ArrayList<ModelEpisode>(100);

		try {
			/* Gets the list in a result set... */
			ResultSet resultSet = _sql.executeQuery(
					"SELECT General_Info_Episodes.ID,"+
					"General_Info_Episodes.movieID,"+
					"General_Info_Episodes.episodeNr,"+
					"General_Info_Episodes.Title, "+
					"General_Info_Episodes.Cover "+
					"FROM General_Info_Episodes "+
					"ORDER BY General_Info_Episodes."+sortBy+", General_Info_Episodes.episodeNr;");


			/* Processes the result set till the end... */
			while (resultSet.next()) {
				list.add(new ModelEpisode(resultSet.getInt("ID"), resultSet.getInt("movieID"), resultSet.getInt("episodeNr"), resultSet.getString("Title"), resultSet.getString("Cover")));
			}
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		/* Returns the list model... */
		return list;
	}

	public ModelMovie getMovie(int index) {
		return getMovie(index, true);
	}


	/** 
	 * Returns a MovieModel that contains the movie at the specified index
	 * in the database.
	 **/
	public ModelMovie getMovie(int index, boolean getCoverData) {

		ModelMovie movie = null;

		try {
			/* Gets the list in a result set... */
			ArrayList<String> listNames = getListsColumnNames();

			ModelDatabaseSearch options = new ModelDatabaseSearch();

			options.setOrderCategory("");
			options.setListOption(0);
			options.getCoverData = getCoverData;

			String query = setTableJoins("", options);
			query += "WHERE General_Info.ID="+index+";";

			ResultSet resultSet = _sql.executeQuery(query);

			/* Processes the result set for one movie */
			if (resultSet.next()) {

				String rating = resultSet.getString("Rating");

				if (rating.equals("-1"))
					rating = "";

				String personalRating = resultSet.getString("Personal_Rating");
				
				if (personalRating == null || personalRating.equals("-1"))
					personalRating = "";
				
				movie = new ModelMovie(resultSet.getInt("id"), 
						resultSet.getString("Imdb"), resultSet.getString("Cover"), 
						resultSet.getString("Date"), resultSet.getString("Title"), 
						resultSet.getString("Directed_By"), resultSet.getString("Written_By"), 
						resultSet.getString("Genre"), rating, personalRating, resultSet.getString("Plot"), 
						resultSet.getString("Cast"), resultSet.getString("Notes"), 
						resultSet.getBoolean("Seen"), resultSet.getString("Aka"), 
						resultSet.getString("Country"), resultSet.getString("Language"), 
						resultSet.getString("Colour"), resultSet.getString("Certification"), 
						resultSet.getString("Mpaa"), resultSet.getString("Sound_Mix"), 
						resultSet.getString("Web_Runtime"), resultSet.getString("Awards"));

				if (getCoverData)
					movie.setCoverData(resultSet.getBytes("CoverData"));

				// Add lists
				int count = listNames.size();

				if (count > 0) {
					for (int i = 0; i < count; i++) {
						if (resultSet.getBoolean(listsAliasPrefix + listNames.get(i)))
							movie.addToMemberOfList((String) listNames.get(i));
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		/* Returns the list model... */
		return movie;
	}


	public ModelEpisode getEpisode(int index) {
		return getEpisode(index, true);
	}

	/** 
	 * Returns a ModelEpisode with the General_Info on a specific episode
	 **/
	public ModelEpisode getEpisode(int index, boolean getCoverData) {
		ModelEpisode episode = null;

		try {
			String query = "SELECT General_Info_Episodes.ID,";

			if (getCoverData)
				query += "General_Info_Episodes.CoverData,";

			query += "General_Info_Episodes.movieID,"+
			"General_Info_Episodes.episodeNr,"+
			"General_Info_Episodes.UrlKey,"+
			"General_Info_Episodes.Cover,"+
			"General_Info_Episodes.Date,"+
			"General_Info_Episodes.Title,"+
			"General_Info_Episodes.Directed_By,"+
			"General_Info_Episodes.Written_By,"+
			"General_Info_Episodes.Genre,"+
			"General_Info_Episodes.Rating,"+
			"General_Info_Episodes.Personal_Rating,"+
			"General_Info_Episodes.Plot,"+
			"General_Info_Episodes.Cast,"+
			"General_Info_Episodes.Notes,"+
			"General_Info_Episodes.Seen,"+
			"General_Info_Episodes.Aka,"+
			"General_Info_Episodes.Country,"+
			"General_Info_Episodes.Language,"+
			"General_Info_Episodes.Colour, "+
			"General_Info_Episodes.Certification, "+ 
			//"General_Info_Episodes.Mpaa, "+ 
			"General_Info_Episodes.Sound_Mix, "+ 
			"General_Info_Episodes.Web_Runtime, "+ 
			"General_Info_Episodes.Awards "+ 
			"FROM General_Info_Episodes "+ 
			"WHERE General_Info_Episodes.ID="+index+";";


			/* Gets the list in a result set... */
			ResultSet resultSet = _sql.executeQuery(query);

			/* Processes the result set till the end... */
			if (resultSet.next()) {

				String rating = resultSet.getString("Rating");

				if (rating.equals("-1"))
					rating = "";

				String personalRating = resultSet.getString("Personal_Rating");

				if (personalRating == null || personalRating.equals("-1"))
					personalRating = "";

				episode = new ModelEpisode(resultSet.getInt("ID"), 
						resultSet.getInt("movieID"), 
						resultSet.getInt("episodeNr"), 
						resultSet.getString("UrlKey"),
						resultSet.getString("Cover"), 
						resultSet.getString("Date"), 
						resultSet.getString("Title"), 
						resultSet.getString("Directed_By"), 
						resultSet.getString("Written_By"), 
						resultSet.getString("Genre"), 
						rating,
						personalRating,
						resultSet.getString("Plot"), 
						resultSet.getString("Cast"),
						resultSet.getString("Notes"), 
						resultSet.getBoolean("Seen"), 
						resultSet.getString("Aka"), 
						resultSet.getString("Country"), 
						resultSet.getString("Language"), 
						resultSet.getString("Colour"), 
						resultSet.getString("Certification"), /*resultSet.getString("Mpaa"),*/ 
						resultSet.getString("Sound_Mix"), 
						resultSet.getString("Web_Runtime"), 
						resultSet.getString("Awards"));

				if (getCoverData)
					episode.setCoverData(resultSet.getBytes("CoverData"));
			}
		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		/* Returns the list model... */
		return episode;
	}


	private byte [] getCoverData(String query) {
		byte [] data = null;

		try {
			/* Gets the list in a result set... */
			ResultSet resultSet = _sql.executeQuery(query);

			/* Processes the result set till the end... */
			if (resultSet.next()) {
				data = resultSet.getBytes("CoverData");
			}

		} catch (Exception e) {
			log.error("", e);
			checkErrorMessage(e);
		} finally {
			/* Clears the Statement in the dataBase... */
			try {
				_sql.clear();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		/* Returns the list model... */
		return data;
	}

	public byte [] getCoverDataMovie(int index) {

		byte [] data = getCoverData("SELECT General_Info.CoverData " +
				"FROM General_Info "+ 
				"WHERE General_Info.ID="+index+";");
		/* Returns the list model... */
		return data;
	}

	public byte [] getCoverDataEpisode(int index) {

		byte [] data = getCoverData("SELECT General_Info_Episodes.CoverData " +
				"FROM General_Info_Episodes "+ 
				"WHERE General_Info_Episodes.ID="+index+";");
		/* Returns the list model... */
		return data;
	}


	/**
	 * Returns the Extra Info field value with index index named name...
	 **/
	public String getExtraInfoMovieField(int index, String name) {

		String data = getString("SELECT Extra_Info." +quote+ name +quote+ " "+
				"FROM Extra_Info "+
				"WHERE Extra_Info.ID="+index+";", name);
		/* Returns the data... */
		return data;
	}

	/**
	 * Returns the Extra Info field with index index named name...
	 **/
	public String getExtraInfoEpisodeField(int index, String name) {

		String data = getString("SELECT Extra_Info_Episodes." +quote+ name +quote+" "+
				"FROM Extra_Info_Episodes "+
				"WHERE Extra_Info_Episodes.ID="+index+";", name);
		/* Returns the data... */
		return data;
	}


	/**
	 * Returns true if the movie at the specific index is a member of the specified list with name name...
	 **/
	protected boolean getList(int index, String name) {

		boolean data = getBoolean("SELECT Lists." +quote+ name +quote+ " "+
				"FROM Lists "+
				"WHERE Lists.ID="+index+";", name);
		/* Returns the data... */
		return data;
	}
}


