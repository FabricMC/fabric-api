package net.fabricmc.fabric.api.client.rendering.v1.hud;

import java.util.function.ToIntFunction;

import net.minecraft.entity.player.PlayerEntity;

/**
 * Define the vertical space occupied by HUD elements, known as status bars, which are positioned on the left and right
 * sides above the player's hotbar.
 *
 * @see HudStatusBarHeightRegistry
 */
@FunctionalInterface
public interface StatusBarHeightProvider extends ToIntFunction<PlayerEntity> {
	/**
	 * @param player the {@link PlayerEntity} from {@link InGameHud#getCameraPlayer()}
	 * @return the vertical space occupied by the status bar
	 */
	@Override
	int applyAsInt(PlayerEntity player);
}
