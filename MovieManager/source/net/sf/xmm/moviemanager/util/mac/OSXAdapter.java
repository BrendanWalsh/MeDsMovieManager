package net.sf.xmm.moviemanager.util.mac;


import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.gui.DialogMovieManager;
import net.sf.xmm.moviemanager.gui.menubar.MovieManagerMenuBar;

import com.apple.eawt.*;

public class OSXAdapter extends ApplicationAdapter {

	static Logger log = Logger.getLogger(OSXAdapter.class);

	// pseudo-singleton model; no point in making multiple instances
	// of the EAWT application or our adapter
	private static OSXAdapter						theAdapter;
	private static com.apple.eawt.Application		theApplication;

	//private static DefaultApplication defaultApplication;
	
	// reference to the app where the existing quit, about, prefs code is
	private DialogMovieManager mainApp;
	
	MovieManagerMenuBar menuBar = null;
		
	private OSXAdapter (DialogMovieManager inApp) {
		mainApp = inApp;
		menuBar = (MovieManagerMenuBar) mainApp.getJMenuBar();
		menuBar.setDatabaseComponentsEnable(false);
	}

	
	// The main entry-point for this functionality.  This is the only method
	// that needs to be called at runtime, and it can easily be done using
	// reflection (see MyApp.java) 
	public static void registerMacOSXApplication(DialogMovieManager inApp) {

		// This does not work on the latest versions of Java for OS X, so the name will be 
		// displayed as the class name when run not run application bundle
		//System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MyApplication");
		
		try {

			if (theApplication == null) {
				theApplication = new com.apple.eawt.Application();
				//defaultApplication = new DefaultApplication();
			}      

			if (theAdapter == null) {
				theAdapter = new OSXAdapter(inApp);
			}

			// Make sure that the about and preferences buttons in OS X works.
			theApplication.addApplicationListener(theAdapter);

			//Image image = FileUtil.getImage("/images/film.png");
			//BufferedImage bufferedImage = Pictures.toBufferedImage(image);

			// Sets the image in the bottom menu bar in OS x (even though it's NOT run in an application bundle)
			//defaultApplication.setApplicationIconImage(bufferedImage);
			
		} catch (RuntimeException e) {
			log.warn("Error occured while registering OS X Application:" + e.getMessage(), e);
		}
	}
	// implemented handler methods.  These are basically hooks into existing 
	// functionality from the main app, as if it came over from another platform.
	public void handleAbout(ApplicationEvent ae) {

		if (mainApp != null) {
			menuBar.getAboutButton().doClick();
			ae.setHandled(true);			
		} else {
			throw new IllegalStateException("handleAbout: instance detached from listener");
		}
	}
	
	public void handlePreferences(ApplicationEvent ae) {

		if (mainApp != null) {
			menuBar.getPreferencesButton().doClick();
			ae.setHandled(true);
		} else {
			throw new IllegalStateException("handlePreferences: instance detached from listener");
		}
	}

	// Another static entry point for EAWT functionality.  Enables the 
	// "Preferences..." menu item in the application menu. 
	public static void enablePrefs(boolean enabled) {

		if (theApplication == null) {
			theApplication = new com.apple.eawt.Application();
		}
		theApplication.setEnabledPreferencesMenu(enabled);
	}

	public void handleQuit(ApplicationEvent ae) {

		if (mainApp != null) {
			/*	
			/	You MUST setHandled(false) if you want to delay or cancel the quit.
			/	This is important for cross-platform development -- have a universal quit
			/	routine that chooses whether or not to quit, so the functionality is identical
			/	on all platforms.  This example simply cancels the AppleEvent-based quit and
			/	defers to that universal method.
			 */
			ae.setHandled(false);
			
			menuBar.getExitButton().doClick();
		} else {
			throw new IllegalStateException("handleQuit: instance detached from listener");
		}
	}
}
