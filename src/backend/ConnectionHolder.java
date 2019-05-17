package backend;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import basicGui.DrawingView;
import basicGui.FingerPath;

public class ConnectionHolder extends Thread implements Runnable {
	private static ConnectionHolder instance;
	public static final int PORT = 49150;
	private final ServerSocket socket;
	private Socket teacher;

	public static final String SET_DRAWING_VIEW_SIZE = "sdvs";
	public static final String INTENTIONAL_DISCONNECT = "idcn";
	public static final String TEACHER_STARTS_DRAWING = "sd";
	public static final String RETURN_TO_WAITING_SCREEN = "rtws";
	public static final String TEACHER_CHANGED_PAGE = "tgtp"; // + pageNo
	public static final String ADD_PERMANENT_PATH = "pp"; // usage: ADD_PERMANENT_PATH + " " + pageNo + " " +
															// fingerPath.toString()
	public static final String UNDO = "ud"; // + pageNo
	public static final String CLEAR = "cl"; // + pageNo
	public static final String ADD_PAGE = "ap"; // + previousPageNo
	public static final String REMOVE_PAGE = "rp"; // + pageNo
	public static final String SET_PAGE_BACKGROUND = "spb"; // + pageNO + BitmapToString

	private TeacherChangedDrawingListener drawingStateListener;
	private PathsChangedListener drawingListener;
	private ConnectionListener connectionListener;

	private boolean drawing = false;

	private int teacherPage = 0;
	private Rectangle teacherBounds = null;
	private ArrayList<ArrayList<FingerPath>> pages = new ArrayList<>();
	private ArrayList<BufferedImage> pageBackgrounds = new ArrayList<>();

