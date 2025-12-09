package net.fabricmc.fabric.impl.client.rendering;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;

import net.fabricmc.fabric.api.client.rendering.v1.DebugRendererFactory;

public final class DebugRendererRegistryImpl {
	public static final List<RendererEntry> RENDERERS = new ArrayList<>();

	private DebugRendererRegistryImpl() {
	}

	public static void register(DebugRendererFactory debugRenderer) {
		RENDERERS.add(new RendererEntry(debugRenderer, null));
	}

	public static void registerConditional(
			DebugRendererFactory debugRenderer,
			Predicate<Minecraft> isEnabled
	) {
		RENDERERS.add(new RendererEntry(debugRenderer, isEnabled));
	}

	public record RendererEntry(
			DebugRendererFactory debugRenderer,
			@Nullable Predicate<Minecraft> isEnabled
	) {
	}
}
