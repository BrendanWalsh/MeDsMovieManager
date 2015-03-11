.. This document is written in reStructuredText format

========================================
   Changelog - MeD's Movie Manager
========================================


Version 2.9.1.3 (20-March-2010)

- General improvements:

  - Enabled anti-aliasing in Swing GUI providing smoother text.
  - When not showing icons in the movie list, the text size is now automatically increased when increasing the list height.
  - The reports in the report generator were showing incorrect page count.
  
- IMDb Library:
  
  - IMDb parser would hang. (Fixed 10.03.2010)


Version 2.9.1.2 (13-February-2010)

- General improvements:

  - Reports for the report generator have been improved and a couple of extra reports were added. 

- Bug fixes:

  - Personal rating was sometimes shown as -1.0 if it hadn't been set.
  - Some strange behaviour with the lists on startup was fixed.
  - Export and Import would fail when using Substance Look and Feel.
  - Report Generator did not function properly.
  

Version 2.9.1.1 (10-February-2010)

- Bug fixes:

  - Export to CSV and Excel was broken.


Version 2.9.1 (07-February-2010)

- New in this version:

  - Added personal rating. This can also be retrieved from IMDb when supplying username and password.
  - Added new Look & Feels (NimROD_).

- Bug fixes:

  - Default values for the additional info fields were saved for the wrong fields when they were rearranged.

- Notes:

  - Because of bugs with additional info default values, the current values may be wrong when imported from old config file.


Version 2.9.0.1 (01-February-2010)

- Bug fixes:
  
  - Config file from v2.8.7 failed to load correctly.


Version 2.9.0 (28-January-2010)

- New in this version:

  - New update notification and automatic download system.

- General improvements:

  - Added better shortcut handling. Press ALT+k on Windows/GNU+Linux or ⌘+k on OSX to see available shortcuts for a window.
  - Updated the HSQLDB driver which will hopefully fix some stability issues with HSQL. 
  - Added right click popup when editing movie/episode to clear additional info values.
  - IMDb dialog now has a field for changing the search text when searching for IMDb info.
  - Added option to remove quotes ("") from series titles.

  - Improvements to the Multi-Add-function:

    - Shows the progress of the import procedure.
    - It's now possible to define custom folders as root devices in the tree.
    - Hardcoded codec info has been replaced with user defined text.
    - Search nfo/txt files for IMDb URL when adding movies.
    - Improved "use folder instead of file name" option to use parent folder if the current folder is named CD1/CD2 etc.
    - New option to prefix movie titles with "_verify_" when using the option "Select First Hit".
    - Added progress bar if expanding a directory takes more than a second.
    - Changed directory separator from '\\' to system file separator in a few places in add multiple movies function.

- Bug fixes:

  - After editing a movie, the lists it is a member of was not shown in the right click popup in the movie list. (Bug ID:2879774)
  - Database converter was unable to convert MySQL or HSQL database to MS Access database.
  - Fixed an issue with selecting multiple entries in the movie list with the SHIFT key.
  - Application would hang if pressing cancel when choosing player after pressing the play button.
  - IMDb scraper would get the episode number and series wrong in some situations.
  - IMDb scraper did not retrieved cast on series.
  - IMDb scraper did not retrieve rating.
  - IMDb scraper could hang (seemingly) indefinitely in some situations (some rare movies).
  

Version 2.8.7 (13-December-2009)

- Bug fixes:

  - Cast was no longer retrieved due to changes in HTML on IMDb.
  - Notes were not saved when editing in main window.
  

Version 2.8.6 (16-July-2009)

- General improvements:

  - No longer have blue border around covers (with some web browsers) when exporting to full HTML.

- Bug fixes:

  - Advanced search was broken with MS Access database.
  - Does not load custom queries when using relative path.


Version 2.8.5 (13-July-2009)

- General improvements:

  - Made the Add/Edit movie panel resizable for those with a row resolution screen.
  - Allow list names containing white space.
  - Added option to skip entries without IMDb id in the "Update IMDb Info" function.

