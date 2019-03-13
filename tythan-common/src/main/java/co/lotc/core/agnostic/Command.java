package co.lotc.core.agnostic;

import java.util.List;

public interface Command {

	String getPermission();
	
	String getName();
	
	String getDescription();
	
	List<String> getAliases();
}
