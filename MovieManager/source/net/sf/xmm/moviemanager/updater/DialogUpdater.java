package net.sf.xmm.moviemanager.updater;

import static com.panayotis.jupidator.i18n.I18N._;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.log4j.Logger;

import net.sf.xmm.moviemanager.MovieManager;

import com.panayotis.jupidator.ApplicationInfo;
import com.panayotis.jupidator.Updater;
import com.panayotis.jupidator.UpdaterException;
import com.panayotis.jupidator.data.TextUtils;
import com.panayotis.jupidator.data.UpdaterAppElements;
import com.panayotis.jupidator.gui.JupidatorGUI;
import com.panayotis.jupidator.gui.swing.AboutDialog;
import com.panayotis.jupidator.loglist.LogItem;
import com.panayotis.jupidator.loglist.LogList;
import com.panayotis.jupidator.loglist.creators.HTMLCreator;

public class DialogUpdater extends JDialog implements JupidatorGUI, HyperlinkListener {

	static Logger log = Logger.getLogger(AppUpdater.class);
	
	private Updater callback;
	
	/** Creates new form SwingGUI */
	public DialogUpdater() {
		super(MovieManager.getDialog(), false);
		initComponents();
		remindButton.requestFocus();
	}

	public void hyperlinkUpdate(HyperlinkEvent event) {

		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			callback.actionLinkClicked(event.getDescription());
		}
	}
	
		
	public void setInformation(Updater callback, UpdaterAppElements el, ApplicationInfo info) throws UpdaterException {
		this.callback = callback;
		
		setTitle("Updates are available!");
		newVersionLabel.setText(_("Updates are available for {0}", el.getAppName()));
		//VersInfoL.setText(_("{0} version {1} is now available - you have {2}.", el.getAppName(), el.getNewVersion(), info.getVersion()));
		//VersInfoL.setText(_("<html>{0} version <b>{1}</b> is now available <br> Your current version is <b>{2}</b>.</html>", 
		//		el.getAppName(), el.getNewVersion(), info.getVersion()));
		
		// If any of the can be downloaded
		LogList list = el.getLogList();
		
		boolean canDownload = false;
		boolean noUpdates = false;
		
		if (list.size() == 0) {
			list.add(new LogItem("No updates are currently available."));
			noUpdates = true;
		}
		else {
			
			for (LogItem t : list) {
				if (t.getAvailable())
					canDownload = true;
			}
		}
		
		installUpdatesButton.setEnabled(canDownload);
		
		InfoPane.setContentType("text/html");
		InfoPane.setText(HTMLCreator.getList(el.getLogList(), "http://xmm.sourceforge.net/index.php?menu=download"));
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				InfoPane.scrollRectToVisible(new Rectangle(1,1,1,1));
			}
		});

		
		if (noUpdates) {
			setTitle("No available updates");
			newVersionLabel.setText(_("You have latest version of {0}", el.getAppName()));
			skipThisVersionButton.setEnabled(false);
			
			buttonPanel.removeAll();
			buttonPanel.add(createNoUpdatesButtonsPanel(), BorderLayout.CENTER);
		}
		
		try {
			URL icon = new URL(el.getIconpath());
					
			if (icon != null) {
				IconL.setIcon(new ImageIcon(icon));
			}
		} catch (MalformedURLException ex) {
			throw new UpdaterException("Unable to load  icon " + ex.getMessage());
		}

		if (info.isSelfUpdate())
			skipThisVersionButton.setVisible(false);
	}

	
	
	public void startDialog() {
		log.debug("DialogUpdater - startDialog");
		setLocationRelativeTo(MovieManager.getDialog());
		setVisible(true);
	}

	public void endDialog() {
		setVisible(false);
		dispose();
	}

	public void setIndetermined() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ActionB.setEnabled(false);
				PBar.setIndeterminate(true);
				PBar.setToolTipText(_("Processing update"));
				PBar.setString("");
				InfoL.setText(_("Deploying files..."));
			}
		});
	}

	public void errorOnCommit(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setInfoArea(message);
				InfoL.setForeground(Color.RED);
				ProgressP.revalidate();
			}
		});
	}

	public void successOnCommit() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setInfoArea(_("Successfully downloaded updates"));
				ActionB.setText(_("Restart application"));
				ActionB.setActionCommand("restart");
				ProgressP.revalidate();
			}
		});
	}

	private void setInfoArea(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ActionB.setEnabled(true);
				BarPanel.remove(PBar);
				ProgressP.remove(InfoL);
				BarPanel.add(InfoL);
				InfoL.setText(message);
			}
		});
	}

	public void setDownloadRatio(final String ratio, final float percent) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				PBar.setValue(Math.round(percent * 100));
				PBar.setToolTipText(_("Download speed: {0}", ratio));
				PBar.setString(ratio);
			}
		});
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
	
		BarPanel = new JPanel();
		PBar = new JProgressBar();
		progressButtonPanel = new JPanel();
		ActionB = new JButton();
		InfoL = new JLabel();
		MainPanel = new JPanel();
		jPanel6 = new JPanel();
		jScrollPane1 = new JScrollPane();
		InfoPane = new JEditorPane();
		jPanel5 = new JPanel();
		VersInfoL = new JLabel();
		NotesL = new JLabel();
		
		jPanel1 = new JPanel();
		
		IconL = new JLabel();
		
		ProgressP = createProgressPanel();
		
		MainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		MainPanel.setMinimumSize(new Dimension(550, 400));
		MainPanel.setPreferredSize(new Dimension(550, 400));
		MainPanel.setLayout(new BorderLayout());

		jPanel6.setLayout(new BorderLayout());

		InfoPane.setEditable(false);
		jScrollPane1.setViewportView(InfoPane);
		InfoPane.addHyperlinkListener(this);

		
		jPanel6.add(jScrollPane1, BorderLayout.CENTER);

		jPanel5.setLayout(new BorderLayout());
		jPanel5.add(VersInfoL, BorderLayout.CENTER);

		
		NotesL.setFont(NotesL.getFont().deriveFont(NotesL.getFont().getStyle() | Font.BOLD));
		NotesL.setText(_("Release Notes"));
		NotesL.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
		jPanel5.add(NotesL, BorderLayout.SOUTH);

		
		newVersionLabel = new JLabel();
		newVersionLabel.setFont(newVersionLabel.getFont().deriveFont(newVersionLabel.getFont().getStyle() | Font.BOLD, newVersionLabel.getFont().getSize()+1));
		newVersionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		jPanel1.add(newVersionLabel, BorderLayout.CENTER);
		
		infoButton = createInfoButton();
		jPanel1.add(infoButton, java.awt.BorderLayout.EAST);
		
		jPanel5.add(jPanel1, BorderLayout.NORTH);
		jPanel6.add(jPanel5, BorderLayout.PAGE_START);

		MainPanel.add(jPanel6, BorderLayout.CENTER);

		IconL.setVerticalAlignment(SwingConstants.TOP);
		IconL.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(IconL, BorderLayout.NORTH);
		
		checkForUpdates = new JCheckBox("Check for updates");
		leftPanel.add(checkForUpdates, BorderLayout.SOUTH);
		
		checkForUpdates.setSelected(true);
		checkForUpdates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				callback.actionCheckForUpdates(checkForUpdates.isSelected());
			}
		});
		
		MainPanel.add(leftPanel, BorderLayout.LINE_START);
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(createDefaultButtonsPanel(), BorderLayout.CENTER);
			
		MainPanel.add(buttonPanel, BorderLayout.SOUTH);

		getContentPane().add(MainPanel, BorderLayout.CENTER);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	
	JPanel createProgressPanel() {
		
		JPanel ProgressP = new JPanel();
		ProgressP.setLayout(new BorderLayout());

		BarPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 0));
		BarPanel.setLayout(new BorderLayout());

		PBar.setStringPainted(true);
		BarPanel.add(PBar, BorderLayout.CENTER);

		ProgressP.add(BarPanel, BorderLayout.CENTER);

		progressButtonPanel.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 8));
		progressButtonPanel.setLayout(new BorderLayout());

		ActionB.setText(_("Cancel"));
		ActionB.setActionCommand("cancel");
		ActionB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				ActionBActionPerformed(evt);
			}
		});
		progressButtonPanel.add(ActionB, BorderLayout.CENTER);

		ProgressP.add(progressButtonPanel, BorderLayout.EAST);

		InfoL.setText(_("Downloading..."));
		ProgressP.add(InfoL, BorderLayout.LINE_START);
	
		return ProgressP;
	}
	
	JPanel createDefaultButtonsPanel() {
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());

		buttonPanelLeft = new JPanel();
		buttonPanelLeft.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
				
		skipThisVersionButton = new JButton();
		skipThisVersionButton.setText(_("Skip this version"));
		skipThisVersionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				SkipBActionPerformed(evt);
			}
		});
		
		buttonPanelLeft.add(skipThisVersionButton);
		
		closeButton = createCloseButton();
		buttonPanelLeft.add(closeButton);
		
		
		buttonPanel.add(buttonPanelLeft, BorderLayout.WEST);

		buttonPanelRight = new JPanel();
		buttonPanelRight.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
		//buttonPanelRight.setLayout(new GridLayout(1, 2, 4, 0));

		remindButton = new JButton();
		remindButton.setText(_("Remind me later"));
		remindButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				LaterBActionPerformed(evt);
			}
		});
		buttonPanelRight.add(remindButton);

		installUpdatesButton = new JButton();
		installUpdatesButton.setText(_("Install Update"));
		installUpdatesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				UpdateBActionPerformed(evt);
			}
		});
		buttonPanelRight.add(installUpdatesButton);

		buttonPanel.add(buttonPanelRight, BorderLayout.EAST);

		return buttonPanel;
	}
	
	JButton createInfoButton() {
		
		JButton infoButton = new JButton();
		infoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/i.png"))); // NOI18N
		infoButton.setBorderPainted(false);
		infoButton.setMaximumSize(new java.awt.Dimension(20, 20));
		infoButton.setMinimumSize(new java.awt.Dimension(20, 20));
		infoButton.setPreferredSize(new java.awt.Dimension(20, 20));
		infoButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				InfoBActionPerformed(evt);
			}
		});
		
		return infoButton;
	}
	
	JPanel createNoUpdatesButtonsPanel() {
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
		buttonPanel.setLayout(new BorderLayout());

		closeButton = createCloseButton();
		buttonPanel.add(closeButton, BorderLayout.EAST);

		return buttonPanel;
	}
	
	JButton createCloseButton() {
		JButton closeButton = new JButton();
		closeButton.setText("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});
		
		return closeButton;
	}

	
	private void UpdateBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_UpdateBActionPerformed
		
		if (callback.actionCommit()) { 
			buttonPanel.setVisible(false);
			ProgressP.setVisible(true);
			MainPanel.add(ProgressP, BorderLayout.SOUTH);
		}
	}//GEN-LAST:event_UpdateBActionPerformed

	private void LaterBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_LaterBActionPerformed
		callback.actionDefer();
	}//GEN-LAST:event_LaterBActionPerformed

	private void SkipBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_SkipBActionPerformed
		callback.actionIgnore();
	}//GEN-LAST:event_SkipBActionPerformed

	private void ActionBActionPerformed(ActionEvent evt) {//GEN-FIRST:event_ActionBActionPerformed
		ActionB.setEnabled(false);
				
		if (ActionB.getActionCommand().startsWith("c")) {
			callback.actionCancel();
		}
		else {
			callback.actionRestart();				
		}
	}//GEN-LAST:event_ActionBActionPerformed

	private void InfoBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InfoBActionPerformed
		new AboutDialog(this).setVisible(true);
	}//G
	
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JButton ActionB;
	private JPanel BarPanel;
	private JPanel progressButtonPanel;
	private JPanel buttonPanel;
	private JLabel IconL;
	
	JCheckBox checkForUpdates;
	JButton closeButton;
	
	private JLabel InfoL;
	private JButton infoButton;
	private JEditorPane InfoPane;
	private JButton remindButton;
	private JPanel MainPanel;
	private JLabel newVersionLabel;
	private JLabel NotesL;
	private JProgressBar PBar;
	private JPanel ProgressP;
	private JButton skipThisVersionButton;
	private JButton installUpdatesButton;
	private JLabel VersInfoL;
	private JPanel jPanel1;
	private JPanel buttonPanelLeft;
	private JPanel buttonPanelRight;
	private JPanel jPanel5;
	private JPanel jPanel6;
	private JScrollPane jScrollPane1;
	// End of variables declaration//GEN-END:variables

	public void setProperty(String key, String value) {		
		
		if (key.toLowerCase().equals("about")) {
			infoButton.setVisible(TextUtils.isTrue(value));
		}
		else if (key.toLowerCase().equals("checkforupdates")) {
			checkForUpdates.setVisible(TextUtils.isTrue(value));
		}
		else if (key.toLowerCase().equals("checkforupdatesselected")) {
			checkForUpdates.setSelected(TextUtils.isTrue(value));
		}
		else if (key.toLowerCase().equals("modal")) {
			setModal(TextUtils.isTrue(value));
		}
		else if (key.toLowerCase().equals("resizable")) {
			setResizable(TextUtils.isTrue(value));
		}
		else if (key.toLowerCase().equals("closebutton")) {
			closeButton.setVisible(TextUtils.isTrue(value));
		}
		else if (key.toLowerCase().equals("diposeonclose")) {
			
			if (TextUtils.isTrue(value))
				setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			else
				setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}
		else if (key.toLowerCase().equals("disposeonescape")) {
			/*Enables dispose when pushing escape*/
			KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
			Action escapeAction = new AbstractAction()  {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			};
			getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
			getRootPane().getActionMap().put("ESCAPE", escapeAction);
		}
	}
}