	public static ConnectionHolder getInstance() {
		if (instance == null)
			try {
				instance = new ConnectionHolder();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		return instance;
	}

	private ConnectionHolder() throws IOException {
		try {
			socket = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
			throw e;
		}
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				disconnect();
				if (!socket.isClosed())
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}));
	}

	public void disconnect() {
		if (teacher != null) {
			interrupt();
			if (teacher.isConnected())
				try {
					teacher.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			teacher = null;
		}
	}

	public boolean isConnected() {
		return socket != null && teacher != null && teacher.isConnected();
	}

	public boolean isDrawing() {
		return drawing;
	}

	public ArrayList<FingerPath> getCurrentPage() {
		return pages.get(teacherPage);
	}

	public BufferedImage getCurrentPageBackground() {
		return pageBackgrounds.get(teacherPage);
	}

	public Rectangle getRemoteTeacherBounds() {
		return teacherBounds;
	}

	protected void newPage(int previousPageNo) {
		try {
			if (pages.size() >= previousPageNo) {
				pages.add(previousPageNo + 1, new ArrayList<FingerPath>());
				pageBackgrounds.add(previousPageNo + 1, null);
			} else {
				pages.add(pages.size(), new ArrayList<FingerPath>());
				pageBackgrounds.add(pages.size(), null);
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

	protected void removePage(int pageNo) {
		if (teacherPage == pageNo) {
			if (teacherPage == pages.size() - 1) {
				setCurrentPage(teacherPage - 1);
			} else {
				setCurrentPage(teacherPage + 1);
			}
		}
		if (teacherPage == pageNo) {
			if (teacherPage == pages.size() - 1) {
				setCurrentPage(teacherPage - 1);
			} else {
				setCurrentPage(teacherPage + 1);
			}
		}
		pages.remove(pageNo);
		pageBackgrounds.remove(pageNo);
		if (teacherPage > pageNo)
			setCurrentPage(teacherPage - 1);
	}

	public void setCurrentPage(int currentPage) {
		if (currentPage != teacherPage && currentPage >= 0 && currentPage < pages.size()) {
			teacherPage = currentPage;
			if (drawingListener != null)
				drawingListener.onRepaintRequired();
		}
	}

	public void setDrawingStateListener(TeacherChangedDrawingListener drawingStateListener) {
		this.drawingStateListener = drawingStateListener;
	}

	public void setDrawingListener(PathsChangedListener drawingListener) {
		this.drawingListener = drawingListener;
	}

	public void setConnectionListener(ConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
	}

	@Override
	public void run() {
		try {
			pages.clear();
			pageBackgrounds.clear();
			teacherPage = 0;
			pages.add(new ArrayList<FingerPath>());
			pageBackgrounds.add(null);
			teacher = socket.accept();
			if (connectionListener != null)
				connectionListener.connectionEstablished();
			readLines(teacher.getInputStream(), new InputStreamReadLineResultListener() {

				@Override
				public void onSuccess(String result) {
					if (INTENTIONAL_DISCONNECT.equals(result)) {
						if (connectionListener != null)
							connectionListener.onDisconnect();
						disconnect();
					} else {
						try {
							if (RETURN_TO_WAITING_SCREEN.equals(result) && drawingStateListener != null)
								drawingStateListener.onTeacherStoppedDrawing();
							else if (TEACHER_STARTS_DRAWING.equals(result) && drawingStateListener != null)
								drawingStateListener.onTeacherStartedDrawing();
							else if (result.startsWith(SET_DRAWING_VIEW_SIZE)) {
								String[] args = result.split(" ", 3);
								teacherBounds = new Rectangle(0, 0, Integer.parseInt(args[1]),
										Integer.parseInt(args[2]));
								if (drawingListener != null)
									drawingListener.onRescaleRequired();
							} else if (result.startsWith(TEACHER_CHANGED_PAGE)) {
								setCurrentPage(Integer.parseInt(result.split(" ")[1].trim()));
							} else if (result.startsWith(ADD_PERMANENT_PATH)) {
								String[] args = result.split(" ", 3);
								int page = Integer.parseInt(args[1].trim());
								FingerPath path = new FingerPath(args[2].trim());
								pages.get(page).add(path);
								if (drawingListener != null && page == teacherPage)
									drawingListener.onPathAdded(path);
							} else if (result.startsWith(UNDO)) {
								int page = Integer.parseInt(result.split(" ", 2)[1].trim());
								if (pages.get(page).size() > 0) {
									pages.get(page).remove(pages.get(page).size() - 1);
									if (drawingListener != null)
										drawingListener.onRepaintRequired();
								}
							} else if (result.startsWith(CLEAR)) {
								int page = Integer.parseInt(result.split(" ", 2)[1].trim());
								pages.get(page).clear();
								pageBackgrounds.set(page, null);
								if (drawingListener != null)
									drawingListener.onRepaintRequired();
							} else if (result.startsWith(ADD_PAGE)) {
								int previousPage = Integer.parseInt(result.split(" ", 2)[1].trim());
								newPage(previousPage);
							} else if (result.startsWith(REMOVE_PAGE)) {
								int pageNo = Integer.parseInt(result.split(" ", 2)[1].trim());
								removePage(pageNo);
							} else if (result.startsWith(SET_PAGE_BACKGROUND)) {
								String[] args = result.split(" ", 3);
								int page = Integer.parseInt(args[1].trim());
								pageBackgrounds.set(page, DrawingView.StringToBitmap(args[2].trim()));
								if (page == teacherPage && drawingListener != null)
									drawingListener.onRepaintRequired();
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							System.err.println("ExceptionDetails: " + result);
						}
					}
				}

				@Override
				public void onError(IOException e) {
					if (connectionListener != null)
						connectionListener.onDisconnect();
					disconnect();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		run();
	}

	public static void readLines(final InputStream in, final InputStreamReadLineResultListener resultCallback) {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			while (true) {
				String line = reader.readLine();
				if (resultCallback != null && line != null)
					resultCallback.onSuccess(line);
				if (INTENTIONAL_DISCONNECT.equals(line))
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (resultCallback != null)
				resultCallback.onError(e);
		}
//			}
//		}).start();
	}
}
