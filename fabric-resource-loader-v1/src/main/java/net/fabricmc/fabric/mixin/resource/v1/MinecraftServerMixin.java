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

package net.fabricmc.fabric.mixin.resource.v1;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.MinecraftServer;

import net.fabricmc.fabric.api.resource.v1.DataResourceStore;
import net.fabricmc.fabric.impl.resource.v1.FabricDataResourceStoreHolder;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements DataResourceStore {
	@Shadow
	private MinecraftServer.ResourceManagerHolder resourceManagerHolder;

	@SuppressWarnings("AddedMixinMembersNamePattern")
	@Override
	public <T> T getOrThrow(Key<T> key) {
		return ((FabricDataResourceStoreHolder) this.resourceManagerHolder.dataPackContents())
				.fabric$getDataResourceStore()
				.getOrThrow(key);
	}
}
