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

package net.fabricmc.fabric.mixin.advancement;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import net.fabricmc.fabric.api.advancement.v1.AdvancementEvents;
import net.fabricmc.fabric.api.advancement.v1.AdvancementSource;
import net.fabricmc.fabric.api.advancement.v1.FabricAdvancementBuilder;
import net.fabricmc.fabric.impl.advancement.AdvancementSourceTracker;

/**
 * Implements the events from {@link AdvancementEvents}.
 */
@Mixin(ServerAdvancementManager.class)
abstract class ServerAdvancementManagerMixin {
	@Shadow
	@Final
	private HolderLookup.Provider registries;

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
	private void onApply(Map<Identifier, Advancement> preparations, ResourceManager manager, ProfilerFiller profiler, CallbackInfo ci) {
		// Populated inside SimpleJsonResourceReloadListenerMixin
		Map<Identifier, AdvancementSource> sources = AdvancementSourceTracker.remove(preparations);
		Map<Identifier, Advancement> modifiedAdvancements = new HashMap<>();

		preparations.forEach((id, advancement) -> {
			AdvancementSource source = sources.getOrDefault(id, AdvancementSource.DATA_PACK);

			// Invoke the REPLACE event for the current advancement.
			Advancement replacement = AdvancementEvents.REPLACE.invoker().replaceAdvancement(id, advancement, source, this.registries);

			if (replacement != null) {
				// Set the advancement to MODIFY to be the replacement advancement.
				// The MODIFY event will also see it as a replaced advancement via the source.
				advancement = replacement;
				source = AdvancementSource.REPLACED;
			}

			// Turn the current advancement into a modifiable builder and invoke the MODIFY event.
			Advancement.Builder builder = FabricAdvancementBuilder.copyOf(advancement);
			AdvancementEvents.MODIFY.invoker().modifyAdvancement(id, builder, source, this.registries);

			modifiedAdvancements.put(id, builder.build(id).value());
		});

		// Replace the original map contents with the modified ones
		preparations.clear();
		preparations.putAll(modifiedAdvancements);
	}

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("RETURN"))
	private void onLoaded(Map<Identifier, Advancement> preparations, ResourceManager manager, ProfilerFiller profiler, CallbackInfo ci) {
		// After everything, so all advancements are loaded
		AdvancementEvents.ALL_LOADED.invoker().onAdvancementsLoaded(manager, preparations, this.registries);
	}
}
