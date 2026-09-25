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

package net.fabricmc.fabric.api.resource.conditions.v1;

import java.util.List;
import java.util.regex.Pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.metadata.MetadataSectionType;

//CHECKSTYLE.OFF: MatchXpath

/**
 * A metadata section used to define pack overlays based on specific conditions.
 * More flexible alternative to the vanilla {@link OverlayMetadataSection}
 */
public record FabricOverlayMetadataSection(List<Entry> overlays) {
	private static final Codec<FabricOverlayMetadataSection> CODEC = Entry.CODEC.listOf().fieldOf("entries").xmap(FabricOverlayMetadataSection::new, FabricOverlayMetadataSection::overlays).codec();
	public static final MetadataSectionType<FabricOverlayMetadataSection> TYPE = new MetadataSectionType<>(ResourceConditions.OVERLAYS_KEY, CODEC);

	public record Entry(ResourceCondition condition, String overlay) {
		private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceCondition.CODEC.fieldOf("condition").forGetter(Entry::condition),
				Codec.STRING.validate(Entry::validateDirectory).fieldOf("directory").forGetter(Entry::overlay)
		).apply(instance, Entry::new));
		private static final Pattern DIRECTORY_NAME_PATTERN = Pattern.compile("[-_a-zA-Z0-9.]+");

		private static DataResult<String> validateDirectory(String directory) {
			boolean valid = DIRECTORY_NAME_PATTERN.matcher(directory).matches();
			return valid ? DataResult.success(directory) : DataResult.error(() -> "Directory name is invalid");
		}
	}
}
