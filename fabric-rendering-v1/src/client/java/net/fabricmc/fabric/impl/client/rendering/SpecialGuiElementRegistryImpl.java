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

import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.GuiBannerResultRenderer;
import net.minecraft.client.gui.render.pip.GuiBookModelRenderer;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.pip.GuiProfilerChartRenderer;
import net.minecraft.client.gui.render.pip.GuiSignRenderer;
import net.minecraft.client.gui.render.pip.GuiSkinRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.GuiBannerResultRenderState;
import net.minecraft.client.gui.render.state.pip.GuiBookModelRenderState;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.gui.render.state.pip.GuiProfilerChartRenderState;
import net.minecraft.client.gui.render.state.pip.GuiSignRenderState;
import net.minecraft.client.gui.render.state.pip.GuiSkinRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;

import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;

public final class SpecialGuiElementRegistryImpl {
	private static final List<SpecialGuiElementRegistry.Factory> FACTORIES = new ArrayList<>();
	private static final Map<Class<? extends PictureInPictureRenderState>, SpecialGuiElementRegistry.Factory> REGISTERED_FACTORIES = new HashMap<>();
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
	public static void onReady(Minecraft client, MultiBufferSource.BufferSource immediate, SubmitNodeCollector orderedRenderCommandQueue, Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> specialElementRenderers) {
		frozen = true;

		registerVanillaFactories();

		ContextImpl context = new ContextImpl(client, immediate, orderedRenderCommandQueue);

		for (SpecialGuiElementRegistry.Factory factory : FACTORIES) {
			PictureInPictureRenderer<?> elementRenderer = factory.createSpecialRenderer(context);
			specialElementRenderers.put(elementRenderer.getRenderStateClass(), elementRenderer);
			REGISTERED_FACTORIES.put(elementRenderer.getRenderStateClass(), factory);
		}
	}

	// null for render states registered outside FAPI
	@Nullable
	public static <S extends PictureInPictureRenderState> PictureInPictureRenderer<S> createNewRenderer(S state, Minecraft client, MultiBufferSource.BufferSource immediate, SubmitNodeCollector orderedRenderCommandQueue) {
		SpecialGuiElementRegistry.Factory factory = REGISTERED_FACTORIES.get(state.getClass());
		return factory == null ? null : (PictureInPictureRenderer<S>) factory.createSpecialRenderer(new ContextImpl(client, immediate, orderedRenderCommandQueue));
	}

	private static void registerVanillaFactories() {
		// Vanilla creates its special element renderers in the GameRenderer constructor
		REGISTERED_FACTORIES.put(GuiEntityRenderState.class, context -> new GuiEntityRenderer(context.vertexConsumers(), context.client().getEntityRenderDispatcher()));
		REGISTERED_FACTORIES.put(GuiSkinRenderState.class, context -> new GuiSkinRenderer(context.vertexConsumers()));
		REGISTERED_FACTORIES.put(GuiBookModelRenderState.class, context -> new GuiBookModelRenderer(context.vertexConsumers()));
		REGISTERED_FACTORIES.put(GuiBannerResultRenderState.class, context -> new GuiBannerResultRenderer(context.vertexConsumers(), context.client().getAtlasManager()));
		REGISTERED_FACTORIES.put(GuiSignRenderState.class, context -> new GuiSignRenderer(context.vertexConsumers(), context.client().getAtlasManager()));
		REGISTERED_FACTORIES.put(GuiProfilerChartRenderState.class, context -> new GuiProfilerChartRenderer(context.vertexConsumers()));
	}

	@VisibleForTesting
	public static Collection<Class<? extends PictureInPictureRenderState>> getRegisteredFactoryStateClasses() {
		return REGISTERED_FACTORIES.keySet();
	}

	record ContextImpl(Minecraft client, MultiBufferSource.BufferSource vertexConsumers, SubmitNodeCollector orderedRenderCommandQueue) implements SpecialGuiElementRegistry.Context { }
}
