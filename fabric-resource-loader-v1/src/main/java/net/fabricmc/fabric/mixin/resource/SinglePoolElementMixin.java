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

package net.fabricmc.fabric.mixin.resource;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.MapCodec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

import net.fabricmc.fabric.api.resource.v1.FabricSinglePoolElement;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.impl.resource.EntityFinalizationUtil;

@Mixin(value = {SinglePoolElement.class, LegacySinglePoolElement.class})
public abstract class SinglePoolElementMixin implements FabricSinglePoolElement {
	@Unique
	private TriState finalizeEntities = TriState.DEFAULT;

	@Override
	public TriState shouldFinalizeEntities() {
		return this.finalizeEntities;
	}

	@Override
	public void setFinalizeEntities(TriState value) {
		this.finalizeEntities = value;
	}

	@Inject(method = "getSettings", at = @At("TAIL"))
	void adjustStructurePlaceSettings(Rotation rotation, BoundingBox chunkBB, LiquidSettings liquid, boolean keepJigsaws, CallbackInfoReturnable<StructurePlaceSettings> cir, @Local(name = "settings") StructurePlaceSettings settings) {
		if (finalizeEntities != TriState.DEFAULT) {
			settings.setFinalizeEntities(finalizeEntities.get());
		}
	}

	@Mutable
	@Shadow(remap = false)
	@Final
	public static MapCodec<? extends SinglePoolElement> CODEC;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void modifyCodec(CallbackInfo ci) {
		CODEC = EntityFinalizationUtil.extendCodec(CODEC);
	}
}
