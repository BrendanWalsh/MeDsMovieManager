/**
 * @(#)MovieManagerCommandPlay.java 1.0 15.11.08 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.MovieManagerConfig;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogMovieManager;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.StringUtil;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.StringUtil.FilenameCloseness;
import net.sf.xmm.moviemanager.util.plugins.MovieManagerStreamerHandler;
import net.sf.xmm.moviemanager.util.tools.SimpleMailbox;

import org.apache.log4j.Logger;

public class MovieManagerCommandPlay implements ActionListener {
	
	static Logger log = Logger.getLogger(MovieManagerCommandPlay.class);

	public void actionPerformed(ActionEvent e) {

		log.debug("ActionPerformed: " + e.getActionCommand());
		
		SimpleMailbox<LaunchError> mailbox = new SimpleMailbox<LaunchError>();
		
		try {
			
			if (!verifyMediaPlayer())
				return;
						
			execute(mailbox);
			mailbox.wait_for_message();
			handleReturnMessage(mailbox);

		} catch (IOException e1) {
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	static boolean verifyMediaPlayer() throws IOException {
		
		String cmd = MovieManager.getConfig().getMediaPlayerPath();

		if (cmd != null && "".equals(cmd)){

			JFileChooser chooser = new JFileChooser();
			int returnVal = chooser.showDialog(null, "Launch");
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return false;

			cmd = chooser.getSelectedFile().getCanonicalPath();
			MovieManager.getConfig().setMediaPlayerPath(cmd);
		}
		return true;
	}
	
	static void handleReturnMessage(SimpleMailbox<LaunchError> mailbox) {
		LaunchError msg = mailbox.getMessage();
		
		// -1 == success
		if (msg != null && msg.errorCode != -1) {
			// errorCode 2: File or dir not found
			// errorCode 13: Permission denied
			
			DialogAlert alert = new DialogAlert(MovieManager.getDialog(), msg.errorTitle, msg.message);
			GUIUtil.show(alert, true);
		}
	}
	
	public static void execute1() throws IOException, InterruptedException {
		execute(null);
	}
	
	public static void executePlay(String [] files) throws IOException, InterruptedException {
		
		if (!verifyMediaPlayer())
			return;
		
		SimpleMailbox<LaunchError> mailbox = new SimpleMailbox<LaunchError>();
		
		executePlay(files, mailbox);
		mailbox.wait_for_message();
		handleReturnMessage(mailbox);
		
	}
	
	public static void executePlay(String [] files, final SimpleMailbox<LaunchError> mailbox) throws IOException, InterruptedException {
		
		
		String [] command = null;
		String cmd = null;
		
		MovieManagerConfig mmc = MovieManager.getConfig();
		File cwd = null;

		if (SysUtil.isWindows() && mmc.getUseDefaultWindowsPlayer()) {
			cmd = "cmd.exe /C  ";

			// Can only have one file as argument. The second is ignored
			for (int i = 0; i < 1; i++) {
				cmd +=  "\"" + files[i] + "\"";
			}
		}
		else {

			ArrayList<String> commandList = new ArrayList<String>();

			cmd = mmc.getMediaPlayerPath();
			
			if (cmd != null && !"".equals(cmd)) {
				commandList.add(cmd);
			}

			String playArg = mmc.getMediaPlayerCmdArgument();

			if (playArg != null && !playArg.equals("")) {
				List<String> args = getArguments(playArg);

				for (int i = 0; i < args.size(); i++)
					commandList.add(args.get(i));
			}

			for (int i= 0; i < files.length; i++) {


				String filePath = files[i];
				String parentPath = new File(filePath).getParentFile().getAbsolutePath();

				// If the parent path contains spaces, use parent path as cwd (current working directory)
				if (parentPath.indexOf(" ") != -1) {

					if (cwd == null)
						cwd = new File(parentPath);

					commandList.add(new File(filePath).getName());
				}
				else
					commandList.add(filePath);
			}

			command = new String[commandList.size()];
			command = (String[]) commandList.toArray(command);
		}

		final String windowsDefault = cmd;

		// LaunchPlayer extends thread
		new LaunchPlayer(command, windowsDefault, cwd, mailbox).start();
	}

		
	public static void execute(final SimpleMailbox<LaunchError> mailbox) throws IOException, InterruptedException {
		
		// check if there is a file selected
		int listIndex = -1;
		DialogMovieManager movieManagerInstance = MovieManager.getDialog();
		TreeModel moviesListTreeModel = movieManagerInstance.getMoviesList().getModel();

		// check whether movies list has entries
		if (moviesListTreeModel.
				getChildCount(moviesListTreeModel.getRoot()) > 0) {

			listIndex = movieManagerInstance.getMoviesList().getLeadSelectionRow();

			if (listIndex == -1)
				listIndex = movieManagerInstance.getMoviesList().getMaxSelectionRow();

			if (movieManagerInstance.getMoviesList().getSelectionCount() > 1) {
				movieManagerInstance.getMoviesList().setSelectionRow(listIndex);
			}
		}

		if (listIndex == -1)
			return;

		ModelEntry selected = ((ModelEntry) ((DefaultMutableTreeNode) movieManagerInstance.getMoviesList().
				getLastSelectedPathComponent()).getUserObject());

		if (selected.getKey() == -1)
			return;

		String fileLocation = selected.getAdditionalInfo().getFileLocation();
		MovieManagerStreamerHandler streamerHandler = MovieManager.getConfig().getStreamerHandler();

		if (streamerHandler != null) {
			String extraInfoField = streamerHandler.getDatabaseUrlField();
			fileLocation = selected.getAdditionalInfo().getExtraInfoFieldValue(extraInfoField);
		}

		if (fileLocation.trim().equals(""))
			return;

		String [] files = fileLocation.split("\\*");

		FilenameCloseness [] closeness = {FilenameCloseness.almostidentical, FilenameCloseness.much, FilenameCloseness.some, FilenameCloseness.litte};

		if (MovieManager.getConfig().getExecuteExternalPlayCommand()) {
			File mediaFile = new File(files[0]);

			File [] dirFiles = mediaFile.getParentFile().listFiles();

			if (dirFiles != null) {
				for (int u = 0; u < closeness.length; u++) {

					for (int i = 0; i < dirFiles.length; i++) {

						if ((!SysUtil.isWindows() && dirFiles[i].getName().endsWith(".xmm.sh")) || 
								(SysUtil.isWindows() && dirFiles[i].getName().endsWith(".xmm.bat"))) {

							FilenameCloseness closeness_result = StringUtil.compareFileNames(dirFiles[i].getName(), mediaFile.getName());

							// File names are similar
							if (closeness_result == closeness[u]) {
								final File dirFile = dirFiles[i];
								final LaunchPlayer player = new LaunchPlayer(null, dirFile.getAbsolutePath(), mediaFile.getParentFile(), mailbox);
								player.start();
								return;
							}
						}
					}
				}
			}
		}
		
		executePlay(files, mailbox);
	}
	
	
	public static void printCommand(String intro, String [] args) {
				
		String str = intro;
		
		for (int i = 0; i < args.length; i++)
			str += SysUtil.getLineSeparator() + "args["+i+"]:" + args[i];
			
		log.debug(str);
	}
	
	
	public static String getCombined(String [] args) {
		String str = args[0];
		
		for (int i = 1; i < args.length; i++)
			str += " " + args[i];
		
		return str;
	}
	
	
	/**
	 * Splits the string on spaces, except when enclosed in single or double quotes
	 * @param arg
	 * @return
	 */
	public static List<String> getArguments(String arg) {
		
		List<String> matchList = new ArrayList<String>();
		Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
		Matcher regexMatcher = regex.matcher(arg);

		while (regexMatcher.find()) {
						
			if (regexMatcher.group(1) != null) {
				// Add double-quoted string without the quotes
				matchList.add(regexMatcher.group(1));
			} else if (regexMatcher.group(2) != null) {
				// Add single-quoted string without the quotes
				matchList.add(regexMatcher.group(2));
			} else {
				// Add unquoted word
				matchList.add(regexMatcher.group(0));
			}
		}
		return matchList;
	}
}