- Bug fixes:

  - Cast was not retrieved after some changes by IMDb.
  - Update covers using "Update IMDb Info" with option "If empty" using MySQL wouldn't always update the cover in the database.
  - IMDb dialog in "Add Multiple Movies" was hiding the "Abort" button.


Version 2.8.4 (04-June-2009)

- General improvements:

  - Support for MediaInfo_ library on 64bit Windows.
  

Version 2.8.3 (01-June-2009)

- Bug fixes:

  - 'Update IMDb Info' would fail for entries with no IMDb id when using Substance L&F.
  - Library was missing making MediaInfo_ library not working at all.


Version 2.8.2 (31-May-2009)

- New 

  - Finding duplicate entries can now be done using the command "DUPLICATES" in the search filter (Not available for MS Access database). Read more about duplicates_.
  - Added Splash Screen during loading of the movie manager for Java 1.6 and later.

- General improvements:

  - Playing media files with space in the name should work better on GNU/Linux. 
  - 'Add multiple movies' now has the option of grouping files together before searching IMDb by right clicking the list elements in list of files to add. Also, files with similar names are shown with the same color in the list of files to add.
  - Support for using MediaInfo_ library on GNU/Linux.
  - Updated the MediaInfo library with better support for mp4 and mkv (Matroska) files.

- Bug fixes:

  - Covers were not saved on full HTML export.
  - Showing unlisted entries *only* was not possible.


Version 2.8.1 (09-May-2009)

- General improvements:

  - Better support for Windows 7. Config and log file is now written to writable user area.

- Bug fixes:

  - Search in the movie list did not work when all lists and unlisted entries were selected (Bug introduced in 2.8.0).
  - "Filter out media files already in database" using the "Add multiple movies" was broken.


Version 2.8.0 (12-April-2009)

- Note

  - From this version on, Java 5 (1.5) or higher is required. To be able to use all the Look & Feels (themes), Java 1.6 is required.

- General improvements:

  - It's now possible to view multiple lists at the same time (combined).
  - The lists each entry is connected to is now shown in the popup when right clicking the list entry.
  - Drag and Drop of media files has been added to the movie list and additional info in main window.
  - This version should work better with some Look & Feels, and a few more are included.
  - Config file and log file is no longer written to the application directory on Windows Vista, and should now work a lot better on that OS.

- Bug fixes:

  - Full HTML export would seemingly hang on the last entry (Caused by inefficient method).
  - HTML export would sometimes fail when pressing the export button. Caused by the file chooser GUI.
  - IMDb search on Non-ASCII characters in titles could fail.
  - Nimbus Look & Feel (Java 1.6 only) was wrongly removed from the list of L&Fs.
  - In "Add Multiple Movies", files containing more than one dot (punctuation) would not appear in the file tree.
  - Ancient bug in "Add Multiple Movies" function causing the "Add Movies" button not to react if it had already been pressed with no valid media files in the "Media files" list.
  	

Version 2.7.7 (22-March-2009)

- General improvements:

  - HTML export now opens a dialog showing the progress.
    
- Bug fixes:

  - "Update IMDb Info" function now lists all the processed movies correctly.
  - On OS X, the quit button in system menu and the shortcut is fixed.
  

Version 2.7.6 (14-March-2009)

- General improvements:

  - Grabs Cast info better with more actors. Non-ASCII characters are now also converted.
    
- Bug fixes:

  - OS X with Java 1.6 would not work because of library link error.


Version 2.7.5 (13-March-2009)

- General improvements:

  - Right-clicking an entry in the list of movies/series/episodes search hits will now open the IMDb page. 
  - Location of files that aren't supported by the file parsers are now saved with "Get File Info" function (instead of being ignored).
  - Better support for launching media player on OS X.
  - If retrieving episode info fails, it will now retry two times before marking the episode as red in the list.
   
- Bug fixes:

  - Some series weren't found when searching IMDb.
  - Cast grabbed from IMDb was sometimes incomplete.
  - It's now possible to create new MySQL databases again.
  - When exporting, some data was missing in the table grid and therefore not exported. 
  - When importing movies, the covers are now saved when retrieving info from IMDb.
   
   
