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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.llamalad7.mixinextras.sugar.Local;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import net.fabricmc.fabric.impl.resource.pack.FabricPack;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("PackRepositoryMixin");

	@Shadow
	@Final
	@Mutable
	public Set<RepositorySource> sources;

	@Shadow
	private Map<String, Pack> available;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void construct(RepositorySource[] resourcePackProviders, CallbackInfo info) {
		// Use a LinkedHashSet to preserve ordering
		this.sources = new LinkedHashSet<>(this.sources);

		// Search repository sources to find any server-related repository source.
		boolean shouldAddServerSource = false;

		for (RepositorySource source : this.sources) {
			if (source instanceof FolderRepositorySource
					&& (((FolderRepositorySource) source).packSource == PackSource.WORLD
					|| ((FolderRepositorySource) source).packSource == PackSource.SERVER)) {
				shouldAddServerSource = true;
				break;
			}
		}

		// On server, add the mod repository source.
		if (shouldAddServerSource) {
			this.sources.add(new ModResourcePackCreator(PackType.SERVER_DATA));
		}
	}

	@Inject(method = "rebuildSelected", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;"))
	private void handleAutoEnableDisable(Collection<String> enabledNames, CallbackInfoReturnable<List<Pack>> cir, @Local(name = "selectedAndPresent") List<Pack> selectedAndPresent) {
		ModPackResourcesUtil.refreshAutoEnabledPacks(selectedAndPresent, this.available);
	}

	@Inject(method = "addPack", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER))
	private void handleAutoEnable(String profile, CallbackInfoReturnable<Boolean> cir, @Local(name = "selectedCopy") List<Pack> selectedCopy) {
		if (ModResourcePackCreator.POST_CHANGE_HANDLE_REQUIRED.contains(profile)) {
			ModPackResourcesUtil.refreshAutoEnabledPacks(selectedCopy, this.available);
		}
	}

	@Inject(method = "removePack", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(Ljava/lang/Object;)Z"))
	private void handleAutoDisable(String profile, CallbackInfoReturnable<Boolean> cir, @Local(name = "selectedCopy") List<Pack> selectedCopy) {
		if (ModResourcePackCreator.POST_CHANGE_HANDLE_REQUIRED.contains(profile)) {
			Set<String> currentlyEnabled = selectedCopy.stream().map(Pack::getId).collect(Collectors.toSet());
			selectedCopy.removeIf(p -> !((FabricPack) p).fabric$parentsEnabled(currentlyEnabled));
			LOGGER.debug("[Fabric] Internal pack auto-removed upon disabling {}, result: {}", profile, selectedCopy.stream().map(Pack::getId).toList());
		}
	}
}
