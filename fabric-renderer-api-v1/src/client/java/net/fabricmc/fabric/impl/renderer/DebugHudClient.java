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

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudEntryCategory;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.renderer.v1.Renderer;

public class DebugHudClient implements ClientModInitializer {
	public static Identifier ACTIVE_RENDERER = DebugHudEntries.register(Identifier.of("fabric", "active_renderer"), new ActiveRendererDebugHudEntry());

	@Override
	public void onInitializeClient() {
	}

	private static class ActiveRendererDebugHudEntry implements DebugHudEntry {
		@Override
		public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
			lines.addLine("[Fabric] Active renderer: " + Renderer.get().getClass().getSimpleName());
		}

		@Override
		public boolean canShow(boolean reducedDebugInfo) {
			return true;
		}

		@Override
		public DebugHudEntryCategory getCategory() {
			return DebugHudEntryCategory.TEXT;
		}
	}
}
