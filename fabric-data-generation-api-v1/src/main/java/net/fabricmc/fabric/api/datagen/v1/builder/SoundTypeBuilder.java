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

package net.fabricmc.fabric.api.datagen.v1.builder;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricSoundsProvider;
import net.fabricmc.fabric.impl.datagen.SoundTypeBuilderImpl;

/**
 * Provides functionality for building entries that go into a {@code sounds.json} file.
 *
 * <p>Use in conjunction with {@link FabricSoundsProvider} to generate sound definitions.
 *
 * @see net.minecraft.client.sound.SoundManager
 * @see net.minecraft.client.sound.WeightedSoundSet
 */
public interface SoundTypeBuilder {
	/**
	 * Creates a new builder pre-filled with a subtitle translation string based on the passed event.
	 *
	 * @param event The sound event.
	 * @return New sound type builder
	 */
	static SoundTypeBuilder of(SoundEvent event) {
		Preconditions.checkArgument(event != null, "Sound event must not be null.");
		return of().subtitle("subtitles." + event.id().getNamespace() + "." + event.id().getPath());
	}

	/**
	 * Creates a new empty builder.
	 *
	 * @param event The sound event.
	 * @return New sound type builder
	 */
	static SoundTypeBuilder of() {
		return new SoundTypeBuilderImpl();
	}

	/**
	 * Sets the sound category the sound event must play on.
	 *
	 * <p>The default category is {@link SoundCategory#NEUTRAL}. GUI elements should use {@link SoundCategory#MASTER}.
	 */
	SoundTypeBuilder category(SoundCategory category);

	/**
	 * Sets an optional translation string to use for the sound's subtitle.
	 *
	 * <p>The default is null (no subtitle).
	 */
	SoundTypeBuilder subtitle(@Nullable String subtitle);

	/**
	 * Adds one sound to the event.
	 */
	SoundTypeBuilder sound(EntryBuilder sound);

	/**
	 * Adds one or more sounds to the event.
	 * This is a shorthand method for quickly adding multiple entries where
	 * each sound is a variant with an index.
	 *
	 * @param sound The base sound to add.
	 * @param count The number of instances of that sound to register.
	 */
	SoundTypeBuilder sound(EntryBuilder sound, int count);

	/**
	 * @see net.minecraft.client.sound.Sound.RegistrationType
	 */
	enum RegistrationType implements StringIdentifiable {
		FILE("file"),
		SOUND_EVENT("event");

		public static final Codec<RegistrationType> CODEC = StringIdentifiable.createCodec(RegistrationType::values);

		private final String name;

		RegistrationType(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return name;
		}
	}

	interface EntryBuilder {
		/**
		 * Creates a builder for constructing a new sound entry.
		 *
		 * @param name The Identifier of the sound file or event this entry must reference.
		 */
		static EntryBuilder builder(RegistrationType type, Identifier name) {
			return SoundTypeBuilderImpl.EntryBuilderImpl.builder(type, name);
		}

		/**
		 * Creates a builder for constructing a new sound entry.
		 *
		 * @param soundFile The Identifier pointing to the sound file (minus .ogg extension).
		 */
		static EntryBuilder ofFile(Identifier soundFile) {
			return SoundTypeBuilderImpl.EntryBuilderImpl.ofFile(soundFile);
		}

		/**
		 * Creates a builder for constructing a new sound entry.
		 *
		 * @param event The sound event this entry must point to.
		 */
		static EntryBuilder ofEvent(SoundEvent event) {
			return SoundTypeBuilderImpl.EntryBuilderImpl.ofEvent(event);
		}

		/**
		 * Sets the volume of the sound.
		 *
		 * @param volume The volume.
		 */
		EntryBuilder volume(float volume);

		/**
		 * Sets the pitch of the sound.
		 *
		 * @param pitch The sound's pitch value.
		 */
		EntryBuilder pitch(float pitch);

		/**
		 * Sets the attenuation block distance of the sound.
		 *
		 * <p>The default attenuation is 16 blocks. Setting it to
		 * higher will cause the sound to be heard from greater distances.
		 */
		EntryBuilder attenuationDistance(int attenuationDistance);

		/**
		 * Sets the weight or "chance" that this sound has of playing when
		 * its parent sound event is called upon.
		 *
		 * <p>The default weight is 1.
		 */
		EntryBuilder weight(int weight);

		/**
		 * Configures the sound to be streamed.
		 * This is usually set for longer sounds like music disks
		 * to prevent delays when the game attempts playing them
		 */
		EntryBuilder stream(boolean stream);

		/**
		 * Configures whether the sound must be pre-loaded by the game.
		 * By default sounds are only loaded the first time they're used.
		 * Set preload to <code>true</code> will cause them to be loaded upon game start.
		 */
		EntryBuilder preload(boolean preload);
	}
}