Version 2.7.4 (20-January-2009)

- Bug fixes:

  - Version check is fixed.
  - Database backup function fixed.


Version 2.7.3 (16-January-2009)

- Bug fixes:

  - Fixed IMDb, now all hits are shown.
  - Using IMDb username and password for login.


Version 2.7.2 (10-January-2009)

- General improvements:

  - Added a new theme 'A Touch of Green' for the HTML panel.

- Bug fixes:
   
  - 'Update IMDb Info' function could remove the additional info on some movies.
  - It was not possible to retrieve DVD Info twice in one session.
  - Add multiple movies function now saves the Cover from IMDb.
  - Multiple bugs have been fixed in the import function:
    
    * Marking each column with correct data field should now work 100%.
    * Can now when importing retrieve IMDb info.
    * Abort and Cancel when importing IMDb info was switched.
  - Import function now has the option to add movies that weren't found at IMDb in a list 'Importer-skipped'. 
  - Fixed som stuff when adding episode entries from IMDb.
  - Saving movie titles from IMDb of specific language set in settings now works.

Version 2.7.1 (20-December-2008)
 
- Bug fixes:

  - Old episodes will now look like they used to without "SnullEnull" in the movie list.
  - Hopefully fixed some issues causing crashes.


Version 2.7 (18-December-2008)

- General improvements:

  - TV.COM support has been replaced by IMDb. There should be no big changes except that grabbing the info will take a bit longer as more URLs must be downloaded and parsed.
  - The Season and episode will be shown in the movie list for each episode.

- Bug fixes:

  - IMDb changed the HTML code again.


Version 2.7 beta 1 (16-July-2008)

- New:

  - "Add multiple movies" function has been rewritten and contains some new features.
  - File tree to specify which directories to search. Only local disks may be searched.
  - Possibility of excluding/including files depending on file extensions and file name, using regular strings and regular expression.
  - Option to exclude files that are already in the database.
  - A list of media files to be added has also been added, which makes it easier to control which files will be added. 
  - Export to HTML will now export the currently used HTML template instead of the old HTML.
  - Export to CSV and excel file format.

- General improvements:
   
  - Windows will no longer appear outside of the screen if the main windows is placed near or outside the screen.
  - Added option in preferences to enable the play button in the toolbar.
  - Cover image in the HTML view is now linking to the IMDb web site for the movie. 
  - Adjustments have been made so that the movie list should load faster with big movie lists.
  - Movie list has now a color matching the current L&F. Also improved consistency when selecting multiple entries with ctrl-key pressed.

- Bug fixes:

  - When editing an episode the key linking the episode to the series was lost.
  - The second of two movies with the same title produced in the same year wouldn't appear in the search hits.
  - Move 'The', 'A' and 'An' to end of title did not work.
  - HTML templates wouldn't load if the HTML code contained dollar signs ($). 
  - Runtime read from ifo (DVD) files were slightly off mark. Fix by gaelead.
  - Title was not added when importing movies from text file.
  - Seen/Unseen wasn't updated in HTML view unless the entry was reloaded.
  - Error alert will no longer appear when trying to save an edited movie with no cover image.
  - Error window would appear when trying to play a movie with spaces in  the file path on Windows.
  - Plot was missing from some movies.
  - Error could occur when generating reports with the Report Generator if the cover was missing.
  - Incorrect directory was used when exporting database to HTML.


Version 2.6.1 (19-February-2008)

- Bug fixes:

  - Fixed some bugs in cover and queries paths settings in Folders menu.

- Note:
 
  - This is most importantly a repack of 2.6 as some important files were missing.


Version 2.6 (18-February-2008)

- New:
   
  - HTML view of movie info using templates.

- Bug fixes:

  - "Playing media files on Linux doesn't work" (Bug ID:1879542)
  - "The jasper report tool may hang during compilation" (Bug ID:1760174)
  - "Unable to add certain films" - IMDb parser slightly modified to correct this error.
  - "Moviemanager Doesn't Shut-Down" - This would only appear on Win 98. Hopefully fixed.
  - Fixed an issue with cover path relative to database/program install directory.


