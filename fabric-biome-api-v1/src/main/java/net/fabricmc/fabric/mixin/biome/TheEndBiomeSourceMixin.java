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

package net.fabricmc.fabric.mixin.biome;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TheEndBiomeSource;

import net.fabricmc.fabric.impl.biome.TheEndBiomeData;

@Mixin(TheEndBiomeSource.class)
public class TheEndBiomeSourceMixin extends BiomeSourceMixin {
	@Shadow
	@Mutable
	@Final
	static MapCodec<TheEndBiomeSource> CODEC;

	@Unique
	private Supplier<TheEndBiomeData.Overrides> overrides;

	@Unique
	private boolean biomeSetModified = false;

	@Unique
	private boolean hasCheckedForModifiedSet = false;

	/**
	 * Modifies the codec, so it calls the static factory method that gives us access to the
	 * full biome registry instead of just the pre-defined biomes that vanilla uses.
	 */
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void modifyCodec(CallbackInfo ci) {
		CODEC = RecordCodecBuilder.mapCodec((instance) -> {
			return instance.group(RegistryOps.retrieveGetter(Registries.BIOME)).apply(instance, instance.stable(TheEndBiomeSource::create));
		});
	}

	/**
	 * Captures the biome registry at the beginning of the static factory method to allow access to it in the
	 * constructor.
	 */
	@Inject(method = "create", at = @At("HEAD"))
	private static void rememberLookup(HolderGetter<Biome> biomes, CallbackInfoReturnable<?> ci) {
		TheEndBiomeData.biomeRegistry.set(biomes);
	}

	/**
	 * Frees up the captured biome registry.
	 */
	@Inject(method = "create", at = @At("TAIL"))
	private static void clearLookup(HolderGetter<Biome> biomes, CallbackInfoReturnable<?> ci) {
		TheEndBiomeData.biomeRegistry.remove();
	}

	/**
	 * Uses the captured biome registry to set up the modded end biomes.
	 */
	@Inject(method = "<init>", at = @At("RETURN"))
	private void init(Holder<Biome> centerBiome, Holder<Biome> highlandsBiome, Holder<Biome> midlandsBiome, Holder<Biome> smallIslandsBiome, Holder<Biome> barrensBiome, CallbackInfo ci) {
		HolderGetter<Biome> biomes = TheEndBiomeData.biomeRegistry.get();

		if (biomes == null) {
			throw new IllegalStateException("Biome registry not set by Mixin");
		}

		overrides = Suppliers.memoize(() -> {
			return TheEndBiomeData.createOverrides(biomes);
		});
	}

	@Inject(method = "getNoiseBiome", at = @At("RETURN"), cancellable = true)
	private void getWeightedEndBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler noise, CallbackInfoReturnable<Holder<Biome>> cir) {
		cir.setReturnValue(overrides.get().pick(biomeX, biomeY, biomeZ, noise, cir.getReturnValue()));
	}

	@Override
	protected Set<Holder<Biome>> modifyBiomeSet(Set<Holder<Biome>> biomes) {
		if (!hasCheckedForModifiedSet) {
			hasCheckedForModifiedSet = true;
			biomeSetModified = !overrides.get().customBiomes.isEmpty();
		}

		if (biomeSetModified) {
			var modifiedBiomes = new LinkedHashSet<>(biomes);
			modifiedBiomes.addAll(overrides.get().customBiomes);
			return Collections.unmodifiableSet(modifiedBiomes);
		}

		return biomes;
	}
}
