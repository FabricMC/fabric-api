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

package net.fabricmc.fabric.mixin.client.rendering.renderstate;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.PanoramaRenderState;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.CameraEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.client.renderer.state.level.WorldBorderRenderState;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

@Mixin({
		BlockModelRenderState.class,
		MovingBlockRenderState.class,
		BlockEntityRenderState.class,
		EntityRenderState.class,
		EntityRenderState.LeashState.class,
		FogData.class,
		ItemStackRenderState.class,
		ItemStackRenderState.LayerRenderState.class,
		GameRenderState.class,
		LightmapRenderState.class,
		MapRenderState.class,
		MapRenderState.MapDecorationRenderState.class,
		OptionsRenderState.class,
		WindowRenderState.class,
		GuiRenderState.class,
		PanoramaRenderState.class,
		BlockBreakingRenderState.class,
		BlockOutlineRenderState.class,
		CameraEntityRenderState.class,
		CameraRenderState.class,
		LevelRenderState.class,
		ParticlesRenderState.class,
		SkyRenderState.class,
		WeatherRenderState.class,
		WorldBorderRenderState.class
})
abstract class RenderStateMixin implements FabricRenderState {
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
