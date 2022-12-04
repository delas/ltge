package ltge.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.List;

import lombok.Getter;
import ltge.graphics.projections.Projection;

/*
 * Inspiration for some of the logic from
 * uIso: https://www.luisrios.eti.br/public/en_us/projects/uiso/ and from
 * https://www.youtube.com/watch?v=4iPEjFUZNsw.
 */
public class Engine implements Runnable {

	private boolean running = false;
	private final double UPDATE_CAP = 1./30.;
	
	private Thread engine;

	private GameCanvas gc;
	private BufferStrategy bs;
	private Graphics canvas;
	
	private Projection coordinateSystem;
	private Map map;
	private Scene scene;
	private Rectangle board = new Rectangle();
	@Getter private Point drawingOrigin = new Point(0, 0);
	
	private Object lock = new Object();
	
	// caching
	private BufferedImage mapTilesCache;
	
	public Engine(Projection coordinateSystem, GameCanvas gc, Map map, Scene scene) {
		this.gc = gc;
		this.gc.setEngine(this);
		this.gc.createBufferStrategy(2);
		this.bs = gc.getBufferStrategy();
		this.coordinateSystem = coordinateSystem;
		this.map = map;
		this.scene = scene;
		
		setViewportSize(gc.getWidth(), gc.getHeight());
	}
	
	private void invalidateMapTilesCache() {
		Rectangle newBoard = coordinateSystem.getBoardSize(map);
		board.width = newBoard.width;
		board.height = newBoard.height;
		mapTilesCache = new BufferedImage(board.width, board.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = mapTilesCache.createGraphics();
		g2d.setColor(Color.red);
		g2d.fillRect(0, 0, board.width, board.height);
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
		
		// draw map tiles
		if (mapTilesCache == null) {
			invalidateMapTilesCache();
		}
		canvas.drawImage(
				mapTilesCache,
				drawingOrigin.x + board.x,
				drawingOrigin.y + board.y,
				null);
		
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
	}
	
	private void drawFPS(String fps) {
		canvas.setColor(Color.black);
		canvas.drawString(fps, 10, 20);
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
	public void run() {
		try {
			running = true;
			
			boolean render = false;
			double firstTime = 0;
			double lastTime = System.nanoTime() / 1000000000d;
			double passedTime = 0;
			double unprocessedTime = 0;
			
			double frameTime = 0;
			int frames = 0;
			int fps = 0;

			while (running) {
				render = false;
				firstTime = System.nanoTime() / 1000000000d;
				passedTime = firstTime - lastTime;
				lastTime = firstTime;
				
				unprocessedTime += passedTime;
				frameTime += passedTime;
				
				while (unprocessedTime >= UPDATE_CAP) {
					unprocessedTime -= UPDATE_CAP;
					render = true;
					
					if (frameTime >= 1) {
						frameTime = 0;
						fps = frames;
						frames = 0;
					}
				}
				
				if (render) {
					synchronized (lock) {
						draw();
						drawFPS(String.format("FPS: %d", fps));
						bs.show();
					}
					
					frames++;
				} else {
					Thread.sleep(5);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void init() {
		engine = new Thread(this);
		engine.start();
	}
}