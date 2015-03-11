package net.sf.xmm.moviemanager.swing.extentions.filetree;

import java.util.EventListener;

//A class must implement this interface to get MyEvents.
public interface FileTreeEventListener extends EventListener {
	public void addSelectedFilesEventOccurred(FileTreeEvent evt);
	public void playSelectedFilesEventOccurred(FileTreeEvent evt);
	public void rootDeviceAddedEventOccurred(FileTreeEvent evt);
	public void rootDeviceRemovedEventOccurred(FileTreeEvent evt);
	public void fileTreeIsWorkingEvent(FileTreeEvent evt);
	public void fileTreeIsReadyEvent(FileTreeEvent evt);
}
