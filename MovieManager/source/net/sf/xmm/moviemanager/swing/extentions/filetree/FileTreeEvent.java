package net.sf.xmm.moviemanager.swing.extentions.filetree;

import java.io.File;
import java.util.EventObject;

//Declare the event. It must extend EventObject.
public class FileTreeEvent extends EventObject {
	
	File file = null;
	boolean searching = false;	
	boolean buildingTree = false;	
	
	public FileTreeEvent(boolean searching) {
		super(searching);
		
		if (searching)
			this.searching = true;
		else
			buildingTree = true;
	}
	
	public FileTreeEvent(File f) {
		super(f);
		file = f;
	}
	
    public FileTreeEvent(Object source) {
        super(source);
    }
    
    public File getFile() {
    	return file;
    }
    
    public boolean isSearching() {
    	return searching;
    }
    
    public boolean isBuildingTree() {
    	return buildingTree;
    }
}
