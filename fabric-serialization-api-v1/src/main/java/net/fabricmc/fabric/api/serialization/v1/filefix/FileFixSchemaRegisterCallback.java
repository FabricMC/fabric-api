package net.fabricmc.fabric.api.serialization.v1.filefix;

import com.mojang.datafixers.schemas.Schema;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.util.filefix.FileFix;
import net.minecraft.util.filefix.FileFixerUpper;

import java.util.function.Function;

/**
 * Event for registering file fixes. This event is called for every {@link Schema} added to
 * Minecraft's {@link FileFixerUpper}, before any of vanilla's fixes are added to it.
 *
 * <p>Please note that this event is called early during the game launch, before {@link net.fabricmc.api.ModInitializer}s
 * are run. Therefore, make sure to register callbacks early during the mod loading process, using
 * a {@link net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint}.</p>
 */
@FunctionalInterface
public interface FileFixSchemaRegisterCallback {

	Event<FileFixSchemaRegisterCallback> EVENT = EventFactory.createArrayBacked(FileFixSchemaRegisterCallback.class,
			listeners -> (fileFixerUpper, schema, version) -> {
				for (FileFixSchemaRegisterCallback callback : listeners) {
					callback.schemaRegistered(fileFixerUpper, schema, version);
				}
			});

	void schemaRegistered(FileFixerUpper.Builder fileFixerUpper, Schema schema, int version);

	/**
	 * Can be used to easily register file fixes for a data version. This method registers a callback
	 * at {@link FileFixSchemaRegisterCallback#EVENT}, which adds the given {@code fixes} to the
	 * game's {@link FileFixerUpper} at the given data version.
	 *
	 * <p>Please note that this method does not throw for invalid data versions, and that
	 * fixes can only be registered for a data version that has file fixes in vanilla, because
	 * there won't be a schema registered for those versions.</p>
	 *
	 * @param version the data version to register the file fixes for.
	 * @param fixes the file fixes to register.
	 */
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
