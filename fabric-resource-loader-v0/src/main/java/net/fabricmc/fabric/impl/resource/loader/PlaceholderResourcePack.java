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

package net.fabricmc.fabric.impl.resource.loader;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import net.minecraft.SharedConstants;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataMap;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record PlaceholderResourcePack(ResourceType type, ResourcePackInfo metadata) implements ResourcePack {
	private static final Text DESCRIPTION_TEXT = Text.translatable("pack.description.modResources");

	public PackResourceMetadata getMetadata() {
		return ModResourcePackUtil.getMetadataPack(
				SharedConstants.getGameVersion().packVersion(type),
				DESCRIPTION_TEXT
		);
	}

	@Nullable
	@Override
	public InputSupplier<InputStream> openRoot(String... segments) {
		if (segments.length > 0) {
			switch (segments[0]) {
			case "pack.mcmeta":
				return () -> {
					DataResult<JsonElement> result = PackResourceMetadata.createCodec(type)
							.encodeStart(JsonOps.INSTANCE, getMetadata());
					String metadata = result.getOrThrow().toString();
					return IOUtils.toInputStream(metadata, StandardCharsets.UTF_8);
				};
			case "pack.png":
				return ModResourcePackUtil::getDefaultIcon;
			}
		}

		return null;
	}

	/**
	 * This pack has no actual contents.
	 */
	@Nullable
	@Override
	public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
		return null;
	}

	@Override
	public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		return Collections.emptySet();
	}

	@Nullable
	@Override
	public <T> T parseMetadata(ResourceMetadataSerializer<T> metaReader) {
		return ResourceMetadataMap.of(PackResourceMetadata.getSerializerFor(type), getMetadata()).get(metaReader);
	}

	@Override
	public ResourcePackInfo getInfo() {
		return metadata;
	}

	@Override
	public String getId() {
		return ModResourcePackCreator.FABRIC;
	}

	@Override
	public void close() {
	}

	public record Factory(ResourceType type, ResourcePackInfo metadata) implements ResourcePackProfile.PackFactory {
		@Override
		public ResourcePack open(ResourcePackInfo var1) {
			return new PlaceholderResourcePack(this.type, metadata);
		}

		@Override
		public ResourcePack openWithOverlays(ResourcePackInfo var1, ResourcePackProfile.Metadata metadata) {
			return open(var1);
		}
	}
}
