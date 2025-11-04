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

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;

public class WorldExtractionContextImpl implements WorldExtractionContext {
	private GameRenderer gameRenderer;
	private LevelRenderer worldRenderer;
	private LevelRenderState worldRenderState;
	private ClientLevel world;
	private Camera camera;
	@Nullable
	private Frustum frustum;
	private DeltaTracker tickCounter;
	private Matrix4f viewMatrix;
	private Matrix4f cullProjectionMatrix;
	private boolean blockOutlines;

	public void prepare(
			GameRenderer gameRenderer,
			LevelRenderer worldRenderer,
			LevelRenderState worldRenderState,
			ClientLevel world,
			DeltaTracker tickCounter,
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
	public LevelRenderer worldRenderer() {
		return worldRenderer;
	}

	@Override
	public LevelRenderState worldState() {
		return worldRenderState;
	}

	@Override
	public ClientLevel world() {
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
	public DeltaTracker tickCounter() {
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
