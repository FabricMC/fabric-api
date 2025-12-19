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

package net.fabricmc.fabric.impl.client.rendering.level;

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

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;

public class LevelExtractionContextImpl implements LevelExtractionContext {
	private GameRenderer gameRenderer;
	private LevelRenderer levelRenderer;
	private LevelRenderState levelRenderState;
	private ClientLevel level;
	private Camera camera;
	@Nullable
	private Frustum frustum;
	private DeltaTracker deltaTracker;
	private Matrix4f viewMatrix;
	private Matrix4f cullProjectionMatrix;
	private boolean blockOutlines;

	public void prepare(
			GameRenderer gameRenderer,
			LevelRenderer levelRenderer,
			LevelRenderState levelRenderState,
			ClientLevel level,
			DeltaTracker deltaTracker,
			boolean blockOutlines,
			Camera camera,
			Matrix4f viewMatrix,
			Matrix4f cullProjectionMatrix
	) {
		this.gameRenderer = gameRenderer;
		this.levelRenderer = levelRenderer;
		this.levelRenderState = levelRenderState;
		this.level = level;

		this.deltaTracker = deltaTracker;
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
	public LevelRenderer levelRenderer() {
		return levelRenderer;
	}

	@Override
	public LevelRenderState levelState() {
		return levelRenderState;
	}

	@Override
	public ClientLevel level() {
		return level;
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
	public DeltaTracker deltaTracker() {
		return this.deltaTracker;
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
