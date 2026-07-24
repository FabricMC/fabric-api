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

package net.fabricmc.fabric.mixin.debug.client;

import net.fabricmc.fabric.impl.debug.client.debugScreen.DebugScreenEntryRegistryImpl;
import net.fabricmc.fabric.impl.debug.client.debugScreen.DebugScreenProfilesImpl;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(DebugScreenEntries.class)
abstract class DebugScreenEntriesMixin {

	@Final
	@Shadow
	private static Map<Identifier, DebugScreenEntry> ENTRIES_BY_ID;

	@Mutable
	@Shadow
	@Final
	private static Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> PROFILES;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void addDebugEntry(CallbackInfo ci) {
		PROFILES = DebugScreenProfilesImpl.invoke(PROFILES);
		DebugScreenEntryRegistryImpl.addEntries(ENTRIES_BY_ID);
	}
}
