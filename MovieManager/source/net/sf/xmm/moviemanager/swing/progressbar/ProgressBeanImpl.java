package net.sf.xmm.moviemanager.swing.progressbar;


import java.beans.PropertyChangeListener;

import net.sf.xmm.moviemanager.util.GUIUtil;

/**
 * Implementation of a progress.
 */
public class ProgressBeanImpl implements ProgressBean, Runnable {

	public PropertyChangeListener listener = null;

	private boolean cancelled = false;
	private double  status;
	private String message = "";
	private Runnable r = null;

	boolean success = true;
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public boolean success() {
		return success;
	}
	
	/**
	 * Empty Constructor.
	 */
	public ProgressBeanImpl() {}

	/**
	 * Constructor taking a runnable object.
	 */
	public ProgressBeanImpl(Runnable r) {
		this.r = r;
	}

	// Must be overridden
	public void run() {}

	/**
	 * Add a listener to property changes.
	 * 
	 * @param listener listener to add
	 */
	synchronized public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.listener = listener;
		return;
	}

	/**
	 * Start.
	 */
	public void start() {

		if (r != null)
			r.run();
		else
			run();
	}

	/**
	 * Cancel the progress.
	 */
	public synchronized void cancel() {
		cancelled = true;
	}

	/**
	 * Get the current status.
	 *
	 * @return    status of progress
	 */
	public double getStatus() {
		GUIUtil.isNotEDT();

		return status;
	}

	public String getMessage() {
		GUIUtil.isNotEDT();
		return message;
	}

	public synchronized boolean getCancelled() {
		return cancelled;
	}

	public boolean isReady() {
		return true;
	}
}

