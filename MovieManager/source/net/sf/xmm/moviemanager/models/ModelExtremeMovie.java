package net.sf.xmm.moviemanager.models;
/**
 * @(#)ModelExtremeMovie.java 1.0 26.01.06 (dd.mm.yy)
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



public class ModelExtremeMovie {
    
    /*The database key for this movie.*/
    public String imdb;
    public String scriptUsed;
    public String title;
    public String date;
    public String directed_by;
    public String written_by;
    public String genre;
    public String rating;
    public String plot;
    public String cast;
    public boolean seen;
    
    /*Aka - Also known as*/
    public String aka;
    public String country;
    public String language;
    public String colour;
    public String mpaa;
    
    public String cover;
    public String notes;
    protected String personalRating;
    public String subtitles;
    public String length;
    public String cds;
    public String codec;
    public String resolution;
    public String videoRate;
    public String bitrate;
    public String audioCodec;
    public String filesize;
    public String sampleRate;
    public String audioBitrate;
    public String channels;
    protected String movieFile1;
    protected String movieFile2;
    protected String movieFile3;
    protected String movieFile4;
    protected String movieFile5;
    protected String movieFile6;
    public String media;
    protected String videoDVD;
    protected String audioDVD;
    public String originalLanguage;
    public String filePath = "";
    public int fileCount = 0;
    
	/**
	 * The constructor.
	 **/
	public ModelExtremeMovie(String imdb, String scriptUsed, String cover, String date, String title, String directed_by, String written_by, String genre, String subgenre, String rating, String personalRating, String plot, String cast, String notes, boolean seen, String aka, String country, String language, String originalLanguage, String mpaa, String colour, String subtitles, String length, String cds, String codec, String resolution, String videoRate, String bitrate, String audioCodec, String filesize, String sampleRate, String audioBitrate, String channels, String media, String videoDVD, String audioDVD, String movieFile1, String movieFile2, String movieFile3, String movieFile4, String movieFile5, String movieFile6) {
	    
	    
	    this.imdb = imdb;
	    this.scriptUsed = scriptUsed;
	    this.cover = cover;
	    this.date = date;
	    this.title = title;
	    this.directed_by = directed_by;
	    this.written_by = written_by;
	    this.genre = genre;
	    
	    if (!genre.equals("") && !subgenre.equals(""))
		this.genre += "/";
	    this.genre += subgenre;
	    
	    this.rating = rating;
	    this.personalRating = personalRating;
	    this.plot = plot.trim();
	    this.cast = cast.trim();
	    this.notes = notes;
	    this.seen = seen;
	    this.aka = aka;
	    this.country = country; 
	    this.language = language;
	    this.originalLanguage = originalLanguage;
	    this.mpaa = mpaa;
	    
	    if (colour.equals("1"))
		this.colour = "Color";
	    else
		this.colour = "Black and White";
	    
	    this.length = length;
	    this.subtitles = subtitles;
	    this.cds = cds;
	    this.codec = codec;
	    this.resolution = resolution;
	    this.videoRate = videoRate;
	    this.bitrate = bitrate;
	    this.audioCodec = audioCodec;
	    
	    this.filesize = filesize;
	    
	    this.sampleRate = sampleRate;
	    this.audioBitrate = audioBitrate;
	    this.channels = channels;
	    this.media = media;
	    this.videoDVD = videoDVD;
	    this.audioDVD = audioDVD;
	    this.movieFile1 = movieFile1;
	    this.movieFile2 = movieFile2;
	    this.movieFile3 = movieFile3;
	    this.movieFile4 = movieFile4;
	    this.movieFile5 = movieFile5;
	    this.movieFile6 = movieFile6;
	    
	    /* Fixing the filePaths */
	    if (!movieFile1.equals("")) {
		filePath += movieFile1;
		fileCount++;
	    }
	    
	    if (!movieFile2.equals("")) {
		if (!filePath.equals(""))
		    filePath += "*";
		filePath += movieFile2;
		fileCount++;
	    }
	    
	    if (!movieFile3.equals("")) {
		if (!filePath.equals(""))
		    filePath += "*";
		filePath += movieFile3;
		fileCount++;
	    }
	    
	    if (!movieFile4.equals("")) {
		if (!filePath.equals(""))
		    filePath += "*";
		filePath += movieFile4;
		fileCount++;
	    }
	    
	    if (!movieFile5.equals("")) {
		if (!filePath.equals(""))
		    filePath += "*";
		filePath += movieFile5;
		fileCount++;
	    }
	    
	    if (!movieFile6.equals("")) {
		if (!filePath.equals(""))
		    filePath += "*";
		filePath += movieFile6;
		fileCount++;
	    }
	}
    
}
