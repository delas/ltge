package ltge.projections;

import java.awt.Point;

public class OrthographicProjection implements Projection {

	@Override
	public Point toActualCoordinates(int row, int col) {
		return new Point(row * 64, col * 64);
	}
}
