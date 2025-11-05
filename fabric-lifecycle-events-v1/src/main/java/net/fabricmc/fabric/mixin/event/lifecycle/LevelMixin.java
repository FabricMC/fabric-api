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

package net.fabricmc.fabric.mixin.event.lifecycle;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;

@Mixin(Level.class)
public abstract class LevelMixin implements LoadedChunksCache {
	@Unique
	private final Set<LevelChunk> loadedChunks = new HashSet<>();

	@Override
	public Set<LevelChunk> fabric_getLoadedChunks() {
		return this.loadedChunks;
	}

	@Override
	public void fabric_markLoaded(LevelChunk chunk) {
		this.loadedChunks.add(chunk);
	}

	@Override
	public void fabric_markUnloaded(LevelChunk chunk) {
		this.loadedChunks.remove(chunk);
	}
}
