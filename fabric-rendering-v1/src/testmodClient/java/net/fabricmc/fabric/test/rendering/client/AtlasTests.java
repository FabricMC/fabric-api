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

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.ExtraCodecs;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.AtlasRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

public class AtlasTests implements ClientModInitializer {
	private static final Identifier ATLAS_ID = Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "test_atlas");
	private static final Identifier TEXTURE_ID = AtlasRegistry.generateTextureLocation(ATLAS_ID);
	private static final SpriteId[] SPRITES = new SpriteId[] {
			new SpriteId(
					TEXTURE_ID,
					Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "test_atlas/double_iron_ingot")
			),
			new SpriteId(
					TEXTURE_ID,
					Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "test_atlas/blank")
			)
	};
	private static final Identifier HUD_ID = Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "atlas_hud");
	public static final MetadataSectionType<Integer> COLOR = new MetadataSectionType<>("color", ExtraCodecs.STRING_ARGB_COLOR);

	@Override
	public void onInitializeClient() {
		AtlasRegistry.register(new AtlasManager.AtlasConfig(TEXTURE_ID, ATLAS_ID, false, Set.of(COLOR)));

		HudElementRegistry.addLast(
				HUD_ID,
				(graphics, deltaTracker) -> {
					final AtlasManager atlasManager = Minecraft.getInstance().getAtlasManager();
					final int y = 18;
					int x = 0;

					for (SpriteId spriteId : SPRITES) {
						final TextureAtlasSprite sprite = atlasManager.get(spriteId);
						final SpriteContents contents = sprite.contents();
						final int color = sprite.contents().getAdditionalMetadata(COLOR).orElse(-1);

						graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, contents.width(), contents.height(), color);

						x += contents.width() + 2;
					}
				}
		);
	}
}
