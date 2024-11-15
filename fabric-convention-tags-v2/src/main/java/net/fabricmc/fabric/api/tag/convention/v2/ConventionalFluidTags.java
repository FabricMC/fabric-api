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

import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;

/**
 * See {@link net.minecraft.registry.tag.FluidTags} for vanilla tags.
 * Note that addition to some vanilla tags implies having certain functionality.
 * <p></p>
 * Note, fluid tags should not be plural to match the vanilla standard.
 * This is the only tag category exempted from many-different-types plural rule.
 *
 * <p>(See {@link net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants} for the correct droplet rates for containers)
 */
public final class ConventionalFluidTags {
	private ConventionalFluidTags() {
	}

	/**
	 * Holds all fluids related to lava without the behaviors attached to the vanilla lava fluid tag.
	 */
	public static final TagKey<Fluid> LAVA = register("lava");
	/**
	 * Holds all fluids related to water without the behaviors attached to the vanilla water fluid tag.
	 */
	public static final TagKey<Fluid> WATER = register("water");
	/**
	 * Holds all fluids related to milk.
	 */
	public static final TagKey<Fluid> MILK = register("milk");
	/**
	 * Holds all fluids related to honey.
	 */
	public static final TagKey<Fluid> HONEY = register("honey");
	/**
	 * Holds all fluids that are gaseous at room temperature.
	 */
	public static final TagKey<Fluid> GASEOUS = register("gaseous");
	/**
	 * Holds all fluids related to experience.
	 *
	 * <p>(Standard unit for experience is 810 droplet per 1 experience. However, extraction from Bottle o' Enchanting should yield 27000 droplets while smashing yields less)
	 */
	public static final TagKey<Fluid> EXPERIENCE = register("experience");
	/**
	 * Holds all fluids related to potions. The effects of the potion fluid should be read from Data Components.
	 * The effects and color of the potion fluid should be read from {@link net.minecraft.component.DataComponentTypes#POTION_CONTENTS}
	 * component that people should be attaching to the FluidVariant of this fluid.
	 */
	public static final TagKey<Fluid> POTION = register("potion");
	/**
	 * Holds all fluids related to Suspicious Stew.
	 * The effects of the suspicious stew fluid should be read from {@link net.minecraft.component.DataComponentTypes#SUSPICIOUS_STEW_EFFECTS}
	 * component that people should be attaching to the FluidVariant of this fluid.
	 */
	public static final TagKey<Fluid> SUSPICIOUS_STEW = register("suspicious_stew");
	/**
	 * Holds all fluids related to Mushroom Stew.
	 */
	public static final TagKey<Fluid> MUSHROOM_STEW = register("mushroom_stew");
	/**
	 * Holds all fluids related to Rabbit Stew.
	 */
	public static final TagKey<Fluid> RABBIT_STEW = register("rabbit_stew");
	/**
	 * Holds all fluids related to Beetroot Soup.
	 */
	public static final TagKey<Fluid> BEETROOT_SOUP = register("beetroot_soup");
	/**
	 * Tag that holds all fluids that recipe viewers should not show to users.
	 */
	public static final TagKey<Fluid> HIDDEN_FROM_RECIPE_VIEWERS = register("hidden_from_recipe_viewers");

	private static TagKey<Fluid> register(String tagId) {
		return TagRegistration.FLUID_TAG.registerC(tagId);
	}
}
