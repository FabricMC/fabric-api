/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.client.rendering.v1.hud;

import java.util.function.ToIntFunction;

import org.jetbrains.annotations.ApiStatus;

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
	int getStatusBarHeight(PlayerEntity player);

	@ApiStatus.NonExtendable
	@Override
	default int applyAsInt(PlayerEntity player) {
		return this.getStatusBarHeight(player);
	}
}
