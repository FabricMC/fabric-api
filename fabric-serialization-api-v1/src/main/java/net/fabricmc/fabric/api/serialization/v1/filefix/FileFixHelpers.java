package net.fabricmc.fabric.api.serialization.v1.filefix;

import com.mojang.datafixers.schemas.Schema;

import net.fabricmc.fabric.impl.serialization.filefix.CombinedFileFixOperation;
import net.fabricmc.fabric.impl.serialization.filefix.FileFixHelpersImpl;

import net.fabricmc.fabric.mixin.serialization.filefix.FileFixerUpperAccessor;

import net.minecraft.resources.Identifier;
import net.minecraft.util.filefix.FileFix;
import net.minecraft.util.filefix.operations.FileFixOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface FileFixHelpers {

	static FileFixOperation createDimensionDataMoveOperation(String oldSaveId, Identifier newSaveId) {
		return createDimensionDataMoveOperation(Map.of(oldSaveId, newSaveId));
	}

	static FileFixOperation createDimensionDataMoveOperation(Map<String, Identifier> saveIdMap) {
		List<FileFixOperation> operations = new ArrayList<>();
		operations.addAll(FileFixHelpersImpl.createDimensionMoveOperations(saveIdMap, "", "dimensions/minecraft/overworld"));
		operations.addAll(FileFixHelpersImpl.createDimensionMoveOperations(saveIdMap, "DIM-1", "dimensions/minecraft/the_nether"));
		operations.addAll(FileFixHelpersImpl.createDimensionMoveOperations(saveIdMap, "DIM1", "dimensions/minecraft/the_end"));
		operations.add(FileFixHelpersImpl.createCustomDimensionDataMoveOperation(saveIdMap));
		return new CombinedFileFixOperation(operations);
	}

	static Function<Schema, FileFix> createDimensionDataMoveFileFix(String oldSaveId, Identifier newSaveId) {
		return createDimensionDataMoveFileFix(Map.of(oldSaveId, newSaveId));
	}

	static Function<Schema, FileFix> createDimensionDataMoveFileFix(Map<String, Identifier> saveIdMap) {
		return schema -> new FileFix(schema) {
			@Override
			public void makeFixer() {
				addFileFixOperation(createDimensionDataMoveOperation(saveIdMap));
			}
		};
	}

	static void registerDimensionDataMoveFileFix(String oldSaveId, Identifier newSaveId) {
		registerDimensionDataMoveFileFix(Map.of(oldSaveId, newSaveId));
	}

	static void registerDimensionDataMoveFileFix(Map<String, Identifier> saveIdMap) {
		FileFixSchemaRegisterCallback.registerFileFixes(FileFixerUpperAccessor.getFileFixerIntroductionVersion(), createDimensionDataMoveFileFix(saveIdMap));
	}
}
