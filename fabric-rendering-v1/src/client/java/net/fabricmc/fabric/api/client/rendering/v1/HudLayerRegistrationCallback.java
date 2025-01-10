package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface HudLayerRegistrationCallback {
	Event<HudLayerRegistrationCallback> EVENT = EventFactory.createArrayBacked(HudLayerRegistrationCallback.class, callbacks -> layeredDrawer -> {
		for (HudLayerRegistrationCallback callback : callbacks) {
			callback.register(layeredDrawer);
		}
	});

	void register(FabricLayeredDrawer layeredDrawer);
}
