package backend;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Canvas;

public class JContentPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public JContentPanel(Canvas content) {
		setLayout(new BorderLayout(0, 0));
		add(content, BorderLayout.CENTER);
		setVisible(true);
	}

}
