package ltge;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AnimatedSceneObject {

	private static int counter = 1;
	
	@EqualsAndHashCode.Include @Getter private int id;
	private BufferedImage[] sprite;
	private int msBetweenFrames;
	private long lastPaint = System.nanoTime();
	private int progress = 0;
	@Setter private Scene scene;
	
	public AnimatedSceneObject(int msBetweenFrames, String...files) {
		this.id = counter++;
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
	
	public int getRow() {
		return scene.getObjectCoordinates(this).y;
	}
	
	public int getCol() {
		return scene.getObjectCoordinates(this).x;
	}
	
	public int getLayer() {
		return scene.getObjectLayer(this);
	}
	
	public void setPosition(int row, int col) {
		int l = getLayer();
		scene.remove(this);
		scene.add(this, row, col, l);
	}
}
