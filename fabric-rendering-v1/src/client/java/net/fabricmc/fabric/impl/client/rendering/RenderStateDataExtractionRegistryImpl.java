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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataExtractor;

public final class RenderStateDataExtractionRegistryImpl {
	private static final Map<EntityType<?>, List<RenderStateDataExtractor<? extends Entity, ?>>> EXTRACTORS = new HashMap<>();
	private static final List<RenderStateDataExtractor<? extends Avatar, ?>> AVATAR_EXTRACTORS = new ArrayList<>();

	public static void applyExtractors(EntityType<?> entityType, EntityRenderer<?, ?> renderer) {
		List<RenderStateDataExtractor<? extends Entity, ?>> extractors = EXTRACTORS.get(entityType);
		if (extractors == null) return;

		for (RenderStateDataExtractor<? extends Entity, ?> extractor : extractors) {
			renderer.addExtractor(extractor);
		}
	}

	public static void applyAvatarExtractors(AvatarRenderer<?> renderer) {
		for (RenderStateDataExtractor<? extends Entity, ?> extractor : AVATAR_EXTRACTORS) {
			renderer.addExtractor(extractor);
		}
	}

	public static <S extends Entity> void register(EntityType<S> rendererClass, RenderStateDataExtractor<S, ?> extractor) {
		EXTRACTORS.computeIfAbsent(rendererClass, _ -> new ArrayList<>()).add(extractor);
	}

	public static void registerAvatar(RenderStateDataExtractor<? extends Avatar, ?> extractor) {
		AVATAR_EXTRACTORS.add(extractor);
	}
}
