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

package net.fabricmc.fabric.test.resource.loader.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.resource.PackVersion;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackPosition;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;

import net.fabricmc.fabric.impl.resource.loader.FabricResourcePackProfile;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;

public class ModResourcePackUtilTests {
	private static final Gson GSON = new Gson();

	@BeforeAll
	static void beforeAll() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void testRefreshAutoEnabledPacks() {
		// Vanilla uses tree map, and we test the behavior
		Map<String, ResourcePackProfile> profiles = new TreeMap<>();
		Map<String, ResourcePackProfile> modAProfiles = new TreeMap<>();
		Map<String, ResourcePackProfile> modBProfiles = new TreeMap<>();
		Map<String, ResourcePackProfile> allProfiles = new TreeMap<>();
		ResourcePackProfile vanilla = mockProfile(profiles, "vanilla", null);
		ResourcePackProfile fabric = mockProfile(profiles, ModResourcePackCreator.FABRIC, null);
		ResourcePackProfile modA = mockProfile(modAProfiles, "mod_a", ModResourcePackCreator.BASE_PARENT);
		ResourcePackProfile modAProg = mockProfile(modAProfiles, "mod_a_programmer_art", ModResourcePackCreator.PROGRAMMER_ART_PARENT);
		ResourcePackProfile modAHigh = mockProfile(modAProfiles, "mod_a_high_contrast", ModResourcePackCreator.HIGH_CONTRAST_PARENT);
		ResourcePackProfile modB = mockProfile(modBProfiles, "mod_b", ModResourcePackCreator.BASE_PARENT);
		ResourcePackProfile modBProg = mockProfile(modBProfiles, "mod_b_programmer_art", ModResourcePackCreator.PROGRAMMER_ART_PARENT);
		ResourcePackProfile modBHigh = mockProfile(modBProfiles, "mod_b_high_contrast", ModResourcePackCreator.HIGH_CONTRAST_PARENT);
		ResourcePackProfile programmerArt = mockProfile(profiles, "programmer_art", null);
		ResourcePackProfile highContrast = mockProfile(profiles, "high_contrast", null);
		ResourcePackProfile userPackA = mockProfile(profiles, "user_pack_a", null);
		ResourcePackProfile userPackB = mockProfile(profiles, "user_pack_b", null);
		modAProfiles.putAll(profiles);
		modBProfiles.putAll(profiles);
		allProfiles.putAll(modAProfiles);
		allProfiles.putAll(modBProfiles);

		testRefreshAutoEnabledPacks(
				profiles,
				List.of(vanilla, fabric),
				List.of(vanilla, fabric),
				"keep (no mods)"
		);
		testRefreshAutoEnabledPacks(
				profiles,
				List.of(vanilla, fabric, userPackA),
				List.of(vanilla, fabric, userPackA),
				"keep (no mods, keep user pack)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA),
				List.of(vanilla, fabric, modA),
				"keep (mod A only)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA, programmerArt, modAProg),
				List.of(vanilla, fabric, modA, programmerArt, modAProg),
				"keep (programmer_art)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, fabric, modA, modB, programmerArt, modAProg, modBProg),
				List.of(vanilla, fabric, modA, modB, programmerArt, modAProg, modBProg),
				"keep (mod A and mod B, programmer_art)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, fabric, modA, modB, programmerArt, modAProg, modBProg, highContrast, modAHigh, modBHigh),
				List.of(vanilla, fabric, modA, modB, programmerArt, modAProg, modBProg, highContrast, modAHigh, modBHigh),
				"keep (mod A and mod B, both)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, fabric, modA, modB, highContrast, modAHigh, modBHigh, programmerArt, modAProg, modBProg),
				List.of(vanilla, fabric, modA, modB, highContrast, modAHigh, modBHigh, programmerArt, modAProg, modBProg),
				"keep (remembers programmer_art-high_contrast order)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric),
				List.of(vanilla, fabric, modA),
				"fix (adding missing mods)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, fabric, userPackA),
				List.of(vanilla, fabric, modA, modB, userPackA),
				"fix (adding missing mods at the right place)"
		);
		testRefreshAutoEnabledPacks(
				allProfiles,
				List.of(vanilla, fabric, modB, modA),
				List.of(vanilla, fabric, modA, modB),
				"fix (mod A and B, sorting)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, userPackB, modA, userPackA),
				List.of(vanilla, fabric, modA, userPackB, userPackA),
				"fix (user pack goes last)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA, programmerArt),
				List.of(vanilla, fabric, modA, programmerArt, modAProg),
				"fix (adding 1 met dep)"
		);
		testRefreshAutoEnabledPacks(
				modBProfiles,
				List.of(vanilla, fabric, modB, highContrast),
				List.of(vanilla, fabric, modB, highContrast, modBHigh),
				"fix (adding 1 met dep, part 2)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA, programmerArt, highContrast),
				List.of(vanilla, fabric, modA, programmerArt, modAProg, highContrast, modAHigh),
				"fix (adding 2 met deps)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA, programmerArt, modAProg, highContrast),
				List.of(vanilla, fabric, modA, programmerArt, modAProg, highContrast, modAHigh),
				"fix (adding 2 met deps + preexisting)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA, modAProg, modAHigh),
				List.of(vanilla, fabric, modA),
				"fix (removing 2 unmet deps)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modA, programmerArt, modAProg, modAHigh),
				List.of(vanilla, fabric, modA, programmerArt, modAProg),
				"fix (removing 1 unmet dep)"
		);
		testRefreshAutoEnabledPacks(
				modBProfiles,
				List.of(vanilla, fabric, modB, highContrast, modBProg, modBHigh),
				List.of(vanilla, fabric, modB, highContrast, modBHigh),
				"fix (removing 1 unmet dep, part 2)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modAProg, programmerArt, modA),
				List.of(vanilla, fabric, modA, programmerArt, modAProg),
				"reorder (bundled comes just after parents)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, modAProg, userPackA, programmerArt, modA, userPackB),
				List.of(vanilla, fabric, modA, userPackA, programmerArt, modAProg, userPackB),
				"reorder (keep user pack order)"
		);
		testRefreshAutoEnabledPacks(
				modAProfiles,
				List.of(vanilla, fabric, userPackB, modA, programmerArt, userPackA, modAProg),
				List.of(vanilla, fabric, modA, userPackB, programmerArt, modAProg, userPackA),
				"reorder (no user pack between parent-bundled)"
		);
	}

