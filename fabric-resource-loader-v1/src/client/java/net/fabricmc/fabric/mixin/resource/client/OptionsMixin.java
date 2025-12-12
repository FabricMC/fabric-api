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

package net.fabricmc.fabric.mixin.resource.client;

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Options;
import net.minecraft.server.packs.repository.Pack;

import net.fabricmc.fabric.impl.resource.client.DefaultResourcePackStorage;
import net.fabricmc.fabric.impl.resource.pack.FabricPack;

@Mixin(Options.class)
public class OptionsMixin {
	@Shadow
	public List<String> resourcePacks;

	@Inject(method = "load", at = @At("RETURN"))
	private void onLoad(CallbackInfo ci) {
		this.resourcePacks = DefaultResourcePackStorage.process(this.resourcePacks);
	}

	@WrapOperation(
			method = "updateResourcePacks",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/Pack;isFixedPosition()Z")
	)
	private boolean excludeInternalResourcePacksFromRefreshCheck(Pack instance, Operation<Boolean> original) {
		// Treat Fabric hidden resource packs as pinned during the check for changed resource packs,
		// so that they won't count as changed when refreshing resource packs
		return original.call(instance) || ((FabricPack) instance).fabric$isHidden();
	}
}
