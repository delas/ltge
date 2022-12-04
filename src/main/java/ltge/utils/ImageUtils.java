package ltge.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ImageUtils {

	public static BufferedImage createEmptyImage(int width, int height, Color color) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g2d = img.getGraphics();
		g2d.setColor(color);
		g2d.fillRect(0, 0, width, height);
		return img;
	}
}
