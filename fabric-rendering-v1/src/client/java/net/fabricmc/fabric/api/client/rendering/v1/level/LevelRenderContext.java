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

package net.fabricmc.fabric.api.client.rendering.v1.level;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;

import net.fabricmc.fabric.impl.client.rendering.LevelRenderContextBackwardsCompatHack;

@ApiStatus.NonExtendable
public interface LevelRenderContext extends LevelTerrainRenderContext, LevelRenderContextBackwardsCompatHack {
	SubmitNodeCollector submitNodeCollector();

	PoseStack poseStack();

	/**
	 * The {@code MultiBufferSource} instance being used by the level renderer for most non-terrain renders.
	 * Generally this will be better for most use cases because quads for the same layer can be buffered
	 * incrementally and then drawn all at once by the level renderer.
	 *
	 * <p>IMPORTANT - all vertex coordinates sent to consumers should be relative to the camera to
	 * be consistent with other quads emitted by the level renderer and other mods.  If this isn't
	 * possible, caller should use a separate "immediate" instance.
	 *
	 * <p>Renders that cannot draw in one of the supported events must be drawn directly to the frame buffer,
	 * preferably in {@link LevelRenderEvents#END_MAIN} to avoid being overdrawn or cleared.
	 */
	@Override
	MultiBufferSource.BufferSource bufferSource();
}
