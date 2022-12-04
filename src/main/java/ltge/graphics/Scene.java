package ltge.graphics;

import java.awt.Point;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.Getter;

public class Scene {

	@Getter private Map map;
	private HashMap<Integer, HashMap<Point, LinkedList<AnimatedSceneObject>>> objects = new HashMap<>();
	private HashMap<AnimatedSceneObject, Point> objectsToCoordinates = new HashMap<>();
	private HashMap<AnimatedSceneObject, Integer> objectsToLayer = new HashMap<>();
	private TreeSet<Integer> layers = new TreeSet<>();
	
	public Scene(Map map) {
		this.map = map;
	}

	public void add(AnimatedSceneObject object, int row, int col, int layer) {
		add(object, new Point(col, row), layer);
	}
	
	public synchronized void add(AnimatedSceneObject object, Point logicalCoordinates, int layer) {
		layers.add(layer);
		if (!objects.containsKey(layer)) {
			objects.put(layer, new HashMap<>());
		}
		if (!objects.get(layer).containsKey(logicalCoordinates)) {
			objects.get(layer).put(logicalCoordinates, new LinkedList<>());
		}
		objects.get(layer).get(logicalCoordinates).add(object);
		objectsToCoordinates.put(object, logicalCoordinates);
		objectsToLayer.put(object, layer);
		object.setScene(this);
	}
	
	public synchronized void remove(AnimatedSceneObject obj) {
		Point coords = objectsToCoordinates.get(obj);
		Integer layer = objectsToLayer.get(obj);
		if (coords != null) {
			objectsToCoordinates.remove(obj);
		}
		if (layer != null) {
			objectsToLayer.remove(obj);
		}
		if (coords != null && layer != null) {
			objects.get(layer).get(coords).remove(obj);
		}
	}
	
	public SortedSet<Integer> getLayers() {
		return layers;
	}
	
	public List<AnimatedSceneObject> getObjects(Point logicalCoordinates, int layer) {
		if (objects.containsKey(layer)) {
			return objects.get(layer).get(logicalCoordinates);
		}
		return null;
	}

	public List<Point> getObjectsAtCoordinates(int layer) {
		if (!objects.containsKey(layer)) {
			return new LinkedList<>();
		}
		List<Point> points = new LinkedList<>(objects.get(layer).keySet());
		points.sort(new Comparator<Point>() {
			@Override
			public int compare(Point o1, Point o2) {
				int p1 = o1.x + o1.y;
				int p2 = o2.x + o2.y;
				return (p1 == p2)? 0 : ((p1 < p2)? -1 : 1); 
			}
		});
		return points;
	}
	
	public Point getObjectCoordinates(AnimatedSceneObject object) {
		return objectsToCoordinates.get(object);
	}
	
	public int getObjectLayer(AnimatedSceneObject object) {
		return objectsToLayer.get(object);
	}
}
