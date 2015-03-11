/**
 * Spin - transparent threading solution for non-freezing Swing applications.
 * Copyright (C) 2002 Sven Meier
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sf.xmm.moviemanager.swing.progressbar;

import java.beans.PropertyChangeListener;

/**
 * A common interface for a progress.
 */
public interface ProgressBean {

  /**
   * Start.
   */
  public void start();

  /**
   * Cancel the progress.
   */
  public void cancel();

  /**
   * Get the current status.
   *
   * @return    status of progress
   */
  public double getStatus();

  public boolean getCancelled();
  
  public boolean isReady();
  
  /**
   * Add a listener top property changes.
   *
   * @param listener  listener to add
   */
  public void addPropertyChangeListener(PropertyChangeListener listener);
}
