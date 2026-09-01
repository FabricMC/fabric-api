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

package net.fabricmc.fabric.test.registry.sync;

import java.util.List;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.fabricmc.fabric.api.event.registry.BootstrapHelper;

public class BootstrapHelperTest {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void testFauxHolder() {
		Holder.Reference<ConfiguredFeature<?, ?>> fakeFeature = BootstrapHelper.createStandInReference(MiscOverworldFeatures.BLUE_ICE);
		Holder.Reference<ConfiguredFeature<?, ?>> fakeFeature2 = BootstrapHelper.createStandInReference(MiscOverworldFeatures.DISK_GRAVEL);
		HolderLookup.Provider registries = BootstrapHelper.createEmptyBootstrappingLookup(pendingRegistries -> {
			pendingRegistries.add(Registries.PLACED_FEATURE, registry -> {
				registry.register(MiscOverworldPlacements.BLUE_ICE, new PlacedFeature(fakeFeature, List.of()));
				registry.register(MiscOverworldPlacements.DISK_GRAVEL, new PlacedFeature(fakeFeature2, List.of()));
			});
		});

		RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
		Assertions.assertDoesNotThrow(() -> PlacedFeature.CODEC.encodeStart(ops, ops.getter(Registries.PLACED_FEATURE).orElseThrow().getOrThrow(MiscOverworldPlacements.BLUE_ICE)).getOrThrow());
		Assertions.assertDoesNotThrow(() -> PlacedFeature.CODEC.encodeStart(ops, ops.getter(Registries.PLACED_FEATURE).orElseThrow().getOrThrow(MiscOverworldPlacements.DISK_GRAVEL)).getOrThrow());
	}

	@Test
	void testFauxHolderSet() {
		HolderSet.Named<Block> fakeTag = BootstrapHelper.createStandInHolderSet(BlockTags.ALL_SIGNS);
		HolderLookup.Provider registries = BootstrapHelper.createEmptyBootstrappingLookup(pendingRegistries -> {
			pendingRegistries.add(Registries.CONFIGURED_FEATURE, registry -> {
				registry.register(CaveFeatures.LARGE_DRIPSTONE, new ConfiguredFeature<>(Feature.LARGE_DRIPSTONE, new LargeDripstoneConfiguration(
						fakeTag,
						47,
						ConstantInt.of(14),
						ConstantFloat.of(10f),
						.9f,
						ConstantFloat.of(5f),
						ConstantFloat.of(5f),
						ConstantFloat.of(1f),
						50,
						4f
				)));
			});
		});

		RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
		Assertions.assertDoesNotThrow(() -> ConfiguredFeature.CODEC.encodeStart(ops, ops.getter(Registries.CONFIGURED_FEATURE).orElseThrow().getOrThrow(CaveFeatures.LARGE_DRIPSTONE)).getOrThrow());
	}
}