Version 2.5.5 (26-December-2007)

- New:
 
  - Database backup function for HSQL and MS Access database.
  - New version update notifier.
  - IMDb authentication.

- General improvements:
 
  - Search filter remembers old search queries.
  - The movie list and movie info window is now divided by a splitpane.

- Bug fixes:
 
  - IMDb info is now saved when using the import funtion.


Version 2.5.4.2 (05-December-2007)

- Bug fixes:
 
  - IMDb parser gave strange results
  - Connecting to the same MySQL database from both Windows and Linux didn't always work.


Version 2.5.4.1 (12-July-2007)

- General improvements:
 
  - IMDb parser slightly modified to show more hits

- Bug fixes:
 
  - The MS Access database is now Access 2002 format instead of Access 2000. (Bug ID: 1738785) This fixes a nasty database error which could make it troublesome to add/remove 'additional info' fields.
  - When using MS Access database: The user defined 'additional info' fields would be hidden if there were more than 3 (Text field in database was too small). 
  - Covers were not saved when adding movies with the 'Add Multiple Movies' function.


Version 2.5.4 (10-July-2007)

- New:
 
  - CSV import

- General improvements:
 
  - It's now possible to select multiple seasons when adding episodes from tv.com (Thanks to Mel)

- Bug fixes:
 
  - Searching on IMDb should work now (Didn't quite catch the changes on IMDb with the last fix ;-) )
  - Can now search the filter with Unicode characters in HSQL database (Bug ID:1730310)
  - When exporting to XML the user defined additional info values are now included (Bug ID:1727149)
  - Alias definitions (advanced search) and additional info values (add/edit movie info) are now saved again (Bug ID:1718944)
  - Covers weren't saved when using "Update IMDb Info" and storing covers relative to database (Bug ID:1718892)
  - Fixed bug: "'Set relative paths' not restored correctly" (Bug ID:1716094)
  - Fixed bug: "Problems with 'Store covers locally' (MySQL)" (Bug ID:1716100)
  - Fixed bug: "Deleting items in MySQL leaves addInfo fields" (Bug ID:1716110)
  - Fixed bug: "Covers not shown in List after load (MySQL only)" (Bug ID:1716107)
  - Plenty of other minor bugs and issues have been fixed


Version 2.5.3.5 (27-June-2007)

- Bug fixes:
 
  - IMDb changed their html.


Version 2.5.3.4 (26-June-2007)

- Bug fixes:
 
  - MeD's Movie Manager can now be closed on Windows Vista.


Version 2.5.3.3 (03-May-2007)

- Bug fixes:
 
  - Fixed a serious bug with the additional info fields.


Version 2.5.3.2 (03-May-2007)

- Bug fixes:
 
  - Binaries were missing some important files.


Version 2.5.3 (02-May-2007)

- Note:
   
  - This is a major bug fix release. Thanks a lot to Matthias for thorough testing and bug-seeking.

- General improvements:

  - Add Multiple Movies can now grab movie title from the directory instead of the filename (Thanks to Dan Vann and Nicholas). 
  - Edit window is now opened on double click on entries in the movie list (except from series).   

- Bug fixes:

  - New additional info fields would not be usable for older entries added before the new additional info field.
  - Notes from previous selected node could overwrite the value of the newly selected node.
  - Mpaa info was not shown after the movie info was grabbed from IMDb and saved.
  - Grabbing info from DVDs should now work better on Linux.
  - Location in additional info is now saved when grabbing media info from CDs.  
  - A lot of smaller fixes and improvements.


Version 2.5.2 (29-April-2007)

- General improvements:
 
  - No longer necessary to save changed notes manually.

- Bug fixes:
 
  - A bunch of smaller issues.


Version 2.5.1 (18-April-2007)

- Bug fixes:

  - The movie list was not sorted.
 

Version 2.5 (18-April-2007)

- General improvements:
  
  - Included a lot more Look & Feels. 

- Bug fixes:

  - Bug fixed where info in add/edit was zeroed out when changing seen/unseen or cover. (Thanks to kreegee).
  - Bug fixes for the Mac GUI. (Thanks to kreegee).
  - Fixed IMDb html parsing. Now retrieves 'directed by' and 'written by'


Version 2.5 beta 4 (07-March-2007)

- General improvements:
 
  - Importing excel spreadsheet should now actually work. 
  - IMDb Updater now allows skipping of entries that already contains an IMDb ID.
  - Non ASCII characters in 'directed by' and 'written by' are now converted again.

- Bug fixes:
 
  - Removed a few bugs from the "Add multiple movies by file" function.
  - Database converter should now work when saving to MS Access.


Version 2.5 beta 3 (24-February-2007)

- General improvements:
 
  - Added new field in the edit window showing the IMDb ID.
  - Excel import is improved.

- Bug fixes:
 
  - Fixed the IMDb parsing issue. 


Version 2.5 beta 2 (21-January-2007)

- General improvements:
 
  - Many small improvements and fixes on the new features added in beta 1.

- Bug fixes:
 
  - Now saves episodes correctly.


Version 2.5 beta 1 (09-January-2007)

- New:
   
  - XML export/import of the movie list.
  - Toolbar customizable by right clicking it.
  - Play functionality (Button available by right clicking the toolbar).
  - It's now possible to have a title of a specific language imported from IMDb.

- General improvements:
 
  - Better handling of special characters when searching the database using the filter.
  - The Update IMDb Info function now searches for info on entries that were previously ignored (missing IMDb id).

- Bug fixes:
 
  - Fixed the parsing routine which failed if there was only one hit on IMDb search.
  - "Show covers in movielist" and relative covers-path (ID: 1602228).
  - "No Matches Found" entry not removed after adding movie (ID: 1595094).
  - Database error - additional info fields - add/delete (ID: 1595092).


Version 2.41 (20-October-2006)

- Bug fixes:
 
  - Subtitles field could be displayed multiple times if some additional info fields were hidden (ID: 1581374).
  - Issue where covers wouldn't be saved when using 'covers relative to database' option.
  - Report function can now be closed without having the process keep running.


Version 2.40 (05-October-2006)

- New:
 
  - Threads with Progress Bar has been introduced to make the app more responsive during database loading and imdb/tv.com searching.
  - Now using HTTPClient to retrieve info from tv.com and IMDb. Should no longer "hang" if there are no hits.
  - New report function to create reports of the movie list.

- General improvements:
 
  - New logging system implemented (log4j) (ID: 1457323).
  - Title bar can now be changed to look and feel mode.
  - Option to choose wether a database should be loaded at startup (ID: 1488086).
  - The location and state of the main window is now saved when shutting down and restored at startup.
  - Retrieving DVD info should now work better (Correct .ifo should be chosen automatically).
  - Loading of database with many series will be faster using MySQL.
  - Covers can now be used as icons in the movie list.

- Bug fixes:
 
  - MySQL pre v4.1 doesn't support the BOOLEAN alias, so now TINYINT is used instead. (ID: 1565396)
  - Episodes were saved so that they appeared in the wrong order (introduced in beta 4.1 versions).
  - Fixed "Episode notes were not saved" (Bug ID:1560185).
  - '"Get DVD Info" fails on Linux' should be fixed. (ID: 1513896)
  - If an error occured when removing an additional info field, it could be impossible to add er edit movies. (ID: 1376750)
  - Application could hang when opening new windows (e.g. Preferences or Queries). (Hopefully fixed)


Version 2.4 Beta 4.1 (18-August-2006)

- New:
 
  - Movie Info update function (ID: 1488702)
  - Windows support for MediaInfo library which enabled parsing of more media files (ID: 1492010).

- Bug fixes:
 
  - Fixed: Searching on TV.COM reported too many seasons.
  - Fixed: Error occured when trying to modify an episode twice.


Version 2.4 Beta 3 (21-July-2006)

- General improvements:
 
  - SplitPane added between Plot/Cast/Miscellaneous and Additional Info/Notes.
  - It's now possible to use a look & feel checkbox instead of the regular seen/unseen images.
  - There is no longer a minimum size to the main window.

- Bug fixes:

  - Fixed: Searching on TV.COM gives zero hits. (ID: 1520387).


Version 2.4 Beta 2 (27-June-2006)

- New:
  
  - The Notes can be edited in the main window and saved with button in Database menu. The info must be saved beofore the list is changed (new searches, different list).
  - The current database can be closed. 

- Bug fixes:
 
  - Fixed: It now actually works with MS Access and HSQLDB. Beta 1 gave bunch of Database errors.
  - The compile/run scripts have been updated to include current directory in the classpath. Basically that means it's no longer dependant on a system CLASSPATH variable containing the '.'.


Version 2.4 Beta (24-June-2006)

- New:
  
  - Queries can be displayed in a tree. (Thanks to Keith)
  - Search function is greatly enhanced (see Advanced_search.txt) (ID: 1486839)
  - Applet version - The MovieManager can now be launched as an applet using Applet.html (ID: 1492012)

- General improvements:
  
  - The date field can contain any character.
  - The entire 'first aired' date is now retrieved from Tv.COM.
  - New option to move 'A ' and 'An' as well as 'The' to the beginning of title.
  - File paths can now be stored relative to the database location. (ID: 1488689)
  - It's now possible to 'lock' the current database so that it will be saved to the config file even though other databases are loaded after. (ID: 1488086)
 
- Bug fixes:
  
  - Fixed: The move-'The'-to-the-beginning option is now saved correctly. (ID: 1488084)
  - Fixed: Not able to get info on series with only one season (ID: 1507762)
  - Fixed: The option Load last used list is buggy. (ID: 1488085)
  - Fixed: The Queries function is faulty on MySQL databases. (ID: 1504144)
  - Fixed: RIFF parser could produce OutOfMemoryError (ID: 1511820)


Version 2.32 (22-April-2006)

- General improvements/Bug fixes:
  
  - Fixed Bug: TV.COM not functioning (BugID: 1472873)
  - Fixed Bug: Location is wrong (BugID: 1457214) The path (Location field) to the imported files didn't always match.
  - Fixed Bug: MPAA information not saved (v 2.31) (BugID: 1424372)
  - Fixed Bug: The Country and Language fields could suppress the other General info fields.
  - Tooltip text showing aka (Also Known As) titles is now displayed when importing movies from IMDb.
  - Number of record hits in the Queries is now displayed in the title of the panel. (Thanks to Keith)


Version 2.31 (31-January-2006)

- General improvements/Bug fixes:
  
  - Fixed Bug: An error occured when trying to sort by duration when using a user-defined list.
  - Fixed Bug: Error could occur when updating the database to be v2.3 compatible. 
  - Fixed Bug: When the additional info fields were customized with a different order than the default
  - an error occured when trying to add/edit movies).


