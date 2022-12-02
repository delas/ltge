package ltge;

import lombok.Getter;

public class Tile {

	@Getter private TileType type;
	
	public Tile(TileType type) {
		this.type = type;
	}
}
