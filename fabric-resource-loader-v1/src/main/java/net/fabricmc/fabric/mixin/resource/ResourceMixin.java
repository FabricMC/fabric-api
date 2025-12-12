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

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;

import net.fabricmc.fabric.api.resource.v1.FabricResource;
import net.fabricmc.fabric.impl.resource.PackSourceTracker;

/**
 * Implements {@link FabricResource} (resource source getter/setter)
 * for vanilla's basic {@link Resource} used for most game resources.
 */
@Mixin(Resource.class)
class ResourceMixin implements FabricResource {
	@SuppressWarnings("ConstantConditions")
	@Override
	public PackSource getFabricPackSource() {
		Resource self = (Resource) (Object) this;
		return PackSourceTracker.getSource(self.source());
	}
}
