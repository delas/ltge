package ltge.graphics;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Timer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AnimatedSceneObject {

	private static int counter = 1;
	
	// general fields
	@EqualsAndHashCode.Include @Getter private int id;
	@Setter private Scene scene;
	
	// configuration of the object
	@Getter private String status = "initial";
	
	// graphics related aspect
	private HashMap<String, ArrayList<BufferedImage>> sprite;
	@Getter private Rectangle boundingBox;
	
	// fields needed for animating
	private int msBetweenFrames;
	private long lastPaint = System.nanoTime();
	private int progress = 0;
	protected boolean arePositionUpdatesUrgent = false;
	
	@Getter private PropertyChangeSupport support;
	
	public AnimatedSceneObject(BufferedImage image) {
		this(null, "initial", 0, image);
	}
	
	public AnimatedSceneObject(Rectangle boundingBox, BufferedImage image) {
		this(boundingBox, "initial", 0, image);
	}
	
	public AnimatedSceneObject(int msBetweenFrames, BufferedImage...images) {
		this(null, "initial", msBetweenFrames, images);
	}
	
	public AnimatedSceneObject(String initialStatusLabel, int msBetweenFrames, BufferedImage...images) {
		this(null, initialStatusLabel, msBetweenFrames, images);
	}
	
	public AnimatedSceneObject(Rectangle boundingBox, String initialStatusLabel, int msBetweenFrames, BufferedImage...images) {
		this.id = counter++;
		this.msBetweenFrames = msBetweenFrames;
		this.sprite = new HashMap<>();
		this.status = initialStatusLabel;
		addState(initialStatusLabel, images);
		if (boundingBox == null) {
			this.boundingBox = new Rectangle(images[0].getWidth(), images[0].getHeight());
		} else {
			this.boundingBox = boundingBox;
		}
		this.support = new PropertyChangeSupport(this);
		
		new Timer(msBetweenFrames, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				support.firePropertyChange("new-frame", false, true);
			}
		}).start();
	}

	public void addState(String label, BufferedImage...images) {
		sprite.put(label, new ArrayList<>(images.length));
		for(int i = 0; i < images.length; i++) {
			sprite.get(label).add(images[i]);
		}
	}
	
	public BufferedImage getSprite() {
		return getSprite(status);
	}

	public BufferedImage getSprite(String label) {
		if (System.nanoTime() >= lastPaint + (msBetweenFrames * 1000000)) {
			progress = (progress + 1) % sprite.get(label).size();
			lastPaint = System.nanoTime();
		}
		return sprite.get(label).get(progress);
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
			if (arePositionUpdatesUrgent) {
				support.firePropertyChange("URGENT", new Point(col, row), new Point(getCol(), getRow()));
			} else {
				support.firePropertyChange("position", new Point(col, row), new Point(getCol(), getRow()));
			}
		}
	}
	
	public void setStatus(String status) {
		String old = this.status;
		this.status = status;
		support.firePropertyChange("status", old, status);
//		System.out.println("fire?");
	}
}
