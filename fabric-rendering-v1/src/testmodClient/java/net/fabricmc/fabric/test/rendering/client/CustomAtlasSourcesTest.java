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

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.slf4j.Logger;

import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.client.texture.atlas.AtlasSource;
import net.minecraft.client.texture.atlas.AtlasSourceType;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.AtlasSourceTypeRegistry;

public class CustomAtlasSourcesTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AtlasSourceTypeRegistry.register(Identifier.of("fabric-rendering-v1-testmod", "double"), DoubleAtlasSource.TYPE);
	}

	private static class DoubleAtlasSource implements AtlasSource {
		private static final Logger LOGGER = LogUtils.getLogger();
		public static final MapCodec<DoubleAtlasSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Identifier.CODEC.fieldOf("resource").forGetter(source -> source.resource),
				Identifier.CODEC.fieldOf("sprite").forGetter(source -> source.sprite)
		).apply(instance, DoubleAtlasSource::new));
		public static final AtlasSourceType TYPE = new AtlasSourceType(CODEC);

		private final Identifier resource;
		private final Identifier sprite;

		DoubleAtlasSource(Identifier resource, Identifier sprite) {
			this.resource = resource;
			this.sprite = sprite;
		}

		@Override
		public void load(ResourceManager resourceManager, SpriteRegions regions) {
			Identifier resourceId = RESOURCE_FINDER.toResourcePath(resource);
			Optional<Resource> optionalResource = resourceManager.getResource(resourceId);

			if (optionalResource.isPresent()) {
				regions.add(sprite, new DoubleSpriteRegion(resourceId, optionalResource.get(), sprite));
			} else {
				LOGGER.warn("Missing sprite: {}", resourceId);
			}
		}

		@Override
		public AtlasSourceType getType() {
			return TYPE;
		}

		private static class DoubleSpriteRegion implements AtlasSource.SpriteRegion {
			private final Identifier resourceId;
			private final Resource resource;
			private final Identifier spriteId;

			DoubleSpriteRegion(Identifier resourceId, Resource resource, Identifier spriteId) {
				this.resourceId = resourceId;
				this.resource = resource;
				this.spriteId = spriteId;
			}

			@Override
			public SpriteContents apply(SpriteOpener spriteOpener) {
				ResourceMetadata metadata;

				try {
					metadata = resource.getMetadata();
				} catch (Exception e) {
					LOGGER.error("Unable to parse metadata from {}", resourceId, e);
					return null;
				}

				NativeImage image;

				try (InputStream inputStream = resource.getInputStream()) {
					image = NativeImage.read(inputStream);
				} catch (IOException e) {
					LOGGER.error("Using missing texture, unable to load {}", resourceId, e);
					return null;
				}

				int imageWidth = image.getWidth();
				int imageHeight = image.getHeight();
				AnimationResourceMetadata animationMetadata = metadata.decode(AnimationResourceMetadata.SERIALIZER)
						.orElse(new AnimationResourceMetadata(Optional.empty(), Optional.empty(), Optional.empty(), 1, false));
				SpriteDimensions dimensions = animationMetadata.getSize(imageWidth, imageHeight);
				int frameWidth = dimensions.width();
				int frameHeight = dimensions.height();

				if (!MathHelper.isMultipleOf(imageWidth, frameWidth) || !MathHelper.isMultipleOf(imageHeight, dimensions.height())) {
					LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}", resourceId, imageWidth, imageHeight, frameWidth, frameHeight);
					image.close();
					return null;
				}

				int frameCountX = imageWidth / frameWidth;
				int frameCountY = imageHeight / frameHeight;
				int offsetX = frameWidth / 16;
				int offsetY = frameHeight / 16;

				NativeImage doubleImage = new NativeImage(image.getFormat(), image.getWidth(), image.getHeight(), false);

				for (int frameY = 0; frameY < frameCountY; frameY++) {
					for (int frameX = 0; frameX < frameCountX; frameX++) {
						blendRect(image, doubleImage, frameX * frameWidth + offsetX, frameY * frameHeight + offsetY, frameX * frameWidth, frameY * frameHeight, frameWidth - offsetX, frameHeight - offsetY);
						blendRect(image, doubleImage, frameX * frameWidth, frameY * frameHeight, frameX * frameWidth + offsetX, frameY * frameHeight + offsetY, frameWidth - offsetX, frameHeight - offsetY);
					}
				}

				return new SpriteContents(spriteId, dimensions, doubleImage, metadata);
			}

			private static void blendRect(NativeImage src, NativeImage dst, int srcX, int srcY, int destX, int destY, int width, int height) {
				for (int y = 0; y < height; ++y) {
					for (int x = 0; x < width; ++x) {
						int c = src.getColorArgb(srcX + x, srcY + y);
						dst.setColorArgb(destX + x, destY + y, c);
					}
				}
			}
		}
	}
}
