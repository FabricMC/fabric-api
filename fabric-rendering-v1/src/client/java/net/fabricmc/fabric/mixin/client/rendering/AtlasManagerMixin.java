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

package net.fabricmc.fabric.mixin.client.rendering;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.resources.model.sprite.AtlasManager;

import net.fabricmc.fabric.impl.client.rendering.AtlasRegistryImpl;

@Mixin(AtlasManager.class)
class AtlasManagerMixin {
	@ModifyExpressionValue(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/sprite/AtlasManager;KNOWN_ATLASES:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
	private static List<AtlasManager.AtlasConfig> addAtlases(List<AtlasManager.AtlasConfig> original) {
		final ImmutableList.Builder<AtlasManager.AtlasConfig> builder = ImmutableList.builder();
		builder.addAll(original);
		builder.addAll(AtlasRegistryImpl.finalizeConfigs());
		return builder.build();
	}
}
