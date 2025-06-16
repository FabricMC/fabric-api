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

package net.fabricmc.fabric.api.client.rendering.v1;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.impl.client.rendering.WorldRendererHooks;

/**
 * Except as noted below, the properties exposed here match the parameters passed to
 * {@link WorldRenderer#render}.
 */
public interface WorldRenderContext {
	/**
	 * Returns the {@code WorldRenderContext} for the given {@code WorldRenderer} instance, for use in cases where you
	 * have access to the world renderer but not the world render context. World render events always pass the world
	 * render context as a parameter, so always prefer to use that over this method.
	 *
	 * @param worldRenderer The world renderer
	 * @return The world render context for the world renderer
	 * @throws IllegalStateException If not currently rendering the world
	 */
	static WorldRenderContext getInstance(WorldRenderer worldRenderer) {
		Preconditions.checkNotNull(worldRenderer, "worldRenderer");
		return ((WorldRendererHooks) worldRenderer).fabric$getWorldRenderContext();
	}

	/**
	 * The world renderer instance doing the rendering and invoking the event.
	 *
	 * @return WorldRenderer instance invoking the event
	 */
	WorldRenderer worldRenderer();

	RenderTickCounter tickCounter();

	boolean blockOutlines();

	Camera camera();

	GameRenderer gameRenderer();

	Matrix4f positionMatrix();

	Matrix4f projectionMatrix();

	/**
	 * Convenient access to {WorldRenderer.world}.
	 *
	 * @return world renderer's client world instance
	 */
	ClientWorld world();

	/**
	 * Test to know if "fabulous" graphics mode is enabled.
	 *
	 * <p>Use this for renders that need to render on top of all translucency to activate or deactivate different
	 * event handlers to get optimal depth testing results. When fabulous is off, it may be better to render
	 * during {@code WorldRenderLastCallback} after clouds and weather are drawn. Conversely, when fabulous mode is on,
	 * it may be better to draw during {@code WorldRenderPostTranslucentCallback}, before the fabulous mode composite
	 * shader runs, depending on which translucent buffer is being targeted.
	 *
	 * @return {@code true} when "fabulous" graphics mode is enabled.
	 */
	boolean advancedTranslucency();

	/**
	 * The {@code VertexConsumerProvider} instance being used by the world renderer for most non-terrain renders.
	 * Generally this will be better for most use cases because quads for the same layer can be buffered
	 * incrementally and then drawn all at once by the world renderer.
	 *
	 * <p>IMPORTANT - all vertex coordinates sent to consumers should be relative to the camera to
	 * be consistent with other quads emitted by the world renderer and other mods.  If this isn't
	 * possible, caller should use a separate "immediate" instance.
	 *
	 * <p>This property is {@code null} before {@link WorldRenderEvents#BEFORE_ENTITIES} and after
	 * {@link WorldRenderEvents#BLOCK_OUTLINE} (translucent) because the consumer buffers are not available before or
	 * drawn after that in vanilla world rendering.  Renders that cannot draw in one of the supported events
	 * must be drawn directly to the frame buffer, preferably in {@link WorldRenderEvents#LAST} to avoid being
	 * overdrawn or cleared.
	 */
	@Nullable
	VertexConsumerProvider consumers();

	/**
	 * View frustum, after it is initialized. Will be {@code null} during
	 * {@link WorldRenderEvents#START}.
	 */
	@Nullable
	Frustum frustum();

	/**
	 * The matrix stack is only not null in {@link WorldRenderEvents#AFTER_ENTITIES} or later events.
	 */
	@Nullable
	MatrixStack matrixStack();

	/**
	 * Meant to be used in {@link WorldRenderEvents#BEFORE_BLOCK_OUTLINE} and {@link WorldRenderEvents#BLOCK_OUTLINE}.
	 * @return {@code true} if the current block outline is being rendered after translucent terrain; {@code false} if
	 * it is being rendered after solid terrain
	 */
	boolean translucentBlockOutline();

	/**
	 * Used in {@link WorldRenderEvents#BLOCK_OUTLINE} to convey the parameters normally sent to
	 * {@code WorldRenderer.drawBlockOutline}.
	 */
	interface BlockOutlineContext {
		Entity entity();

		double cameraX();

		double cameraY();

		double cameraZ();

		BlockPos blockPos();

		BlockState blockState();

		/**
		 * @deprecated Use {@link #consumers()} directly.
		 */
		@Deprecated
		VertexConsumer vertexConsumer();
	}
}
