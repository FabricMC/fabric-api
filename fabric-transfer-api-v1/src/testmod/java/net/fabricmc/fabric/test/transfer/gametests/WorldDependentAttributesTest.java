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

package net.fabricmc.fabric.test.transfer.gametests;

import static net.fabricmc.fabric.test.transfer.TestUtil.assertEquals;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;

public class WorldDependentAttributesTest {
	@GameTest
	public void testViscosity(GameTestHelper context) {
		ServerLevel overworld = context.getLevel();
		ServerLevel nether = overworld.getServer().getLevel(ServerLevel.NETHER);
		FluidVariant lava = FluidVariant.of(Fluids.LAVA);

		// Test that lava viscosity correctly depends on the dimension.
		assertEquals(FluidConstants.LAVA_VISCOSITY, FluidVariantAttributes.getViscosity(lava, overworld));
		assertEquals(FluidConstants.LAVA_VISCOSITY_NETHER, FluidVariantAttributes.getViscosity(lava, nether));

		// Test that lava and water viscosities match VISCOSITY_RATIO * tick rate
		assertEquals(FluidConstants.WATER_VISCOSITY, FluidConstants.VISCOSITY_RATIO * Fluids.WATER.getTickDelay(overworld));
		assertEquals(FluidConstants.WATER_VISCOSITY, FluidConstants.VISCOSITY_RATIO * Fluids.WATER.getTickDelay(nether));
		assertEquals(FluidConstants.LAVA_VISCOSITY, FluidConstants.VISCOSITY_RATIO * Fluids.LAVA.getTickDelay(overworld));
		assertEquals(FluidConstants.LAVA_VISCOSITY_NETHER, FluidConstants.VISCOSITY_RATIO * Fluids.LAVA.getTickDelay(nether));

		context.succeed();
	}
}
