package co.lotc.core;

public interface TythanCommon extends Tythan {

	default void onLoad() {
		TythanProvider.init(this);
		DependencyLoader.loadJars(getDataFolder());
		
		load();
	}
	
	void load();
	
}
