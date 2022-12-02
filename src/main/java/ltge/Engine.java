package ltge;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import ltge.projections.Projection;

/*
 * Inspiration for some of the logic from uIso: https://www.luisrios.eti.br/public/en_us/projects/uiso/
 */
public class Engine implements Runnable {

	private static final float FRAME_TIME = 1000.f / 30.f;
	public static final int MIN_SLEEP_TIME = 5;
	
	private Thread engine;

	private GameCanvas gc;
	private BufferStrategy bs;
	private Graphics canvas;
	
	private Projection coordinateSystem;
	private Map map;
	private Scene scene;
	private int viewportWidth = 800;
	private int viewportHeight = 600;
	
	// caching mechanism
	private BufferedImage mapTilesCache;
	
	public Engine(Projection coordinateSystem, GameCanvas gc, Map map, Scene scene) {
		this.gc = gc;
		this.gc.createBufferStrategy(2);
		this.bs = gc.getBufferStrategy();
		this.canvas = bs.getDrawGraphics();
		if (canvas instanceof Graphics2D) {
			((Graphics2D) this.canvas).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		this.coordinateSystem = coordinateSystem;
		this.map = map;
		this.scene = scene;
	}
	
	private void invalidateMapTilesCache() {
		mapTilesCache = new BufferedImage(viewportWidth, viewportHeight, BufferedImage.TYPE_INT_RGB);
		for (int r = 0; r < map.getRows(); r++) {
			for (int c = 0; c < map.getCols(); c++) {
				TileType t = map.get(r, c).getType();
				Point origin = coordinateSystem.toActualCoordinates(c, r);
				mapTilesCache.getGraphics().drawImage(t.getSprite(),
						origin.x,
						origin.y,
						64,
						64,
						null);
			}
		}
	}
	
	public void draw() {
		// cleanup the canvas and set the clip
		canvas.setClip(0, 0, viewportWidth, viewportHeight);
		canvas.clearRect(0, 0, viewportWidth, viewportHeight);
		
		// draw map tiles
		if (mapTilesCache == null) {
			invalidateMapTilesCache();
		}
		canvas.drawImage(mapTilesCache, 0, 0, viewportWidth, viewportHeight, null);
		
		// draw scene
		for(AnimatedSceneObject obj : scene) {
			Point origin = coordinateSystem.toActualCoordinates(obj.getCol(), obj.getRow());
			canvas.drawImage(obj.getSprite(),
					origin.x,
					origin.y,
					64,
					64,
					null);
		}
	}
	
	private void drawFPS(String fps) {
		canvas.setColor(Color.black);
		canvas.drawString(fps, 10, 20);
	}
	
	@Override
	public void run() {
		try {
			int frame = 1;
			long frameTimeBeforeDrawing;
			long second_time_before = System.nanoTime();
			long timeAfterDrawing;
			float fps = 30f;

			while (true) {
				frameTimeBeforeDrawing = System.nanoTime();
				draw();
				drawFPS(String.format("FPS: %.0f", fps));
				
				bs.show();

				timeAfterDrawing = System.nanoTime();
				if (timeAfterDrawing - second_time_before >= 1000000000L) {
					fps = frame * 1000.f / ((timeAfterDrawing - second_time_before) / 1000000.f);
					second_time_before = timeAfterDrawing;
					frame = 0;
				}

				frame++;

				timeAfterDrawing = System.nanoTime();
				int msToSleep = Math.round(FRAME_TIME - (timeAfterDrawing - frameTimeBeforeDrawing) / 1000000.f);
				msToSleep = msToSleep <= 0 ? MIN_SLEEP_TIME : msToSleep;

				Thread.sleep(msToSleep);
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
