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
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

import net.fabricmc.fabric.impl.client.rendering.PictureInPictureRendererRegistryImpl;

/**
 * Allows registering {@linkplain PictureInPictureRenderer picture-in-picture renderers},
 * used to render custom gui elements beyond the methods available in {@link net.minecraft.client.gui.GuiGraphicsExtractor GuiGraphics}.
 *
 * <p>To render a custom gui element, first implement and register a {@link PictureInPictureRenderer}.
 * When you want to render, add an instance of the corresponding render state to {@link net.minecraft.client.gui.GuiGraphicsExtractor#guiRenderState GuiGraphicsExtractor#guiRenderState} using {@link net.minecraft.client.renderer.state.gui.GuiRenderState#addPicturesInPictureState(PictureInPictureRenderState)}  GuiRenderState#addPicturesInPictureState(PictureInPictureRenderState)}.
 */
public final class PictureInPictureRendererRegistry {
	/**
	 * Registers a new {@link Factory} used to create a new {@link PictureInPictureRenderer} instance.
	 */
	public static void register(Factory factory) {
		Objects.requireNonNull(factory, "factory");
		PictureInPictureRendererRegistryImpl.register(factory);
	}

	/**
	 * A factory to create a new {@link PictureInPictureRenderer} instance.
	 */
	@FunctionalInterface
	public interface Factory {
		PictureInPictureRenderer<?> createRenderer(Context ctx);
	}

	@ApiStatus.NonExtendable
	public interface Context {
		/**
		 * @return the {@link MultiBufferSource.BufferSource}.
		 */
		MultiBufferSource.BufferSource bufferSource();

		/**
		 * @return the {@link Minecraft} instance.
		 */
		Minecraft minecraft();

		/**
		 * @return the {@link SubmitNodeCollector} instance.
		 */
		SubmitNodeCollector submitNodeCollector();
	}
}
