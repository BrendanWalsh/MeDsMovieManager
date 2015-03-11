package net.sf.xmm.moviemanager.util.tools;

import java.io.File;
import java.lang.reflect.Method;

import net.sf.xmm.moviemanager.util.SysUtil;

import org.apache.log4j.Logger;

import edu.stanford.ejalbert.BrowserLauncher;

public class BrowserOpener {

	Logger log = Logger.getLogger(getClass());
    
    private String url;
    
    /**
     * Constructor. Initialises the _url var.
     **/
    public BrowserOpener(String url) {
        this.url = url;
    } 
    
    public void executeOpenBrowser(String browser, String browserPath) {
    	executeOpenBrowser(browser, new File(browserPath));
    }
    
    public void executeOpenBrowser(final String browser, final File browserPath) {
        
        class LaunchBrowser extends Thread {
            
            public void run() {
            	
            	boolean browserLauncherFailed = false;
                boolean customBrowser = false;
                
                if (browser.equals("Custom"))
                	customBrowser = true;
                                
                if (!customBrowser) {
                    
                    try {
                        BrowserLauncher launcher = new BrowserLauncher(null);
                                    	                        
                        if (browser.equals("Default")) {
                        	log.debug("Launching BrowserLauncher(default):" + url);
                        	launcher.openURLinBrowser(url);
                        }
                        else {
                        	log.debug("Launching BrowserLauncher:" + browser);
                        	launcher.openURLinBrowser(browser, url);
                        } 
                    }  
                    catch (Exception e) {
                    	log.debug("BrowserLauncher2 failed:" + e.getMessage());
                        browserLauncherFailed = true;
                    }
                }
                                
                if (browserLauncherFailed || customBrowser) {
                    
                    try {
                        Process p = null;
                    	
                        if (customBrowser && browserPath.isFile()) {
                            String cmd = browserPath + " " + url;
                            
                            log.debug("Manually launch:" + cmd);
                            p = Runtime.getRuntime().exec(cmd);
                        }
                        else {
                        	                         	 
                            if (browser.equals("Default") && SysUtil.isWindows()) {
                            	log.debug("Manually launch default Windows browser");
                                p = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                            }
                            else if (browser.equals("Default") && SysUtil.isMac()) {
                            	log.debug("Manually launch default OSX browser");
                            	
                                Class<?> macUtils = Class.forName("com.apple.mrj.MRJFileUtils");
                                Method openURL = macUtils.getDeclaredMethod("openURL", new Class[] {String.class});
                                openURL.invoke(null, new Object[] {url});
                            }
                            else {
                                
                                String cmd;                                
                                String remoteOpenURL = " -remote openURL" + "(" + url + ")";
                                
                                for (int i = 0; i < 5; i++) {
                                    
                                    if (i == 0)
                                        cmd = "opera" + remoteOpenURL; 
                                    else if (i == 1)
                                        cmd = "firefox" + remoteOpenURL;
                                    else if (i == 2)
                                        cmd = "mozilla" + remoteOpenURL;
                                    else if (i == 3)
                                        cmd = "konqueror" + remoteOpenURL;
                                    else 
                                        cmd = "netscape" + remoteOpenURL;
                                                                         
                                    log.debug("Manually launch browser:" + cmd);
                                    p = Runtime.getRuntime().exec(cmd);
                                     
                                    try {
                                        int exitCode = p.waitFor();
                                        
                                        if (exitCode != 0) {
                                            /*Command failed, start up the browser*/
                                            p = Runtime.getRuntime().exec(cmd);
                                        }
                                        break;
                                    }
                                    
                                    catch(InterruptedException x) {
                                    	System.out.println("Error bringing up browser, cmd='" + cmd + "'");
                                    }
                                }
                            }
                         // Clear input/error streams to avoid dead lock in subprocess
            				SysUtil.cleaStreams(p);
                        }
                    } catch (Exception e) {
                    	System.out.println("Exception: "+  e.getMessage());
                    }
                }
            }
        }
        
        /* Creating a Object wrapped in a Thread */
        Thread t = new Thread(new LaunchBrowser());
        t.start();
    }
}