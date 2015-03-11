package net.sf.xmm.moviemanager.commands.guistarters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.gui.DialogReportGenerator;

public class MovieManagerCommandReportGenerator implements ActionListener {

	Logger log = Logger.getLogger(getClass());
	
    /**
     * Executes the command.
     **/
    protected static void execute() {
        DialogReportGenerator dialogPrint = new DialogReportGenerator(MovieManager.getDialog());
    }

    /**
     * Invoked when an action occurs.
     **/
    public void actionPerformed(ActionEvent event) {
        log.debug("ActionPerformed: " + event.getActionCommand());
        execute();
    }
}
