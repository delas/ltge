package ltge.graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import lombok.Getter;
import ltge.graphics.projections.Projection;

/*
 * Inspiration for some of the logic from
 * uIso: https://www.luisrios.eti.br/public/en_us/projects/uiso/ and from
 * https://www.youtube.com/watch?v=4iPEjFUZNsw.
 */
public class Engine implements PropertyChangeListener {

	protected GameCanvas gc;
	protected BufferStrategy bs;
	protected Graphics canvas;
	
	protected Projection coordinateSystem;
	protected Map map;
	protected Scene scene;
	protected Rectangle board = new Rectangle();
	@Getter protected Point drawingOrigin = new Point(0, 0);
	
	protected Painter backgroundPainter;
	protected Painter foregroundPainter;
	
	protected Object lock = new Object();
	private double lastPaint = System.nanoTime();
	
	// caching
	private BufferedImage mapTilesCache = null;
	
	public Engine(Projection coordinateSystem, GameCanvas gc, Map map, Scene scene) {
		this.gc = gc;
		this.gc.setEngine(this);
		this.gc.createBufferStrategy(2);
		this.bs = gc.getBufferStrategy();
		this.coordinateSystem = coordinateSystem;
		this.map = map;
		this.scene = scene;
		this.scene.registerAllObjectsToEngine(this);
		
		setViewportSize(gc.getWidth(), gc.getHeight());
	}
	
	public void setBackgroundPainter(Painter p) {
		this.backgroundPainter = p;
	}
	
	public void setForegroundPainter(Painter p) {
		this.foregroundPainter = p;
	}
	
	private void invalidateMapTilesCache() {
		Rectangle newBoard = coordinateSystem.getBoardSize(map);
		board.width = newBoard.width;
		board.height = newBoard.height;
		mapTilesCache = new BufferedImage(board.width, board.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = mapTilesCache.createGraphics();
		
		for (int r = 0; r < map.getRows(); r++) {
			for (int c = 0; c < map.getCols(); c++) {
				TileType t = map.get(r, c).getType();
				Point origin = coordinateSystem.toActualCoordinates(r, c);
				g2d.drawImage(
						t.getSprite(),
						origin.x - t.getBoundingBox().x,
						origin.y - t.getBoundingBox().y,
						null);
			}
		}
	}

	public void draw() {
		// cleanup the canvas and set the clip
		canvas.setColor(gc.getBackground());
		canvas.fillRect(0, 0, gc.getWidth(), gc.getHeight());
		
		// call the background painter
		if (backgroundPainter != null) {
			Rectangle boardArea = new Rectangle(drawingOrigin.x + board.x, drawingOrigin.y + board.y, board.width, board.height);
			backgroundPainter.paint(canvas, boardArea);
		}
		
		// draw map tiles
		if (mapTilesCache == null) {
			invalidateMapTilesCache();
			canvas.drawImage(
					mapTilesCache,
					drawingOrigin.x + board.x,
					drawingOrigin.y + board.y,
					null);
		}
		
		// draw scene
		synchronized (scene) {
			for (int layer : scene.getLayers()) {
				for(Point p : scene.getObjectsAtCoordinates(layer)) {
					TileType t = map.get(p.y, p.x).getType();
					Point objectOrigin = coordinateSystem.toActualCoordinates(p.y, p.x);
					List<AnimatedSceneObject> objects = scene.getObjects(p, layer);
					int objsInSameTile = objects.size();
					for (int i = 0; i < objsInSameTile; i++) {
						AnimatedSceneObject obj = objects.get(i);
						int horizSpaceBetweeObj = t.getBoundingBox().width / (objsInSameTile + 1);
						canvas.drawImage(
								obj.getSprite(),
								//                                           center of object around its bounding box
								//-------------   -------   --------------   ------------------------------------------------------------------------------------------
								drawingOrigin.x + board.x + objectOrigin.x + (horizSpaceBetweeObj * (i + 1)) - obj.getBoundingBox().x - (obj.getBoundingBox().width / 2),
								drawingOrigin.y + board.y + objectOrigin.y + (t.getBoundingBox().height / 2) - obj.getBoundingBox().y - (obj.getBoundingBox().height / 2),
								obj.getSprite().getWidth(),
								obj.getSprite().getHeight(),
								null);
					}
				}
			}
		}
		
		// call the foreground painter
		if (foregroundPainter != null) {
			Rectangle boardArea = new Rectangle(drawingOrigin.x + board.x, drawingOrigin.y + board.y, board.width, board.height);
			foregroundPainter.paint(canvas, boardArea);
		}
	}
	
	public void setViewportSize(int width, int height) {
		synchronized (lock) {
			this.bs = gc.getBufferStrategy();
			this.canvas = bs.getDrawGraphics();
		}
		if (canvas instanceof Graphics2D) {
			((Graphics2D) this.canvas).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		invalidateMapTilesCache();
		
		this.board.x = (gc.getWidth() - board.width) / 2;
		this.board.y = (gc.getHeight() - board.height) / 2;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		double toPaint = System.nanoTime();
		if ((toPaint - lastPaint) / 1_000_000.0 > 5) {
			draw();
			bs.show();
			lastPaint = toPaint;
		}
	}
}