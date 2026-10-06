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

package net.fabricmc.fabric.test.resource.reloader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.repository.Pack;

import net.fabricmc.fabric.api.resource.v1.pack.FabricPack;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.fabricmc.fabric.impl.resource.pack.PackHooks;

public class ModPackResourcesUtilTests {
	private static final Gson GSON = new Gson();

	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void testRefreshAutoEnabledPacks() {
		// Vanilla uses tree map, and we test the behavior
		Map<String, Pack> profiles = new TreeMap<>();
		Map<String, Pack> modAProfiles = new TreeMap<>();
		Map<String, Pack> modBProfiles = new TreeMap<>();
		Map<String, Pack> allProfiles = new TreeMap<>();
		Pack vanilla = mockProfile(profiles, "vanilla", null);
		Pack modA = mockProfile(modAProfiles, "mod_a", ModResourcePackCreator.BASE_PARENT);
		Pack modAProg = mockProfile(modAProfiles, "mod_a_programmer_art", ModResourcePackCreator.PROGRAMMER_ART_PARENT);
		Pack modAHigh = mockProfile(modAProfiles, "mod_a_high_contrast", ModResourcePackCreator.HIGH_CONTRAST_PARENT);
		Pack modB = mockProfile(modBProfiles, "mod_b", ModResourcePackCreator.BASE_PARENT);
		Pack modBProg = mockProfile(modBProfiles, "mod_b_programmer_art", ModResourcePackCreator.PROGRAMMER_ART_PARENT);
		Pack modBHigh = mockProfile(modBProfiles, "mod_b_high_contrast", ModResourcePackCreator.HIGH_CONTRAST_PARENT);
		Pack programmerArt = mockProfile(profiles, "programmer_art", null);
		Pack highContrast = mockProfile(profiles, "high_contrast", null);
		Pack userPackA = mockProfile(profiles, "user_pack_a", null);
		Pack userPackB = mockProfile(profiles, "user_pack_b", null);
		modAProfiles.putAll(profiles);
		modBProfiles.putAll(profiles);
		allProfiles.putAll(modAProfiles);
		allProfiles.putAll(modBProfiles);

		testRefreshAutoEnabledPacks(
				profiles,
				List.of(vanilla),
				List.of(vanilla),
				"keep (no mods)"
		);
		testRefreshAutoEnabledPacks(
				profiles,
				List.of(vanilla, userPackA),
				List.of(vanilla, userPackA),
				"keep (no mods, keep user pack)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, modA),
				List.of(vanilla, modA),
				"keep (mod A only)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, modA, programmerArt, modAProg),
				List.of(vanilla, modA, programmerArt, modAProg),
				"keep (programmer_art)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, modA, modB, programmerArt, modAProg, modBProg),
				List.of(vanilla, modA, modB, programmerArt, modAProg, modBProg),
				"keep (mod A and mod B, programmer_art)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, modA, modB, programmerArt, modAProg, modBProg, highContrast, modAHigh, modBHigh),
				List.of(vanilla, modA, modB, programmerArt, modAProg, modBProg, highContrast, modAHigh, modBHigh),
				"keep (mod A and mod B, both)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, modA, modB, highContrast, modAHigh, modBHigh, programmerArt, modAProg, modBProg),
				List.of(vanilla, modA, modB, highContrast, modAHigh, modBHigh, programmerArt, modAProg, modBProg),
				"keep (remembers programmer_art-high_contrast order)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla),
				List.of(vanilla, modA),
				"fix (adding missing mods)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, userPackA),
				List.of(vanilla, modA, modB, userPackA),
				"fix (adding missing mods at the right place)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, modB, modA),
				List.of(vanilla, modA, modB),
				"fix (mod A and B, sorting)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, userPackB, modA, userPackA),
				List.of(vanilla, modA, userPackB, userPackA),
				"fix (user pack goes last)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, modA, programmerArt),
				List.of(vanilla, modA, programmerArt, modAProg),
				"fix (adding 1 met dep)"
		);
		testRefreshAutoEnabledPacks(
				modBProfiles,
				List.of(vanilla, modB, highContrast),
				List.of(vanilla, modB, highContrast, modBHigh),
				"fix (adding 1 met dep, part 2)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, modA, programmerArt, highContrast),
				List.of(vanilla, modA, programmerArt, modAProg, highContrast, modAHigh),
				"fix (adding 2 met deps)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, modA, programmerArt, modAProg, highContrast),
				List.of(vanilla, modA, programmerArt, modAProg, highContrast, modAHigh),
				"fix (adding 2 met deps + preexisting)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, modA, modAProg, modAHigh),
				List.of(vanilla, modA),
				"fix (removing 2 unmet deps)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, modA, programmerArt, modAProg, modAHigh),
				List.of(vanilla, modA, programmerArt, modAProg),
				"fix (removing 1 unmet dep)"
		);
		testRefreshAutoEnabledPacks(
				modBProfiles,
				List.of(vanilla, modB, highContrast, modBProg, modBHigh),
				List.of(vanilla, modB, highContrast, modBHigh),
				"fix (removing 1 unmet dep, part 2)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, modAProg, programmerArt, modA),
				List.of(vanilla, modA, programmerArt, modAProg),
				"reorder (bundled comes just after parents)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, modAProg, userPackA, programmerArt, modA, userPackB),
				List.of(vanilla, modA, userPackA, programmerArt, modAProg, userPackB),
				"reorder (keep user pack order)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, userPackB, modA, programmerArt, userPackA, modAProg),
				List.of(vanilla, modA, userPackB, programmerArt, modAProg, userPackA),
				"reorder (no user pack between parent-bundled)"
		);
	}

