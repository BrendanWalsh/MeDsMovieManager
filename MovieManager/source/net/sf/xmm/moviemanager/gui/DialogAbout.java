/**
 * @(#)DialogAbout.java 1.0 24.01.06 (dd.mm.yy)
 *
 * Copyright (2003) Mediterranean
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
 * Contact: mediterranean@users.sourceforge.net
 **/

package net.sf.xmm.moviemanager.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.commands.CommandDialogDispose;
import net.sf.xmm.moviemanager.commands.MovieManagerCommandOpenPage;
import net.sf.xmm.moviemanager.gui.imdb.DialogAboutIMDbLib;
import net.sf.xmm.moviemanager.imdblib.IMDbLib;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.Localizer;
import net.sf.xmm.moviemanager.util.SysUtil;

public class DialogAbout extends JDialog {

	/**
	 * The Constructor.
	 **/
	public DialogAbout() {
		/* Dialog creation...*/
		super(MovieManager.getDialog());

		GUIUtil.enableDisposeOnEscapeKey(this);

		/* Dialog properties...*/
		setTitle(Localizer.get("DialogAbout.title")); //$NON-NLS-1$
		setModal(true);
		setResizable(false);


		JPanel about = createAboutContent();
		JPanel aboutIMdb = DialogAboutIMDbLib.createAboutPanel();
		JPanel system = createSystemPanel();

		JTabbedPane aboutTabs = new JTabbedPane();
		aboutTabs.add(about, " About ");
		aboutTabs.add(aboutIMdb, "IMDb Library");
		aboutTabs.add(system, "System Info");

		JPanel panelButtons = createButtonsPanel();

		/* Adds all and buttonsPanel... */
		getContentPane().add(aboutTabs,BorderLayout.CENTER);
		getContentPane().add(panelButtons,BorderLayout.SOUTH);
		/* Packs and sets location... */
		
		pack();

		setLocation((int)MovieManager.getIt().getLocation().getX()+(MovieManager.getIt().getWidth()-getWidth())/2,
				(int)MovieManager.getIt().getLocation().getY()+(MovieManager.getIt().getHeight()-getHeight())/2);
	}


    JPanel createButtonsPanel() {
    	/* Buttons panel...*/
    	JPanel panelButtons = new JPanel();
    	panelButtons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    	panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
    	JButton buttonOk = new JButton(Localizer.get("DialogAbout.button-close.title")); //$NON-NLS-1$
    	buttonOk.setToolTipText(Localizer.get("DialogAbout.button-close.tooltip")); //$NON-NLS-1$
    	buttonOk.setActionCommand("About - OK"); //$NON-NLS-1$
    	buttonOk.addActionListener(new CommandDialogDispose(this));
    	panelButtons.add(buttonOk);
    	return panelButtons;
    }
    
