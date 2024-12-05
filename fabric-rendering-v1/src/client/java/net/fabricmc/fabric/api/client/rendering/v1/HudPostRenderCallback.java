package net.fabricmc.fabric.api.client.rendering.v1;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface HudPostRenderCallback {
	Event<HudPostRenderCallback> EVENT = EventFactory.createArrayBacked(HudPostRenderCallback.class, (listeners) -> (matrixStack, delta) -> {
		for (HudPostRenderCallback event : listeners) {
			event.onHudPostRender(matrixStack, delta);
		}
	});

	/**
	 * Called after rendering the whole hud, which is displayed in game, in a world.
	 *
	 * @param drawContext the {@link DrawContext} instance
	 * @param tickCounter the {@link RenderTickCounter} instance
	 */
	void onHudPostRender(DrawContext drawContext, RenderTickCounter tickCounter);
}
