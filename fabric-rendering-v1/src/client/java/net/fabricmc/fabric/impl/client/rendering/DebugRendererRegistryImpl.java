package net.fabricmc.fabric.impl.client.rendering;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.util.debug.DebugSubscription;

import net.fabricmc.fabric.api.client.rendering.v1.debug.DebugRendererFactory;

public final class DebugRendererRegistryImpl {
	public static final List<RendererEntry> RENDERERS = new ArrayList<>();

	private DebugRendererRegistryImpl() {
	}

	public static <T> void register(DebugRendererFactory debugRenderer, DebugSubscription<T> debugSubscription) {
		RENDERERS.add(new RendererEntry<>(debugRenderer, debugSubscription, null));
	}

	public static <T> void registerConditional(
			DebugRendererFactory debugRenderer,
			DebugSubscription<T> debugSubscription,
			Predicate<Minecraft> isEnabled
	) {
		RENDERERS.add(new RendererEntry<>(debugRenderer, debugSubscription, isEnabled));
	}

	public record RendererEntry<T>(
			DebugRendererFactory debugRenderer,
			DebugSubscription<T> debugSubscription,
			@Nullable Predicate<Minecraft> isEnabled
	) {
	}
}
