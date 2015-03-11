package net.sf.xmm.moviemanager.swing.util;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.BalloonTip.AttachLocation;
import net.java.balloontip.BalloonTip.Orientation;
import net.java.balloontip.styles.ModernBalloonStyle;
import net.sf.xmm.moviemanager.util.SysUtil;

public class KeyboardShortcutManager {

	static Logger log = Logger.getLogger(KeyboardShortcutManager.class);
	
	LinkedHashMap<String, KeyMapping> map = new LinkedHashMap<String, KeyMapping>();
	
	JDialog jDialog = null;
	JFrame jFrame = null;
	JRootPane rootPane;
	
	String macCmdChar = "@";
	String macCmdCharacter = "&#8984;";
	
	JComponent tooltipAreaComponent;
	
	ModernBalloonStyle toolTipLook = null;
	BalloonTip bTip = null;	
	
	public KeyboardShortcutManager(JDialog window) {
		jDialog = window;
		rootPane = window.getRootPane();
	}
	
	public KeyboardShortcutManager(JFrame window) {
		jFrame = window;
		rootPane = window.getRootPane();
	}
	
	public boolean isJDialog() {
		return jDialog != null;
	}
	
	public boolean isJFrame() {
		return jFrame != null;
	}
	
	public JDialog getJDialog() {
		return jDialog;
	}
	
	public JFrame getJFrame() {
		return jFrame;
	}
	
	public void setKeysToolTipComponent(JComponent c) {
		tooltipAreaComponent = c;
	}
	
	public boolean isShortCutPanelVisible() {
		
		if (bTip == null)
			return false;
			
		return bTip.isVisible();
	}
	
	public void hideShortCutPanel() {
		
		if (bTip != null)
			bTip.setVisible(false);
	}
	
	public static int getToolbarShortcutMask() {
		
		int value = InputEvent.ALT_MASK;
		
		if (SysUtil.isMac())
			value = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
			
		return value;
	}
	
	public void registerShowKeysKey() {
				
		try {
			registerKeyboardShortcut(
					KeyStroke.getKeyStroke(KeyEvent.VK_K, getToolbarShortcutMask()),
					"Show available shortcuts for this window", new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					
					if (tooltipAreaComponent != null) {
						
						// No shortcuts available for this dialog
						if (getShortCutsString() == null)
							return;
											
						if (bTip == null) {
							toolTipLook = new ModernBalloonStyle(13, 13, new Color(153, 153, 255), new Color(255, 255, 255), new Color(102, 102, 255));
							bTip = new BalloonTip(tooltipAreaComponent, getShortCutsString(), toolTipLook, Orientation.LEFT_ABOVE, AttachLocation.NORTHWEST, 0, 0, true);
							bTip.setCloseButtonActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									bTip.setVisible(false);
								}
							});
							bTip.setVisible(false);
						}
						
						bTip.setVisible(!bTip.isVisible());
					}
				}
			});
		} catch (Exception e) {
			log.warn("Exception:" + e.getMessage(), e);
		}
	}
	
	
	public KeyMapping registerKeyboardShortcut(KeyStroke key, String shortcutString, Action action, JComponent tooltipComponent) throws Exception {
		
		KeyMapping mapping = registerKeyboardShortcut(key, shortcutString, action);
						
		if (tooltipComponent != null) {
			// Adding shortcut explanation to tooltip
			String tooltip = tooltipComponent.getToolTipText();

			if (tooltip == null)
				tooltip = mapping.getDisplayName();
			else
				tooltip += " - " + mapping.getDisplayName();

			tooltip = "<html>" + tooltip + "</html>";
			
			tooltipComponent.setToolTipText(tooltip); //$NON-NLS-1$
		}
		
		return mapping;
	}
	
	public KeyMapping registerKeyboardShortcut(KeyStroke key, String shortcutString, Action action) throws Exception {
				
		if (map.containsKey(key.toString())) {
			log.warn("Already contains shortcut:" + key);
			throw new Exception("Already contains shortcut:" + key);
		}
		
		KeyMapping keyMapping = new KeyMapping(key, shortcutString);
		map.put(key.toString(), keyMapping);
		
		registerKeyboardShortcut(key, action, rootPane);
		return keyMapping;
	}

	public static void registerKeyboardShortcut(KeyStroke key, final Action action, JRootPane rootPane) {
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, key.toString()); //$NON-NLS-1$
		rootPane.getActionMap().put(key.toString(), action); //$NON-NLS-1$
	}
	
	
	public class KeyMapping {
		
		KeyStroke key;
		String shortcutString;
		
		KeyMapping(KeyStroke key, String shortcutString) {
			this.key = key;
			this.shortcutString = shortcutString;
		}
				
		public String getDisplayName() {
			
			String name = "";
			
			if ((key.getModifiers() & InputEvent.CTRL_MASK) != 0) {
				name += "CTRL";
			}
			if ((key.getModifiers() & InputEvent.ALT_MASK) != 0) {
				
				if (name.length() > 0)
					name += "+";
				
				name += "ALT";
			}
			if ((key.getModifiers() & InputEvent.META_MASK) != 0) {
				
				if (name.length() > 0)
					name += "+";
				
				if (SysUtil.isMac()){
					name += macCmdCharacter;
				}
				else
					name += "META";
			}
			
			if (name.length() > 0)
				name += "+";
						
			if (key.getKeyCode() == KeyEvent.VK_ESCAPE) {
				name += "Escape";
			}
			else if (key.getKeyChar() < 128) {
				name += key.getKeyChar();
			}// Invalid character, use code instead
			else if (key.getKeyCode() < 128) {
				name += (char) key.getKeyCode();
			}		
			
			return name;
		}
	}

		
	String getShortCutsString() {
		
		// only K is registered
		if (map.size() == 1)
			return null;
			
		StringBuffer text = new StringBuffer();
		
		text.append("<html>");
				
		text.append("<head><style type='text/css'>");
		text.append("body {color:#444444;}");
		text.append("</style></head>");
                
        text.append("<table>");
        
		text.append("<tr>");
		text.append("<th colspan=3>").append("<font size=4>Keyboard shortcuts</font>").append("</th>");
		text.append("</tr>");
				
		Collection<KeyMapping> c = map.values();
		
		for (KeyMapping k : c) {
			text.append("<tr>");
			text.append("<td>").append(k.getDisplayName()).append("</td><td>-</td><td>").append(k.shortcutString).append("</td>");
			text.append("</tr>");
		}
		
		text.append("</table></html>");
		
		return text.toString();
	}
}
