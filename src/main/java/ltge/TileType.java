package ltge;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import lombok.Getter;

public class TileType {

	@Getter private BufferedImage sprite;
	@Getter private int width;
	@Getter private int height;

	public TileType(String file) {
		try {
			sprite = ImageIO.read(TileType.class.getClassLoader().getResourceAsStream(file));
			width = sprite.getWidth();
			height = sprite.getHeight();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
