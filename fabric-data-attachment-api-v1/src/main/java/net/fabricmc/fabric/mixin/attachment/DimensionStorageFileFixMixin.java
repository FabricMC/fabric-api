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

package net.fabricmc.fabric.mixin.attachment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.util.filefix.fixes.DimensionStorageFileFix;
import net.minecraft.util.filefix.operations.FileFixOperation;
import net.minecraft.util.filefix.operations.FileFixOperations;

@Mixin(DimensionStorageFileFix.class)
abstract class DimensionStorageFileFixMixin {
	@ModifyArg(
			method = "makeFixer",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/filefix/operations/FileFixOperations;applyInFolders(Lnet/minecraft/util/filefix/access/FileRelation;Ljava/util/List;)Lnet/minecraft/util/filefix/operations/ApplyInFolders;",
					ordinal = 1
			),
			index = 1
	)
	private List<FileFixOperation> addFabricAttachmentsMigration(List<FileFixOperation> original) {
		List<FileFixOperation> operations = new ArrayList<>(original);
		operations.add(FileFixOperations.move("fabric_attachments.dat", "fabric/attachments.dat"));
		return Collections.unmodifiableList(operations);
	}
}
