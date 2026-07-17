package net.fabricmc.fabric.api.advancement.event.v1;

import net.fabricmc.fabric.impl.resource.pack.BuiltinModPackSource;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AdvancementUtil {
	public static final Map<Identifier, AdvancementSource> SOURCES = new ConcurrentHashMap<>();

	private AdvancementUtil() {
	}

	public static AdvancementSource determineSource(Resource resource) {
		PackSource packSource = resource.getFabricPackSource();

		if (packSource == PackSource.BUILT_IN) {
			return AdvancementSource.VANILLA;
		} else if (packSource == ModResourcePackCreator.RESOURCE_PACK_SOURCE || packSource instanceof BuiltinModPackSource) {
			return AdvancementSource.MOD;
		}

		// If not builtin or mod, assume external data pack.
		// It might also be a virtual advancement injected via mixin instead of being loaded
		// from a resource, but we can't determine that here.
		return AdvancementSource.DATA_PACK;
	}
}
