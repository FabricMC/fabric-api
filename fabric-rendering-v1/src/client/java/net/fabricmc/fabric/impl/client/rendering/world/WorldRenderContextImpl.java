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

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.SectionRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;

import net.fabricmc.fabric.api.client.rendering.v1.world.AbstractWorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldTerrainRenderContext;

public final class WorldRenderContextImpl implements AbstractWorldRenderContext, WorldTerrainRenderContext, WorldRenderContext {
	private GameRenderer gameRenderer;
	private WorldRenderer worldRenderer;
	private WorldRenderState worldRenderState;

	private SectionRenderState sectionRenderState;
	private OrderedRenderCommandQueue commandQueue;
	@Nullable
	private MatrixStack matrixStack;
	private VertexConsumerProvider consumers;

	public void prepare(
			GameRenderer gameRenderer,
			WorldRenderer worldRenderer,
			WorldRenderState worldRenderState,
			SectionRenderState sectionRenderState,
			OrderedRenderCommandQueue commandQueue,
			VertexConsumerProvider consumers
	) {
		this.gameRenderer = gameRenderer;
		this.worldRenderer = worldRenderer;
		this.worldRenderState = worldRenderState;
		this.sectionRenderState = sectionRenderState;

		this.commandQueue = commandQueue;
		this.consumers = consumers;

		matrixStack = null;
	}

	public void setMatrixStack(@Nullable MatrixStack matrixStack) {
		this.matrixStack = matrixStack;
	}

	@Override
	public GameRenderer gameRenderer() {
		return gameRenderer;
	}

	@Override
	public WorldRenderer worldRenderer() {
		return worldRenderer;
	}

	@Override
	public WorldRenderState worldState() {
		return worldRenderState;
	}

	@Override
	public SectionRenderState sectionState() {
		return sectionRenderState;
	}

	@Override
	public OrderedRenderCommandQueue commandQueue() {
		return commandQueue;
	}

	@Override
	@Nullable
	public MatrixStack matrices() {
		return matrixStack;
	}

	@Override
	public VertexConsumerProvider consumers() {
		return consumers;
	}
}
