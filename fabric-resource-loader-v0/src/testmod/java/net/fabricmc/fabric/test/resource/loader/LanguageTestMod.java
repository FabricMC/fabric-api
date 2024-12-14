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

package net.fabricmc.fabric.test.resource.loader;

import net.minecraft.text.Text;

import net.fabricmc.api.DedicatedServerModInitializer;

public class LanguageTestMod implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		testTranslationLoaded();
	}

	private static void testTranslationLoaded() {
		testTranslationLoaded("item.minecraft.potato", "Potato"); // Test that vanilla translation loads
		testTranslationLoaded("text.fabric-resource-loader-v0-testmod.server.lang.override", "Vanilla override test");
		testTranslationLoaded("pack.source.fabricmod", "Fabric mod");
		testTranslationLoaded("text.fabric-resource-loader-v0-testmod.server.lang.test0", "Test from fabric-resource-loader-v0-testmod");
		testTranslationLoaded("text.fabric-resource-loader-v0-testmod.server.lang.test1", "Test from fabric-resource-loader-v0-testmod-test1");
	}

	private static void testTranslationLoaded(String key, String expected) {
		String actual = Text.translatable(key).getString();

		if (!expected.equals(actual)) {
			throw new AssertionError("Expected " + key + " to translate to " + expected + ", but translated to " + actual);
		}
	}
}
