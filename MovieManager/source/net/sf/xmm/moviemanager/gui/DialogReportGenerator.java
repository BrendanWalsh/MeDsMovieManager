package net.sf.xmm.moviemanager.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.view.JRViewer;
import net.sf.xmm.moviemanager.MovieManager;
import net.sf.xmm.moviemanager.database.Database;
import net.sf.xmm.moviemanager.models.ModelEntry;
import net.sf.xmm.moviemanager.models.ModelEpisode;
import net.sf.xmm.moviemanager.models.ModelMovie;
import net.sf.xmm.moviemanager.util.FileUtil;
import net.sf.xmm.moviemanager.util.GUIUtil;
import net.sf.xmm.moviemanager.util.SysUtil;
import net.sf.xmm.moviemanager.util.tools.ReportGeneratorDataSource;

import org.apache.log4j.Logger;

/**
 * ReportGenerator using JasperReports. Current entries in movielist is used as
 * data.
 *
 * @author olba2
 */
public class DialogReportGenerator extends JFrame implements ActionListener, WindowListener, FilenameFilter, ListSelectionListener {
    private static final int PREVIEW_MARGIN = 20;
    private static DialogReportGenerator instance = null;

    private LayoutItem selectedLayout = null;
    private ReportGeneratorDataSource ds = null;

