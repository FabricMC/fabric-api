package net.fabricmc.fabric.api.client.rendering.v1;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public interface HudLayer {
	void render(DrawContext context, RenderTickCounter tickCounter);
}
