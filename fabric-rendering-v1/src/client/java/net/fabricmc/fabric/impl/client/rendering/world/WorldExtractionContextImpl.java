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
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.world.ClientWorld;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;

public class WorldExtractionContextImpl implements WorldExtractionContext {
	private GameRenderer gameRenderer;
	private WorldRenderer worldRenderer;
	private WorldRenderState worldRenderState;
	private ClientWorld world;
	private Camera camera;
	@Nullable
	private Frustum frustum;
	private RenderTickCounter tickCounter;
	private Matrix4f viewMatrix;
	private Matrix4f cullProjectionMatrix;
	private boolean blockOutlines;

	public void prepare(
			GameRenderer gameRenderer,
			WorldRenderer worldRenderer,
			WorldRenderState worldRenderState,
			ClientWorld world,
			RenderTickCounter tickCounter,
			boolean blockOutlines,
			Camera camera,
			Matrix4f viewMatrix,
			Matrix4f cullProjectionMatrix
	) {
		this.gameRenderer = gameRenderer;
		this.worldRenderer = worldRenderer;
		this.worldRenderState = worldRenderState;
		this.world = world;

		this.tickCounter = tickCounter;
		this.blockOutlines = blockOutlines;
		this.camera = camera;
		this.viewMatrix = viewMatrix;
		this.cullProjectionMatrix = cullProjectionMatrix;

		frustum = null;
	}

	public void setFrustum(@Nullable Frustum frustum) {
		this.frustum = frustum;
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
	public ClientWorld world() {
		return world;
	}

	@Override
	public Camera camera() {
		return camera;
	}

	@Override
	@Nullable
	public Frustum frustum() {
		return frustum;
	}

	@Override
	public RenderTickCounter tickCounter() {
		return this.tickCounter;
	}

	@Override
	public Matrix4fc viewMatrix() {
		return viewMatrix;
	}

	@Override
	public Matrix4fc cullProjectionMatrix() {
		return cullProjectionMatrix;
	}

	@Override
	public boolean blockOutlines() {
		return blockOutlines;
	}
}
