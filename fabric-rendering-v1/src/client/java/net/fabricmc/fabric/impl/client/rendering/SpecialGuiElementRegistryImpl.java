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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.VertexConsumerProvider;

import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.fabricmc.fabric.mixin.client.rendering.GuiRendererAccessor;

public final class SpecialGuiElementRegistryImpl {
	private static final List<SpecialGuiElementRegistry.Factory> factories = new ArrayList<>();
	private static final Collection<GuiRenderer> renderers = Collections.newSetFromMap(new WeakHashMap<>());

	private SpecialGuiElementRegistryImpl() {
	}

	public static void register(SpecialGuiElementRegistry.Factory factory) {
		factories.add(factory);

		for (GuiRenderer guiRenderer : renderers) {
			GuiRendererAccessor guiRendererAcc = (GuiRendererAccessor) guiRenderer;
			ContextImpl context = new ContextImpl(MinecraftClient.getInstance(), guiRendererAcc.getVertexConsumers());
			applyFactory(factory, context, guiRendererAcc.getSpecialElementRenderers());
		}
	}

	// Called after the vanilla special renderers are created.
	public static void onReady(GuiRenderer guiRenderer) {
		renderers.add(guiRenderer);

		GuiRendererAccessor guiRendererAcc = (GuiRendererAccessor) guiRenderer;
		ContextImpl context = new ContextImpl(MinecraftClient.getInstance(), guiRendererAcc.getVertexConsumers());

		for (SpecialGuiElementRegistry.Factory factory : factories) {
			applyFactory(factory, context, guiRendererAcc.getSpecialElementRenderers());
		}
	}

	private static void applyFactory(SpecialGuiElementRegistry.Factory factory, SpecialGuiElementRegistry.Context context,
									Map<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> specialElementRenderers) {
		SpecialGuiElementRenderer<?> elementRenderer = factory.createSpecialRenderer(context);
		specialElementRenderers.put(elementRenderer.getElementClass(), elementRenderer);
	}

	record ContextImpl(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers) implements SpecialGuiElementRegistry.Context { }
}
