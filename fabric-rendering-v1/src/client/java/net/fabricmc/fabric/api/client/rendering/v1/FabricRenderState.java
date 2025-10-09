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
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.block.MovingBlockRenderState;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.render.state.SkyRenderState;
import net.minecraft.client.render.state.WeatherRenderState;
import net.minecraft.client.render.state.WorldBorderRenderState;
import net.minecraft.client.render.state.WorldRenderState;

/**
 * Fabric-provided extensions for render states, allowing for the addition of extra render data.
 *
 * <p>Note: This interface is automatically implemented on the following classes via Mixin and interface injection:
 * <ul>
 *     <li>{@link EntityRenderState},
 *     <li>{@link BlockEntityRenderState}
 *     <li>{@link ItemRenderState} and {@link ItemRenderState.LayerRenderState}
 *     <li>{@link MapRenderState} and {@link MapRenderState.Decoration}
 *     <li>{@link MovingBlockRenderState}
 *     <li>{@link WorldRenderState}
 *     <li>{@link CameraRenderState}
 *     <li>{@link OutlineRenderState}
 *     <li>{@link WeatherRenderState}
 *     <li>{@link WorldBorderRenderState}
 *     <li>{@link SkyRenderState}
 * </ul>
 */
@ApiStatus.NonExtendable
public interface FabricRenderState {
	/**
	 * Get extra render data from the render state.
	 * @param key the key of the data
	 * @param <T> the type of the data
	 * @return the data, or {@code null} if it cannot be found.
	 */
	@Nullable
	default <T> T getData(RenderStateDataKey<T> key) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Get extra render data from the render state, or a default value if it cannot be found.
	 * @param key the key of the data
	 * @param defaultValue the default value
	 * @param <T> the type of the data
	 * @return the data, or the default value if it cannot be found.
	 */
	default <T> T getDataOrDefault(RenderStateDataKey<T> key, T defaultValue) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Set extra render data to the render state.
	 * @param key the key of the data
	 * @param value the data
	 * @param <T> the type of the data
	 */
	default <T> void setData(RenderStateDataKey<T> key, @Nullable T value) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Clears all extra render data on the render state.
	 */
	default void clearExtraData() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
