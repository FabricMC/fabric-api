package net.fabricmc.fabric.api.advancement.event.v1;


import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events for manipulating advancements.
 */
public final class AdvancementEvents {
	private AdvancementEvents() {
	}

	/**
	 * This event can be used to modify advancements.
	 * The main use case is to add items to vanilla or mod advancements (e.g. modded lava bucket to "Hot Stuff").
	 */
	public static final Event<Modify> MODIFY = EventFactory.createArrayBacked(Modify.class, listeners -> (map) -> {
		for (Modify listener : listeners) {
			listener.modifyAdvancement(map);
		}
	});

	@FunctionalInterface
	public interface Modify {
		/**
		 * Called when an advancement is loading to modify advancements.
		 */
		void modifyAdvancement(AdvancementMapWrapper mapWrapper);
	}
}
