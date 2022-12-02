package ltge;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import lombok.Getter;
import lombok.Setter;

public class AnimatedSceneObject {

	private BufferedImage[] sprite;
	private int msBetweenFrames;
	@Getter @Setter private int row;
	@Getter @Setter private int col;
	private long lastPaint = System.nanoTime();
	private int progress = 0;

	public AnimatedSceneObject(int msBetweenFrames, String...files) {
		this.msBetweenFrames = msBetweenFrames;
		try {
			sprite = new BufferedImage[files.length];
			for(int i = 0; i < files.length; i++) {
				this.sprite[i] = ImageIO.read(TileType.class.getClassLoader().getResourceAsStream(files[i]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BufferedImage getSprite() {
		if (System.nanoTime() >= lastPaint + (msBetweenFrames * 1000000)) {
			progress = (progress + 1) % sprite.length;
			lastPaint = System.nanoTime();
		}
		return sprite[progress];
	}
}
