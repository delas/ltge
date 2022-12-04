package ltge.graphics;

import java.awt.Canvas;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import lombok.Getter;
import lombok.Setter;

public class GameCanvas extends Canvas {

	private static final long serialVersionUID = -1372862841511384090L;
	@Getter @Setter private Engine engine = null;
	
	public GameCanvas() {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (engine != null) {
					engine.setViewportSize(getWidth(), getHeight());
				}
			}
		});
	}
}
