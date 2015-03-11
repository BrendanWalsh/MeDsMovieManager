package net.sf.xmm.moviemanager.util.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.DatabaseMySQL;
import net.sf.xmm.moviemanager.models.ModelAdditionalInfo;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelMovie;

import org.apache.log4j.Logger;

/**
 * DataSource for Report Generator
 *
 * @author olba2
 */
public class ReportGeneratorDataSource implements JRDataSource {
    private Iterator<ModelEntry> iterator;
    private ModelEntry entry;
    private JProgressBar progressBar;
    private int count = 0;
    private URL defaultCoverImageURL;
    private boolean mySQL;
    private String coversFolder;
    private boolean testmode;
    private boolean interrupt = false;

    private String reportTitle = "Movielist";
    
    /**
     * Constructor
     *
     * @param root DefaultMutableTreeNode - pass MovieList root
     * @param includeEpisodes boolean - if true, episodes are included in the
     *   report, otherwise they are omitted.
     * @param sortField String - name of field to sort list by - "none" for no
     *   sorting.
     * @param progressBar JProgressBar - progressbar to update during
     *   generation of report
     * @param defaultCoverImageURL URL - default image for movies without cover
     * @param testmode boolean - if true only dummydata is returned
     */
    public ReportGeneratorDataSource(final List<ModelEntry> movies, String reportTitle, String sortField, final JProgressBar progressBar, URL defaultCoverImageURL, boolean testmode) {
        this.progressBar = progressBar;
        this.defaultCoverImageURL = defaultCoverImageURL;
        this.testmode = testmode;
        this.mySQL = MovieManager.getIt().getDatabase().isMySQL();
        this.coversFolder = MovieManager.getConfig().getCoversPath();

        if (reportTitle != null)
        	this.reportTitle = reportTitle;
        	
        if (sortField != null && sortField.length() > 0 && !sortField.equalsIgnoreCase("none")) {
            Collections.sort(movies, new MovieComparator(sortField));
        }
        
        if (progressBar != null) {
        	 SwingUtilities.invokeLater(new Runnable() {
             	public void run() {
             		 progressBar.setMinimum(0);
                     progressBar.setMaximum(movies.size());
                     progressBar.setValue(0);
             	}
             });
        }
        
        iterator = movies.iterator();
    }

    /**
     * next - called when report generator is moving to next entry
     *
     * @return boolean - true as long as there are more records
     * @throws JRException
     */
    public boolean next() throws JRException {
    	    	
        if (iterator.hasNext() && !interrupt) {
            entry = (ModelEntry) iterator.next();
                        
            int key = entry.getKey();
            if (key >= 0) {
                if (entry instanceof ModelMovie) {
                    entry = MovieManager.getIt().getDatabase().getMovie(key);
                }
                else {
                    entry = MovieManager.getIt().getDatabase().getEpisode(key);
                }
                if (progressBar != null) {
                	 SwingUtilities.invokeLater(new Runnable() {
                     	public void run() {
                     		progressBar.setValue(count++);
                     	}
                     });
                }
            }
            return true;
        }
        return false;
    }

