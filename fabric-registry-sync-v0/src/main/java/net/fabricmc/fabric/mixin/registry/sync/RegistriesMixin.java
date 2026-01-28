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

package net.fabricmc.fabric.mixin.registry.sync;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

// Vanilla doesn't mark namespaces in the directories of tags and dynamic registry elements at all,
// so we prepend the directories with the namespace if it's a modded registry id.
@Mixin(Registries.class)
public class RegistriesMixin {
	@ModifyReturnValue(method = "elementsDirPath", at = @At("RETURN"))
	private static String prependDirectoryWithNamespace(String original, @Local(argsOnly = true) ResourceKey<? extends Registry<?>> registryRef) {
		Identifier id = registryRef.identifier();

		if (!id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
			return id.getNamespace() + "/" + id.getPath();
		}

		return original;
	}

	@ModifyReturnValue(method = "tagsDirPath", at = @At("RETURN"))
	private static String prependTagDirectoryWithNamespace(String original, @Local(argsOnly = true) ResourceKey<? extends Registry<?>> registryRef) {
		Identifier id = registryRef.identifier();

		if (!id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
			return "tags/" + id.getNamespace() + "/" + id.getPath();
		}

		return original;
	}
}
