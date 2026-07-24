package net.fabricmc.fabric.test.debug.client;

import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import org.jspecify.annotations.Nullable;

import static net.fabricmc.fabric.test.debug.client.DebugApiTestClient.SUSSY_CATEGORY;

public class SussyGraphicsEntry implements DebugScreenEntry {
	@Override
	public void display(DebugScreenDisplayer displayer, @Nullable Level serverOrClientLevel, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {}

	@Override
	public DebugEntryCategory category() {
		return SUSSY_CATEGORY;
	}
}
