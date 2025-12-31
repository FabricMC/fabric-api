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

package net.fabricmc.fabric.api.object.builder.v1.block.type;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

/// This class allows easy creation of [WoodType]s.
///
/// A [WoodType] is used to tell the game what textures signs should use, as well as sounds for both signs and fence gates.
///
/// Regular sign textures are stored at `[namespace]/textures/entity/signs/[path].png`.
///
///Hanging sign textures are stored at `[namespace]/textures/entity/signs/hanging/[path].png`.
///
/// @see BlockSetTypeBuilder
public final class WoodTypeBuilder {
	private SoundType soundType = SoundType.WOOD;
	private SoundType hangingSignSoundType = SoundType.HANGING_SIGN;
	private SoundEvent fenceGateCloseSound = SoundEvents.FENCE_GATE_CLOSE;
	private SoundEvent fenceGateOpenSound = SoundEvents.FENCE_GATE_OPEN;

	/// Sets this wood type's sound type.
	///
	/// Defaults to [SoundType#WOOD].
	///
	/// @return this builder for chaining
	public WoodTypeBuilder soundType(SoundType soundType) {
		this.soundType = soundType;
		return this;
	}

	/// Sets this wood type's hanging sign sound type.
	///
	/// Defaults to [SoundType#HANGING_SIGN].
	///
	/// @return this builder for chaining
	public WoodTypeBuilder hangingSignSoundType(SoundType hangingSignSoundType) {
		this.hangingSignSoundType = hangingSignSoundType;
		return this;
	}

	/// Sets this wood type's fence gate close sound.
	///
	/// Defaults to [SoundEvents#FENCE_GATE_CLOSE].
	///
	/// @return this builder for chaining
	public WoodTypeBuilder fenceGateCloseSound(SoundEvent fenceGateCloseSound) {
		this.fenceGateCloseSound = fenceGateCloseSound;
		return this;
	}

	/// Sets this wood type's fence gate open sound.
	///
	/// Defaults to [SoundEvents#FENCE_GATE_OPEN].
	///
	/// @return this builder for chaining
	public WoodTypeBuilder fenceGateOpenSound(SoundEvent fenceGateOpenSound) {
		this.fenceGateOpenSound = fenceGateOpenSound;
		return this;
	}

	/// Creates a new [WoodTypeBuilder] that copies all of another builder's values.
	///
	/// @param builder the [WoodTypeBuilder] whose values are to be copied
	///
	/// @return the created copy
	public static WoodTypeBuilder copyOf(WoodTypeBuilder builder) {
		WoodTypeBuilder copy = new WoodTypeBuilder();
		copy.soundType(builder.soundType);
		copy.hangingSignSoundType(builder.hangingSignSoundType);
		copy.fenceGateCloseSound(builder.fenceGateCloseSound);
		copy.fenceGateOpenSound(builder.fenceGateOpenSound);
		return copy;
	}

	/// Creates a new [WoodTypeBuilder] that copies all of another wood type's values.
	///
	/// @param woodType the [WoodType] whose values are to be copied
	///
	/// @return the created copy
	public static WoodTypeBuilder copyOf(WoodType woodType) {
		WoodTypeBuilder copy = new WoodTypeBuilder();
		copy.soundType(woodType.soundType());
		copy.hangingSignSoundType(woodType.hangingSignSoundType());
		copy.fenceGateCloseSound(woodType.fenceGateClose());
		copy.fenceGateOpenSound(woodType.fenceGateOpen());
		return copy;
	}

	/// Builds and registers a [WoodType] from this builder's values.
	///
	/// Alternatively, you can use [#build(Identifier, BlockSetType)] to build without registering.
	///
	///Then [WoodType#register(WoodType)] can be used to register it later.
	///
	/// @param id the id for the built [WoodType]
	/// @param setType the [BlockSetType] for the built [WoodType]
	///
	/// @return the built and registered [WoodType]
	public WoodType register(Identifier id, BlockSetType setType) {
		return WoodType.register(this.build(id, setType));
	}

	/// Builds a [WoodType] from this builder's values without registering it.
	///
	/// Use [WoodType#register(WoodType)] to register it later.
	///
	///Alternatively, you can use [#register(Identifier, BlockSetType)] to build and register it now.
	///
	/// @param id the id for the built [WoodType]
	/// @param setType the [BlockSetType] for the built [WoodType]
	///
	/// @return the built [WoodType]
	public WoodType build(Identifier id, BlockSetType setType) {
		return new WoodType(id.toString(), setType,
				soundType,
				hangingSignSoundType, fenceGateCloseSound, fenceGateOpenSound);
	}
}
