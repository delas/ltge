package ltge.projections;

import java.awt.Point;

public class IsometricProjection implements Projection {

	@Override
	public Point toActualCoordinates(int row, int col) {
		int x = (col - row) * 64;
		int y = (col + row) * 62;
		return new Point(x, y);
	}

}
