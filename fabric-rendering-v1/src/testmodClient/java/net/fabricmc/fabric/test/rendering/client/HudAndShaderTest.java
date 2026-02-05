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

package net.fabricmc.fabric.test.rendering.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * Tests {@link HudRenderCallback} and custom shaders by drawing a green rectangle
 * in the lower-right corner of the screen.
 */
public class HudAndShaderTest implements ClientModInitializer {
	/* TODO 1.21.5
	private static final ShaderProgramKey TEST_SHADER = new ShaderProgramKey(
			Identifier.of("fabric-rendering-v1-testmod", "core/test"),
			VertexFormats.POSITION, Defines.EMPTY);
	 */

	@Override
	public void onInitializeClient() {
		/* TODO 1.21.5
		ShaderProgramKeys.getAll().add(TEST_SHADER);

		HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
			if (true) {
				// TODO needs fixing for 1.21.5, check debug text color
				return;
			}

			MinecraftClient client = MinecraftClient.getInstance();
			Window window = client.getWindow();
			int x = window.getScaledWidth() - 15;
			int y = window.getScaledHeight() - 15;
			RenderSystem.setShader(TEST_SHADER);
			RenderSystem.setShaderColor(0f, 1f, 0f, 1f);
			Matrix4f positionMatrix = graphics.getMatrices().peek().getPositionMatrix();

			graphics.draw(multiBufferSource -> {
				VertexConsumer buffer = multiBufferSource.getBuffer(RenderLayer.getGui());
				buffer.vertex(positionMatrix, x, y, 50).color(255, 255, 255, 255);
				buffer.vertex(positionMatrix, x, y + 10, 50).color(255, 255, 255, 255);
				buffer.vertex(positionMatrix, x + 10, y + 10, 50).color(255, 255, 255, 255);
				buffer.vertex(positionMatrix, x + 10, y, 50).color(255, 255, 255, 255);
			});

			// Reset shader color
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		});
		 */
	}
}
