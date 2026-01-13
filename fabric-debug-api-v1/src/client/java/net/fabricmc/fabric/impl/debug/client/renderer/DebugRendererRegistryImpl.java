package net.fabricmc.fabric.impl.debug.client.renderer;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import net.minecraft.util.debug.DebugSubscription;

import net.fabricmc.fabric.api.debug.v1.client.renderer.DebugRendererFactory;

public final class DebugRendererRegistryImpl {
	public static final Set<Entry> RENDERERS = new HashSet<>();

	public static <T> void register(
			DebugSubscription<T> debugSubscription,
			DebugRendererFactory rendererFactory
	) {
		RENDERERS.add(new Entry(debugSubscription, rendererFactory));
	}

	public record Entry(
			DebugSubscription<?> debugSubscription,
			DebugRendererFactory rendererFactory
	) {
		// Ensure DebugSubscriptions with different values don't both
		// get into the Set, causing undesirable behavior

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) return false;
			Entry entry = (Entry) o;
			return Objects.equals(
					debugSubscription,
					entry.debugSubscription
			);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(debugSubscription);
		}
	}
}
