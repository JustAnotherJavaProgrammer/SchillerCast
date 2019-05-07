package headless;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import mdlaf.MaterialLookAndFeel;
import top.gigabox.supportcomponent.toast.MaterialTost;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Headless extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	static GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Headless frame = new Headless();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Headless() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(new MaterialLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		setExtendedState(MAXIMIZED_BOTH);
		setUndecorated(true);
		if (device.isFullScreenSupported())
			device.setFullScreenWindow(this);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JLabel lblWaitingForConnections = new JLabel("Waiting for connections...");
		lblWaitingForConnections.setFont(lblWaitingForConnections.getFont().deriveFont(getWidth() / 25f));
		lblWaitingForConnections.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblWaitingForConnections, BorderLayout.SOUTH);

		JLabel lblTitle = new JLabel("WorkTogether-Cast");
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, getWidth() / 10f));
		contentPane.add(lblTitle, BorderLayout.CENTER);
		
		JLabel lblNewLabel = new JLabel("Development version - Not yet suitable for use in production");
		lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		contentPane.add(lblNewLabel, BorderLayout.NORTH);

	}

}
