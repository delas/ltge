package ltge.projections;

import java.awt.Point;
import java.awt.Rectangle;

import lombok.Setter;
import ltge.Map;

public class OrthographicProjection {

	@Setter private int tileWidth;
	@Setter private int tileHeight;
	
	public OrthographicProjection(int tileWidth, int tileHeight) {
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}
	
	public Point toActualCoordinates(int row, int col) {
		return new Point(row * tileWidth, col * tileHeight);
	}

	public Rectangle getBoardSize(Map map) {
		return new Rectangle(map.getCols() * tileWidth, map.getRows() * tileHeight);
	}
}
