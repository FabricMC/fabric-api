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

import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.VertexConsumerProvider;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Allows registering and modifying {@link SpecialGuiElementRenderer special gui element renderers},
 * used to render custom gui elements beyond the methods available in {@link net.minecraft.client.gui.DrawContext DrawContext}.
 *
 * <p>To render a custom gui element, first implement and register a {@link SpecialGuiElementRenderer}.
 * When you want to render, add an instance of the corresponding render state to {@link net.minecraft.client.gui.DrawContext#state DrawContext#state} using {@link net.minecraft.client.gui.render.state.GuiRenderState#addSpecialElement(net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState) GuiRenderState#addSpecialElement(SpecialGuiElementRenderState)}.
 */
public class SpecialGuiElementRendererRegistrationCallback {
	/**
	 * Event for registering and modifying special gui element renderers.
	 * Add a renderer by adding it to the provided list.
	 */
	public static final Event<BiConsumer<List<SpecialGuiElementRenderer<?>>, VertexConsumerProvider.Immediate>> EVENT = EventFactory.createArrayBacked(BiConsumer.class, listeners -> (specialElements, immediate) -> {
		for (BiConsumer<List<SpecialGuiElementRenderer<?>>, VertexConsumerProvider.Immediate> listener : listeners) {
			listener.accept(specialElements, immediate);
		}
	});

	private SpecialGuiElementRendererRegistrationCallback() { }
}
