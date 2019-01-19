package co.lotc.core.agnostic;

import java.util.List;

public interface Config {
	
	String getString(String path);
	
	List<String> getStringList(String path);
	
	Object get(String path);
	
	int getInt(String path);

	long getLong(String path);
	
	boolean getBoolean(String path);
}
