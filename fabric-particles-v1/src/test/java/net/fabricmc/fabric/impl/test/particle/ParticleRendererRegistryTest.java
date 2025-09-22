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

package net.fabricmc.fabric.impl.test.particle;

import static net.fabricmc.fabric.api.client.particle.v1.ParticleRendererRegistry.getId;
import static net.minecraft.client.particle.ParticleTextureSheet.ELDER_GUARDIANS;
import static net.minecraft.client.particle.ParticleTextureSheet.ITEM_PICKUP;
import static net.minecraft.client.particle.ParticleTextureSheet.NO_RENDER;
import static net.minecraft.client.particle.ParticleTextureSheet.SINGLE_QUADS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.client.particle.ParticleTextureSheet;

import net.fabricmc.fabric.impl.client.particle.ParticleRendererRegistryImpl;

public class ParticleRendererRegistryTest {
	List<ParticleTextureSheet> sheets = getVanillaSheets();

	@Test
	void testInitialSorting() {
		var registry = new ParticleRendererRegistryImpl(sheets);

		assertSame(SINGLE_QUADS, sheets.getFirst());
		assertSame(ITEM_PICKUP, sheets.get(1));
		assertSame(ELDER_GUARDIANS, sheets.get(2));
		assertSame(NO_RENDER, sheets.getLast());
		assertEquals(4, sheets.size());
	}

	@Test
	void insertBefore() {
		var registry = new ParticleRendererRegistryImpl(sheets);

		var customSheet = new ParticleTextureSheet("mymod:custom");
		registry.register(customSheet, particleManager -> null);
		registry.registerOrdering(getId(customSheet), getId(ITEM_PICKUP));

		assertSame(customSheet, sheets.getFirst()); // TODO is this expected behavior?
		assertSame(SINGLE_QUADS, sheets.get(1));
		assertSame(ITEM_PICKUP, sheets.get(2));
		assertSame(ELDER_GUARDIANS, sheets.get(3));
		assertSame(NO_RENDER, sheets.getLast());
		assertEquals(5, sheets.size());
	}

	@Test
	void insertAfter() {
		var registry = new ParticleRendererRegistryImpl(sheets);

		var customSheet = new ParticleTextureSheet("mymod:custom");
		registry.register(customSheet, particleManager -> null);
		registry.registerOrdering(getId(ITEM_PICKUP), getId(customSheet));

		assertSame(SINGLE_QUADS, sheets.getFirst());
		assertSame(ITEM_PICKUP, sheets.get(1));
		assertSame(customSheet, sheets.get(2));
		assertSame(ELDER_GUARDIANS, sheets.get(3));
		assertSame(NO_RENDER, sheets.getLast());
		assertEquals(5, sheets.size());
	}

	private static List<ParticleTextureSheet> getVanillaSheets() {
		var list = new ArrayList<ParticleTextureSheet>();
		list.add(SINGLE_QUADS);
		list.add(ITEM_PICKUP);
		list.add(ELDER_GUARDIANS);
		list.add(NO_RENDER);
		return list;
	}
}
