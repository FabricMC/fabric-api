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

package net.fabricmc.fabric.impl.renderer;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.renderer.v1.Renderer;

public class DebugHudClient implements ClientModInitializer {
	public static Identifier ACTIVE_RENDERER = DebugScreenEntries.register(Identifier.fromNamespaceAndPath("fabric", "active_renderer"), new ActiveRendererDebugHudEntry());

	@Override
	public void onInitializeClient() {
	}

	private static class ActiveRendererDebugHudEntry implements DebugScreenEntry {
		@Override
		public void display(DebugScreenDisplayer lines, @Nullable Level world, @Nullable LevelChunk clientChunk, @Nullable LevelChunk chunk) {
			lines.addLine("[Fabric] Active renderer: " + Renderer.get().getClass().getSimpleName());
		}

		@Override
		public boolean isAllowed(boolean reducedDebugInfo) {
			return true;
		}

		@Override
		public DebugEntryCategory category() {
			return DebugEntryCategory.SCREEN_TEXT;
		}
	}
}
