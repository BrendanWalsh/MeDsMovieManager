package net.sf.xmm.moviemanager.swing.extentions.filetree;

//Add the event registration and notification code to a class.
public class FileTreeEventsHandler {
   
	// Create the listener list
    protected javax.swing.event.EventListenerList listenerList =
        new javax.swing.event.EventListenerList();

    // This methods allows classes to register for AddSelectedFilesEvents
    public void addFileTreeEventListener(FileTreeEventListener listener) {
        listenerList.add(FileTreeEventListener.class, listener);
    }

    // This methods allows classes to unregister for AddSelectedFilesEvents
    public void removeFileTreeEventListener(FileTreeEventListener listener) {
        listenerList.remove(FileTreeEventListener.class, listener);
    }
  
    void fireAddSelectedFilesEvent(FileTreeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
         
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == FileTreeEventListener.class) {
                ((FileTreeEventListener)listeners[i+1]).addSelectedFilesEventOccurred(evt);
            }
        }
    }
  
    void firePlaySelectedFilesEvent(FileTreeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
         
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == FileTreeEventListener.class) {
                ((FileTreeEventListener)listeners[i+1]).playSelectedFilesEventOccurred(evt);
            }
        }
    }
  
    void fireRootDeviceAddedEvent(FileTreeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
         
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == FileTreeEventListener.class) {
                ((FileTreeEventListener)listeners[i+1]).rootDeviceAddedEventOccurred(evt);
            }
        }
    }
   
    void fireRootDeviceRemovedEvent(FileTreeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
         
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == FileTreeEventListener.class) {
                ((FileTreeEventListener)listeners[i+1]).rootDeviceRemovedEventOccurred(evt);
            }
        }
    }
    
    void fireFileTreeWorkingEvent(FileTreeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
         
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == FileTreeEventListener.class) {
                ((FileTreeEventListener)listeners[i+1]).fileTreeIsWorkingEvent(evt);
            }
        }
    }
    
    void fireFileTreeReadyEvent(FileTreeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
         
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == FileTreeEventListener.class) {
                ((FileTreeEventListener)listeners[i+1]).fileTreeIsReadyEvent(evt);
            }
        }
    }
}