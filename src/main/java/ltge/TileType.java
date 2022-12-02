package ltge;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import lombok.Getter;

public class TileType {

	@Getter private BufferedImage sprite;
	@Getter private Rectangle tileArea;

	public TileType(String file) {
		this(file, null);
	}
	
	public TileType(String file, Rectangle tileArea) {
		try {
			sprite = ImageIO.read(TileType.class.getClassLoader().getResourceAsStream(file));
			if (tileArea == null) {
				this.tileArea = new Rectangle(sprite.getWidth(), sprite.getHeight());
			} else {
				this.tileArea = tileArea;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
