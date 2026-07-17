package net.fabricmc.fabric.api.advancement.event.v1;

import net.fabricmc.fabric.impl.resource.pack.BuiltinModPackSource;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;

import java.util.HashMap;
import java.util.Map;

public final class AdvancementUtil {
	public static final ThreadLocal<Map<Identifier, AdvancementSource>> SOURCES = ThreadLocal.withInitial(HashMap::new);

	private AdvancementUtil() {
	}

	public static AdvancementSource determineSource(Resource resource) {
		if (resource != null) {
			PackSource packSource = resource.getFabricPackSource();

			if (packSource == PackSource.BUILT_IN) {
				return AdvancementSource.VANILLA;
			} else if (packSource == ModResourcePackCreator.RESOURCE_PACK_SOURCE || packSource instanceof BuiltinModPackSource) {
				return AdvancementSource.MOD;
			}
		}

		// If not builtin or mod, assume external data pack.
		return AdvancementSource.DATA_PACK;
	}
}