    private JLabel labelProgress = new JLabel();
    private JLabel labelExample = new JLabel();
    private JLabel labelDescription = new JLabel();
    private JPanel jPanel1 = new JPanel();
    private JPanel jPanel2 = new JPanel();
    private JPanel jPanel3 = new JPanel();
    private JPanel jPanel4 = new JPanel();
    private JPanel jPanel5 = new JPanel();
    private JPanel jPanel6 = new JPanel();
    private JPanel jPanelReportTitle = new JPanel();
    private JPanel panelOptions = new JPanel();
    private JPanel panelProgress = new JPanel();
    private JPanel panelReport = new JPanel();
    private JButton buttonAction = new JButton();
    private JButton buttonClose = new JButton();
    private JList layoutList = new JList();
    private JRadioButton radioButtonCurrentList = new JRadioButton();
    private JRadioButton radioButtonSelectedMovies = new JRadioButton();
    private JRadioButton radioButtonAllMovies = new JRadioButton();
    private ButtonGroup buttonGroup = new ButtonGroup();
    private JCheckBox checkBoxEpisodes = new JCheckBox();
    private JSplitPane splitPane = new JSplitPane();
    private JScrollPane scrollPaneList = new JScrollPane();
    private JProgressBar progressBar = new JProgressBar();
    private BorderLayout borderLayout1 = new BorderLayout();
    private BorderLayout borderLayout2 = new BorderLayout();
    private BorderLayout borderLayout3 = new BorderLayout();
    private BorderLayout borderLayout4 = new BorderLayout();
    private BorderLayout borderLayout5 = new BorderLayout();
    private CardLayout cardLayout1 = new CardLayout();
    private FlowLayout flowLayout1 = new FlowLayout();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private GridLayout gridLayout1 = new GridLayout();
    private Border border1 = BorderFactory.createEmptyBorder(0, 3, 10, 3);
    private Border border2 = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
        " Content ",
        TitledBorder.DEFAULT_JUSTIFICATION,
        TitledBorder.DEFAULT_POSITION,
        new Font(layoutList.getFont().getName(), Font.BOLD, layoutList.getFont().getSize())),
        BorderFactory.createEmptyBorder(0, 5, 5, 5));
    private Border border3 = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
        " Layout ",
        TitledBorder.DEFAULT_JUSTIFICATION,
        TitledBorder.DEFAULT_POSITION,
        new Font(layoutList.getFont().getName(), Font.BOLD, layoutList.getFont().getSize())),
        BorderFactory.createEmptyBorder(0, 5, 5, 5));
    
    
    private Border reportTitleBorder = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
            " Report Title ",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font(layoutList.getFont().getName(), Font.BOLD, layoutList.getFont().getSize())),
            BorderFactory.createEmptyBorder(0, 5, 5, 5));
        
    
    private Border border4 = BorderFactory.createEmptyBorder(0, 5, 0, 3);

    private static String reportsDir = getReportsDir();
    
    JTextField reportTitleField = new JTextField();
    
    public String getReportTitle() {
    	if (reportTitleField.getText().trim().equals(""))
    		return null;
    	else
    		return reportTitleField.getText();
    }
    
    public DialogReportGenerator() {
        this(null);
    }

    private static String getReportsDir() {
    	if (SysUtil.isMacAppBundle()) { 
    		return "reports/";
    	}
    	return SysUtil.getUserDir() + "/reports/";
	}

	public DialogReportGenerator(Frame frame) {
        if (instance != null) { // on second instantiation, just show the first
            instance.setState(Frame.NORMAL);
            instance.toFront();
        }
        else {
            setTitle( (frame != null ? frame.getTitle() : "") + " - Report Generator");
            try {
                jbInit();
                setLocation(50, 50);
                setSize(640, 480);
                GUIUtil.show(this, true);
                loadReportLayouts();
            }
            catch (Exception ex) {
                Logger.getRootLogger().error("Error initializing Report Generator", ex);
            }
            instance = this;
        }
    }

    /**
     * jbInit - sets up window content
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {
        setIconImage(MovieManager.getDialog().getIconImage());
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                this_componentResized(e);
            }
        });
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(this);
        buttonClose.setText("Close");
        buttonClose.addActionListener(this);
        buttonAction.setText("Generate");
        buttonAction.addActionListener(this);
        buttonAction.setEnabled(false);
        panelOptions.setLayout(borderLayout1);
        jPanel2.setLayout(borderLayout2);
        jPanel3.setLayout(borderLayout3);
        jPanel4.setLayout(cardLayout1);
        panelReport.setLayout(borderLayout4);
        jPanel1.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        panelProgress.setLayout(gridBagLayout1);
        labelExample.setBackground(UIManager.getColor("controlShadow"));
        labelExample.setForeground(UIManager.getColor("controlHighlight"));
        labelExample.setOpaque(true);
        labelExample.setHorizontalAlignment(SwingConstants.CENTER);
        labelDescription.setBackground(UIManager.getColor("controlShadow"));
        labelProgress.setText("Generating report... Please wait");
        layoutList.addListSelectionListener(this);
        labelDescription.setBorder(border1);
        labelDescription.setOpaque(true);
        labelDescription.setHorizontalAlignment(SwingConstants.CENTER);
        labelDescription.setText(" ");
        jPanel5.setBorder(border2);
        jPanel5.setLayout(gridLayout1);
        
        jPanelReportTitle.setBorder(reportTitleBorder);
        
        
        radioButtonCurrentList.setText("Current movielist");
        radioButtonSelectedMovies.setText("Selected movies");
        radioButtonAllMovies.setText("All movies");
        radioButtonCurrentList.setSelected(true);
        buttonGroup.add(radioButtonCurrentList);
        buttonGroup.add(radioButtonSelectedMovies);
        buttonGroup.add(radioButtonAllMovies);
        checkBoxEpisodes.setText("Include episodes");
        checkBoxEpisodes.setSelected(true);
        gridLayout1.setRows(4);
        jPanel6.setLayout(borderLayout5);
        jPanel6.setBorder(border3);
        jPanel3.setBorder(border4);
        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
        jPanel1.add(buttonAction);
        jPanel1.add(buttonClose);
        splitPane.add(jPanel3, JSplitPane.LEFT);
        splitPane.add(jPanel2, JSplitPane.RIGHT);
        splitPane.setDividerLocation(200);
        panelOptions.add(splitPane, java.awt.BorderLayout.CENTER);
        jPanel4.add(panelOptions, "options");
        jPanel4.add(panelProgress, "progress");
        jPanel4.add(panelReport, "report");
        getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);
        panelProgress.add(progressBar, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 30, 0, 30), 0, 0));
        panelProgress.add(labelProgress, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 30, 10, 30), 0, 0));
        jPanel2.add(labelExample, java.awt.BorderLayout.CENTER);
        jPanel2.add(labelDescription, java.awt.BorderLayout.SOUTH);
        
              
        
        jPanel5.add(radioButtonCurrentList);
        jPanel5.add(radioButtonSelectedMovies);
        jPanel5.add(radioButtonAllMovies);
        jPanel5.add(checkBoxEpisodes);
        
        jPanelReportTitle.setLayout(new BorderLayout());
        reportTitleField = new JTextField();
        jPanelReportTitle.add(reportTitleField, BorderLayout.CENTER);
        
        JPanel listOptions = new JPanel();
        listOptions.setLayout(new BoxLayout(listOptions, BoxLayout.Y_AXIS));
        listOptions.add(jPanel5);
        listOptions.add(jPanelReportTitle);
        
        jPanel3.add(listOptions, java.awt.BorderLayout.SOUTH);
        
        scrollPaneList.getViewport().add(layoutList);
        jPanel6.add(scrollPaneList, java.awt.BorderLayout.CENTER);
        jPanel3.add(jPanel6, java.awt.BorderLayout.CENTER);
    }

    /**
     * loadReportLayouts - scans "reports" directory for jasper files and shows
     * the filenames in the JList.
     */
    private void loadReportLayouts() {
        File reportDir = new File(reportsDir);
        
        String[] files = reportDir.list(this);
        
        if (files.length > 0) {
            LayoutItem[] layouts = new LayoutItem[files.length];
            for (int i = 0; i < files.length; i++) {
                layouts[i] = new LayoutItem(files[i]);
            }
            layoutList.setListData(layouts);
            layoutList.setSelectedIndex(0);
        }
    }

    /**
     * createReport - creates report using selected report layout
     *
     * @param panel JPanel - report preview will be added to this parent
     *   container.
     */
    private void createReport(final JPanel panel) {
        buttonAction.setText("Abort");
        cardLayout1.show(jPanel4, "progress");

        LinkedList<ModelEntry> movies = new LinkedList<ModelEntry>();
        boolean includeEpisodes = checkBoxEpisodes.isSelected();
        if (radioButtonAllMovies.isSelected()) { // load all movies from database
            labelProgress.setText("Loading movies... Please wait");
            progressBar.setValue(0);
            
            Database database = MovieManager.getIt().getDatabase();
            ArrayList<ModelMovie> movieList = database.getMoviesList();
            ArrayList<ModelEpisode> episodesList = includeEpisodes ? database.getEpisodeList() : null;
            
            for (int i = 0; i < movieList.size(); i++) {
                movies.add(movieList.get(i));
                if (includeEpisodes) { // add episodes
                    int tempKey = movieList.get(i).getKey();
                    for (int u = 0; u < episodesList.size(); u++) {
                        if (tempKey == ( (ModelEpisode) episodesList.get(u)).getMovieKey()) {
                            movies.add(episodesList.get(u));
                        }
                    }
                }
            }
        }
        else { // tree contains movies
            boolean onlySelected = radioButtonSelectedMovies.isSelected();
            JTree tree = MovieManager.getDialog().getMoviesList();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
            int n = root.getChildCount();
            for (int i = 0; i < n; i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
                boolean addnode = true;
                if (onlySelected) {
                    int row = tree.getRowForPath(new TreePath(node.getPath()));
                    if (!tree.isRowSelected(row)) {
                        addnode = false;
                    }
                }
                if (addnode) {
                    movies.add((ModelEntry) node.getUserObject());
                }
                if (includeEpisodes) {
                    int episodeCount = node.getChildCount();
                    for (int j = 0; j < episodeCount; j++) {
                        DefaultMutableTreeNode episodeNode = (DefaultMutableTreeNode) node.getChildAt(j);
                        addnode = true;
                        if (onlySelected) {
                            int row = tree.getRowForPath(new TreePath(episodeNode.getPath()));
                            if (!tree.isRowSelected(row)) {
                                addnode = false;
                            }
                        }
                        if (addnode) {
                            movies.add((ModelEntry) episodeNode.getUserObject());
                        }
                    }
                }
            }
        }

        try {
            labelProgress.setText("Generating report... Please wait");
            HashMap<String, String> parms = new HashMap<String, String>();
            parms.put("logo", FileUtil.getImageURL("/images/filmFolder.png").toString());
            
            ds = new ReportGeneratorDataSource(movies, getReportTitle(), selectedLayout.sortField, progressBar, FileUtil.getImageURL("/images/movie.png"), false);
            
            final JasperPrint print = JasperFillManager.fillReport(new File(reportsDir, selectedLayout.filename).getAbsolutePath(), parms, ds);
          
            if (ds != null) {
                ds = null;
                
                SwingUtilities.invokeLater(new Runnable() {
                	public void run() {

                		JRViewer viewerPanel = new JRViewer(print);
                		panel.removeAll();
                		panel.add(viewerPanel, BorderLayout.CENTER);
                		cardLayout1.show(jPanel4, "report");
                		viewerPanel.setFitWidthZoomRatio();
                		buttonAction.setText("Select Layout");
                	}
                });
            }
        }
        catch (Exception ex) {
        	 SwingUtilities.invokeLater(new Runnable() {
             	public void run() {
             		labelProgress.setText("Error generating report");
                    progressBar.setValue(0);
             	}
             });
            
            Logger.getRootLogger().error("Error generating report", ex);
        }
    }

   
    /**
     * updateExample - updates example. Call when window changes size or layout
     * selection changes
     */
    private void updateExample() {
        if (selectedLayout != null) {

            // display example
            if (selectedLayout.exampleImage != null) {
                ImageIcon icon = new ImageIcon(selectedLayout.exampleImage); // getting size directly from image was unreliable
                int height = labelExample.getHeight() - PREVIEW_MARGIN;
                int width = (height * icon.getIconWidth()) / icon.getIconHeight();
                if (width > labelExample.getWidth() - PREVIEW_MARGIN) {
                    width = labelExample.getWidth() - PREVIEW_MARGIN;
                    height = (width * icon.getIconHeight()) / icon.getIconWidth();
                }

                Image image = selectedLayout.exampleImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                icon = new ImageIcon(image);
                labelExample.setIcon(icon);
                labelExample.setText("");
            }
            else {
                labelExample.setIcon(null);
                labelExample.setText("No example");
            }

            labelDescription.setText(selectedLayout.description != null ? selectedLayout.description : "No description");
        }
        else {
            labelExample.setIcon(null);
            labelExample.setText("Select layout");
            labelDescription.setText(" ");
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonClose) {
            dispose();
        }
        else if (e.getSource() == buttonAction) {
            if (buttonAction.getText().equalsIgnoreCase("Generate")) {
                Thread t = new Thread() { // generate using thread - ensures update of progress bar
                    public void run() {
                        createReport(panelReport);
                    }
                };
                t.start();
            }
            else {
                if (ds != null) {
                    ds.interrupt();
                    ds = null;
                }
                panelReport.removeAll();
                cardLayout1.show(jPanel4, "options");
                buttonAction.setText("Generate");
            }
        }
    }

    public void windowOpened(WindowEvent e) {}

    public void windowClosing(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {
        if (ds != null) {
            ds.interrupt();
            ds = null;
        }
        instance = null;
    }

    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    /**
     * accept - filter for jasper filenames
     *
     * @param dir File - not used
     * @param name String - filename
     * @return boolean - true for accepted files
     */
    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith("jasper");
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (layoutList.getSelectedValue() != null) {
                selectedLayout = (LayoutItem) layoutList.getSelectedValue();
                selectedLayout.fetchInfo();
                buttonAction.setEnabled(true);
                checkBoxEpisodes.setSelected(selectedLayout.episodes);
            }
            else {
                selectedLayout = null;
                buttonAction.setEnabled(false);
            }
            updateExample();
        }
    }

    /**
     * this_componentResized - updates example when window size changes
     *
     * @param e ComponentEvent
     */
    public void this_componentResized(ComponentEvent e) {
        updateExample();
    }

    /**
     * class keeping track of filenames and displayed names
     */
    private class LayoutItem {
        String filename;
        private String displayname;
        private String examplename;
        private String sourcename;

        boolean infoFetched = false;
        Image exampleImage;
        String description;
        String sortField;
        boolean episodes;

        public LayoutItem(String filename) {
        	this.filename = filename;

        	if (filename.indexOf(".") != -1) {
        		String prefix = filename.substring(0, filename.lastIndexOf("."));
        		displayname = prefix.replaceAll("_", " ");
        		examplename = prefix + ".png";
        		sourcename = prefix + ".jrxml";
        	}
        }

        public String toString() {
            return displayname;
        }

        public void fetchInfo() {
        	
            if (!infoFetched) {
                // example image
                String filename = reportsDir + examplename;
                
                if (new File(filename).exists()) {
                    exampleImage = Toolkit.getDefaultToolkit().getImage(filename);
                }

                // xmm custom property values
                filename = reportsDir + sourcename;
                
                if (new File(filename).exists()) {
                    
                	try {
                		String bufferText = FileUtil.readFileToStringBuffer(filename).toString();
                        description = getCustomPropertyValue(bufferText, "xmm.description");
                        sortField = getCustomPropertyValue(bufferText, "xmm.sortfield");
                        String s = getCustomPropertyValue(bufferText, "xmm.episodes");
                        episodes = s != null ? s.equalsIgnoreCase("true") : false;
                    }
                    catch (Exception ex) {
                    }
                }
                infoFetched = true;
            }
        }

        private String getCustomPropertyValue(String bufferText, String name) {
            String searchText = "<property name=\"" + name + "\" value=\"";
            int pos = bufferText.indexOf(searchText);
            if (pos >= 0) {
                pos += searchText.length();
                int pos2 = bufferText.indexOf('"', pos);
                if (pos2 > pos) {
                    return bufferText.substring(pos, pos2);
                }
            }
            return null;
        }
    }

    /**
     * printDirect - print selected movies in tree directly using movie_details
     * layout
     *
     * @param tree - the movielist tree
     */
    public static void printDirect(JTree tree) {
        // list of selected movies
        LinkedList<ModelEntry> movies = new LinkedList<ModelEntry>();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        int n = root.getChildCount();
        for (int i = 0; i < n; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
            int row = tree.getRowForPath(new TreePath(node.getPath()));
            if (tree.isRowSelected(row)) {
                movies.add((ModelEntry) node.getUserObject());
            }
            int episodeCount = node.getChildCount();
            for (int j = 0; j < episodeCount; j++) {
                DefaultMutableTreeNode episodeNode = (DefaultMutableTreeNode) node.getChildAt(j);
                row = tree.getRowForPath(new TreePath(episodeNode.getPath()));
                if (tree.isRowSelected(row)) {
                    movies.add((ModelEntry) episodeNode.getUserObject());
                }
            }
        }

        // print it
        if (!movies.isEmpty()) {
            try {
                HashMap<String, String> parms = new HashMap<String, String>();
                parms.put("logo", FileUtil.getImageURL("/images/filmFolder.png").toString());
                ReportGeneratorDataSource ds = new ReportGeneratorDataSource(movies, null, "none", null, FileUtil.getImageURL("/images/movie.png"), false);
                JasperPrint print = JasperFillManager.fillReport(reportsDir + "movie_details.jasper", parms, ds);
                JasperPrintManager.printReport(print, true);
            }
            catch (JRException ex) {
                Logger.getRootLogger().error("Error printing report", ex);
            }
        }
    }
}
