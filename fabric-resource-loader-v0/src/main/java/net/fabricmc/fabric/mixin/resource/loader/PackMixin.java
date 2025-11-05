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

package net.fabricmc.fabric.mixin.resource.loader;

import java.util.Set;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import net.fabricmc.fabric.impl.resource.loader.FabricResourcePackProfile;
import net.fabricmc.fabric.impl.resource.loader.ResourcePackSourceTracker;

/**
 * Implements resource pack source tracking (for {@link net.fabricmc.fabric.impl.resource.loader.FabricResource}).
 * {@link PackResources} doesn't hold a reference to its {@link PackSource}
 * so we store the source in a global tracker when the resource packs are created.
 *
 * @see ResourcePackSourceTracker
 */
@Mixin(Pack.class)
abstract class PackMixin implements FabricResourcePackProfile {
	@Unique
	private static final Predicate<Set<String>> DEFAULT_PARENT_PREDICATE = parents -> true;
	@Unique
	private Predicate<Set<String>> parentsPredicate = DEFAULT_PARENT_PREDICATE;

	@Shadow
	public abstract PackLocationInfo location();

	@Inject(method = "open", at = @At("RETURN"))
	private void onCreateResourcePack(CallbackInfoReturnable<PackResources> info) {
		ResourcePackSourceTracker.setSource(info.getReturnValue(), location().source());
	}

	@Override
	public boolean fabric_isHidden() {
		return parentsPredicate != DEFAULT_PARENT_PREDICATE;
	}

	@Override
	public boolean fabric_parentsEnabled(Set<String> enabled) {
		return parentsPredicate.test(enabled);
	}

	@Override
	public void fabric_setParentsPredicate(Predicate<Set<String>> predicate) {
		this.parentsPredicate = predicate;
	}
}
