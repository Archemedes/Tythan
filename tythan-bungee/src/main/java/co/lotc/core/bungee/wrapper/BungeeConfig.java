package co.lotc.core.bungee.wrapper;

import co.lotc.core.agnostic.AgnosticObject;
import co.lotc.core.agnostic.Config;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import net.md_5.bungee.config.Configuration;

@RequiredArgsConstructor
public class BungeeConfig implements AgnosticObject<Configuration>, Config {

	@Getter
	@Delegate(types=Config.class)
	private final Configuration handle;
}
