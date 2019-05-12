package server;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import com.sun.prism.paint.Paint;

import headless.Headless;

public class Server {

	private static ArrayList<ArrayList<GeneralPath>> pages = new ArrayList<>();
	private static float pencilMode;
	private static String color;
	private static float strokeWidth;

	public static void start(Headless main) {

		try {
			System.out.println("Server Startet on " + InetAddress.getLocalHost());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {

					ServerSocket ssock = new ServerSocket(4444);
					Socket sock = ssock.accept();
					main.connected();

					InputStreamReader ir = new InputStreamReader(sock.getInputStream());
					BufferedReader bf = new BufferedReader(ir);
					while (main.stop == 0) {

						

						input(bf.readLine(),main);
						
						

						

					}
					ssock.close();

				} catch (IOException e) {

					e.printStackTrace();
				}

			}
		}).start();

		// Pages in ArrayList speichern und benutzen Pages = arraylist welche alle
		// strokes oder buffered images enthalten enthalten
		// Bei app unter Fingerpath nach Seriliseble path suchen und code am besten
		// kopieren
		//
//		

	}

	public static GeneralPath SerializablePath(String serialized,Headless main) {

		GeneralPath path = new GeneralPath();

		String[] instructions = serialized.split(" ");
		for (String instruction : instructions) {
			instruction = instruction.trim();
			String[] opts = instruction.split(",");
			for (int i = 0; i < opts.length; i++) {
				opts[i] = opts[i].substring(0, opts[i].length() - 1);
			}
			if ("mt".equals(opts[0])) {
				path.moveTo(Float.parseFloat(opts[1]), Float.parseFloat(opts[2]));
			} else if ("qt".equals(opts[0])) {
				path.quadTo(Float.parseFloat(opts[1]), Float.parseFloat(opts[2]), Float.parseFloat(opts[3]),
						Float.parseFloat(opts[4]));
			} else if ("rlt".equals(opts[0])) {
				path.lineTo(Float.parseFloat(opts[1]), Float.parseFloat(opts[2]));
			}

		}
		return path;
	}

	public static void input(String result,Headless main) {
		if (result.startsWith("pp")) {
			String[] args = result.split(" ", 3);
			int pageNO = Integer.parseInt(args[1].trim());
			pages.get(pageNO).add(FingerPath(args[2].trim(),main));
			refresh(pageNO,main);
		} else if (result.startsWith("undo")) {
			int pageNO = Integer.parseInt(result.split(" ", 2)[1].trim());
			if (pages.get(pageNO).size() > 0)
				pages.get(pageNO).remove(pages.get(pageNO).size() - 1);
		} else if (result.startsWith("clear")) {
			int pageNO = Integer.parseInt(result.split(" ", 2)[1].trim());
			pages.get(pageNO).clear();
		} else if (result.startsWith("ap")) {
			int previousPage = Integer.parseInt(result.split(" ", 2)[1].trim());
			ArrayList<GeneralPath> newpage= new ArrayList<>();
			pages.add(newpage);
		} else if (result.startsWith("rp")) {
			int pageNo = Integer.parseInt(result.split(" ", 2)[1].trim());
			pages.remove(pageNo);
			refresh(pageNo - 1,main);
		}
		

	}

	public static GeneralPath FingerPath(String s,Headless main) {

		GeneralPath path = new GeneralPath();
		String[] args = s.split(" ", 4);
		for (int i = 0; i < args.length; i++) {
			args[i] = args[i].trim();
		}
		pencilMode = Integer.parseInt(args[0]);
		color = args[1];
		strokeWidth = Integer.parseInt(args[2]);
		path = SerializablePath(args[3],main);

		return path;
	}

	public static void refresh(int page,Headless main) {

		BufferedImage i = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = i.createGraphics();
		g2d.setStroke(new BasicStroke(4.0f));
		g2d.setPaint(Color.decode(color));

		ArrayList<GeneralPath> curpage = pages.get(page);
		for (int j = 0; j < curpage.size(); j++) {
			g2d.draw(curpage.get(j));
		}
		main.contentPane.setBackground(i);
	}

}
