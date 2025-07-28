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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.VertexConsumerProvider;

import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;

public final class SpecialGuiElementRegistryImpl {
	private static final List<SpecialGuiElementRegistry.Factory> FACTORIES = new ArrayList<>();
	private static boolean frozen;

	private SpecialGuiElementRegistryImpl() {
	}

	public static void register(SpecialGuiElementRegistry.Factory factory) {
		if (frozen) {
			throw new IllegalStateException("Too late to register, GuiRenderer has already been initialized.");
		}

		FACTORIES.add(factory);
	}

	// Called after the vanilla special renderers are created.
	public static void onReady(MinecraftClient client, VertexConsumerProvider.Immediate immediate,
								Map<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> specialElementRenderers) {
		frozen = true;

		ContextImpl context = new ContextImpl(client, immediate);

		for (SpecialGuiElementRegistry.Factory factory : FACTORIES) {
			SpecialGuiElementRenderer<?> elementRenderer = factory.createSpecialRenderer(context);
			specialElementRenderers.put(elementRenderer.getElementClass(), elementRenderer);
		}
	}

	record ContextImpl(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers) implements SpecialGuiElementRegistry.Context { }
}
