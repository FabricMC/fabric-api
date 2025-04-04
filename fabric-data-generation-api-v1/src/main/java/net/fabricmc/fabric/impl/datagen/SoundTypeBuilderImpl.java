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

package net.fabricmc.fabric.impl.datagen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.include.com.google.common.base.Preconditions;

import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.datagen.v1.builder.SoundTypeBuilder;

@ApiStatus.Internal
public final class SoundTypeBuilderImpl implements SoundTypeBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(FabricDataGenHelper.class);

	private SoundCategory category = SoundCategory.NEUTRAL;
	@Nullable
	private String subtitle;
	private final List<Entry> sounds = new ArrayList<>();

	public SoundTypeBuilderImpl() { }

	@Override
	public SoundTypeBuilder category(SoundCategory category) {
		Preconditions.checkArgument(category != null, "Sound event category must not be null.");
		this.category = category;
		return this;
	}

	@Override
	public SoundTypeBuilder subtitle(@Nullable String subtitle) {
		this.subtitle = subtitle;
		return this;
	}

	@Override
	public SoundTypeBuilder sound(EntryBuilder sound) {
		Preconditions.checkArgument(sound != null, "Sound must not be null.");
		sounds.add(((EntryBuilderImpl)sound).build(""));
		return this;
	}

	@Override
	public SoundTypeBuilder sound(EntryBuilder sound, int count) {
		Preconditions.checkArgument(sound != null, "Sound must not be null.");
		Preconditions.checkArgument(count > 0, "Count must be greater than zero.");
		for (int i = 1; i <= count; i++) {
			sounds.add(((EntryBuilderImpl)sound).build("" + i));
		}
		return this;
	}

	public SoundType build() {
		Preconditions.checkState(!sounds.isEmpty(), "Sound definition must have at least one sound file");
		for (Entry sound : sounds) {
			if (sound.type() == RegistrationType.SOUND_EVENT) {
				Registries.SOUND_EVENT.getOptionalValue(sound.name()).orElseThrow(() -> new IllegalStateException("References sound event " + sound.name() + " does not exist"));
			}
		}

		return new SoundType(sounds, category, Optional.ofNullable(subtitle));
	}

	@ApiStatus.Internal
	public record SoundType(List<Entry> sounds, SoundCategory category, Optional<String> subtitle) {
		private static final Map<String, SoundCategory> CATEGORIES = Arrays.stream(SoundCategory.values()).collect(Collectors.toMap(SoundCategory::getName, Function.identity()));
		private static final Codec<SoundCategory> SOUND_CATEGORY_CODEC = Codec.stringResolver(SoundCategory::getName, name -> CATEGORIES.getOrDefault(name.toLowerCase(Locale.ROOT), SoundCategory.NEUTRAL));
		@ApiStatus.Internal
		public static final Codec<SoundType> CODEC = RecordCodecBuilder.create(i -> i.group(
				Entry.CODEC.listOf().fieldOf("sounds").forGetter(SoundType::sounds),
				SOUND_CATEGORY_CODEC.fieldOf("category").forGetter(SoundType::category),
				Codec.STRING.optionalFieldOf("subtitle").forGetter(SoundType::subtitle)
		).apply(i, SoundType::new));
	}

	@ApiStatus.Internal
	private record Entry(Identifier name, RegistrationType type, float volume, float pitch, int weight, int attenuationDistance, boolean stream, boolean preload) {
		private static final Codec<Entry> MAP_CODEC = RecordCodecBuilder.create(i -> i.group(
				Identifier.CODEC.fieldOf("name").forGetter(Entry::name),
				RegistrationType.CODEC.optionalFieldOf("type", RegistrationType.FILE).forGetter(Entry::type),
				Codec.FLOAT.optionalFieldOf("volume", 1F).forGetter(Entry::volume),
				Codec.FLOAT.optionalFieldOf("pitch", 1F).forGetter(Entry::pitch),
				Codec.INT.optionalFieldOf("weight", 1).forGetter(Entry::weight),
				Codec.INT.optionalFieldOf("attenuation_distance", 16).forGetter(Entry::attenuationDistance),
				Codec.BOOL.optionalFieldOf("stream", false).forGetter(Entry::stream),
				Codec.BOOL.optionalFieldOf("preload", false).forGetter(Entry::preload)
		).apply(i, Entry::new));

		private static final Codec<Entry> STRING_CODEC = Identifier.CODEC.xmap(
				id -> new Entry(id, RegistrationType.FILE, 1F, 1F, 1, 16, false, false),
				Entry::name
		);
		private static final Codec<Entry> CODEC = Codec.xor(STRING_CODEC, MAP_CODEC).xmap(Either::unwrap, sound -> {
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
	}

	@ApiStatus.Internal
	public static final class EntryBuilderImpl implements EntryBuilder {
		private final Identifier name;
		private final RegistrationType type;

		private float volume = 1F;
		private float pitch = 1F;
		private int attenuationDistance = 16;
		private int weight = 1;
		private boolean stream = false;
		private boolean preload = false;

		private EntryBuilderImpl(RegistrationType type, Identifier name) {
			this.type = type;
			this.name = name;
		}

		public static EntryBuilder builder(RegistrationType type, Identifier name) {
			return new EntryBuilderImpl(type, name);
		}

		public static EntryBuilder ofFile(Identifier soundFile) {
			Preconditions.checkArgument(soundFile != null, "Sound file/event id must not be null.");
			if (soundFile.getPath().indexOf('.') != -1) {
				LOGGER.warn("Sound file id \"" + soundFile + "\" should not have a file extension and may result in the sound event not playing.");
			}
			return builder(RegistrationType.FILE, soundFile);
		}

		public static EntryBuilder ofEvent(SoundEvent event) {
			Preconditions.checkArgument(event != null, "Sound event must not be null.");
			return builder(RegistrationType.SOUND_EVENT, event.id());
		}

		@Override
		public EntryBuilder volume(float volume) {
			Preconditions.checkArgument(volume > 0 && volume <= 1, "Sound volume must be greater than 0 and less than or equal to 1.");
			this.volume = volume;
			return this;
		}

		@Override
		public EntryBuilder pitch(float pitch) {
			Preconditions.checkArgument(pitch > 0, "Sound pitch must be greater than 0.");
			this.pitch = pitch;
			return this;
		}

		@Override
		public EntryBuilder attenuationDistance(int attenuationDistance) {
			this.attenuationDistance = attenuationDistance;
			return this;
		}

		@Override
		public EntryBuilder weight(int weight) {
			Preconditions.checkArgument(weight >= 1, "Sound must have a weight of at least 1.");
			this.weight = weight;
			return this;
		}

		@Override
		public EntryBuilder stream(boolean stream) {
			this.stream = stream;
			return this;
		}

		@Override
		public EntryBuilder preload(boolean preload) {
			this.preload = preload;
			return this;
		}

		public Entry build(@Nullable String suffix) {
			return new Entry(name.withSuffixedPath(Strings.nullToEmpty(suffix)), type, volume, pitch, weight, attenuationDistance, stream, preload);
		}
	}
}
