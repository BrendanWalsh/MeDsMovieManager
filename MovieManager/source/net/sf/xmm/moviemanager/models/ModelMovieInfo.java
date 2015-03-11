/**
 * @(#)ModelMovieInfo.java 29.01.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.models;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;
import net.sf.xmm.moviemanager.fileproperties.FilePropertiesMovie;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.util.events.ModelUpdatedEventListener;
import net.sf.xmm.moviemanager.util.events.ModelUpdatedHandler;
import net.sf.xmm.moviemanager.util.events.ModelUpdatedEvent.IllegalEventTypeException;

import org.apache.log4j.Logger;

public class ModelMovieInfo {
    	  
	static Logger log = Logger.getLogger(ModelMovieInfo.class);
		
    private ModelUpdatedHandler modelUpdatedHandler = new ModelUpdatedHandler();
        
    private boolean _edit = false;
    private boolean _saveCover = false;
    public boolean _hasReadProperties = false; // File info has been read and stored
    public boolean isEpisode = false;
    
    private List<String> _fieldNames = new ArrayList<String>();
    private List<String> _fieldValues = new ArrayList<String>();
    
    public boolean saveAdditionalInfo = true;
    
    // The index of the first extra info field
    public int EXTRA_START = 17;
    
    public ModelEntry model;
    
    public ModelSeries modelSeries = null;
    
    boolean nocover = true;
    
    /* Loading an empty episode model (adding episode) */
    public ModelMovieInfo(ModelSeries modelSeries) {
        this.modelSeries = modelSeries;
    	     	
        isEpisode = true;
        model = new ModelEpisode(modelSeries.getMovieKey());
        
        initializeAdditionalInfo(true);
	}
    
    /* Loading an empty model (movie) */
    public ModelMovieInfo() {
    	this(false, true);
    }
    
    /* Loading an empty model (movie or episode) */
    public ModelMovieInfo(boolean episode) {
    	this(episode, true);
    }
    
    /* Loading an empty model  (movie or episode) */
    public ModelMovieInfo(boolean episode, boolean loadEmptyAdditionalInfoFields) {
        
        isEpisode = episode;
        
        if (isEpisode)
            model = new ModelEpisode();
        else
            model = new ModelMovie();
		
        initializeAdditionalInfo(loadEmptyAdditionalInfoFields);
    }
    
    /* Initializes with the info from a model (Editing entry) */
    public ModelMovieInfo(ModelEntry model, boolean loadEmptyAdditionalInfoFields) {
    	    	
    	_edit = true;
    	
        if (model instanceof ModelEpisode) {
            this.model = new ModelEpisode((ModelEpisode) model);
            isEpisode = true;
        }
        else {
            this.model = new ModelMovie((ModelMovie) model);
        }
        
        initializeAdditionalInfo(loadEmptyAdditionalInfoFields);
    }
    
    /* Edit a movie without additional info (special case) */
    public ModelMovieInfo(ModelMovie model) {
        _edit = true;
        this.model = model;
        saveAdditionalInfo = false;
    }
    
    public ModelEntry getModel() {
    	return  model;
    }
        
    public boolean isEditMode() {
    	return _edit;
    }
    
    public List<String> getFieldNames() {
    	
    	if (!model.getHasAdditionalInfoData())
    		model.updateAdditionalInfoData();
    	
    	return _fieldNames;    	
    }
    
    public void setFieldNames(List<String> fieldNames) {
    	_fieldNames = fieldNames;
    }
    
    public List<String> getFieldValues() {
    	
    	if (!model.getHasAdditionalInfoData())
			model.updateAdditionalInfoData();	
    	
    	return _fieldValues;
    }
    
    public void setFieldValues(List<String> fieldValues) {
    	 _fieldValues = fieldValues;
    }
    
    public void addModelChangedEventListenener(ModelUpdatedEventListener listener) {
        modelUpdatedHandler.addModelChangedEventListenener(listener);
    }
    
    public void modelChanged(Object source, String type) throws IllegalEventTypeException {
        modelUpdatedHandler.modelChanged(source, type);
    }
        
    public Image getCoverImage() {
        
        Image image = null;
        
        try {
        	
            if (model.getCoverData() != null) {
            	 image = Toolkit.getDefaultToolkit().createImage(model.getCoverData()).getScaledInstance(97,145, Image.SCALE_SMOOTH);
            }
            else if (MovieManager.getIt().getDatabase().getDatabaseType().equals("MySQL") && 
                    !MovieManager.getConfig().getStoreCoversLocally()) {
            	
                model.updateCoverData();
                byte [] coverData = model.getCoverData();
                
                if (coverData != null)
                    image = Toolkit.getDefaultToolkit().createImage(model.getCoverData());
                else
                    log.warn("Cover data not available."); //$NON-NLS-1$
            }
            else if (!model.getCover().equals("")) {
            	
                File cover = new File(MovieManager.getConfig().getCoversPath(), model.getCover());
                
                if ((cover.exists())) {
                    /* Loads the image...*/
                    image = FileUtil.getImage(cover.getAbsolutePath()).getScaledInstance(97,145, Image.SCALE_SMOOTH);
                }
            }
            
            // Set nocover image
            if (image == null) {
                image = FileUtil.getImage("/images/" + MovieManager.getConfig().getNoCoverFilename()).getScaledInstance(97,145,Image.SCALE_SMOOTH); //$NON-NLS-1$
                nocover = true;
            }
            else
            	 nocover = false;
            
            } catch (Exception e) {
            log.error("", e); //$NON-NLS-1$
        }
        
        if (image == null) 
            log.warn("Cover file not found."); //$NON-NLS-1$
        
        return image;
    }
    
    public Image getSeenImage() {
        
        Image image = null;
        
        if (model.getSeen()) 
            image = FileUtil.getImage("/images/seen.png"); //$NON-NLS-1$
        else
            image = FileUtil.getImage("/images/unseen.png"); //$NON-NLS-1$
        
        return image.getScaledInstance(18,18,Image.SCALE_SMOOTH);    
    }
    
    public boolean getNoCover() {
    	return nocover;
    }
    
    public void setCover(String cover, byte[] coverData) {
    	setCover(cover, coverData, true);
    }
    
    public void setCover(String cover, byte[] coverData, boolean modelChanged) {
    	model.setCover(cover);
        model.setCoverData(coverData);
        
        if (cover != null && !cover.equals(""))
        	nocover = false;
        else
        	nocover = false;
        
        if (!modelChanged)
        	return;
        
        try {
        	modelChanged(this, "GeneralInfo");
        } catch (IllegalEventTypeException e) {
        	log.error("IllegalEventTypeException:" + e.getMessage());
        }
    }	
    
    public void setTitle(String title) {
        model.setTitle(title);
        
        try {
        	modelChanged(this, "GeneralInfo");
        } catch (IllegalEventTypeException e) {
        	log.error("IllegalEventTypeException:" + e.getMessage());
        }
    }
    
    public void setSeen(boolean seen) {
        model.setSeen(seen);
        
        try {
        	modelChanged(this, "GeneralInfo");
        } catch (IllegalEventTypeException e) {
        	log.error("IllegalEventTypeException:" + e.getMessage());
        }
    }
    
    public void setGeneralInfoFieldsEmpty() {
       
    	model.setKey(-1);
        model.setUrlKey("");
        model.setCover("");
        model.setCoverData(null);
        model.setDate("");
        model.setDirectedBy("");
        model.setWrittenBy("");
        model.setGenre("");
        model.setRating("");
        model.setPersonalRating("");
        model.setPlot("");
        model.setCast("");
        model.setNotes("");
        model.setSeen(false);
        model.setAka("");
        model.setCountry("");
        model.setLanguage("");
        model.setColour("");
        model.setMpaa("");
        model.setCertification("");
        model.setWebSoundMix("");
        model.setWebRuntime("");
        model.setAwards("");
        
        if (isEpisode) {
            ((ModelEpisode) model).setMovieKey(-1);
            ((ModelEpisode) model).setEpisodeKey(-1);
        }
        
        try {
        	modelChanged(this, "GeneralInfo");
        } catch (IllegalEventTypeException e) {
        	log.error("IllegalEventTypeException:" + e.getMessage());
        }
    }
    
    /**
     * Empties all the additional fields values stored in the _fieldValues arrayList
     **/
    public void setAdditionalInfoFieldsEmpty() {
        
        try {
            for (int i = 0; i < _fieldValues.size(); i++)
                _fieldValues.set(i,""); //$NON-NLS-1$
            
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage()); //$NON-NLS-1$
            e.printStackTrace();
            _hasReadProperties = false;
        }
        
        try {
        	modelChanged(this, "AdditionalInfo");
        } catch (IllegalEventTypeException e) {
        	log.error("IllegalEventTypeException:" + e.getMessage());
        }
    }
    
    public void saveAdditionalInfoData() {
        
    	if (!saveAdditionalInfo)
    		return;
    	
        ModelAdditionalInfo additionalInfo = model.getAdditionalInfo();
        
        /* Sets the additional info... */
        
        // Duration
        if (!((String) _fieldValues.get(1)).equals("")) { //$NON-NLS-1$
            additionalInfo.setDuration(Integer.parseInt((String) _fieldValues.get(1)));
        }
        else
        	additionalInfo.setDuration(0);
        
        // file size
        if (!((String) _fieldValues.get(2)).equals("")) { //$NON-NLS-1$
            additionalInfo.setFileSize(Integer.parseInt((String) _fieldValues.get(2)));
        }
        else
        	additionalInfo.setFileSize(0);
        
        // Cds
        if (!((String) _fieldValues.get(3)).equals("")) { //$NON-NLS-1$
            additionalInfo.setCDs(Integer.parseInt((String) _fieldValues.get(3)));
        }	 
        else
        	additionalInfo.setCDs(0);
        
        // CD cases
        if (!((String) _fieldValues.get(4)).equals("")) { //$NON-NLS-1$
            additionalInfo.setCDCases(Double.parseDouble((String) _fieldValues.get(4)));
        }
        else
        	additionalInfo.setCDCases(0);
        
        // File count
        if (!((String) _fieldValues.get(14)).equals("")) { //$NON-NLS-1$
            additionalInfo.setFileCount(Integer.parseInt((String) _fieldValues.get(14)));
        }
        else
        	additionalInfo.setFileCount(0);
		
        additionalInfo.setSubtitles((String) _fieldValues.get(0));
        additionalInfo.setResolution((String) _fieldValues.get(5));
        additionalInfo.setVideoCodec((String) _fieldValues.get(6));
        additionalInfo.setVideoRate((String) _fieldValues.get(7));
        additionalInfo.setVideoBitrate((String) _fieldValues.get(8));
        additionalInfo.setAudioCodec((String) _fieldValues.get(9));
        additionalInfo.setAudioRate((String) _fieldValues.get(10));
        additionalInfo.setAudioBitrate((String) _fieldValues.get(11));
        additionalInfo.setAudioChannels((String) _fieldValues.get(12));
        additionalInfo.setFileLocation((String) _fieldValues.get(13));
        
        additionalInfo.setContainer((String) _fieldValues.get(15));
        additionalInfo.setMediaType((String) _fieldValues.get(16));
        
        
        ArrayList<String> extraFieldValuesList = new ArrayList<String>();
        
        for (int i = EXTRA_START; i < _fieldNames.size(); i++) {
            extraFieldValuesList.add( _fieldValues.get(i));
        }
        additionalInfo.setExtraInfoFieldValues(extraFieldValuesList);
        
    }
    
    public synchronized ModelEntry saveToDatabase() throws Exception {
    	return saveToDatabase(null);
    }
    
    public synchronized ModelEntry saveToDatabase(ArrayList<String> listNames) throws Exception {
    	
    	saveAdditionalInfoData();

//      Removing the cached info for the node
    	MovieManager.getDialog().getTreeCellRenderer().removeEntry(model);
    	
    	return saveToDatabase(model, _edit, listNames);
    }
        
    public synchronized ModelEntry saveToDatabase(ModelEntry modelToSave, boolean edit, ArrayList <String> listNamesToApply) throws Exception {
      
    	Database database = MovieManager.getIt().getDatabase();
        ModelAdditionalInfo additionalInfo = modelToSave.getAdditionalInfo();
        
        if (isEpisode) {
            
            if (edit) {
                /* Editing episode */
                
                database.setGeneralInfoEpisode(modelToSave.getKey(), (ModelEpisode) modelToSave);
                database.setAdditionalInfoEpisode(modelToSave.getKey(), additionalInfo);
                	
                //Must save to extra info even though there are no extra info fields. The rows must still be created in the database
                database.setExtraInfoEpisode(modelToSave.getKey(), database.getExtraInfoFieldNames(false), additionalInfo.getExtraInfoFieldValues());
               
            } else {
                
                if (((ModelEpisode) modelToSave).getMovieKey() == -1)
                    throw new Exception("Cannot add episode with MovieKey: -1");
                
                /* Adding episode */
                
                int episodeindex = database.addGeneralInfoEpisode((ModelEpisode) modelToSave);
                 
                if (episodeindex != -1) {
                    
                    int ret;
                    
                    ((ModelEpisode) modelToSave).setKey(episodeindex);
                    
                    /* Adds the additional info... */
                    ret = database.addAdditionalInfoEpisode(episodeindex, additionalInfo);
                     
                    //Must save to extra info even though there are no extra info fields. The rows must still be created in the database
                    ret = database.addExtraInfoEpisode(episodeindex, database.getExtraInfoFieldNames(false), additionalInfo.getExtraInfoFieldValues());
                      
                }
            }
        } else {
            
        	/* Editing movie */
            if (edit) {
            	
                database.setGeneralInfo((ModelMovie) modelToSave);
                
                if (saveAdditionalInfo) {
                    database.setAdditionalInfo(modelToSave.getKey(), additionalInfo);
                    
					// Must save to extra info even though there are no extra info fields. The rows must still be created in the database
					database.setExtraInfoMovie(modelToSave.getKey(), database.getExtraInfoFieldNames(false), additionalInfo.getExtraInfoFieldValues());
				}
                
            } else {
                /* Adding new movie */
                
            	int index = MovieManager.getIt().getDatabase().addGeneralInfo((ModelMovie) modelToSave);
                
                modelToSave.setKey(index);
                
                if (index != -1) {
                    
                    database.addAdditionalInfo(modelToSave.getKey(), additionalInfo);
                    
					// Must save to extra info even though there are no extra info fields. The rows must still be created in the database
					database.addExtraInfoMovie(modelToSave.getKey(), database.getExtraInfoFieldNames(false), additionalInfo.getExtraInfoFieldValues());
                    
                    
                    /* Add new row in Lists table with default values */
                    ArrayList<String> dbListNames = database.getListsColumnNames();
                    ArrayList<Boolean> listValues = new ArrayList<Boolean>();
                    
                   if (listNamesToApply == null)
                	   listNamesToApply = new ArrayList<String>();
                    
                    for (int i = 0; i < dbListNames.size(); i++) {
                    	
                    	if (listNamesToApply.contains(dbListNames.get(i)))
                    		listValues.add(new Boolean(true));
                        else
                            listValues.add(new Boolean(false));
                    }
                    database.addLists(modelToSave.getKey(), dbListNames, listValues);
                }
            }
        }
        
		
        return modelToSave;
    }
    
    
    /**
     * Sets _saveCover.
     **/
    public void setSaveCover(boolean saveCover) {
        
        if (!((MovieManager.getIt().getDatabase() instanceof DatabaseMySQL) && !MovieManager.getConfig().getStoreCoversLocally()))
            _saveCover = saveCover;
        else
            _saveCover = false;
    }
    
    /**
     * Gets the file properties...
     * @throws Exception 
     **/
    public void getFileInfo(File [] file) throws Exception {
        
        for (int i = 0; i < file.length; i++) {
            
            if (!file[i].isFile())
                throw new FileNotFoundException(file[i].getAbsolutePath());
            
            /* Reads the info... */
            FilePropertiesMovie properties = new FilePropertiesMovie(file[i].getAbsolutePath(), MovieManager.getConfig().getUseMediaInfoDLL());

            getFileInfo(properties);
        }
    }    
    

    public void getFileInfo(FilePropertiesMovie properties) {

    	try {

    		if (!properties.getSubtitles().equals("")) //$NON-NLS-1$
    			_fieldValues.set(0, properties.getSubtitles());

    		/* Saves info... */
    		int duration = properties.getDuration();

    		if (_hasReadProperties && duration != -1 && !((String) _fieldValues.get(1)).equals("")) { //$NON-NLS-1$
    			duration += Integer.parseInt((String) _fieldValues.get(1));
    		}

    		if (duration != -1) {
    			_fieldValues.set(1, String.valueOf(duration));
    		} else {
    			_fieldValues.set(1, ""); //$NON-NLS-1$
    		}

    		int fileSize = properties.getFileSize();

    		if (_hasReadProperties && fileSize != -1 && !((String) _fieldValues.get(2)).equals("")) { //$NON-NLS-1$
    			fileSize += Integer.parseInt((String) _fieldValues.get(2));
    		}

    		if (fileSize != -1) {
    			_fieldValues.set(2,String.valueOf(fileSize));
    		} else {
    			_fieldValues.set(2,""); //$NON-NLS-1$
    		}

    		if (fileSize != -1) {
    			int cds = (int)Math.ceil(fileSize / 702.0);
    			_fieldValues.set(3,String.valueOf(cds));
    		}

    		if (((String) _fieldValues.get(4)).equals("")) { //$NON-NLS-1$
    			_fieldValues.set(4,String.valueOf(1));
    		}

    		_fieldValues.set(5,properties.getVideoResolution());
    		_fieldValues.set(6,properties.getVideoCodec());
    		_fieldValues.set(7,properties.getVideoRate());
    		_fieldValues.set(8,properties.getVideoBitrate());
    		_fieldValues.set(9,properties.getAudioCodec());
    		_fieldValues.set(10,properties.getAudioRate());
    		_fieldValues.set(11,properties.getAudioBitrate());

    		String audioChannels = properties.getAudioChannels();
    		_fieldValues.set(12, audioChannels);

    		String location = properties.getLocation();

    		String currentValue = (String) _fieldValues.get(13);

    		if (!currentValue.equals("") && _hasReadProperties) {

    			StringTokenizer tokenizer = new StringTokenizer(currentValue, "*"); //$NON-NLS-1$
    			boolean fileAlreadyAdded = false;

    			while (tokenizer.hasMoreTokens()) {
    				if (tokenizer.nextToken().equals(location))
    					fileAlreadyAdded = true;
    			}

    			if (fileAlreadyAdded)
    				log.warn("file already added"); //$NON-NLS-1$
    			else {
    				currentValue += "*" + location; //$NON-NLS-1$
    				_fieldValues.set(13, currentValue);
    			}
    		}
    		else 
    			_fieldValues.set(13, location);


    		int fileCount = 1;
    		if (_hasReadProperties && !((String) _fieldValues.get(14)).equals("")) { //$NON-NLS-1$
    			fileCount += Integer.parseInt((String) _fieldValues.get(14));
    		}

    		_fieldValues.set(14, String.valueOf(fileCount));

    		_fieldValues.set(15, properties.getContainer());

    		_fieldValues.set(16, model.getAdditionalInfo().getMediaType());

    		/* Sets title if title field is empty... */
    		if (model.getTitle().equals("")) { //$NON-NLS-1$
    			String title = "";
				
    			if (properties.getMetaDataTagInfo("INAM") != null && !properties.getMetaDataTagInfo("INAM").equals("")) //$NON-NLS-1$ //$NON-NLS-2$
    				setTitle(title + properties.getMetaDataTagInfo("INAM")); //$NON-NLS-1$

    			else {
    				title = title + properties.getFileName();

    				if (title.lastIndexOf(".") != -1) //$NON-NLS-1$
    					title = title.substring(0, title.lastIndexOf('.'));

    				setTitle(title);
    			}
    		}

    		_hasReadProperties = true;
	
    		saveAdditionalInfoData();
    		
    		modelChanged(this, "AdditionalInfo");

    	} catch (Exception e) {
    		log.error("Exception: " + e.getMessage(), e); //$NON-NLS-1$
    	}

    }
    
        
    public void initializeAdditionalInfo(boolean loadEmpty) {
       
        ModelAdditionalInfo additionalInfo = null;
        
        _fieldNames = new ArrayList<String>();
        _fieldValues = new ArrayList<String>();
        
        if (!loadEmpty) {
         	
        	if (!model.getHasAdditionalInfoData())
                model.updateAdditionalInfoData();
            
            additionalInfo = model.getAdditionalInfo();
        }   
        
        _fieldNames.add("Subtitles"); //$NON-NLS-1$
            
        if (loadEmpty)
            _fieldValues.add("");
        else if (additionalInfo != null)
            _fieldValues.add(additionalInfo.getSubtitles());
        
        _fieldNames.add("Duration"); //$NON-NLS-1$
        
        
        if (!loadEmpty && additionalInfo != null && additionalInfo.getDuration() > 0) {
            _fieldValues.add(String.valueOf(additionalInfo.getDuration()));
        } else {
            _fieldValues.add(""); //$NON-NLS-1$
        }
         
        _fieldNames.add("File Size"); //$NON-NLS-1$
        
	   
        if (!loadEmpty && additionalInfo != null && additionalInfo.getFileSize() > 0) {
            _fieldValues.add(String.valueOf(additionalInfo.getFileSize()));
        } else {
            _fieldValues.add(""); //$NON-NLS-1$
        }
        
        _fieldNames.add("CDs"); //$NON-NLS-1$
        
        if (!loadEmpty && additionalInfo != null && additionalInfo.getCDs() > 0) {
            _fieldValues.add(String.valueOf(additionalInfo.getCDs()));
        } else {
            _fieldValues.add(""); //$NON-NLS-1$
        }
        
        _fieldNames.add("CD Cases"); //$NON-NLS-1$
        
        if (!loadEmpty && additionalInfo != null && additionalInfo.getCDCases() > 0) {
            _fieldValues.add(String.valueOf(additionalInfo.getCDCases()));
        } else {
            _fieldValues.add(""); //$NON-NLS-1$
        }
        
        _fieldNames.add("Resolution"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else if (additionalInfo != null)
            _fieldValues.add(additionalInfo.getResolution());
        
        _fieldNames.add("Video Codec"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else if (additionalInfo != null)
            _fieldValues.add(additionalInfo.getVideoCodec());
        
        _fieldNames.add("Video Rate"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else if (additionalInfo != null)
            _fieldValues.add(additionalInfo.getVideoRate());
        
        _fieldNames.add("Video Bit Rate"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else if (additionalInfo != null)
            _fieldValues.add(additionalInfo.getVideoBitrate());
        
        _fieldNames.add("Audio Codec"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else if (additionalInfo != null)
            _fieldValues.add(additionalInfo.getAudioCodec());
        
        _fieldNames.add("Audio Rate"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else if (additionalInfo != null)
            _fieldValues.add(additionalInfo.getAudioRate());
        
        _fieldNames.add("Audio Bit Rate"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else if (additionalInfo != null)
            _fieldValues.add(additionalInfo.getAudioBitrate());
        
        _fieldNames.add("Audio Channels"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else if (additionalInfo != null)
            _fieldValues.add(additionalInfo.getAudioChannels());
        
        _fieldNames.add("Location"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else if (additionalInfo != null)
            _fieldValues.add(additionalInfo.getFileLocation());
        
        _fieldNames.add("File Count"); //$NON-NLS-1$
        
        
        if (!loadEmpty && additionalInfo != null && additionalInfo.getFileCount() > 0) {
            _fieldValues.add(String.valueOf(additionalInfo.getFileCount()));
        } else {
            _fieldValues.add(""); //$NON-NLS-1$
        }
        
        _fieldNames.add("Container"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else if (additionalInfo != null)
            _fieldValues.add(additionalInfo.getContainer());
        
        _fieldNames.add("Media Type"); //$NON-NLS-1$
        
        if (loadEmpty)
            _fieldValues.add("");
        else if (additionalInfo != null)
            _fieldValues.add(additionalInfo.getMediaType());
        
        
        ArrayList<String> extraFieldNames = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(true);
        ArrayList<String> extraFieldValues = null;
        
        if (!loadEmpty && additionalInfo != null)
            extraFieldValues = additionalInfo.getExtraInfoFieldValues();
		  
        for (int i = 0; i < extraFieldNames.size(); i++) {
            
            _fieldNames.add(extraFieldNames.get(i));
            
            if (loadEmpty)
                _fieldValues.add("");
            else if (additionalInfo != null)
                _fieldValues.add(extraFieldValues.get(i));
        }
        
        for (int i = EXTRA_START; i < _fieldNames.size(); i++) {
            
            if (loadEmpty) {
                _fieldValues.add("");
            }
            else {
                if (isEpisode)
                    _fieldValues.add(MovieManager.getIt().getDatabase().getExtraInfoEpisodeField(model.getKey(),(String)_fieldNames.get(i)));
                else
                    _fieldValues.add(MovieManager.getIt().getDatabase().getExtraInfoMovieField(model.getKey(),(String)_fieldNames.get(i)));
            }        
        }
    }
    
    
    
    public boolean saveCoverToFile() throws Exception {
     	
		if (!_saveCover)
			return false;
	
        byte [] cover = model.getCoverData();
        String coverName = model.getCover();
         
        if (cover == null) {
        	throw new Exception("Unable to save cover: Data is null");
        }
        else if (coverName == null || coverName.equals("")) {
        	throw new Exception("Unable to save cover file:" + coverName);
        }
        
        /* Saves the cover... */      
        
        String coversFolder = MovieManager.getConfig().getCoversPath();
        File coverFile = new File(coversFolder, coverName);
        
        if (coverFile.exists()) {
            if (!coverFile.delete() ) {
             	
            	File tempDir = new File(coverFile.getParentFile(), "temp");
            	
            	if (!tempDir.exists() && !tempDir.mkdir()) {
            		throw new Exception("Failed to create temporary directory in cover dir:" + tempDir); //$NON-NLS-1$
            	}
              		
            	if (!coverFile.delete())
            		throw new Exception("Failed to delete old cover file:" + coverFile); //$NON-NLS-1$
            }
            else if (!coverFile.createNewFile()) {
            	throw new Exception("Failed to create new cover file:" + coverFile); //$NON-NLS-1$
            }
        } else {
        	try {
        		if (!coverFile.createNewFile()) {
        			throw new Exception("Cannot create cover file:" + coverFile.getAbsolutePath()); //$NON-NLS-1$
        		}
        	} catch (IOException e) {
        		throw new Exception("Cannot create cover file:" + coverFile.getAbsolutePath()); //$NON-NLS-1$
        	}
        }
        
        /* Copies the cover to the covers folder... */
        FileUtil.writeToFile(cover, coverFile);
       
        return true;
    }
    

    public void modifyTitle() {
    	executeTitleModification(model);
    	setTitle(model.getTitle());

    	try {
    		modelChanged(this, "GeneralInfo");
    	} catch (IllegalEventTypeException e) {
    		log.error("IllegalEventTypeException:" + e.getMessage());
    	}
    }
    
    /**
     * Makes changes to the aka titles, and/or the main title according to the settings i the preferences.
     * 
     * @param originalTitle
     */
    public static void executeTitleModification(ModelEntry modelToModify) {
	
    	boolean removeDuplicates = !MovieManager.getConfig().getIncludeAkaLanguageCodes();

    	ArrayList<String> akaKeys = new ArrayList<String>();
    	ArrayList<String> akaValues = new ArrayList<String>();

    	String title = modelToModify.getTitle();
    	String akaTitles = modelToModify.getAka();
    	String newAkaTitles = "";

    	StringTokenizer tokenizer = new StringTokenizer(akaTitles, "\r\n", false);

    	String value;
    	String tmp;
    	String key = "";
    	int index = 0;
    	String languageCode = MovieManager.getConfig().getTitleLanguageCode();
	
    	while (tokenizer.hasMoreTokens()) {

    		value = tokenizer.nextToken();
    		
    		try {
				key = StringUtil.performExcludeParantheses(value, false).trim();
			} catch (Exception e) {
				log.error("Exception:" + e.getMessage(), e);
			}
	    		
    		if (MovieManager.getConfig().getUseLanguageSpecificTitle() && value.indexOf("[" + languageCode + "]") != -1) {

    			if (MovieManager.getConfig().getIncludeAkaLanguageCodes()) {
    				akaKeys.add(0, modelToModify.getTitle());
    				akaValues.add(0, title + " (Original)");
    			}
    			else if (removeDuplicates && akaKeys.indexOf(modelToModify.getTitle()) != -1) {
    				
    				akaKeys.remove(modelToModify.getTitle());
    				akaValues.remove(modelToModify.getTitle());
    				
    				akaKeys.add(0, modelToModify.getTitle());
    				akaValues.add(0, modelToModify.getTitle());
    			}
    			else  {
    				akaKeys.add(0, title);	
    				akaValues.add(0, title);
    			}	
    			modelToModify.setTitle(key);
    		}

    		boolean allAkaTitles = MovieManager.getConfig().getStoreAllAkaTitles();

    		index = akaKeys.indexOf(key);

    		//  Title already exists. Adds the language code to the existing title 
    		if (removeDuplicates && index != -1) {

    			if (value.indexOf("[") != -1) {
    				value =  value.replaceFirst(key, "");
    				tmp = akaValues.get(index) + " " + value.trim();
    				akaValues.set(index, tmp);
    			}   
    		}
    		else if (!(!allAkaTitles && value.indexOf("[") != -1)) {

    			/* Removes comments and language code */
    			if (!MovieManager.getConfig().getIncludeAkaLanguageCodes()) {
    				value = key;
    			}

    			if (!value.equals(modelToModify.getTitle())) {
    				akaKeys.add(key);
    				akaValues.add(value);
    			}
    		}
    	}

    	while (!akaValues.isEmpty()) {
    		newAkaTitles += akaValues.remove(0) + "\r\n";
    	}   

    	modelToModify.setAka(newAkaTitles.trim());
    }

    
    public void clearModel() {
    	clearModel(false);
    }
    
    /**
     * *
     * * @param addNewEpisode     true if a new episode will be added
     */
    public void clearModel(boolean addNewEpisode) {
		
        if (isEpisode) {
			
        	if (addNewEpisode && !_edit) {
        		model = new ModelEpisode(modelSeries.getMovieKey());
        		model.setTitle(modelSeries.getMovie().getTitle());
        	}
        	else
        		model = new ModelEpisode();
        }
        else
            model = new ModelMovie();
               
        try {
        	modelChanged(this, "GeneralInfo");
        } catch (IllegalEventTypeException e) {
        	log.error("IllegalEventTypeException:" + e.getMessage());
        }
    }
    
    public synchronized void setModel(ModelEntry model, boolean copyKey, boolean modelChanged) {
        
    	if (model.isEpisode()) {
			
            if (copyKey) {
            	if (_edit)
            		((ModelEpisode) model).setKey(this.model.getKey());
            	else
            		((ModelEpisode) model).setMovieKey(modelSeries.getMovieKey());
            }
            isEpisode = true;
        }
        else
            isEpisode = false;
        
        this.model = model;
                
        initializeAdditionalInfo(false);
	
        if (modelChanged) {
        	try {
            	modelChanged(this, "GeneralInfo");
            } catch (IllegalEventTypeException e) {
            	log.error("IllegalEventTypeException:" + e.getMessage());
            }
        }
    }
}
