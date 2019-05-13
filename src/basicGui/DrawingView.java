package basicGui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import backend.ConnectionHolder;
import backend.PathsChangedListener;

public class DrawingView extends Canvas {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ConnectionHolder connectionHolder = ConnectionHolder.getInstance();

	BufferedImage cache;
	Graphics2D cacheCanvas;
	public static Color BACKGROUND_COLOR = Color.WHITE;
	private final Color bckg;

	private float scale = 1;
	private int offsetX = 0;
	private int offsetY = 0;
	private int height = -1;
	private int width = -1;

	HashMap<Key, Object> renderingHints = new HashMap<>();

	public DrawingView() {
		bckg = Color.LIGHT_GRAY;
		setBackground(bckg);
		renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		renderingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		renderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		changeDocumentBounds(getWidth(), getHeight(), true);
		connectionHolder.setDrawingListener(new PathsChangedListener() {
			@Override
			public void onRescaleRequired() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						rescale();
					}
				});
			}

			@Override
			public void onRepaintRequired() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						refresh();
					}
				});
			}

			@Override
			public void onPathAdded(FingerPath addedPath) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (cacheCanvas != null)
							addedPath.drawPath(cacheCanvas, scale);
						repaint();
					}
				});
			}
		});
		setVisible(true);
	}

	public void changeDocumentBounds(int w, int h, boolean forceRescale) {
		if (w != width || h != height || forceRescale) {
			width = w;
			height = h;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					rescale();
				}
			});
		}
	}

	public void rescale() {
		Rectangle teacherBounds = connectionHolder.getRemoteTeacherBounds();
		if (teacherBounds == null)
			teacherBounds = new Rectangle(0, 0, width, height);
		if (width >= 0 && height >= 0) {
			scale = Math.min((float) (height) / (float) (teacherBounds.getHeight()),
					(float) (width) / (float) (teacherBounds.getWidth()));
			offsetX = (int) ((width - (teacherBounds.getWidth() * scale)) / 2);
			offsetY = (int) ((height - (teacherBounds.getHeight() * scale)) / 2);
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
			cacheCanvas.setRenderingHints(renderingHints);
		}
		refresh();
	}

	@Override
	public void paint(Graphics canvas) {
		super.paint(canvas);
		if (canvas != null && cache != null) {
			Graphics2D canvas2D = (Graphics2D) canvas;
			canvas2D.setRenderingHints(renderingHints);
			canvas2D.drawImage(cache, offsetX, offsetY, null, null);
		}
	}

	protected void redrawCache(Graphics2D cacheCanvas) {
		if (cacheCanvas != null) {
			cacheCanvas.setColor(BACKGROUND_COLOR);
			cacheCanvas.fillRect(0, 0, cache.getWidth(), cache.getHeight());
			if (connectionHolder.getCurrentPageBackground() != null) {
				BufferedImage background = connectionHolder.getCurrentPageBackground();
				float scale = Math.min((float) (cache.getHeight()) / (float) (background.getHeight()),
						(float) (cache.getWidth()) / (float) (background.getWidth()));
				int x = (int) ((cache.getWidth() - (background.getWidth() * scale)) / 2);
				int y = (int) ((cache.getHeight() - (background.getHeight() * scale)) / 2);
				cacheCanvas.drawImage(background, x, y, (int) (background.getWidth() * scale),
						(int) (background.getHeight() * scale), null, null);
			}
			FingerPath[] currentPage = connectionHolder.getCurrentPage().toArray(new FingerPath[] {});
			for (FingerPath fp : currentPage) {
				fp.drawPath(cacheCanvas, scale);
			}
		}
	}

	public void refresh() {
		redrawCache(cacheCanvas);
		repaint();
	}

	@Override
	public void validate() {
		super.validate();
		changeDocumentBounds(getWidth(), getHeight(), false);
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
