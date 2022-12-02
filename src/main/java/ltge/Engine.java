package ltge;

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
import ltge.projections.OrthographicProjection;

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
	
	private OrthographicProjection coordinateSystem;
	private Map map;
	private Scene scene;
	private Rectangle board = new Rectangle();
	@Getter private Point drawingOrigin = new Point(0, 0);
	
	private Object lock = new Object();
	
	// caching
	private BufferedImage mapTilesCache;
	
	public Engine(OrthographicProjection coordinateSystem, GameCanvas gc, Map map, Scene scene) {
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
		mapTilesCache = new BufferedImage(board.width, board.height, BufferedImage.TYPE_INT_RGB);
		for (int r = 0; r < map.getRows(); r++) {
			for (int c = 0; c < map.getCols(); c++) {
				TileType t = map.get(r, c).getType();
				Point origin = coordinateSystem.toActualCoordinates(r, c);
				mapTilesCache.getGraphics().drawImage(
						t.getSprite(),
						origin.x,
						origin.y,
						t.getWidth(),
						t.getHeight(),
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
				board.width,
				board.height, null);
		
		// draw scene
		synchronized (scene) {
			for (int layer : scene.getLayers()) {
				for(Point p : scene.getObjectsAtCoordinates(layer)) {
					TileType t = map.get(p.y, p.x).getType();
					Point origin = coordinateSystem.toActualCoordinates(p.y, p.x);
					List<AnimatedSceneObject> objects = scene.getObjects(p, layer);
	//				int objsInSameTile = objects.size();
					for (AnimatedSceneObject obj : objects) {
						canvas.drawImage(obj.getSprite(),
								drawingOrigin.x + board.x + origin.x + (t.getWidth() / 2) - (obj.getSprite().getWidth() / 2),
								drawingOrigin.y + board.y + origin.y + (t.getHeight() / 2) - (obj.getSprite().getHeight() / 2),
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
