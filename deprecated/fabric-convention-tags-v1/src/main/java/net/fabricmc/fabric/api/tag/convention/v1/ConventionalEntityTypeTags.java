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

package net.fabricmc.fabric.api.tag.convention.v1;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import net.fabricmc.fabric.impl.tag.convention.TagRegistration;

/**
 * @deprecated Please use {@link net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags}
 */
@Deprecated
public final class ConventionalEntityTypeTags {
	private ConventionalEntityTypeTags() {
	}

	/**
	 * Tag containing entity types that display a boss health bar.
	 */
	public static final TagKey<EntityType<?>> BOSSES = register("bosses");
	public static final TagKey<EntityType<?>> MINECARTS = register("minecarts");
	public static final TagKey<EntityType<?>> BOATS = register("boats");

	private static TagKey<EntityType<?>> register(String tagID) {
		return TagRegistration.ENTITY_TYPE_TAG_REGISTRATION.registerC(tagID);
	}
}
