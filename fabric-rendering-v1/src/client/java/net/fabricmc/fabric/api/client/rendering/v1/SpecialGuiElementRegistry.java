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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.VertexConsumerProvider;

import net.fabricmc.fabric.impl.client.rendering.SpecialGuiElementRegistryImpl;

/**
 * Allows registering {@linkplain SpecialGuiElementRenderer special gui element renderers},
 * used to render custom gui elements beyond the methods available in {@link net.minecraft.client.gui.DrawContext DrawContext}.
 *
 * <p>To render a custom gui element, first implement and register a {@link SpecialGuiElementRenderer}.
 * When you want to render, add an instance of the corresponding render state to {@link net.minecraft.client.gui.DrawContext#state DrawContext#state} using {@link net.minecraft.client.gui.render.state.GuiRenderState#addSpecialElement(net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState) GuiRenderState#addSpecialElement(SpecialGuiElementRenderState)}.
 */
public final class SpecialGuiElementRegistry {
	/**
	 * Registers a new {@link Factory} used to create a new {@link SpecialGuiElementRenderer} instance.
	 */
	public static void register(Factory factory) {
		Objects.requireNonNull(factory, "factory");
		SpecialGuiElementRegistryImpl.register(factory);
	}

	/**
	 * A factory to create a new {@link SpecialGuiElementRenderer} instance.
	 */
	@FunctionalInterface
	public interface Factory {
		SpecialGuiElementRenderer<?> createSpecialRenderer(Context ctx);
	}

	@ApiStatus.NonExtendable
	public interface Context {
		/**
		 * @return the {@link VertexConsumerProvider.Immediate}.
		 */
		VertexConsumerProvider.Immediate vertexConsumers();

		/**
		 * @return the {@link MinecraftClient} instance.
		 */
		MinecraftClient client();
	}
}
