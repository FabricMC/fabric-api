package net.fabricmc.fabric.impl.serialization.filefix;

import net.minecraft.util.filefix.operations.FileFixOperation;
import net.minecraft.util.worldupdate.UpgradeProgress;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public record CombinedFileFixOperation(List<FileFixOperation> operations) implements FileFixOperation {

	@Override
	public void fix(Path baseDirectory, UpgradeProgress upgradeProgress) throws IOException {
		for (FileFixOperation operation : operations) {
			operation.fix(baseDirectory, upgradeProgress);
		}
	}
}
