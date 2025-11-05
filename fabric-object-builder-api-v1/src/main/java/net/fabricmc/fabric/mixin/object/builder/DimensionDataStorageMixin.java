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

package net.fabricmc.fabric.mixin.object.builder;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.DimensionDataStorage;

@Mixin(DimensionDataStorage.class)
class DimensionDataStorageMixin {
	/**
	 * Handle mods passing a null DataFixTypes to a PersistentState.Type.
	 */
	@WrapOperation(method = "readTagFromDisk", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;II)Lnet/minecraft/nbt/CompoundTag;"))
	private CompoundTag handleNullDataFixType(DataFixTypes dataFixTypes, DataFixer dataFixer, CompoundTag nbt, int oldVersion, int newVersion, Operation<CompoundTag> original) {
		if (dataFixTypes == null) {
			return nbt;
		}

		return original.call(dataFixTypes, dataFixer, nbt, oldVersion, newVersion);
	}
}
