package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public interface HudPreRenderCallback {
	Event<HudPreRenderCallback> EVENT = EventFactory.createArrayBacked(HudPreRenderCallback.class, (listeners) -> (matrixStack, delta) -> {
		for (HudPreRenderCallback event : listeners) {
			event.onHudPreRender(matrixStack, delta);
		}
	});

	/**
	 * Called before rendering the whole hud, which is displayed in game, in a world.
	 *
	 * @param drawContext the {@link DrawContext} instance
	 * @param tickCounter the {@link RenderTickCounter} instance
	 */
	void onHudPreRender(DrawContext drawContext, RenderTickCounter tickCounter);
}
