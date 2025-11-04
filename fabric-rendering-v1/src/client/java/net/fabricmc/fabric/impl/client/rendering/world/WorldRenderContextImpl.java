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

package net.fabricmc.fabric.impl.client.rendering.world;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.LevelRenderState;

import net.fabricmc.fabric.api.client.rendering.v1.world.AbstractWorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldTerrainRenderContext;

public final class WorldRenderContextImpl implements AbstractWorldRenderContext, WorldTerrainRenderContext, WorldRenderContext {
	private GameRenderer gameRenderer;
	private LevelRenderer worldRenderer;
	private LevelRenderState worldRenderState;

	private ChunkSectionsToRender sectionRenderState;
	private SubmitNodeCollector commandQueue;
	@Nullable
	private PoseStack matrixStack;
	private MultiBufferSource consumers;

	public void prepare(
			GameRenderer gameRenderer,
			LevelRenderer worldRenderer,
			LevelRenderState worldRenderState,
			ChunkSectionsToRender sectionRenderState,
			SubmitNodeCollector commandQueue,
			MultiBufferSource consumers
	) {
		this.gameRenderer = gameRenderer;
		this.worldRenderer = worldRenderer;
		this.worldRenderState = worldRenderState;
		this.sectionRenderState = sectionRenderState;

		this.commandQueue = commandQueue;
		this.consumers = consumers;

		matrixStack = null;
	}

	public void setMatrixStack(@Nullable PoseStack matrixStack) {
		this.matrixStack = matrixStack;
	}

	@Override
	public GameRenderer gameRenderer() {
		return gameRenderer;
	}

	@Override
	public LevelRenderer worldRenderer() {
		return worldRenderer;
	}

	@Override
	public LevelRenderState worldState() {
		return worldRenderState;
	}

	@Override
	public ChunkSectionsToRender sectionState() {
		return sectionRenderState;
	}

	@Override
	public SubmitNodeCollector commandQueue() {
		return commandQueue;
	}

	@Override
	@Nullable
	public PoseStack matrices() {
		return matrixStack;
	}

	@Override
	public MultiBufferSource consumers() {
		return consumers;
	}
}
