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

package net.fabricmc.fabric.impl.resource;

import java.util.function.Function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;

import net.fabricmc.fabric.api.resource.v1.FabricSinglePoolElement;
import net.fabricmc.fabric.api.util.TriState;

public final class EntityFinalizationUtil {
	private EntityFinalizationUtil() {
	}

	private static <T extends SinglePoolElement> T withEntityFinalization(T element, TriState finalizeEntities) {
		element.setFinalizeEntities(finalizeEntities);
		return element;
	}

	private static <T extends SinglePoolElement, E extends T> RecordCodecBuilder<E, TriState> createCodecExtension() {
		return TriState.CODEC.optionalFieldOf("fabric:finalize_entities", TriState.DEFAULT).forGetter(FabricSinglePoolElement::shouldFinalizeEntities);
	}

	/**
	 * Used to extend the CODECs of {@link net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement}
	 * and {@link net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement} in
	 * {@link net.fabricmc.fabric.mixin.resource.SinglePoolElementMixin}.
	 */
	public static <T extends SinglePoolElement> MapCodec<T> extendCodec(MapCodec<T> codec) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				codec.forGetter(Function.identity()), EntityFinalizationUtil.createCodecExtension()
		).apply(instance, EntityFinalizationUtil::withEntityFinalization));
	}
}
