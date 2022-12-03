package ltge;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import lombok.Getter;

public class TileType {

	@Getter private BufferedImage sprite;
	@Getter private Rectangle boundingBox;

	public TileType(String file) {
		this(file, null);
	}
	
	public TileType(String file, Rectangle boundingBox) {
		try {
			sprite = ImageIO.read(TileType.class.getClassLoader().getResourceAsStream(file));
			if (boundingBox == null) {
				this.boundingBox = new Rectangle(0, 0, sprite.getWidth(), sprite.getHeight());
			} else {
				this.boundingBox = boundingBox;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
