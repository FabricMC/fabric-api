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

package net.fabricmc.fabric.mixin.event.lifecycle.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceProvider;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public class ClientConfigurationPacketListenerImplMixin {
	@Inject(method = "method_57043", at = @At(value = "RETURN"))
	private void invokeTagsLoaded(ResourceProvider factory, CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
		CommonLifecycleEvents.TAGS_LOADED.invoker().onTagsLoaded(cir.getReturnValue(), true);
	}
}
