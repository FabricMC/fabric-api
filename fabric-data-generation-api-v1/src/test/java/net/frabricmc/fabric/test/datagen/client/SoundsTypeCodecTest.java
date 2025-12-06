package net.frabricmc.fabric.test.datagen.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.impl.datagen.client.SoundTypeBuilderImpl;

import net.minecraft.SharedConstants;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.client.resources.sounds.SoundEventRegistrationSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.sounds.SoundEvents;

import net.minecraft.util.RandomSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class SoundsTypeCodecTest {
	/**
	 * {@link net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider#CODEC}
	 */
	private static final Codec<Map<String, SoundTypeBuilderImpl.SoundType>> CODEC =
			Codec.unboundedMap(Codec.STRING, SoundTypeBuilderImpl.SoundType.CODEC);
	/**
	 * {@link net.minecraft.client.sounds.SoundManager#GSON}
	 */
	final Gson GSON = new GsonBuilder().registerTypeAdapter(SoundEventRegistration.class,
			new SoundEventRegistrationSerializer()).create();
	/**
	 * {@link net.minecraft.client.sounds.SoundManager#SOUND_EVENT_REGISTRATION_TYPE}
	 */
	final TypeToken<Map<String, SoundEventRegistration>> SOUND_EVENT_REGISTRATION_TYPE = new TypeToken<>() {};

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
