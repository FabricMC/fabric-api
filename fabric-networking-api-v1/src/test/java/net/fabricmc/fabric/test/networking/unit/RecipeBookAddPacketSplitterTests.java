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

package net.fabricmc.fabric.test.networking.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.RecipeBookAddS2CPacket;
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.display.FurnaceRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.IndexedIterable;

import net.fabricmc.fabric.impl.networking.RecipeBookAddPacketSplitter;

public class RecipeBookAddPacketSplitterTests {
	private static final Text LONG_TEXT = Text.of("x".repeat(10000));

	@BeforeAll
	static void beforeAll() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void sendEmpty() {
		RecipeBookAddS2CPacket packet = new RecipeBookAddS2CPacket(List.of(), true);
		List<RecipeBookAddS2CPacket> splitPackets = getSplitPackets(packet);

		assertEquals(1, splitPackets.size());
		assertTrue(splitPackets.getFirst().replace());
		assertEquals(0, splitPackets.getFirst().entries().size());
	}

	@Test
	void sendSingleEntry() {
		RecipeBookAddS2CPacket packet = new RecipeBookAddS2CPacket(entries(1), true);
		List<RecipeBookAddS2CPacket> splitPackets = getSplitPackets(packet);

		assertEquals(1, splitPackets.size());
		assertTrue(splitPackets.getFirst().replace());
		assertEquals(1, splitPackets.getFirst().entries().size());
	}

	@Test
	void sendFewEntries() {
		RecipeBookAddS2CPacket packet = new RecipeBookAddS2CPacket(entries(5), true);
		List<RecipeBookAddS2CPacket> splitPackets = getSplitPackets(packet);

		assertEquals(1, splitPackets.size());
		assertTrue(splitPackets.getFirst().replace());
		assertEquals(5, splitPackets.getFirst().entries().size());
	}

	@Test
	void sendManyEntriesReplace() {
		RecipeBookAddS2CPacket packet = new RecipeBookAddS2CPacket(entries(1000), true);
		List<RecipeBookAddS2CPacket> splitPackets = getSplitPackets(packet);

		assertEquals(5, splitPackets.size());
		assertTrue(splitPackets.getFirst().replace());
		assertFalse(splitPackets.get(1).replace());
		assertEquals(1000, splitPackets.stream().mapToLong(p -> p.entries().size()).sum());
	}

	@Test
	void sendManyEntriesDontReplace() {
		RecipeBookAddS2CPacket packet = new RecipeBookAddS2CPacket(entries(1000), false);
		List<RecipeBookAddS2CPacket> splitPackets = getSplitPackets(packet);

		assertEquals(5, splitPackets.size());
		assertFalse(splitPackets.getFirst().replace());
		assertFalse(splitPackets.getLast().replace());
		assertEquals(1000, splitPackets.stream().mapToLong(p -> p.entries().size()).sum());
	}

	private static List<RecipeBookAddS2CPacket> getSplitPackets(RecipeBookAddS2CPacket packet) {
		ServerPlayNetworkHandler networkHandler = getMockNetworkHandler();

		List<RecipeBookAddS2CPacket> sentPackets = new ArrayList<>();
		RecipeBookAddPacketSplitter.split(packet, networkHandler, sentPackets::add);
		return sentPackets;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static ServerPlayNetworkHandler getMockNetworkHandler() {
		IndexedIterable indexedEntries = mock(IndexedIterable.class);
		when(indexedEntries.getRawIdOrThrow(any())).thenReturn(0);

		Registry<?> registry = mock(Registry.class);
		when(registry.getIndexedEntries()).thenReturn(indexedEntries);

		DynamicRegistryManager drm = mock(DynamicRegistryManager.class);
		when(drm.getOps(any())).thenReturn((RegistryOps<Object>) (Object) RegistryOps.of(NbtOps.INSTANCE, drm));
		when(drm.getOrThrow(any())).thenReturn((Registry<Object>) registry);

		ServerPlayerEntity player = mock(ServerPlayerEntity.class);
		when(player.getRegistryManager()).thenReturn(drm);

		ServerPlayNetworkHandler handler = mock(ServerPlayNetworkHandler.class);
		when(handler.getPlayer()).thenReturn(player);

		return handler;
	}

	private static List<RecipeBookAddS2CPacket.Entry> entries(int size) {
		List<RecipeBookAddS2CPacket.Entry> entries = new ArrayList<>();

		for (int i = 0; i < size; i++) {
			entries.add(entry(i));
		}

		return entries;
	}

	private static RecipeBookAddS2CPacket.Entry entry(int id) {
		ItemStack largeStack = new ItemStack(Items.DIAMOND);
		largeStack.set(DataComponentTypes.CUSTOM_NAME, LONG_TEXT);
		SlotDisplay.StackSlotDisplay slotDisplay = new SlotDisplay.StackSlotDisplay(largeStack);

		final var display = new FurnaceRecipeDisplay(
				slotDisplay,
				slotDisplay,
				slotDisplay,
				slotDisplay,
				100,
				5f
		);
		return new RecipeBookAddS2CPacket.Entry(
				new RecipeDisplayEntry(
						new NetworkRecipeId(id),
						display,
						OptionalInt.empty(),
						RecipeBookCategories.FURNACE_MISC,
						Optional.empty()
				),
				true,
				true
		);
	}
}
