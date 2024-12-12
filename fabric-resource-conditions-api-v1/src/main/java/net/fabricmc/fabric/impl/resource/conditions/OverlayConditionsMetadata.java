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

package net.fabricmc.fabric.impl.resource.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resource.metadata.ResourceMetadataSerializer;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

public record OverlayConditionsMetadata(List<Entry> overlays) {
	public static final Codec<OverlayConditionsMetadata> CODEC = Entry.CODEC.listOf().fieldOf("entries").xmap(OverlayConditionsMetadata::new, OverlayConditionsMetadata::overlays).codec();
	public static final ResourceMetadataSerializer<OverlayConditionsMetadata> SERIALIZER = new ResourceMetadataSerializer<>(ResourceConditions.OVERLAYS_KEY, CODEC);

	public List<String> appliedOverlays() {
		List<String> appliedOverlays = new ArrayList<>();

		for (Entry entry : this.overlays()) {
			if (entry.condition().test(null)) {
				appliedOverlays.add(entry.directory());
			}
		}

		return appliedOverlays;
	}

	public record Entry(String directory, ResourceCondition condition) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.validate(Entry::validateDirectory).fieldOf("directory").forGetter(Entry::directory),
				ResourceCondition.CODEC.fieldOf("condition").forGetter(Entry::condition)
		).apply(instance, Entry::new));
		private static final Pattern DIRECTORY_NAME_PATTERN = Pattern.compile("[-_a-zA-Z0-9.]+");

		private static DataResult<String> validateDirectory(String directory) {
			boolean valid = DIRECTORY_NAME_PATTERN.matcher(directory).matches();
			return valid ? DataResult.success(directory) : DataResult.error(() -> "Directory name is invalid");
		}
	}
}
