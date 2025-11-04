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
import org.jspecify.annotations.Nullable;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;

public record PlaceholderResourcePack(PackType type, PackLocationInfo metadata) implements PackResources {
	private static final Component DESCRIPTION_TEXT = Component.translatable("pack.description.modResources");

	public PackMetadataSection getMetadata() {
		return ModResourcePackUtil.getMetadataPack(
				SharedConstants.getCurrentVersion().packVersion(type),
				DESCRIPTION_TEXT
		);
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getRootResource(String... segments) {
		if (segments.length > 0) {
			switch (segments[0]) {
			case "pack.mcmeta":
				return () -> {
					DataResult<JsonElement> result = PackMetadataSection.codecForPackType(type)
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
	public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
		return null;
	}

	@Override
	public void listResources(PackType type, String namespace, String prefix, ResourceOutput consumer) {
	}

	@Override
	public Set<String> getNamespaces(PackType type) {
		return Collections.emptySet();
	}

	@Nullable
	@Override
	public <T> T getMetadataSection(MetadataSectionType<T> metaReader) {
		return BuiltInMetadata.of(PackMetadataSection.forPackType(type), getMetadata()).get(metaReader);
	}

	@Override
	public PackLocationInfo location() {
		return metadata;
	}

	@Override
	public String packId() {
		return ModResourcePackCreator.FABRIC;
	}

	@Override
	public void close() {
	}

	public record Factory(PackType type, PackLocationInfo metadata) implements Pack.ResourcesSupplier {
		@Override
		public PackResources openPrimary(PackLocationInfo var1) {
			return new PlaceholderResourcePack(this.type, metadata);
		}

		@Override
		public PackResources openFull(PackLocationInfo var1, Pack.Metadata metadata) {
			return openPrimary(var1);
		}
	}
}
