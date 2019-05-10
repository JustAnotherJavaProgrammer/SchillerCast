package headless;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import mdlaf.MaterialLookAndFeel;
import server.Server;

public class Headless extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JBackgroundPanel contentPane;
	static GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
	private JLabel lblTitle;
	private JLabel lblNewLabel;
	private JLabel lblWaitingForConnections_1;
	public int stop = 0;
	private JLabel Adress;
	public float[] cords; 

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
		KeyListener l = new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == 27) {
					System.exit(0);
				}

			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stu  
			}
		};
		addKeyListener(l);

		try {
			UIManager.setLookAndFeel(new MaterialLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		setExtendedState(MAXIMIZED_BOTH);
		setUndecorated(true);
		if (device.isFullScreenSupported())
			device.setFullScreenWindow(this);

		contentPane = new JBackgroundPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JLabel lblWaitingForConnections = null;
		lblWaitingForConnections_1 = new JLabel("Waiting for connections...");
		lblWaitingForConnections_1.setFont(lblWaitingForConnections_1.getFont().deriveFont(getWidth() / 25f));
		lblWaitingForConnections_1.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblWaitingForConnections_1, BorderLayout.SOUTH);

		lblTitle = new JLabel("WorkTogether-Cast");
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, getWidth() / 10f));
		contentPane.add(lblTitle, BorderLayout.CENTER);

		lblNewLabel = new JLabel("Development version - Not yet suitable for use in production");
		lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		contentPane.add(lblNewLabel, BorderLayout.NORTH);
		
		try {
			Adress = new JLabel("Adress: " + InetAddress.getLocalHost());
		} catch (UnknownHostException e1) {
			
			e1.printStackTrace();
		}
		lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, getWidth() / 15f));
		contentPane.add(Adress, BorderLayout.NORTH);

		Server.start(this);
	}

	public void connected() {
		lblNewLabel.setVisible(false);
		lblTitle.setVisible(false);
		lblWaitingForConnections_1.setVisible(false);
		Adress.setVisible(false);
		
	}
	
	public void draw(String color,String with,String[] args) {
		
		
		
		
		
		BufferedImage i = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = i.createGraphics();
		GeneralPath path = new GeneralPath();
		g2d.setColor(Color.decode(color));
		g2d.setStroke(new BasicStroke(Integer.parseInt(with)));
		path.moveTo(Float.parseFloat(args[1]), Float.parseFloat(args[2]));
		for (int j = 4; j < args.length-4; j=j+4) {
			path.quadTo(Float.parseFloat(args[j]), Float.parseFloat(args[j+1]), Float.parseFloat(args[j+2]), Float.parseFloat(args[j+3]));
		}
		g2d.draw(path);
		
		contentPane.setBackground(i);
	}
	

}
