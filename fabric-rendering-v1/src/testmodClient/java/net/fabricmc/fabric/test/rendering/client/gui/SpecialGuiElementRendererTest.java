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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.block.WoodType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SignGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.util.DyeColor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.impl.client.rendering.SpecialGuiElementRegistryImpl;
import net.fabricmc.fabric.test.rendering.client.mixin.GameRendererAccessor;
import net.fabricmc.fabric.test.rendering.client.mixin.GuiRendererAccessor;

/**
 * This test mod renders two banners and two signs in the top left corner.
 */
public class SpecialGuiElementRendererTest implements ClientModInitializer, FabricClientGameTest {
	@Override
	public void onInitializeClient() {
		SpecialGuiElementRegistry.register(ctx -> new BannerGuiElementRenderer(ctx.vertexConsumers()));

		// TODO: Migrate to new HUD API once available
		//noinspection deprecation
		HudRenderCallback.EVENT.register((context, tickCounter) -> {
			// render it twice to test that special GUI elements can be added multiple times in the same frame
			context.state.addSpecialElement(new BannerGuiElementRenderState(DyeColor.BLUE, 20, 0, 40, 20, new ScreenRect(20, 0, 40, 20)));
			context.state.addSpecialElement(new BannerGuiElementRenderState(DyeColor.RED, 40, 0, 60, 20, new ScreenRect(40, 0, 60, 20)));

			// also render some vanilla special GUI elements to check that they still work and can be rendered multiple times
			context.state.addSpecialElement(createSignState(60, WoodType.BIRCH));
			context.state.addSpecialElement(createSignState(80, WoodType.DARK_OAK));
		});

		// Test that InventoryScreen.drawEntity works with the same type of entity more than once
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof InventoryScreen) {
				ScreenEvents.afterRender(screen).register((screen1, context, mouseX, mouseY, tickDelta) -> {
					// no need to modify anything about this player, since they're in different locations they will be
					// looking towards the mouse at different angles
					InventoryScreen.drawEntity(context, 26, 8, 75, 78, 30, 0.0625F, mouseX, mouseY, client.player);
				});
			}
		});
	}

	private static SignGuiElementRenderState createSignState(int x, WoodType woodType) {
		Model.SinglePartModel signModel = SignBlockEntityRenderer.createSignModel(MinecraftClient.getInstance().getLoadedEntityModels(), woodType, true);
		return new SignGuiElementRenderState(signModel, woodType, x, 0, x + 20, 20, 10f, new ScreenRect(x, 0, x + 20, 20));
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		context.runOnClient(client -> {
			GuiRenderer guiRenderer = ((GameRendererAccessor) client.gameRenderer).getGuiRenderer();
			Map<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> specialElementRenderers = ((GuiRendererAccessor) guiRenderer).getSpecialElementRenderers();
			Set<Class<? extends SpecialGuiElementRenderState>> missingRenderFactories = new HashSet<>(specialElementRenderers.keySet());

			for (Class<? extends SpecialGuiElementRenderState> registeredFactoryStateClass : SpecialGuiElementRegistryImpl.getRegisteredFactoryStateClasses()) {
				missingRenderFactories.remove(registeredFactoryStateClass);
			}

			if (!missingRenderFactories.isEmpty()) {
				String missingFactoriesString = missingRenderFactories.stream().map(Class::getSimpleName).sorted().collect(Collectors.joining(", "));
				throw new AssertionError("Missing special GUI element render factories for state classes: " + missingFactoriesString + ". "
						+ "Please add them to SpecialGuiElementRegistryImpl.registerVanillaFactories");
			}
		});
	}
}