Version 2.3 (29-January-2006)

- New:
  
  - Support for MySQL database.
  - Added new fields: 'Also Known As', 'Certification', 'MPAA', 'IMDB runtime', 'Awards'.
  - Extended filter options. Can now use 'AND' and 'OR' when searching.
  - Saving last values on the additional info fields where the info isn't imported from media files.
  - The DivX container is now identified as DivX and not AVI.
  - Will now find the correct duration on openDML AVI files.
- General improvements/Bug fixes:
  
  - Searching should now be faster as it's now done solely by the database.
  - Fixed Bug: Unable to download covers from IMDb.
  - Fixed Bug: The title of imported movie info would contain a space in the beginning.
  - Fixed Bug: The "order by" setting was not saved correctly.
  - Fixed Bug: Plot contains html code on some IMDb info.
  - Fixed Bug: '&' would appear as '&amp;' in titles imported from tv.com.
  - Fixed bug: The user defined additional info fields (extra info) wouldn't be properly removed using the remove function. (Bug introduced in v2.2)
  - Hopefully fixed Bug: Infinite loop could occur when obtaining the episodes from tv.com.


Version 2.22 (04-November-2005)

- General improvements/Bug fixes:
  
  - Queries function has been optimized and will no longer be slow if there are many hits.
  - Fixed a bug in the extreme movie manager import function, where the info of a movie could be imported to the next movie if the info of the next movie was empty.   
  - Delete function has been optimized, and will be a lot faster when deleting more than 10 entries in one go.


