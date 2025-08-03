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

package net.fabricmc.fabric.test.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.command.CommandSource;
import net.minecraft.util.Identifier;

public class CommandSuggestionOrderTest {
	@Test
	void testModIdentifierPrecedence() {
		List<String> identifiers = List.of(
				"minecraft:dirt",
				"modid:dirt",
				"minecraft:deepslate"
		);
		List<String> results = new ArrayList<>();

		CommandSource.forEachMatching(identifiers, "di", Identifier::of, results::add);

		// Vanilla returns ["minecraft:dirt"]
		assertEquals(List.of("minecraft:dirt", "modid:dirt"), results);
	}

	@Test
	void testModIdentifierPresence() {
		List<String> identifiers = List.of(
				"minecraft:dirt",
				"modid:path",
				"minecraft:deepslate"
		);
		List<String> results = new ArrayList<>();

		CommandSource.forEachMatching(identifiers, "path", Identifier::of, results::add);

		// Vanilla returns []
		assertEquals(List.of("modid:path"), results);
	}
}
