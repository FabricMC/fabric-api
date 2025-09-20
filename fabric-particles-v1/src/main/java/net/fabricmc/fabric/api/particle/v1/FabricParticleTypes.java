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

package net.fabricmc.fabric.api.particle.v1;

import java.util.function.Function;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;

/**
 * Methods for creating particle types, both simple and using an existing attribute factory.
 *
 * <p>Usage:
 * <blockquote>
 * <pre>
 * public static final SimpleParticleType SIMPLE_TEST_PARTICLE = FabricParticleTypes.simple();
 * public static final SimpleParticleType CUSTOM_TEST_PARTICLE = FabricParticleTypes.simple();
 *
 * {@literal @}Override
 * public void onInitialize() {
 *     Registry.register(Registries.PARTICLE_TYPE, Identifier.of("testmod", "simple"), SIMPLE_TEST_PARTICLE);
 *     Registry.register(Registries.PARTICLE_TYPE, Identifier.of("testmod", "custom"), CUSTOM_TEST_PARTICLE);
 * }}
 * </pre>
 * </blockquote>
 */
public final class FabricParticleTypes {
	private FabricParticleTypes() { }

	/**
	 * Creates a new, default particle type for the given id.
	 */
	public static SimpleParticleType simple() {
		return simple(false);
	}

	/**
	 * Creates a new, default particle type for the given id.
	 *
	 * @param alwaysSpawn True to always spawn the particle regardless of distance.
	 */
	public static SimpleParticleType simple(boolean alwaysSpawn) {
		return new SimpleParticleType(alwaysSpawn) { };
	}

	/**
	 * Creates a new particle type with a custom factory and codecs for packet/data serialization.
	 *
	 * @param codec The codec for serialization.
	 * @param packetCodec The packet codec for network serialization.
	 */
	public static <T extends ParticleEffect> ParticleType<T> complex(final MapCodec<T> codec, final PacketCodec<? super RegistryByteBuf, T> packetCodec) {
		return complex(false, codec, packetCodec);
	}

	/**
	 * Creates a new particle type with a custom factory and codecs for packet/data serialization.
	 *
	 * @param alwaysSpawn True to always spawn the particle regardless of distance.
	 * @param codec The codec for serialization.
	 * @param packetCodec The packet codec for network serialization.
	 */
	public static <T extends ParticleEffect> ParticleType<T> complex(boolean alwaysSpawn, final MapCodec<T> codec, final PacketCodec<? super RegistryByteBuf, T> packetCodec) {
		return new ParticleType<>(alwaysSpawn) {
			@Override
			public MapCodec<T> getCodec() {
				return codec;
			}

			@Override
			public PacketCodec<? super RegistryByteBuf, T> getPacketCodec() {
				return packetCodec;
			}
		};
	}

	/**
	 * Creates a new particle type with a custom factory and codecs for packet/data serialization.
	 * This method is useful when two different {@link ParticleType}s share the same {@link ParticleEffect} implementation.
	 *
	 * @param codecGetter A function that, given the newly created type, returns the codec for serialization.
	 * @param packetCodecGetter A function that, given the newly created type, returns the packet codec for network serialization.
	 */
	public static <T extends ParticleEffect> ParticleType<T> complex(final Function<ParticleType<T>, MapCodec<T>> codecGetter, final Function<ParticleType<T>, PacketCodec<? super RegistryByteBuf, T>> packetCodecGetter) {
		return complex(false, codecGetter, packetCodecGetter);
	}

	/**
	 * Creates a new particle type with a custom factory and codecs for packet/data serialization.
	 * This method is useful when two different {@link ParticleType}s share the same {@link ParticleEffect} implementation.
	 *
	 * @param alwaysSpawn True to always spawn the particle regardless of distance.
	 * @param codecGetter A function that, given the newly created type, returns the codec for serialization.
	 * @param packetCodecGetter A function that, given the newly created type, returns the packet codec for network serialization.
	 */
	public static <T extends ParticleEffect> ParticleType<T> complex(boolean alwaysSpawn, final Function<ParticleType<T>, MapCodec<T>> codecGetter, final Function<ParticleType<T>, PacketCodec<? super RegistryByteBuf, T>> packetCodecGetter) {
		return new ParticleType<>(alwaysSpawn) {
			@Override
			public MapCodec<T> getCodec() {
				return codecGetter.apply(this);
			}

			@Override
			public PacketCodec<? super RegistryByteBuf, T> getPacketCodec() {
				return packetCodecGetter.apply(this);
			}
		};
	}
}
