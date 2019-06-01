package co.lotc.core.save;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;

public class PreventBukkitEncodingRegistry implements CodecRegistry {

	@Override
	public <T> Codec<T> get(Class<T> clazz) {
		String packName = clazz.getPackage().getName();
		if(packName.startsWith("org.bukkit") || packName.startsWith("net.minecraft.server")) {
			throw new IllegalArgumentException("All Bukkit and NMS classes must be explicitly specified as a codec");
		}
		
		clazz.getPackage().getName();
		return null;
	}

}
