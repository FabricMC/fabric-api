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

import net.minecraft.client.gui.Gui;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.impl.client.rendering.hud.HudStatusBarHeightRegistryImpl;

/// A registry for [StatusBarHeightProvider] instances, known as height providers. These providers define the
/// vertical space occupied by HUD elements, known as status bars, which are positioned on the left and right sides above
/// the player's hotbar.
///
/// Registering a height provider allows the game to automatically adjust the layout of existing HUD elements,
/// including vanilla ones, to accommodate new bars without overlap. The system calculates the cumulative height from
/// registered providers on each side and shifts status bars like health, armor, food, and air bars accordingly.
///
/// Height providers are associated with an [Identifier]. The function itself should return the height of the
/// custom bar. The identifier must also be registered with a corresponding [HudElement] in
/// [HudElementRegistry]. The relative positioning to other HUD elements is determined from that registration. For
/// instance, registering a height provider for a HUD element attached before
/// [ARMOR_BAR][VanillaHudElements#ARMOR_BAR] via [#addLeft(Identifier, StatusBarHeightProvider)] implies the
/// custom bar is on the left side and affects the vertical positioning of elements starting from the armor bar upwards.
///
/// The final vertical offset for a HUD element is determined by summing the heights of all custom providers
/// registered for elements that would appear "below" it on the same side. This includes all HUD elements that have been
/// attached before it in [HudElementRegistry].
///
/// Mods that would otherwise have a mixin for altering a vanilla status bar are encouraged to instead register a
/// full replacement via [HudElementRegistry] and this class.
///
/// For vanilla HUD element identifiers, see [VanillaHudElements].
public final class HudStatusBarHeightRegistry {
	/// Adds a height provider for a status bar on the left side above the hotbar.
	///
	/// The provided function should return the vertical space (height)
	/// that the custom element associated with the given `id` occupies. This height contributes to the total
	/// offset applied to elements positioned above it on the right side. Conditions implemented for the rendering of the
	/// actual element must also be taken into account here; so when an element currently does not actually render
	/// `0` must be returned.
	///
	/// Vanilla height providers for this side are: [HudStatusBarHeightRegistryImpl#HEALTH_BAR],
	/// [HudStatusBarHeightRegistryImpl#ARMOR_BAR]
	///
	/// Existing height providers (like vanilla) can be replaced to coincide with
	/// [HudElementRegistry#replaceElement(Identifier, Function)].
	///
	/// Registration is frozen once the client has fully started.
	///
	/// @param id             the [Identifier]; must be registered with a corresponding [HudElement] in
	///                       [HudElementRegistry].
	/// @param heightProvider a [StatusBarHeightProvider] that takes a [Player] from
	///                       [Gui#getCameraPlayer()] and returns the height.
	public static void addLeft(Identifier id, StatusBarHeightProvider heightProvider) {
		Objects.requireNonNull(id, "id is null");
		Objects.requireNonNull(heightProvider, "height provider is null");
		HudStatusBarHeightRegistryImpl.addLeft(id, heightProvider);
	}

	/// Adds a height provider for a status bar on the right side above the hotbar.
	///
	/// The provided function should return the vertical space (height)
	/// that the custom element associated with the given `id` occupies. This height contributes to the total
	/// offset applied to elements positioned above it on the right side. Conditions implemented for the rendering of the
	/// actual element must also be taken into account here; so when an element currently does not actually render
	/// `0` must be returned.
	///
	/// Vanilla height providers for this side are: [HudStatusBarHeightRegistryImpl#MOUNT_HEALTH],
	/// [HudStatusBarHeightRegistryImpl#FOOD_BAR], [HudStatusBarHeightRegistryImpl#AIR_BAR]
	///
	/// Existing height providers (like vanilla) can be replaced to coincide with
	/// [HudElementRegistry#replaceElement(Identifier, Function)].
	///
	/// Registration is frozen once the client has fully started.
	///
	/// @param id             the [Identifier]; must be registered with a corresponding [HudElement] in
	///                       [HudElementRegistry].
	/// @param heightProvider a [StatusBarHeightProvider] that takes a [Player] from
	///                       [Gui#getCameraPlayer()] and returns the height.
	public static void addRight(Identifier id, StatusBarHeightProvider heightProvider) {
		Objects.requireNonNull(id, "id is null");
		Objects.requireNonNull(heightProvider, "height provider is null");
		HudStatusBarHeightRegistryImpl.addRight(id, heightProvider);
	}

	/// Gets the total calculated height offset for a given HUD element ID. Usage:
	/// {@snippet :
	///  - net.minecraft.client.gui.GuiGraphics.guiHeight() - (39 + renderHeight)
	///  + net.minecraft.client.gui.GuiGraphics.guiHeight() - HudStatusBarHeightRegistry.getHeight(id)
	///  }
	///
	/// This method is typically used by the rendering system to determine how much
	/// to shift a HUD element. It returns the default HUD height which is `39` plus the sum of all registered
	/// provider heights that are considered "below" the position of the element associated with the given `id`.
	///
	/// Note: The registry must be initialized (frozen) before this method returns
	/// values without throwing an exception. This initialization happens during the Minecraft client setup.
	///
	/// @param id the [Identifier] of the HUD element.
	/// @return the total height offset.
	public static int getHeight(Identifier id) {
		Objects.requireNonNull(id, "id is null");
		return HudStatusBarHeightRegistryImpl.getHeight(id);
	}

	private HudStatusBarHeightRegistry() {
	}
}
