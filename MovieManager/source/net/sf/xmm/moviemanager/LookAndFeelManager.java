/**
 * @(#)LookAndFeelManager.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogMovieManager;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedJTree;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedJTreeUI;
import net.sf.xmm.moviemanager.swing.extentions.ExtendedTreeCellRenderer;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.events.NewLookAndFeelLoadedHandler;

import org.apache.log4j.Logger;

import com.l2fprod.gui.plaf.skin.Skin;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;
import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;

public class LookAndFeelManager {
    
    static Logger log = Logger.getRootLogger();
    
    public static NewLookAndFeelLoadedHandler newLookAndFeelLoadedHandler = new NewLookAndFeelLoadedHandler();
    
	private String lookAndFeel = "Metal";

	private String skinlfThemePack = "";
	private String skinlfThemePackDir;

	private String nimRODTheme = "";
	private String nimRODThemeDir = "";

	private String substanceSkin = "Sahara";
	
	public enum LookAndFeelType {CustomLaF, SkinlfLaF, Substance, NimROD}
	
	private LookAndFeelType lookAndFeelType = LookAndFeelType.CustomLaF;

	private boolean defaultLookAndFeelDecorated = false;
    
	static {
		// Enable anti-aliasing for Java 1.5
		System.setProperty("swing.aatext","true");
		System.setProperty("awt.useSystemAAFontSettings", "on");
	}
	
    public boolean getDefaultLookAndFeelDecorated() {
		return defaultLookAndFeelDecorated;
	}

	public void setDefaultLookAndFeelDecorated(boolean defaultLookAndFeelDecorated) {
		this.defaultLookAndFeelDecorated = defaultLookAndFeelDecorated;
	}

	public String getCustomLookAndFeel() {
		return lookAndFeel;
	}

	public void setCustomLookAndFeel(String lookAndFeel) {
		this.lookAndFeel = lookAndFeel;
	}

	public String getSkinlfThemePack() {
		return skinlfThemePack;
	}

	public void setSkinlfThemePack(String skinlfThemePack) {
		this.skinlfThemePack = skinlfThemePack;
	}

	public String getSkinlfThemePackDir() {
		return skinlfThemePackDir;
	}

	public void setSkinlfThemePackDir(String skinlfThemePackDir) {
		this.skinlfThemePackDir = skinlfThemePackDir;
	}
	
	
	public String getNimRODTheme() {
		return nimRODTheme;
	}

	public void setNimRODTheme(String nimRODTheme) {
		this.nimRODTheme = nimRODTheme;
	}

	public String getNimRODThemeDir() {
		return nimRODThemeDir;
	}

	public void setNimRODThemeDir(String nimRODThemeDir) {
		this.nimRODThemeDir = nimRODThemeDir;
	}
	
	
	public String getSubstanceSkin() {
		return substanceSkin;
	}

	public void setSubstanceSkin(String substanceSkin) {		
		if (substanceSkin != null && !substanceSkin.equals(""))
			this.substanceSkin = substanceSkin;
	}
	
	
	
	public LookAndFeelType getLookAndFeelType() {
		return lookAndFeelType;
	}

	public void setLookAndFeelType(LookAndFeelType lafType) {
		this.lookAndFeelType = lafType;
	}
	
    
    public boolean laFSet = false;
    
    public void setLookAndFeel() {
                        
        if (getCustomLookAndFeel().equals("")) {
        	setDefaultCustomLaF();
        }
        
        try {
        	
        	if (getLookAndFeelType() == LookAndFeelType.CustomLaF && 
            		!getCustomLookAndFeel().equals(UIManager.getLookAndFeel().getName())) {
            	
            	GUIUtil.invokeAndWait(new Runnable() {
            		public void run() {
            			laFSet = setCustomLaf(getCustomLookAndFeel());
            		}
            	});
        	}        
            else if (getLookAndFeelType() == LookAndFeelType.SkinlfLaF &&
            		getSkinlfThemepackList() != null) {
                
            	File theme = new File(getSkinlfThemePackDir(), getSkinlfThemePack());
            	
            	// All the theme file names were changed (prettyfied). Handle if old theme name is loaded from config 
            	if (!theme.isFile()) {
            		 String newName = theme.getName();
                     newName = newName.replace("themepack", "");
                     
                     char[] newNameArray = newName.toCharArray();
                     newNameArray[0] = Character.toUpperCase(newNameArray[0]);
                     theme = new File(theme.getParentFile(), new String(newNameArray));
            	}
            	
            	if (theme.isFile()) {
            		laFSet = setSkinlfLookAndFeel(theme.getName());
            	}
            	else {
            		log.debug("SkinLf theme file not found:" + theme);
            		log.debug("Default Look & Feel is used.");
            	}
            }
            else if (getLookAndFeelType() == LookAndFeelType.Substance) {
            	GUIUtil.invokeAndWait(new Runnable() {
            		public void run() {
            			laFSet = setSubstanceLookAndFeel(getSubstanceSkin());
            		}
            	});
            }
            else if (getLookAndFeelType() == LookAndFeelType.NimROD && getNimrodThemeList() != null) {
            	GUIUtil.invokeAndWait(new Runnable() {
            		public void run() {
            			laFSet = setNimRODLookAndFeel(getNimRODTheme());
            		}
            	});
            }
        	
            if (laFSet) {
            	ExtendedTreeCellRenderer.setDefaultColors();
            	
            	if (MovieManager.getDialog() != null && 
            			MovieManager.getDialog().getMoviesList() instanceof ExtendedJTree)
            		((ExtendedJTree) MovieManager.getDialog().getMoviesList()).resetUI();
            }
            else {
            	setLookAndFeelType(LookAndFeelType.CustomLaF);
        		setCustomLookAndFeel("Metal");
            }

        } catch (final Exception e) {
            log.error("Exception: " + e.getMessage());
            e.printStackTrace();
            GUIUtil.invokeLater(new Runnable() {
            	public void run() {
            		DialogAlert alert = new DialogAlert(MovieManager.getDialog(), "Look and Feel error", "Look and feel may not be properly installed.", e.getMessage());
            		GUIUtil.showAndWait(alert, true);
				}
			});
        }
    }
    
    void setDefaultCustomLaF() {

    	final UIManager.LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();

    	LookAndFeel currentLAF = UIManager.getLookAndFeel();
    	if (currentLAF != null) {
    		setCustomLookAndFeel(currentLAF.getName());
    	} else {
    		for (int i = 0;i < installedLookAndFeels.length;i++) {
    			if(installedLookAndFeels[i].getClassName().equals(UIManager.getSystemLookAndFeelClassName())) {
    				setCustomLookAndFeel(installedLookAndFeels[i].getName());
    			}
    		}
    	}
    }
    
    /**
     * Takes the name of a custom Look & Feel, finds it's classname, and sets the L&F.
     * @param lafName
     * @return
     */
    public boolean setCustomLaf(String lafName) {
    	
    	log.debug("Setting Custom L&F:" + lafName);
    	
    	ArrayList<LookAndFeelInfo> lafs = getCustomLaFList();
    	    	
    	for (int i = 0; i < lafs.size(); i++) {
    		if (lafs.get(i).getName().equals(lafName)) {
    			return setLookAndFeel(lafs.get(i).getClassName());
    		}
    	}
    	return false;
    }
    
    /**
     * Takes the name of a SkinLf themepack and loads the theme pack from the default theme pack directory.
     * @param skinLfSkinFile
     * @return
     * @throws MalformedURLException
     * @throws Exception
     */
    public boolean setSkinlfLookAndFeel(String skinLfSkinFile) throws MalformedURLException, Exception {

    	log.debug("Setting Skinlf theme:" + skinLfSkinFile);
    	
    	String skinlfThemePackPath = getSkinlfThemePackDir() + skinLfSkinFile;

    	Skin skin = null;

    	if (MovieManager.isApplet())
    		skin = SkinLookAndFeel.loadThemePack(FileUtil.getAppletFile(skinlfThemePackPath, DialogMovieManager.applet).toURI().toURL());
    	else {
    		skin = SkinLookAndFeel.loadThemePack(skinlfThemePackPath);
    	}
    	SkinLookAndFeel.setSkin(skin);
    	
    	return setLookAndFeel(new SkinLookAndFeel());
    }
    
    
    /**
     * Takes the name of a SkinLf themepack and loads the theme pack from the default theme pack directory.
     * @param skinLfSkinFile
     * @return
     * @throws MalformedURLException
     * @throws Exception
     */
    public boolean setNimRODLookAndFeel(String nimRODThemeFile) {

    	log.debug("Setting NimROD theme:" + nimRODThemeFile);
    	
    	if (!nimRODThemeFile.endsWith(".theme"))
    		nimRODThemeFile += ".theme";
    		
    	File romRODThemeFile = new File(getNimRODThemeDir(), nimRODThemeFile);

    	NimRODLookAndFeel nimRODLF = new NimRODLookAndFeel();

    	if (MovieManager.isApplet()) {
    		//skin = SkinLookAndFeel.loadThemePack(FileUtil.getAppletFile(skinlfThemePackPath, DialogMovieManager.applet).toURI().toURL());
    	}
    	else {
    		NimRODTheme nt = new NimRODTheme(romRODThemeFile.getAbsolutePath());
    		NimRODLookAndFeel.setCurrentTheme(nt);
    	}
    	    	
    	return setLookAndFeel(nimRODLF);
    }
    
    
    public boolean setSubstanceLookAndFeel(String skinName) {
    	
    	log.debug("Setting Substance Skin:" + skinName);
    	
    	ArrayList<LookAndFeelInfo> list = getSubstanceSkinList();
    	
    	for (LookAndFeelInfo laf : list) {
    		
    		if (skinName.equals(laf.getName())) {
    			log.debug("Setting substance skin " + laf.getClassName());
    			return setLookAndFeel(laf.getClassName());
    		}
    	}
    	return false;
    }
    
    
    // "org.jvnet.substance.skin"
    final String substanceClassPrefix = "org.pushingpixels.substance.api.skin";
    
    ArrayList<LookAndFeelInfo> customLaFs = null;
    ArrayList<LookAndFeelInfo> substanceSkins = null;
    
    void setupLaFList() {
    	
    	UIManager.LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
    	customLaFs = new ArrayList<LookAndFeelInfo>();
    	substanceSkins = new ArrayList<LookAndFeelInfo>();
    	
    	for (int i = 0; i < installedLookAndFeels.length; i++) {
    		
    		if (installedLookAndFeels[i].getClassName().startsWith(substanceClassPrefix)) {
    			substanceSkins.add(installedLookAndFeels[i]);
    		}
    		// Skip the ugly motif L&F
    		else if (!installedLookAndFeels[i].getName().equals("CDE/Motif"))
    			customLaFs.add(installedLookAndFeels[i]);
    	}
    }
    
    
    public ArrayList<LookAndFeelInfo> getCustomLaFList() {

    	if (customLaFs == null)
    		setupLaFList();
    		
    	return customLaFs;
    }
    
    public ArrayList<LookAndFeelInfo> getSubstanceSkinList() {

    	if (substanceSkins == null)
    		setupLaFList();
    		
    	return substanceSkins;
    }
    
    
    public String [] getLaFListArray() {
    	
    	ArrayList<LookAndFeelInfo> list = getCustomLaFList();
    	String [] names = new String[list.size()];
    	
    	for (int i = 0; i < names.length; i++)
    		names[i] = list.get(i).getName();
    		
    	return names;
    }
    
    public String [] getSubstanceSkinListArray() {
    	    	
    	ArrayList<LookAndFeelInfo> list = getSubstanceSkinList();
    	String [] names = new String[list.size()];
    	
    	for (int i = 0; i < names.length; i++)
    		names[i] = list.get(i).getName();
    		
    	return names;
    }
    

    public String [] getSkinlfThemepackList() {
        
        try {
            File dir;
            String dirSep = SysUtil.getDirSeparator();
            
            if (!MovieManager.isApplet()) {
                 
                setSkinlfThemePackDir(SysUtil.getUserDir() + dirSep + "lib/LookAndFeels" + dirSep + "Skinlf_Theme_Packs" + dirSep);
                dir = new File(getSkinlfThemePackDir());
                
                if (!dir.exists()) {
                	return null;
                }
               // File [] listFiles = dir.listFiles();
                String [] list = dir.list();
                ArrayList<String> themePackList = new ArrayList<String>();
                
                if (list != null) {
                    for (int i = 0; i < list.length; i++) {
                        if (list[i].endsWith(".zip")) {
                            themePackList.add(list[i]);
                        }
                    }
                }
                if (themePackList.size() == 0)
                    return null;
                
                String [] tempList = new String[themePackList.toArray().length];
                tempList = (String[]) themePackList.toArray(tempList);
                return tempList;
            }
            
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }
    
        

    public String [] getNimrodThemeList() {
        
    	String [] tempList = new String[0];
    	
        try {
            File dir;
            String dirSep = SysUtil.getDirSeparator();
            
            if (!MovieManager.isApplet()) {
                 
            	setNimRODThemeDir(SysUtil.getUserDir() + dirSep + "lib/LookAndFeels" + dirSep + "NimROD_Themes" + dirSep);
                
                dir = new File(getNimRODThemeDir());
                
                if (!dir.exists()) {
                	return tempList;
                }
                
                String [] list = dir.list();
                ArrayList<String> themePackList = new ArrayList<String>();
                
                if (list != null) {
                    for (int i = 0; i < list.length; i++) {
                        if (list[i].endsWith(".theme")) {
                            themePackList.add(list[i].substring(0, list[i].indexOf(".theme")));
                        }
                    }
                }
                if (themePackList.size() == 0)
                    return tempList;
                
                tempList = new String[themePackList.toArray().length];
                tempList = (String[]) themePackList.toArray(tempList);
                return tempList;
            }
            
        } catch (Exception e) {
            log.error("", e);
        }
        return tempList;
    }
    
    
   boolean error = false;
    
    private boolean setLookAndFeel(final String className) {
    	
    	error = false;
    
    	try {
    		    		
    		GUIUtil.invokeAndWait(new Runnable() {
    			public void run() {
    				try {
    					UIManager.setLookAndFeel(className);
    					DialogMovieManager.setDefaultLookAndFeelDecorated(getDefaultLookAndFeelDecorated());
    				} catch (Exception e) {
    					error = true;
    					e.printStackTrace();
    				}
    			}
    		});
    		newLookAndFeelLoadedHandler.newLookAndFeelLoaded(this);
    	} catch (Exception e) {
    		error = true;
			e.printStackTrace();
		}
    	   	
    	return !error;
    }
    
    
    private boolean setLookAndFeel(final LookAndFeel lookAndFeel) {
    	    	
    	error = false;

    	try {
    		GUIUtil.invokeAndWait(new Runnable() {
    			public void run() {
    				try {
    					UIManager.setLookAndFeel(lookAndFeel);
    				} catch (UnsupportedLookAndFeelException e) {
    					log.warn("Exception:" + e.getMessage(), e);
    					error = true;
    				}
    			}
    		});
    		newLookAndFeelLoadedHandler.newLookAndFeelLoaded(this);
    	} catch (Exception e) {
    		error = true;
			e.printStackTrace();
		}
    	return !error;
    }
    
    
    
    protected void instalLAFs() {
        
        try {
        	           
        	log.debug("Installing Look & Feels.");
        	
        	File lookAndFeel;
        	
        	if (!SysUtil.isMacAppBundle()) {
        		lookAndFeel =  FileUtil.getFile("lib/LookAndFeels/lookAndFeels.ini");
        	} else {
        		File f = new File(SysUtil.getJarLocation()).getParentFile();
        		lookAndFeel = new File(f, "lib/LookAndFeels/lookAndFeels.ini");
        	}
        	
        	if (!lookAndFeel.isFile()) {
        		log.warn("lookAndFeels.ini was not found:" + lookAndFeel.getAbsolutePath());
        		return;
        	}
        	
        	BufferedReader reader = new BufferedReader(new FileReader(lookAndFeel));
        	Pattern p = Pattern.compile("\"(.+?)\"\\s+?\"(.+?)\"\\s+?\"(.+?)\"\\s*(?:\"(.+?)\")?.*");
        	
        	double javaVersion = Double.parseDouble(System.getProperty("java.version").substring(0, 3));
        	
        	String line;
        	boolean start = false;
        	
            while (true) {
             	
            	line = reader.readLine();
            	
            	if (line == null)
            		break;
            	
            	if (!start) {
            		if (line.indexOf("#") != -1)
            			start = true;
            		continue;
            	}
            	
            	line = line.trim();
            	
            	if (line.equals(""))
            		continue;
            	
            	Matcher m = p.matcher(line);
            	
            	if (!m.matches() || m.groupCount() < 3)
            		continue;
            	            	
            	try {
            		
            		double javaVersionSupported =  Double.parseDouble(m.group(3));
            		
            		// Check java version
            		if (javaVersionSupported > javaVersion)
            			continue;
            		
            		// Check platform
            		if (m.groupCount() == 4) {
            			String os = m.group(4);
            			
            			if (os != null) {

            				// Currently there is only the office and jgoodies L&F which is unique for Windows and Quaque for Mac.

            				if (os.indexOf("Windows") != -1) {
            					if (!SysUtil.isWindows())	            				
            						continue;
            				}
            				else if (os.indexOf("Mac") != -1) {
            					if (!SysUtil.isMac())	            				
            						continue;
            				}
            				else if (os.indexOf("Linux") != -1) {
            					if (!SysUtil.isLinux())	            				
            						continue;
            				}
            			}
            		}
           		
            		try {
						Class.forName(m.group(2));
						UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo(m.group(1), m.group(2)));
					} catch (ClassNotFoundException e) {
						log.debug("Failed to locate L&F class " + m.group(2) + " on classpath.");
					} catch(Exception e) {
						log.warn("Exception:" + e .getMessage());
					}										
                }
                catch (SecurityException s) {
                	log.warn("SecurityException: "+ s);
                } catch(java.lang.NoClassDefFoundError e) {
                	log.warn("NoClassDefFoundError:" + e.getMessage());
                }	
            }
        }
        catch (Exception e) {
            log.warn("Failed to install Look & Feels.", e);
        }
    }
    
       
    public void setupOSXLaF() {
        
        if (SysUtil.isMac()) {
         	
        	log.debug("Setting up OS X L&F");
        	
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.showGrowBox", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MeD's Movie Manager");
            
            try {
                Class<?> quaquaClass = ClassLoader.getSystemClassLoader().loadClass("ch.randelshofer.quaqua.QuaquaLookAndFeel");
                LookAndFeel quaquqLAF = (LookAndFeel) quaquaClass.newInstance();
                UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo(quaquqLAF.getName(), "ch.randelshofer.quaqua.QuaquaLookAndFeel"));
                // Override system look and feel
                setLookAndFeel(quaquqLAF);
                System.load(FileUtil.getFile("lib/mac/libquaqua.jnilib").getPath());
                
                System.setProperty("Quaqua.JNI.isPreloaded","true");
                log.debug("Quaqua installed");
            } catch (Exception e) {
                log.error("Quaqua Look and Feel not installed: ", e);
            }
            catch(UnsatisfiedLinkError e) {
                log.error("Quaqua installed, but without the native parts: ", e);
            }
        }
    }
    
    public static void macOSXRegistration(JFrame dialog) {
    	
        if (SysUtil.isMac()) {
        
        	log.debug("Registering OS X application menu");
        	
        	try {
                Class<?> osxAdapter = ClassLoader.getSystemClassLoader().loadClass("net.sf.xmm.moviemanager.util.mac.OSXAdapter");
                Class<?>[] defArgs = {DialogMovieManager.class};
                
                Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);
                 
                if (registerMethod != null) {
                    Object[] args = { dialog };
                    registerMethod.invoke(osxAdapter, args);
                }
             
                defArgs[0] = boolean.class;
                Method prefsEnableMethod =  osxAdapter.getDeclaredMethod("enablePrefs", defArgs);
               
                if (prefsEnableMethod != null) {
                    Object args[] = {Boolean.TRUE};
                    prefsEnableMethod.invoke(osxAdapter, args);
                }
                
            } catch (NoClassDefFoundError e) {
                // This will be thrown first if the OSXAdapter is loaded on a system without the EAWT
                // because OSXAdapter extends ApplicationAdapter in its def
                log.error("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")", e);
            } catch (ClassNotFoundException e) {
                // This shouldn't be reached; if there's a problem with the OSXAdapter we should get the 
                // above NoClassDefFoundError first.
                log.error("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")", e);
            } catch (Exception e) {
                log.error("Exception while loading the OSXAdapter:", e);
            } catch (Error e) {
            	log.error("Exception while loading the OSXAdapter:", e);
            }
        }
    }
}