    static JPanel createAboutContent() {

    	/* Info panel...*/
    	JPanel panelInfo = new JPanel();
    	panelInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogAbout.panel-info.title")), //$NON-NLS-1$
    			BorderFactory.createEmptyBorder(5,5,5,5)));
    	JLabel labelInfo = new JLabel(" MeD's Movie Manager version "+MovieManager.getConfig().sysSettings.getVersion(), //$NON-NLS-1$
    			new ImageIcon(FileUtil.getImage("/images/filmFolder.png").getScaledInstance(55,55,Image.SCALE_SMOOTH)), //$NON-NLS-1$
    			JLabel.CENTER);
    	labelInfo.setFont(new Font(labelInfo.getFont().getName(),Font.PLAIN,labelInfo.getFont().getSize()));
    	panelInfo.add(labelInfo);
    	/* Copyright panel... */
    	JPanel panelCopyright = new JPanel();
    	panelCopyright.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()," Copyright "), //$NON-NLS-1$
    			BorderFactory.createEmptyBorder(5,5,5,5)));
    	JLabel labelCopyright = new JLabel("(C) 2003-2010 Mediterranean, Bro",JLabel.CENTER); //$NON-NLS-1$
    	labelCopyright.setFont(new Font(labelCopyright.getFont().getName(),Font.PLAIN,labelCopyright.getFont().getSize()));
    	panelCopyright.add(labelCopyright);
    	/* Developers panel... */
    	JPanel panelDevelopers = new JPanel();
    	panelDevelopers.setLayout(new BorderLayout());
    	panelDevelopers.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogAbout.panel-developers.title")), //$NON-NLS-1$
    			BorderFactory.createEmptyBorder(0,5,5,5)));
    	JLabel labelDevelopers = new JLabel("<html>Mediterranean, Bro</html>",JLabel.CENTER); //$NON-NLS-1$
    	labelDevelopers.setFont(new Font(labelDevelopers.getFont().getName(),Font.PLAIN,labelDevelopers.getFont().getSize()));
    	JLabel labelContributers = new JLabel("<html><center>Contributors:</center><br>olba2, Steven, kreegee Matthias Ihmig, Johannes Adams</html>",JLabel.CENTER); //$NON-NLS-1$
    	labelContributers.setFont(new Font(labelContributers.getFont().getName(),Font.PLAIN, labelContributers.getFont().getSize()));

    	panelDevelopers.add(labelDevelopers, BorderLayout.NORTH);
    	panelDevelopers.add(labelContributers, BorderLayout.SOUTH);

    	/* Licenses panel... */
    	JPanel panelLicenses = new JPanel();
    	panelLicenses.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogAbout.panel-license.title")), //$NON-NLS-1$
    			BorderFactory.createEmptyBorder(5,5,5,5)));
    	JLabel labelLicense = new JLabel(Localizer.get("DialogAbout.panel-license.text"),JLabel.CENTER); //$NON-NLS-1$
    	labelLicense.setFont(new Font(labelLicense.getFont().getName(),Font.PLAIN,labelLicense.getFont().getSize()-2));
    	labelLicense.addMouseListener(new MovieManagerCommandOpenPage("http://www.fsf.org/licenses/info/GPLv2.html")); //$NON-NLS-1$
    	panelLicenses.add(labelLicense);

    	/* All stuff together... */
    	JPanel all = new JPanel();
    	all.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    	all.setLayout(new BoxLayout(all,BoxLayout.Y_AXIS));
    	all.add(panelInfo);
    	all.add(panelCopyright);
    	all.add(panelDevelopers);
    	all.add(panelLicenses);
    	
    	return all;
    }
    
    JPanel createSystemPanel() {
    	/* System panel... */
    	JPanel panelSystem = new JPanel();
    	panelSystem.setLayout(new BorderLayout());
    	
    	//panelSystem.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8,8,8,8), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),Localizer.get("DialogAbout.panel-system.title")), //$NON-NLS-1$
    	//		BorderFactory.createEmptyBorder(5,5,5,5))));
    	/*JLabel labelSystem = new JLabel("<html>" +  //$NON-NLS-1$
    			SysUtil.getSystemInfo("<br>") + //$NON-NLS-1$
    			"</html>",JLabel.CENTER); //$NON-NLS-1$

    	panelSystem.add(labelSystem);
*/    	
    	int freeMemory = (int) Runtime.getRuntime().freeMemory()/1024/1024;
    	int totalMemory = (int) Runtime.getRuntime().totalMemory()/1024/1024;
    	int maxMemory = (int) Runtime.getRuntime().maxMemory()/1024/1024;
    	
    	String mediaInfoLibVersion = SysUtil.getMediaInfoLibVersion();
    	
    	String[][] data = new String[][] {
    			{"Operating System: ",  System.getProperty("os.name")},
    			{"OS version:", System.getProperty("os.version")},
    			{"Architecture: ", System.getProperty("os.arch")},
    			{"Java version: ", System.getProperty("java.runtime.version")},
    			{"Vendor:", System.getProperty("java.vm.specification.vendor")},
    			{"Free VM memory: ", freeMemory + "MB"},
    			{"Total VM memory: ", totalMemory + "MB"},
    			{"Max VM memory: ", maxMemory + "MB"},
    			{"MovieManager release:", "" + MovieManager.getConfig().sysSettings.getRelease()},
    			{"IMDb Lib release:", "" + IMDbLib.getRelease() + " (" + IMDbLib.getVersion() + ")"},
    			{"MediaInfoLib version:", (mediaInfoLibVersion == null ? "Not available" : mediaInfoLibVersion)}
    	};
    	
    	DefaultTableModel model = new DefaultTableModel();
    	String [] title = new String[] {"System information", ""};
    	model.setDataVector(data, title);
        JTable table = new JTable(model);
        
        // Avoid white background
        table.setBackground(panelSystem.getBackground());
        
        JPanel tablePanel = new JPanel();
                
        tablePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8,8,8,8) , BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " System Info "), //$NON-NLS-1$
    			BorderFactory.createEmptyBorder(5,5,5,5))));
    	
        
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(table, BorderLayout.CENTER);
        
        JScrollPane scroll = new JScrollPane(tablePanel);
        panelSystem.add(scroll, BorderLayout.CENTER);
    	return panelSystem;
    }
}
