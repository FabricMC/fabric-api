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

package net.fabricmc.fabric.impl.client.rendering;

import org.joml.Matrix4f;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public final class WorldRenderContextImpl implements WorldRenderContext.BlockOutlineContext, WorldRenderContext {
	private WorldRenderer worldRenderer;
	private RenderTickCounter tickCounter;
	private MatrixStack matrixStack;
	private boolean blockOutlines;
	private Camera camera;
	private Frustum frustum;
	private GameRenderer gameRenderer;
	private Matrix4f projectionMatrix;
	private Matrix4f positionMatrix;
	private VertexConsumerProvider consumers;
	private boolean advancedTranslucency;
	private ClientWorld world;

	private Entity entity;
	private double cameraX;
	private double cameraY;
	private double cameraZ;
	private BlockPos blockPos;
	private BlockState blockState;

	public boolean renderBlockOutline = true;

	public void prepare(
			WorldRenderer worldRenderer,
			RenderTickCounter delta,
			boolean blockOutlines,
			Camera camera,
			GameRenderer gameRenderer,
			Matrix4f projectionMatrix,
			Matrix4f positionMatrix,
			VertexConsumerProvider consumers,
			boolean advancedTranslucency,
			ClientWorld world
	) {
		this.worldRenderer = worldRenderer;
		this.tickCounter = delta;
		this.matrixStack = null;
		this.blockOutlines = blockOutlines;
		this.camera = camera;
		this.gameRenderer = gameRenderer;
		this.projectionMatrix = projectionMatrix;
		this.positionMatrix = positionMatrix;
		this.consumers = consumers;
		this.advancedTranslucency = advancedTranslucency;
		this.world = world;
	}

	public void setFrustum(Frustum frustum) {
		this.frustum = frustum;
	}

	public void setMatrixStack(MatrixStack matrixStack) {
		this.matrixStack = matrixStack;
	}

	public void prepareBlockOutline(
			Entity entity,
			double cameraX,
			double cameraY,
			double cameraZ,
			BlockPos blockPos,
			BlockState blockState
	) {
		this.entity = entity;
		this.cameraX = cameraX;
		this.cameraY = cameraY;
		this.cameraZ = cameraZ;
		this.blockPos = blockPos;
		this.blockState = blockState;
	}

	@Override
	public WorldRenderer worldRenderer() {
		return worldRenderer;
	}

	@Override
	public MatrixStack matrixStack() {
		return matrixStack;
	}

	@Override
	public RenderTickCounter tickCounter() {
		return this.tickCounter;
	}

	@Override
	public boolean blockOutlines() {
		return blockOutlines;
	}

	@Override
	public Camera camera() {
		return camera;
	}

	@Override
	public Matrix4f projectionMatrix() {
		return projectionMatrix;
	}

	@Override
	public Matrix4f positionMatrix() {
		return positionMatrix;
	}

	@Override
	public ClientWorld world() {
		return world;
	}

	@Override
	public Frustum frustum() {
		return frustum;
	}

	@Override
	public VertexConsumerProvider consumers() {
		return consumers;
	}

	@Override
	public GameRenderer gameRenderer() {
		return gameRenderer;
	}

	@Override
	public boolean advancedTranslucency() {
		return advancedTranslucency;
	}

	@Override
	public VertexConsumer vertexConsumer() {
		return consumers.getBuffer(RenderLayer.getLines());
	}

	@Override
	public Entity entity() {
		return entity;
	}

	@Override
	public double cameraX() {
		return cameraX;
	}

	@Override
	public double cameraY() {
		return cameraY;
	}

	@Override
	public double cameraZ() {
		return cameraZ;
	}

	@Override
	public BlockPos blockPos() {
		return blockPos;
	}

	@Override
	public BlockState blockState() {
		return blockState;
	}
}
