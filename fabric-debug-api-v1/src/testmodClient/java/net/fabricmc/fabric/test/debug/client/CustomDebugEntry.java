package net.fabricmc.fabric.test.debug.client;

import net.fabricmc.fabric.test.debug.DebugApiTest;

import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import org.jspecify.annotations.Nullable;

public class CustomDebugEntry implements DebugScreenEntry {
	@Override
	public void display(DebugScreenDisplayer displayer, @Nullable Level serverOrClientLevel, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
		displayer.addLine("sussy? : " + DebugApiTest.DEBUG_SUS_AVATAR);
	}

	@Override
	public DebugEntryCategory category() {
		return DebugEntryCategory.SCREEN_TEXT;
	}
}
