package backend;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JProgressBar;

public class WaitingForTeacher extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public WaitingForTeacher() {
		setLayout(new BorderLayout(0, 0));

		JLabel lblWaitingForThe = new JLabel("Waiting for the teacher to start drawing...");
		lblWaitingForThe.setHorizontalAlignment(SwingConstants.CENTER);
		lblWaitingForThe.setFont(lblWaitingForThe.getFont().deriveFont(getWidth() / 25f));
		add(lblWaitingForThe, BorderLayout.SOUTH);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		add(progressBar, BorderLayout.CENTER);

	}

}
