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

package net.fabricmc.fabric.test.resource.loader.client;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.JsonOps;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.util.InclusiveRange;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.client.pack.ClientMutablePackResources;
import net.fabricmc.fabric.api.resource.v1.pack.InMemoryResourcePack;
import net.fabricmc.fabric.api.resource.v1.pack.SimplePackResourcesSupplier;

public class RepositorySourceTestMod implements ClientModInitializer {
	private static final String PACK_NAME = "Visible Test Virtual Pack";

	@Override
	public void onInitializeClient() {
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerRepositorySource(profileAdder -> {
			var location = new PackLocationInfo(
					PACK_NAME,
					Component.literal(PACK_NAME),
					PackSource.create(
							text -> text.copy().append(Component.literal(" (Virtual Provider)").withColor(TextColor.DARK_GRAY)),
							false
					),
					Optional.empty()
			);

			profileAdder.accept(Objects.requireNonNull(Pack.readMetaAndCreate(
					location,
					new SimplePackResourcesSupplier(new TestPack(location)),
					PackType.CLIENT_RESOURCES,
					new PackSelectionConfig(false, Pack.Position.TOP, false)
			)));
		});
	}

	static class TestPack extends InMemoryResourcePack.Located implements ClientMutablePackResources {
		private static final Identifier DIRT_IDENTIFIER = Identifier.withDefaultNamespace("textures/block/dirt.png");
		private final Random random = new Random();

		TestPack(PackLocationInfo location) {
			super(location);

			this.putText("pack.mcmeta", this::createPackMeta);
			this.putImage("pack.png", this::createRandomImage);
			this.putImage(DIRT_IDENTIFIER, this::createRandomImage);
		}

		private String createPackMeta() {
			PackFormat packFormat = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES);
			var section = new PackMetadataSection(Component.literal("Just testing."), new InclusiveRange<>(packFormat));
			JsonObject packMeta = PackMetadataSection.codecForPackType(PackType.CLIENT_RESOURCES)
					.encodeStart(JsonOps.INSTANCE, section)
					.getOrThrow()
					.getAsJsonObject();
			var parent = new JsonObject();
			parent.add("pack", packMeta);
			return parent.toString();
		}

		private NativeImage createRandomImage() {
			var image = new NativeImage(16, 16, true);
			boolean t = this.random.nextBoolean();

			for (int y = 0; y < 16; y++) {
				int color = 0xff << 24;
				color |= random.nextInt(256) << 16;
				color |= random.nextInt(256) << 8;
				color |= random.nextInt(256);

				for (int x = 0; x < 16; x++) {
					image.setPixelABGR(t ? x : y, t ? y : x, color);
				}
			}

			return image;
		}
	}
}
