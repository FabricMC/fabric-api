package net.fabricmc.fabric.api.serialization.v1.filefix;

import com.mojang.datafixers.schemas.Schema;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.util.filefix.FileFix;
import net.minecraft.util.filefix.FileFixerUpper;

import java.util.function.Function;

@FunctionalInterface
public interface FileFixSchemaRegisterCallback {

	Event<FileFixSchemaRegisterCallback> EVENT = EventFactory.createArrayBacked(FileFixSchemaRegisterCallback.class,
			listeners -> (fileFixerUpper, schema, version) -> {
				for (FileFixSchemaRegisterCallback callback : listeners) {
					callback.schemaRegistered(fileFixerUpper, schema, version);
				}
			});

	void schemaRegistered(FileFixerUpper.Builder fileFixerUpper, Schema schema, int version);

	@SafeVarargs
	static void registerFileFixes(int version, Function<Schema, FileFix>... fixes) {
		EVENT.register((fileFixerUpper, schema, schemaVersion) -> {
			if (schemaVersion == version) {
				for (Function<Schema, FileFix> fix : fixes) {
					fileFixerUpper.addFixer(fix.apply(schema));
				}
			}
		});
	}
}
