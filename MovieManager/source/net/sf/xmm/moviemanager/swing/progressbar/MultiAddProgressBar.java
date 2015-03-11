
package net.sf.xmm.moviemanager.swing.progressbar;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;

import net.sf.xmm.moviemanager.util.GUIUtil;


public class MultiAddProgressBar extends JDialog implements PropertyChangeListener {

	JProgressBar progressBar;
	JLabel label;
	ProgressBean progressBean;
	boolean done = false;
	
	JPanel panel;
	JPanel buttonPanel;
	
	boolean abortButtonVisible = true;
	
	public MultiAddProgressBar(Dialog parent, String title, boolean modal, ProgressBean progressBean) {
		super(parent, modal);
		this.progressBean = progressBean;
		setTitle(title);
		progressBean.addPropertyChangeListener(this);	
		createProgressBar(parent, title);
	}


	public MultiAddProgressBar(Frame parent, String title, boolean modal, ProgressBean progressBean) {
		super(parent, modal);
		this.progressBean = progressBean;
		setTitle(title);
		progressBean.addPropertyChangeListener(this);
		createProgressBar(parent, title);
	}


	public void createProgressBar(Window parent, String title) {

		/* Close dialog... */
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (abortButtonVisible)
					close();
				else
					dispose();
			}
		});
		
		GUIUtil.enableDisposeOnEscapeKey(this, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				
				if (abortButtonVisible)
					close();
				else
					dispose();
			}
		});
		
		panel = new JPanel();
		//panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		double size[][] = {{10, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL, 10}, {5, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 15, TableLayout.PREFERRED, 1}};

		panel.setLayout(new TableLayout(size));

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setPreferredSize(new Dimension(220, 40));

		label = new JLabel(title);
		label.setPreferredSize(new Dimension(40, 35));

		label.setFont(new Font(label.getFont().getName(), label.getFont().getStyle(), 20));

		JButton abortButton = new JButton("Abort");

		abortButton.setPreferredSize(new Dimension(150, 30));
		abortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});

		buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(abortButton, BorderLayout.NORTH);

		panel.add(label, "1, 1, 4, 1");
		panel.add(progressBar, "1, 3, 3, 3");
		panel.add(buttonPanel, "2, 5");

		getContentPane().add(panel);

		setPreferredSize(new Dimension(400, 180));
		pack();
		//setSize(400, 200);
		setLocationRelativeTo(parent);
	}

	public void showAbortButton(boolean show) {
		
		if (!show) {
			panel.remove(buttonPanel);
			abortButtonVisible = false;
		}
		else {
		
			if (!abortButtonVisible)
				panel.add(buttonPanel, "2, 5");
			
			abortButtonVisible = true;
		}
	}
	
	public void setString(String str) {
		label.setText(str);
	}

	public void close() {
		progressBean.cancel();
		
		while (!progressBean.isReady()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		dispose();
	}


	/**
	 * @see java.beans.PropertyChangeListener
	 */
	public void propertyChange(final PropertyChangeEvent evt) {

		//GUIUtil.isNotEDT();

		if ("value".equals(evt.getPropertyName())) {

			try {

				GUIUtil.invokeLater(new Runnable() {

					public void run() {
						Object status = evt.getNewValue();

						// Done
						if (status == null) {
							done = true;
							dispose();
						}
						else
							setString((String)status);

					}});
			} catch (Exception e) {
				;
			}
		}
	}
}
