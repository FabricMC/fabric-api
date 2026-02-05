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

package net.fabricmc.fabric.test.transfer.unittests;

import static net.fabricmc.fabric.test.transfer.TestUtil.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;

/**
 * Test that fluid attributes for vanilla fluids have the correct values.
 */
public class AttributeTests extends AbstractTransferApiTest {
	@BeforeAll
	static void beforeAll() {
		bootstrap();
	}

	@Test
	public void testWater() {
		FluidVariant water = FluidVariant.of(Fluids.WATER);

		assertEquals(SoundEvents.BUCKET_FILL, FluidVariantAttributes.getFillSound(water));
		assertEquals(SoundEvents.BUCKET_EMPTY, FluidVariantAttributes.getEmptySound(water));
		assertEquals(0, FluidVariantAttributes.getLuminance(water));
		assertEquals(FluidConstants.WATER_TEMPERATURE, FluidVariantAttributes.getTemperature(water));
		assertEquals(FluidConstants.WATER_VISCOSITY, FluidVariantAttributes.getViscosity(water, null));
		assertEquals(false, FluidVariantAttributes.isLighterThanAir(water));
	}

	@Test
	public void testLava() {
		FluidVariant lava = FluidVariant.of(Fluids.LAVA);

		assertEquals(SoundEvents.BUCKET_FILL_LAVA, FluidVariantAttributes.getFillSound(lava));
		assertEquals(SoundEvents.BUCKET_EMPTY_LAVA, FluidVariantAttributes.getEmptySound(lava));
		assertEquals(15, FluidVariantAttributes.getLuminance(lava));
		assertEquals(FluidConstants.LAVA_TEMPERATURE, FluidVariantAttributes.getTemperature(lava));
		assertEquals(FluidConstants.LAVA_VISCOSITY, FluidVariantAttributes.getViscosity(lava, null));
		assertEquals(false, FluidVariantAttributes.isLighterThanAir(lava));
	}
}
