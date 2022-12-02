package ltge;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Scene {

	private HashMap<Integer, HashMap<Point, LinkedList<AnimatedSceneObject>>> objects = new HashMap<>();
	private HashMap<AnimatedSceneObject, Point> objectsToCoordinates = new HashMap<>();
	private HashMap<AnimatedSceneObject, Integer> objectsToLayer = new HashMap<>();
	private TreeSet<Integer> layers = new TreeSet<>();
	
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

	public Set<Point> getObjectsAtCoordinates(int layer) {
		if (!objects.containsKey(layer)) {
			return new HashSet<>();
		}
		return objects.get(layer).keySet();
	}
	
	public Point getObjectCoordinates(AnimatedSceneObject object) {
		return objectsToCoordinates.get(object);
	}
	
	public int getObjectLayer(AnimatedSceneObject object) {
		return objectsToLayer.get(object);
	}
}
