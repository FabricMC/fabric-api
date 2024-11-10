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

package net.fabricmc.fabric.mixin.tag;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.SimpleRegistry;

import net.fabricmc.fabric.impl.tag.SimpleRegistryExtension;

/**
 * This is a mixin to the Registry.PendingTagLoad implementation in SimpleRegistry.
 * It applies pending tag aliases to static registries when data packs are loaded
 * and to dynamic registries when data packs are reloaded using the {@code /reload} command.
 * (Tags run on their own data loading system separate from resource reloaders, so we need to inject them
 * once the tag and resource reloads are done, which is here.)
 */
@Mixin(targets = "net.minecraft.registry.SimpleRegistry$3")
abstract class SimpleRegistry3Mixin {
	@Shadow
	@Final
	SimpleRegistry<?> field_53689;

	@Inject(method = "apply", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/SimpleRegistry;refreshTags()V"))
	private void applyTagAliases(CallbackInfo info) {
		((SimpleRegistryExtension) field_53689).fabric_applyPendingTagAliases();
	}
}
