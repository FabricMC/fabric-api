package net.fabricmc.fabric.api.client.rendering.v1.world;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.WorldRenderState;

public interface AbstractWorldRenderContext {
	/**
	 * The game renderer instance.
	 *
	 * @return GameRenderer instance
	 */
	GameRenderer gameRenderer();

	/**
	 * The world renderer instance doing the rendering and invoking the event.
	 *
	 * @return WorldRenderer instance invoking the event
	 */
	WorldRenderer worldRenderer();

	/**
	 * The world render state, containing information used for rendering.
	 *
	 * @return WorldRenderState instance
	 */
	WorldRenderState worldRenderState();
}
