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

import java.util.Objects;
import java.util.function.Function;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.rendering.hud.HudStatusBarHeightRegistryImpl;

/**
 * A registry for {@link StatusBarHeightProvider} instances, known as height providers. These providers define the
 * vertical space occupied by HUD elements, known as status bars, which are positioned on the left and right sides above
 * the player's hotbar.
 *
 * <p>Registering a height provider allows the game to automatically adjust the layout of existing HUD elements,
 * including vanilla ones, to accommodate new bars without overlap. The system calculates the cumulative height from
 * registered providers on each side and shifts status bars like health, armor, food, and air bars accordingly.
 *
 * <p>Height providers are associated with an {@link Identifier}. The function itself should return the height of the
 * custom bar. The identifier must also be registered with a corresponding {@link HudElement} in
 * {@link HudElementRegistry}. The relative positioning to other HUD elements is determined from that registration. For
 * instance, registering a height provider for a HUD element attached before
 * {@link VanillaHudElements#ARMOR_BAR ARMOR_BAR} via {@link #addLeft(Identifier, StatusBarHeightProvider)} implies the
 * custom bar is on the left side and affects the vertical positioning of elements starting from the armor bar upwards.
 *
 * <p>The final vertical offset for a HUD element is determined by summing the heights of all custom providers
 * registered for elements that would appear "below" it on the same side. This includes all HUD elements that have been
 * attached before it in {@link HudElementRegistry}.
 *
 * <p>Mods that would otherwise have a mixin for altering a vanilla status bar are encouraged to instead register a
 * full replacement via {@link HudElementRegistry} and this class.
 *
 * <p>For vanilla HUD element identifiers, see {@link VanillaHudElements}.
 */
public final class HudStatusBarHeightRegistry {
	/**
	 * Adds a height provider for a status bar on the left side above the hotbar.
	 *
	 * <p>The provided function should return the vertical space (height)
	 * that the custom element associated with the given {@code id} occupies. This height contributes to the total
	 * offset applied to elements positioned above it on the right side. Conditions implemented for the rendering of the
	 * actual element must also be taken into account here; so when an element currently does not actually render
	 * {@code 0} must be returned.
	 *
	 * <p>Vanilla height providers for this side are: {@link HudStatusBarHeightRegistryImpl#HEALTH_BAR},
	 * {@link HudStatusBarHeightRegistryImpl#ARMOR_BAR}
	 *
	 * <p>Existing height providers (like vanilla) can be replaced to coincide with
	 * {@link HudElementRegistry#replaceElement(Identifier, Function)}.
	 *
	 * <p>Registration is frozen once the client has fully started.
	 *
	 * @param id             the {@link Identifier}; must be registered with a corresponding {@link HudElement} in
	 *                       {@link HudElementRegistry}.
	 * @param heightProvider a {@link StatusBarHeightProvider} that takes a {@link PlayerEntity} from
	 *                       {@link InGameHud#getCameraPlayer()} and returns the height.
	 */
	public static void addLeft(Identifier id, StatusBarHeightProvider heightProvider) {
		Objects.requireNonNull(id, "id is null");
		Objects.requireNonNull(heightProvider, "height provider is null");
		HudStatusBarHeightRegistryImpl.addLeft(id, heightProvider);
	}

	/**
	 * Adds a height provider for a status bar on the right side above the hotbar.
	 *
	 * <p>The provided function should return the vertical space (height)
	 * that the custom element associated with the given {@code id} occupies. This height contributes to the total
	 * offset applied to elements positioned above it on the right side. Conditions implemented for the rendering of the
	 * actual element must also be taken into account here; so when an element currently does not actually render
	 * {@code 0} must be returned.
	 *
	 * <p>Vanilla height providers for this side are: {@link HudStatusBarHeightRegistryImpl#MOUNT_HEALTH},
	 * {@link HudStatusBarHeightRegistryImpl#FOOD_BAR}, {@link HudStatusBarHeightRegistryImpl#AIR_BAR}
	 *
	 * <p>Existing height providers (like vanilla) can be replaced to coincide with
	 * {@link HudElementRegistry#replaceElement(Identifier, Function)}.
	 *
	 * <p>Registration is frozen once the client has fully started.
	 *
	 * @param id             the {@link Identifier}; must be registered with a corresponding {@link HudElement} in
	 *                       {@link HudElementRegistry}.
	 * @param heightProvider a {@link StatusBarHeightProvider} that takes a {@link PlayerEntity} from
	 *                       {@link InGameHud#getCameraPlayer()} and returns the height.
	 */
	public static void addRight(Identifier id, StatusBarHeightProvider heightProvider) {
		Objects.requireNonNull(id, "id is null");
		Objects.requireNonNull(heightProvider, "height provider is null");
		HudStatusBarHeightRegistryImpl.addRight(id, heightProvider);
	}

	/**
	 * Gets the total calculated height offset for a given HUD element ID. Usage:
	 * {@snippet :
	 * - net.minecraft.client.gui.DrawContext.getScaledWindowHeight() - (39 + renderHeight)
	 * + net.minecraft.client.gui.DrawContext.getScaledWindowHeight() - HudStatusBarHeightRegistry.getHeight(id)
	 * }
	 *
	 * <p>This method is typically used by the rendering system to determine how much
	 * to shift a HUD element. It returns the default HUD height which is {@code 39} plus the sum of all registered
	 * provider heights that are considered "below" the position of the element associated with the given {@code id}.
	 *
	 * <p>Note: The registry must be initialized (frozen) before this method returns
	 * values without throwing an exception. This initialization happens during the Minecraft client setup.
	 *
	 * @param id the {@link Identifier} of the HUD element.
	 * @return the total height offset.
	 */
	public static int getHeight(Identifier id) {
		Objects.requireNonNull(id, "id is null");
		return HudStatusBarHeightRegistryImpl.getHeight(id);
	}

	private HudStatusBarHeightRegistry() {
	}
}
