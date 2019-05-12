package basicGui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import backend.ConnectionHolder;
import backend.PathsChangedListener;

public class DrawingView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ConnectionHolder connectionHolder = ConnectionHolder.getInstance();

	FingerPath currentPath;
	BufferedImage cache;
	Graphics2D cacheCanvas;
	public static Color BACKGROUND_COLOR = Color.WHITE;
	private final Color bckg;

	private float scale = 1;
	private int offsetX = 0;
	private int offsetY = 0;
	private int height = -1;
	private int width = -1;

	public DrawingView() {
		bckg = Color.LIGHT_GRAY;
		changeDocumentBounds(getWidth(), getHeight(), true);
		connectionHolder.setDrawingListener(new PathsChangedListener() {

			@Override
			public void onRescaleRequired() {
				rescale();
			}

			@Override
			public void onRepaintRequired() {
				refresh();
			}

			@Override
			public void onPathAdded(FingerPath addedPath) {
				addedPath.drawPath(cacheCanvas, scale, 0, 0);
				repaint();
			}
		});
	}
	
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		changeDocumentBounds(width, height, false);
	}
	
	public void changeDocumentBounds(int w, int h, boolean forceRescale) {
        if (w != width || h != height || forceRescale) {
            width = w;
            height = h;
            rescale();
        }
    }

	public void rescale() {
		Rectangle teacherBounds = connectionHolder.getRemoteTeacherBounds();
		if (teacherBounds == null)
			teacherBounds = new Rectangle(0, 0, width, height);
		if (width >= 0 && height >= 0) {
			scale = (float) (height) / (float) (teacherBounds.getHeight());
			if (teacherBounds.getWidth() * scale > width) {
				scale = (float) (width) / (float) (teacherBounds.getWidth());
			}
			offsetX = (int) ((width - (teacherBounds.getWidth() * scale)) / 2);
			offsetY = (int) ((height - (teacherBounds.getWidth() * scale)) / 2);
		} else {
			scale = 1;
			offsetX = 0;
			offsetY = 0;
//            Toast.makeText(getContext().getApplicationContext(), "chose default scale", Toast.LENGTH_SHORT).show();
		}
		if (width > 0 && height > 0) {
			cache = new BufferedImage((int) (teacherBounds.getWidth() * scale),
					(int) (teacherBounds.getHeight() * scale), BufferedImage.TYPE_INT_ARGB);
			cacheCanvas = cache.createGraphics();
		}
		refresh();
	}

	@Override
	public void paint(Graphics canvas) {
		canvas.setColor(bckg);
		canvas.drawRect(0, 0, width, height);
		if (cache != null)
			canvas.drawImage(cache, offsetX, offsetY, null, null);
		if (currentPath != null)
			currentPath.drawPath((Graphics2D) canvas, scale, offsetX, offsetY);
	}

	protected void redrawCache(Graphics2D cacheCanvas) {
		if (cacheCanvas != null) {
			cacheCanvas.fillRect(0, 0, cache.getWidth(), cache.getHeight());
			if (connectionHolder.getCurrentPageBackground() != null) {
				BufferedImage background = connectionHolder.getCurrentPageBackground();
				float scale = Math.min((float) (height) / (float) (background.getHeight()),
						(float) (width) / (float) (background.getWidth()));
				int x = (int) ((width - (background.getWidth() * scale)) / 2);
				int y = (int) ((height - (background.getHeight() * scale)) / 2);
				cacheCanvas.drawImage(background, x, y, (int) (background.getWidth() * scale),
						(int) (background.getHeight() * scale), null, null);
			}
			FingerPath[] currentPage = connectionHolder.getCurrentPage().toArray(new FingerPath[] {});
			for (FingerPath fp : currentPage) {
				fp.drawPath(cacheCanvas, scale, 0, 0);
			}
		}
	}

	public void refresh() {
		redrawCache(cacheCanvas);
		repaint();
	}

	// Bitmaps

	public static BufferedImage StringToBitmap(String encodedString) {
		try {
			byte[] encodeByte = Base64.getDecoder().decode(encodedString);
			BufferedImage bitmap = ImageIO.read(new ByteArrayInputStream(encodeByte));
			return bitmap;
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.err.println("StringToBitmap" + ": " + encodedString);
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
