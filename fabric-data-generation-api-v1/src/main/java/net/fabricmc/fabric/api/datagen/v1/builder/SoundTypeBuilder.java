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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.google.common.base.Strings;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricSoundsProvider;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

/**
 * Provides functionality for building entries that go into a sounds.json file.
 *
 * <p>Use in conjunction with {@link FabricSoundsProvider} to generate sound definitions.
 *
 * @see {@link net.minecraft.client.sound.SoundManager}
 * @see {@link net.minecraft.client.sound.WeightedSoundSet}
 */
public final class SoundTypeBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(FabricDataGenHelper.class);

	private SoundCategory category = SoundCategory.NEUTRAL;
	@Nullable
	private String subtitle;
	private final List<Sound> sounds = new ArrayList<>();

	/**
	 * Creates a new builder pre-filled with a subtitle translation string based on the passed event.
	 *
	 * @param event The sound event.
	 * @return New sound type builder
	 */
	public static SoundTypeBuilder of(SoundEvent event) {
		Preconditions.checkArgument(event != null, "Sound event must not be null.");
		return of().subtitle("subtitles." + event.id().getNamespace() + "." + event.id().getPath());
	}

	/**
	 * Creates a new empty builder.
	 *
	 * @param event The sound event.
	 * @return New sound type builder
	 */
	public static SoundTypeBuilder of() {
		return new SoundTypeBuilder();
	}

	private SoundTypeBuilder() { }

	/**
	 * Sets the sound category the sound event must play on.
	 *
	 * <p>The default category is {@link SoundCategory#NEUTRAL}. GUI elements should use {@link SoundCategory#MASTER}.
	 */
	public SoundTypeBuilder category(SoundCategory category) {
		Preconditions.checkArgument(category != null, "Sound event category must not be null.");
		this.category = category;
		return this;
	}

	/**
	 * Sets an optional translation string to use for the sound's subtitle.
	 *
	 * <p>The default is null (no subtitle).
	 */
	public SoundTypeBuilder subtitle(@Nullable String subtitle) {
		this.subtitle = subtitle;
		return this;
	}

	/**
	 * Adds one sound to the event.
	 */
	public SoundTypeBuilder sound(Sound.Builder sound) {
		Preconditions.checkArgument(sound != null, "Sound must not be null.");
		sounds.add(sound.build(""));
		return this;
	}

	/**
	 * Adds one or more sounds to the event.
	 * This is a shorthand method for quickly adding multiple entries where
	 * each sound is a variant with an index.
	 *
	 *
	 * @param sound The base sound to add.
	 * @param count The number of instances of that sound to register.
	 */
	public SoundTypeBuilder sound(Sound.Builder sound, int count) {
		Preconditions.checkArgument(sound != null, "Sound must not be null.");
		Preconditions.checkArgument(count > 0, "Count must be greater than zero.");
		for (int i = 1; i <= count; i++) {
			sounds.add(sound.build("" + i));
		}
		return this;
	}

	public SoundType build() {
		Preconditions.checkState(!sounds.isEmpty(), "Sound definition must have at least one sound file");
		for (Sound sound : sounds) {
			if (sound.type() == Sound.RegistrationType.SOUND_EVENT) {
				Registries.SOUND_EVENT.getOptionalValue(sound.name()).orElseThrow(() -> new IllegalStateException("References sound event " + sound.name() + " does not exist"));
			}
		}

		return new SoundType(sounds, category, Optional.ofNullable(subtitle));
	}

	/**
	 * The entry in a sounds.json file that defines the properties of each {@link SoundEvent} object.
	 */
	public record SoundType(List<Sound> sounds, SoundCategory category, Optional<String> subtitle) {
		private static final Map<String, SoundCategory> CATEGORIES = Arrays.stream(SoundCategory.values()).collect(Collectors.toMap(SoundCategory::getName, Function.identity()));
		private static final Codec<SoundCategory> SOUND_CATEGORY_CODEC = Codec.stringResolver(SoundCategory::getName, name -> CATEGORIES.getOrDefault(name.toLowerCase(Locale.ROOT), SoundCategory.NEUTRAL));
		@ApiStatus.Internal
		public static final Codec<SoundType> CODEC = RecordCodecBuilder.create(i -> i.group(
				Sound.CODEC.listOf().fieldOf("sounds").forGetter(SoundType::sounds),
				SOUND_CATEGORY_CODEC.fieldOf("category").forGetter(SoundType::category),
				Codec.STRING.optionalFieldOf("subtitle").forGetter(SoundType::subtitle)
		).apply(i, SoundType::new));
	}

	/**
	 * Represents a single sound file or event entry to be assigned to a {@link SoundEvent}.
	 *
	 * @see {@link net.minecraft.client.sound.Sound}
	 */
	public record Sound(Identifier name, RegistrationType type, float volume, float pitch, int weight, int attenuationDistance, boolean stream, boolean preload) {
		private static final Codec<Sound> MAP_CODEC = RecordCodecBuilder.create(i -> i.group(
				Identifier.CODEC.fieldOf("name").forGetter(Sound::name),
				RegistrationType.CODEC.optionalFieldOf("type", RegistrationType.FILE).forGetter(Sound::type),
				Codec.FLOAT.optionalFieldOf("volume", 1F).forGetter(Sound::volume),
				Codec.FLOAT.optionalFieldOf("pitch", 1F).forGetter(Sound::pitch),
				Codec.INT.optionalFieldOf("weight", 1).forGetter(Sound::weight),
				Codec.INT.optionalFieldOf("attenuation_distance", 16).forGetter(Sound::attenuationDistance),
				Codec.BOOL.optionalFieldOf("stream", false).forGetter(Sound::stream),
				Codec.BOOL.optionalFieldOf("preload", false).forGetter(Sound::preload)
		).apply(i, Sound::new));

		private static final Codec<Sound> STRING_CODEC = Identifier.CODEC.xmap(
				id -> new Sound(id, RegistrationType.FILE, 1F, 1F, 1, 16, false, false),
				Sound::name
		);
		private static final Codec<Sound> CODEC = Codec.xor(STRING_CODEC, MAP_CODEC).xmap(Either::unwrap, sound -> {
			if (sound.type() != RegistrationType.FILE
					|| sound.volume() != 1F
					|| sound.pitch() != 1F
					|| sound.weight() != 1
					|| sound.attenuationDistance() != 16
					|| sound.stream()
					|| sound.preload()) {
				return Either.right(sound);
			}
			return Either.left(sound);
		});

		/**
		 * Creates a builder for constructing a new sound entry.
		 *
		 * @param name The Identifier of the sound file or event this entry must reference.
		 */
		public static Builder builder(RegistrationType type, Identifier name) {
			return new Builder(type, name);
		}

		/**
		 * Creates a builder for constructing a new sound entry.
		 *
		 * @param soundFile The Identifier pointing to the sound file (minus .ogg extension).
		 */
		public static Builder ofFile(Identifier soundFile) {
			Preconditions.checkArgument(soundFile != null, "Sound file/event id must not be null.");
			if (soundFile.getPath().indexOf('.') != -1) {
				LOGGER.warn("Sound file id \"" + soundFile + "\" should not have a file extension and may result in the sound event not playing.");
			}
			return builder(RegistrationType.FILE, soundFile);
		}

		/**
		 * Creates a builder for constructing a new sound entry.
		 *
		 * @param event The sound event this entry must point to.
		 */
		public static Builder ofEvent(SoundEvent event) {
			Preconditions.checkArgument(event != null, "Sound event must not be null.");
			return builder(RegistrationType.SOUND_EVENT, event.id());
		}

		/**
		 * @see {@link net.minecraft.client.sound.Sound.RegistrationType}
		 */
		public enum RegistrationType implements StringIdentifiable {
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

		public static final class Builder {
			private final Identifier name;
			private final RegistrationType type;

			private float volume = 1F;
			private float pitch = 1F;
			private int attenuationDistance = 16;
			private int weight = 1;
			private boolean stream = false;
			private boolean preload = false;

			private Builder(RegistrationType type, Identifier name) {
				this.type = type;
				this.name = name;
			}

			/**
			 * Sets the volume of the sound.
			 *
			 * @param volume The volume.
			 */
			public Builder volume(float volume) {
				Preconditions.checkArgument(volume > 0 && volume <= 1, "Sound volume must be greater than 0 and less than or equal to 1.");
				this.volume = volume;
				return this;
			}

			/**
			 * Sets the pitch of the sound.
			 *
			 * @param pitch The sound's pitch value.
			 * @return
			 */
			public Builder pitch(float pitch) {
				Preconditions.checkArgument(pitch > 0, "Sound pitch must be greater than 0.");
				this.pitch = pitch;
				return this;
			}

			/**
			 * Sets the attenuation block distance of the sound.
			 * <p>
			 * The default attenuation is 16 blocks. Setting it to
			 * higher will cause the sound to be heard from greater distances.
			 */
			public Builder attenuationDistance(int attenuationDistance) {
				this.attenuationDistance = attenuationDistance;
				return this;
			}

			/**
			 * Sets the weight or "chance" that this sound has of playing when
			 * its parent sound event is called upon.
			 * <p>
			 * The default weight is 1.
			 */
			public Builder weight(int weight) {
				Preconditions.checkArgument(weight >= 1, "Sound must have a weight of at least 1.");
				this.weight = weight;
				return this;
			}

			/**
			 * Configures the sound to be streamed.
			 * This is usually set for longer sounds like music disks
			 * to prevent delays when the game attempts playing them
			 */
			public Builder stream(boolean stream) {
				this.stream = stream;
				return this;
			}

			/**
			 * Configures whether the sound must be pre-loaded by the game.
			 * By default sounds are only loaded the first time they're used.
			 * Set preload to <code>true</code> will cause them to be loaded upon game start.
			 *
			 * @param preload
			 */
			public Builder preload(boolean preload) {
				this.preload = preload;
				return this;
			}

			/**
			 * Builds an immutable sound entry with an optional suffix to append to its identifier.
			 */
			@ApiStatus.Internal
			public Sound build(@Nullable String suffix) {
				return new Sound(name.withSuffixedPath(Strings.nullToEmpty(suffix)), type, volume, pitch, weight, attenuationDistance, stream, preload);
			}
		}
	}

}
