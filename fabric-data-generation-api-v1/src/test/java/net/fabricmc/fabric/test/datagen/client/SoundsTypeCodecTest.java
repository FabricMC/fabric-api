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

package net.fabricmc.fabric.test.datagen.client;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.client.resources.sounds.SoundEventRegistrationSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;

import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.impl.datagen.client.SoundTypeBuilderImpl;

public class SoundsTypeCodecTest {
	/**
	 * Codec copied from {@link net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider} to use in testing, as it is not accessible.
	 */
	private static final Codec<Map<String, SoundTypeBuilderImpl.SoundType>> CODEC =
			Codec.unboundedMap(Codec.STRING, SoundTypeBuilderImpl.SoundType.CODEC);
	/**
	 * Gson copied from {@link net.minecraft.client.sounds.SoundManager} to use in testing, as it is not accessible.
	 */
	private static final Gson GSON = new GsonBuilder().registerTypeAdapter(SoundEventRegistration.class,
			new SoundEventRegistrationSerializer()).create();
	/**
	 * Type token copied from {@link net.minecraft.client.sounds.SoundManager} to use in testing, as it is not accessible.
	 */
	private static final TypeToken<Map<String, SoundEventRegistration>> SOUND_EVENT_REGISTRATION_TYPE = new TypeToken<>() { };

	private static final Identifier IDENTIFIER =
			Identifier.fromNamespaceAndPath("datagen-test", "sound-event-registrable-codec");

	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	public void soundsTypeCodec1() {
		SoundTypeBuilder builder = SoundTypeBuilder.of(SoundEvents.ANVIL_USE)
				.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.withDefaultNamespace("mob/parrot/idle"))
						.volume(0.7F), 1)
				.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.withDefaultNamespace("mob/parrot/idle2")))
				.sound(SoundTypeBuilder.EntryBuilder.ofEvent(SoundEvents.ANVIL_HIT)
						.weight(100))
				.sound(SoundTypeBuilder.EntryBuilder.ofEvent(SoundEvents.ARMOR_EQUIP_GENERIC))
				.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.withDefaultNamespace("mob/parrot/idle"))
						.volume(0.3F).pitch(0.5F).stream(true).preload(true).attenuationDistance(8)
				).replace(true);

		final Map<String, SoundTypeBuilderImpl.SoundType> data =
				Map.of(IDENTIFIER.getPath(), ((SoundTypeBuilderImpl) builder).build());

		expectInputDataInOutput(data, process(data));
	}

	@Test
	public void soundsTypeCodec2() {
		SoundTypeBuilder builder = SoundTypeBuilder.of()
				.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.withDefaultNamespace("mob/creeper/hurt"))
						.volume(1.0F).pitch(2.0F))
				.sound(SoundTypeBuilder.EntryBuilder.ofEvent(SoundEvents.STONE_BREAK)
						.weight(1))
				.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.withDefaultNamespace("block/beacon/power"))
						.volume(Float.MIN_VALUE).pitch(0.5F).stream(false).preload(false).attenuationDistance(0)
				);

		final Map<String, SoundTypeBuilderImpl.SoundType> data =
				Map.of(IDENTIFIER.getPath(), ((SoundTypeBuilderImpl) builder).build());

		expectInputDataInOutput(data, process(data));
	}

	@Test
	public void soundsTypeCodec3() {
		SoundTypeBuilder builder = SoundTypeBuilder.of()
				.subtitle("super_subtitle")
				.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.withDefaultNamespace("sound")));

		final Map<String, SoundTypeBuilderImpl.SoundType> data =
				Map.of(IDENTIFIER.getPath(), ((SoundTypeBuilderImpl) builder).build());

		expectInputDataInOutput(data, process(data));
	}

	/**
	 * Test if the output data has all values present in the input data.
	 *
	 * @param inputData Sounds input data used for data generation.
	 * @param outputData Sounds output data interpreted from sounds file.
	 */
	private static void expectInputDataInOutput(Map<String, SoundTypeBuilderImpl.SoundType> inputData,
												Map<String, SoundEventRegistration> outputData) {
		for (String identifier : inputData.keySet()) {
			SoundEventRegistration soundEventRegistration = outputData.get(identifier);
			Assertions.assertNotNull(soundEventRegistration);

			SoundTypeBuilderImpl.SoundType soundType = inputData.get(identifier);

			Assertions.assertEquals(soundType.replace(), soundEventRegistration.isReplace());
			Assertions.assertEquals(soundType.subtitle().orElse(null), soundEventRegistration.getSubtitle());

			List<SoundTypeBuilderImpl.Entry> entryList = soundType.sounds();
			List<Sound> soundList = soundEventRegistration.getSounds();
			Assertions.assertEquals(entryList.size(), soundList.size());

			for (int i = 0; i < entryList.size(); i++) {
				SoundTypeBuilderImpl.Entry entry = entryList.get(i);
				Sound sound = soundList.get(i);
				expectInputDataInOutput(entry, sound);
			}
		}
	}

	/**
	 * Test if the output data has all values present in the input data.
	 *
	 * @param entry Entry used to represent sound for data generation.
	 * @param sound Sound interpreted from sounds file.
	 */
	private static void expectInputDataInOutput(SoundTypeBuilderImpl.Entry entry, Sound sound) {
		Assertions.assertEquals(entry.name(), sound.getLocation());
		Assertions.assertEquals(entry.type().name(), sound.getType().name());
		Assertions.assertEquals(entry.stream(), sound.shouldStream());
		Assertions.assertEquals(entry.preload(), sound.shouldPreload());
		Assertions.assertEquals(entry.attenuationDistance(), sound.getAttenuationDistance());
		Assertions.assertEquals(entry.weight(), sound.getWeight());
		Assertions.assertEquals(entry.volume(), sound.getVolume().sample(RandomSource.create()));
		Assertions.assertEquals(entry.pitch(), sound.getPitch().sample(RandomSource.create()));
	}

	/**
	 * Generate and interpret data like the sounds provider and sounds manager respectively.
	 *
	 * @see net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider
	 * @see net.minecraft.client.sounds.SoundManager
	 */
	private Map<String, SoundEventRegistration> process(Map<String, SoundTypeBuilderImpl.SoundType> data) {
		// Generate json element, matching the codec from fabric sounds provider.
		DataResult<JsonElement> result = CODEC.encodeStart(JsonOps.INSTANCE, data);

		// Interpret json data, matching the Gson and type from sound manager.
		return GSON.fromJson(result.getOrThrow(), SOUND_EVENT_REGISTRATION_TYPE);
	}
}
