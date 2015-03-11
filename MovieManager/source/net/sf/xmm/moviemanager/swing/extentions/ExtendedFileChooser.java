/**
 * @(#)ExtendedFileChooser.java 1.0 26.09.06 (dd.mm.yy)
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

package net.sf.xmm.moviemanager.swing.extentions;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.basic.BasicFileChooserUI;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogAlert;
import net.sf.xmm.moviemanager.gui.DialogQuestion;
import net.sf.xmm.moviemanager.util.CustomFileFilter;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.SysUtil;


public class ExtendedFileChooser extends JFileChooser {

	private JList jList;
	private ExtendedFileChooser fileChooser;
	private File lastSelectedFile = null;
	private ArrayList<File> directories;
	private boolean approveSelected = false;
	private String fileAlreadyExistWarningMessage = "";

	private File selectedFile;

	boolean textFieldAction = false;
	boolean approveFileSelection = true;
	
	public void setApproveFileSelection(boolean approveFileSelection) {
		this.approveFileSelection = approveFileSelection;
	}
	
	public ExtendedFileChooser() {
		super();
		fileChooser = this;
		setUp();
	}

	public ExtendedFileChooser(File currentDirectory, int fileSelectionMode) {
		super(currentDirectory);
		setFileSelectionMode(fileSelectionMode);
		fileChooser = this;
		setUp();
	}


	public ExtendedFileChooser(File currentDirectory, FileSystemView fsv) {
		super(currentDirectory, fsv);
		fileChooser = this;
		setUp();
	}

	public ExtendedFileChooser(FileSystemView fsv) {
		super(fsv);
		fileChooser = this;
		setUp();
	}

	public ExtendedFileChooser(String currentDirectoryPath) {
		super(currentDirectoryPath);
		fileChooser = this;
		setUp();
	}

	public ExtendedFileChooser(String currentDirectoryPath, int fileSelectionMode) {
		super(currentDirectoryPath);
		setFileSelectionMode(fileSelectionMode);
		fileChooser = this;
		setUp();
	}

	public ExtendedFileChooser(String currentDirectoryPath, FileSystemView fsv) {
		super(currentDirectoryPath, fsv);
		fileChooser = this;
		setUp();
	}

	public ExtendedFileChooser(File currentDirectory) {
		super(currentDirectory);
		fileChooser = this;
		setUp();
	}

	private void setUp() {

		if (jList == null)
			setJList();

		/* Sets the value of the name JTextField to current directory */ 
		if (fileChooser.getFileSelectionMode() == ExtendedFileChooser.DIRECTORIES_ONLY) {
			FileChooserUI ui = getUI();

			if (ui instanceof BasicFileChooserUI) {
				((BasicFileChooserUI)ui).setFileName(getCurrentDirectory().getAbsolutePath());
			}
		}

		if (jList != null) {
			jList.addKeyListener(new KeyHandler());
			jList.addMouseListener(new MouseHandler());
		}



		/* text field */
		final JTextField textField = (JTextField) findComponent(this, JTextField.class);

		//ActionListener[] listeners = textField.getActionListeners();

		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldAction = true;
				approveSelection();
				textFieldAction = false;
			}
		});
	}


	public void approveSelection() {

		approveSelected = true;

		if (getDialogType() == JFileChooser.SAVE_DIALOG || 
				getDialogType() == JFileChooser.CUSTOM_DIALOG) {

			File selectedFile = getSelectedFile();
			FileFilter fileFilter = getFileFilter();
			
			if (fileFilter == null || !approveFileSelection) {
				super.approveSelection();
				return;
			}
			
			CustomFileFilter customFilter = (CustomFileFilter) fileFilter;
			
			String [] extenstions = customFilter.getExtensions();
			boolean exists = false;
			File file = null;

			/*Goes through the extensions array and checks if each of the files exist*/
			if (selectedFile != null) {
				for (int i = 0; i < extenstions.length && !exists; i++) {
					if (selectedFile.getName().endsWith("." + extenstions[i])) {
	
						String filePath = selectedFile.getAbsolutePath();
						filePath = filePath.substring(0, filePath.lastIndexOf(".")+1);
	
						for (int u = 0; u < extenstions.length; u++) {
							file = new File(filePath+extenstions[u]);
	
							if (file.exists()) {
								exists = true;
								break;
							}
						}
					}
					else {
						file = new File(selectedFile.getAbsolutePath() + "." + extenstions[i]);
						if (file.exists()) {
							exists = true;
							break;
						}
					}
				}
			}

			if (exists) {
				String fileName = selectedFile.getName();
				if (fileName.indexOf(".") != -1)
					fileName = fileName.substring(0, fileName.lastIndexOf("."));

				String msg;
				if (fileAlreadyExistWarningMessage.equals("")) {
					msg = "File with name " + file.getName() + " already exists. Do you wish to replace it?";
				}
				else
					msg = fileAlreadyExistWarningMessage + fileName + " already exists. Do you wish to replace it?";

				/* Delete confirmation */
				DialogQuestion question = new DialogQuestion("Warning", msg);
				GUIUtil.showAndWait(question, true);

				if (question.getAnswer()) {
					if (!deleteFiles(file)) {
						DialogAlert alert; 

						if (fileAlreadyExistWarningMessage.equals(""))
							alert = new DialogAlert(MovieManager.getDialog(), "Error", "File could not be deleted.");
						else
							alert = new DialogAlert(MovieManager.getDialog(), "Error", fileAlreadyExistWarningMessage + selectedFile.getName() + " could not be deleted.");
						GUIUtil.showAndWait(alert, true);

						approveSelected = false;
						return;
					}
					else {
						super.approveSelection();
					}
				}
			}
			else {
				super.approveSelection();
			}
		}

		if(getDialogType() == JFileChooser.OPEN_DIALOG) {

			FileChooserUI ui = getUI();
			String fileName = "";
			File tempFile = null;

			if (ui instanceof BasicFileChooserUI)
				fileName = ((BasicFileChooserUI) ui).getFileName();

			String currentDir = fileChooser.getCurrentDirectory().getAbsolutePath();
			boolean customFilter = fileChooser.getFileFilter() instanceof CustomFileFilter ? true : false;

			if (fileName != null && !fileName.equals("")) {
				tempFile = new File(fileName);
			}

			if (textFieldAction && (fileChooser.getFileSelectionMode() == JFileChooser.FILES_ONLY || (customFilter && ((CustomFileFilter) fileChooser.getFileFilter()).getFileAcceptMode() == CustomFileFilter.DIRECTORIES_ONLY))) {

				/* Change current directory */
				if (currentDir != null && fileName != null) {

					if (currentDir.equals(fileName)) {
						this.selectedFile = tempFile;
						super.approveSelection();
						return;
					}


					if (fileName.equals("")) {
						this.selectedFile = new File(currentDir);
						super.setSelectedFile(this.selectedFile);
						super.approveSelection();
						return;
					}	

					if (fileName.startsWith(File.separator)) {
						File newDir = new File(fileName);

						if (newDir.isDirectory()) {
							setCurrentDirectory(newDir);
							return;
						}

					}

					File newFile = new File(currentDir, fileName);

					if (newFile.isDirectory()) {

						if (ui instanceof BasicFileChooserUI)
							((BasicFileChooserUI) ui).setFileName("");

						setCurrentDirectory(newFile);
						return;
					}
					else if (newFile.isFile()) {
						this.selectedFile = newFile;
						super.setSelectedFile(this.selectedFile);
						super.approveSelection();
						return;
					}
				}
			}

			if (tempFile != null) {

				if (tempFile.exists()) {
					this.selectedFile = tempFile;
					super.approveSelection();
					return;
				}

				if (!currentDir.endsWith(SysUtil.getDirSeparator()))
					currentDir += SysUtil.getDirSeparator();

				if (fileName.lastIndexOf(SysUtil.getDirSeparator()) != -1)
					fileName = fileName.substring(fileName.lastIndexOf(SysUtil.getDirSeparator())+1, fileName.length());

				tempFile = new File(currentDir + fileName);

				if (tempFile.exists()) {
					this.selectedFile = tempFile;
					super.approveSelection();
					return;
				}
			}

			if (isMultiSelectionEnabled() && super.getSelectedFiles().length > 1) {
				super.approveSelection();
				return;
			}
			else if (getSelectedFile() != null && getSelectedFile().exists()) {
				super.approveSelection();
				return;
			}
		}
	}

	private boolean deleteFiles(File databaseFile) {

		String [] extensions = ((CustomFileFilter) getFileFilter()).getExtensions();

		if (extensions.length > 1) {
			String databasePath = databaseFile.getAbsolutePath();

			databasePath = databasePath.substring(0, databasePath.lastIndexOf(".")+1);
			File file;

			for (int i = 0; i < extensions.length; i++) {
				file = new File(databasePath + extensions[i]);

				if (file.exists()) {
					if (!file.delete())
						return false;
				}
			}
		}
		return true;
	}

	/* Deletes a file or recursively deletes a directory with files/subdirectories */
	public static boolean deleteDir(File dir) {

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}


	//  public void setSelectedFile(File selectedFile) {

	//  	File oldValue = this.selectedFile;

	//  	System.out.println("setSelectedFile");

	//  	if (selectedFile != null) {

	//  	    this.selectedFile = selectedFile;

	//  	    firePropertyChange(SELECTED_FILE_CHANGED_PROPERTY, oldValue, selectedFile);
	//  	}
	//      }	    


	//    public File[] getSelectedFiles() {

	//  	System.out.println("getSelectedFiles");

	//  	if(selectedFiles == null) {
	//  	    return new File[0];
	//  	} else {
	//  	    System.out.println("return:" +selectedFiles.length);
	//  	    return (File[]) selectedFiles.clone();
	//  	}
	//      }


	//   public void setSelectedFiles(File[] selectedFiles) {

	//  	System.out.println("setSelectedFiles:"+ selectedFiles.length);

	//  	File[] oldValue = this.selectedFiles;
	//  	if (selectedFiles != null && selectedFiles.length == 0) {
	//  	    selectedFiles = null;
	//  	}
	//  	this.selectedFiles = selectedFiles;
	//  	setSelectedFile((selectedFiles != null) ? selectedFiles[0] : null);
	//  	firePropertyChange(SELECTED_FILES_CHANGED_PROPERTY, oldValue, this.selectedFiles);
	//      }

	//   ListModel model = jList.getModel();

	//  	    for (int i = 0; i < model.getSize(); i++) {
	//  		if (model.getElementAt(i).equals(file)) {
	//  		    jList.setSelectedIndex(i);
	//  		    break;
	//  		}
	//  	    }
	//}
	//}



	//   public void setSelectedFiles(File [] file) {

	//  	int indices

	//  	System.out.println("setSelectedFiles");

	//  	if (file != null) {

	//  	    ListModel model = jList.getModel();

	//  	    for (int i = 0; i < model.getSize(); i++) {
	//  		if (model.getElementAt(i).equals(file)) {
	//  		    jList.setSelectedIndex(i);
	//  		    break;
	//  		}
	//  	    }
	//  	}
	//      }


	public File [] getSelectedFiles() {

		if (isMultiSelectionEnabled()) {

			File [] selected1 = getSelectedFiles2();
			File [] selected2 = super.getSelectedFiles();

			int selected1Count = 0;
			int selected2Count = 0;

			if (selected1 != null)
				selected1Count = selected1.length;

			if (selected2 != null)
				selected2Count = selected2.length;

			if (selected1Count > selected2Count) {
				return selected1;
			}
		}
		return super.getSelectedFiles();
	}

	/* Gets the selected files directly from the JFileChooser's JList */
	public File [] getSelectedFiles2() {

		if (jList != null && jList.isSelectionEmpty() && approveSelected) {

			FileChooserUI ui = getUI();
			File [] selectedFile2 = new File[1];

			if (ui instanceof BasicFileChooserUI) {
				String fileName = ((BasicFileChooserUI)ui).getFileName();

				if (fileName != null && !fileName.equals("")) {
					File currentDir = fileChooser.getCurrentDirectory();

					if (selectedFile != null) {
						selectedFile2[0] = selectedFile;
						return selectedFile2;
					}
					else {
						selectedFile2[0] = new File(currentDir.getAbsolutePath() + SysUtil.getDirSeparator()+fileName);
						return selectedFile2;
					}
				}
			}
		}

		if (jList == null || jList.getSelectedIndex() == -1) {
			return null;
		}

		Object [] fileObjects = jList.getSelectedValues();
		File [] files = new File[0];

		if (fileObjects != null && fileObjects.length > 0) {

			files = new File[fileObjects.length];

			for (int i = 0; i < fileObjects.length; i++)
				files[i] = (File) fileObjects[i];
		}

		return files;
	}

	public File getSelectedFile() {

		File file = null;

		if (isMultiSelectionEnabled()) {
			file = selectedFile;
		}

		if (file == null) {
			file = super.getSelectedFile();
		}

		if (file == null) {

		}
		return file;
	}

	public File getSelectedFile2() {

		if (jList != null && jList.isSelectionEmpty() && approveSelected) {
			FileChooserUI ui = getUI();

			if (ui instanceof BasicFileChooserUI) {
				String fileName = ((BasicFileChooserUI)ui).getFileName();

				if (fileName != null && !fileName.equals("")) {
					File currentDir = fileChooser.getCurrentDirectory();

					if (selectedFile != null) {
						return selectedFile;
					}
					else {
						return new File(currentDir.getAbsolutePath() + SysUtil.getDirSeparator()+fileName);
					}
				}
			}
		}

		if (jList == null || jList.getSelectedIndex() == -1) {
			return null;
		}

		return (File) jList.getSelectedValue();
	}


	//      public void setSelectedFile(File file) {

	//  	System.out.println("setSelectedFile");

	//  	File oldValue = selectedFile;
	//  	selectedFile = file;
	//  	if(selectedFile != null) {
	//  	    if (file.isAbsolute() && !getFileSystemView().isParent(getCurrentDirectory(), selectedFile)) {
	//  		setCurrentDirectory(selectedFile.getParentFile());
	//  	    }
	//  	    if (!isMultiSelectionEnabled() || selectedFiles == null || selectedFiles.length > 1) {
	//  		ensureFileIsVisible(selectedFile);
	//  	    }
	//  	}
	//  	firePropertyChange(SELECTED_FILE_CHANGED_PROPERTY, oldValue, selectedFile);

	//  	super.setSelectedFile(file);
	//      }


	//  public File[] getSelectedFiles() {

	//  	System.out.println(super.getSelectedFiles().length);

	//  	if(selectedFiles == null) {
	//  	    return new File[0];
	//  	} else {
	//  	    return (File[]) selectedFiles.clone();
	//  	}
	//      }


	//   public void setSelectedFiles(File[] selectedFiles) {

	//  	System.out.println("setSelectedFile:" + selectedFiles.length);

	//  	File[] oldValue = this.selectedFiles;
	//  	if (selectedFiles != null && selectedFiles.length == 0) {
	//  	    selectedFiles = null;
	//  	}
	//  	this.selectedFiles = selectedFiles;
	//  	setSelectedFile((selectedFiles != null) ? selectedFiles[0] : null);
	//  	firePropertyChange(SELECTED_FILES_CHANGED_PROPERTY, oldValue, this.selectedFiles);

	//  	super.setSelectedFiles(selectedFiles);
	//      }


	private void setJList() {

		jList = (JList) findJList(this);

		try {
			if (jList == null) {	
				JScrollPane jsp = (JScrollPane) findJScrollPane(this);

				if (jsp.getViewport().getView() instanceof JList)
					jList = (JList) jsp.getViewport().getView();

				if (jList == null)
					System.out.println("findJScrollPane failed");

				jsp = null;
			}
		}

		catch (Exception e) {
			System.out.println("Exception: "+e.getMessage());
			e.printStackTrace();
		}
	}

	/*Recursively runs through the component and checks if every component is a JList*/
	private Component findJList(Component comp) {

		if (comp.getClass() == JList.class) return comp;

		if (comp instanceof Container) {
			Component[] components = ((Container)comp).getComponents();
			for(int i = 0; i < components.length; i++) {
				Component child = findJList(components[i]);
				if (child != null) return child;
			}
		}
		return null;
	}

	/*Recursively runs through the component and checks if every component is a JScrollPane*/
	private Component findJScrollPane(Component comp) {

		if (comp.getClass() == JScrollPane.class) return comp;

		if (comp instanceof Container) {
			Component[] components = ((Container)comp).getComponents();
			for(int i = 0; i < components.length; i++) {
				Component child = findJScrollPane(components[i]);
				if (child != null) return child;
			}
		}
		return null;
	}




	public static Component findComponent(Component comp, Class<JTextField> c) {

		if(c.isAssignableFrom(comp.getClass())) {
			return comp;
		}

		if(comp instanceof Container) {
			Component[] comps = ((Container) comp).getComponents();
			for(int i = 0; i < comps.length; i++) {
				Component tmp = findComponent(comps[i], c);
				if(tmp != null) {
					return tmp;
				}
			}
		}
		return null;
	}


	public void setFileAlreadyExistWarningMessage(String msg) {
		fileAlreadyExistWarningMessage = msg;
	}

	private void updateNameField() {

		File selectedFile = getSelectedFile();
		String value = "";

		if (selectedFile == null || isDirectorySelectionEnabled()) {
			return;
		}

		value = selectedFile.getName();

		FileChooserUI ui = getUI();

		if (ui instanceof BasicFileChooserUI) {
			((BasicFileChooserUI)ui).setFileName(value);
		}
	}


	class KeyHandler extends KeyAdapter {

		ListModel listModel;

		public KeyHandler() {
			listModel = jList.getModel();
		}

		public void keyReleased(KeyEvent e) {

			lastSelectedFile = fileChooser.getSelectedFile();

			if (e.getKeyChar() == KeyEvent.VK_ENTER) {

				if (fileChooser.getFileSelectionMode() == ExtendedFileChooser.DIRECTORIES_ONLY) {
					if (fileChooser.getSelectedFile() != null && fileChooser.getSelectedFile().isDirectory())
						;	//fileChooser.approveSelection();
				}

				if (fileChooser.getFileSelectionMode() == ExtendedFileChooser.FILES_ONLY) {
					if (fileChooser.getSelectedFile() != null && fileChooser.getSelectedFile().isFile())
						fileChooser.approveSelection();

				}
			}

			if(e.getKeyCode() == KeyEvent.VK_DELETE) {
				String msg = "Confirm deletion";

				File [] selectedFiles = getSelectedFiles2();

				if (selectedFiles == null) {
					selectedFiles = new File[1];
					selectedFiles[0] = fileChooser.getSelectedFile();
				}

				if (selectedFiles[0] == null)
					selectedFiles[0] = getSelectedFile2();

				if (selectedFiles[0] == null)
					return;

				if (selectedFiles.length > 1)
					msg = "Are you sure you want to permanently delete these "+ selectedFiles.length +" items ?";
				else {
					if (selectedFiles[0] != null && selectedFiles[0].isFile())
						msg = "Are you sure you want to permanently delete this file ?";
					else if (selectedFiles[0] != null && selectedFiles[0].isDirectory())
						msg = "Are you sure you want to permanently delete this directory and all of it's content ?";
				}

				DialogQuestion question = new DialogQuestion("Delete confirmation", msg);
				GUIUtil.showAndWait(question, true);

				if (!question.getAnswer()) {
					return;
				}

				for (int i = 0; i < selectedFiles.length; i++) {

					if (!deleteDir(selectedFiles[i])) {

						if (selectedFiles[i].isFile())
							msg = "Failed to delete file "+ selectedFiles[i].toString();
						else 
							msg = "Failed to delete directory "+selectedFiles[i].toString();

						DialogAlert alert = new DialogAlert(MovieManager.getDialog(), "Error", msg);
						GUIUtil.showAndWait(alert, true);
						break;
					}
				}

				fileChooser.rescanCurrentDirectory();
				lastSelectedFile = null;

				jList.clearSelection();
			}

			updateNameField();
		}

		public void keyPressed(KeyEvent e) {
			updateNameField();
		}

		public void keyTyped(KeyEvent ke) {

			if (fileChooser.getSelectedFile() != null) {

				//String absolutePath = fileChooser.getSelectedFile().getAbsolutePath();
				char upperCasePartialName = Character.toUpperCase(ke.getKeyChar());

				directories = new ArrayList<File>(listModel.getSize());

				for (int i = 0; i < listModel.getSize(); i++) {
					if (Character.toUpperCase(((File) listModel.getElementAt(i)).getName().charAt(0))
							== upperCasePartialName)
						directories.add((File) listModel.getElementAt(i));
				}

				int index = 0;
				for (int i = 0; i < directories.size(); i++) {

					if (directories.get(i).equals(lastSelectedFile)) {
						if (i+1 < directories.size()) {
							index = i+1;
							break;
						} 
					}
				}

				if (directories.size() > 0) {
					lastSelectedFile = directories.get(index);
					fileChooser.setSelectedFile(directories.get(index));
					fileChooser.ensureFileIsVisible(directories.get(index));
				}

				updateNameField();
			}
		}
	}

	class MouseHandler extends MouseAdapter {

		public void mouseReleased(MouseEvent e){
			lastSelectedFile = fileChooser.getSelectedFile();
			updateNameField();
		}
	}
}
