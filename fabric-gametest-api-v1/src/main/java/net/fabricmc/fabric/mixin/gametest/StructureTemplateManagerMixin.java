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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.loader.TemplateSource;
import net.minecraft.world.level.storage.LevelStorageSource;

import net.fabricmc.fabric.impl.gametest.FabricGameTestRunner;

@Mixin(StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin {
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList$Builder;add(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;", ordinal = 2, shift = At.Shift.AFTER))
	private void addFabricTemplateProvider(ResourceManager resourceManager, LevelStorageSource.LevelStorageAccess storageAccess, DataFixer dataFixer, HolderGetter<Block> blockLookup, CallbackInfo ci, @Local(name = "sources") ImmutableList.Builder<TemplateSource> builder) {
		builder.add(new TemplateSource(dataFixer, blockLookup) {
			@Override
			public Optional<StructureTemplate> load(Identifier id) {
				Identifier path = FabricGameTestRunner.GAMETEST_STRUCTURE_FINDER.idToFile(id);
				Optional<Resource> resource = resourceManager.getResource(path);

				if (resource.isPresent()) {
					try {
						String snbt = IOUtils.toString(resource.get().openAsReader());
						CompoundTag tag = NbtUtils.snbtToStructure(snbt);

						// Replicate readStructure logic from TemplateSource
						StructureTemplate structureTemplate = new StructureTemplate();
						int version = NbtUtils.getDataVersion(tag, 500);
						structureTemplate.load(blockLookup, DataFixTypes.STRUCTURE.updateToCurrentVersion(dataFixer, tag, version));

						return Optional.of(structureTemplate);
					} catch (IOException | CommandSyntaxException e) {
						throw new RuntimeException("Failed to load GameTest structure " + id, e);
					}
				}

				return Optional.empty();
			}

			@Override
			public Stream<Identifier> list() {
				FileToIdConverter finder = FabricGameTestRunner.GAMETEST_STRUCTURE_FINDER;
				return finder.listMatchingResources(resourceManager).keySet().stream().map(finder::fileToId);
			}
		});
	}
}