Version 2.21 (20-October-2005)

- New: 
  
  - Macintosh support (tested on Mac OS X 10.3.9

- General improvements/fixes:
  
  - Export function slightly updated - It's not possible to export parts of your collection.
  - Import function is updated to show the progress during import.
  - Extreme movie manager import is improved with language option, and a few bugfixes.
  - Opening browser on Unix/Linux platforms will not lock the application anymore.
  - Cover settings added to Preferences


Version 2.2 (26-September-2005)
 
- New: 

  - Episode functionality for series, with automatic download of episode info from tv.com
  - Import function (3 modes - Simple text, excel and extreme movie manager (v4.5)
  - List functionality

- General improvements/fixes:
  
  - Fixed important bug where additional info fields with equal names could be added.
  - Fixed bug where Covers folder couldn't be saved on HSQL database (Thanks to linebaugh).
  - Set multiple entries to seen/unseen


Version 2.11 (27-June-2005)

- General improvements/fixes:
  
  - AVI bug fixed where the entire file would be parsed if no DivX5/Xvid bitstream version existed. 
  - HSQLDB -> MS Acess conversion would leave out the extension on the access database file. Fixed. 
  - Return/Enter on movie title in the Movie Info window will activate the get IMDB info. If title is empty Get file info is activated. 
  - Double click on movie entry opens Movie Info in Edit mode. 
  - Add/Edit functions is optimized to go a little faster. 


Version 2.1 (08-June-2005)

- New: 
  
  - Main window is resizable 
  - DVD and OGM file info support. 

- General improvements/fixes:
  
  - AVI video bitrate is correct. 
  - Multiple files can now be selected when getting file info. 
  - It should be a lot safer to change Look and Feels. 
  - Additional info fields can now be hidden and rearranged. 

- Note:
  
  - Old databases needs to be updated and will not be compatible with older versions 


Version 2.02 (23-April-2005)

- General improvements/fixes: 
  
  - It's possible to select multiple entries from the movie list to make removing multiple movies easier. 
  - Export feature is slightly updated. You can order output by title, directed by, rating or date. 
  - It's possible to use relative paths for database, queries and covers. (If you need to move the movie manager around). 


Version 2.01 (20-April-2005)

- New: 
  
  - Export function is slightly changed. It's now possible to divide full export list alphabetically. 


Version 2.0 (14-April-2005)

- New: 
   
  - HSQL database support which runs on Unix/Linux as well as Windows. 
  - Support for custom look and feels. 
  - Extended DivX5/Xvid version detection. 
  - Proxy SOCKS support. 
  - New icons/images has replaced the old ones. 

- General improvements/fixes:
  
  - Main window is slightly bigger which gives the additional info and notes areas more space. 
  - Fixed yet another bug with the entries. 


Version 1.81 (27-February-2005)

- New: 
  
  - Proxy support (HTTP). 

- General improvements/fixes:
  
  - Fixed a few bugs with the multi-add function. 
  - Fixed a bug with the entries. Should now show correct number after deleting and adding movies. 


Version 1.80 (14-February-2005)

- New: 
  
  - Multi-add feature. 
  - Three more info fields has been added (Country, Language and Colour). 
  - Layout can now be changed (Three different looks). 

- General improvements/fixes:
  
  - Search window is no longer modal, which means you do not need to close the search window to be able to search for movies. 
  - Search options and other info is now stored in the config.ini file. 
  - Fixed a bug with the entries. 


Version 1.70 (17-January-2005)

- New: 
  
  - Advanced Search: 
   
    - Filter by: Movie Title, Director, Writer, Genre, Cast. 
    - Order by: Movie Title, Director, Date, Rating. 
    - Show only movies: that are Seen/Unseen, with date above/below, with rating above/below. 
   
  - Seen/Unseen can be changed directly in the main movielist, by rightclicking on a selected movie.
  - Number of entries currently in the list is now displayed in the main window.
  - HotKeys has been added to: Add Movie, Remove Movie, Edit Movie and Advanced Search, in Main window.
  - HotKeys has been added to: Save, Get File Info, Get IMDB Info and Cancel, when adding/editing a movie.


- General improvements/fixes:

  - The program now retrives correct codec from .avi files, and an approximate value of the video bitrate. 
  - When searching for movies only the most popular results are shown. More hits can be accessed by pushing "More Titles". 
  - Shortcut to imdb movie site by clicking the image is fixed. 
  - Title field is focused by default when adding and editing movies. 


Version 1.67 beta (10-January-2005)

 - IMDB changed some of the html structure leaving the movie manager unable to download info on most movies 


Version 1.66 beta (10-February-2004)

 - Added remove confirmation dialog to prevent catastrophes for those of you still using this prototype... 


Version 1.65 beta (07-February-2004)

 - IMDB links structure have changed again (thanks Kica, for noticing me) 


Version 1.64 beta (03-September-2003)

 - IMDB links structure have changed - www.imdb.com/Title?0000000 can now also be www.imdb.com/title/tt000000 


Version 1.63 beta (21-July-2003)

 - Help URLs changed. All pages are now @ SourceForge.net 
 - This program has been discontinued and it will be redesigned, now that I known how to design UIs... It was written has a prototype only. 
 - Feel free to leave your suggestions for the new Movie Manager to come at the Forum. 
 - The new version will still be GPL, but maybe it will be written in C#... Furthermore the databases will be compatible, I expect... 


Version 1.62 beta (29-June-2003)

 - Bug fix: imdb ID was being lost after editing an existing movie 


Version 1.6 beta (17-June-2003)

 - Source code is now available under the GPL license 
 - Install and source files from this version up are now hosted @ SourceForge.net 


Version 1.5 beta (25-May-2003)

 - Added IMDB rating to Full HTML Export 


Version 1.4 beta (28-March-2003)

 - The Queries list is now sorted by name 
 - The default queries aren't extracted to the Queries folder anymore, they now stay inside the .jar 
 - If you have installed a previous version, then remove the following files from the Queries folder of each database: 
 - ( just remove all the contents of the Queries directory if you haven't added any new custom query... ) 
 - Count CD Cases.qry 
 - Count CDs.qry 
 - Count Movies.qry 
 - Sum Durations.qry 
 - Sum GB.qry 
 - Movies With SubTitles.qry 
 - Unseen Movies.qry 
 - Unseen Movies With SubTitles.qry 


Version 1.3 beta (25-March-2003)

 - Added 42 new RIFF audio tags support 
 - Added 33 new RIFF audio tags support 
 - "Sum MB" query is now "Sum GB" 


Version 1.2 beta (24-March-2003)

 - Fixed exit bug after database creation failed 
 - Fixed path resolution bug on database creation 
 - Some other minor and user-invisible code changes 


Version 1.1 beta (22-March-2003)

 - Added log file 
 - Version 1.0 beta (18-March-2003) - Non Install ZIP File or MSI Install File 
 - You have now an install file for directory and shortcuts creation 
 - The non install zip version will still be available for those of you who don't like install programs ;-) 
 - Added alert dialog if database creation fails 
 - Added browse option to dialog 'Database'->'Folders' 
 - Some other minor changes... 


Version 0.9 beta (16-March-2003) 

 - 0x2000 RIFF tag is now "AC3 Dolby Digital" and not "DVM" 
 - Fixed start connection to database bug on some systems 
 - Fixed close connection to database bug 


Version 0.8 beta (14-March-2003)

Version 0.7 beta (13-March-2003)


.. _MediaInfo: http://mediainfo.sourceforge.net
.. _duplicates: http://xmm.sourceforge.net/index.php?menu=help#duplicates
.. _NimROD: http://personales.ya.com/nimrod/index-en.html
