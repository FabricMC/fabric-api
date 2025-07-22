package net.fabricmc.fabric.command.unittest;

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
