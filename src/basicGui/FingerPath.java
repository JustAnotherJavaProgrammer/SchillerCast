package basicGui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

public class FingerPath {
	public static final int PENCIL_MODE_STANDARD = 0;
	public static final int PENCIL_MODE_MARKER = 1;
	public static final int PENCIL_MODE_ERASER = 2;
	public static final int PENCIL_MODE_FILL = 3;

	public GeneralPath path;
	public int pencilMode;
	public Color color;
	public int strokeWidth;
	private BasicStroke paint;

	public FingerPath(String s) {
		String[] args = s.split(" ", 4);
		for (int i = 0; i < args.length; i++) {
			args[i] = args[i].trim();
		}
		pencilMode = Integer.parseInt(args[0]);
		color = new Color(Integer.parseInt(args[1]));
		strokeWidth = Integer.parseInt(args[2]);
		path = stringToGeneralPath(args[3]);
		setPaintPreferences();
	}

	public void setPaintPreferences() {
		paint = new BasicStroke(pencilMode == PENCIL_MODE_MARKER ? strokeWidth * 10 : strokeWidth,
				pencilMode == PENCIL_MODE_MARKER ? BasicStroke.CAP_SQUARE : BasicStroke.CAP_ROUND,
				pencilMode == PENCIL_MODE_MARKER ? BasicStroke.JOIN_MITER : BasicStroke.JOIN_ROUND);
	}

	public void drawPath(Graphics2D canvas, float scale) {
//	        Log.d("FingerPath", "scale: " + scale);
		Color c = pencilMode == PENCIL_MODE_ERASER ? DrawingView.BACKGROUND_COLOR : this.color;
		canvas.setColor(
				new Color(c.getRed(), c.getGreen(), c.getBlue(), this.pencilMode == PENCIL_MODE_MARKER ? 30 : 255));
		if (scale == 1) {
			canvas.setStroke(paint);
			canvas.draw(path);
		} else {
			canvas.setStroke(new BasicStroke(paint.getLineWidth() * scale, paint.getEndCap(), paint.getLineJoin()));
			GeneralPath path = new GeneralPath(this.path);
			AffineTransform scaleMatrix = new AffineTransform();
			scaleMatrix.setToScale(scale, scale);
			path.transform(scaleMatrix);
			canvas.draw(path);
		}
	}

	private static GeneralPath stringToGeneralPath(String serialized) {
		GeneralPath result = new GeneralPath();
		String[] instructions = serialized.split(" ");
		float lastX = 0;
		float lastY = 0;
		for (String instruction : instructions) {
			instruction = instruction.trim();
			String[] opts = instruction.split(",");
//            for (int i = 0; i < opts.length; i++) {
//                opts[i] = opts[i].substring(0, opts[i].length()-1);
//            }
			if ("mt".equals(opts[0])) {
				lastX = Float.parseFloat(opts[1]);
				lastY = Float.parseFloat(opts[2]);
				result.moveTo(lastX, lastY);
			} else if ("qt".equals(opts[0])) {
				lastX = Float.parseFloat(opts[3]);
				lastY = Float.parseFloat(opts[4]);
				result.quadTo(Float.parseFloat(opts[1]), Float.parseFloat(opts[2]), lastX, lastY);
			} else if ("rlt".equals(opts[0])) {
				result.lineTo(lastX + Float.parseFloat(opts[1]), lastY + Float.parseFloat(opts[2]));
			} else {
				System.out.println("parsingPath: instruction: " + instruction);
				throw new RuntimeException("The drawing instruction \"" + opts[0] + "\" is not recognized!");
			}
		}
		return result;
	}
}
