package ltge;

import lombok.Getter;

public class Map {

	private Tile[][] map;
	@Getter private int rows;
	@Getter private int cols;
	
	public Map(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.map = new Tile[rows][cols];
	}
	
	public Tile get(int row, int col) {
		return map[row][col];
	}
	
	public void set(int row, int col, TileType tileType) {
		set(row, col, new Tile(tileType));
	}
	
	public void set(int row, int col, Tile tile) {
		map[row][col] = tile;
	}
}