    /**
     * getFieldValue - return value for a field from the current when report
     * generator asks for it by name.
     *
     * @param jRField JRField - identifies a field in the report
     * @return Object - value for field. Type must match definition in report
     *   layout.
     * @throws JRException
     */
    public Object getFieldValue(JRField jRField) throws JRException {
    	String name = jRField.getName();
    	Object ret = null;
    	// General fields
    	
    	if (name.equalsIgnoreCase("ReportTitle")) {
    		ret = reportTitle;
    	}
    	else if (name.equalsIgnoreCase("Cover")) {
        		
            if (!testmode && entry.getCover() != null && entry.getCover().length() > 0) {
            	String filename = coversFolder + "/" + entry.getCover();
                
                if (mySQL && entry.getCoverData() != null) {
                    try {
                        File tempFile = File.createTempFile("xmm", filename.substring(filename.indexOf('.')));
                        tempFile.deleteOnExit();
                        FileOutputStream fos = new FileOutputStream(tempFile);
                        fos.write(entry.getCoverData());
                        fos.close();
                        ret = tempFile.getPath();
                    }
                    catch (Exception ex) {
                        Logger.getRootLogger().error("Error saving temporary coverfile for " + filename, ex);
                    }
                }
                
                if (new File(filename).exists()) {
                	ret = filename;
                }
                else
                	ret = defaultCoverImageURL.toString();
            }
            else {
            	ret = defaultCoverImageURL.toString();
            }
        }
        else if (name.equalsIgnoreCase("Genre")) {
        	ret = entry.getGenre();
        }
        else if (name.equalsIgnoreCase("Awards")) {
        	ret = entry.getAwards();
        }
        else if (name.equalsIgnoreCase("Notes")) {
        	ret = entry.getNotes();
        }
        else if (name.equalsIgnoreCase("Mpaa")) {
        	ret = entry.getMpaa();
        }
        else if (name.equalsIgnoreCase("Cast")) {
        	ret = entry.getCast();
        }
        else if (name.equalsIgnoreCase("Date")) {
        	ret = entry.getDate();
        }
        else if (name.equalsIgnoreCase("IMDB")) {
        	ret = entry.getUrlKey();
        }
        else if (name.equalsIgnoreCase("imdb-link")) {
        	ret = entry.getCompleteUrl();
        }
        else if (name.equalsIgnoreCase("DirectedBy")) {
        	ret = entry.getDirectedBy();
        }
        else if (name.equalsIgnoreCase("Plot")) {
        	ret = testmode ? "" : entry.getPlot();
        }
        else if (name.equalsIgnoreCase("WrittenBy")) {
        	ret = entry.getWrittenBy();
        }
        else if (name.equalsIgnoreCase("SoundMix")) {
        	ret = entry.getWebSoundMix();
        }
        else if (name.equalsIgnoreCase("Language")) {
        	ret = entry.getLanguage();
        }
        else if (name.equalsIgnoreCase("Genre")) {
        	ret = entry.getGenre();
        }
        else if (name.equalsIgnoreCase("Colour")) {
        	ret = entry.getColour();
        }
        else if (name.equalsIgnoreCase("Seen")) {
        	ret = Boolean.valueOf(entry.getSeen());
        }
        else if (name.equalsIgnoreCase("Country")) {
        	ret = entry.getCountry();
        }
        else if (name.equalsIgnoreCase("Title")) {
        	ret = testmode ? "Movie title " + count : entry.getTitle();
        }
        else if (name.equalsIgnoreCase("Title+Date")) {
        	ret = testmode ? "Movie title " + count : entry.getTitle();
        	
        	if (!entry.getDate().equals(""))
        		ret = ret + " (" + entry.getDate() + ")";
        		
        }
        else if (name.equalsIgnoreCase("Aka")) {
        	ret = testmode ? "Aka " + count : entry.getAka();
        }
        else if (name.equalsIgnoreCase("WebRuntime")) {
        	ret = entry.getWebRuntime();
        }
        else if (name.equalsIgnoreCase("Rating")) {
        	ret = entry.getRating();
        }
        else if (name.equalsIgnoreCase("PersonalRating")) {
        	ret = entry.getPersonalRating();
        }
        else if (name.equalsIgnoreCase("Rating+PersonalRating")) {
        	ret = entry.getRating();
        	        	
        	if (!entry.getPersonalRating().equals(""))
        		ret = ret + " (" + entry.getPersonalRating() + ")";
        	
        }
        else if (name.equalsIgnoreCase("Certification")) {
        	ret = entry.getCertification();
        }
        else {
        	// Additional fields

        	ModelAdditionalInfo a = null;
        	if (entry.getHasAdditionalInfoData()) {
        		a = entry.getAdditionalInfo();
        	}
        	else {
        		if (entry instanceof ModelMovie) {
        			a = MovieManager.getIt().getDatabase().getAdditionalInfo(entry.getKey(), false);
        		}
        		else {
        			a = MovieManager.getIt().getDatabase().getAdditionalInfo(entry.getKey(), true);
        		}
        		entry.setAdditionalInfo(a);
        	}
        	
        	if (a != null) {
        		if (name.equalsIgnoreCase("Subtitles")) {
        			ret = a.getSubtitles();
        		}
        		else if (name.equalsIgnoreCase("Duration")) {
        			int tempInt = a.getDuration();

        			if (tempInt != -1) {
        				int hours = tempInt / 3600;
        				int mints = tempInt / 60 - hours * 60;
        				int secds = tempInt - hours * 3600 - mints * 60;
        				
        				if (hours == 0 && mints == 0 && secds == 0)
        					ret = "";
        				else
        					ret = hours + ":" + mints + "." + secds;
        			}
        			else {
        				ret = "";
        			}
        		}
        		else if (name.equalsIgnoreCase("Filesize")) {
        			int tempInt = a.getFileSize();
        			if (tempInt != -1 && tempInt != 0) {
        				ret = tempInt + " MB";
        			}
        			else {
        				ret = "";
        			}
        		}
        		else if (name.equalsIgnoreCase("CDs")) {
        			int tempInt = a.getCDs();
        			if (tempInt != -1 && tempInt != 0) {
        				ret = "" + tempInt;
        			}
        			else {
        				ret = "";
        			}
        		}
        		else if (name.equalsIgnoreCase("CDCases")) {
        			double tempDouble = a.getCDCases();
        			if (tempDouble > 0) {
        				ret = String.valueOf(tempDouble);
        			}
        			else {
        				ret = "";
        			}
        		}
        		else if (name.equalsIgnoreCase("Resolution")) {
        			ret = a.getResolution();
        		}
        		else if (name.equalsIgnoreCase("VideoCodec")) {
        			ret = a.getVideoCodec();
        		}
        		else if (name.equalsIgnoreCase("VideoRate")) {
        			String vRate = a.getVideoRate();
        			if (!vRate.equals("")) {
        				ret = vRate + " fps";
        			}
        			else {
        				ret = "";
        			}
        		}
        		else if (name.equalsIgnoreCase("VideoBitrate")) {
        			String vRate = a.getVideoBitrate();
        			if (!vRate.equals("")) {
        				ret = vRate + " kbps";
        			}
        			else {
        				ret = "";
        			}
        		}
        		else if (name.equalsIgnoreCase("AudioCodec")) {
        			ret = a.getAudioCodec();
        		}
        		else if (name.equalsIgnoreCase("AudioRate")) {
        			String aRate = a.getAudioRate();
        			if (!aRate.equals("")) {
        				ret = aRate + " Hz";
        			}
        			else {
        				ret = "";
        			}
        		}
        		else if (name.equalsIgnoreCase("AudioBitrate")) {
        			String aRate = a.getAudioBitrate();
        			
        			if (!aRate.equals("")) {
        				ret = aRate + " kbps";
        			}
        			else {
        				ret = "";
        			}
        		}
        		else if (name.equalsIgnoreCase("AudioChannels")) {
        			ret = a.getAudioChannels();
        		}
        		else if (name.equalsIgnoreCase("FileLocation")) {
        			ret = a.getFileLocation();
        		}
        		else if (name.equalsIgnoreCase("FileCount")) {
        			ret = new Integer(a.getFileCount());
        		}
        		else if (name.equalsIgnoreCase("Container")) {
        			ret = a.getContainer();
        		}
        		else if (name.equalsIgnoreCase("MediaType")) {
        			ret = a.getMediaType();
        		}

        		// Extra info
        		else {
        			ArrayList<String> extra = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);
        			if (extra != null) {
        				for (int i = 0; i < extra.size(); i++) {
        					if (name.equalsIgnoreCase(extra.get(i))) {
        						ret = a.getExtraInfoFieldValue(i);
        					}
        				}
        			}
        		}
        	}
        }
        
