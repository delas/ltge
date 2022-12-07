package ltge.graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JPanel;

import lombok.Getter;
import lombok.Setter;
import ltge.graphics.projections.Projection;

public class EngineCanvas extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = -1372862841511384090L;
	
	@Getter protected Projection coordinateSystem;
	@Getter protected Map map;
	@Getter protected Scene scene;
	@Getter protected Rectangle board = new Rectangle();
	@Getter protected Point drawingOrigin = new Point(0, 0);
	
	@Setter private Painter backgroundPainter;
	@Setter private Painter foregroundPainter;
	
	private double lastRepaint = System.nanoTime();
	
	// caching
	private BufferedImage mapTilesCache = null;
	
	public EngineCanvas(Projection coordinateSystem, Map map, Scene scene) {
		this.coordinateSystem = coordinateSystem;
		this.map = map;
		this.scene = scene;
		this.scene.registerAllObjectsToEngine(this);
		
		invalidateMapTilesCache();
	}
	
	public void invalidateMapTilesCache() {
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
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		
		// cleanup the canvas and set the clip
		g2d.setColor(getBackground());
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		// call the background painter
		if (backgroundPainter != null) {
			Rectangle boardArea = new Rectangle(drawingOrigin.x + board.x, drawingOrigin.y + board.y, board.width, board.height);
			backgroundPainter.paint(g2d, boardArea);
		}
		
		// draw map tiles
		if (mapTilesCache == null) {
			invalidateMapTilesCache();
			g2d.drawImage(
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
						g2d.drawImage(
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
			foregroundPainter.paint(g2d, boardArea);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("URGENT")) {
			repaint();
		} else if (System.nanoTime() - lastRepaint > 150_000_000d) {
			repaint();
			lastRepaint = System.nanoTime();
		}
	}
}
