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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.BannerResultGuiElementRenderer;
import net.minecraft.client.gui.render.BookModelGuiElementRenderer;
import net.minecraft.client.gui.render.EntityGuiElementRenderer;
import net.minecraft.client.gui.render.PlayerSkinGuiElementRenderer;
import net.minecraft.client.gui.render.ProfilerChartGuiElementRenderer;
import net.minecraft.client.gui.render.SignGuiElementRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.BannerResultGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.BookModelGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.EntityGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.PlayerSkinGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.ProfilerChartGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.SignGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;

import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;

public final class SpecialGuiElementRegistryImpl {
	private static final List<SpecialGuiElementRegistry.Factory> FACTORIES = new ArrayList<>();
	private static final Map<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRegistry.Factory> REGISTERED_FACTORIES = new HashMap<>();
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
	public static void onReady(MinecraftClient client, VertexConsumerProvider.Immediate immediate, OrderedRenderCommandQueue orderedRenderCommandQueue, Map<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> specialElementRenderers) {
		frozen = true;

		registerVanillaFactories();

		ContextImpl context = new ContextImpl(client, immediate, orderedRenderCommandQueue);

		for (SpecialGuiElementRegistry.Factory factory : FACTORIES) {
			SpecialGuiElementRenderer<?> elementRenderer = factory.createSpecialRenderer(context);
			specialElementRenderers.put(elementRenderer.getElementClass(), elementRenderer);
			REGISTERED_FACTORIES.put(elementRenderer.getElementClass(), factory);
		}
	}

	@Nullable("null for render states registered outside FAPI")
	public static <S extends SpecialGuiElementRenderState> SpecialGuiElementRenderer<S> createNewRenderer(S state, MinecraftClient client, VertexConsumerProvider.Immediate immediate, OrderedRenderCommandQueue orderedRenderCommandQueue) {
		SpecialGuiElementRegistry.Factory factory = REGISTERED_FACTORIES.get(state.getClass());
		return factory == null ? null : (SpecialGuiElementRenderer<S>) factory.createSpecialRenderer(new ContextImpl(client, immediate, orderedRenderCommandQueue));
	}

	private static void registerVanillaFactories() {
		// Vanilla creates its special element renderers in the GameRenderer constructor
		REGISTERED_FACTORIES.put(EntityGuiElementRenderState.class, context -> new EntityGuiElementRenderer(context.vertexConsumers(), context.client().getEntityRenderDispatcher()));
		REGISTERED_FACTORIES.put(PlayerSkinGuiElementRenderState.class, context -> new PlayerSkinGuiElementRenderer(context.vertexConsumers()));
		REGISTERED_FACTORIES.put(BookModelGuiElementRenderState.class, context -> new BookModelGuiElementRenderer(context.vertexConsumers()));
		REGISTERED_FACTORIES.put(BannerResultGuiElementRenderState.class, context -> new BannerResultGuiElementRenderer(context.vertexConsumers(), context.client().getAtlasManager()));
		REGISTERED_FACTORIES.put(SignGuiElementRenderState.class, context -> new SignGuiElementRenderer(context.vertexConsumers(), context.client().getAtlasManager()));
		REGISTERED_FACTORIES.put(ProfilerChartGuiElementRenderState.class, context -> new ProfilerChartGuiElementRenderer(context.vertexConsumers()));
	}

	@VisibleForTesting
	public static Collection<Class<? extends SpecialGuiElementRenderState>> getRegisteredFactoryStateClasses() {
		return REGISTERED_FACTORIES.keySet();
	}

	record ContextImpl(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers, OrderedRenderCommandQueue orderedRenderCommandQueue) implements SpecialGuiElementRegistry.Context { }
}
