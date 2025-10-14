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

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Blocks;
import net.minecraft.class_12206;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;

/**
 * Common fluid variant attributes, accessible both client-side and server-side.
 */
public final class FluidVariantAttributes {
	private static final ApiProviderMap<Fluid, FluidVariantAttributeHandler> HANDLERS = ApiProviderMap.create();
	private static final FluidVariantAttributeHandler DEFAULT_HANDLER = new FluidVariantAttributeHandler() { };
	private static volatile boolean coloredVanillaFluidNames = false;

	private FluidVariantAttributes() {
	}

	/**
	 * Register an attribute handler for the passed fluid.
	 */
	public static void register(Fluid fluid, FluidVariantAttributeHandler handler) {
		if (HANDLERS.putIfAbsent(fluid, handler) != null) {
			throw new IllegalArgumentException("Duplicate handler registration for fluid " + fluid);
		}
	}

	/**
	 * Enable blue- and red-colored names for water and lava respectively.
	 */
	public static void enableColoredVanillaFluidNames() {
		coloredVanillaFluidNames = true;
	}

	/**
	 * Return the attribute handler for the passed fluid, if available, and {@code null} otherwise.
	 */
	@Nullable
	public static FluidVariantAttributeHandler getHandler(Fluid fluid) {
		return HANDLERS.get(fluid);
	}

	/**
	 * Return the attribute handler for the passed fluid, if available, or the default instance otherwise.
	 */
	public static FluidVariantAttributeHandler getHandlerOrDefault(Fluid fluid) {
		FluidVariantAttributeHandler handler = HANDLERS.get(fluid);
		return handler == null ? DEFAULT_HANDLER : handler;
	}

	/**
	 * Return the name that should be used for the passed fluid variant.
	 */
	public static Text getName(FluidVariant variant) {
		return getHandlerOrDefault(variant.getFluid()).getName(variant);
	}

	/**
	 * Return the sound corresponding to a container of this fluid variant being filled if available,
	 * or the default (water) filling sound otherwise.
	 */
	public static SoundEvent getFillSound(FluidVariant variant) {
		return getHandlerOrDefault(variant.getFluid()).getFillSound(variant)
				.or(() -> variant.getFluid().getBucketFillSound())
				.orElse(SoundEvents.ITEM_BUCKET_FILL);
	}

	/**
	 * Return the sound corresponding to a container of this fluid variant being emptied if available,
	 * or the default (water) emptying sound otherwise.
	 */
	public static SoundEvent getEmptySound(FluidVariant variant) {
		return getHandlerOrDefault(variant.getFluid()).getEmptySound(variant).orElse(SoundEvents.ITEM_BUCKET_EMPTY);
	}

	/**
	 * Return an integer in [0, 15]: the light level emitted by this fluid variant, or 0 if it doesn't naturally emit light.
	 */
	public static int getLuminance(FluidVariant variant) {
		int luminance = getHandlerOrDefault(variant.getFluid()).getLuminance(variant);

		if (luminance < 0 || luminance > 15) {
			TransferApiImpl.LOGGER.warn("Broken FluidVariantAttributeHandler. Invalid luminance %d for fluid variant %s".formatted(luminance, variant));
			return DEFAULT_HANDLER.getLuminance(variant);
		}

		return luminance;
	}

	/**
	 * Return a non-negative integer, representing the temperature of this fluid in Kelvin.
	 * The reference values are {@value FluidConstants#WATER_TEMPERATURE} for water, and {@value FluidConstants#LAVA_TEMPERATURE} for lava.
	 */
	public static int getTemperature(FluidVariant variant) {
		int temperature = getHandlerOrDefault(variant.getFluid()).getTemperature(variant);

		if (temperature < 0) {
			TransferApiImpl.LOGGER.warn("Broken FluidVariantAttributeHandler. Invalid temperature %d for fluid variant %s".formatted(temperature, variant));
			return DEFAULT_HANDLER.getTemperature(variant);
		}

		return temperature;
	}

	/**
	 * Return a positive integer, representing the viscosity of this fluid variant.
	 * Fluids with lower viscosity generally flow faster than fluids with higher viscosity.
	 *
	 * <p>More precisely, viscosity should be {@value FluidConstants#VISCOSITY_RATIO} * {@link FlowableFluid#getMaxFlowDistance} for flowable fluids.
	 * The reference values are {@value FluidConstants#WATER_VISCOSITY} for water,
	 * {@value FluidConstants#LAVA_VISCOSITY_NETHER} for lava in ultrawarm dimensions (such as the nether),
	 * and {@value FluidConstants#LAVA_VISCOSITY} for lava in other dimensions.
	 *
	 * @param world World if available, otherwise null.
	 */
	public static int getViscosity(FluidVariant variant, @Nullable World world) {
		int viscosity = getHandlerOrDefault(variant.getFluid()).getViscosity(variant, world);

		if (viscosity <= 0) {
			TransferApiImpl.LOGGER.warn("Broken FluidVariantAttributeHandler. Invalid viscosity %d for fluid variant %s".formatted(viscosity, variant));
			return DEFAULT_HANDLER.getViscosity(variant, world);
		}

		return viscosity;
	}

	/**
	 * Return true if this fluid is lighter than air.
	 * Fluids that are lighter than air generally flow upwards.
	 */
	public static boolean isLighterThanAir(FluidVariant variant) {
		return getHandlerOrDefault(variant.getFluid()).isLighterThanAir(variant);
	}

	static {
		register(Fluids.WATER, new FluidVariantAttributeHandler() {
			@Override
			public Text getName(FluidVariant fluidVariant) {
				if (coloredVanillaFluidNames) {
					return Blocks.WATER.getName().setStyle(Style.EMPTY.withColor(Formatting.BLUE));
				} else {
					return FluidVariantAttributeHandler.super.getName(fluidVariant);
				}
			}

			@Override
			public Optional<SoundEvent> getEmptySound(FluidVariant variant) {
				return Optional.of(SoundEvents.ITEM_BUCKET_EMPTY);
			}
		});
		register(Fluids.LAVA, new FluidVariantAttributeHandler() {
			@Override
			public Text getName(FluidVariant fluidVariant) {
				if (coloredVanillaFluidNames) {
					return Blocks.LAVA.getName().setStyle(Style.EMPTY.withColor(Formatting.RED));
				} else {
					return FluidVariantAttributeHandler.super.getName(fluidVariant);
				}
			}

			@Override
			public Optional<SoundEvent> getFillSound(FluidVariant variant) {
				return Optional.of(SoundEvents.ITEM_BUCKET_FILL_LAVA);
			}

			@Override
			public Optional<SoundEvent> getEmptySound(FluidVariant variant) {
				return Optional.of(SoundEvents.ITEM_BUCKET_EMPTY_LAVA);
			}

			@Override
			public int getTemperature(FluidVariant variant) {
				return FluidConstants.LAVA_TEMPERATURE;
			}

			@Override
			public int getViscosity(FluidVariant variant, @Nullable World world) {
				if (world != null && world.method_75598().method_75694(class_12206.FAST_LAVA_GAMEPLAY)) {
					return FluidConstants.LAVA_VISCOSITY_NETHER;
				} else {
					return FluidConstants.LAVA_VISCOSITY;
				}
			}
		});
	}
}
