package ltge.graphics;

import java.awt.Color;
import java.beans.PropertyChangeEvent;

import ltge.graphics.projections.Projection;

public class EngineAlwaysOn extends Engine implements Runnable {

	private boolean running = false;
	private final double UPDATE_CAP = 1./10.;
	
	private Thread engine;
	
	
	public EngineAlwaysOn(Projection coordinateSystem, GameCanvas gc, Map map, Scene scene) {
		super(coordinateSystem, gc, map, scene);
	}

	private void drawFPS(String fps) {
		canvas.setColor(Color.black);
		canvas.drawString(fps, 10, 20);
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
					Thread.sleep(100);
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
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// NO-OP
	}
}
