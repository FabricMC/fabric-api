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

package net.fabricmc.fabric.mixin.client.rendering;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.client.renderer.state.WorldBorderRenderState;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

@Mixin({
		EntityRenderState.class,
		BlockEntityRenderState.class,
		ItemStackRenderState.class,
		ItemStackRenderState.LayerRenderState.class,
		MapRenderState.class,
		MapRenderState.MapDecorationRenderState.class,
		MovingBlockRenderState.class,
		LevelRenderState.class,
		CameraRenderState.class,
		BlockOutlineRenderState.class,
		WeatherRenderState.class,
		WorldBorderRenderState.class,
		SkyRenderState.class
})
public abstract class RenderStateMixin implements FabricRenderState {
	@Unique
	@Nullable
	private Map<RenderStateDataKey<?>, Object> renderStateData;

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T getData(RenderStateDataKey<T> key) {
		return renderStateData == null ? null : (T) renderStateData.get(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getDataOrDefault(RenderStateDataKey<T> key, T defaultValue) {
		return renderStateData == null ? defaultValue : (T) renderStateData.getOrDefault(key, defaultValue);
	}

	@Override
	public <T> void setData(RenderStateDataKey<T> key, T value) {
		if (renderStateData == null) {
			renderStateData = new Reference2ObjectOpenHashMap<>();
		}

		renderStateData.put(key, value);
	}

	@Override
	public void clearExtraData() {
		if (renderStateData != null) {
			renderStateData.clear();
		}
	}
}
