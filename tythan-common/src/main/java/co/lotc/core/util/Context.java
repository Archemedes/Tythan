package co.lotc.core.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;

public class Context implements Iterable<Entry<String, Object>> {
	@Getter private final Map<String, Object> map = new HashMap<>();

	public boolean contains(String key) {
		return has(key);
	}
	
	public boolean has(String key) {
		return map.containsKey(key);
	}
	
	public void set(String key, Object value) {
		map.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		return (T) map.get(key);
	}
	
	public String getString(String key) {
		return String.valueOf(map.get(key));
	}
	
	@Override
	public Iterator<Entry<String, Object>> iterator() {
		return map.entrySet().iterator();
	}
	
}
