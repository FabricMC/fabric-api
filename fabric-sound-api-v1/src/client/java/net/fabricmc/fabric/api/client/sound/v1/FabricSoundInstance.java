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

package net.fabricmc.fabric.api.client.sound.v1;

import java.util.concurrent.CompletableFuture;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;

/**
 * General purpose Fabric-provided extensions to {@link SoundInstance}.
 *
 * <p>This interface is implicitly implemented on all {@link SoundInstance}s via a mixin and interface injection.
 */
public interface FabricSoundInstance {
	/**
	 * An empty sound, which may be used as a placeholder in your {@code sounds.json} file for sounds with custom audio
	 * streams.
	 *
	 * @see #getAudioStream(SoundBufferLibrary, Identifier, boolean)
	 */
	Identifier EMPTY_SOUND = Identifier.fromNamespaceAndPath("fabric-sound-api-v1", "empty");

	/**
	 * Loads the audio stream for this sound.
	 *
	 * <p>By default this will load {@code .ogg} files from active resource packs. It may be overridden to provide a
	 * custom {@link AudioStream} implementation which provides audio from another source, such as over the network or
	 * driven from user input.
	 *
	 * <h3>Usage Example</h3>
	 *
	 * <p>Creating a sound with a custom audio stream requires the following:
	 *
	 * <p>Firstly, an entry in {@code sounds.json}. The name can be set to any sound (though it is recommended to use
	 * the dummy {@link #EMPTY_SOUND}), and the "stream" property set to true:
	 *
	 * <pre>{@code
	 * {
	 *   "custom_sound": {"sounds": [{"name": "fabric-sound-api-v1:empty", "stream": true}]}
	 * }
	 * }</pre>
	 *
	 * <p>You should then define your own implementation of {@link AudioStream}, which provides audio data to the sound
	 * engine.
	 *
	 * <p>Finally, you'll need an implementation of {@link SoundInstance} which overrides {@link #getAudioStream} to
	 * return your custom implementation. {@link SoundInstance#getSound()} should return the newly-added entry in
	 * {@code sounds.json}.
	 *
	 * <pre>{@code
	 * class CustomSound extends AbstractSoundInstance {
	 *     CustomSound() {
	 *         // Use the sound defined in sounds.json
	 *         super(Identifier.fromNamespaceAndPath("modid", "custom_sound"), SoundCategory.BLOCKS, SoundInstance.createRandom());
	 *     }
	 *
	 *     @Override
	 *     public CompletableFuture<AudioStream> getAudioStream(SoundLoader loader, Identifier id, boolean repeatInstantly) {
	 *         // Return your custom AudioStream implementation.
	 *         return CompletableFuture.completedFuture(new CustomStream());
	 *     }
	 * }
	 * }</pre>
	 *
	 * @param loader          The default sound loader, capable of loading {@code .ogg} files.
	 * @param id              The resolved sound ID, equal to {@link SoundInstance#getSound()}'s location.
	 * @param repeatInstantly Whether this sound should loop. This is true when the sound
	 *                        {@linkplain SoundInstance#isLooping()}  is repeatable} and has
	 *                        {@linkplain SoundInstance#getDelay()}  no delay}.
	 * @return the loaded audio stream
	 */
	default CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, Identifier id, boolean repeatInstantly) {
		return loader.getStream(id, repeatInstantly);
	}
}
