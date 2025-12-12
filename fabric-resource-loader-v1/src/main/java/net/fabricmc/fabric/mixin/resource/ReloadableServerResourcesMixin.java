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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.flag.FeatureFlagSet;

import net.fabricmc.fabric.api.resource.v1.DataResourceStore;
import net.fabricmc.fabric.impl.resource.DataResourceStoreImpl;
import net.fabricmc.fabric.impl.resource.FabricDataResourceStoreHolder;
import net.fabricmc.fabric.impl.resource.SetupMarkerResourceReloader;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin implements FabricDataResourceStoreHolder {
	@Unique
	private final DataResourceStore.Mutable dataResourceStore = new DataResourceStoreImpl();

	@ModifyArg(
			method = "method_58296",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"
			)
	)
	private static List<PreparableReloadListener> onSetupDataReloaders(
			List<PreparableReloadListener> reloaders,
			@Local(argsOnly = true) ReloadableServerRegistries.LoadResult loadResult,
			@Local(argsOnly = true) FeatureFlagSet featureSet,
			@Local ReloadableServerResources dataPackContents
	) {
		var list = new ArrayList<>(reloaders);
		list.addFirst(
				new SetupMarkerResourceReloader(
						dataPackContents,
						loadResult.lookupWithUpdatedTags(),
						featureSet
				)
		);
		return Collections.unmodifiableList(list);
	}

	@Override
	public DataResourceStore.Mutable fabric$getDataResourceStore() {
		return this.dataResourceStore;
	}
}
