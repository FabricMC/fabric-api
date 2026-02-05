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

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;

import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;

@Mixin(BuiltInPackSource.class)
public class BuiltInPackSourceMixin {
	@Inject(method = "loadPacks", at = @At("RETURN"))
	private void addBuiltinResourcePacks(Consumer<Pack> consumer, CallbackInfo ci) {
		// Register mod and built-in resource packs after the vanilla built-in resource packs are registered.
		// noinspection ConstantConditions
		if ((Object) this instanceof ClientPackSource) {
			ModResourcePackCreator.CLIENT_RESOURCE_PACK_PROVIDER.loadPacks(consumer);
		}
	}
}
