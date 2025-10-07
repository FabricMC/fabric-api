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

package net.fabricmc.fabric.api.client.rendering.v1.world;

import org.joml.Matrix4f;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;

/**
 * Except as noted below, the properties exposed here match the parameters passed to
 * {@link WorldRenderer#render}.
 */
public interface WorldExtractionContext extends AbstractWorldRenderContext {
	/**
	 * Convenient access to {WorldRenderer.world}.
	 *
	 * @return world renderer's client world instance
	 */
	ClientWorld world();

	Camera camera();

	/**
	 * The view frustum.
	 */
	Frustum frustum();

	RenderTickCounter tickCounter();

	Matrix4f positionMatrix();

	Matrix4f projectionMatrix();

	boolean blockOutlines();
}
