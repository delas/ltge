package ltge.graphics;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AnimatedSceneObject {

	private static int counter = 1;
	
	@EqualsAndHashCode.Include @Getter private int id;
	@Getter private String name;
	private BufferedImage[] sprite;
	@Getter private Rectangle boundingBox;
	private int msBetweenFrames;
	private long lastPaint = System.nanoTime();
	private int progress = 0;
	@Setter private Scene scene;
	
	public AnimatedSceneObject(BufferedImage image) {
		this(null, 0, image);
	}
	
	public AnimatedSceneObject(Rectangle boundingBox, BufferedImage image) {
		this(boundingBox, 0, image);
	}
	
	public AnimatedSceneObject(int msBetweenFrames, BufferedImage...images) {
		this(null, msBetweenFrames, images);
	}
	
	public AnimatedSceneObject(Rectangle boundingBox, int msBetweenFrames, BufferedImage...images) {
		this.id = counter++;
		this.msBetweenFrames = msBetweenFrames;
		sprite = new BufferedImage[images.length];
		for(int i = 0; i < images.length; i++) {
			this.sprite[i] = images[i];
		}
		if (boundingBox == null) {
			this.boundingBox = new Rectangle(sprite[0].getWidth(), sprite[0].getHeight());
		} else {
			this.boundingBox = boundingBox;
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
		if (row < 0) row = 0;
		if (col < 0) col = 0;
		if (row >= scene.getMap().getRows()) row = scene.getMap().getRows() - 1;
		if (col >= scene.getMap().getCols()) col = scene.getMap().getCols() - 1;
		
		if (row != getRow() || col != getCol()) {
			int l = getLayer();
			scene.remove(this);
			scene.add(this, row, col, l);
		}
	}
}
