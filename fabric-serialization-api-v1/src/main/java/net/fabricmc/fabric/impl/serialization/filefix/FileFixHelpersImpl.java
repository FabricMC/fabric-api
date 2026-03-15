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
				.map(entry -> (FileFixOperation)
						FileFixOperations.move(oldDimensionPath + "/data/" + entry.getKey() + ".dat",
								newDimensionPath + "/data/" + entry.getValue().getNamespace() + "/" + entry.getValue().getPath() + ".dat"))
				.toList();
	}

	static FileFixOperation createCustomDimensionDataMoveOperation(Map<String, Identifier> saveIdMap) {
		return FileFixOperations.applyInFolders(
				FileRelation.DIMENSIONS_DATA,
				saveIdMap.entrySet().stream()
						// example.dat -> example_mod/example.dat
						.map(entry -> (FileFixOperation)
								FileFixOperations.move(entry.getKey() + ".dat", entry.getValue().getNamespace() + "/" + entry.getValue().getPath() + ".dat"))
						.toList()
		);
	}
}
