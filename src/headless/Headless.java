package headless;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import backend.ConnectionHolder;
import backend.ConnectionListener;
import backend.TeacherChangedDrawingListener;
import backend.WaitingForTeacher;
import basicGui.DrawingView;
import mdlaf.MaterialLookAndFeel;
//import top.gigabox.supportcomponent.toast.MaterialTost;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Headless extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel waitingForConnections;
	static GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
	private ConnectionHolder connectionHolder = ConnectionHolder.getInstance();
	private DrawingView drawingView = new DrawingView();
	private WaitingForTeacher waitingForTeacher = new WaitingForTeacher();

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
		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				System.exit(0);
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		try {
			UIManager.setLookAndFeel(new MaterialLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		setExtendedState(MAXIMIZED_BOTH);
		setUndecorated(true);
		if (device.isFullScreenSupported())
			device.setFullScreenWindow(this);

		waitingForConnections = new JPanel();
		waitingForConnections.setBorder(new EmptyBorder(5, 5, 5, 5));
		waitingForConnections.setLayout(new BorderLayout(0, 0));
		setContentPane(waitingForConnections);

		JLabel lblWaitingForConnections;
		try {
			lblWaitingForConnections = new JLabel(
					"Waiting for connections on '" + InetAddress.getLocalHost().getCanonicalHostName() + "'...");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			lblWaitingForConnections = new JLabel("Waiting for connections...");
		}
		lblWaitingForConnections.setFont(lblWaitingForConnections.getFont().deriveFont(getWidth() / 25f));
		lblWaitingForConnections.setHorizontalAlignment(SwingConstants.CENTER);
		waitingForConnections.add(lblWaitingForConnections, BorderLayout.SOUTH);

		JLabel lblTitle = new JLabel("WorkTogether-Cast");
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, getWidth() / 10f));
		waitingForConnections.add(lblTitle, BorderLayout.CENTER);

		JLabel lblNewLabel = new JLabel("Development version - Not yet suitable for use in production");
		lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		waitingForConnections.add(lblNewLabel, BorderLayout.NORTH);

		// init ConnectionHolder
		connectionHolder.setConnectionListener(new ConnectionListener() {

			@Override
			public void onDisconnect() {
				setContentPane(waitingForConnections);
			}

			@Override
			public void connectionEstablished() {
				if (!getContentPane().equals(drawingView))
					setContentPane(waitingForTeacher);
			}
		});

		connectionHolder.setDrawingStateListener(new TeacherChangedDrawingListener() {

			@Override
			public void onTeacherStoppedDrawing() {
				setContentPane(waitingForTeacher);
			}

			@Override
			public void onTeacherStartedDrawing() {
				setContentPane(drawingView);
			}
		});
		connectionHolder.start();
	}

}
