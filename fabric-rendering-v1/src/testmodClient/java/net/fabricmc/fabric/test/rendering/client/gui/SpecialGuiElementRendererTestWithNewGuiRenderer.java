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

import java.util.Collections;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.util.DyeColor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * This test mod renders a second banner in the top left corner next to the one of
 * {@link SpecialGuiElementRendererTest}. It does so via a dedicated {@link GuiRenderer}.
 */
public class SpecialGuiElementRendererTestWithNewGuiRenderer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// BannerGuiElementRenderer is already registered by SpecialGuiElementRendererTest

		// TODO: Migrate to new HUD API once available
		//noinspection deprecation
		HudRenderCallback.EVENT.register((context, tickCounter) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			GuiRenderState newGuiRenderState = new GuiRenderState();
			DrawContext newContext = new DrawContext(mc, newGuiRenderState);

			newContext.state.addSpecialElement(new BannerGuiElementRenderState(DyeColor.BLUE, 60, 0, 80, 20, new ScreenRect(60, 0, 40, 20)));

			GpuBufferSlice orgProjectionMatrixBuffer = RenderSystem.getProjectionMatrixBuffer();
			ProjectionType orgProjectionType = RenderSystem.getProjectionType();
			GpuBufferSlice orgShaderFog = RenderSystem.getShaderFog();

			GuiRenderer guiRenderer = new GuiRenderer(newGuiRenderState, mc.getBufferBuilders().getEntityVertexConsumers(), mc.gameRenderer.getEntityRenderCommandQueue(), mc.gameRenderer.getEntityRenderDispatcher(), Collections.emptyList());
			FogRenderer fogRenderer = new FogRenderer();
			guiRenderer.render(fogRenderer.getFogBuffer(FogRenderer.FogType.NONE));
			fogRenderer.close();
			guiRenderer.close();

			RenderSystem.setProjectionMatrix(orgProjectionMatrixBuffer, orgProjectionType);
			RenderSystem.setShaderFog(orgShaderFog);
		});
	}
}
