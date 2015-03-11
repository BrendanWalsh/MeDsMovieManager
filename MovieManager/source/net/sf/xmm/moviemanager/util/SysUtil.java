 /**
 * @(#)SysUtil.java
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

package net.sf.xmm.moviemanager.util;

import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.swing.filechooser.FileSystemView;


import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogMovieManager;
import net.sf.xmm.moviemanager.mediainfodll.MediaInfo;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerConfigHandler;

import org.apache.log4j.Logger;


public class SysUtil {

	static Logger log = Logger.getLogger(SysUtil.class);

	/**
	 * Getting the 'root directory' of the app.
	 **/
	public static String getUserDir() {
		
		String path = ""; //$NON-NLS-1$

		try {
			java.net.URL url = FileUtil.class.getProtectionDomain().getCodeSource().getLocation();
			
			File file = new File(java.net.URLDecoder.decode(url.getPath(), "UTF-8")); //$NON-NLS-1$
		
			// If running in a jar file the parent is the root dir 
			if (file.isFile())
				path = file.getParentFile().getAbsolutePath();
			else
				path = file.getAbsolutePath();

		}
		catch (UnsupportedEncodingException e) {
			path = System.getProperty("user.dir"); //$NON-NLS-1$
		}

		if (!path.endsWith(getDirSeparator()))
			path += getDirSeparator();
		
		return path;
	}

	public static String getUserHome() {
		String userHome = (String) AccessController.doPrivileged(
				new PrivilegedAction<Object>() {
					public Object run() {
						return System.getProperty("user.home");
					}
				}
		);
		return userHome;
	}
	
	  
    public static boolean isRestrictedSandbox() {
    	
    	SecurityManager securityManager = System.getSecurityManager();
    	
    	if (securityManager == null) {
    		return false;
    	}
    	
    	try {
    		securityManager.checkPropertiesAccess();
    	} catch (Exception e) {
    		log.debug("Exception:" + e.getMessage());
    		return true;
    	} 
    	
    	return false;
    }
    
    public static boolean isMacAppBundle() {
    	return SysUtil.isMac() && (getJarLocation().indexOf(".app/Contents/Resources") > -1);
    }

    public static String getJarLocation() {
    	
    	String jar = null;
    	
    	jar = MovieManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    	jar = jar.replaceAll("%20", " ");
    	return jar;
    }
    
	
	/**
	 * 
	 * @throws Exception 
	 **/
	public static File getConfigDir() throws Exception {
		
		File dir = null;
		
		try {
			
			/* If running in a mac application bundle, we can't write in the application-directory, so we use the /Library/Application Support */
			if (isMac()) {
				String path = System.getProperty("user.home") + "/Library/Application Support/MovieManager/";
				dir = new File(path);
			}
			else if (isWindowsVista() || isWindows7()) {
				
				String path = System.getenv("APPDATA");
				
				if (path == null)
					path = System.getProperty("user.home") + "/" + "Application Data";
					
				dir = new File(path, "MovieManager");
			}

			// Resetting config to local mode
			if (MovieManager.getConfig().getLocalConfigMode()) {
				log.debug("Using localconfigmode for config");
				dir = null;
			}
						
			if (dir != null) {
								
				if (!dir.exists() && !dir.mkdir()) {
					log.error("Could not create settings folder.");
					throw new Exception("Could not create settings folder:" + dir);
				}
			}
			else
				dir = new File(getUserDir(), "config");
						
		}
		catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}

		return dir;
	}
	
	public static boolean canWriteToInstallDir() {
		try {
			return FileUtil.canWriteToDir(new File(getUserDir()));
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
		return false;
	}
	
	
	public static URL getConfigURL() {

		URL url = null;
		
		try {
			int appMode = getAppMode();

			// Applet
			if (appMode == 1)
				url = FileUtil.getFileURL("config/Config_Applet.ini", DialogMovieManager.applet);
			else if (appMode == 2) { // Java Web Start
				MovieManagerConfigHandler configHandler = MovieManager.getConfig().getConfigHandler();

				if (configHandler != null)
					url = configHandler.getConfigURL();

			} else {

				String conf = "Config.ini";

				if (SysUtil.isMac())
					url = new File(SysUtil.getConfigDir(), conf).toURI().toURL();
				else {
					
					// First check if Vista or Win7
					// Changed the default location on Vista from program directory to System.getenv("APPDATA")
					
					if (isWindowsVista() || isWindows7()) {
						File config = new File(getConfigDir(), conf);
						
						if (config.isFile())
							url = config.toURI().toURL();
					}
					
					// Find config in install directory
					if (url == null) {

						File conf1 = FileUtil.getFile("config/" + conf);
						File conf2 = FileUtil.getFile(conf);
												
						if (conf1.isFile()) {
							url = FileUtil.getFileURL("config/" + conf);
						}
						else if (conf2.isFile()) {
							url = FileUtil.getFileURL(conf);
						}
					}
					
					// if config file isn't found, check old install dir
					if (url == null) {
					
					// changed default install directory from "MeD's Movie Manager" to "MeDs-Movie-Manager"
						File userDir = new File(getUserDir());

						if (!userDir.getName().equals("MeD's Movie Manager")) {

							// Check old install dir
							File oldInstallDir = new File(userDir.getParentFile(), "MeD's Movie Manager");
							File tmpConfig = new File(oldInstallDir, "config/Config.ini");

							if (tmpConfig.isFile()) {
								url = tmpConfig.toURI().toURL();
							}
							else if ((tmpConfig = new File(oldInstallDir, "Config.ini")).isFile()) {
								url = tmpConfig.toURI().toURL();
							}						
						}
					}
				}
			}
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
		return url;
	}
	
	
	  // 0 = Normal application, 1 = Applet, 2 = Java Web Start
    public static int getAppMode() {

    	int mode = -1;

    	SecurityManager securityManager = System.getSecurityManager();

    	if (securityManager == null) {
    		mode = 0;
		}
    	else {
    		String securityManagerString = securityManager.getClass().getName();

    		if ("com.sun.javaws.security.JavaWebStartSecurity".equals(securityManagerString))
    			mode = 2;
    		else if ("sun.applet.AppletSecurity".equals(securityManagerString))
    			mode = 1;
    	}
    	return mode;
    }

	
	
	public static String getDriveDisplayName(File path) {

		FileSystemView fsv = new javax.swing.JFileChooser().getFileSystemView();

		if (fsv != null) {

			File tmp = path;

			while (tmp.getParentFile() != null)
				tmp = tmp.getParentFile();

			String displayName = fsv.getSystemDisplayName(tmp);

			if (!displayName.trim().equals(""))
				return displayName;

			return "";
		}

		return null;
	}

 
	public static Object getClass(String className) {
		
		if (className != null) {

			try {
				Class<?> classForName = Class.forName(className);
				Object classInstance = classForName.newInstance();
				log.debug("Successfully loaded LoginHandler");
				return classInstance;

			} catch (ClassNotFoundException e) {
				log.error("ClassNotFoundException. Failed to load class " + className);
			} catch (IllegalAccessException e) {
				log.error("IllegalAccessException. Failed to load class " + className);
			} catch (InstantiationException e) {
				log.error("InstantiationException. Failed to load class " + className);
			}
		}
		return null;
	}
	
    
    /* Adds all the files ending in .jar to the classpath */
    public static void includeJarFilesInClasspath(String path) {
            	
        URL url = FileUtil.getFileURL(path);
                
        if (url.toExternalForm().startsWith("http://")) //$NON-NLS-1$
            return;
                
        try {
        	File dir = new File(url.toURI());
        	
        	if (!dir.isDirectory())
        		return;
        	
            File [] jarList = dir.listFiles();
            
            if (jarList != null) {
                
                String absolutePath = ""; //$NON-NLS-1$
                for (int i = 0; i < jarList.length; i++) {
                    
                    absolutePath = jarList[i].getAbsolutePath();
                     
                    if (absolutePath.endsWith(".jar")) { //$NON-NLS-1$
                    	net.sf.xmm.moviemanager.util.ClassPathHacker.addFile(absolutePath);
                        log.debug(absolutePath+ " added to classpath"); //$NON-NLS-1$
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("Exception:" + e.getMessage()); //$NON-NLS-1$
        }
    }
    
    
    public static void cleaStreams(Process p) {
    	
    	if (p == null)
    		return;
    	
    	/**
		 * Clears the streams to avoid having the subprocess hang
		 * @author Bro
		 */
		class StreamHandler extends Thread {

			InputStream inpStr;
			String strType;

			public StreamHandler(InputStream inpStr, String strType) {
				this.inpStr = inpStr;
				this.strType = strType;
			}

			public void run() {
				try {
					InputStreamReader inpStrd = new InputStreamReader(inpStr);
					BufferedReader buffRd = new BufferedReader(inpStrd);
					String line = null;
					String str = "";
					
					while((line = buffRd.readLine()) != null) {
						str += line;
					}
					
					log.debug(line);  
					buffRd.close();
				} catch (IOException e) {
					log.error("1Exception:" + e.getMessage(), e);  
				}
			}
		}
    	
		StreamHandler input = new StreamHandler(p.getInputStream(), "INPUT");
		StreamHandler err = new StreamHandler(p.getErrorStream(), "ERROR");

		input.start();
		err.start();
    }
	
	public static String getLineSeparator() {
		return System.getProperty("line.separator"); //$NON-NLS-1$
	}


	public static String getDirSeparator() {
		return File.separator;
	}

	public static boolean isCtrlPressed(InputEvent event) {
		return ((event.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK);
	}

	public static boolean isShiftPressed(InputEvent event) {
		return ((event.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK);
	}	

	public static boolean isMac() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("mac"); //$NON-NLS-1$
	}

	public static boolean isOSX() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("Mac OS X"); //$NON-NLS-1$
	}

	public static boolean isLinux() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("linux"); //$NON-NLS-1$
	}

	public static boolean isSolaris() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && (os.toLowerCase().startsWith("sunos") || os.toLowerCase().startsWith("solaris")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("windows"); //$NON-NLS-1$
	}

	public static boolean isWindows98() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.toLowerCase().startsWith("Windows 98"); //$NON-NLS-1$
	}
	
	public static boolean isWindowsXP() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		String osVersion = System.getProperty("os.version"); //$NON-NLS-1$
			
		return os != null && osVersion != null
			&& os.toLowerCase().indexOf("windows") != -1 &&
			osVersion.equals("5.1"); //$NON-NLS-1$
	}

	/**
	 * Bug in Java (not yet fixed in 1.6.0_13) causing System.getProperty("os.name")
	 * to return "Windows XP" on Windows Vista.
	 * System.getProperty("os.version") returns "6.0" on Windows Vista and "5.1" on XP
	 * @return
	 */
	public static boolean isWindowsVista() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		String osVersion = System.getProperty("os.version"); //$NON-NLS-1$
			
		return os != null && osVersion != null
			&& os.toLowerCase().indexOf("windows") != -1 &&
			osVersion.equals("6.0"); //$NON-NLS-1$
	}

	/**
	 * Bug in Java (not yet fixed in 1.6.0_13) causing System.getProperty("os.name")
	 * to return "Windows Vista" on Windows 7.
	 * System.getProperty("os.version") returns "6.1" on Windows 7 and "6.0" on Vista
	 * @return
	 */
	public static boolean isWindows7() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		String osVersion = System.getProperty("os.version"); //$NON-NLS-1$
			
		return os != null && osVersion != null
			&& os.toLowerCase().indexOf("windows") != -1 &&
			osVersion.equals("6.1"); //$NON-NLS-1$
	}
	
	
	public static boolean isAMD64() {
		String arch = System.getProperty("os.arch"); //$NON-NLS-1$
		return arch != null  && arch.equals("amd64");
	}
	
	
	public static void openFileLocationOnWindows(File file) {
		try {
			//Desktop.getDesktop().browse(file.toURI());
			Runtime runtime = Runtime.getRuntime();
			runtime.exec("explorer.exe /select,\"" + file.getAbsolutePath() );
		} catch(IOException ioe) {
			ioe.printStackTrace();
			//JOptionPane.showMessageDialog(null, "Could Not Open File Location: " + file.getAbsolutePath());
		}
	}

	  
    public static boolean isCurrentJRES14() {
    	
    	double javaVersion = Double.parseDouble(System.getProperty("java.version").substring(0, 3));
    		
    	if (javaVersion < 1.5) {
    		//log.error("Version:" + javaVersion + " is not supported. Must be 1.5 or higher.");
    		return true;
    	}
    	return false;
    }
    

    public static String getDefaultPlatformBrowser() {
        String browser = "";
        
        if (SysUtil.isWindows())
            browser = "Default";
        else if (SysUtil.isMac())
            browser = "Safari";
        else
            browser = "Firefox";
        
        return browser;
    }
    
    
    public static String getSystemInfo(String separator) {
    
    	String sep = separator == null ? "\\n" : separator;
    	
    	int freeMemory = (int) Runtime.getRuntime().freeMemory()/1024/1024;
    	int totalMemory = (int) Runtime.getRuntime().totalMemory()/1024/1024;
    	int maxMemory = (int) Runtime.getRuntime().maxMemory()/1024/1024;
    	
    	StringBuffer info = new StringBuffer();
    	info.append("Operating System: ").append(System.getProperty("os.name"));
    	info.append(" version: " + System.getProperty("os.version"));
    	info.append(sep);
    	info.append("Architecture: ").append(System.getProperty("os.arch"));
    	
    	info.append(sep);
    	info.append("Java version: ").append(System.getProperty("java.runtime.version"));
    	info.append(sep);
    	info.append("Vendor:").append(System.getProperty("java.vm.specification.vendor"));
    	info.append(sep);
    	
    	info.append("Free VM memory: ").append(freeMemory).append(" MB, ");
    	info.append(sep);
    	info.append("Total VM memory: ").append(totalMemory + " MB");
    	info.append(sep);
    	info.append("Max VM memory: ").append(maxMemory + " MB");
    	
    	return info.toString();
    }
    
    static boolean mediaInfoLibLoaded = false;
    
    public static String getMediaInfoLibVersion() {
    
    	try {
			loadMediaInfoLib();
			String version = MediaInfo.Option_Static("Info_Version");
			return version.split("-")[1].trim();
    	} catch (Exception e) {
			log.warn("Exception:" + e.getMessage());
		}
    	catch (UnsatisfiedLinkError e) {
    		log.debug("UnsatisfiedLinkError:" + e.getMessage());
    		log.debug("Failed to load MediaInfo library");
    	}
    	return null;
    }
    

	public static void loadMediaInfoLib() throws Exception {
		
		if (mediaInfoLibLoaded)
			return;
		
		// Load library on Windows only
		if (SysUtil.isWindows()) {

			String mediaInfoDll = "lib\\MediaInfo\\x86\\MediaInfo.dll";
			
			if (SysUtil.isAMD64()) {
				log.debug("Using MediaInfo library for amd64");
				mediaInfoDll = "lib\\MediaInfo\\amd64\\MediaInfo.dll";
			}
			else {
				log.debug("Using MediaInfo library for x86");
			}
			
			File mediaInfo = new File((FileUtil.getFile(mediaInfoDll)).getPath());

			if (mediaInfo.exists()) {
				LibPathHacker.addDir(FileUtil.getFile("lib").getAbsolutePath());
				System.load(mediaInfo.getAbsolutePath());
			}
			else {
				String error = "";

				if (!mediaInfo.exists()) {
					error += "MediaInfo.dll";
				}

				error = "Following libraries are missing:" + error;
				throw new Exception(error);
			}
		}
		
		/*
		if (SysUtil.isMac()) {

			try {
				System.out.println("load media info for OSX");
				
				String mediaInfoDll = "/Users/bro/Desktop/mediainfo/MediaInfoLib/libmediainfo.0.0.0.dylib";
							
				File mediaInfo = new File((FileUtil.getFile(mediaInfoDll)).getPath());

				
				
				if (mediaInfo.exists()) {
					System.out.println("Loading:" + mediaInfo.getAbsolutePath());
					//LibPathHacker.addDir("/Users/bro/Desktop/mediainfo/MediaInfoLib/");
					//System.load(mediaInfo.getAbsolutePath());
					
					System.out.println("libpath:" + System.getProperty("java.library.path"));
								
					System.loadLibrary("libmediainfo");
				}
				else {
					System.out.println("File doesnt exist");
				}
			} catch (UnsatisfiedLinkError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
	}
	
    
    /**
     * Checks if the current running JRE version is at least 1.6
     * @return   true if JRE is at least 1.6
     */
    public static boolean isAtLeastJRE6() {

    	double javaVersion = Double.parseDouble(System.getProperty("java.version").substring(0, 3));

    	if (javaVersion >= 1.6) {
    		return true;
    	}
    	return false;
    }

	
	public static String colourToString(java.awt.Color c) {
	    return Integer.toHexString(0xFF000000 | c.getRGB()).substring(2);
	}
 
}
