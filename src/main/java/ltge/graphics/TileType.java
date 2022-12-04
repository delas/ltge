package ltge.graphics;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import lombok.Getter;

public class TileType {

	@Getter private BufferedImage sprite;
	@Getter private Rectangle boundingBox;
	
	public TileType(BufferedImage sprite) {
		this(sprite, null);
	}

	public TileType(String file) throws IOException {
		this(file, null);
	}
	
	public TileType(String file, Rectangle boundingBox) throws IOException {
		this(ImageIO.read(TileType.class.getClassLoader().getResourceAsStream(file)), boundingBox);
	}
	
	public TileType(BufferedImage sprite, Rectangle boundingBox) {
		this.sprite = sprite;
		if (boundingBox == null) {
			this.boundingBox = new Rectangle(0, 0, sprite.getWidth(), sprite.getHeight());
		} else {
			this.boundingBox = boundingBox;
		}
	}
}
