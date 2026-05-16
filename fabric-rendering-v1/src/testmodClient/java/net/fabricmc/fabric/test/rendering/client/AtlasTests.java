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

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.AtlasRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public class AtlasTests implements ClientModInitializer {
	private static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "textures/atlases/test_atlas.png");
	private static final Identifier ATLAS_ID = Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "test_atlas");

	@Override
	public void onInitializeClient() {
		AtlasRegistry.register(TEXTURE_ID, ATLAS_ID, false);

		LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
			final TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(ATLAS_ID).getSprite(Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "item/double_iron_ingot"));
			context.submitNodeCollector().submitCustomGeometry(
					context.poseStack(),
					RenderTypes.entityCutoutCull(TEXTURE_ID),
					(pose, consumer) -> {
						final VertexConsumer spriteConsumer = sprite.wrap(consumer);
						spriteConsumer.addVertex(0, 0, 1, -1, 0, 1, OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_BRIGHT, 0, 0, 1);
						spriteConsumer.addVertex(0, 1, 1, -1, 0, 0, OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_BRIGHT, 0, 0, 1);
						spriteConsumer.addVertex(1, 1, 1, -1, 1, 0, OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_BRIGHT, 0, 0, 1);
						spriteConsumer.addVertex(1, 0, 1, -1, 1, 1, OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_BRIGHT, 0, 0, 1);
					}
			);
		});
	}
}
