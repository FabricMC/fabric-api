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

import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;

/// General purpose Fabric-provided extensions to [net.minecraft.client.resources.sounds.SoundInstance].
///
/// This interface is implicitly implemented on all [net.minecraft.client.resources.sounds.SoundInstance]s via a mixin and interface injection.
public interface FabricSoundInstance {
	/// An empty sound, which may be used as a placeholder in your `sounds.json` file for sounds with custom audio
	/// streams.
	///
	/// @see #getAudioStream(SoundBufferLibrary, Identifier, boolean)
	Identifier EMPTY_SOUND = Identifier.fromNamespaceAndPath("fabric-sound-api-v1", "empty");

	/// Loads the audio stream for this sound.
	///
	/// By default this will load `.ogg` files from active resource packs. It may be overridden to provide a
	/// custom [AudioStream] implementation which provides audio from another source, such as over the network or
	/// driven from user input.
	/// ### Usage Example
	///
	/// Creating a sound with a custom audio stream requires the following:
	///
	/// Firstly, an entry in `sounds.json`. The name can be set to any sound (though it is recommended to use
	/// the dummy [#EMPTY_SOUND]), and the "stream" property set to true:
	///
	/// ```json
	/// {"custom_sound":{"sounds": [{"name": "fabric-sound-api-v1:empty", "stream": true}]}}
	/// ```
	///
	/// You should then define your own implementation of [AudioStream], which provides audio data to the sound
	/// engine.
	///
	/// Finally, you'll need an implementation of [net.minecraft.client.resources.sounds.SoundInstance] which overrides [#getAudioStream] to
	/// return your custom implementation. [net.minecraft.client.resources.sounds.SoundInstance#getSound()] should return the newly-added entry in
	/// `sounds.json`.
	///
	/// ```java
	/// class CustomSound extends AbstractSoundInstance {
	/// 	CustomSound() {
	/// 		// Use the sound defined in sounds.json
	/// 		super(Identifier.fromNamespaceAndPath("modid", "custom_sound"), SoundSource.BLOCKS, net.minecraft.client.resources.sounds.SoundInstance.createUnseededRandom());
	/// 	}
	///
	/// 	CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary library, Identifier id, boolean repeatInstantly) {
	/// 		// Return your custom AudioStream implementation.
	/// 		return CompletableFuture.completedFuture(new CustomStream());
	/// 	}
	/// }
	/// ```
	///
	/// @param library         The default buffer library, capable of loading `.ogg` files.
	/// @param id              The resolved sound ID, equal to [net.minecraft.client.resources.sounds.SoundInstance#getSound()]'s location.
	/// @param repeatInstantly Whether this sound should loop. This is true when the sound
	///                        {@linkplain net.minecraft.client.resources.sounds.SoundInstance#isLooping() is repeatable} and has
	///                        {@linkplain net.minecraft.client.resources.sounds.SoundInstance#getDelay() no delay}.
	/// @return the loaded audio stream
	default CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary library, Identifier id, boolean repeatInstantly) {
		return library.getStream(id, repeatInstantly);
	}
}
