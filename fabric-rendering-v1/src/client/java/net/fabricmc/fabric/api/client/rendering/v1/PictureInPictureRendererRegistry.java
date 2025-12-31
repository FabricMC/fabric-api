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

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;

import net.fabricmc.fabric.impl.client.rendering.PictureInPictureRendererRegistryImpl;

/// Allows registering [picture-in-picture renderers][PictureInPictureRenderer],
/// used to render custom gui elements beyond the methods available in [GuiGraphics][net.minecraft.client.gui.GuiGraphics].
///
/// To render a custom gui element, first implement and register a [PictureInPictureRenderer].
/// When you want to render, add an instance of the corresponding render state to [GuiGraphics#guiRenderState][net.minecraft.client.gui.GuiGraphics#guiRenderState] using [GuiRenderState#submitPicturesInPictureState(PictureInPictureRenderState)][net.minecraft.client.gui.render.state.GuiRenderState#submitPicturesInPictureState(net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState)].
public final class PictureInPictureRendererRegistry {
	/// Registers a new [Factory] used to create a new [PictureInPictureRenderer] instance.
	public static void register(Factory factory) {
		Objects.requireNonNull(factory, "factory");
		PictureInPictureRendererRegistryImpl.register(factory);
	}

	/// A factory to create a new [PictureInPictureRenderer] instance.
	@FunctionalInterface
	public interface Factory {
		PictureInPictureRenderer<?> createRenderer(Context ctx);
	}

	@ApiStatus.NonExtendable
	public interface Context {
		/// @return the [MultiBufferSource.BufferSource].
		MultiBufferSource.BufferSource bufferSource();

		/// @return the [Minecraft] instance.
		Minecraft minecraft();

		/// @return the [SubmitNodeCollector] instance.
		SubmitNodeCollector submitNodeCollector();
	}
}
