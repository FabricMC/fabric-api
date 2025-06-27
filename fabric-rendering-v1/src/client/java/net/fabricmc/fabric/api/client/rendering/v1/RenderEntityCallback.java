package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.entity.Entity;

public interface RenderEntityCallback {
	Event<RenderEntityCallback> EVENT = EventFactory.createArrayBacked(RenderEntityCallback.class,
			(listeners) -> (entity) -> {
				for (RenderEntityCallback listener : listeners) {
					listener.render(entity);

				}

			});

	/**
	 * Called before rendering an entity.
	 *
	 * @param entity the {@link Entity} instance
	 */

	void render(Entity entity);
}
