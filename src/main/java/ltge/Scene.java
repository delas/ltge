package ltge;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class Scene {

	private HashMap<Integer, LinkedList<AnimatedSceneObject>> objects = new HashMap<>();
	private TreeSet<Integer> layers = new TreeSet<>();
	
	public void add(AnimatedSceneObject object, int layer) {
		layers.add(layer);
		if (!objects.containsKey(layer)) {
			objects.put(layer, new LinkedList<>());
		}
		objects.get(layer).add(object);
	}
	
	public SortedSet<Integer> getLayers() {
		return layers;
	}
	
	public List<AnimatedSceneObject> getObjects(int layer) {
		return objects.get(layer);
	}
}
