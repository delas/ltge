package ltge.projections;

import java.awt.Point;
import java.awt.Rectangle;

import lombok.Setter;
import ltge.Map;

/*
 * Math from:
 * - https://clintbellanger.net/articles/isometric_math/
 * - https://www.youtube.com/watch?v=04oQ2jOUjkU
 */
public class IsometricProjection implements Projection {

	@Setter private int tileWidthHalf;
	@Setter private int tileHeightHalf;
	private Rectangle boardSize = null;
	
	public IsometricProjection(int tileWidth, int tileHeight) {
		this.tileWidthHalf = tileWidth;
		this.tileHeightHalf = tileHeight / 2;
	}
	
	@Override
	public Point toActualCoordinates(int row, int col) {
		row++; col++;
		int x = (col - row) * tileWidthHalf + boardSize.width / 2 - tileWidthHalf;
		int y = (col + row) * tileHeightHalf;
		return new Point(x, y);
	}

	@Override
	public Rectangle getBoardSize(Map map) {
		boardSize = new Rectangle(
				(map.getCols() + 2) * tileWidthHalf * 2,
				(map.getRows() + 2) * tileHeightHalf * 2);
		return boardSize;
	}

}
