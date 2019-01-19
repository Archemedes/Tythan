package co.lotc.core.bukkit.wrapper;

import org.bukkit.configuration.file.FileConfiguration;

import co.lotc.core.agnostic.AgnosticObject;
import co.lotc.core.agnostic.Config;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class BukkitConfig implements AgnosticObject<FileConfiguration>, Config {
	
	@Getter
	@Delegate(types=Config.class)
	private final FileConfiguration handle;
}
