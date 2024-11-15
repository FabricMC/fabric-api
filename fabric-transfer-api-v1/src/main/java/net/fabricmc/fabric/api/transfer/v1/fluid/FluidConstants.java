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

package net.fabricmc.fabric.api.transfer.v1.fluid;

import net.minecraft.fluid.FlowableFluid;

/**
 * Constants for fluid transfer. In general, 1 bucket = 81000 droplets = 1 block.
 *
 * <p>If you don't know how many droplets you should pick for a specific resource that has a block form,
 * the convention is to use 81000 droplets for what is worth one block of that resource.
 */
public final class FluidConstants {
	///////////////////////////
	// ==== FLUID UNITS ==== //
	///////////////////////////
	public static final long BUCKET = 81000;
	public static final long BOTTLE = 27000;
	public static final long BOWL = 27000;
	public static final long BLOCK = 81000;
	public static final long INGOT = 9000;
	public static final long NUGGET = 1000;
	public static final long DROPLET = 1;

	/**
	 * Convert a fraction of buckets into droplets.
	 *
	 * <p>For example, passing {@code (1, 3)} will return the 1/3 of a bucket as droplets, so 27000.
	 *
	 * @return The amount of droplets that the passed fraction is equivalent to.
	 * @throws IllegalArgumentException If the fraction can't be converted to droplets exactly.
	 */
	public static long fromBucketFraction(long numerator, long denominator) {
		long total = numerator * BUCKET;

		if (total % denominator != 0) {
			throw new IllegalArgumentException("Not a valid number of droplets!");
		} else {
			return total / denominator;
		}
	}

	// ==========================
	// ==== FLUID ATTRIBUTES ====
	// ==========================
	/**
	 * Water temperature, in Kelvin.
	 */
	public static final int WATER_TEMPERATURE = 300;
	/**
	 * Lava temperature, in Kelvin.
	 */
	public static final int LAVA_TEMPERATURE = 1300;

	public static final int WATER_VISCOSITY = 1000;
	public static final int LAVA_VISCOSITY = 6000;
	public static final int LAVA_VISCOSITY_NETHER = 2000;
	/**
	 * For flowable fluids, the viscosity should match {@code VISCOSITY_RATIO} * {@link FlowableFluid#getFlowSpeed}.
	 */
	public static final int VISCOSITY_RATIO = 200;

	private FluidConstants() {
	}
}
