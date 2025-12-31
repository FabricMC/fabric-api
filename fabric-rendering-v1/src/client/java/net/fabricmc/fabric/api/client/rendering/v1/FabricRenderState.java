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

package net.fabricmc.fabric.api.client.rendering.v1;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Fabric-provided extensions for render states, allowing for the addition of extra render data.
///
/// Note: This interface is automatically implemented on the following classes via Mixin and interface injection:
///
///   - [net.minecraft.client.renderer.entity.state.EntityRenderState],
///   - [net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState]
///   - [net.minecraft.client.renderer.item.ItemStackRenderState] and [net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState]
///   - [net.minecraft.client.renderer.state.MapRenderState] and [net.minecraft.client.renderer.state.MapRenderState.MapDecorationRenderState]
///   - [net.minecraft.client.renderer.block.MovingBlockRenderState]
///   - [net.minecraft.client.renderer.state.LevelRenderState]
///   - [net.minecraft.client.renderer.state.CameraRenderState]
///   - [net.minecraft.client.renderer.state.BlockOutlineRenderState]
///   - [net.minecraft.client.renderer.state.WeatherRenderState]
///   - [net.minecraft.client.renderer.state.WorldBorderRenderState]
///   - [net.minecraft.client.renderer.state.SkyRenderState]
///
@ApiStatus.NonExtendable
public interface FabricRenderState {
	/// Get extra render data from the render state.
	/// @param key the key of the data
	/// @param <T> the type of the data
	/// @return the data, or `null` if it cannot be found.
	@Nullable
	default <T> T getData(RenderStateDataKey<T> key) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/// Get extra render data from the render state, or a default value if it cannot be found.
	/// @param key the key of the data
	/// @param defaultValue the default value
	/// @param <T> the type of the data
	/// @return the data, or the default value if it cannot be found.
	default <T> T getDataOrDefault(RenderStateDataKey<T> key, T defaultValue) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/// Set extra render data to the render state.
	/// @param key the key of the data
	/// @param value the data
	/// @param <T> the type of the data
	default <T> void setData(RenderStateDataKey<T> key, @Nullable T value) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/// Clears all extra render data on the render state.
	default void clearExtraData() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
