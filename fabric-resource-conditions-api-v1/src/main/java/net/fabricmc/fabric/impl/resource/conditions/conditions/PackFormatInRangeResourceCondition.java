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

package net.fabricmc.fabric.impl.resource.conditions.conditions;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.Nullable;

import net.minecraft.SharedConstants;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.impl.resource.conditions.DefaultResourceConditionTypes;

public record PackFormatInRangeResourceCondition(PackType packType, Optional<PackFormat> minFormat, Optional<PackFormat> maxFormat) implements ResourceCondition {
	private static final Codec<PackType> PACK_TYPE_CODEC = Codec.STRING.comapFlatMap(
			PackFormatInRangeResourceCondition::parsePackType,
			t -> t == PackType.SERVER_DATA ? "data" : "assets"
	);

	private static DataResult<PackType> parsePackType(String s) {
		if ("data".equals(s)) {
			return DataResult.success(PackType.SERVER_DATA);
		} else if ("assets".equals(s)) {
			return DataResult.success(PackType.CLIENT_RESOURCES);
		}

		return DataResult.error(() -> "Unknown pack type: " + s + ", expected 'data' or 'assets'");
	}

	public static final MapCodec<PackFormatInRangeResourceCondition> CODEC = RecordCodecBuilder.<PackFormatInRangeResourceCondition>mapCodec(instance -> instance.group(
			PACK_TYPE_CODEC.fieldOf("pack_type").forGetter(PackFormatInRangeResourceCondition::packType),
			PackFormat.BOTTOM_CODEC.optionalFieldOf("min_format").forGetter(PackFormatInRangeResourceCondition::minFormat),
			PackFormat.TOP_CODEC.optionalFieldOf("max_format").forGetter(PackFormatInRangeResourceCondition::maxFormat)
	).apply(instance, PackFormatInRangeResourceCondition::new)).flatXmap(
			PackFormatInRangeResourceCondition::validateBounds,
			PackFormatInRangeResourceCondition::validateBounds
	);

	private static DataResult<PackFormatInRangeResourceCondition> validateBounds(PackFormatInRangeResourceCondition condition) {
		if (condition.minFormat.isEmpty() && condition.maxFormat.isEmpty()) {
			return DataResult.error(() -> "pack_format_in_range must specify at least one of min_format or max_format");
		}

		return DataResult.success(condition);
	}

	@Override
	public ResourceConditionType<?> getType() {
		return DefaultResourceConditionTypes.PACK_FORMAT_IN_RANGE;
	}

	@Override
	public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryInfo) {
		PackFormat currentFormat = SharedConstants.getCurrentVersion().packVersion(this.packType);

		if (this.minFormat.isPresent() && currentFormat.compareTo(this.minFormat.get()) < 0) {
			return false;
		}

		if (this.maxFormat.isPresent() && currentFormat.compareTo(this.maxFormat.get()) > 0) {
			return false;
		}

		return true;
	}
}
