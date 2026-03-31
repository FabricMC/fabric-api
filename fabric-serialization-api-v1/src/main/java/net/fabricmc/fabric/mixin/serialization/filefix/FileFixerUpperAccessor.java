package net.fabricmc.fabric.mixin.serialization.filefix;

import net.minecraft.util.filefix.FileFixerUpper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FileFixerUpper.class)
public interface FileFixerUpperAccessor {

	@Accessor("FILE_FIXER_INTRODUCTION_VERSION")
	static int getFileFixerIntroductionVersion() {
		throw new AssertionError();
	}
}
