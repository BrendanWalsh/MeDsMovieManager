package net.sf.xmm.moviemanager.updater;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandExit;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogQuestion;
import net.sf.xmm.moviemanager.imdblib.IMDbLib;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.events.UpdatesAvailableHandler;
import net.sf.xmm.moviemanager.util.tools.BrowserOpener;

import com.panayotis.jupidator.ApplicationInfo;
import com.panayotis.jupidator.UpdatedApplication;
import com.panayotis.jupidator.Updater;
import com.panayotis.jupidator.UpdaterException;
import com.panayotis.jupidator.gui.JupidatorGUI;

public class AppUpdater implements UpdatedApplication {

	static Logger log = Logger.getLogger(AppUpdater.class);
	
	static private Updater updater = null;
	
	public static UpdatesAvailableHandler updatesAvailableHandler = new UpdatesAvailableHandler();
	
	private boolean forceDisplay = false;
	private boolean showAutomatically = true;
	
	
    public void runUpdate() {
    	log.debug("Loading AppUpdater - forceDisplay:" + forceDisplay);
    	
        try {
        
        	int release = MovieManager.getConfig().sysSettings.getRelease();
        	String version = MovieManager.getConfig().sysSettings.getVersion() + " - " + IMDbLib.getVersionString();
        	
        	log.debug("Current Movie manager version:" + version + " (Release "+ release +")");
        	
        	// IMDb lib is newer
        	if (release < IMDbLib.getRelease()) {
        		release = IMDbLib.getRelease();
        		log.debug("IMDb Lib is newest; Version " + IMDbLib.getVersion() + " (Release "+ release +")");
        	}
        	        	
        	ApplicationInfo ap = new ApplicationInfo(
                    SysUtil.getUserDir(),
                    SysUtil.getConfigDir().getAbsolutePath(),
                    "" + release,
                    version);
        	
        	ap.setForceDisplay(forceDisplay);
        	ap.setShowAutomatically(showAutomatically);
        	        	
        	log.debug("Loading updater config from " + ap.getUpdaterConfigFile());
        	
        	updater = new Updater("http://xmm.sourceforge.net/updates/update.xml", ap, this);
        	        	
        } catch (UpdaterException ex) {
        	log.error("UpdaterException:" + ex.getMessage(), ex);
        } catch (Exception e) {
        	log.error("UpdaterException:" + e.getMessage(), e);
		}
    }

    public static boolean updatesAvailable() {
    	return updater.updatesAvailable();
    }
  
    public void setForceDisplay(boolean forceDisplay) {
		this.forceDisplay = forceDisplay;
	}
	
	public void setShowAutomatically(boolean showAutomatically) {
		this.showAutomatically = showAutomatically;
	}
    
	public static void showUpdateWindow() {
		try {			
			GUIUtil.invokeAndWait(new Runnable() {
				public void run() {
					JupidatorGUI gui = new DialogUpdater();
					gui.setProperty("about", "false");
					gui.setProperty("checkForUpdates", "true");
					gui.setProperty("checkForUpdatesSelected", Boolean.toString(MovieManager.getConfig().getCheckForProgramUpdates()));
					gui.setProperty("disposeonescape", "true");
					gui.setProperty("diposeonclose", "true");
					gui.setProperty("resizable", "true");
					gui.setProperty("modal", "true");
					gui.setProperty("closebutton", "true");
					        			
					updater.setGUI(gui);
										
					try {
						updater.actionDisplay();
					} catch (UpdaterException e) {
						log.warn("Exception:" + e.getMessage(), e);
					}
				}
			});
		} catch (InterruptedException e) {
			log.error("Exception:" + e.getMessage(), e);
		} catch (InvocationTargetException e) {
			log.error("Exception:" + e.getMessage(), e);
		}
	}
	
    public boolean requestRestart() {
    	
    	DialogQuestion q = new DialogQuestion("Restart", "Restart necessary for changes to take effect. Restart now?");
    	q.setVisible(true);
    	
    	if (q.getAnswer()) {
    	
    		try {
				MovieManagerCommandExit.shutDown();
			} catch (Exception e) {
				log.error("Exception:" + e.getMessage(), e);
				
				DialogAlert alert = new DialogAlert(MovieManager.getDialog(), "Error shutting down", "Error occured when trying to close");
				GUIUtil.show(alert, true);
			}
			return true;
    	}
    	else
    		return false;
    }

    public boolean requestInstall() {
    	
    	// Unable to write to install dir
		if (!SysUtil.canWriteToInstallDir()) {
			
			String alertTitle = "Update alert";
			String alertMessage = "<html>Application is not allowed to write to the install directory.<br> Updates will not be installed.</html>";
			
			if (SysUtil.isWindowsVista() || SysUtil.isWindows7()) {
				alertMessage = "<html>Application must be run as administrator to be able to update.</html>";
			}
			
			DialogAlert alert = new DialogAlert(MovieManager.getDialog(), alertTitle, alertMessage, true);
			GUIUtil.show(alert, true);
			return false;
		}
    	
		return true;
    }
    
    
    public void receiveMessage(String message) {
        log.debug("Update message::" + message);
    }
    
    public void actionCheckForUpdates(boolean checkForUpdates) {
    	MovieManager.getConfig().setCheckForProgramUpdates(checkForUpdates);
    }
    
    
    public static void handleVersionUpdate() {
    	handleVersionUpdate(false);
    }
    
    public static void handleVersionUpdate(final boolean forceDisplay) {
    	    	
    	final MovieManagerConfig config = MovieManager.getConfig();
    	    	    	
    	if (!config.getCheckForProgramUpdates() && !forceDisplay)
    		return;

    	log.debug("handleVersionUpdate");
    	    	
    	Thread t = new Thread() {

    		public void run() { 
	
    			Thread.currentThread().setPriority(NORM_PRIORITY);
    			
    			try {
					//Thread.sleep(3000);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				AppUpdater appUpdater = new AppUpdater();
				appUpdater.setForceDisplay(forceDisplay);
				
				appUpdater.runUpdate();
				
				log.debug("Updates available:" + updatesAvailable());
				
				if (forceDisplay) {
					// Will have no effect if forcedisplay isn't set to true
					showUpdateWindow();
				}
				
				// Notify about updates
				if (updater.updatesAvailable() && !forceDisplay) {
					updatesAvailableHandler.updatesAvailable(this);
				}
				
				log.debug("Version update check finished.");
    		}
    	};
    	t.start();
    }

	public void linkClicked(String link) {
		BrowserOpener opener = new BrowserOpener(link);
		opener.executeOpenBrowser(MovieManager.getConfig().getSystemWebBrowser(), MovieManager.getConfig().getBrowserPath());
	}
}
