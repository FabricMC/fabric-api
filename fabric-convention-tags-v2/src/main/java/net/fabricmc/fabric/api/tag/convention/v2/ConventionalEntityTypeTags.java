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

package net.fabricmc.fabric.api.tag.convention.v2;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;

/**
 * See {@link net.minecraft.tags.EntityTypeTags} for vanilla tags.
 * Note that addition to some vanilla tags implies having certain functionality.
 */
public final class ConventionalEntityTypeTags {
	private ConventionalEntityTypeTags() {
	}

	/**
	 * Tag containing entity types that display a boss health bar.
	 */
	public static final TagKey<EntityType<?>> BOSSES = register("bosses");

	public static final TagKey<EntityType<?>> MINECARTS = register("minecarts");
	public static final TagKey<EntityType<?>> BOATS = register("boats");

	/**
	 * Entities should be included in this tag if they are not allowed to be picked up by items or grabbed in a way
	 * that a player can easily move the entity to anywhere they want. Ideal for special entities that should not
	 * be able to be put into a mob jar for example.
	 */
	public static final TagKey<EntityType<?>> CAPTURING_NOT_SUPPORTED = register("capturing_not_supported");

	/**
	 * Entities should be included in this tag if they are not allowed to be teleported in any way.
	 * This is more for mods that allow teleporting entities within the same dimension. Any mod that is
	 * teleporting entities to new dimensions should be checking canUsePortals method on the entity itself.
	 */
	public static final TagKey<EntityType<?>> TELEPORTING_NOT_SUPPORTED = register("teleporting_not_supported");

	private static TagKey<EntityType<?>> register(String tagId) {
		return TagRegistration.ENTITY_TYPE_TAG.registerC(tagId);
	}
}
