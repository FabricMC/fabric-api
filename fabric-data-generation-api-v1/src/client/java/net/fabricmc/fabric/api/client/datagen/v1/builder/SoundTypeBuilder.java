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

package net.fabricmc.fabric.api.client.datagen.v1.builder;

import java.util.Objects;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider;
import net.fabricmc.fabric.impl.datagen.client.SoundTypeBuilderImpl;

/**
 * Provides functionality for building entries that go into a {@code sounds.json} file.
 *
 * <p>Use in conjunction with {@link FabricSoundsProvider} to generate sound definitions.
 *
 * @see net.minecraft.client.sound.SoundManager
 * @see net.minecraft.client.sound.WeightedSoundSet
 */
@ApiStatus.NonExtendable
public interface SoundTypeBuilder {
	/**
	 * Creates a new builder pre-filled with a subtitle translation string based on the passed event.
	 *
	 * @return New sound type builder
	 */
	static SoundTypeBuilder of(SoundEvent event) {
		Objects.requireNonNull(event, "Sound event cannot be null.");
		return of().subtitle(Util.createTranslationKey("subtitles", event.id()));
	}

	/**
	 * Creates a new empty builder.
	 *
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
	 *
	 * @param sound base sound to add
	 */
	SoundTypeBuilder sound(EntryBuilder sound);

	/**
	 * Adds one or more sounds to the event.
	 *
	 * <p>This is a shorthand method for quickly adding multiple
	 * entries where each sound is a variant with an index at the end of their name.
	 *
	 * <p>Calling this with the a count value of {@code 3} is the equivalent of doing:
	 *
	 * <p>{@code builder.sound(EntryBuilder.ofFile(id.withSuffixedPath("1"));}
	 * {@code builder.sound(EntryBuilder.ofFile(id.withSuffixedPath("2"));}
	 * {@code builder.sound(EntryBuilder.ofFile(id.withSuffixedPath("3"));}
	 *
	 * @param sound base sound to add
	 * @param count number of instances of that sound to register
	 */
	SoundTypeBuilder sound(EntryBuilder sound, int count);

	/**
	 * Represents the type of weighted sound entry.
	 *
	 * @see net.minecraft.client.sound.Sound.RegistrationType
	 */
	enum RegistrationType implements StringIdentifiable {
		/**
		 * Direct references to sound files by path and filename excluding {@code *.ogg} extension.
		 */
		FILE("file"),
		/**
		 * References to another sound event.
		 */
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

	/**
	 * Builder for creating a weighted sound entry that can be played for a particular sound type.
	 */
	@ApiStatus.NonExtendable
	interface EntryBuilder {
		/**
		 * The default sound volume.
		 */
		float DEFAULT_VOLUME = 1F;

		/**
		 * The default sound pitch.
		 */
		float DEFAULT_PITCH = 1F;

		/**
		 * The default weight applied to individual sounds.
		 */
		int DEFAULT_WEIGHT = 1;

		/**
		 * The default attentuation distance for a sound (16 blocks).
		 */
		int DEFAULT_ATTENUATION_DISTANCE = 16;

		/**
		 * Creates a builder for constructing a new sound entry.
		 *
		 * @param id sound file or event
		 */
		static EntryBuilder create(RegistrationType type, Identifier id) {
			return SoundTypeBuilderImpl.EntryBuilderImpl.create(type, id);
		}

		/**
		 * Creates a builder for constructing a new sound entry.
		 *
		 * @param soundFile sound file excluding the {@code .ogg} extension
		 */
		static EntryBuilder ofFile(Identifier soundFile) {
			return SoundTypeBuilderImpl.EntryBuilderImpl.ofFile(soundFile);
		}

		/**
		 * Creates a builder for constructing a new sound entry.
		 *
		 * @param event the sound event
		 */
		static EntryBuilder ofEvent(SoundEvent event) {
			return SoundTypeBuilderImpl.EntryBuilderImpl.ofEvent(event);
		}

		/**
		 * Creates a builder for constructing a new sound entry.
		 *
		 * @param event the sound event
		 */
		static EntryBuilder ofEvent(RegistryEntry<SoundEvent> event) {
			return SoundTypeBuilderImpl.EntryBuilderImpl.ofEvent(event);
		}

		/**
		 * Sets the volume of the sound.
		 *
		 * <p>Must be a value between {@code 0} and {@code 1} (inclusive).
		 *
		 * <p>The default volume is {@link EntryBuilder.DEFAULT_VOLUME} ({@code 1F}).
		 *
		 * @see net.minecraft.client.sound.SoundSystem#MIN_VOLUME
		 * @see net.minecraft.client.sound.SoundSystem#MAX_VOLUME
		 */
		EntryBuilder volume(float volume);

		/**
		 * Sets the pitch of the sound.
		 *
		 * <p>Must be a value between {@code 0.5} and {@code 2}.
		 *
		 * <p>The default pitch is {@link EntryBuilder.DEFAULT_PITCH} ({@code 1F}).
		 *
		 * @see net.minecraft.client.sound.SoundSystem#MIN_PITCH
		 * @see net.minecraft.client.sound.SoundSystem#MAX_PITCH
		 */
		EntryBuilder pitch(float pitch);

		/**
		 * Sets the attenuation block distance of the sound.
		 *
		 * <p>The default attenuation is {@link EntryBuilder.DEFAULT_ATTENUATION_DISTANCE} ({@code 16} blocks). Setting it to
		 * higher will cause the sound to be heard from greater distances.
		 */
		EntryBuilder attenuationDistance(int attenuationDistance);

		/**
		 * Sets the weight or "chance" that this sound has of playing when
		 * its parent sound event is called upon.
		 *
		 * <p>The default weight is {@link EntryBuilder.DEFAULT_WEIGHT} ({@code 1}).
		 */
		EntryBuilder weight(int weight);

		/**
		 * Configures the sound to be streamed.
		 * This is usually set for longer sounds like music discs
		 * to prevent delays when the game tries to play them.
		 *
		 * <p>The default value is {@code false}.
		 */
		EntryBuilder stream(boolean stream);

		/**
		 * Configures whether the sound must be pre-loaded by the game.
		 * By default, sounds are only loaded upon playing.
		 *
		 * <p>Setting this to {@code true} will cause them to be loaded when the game starts.
		 *
		 * <p>The default value is {@code false}.
		 */
		EntryBuilder preload(boolean preload);
	}
}
