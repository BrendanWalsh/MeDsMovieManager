package net.sf.xmm.moviemanager.swing.extentions;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

class MultiLineToolTipUI extends BasicToolTipUI {
    
    static MultiLineToolTipUI sharedInstance = new MultiLineToolTipUI();
    Font smallFont; 			     
    static JToolTip tip;
    protected CellRendererPane rendererPane;
	
    private static JTextArea textArea ;
	
    public static ComponentUI createUI(JComponent c) {
	return sharedInstance;
    }
	
    public MultiLineToolTipUI() {
	super();
    }
	
    public void installUI(JComponent c) {
	super.installUI(c);
	tip = (JToolTip)c;
	rendererPane = new CellRendererPane();
	c.add(rendererPane);
    }
	
    public void uninstallUI(JComponent c) {
	super.uninstallUI(c);
		
	c.remove(rendererPane);
	rendererPane = null;
    }
	
    public void paint(Graphics g, JComponent c) {
	
	Dimension size = c.getSize();
	
	textArea.setBackground(c.getBackground());
	rendererPane.paintComponent(g, textArea, c, 1, 1,
				    size.width - 1, size.height - 1, true);
    }
	
    public Dimension getPreferredSize(JComponent c) {
	String tipText = ((JToolTip)c).getTipText();
	
	if (tipText == null)
	    return new Dimension(0, 0);
	
	textArea = new JTextArea(tipText);
	rendererPane.removeAll();
	rendererPane.add(textArea );
	textArea.setWrapStyleWord(true);
	
	int width = ((JMultiLineToolTip)c).getFixedWidth();
	int columns = ((JMultiLineToolTip)c).getColumns();
	
	FontMetrics fontmetrics = textArea.getFontMetrics(textArea.getFont());
	
	String [] split = tipText.split(System.getProperty("line.separator"));
	
	/* Find the width of the widest line */
	for (int i = 0; i < split.length; i++) {
	    if (fontmetrics.stringWidth(split[i]) > width)
		width = fontmetrics.stringWidth(split[i]);
		//width = (int) fontmetrics.getStringBounds(split[i], textArea.getGraphics()).getWidth();
	}
	
	if(columns > 0)
	    {
		textArea.setColumns(columns);
		textArea.setSize(0,0);
		textArea.setLineWrap(true);
		textArea.setSize( textArea.getPreferredSize() );
	    }
	else if(width > 0)
	    {
		textArea.setLineWrap(true);
		Dimension d = textArea.getPreferredSize();
		d.width = width;
		d.height++;
		textArea.setSize(d);
	    }
	else
	    textArea.setLineWrap(false);
	
	Dimension dim = textArea.getPreferredSize();
		
	dim.height += 1;
	
	if (dim.width > 0)
	    dim.width += 10;
	
	return dim;
    }
	
    public Dimension getMinimumSize(JComponent c) {
	return getPreferredSize(c);
    }
	
    public Dimension getMaximumSize(JComponent c) {
	return getPreferredSize(c);
    }
}
