package server;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.sun.corba.se.spi.orbutil.fsm.Input;

import headless.Headless;

public class Server {

	private static ArrayList<ArrayList<GeneralPath>> pages = new ArrayList<>();
	private static ArrayList<ArrayList<String>> pagescolor = new ArrayList<>();
	private static ArrayList<ArrayList<Integer>> pagesstoke = new ArrayList<>();
	private static ArrayList<ArrayList<Integer>> pagesmode = new ArrayList<>();
	private static int witdh;
	private static int hieth;
	private static int curpage = 0;

	public static void start(Headless main) {

		ArrayList<GeneralPath> a = new ArrayList<>();
		pages.add(a);
		ArrayList<Integer> b = new ArrayList<>();
		pagesmode.add(b);
		ArrayList<String> c = new ArrayList<>();
		pagescolor.add(c);
		ArrayList<Integer> d = new ArrayList<>();
		pagesstoke.add(d);

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
						String input = bf.readLine();
						input(input, main);
						System.out.println(input);
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

	public static GeneralPath SerializablePath(String serialized, Headless main) {

		GeneralPath path = new GeneralPath();
		String[] instructions = serialized.split(" ");
		for (String instruction : instructions) {
			instruction = instruction.trim();
			String[] opts = instruction.split(",");
			for (int i = 0; i < opts.length; i++) {
				opts[i] = opts[i].substring(0, opts[i].length() - 1);
			}

			if ("m".equals(opts[0])) {
				path.moveTo(Float.parseFloat(opts[1]), Float.parseFloat(opts[2]));

			} else if ("q".equals(opts[0])) {
				path.quadTo(Float.parseFloat(opts[1]), Float.parseFloat(opts[2]), Float.parseFloat(opts[3]),
						Float.parseFloat(opts[4]));
			} else if ("rlt".equals(opts[0])) {
				path.lineTo(Float.parseFloat(opts[1]), Float.parseFloat(opts[2]));
			}

		}

		return path;
	}

	public static void input(String result, Headless main) {
		if (result.startsWith("pp")) {
			String[] args = result.split(" ", 3);
			int pageNO = Integer.parseInt(args[1].trim());
			pages.get(pageNO).add(FingerPath(args[2].trim(), main));
			refresh(pageNO, main);
		} else if (result.startsWith("ud")) {
			int pageNO = Integer.parseInt(result.split(" ", 2)[1].trim());
			if (pages.get(pageNO).size() > 0)
				pages.get(pageNO).remove(pages.get(pageNO).size() - 1);
		} else if (result.startsWith("cl")) {
			int pageNO = Integer.parseInt(result.split(" ", 2)[1].trim());
			pages.get(pageNO).clear();
			pagesmode.get(pageNO).clear();
			pagescolor.get(pageNO).clear();
			pagesstoke.get(pageNO).clear();
			refresh(pageNO, main);
		} else if (result.startsWith("ap")) {
			int previousPage = Integer.parseInt(result.split(" ", 2)[1].trim());
			curpage = previousPage + 1;
			ArrayList<GeneralPath> newpage = new ArrayList<>();
			ArrayList<String> color = new ArrayList<>();
			ArrayList<Integer> stoke = new ArrayList<>();
			ArrayList<Integer> mode = new ArrayList<>();
			pagescolor.add(color);
			pagesstoke.add(stoke);
			pagesmode.add(mode);
			pages.add(newpage);
			refresh(curpage, main);
		} else if (result.startsWith("rp")) {
			int pageNo = Integer.parseInt(result.split(" ", 2)[1].trim());
			pages.remove(pageNo);
			pagesmode.remove(pageNo);
			pagescolor.remove(pageNo);
			pagesstoke.remove(pageNo);
			curpage = pageNo - 1;
			refresh(pageNo - 1, main);
		} else if (result.startsWith("tgtp")) {
			int changedpage = Integer.parseInt(result.split(" ")[1].trim());
			if (changedpage >= 0 && changedpage <= pages.size()) {
				curpage = changedpage;
				refresh(changedpage, main);
			}
		} else if (result.startsWith("sdvs")) {
			String[] args = result.split(" ", 3);
			witdh = Integer.parseInt(args[1]);
			hieth = Integer.parseInt(args[2]);
		}

	}

	public static GeneralPath FingerPath(String s, Headless main) {

		GeneralPath path = new GeneralPath();
		String[] args = s.split(" ", 4);
		for (int i = 0; i < args.length; i++) {
			args[i] = args[i].trim();
		}
		pagesmode.get(curpage).add(Integer.parseInt(args[0]));
		pagescolor.get(curpage).add(args[1]);
		pagesstoke.get(curpage).add(Integer.parseInt(args[2]));
		path = SerializablePath(args[3], main);

		return path;
	}

	public static void refresh(int page, Headless main) {

		BufferedImage i = new BufferedImage(witdh, hieth, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = i.createGraphics();

		ArrayList<GeneralPath> curpage = pages.get(page);
		for (int j = 0; j < curpage.size(); j++) {
			g2d.setStroke(new BasicStroke(pagesstoke.get(page).get(j)));
			if (pagesmode.get(page).get(j) == 1) {
				g2d.setPaint(new Color(Color.decode(pagescolor.get(page).get(j)).getRed(),
						Color.decode(pagescolor.get(page).get(j)).getGreen(),
						Color.decode(pagescolor.get(page).get(j)).getBlue(), 120));
				g2d.setStroke(new BasicStroke(pagesstoke.get(page).get(j) * 10));
			} else if (pagesmode.get(page).get(j) == 0) {
				g2d.setPaint(Color.decode(pagescolor.get(page).get(j)));
			} else if (pagesmode.get(page).get(j) == 2) {
				g2d.setPaint(Color.WHITE);
			}

			g2d.draw(curpage.get(j));
		}
		g2d.drawString("test", 50, 50);
		main.contentPane.setBackground(i);
	}

}
