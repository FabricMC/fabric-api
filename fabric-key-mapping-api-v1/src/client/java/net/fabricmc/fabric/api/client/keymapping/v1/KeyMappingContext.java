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

package net.fabricmc.fabric.api.client.keymapping.v1;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import net.fabricmc.fabric.impl.client.keymapping.KeyMappingContextImpl;
import net.fabricmc.fabric.impl.client.keymapping.KeyMappingExtensions;

/// Controls when a [KeyMapping] is active and which other contexts it conflicts with.
///
/// Activation restrictions apply only to mappings registered with an explicit context through
/// [KeyMappingHelper#registerKeyMapping(KeyMapping, KeyMappingContext)]. Other mappings retain vanilla input handling
/// and use their default context only for conflict checks.
/// Presses captured by the controls screen for rebinding do not activate contextual mappings.
/// A press that starts in a screen is not delivered again to contextual mappings if a listener closes that screen.
/// Opening or replacing a screen uses vanilla's input reset. Closing to gameplay does not add an extra reset;
/// mappings retain their stored state, subject to their context and vanilla toggle behavior.
///
/// Mappings with nonconflicting contexts can share a key without appearing as conflicts in the controls screen.
/// All active mappings bound to a key receive input, as in vanilla.
/// Mods can implement this interface to define custom contexts.
public interface KeyMappingContext {
	/// Active while no screen is open. Also the default conflict context for mappings without an explicit context,
	/// except for vanilla bindings handled in screens or globally.
	KeyMappingContext IN_GAME = KeyMappingContextImpl.IN_GAME;

	/// Active while a screen is open.
	KeyMappingContext IN_SCREEN = KeyMappingContextImpl.IN_SCREEN;

	/// Always active and conflicts with every context. Also the conflict context of vanilla bindings handled in screens or globally.
	KeyMappingContext ALL = KeyMappingContextImpl.ALL;

	/// Returns the mapping's explicit context, or its default context for conflict checks.
	static KeyMappingContext of(KeyMapping mapping) {
		return ((KeyMappingExtensions) mapping).fabric_getConflictContext();
	}

	/// Returns whether either context declares a conflict with the other.
	static boolean conflicts(KeyMappingContext left, KeyMappingContext right) {
		return left.conflicts(right) || right.conflicts(left);
	}

	/// Returns whether mappings registered with this context can be activated in the current game state.
	/// Mappings explicitly registered with an inactive context return `false` from
	/// [KeyMapping#isDown()] and [KeyMapping#consumeClick()].
	boolean isActive(Minecraft client);

	/// Returns whether this context conflicts with the other.
	///
	/// Call [#conflicts(KeyMappingContext, KeyMappingContext)] to check conflicts in both directions.
	/// Most custom contexts should conflict with themselves and either [#IN_GAME] or [#IN_SCREEN],
	/// unless they are intended to be used alongside those contexts:
	///
	/// ```java
	/// return this == other || other == IN_GAME;
	/// ```
	@ApiStatus.OverrideOnly
	boolean conflicts(KeyMappingContext other);
}
