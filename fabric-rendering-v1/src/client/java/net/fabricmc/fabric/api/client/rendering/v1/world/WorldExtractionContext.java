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

import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4fc;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;

@ApiStatus.NonExtendable
public interface WorldExtractionContext extends AbstractWorldRenderContext {
	/**
	 * Convenient access to {@link LevelRenderer#level}.
	 *
	 * @return the world renderer's client world instance
	 */
	@SuppressWarnings("JavadocReference")
	ClientLevel world();

	Camera camera();

	Frustum frustum();

	DeltaTracker tickCounter();

	Matrix4fc viewMatrix();

	Matrix4fc cullProjectionMatrix();

	boolean blockOutlines();
}
