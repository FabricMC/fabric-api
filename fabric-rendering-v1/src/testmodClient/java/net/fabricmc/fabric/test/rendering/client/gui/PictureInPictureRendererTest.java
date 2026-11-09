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

package net.fabricmc.fabric.test.rendering.client.gui;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.social.EntityPortraitWidget;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.impl.client.rendering.PictureInPictureRendererRegistryImpl;
import net.fabricmc.fabric.test.rendering.client.mixin.GameRendererAccessor;
import net.fabricmc.fabric.test.rendering.client.mixin.GuiRendererAccessor;

/**
 * This test mod renders two banners and two signs in the top left corner.
 */
public class PictureInPictureRendererTest implements ClientModInitializer, FabricClientGameTest {
	private static int rendererCreations;
	@Override
	public void onInitializeClient() {
		PictureInPictureRendererRegistry.register(ctx -> {
			rendererCreations++;
			return new BannerGuiElementRenderer();
		});

		HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "pip"), graphics -> {
			// render it twice to test that PiPs can be added multiple times in the same frame
			graphics.guiRenderState.addPicturesInPictureState(new BannerGuiElementRenderState(DyeColor.BLUE, 20, 0, 40, 20, new ScreenRectangle(20, 0, 40, 20)));
			graphics.guiRenderState.addPicturesInPictureState(new BannerGuiElementRenderState(DyeColor.RED, 40, 0, 60, 20, new ScreenRectangle(40, 0, 60, 20)));
		});

		// Test that InventoryScreen.drawEntity works with the same type of entity more than once
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof InventoryScreen) {
				ScreenEvents.afterExtract(screen).register((screen1, graphics) -> {
					// no need to modify anything about this player, since they're in different locations they will be
					// looking towards the mouse at different angles
					EntityPortraitWidget.extractEntityInInventoryFollowsMouse(graphics, 26, 8, 75, 78, 30.0F, 0.0625F, client.player);
				});
			}
		});
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		context.runOnClient(client -> {
			// A renderer constructed outside GameRenderer must also receive registered PiP renderers.
			GuiRenderState state = new GuiRenderState();
			state.addPicturesInPictureState(new BannerGuiElementRenderState(DyeColor.BLUE, 20, 0, 40, 20, null));
			state.addPicturesInPictureState(new BannerGuiElementRenderState(DyeColor.RED, 40, 0, 60, 20, null));
			GpuBufferSlice projection = RenderSystem.getProjectionMatrixBuffer();
			ProjectionType projectionType = RenderSystem.getProjectionType();
			GpuBufferSlice fog = RenderSystem.getShaderFog();

			try {
				int creationsBefore = rendererCreations;

				try (GuiRenderer empty = new GuiRenderer(new GuiRenderState(), client.gameRenderer.featureRenderDispatcher(), List.of())) {
					empty.render();
				}

				if (rendererCreations != creationsBefore) {
					throw new AssertionError("Created unused PiP renderers in an empty secondary GuiRenderer");
				}

				try (GuiRenderer secondary = new GuiRenderer(state, client.gameRenderer.featureRenderDispatcher(), List.of())) {
					secondary.render();

					if (!((GuiRendererAccessor) secondary).getSpecialElementRenderers().containsKey(BannerGuiElementRenderState.class)) {
						throw new AssertionError("Missing registered PiP renderer in secondary GuiRenderer");
					}

					if (rendererCreations != creationsBefore + 2) {
						throw new AssertionError("Expected separate PiP renderers for two states in secondary GuiRenderer");
					}
				}
			} finally {
				RenderSystem.setProjectionMatrix(projection, projectionType);
				RenderSystem.setShaderFog(fog);
			}

			GuiRenderer guiRenderer = ((GameRendererAccessor) client.gameRenderer).getGuiRenderer();
			Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> specialElementRenderers = ((GuiRendererAccessor) guiRenderer).getSpecialElementRenderers();
			Set<Class<? extends PictureInPictureRenderState>> missingRenderFactories = new HashSet<>(specialElementRenderers.keySet());

			for (Class<? extends PictureInPictureRenderState> registeredFactoryStateClass : PictureInPictureRendererRegistryImpl.getRegisteredFactoryStateClasses()) {
				missingRenderFactories.remove(registeredFactoryStateClass);
			}

			if (!missingRenderFactories.isEmpty()) {
				String missingFactoriesString = missingRenderFactories.stream().map(Class::getSimpleName).sorted().collect(Collectors.joining(", "));
				throw new AssertionError("Missing PiP render factories for state classes: " + missingFactoriesString + ". "
						+ "Please add them to PictureInPictureRendererRegistryImpl.registerVanillaFactories");
			}
		});
	}
}
