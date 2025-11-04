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

package net.fabricmc.fabric.test.rendering.client;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;

public class DebugOptionsTests implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		DebugScreenEntries.register(Identifier.fromNamespaceAndPath("fabric-rendering", "example"), (lines, world, clientChunk, chunk) -> {
			lines.addLine("Very important debug information");
		});

		DebugScreenEntry nope = (lines, world, clientChunk, chunk) -> {
		};

		// Test sorting
		DebugScreenEntries.register(Identifier.fromNamespaceAndPath("fabric-rendering", "a"), nope);
		DebugScreenEntries.register(Identifier.fromNamespaceAndPath("fabric-rendering", "b"), nope);
		DebugScreenEntries.register(Identifier.fromNamespaceAndPath("fabric-rendering", "z"), nope);
	}
}