        if (ret == null)
        	ret = "unknown field " + name;
                
        return ret;
    }

    /**
     * Movie comparater, sorts movies by named field
     */
    private class MovieComparator implements Comparator<ModelEntry> {
        private String sortField;

        public MovieComparator(String sortField) {
            this.sortField = sortField;

        }

        public int compare(ModelEntry m1, ModelEntry m2) {
        
        	int result = 0;
            
            if (sortField.equalsIgnoreCase("Genre")) {
                result = m1.getGenre().compareToIgnoreCase(m2.getGenre());
            }
            else if (sortField.equalsIgnoreCase("Awards")) {
                result = m1.getAwards().compareToIgnoreCase(m2.getAwards());
            }
            else if (sortField.equalsIgnoreCase("Notes")) {
                result = m1.getNotes().compareToIgnoreCase(m2.getNotes());
            }
            else if (sortField.equalsIgnoreCase("Mpaa")) {
                result = m1.getMpaa().compareToIgnoreCase(m2.getMpaa());
            }
            else if (sortField.equalsIgnoreCase("Cast")) {
                result = m1.getCast().compareToIgnoreCase(m2.getCast());
            }
            else if (sortField.equalsIgnoreCase("Date")) {
                result = m1.getDate().compareToIgnoreCase(m2.getDate());
            }
            else if (sortField.equalsIgnoreCase("IMDB")) {
                result = m1.getUrlKey().compareToIgnoreCase(m2.getUrlKey());
            }
            else if (sortField.equalsIgnoreCase("DirectedBy")) {
                result = m1.getDirectedBy().compareToIgnoreCase(m2.getDirectedBy());
            }
            else if (sortField.equalsIgnoreCase("Plot")) {
                result = m1.getPlot().compareToIgnoreCase(m2.getPlot());
            }
            else if (sortField.equalsIgnoreCase("WrittenBy")) {
                result = m1.getWrittenBy().compareToIgnoreCase(m2.getWrittenBy());
            }
            else if (sortField.equalsIgnoreCase("SoundMix")) {
                result = m1.getCast().compareToIgnoreCase(m2.getCast());
            }
            else if (sortField.equalsIgnoreCase("Language")) {
                result = m1.getLanguage().compareToIgnoreCase(m2.getLanguage());
            }
            else if (sortField.equalsIgnoreCase("Genre")) {
                result = m1.getGenre().compareToIgnoreCase(m2.getGenre());
            }
            else if (sortField.equalsIgnoreCase("Colour")) {
                result = m1.getColour().compareToIgnoreCase(m2.getColour());
            }
            else if (sortField.equalsIgnoreCase("Seen")) {
                if (!m1.getSeen() && m1.getSeen()) {
                    result = -1;
                }
                else if (m1.getSeen() && !m1.getSeen()) {
                    result = 1;
                }
            }
            else if (sortField.equalsIgnoreCase("Country")) {
                result = m1.getCountry().compareToIgnoreCase(m2.getCountry());
            }
            else if (sortField.equalsIgnoreCase("Title")) {
                result = m1.getTitle().compareToIgnoreCase(m2.getTitle());
            }
            else if (sortField.equalsIgnoreCase("Aka")) {
                result = m1.getAka().compareToIgnoreCase(m2.getAka());
            }
            else if (sortField.equalsIgnoreCase("WebRuntime")) {
                result = m1.getWebRuntime().compareToIgnoreCase(m2.getWebRuntime());
            }
            else if (sortField.equalsIgnoreCase("Rating")) {
                result = m1.getRating().compareToIgnoreCase(m2.getRating());
            }
            else if (sortField.equalsIgnoreCase("Certification")) {
                result = m1.getCertification().compareToIgnoreCase(m2.getCertification());
            }

            // Additional fields

            if (m1.getHasAdditionalInfoData() && m2.getHasAdditionalInfoData()) {
                ModelAdditionalInfo a1 = m1.getAdditionalInfo();
                ModelAdditionalInfo a2 = m2.getAdditionalInfo();
                if (a1 != null && a2 != null) {
                    if (sortField.equalsIgnoreCase("Subtitles")) {
                        result = a1.getSubtitles().compareToIgnoreCase(a2.getSubtitles());
                    }
                    else if (sortField.equalsIgnoreCase("Duration")) {
                        result = new Integer(a1.getDuration()).compareTo(new Integer(a2.getDuration()));
                    }
                    else if (sortField.equalsIgnoreCase("Filesize")) {
                        result = new Integer(a1.getFileSize()).compareTo(new Integer(a2.getFileSize()));
                    }
                    else if (sortField.equalsIgnoreCase("CDs")) {
                        result = new Integer(a1.getCDs()).compareTo(new Integer(a2.getCDs()));
                    }
                    else if (sortField.equalsIgnoreCase("CDCases")) {
                        result = new Double(a1.getCDCases()).compareTo(new Double(a2.getCDCases()));
                    }
                    else if (sortField.equalsIgnoreCase("Resolution")) {
                        result = a1.getResolution().compareToIgnoreCase(a2.getResolution());
                    }
                    else if (sortField.equalsIgnoreCase("VideoCodec")) {
                        result = a1.getVideoCodec().compareToIgnoreCase(a2.getVideoCodec());
                    }
                    else if (sortField.equalsIgnoreCase("VideoRate")) {
                        result = a1.getVideoRate().compareToIgnoreCase(a2.getVideoRate());
                    }
                    else if (sortField.equalsIgnoreCase("VideoBitrate")) {
                        result = a1.getVideoBitrate().compareToIgnoreCase(a2.getVideoBitrate());
                    }
                    else if (sortField.equalsIgnoreCase("AudioCodec")) {
                        result = a1.getAudioCodec().compareToIgnoreCase(a2.getAudioCodec());
                    }
                    else if (sortField.equalsIgnoreCase("AudioRate")) {
                        result = a1.getAudioRate().compareToIgnoreCase(a2.getAudioRate());
                    }
                    else if (sortField.equalsIgnoreCase("AudioBitrate")) {
                        result = a1.getAudioBitrate().compareToIgnoreCase(a2.getAudioBitrate());
                    }
                    else if (sortField.equalsIgnoreCase("AudioChannels")) {
                        result = a1.getAudioChannels().compareToIgnoreCase(a2.getAudioChannels());
                    }
                    else if (sortField.equalsIgnoreCase("FileLocation")) {
                        result = a1.getFileLocation().compareToIgnoreCase(a2.getFileLocation());
                    }
                    else if (sortField.equalsIgnoreCase("FileCount")) {
                        result = new Integer(a1.getFileCount()).compareTo(new Integer(a2.getFileCount()));
                    }
                    else if (sortField.equalsIgnoreCase("Container")) {
                        result = a1.getContainer().compareToIgnoreCase(a2.getContainer());
                    }
                    else if (sortField.equalsIgnoreCase("MediaType")) {
                        result = a1.getMediaType().compareToIgnoreCase(a2.getMediaType());
                    }

                    // Extra info
                    else {
                        ArrayList<String> extra1 = MovieManager.getIt().getDatabase().getExtraInfoFieldNames(false);
                        
                        if (extra1 != null) {
                            for (int i = 0; i < extra1.size(); i++) {
                                if (sortField.equalsIgnoreCase(extra1.get(i))) {
                                    result = a1.getExtraInfoFieldValue(i).compareToIgnoreCase(a2.getExtraInfoFieldValue(i));
                                }
                            }
                        }
                    }
                }
            }

            if (result == 0) { // equal, sort by title as secondary sortoption
                result = m1.getTitle().compareToIgnoreCase(m2.getTitle());
            }

            return result;
        }
    }

    /**
     * interrupt - call to interrupt generation
     */
    public void interrupt() {
    	interrupt = true;
    }
}
