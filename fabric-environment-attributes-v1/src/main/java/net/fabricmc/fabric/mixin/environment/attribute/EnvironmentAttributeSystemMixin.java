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

package net.fabricmc.fabric.mixin.environment.attribute;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.environment.attribute.v1.AttributeLayerPosition;
import net.fabricmc.fabric.impl.environment.attribute.EnvironmentAttributeEventsImpl;

@Mixin(EnvironmentAttributeSystem.class)
public class EnvironmentAttributeSystemMixin {
	@Inject(
			method = "addDefaultLayers",
			at = @At(value = "HEAD")
	)
	private static void addLayersBeforeAll(EnvironmentAttributeSystem.Builder builder, Level level, CallbackInfo ci) {
		EnvironmentAttributeEventsImpl.insertLayers(AttributeLayerPosition.BEFORE_ALL, builder, level);
	}

	@Inject(
			method = "addDefaultLayers",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/EnvironmentAttributeSystem;addBiomeLayer(Lnet/minecraft/world/attribute/EnvironmentAttributeSystem$Builder;Lnet/minecraft/core/HolderLookup;Lnet/minecraft/world/level/biome/BiomeManager;)V")
	)
	private static void addLayersAfterDimension(EnvironmentAttributeSystem.Builder builder, Level level, CallbackInfo ci) {
		EnvironmentAttributeEventsImpl.insertLayers(AttributeLayerPosition.BETWEEN_DIMENSION_AND_BIOMES, builder, level);
	}

	@Inject(
			method = "addDefaultLayers",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;dimensionType()Lnet/minecraft/world/level/dimension/DimensionType;")
	)
	private static void addLayersAfterBiomes(EnvironmentAttributeSystem.Builder builder, Level level, CallbackInfo ci) {
		EnvironmentAttributeEventsImpl.insertLayers(AttributeLayerPosition.BETWEEN_BIOMES_AND_TIMELINES, builder, level);
	}

	@Inject(
			method = "addDefaultLayers",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;canHaveWeather()Z")
	)
	private static void addLayersAfterTimelines(EnvironmentAttributeSystem.Builder builder, Level level, CallbackInfo ci) {
		EnvironmentAttributeEventsImpl.insertLayers(AttributeLayerPosition.BETWEEN_TIMELINES_AND_WEATHER, builder, level);
	}

	@Inject(
			method = "addDefaultLayers",
			at = @At(value = "TAIL")
	)
	private static void addLayersAfterAll(EnvironmentAttributeSystem.Builder builder, Level level, CallbackInfo ci) {
		EnvironmentAttributeEventsImpl.insertLayers(AttributeLayerPosition.AFTER_ALL, builder, level);
	}
}