	@Test
	void testHiddenFlag() {
		Map<String, Pack> profiles = new TreeMap<>();
		Pack pack = mockProfile(profiles, "mod_a", null);

		assertTrue(pack instanceof FabricPack, "Pack should implement the public FabricPack interface");
		assertFalse(pack.isHidden(), "Pack should not be hidden by default");

		assertTrue(pack.setHidden(true), "setHidden(true) should report the pack as hidden");
		assertTrue(pack.isHidden(), "setHidden(true) should hide the pack");

		assertFalse(pack.setHidden(false), "setHidden(false) should report the pack as revealed");
		assertFalse(pack.isHidden(), "setHidden(false) should reveal the pack");
	}

	@Test
	void testParentGatedPackStaysHiddenWhenRevealed() {
		Map<String, Pack> profiles = new TreeMap<>();
		Pack pack = mockProfile(profiles, "mod_a", ModResourcePackCreator.BASE_PARENT);

		assertTrue(((PackHooks) pack).fabric$isHiddenByParents(), "A parent-gated pack is hidden by parents");
		assertTrue(pack.isHidden(), "A parent-gated pack is hidden");

		assertTrue(pack.setHidden(false), "setHidden(false) should report a parent-gated pack as still hidden");
		assertTrue(((PackHooks) pack).fabric$isHiddenByParents(), "Clearing the explicit flag does not remove the parent predicate");
		assertTrue(pack.isHidden(), "A parent-gated pack stays hidden after setHidden(false)");
	}

	@Test
	void testExplicitlyHiddenPackIsNotAutoEnabled() {
		Map<String, Pack> profiles = new TreeMap<>();
		Pack vanilla = mockProfile(profiles, "vanilla", null);
		Pack hidden = mockProfile(profiles, "hidden_pack", null);
		hidden.setHidden(true);

		// An explicitly hidden pack must not be mistaken for a parent-gated internal pack.
		assertFalse(((PackHooks) hidden).fabric$isHiddenByParents(), "Explicit hiding must not set a parent predicate");

		var enabled = new ArrayList<>(List.of(vanilla));
		ModPackResourcesUtil.refreshAutoEnabledPacks(enabled, profiles);

		assertEquals(
				List.of(vanilla.getId()),
				enabled.stream().map(Pack::getId).toList(),
				"An explicitly hidden pack without a parent predicate must not be auto-enabled"
		);
		assertFalse(enabled.contains(hidden), "The explicitly hidden pack must not be auto-enabled");
	}

	private Pack mockProfile(Map<String, Pack> packs, String id, @Nullable Predicate<Set<String>> parents) {
		Pack pack = new Pack(
				new PackLocationInfo(
						id,
						null,
						null,
						Optional.empty()
				),
				null,
				null,
				new PackSelectionConfig(
						false,
						null,
						false)
		);

		if (parents != null) ((PackHooks) pack).fabric$setParentsPredicate(parents);

		packs.put(id, pack);
		return pack;
	}

	private void testRefreshAutoEnabledPacks(Map<String, Pack> profiles, List<Pack> before, List<Pack> after, String reason) {
		var processed = new ArrayList<>(before);
		ModPackResourcesUtil.refreshAutoEnabledPacks(processed, profiles);
		assertEquals(
				after.stream().map(Pack::getId).toList(),
				processed.stream().map(Pack::getId).toList(),
				() -> "Testing %s; input %s".formatted(reason, before.stream().map(Pack::getId).toList())
		);
	}

	@Test
	void testSerializeMetadata() {
		// Test various metadata serialization issues (#2407)
		testMetadataSerialization("");
		testMetadataSerialization("Quotes: \"\" \"");
		testMetadataSerialization("Backslash: \\ \\\\");
	}

	private void testMetadataSerialization(String description) throws JsonParseException {
		String metadata = ModPackResourcesUtil.serializeMetadata(new PackFormat(1, 0), description, PackType.CLIENT_RESOURCES);
		JsonObject json = assertDoesNotThrow(() -> GSON.fromJson(metadata, JsonObject.class), () -> "Failed to serialize " + description);

		String parsedDescription = json.get("pack").getAsJsonObject().get("description").getAsString();
		assertEquals(description, parsedDescription, "Parsed description differs from original one");
	}
}
