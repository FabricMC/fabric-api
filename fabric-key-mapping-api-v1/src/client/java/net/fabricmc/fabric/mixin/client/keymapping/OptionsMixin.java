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

package net.fabricmc.fabric.mixin.client.keymapping;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingContext;
import net.fabricmc.fabric.impl.client.keymapping.KeyMappingExtensions;
import net.fabricmc.fabric.impl.client.keymapping.KeyMappingRegistryImpl;

@Mixin(Options.class)
public class OptionsMixin {
	@Mutable
	@Shadow
	@Final
	public KeyMapping[] keyMappings;

	@Inject(at = @At("HEAD"), method = "load()V")
	public void loadHook(CallbackInfo info) {
		Options options = (Options) (Object) this;

		// These bindings are also handled by screens or global input handlers.
		setGlobalConflictContext(
				options.keyInventory,
				options.keyDrop,
				options.keyPickItem,
				options.keySwapOffhand,
				options.keyChat,
				options.keyFriends,
				options.keySocialInteractions,
				options.keyScreenshot,
				options.keyFullscreen,
				options.keyAdvancements,
				options.keyDebugOverlay,
				options.keyDebugModifier
		);
		setGlobalConflictContext(options.keyHotbarSlots);
		setGlobalConflictContext(options.debugKeys);
		keyMappings = KeyMappingRegistryImpl.process(keyMappings);
	}

	@Unique
	private static void setGlobalConflictContext(KeyMapping... mappings) {
		for (KeyMapping mapping : mappings) {
			((KeyMappingExtensions) mapping).fabric_setConflictContext(KeyMappingContext.ALL);
		}
	}
}
