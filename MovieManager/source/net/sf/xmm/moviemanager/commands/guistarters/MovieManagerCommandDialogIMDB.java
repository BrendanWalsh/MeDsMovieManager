package net.sf.xmm.moviemanager.commands.guistarters;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogIMDB;
import net.sf.xmm.moviemanager.gui.DialogIMDbUpdate;
import net.sf.xmm.moviemanager.gui.DialogIMDbMultiAdd;
import net.sf.xmm.moviemanager.models.ModelMovieInfo;
import net.sf.xmm.moviemanager.util.GUIUtil;

public class MovieManagerCommandDialogIMDB {

	Logger log = Logger.getLogger(this.getClass());
	
	public boolean cancel = false;
	public boolean cancelAll = false;

	String tmpUrlKey;
		
	public synchronized String getIMDBKey(final String movieTitle) throws InterruptedException {
		
		if (cancelAll)
			return null;

		tmpUrlKey = null;
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					ModelMovieInfo modelInfo = new ModelMovieInfo(false, true);
					modelInfo.model.setTitle(movieTitle);

					DialogIMDbUpdate dialogIMDb = new DialogIMDbUpdate(modelInfo.model, movieTitle, true);
					GUIUtil.showAndWait(dialogIMDb, true);
					
					tmpUrlKey = modelInfo.model.getUrlKey();
					
					cancel = dialogIMDb.getCanceled();
					cancelAll = dialogIMDb.getAborted();
				}
			});
		} catch (InvocationTargetException e) {
			log.error("Exception:" + e.getMessage(), e);
		}
		
		return tmpUrlKey;
	}
}
