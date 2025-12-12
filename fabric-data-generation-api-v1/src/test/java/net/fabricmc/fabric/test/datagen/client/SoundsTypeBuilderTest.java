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
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.sounds.SoundEvents;

import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.impl.datagen.client.SoundTypeBuilderImpl;

public class SoundsTypeBuilderTest {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	public void buildSoundType1() {
		SoundTypeBuilderImpl.SoundType expected = new SoundTypeBuilderImpl.SoundType(
				List.of(
						new SoundTypeBuilderImpl.Entry(Identifier.withDefaultNamespace("mob/parrot/idle1"),
								SoundTypeBuilder.RegistrationType.FILE, 0.7F, 1.0F, 1,
								16, false, false),
						new SoundTypeBuilderImpl.Entry(Identifier.withDefaultNamespace("mob/parrot/idle2"),
								SoundTypeBuilder.RegistrationType.FILE, 1.0F, 1.0F, 1,
								16, false, false),
						new SoundTypeBuilderImpl.Entry(SoundEvents.ANVIL_HIT.location(),
								SoundTypeBuilder.RegistrationType.SOUND_EVENT, 1.0F, 1.0F, 100,
								16, false, false),
						new SoundTypeBuilderImpl.Entry(SoundEvents.ARMOR_EQUIP_GENERIC.value().location(),
								SoundTypeBuilder.RegistrationType.SOUND_EVENT, 1.0F, 1.0F, 1,
								16, false, false),
						new SoundTypeBuilderImpl.Entry(Identifier.withDefaultNamespace("mob/parrot/idle"),
								SoundTypeBuilder.RegistrationType.FILE, 0.3F, 0.5F, 1,
								8, true, true)
				),
				true,
				Optional.of("subtitles.minecraft.block.anvil.use")
		);

		SoundTypeBuilderImpl.SoundType soundType = ((SoundTypeBuilderImpl) SoundTypeBuilder.of(SoundEvents.ANVIL_USE)
				.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.withDefaultNamespace("mob/parrot/idle"))
						.volume(0.7F), 1)
				.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.withDefaultNamespace("mob/parrot/idle2")))
				.sound(SoundTypeBuilder.EntryBuilder.ofEvent(SoundEvents.ANVIL_HIT)
						.weight(100))
				.sound(SoundTypeBuilder.EntryBuilder.ofEvent(SoundEvents.ARMOR_EQUIP_GENERIC))
				.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.withDefaultNamespace("mob/parrot/idle"))
						.volume(0.3F).pitch(0.5F).stream(true).preload(true).attenuationDistance(8)
				).replace(true)).build();

		soundTypeEquals(expected, soundType);
	}

	@Test
	public void buildSoundType2() {
		SoundTypeBuilderImpl.SoundType expected = new SoundTypeBuilderImpl.SoundType(
				List.of(
						new SoundTypeBuilderImpl.Entry(Identifier.withDefaultNamespace("mob/creeper/hurt"),
								SoundTypeBuilder.RegistrationType.FILE, 1.0F, 2.0F, 1,
								16, false, false),
						new SoundTypeBuilderImpl.Entry(SoundEvents.STONE_BREAK.location(),
								SoundTypeBuilder.RegistrationType.SOUND_EVENT, 1.0F, 1.0F, 1,
								16, false, false),
						new SoundTypeBuilderImpl.Entry(Identifier.withDefaultNamespace("block/beacon/power"),
								SoundTypeBuilder.RegistrationType.FILE, Float.MIN_VALUE, 0.5F, 1,
								0, false, false)
				),
				false,
				Optional.empty()
		);

		SoundTypeBuilderImpl.SoundType soundType = ((SoundTypeBuilderImpl) SoundTypeBuilder.of()
				.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.withDefaultNamespace("mob/creeper/hurt"))
						.volume(1.0F).pitch(2.0F))
				.sound(SoundTypeBuilder.EntryBuilder.ofEvent(SoundEvents.STONE_BREAK)
						.weight(1))
				.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.withDefaultNamespace("block/beacon/power"))
						.volume(Float.MIN_VALUE).pitch(0.5F).stream(false).preload(false).attenuationDistance(0)
				)).build();

		soundTypeEquals(expected, soundType);
	}

	@Test
	public void buildSoundType3() {
		SoundTypeBuilderImpl.SoundType expected = new SoundTypeBuilderImpl.SoundType(
				List.of(
						new SoundTypeBuilderImpl.Entry(Identifier.withDefaultNamespace("sound"),
								SoundTypeBuilder.RegistrationType.FILE, 1.0F, 1.0F, 1,
								16, false, false)
				),
				false,
				Optional.of("super_subtitle")
		);

		SoundTypeBuilderImpl.SoundType soundType = ((SoundTypeBuilderImpl) SoundTypeBuilder.of()
				.subtitle("super_subtitle")
				.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.withDefaultNamespace("sound")))).build();

		soundTypeEquals(expected, soundType);
	}

	/**
	 * Assert that the expected and specified sound type equal.
	 *
	 * @param expected sound type to be expected
	 * @param soundType sound type to assert against
	 */
	public void soundTypeEquals(SoundTypeBuilderImpl.SoundType expected, SoundTypeBuilderImpl.SoundType soundType) {
		Assertions.assertEquals(expected.subtitle(), soundType.subtitle());
		Assertions.assertEquals(expected.replace(), soundType.replace());
		Assertions.assertEquals(expected.sounds().size(), soundType.sounds().size());

		for (int i = 0; i < expected.sounds().size(); i++) {
			SoundTypeBuilderImpl.Entry expectedEntry = expected.sounds().get(i);
			SoundTypeBuilderImpl.Entry entry = soundType.sounds().get(i);
			entryEquals(expectedEntry, entry);
		}
	}

	/**
	 * Assert that all fields of the expected and specified entry equal each other.
	 *
	 * @param expected entry with fields to be expected
	 * @param entry entry to assert against
	 */
	public void entryEquals(SoundTypeBuilderImpl.Entry expected, SoundTypeBuilderImpl.Entry entry) {
		Assertions.assertEquals(expected.name().getNamespace(), entry.name().getNamespace());
		Assertions.assertEquals(expected.type().name(), entry.type().name());
		Assertions.assertEquals(expected.stream(), entry.stream());
		Assertions.assertEquals(expected.preload(), entry.preload());
		Assertions.assertEquals(expected.attenuationDistance(), entry.attenuationDistance());
		Assertions.assertEquals(expected.weight(), entry.weight());
		Assertions.assertEquals(expected.volume(), entry.volume());
		Assertions.assertEquals(expected.pitch(), entry.pitch());
	}
}
