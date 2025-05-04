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

package net.fabricmc.fabric.mixin.gametest;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.storage.LevelStorage;

import net.fabricmc.fabric.impl.gametest.FabricGameTestRunner;

@Mixin(StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin {
	@Shadow
	private ResourceManager resourceManager;

	@Shadow
	public abstract StructureTemplate createTemplate(NbtCompound nbt);

	private Optional<StructureTemplate> fabric_loadSnbtFromResource(Identifier id) {
		Identifier path = FabricGameTestRunner.GAMETEST_STRUCTURE_FINDER.toResourcePath(id);
		Optional<Resource> resource = this.resourceManager.getResource(path);

		if (resource.isPresent()) {
			try {
				String snbt = IOUtils.toString(resource.get().getReader());
				NbtCompound nbt = NbtHelper.fromNbtProviderString(snbt);
				return Optional.of(this.createTemplate(nbt));
			} catch (IOException | CommandSyntaxException e) {
				throw new RuntimeException("Failed to load GameTest structure " + id, e);
			}
		}

		return Optional.empty();
	}

	private Stream<Identifier> fabric_streamTemplatesFromResource() {
		ResourceFinder finder = FabricGameTestRunner.GAMETEST_STRUCTURE_FINDER;
		return finder.findResources(this.resourceManager).keySet().stream().map(finder::toResourceId);
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList$Builder;add(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;", ordinal = 2, shift = At.Shift.AFTER, remap = false))
	private void addFabricTemplateProvider(ResourceManager resourceManager, LevelStorage.Session session, DataFixer dataFixer, RegistryEntryLookup<Block> blockLookup, CallbackInfo ci, @Local ImmutableList.Builder<StructureTemplateManager.Provider> builder) {
		builder.add(new StructureTemplateManager.Provider(this::fabric_loadSnbtFromResource, this::fabric_streamTemplatesFromResource));
	}
}
