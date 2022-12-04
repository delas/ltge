package ltge.graphics.projections;

import java.awt.Point;
import java.awt.Rectangle;

import ltge.graphics.Map;

public interface Projection {

	public Point toActualCoordinates(int row, int col);

	public Rectangle getBoardSize(Map map);
}