class LaunchError {
	
	int errorCode;
	String errorTitle;
	String message;
	
	LaunchError(int errorCode, String errorTitle, String message) {
		this.errorCode = errorCode;
		this.errorTitle = errorTitle;
		this.message = message;
	}
	
	public String toString() {
		return message;
	}
}

/**
 * Runs the command using Runtimes exec method.
 * @author Bro
 */
class LaunchPlayer extends Thread {

	static Logger log = Logger.getLogger(LaunchPlayer.class);
	
	String [] args;
	String command;
	File cwd;
	
	SimpleMailbox<LaunchError> mailbox = null;
	
	public Process p;
	
	// cwd = Current working directory
	LaunchPlayer(String [] args, String command, File cwd) {
		this.args = args;
		this.command = command;
		this.cwd = cwd;
	}
	
	LaunchPlayer(String [] args, String command, File cwd, SimpleMailbox<LaunchError> mailbox) {
		this(args, command, cwd);
		this.mailbox = mailbox;
	}
	
	
	public void run() {
		
		try {
					
			if (args != null && cwd != null) {
				log.debug("Execute command from cwd:" + cwd);
				printCommand("Command executed:", args);
				
				p = Runtime.getRuntime().exec(args, null, cwd);
			}
			else if (cwd != null) {
				log.debug("Execute file:" + command);
				p = Runtime.getRuntime().exec(command, null, cwd);
			} else if (args == null) {
				log.debug("Execute command:" + command);
				p = Runtime.getRuntime().exec(command);
			} else {
				printCommand("Execute default player:", args);
				p = Runtime.getRuntime().exec(args);
			}
								
		} catch (IOException e) {
			log.error("Exception: " + e.getMessage(), e);
								
			int error = -1;
			String errorTitle = null;
			String message = null;
			
			Pattern p = Pattern.compile("(.*?): java.io.IOException: error=(\\d+),(.*)");
			Matcher m = p.matcher(e.getMessage());
				
			// Example error message with invalid path to vlc
			// Cannot run program "/home/bro/vlc" (in directory "/media/shared/Video/Filmer/21 Grams (2003)"): java.io.IOException: error=2, No such file or directory
		
			if (m.matches()) {
				message = m.group(1);
				error = Integer.parseInt(m.group(2));
				errorTitle = m.group(3);
			}
						
			if (mailbox != null) {
				try {
					if (error != -1)					
						mailbox.setMessage(new LaunchError(error, errorTitle, message));
					else
						mailbox.setMessage(new LaunchError(0, "An error occured", e.getMessage()));
					
				} catch (InterruptedException e1) {
					log.error("Exception: " + e1.getMessage(), e1);
				}
			}
		}
		finally {
			
			if (mailbox != null)
				try {
					mailbox.setMessage(new LaunchError(-1, "exec done", null));
				} catch (InterruptedException e) {
					log.error("Exception: " + e.getMessage(), e);
				}
		}
		
		if (p == null)
			return;
		
		// Clear input/error streams to avoid dead lock in subprocess
		SysUtil.cleaStreams(p);
	}
	
	public static void printCommand(String intro, String [] args) {
		
		String str = intro;
		
		for (int i = 0; i < args.length; i++)
			str += SysUtil.getLineSeparator() + "args["+i+"]:" + args[i];
			
		log.debug(str);
	}
}
