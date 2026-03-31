package net.fabricmc.fabric.impl.serialization.filefix;

import net.minecraft.resources.Identifier;
import net.minecraft.util.filefix.access.FileRelation;
import net.minecraft.util.filefix.operations.FileFixOperation;
import net.minecraft.util.filefix.operations.FileFixOperations;

import java.util.List;
import java.util.Map;

public interface FileFixHelpersImpl {

	static List<FileFixOperation> createDimensionMoveOperations(Map<String, Identifier> saveIdMap,
																String oldDimensionPath,
																String newDimensionPath) {
		return saveIdMap.entrySet().stream()
				.map(entry -> createNamespacedDataMoveOperation(entry.getKey(), entry.getValue(),
						oldDimensionPath + "/data/", newDimensionPath + "/data/"))
				.toList();
	}

	static FileFixOperation createCustomDimensionDataMoveOperation(Map<String, Identifier> saveIdMap) {
		return FileFixOperations.applyInFolders(
				FileRelation.DIMENSIONS_DATA,
				saveIdMap.entrySet().stream()
						.map(entry -> createNamespacedDataMoveOperation(entry.getKey(), entry.getValue(), "", ""))
						.toList()
		);
	}

	static FileFixOperation createNamespacedDataMoveOperation(String oldId, Identifier newId, String oldPrefix, String newPrefix) {
		return FileFixOperations.move(oldPrefix + oldId + ".dat", newPrefix + newId.getNamespace() + "/" + newId.getPath() + ".dat");
	}
}
