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

import net.minecraft.client.gui.ScreenRect;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;

/**
 * This test mod renders a banner in the top left corner.
 */
public class SpecialGuiElementRendererTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		SpecialGuiElementRegistry.register(ctx -> new BannerGuiElementRenderer(ctx.vertexConsumers()));

		// TODO: Migrate to new HUD API once available
		//noinspection deprecation
		HudRenderCallback.EVENT.register((context, tickCounter) -> {
			context.state.addSpecialElement(new BannerGuiElementRenderState(20, 0, 40, 20, new ScreenRect(20, 0, 40, 20)));
		});
	}
}
