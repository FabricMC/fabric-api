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

import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;

public final class SpecialGuiElementRegistryImpl {
	private static RegistrationHandler registrationHandler = new EarlyRegistrationHandler();
	private static final Map<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRegistry.Factory> FACTORIES = new HashMap<>();

	static {
		registerVanillaFactories();
	}

	private SpecialGuiElementRegistryImpl() {
	}

	public static void register(SpecialGuiElementRegistry.Factory factory) {
		registrationHandler.register(factory);
	}

	// Called after the vanilla special renderers are created.
	public static void onReady(MinecraftClient client, VertexConsumerProvider.Immediate immediate,
								Map<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> specialElementRenderers) {
		switch (registrationHandler) {
		case EarlyRegistrationHandler handler -> registrationHandler = handler.onCreated(client, immediate, specialElementRenderers);
		case LateRegistrationHandler handler -> throw new IllegalStateException("Already transitioned to late registration handler");
		}
	}

	@Nullable("null for render states registered outside FAPI")
	public static <S extends SpecialGuiElementRenderState> SpecialGuiElementRenderer<S> createNewRenderer(S state, MinecraftClient client, VertexConsumerProvider.Immediate immediate) {
		SpecialGuiElementRegistry.Factory factory = FACTORIES.get(state.getClass());
		return factory == null ? null : (SpecialGuiElementRenderer<S>) factory.createSpecialRenderer(new ContextImpl(client, immediate));
	}

	private static void registerVanillaFactories() {
		// Vanilla creates its special element renderers in the GameRenderer constructor
		FACTORIES.put(EntityGuiElementRenderState.class, context -> new EntityGuiElementRenderer(context.vertexConsumers(), context.client().getEntityRenderDispatcher()));
		FACTORIES.put(PlayerSkinGuiElementRenderState.class, context -> new PlayerSkinGuiElementRenderer(context.vertexConsumers()));
		FACTORIES.put(BookModelGuiElementRenderState.class, context -> new BookModelGuiElementRenderer(context.vertexConsumers()));
		FACTORIES.put(BannerResultGuiElementRenderState.class, context -> new BannerResultGuiElementRenderer(context.vertexConsumers()));
		FACTORIES.put(SignGuiElementRenderState.class, context -> new SignGuiElementRenderer(context.vertexConsumers()));
		FACTORIES.put(ProfilerChartGuiElementRenderState.class, context -> new ProfilerChartGuiElementRenderer(context.vertexConsumers()));
	}

	@VisibleForTesting
	public static Collection<Class<? extends SpecialGuiElementRenderState>> getRegisteredFactoryStateClasses() {
		return FACTORIES.keySet();
	}

	private sealed interface RegistrationHandler permits EarlyRegistrationHandler, LateRegistrationHandler {
		void register(SpecialGuiElementRegistry.Factory factory);

		default void applyFactory(SpecialGuiElementRegistry.Factory factory, SpecialGuiElementRegistry.Context context,
									Map<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> specialElementRenderers) {
			SpecialGuiElementRenderer<?> elementRenderer = factory.createSpecialRenderer(context);
			specialElementRenderers.put(elementRenderer.getElementClass(), elementRenderer);
			FACTORIES.put(elementRenderer.getElementClass(), factory);
		}
	}

	// Handle calls to register before the vanilla special renderers are created.
	private static final class EarlyRegistrationHandler implements RegistrationHandler {
		private List<SpecialGuiElementRegistry.Factory> pendingFactories = new ArrayList<>();

		@Override
		public void register(SpecialGuiElementRegistry.Factory factory) {
			pendingFactories.add(factory);
		}

		// Transition to late registration handler after the vanilla special renderers are created.
		public LateRegistrationHandler onCreated(MinecraftClient client, VertexConsumerProvider.Immediate immediate,
												Map<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> specialElementRenderers) {
			var context = new ContextImpl(client, immediate);

			for (SpecialGuiElementRegistry.Factory factory : pendingFactories) {
				applyFactory(factory, context, specialElementRenderers);
			}

			return new LateRegistrationHandler(context, specialElementRenderers);
		}
	}

	// Handle calls to register after the vanilla special renderers are created.
	private record LateRegistrationHandler(SpecialGuiElementRegistry.Context context,
														Map<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> specialElementRenderers) implements RegistrationHandler {
		@Override
		public void register(SpecialGuiElementRegistry.Factory factory) {
			applyFactory(factory, context, specialElementRenderers);
		}
	}

	record ContextImpl(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers) implements SpecialGuiElementRegistry.Context { }
}
