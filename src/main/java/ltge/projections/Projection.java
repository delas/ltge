package ltge.projections;

import java.awt.Point;

public interface Projection {

	public Point toActualCoordinates(int row, int col);
}