	private ResourcePackProfile mockProfile(Map<String, ResourcePackProfile> profiles, String id, @Nullable Predicate<Set<String>> parents) {
		ResourcePackProfile profile = new ResourcePackProfile(
				new ResourcePackInfo(
						id,
						null,
						null,
						Optional.empty()
				),
				null,
				null,
				new ResourcePackPosition(
						false,
						null,
						false)
		);

		if (parents != null) ((FabricResourcePackProfile) profile).fabric_setParentsPredicate(parents);

		profiles.put(id, profile);
		return profile;
	}

	private void testRefreshAutoEnabledPacks(Map<String, ResourcePackProfile> profiles, List<ResourcePackProfile> before, List<ResourcePackProfile> after, String reason) {
		List<ResourcePackProfile> processed = new ArrayList<>(before);
		ModResourcePackUtil.refreshAutoEnabledPacks(processed, profiles);
		assertEquals(
				after.stream().map(ResourcePackProfile::getId).toList(),
				processed.stream().map(ResourcePackProfile::getId).toList(),
				() -> "Testing %s; input %s".formatted(reason, before.stream().map(ResourcePackProfile::getId).toList())
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
		String metadata = ModResourcePackUtil.serializeMetadata(new PackVersion(1, 0), description, ResourceType.CLIENT_RESOURCES);
		JsonObject json = assertDoesNotThrow(() -> GSON.fromJson(metadata, JsonObject.class), () -> "Failed to serialize " + description);

		String parsedDescription = json.get("pack").getAsJsonObject().get("description").getAsString();
		assertEquals(description, parsedDescription, "Parsed description differs from original one");
	}
}
